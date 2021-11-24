package cmu.edu.gtfs_realtime_processor.models;

import javax.xml.bind.annotation.XmlElement;

public class Route {
	private String route;
	private String route_name;

	public Route() {
	}
	
	public Route(String route) {
		this.route = route;
	}

	@XmlElement(name = "rt")
	public void setRoute(String rt) {
		this.route = rt;
	}

	public String getRoute() {
		return route;
	}

	@XmlElement(name = "rtnm")
	public void setRouteName(String rtnm) {
		this.route_name = rtnm;
	}

	public String getRouteName() {
		return route_name;
	}

	public String toString() {
		return "Route: " + route + ", Route Name: " + route_name + "\n";
	}
}
