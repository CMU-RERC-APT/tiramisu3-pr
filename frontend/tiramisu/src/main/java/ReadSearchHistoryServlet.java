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
 * Servlet implementation class ReadSearchHistoryServlet
 */
@WebServlet("/ReadSearchHistoryServlet")
public class ReadSearchHistoryServlet extends AbstractServlet {
    // A version number for each serializable class
	// if not explicitly specified, a value is generated on the run, but it may be brittle
    private static final long serialVersionUID = 1L;
    
    private String servletName = "ReadSearchHistoryServlet";
    private ArrayList<String> errors;
    private Map<String, String> expectedQueryParams;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReadSearchHistoryServlet() {
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
	    JSONArray recentSearch = readSearchHistory(queryParams);
	    super.writeSelectResponse(response, servletName, recentSearch);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    // Helper function to read parameters from request and write to data base
    public JSONArray readSearchHistory(Map<String, Object> queryParams) throws SQLException{
	
		//String readHistorySql = "SELECT DISTINCT query, place_lat, place_lon, stamp FROM user_data.search WHERE device_id = ? " + 
	    //        "AND is_repeat = FALSE ORDER BY stamp DESC LIMIT 5";
	    String readHistorySql = "SELECT query, place_lat, place_lon, stamp FROM user_data.search WHERE device_id = ? " + 
	    "AND id IN (SELECT MAX(id) FROM user_data.search WHERE device_id = ? GROUP BY query) ORDER BY stamp DESC LIMIT 5";
		// add distinct
	
		Object[] values = {
		    queryParams.get("device_id"),
		    queryParams.get("device_id")
		};
	
		try {
			return super.doQuery(readHistorySql, values);		    
		} catch (SQLException e) {
		    //TODO Handle Error
			throw e;
		}	
    }
}
