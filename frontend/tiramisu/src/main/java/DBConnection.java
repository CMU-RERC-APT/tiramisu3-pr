package main.java;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;


/* 
 * Connection class for push
 * */
public class DBConnection {
	
	// set up to access local database
	/* TO DO: these probably need to be in a properties file?*/
	public static final String USERNAME = "postgres";
	public static final String PASSWORD = "syz2xxx1314";
	public static final String CONN_STRING =
			"jdbc:postgresql://localhost/TiramisuV3";

	public Connection conn;
	
	public DBConnection(){
		
		conn = null;
		
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		try {
			System.out.println("Connnecting to database...\n");
			conn = DriverManager.getConnection(CONN_STRING, USERNAME, PASSWORD);
			System.out.println("Connected!\n");
		} catch (SQLException e) {
			System.err.println(e);
		}
		
	}
	
	/* Copied from AbstractServlet.java*/
    // To be used for UPDATE and INSERT
    protected int doUpdate(String sql, Object[] values) throws SQLException {

	int numRowsAffected = -1;

	try {

	    PreparedStatement ps = conn.prepareStatement(sql);
	    substituteValues(ps, values);

	    numRowsAffected = ps.executeUpdate();

	} catch (Exception e) {
	    throw new SQLException(e);
	    // TODO handle error
	}

		return numRowsAffected;
    }
    
	/* copied from abstractServlet*/
    // To be used for SELECT
    protected ResultSet doQuery(String sql){

		ResultSet rs = null;
	
	    PreparedStatement ps;
		try {
			ps = conn.prepareStatement(sql);
		    rs = ps.executeQuery();

		} catch (SQLException e) {
			System.out.println("Error executing query.");
			e.printStackTrace();
			System.exit(0);
		}
	
	
		return rs;
    }

    
    /* Copied from AbstractServlet.java*/
    protected void substituteValues(PreparedStatement ps, Object[] values) throws Exception {

	for (int i = 0; i < values.length; i++) {
	    Object value = values[i];
	    if (value instanceof java.lang.Integer) {
		Integer intValue = (Integer) values[i];
		ps.setInt(i + 1, intValue);
	    } else if (value instanceof java.lang.Long) {
		Long longValue = (Long) values[i];
		ps.setLong(i + 1, longValue);
	    } else if (value instanceof java.lang.Float) {
		Float floatValue = (Float) values[i];
		ps.setFloat(i + 1, floatValue);
	    } else if (value instanceof java.lang.Double) {
		Double doubleValue = (Double) values[i];
		ps.setDouble(i + 1, doubleValue);
	    } else if (value instanceof java.lang.Boolean) {
		Boolean booleanValue = (Boolean) values[i];
		ps.setBoolean(i + 1, booleanValue);
	    } else if (value instanceof java.lang.String) {
		String stringValue = (String) value;
		ps.setString(i + 1, stringValue);
	    } else if (value instanceof Date) {
		Date dateValue = (java.sql.Date) value;
		ps.setDate(i + 1, dateValue);
	    } else if (value instanceof Time) {
		Time timeValue = (java.sql.Time) value;
		ps.setTime(i + 1, timeValue);
	    } else if (value instanceof Timestamp) {
		Timestamp timestampValue = (Timestamp) value;
		ps.setTimestamp(i + 1, timestampValue);
	    } else if (value instanceof byte[]) {
		byte[] byteArrayValue = (byte[]) value;
		ps.setBytes(i + 1, byteArrayValue);
	    } else if (null == value) {
		// The DB request might fail due to the next line
		// if the server cannot infer the type of the column
		// from the rest of the request.
		ps.setNull(i + 1, java.sql.Types.NULL);
	    } else {
		//logger.error("AbstractServlet.substituteValues: Invalid type for value: "
		//+ value);
		throw new Exception(
				    "AbstractServlet.substituteValues: Invalid type for value: "
				    + value);
	    }
	}
    }


    
}
	
	