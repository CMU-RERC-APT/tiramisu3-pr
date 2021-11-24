package cmu.edu.gtfs_realtime_processor.util;

import cmu.edu.gtfs_realtime_processor.avl.FeedQueue;
import cmu.edu.gtfs_realtime_processor.avl.TiramisuDb;


/* Debugging tool for clearing aws queues and dynamodb, etc*/
public class RealtimeUtil {
	public static void main(String[] args){
		
		args[0] = "both";
		
				
        if (args.length == 0) {
            displayUsage();
            System.exit(0);
        }
        
        if (args[0].equals("clearQueue")){
        	FeedQueue q = FeedQueue.getQueue();
            q.clear();
            System.out.println("Successfully cleared queue.");
            System.exit(0);
        }
        else if (args[0].equals("clearDb")){
        	TiramisuDb db = TiramisuDb.getDb("RealtimeObservationsTable");
        	db.removeAllData();
            System.out.println("Successfully cleared Db.");
        	System.exit(0);
        }
        else if (args[0].equals("both")){
        	FeedQueue q = FeedQueue.getQueue();
            q.clear();
            System.out.println("Successfully cleared queue.");
            TiramisuDb db = TiramisuDb.getDb("RealtimeObservationsTable");
        	db.removeAllData();
            System.out.println("Successfully cleared Db.");
        	System.exit(0);
        }       
	}

	private static void displayUsage() {
        System.out.println("Usage:\n\tjava RealtimeUtil clearQueue\n\t "
        		+ "java RealtimeUtil clearDb");
		
	}
}
