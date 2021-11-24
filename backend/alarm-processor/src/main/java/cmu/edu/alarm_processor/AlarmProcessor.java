package cmu.edu.alarm_processor;

import java.io.IOException;
import java.io.File;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
//import javax.net.ssl.SSLException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Future;

import java.util.logging.Logger;
import static java.util.logging.Level.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

//import com.notnoop.apns.APNS;
//import com.notnoop.apns.ApnsService;

import com.turo.pushy.apns.*;
import com.turo.pushy.apns.util.*;
import com.turo.pushy.apns.auth.*;

import io.netty.util.concurrent.Future;

import cmu.edu.alarm_processor.util.TimeUtil;

//import main.java.ResultSetJsonConverter;

public class AlarmProcessor {

    private static Logger logger = Logger.getLogger(AlarmProcessor.class.getName());
	
    private static TiramisuDb propertiesDb = TiramisuDb.getDb("Properties");
    private static JSONObject properties = new JSONObject(propertiesDb.get("AlarmProcessor", null))
        .getJSONObject("properties");
	
    private static final int DELAY = properties.getInt("DELAY");

    private static final long ALARM_BUFFER_PERIOD = properties.getLong("ALARM_BUFFER_PERIOD");
	
    private static final int NUM_THREADS = properties.getInt("NUM_THREADS");
		
    private static final String AUTHORIZATION_KEY = properties.getString("AUTHORIZATION_KEY");
	
    private static final String FCM_URL = properties.getString("FCM_URL");
	
    private static final String IOS_CERTIFICATE_PROD = properties.getString("IOS_CERTIFICATE_PROD");
    private static final String IOS_CERTIFICATE_TEST = properties.getString("IOS_CERTIFICATE_TEST");
	
    private static final String IOS_CERTIFICATE_PASSWORD = properties.getString("IOS_CERTIFICATE_PASSWORD");
	
    private static DBConnection db = null;
	
    private static HttpClient httpClient = HttpClientBuilder.create().build();
		
    private static ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

    private static ApnsClient apnsClient = null;		
	
    public static void main(String[] args) {
		
        db = new DBConnection();
		
        RequestConfig requestConfig = RequestConfig.custom().
            setConnectionRequestTimeout(5000).setConnectTimeout(5000).setSocketTimeout(5000).build();
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

        try {
            apnsClient = new ApnsClientBuilder()
                .setClientCredentials(new File(IOS_CERTIFICATE_PROD), IOS_CERTIFICATE_PASSWORD)
                .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true)  {

            JSONArray pendingAlarms = getPendingAlarms();
            if (pendingAlarms == null) {
                System.out.println("No alarms");
            } else {
                try {
                    processAlarms(pendingAlarms);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                System.out.println("Interrupteed exception");
            }
            logger.info("finished one iteration");
        }
    }
	
    private static long getETA(JSONObject OBAResponse){
        long eta = 0;
        //get real time if we can, otherwise use scheduled time
        try{
            eta = OBAResponse.getJSONObject("data").getJSONObject("entry").getLong("predictedArrivalTime");
            if (eta==0) {
                eta = OBAResponse.getJSONObject("data").getJSONObject("entry").getLong("scheduledArrivalTime");
            }
        } catch (Exception e){
            System.out.println("Error parsing OBA Response");
            e.printStackTrace();
            System.exit(0);
        }
        return eta;
		
    }
	
    private static void processAlarms(JSONArray pendingAlarms) {
        CompletionService<Void> completionService = 
            new ExecutorCompletionService<Void>(executor);

        System.out.println("# pendingAlarms: "+String.valueOf(pendingAlarms.length()));

        int len = pendingAlarms.length();
        for (int i = 0; i < len; i++) {
            final JSONObject curAlarm = pendingAlarms.getJSONObject(i);
            //set up a completion service to send alarms asynchronously
            completionService.submit(new Callable<Void>() {
                    public Void call() {
                        JSONObject OBAResponse = OBAConnection.getArrivalAndDepartureForStop(curAlarm);
                        System.out.println("OBAResponse: " + OBAResponse.toString());
                        long eta = getETA(OBAResponse);
                        if (alarmExpires(eta)){
                            sendAlarm(curAlarm);
                            addExpiresEntry(curAlarm);
                        } else if (alarmIsStale(eta)) {
                            System.out.println("Removing alarm without sending: " + curAlarm);
                            addExpiresEntry(curAlarm);
                        } else {
                        }
                        return null;
                    }
                });
        }
        //make sure that all alarms have been sent
        while (len>0) {
            try {
                java.util.concurrent.Future<Void> response = completionService.take();
                response.get();
                len--;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // TODO Auto-generated method stub
		
    }
	
    private static void addExpiresEntry(JSONObject entry) {
		
        int id = entry.getInt("id");
        Timestamp serviceDate = new Timestamp(entry.getLong("service_date"));

        String writeRouteSql = "INSERT INTO user_data.alarm (event, device_id, device_platform, registration_id, user_id, trip_id, stop_id, service_date, pair_id, stamp) "
            + "VALUES ('expire', ?, ?, ?, ?, ?, ?, ?, ?, NOW());";
			
        Object[] values = {
            entry.getString("device_id"),
            entry.getString("device_platform"),
            entry.getString("registration_id"),
            entry.getString("user_id"),
            entry.getString("trip_id"),
            entry.getString("stop_id"),
            serviceDate,
            id
        };
		
        /* should we detect if device_id, service_date, trip_id, stop_id tuple is duplicate?*/
        try {
            db.doUpdate(writeRouteSql, values);
        } catch (SQLException e) {
            System.out.println("Error: addExpiresEntry");
            e.printStackTrace();
            System.exit(0);
        }
    }
	
    private static void sendAlarmIOS(String registration_id, JSONObject alarm) {
        String routeName = alarm.getString("route_name");
        String tripHeadsign = alarm.getString("trip_headsign");
        String stopName = alarm.getString("stop_name");

        try {
            
            //final Future<Void> connectFuture = apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
            final Future<Void> connectFuture = apnsClient.connect(ApnsClient.PRODUCTION_APNS_HOST);
            connectFuture.await();

            final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
            payloadBuilder.setAlertBody(routeName + " (" + tripHeadsign + ") is now approaching " + stopName);
            payloadBuilder.setMutableContent(true);
            payloadBuilder.setContentAvailable(true);
            payloadBuilder.setSoundFileName("default");
            payloadBuilder.addCustomProperty("alarmInfo", alarm.toString());

            final String payload = payloadBuilder.buildWithDefaultMaximumLength();
            //final String token = TokenUtil.sanitizeTokenString(registration_id);

            SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(registration_id, "com.tiramisutransit.tiramisu", payload); 

            final Future<PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture =
                apnsClient.sendNotification(pushNotification);

            try {
                final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse =
                    sendNotificationFuture.get();

                if (pushNotificationResponse.isAccepted()) {
                    System.out.println("Push notification accepted by APNs gateway.");
                } else {
                    System.out.println("Notification rejected by the APNs gateway: " +
                                       pushNotificationResponse.getRejectionReason());

                    if (pushNotificationResponse.getTokenInvalidationTimestamp() != null) {
                        System.out.println("\t…and the token is invalid as of " +
                                           pushNotificationResponse.getTokenInvalidationTimestamp());
                    }
                }
            } catch (final Exception e) {
                System.err.println("Failed to send push notification.");
                e.printStackTrace();

                if (e.getCause() instanceof ClientNotConnectedException) {
                    System.out.println("Waiting for client to reconnect…");
                    apnsClient.getReconnectionFuture().await();
                    System.out.println("Reconnected.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
    private static void sendAlarmAndroid(String registration_id, JSONObject alarm) {
        JSONObject content = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("alarmInfo", alarm);
        data.put("content-available", "1");
        content.put("data", data);
        content.put("to", registration_id);
        HttpPost request = new HttpPost(FCM_URL);
        try {
            StringEntity params = new StringEntity(content.toString());
            request.setEntity(params);
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Authorization", "key="+AUTHORIZATION_KEY);
            //System.out.println(request);
            httpClient.execute(request);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            request.releaseConnection();
        }
    }

    private static void sendAlarm(JSONObject alarmInfo) {
        System.out.println("sending alarm for "+alarmInfo);
				
        String registration_id = alarmInfo.getString("registration_id");
        String devicePlatform = alarmInfo.getString("device_platform");
		
        //alarmInfo needs to be sent back to client to be displayed on screen and unhighlight expired alarm
        switch (devicePlatform) {
        case "ios":
            sendAlarmIOS(registration_id, alarmInfo);
            break;
        case "android":
            sendAlarmAndroid(registration_id, alarmInfo);
            break;
        }
    }

    private static boolean alarmIsStale(long eta) {
        //System.out.println("alarm stale?");
        return eta < TimeUtil.getTimeMillis();
    }
	
    private static boolean alarmExpires(long eta) {		
        //System.out.println("alarm expires?");
        return eta < (TimeUtil.getTimeMillis() + ALARM_BUFFER_PERIOD) && eta > TimeUtil.getTimeMillis();
    }

    /* get all entries where pending = t*/ 
    private static JSONArray getPendingAlarms() {
        String readStopsSql = "SELECT id, device_id, registration_id, device_platform, user_id, route_name, trip_headsign, trip_id, stop_id, stop_name, " 
            + "EXTRACT(EPOCH FROM service_date) * 1000 AS service_date FROM user_data.alarm WHERE "
            + "event = 'put' "
            + "AND id NOT IN (SELECT pair_id FROM user_data.alarm WHERE event = 'expire' OR event = 'remove'); ";

        ResultSet rs = null;
		
        JSONArray pendingAlarms = new JSONArray();

        try {
            rs = db.doQuery(readStopsSql);
            pendingAlarms = ResultSetJsonConverter.toJson(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
		
        return pendingAlarms;
		
    }
	
}
