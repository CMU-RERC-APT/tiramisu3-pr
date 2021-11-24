package main.java.realtime;

import java.io.IOException;


import main.java.realtime.GtfsInfo;
import main.java.util.*;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs_realtime.exporter.GtfsRealtimeLibrary;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.Stop;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//import javax.inject.Inject;
//import javax.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
//import org.onebusway.gtfs_realtime.exporter.GtfsRealtimeExporterModule;
//import org.onebusway.gtfs_realtime.exporter.GtfsRealtimeLibrary;
//import org.onebusway.gtfs_realtime.exporter.GtfsRealtimeMutableProvider;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.util.logging.Logger;
import static java.util.logging.Level.*;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.FeedMessage.Builder;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

/**
 * Servlet implementation class vehiclePositions
 */
public class GtfsRealtimeProvider {

    private static Logger logger = Logger.getLogger(GtfsRealtimeProvider.class.getName());

    private Collection<Route> PAACroutes = null;
    private Collection<Route> MTAroutes = null;

    public FeedMessage MTAvehiclePositions = null;
    public FeedMessage MTAtripUpdates = null;

    public FeedMessage PAACtripUpdates = null;
    public FeedMessage PAACvehiclePositions = null;

    private GtfsInfo PAACInfo = GtfsInfo.PAACInfo;
    private GtfsInfo MTAInfo = GtfsInfo.MTAInfo;

    //@PostConstruct
    //	public void start() {
    //		thread = new Thread(new VehiclesRefreshTask());
    //		thread.start();
    //	}

    //	@Inject
    //	public void setProvider(GtfsRealtimeMutableProvider provider) {
    //	    this.provider = provider;
    //	}

    public ArrayList<String> getVehiclesFromRoutes(Collection<Route> routes, String agency) {
        ArrayList<String> data = new ArrayList<String>();
        TiramisuDb db = TiramisuDb.getDb("RealtimeObservationsTable");
        logger.severe("Step 14....");
        for (Route r : routes) {
            try{
                data.add(db.get(r.getShortName(), agency));
                System.out.println("Got vehicle from route " +r.getShortName());
                //				System.out.println(data.get(data.size()-1));
            }catch (Exception e){

                System.out.println("Can't get vehicles from route " + r.getShortName());
            }
        }
        logger.severe("Step 15....");
        return data;
    }

    public void buildFeed() {

    	logger.severe("Step 12....");
        if (PAACroutes==null) {
            PAACroutes = PAACInfo.getRoutes();
            //System.out.println("PAACroutes: "+PAACroutes.toString());
        }
        if (MTAroutes==null) {
            MTAroutes = MTAInfo.getRoutes();
        }
        logger.severe("Step 13....");

        System.out.println("Getting data for PAAC");
        ArrayList<String> PAACdata = getVehiclesFromRoutes(PAACroutes, "PAAC");

        System.out.println("Getting data for MTA");
        ArrayList<String> MTAdata = getVehiclesFromRoutes(MTAroutes, "MTA");

        System.out.println("PAAC data SIZE: " + PAACdata.size());
        System.out.println("MTA data SIZE: " + MTAdata.size());

        HashMap<String, FeedMessage.Builder> builders = null;

        builders = build("PAAC", PAACdata);	    
        PAACvehiclePositions = builders.get("vehiclePositions").build();
        PAACtripUpdates = builders.get("tripUpdates").build();
        logger.severe("Step 16....");

        builders = build("MTA", MTAdata);	    
        MTAvehiclePositions = builders.get("vehiclePositions").build();
        MTAtripUpdates = builders.get("tripUpdates").build();

        System.out.println("finished building for MTA and PAAC, PAAC size = "+PAACdata.size()+" MTA size ="+MTAdata.size());		
        //	    provider.setVehiclePositions(PAACvehiclePositions.build());
        //	    provider.setTripUpdates(PAACtripUpdates.build());
    }

    /* given vehicle data, build real time feed for specific agency*/
    private HashMap<String, Builder> build(String agency, ArrayList<String> data) {

        FeedMessage.Builder vehiclePositions = GtfsRealtimeLibrary.createFeedMessageBuilder();
        FeedMessage.Builder tripUpdates = GtfsRealtimeLibrary.createFeedMessageBuilder();

        System.out.println("Buildin feed for agency = "+agency);

        for (int i=0; i<data.size(); i++) {

            JSONObject obj = new JSONObject(data.get(i));
			
            JSONObject observations = obj.getJSONObject("observations");

            Iterator<String> vehicleIterator = observations.keys();
            while (vehicleIterator.hasNext()) {
                String vid = vehicleIterator.next();

                JSONObject vehicle = observations.getJSONObject(vid);

                Position.Builder position = Position.newBuilder();

                position.setLatitude(Float.parseFloat(vehicle.getString("lat")));
                position.setLongitude(Float.parseFloat(vehicle.getString("lon")));


                /**
                 * Construct TripDescriptor (field shared by TripUpdate and Vehicle Position)
                 */
                String tripId = vehicle.getString("tatripid");

                TripDescriptor.Builder tripDescriptor = TripDescriptor.newBuilder();
                tripDescriptor.setTripId(tripId);

                /** 
                 * Construct TripUpdate
                 */

                VehicleDescriptor.Builder vehicleDescriptor = VehicleDescriptor.newBuilder();
                vehicleDescriptor.setId(vid);	    	    

                TripUpdate.Builder tripUpdate = TripUpdate.newBuilder();
                tripUpdate.setTrip(tripDescriptor);
                tripUpdate.setVehicle(vehicleDescriptor);
				
                //System.out.println(vehicle);

                JSONArray predictions;
                try {
                    predictions = vehicle.getJSONArray("predictions");
                } catch (Exception e) {
                    continue;
                }

                for (int index = 0; index<predictions.length(); index++){		    	    
                    JSONObject prediction = predictions.getJSONObject(index);

                    StopTimeEvent.Builder departure = StopTimeEvent.newBuilder();

                    long departureTime = -1;
                    try {
                        departureTime = Long.parseLong(prediction.getString("departure-time"));
                    } catch (JSONException e) {
                        logger.log(SEVERE, "JSONException while parsing departure-time for agency " + agency + " route " + vehicle.getString("route"));
                        logger.log(SEVERE, prediction.toString());
                    }

                    departure.setTime(departureTime);

                    StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate.newBuilder();
                    stopTimeUpdate.setDeparture(departure);

                    String stop_id = prediction.getString("stop-id");

                    stopTimeUpdate.setStopId(stop_id);
                    tripUpdate.addStopTimeUpdate(stopTimeUpdate);
                }

                /** 
                 * Construct Vehicle Position
                 */
                VehiclePosition.Builder vehiclePosition = VehiclePosition.newBuilder();
                vehiclePosition.setPosition(position);

                vehiclePosition.setVehicle(vehicleDescriptor);
                vehiclePosition.setTrip(tripDescriptor);

                FeedEntity.Builder vehiclePositionEntity = FeedEntity.newBuilder();
                vehiclePositionEntity.setId(vid);
                vehiclePositionEntity.setVehicle(vehiclePosition);
                vehiclePositions.addEntity(vehiclePositionEntity);

                FeedEntity.Builder tripUpdateEntity = FeedEntity.newBuilder();
                tripUpdateEntity.setId(vid);
                tripUpdateEntity.setTripUpdate(tripUpdate);
                tripUpdates.addEntity(tripUpdateEntity);

            }
        }

        HashMap<String, FeedMessage.Builder> retMap = new HashMap<String, FeedMessage.Builder>();

        retMap.put("tripUpdates", tripUpdates);
        retMap.put("vehiclePositions", vehiclePositions);

        return retMap;
    }
    
    
    
     
    public static void main(String[] args) {
    	GtfsRealtimeProvider provider = new GtfsRealtimeProvider();
        provider.buildFeed();
        FeedMessage PAACtripUpdates = provider.PAACtripUpdates;
        FeedMessage PAACvehiclePositions = provider.PAACvehiclePositions;
        System.out.println("updates:\n" + PAACtripUpdates);
        System.out.println("positions:\n" + PAACvehiclePositions);
    }


    //	private class VehiclesRefreshTask implements Runnable {
    //
    //	    @Override
    //	    public void run() {
    //	    	while(true) {
    //		    	try {
    //		    		System.out.println("Building feed...");
    //		    		try{
    //			    		buildFeed();
    //		    		} catch (Exception e){
    //		    			e.printStackTrace();
    //		    		}
    //		    		Thread.sleep(30000);
    //		    	}catch (Exception e){
    ////		    		System.out.println("error refreshing");
    //		    		e.printStackTrace();
    //		    	}
    //	    	}
    //	    }
    //	}
}


