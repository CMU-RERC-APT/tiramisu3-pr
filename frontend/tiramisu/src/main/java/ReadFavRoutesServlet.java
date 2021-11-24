package main.java;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.json.JSONArray;
import org.json.JSONObject;

import org.onebusaway.gtfs.model.Route;

import main.java.util.*;
import main.java.realtime.GtfsInfo;

/**
 * Servlet implementation class ReadFavRoutes
 */
@WebServlet("/ReadFavRoutesServlet")
public class ReadFavRoutesServlet extends AbstractServlet {
    // A version number for each serializable class
	// if not explicitly specified, a value is generated on the run, but it may be brittle
    private static final long serialVersionUID = 1L;
    
    private String servletName = "ReadFavRoutesServlet";
    private ArrayList<String> errors;
    private Map<String, String> expectedQueryParams;

    private static String predictUrl = System.getProperty("PREDICT_URL");
    private static String routePath = System.getProperty("ROUTE_PATH");

    private GtfsInfo PAACInfo = GtfsInfo.PAACInfo;
    private GtfsInfo MTAInfo = GtfsInfo.MTAInfo;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReadFavRoutesServlet() {
        super();
    	this.expectedQueryParams = new HashMap<String, String>();
    	this.expectedQueryParams.put("device_id", "string");
    	this.expectedQueryParams.put("user_lat", "double");
    	this.expectedQueryParams.put("user_lon", "double");
    }
    
    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	// TODO Auto-generated method stub
	doGet(request, response);
    }
    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Map<String, Object> queryParams = null;
	
		try {
		    queryParams = super.getQueryParams(request, this.expectedQueryParams);
		} catch (Exception e) {
		    super.writeErrorResponse(response, servletName, e.getMessage());
		}
	
		//System.out.println("Received GET request!");
	
        JSONArray favRoutes = new JSONArray();
		try {
		    favRoutes = readFavRoutes(queryParams);
		    //super.writeSelectResponse(response, servletName, favRoutes);
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
        }

        for(int i = 0; i < favRoutes.length(); i++) {
            removeRoute(queryParams, favRoutes.getJSONObject(i));
        }
        
        JSONArray routes = new JSONArray();
        //super.writeSelectResponse(response, servletName, routes);
        
        int expCond = ExpCondUtil.getExperimentCondition((String)queryParams.get("device_id"), 1);

        if(expCond == -1) {
            expCond = ThreadLocalRandom.current().nextInt(0, 2);
            ExpCondUtil.setExperimentCondition((String)queryParams.get("device_id"), 1, expCond);
        }

        //System.out.println("expCond:" + expCond.toString());

        if(expCond == 1 || expCond == 2) {
            //System.out.println("In predict condition");
            routes = getPredictedRoutes(queryParams);
            //System.out.println(routes);
            for(int i = 0; i < routes.length(); i++) {
                writeRoute(queryParams, routes.getJSONObject(i));
            }
        }
        super.writeSelectResponse(response, servletName, routes);
        
    }

    // Helper function to read parameters from request and write to data base
    public JSONArray readFavRoutes(Map<String, Object> queryParams) throws SQLException {
	
		String readRoutesSql = "SELECT route_id, route_short_name, agency_id FROM user_data.route WHERE device_id = ? "
		    + "AND (event = 'write' OR event = 'autowrite')"
		    + "AND id NOT IN (SELECT pair_id FROM user_data.route WHERE (event = 'remove' OR event = 'autoremove') "
	            + "AND pair_id IS NOT NULL "
		    + "AND device_id = ?)";
		
		Object[] values = {
		    queryParams.get("device_id"),
		    queryParams.get("device_id")
		};
	
		try {
		    return super.doQuery(readRoutesSql, values);	    
		} catch (SQLException e) {
		    //TODO: Handle Error
			throw e;
		}
    }


    public void removeRoute(Map<String, Object> queryParams, JSONObject route) {
        Map<String, String> params = convertMap(queryParams);
        params.put("agency_id", route.getString("agency_id"));
        params.put("route_id", route.getString("route_id"));
        params.put("route_short_name", route.getString("route_short_name"));
        params.put("event", "autoremove");

        //System.out.println(params);
        String url = UrlUtil.makeURL("http://localhost:8080/tiramisu/", "RemoveFavRouteServlet", params);
        //System.out.println(url);

        try {
            JSONObject res = JsonUtil.readJsonFromUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void writeRoute(Map<String, Object> queryParams, JSONObject route) {
        Map<String, String> params = convertMap(queryParams);
        params.put("agency_id", route.getString("agency_id"));
        params.put("route_id", route.getString("route_id"));
        params.put("route_short_name", route.getString("route_short_name"));
        params.put("event", "autowrite");

        //System.out.println(params);
        String url = UrlUtil.makeURL("http://localhost:8080/tiramisu/", "WriteFavRouteServlet", params);
        //System.out.println(url);

        try {
            JSONObject res = JsonUtil.readJsonFromUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }


    public Map<String, String> convertMap(Map<String, Object> initialMap) {
        Map<String, String> newMap = new HashMap<String, String>();
        for (Map.Entry<String, Object> entry : initialMap.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue().toString());
        }
       
        return newMap;
    }


    public JSONObject routeFromPrediction(String prediction) {
        String[] parts = prediction.split("_");
        String agency_id = parts[0];
        String route_short_name = parts[1];

        JSONObject route = new JSONObject();
        route.put("agency_id", agency_id);
        route.put("route_short_name", route_short_name);

        for(Route possibleRoute : PAACInfo.getRoutes()) {
            //System.out.println(possibleRoute.getAgency().getId());
            if(agency_id.equals(possibleRoute.getAgency().getId()) && route_short_name.equals(possibleRoute.getShortName())) {
                //System.out.println("Adding route id: " + possibleRoute.getId());
                route.put("route_id", possibleRoute.getId().toString());
            } 
        }
        return route;
    }


    public JSONArray getPredictedRoutes(Map<String, Object> queryParams) {
        Map<String, String> params = convertMap(queryParams);
        String url = UrlUtil.makeURL(predictUrl, "routes", params);
        JSONArray predictions = null;
        try {
            predictions = JsonUtil.readJsonFromUrl(url).getJSONArray("data");
        } catch (Exception e){
            e.printStackTrace();
        }

        JSONArray routes = new JSONArray();
        if(predictions != null) {
	        for(int i = 0; i < predictions.length(); i++) {
	            String prediction = predictions.getString(i);
	            routes.put(routeFromPrediction(prediction));
	        }
        }
        return routes;
    }

}
