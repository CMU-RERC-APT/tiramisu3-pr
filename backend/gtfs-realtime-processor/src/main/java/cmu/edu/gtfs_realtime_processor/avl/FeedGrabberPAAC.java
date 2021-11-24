package cmu.edu.gtfs_realtime_processor.avl;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;

import cmu.edu.gtfs_realtime_processor.util.TimeUtil;

public class FeedGrabberPAAC {

	private static Logger logger = Logger.getLogger(FeedGrabberPAAC.class.getName());
	private static TiramisuDb propertiesDb = TiramisuDb.getDb("Properties");
	private static JSONObject properties = new JSONObject(propertiesDb.get("FeedGrabberPAAC", null))
			.getJSONObject("properties");
	public static final int DELAY = properties.getInt("DELAY");
	private static final int MAX_COUNT = properties.getInt("MAX_COUNT");
	private static final int NUM_STOPS = properties.getInt("NUM_STOPS");
	private static final FeedQueue queue = FeedQueue.getQueue();
	private static long time;
	private static long timeTotal;
	private static int totalMessages = 0;

	public static void main(String args[]) throws Exception {
		logger.info("RUNNING FEEDGRABBER");
		queue.clear();
		while (true) {
			time = System.currentTimeMillis();
			timeTotal = time;
			fetchAllVehicles();
			logger.info("finished one batch");
			logger.info("Total time: " + ((System.currentTimeMillis() - timeTotal)) + " and total messages: "
					+ totalMessages);
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			totalMessages = 0;
		}
	}

	public static void printTime(String label) {
		logger.info("Time for (" + label + ") is: " + ((System.currentTimeMillis() - time)));
		time = System.currentTimeMillis();
	}

	/**
	 * Retrieves all the vehicles/routes that have real time tracking
	 * 
	 * @return
	 */
	public static Map<String, List<VehiclePosition>> fetchRoutes() {
		Map<String, List<VehiclePosition>> routes = new HashMap<>();
		try {
			URL url = new URL("http://truetime.portauthority.org/gtfsrt-bus/vehicles");
			FeedMessage feed = FeedMessage.parseFrom(url.openStream());
			for (FeedEntity entity : feed.getEntityList()) {
				if (entity.hasVehicle()) {
					VehiclePosition vehicle = entity.getVehicle();
					String routeId = vehicle.hasTrip() && vehicle.getTrip().hasRouteId()
							? vehicle.getTrip().getRouteId()
							: null;
					String vehicleId = vehicle.hasVehicle() && vehicle.getVehicle().hasId()
							? vehicle.getVehicle().getId()
							: null;
					if (routeId != null) {
						if (routes.containsKey(routeId)) {
							routes.get(routeId).add(vehicle);
						} else {
							List<VehiclePosition> vehiclesTemp = new ArrayList<>();
							vehiclesTemp.add(vehicle);
							routes.put(routeId, vehiclesTemp);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return routes;
	}

	private static void fetchAllVehicles() {
		long timeStampMillis = TimeUtil.getTimeMillis();
		try {
			Map<String, HashMap<String, List<StopTimePair>>> stopTimes = getPredictions();
			printTime("getPredictions");
			Map<String, List<VehiclePosition>> routes = fetchRoutes();
			printTime("fetchRoutes");
			for (String routeId : routes.keySet()) {
				JSONObject routeObject = new JSONObject();
				JSONObject vehicleArray = new JSONObject();
				List<VehiclePosition> vehicles = routes.get(routeId);

				for (VehiclePosition vehicle : vehicles) {
					String tripId = vehicle.getTrip().getTripId();
					String lat = Float.toString(vehicle.getPosition().getLatitude());
					String lon = Float.toString(vehicle.getPosition().getLongitude());
					String vid = vehicle.getVehicle().getId() + "_" + tripId;
					String directionId = ""; // Integer.toString(vehicle.getTrip().hasDirectionId()?
					// vehicle.getTrip().getDirectionId() : 0);

					HashMap<String, List<StopTimePair>> tripStopTimes = stopTimes.get(vid);
					JSONObject vehicleData = constructVehicle(lat, lon, tripId, Long.toString(timeStampMillis),
							directionId);
					if (tripStopTimes != null) {
						// in this case we have realtime for vid, there might be several trips
						// associated with
						// one vehicle(the vehicle might reach the destination soon and turn around)
						// the belief is 1.0 for all realtime data
						vehicleData.put("belief", "1.0");
						addPredictions(vehicleData, tripStopTimes.get(tripId));
					}
					vehicleArray.put(vid, vehicleData);
				}
				routeObject.put("route", routeId);
				routeObject.put("agency", "PAAC");
				routeObject.put("vehicles", vehicleArray);
				queue.putMessage(routeObject.toString());
				printTime("putMessage");
				totalMessages++;
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.severe("Parsing issue");
		}
	}

	private static Map<String, HashMap<String, List<StopTimePair>>> getPredictions() {
		Map<String, HashMap<String, List<StopTimePair>>> stopTimes = new HashMap<String, HashMap<String, List<StopTimePair>>>();
		try {
			URL url = new URL("http://truetime.portauthority.org/gtfsrt-bus/trips");
			FeedMessage feed = FeedMessage.parseFrom(url.openStream());
			for (FeedEntity entity : feed.getEntityList()) {
				if (entity.hasTripUpdate()) {
					if (!entity.getTripUpdate().hasTrip()) {
						continue;
					}
					String tripId = entity.getTripUpdate().getTrip().getTripId();
					String routeId = entity.getTripUpdate().getTrip().getRouteId();
					String vehicleId = entity.getTripUpdate().getVehicle().getId();
					for (StopTimeUpdate stu : entity.getTripUpdate().getStopTimeUpdateList()) {
						String stopId = stu.getStopId();
						String posixTime = Long.toString(stu.hasArrival()
								? (stu.getArrival().hasTime() ? stu.getArrival().getTime()
										: stu.getArrival().getDelay())
								: (stu.getDeparture().hasTime() ? stu.getDeparture().getTime()
										: stu.getDeparture().getDelay()));
						String vid = vehicleId + "_" + tripId;
						if (stopTimes.containsKey(vid)) {
							if (stopTimes.get(vid).containsKey(tripId)) {
								List<StopTimePair> stops = stopTimes.get(vid).get(tripId);
								if (stops.size() >= NUM_STOPS) {
									continue;
								}
								stops.add(new StopTimePair(stopId, posixTime));
							} else {
								List<StopTimePair> stops = new ArrayList<StopTimePair>();
								stops.add(new StopTimePair(stopId, posixTime));
								stopTimes.get(vid).put(tripId, stops);
							}
						} else {
							List<StopTimePair> l = new ArrayList<StopTimePair>();
							l.add(new StopTimePair(stopId, posixTime));
							HashMap<String, List<StopTimePair>> temp = new HashMap<String, List<StopTimePair>>();
							temp.put(tripId, l);
							stopTimes.put(vid, temp);
						}

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stopTimes;
	}

	public static void fetchPredictions(Map<String, List<VehiclePosition>> routes,
			Map<String, VehiclePosition> vehicles) {
		try {
			URL url = new URL("http://truetime.portauthority.org/gtfsrt-bus/vehicles");
			FeedMessage feed = FeedMessage.parseFrom(url.openStream());
			for (FeedEntity entity : feed.getEntityList()) {
				if (entity.hasVehicle()) {
					VehiclePosition vehicle = entity.getVehicle();
					String routeId = vehicle.hasTrip() && vehicle.getTrip().hasRouteId()
							? vehicle.getTrip().getRouteId()
							: null;
					String vehicleId = vehicle.hasVehicle() && vehicle.getVehicle().hasId()
							? vehicle.getVehicle().getId()
							: null;
					if (routeId != null) {
						if (routes.containsKey(routeId)) {
							routes.get(routeId).add(vehicle);
						} else {
							List<VehiclePosition> vehiclesTemp = new ArrayList<>();
							vehiclesTemp.add(vehicle);
							routes.put(routeId, vehiclesTemp);
						}
					}
					if (vehicleId != null) {
						vehicles.put(vehicleId, vehicle);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static JSONObject constructVehicle(String lat, String lon, String tatripid, String timeStampMillis,
			String directionId) {
		JSONObject vehicleData = new JSONObject();
		vehicleData.put("lat", lat);
		vehicleData.put("lon", lon);
		vehicleData.put("time-stamp-millis", timeStampMillis);
		vehicleData.put("direction_id", directionId);
		vehicleData.put("tatripid", tatripid);
		return vehicleData;
	}

	private static void addPredictions(JSONObject vehicleData, List<StopTimePair> stopTimes) {
		// convert the stopTimes list to JSON array and add it to vehicleData
		JSONArray predictions = new JSONArray();
		int index = 0;
		for (StopTimePair st : stopTimes) {
			JSONObject curSt = new JSONObject();
			try {
				curSt.put("stop-id", st.stopId);
				curSt.put("departure-time", st.time);
				predictions.put(index, curSt);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			++index;
		}
		try {
			vehicleData.put("predictions", predictions);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
