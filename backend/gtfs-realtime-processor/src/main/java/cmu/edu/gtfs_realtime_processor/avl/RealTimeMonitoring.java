package cmu.edu.gtfs_realtime_processor.avl;

import java.util.logging.Logger;

import static java.util.logging.Level.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.StringBuffer;
import java.net.MalformedURLException;

import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import cmu.edu.gtfs_realtime_processor.util.JsonUtil;
import cmu.edu.gtfs_realtime_processor.util.UrlUtil;

/*import javax.mail.*;  
import javax.mail.internet.*;  
import javax.activation.*;*/

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.*;


public class RealTimeMonitoring {
    private static TiramisuDb propertiesDb = TiramisuDb.getDb("Properties");
    private static JSONObject properties = new JSONObject(propertiesDb.get("RealtimeMonitor", null))
        .getJSONObject("properties");
    private static final String frontendUrl = properties.getString("TIRAMISU_URL");
    private static final int DELAY = properties.getInt("DELAY");
    private static String servletName = "SchedulesForLocationServlet";
    private static final JSONObject locationMap = properties.getJSONObject("LOCATIONS");
    private static Logger logger = Logger.getLogger(RealTimeMonitoring.class.getName());
    private static Properties props = new Properties(); 
    private static String[] toEmailAdd = null;

    private static String host = "localhost";

    public static void main(String[] args){
        while (true) {
            logger.info("RUNNING REALTIME CHECK");
            try {
				props.load(getFileFromResources("config.properties"));
			} catch (IOException e1) {		
				e1.printStackTrace();
			}
            toEmailAdd = props.getProperty("notifications.emails").split(",");
            logger.log(INFO, "emails: " + Arrays.toString(toEmailAdd));
            //logger.log(INFO,"just runnning");

            String subject = "MISSING REAL-TIME";
            String message = "Following location(s) missing real-time: ";
            Boolean sendMessage = false;
        
            Iterator<String> keys = locationMap.keys();
            while ( keys.hasNext()) {
                String location = (String) keys.next();
                //System.out.println(location);
                JSONArray locationList = locationMap.getJSONArray(location);
                if (!hasRealTime(locationList)) {
                    sendMessage = true;
                    message += location + ", ";
                }
            }

            if (sendMessage) {
                send(subject, message);
            }else {
            	logger.log(INFO, "Real-time is working fine!....");
            }

            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                logger.warning("Interrupted exception while sleeping");
            }
        }
    }

    private static boolean hasRealTime(JSONArray location){
        int working_realtime = 0;
        for(int i=0; i<location.length(); i++){
            JSONArray loc = location.getJSONArray(i);
            double lat_value = loc.getDouble(0);
            double lon_value = loc.getDouble(1);
            String lat = Double.toString(lat_value);
            String lon = Double.toString(lon_value);
            if(checkPredict(lat,lon))
                working_realtime++;
        }
        if(working_realtime==0) {
            //send("REAL-TIME MONITOR","something is wrong");
            return false;
        } else {
            return true;
        }
        //send("REAL-TIME MONITOR","test message");
    }

    private static Boolean checkPredict(String lat, String lon) {

        Map<String, String> params = new HashMap<String, String>();
        params.put("lat", lat);
        params.put("lon", lon);
        String myURL = UrlUtil.makeURL(frontendUrl, servletName, params);
        /*logger.info("Grabbing url");
        logger.log(INFO,myURL);*/

        try {
            JSONArray datas = JsonUtil.readJsonFromUrl(myURL).getJSONArray("data");
            int workingPrediction = 0;
            for (int i=0; i<datas.length(); i++) {
                JSONObject data = (JSONObject) datas.get(i);
                if (get_predict(data))
                    workingPrediction++;
            }
            if(workingPrediction==0)
                return false;
            /*int l = datas.length();
            logger.info("Count:");
            logger.log(INFO, Integer.toString(l));*/          

        }catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private static void send (String subject, String message) {
        /*final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        //Get a Properties object(this is for Gmail)
         Properties props = System.getProperties();
         props.setProperty("mail.smtp.host", "smtp.gmail.com");
         props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
         props.setProperty("mail.smtp.socketFactory.fallback", "false");
         props.setProperty("mail.smtp.port", "465"); //Gmail SSL port is 465
         props.setProperty("mail.smtp.socketFactory.port", "465");
         props.put("mail.smtp.auth", "true");
         props.put("mail.debug", "true");
         props.put("mail.store.protocol", "pop3");
         props.put("mail.transport.protocol", "smtp");
         final String username = "xxxx@gmail.com"; //Change sender email
         final String password = "xxxx";//Change sender password
         try{
         Session session = Session.getDefaultInstance(props, 
            new Authenticator(){
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

        //Create message
         Message msg = new MimeMessage(session);

        //Set From and To fields
         msg.setFrom(new InternetAddress("xxxx@gmail.com")); //Change sender email
         msg.setRecipients(Message.RecipientType.TO, 
                InternetAddress.parse("tianleip@andrew.cmu.edu",false)); //Change recipient email
         msg.setSubject(subject);
         msg.setText(message);
         msg.setSentDate(new Date());
         Transport.send(msg);
         System.out.println("Message sent."); //Console message
         }catch (MessagingException e){ System.out.println("Error, cause: " + e);}*/
        AmazonSimpleEmailService client = new AmazonSimpleEmailServiceClient();
        SendEmailRequest request = new SendEmailRequest()
            .withDestination(new Destination()
                             .withToAddresses(toEmailAdd))
            .withMessage(new Message()
                         .withBody(new Body().withText(new Content()
                                                       .withCharset("UTF-8")
                                                       .withData(message)))
                         .withSubject(new Content()
                                      .withCharset("UTF-8")
                                      .withData(subject)))
            .withSource("tiramisutransit@gmail.com");
        SendEmailResult response = client.sendEmail(request);
    }

    private static Boolean get_predict(JSONObject data){
            Boolean predicted = data.getBoolean("predicted");
            /*logger.info("Monitoring:");
            if(predicted)
                logger.log(INFO, "true");
            else
                logger.log(INFO,"false");*/
            
            return predicted;
    }
    
    
    // get file from classpath, resources folder
    private static InputStream getFileFromResources(String fileName) {
        ClassLoader classLoader = RealTimeMonitoring.class.getClassLoader();
        InputStream resource = classLoader.getResourceAsStream(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return resource;
        }

    }

}
