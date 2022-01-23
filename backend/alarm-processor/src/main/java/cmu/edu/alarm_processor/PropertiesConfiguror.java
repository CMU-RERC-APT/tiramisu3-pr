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
		alarmProcessorProperties.put("IOS_CERTIFICATE", "YOUR_URL_HERE");
		alarmProcessorProperties.put("IOS_CERTIFICATE_PASSWORD", "YOUR_PASSWORD_HERE");
		alarmProcessorProperties.put("AUTHORIZATION_KEY", "YOUR_PASSWORD_HERE");
		alarmProcessorProperties.put("DELAY", 10000);
		propertiesDb.put("AlarmProcessor", null, alarmProcessorProperties.toString());
		
		JSONObject dbConnectionProperties = new JSONObject();
		dbConnectionProperties.put("CONN_STRING", "jdbc:postgresql://localhost/mydb");
		dbConnectionProperties.put("USERNAME", "Yang_Jin");
		dbConnectionProperties.put("PASSWORD", "YOUR_PASSWORD_HERE");
		propertiesDb.put("DBConnection", null, dbConnectionProperties.toString());
		
		JSONObject obaConnectionProperties = new JSONObject();
		obaConnectionProperties.put("API_KEY", "TEST");
		obaConnectionProperties.put("API_URL_BASE", "YOUR_URL_HERE");
		propertiesDb.put("OBAConnection", null, obaConnectionProperties.toString());
	}
}

