package cmu.edu.alarm_processor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class ResultSetJsonConverter {

    public static JSONArray toJson(ResultSet resultSet) throws SQLException {

	JSONArray resultJson = new JSONArray();

	ResultSetMetaData rsmd = resultSet.getMetaData();
	int numColumns = rsmd.getColumnCount();

	//according to postgresql doc resultSet is initially before the first row
	//calling next on it will move it to the first row
	while(resultSet.next()) {
	    JSONObject resultElement = new JSONObject();	    

	    for(int i = 1; i < numColumns + 1; i++) {

			String columnValue = resultSet.getString(i);
	
			if(resultSet.wasNull()) {
			    
			    resultElement.put(rsmd.getColumnLabel(i), "");
	
			} else {
			    
			    resultElement.put(rsmd.getColumnLabel(i),
			    		columnValue);
			}
	    }

	    resultJson.put(resultElement);
	}

	return resultJson;
    }
}
