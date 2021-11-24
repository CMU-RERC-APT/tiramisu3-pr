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

@WebServlet("/StatusLogServlet")
public class StatusLogServlet extends AbstractServlet {
    private static final long serialVersionUID = 1L;
    private String servletName = "StatusLogServlet";
    private ArrayList<String> errors;
    private Map<String, String> expectedQueryParams;
    private Map<String, String> optionalQueryParams;

    public StatusLogServlet() {
        super();
        this.expectedQueryParams = new HashMap<String, String>();
        this.expectedQueryParams.put("device_id", "string");
        this.expectedQueryParams.put("device_platform","string");
        this.expectedQueryParams.put("app_version", "string");
        this.expectedQueryParams.put("screen_reader_on", "boolean");
        this.expectedQueryParams.put("location_available", "boolean");

        this.optionalQueryParams = new HashMap<String, String>();
        this.optionalQueryParams.put("user_id", "string");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        System.out.println("StatusLogServlet Received GET request!");

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
        String writeDataSql = "INSERT INTO log.status ("+columnString+", stamp) VALUES ("
            + valuesString + ", NOW())";
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
