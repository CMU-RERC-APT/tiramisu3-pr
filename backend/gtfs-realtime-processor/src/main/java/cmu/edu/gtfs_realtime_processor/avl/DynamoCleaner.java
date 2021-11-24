package cmu.edu.gtfs_realtime_processor.avl;

import java.util.logging.Logger;
import static java.util.logging.Level.WARNING;

import org.json.JSONObject;
import org.json.JSONArray;

import org.onebusaway.gtfs.model.Route;

public class DynamoCleaner {

    private static TiramisuDb propertiesDb = TiramisuDb.getDb("Properties");
    private static JSONObject properties = new JSONObject(propertiesDb.get("DynamoCleaner", null))
        .getJSONObject("properties");

    private static TiramisuDb realtimeObservationDb = TiramisuDb.getDb("RealtimeObservationsTable");

    public static final int DELAY = properties.getInt("DELAY");

    private static Logger logger = Logger.getLogger(DynamoCleaner.class.getName());

    public static void main(String[] args) {
        while (true) {
            realtimeObservationDb.removeOldData();
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                logger.warning("Interrupted exception while sleeping");
            }
        }
    }
}
