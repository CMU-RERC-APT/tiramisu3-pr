package main.java.services;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONObject;

import main.java.util.GtfsUtil;

public class ScheduleMerger {
    public static JSONArray mergeSchedules(CompletionService<JSONArray> completionService, JSONArray stops, 
                                           Double lat, Double lon) {

        Map<String, PriorityQueue<JSONObject> > scheduleMap = new HashMap<String, PriorityQueue<JSONObject> >();
        Map<String, JSONObject> stopMap = new HashMap<String, JSONObject>();
        JSONArray schedules = new JSONArray();

        int len = stops.length();
        for (int i=0; i<len; i++) {
            JSONObject stop = (JSONObject) stops.get(i);
            stopMap.put(stop.getString("id"), stop);
        }

        while (len>0) {
            try {
                Future<JSONArray> futureSchedule = completionService.take();
                JSONArray schedule = futureSchedule.get();
                merge(scheduleMap, schedule, stopMap, lat, lon);
                len--;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        StopComparator stopCompare = new StopComparator(lat, lon);
        PriorityQueue<JSONObject> closestStops = new PriorityQueue<JSONObject>(stopCompare);
        for (PriorityQueue<JSONObject> stopScheduleQueue : scheduleMap.values()) {
            JSONObject arrival = stopScheduleQueue.peek();
            JSONObject stop = stopMap.get(arrival.getString("stopId"));
            if (!closestStops.contains(stop)) {
                closestStops.add(stop);

                if (closestStops.size() > 10) {
                    closestStops.poll();
                }
            }
        }

        for (PriorityQueue<JSONObject> stopScheduleQueue : scheduleMap.values()) {
            //PriorityQueue<JSONObject> stopScheduleQueue = scheduleMap.get(key);
            JSONObject arrival = stopScheduleQueue.peek();
            JSONObject stop = stopMap.get(arrival.getString("stopId"));

            if (closestStops.contains(stop)) {
            
                ArrayList<JSONObject> upcoming = new ArrayList<JSONObject>();

                while(stopScheduleQueue.size() > 1) {
                    JSONObject upcomingArrival = stopScheduleQueue.poll();
                    if(upcomingArrival.getBoolean("predicted")) {
                        upcomingArrival.put("predictedArrivalTime", upcomingArrival.getLong("predictedArrivalTime") - 60000);
                    }
                    upcoming.add(upcomingArrival);
                }

                JSONObject mainArrival = stopScheduleQueue.poll();
                if(mainArrival.getBoolean("predicted")) {
                    mainArrival.put("predictedArrivalTime", mainArrival.getLong("predictedArrivalTime") - 60000);
                }

                Collections.reverse(upcoming);
                JSONArray upcomingArrivals = new JSONArray(upcoming);
                mainArrival.put("upcomingArrivals", upcomingArrivals);
                schedules.put(mainArrival);
            }
        }
        return schedules;
    }
	
    private static double getArrivalTime(JSONObject entry) {
        long realtime = entry.getLong("predictedArrivalTime");
        long scheduletime = entry.getLong("scheduledArrivalTime");
        if (realtime!=0) {
            return realtime;
        }else{
            return scheduletime;
        }
    }
	
    private static void merge (Map<String, PriorityQueue<JSONObject> > original, JSONArray current, Map<String, JSONObject> stopMap,
                               Double lat, Double lon) {
        if (current.length()==0) return;
        ScheduleEntryComparator scheduleCompare = new ScheduleEntryComparator();
        JSONObject currentStop = stopMap.get(((JSONObject) current.get(0)).getString("stopId"));
        for (int i=0; i<current.length(); i++) {
            JSONObject currentEntry = current.getJSONObject(i);
            String key = currentEntry.getString("routeId") + currentEntry.getString("directionId");
            PriorityQueue<JSONObject> stopScheduleQueue = original.get(key);
            if (stopScheduleQueue==null) {
                PriorityQueue<JSONObject> tempQueue = new PriorityQueue<JSONObject>(scheduleCompare);
                tempQueue.add(currentEntry);
                original.put(key, tempQueue);
            }else{
                JSONObject formerEntry = stopScheduleQueue.peek();
                if (currentEntry.getString("stopId").equals(formerEntry.getString("stopId"))) {
                    stopScheduleQueue.add(currentEntry);
                    if(stopScheduleQueue.size() > 3) {
                        stopScheduleQueue.poll();
                    }
                    original.put(key, stopScheduleQueue);
                }else{
                    JSONObject formerStop = stopMap.get(formerEntry.getString("stopId"));
                    double currentLat = currentStop.getDouble("lat");
                    double currentLon = currentStop.getDouble("lon");
                    double formerLat = formerStop.getDouble("lat");
                    double formerLon = formerStop.getDouble("lon");
                    if (Math.abs(GtfsUtil.distance(currentLat, currentLon, lat, lon)) < 
                        Math.abs(GtfsUtil.distance(formerLat, formerLon, lat, lon))) {
                        PriorityQueue<JSONObject> tempQueue = new PriorityQueue<JSONObject>(scheduleCompare);
                        tempQueue.add(currentEntry);
                        original.put(key, tempQueue);
                    }
                }
            }
        }
    }

    public static class ScheduleEntryComparator implements Comparator<JSONObject> {
        
        public int compare(JSONObject entry1, JSONObject entry2) {
            return getArrivalTime(entry1) < getArrivalTime(entry2) ? 1 : -1;
        }
    }

    public static class StopComparator implements Comparator<JSONObject> {

        double lat;
        double lon;
        
        public StopComparator(Double lat, Double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public int compare(JSONObject stop1, JSONObject stop2) {
            double lat1 = stop1.getDouble("lat");
            double lon1 = stop1.getDouble("lon");
            double lat2 = stop2.getDouble("lat");
            double lon2 = stop2.getDouble("lon");
            return Math.abs(GtfsUtil.distance(lat1, lon1, this.lat, this.lon)) < Math.abs(GtfsUtil.distance(lat2, lon2, this.lat, this.lon)) ? 1 : -1;
        }
    }
}
