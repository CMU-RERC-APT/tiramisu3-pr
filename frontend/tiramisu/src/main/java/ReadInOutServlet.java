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

import main.java.util.*;

/**
 * Servlet implementation class ReadFavRoutes
 */
@WebServlet("/ReadInOutServlet")
public class ReadInOutServlet extends AbstractServlet {
    // A version number for each serializable class
	// if not explicitly specified, a value is generated on the run, but it may be brittle
    private static final long serialVersionUID = 1L;
    
    private String servletName = "ReadInOutServlet";
    private ArrayList<String> errors;
    private Map<String, String> expectedQueryParams;

    private static String predictUrl = System.getProperty("PREDICT_URL");
    private static String inoutPath = System.getProperty("INOUT_PATH");
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReadInOutServlet() {
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

	System.out.println("Received GET request!");

        JSONArray inout = new JSONArray();
        //inout.put("");
        //super.writeSelectResponse(response, servletName, inout);

        int expCond = ExpCondUtil.getExperimentCondition((String)queryParams.get("device_id"), 1);

        if(expCond == -1) {
            expCond = ThreadLocalRandom.current().nextInt(0, 2);
            ExpCondUtil.setExperimentCondition((String)queryParams.get("device_id"), 1, expCond);
        }

        if(expCond == 0) {
            inout.put("");
        } else if (expCond == 1 || expCond == 2) {
        
            String predictedInOut = getPredictedInOut(queryParams);
            inout.put(predictedInOut);
            if (!predictedInOut.equals("")) {
                logWrite(queryParams, predictedInOut);
            }
        }
        super.writeSelectResponse(response, servletName, inout);
        
    }


    public void logWrite(Map<String, Object> queryParams, String predictedInOut) {
        Map<String, String> params = convertMap(queryParams);
        params.put("device_platform", "backend");
        params.put("page", "ReadInOutServlet");
        params.put("button_type", predictedInOut);
        params.put("event", "autowrite");

        String url = UrlUtil.makeURL("http://localhost:8080/tiramisu/", "ButtonLogServlet", params);

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

    
    public String getPredictedInOut(Map<String, Object> queryParams) {
        Map<String, String> params = convertMap(queryParams);
        String url = UrlUtil.makeURL(predictUrl, inoutPath, params);
        try {
            String predictedInOut = JsonUtil.readJsonFromUrl(url).getString("data");
            System.out.println(predictedInOut);
            return predictedInOut;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
}
