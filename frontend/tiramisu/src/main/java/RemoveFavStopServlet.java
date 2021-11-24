package main.java;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.sql.SQLException;

/**
 * Servlet implementation class RemoveFavServlet
 */
@WebServlet("/RemoveFavStopServlet")
public class RemoveFavStopServlet extends AbstractServlet{
    private static final long serialVersionUID = 1L;

    private String servletName = "RemoveFavStopServlet";
    private ArrayList<String> errors;
    private Map<String, String> expectedQueryParams;
    
    public RemoveFavStopServlet() {
	expectedQueryParams = new HashMap<String, String>();
	this.expectedQueryParams.put("device_id", "string");
	this.expectedQueryParams.put("user_lat", "double");
	this.expectedQueryParams.put("user_lon", "double");
	this.expectedQueryParams.put("agency_id", "string");
	this.expectedQueryParams.put("stop_id", "string");
	this.expectedQueryParams.put("stop_name", "string");
	this.expectedQueryParams.put("stop_lat", "double");
	this.expectedQueryParams.put("stop_lon", "double");
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
	System.out.println("Log Test Message");

	Map<String, Object> queryParams = null;

	try {
	    queryParams = super.getQueryParams(request, this.expectedQueryParams);
	} catch (Exception e) {
	    super.writeErrorResponse(response, servletName, e.getMessage());
	}

	int numStopsRemoved = -1;
	
	try {
	    numStopsRemoved = removeStop(queryParams);
	    super.writeUpdateResponse(response, servletName, numStopsRemoved);
	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
    }
    
    // Helper function to read parameters from request and write to data base
    public int removeStop(Map<String, Object> queryParams) throws SQLException{
	
	String removeStopSql = "INSERT INTO user_data.stop (device_id, user_lat, user_lon, agency_id, stop_id, stop_name, stop_lat, stop_lon, event, pair_id, stamp) "
	    + "SELECT ?, ?, ?, ?, ?, ?, ?, ?, 'remove', "
	    + "(SELECT MIN(id) FROM user_data.stop WHERE event = 'write'"
            + "AND id NOT IN (SELECT pair_id FROM user_data.stop WHERE event = 'remove' AND device_id = ? AND pair_id IS NOT NULL) "
	    + "AND device_id = ? "
	    + "AND agency_id = ? "
	    + "AND stop_id = ?), "
	    + "NOW() "
            + "WHERE EXISTS "
            + "(SELECT * FROM user_data.stop WHERE event = 'write' "
            + "AND device_id = ? "
            + "AND agency_id = ? "
            + "AND stop_id = ? "
            + "AND id NOT IN (SELECT pair_id FROM user_data.stop WHERE event = 'remove' AND device_id = ? AND pair_id IS NOT NULL))";

	Object[] values = {
	    queryParams.get("device_id"),
	    queryParams.get("user_lat"),
	    queryParams.get("user_lon"),
	    queryParams.get("agency_id"),
	    queryParams.get("stop_id"),
	    queryParams.get("stop_name"),
	    queryParams.get("stop_lat"),
	    queryParams.get("stop_lon"),
	    queryParams.get("device_id"),
	    queryParams.get("device_id"),
	    queryParams.get("agency_id"),
	    queryParams.get("stop_id"),
	    queryParams.get("device_id"),
	    queryParams.get("agency_id"),
	    queryParams.get("stop_id"),
	    queryParams.get("device_id")
	};

	int numStopsRemoved = -1;

	try {
	    numStopsRemoved = super.doUpdate(removeStopSql, values);
	} catch (SQLException e) {
	    // TODO handle error
	}

	return numStopsRemoved;
    }
    
}
