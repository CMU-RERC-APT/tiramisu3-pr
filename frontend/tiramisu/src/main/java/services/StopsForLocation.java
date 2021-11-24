package main.java.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


import main.java.util.*;

public class StopsForLocation {
	
    private static String oba_url = "https://www.riderinfo.org/oba/api/where/"; //System.getProperty("OBA_URL");
    //private static String oba_url = "http://localhost:8080/oba/api/where/";
    private static String oba_key = "TEST"; //System.getProperty("OBA_API_KEY");
    //private static String oba_key = "TEST";
    private static String api_call = "stops-for-location.json";
	
    public StopsForLocation() {
    }
	
    public static JSONArray getStops(String lat, String lon) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("key", oba_key);
        params.put("lon", lon);
        params.put("lat", lat);
        params.put("radius", "500");
        String url = UrlUtil.makeURL(oba_url, api_call, params);
        //		System.out.println(url);
        try {
            return JsonUtil.readJsonFromUrl(url).getJSONObject("data").getJSONArray("list");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
