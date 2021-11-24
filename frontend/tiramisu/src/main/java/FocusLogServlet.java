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

@WebServlet("/FocusLogServlet")
public class FocusLogServlet extends AbstractServlet {
    private static final long serialVersionUID = 1L;
    private String servletName = "FocusLogServlet";
    private ArrayList<String> errors;
    private Map<String, String> expectedQueryParams;
    private Map<String, String> optionalQueryParams;

    public FocusLogServlet() {
        super();
        this.expectedQueryParams = new HashMap<String, String>();
        this.expectedQueryParams.put("device_id", "string");
        this.expectedQueryParams.put("device_platform","string");
        this.expectedQueryParams.put("user_lon", "double");
        this.expectedQueryParams.put("user_lat", "double");
        this.expectedQueryParams.put("event", "string");

        this.optionalQueryParams = new HashMap<String, String>();
        this.optionalQueryParams.put("user_id", "string");
        this.optionalQueryParams.put("stamp", "unix_time");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        System.out.println("FocusLogServlet Received GET request!");

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

        String writeDataSql = "";
        if(queryParams.containsKey("stamp")) {
            writeDataSql = "INSERT INTO log.focus ("+columnString+") VALUES ("
                + valuesString + ")";
        } else {
            writeDataSql = "INSERT INTO log.focus ("+columnString+", stamp) VALUES ("
                + valuesString + ", NOW())";
        }
        Object[] values = super.generateValues(queryParams);
        System.out.println(writeDataSql);
        super.printValues(values);

        int dataAdded = -1;
        dataAdded = super.doUpdate(writeDataSql, values);
        return dataAdded;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
