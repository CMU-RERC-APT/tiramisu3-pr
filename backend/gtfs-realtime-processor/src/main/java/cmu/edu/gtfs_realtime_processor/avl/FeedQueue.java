package cmu.edu.gtfs_realtime_processor.avl;

import java.util.logging.Logger;

import org.json.JSONObject;

import static java.util.logging.Logger.getLogger; 
import static java.util.logging.Level.SEVERE; 
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;


public class FeedQueue {
    private static TiramisuDb propertiesDb = TiramisuDb.getDb("Properties");
    private static JSONObject properties = new JSONObject(propertiesDb.get("FeedQueue", null))
        .getJSONObject("properties");
	
    private static final int NUM_QUEUES = properties.getInt("NUM_QUEUES");
    private static final String QUEUE_NAME = properties.getString("QUEUE_NAME");
    private static final String SQS_ENDPOINT = properties.getString("SQS_ENDPOINT");
    private static int enqueueIndex;	//the next queue to enqueue
    private static int dequeueIndex; //the next queue to pull data from
    private static Logger logger = Logger.getLogger(FeedQueue.class.getName());
    private AmazonSQSAsyncClient sqs;
    private String[] queueUrl;
    private Map<String, Future<SendMessageResult>> futureMessages;
    private static volatile FeedQueue feedQueue;

    protected FeedQueue() {
        try {
            sqs = new AmazonSQSAsyncClient(new DefaultAWSCredentialsProviderChain());
            //TODO: verify AWS console region.
            sqs.setEndpoint(SQS_ENDPOINT);

            queueUrl = new String[NUM_QUEUES];
            for (int i=0; i<NUM_QUEUES; i++) {
                CreateQueueRequest request = new CreateQueueRequest(QUEUE_NAME + i);
                queueUrl[i] = sqs.createQueue(request).getQueueUrl();
            }

            enqueueIndex = 0;
            dequeueIndex = 0;

            futureMessages = new HashMap<String, Future<SendMessageResult>>();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FeedQueue getQueue() {
        if (feedQueue == null) {
            feedQueue = new FeedQueue();
        }
        return feedQueue;
    }

	public void putMessage(String message) {
        String currentUrl = queueUrl[enqueueIndex];
        //finish the previous enqueue operation
        if (futureMessages.containsKey(currentUrl)) {
            try {
                futureMessages.get(currentUrl).get();
                //by calling get on the future object representing the last enqueue operation
                //we make sure that the previous enqueue has finished on the current queue before enqueueing 
                //another message
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        Future<SendMessageResult> futureMessage = sqs.sendMessageAsync(currentUrl, message);
        futureMessages.put(currentUrl, futureMessage);
        enqueueIndex = (enqueueIndex+1) % NUM_QUEUES;
    }

    public Message getMessage() {
        //TODO: Investigate timeout behavior;
        int cur = dequeueIndex;
        //traverse all the queues, if everyone of them is empty then return null
        while (cur!=(dequeueIndex-1+NUM_QUEUES)%NUM_QUEUES) {
            try {
                List<Message> messages = sqs.receiveMessage(new ReceiveMessageRequest().
                                                            withQueueUrl(queueUrl[cur]).withMaxNumberOfMessages(1)).getMessages();
                if (messages.size()>0) {
                    dequeueIndex = cur;
                    return messages.get(0);
                }
                cur = (cur+1) % NUM_QUEUES;
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }
        }
        return null;
    }

    public void deleteMessage(Message message) {
        String handle = message.getReceiptHandle();
        for(int tries = 0; tries < 4; tries++){
            try {
                sqs.deleteMessage(new DeleteMessageRequest(queueUrl[dequeueIndex], handle));
                break;
            } catch (Exception e) {
                logger.warning(e.getMessage() + " attempt: " + String.valueOf(tries));
            }
        }
    }

	public void clear() {
        for (int i=0; i<NUM_QUEUES; i++) {
            sqs.purgeQueue((new PurgeQueueRequest()).withQueueUrl(queueUrl[i]));
        }
    }


}
