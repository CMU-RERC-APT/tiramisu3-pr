package main.java.util;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;

import main.java.AbstractServlet;

public class ExpCondUtil {

    private static AbstractServlet parent = new AbstractServlet();
    
    public static int getExperimentCondition(String device_id, int experiment_id) {
        String getCondSql = "SELECT condition_num FROM exp.user_condition WHERE device_id = ? AND experiment_id = ? ORDER BY stamp ASC;";

        Object[] values = {
            device_id,
            experiment_id
        };

        try {
            JSONArray result = parent.doQuery(getCondSql, values);           
            if(result.length() > 0) {
                return ((Integer)result.get(0));
            }
        } catch (Exception e) {
            System.out.println("There's no assigned condition.... Randmly assigning a new one.");
        }
        return -1;
    }

    public static void setExperimentCondition(String device_id, int experiment_id, int condition_num) {

        String setCondSql = "INSERT INTO exp.user_condition (device_id, experiment_id, condition_num) "
            + "SELECT ?, ?, ? "
            + "WHERE NOT EXISTS (SELECT * FROM exp.user_condition WHERE device_id = ?)";

        Object[] values = {
            device_id,
            experiment_id,
            condition_num,
            device_id
        };

        int rowsAdded = -1;

        try {
            rowsAdded = parent.doUpdate(setCondSql, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
