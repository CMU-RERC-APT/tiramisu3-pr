package main.java;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import main.java.AbstractServlet;
import main.java.ResultSetJsonConverter;
/**
 * Servlet implementation class ReadAlarmServlet
 */
@WebServlet("/ReadAlarmServlet")
public class ReadAlarmServlet extends AbstractServlet{
    private static final long serialVersionUID = 1L;
    private String servletName = "ReadAlarmServlet";
    private Map<String, String> expectedQueryParams;


    /**
     * @see HttpServlet#HttpServlet()
     */
    public ReadAlarmServlet() {
        super();
        expectedQueryParams = new HashMap<String, String>();
        this.expectedQueryParams.put("registration_id", "string");
    	this.expectedQueryParams.put("device_id", "string");
    	this.expectedQueryParams.put("device_platform", "string");
    	//this.expectedQueryParams.put("user_id", "string");
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub		
        Map<String, Object> queryParams = null;
		
        try {
            queryParams = super.getQueryParams(request, this.expectedQueryParams);
        } catch (Exception e) {
            super.writeErrorResponse(response, servletName, e.getMessage());
        }
		
        try {
            JSONArray setAlarms = readAlarm(queryParams);
            super.writeSelectResponse(response, servletName, setAlarms);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
	
    private JSONArray readAlarm(Map<String, Object> queryParams) throws SQLException {
        String readAlarmSql = "SELECT * FROM user_data.alarm WHERE "
            + "event = 'put' "
            + "AND device_id = ? "
            + "AND device_platform = ? "
            //+ "AND user_id = ? "
            + "AND id NOT IN (SELECT pair_id FROM user_data.alarm WHERE event = 'expire' OR event = 'remove'); ";

        Object[] values = {
            queryParams.get("device_id"),
            queryParams.get("device_platform"),
            //queryParams.get("user_id")
        };
                
        try {
        	return super.doQuery(readAlarmSql, values);            
        } catch (SQLException e) {
            //TODO: handle error
        	throw e;
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
