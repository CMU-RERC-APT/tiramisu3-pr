package main.java.realtime;
//package cmu.edu.gtfs_realtime_processor.avl;

import java.util.logging.Logger;
import static java.util.logging.Level.*;

import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.csv_entities.CsvInputSource;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.Stop;

import org.json.JSONObject;

import main.java.util.*;
//import cmu.edu.gtfs_realtime_processor.util.UrlUtil;
//import cmu.edu.gtfs_realtime_processor.util.TimeUtil;
//import cmu.edu.gtfs_realtime_processor.util.GtfsUtil;

public class GtfsInfo {
    private static TiramisuDb gtfsVersionDb = TiramisuDb.getDb("GtfsVersions");

    private static Logger logger = Logger.getLogger(GtfsInfo.class.getName());
    private Collection<Route> routes;
    private Collection<Trip> trips;
    private Collection<Stop> stops;
    private Map<String, List<StopTime>> tripToStops;
    private Collection<ShapePoint> shapePoints;
    private Map<String, StopTime> idToStopTime; // trip, stop ids -> stoptime
    private Map<String, Trip> idToTrip;
    private Map<String, String> stopCodeToId;
    private Map<String, List<Trip>> shapeIdToTrips;
    public static volatile GtfsInfo PAACInfo = getAgencyInfo("PAAC");
    public static volatile GtfsInfo MTAInfo = getAgencyInfo("MTA");

    public static GtfsInfo getAgencyInfo(String agency_id) {
    	logger.severe("Step 2....");
        String gtfsVersionJson = gtfsVersionDb.getGtfsPath(agency_id);
        logger.severe("Step 3....");
        GtfsInfo agencyInfo = new GtfsInfo(gtfsVersionJson);
        return agencyInfo;
    }
    
    protected GtfsInfo(String gtfsVersionJson) {

        load(gtfsVersionJson);
    }

    private void load(String gtfsVersionJson) {
        try {
            logger.log(INFO, "Loading GTFS data...");
            logger.severe("Step 4....");

            CsvInputSource s3Source = new S3CsvInputSource(gtfsVersionJson);
            GtfsReader reader = new GtfsReader();
            GtfsDaoImpl store = new GtfsDaoImpl();
            logger.severe("Step 5....");

            //reader.setInputLocation(new File(path));
            reader.setInputSource(s3Source);
            reader.setEntityStore(store);
            reader.run();

            routes = store.getAllRoutes();
            logger.severe("Step 6....");

            for (Route curRoute : routes) {
                logger.fine(curRoute.toString());
            }

            stops = store.getAllStops();
            trips = store.getAllTrips();
            shapePoints = store.getAllShapePoints();
            logger.severe("Step 7....");
            
            shapeIdToTrips = new HashMap<String, List<Trip>>();
            tripToStops = new HashMap<String, List<StopTime>>();
            idToStopTime = new HashMap<String, StopTime>();
            idToTrip = new HashMap<String, Trip>();
            stopCodeToId = new HashMap<String, String>();
            
            Collection<StopTime> stopTimes = store.getAllStopTimes();
            for (StopTime st : stopTimes) {

                String tripId = st.getTrip().getId().getId();
                String routeId = GtfsUtil.extractPAACRealtimeId(st.getTrip().getRoute().getId().getId());
                String tripRouteId = GtfsUtil.routeTripId(routeId, tripId);
                String stopId = st.getStop().getId().getId();
                String tripStopId = GtfsUtil.tripStopId(tripId, stopId);
                               
                if (tripRouteId != null) {
                    if (tripToStops.containsKey(tripRouteId)) {
                        tripToStops.get(tripRouteId).add(st);
                    } else {
                        ArrayList<StopTime> temp = new ArrayList<StopTime>();
                        temp.add(st);
                        tripToStops.put(tripRouteId, temp);
                    }
                } else {
                    logger.log(WARNING, "No match");
                }
                if (tripStopId != null) {
                    idToStopTime.put(GtfsUtil.tripStopId(tripId, stopId), st);
                } else {
                    logger.log(WARNING, "No match");
                }
            }
            logger.severe("Step 8....");

            for (String id : tripToStops.keySet()) {
                Collections.sort(tripToStops.get(id), new StopTimeComparator());
            }
            logger.severe("Step 9....");
            
            for (Trip trip : trips) {
                try {
                    String shapeId = trip.getShapeId().getId();
                    if (shapeIdToTrips.containsKey(shapeId)) {
                        shapeIdToTrips.get(shapeId).add(trip);
                    }else{
                        List<Trip> trips = new ArrayList<Trip>();
                        trips.add(trip);
                        shapeIdToTrips.put(shapeId, trips);
                    }
                }catch(Exception e) {
                    //this trip doesn't have shape id, move on to the next
                }
            	
                String tripId = trip.getId().getId();
                idToTrip.put(tripId, trip);

                // We need this because PAAC doesn't include full tripIds in their realtime
                String shortTripId = GtfsUtil.extractPAACRealtimeId(tripId);
                String blockId = "";
                String searchId = "";
                if (trip.getBlockId()!=null && shortTripId != null) {
                    blockId = trip.getBlockId().replaceAll("\\s+","");
                    searchId = GtfsUtil.tripBlockId(shortTripId, blockId);
                }
                //            	System.out.println("SEARCH ID IN INFO: " + searchId);
                if (idToTrip.containsKey(searchId)) {
                    idToTrip.put(searchId, null);
                }else{
                    idToTrip.put(searchId, trip);
                }
                /*if (idToTrip.containsKey(tripId)) {
                    idToTrip.put(tripId, null);
                }else{
                    idToTrip.put(tripId, trip);
                    }*/
            }
            logger.severe("Step 10....");
            
            for (Stop stop : stops) {
                stopCodeToId.put(stop.getCode(),stop.getId().getId());
            }
            logger.severe("Step 11....");
            logger.log(INFO, "GTFS data loaded!");
        } catch (Exception e) {
            //e.printStackTrace();
            //logger.log(SEVERE, "No data");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logger.log(SEVERE, "Exception in GTFSInfo: " + e.getMessage() + "\n" + sw.toString());
        }
    }

    public Collection<Route> getRoutes() {
        return routes;
    }

    public Collection<Trip> getTripsWithShapeId(String shapeId) {
        return shapeIdToTrips.get(shapeId);
    }
    
    public Collection<StopTime> getStops(String id) { // Route + trip
        return tripToStops.get(id);
    }

    public Collection<ShapePoint> getShapePoints() {
        return shapePoints;
    }
    
    public StopTime getStopTime(String id) { // Trip + stop
        return idToStopTime.get(id);
    }
    
    //this just for PAAC
    public Trip getTrip(String id) {
        Trip ans = idToTrip.get(id);
        /*if (ans==null) {
            return idToTrip.get(GtfsUtil.extractId(id));
            }*/
        return ans;
    }
    
    public Collection<Trip> getTrips() {
        return trips;
    }
    
    public String getStopId(String code) {
        return stopCodeToId.get(code);
    }
}

class StopTimeComparator implements Comparator<StopTime> {
    public int compare(StopTime a, StopTime b) {
        return a.getStopSequence() - b.getStopSequence();
    }
}
