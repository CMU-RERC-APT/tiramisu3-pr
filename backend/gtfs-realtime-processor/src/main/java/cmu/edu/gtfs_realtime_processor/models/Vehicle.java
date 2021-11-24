package cmu.edu.gtfs_realtime_processor.models;

import javax.xml.bind.annotation.*;

public class Vehicle {
	private String vehicleId;
	private String timestamp;
	private String avl_date;
	private String avl_hour;
	private String avl_minute;
	private String latitude;
	private String longitude;
	private String heading;
	private String patternId;
	private String routeId;
	private String headsign;
	private String distanceTraveled;
	private String taTripId;
	private String taBlockId;

	public Vehicle() {
	}

	@XmlElement(name = "vid")
	public void setVehicleId(String vid) {
		this.vehicleId = vid;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	@XmlElement(name = "tmstmp")
	public void setTimestamp(String t) {
		this.timestamp = t;
		this.avl_date = t.substring(0, 4) + '-' + t.substring(4, 6) + '-' + t.substring(6, 8);
		this.avl_hour = t.substring(t.indexOf(' ') + 1, t.indexOf(':'));
		this.avl_minute = t.substring(t.indexOf(':') + 1);
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getAvlDate() {
		return avl_date;
	}

	public String getAvlHour() {
		return avl_hour;
	}

	public String getAvlMinute() {
		return avl_minute;
	}

	@XmlElement(name = "lat")
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLatitude() {
		return latitude;
	}

	@XmlElement(name = "lon")
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLongitude() {
		return longitude;
	}

	@XmlElement(name = "hdg")
	public void setHeading(String heading) {
		this.heading = heading;
	}

	public String getHeading() {
		return heading;
	}

	@XmlElement(name = "pid")
	public void setPatternId(String patternId) {
		this.patternId = patternId;
	}

	public String getPatternId() {
		return patternId;
	}

	@XmlElement(name = "rt")
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public String getRouteId() {
		return routeId;
	}

	@XmlElement(name = "des")
	public void setHeadsign(String headsign) {
		this.headsign = headsign;
	}

	public String getHeadsign() {
		return headsign;
	}

	@XmlElement(name = "pdist")
	public void setDistanceTraveled(String distanceTraveled) {
		this.distanceTraveled = distanceTraveled;
	}

	public String getDistanceTraveled() {
		return distanceTraveled;
	}

	/*
	 * NB: The tatripid and tablockid fields exist for both Centro and PAAC.
	 *
	 * This may break the Unmarshaller for Chicago's CTA depending on its
	 * behaviour.
	 */
	@XmlElement(name = "tatripid")
	public void setTaTripId(String taTripId) {
		this.taTripId = taTripId;
	}

	public String getTaTripId() {
		return taTripId;
	}

	@XmlElement(name = "tablockid")
	public void setTaBlockId(String taBlockId) {
		this.taBlockId = taBlockId;
	}

	public String getTaBlockId() {
		return taBlockId;
	}

	public String toString() {
		return "Vehicle Id: " + vehicleId + ", Route Id: " + routeId + ", Headsign: " + headsign + ", Timestamp: "
				+ timestamp + ", Date: " + avl_date + ", Hour: " + avl_hour + ", Minute: " + avl_minute + ", Latitude: "
				+ latitude + ", Longitude: " + longitude + ", Heading: " + heading + ", Pattern Id: " + patternId
				+ ", Distance Traveled: " + distanceTraveled + ", TATrip Id: " + taTripId + ", TABlock Id: "
				+ taBlockId;
	}
}
