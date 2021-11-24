package main.java;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Time;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import main.java.Database;

import java.util.logging.Logger;
import static java.util.logging.Level.*;

/**
 * Servlet implementation class AbstractServlet
 */
//@WebServlet("/AbstractServlet")
public class AbstractServlet extends HttpServlet {
    // A version number for each serializable class
    // if not explicitly specified, a value is generated on the run, but it may be brittle
    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(AbstractServlet.class.getName());

    private Database database;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AbstractServlet() {
    	try {
    		database = Database.getInstance();
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    }


    protected Map<String, Object> getQueryParams(HttpServletRequest request, Map<String, String> expectedQueryParams) 
        throws Exception{
        return getQueryParams(request, expectedQueryParams, new HashMap<String, String>());
    }
    
    protected Map<String, Object> getQueryParams(HttpServletRequest request, Map<String, String> expectedQueryParams, 
                                                 Map<String, String> optionalQueryParams) throws Exception {
    	
          /* Used linked hash map to make sure queryParam iterates in the same order,
           * so that later the columns and values are matched correctly*/
      Map<String, Object> queryParams = new LinkedHashMap<String, Object>();

      for(String key: expectedQueryParams.keySet()) {
          String param = request.getParameter(key);
          String paramType = expectedQueryParams.get(key);
          try {

          	Object paramValue = Validator.validateParam(param, paramType);
                  queryParams.put(key, paramValue);

          } catch (Exception e) {
            this.logger.log(SEVERE, "Exception: " + e.getMessage()); // highest Level of SEVERE
            throw new Exception("Error parsing required parameter: " + key + " type/format: " + paramType);
          }
      }

      for (String key: optionalQueryParams.keySet()){
          String param = request.getParameter(key);
          String paramType = optionalQueryParams.get(key);
          try {
          	if (param != null){
                      Object paramValue = Validator.validateParam(param, paramType);
                      queryParams.put(key, paramValue);
          	}

          } catch (Exception e) {
            this.logger.log(SEVERE, "Exception: " + e.getMessage()); // highest Level of SEVERE
      	    throw new Exception("Error parsing optional parameter: " + key + " type/format: " + paramType);
          }
      	
      }
          return queryParams;
    }

    // To be used for SELECT
    public JSONArray doQuery(String sql, Object[] values) throws SQLException {
    	return database.doQuery(sql, values);
    }

    // To be used for UPDATE and INSERT
    public int doUpdate(String sql, Object[] values) throws SQLException {
    	return database.doUpdate(sql, values);
    }
  
  
    protected void writeSelectResponse(HttpServletResponse response, String servletName, JSONArray data) throws IOException {

	  	response.setContentType("application/json");
	
	  	JSONObject jsonResponse = new JSONObject();
	
	  	jsonResponse.put("servlet", servletName);
	  	jsonResponse.put("data", data);
	
	  	response.getWriter().println(jsonResponse);

    }

    protected void writeUpdateResponse(HttpServletResponse response, String servletName, int rowsAffected) throws IOException {

	  	JSONObject responseData = new JSONObject();
	
	  	responseData.put("rowsAffected", rowsAffected);
	
	  	JSONArray responseDataWrapper = new JSONArray();
	
	  	responseDataWrapper.put(responseData);
	
	  	writeSelectResponse(response, servletName, responseDataWrapper);
    }

    protected void writeErrorResponse(HttpServletResponse response, String servletName, ArrayList<String> errors) throws IOException {

	  	JSONObject responseData = new JSONObject();
	  	JSONArray errorList = new JSONArray();
	
	  	for(String error: errors) {
	  	    errorList.put(error);
	  	}
	
	  	responseData.put("errors", errorList);
	
	  	JSONArray responseDataWrapper = new JSONArray();
	
	  	responseDataWrapper.put(responseData);
	
	  	writeSelectResponse(response, servletName, responseDataWrapper);
    }

    protected void writeErrorResponse(HttpServletResponse response, String servletName, String error) throws IOException {

	  	JSONObject responseData = new JSONObject();
	
	  	responseData.put("error", error);
	
	  	JSONArray responseDataWrapper = new JSONArray();
	
	  	responseDataWrapper.put(responseData);
	
	  	writeSelectResponse(response, servletName, responseDataWrapper);
    }
	
    protected Object[] generateValues(Map<String, Object> queryParams) {
	  	ArrayList<Object> ret = new ArrayList<Object>();
	  	for(String key: queryParams.keySet()) {
	            ret.add(queryParams.get(key));
	  	}
	  	return ret.toArray();
    }

    protected String generateSqlColumnString(Map<String, Object> queryParams) {
        String ret = "";
        for (String key: queryParams.keySet()) {
            ret += key+", ";
        }
        ret = ret.replaceAll(", $", "");
		
        return ret;
    }

    public String generateSqlValuesString(Map<String, Object> queryParams) {
		
        String ret = "";
        for (String key: queryParams.keySet()) {
            ret += "?, ";
        }
        ret = ret.replaceAll(", $", "");
        return ret;
    }

    // Helper function: check Object[] type and value
    public void printValues(Object[] values) {
	  for (Object value: values) {
	    System.out.println(value.getClass().getName() + ": " + value);
	  }
    }

    
}
