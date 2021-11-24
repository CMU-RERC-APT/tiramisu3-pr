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

import org.json.JSONArray;

/**
 * Servlet implementation class ReadFavStops
 */
@WebServlet("/ReadFavStopsServlet")
public class ReadFavStopsServlet extends AbstractServlet {
    // A version number for each serializable class
	// if not explicitly specified, a value is generated on the run, but it may be brittle
    private static final long serialVersionUID = 1L;
    
    private String servletName = "ReadFavStopsServlet";
    private ArrayList<String> errors;
    private Map<String, String> expectedQueryParams;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReadFavStopsServlet() {
	expectedQueryParams = new HashMap<String, String>();
	this.expectedQueryParams.put("device_id", "string");
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
	// Check if mode is WRITE
	try {
	    JSONArray favStops = readFavStops(queryParams);
	    super.writeSelectResponse(response, servletName, favStops);

	} catch (Exception e) {
	    throw new ServletException(e);
	}
    }
    
    // Helper function to read parameters from request and write to data base
    public JSONArray readFavStops(Map<String, Object> queryParams) throws SQLException{
	
		String readStopsSql = "SELECT stop_id, stop_name, stop_lat, stop_lon, agency_id FROM user_data.stop WHERE device_id = ? "
		    + "AND event = 'write' "
		    + "AND id NOT IN (SELECT pair_id FROM user_data.stop WHERE event = 'remove' "
	            + "AND pair_id IS NOT NULL "
		    + "AND device_id = ?)";
	
		Object[] values = {
		    queryParams.get("device_id"),
		    queryParams.get("device_id")
		};
	
		return super.doQuery(readStopsSql, values);		   		
    }
}
