package main.java.realtime;


import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static java.util.logging.Level.SEVERE;
//import java.util.Properties;
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

//import java.io.FileInputStream;

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
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;

import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;

import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import org.json.JSONObject;

public class TiramisuDb { 
    private static final String DYNAMODB_ENDPOINT = "dynamodb.us-east-1.amazonaws.com";
    private static final long ONE_DAY_MILLIS = 86400000;
    //private static BasicAWSCredentials credentials;
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
            logger.log(SEVERE, "Error creating AmazonDynamoDBClient!");
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

    //Only for GTFSVersions Table
    public String getGtfsPath(String agency_id) {
        System.out.println("pepitoelrobot 1");
        
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        String currentDate = dateFormat.format(cal.getTime());
        System.out.println("pepitoelrobot 2");

        QuerySpec spec = new QuerySpec()
            .withKeyConditionExpression("agency_id = :v_agency_id and end_date >= :v_end_date")
            .withValueMap(new ValueMap()
                          .withString(":v_agency_id", agency_id)
                          .withString(":v_end_date", currentDate));
        System.out.println("pepitoelrobot 3");

        ItemCollection<QueryOutcome> items = table.query(spec);

        //Should only be one item, so returning first item for now
        Iterator<Item> iter = items.iterator();
        System.out.println("pepitoelrobot 4");
        while(iter.hasNext()) {
            Item gtfsVersion = iter.next(); 
            String startDate = gtfsVersion.getString("start_date");
            if( startDate.compareTo(currentDate) <= 0) {
                return gtfsVersion.toJSON();
            }
        }
        System.out.println("pepitoelrobot 5");
        return null;
        
    }
}
