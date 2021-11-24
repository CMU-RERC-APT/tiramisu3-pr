package main.java;

import static java.util.logging.Level.SEVERE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.json.JSONArray;

public class Database {
	
	private static Database instance;
    private DataSource DBDataSource;   
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(Database.class.getName());

    private Database() {
		if (System.getProperty("RDS_HOSTNAME") != null) {
			
		    // This is a seemingly messy way of doing things but allows all of these values
		    // to be changed without re-uploading the application to AWS Elastic Beanstalk
		    // (although it does have to be restarted).
		    // The default values can be found in .ebextensions/options.config.
		    // Use setenv.sh to deploy this outside of AWS
			
			
			// In AWS, the sys properties below appear in EB > tiramisu-v2 > Configuration >
			// Environment Properties. To run it locally (Eclipse), set/change the values in
			// Debub/Run Configurations...
		    String hostname = System.getProperty("RDS_HOSTNAME");
		    String dbname = System.getProperty("RDS_DB_NAME");
		    String username = System.getProperty("RDS_USERNAME");
		    String password = System.getProperty("RDS_PASSWORD");
		    String port = System.getProperty("RDS_PORT");
		    String driverClassName = System.getProperty("driverClassName");
		    String validationQuery = System.getProperty("validationQuery");

		    Integer maxActive = Integer.valueOf(System.getProperty("maxActive"));
		    Integer maxIdle = Integer.valueOf(System.getProperty("maxIdle"));
		    Integer minIdle = Integer.valueOf(System.getProperty("minIdle"));
		    Integer initialSize = Integer.valueOf(System.getProperty("initialSize"));
		    Integer maxWait = Integer.valueOf(System.getProperty("maxWait"));

		    Boolean testWhileIdle = Boolean.valueOf(System.getProperty("testWhileIdle"));
		    Boolean removeAbandoned = Boolean.valueOf(System.getProperty("removeAbandoned"));
		    Boolean logAbandoned = Boolean.valueOf(System.getProperty("logAbandoned"));

		    String dbUrl = "jdbc:postgresql://" + hostname + ":" + port + "/" + dbname;			   

		    PoolProperties pp = new PoolProperties();
		    pp.setUrl(dbUrl);
		    pp.setDriverClassName(driverClassName);
		    pp.setUsername(username);
		    pp.setPassword(password);
		    pp.setValidationQuery(validationQuery);
		    pp.setMaxActive(maxActive);
		    pp.setMaxIdle(maxIdle);
		    pp.setMinIdle(minIdle);
		    pp.setInitialSize(initialSize);
		    pp.setMaxWait(maxWait);
		    pp.setTestWhileIdle(testWhileIdle);
		    pp.setRemoveAbandoned(removeAbandoned);
		    pp.setLogAbandoned(logAbandoned);

		    this.DBDataSource = new DataSource();
		    this.DBDataSource.setPoolProperties(pp);		  		   
		}
		else {
			throw new IllegalStateException("test: hostname is null");
		    // TODO throw error
		}
    }
    
	 // Create a ResultSetHandler implementation to convert the
	 // rows into an JSONArray.
	 private ResultSetHandler<JSONArray> handler = new ResultSetHandler<JSONArray>() {
	     public JSONArray handle(ResultSet rs) throws SQLException {
	         if (!rs.next()) {
	             return null;
	         }	        
	         return ResultSetJsonConverter.toJson(rs);	    	     
	     }
	 };

    
    /**
     * We only need a single instance of Database (singleton), which in turn 
     * will manage a pool of connections 
     * @return
     */
    public static Database getInstance() {
    	if(instance == null) {
    		instance = new Database();
    	}
    	return instance;
    }
    
    // Creates new connections to database
    private Connection getConnection() throws SQLException {    		
    	Connection conn = null;
		try {
		    conn = DBDataSource.getConnection();
		} catch (SQLException e) {
		    // TODO handle error
			throw e;
		} 
		return conn;
    }
    
    
    public JSONArray doQuery(String sql, Object[] values) throws SQLException {    	
  	  	try{  	  		
  	  		QueryRunner run = new QueryRunner();  	  	
  	  		Connection conn = getConnection();  	  		
  	  		JSONArray result = run.query(conn, sql, handler, values);  	  	
  	  		DbUtils.close(conn);  	  	  	  		
  	  		return (result != null? result : new JSONArray());
        }catch (Exception e) {
            this.logger.log(SEVERE, "Exception: " + e.getMessage()); // highest Level of SEVERE
            //TODO: handle error
            e.printStackTrace();
            throw e;            
        }        
    }
    
    
 // To be used for UPDATE and INSERT
    public int doUpdate(String sql, Object[] values) throws SQLException {    
	  	int numRowsAffected = -1;	  
	  	try{
	  		QueryRunner run = new QueryRunner();	  		
	  		Connection conn = getConnection();
  	  		numRowsAffected = run.update(conn, sql, values);  	  		
  	  		DbUtils.close(conn);
  	  		return numRowsAffected;
	
	  	} catch (Exception e) {
	        this.logger.log(SEVERE, "Exception: " + e.getMessage()); // highest Level of SEVERE
	        //TODO: handle error
	        e.printStackTrace();
	  	    throw e;	  	    
	  	}
    }

}
