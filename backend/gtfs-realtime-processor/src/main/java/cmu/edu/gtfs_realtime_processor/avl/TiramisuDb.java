package cmu.edu.gtfs_realtime_processor.avl;

import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Level.INFO;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;

import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;

import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import org.json.JSONObject;
import org.json.JSONArray;

import cmu.edu.gtfs_realtime_processor.util.UrlUtil;
import cmu.edu.gtfs_realtime_processor.util.TimeUtil;
import cmu.edu.gtfs_realtime_processor.util.GtfsUtil;

public class TiramisuDb { 
    private static final String DYNAMODB_ENDPOINT = "dynamodb.us-east-1.amazonaws.com";
    private static final long ONE_DAY_MILLIS = 86400000;
    private static Logger logger = Logger.getLogger(TiramisuDb.class.getName());
    private Table table;
    private String tableName;
    private String hashKeyName;
    private String rangeKeyName;
    private String valueKeyName;
    private DynamoDB dynamoDb;
    private AmazonDynamoDBClient client;

    /*
      public static void main(String[] args) {
      Item item = (new Item()).withPrimaryKey(hashKeyName, "61A")
      .withString("Agency", "paac")
      .withString("Observations", "aaaaaaaaaaaa");
      System.out.println(item.toJSONPretty());
      item = (new Item()).withString(hashKeyName, "61A")
      .withString("Agency", "paac")
      .withString("Observations", "aaaaaaaaaaaa");
      System.out.println(item.toJSONPretty());
      Result:
      {
      hashKeyName : "61A",
      "Agency" : "paac",
      "Observations" : "aaaaaaaaaaaa"
      }
      {
      hashKeyName : "61A",
      "Agency" : "paac",
      "Observations" : "aaaaaaaaaaaa"
      }
      TiramisuDb db = TiramisuDb.getDb("/home/bili/AwsCredentials.properties");
      db.displayTableInformation();
      }
    */

    protected TiramisuDb(String tableName) {
    	try {
            client = new AmazonDynamoDBClient(new DefaultAWSCredentialsProviderChain());
            client.setEndpoint(DYNAMODB_ENDPOINT);
            dynamoDb = new DynamoDB(client);
            this.tableName = tableName;
            switch (tableName) {
            case "RealtimeObservationsTable":
            	hashKeyName = "route_short_name";
            	rangeKeyName = "agency_id";
            	valueKeyName = "observations";
            	break;
            case "Shape":
            	hashKeyName = "shape_id";
            	rangeKeyName = "agency_id";
            	valueKeyName = "shape_points";
            	break;
            case "Properties":
            	hashKeyName = "filename";
            	valueKeyName = "properties";
            	break;
            case "GtfsVersions":
                hashKeyName = "initial_end_date";
                rangeKeyName = "agency_id";
                break;
            }
            table = dynamoDb.getTable(tableName);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.log(SEVERE, e.getMessage());
        }
    }

    public static TiramisuDb getDb(String tableName) {
        try {
            TiramisuDb tiramisuDb = new TiramisuDb(tableName);
            return tiramisuDb;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.log(SEVERE, e.getMessage());
        }
        return null;
    }


    public void displayTableInformation() {
        try {
            Table curTable = dynamoDb.getTable(tableName);
            TableDescription tableDescription = curTable.describe();
            System.out.println(tableDescription);
            //            System.out.format("Name: %s:\n" + "Status: %s \n"
            //                    + "Provisioned Throughput (read capacity units/sec): %d \n"
            //                    + "Provisioned Throughput (write capacity units/sec): %d \n",
            //            tableDescription.getTableName(), 
            //            tableDescription.getTableStatus(), 
            //            tableDescription.getProvisionedThroughput().getReadCapacityUnits(),
            //            tableDescription.getProvisionedThroughput().getWriteCapacityUnits());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public String get(String hashKeyVal, String rangeKeyVal) {
        /*
          There are several variations of this function. This version takes hash and range keys.
          Not sure if it works if the table has only a hash key. The other version takes only the first two params.
          Note curTable.getItem(hashKeyName, route) is sufficient given the table has
          only a hash key defined.
          If range key defined:
          getItem(hashKeyName, hashKeyVal, rangeKeyName, rangeKeyVal)
        */
    	if (rangeKeyVal == null) {
            return table.getItem(hashKeyName, hashKeyVal).toJSON().toString();
    	}
        return table.getItem(hashKeyName, hashKeyVal, rangeKeyName, rangeKeyVal).toJSON().toString();
    }

    public void put(String hashKey, String rangeKey, String value) {
    	Item item;
    	if (rangeKey==null) {
            item = (new Item()).withPrimaryKey(hashKeyName, hashKey)
                .withJSON(valueKeyName, value);
    	}else{
            item = (new Item()).withPrimaryKey(hashKeyName, hashKey)
                .withString(rangeKeyName, rangeKey)
                .withJSON(valueKeyName, value);
    	}
        
        boolean success = false;
        int tries = 1;
        do {
            try {
            	table.putItem(item);
            	success = true;
            }catch(Exception e) {
            	System.out.println("Number of tries: " + tries);
            	tries++;
            	try {
            		Thread.sleep(100);  //5*1000
            	}catch(Exception e1) {                	
            	}                		
            }
        }while(!success);	 	
    }

    public void delete(String hashKey, String rangeKey) {
    	if (rangeKey==null) {
            table.deleteItem(hashKeyName, hashKey);
    	}else{
            table.deleteItem(hashKeyName, hashKey, rangeKeyName, rangeKey);
    	}
    }

    public void removeAllData() {
    	
        ScanResult result = null;
        do {
            ScanRequest req = new ScanRequest().withTableName(tableName)
                .withProjectionExpression(hashKeyName+","+rangeKeyName+","+valueKeyName)
                .withLimit(100);
        	
            if (result != null) {
                req.setExclusiveStartKey(result.getLastEvaluatedKey());
            }
            result = client.scan(req);

            List<Map<String, AttributeValue>> routes = result.getItems();
            for (Map<String, AttributeValue> map : routes) {
                String hashValue = map.get(hashKeyName).getS();
                String rangeValue = map.get(rangeKeyName).getS();
                System.out.println("removed " + hashValue + " on " + rangeValue); 
                
                boolean success = false;
                int tries = 1;
                do {
	                try {
	                	delete(hashValue, rangeValue);
	                	success = true;
	                }catch(Exception e) {
	                	System.out.println("Number of tries: " + tries);
	                	tries++;
	                	try {
	                		Thread.sleep(5*1000);
	                	}catch(Exception e1) {                	
	                	}                		
	                }
                }while(!success);	 	               
                
            }
        } while (result.getLastEvaluatedKey() != null);
    }
    
    //Only for RealtimeObservations table
    public void removeOldData() {

        ItemCollection<ScanOutcome> result = table.scan(null, hashKeyName+", "+rangeKeyName+", "+valueKeyName, null, null);

        Iterator<Item> routes = result.iterator();
        while (routes.hasNext()) {
            Item route = routes.next();
            String hashValue = route.getString(hashKeyName);
            logger.info("Route: " + hashValue);
            String rangeValue = route.getString(rangeKeyName);
            String observations = route.getJSON(valueKeyName);
            String newObservations = removeStale(observations);
            if (!newObservations.isEmpty()) {
                put(hashValue, rangeValue, newObservations);
            }
            /*if (data == null) {
              delete(hashValue, rangeValue);
              } else {
              deleteStale(hashValue, rangeValue, data, curTime);
              }*/
        }
        //} while (result.getLastEvaluatedKey() != null);
        //} while (result != null);
    }

    private String removeStale(String observations) {
        try {
            long curTime = TimeUtil.getTimeMillis() / 1000;
            JSONObject data = new JSONObject(observations);
            Iterator<String> vehicleIds = data.keys();
            int vehiclesRemoved = 0;
            while(vehicleIds.hasNext()) {

                String vehicleId = vehicleIds.next();
                JSONObject vehicle = data.getJSONObject(vehicleId); 
                //System.out.println(vehicle);
                JSONArray predictions = vehicle.optJSONArray("predictions");
                if (predictions != null) {
                    for(int i = 0; i < predictions.length(); i++) {

                        JSONObject pred = predictions.getJSONObject(i);
                        //logger.info("prediction: " + pred.getString("departure-time") + " current: " + curTime);
                        if(curTime > pred.getLong("departure-time")){
                            predictions.remove(i);
                            i--;
                            //logger.info("prediction removed");
                        }
                    }
                }
                else {
                    logger.warning("predictions not found");
                }

                if(predictions == null || (predictions != null && predictions.length() <= 0)) {
                    vehicleIds.remove();
                    data.remove(vehicleId);
                    vehiclesRemoved++;
                }
            }
            logger.info(vehiclesRemoved + " vehicles removed");
            return data.toString();
            /*Iterator<?> vehicles = obj.keys();
              logger.fine("Before:\n" + obj.toString());
              while (vehicles.hasNext()) {
              String vid = (String) vehicles.next();
              JSONObject vehicleData = obj.getJSONObject(vid);
              long timeStampMillis = Long.parseLong(vehicleData.getString("time-stamp-millis"));
              if (curTime - timeStampMillis >= ONE_DAY_MILLIS) {
              vehicles.remove();
              }
              }
              logger.fine("After:\n" + obj.toString());
              if (obj.length() == 0) {
              delete(data, rangeValue);
              } else {
              put(data, rangeValue, obj.toString());
              }*/
        } catch (Exception e) {
            logger.severe(e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    //Only for GTFSVersions Table
    public String getGtfsPath(String agency_id) {
        
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        String currentDate = dateFormat.format(cal.getTime());

        QuerySpec spec = new QuerySpec()
            .withKeyConditionExpression("agency_id = :v_agency_id and end_date >= :v_end_date")
            .withValueMap(new ValueMap()
                          .withString(":v_agency_id", agency_id)
                          .withString(":v_end_date", currentDate));

        ItemCollection<QueryOutcome> items = table.query(spec);

        //Should only be one item, so returning first item for now
        Iterator<Item> iter = items.iterator();
        while(iter.hasNext()) {
            Item gtfsVersion = iter.next(); 
            String startDate = gtfsVersion.getString("start_date");
            if( startDate.compareTo(currentDate) <= 0) {
                return gtfsVersion.toJSON();
            }
        }
        return null;
        
    }
}
