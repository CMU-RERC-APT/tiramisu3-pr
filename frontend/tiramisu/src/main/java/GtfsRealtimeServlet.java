package main.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;

import main.java.AbstractServlet;
import main.java.realtime.GtfsRealtimeProvider;

/**
 * Servlet implementation class GtfsRealtimeServlet
 */
@WebServlet("/GtfsRealtimeServlet")
public class GtfsRealtimeServlet extends AbstractServlet {
    private static final long serialVersionUID = 1L;
       
    /**
     * @see AbstractServlet#AbstractServlet()
     */
    private String servletName = "GtfsRealtimeServlet";
    private ArrayList<String> errors;
    private Map<String, String> expectedQueryParams;
    
    private GtfsRealtimeProvider provider;
    private FeedMessage MTAvehiclePositions = null;
    private FeedMessage MTAtripUpdates = null;
    private FeedMessage PAACtripUpdates = null;
    private FeedMessage PAACvehiclePositions = null;
	
    private long lastUpdateTime;
	
    public GtfsRealtimeServlet() {
        super();
        // TODO Auto-generated constructor stub
        provider = new GtfsRealtimeProvider();
        provider.buildFeed();
        MTAvehiclePositions = provider.MTAvehiclePositions;
        MTAtripUpdates = provider.MTAtripUpdates;
        PAACtripUpdates = provider.PAACtripUpdates;
        PAACvehiclePositions = provider.PAACvehiclePositions;
        
        expectedQueryParams = new HashMap<String, String>();
        expectedQueryParams.put("agency", "string");
        expectedQueryParams.put("update", "string");
        expectedQueryParams.put("debug", "boolean");
        
        lastUpdateTime = System.currentTimeMillis();
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
		
        long currentTime = System.currentTimeMillis();
        if (currentTime-lastUpdateTime>=30000) {
            provider.buildFeed();
            MTAvehiclePositions = provider.MTAvehiclePositions;
            MTAtripUpdates = provider.MTAtripUpdates;
            PAACtripUpdates = provider.PAACtripUpdates;
            PAACvehiclePositions = provider.PAACvehiclePositions;
            lastUpdateTime = System.currentTimeMillis();
        }
		
        String agency = (String) queryParams.get("agency");
        String update = (String) queryParams.get("update");
        boolean debug = (boolean) queryParams.get("debug");
		
        switch (agency+" "+update) {
        case "PAAC tripUpdates":
            if (debug) {
                response.getWriter().println(PAACtripUpdates);
            }else{
                PAACtripUpdates.writeTo(response.getOutputStream());
            }
            break;
        case "PAAC vehiclePositions":
            if (debug) {
                response.getWriter().println(PAACvehiclePositions);
            }else{
                PAACvehiclePositions.writeTo(response.getOutputStream());
            }
            break;
        case "MTA tripUpdates":
            if (debug) {
                response.getWriter().println(MTAtripUpdates);
            }else{
                MTAtripUpdates.writeTo(response.getOutputStream());
            }
            break;
        case "MTA vehiclePositions":
            if (debug) {
                response.getWriter().println(MTAvehiclePositions);
            }else{
                MTAvehiclePositions.writeTo(response.getOutputStream());
            }
            break;
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        doGet(request, response);
    }

}
