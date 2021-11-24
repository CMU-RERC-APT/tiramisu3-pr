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
 * Servlet implementation class Servlet
 */
@WebServlet(description = "Write to user_data.search table", urlPatterns = { "/WriteSearchHistoryServlet" })
public class WriteSearchHistoryServlet extends AbstractServlet {
    private static final long serialVersionUID = 1L;

    private String servletName = "WriteSearchHistoryServlet";
    private ArrayList<String> errors;
    private Map<String, String> expectedQueryParams;
    private boolean is_recent;
	
    public WriteSearchHistoryServlet() {
	expectedQueryParams = new HashMap<String, String>();
	this.expectedQueryParams.put("device_id", "string");
	this.expectedQueryParams.put("user_lat", "double");
	this.expectedQueryParams.put("user_lon", "double");
	this.expectedQueryParams.put("query", "string");
	this.expectedQueryParams.put("place_lat", "double");
	this.expectedQueryParams.put("place_lon", "double");
	this.expectedQueryParams.put("is_recent", "boolean");
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
    
    // Temporarily testing with http GET method
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	System.out.println("Log Test Message");

	Map<String, Object> queryParams = null;

	try {
	    queryParams = super.getQueryParams(request, this.expectedQueryParams);
	} catch (Exception e) {
	    super.writeErrorResponse(response, servletName, e.getMessage());
	}

	int searchAdded = -1;

	try {
	    searchAdded = writeSearchHistory(queryParams);
	    super.writeUpdateResponse(response, servletName, searchAdded);
	} catch (SQLException e) {
	    throw new ServletException(e);
	}
    }

    // Helper function to read parameters from request and write to data base
    public int writeSearchHistory(Map<String, Object> queryParams) throws SQLException{

	String writeSearchSql = "INSERT INTO user_data.search (device_id, query, user_lat, user_lon, place_lat, place_lon, is_recent) "
	    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

	Object[] values = {
	    queryParams.get("device_id"),
	    queryParams.get("query"),
	    queryParams.get("user_lat"),
	    queryParams.get("user_lon"),
	    queryParams.get("place_lat"),
	    queryParams.get("place_lon"),
	    queryParams.get("is_recent")
	};

	int searchAdded = -1;
	
	searchAdded = super.doUpdate(writeSearchSql, values);

	return searchAdded;

    }

    

}
