package cmu.edu.gtfs_realtime_processor.avl;


import java.util.logging.Logger;
import static java.util.logging.Level.*;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.Stop;

import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import com.amazonaws.services.sqs.model.Message;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import cmu.edu.gtfs_realtime_processor.util.UrlUtil;
import cmu.edu.gtfs_realtime_processor.util.TimeUtil;
import cmu.edu.gtfs_realtime_processor.util.GtfsUtil;

public class ObservationProcessor {
    private static TiramisuDb propertiesDb = TiramisuDb.getDb("Properties");
    private static JSONObject properties = new JSONObject(propertiesDb.get("ObservationProcessor", null))
        .getJSONObject("properties");
	
    private static final double THRESHOLD = properties.getDouble("THRESHOLD");
    private static final int NUM_STOPS = properties.getInt("NUM_STOPS");
    public static final int DELAY = properties.getInt("DELAY");

    private static Logger logger = Logger.getLogger(ObservationProcessor.class.getName());

    private static GtfsInfo PAACInfo = GtfsInfo.getAgencyInfo("PAAC");
    private static GtfsInfo MTAInfo = GtfsInfo.getAgencyInfo("MTA");

    private static FeedQueue q = FeedQueue.getQueue();
    private static TiramisuDb realtimeObservationDb = TiramisuDb.getDb("RealtimeObservationsTable");
    private static TiramisuDb shapeDb = TiramisuDb.getDb("Shape");

    private static long time;
	private static long timeTotal;
	private static int totalMessages = 0;
	

    public static void main(String[] args) {
        logger.info("RUNNING PROCESSOR");
        logger.log(INFO, "Getting from queue @ " + TimeUtil.getTime());
        realtimeObservationDb.removeAllData();
        logger.log(INFO, "All data have been removed...");
        while (true)  {
        	time = System.currentTimeMillis();
			timeTotal = time;
            Message m = q.getMessage();
            if (m == null) {
                logger.info("Nothing in queue");
                try {
                    Thread.sleep(100);  // DELAY
                } catch (InterruptedException e) {
                    logger.log(WARNING, "Interrupted exception");
                }
            } else {
                q.deleteMessage(m);
                try {
                    processObservation(new JSONObject(m.getBody()));                	
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            logger.info("Total time: " + ((System.currentTimeMillis() - timeTotal)) + " and total messages: " + totalMessages);
			totalMessages = 0;
        }
    }
    
    public static void printTime(String label) {
		logger.info("Time for (" + label + ") is: " + ((System.currentTimeMillis() - time)));
		time = System.currentTimeMillis();
	}
    

    public static void processObservation(JSONObject obs) throws JSONException {
        String routeId = obs.getString("route");
        String agency = obs.getString("agency");
        JSONObject vehicles = obs.getJSONObject("vehicles");
        Iterator<String> vids = vehicles.keys();
        printTime("step 1");

        while (vids.hasNext()) {
            String vid = vids.next();
            JSONObject vehicleData = vehicles.getJSONObject(vid);
            String curLat = vehicleData.getString("lat");
            String curLon = vehicleData.getString("lon");
            logger.fine("lat: " + curLat);
            logger.fine("lon: " + curLon);
            GtfsInfo info = null;

            switch (agency) {
            case "PAAC":
                {
                    info = PAACInfo;
                    break;
                }
            case "MTA":
                {
                    info = MTAInfo;
                    break;
                }
            }
            updatePredictions(vehicleData, info, agency);
            totalMessages++;
            printTime("step 2");
        }
        updateObservation(routeId, agency, vehicles);
        printTime("step 3");
    }

    
    private static void updatePredictions(JSONObject vehicleData, GtfsInfo info, String agency) throws JSONException {
    	try {
	        String curTripId = vehicleData.getString("tatripid");
	        Trip trip = info.getTrip(curTripId);
	        if(trip == null) {
	        	logger.severe("No trip found in the static GTFS for trip id: " + curTripId);
	            return;
	        }
	        String route = GtfsUtil.extractPAACRealtimeId(trip.getRoute().getId().getId());
	        Collection<StopTime> stops = info.getStops(GtfsUtil.routeTripId(route, curTripId));
	        if (stops == null){
	            logger.severe("No stops found for route: " + route + " trip: " + curTripId);
	            return;
	        }
	        JSONArray predictions = null;
	        try {
	            predictions = vehicleData.getJSONArray("predictions");
	        }catch(JSONException e) {
	            logger.info("shape data needs to be used for " + vehicleData.toString());
	        }
	        StopTime curStop = null;
	        long realTime = 0;
	        int curNum = 0; //curNum is the number of stops that exists in scheduled data and have realtime
	        if (predictions!=null && predictions.length() > 0) {
	            //in this case we have predictions for some stops, no need for shape data
	            curNum = predictions.length();
	            //find the last stop in predictions that have both scheduled and real time
	            int index = curNum-1;
	            JSONObject curStopJSON = predictions.getJSONObject(index);
	            while (curStop==null && index>=0) {
	                curNum--;
	                curStopJSON = predictions.getJSONObject(index);
	                String curStopId = curStopJSON.getString("stop-id");
	                curStop = info.getStopTime(GtfsUtil.tripStopId(curTripId, curStopId));
	                index--;
	            }
				
	            if (curStop == null) {
	                logger.severe("Trip stop pair not found for all stops");
	                return;
	            }
				
	            //realTime is the departure time of the last stop on the trip that have both 
	            realTime = Long.parseLong(curStopJSON.getString("departure-time"));
				
	            int len = curNum;
	            //this for-loop is used to handle the case when predictions contain stops that don't have realtime
	            for (int i=0; i<len; i++) {
	                curStopJSON = predictions.getJSONObject(i);
	                //departure time is -1, indicating that no realtime
	                if (curStopJSON.getString("departure-time").compareTo("-1")==0) {
	                    long curScheduledTime;
	                    try {
	                        curScheduledTime = info.getStopTime
	                            (GtfsUtil.tripStopId(curTripId, curStopJSON.getString("stop-id"))).getDepartureTime();
	                    }catch(Exception e) {
	                        //this stop doesn't exist in scheduled data, no need to process it
	                        curNum--;
	                        continue;
	                    }
	                    int next = i+1;
	                    int pre = i-1;
	                    //this while loops aims to find the nearest stop with real time
	                    //around the current stop without real time
	                    while (next<len || pre>=0) {
	                        //first see if stops before the current one has real time
	                        if (pre>=0){
	                            JSONObject preStopJSON = predictions.getJSONObject(pre);
	                            long preRealTime = Long.parseLong(preStopJSON.getString("departure-time"));
	                            try {
	                                long preScheduledTime = info.getStopTime(GtfsUtil.tripStopId(curTripId, 
	                                                                                             preStopJSON.getString("stop-id"))).getDepartureTime();
	                                curStopJSON.put("departure-time", Long.toString(curScheduledTime+(preRealTime-preScheduledTime)));
	                                break;
	                            }catch (Exception e) {
	                                //previous stop failed, try next stop
	                            }
	                        }else{
	                            JSONObject nextStopJSON = predictions.getJSONObject(next);
	                            long nextRealTime = Long.parseLong(nextStopJSON.getString("departure-time"));
	                            try {
	                                long nextScheduledTime = info.getStopTime(GtfsUtil.tripStopId(curTripId,
	                                                                                              nextStopJSON.getString("stop-id"))).getDepartureTime();
	                                curStopJSON.put("departure-time", Long.toString(curScheduledTime+(nextRealTime-nextScheduledTime)));
	                                break;
	                            }catch(Exception e) {
	                                //neither pre nor next stop has scheduled data, need to try more stops
	                            }
	                        }
	                        next++;
	                        pre--;
	                    }
	                }
	            }
	        }else{
	            //here we have no predictions at all, requiring us to use shape data
	            double curLat = Double.parseDouble(vehicleData.getString("lat"));
	            double curLon = Double.parseDouble(vehicleData.getString("lon"));
	            String shapeId = trip.getShapeId().getId();
	            JSONArray shapePoints = null;
	            JSONObject previousStop = null;
	            JSONObject nearestPreviousStop = null;
	            JSONObject nearestShapePoint = null;
	            JSONObject nearestNextStop = null;
	            try {
	                shapePoints = new JSONObject(shapeDb.get(shapeId, agency)).getJSONArray("shape_points");
	                
	                double minDist = Double.MAX_VALUE;
	                //get the nearest shape point and its previous and next stop
	                for (int i=0; i<shapePoints.length(); i++) {
	                    JSONObject curShapePoint = shapePoints.getJSONObject(i);
	                    double curDist = GtfsUtil.distance(curShapePoint.getDouble("lat"),
	                                                    curShapePoint.getDouble("lon"), curLat, curLon);
	                    if (curDist < minDist) {
	                        nearestPreviousStop = previousStop;
	                        minDist = curDist;
	                        nearestShapePoint = curShapePoint;
	                    }
	                    try {
	                        String curStopId = curShapePoint.getString("stpid");
	                        if (curStopId.compareTo(nearestShapePoint.getString("nstp"))==0) {
	                            nearestNextStop = curShapePoint;
	                        }
	                        previousStop = curShapePoint;
	                    }catch(JSONException e) {
	                        //not a stop, don't have to do anything
	                    }
	                }
	            } catch(Exception e){
	            	e.printStackTrace();
	                logger.warning("No shape data for shapeId " + shapeId);
	            }
	
	            if (nearestShapePoint==null) {
	                logger.warning("NO SHAPE POINTS!");
	                return;
	            }
	            if (nearestNextStop==null) {
	                try {
	                    nearestShapePoint.get("stpid");
	                    nearestNextStop = nearestShapePoint;
	                }catch(Exception e){
	                    //the vehicle has traveled beyond the last stop
	                    //no need for further predictions
	                    return;
	                }
	            }else if (nearestPreviousStop==null) {
	                //the vehicle is just beyond the first stop
	                //treat it as being on the first stop
	                for (int i=0; i<shapePoints.length(); i++) {
	                    try {
	                        JSONObject cur = shapePoints.getJSONObject(i);
	                        cur.get("stpid");
	                        nearestShapePoint = cur;
	                        nearestPreviousStop = cur;
	                        break;
	                    }catch(Exception e) {
	                        continue;
	                    }
	                }
	            }
	
	            double distToPreviousStop = nearestShapePoint.getDouble("dist")-nearestPreviousStop.getDouble("dist");
	            double distToNextStop = nearestNextStop.getDouble("dist")-nearestShapePoint.getDouble("dist");
	
	            StopTime previousStopTime = info.getStopTime(GtfsUtil.tripStopId(curTripId, nearestPreviousStop.getString("stpid")));
	            StopTime nextStopTime = info.getStopTime(GtfsUtil.tripStopId(curTripId, nearestNextStop.getString("stpid")));
	            curStop = previousStopTime;
				
				
	            try {
	                long diff = (nextStopTime.getDepartureTime() - previousStopTime.getDepartureTime())
	                    % TimeUtil.SECONDS_IN_DAY;
	                long currentTime = TimeUtil.getTimeMillis() / 1000;
	                //realTime is the departure time of the previous stop on the trip
	                realTime = (long) (currentTime - diff * distToPreviousStop / (distToNextStop + distToPreviousStop));
	            }catch(Exception e) {
	                logger.info(curTripId+" "+nearestPreviousStop.getString("stpid")+" "+nearestNextStop.getString("stpid"));
	            }
				
	            curNum = 0; //there's nothing in predictions yet
	
	        }
	        String timeZone = null;
	        switch (agency) {
	        case "PAAC":
	            timeZone = "America/New_York";
	            break;
	        case "MTA":
	            timeZone = "America/New_York";
	            break;
	        }
	        //we must use the same day here because daylight saving time will mess things up if we choose a
	        //day that's in a different season
	        String scheduledReadableTime = TimeUtil.convertSecondsMidnight(curStop.getDepartureTime());
	        String today = TimeUtil.convertMillisToDate(TimeUtil.getTimeMillis(), timeZone).substring(0, 8);
	        //since the error can't get more than half a day, we are safe to assume that if the bus
	        //is scheduled to arrive yesterday, the current time shouldn't be more 
	        //than half a day past today's midnight
	        //therefore only subtracting off half a day would make sure that we don't 
	        //over subtract in a day with 23 hours(daylight saving)
	        String yesterday = TimeUtil.convertMillisToDate(TimeUtil.getTimeMillis()-TimeUtil.SECONDS_IN_DAY/2, timeZone).substring(0,8);
	        //same as above only add half a day
	        String tomorrow = TimeUtil.convertMillisToDate(TimeUtil.getTimeMillis()+TimeUtil.SECONDS_IN_DAY/2, timeZone).substring(0,8);
					
	        long scheduledTime = TimeUtil.converStringToPosix(today+" "+scheduledReadableTime, "yyyyMMdd HH:mm", timeZone);
	        long yesterdayScheduledTime = TimeUtil.converStringToPosix(yesterday+" "+scheduledReadableTime, "yyyyMMdd HH:mm", timeZone);
	        long tomorrowScheduledTime = TimeUtil.converStringToPosix(tomorrow+" "+scheduledReadableTime, "yyyyMMdd HH:mm", timeZone);
			
	        //see which on which day is the scheduled time closest to real time
	        if (Math.abs(yesterdayScheduledTime-realTime)<Math.abs(scheduledTime-realTime)) {
	            scheduledTime = yesterdayScheduledTime;
	            today = yesterday;
	        }
	        if (Math.abs(tomorrowScheduledTime-realTime)<Math.abs(scheduledTime-realTime)) {
	            scheduledTime = tomorrowScheduledTime;
	            today = tomorrow;
	        }
			
	        long error = realTime - scheduledTime;
	
	        //update the future stops that only have scheduled time based on error
	        for (StopTime st : stops) {
	            if (curNum >= NUM_STOPS) return;
	            if (st.getStopSequence() > curStop.getStopSequence()) {
	                String stopId = st.getStop().getId().getId();
	                if (stopId == null) {
	                    continue;
	                }
	                long departureTime = TimeUtil.converStringToPosix(today, "yyyyMMdd", timeZone) + st.getDepartureTime();
	                if (departureTime<scheduledTime) {
	                    //departure time should be going to the next day
	                    //we can add half a day to scheduled time to get tomorrow 
	                    //assuming that a trip can't go longer than half a day
	                    today = TimeUtil.convertMillisToDate(scheduledTime+TimeUtil.SECONDS_IN_DAY/2, timeZone).substring(0,8);
	                    departureTime = TimeUtil.converStringToPosix(today, "yyyyMMdd", timeZone) + st.getDepartureTime();
	                }
	                departureTime += error;
	                JSONObject nextStop = new JSONObject();
	                nextStop.put("stop-id", stopId);
	                nextStop.put("departure-time", Long.toString(departureTime));
	                if (predictions==null) {
	                    predictions = new JSONArray();
	                    vehicleData.put("predictions", predictions);
	                }
	                predictions.put(nextStop);
	                ++curNum;
	            }
	        }
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    }

    private static void updateObservation(String route, String agency, JSONObject update) throws JSONException {

        JSONObject dbRoute;
        try {
            dbRoute = new JSONObject(realtimeObservationDb.get(route, agency));
        } catch (Exception e) {
            realtimeObservationDb.put(route, agency, update.toString());
            return;
        }
        JSONObject dbObservations = dbRoute.getJSONObject("observations"); //existing vehicles

        Iterator<String> updateVehicleIds = update.keys();
        while(updateVehicleIds.hasNext()) {
            String vehicleId = updateVehicleIds.next();

            JSONObject vehicle = update.getJSONObject(vehicleId);

            dbObservations.put(vehicleId, vehicle);
        }

        realtimeObservationDb.put(route, agency, dbObservations.toString());

        /*Set<String> dbVehicleSet = dbObservations.keySet(); //existing vehicles
        while (vids.hasNext()) {
            String vid = vids.next();

            JSONObject updateVal = update.getJSONObject(vid); 
            if (dbVehicleSet.contains(vid)) {

                dbObservations.put(vid, updateVal);

                /*double belief = Double.parseDouble(updateVal.getString("belief"));
                  if (belief > THRESHOLD) {
                  dbObservations.put(vid, updateVal);
                  }
                  } else {
                  dbObservations.put(vid, updateVal);
                  }*/
        //    }
        //    realtimeObservationDb.put(route, agency, dbObservations.toString());
	//}
    }
}
