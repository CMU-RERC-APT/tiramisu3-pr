package cmu.edu.gtfs_realtime_processor.util;

import java.util.logging.Logger;
import static java.util.logging.Level.*;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;



public class GtfsUtil {
    private static String regex = "^0*([^-\\s]+)";
    
    private static Logger logger = Logger.getLogger(GtfsUtil.class.getName());
    private static Pattern pattern;
    
    static {
       pattern = Pattern.compile(regex);
    }
    
    public static String extractPAACRealtimeId(String id) {
        Matcher matcher = pattern.matcher(id);
        if (!matcher.find()) {
            return null;
        } else {
            return matcher.group(1);
        }
    }

    public static String routeTripId(String routeId, String tripId) {
        return routeId + "-" + tripId;
    }

    public static String tripStopId(String tripId, String stopId) {
        return tripId + "-" + stopId;
    }

    public static String tripBlockId(String tripId, String blockId) {
    	return tripId + "-" + blockId;
    }
    
    public static double distance(double lat0, double lon0, double lat1, double lon1) {
        double radius = 6371000;
        double dLat = Math.toRadians(lat1 - lat0);
        double dLon = Math.toRadians(lon1 - lon0);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat1)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = (double) (radius * c);
        return dist;
    }
    
    public static boolean isContained(double lat1, double lon1, double lat2, double lon2, double lat, double lon) {

		double midLat = (lat1+lat2)/2;
		double midLon = (lon1+lon2)/2;
		double dir = GtfsUtil.distance(lat1, midLon, lat2, midLon) -
				GtfsUtil.distance(midLat, lon1, midLat, lon2);
		if (dir>0) {
			return (lat1-lat)*(lat-lat2)>0;
		}else if (dir<0){
			return (lon1-lon)*(lon-lon2)>0;
		}
		return (lat1-lat)*(lat-lat2)>0 && (lon1-lon)*(lon-lon2)>0;
	}
    
	public static boolean isSameLocation(double lat0, double lon0, double lat1, double lon1, double error) {
		return GtfsUtil.distance(lat0, lon0, lat1, lon1)<error;
	}
	
}
