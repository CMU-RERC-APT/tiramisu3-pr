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
 * Servlet implementation class Servlet
 */
@WebServlet(description = "Read from user_data.settings database", urlPatterns = { "/ReadSettingsServlet" })
public class ReadSettingsServlet extends AbstractServlet {
    private static final long serialVersionUID = 1L;
    private String servletName = "ReadSettingsServlet";
    private ArrayList<String> errors;
    private Map<String, String> expectedQueryParams;
    private Object[] values = new Object[8];

    public ReadSettingsServlet() {
        this.expectedQueryParams = new HashMap<String, String>();
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
    
    // Temporarily testing with http GET method
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Log Read Message");

        Map<String, Object> queryParams = null;

        try {
            queryParams = super.getQueryParams(request, this.expectedQueryParams);
        } catch (Exception e) {
            super.writeErrorResponse(response, servletName, e.getMessage());
        }

        System.out.println("Received GET request");
        // Check if mode is WRITE
        try {
            JSONArray settingsRead = readSettings(queryParams);
            super.writeSelectResponse(response, servletName, settingsRead);
        } catch (Exception e) {
        // TODO Auto-generated catch block
          e.printStackTrace();
        }
    }

    // Helper function to read parameters from request and write to data base
    public JSONArray readSettings(Map<String, Object> queryParams) throws SQLException{

        String readDisabilitiesSql = "WITH is_deaf AS (select * from user_data.settings WHERE is_deaf IS NOT NULL AND device_id = ? ORDER by stamp DESC LIMIT 1), need_seat AS (select * from user_data.settings WHERE need_seat IS NOT NULL AND device_id = ? ORDER by stamp DESC LIMIT 1), scooter_user AS (select * from user_data.settings WHERE scooter_user IS NOT NULL AND device_id = ? ORDER by stamp DESC LIMIT 1), show_map_on_start AS (select * from user_data.settings WHERE show_map_on_start IS NOT NULL AND device_id = ? ORDER by stamp DESC LIMIT 1), is_blind AS (select * from user_data.settings WHERE is_blind IS NOT NULL AND device_id = ? ORDER by stamp DESC LIMIT 1), walker_user AS (select * from user_data.settings WHERE walker_user IS NOT NULL AND device_id = ? ORDER by stamp DESC LIMIT 1), cog_disab AS (select * from user_data.settings WHERE cog_disab IS NOT NULL AND device_id = ? ORDER by stamp DESC LIMIT 1), other AS (select * from user_data.settings WHERE other IS NOT NULL AND device_id = ? ORDER by stamp DESC LIMIT 1) SELECT is_deaf.is_deaf, need_seat.need_seat, show_map_on_start.show_map_on_start,is_blind.is_blind, scooter_user.scooter_user, walker_user.walker_user, cog_disab.cog_disab, other.other FROM is_deaf FULL OUTER JOIN need_seat ON  is_deaf.device_id = need_seat.device_id FULL OUTER JOIN show_map_on_start ON show_map_on_start.device_id = is_deaf.device_id FULL OUTER JOIN is_blind ON is_blind.device_id = is_deaf.device_id FULL OUTER JOIN scooter_user ON scooter_user.device_id = is_deaf.device_id FULL OUTER JOIN walker_user ON walker_user.device_id = is_deaf.device_id FULL OUTER JOIN cog_disab ON cog_disab.device_id = is_deaf.device_id FULL OUTER JOIN other ON other.device_id = is_deaf.device_id;";
    
        if (queryParams!=null){
            this.values[0] = queryParams.get("device_id");
            this.values[1] = queryParams.get("device_id");
            this.values[2] = queryParams.get("device_id");
            this.values[3] = queryParams.get("device_id");
            this.values[4] = queryParams.get("device_id");
            this.values[5] = queryParams.get("device_id");
            this.values[6] = queryParams.get("device_id");
            this.values[7] = queryParams.get("device_id");
        }
            
        try {
            return super.doQuery(readDisabilitiesSql, this.values);           
        } catch (SQLException e) {
        	//TODO: Handle Error
        	throw e;
        }            
     }

    

}
