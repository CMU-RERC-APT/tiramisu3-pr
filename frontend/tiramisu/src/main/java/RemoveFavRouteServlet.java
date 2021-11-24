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
@WebServlet("/RemoveFavRouteServlet")
public class RemoveFavRouteServlet extends AbstractServlet{
    private static final long serialVersionUID = 1L;
    
    private String servletName = "RemoveFavRouteServlet";
    private ArrayList<String> errors;
    private Map<String, String> expectedQueryParams;

    public RemoveFavRouteServlet() {
	expectedQueryParams = new HashMap<String, String>();
	this.expectedQueryParams.put("device_id", "string");
	this.expectedQueryParams.put("user_lat", "double");
	this.expectedQueryParams.put("user_lon", "double");
	this.expectedQueryParams.put("agency_id", "string");
	this.expectedQueryParams.put("route_id", "string");
	this.expectedQueryParams.put("route_short_name", "string");
	this.expectedQueryParams.put("event", "string");
    }
    
    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	// TODO Auto-generated method stub
	doGet(request, response);
	System.out.println("pepitoelrobot");
    }
    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	System.out.println("Log Test Message");
	System.out.println("pepitoelrobot");

	Map<String, Object> queryParams = null;
	
	try {
	    queryParams = super.getQueryParams(request, this.expectedQueryParams);
	} catch (Exception e) {
	    super.writeErrorResponse(response, servletName, e.getMessage());
	}

	int numRoutesRemoved = -1;
	
	try {
	    numRoutesRemoved = removeRoute(queryParams);
	    super.writeUpdateResponse(response, servletName, numRoutesRemoved);
	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    // Helper function to read parameters from request and write to data base
    public int removeRoute(Map<String, Object> queryParams) throws SQLException{
	
	String removeRouteSql = "INSERT INTO user_data.route (device_id, user_lat, user_lon, agency_id, route_id, route_short_name, event, pair_id, stamp) "
	    + "SELECT ?, ?, ?, ?, ?, ?, ?, "
	    + "(SELECT MIN(id) FROM user_data.route WHERE (event = 'write' OR event = 'autowrite')"
            + "AND id NOT IN (SELECT pair_id FROM user_data.route WHERE (event = 'remove' OR event = 'autoremove') AND device_id = ? AND pair_id IS NOT NULL) "
	    + "AND device_id = ? "
	    + "AND agency_id = ? "
	    + "AND route_id = ?), "
	    + "NOW() "
            + "WHERE EXISTS "
            + "(SELECT * FROM user_data.route WHERE (event = 'write' OR event = 'autowrite')"
            + "AND device_id = ? "
            + "AND agency_id = ? "
            + "AND route_id = ? "
            + "AND id NOT IN (SELECT pair_id FROM user_data.route WHERE (event = 'remove' OR event = 'autoremove') AND device_id = ? AND pair_id IS NOT NULL))";

	Object[] values = {
	    queryParams.get("device_id"),
	    queryParams.get("user_lat"),
	    queryParams.get("user_lon"),
	    queryParams.get("agency_id"),
	    queryParams.get("route_id"),
	    queryParams.get("route_short_name"),
	    queryParams.get("event"),
	    queryParams.get("device_id"),
	    queryParams.get("device_id"),
	    queryParams.get("agency_id"),
	    queryParams.get("route_id"),
	    queryParams.get("device_id"),
	    queryParams.get("agency_id"),
	    queryParams.get("route_id"),
	    queryParams.get("device_id")
	};

	int numRoutesRemoved = -1;

	try {
	    numRoutesRemoved = super.doUpdate(removeRouteSql, values);
	} catch (SQLException e) {
	    // TODO handle error
	}

	return numRoutesRemoved;
    }
    
    
}
