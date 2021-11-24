package cmu.edu.alarm_processor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

import cmu.edu.alarm_processor.util.JsonUtil;
import cmu.edu.alarm_processor.util.UrlUtil;

/* A class that handles all OBA api call*/
public class OBAConnection {
	
	private static TiramisuDb propertiesDb = TiramisuDb.getDb("Properties");
	private static JSONObject properties = new JSONObject(propertiesDb.get("OBAConnection", null))
			.getJSONObject("properties");

	private static final String API_URL_BASE = properties.getString("API_URL_BASE");

	private static final String API_KEY = properties.getString("API_KEY");

	
	public static JSONObject getArrivalAndDepartureForStop(JSONObject tableEntry){
		
		Map<String, String> params = new HashMap<String, String>();
		
//		ServiceDate serviceDate = new ServiceDate(today);
		
		String stopId = tableEntry.getString("stop_id");
		String tripId = tableEntry.getString("trip_id");
		String serviceDate = tableEntry.getString("service_date");
		
		params.put("key", API_KEY);
		params.put("tripId", tripId);
		params.put("serviceDate", serviceDate);
		
		URL myURL = null;
		
		try {
			myURL = new URL(UrlUtil.makeURL(API_URL_BASE, "arrival-and-departure-for-stop/" + stopId + ".json", params));
		} catch (MalformedURLException e) {
			System.out.println("Create url failure");
			e.printStackTrace();
			System.exit(0);
		}
		
		JSONObject OBAResponse = null;
		
		try {
			OBAResponse = JsonUtil.readJsonFromUrl(myURL.toString());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		
		return OBAResponse;
	}
	
}
