package cmu.edu.alarm_processor;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.SendMessageResult;

public class PropertiesConfiguror {
	private static final TiramisuDb propertiesDb = TiramisuDb.getDb("Properties");
	
	private static final double THRESHOLD = 0.9;
	private static final int NUM_STOPS = 30;
	public static final int DELAY = 10000000;

	public static void main(String[] args) {		
		JSONObject alarmProcessorProperties = new JSONObject();
		alarmProcessorProperties.put("ALARM_BUFFER_PERIOD", 120000);
		alarmProcessorProperties.put("FCM_URL", "https://fcm.googleapis.com/fcm/send");
		alarmProcessorProperties.put("NUM_THREADS", 10);
		alarmProcessorProperties.put("IOS_CERTIFICATE", "/Users/Yang_Jin/Documents/projects/Tiramisu/tiramisuV3/backend/alarm-processor/src/main/resource/ios_apns_certificate2.p12");
		alarmProcessorProperties.put("IOS_CERTIFICATE_PASSWORD", "tiramisu");
		alarmProcessorProperties.put("AUTHORIZATION_KEY", "AIzaSyA5_hpmoV8PkQa4jqW0Mvfc1lrXW1jp3cg");
		alarmProcessorProperties.put("DELAY", 10000);
		propertiesDb.put("AlarmProcessor", null, alarmProcessorProperties.toString());
		
		JSONObject dbConnectionProperties = new JSONObject();
		dbConnectionProperties.put("CONN_STRING", "jdbc:postgresql://localhost/mydb");
		dbConnectionProperties.put("USERNAME", "Yang_Jin");
		dbConnectionProperties.put("PASSWORD", "password");
		propertiesDb.put("DBConnection", null, dbConnectionProperties.toString());
		
		JSONObject obaConnectionProperties = new JSONObject();
		obaConnectionProperties.put("API_KEY", "TEST");
		obaConnectionProperties.put("API_URL_BASE", "http://onebusaway-test.us-east-1.elasticbeanstalk.com/oba/api/where/");
		propertiesDb.put("OBAConnection", null, obaConnectionProperties.toString());
	}
}

