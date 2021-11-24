package main.java;

import java.io.Console;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import main.java.util.JsonUtil;
import main.java.util.UrlUtil;

/**
 * Servlet implementation class ArrivalForTripServlet
 */
@WebServlet("/ArrivalForTripServlet")
public class ArrivalForTripServlet extends AbstractServlet {
    private static final long serialVersionUID = 1L;
    private HashMap<String, String> expectedQueryParams;
    private String servletName = "ArrivalForTripServlet";
    private String obaTripDetailServletPath = "trip-details";
    private String obaArrivalForStopPath = "arrival-and-departure-for-stop";
    /* Is API_KEY right? */
    private final String APIKey = System.getProperty("OBA_API_KEY");
    private final String backendUrl = System.getProperty("OBA_URL");




    /**
     * @see HttpServlet#HttpServlet()
     */
    public ArrivalForTripServlet() {
        super();
        this.expectedQueryParams = new HashMap<String, String>();
        this.expectedQueryParams.put("trip_id", "string");
        this.expectedQueryParams.put("service_date", "string");
        this.expectedQueryParams.put("stop_sequence", "string");
        this.expectedQueryParams.put("current_stop_id", "string");

        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Received get request:" + request.toString());
        Map<String, Object> queryParams = null;
        try {
            queryParams = super.getQueryParams(request, this.expectedQueryParams);
        } catch (Exception e) {
            super.writeErrorResponse(response, servletName, e.getMessage());
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", this.APIKey);
        String serviceDate = queryParams.get("service_date").toString();
        String stopSequence = queryParams.get("stop_sequence").toString();
        params.put("serviceDate", serviceDate);
        URL tripDetailURL = null;

        String tripId = queryParams.get("trip_id").toString();
        String currentStopId = queryParams.get("current_stop_id").toString();
        String fullTripDetailPath = obaTripDetailServletPath+"/"+tripId+".json";
        try {
            tripDetailURL = new URL(UrlUtil.makeURL(backendUrl, fullTripDetailPath, params));
            System.out.println("OBA URL:"+tripDetailURL.toString());
            JSONObject OBAresponse = JsonUtil.readJsonFromUrl(tripDetailURL.toString());
            //need to get arrival time via another call
            JSONObject resp = OBAresponse.getJSONObject("data");
            JSONObject ret = new JSONObject();
            ret.put("references", resp.getJSONObject("references"));
            JSONArray stopTimes = resp.getJSONObject("entry").getJSONObject("schedule").getJSONArray("stopTimes");
            JSONArray compressedStopTimes = new JSONArray();

            boolean currentStopSeen = false; //know that the current stop must be in the stop list
            for (int i = 0; i < stopTimes.length(); i++){
              JSONObject stopTime = (JSONObject) stopTimes.get(i);
              String stopId = stopTime.getString("stopId");
              if (currentStopId.equals(stopId)) currentStopSeen = true;
              if (currentStopSeen){
                JSONObject compressedEntry = getArrivalTimeForStop(stopId, tripId, serviceDate, stopSequence);
                compressedStopTimes.put(compressedEntry);
              }
            }
            ret.put("stopTimes", compressedStopTimes);
            response.getWriter().println(ret);
            //super.writeSelectResponse(response, servletName, compressedStopTimes);
        }
        catch (Exception e){
            // ??? error handling?
            e.printStackTrace();
        }

    }

    private JSONObject getArrivalTimeForStop(String stopId, String tripId, String serviceDate, String stopSequence) {
        URL arrivalForStopURL = null;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", this.APIKey);
        params.put("tripId", tripId);
        params.put("serviceDate", serviceDate);
        params.put("stopSequence", stopSequence);

        JSONObject ret = null;
        String fullArrivalForStopPath = obaArrivalForStopPath + "/" + stopId + ".json";
        try {
            arrivalForStopURL = new URL(UrlUtil.makeURL(backendUrl, fullArrivalForStopPath, params));
            JSONObject OBAresponse = JsonUtil.readJsonFromUrl(arrivalForStopURL.toString());
            JSONObject entry = OBAresponse.getJSONObject("data").getJSONObject("entry");

            /* compress to necessary information*/
            ret = new JSONObject();
            ret.put("predicted", entry.get("predicted"));
            ret.put("predictedArrivalTime", entry.get("predictedArrivalTime"));
            ret.put("predictedDepartureTime", entry.get("predictedDepartureTime"));
            ret.put("scheduledArrivalTime", entry.get("scheduledArrivalTime"));
            ret.put("scheduledDepatureTime", entry.get("scheduledDepartureTime"));
            ret.put("serviceDate", entry.get("serviceDate"));
            ret.put("tripId", entry.get("tripId"));
            ret.put("stopId", entry.get("stopId"));
            ret.put("stopSequence", entry.get("stopSequence"));

        } catch (Exception e) {
            System.out.println("obaArrivalForStop error");
            e.printStackTrace();
        }
        return ret;
    }


    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
