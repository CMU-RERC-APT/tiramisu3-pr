package main.java;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import main.java.AbstractServlet;

/**
 * Servlet implementation class SetAlarmServlet
 */
@WebServlet("/SetAlarmServlet")
public class SetAlarmServlet extends AbstractServlet {
	
    private String servletName = "SetAlarmServlet";
    private static final long serialVersionUID = 1L;
    private Map<String, String> expectedQueryParams;
    private Map<String, String> optionalQueryParams;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetAlarmServlet() {
    	expectedQueryParams = new HashMap<String, String>();
    	this.expectedQueryParams.put("device_id", "string");
    	this.expectedQueryParams.put("device_platform", "string");
    	this.expectedQueryParams.put("registration_id", "string");
    	this.expectedQueryParams.put("user_lon", "double");
    	this.expectedQueryParams.put("user_lat", "double");
    	this.expectedQueryParams.put("route_name", "string");
    	this.expectedQueryParams.put("trip_headsign", "string");
    	this.expectedQueryParams.put("trip_id", "string");
    	this.expectedQueryParams.put("stop_id", "string");
    	this.expectedQueryParams.put("stop_name", "string");
    	this.expectedQueryParams.put("service_date", "unix_time");

        this.optionalQueryParams = new HashMap<String, String>();
        this.optionalQueryParams.put("user_id", "string");

    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub

        Map<String, Object> queryParams = null;

        try {
            queryParams = super.getQueryParams(request, this.expectedQueryParams, this.optionalQueryParams);
        } catch (Exception e) {
            super.writeErrorResponse(response, servletName, e.getMessage());
        }
		

        int routesAdded = -1;

        try {
            routesAdded = putAlarm(queryParams);
            super.writeUpdateResponse(response, servletName, routesAdded);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
		
    }
	
    public int putAlarm(Map<String, Object> queryParams) throws SQLException{

        String writeRouteSql = "INSERT INTO user_data.alarm (event, device_id, device_platform, registration_id, user_lon, user_lat, route_name, trip_headsign, trip_id, stop_id, stop_name, service_date, stamp) "
            + "SELECT 'put', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW() "
            + "WHERE NOT EXISTS ( "
            + "SELECT * FROM user_data.alarm WHERE event = 'put' "
            + "AND id NOT IN (SELECT pair_id FROM user_data.alarm WHERE (event = 'expire' OR event = 'remove') AND device_id = ?) "
            + "AND device_id = ? "
            + "AND registration_id = ? "
            + "AND trip_id = ? "
            + "AND stop_id = ? "
            + "AND service_date = ?)";
		
        Object[] values = {
            queryParams.get("device_id"),
            queryParams.get("device_platform"),
            queryParams.get("registration_id"),
            queryParams.get("user_lon"),
            queryParams.get("user_lat"),
            queryParams.get("route_name"),
            queryParams.get("trip_headsign"),
            queryParams.get("trip_id"),
            queryParams.get("stop_id"),
            queryParams.get("stop_name"),
            queryParams.get("service_date"),
            queryParams.get("device_id"),
            queryParams.get("device_id"),
            queryParams.get("registration_id"),
            queryParams.get("trip_id"),
            queryParams.get("stop_id"),
            queryParams.get("service_date")
        };
		
        int routesAdded = -1;
		
        /* should we detect if device_id, service_date, trip_id, stop_id tuple is duplicate?*/
        routesAdded = super.doUpdate(writeRouteSql, values);
	
        return routesAdded;

    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
