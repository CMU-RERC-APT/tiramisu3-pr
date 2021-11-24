package cmu.edu.gtfs_realtime_processor.models;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "bustime-response")
public class BustimeResponse {
	ArrayList<Vehicle> vehicles;
	ArrayList<Route> routes;
	ArrayList<Prediction> predictions;
	ArrayList<Error> errors;

	public BustimeResponse() {
		vehicles = new ArrayList<Vehicle>();
		routes = new ArrayList<Route>();
		errors = new ArrayList<Error>();
		predictions = new ArrayList<Prediction>();
	}

	public ArrayList<Prediction> getPredictions() {
		return predictions;
	}

	@XmlElement(name = "prd")
	public void setPredictions(ArrayList<Prediction> predictions) {
		this.predictions = predictions;
	}

	/**
	 * Required function for unmarshalling library
	 *
	 * @param ArrayList
	 *            of Vehicles
	 */
	@XmlElement(name = "vehicle")
	public void setVehicles(ArrayList<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	/**
	 * Provides a list of vehicles on the road from a Clever Devices-based API.
	 *
	 * To be used with response from endpoint "getvehicles"
	 *
	 * @return ArrayList of Vehicles
	 */
	public ArrayList<Vehicle> getVehicles() {
		return vehicles;
	}

	/**
	 * Required function for unmarshalling library
	 *
	 * @param ArrayList
	 *            of Routes
	 */
	@XmlElement(name = "route")
	public void setRoutes(ArrayList<Route> routes) {
		this.routes = routes;
	}

	/**
	 * Provides a list of valid routes from a Clever Devices-based API.
	 *
	 * To be used with response from endpoint "getroutes"
	 *
	 * @return ArrayList of Routes
	 */
	public ArrayList<Route> getRoutes() {
		return routes;
	}

	/**
	 * Required function for unmarshalling library
	 *
	 * @param ArrayList
	 *            of Errors
	 */
	@XmlElement(name = "error")
	public void setErrors(ArrayList<Error> errors) {
		this.errors = errors;
	}

	/**
	 * @return ArrayList of Errors
	 */
	public ArrayList<Error> getErrors() {
		return errors;
	}
}
