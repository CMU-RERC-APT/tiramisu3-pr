package main.java;

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

	while(resultSet.next()) {
	    JSONObject resultElement = new JSONObject();	    

	    for(int i = 1; i < numColumns + 1; i++) {

		String columnValue = resultSet.getString(i);

		if(resultSet.wasNull()) {
		    
		    resultElement.put(rsmd.getColumnLabel(i), "");

		} else {
		    
		    resultElement.put(rsmd.getColumnLabel(i),
				      resultSet.getString(i));
		}
	    }

	    resultJson.put(resultElement);
	}

	return resultJson;
    }
}
