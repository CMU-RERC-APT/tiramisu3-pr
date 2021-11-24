package cmu.edu.gtfs_realtime_processor.avl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;

import cmu.edu.gtfs_realtime_processor.util.GtfsUtil;

public class ShapeDataProcessor {

	private static Logger logger = Logger.getLogger(ShapeDataProcessor.class.getName());
	private static TiramisuDb shapeDb = TiramisuDb.getDb("Shape");
	
	private static TiramisuDb propertiesDb = TiramisuDb.getDb("Properties");
	private static JSONObject properties = new JSONObject(propertiesDb.get("ShapeDataProcessor", null))
			.getJSONObject("properties");
	
    private static int DISTANCE_ERROR = properties.getInt("DISTANCE_ERROR");

	public static void main(String[] args) {
		shapeDb.removeAllData();
//		GtfsInfo MTAInfo = GtfsInfo.getAgencyInfo("MTA");
		GtfsInfo PAACInfo = GtfsInfo.getAgencyInfo("PAAC");		
		processShapeData(PAACInfo, "PAAC");
//		processShapeData(MTAInfo, "MTA");
	}

	private static Map<String, List<ShapePoint>> getShapePoints(GtfsInfo info) {
		Map<String, List<ShapePoint>> shapeIds = new HashMap<String, List<ShapePoint>>();

		Collection<ShapePoint> shapePoints = info.getShapePoints();
		if(shapePoints == null) {
			System.currentTimeMillis();
		}

		for (ShapePoint sp : shapePoints) {
			String shapeId = sp.getShapeId().getId();
			if (!shapeIds.containsKey(shapeId)) {
				List<ShapePoint> points = new ArrayList<ShapePoint>();
				points.add(sp);
				shapeIds.put(shapeId, points);
			}else{
				shapeIds.get(shapeId).add(sp);
			}
		}

		for (String shapeId : shapeIds.keySet()) {
			Collections.sort(shapeIds.get(shapeId), (sp1, sp2) -> ((ShapePoint) sp1).getSequence() - ((ShapePoint) sp2).getSequence());
		}

		return shapeIds;
	}

	private static Map<String, Stop> getStopsOnShape(String shapeId, GtfsInfo info) {
		Collection<Trip> trips = info.getTripsWithShapeId(shapeId);
		Map<String, Stop> stops = new HashMap<String, Stop>();
		if(trips != null) {
			for (Trip trip : trips) {
				Collection<StopTime> stopTimes = info.getStops(GtfsUtil.routeTripId(trip.getRoute().getId().getId(), trip.getId().getId()));
				if(stopTimes == null) {
					logger.severe("No stop times for route id: " + trip.getRoute().getId().getId() + "  and trip id: " + trip.getId().getId());
					continue;
				}
				for (StopTime st : stopTimes) {
					Stop stop = st.getStop();
					stops.put(stop.getId().getId(), stop);
				}
			}
		}
		return stops;
	}

	private static JSONObject constructShapePoint (ShapePoint sp) {
		JSONObject shapePoint = new JSONObject();
		shapePoint.put("lat", sp.getLat());
		shapePoint.put("lon", sp.getLon());
		//		shapePoint.put("seq", (double)sp.getSequence());
		return shapePoint;
	}

	private static JSONObject shapePointFromStop (Stop st) {
		JSONObject shapePoint = new JSONObject();
		shapePoint.put("lat", st.getLat());
		shapePoint.put("lon", st.getLon());
		shapePoint.put("stpid", st.getId().getId());
		return shapePoint;
	}
	
	private static int nearestShapePoint(List<ShapePoint> shapePoints, double lat, double lon) {
    	int index = 0;
    	if (shapePoints==null) return -1;
    	double shortestDist = Double.MAX_VALUE;
    	for (int i=0; i<shapePoints.size(); i++) {
    		ShapePoint curShapePoint = shapePoints.get(i);
    		double curDist = GtfsUtil.distance(lat, lon, curShapePoint.getLat(), curShapePoint.getLon());
    		if (curDist<=shortestDist) {
    			shortestDist = curDist;
    			index = i;
    		}
    	}
    	return index;
    }
	
	private static void processShapeData(GtfsInfo info, String agency) {
		Map<String, List<ShapePoint>> idToShapePoints = getShapePoints(info);

		for (String shapeId : idToShapePoints.keySet()) {

			JSONArray shapePointsWithStops = new JSONArray();
			Stack<JSONObject> shapePointStack = new Stack<JSONObject>();
			
			Map<String, Stop> idToStops = getStopsOnShape(shapeId, info);

			List<ShapePoint> shapePoints = idToShapePoints.get(shapeId);

			List<Stop> firstStops = new ArrayList<Stop>();
			Map<ShapePoint, ArrayList<Stop>> nextStops = new HashMap<ShapePoint, ArrayList<Stop>>();
			Map<ShapePoint, Stop> shapePointStops = new HashMap<ShapePoint, Stop>();
			//nextStops maps a shape point to its immediate next stop using "nextStops"
			//if the shape point is a stop itself, then map it to its corresponding stop using "shapePointStops"
			//if there's no stop between the current and the next shape points, the current shape point won't be added
			for (String stpid : idToStops.keySet()) {
				Stop curStop = idToStops.get(stpid);
				double lat = curStop.getLat();
				double lon = curStop.getLon();

				int index = nearestShapePoint(shapePoints, lat, lon);
				ShapePoint nearest = shapePoints.get(index);
				ShapePoint pre = index==0?null:shapePoints.get(index-1);
				ShapePoint next = index==shapePoints.size()-1?null:shapePoints.get(index+1);

				if(GtfsUtil.isSameLocation(nearest.getLat(), nearest.getLon(), lat, lon, DISTANCE_ERROR)
						|| (pre!=null && pre.getLat()==nearest.getLat() && pre.getLon()==nearest.getLon())) {
					//the stop's location is marked by a shape point
					shapePointStops.put(nearest, curStop);
				}else if (pre!=null && GtfsUtil.isContained(pre.getLat(), pre.getLon(), nearest.getLat(), nearest.getLon(), lat, lon)) {
					//the stop is between the previous and nearest shape point
					if (!nextStops.containsKey(pre)) {
						ArrayList<Stop> stops = new ArrayList<Stop>();
						nextStops.put(pre, stops);
					}
					nextStops.get(pre).add(curStop);
				}else if(next==null || GtfsUtil.isContained(nearest.getLat(), nearest.getLon(), next.getLat(), next.getLon(), lat, lon)) {
					//the stop is after the nearest shape point						
					if (!nextStops.containsKey(nearest)) {
						ArrayList<Stop> stops = new ArrayList<Stop>();
						nextStops.put(nearest, stops);
					}
					nextStops.get(nearest).add(curStop);
				}else if(pre==null) {
					//no shape point is before the stop
					firstStops.add(curStop);
				}else{
					logger.warning("COULDN'T FIND A PLACE TO FIT THE STOP");
				}
			}
			Stop nextStop = null;
			for (int i=shapePoints.size()-1; i>=0; i--) {
				ShapePoint curShapePoint = shapePoints.get(i);
				if (i<shapePoints.size()-1 && curShapePoint.getLat()==shapePoints.get(i+1).getLat() &&
						curShapePoint.getLon()==shapePoints.get(i+1).getLon()) {
					continue;
				}

				if (nextStops.containsKey(curShapePoint)) {
					ArrayList<Stop> curStops = nextStops.get(curShapePoint);
					for (int j=curStops.size()-1; j>=0; j--) {
						Stop curStop = curStops.get(j);
						JSONObject stopShapePoint = shapePointFromStop(curStop);

						if (nextStop!=null) {
							stopShapePoint.put("nstp", nextStop.getId().getId());
						}
						nextStop = curStop;
						shapePointStack.push(stopShapePoint);
					}
				}
				JSONObject shapePoint = constructShapePoint(curShapePoint);
				if (shapePointStops.containsKey(curShapePoint)) {
					Stop curStop = shapePointStops.get(curShapePoint);
					shapePoint.put("stpid", curStop.getId().getId());
					if (nextStop!=null) {
						shapePoint.put("nstp", nextStop.getId().getId());
					}
					nextStop = curStop;
				}else if (nextStop!=null) {
					shapePoint.put("nstp", nextStop.getId().getId());
				}
			  nnn h/b	shapePointStack.push(shapePoint);
			} 
			for (int i=firstStops.size()-1; i>=0; i--) {
				Stop curStop = firstStops.get(i);
				JSONObject stopShapePoint = shapePointFromStop(curStop);

				if (nextStop!=null) {
					stopShapePoint.put("nstp", nextStop.getId().getId());
				}
				nextStop = curStop;
				shapePointStack.push(stopShapePoint);
			}
			JSONObject pre = null;
			while (!shapePointStack.empty()) {
				JSONObject cur = shapePointStack.pop();
				if (pre!=null) {
					double preLat = pre.getDouble("lat");
					double preLon = pre.getDouble("lon");
					double curLat = cur.getDouble("lat");
					double curLon = cur.getDouble("lon");
					double dis = pre.getDouble("dist") + GtfsUtil.distance(preLat, preLon, curLat, curLon);
					cur.put("dist", dis);
				}else{
					cur.put("dist", 0);
				}
				shapePointsWithStops.put(cur);
				pre = cur;
			}

			shapeDb.put(shapeId, agency, shapePointsWithStops.toString());
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
