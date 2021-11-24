package main.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import main.java.AbstractServlet;
import main.java.services.*;
/**
 * Servlet implementation class SchedulesForLocationServlet
 */
@WebServlet("/SchedulesForLocationServlet")


public class SchedulesForLocationServlet extends AbstractServlet {
    private static final long serialVersionUID = 1L;
	
    private static final int NUM_THREADS = 8;
	
    private String servletName = "SchedulesForLocationServlet";
    private ArrayList<String> errors;
    private Map<String, String> expectedQueryParams;
    private ExecutorService executor = null;
    
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SchedulesForLocationServlet() {
        super();
        expectedQueryParams = new HashMap<String, String>();
    	this.expectedQueryParams.put("lat", "double");
    	this.expectedQueryParams.put("lon", "double");
        this.executor = Executors.newFixedThreadPool(NUM_THREADS);
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        Map<String, Object> queryParams = null;

        try {
            queryParams = super.getQueryParams(request, this.expectedQueryParams);
        } catch (Exception e) {
            super.writeErrorResponse(response, servletName, e.getMessage());
        }

        System.out.println("Received GET request!");
		
        Double lat = (Double) queryParams.get("lat");
        Double lon = (Double) queryParams.get("lon");
		
        System.out.println(lon);
        System.out.println(lat);
        //		System.out.println(StopsForLocation.getStops(lon, lat).toString());
        //		System.out.println(SchedulesForStop.getSchedules("MTA%20NYCT_400069"));
		
        JSONArray stops = StopsForLocation.getStops(Double.toString(lat), Double.toString(lon));
        //		System.out.println(stops);
        //		Collection<Future<JSONObject>> futureSchedules = new ArrayList<Future<JSONObject>>();
        CompletionService<JSONArray> completionService = 
            new ExecutorCompletionService<JSONArray>(this.executor);
        for (int i=0; i<stops.length(); i++) {
            JSONObject stop = stops.getJSONObject(i);
            completionService.submit(new Callable<JSONArray>() {
                    public JSONArray call() {
                        return SchedulesForStop.getSchedules(stop);
                    }
                });
        }
        JSONArray schedules = ScheduleMerger.mergeSchedules(completionService, stops, lat, lon);
		
        super.writeSelectResponse(response, servletName, schedules);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        doGet(request, response);
    }

}
