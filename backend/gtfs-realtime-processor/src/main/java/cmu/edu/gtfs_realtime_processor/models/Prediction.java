package cmu.edu.gtfs_realtime_processor.models;

import javax.xml.bind.annotation.XmlElement;

public class Prediction {

	String timeStamp;
	String type;

	/**
	 * This stopId is actually stored as stop_code in our current
	 * database@2015.2.27
	 */
	String stopId;

	String stopName;
	String vehicleId;
	String distanceToStop;
	String route;
	String predictTime;
	String minutesLeftToStop;
	String tripId;
	String blockId;

	public String getMinutesLeftToStop() {
		return minutesLeftToStop;
	}

	@XmlElement(name = "prdctdn")
	public void setMinutesLeftToStop(String minutesLeftToStop) {
		this.minutesLeftToStop = minutesLeftToStop;
	}

	public String getTripId() {
		return tripId;
	}

	@XmlElement(name = "tatripid")
	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

	public String getTaBlockId() {
		return blockId;
	}

	@XmlElement(name = "tablockid")
	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	@XmlElement(name = "tmstmp")
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getType() {
		return type;
	}

	@XmlElement(name = "typ")
	public void setType(String type) {
		this.type = type;
	}

	public String getStopId() {
		return stopId;
	}

	@XmlElement(name = "stpid")
	public void setStopId(String stopId) {
		this.stopId = stopId;
	}

	public String getStopName() {
		return stopName;
	}

	@XmlElement(name = "stpnm")
	public void setStopName(String stopName) {
		this.stopName = stopName;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	@XmlElement(name = "vid")
	public void setVehicleId(String vehicleId) {
		this.vehicleId = vehicleId;
	}

	public String getDistanceToStop() {
		return distanceToStop;
	}

	@XmlElement(name = "dstp")
	public void setDistanceToStop(String distanceToStop) {
		this.distanceToStop = distanceToStop;
	}

	public String getRoute() {
		return route;
	}

	@XmlElement(name = "rt")
	public void setRoute(String route) {
		this.route = route;
	}

	public String getPredictTime() {
		return predictTime;
	}

	@XmlElement(name = "prdtm")
	public void setPredictTime(String predictTime) {
		this.predictTime = predictTime;
	}
}
