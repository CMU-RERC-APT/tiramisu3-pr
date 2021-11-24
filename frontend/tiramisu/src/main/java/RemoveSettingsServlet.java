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
@WebServlet(description = "Write to user_data.settings database", urlPatterns = { "/RemoveSettingsServlet" })
public class RemoveSettingsServlet extends AbstractServlet {
    private static final long serialVersionUID = 1L;

    private String servletName = "RemoveSettingsServlet";
    private ArrayList<String> errors;
    private Map<String, String> expectedQueryParams;

    public RemoveSettingsServlet() {
        expectedQueryParams = new HashMap<String, String>();
        this.expectedQueryParams.put("device_id", "string");
        this.expectedQueryParams.put("user_lat", "double");
        this.expectedQueryParams.put("user_lon", "double");
        this.expectedQueryParams.put("disability_info", "string");
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

    int settingsRemoved = -1;

    try {
        settingsRemoved = removeSettings(queryParams);
        super.writeUpdateResponse(response, servletName, settingsRemoved);
    } catch (SQLException e) {
        throw new ServletException(e);
    }
    }

    // Helper function to read parameters from request and write to data base
    public int removeSettings(Map<String, Object> queryParams) throws SQLException{

    String removeSettingsSql = "INSERT INTO user_data.settings (device_id, user_lat, user_lon, " +queryParams.get("disability_info")+ ", stamp) "
        + "SELECT ?, ?, ?, false, NOW() ";
            
    Object[] values = {
        queryParams.get("device_id"),
        queryParams.get("user_lat"),
        queryParams.get("user_lon")
    };

    int settingsRemoved = -1;
    
    settingsRemoved = super.doUpdate(removeSettingsSql, values);

    return settingsRemoved;

    }

    

}

