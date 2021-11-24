package cmu.edu.gtfs_realtime_processor.avl;

import java.util.logging.Logger;
import static java.util.logging.Level.*;

import java.io.InputStream;
import java.lang.StringBuffer;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import cmu.edu.gtfs_realtime_processor.util.JsonUtil;
import cmu.edu.gtfs_realtime_processor.util.UrlUtil;
import cmu.edu.gtfs_realtime_processor.util.TimeUtil;
import cmu.edu.gtfs_realtime_processor.util.GtfsUtil;

public class FeedGrabberMTA {
    private static Logger logger = Logger.getLogger(FeedGrabberMTA.class.getName());
    private static TiramisuDb propertiesDb = TiramisuDb.getDb("Properties");
    private static JSONObject properties = new JSONObject(propertiesDb.get("FeedGrabberMTA", null)).getJSONObject("properties");
    private static final int NUM_STOPS = properties.getInt("NUM_STOPS");
    private static final int QUEUE_THRESHOLD_SIZE = properties.getInt("QUEUE_THRESHOLD_SIZE");
    private static final String funcName = properties.getString("funcName");
    public static final int DELAY = properties.getInt("DELAY");
	
    private static final Agency provider = new Agency(properties.getString("AGENCY_NAME"), 
                                                      properties.getString("AGENCY_URL"), properties.getString("AGENCY_KEY"));
	
    GtfsInfo info = GtfsInfo.getAgencyInfo("MTA");
	
    /* fetch the JSONFile from MTA with all vehicle information*/
    public static JSONObject fetchVehicles(Agency provider) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("key", provider.key);
        params.put("VehicleMonitoringDetailLevel", "calls");
        URL myURL = null;
	String url = provider.url + funcName + ".json";
        try {
            myURL = new URL(UrlUtil.makeURL(provider.url + funcName + ".json", "", params));
            JSONObject MTAresponse = JsonUtil.readJsonFromUrl(myURL.toString());
            return MTAresponse;
	} 
	catch (Exception e){
	    e.printStackTrace();
            logger.severe("Wrong URL: " + url);
            return null;
	}
	
    }

    public static void main(String[] args) {
        FeedGrabberMTA feedGrabberMTA = new FeedGrabberMTA();
        feedGrabberMTA.run();
    }
 
    public void run(){
        FeedQueue q = FeedQueue.getQueue();
        q.clear();
        while (true) {
            long timeStampMillis = TimeUtil.getTimeMillis();
            logger.info("Grabbing MTA feed...");
			
            JSONObject MTAresponse = FeedGrabberMTA.fetchVehicles(provider);
            if (MTAresponse != null) {
                FeedGrabberMTA.parse(q, MTAresponse, timeStampMillis);
            }
            try {
                Thread.sleep(DELAY);
            }
            catch (InterruptedException e) {
                logger.log(WARNING, "Interrupted exception");
            }
        } 
    }
	
    /* get the substring after the first underscore for MTA id string formats*/
    public static String getIdAfterUnderscore(String id){
        if (id.indexOf("_") == -1){
            logger.severe("no '_' in string");
            return id;
        }
        id = id.substring(id.indexOf("_")+1);
        return id;
    }
	
    /* parse vehicles from MTAresponse, group by route ids, and send to queue*/
    public static void parse(FeedQueue q, JSONObject MTAresponse, long timeStampMillis) {

        JSONArray allVehicles = MTAresponse.getJSONObject("Siri").getJSONObject("ServiceDelivery").
            getJSONArray("VehicleMonitoringDelivery").getJSONObject(0).getJSONArray("VehicleActivity");
		
        /* used to store all the vehicle grouped by route*/
        JSONObject routesFeed = new JSONObject();
		
		
        for (int i = 0; i < allVehicles.length(); i++) {
			
            logger.info("Processing "+Integer.toString(i)+" of "+Integer.toString(allVehicles.length()));
            /* read from MTA feed */
            JSONObject vehicle = allVehicles.getJSONObject(i).getJSONObject("MonitoredVehicleJourney");
            String tatripid = vehicle.getJSONObject("FramedVehicleJourneyRef").getString("DatedVehicleJourneyRef");
            System.out.println("original trip id = "+tatripid);
            tatripid = getIdAfterUnderscore(tatripid);
            System.out.println("trip id = "+tatripid);

			
            Double lon = vehicle.getJSONObject("VehicleLocation").getDouble("Longitude");
            Double lat = vehicle.getJSONObject("VehicleLocation").getDouble("Latitude");
            String vid = vehicle.getString("VehicleRef");
            /*??????*/
            vid = getIdAfterUnderscore(vid);
            /* some vehicles don't have block id information*/
            String tablockid = null;
            try{
                tablockid = vehicle.getString("BlockRef");
            } catch (Exception e){
                logger.severe("No block id!");
            }
            String route = vehicle.getString("LineRef");			
            route = getIdAfterUnderscore(route);

	
            /* if no route entry exists in route */
            try {
                routesFeed.getJSONObject(route);
            } catch (Exception e) {
                routesFeed.put(route, new JSONObject());
            }
			
            JSONObject vehiclesOfRoute = routesFeed.getJSONObject(route);
					
            try{
                vehiclesOfRoute.getJSONObject(vid);
            } catch (Exception e){
                vehiclesOfRoute.put(vid, new JSONObject());
            }

            /* set parameters */
            JSONObject vehicleData = vehiclesOfRoute.getJSONObject(vid);
            vehicleData.put("route", route);
            vehicleData.put("agency", "MTA");
            vehicleData.put("tatripid", tatripid);
            vehicleData.put("belief", "1.0");
            vehicleData.put("lat", Double.toString(lat));
            vehicleData.put("lon", Double.toString(lon));
            vehicleData.put("time-stamp-millis", Long.toString(timeStampMillis));
            vehicleData.put("tablockid", tablockid); 
            addPredictions(vid, vehicleData, vehicle);
			
            if (routesFeed.getJSONObject(route).toString().length() > QUEUE_THRESHOLD_SIZE){
                sendRoutes(routesFeed.getJSONObject(route), route, q);
                routesFeed.put(route, new JSONObject());
            }
			
        }
		
        for (Object key : routesFeed.keySet()) {
            /* key in routesFeed is route_id */
            String keyStr = (String)key;
            sendRoutes(routesFeed.getJSONObject(keyStr), keyStr, q);
        }
    }
	
    /* Send all a route JSONObject to queue*/
    private static void sendRoutes(JSONObject vehicles, String route, FeedQueue q){
        JSONObject msg = new JSONObject();
        msg.put("route", route);
        msg.put("agency", "MTA");
        msg.put("vehicles", vehicles);
        logger.info("Sending message to sqs queue, size = "+msg.toString().length());	
        q.putMessage(msg.toString());
    }
	
    /* add list of predictions to vehicleData*/
    private static void addPredictions(String vid, JSONObject vehicleData, JSONObject vehicle) {
        List<StopTimePair> stopTimes = getPredictions(vid, vehicle);

        JSONArray predictions = new JSONArray();
        int index = 0;
		
        /* return when there's no arrival information for this vehicle*/
        if (stopTimes.size() == 0){
            vehicleData.put("predictions", predictions);
            return;
        }
		
        for (StopTimePair st : stopTimes) {
            JSONObject curSt = new JSONObject();
			
            curSt.put("stop-id", st.stopId);
            /* TO DO: what to put for departure time if there's no expected info?*/
            Long departureTime = (long)-1;
            if (st.time == null){
                departureTime = (long)-1;
            }
            else{
                String timeString = st.time.substring(0, 10) + st.time.substring(11, 19);
                departureTime = TimeUtil.converStringToPosix(timeString, "yyyy-MM-ddHH:mm:ss", "America/New_York");	
            }
            curSt.put("departure-time", Long.toString(departureTime));
            predictions.put(index, curSt);
            ++index;
        }
        vehicleData.put("predictions", predictions);
    }
	
    /* return all the stop-time pairs from OnwardCalls related to the vehicle id */
    private static List<StopTimePair> getPredictions(String vid, JSONObject vehicle) {
        //		System.out.println("Processing onward calls for vehicle "+vid);
        List<StopTimePair> pairs = new ArrayList<StopTimePair>();
        JSONObject onwardCalls = vehicle.getJSONObject("OnwardCalls");
        if (onwardCalls.length() == 0){
            logger.severe("No onward calls");
        }
        else{	
            JSONArray stops = onwardCalls.getJSONArray("OnwardCall");
            for (int i = 0; i < Math.min(stops.length(), NUM_STOPS); i++){
                String stopId = stops.getJSONObject(i).getString("StopPointRef");
                stopId = getIdAfterUnderscore(stopId);
                String time = null;
                try{
                    time = stops.getJSONObject(i).getString("ExpectedArrivalTime");
                } catch (Exception e1){
                    try {
                        time = stops.getJSONObject(i).getString("ExpectedDepartureTime");
                    }
                    catch (Exception e2) {
                        logger.severe("No expected departure time info for vehicle: "+vid);
                    }
                    logger.severe("No expected arrival time info for vehicle: "+vid);
                }
                pairs.add(new StopTimePair(stopId, time));

            }
        }
        return pairs;
    }
}
