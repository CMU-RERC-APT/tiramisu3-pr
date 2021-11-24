package cmu.edu.gtfs_realtime_processor.avl;

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
		JSONObject feedGrabberPAACProperties = new JSONObject();
		feedGrabberPAACProperties.put("DELAY", 10000);
		feedGrabberPAACProperties.put("NUM_STOPS", 30);
		feedGrabberPAACProperties.put("GET_VEHICLES_FUNC_NAME", "getvehicles");
		feedGrabberPAACProperties.put("GET_ROUTES_FUNC_NAME", "getroutes");
		feedGrabberPAACProperties.put("MAX_COUNT", 10);
		feedGrabberPAACProperties.put("AGENCY_NAME", "PAAC");
		feedGrabberPAACProperties.put("AGENCY_URL", "http://realtime.portauthority.org/bustime/api/v1/");
		feedGrabberPAACProperties.put("AGENCY_KEY", "wkrSrHHzJpj9kz2aDFUPy68tz");
		propertiesDb.put("FeedGrabberPAAC", null, feedGrabberPAACProperties.toString());

		JSONObject feedGrabberMTAProperties = new JSONObject();
		feedGrabberMTAProperties.put("DELAY", 10000);
		feedGrabberMTAProperties.put("NUM_STOPS", 30);
		feedGrabberMTAProperties.put("QUEUE_THRESHOLD_SIZE", 240000);
		feedGrabberMTAProperties.put("funcName", "vehicle-monitoring");
		feedGrabberMTAProperties.put("AGENCY_NAME", "MTA");
		feedGrabberMTAProperties.put("AGENCY_URL", "http://api.prod.obanyc.com/api/siri/");
		feedGrabberMTAProperties.put("AGENCY_KEY", "8645e7f1-2ddd-4b0b-92b1-44490b182ff1");
		propertiesDb.put("FeedGrabberMTA", null, feedGrabberMTAProperties.toString());

		JSONObject feedQueueProperties = new JSONObject();
		feedQueueProperties.put("NUM_QUEUES", 10);
		feedQueueProperties.put("QUEUE_NAME", "RealtimeObservationsQueue");
		feedQueueProperties.put("SQS_ENDPOINT", "https://sqs.us-east-1.amazonaws.com");
		propertiesDb.put("FeedQueue", null, feedQueueProperties.toString());

		JSONObject observationProcessorProperties = new JSONObject();
		observationProcessorProperties.put("THRESHOLD", 0.9);
		observationProcessorProperties.put("NUM_STOPS", 30);
		observationProcessorProperties.put("DELAY", 100000);
		propertiesDb.put("ObservationProcessor", null, observationProcessorProperties.toString());

		JSONObject shapeDataProcessorProperties = new JSONObject();
		shapeDataProcessorProperties.put("DISTANCE_ERROR", 35);
		propertiesDb.put("ShapeDataProcessor", null, shapeDataProcessorProperties.toString());
	}
}
