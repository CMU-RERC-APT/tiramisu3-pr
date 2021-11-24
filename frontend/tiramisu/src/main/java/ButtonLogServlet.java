package main.java;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import main.java.AbstractServlet;

/**
 * Servlet implementation class DataCollectionServlet
 */
@WebServlet("/ButtonLogServlet")
public class ButtonLogServlet extends AbstractServlet {
    private static final long serialVersionUID = 1L;
    private String servletName = "ButtonLogServlet";
    private ArrayList<String> errors;
    private Map<String, String> expectedQueryParams;
    private Map<String, String> optionalQueryParams;
        
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ButtonLogServlet() {
        super();
        this.expectedQueryParams = new HashMap<String, String>();
        this.expectedQueryParams.put("device_id", "string");
        this.expectedQueryParams.put("device_platform", "string");
        this.expectedQueryParams.put("user_lon", "double");
        this.expectedQueryParams.put("user_lat", "double");
        this.expectedQueryParams.put("page", "string");
        this.expectedQueryParams.put("button_type", "string");
        
        this.optionalQueryParams = new HashMap<String, String>();
        this.optionalQueryParams.put("user_id", "string");
        this.optionalQueryParams.put("route_id", "string");
        this.optionalQueryParams.put("stop_id", "string");
        this.optionalQueryParams.put("trip_id", "string");
        this.optionalQueryParams.put("arrival_time", "unix_time");
        this.optionalQueryParams.put("event", "string");
        
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("ButtonLogServlet Received GET request!");

        // TODO Auto-generated method stub
        Map<String, Object> queryParams = null;

        try {
            queryParams = super.getQueryParams(request, this.expectedQueryParams, this.optionalQueryParams);
        } catch (Exception e) {
            super.writeErrorResponse(response, servletName, e.getMessage());
        }

                
        int dataAdded = -1;
                                
        try {
            dataAdded = writeData(queryParams);
            super.writeUpdateResponse(response, servletName, dataAdded);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
        
    public int writeData(Map<String, Object> queryParams) throws SQLException{
                
        String columnString = super.generateSqlColumnString(queryParams);
        String valuesString = super.generateSqlValuesString(queryParams);
        String writeDataSql = "INSERT INTO log.button ("+columnString+", stamp) VALUES ("
            + valuesString + ", NOW())";
                
        Object[] values = super.generateValues(queryParams);
                
        System.out.println(writeDataSql);
        super.printValues(values);
                
        int dataAdded = -1;
                
        dataAdded = super.doUpdate(writeDataSql, values);

        return dataAdded;

    }
        

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        doGet(request, response);
    }

}
