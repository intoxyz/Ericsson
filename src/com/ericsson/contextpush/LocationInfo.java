package com.ericsson.contextpush;

public class LocationInfo {
	private double lat, lng;
	private int rad;
	
	public LocationInfo(double lat, double lng, int rad) {
		this.lat = lat;
		this.lng = lng;
		this.rad = rad;
	}
	
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public int getRad() {
		return rad;
	}
	public void setRad(int rad) {
		this.rad = rad;
	}
	
}
