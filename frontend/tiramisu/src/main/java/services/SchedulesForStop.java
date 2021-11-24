package main.java.services;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import main.java.util.JsonUtil;
import main.java.util.UrlUtil;

public class SchedulesForStop {

    private static String oba_url = "https://www.riderinfo.org/oba/api/where/"; //System.getProperty("OBA_URL");
    //private static String oba_url = "http://localhost:8080/oba/api/where/";
    private static String oba_key = "TEST"; //System.getProperty("OBA_API_KEY");
    //private static String oba_key = "TEST";
    private static String api_call = "arrivals-and-departures-for-stop";
    
    public static JSONArray getSchedules(JSONObject stop) {
        String stopId = stop.getString("id");
        Map<String, String> params = new HashMap<String, String>();
        params.put("key", oba_key);
        params.put("minutesBefore", "2");
        params.put("minutesAfter", "720");
        String function_call = api_call + "/" + stopId + ".json";
        String url = UrlUtil.makeURL(oba_url, function_call, params);
        try {
            JSONObject data = JsonUtil.readJsonFromUrl(url).getJSONObject("data");
            JSONArray trips = data.getJSONObject("references").getJSONArray("trips");
            JSONArray schedules = data.getJSONObject("entry").getJSONArray("arrivalsAndDepartures");
            Map<String, String> tripToDirection = new HashMap<String, String>();
            for (int i=0; i<trips.length(); i++) {
                JSONObject trip = trips.getJSONObject(i);
                tripToDirection.put(trip.getString("id"), trip.getString("directionId"));
            }
            for (int i=0; i<schedules.length(); i++) {
                JSONObject schedule = (JSONObject) schedules.get(i);
                schedule.put("directionId", tripToDirection.get(schedule.getString("tripId")));
                schedule.put("stopName", stop.getString("name"));
            }
            return schedules;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }}
