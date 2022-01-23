package cmu.edu.alarm_processor;


import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static java.util.logging.Level.SEVERE;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;

import org.json.JSONObject;

import cmu.edu.alarm_processor.util.UrlUtil;
import cmu.edu.alarm_processor.util.TimeUtil;

public class TiramisuDb { 
    private static final String DYNAMODB_ENDPOINT = "YOUR_URL_HERE";
    private static final long ONE_DAY_MILLIS = 86400000;
    private static Logger logger = Logger.getLogger(TiramisuDb.class.getName());
    private Table table;
    private String tableName;
    private String hashKeyName;
    private String rangeKeyName;
    private String valueKeyName;
    private DynamoDB dynamoDb;
    private AmazonDynamoDBClient client;
    private static TiramisuDb realtimeObservationDb;
    private static TiramisuDb shapeDb;
    private static TiramisuDb propertiesDb;


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
        TiramisuDb db = TiramisuDb.getDb("YOUR_URL_HERE");
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
            }
            table = dynamoDb.getTable(tableName);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.log(SEVERE, "Error creating awss3client!");
        }
    }

    public static TiramisuDb getDb(String tableName) {
    	switch (tableName) {
    	case "RealtimeObservationsTable":
    		if (realtimeObservationDb == null) {
    			realtimeObservationDb = new TiramisuDb(tableName);
            }
            return realtimeObservationDb;
    	case "Shape":
    		if (shapeDb == null) {
    			shapeDb = new TiramisuDb(tableName);
    		}
    		return shapeDb;
    	case "Properties":
    		if (propertiesDb == null) {
                    propertiesDb = new TiramisuDb(tableName);
    		}
    		return propertiesDb;
    	}
    	return null;
    }

    /*
    {
    AttributeDefinitions: [{AttributeName: route,AttributeType: S}],
    TableName: RealtimeObservationsTable,
    KeySchema: [{AttributeName: route,KeyType: HASH}],
    TableStatus: ACTIVE,
    CreationDateTime: Wed Dec 02 15:13:25 EST 2015,
    ProvisionedThroughput: {NumberOfDecreasesToday: 0,ReadCapacityUnits:
        5,WriteCapacityUnits: 5},
    TableSizeBytes: 0,
    ItemCount: 0,
    TableArn: YOUR_URL_HERE,
    }
     */

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
		table.putItem(item);
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

                delete(hashValue, rangeValue);
            }
        } while (result.getLastEvaluatedKey() != null);
    }
    
    public void removeOldData() {
        long curTime = TimeUtil.getTimeMillis();
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
                String data = map.get(valueKeyName).getS();
                if (data == null) {
                    delete(hashValue, rangeValue);
                } else {
                    deleteStale(data, rangeValue, hashValue, curTime);
                }
            }
        } while (result.getLastEvaluatedKey() != null);
    }

    private void deleteStale(String hashValue, String rangeValue, String data, long curTime) {
        try {
            JSONObject obj = new JSONObject(hashValue);
            Iterator<?> vehicles = obj.keys();
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
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }
}
