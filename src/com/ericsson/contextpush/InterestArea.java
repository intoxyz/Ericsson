package com.ericsson.contextpush;

import java.io.Serializable;

public class InterestArea implements Serializable {
	
	private static final long serialVersionUID = 1L; //version name
	
	public String name;
	public LocationInfo location;
	public boolean severity[] = {false, false, false, false};
	
	public InterestArea(String _name, LocationInfo _location, boolean _severity[]){
		name = _name;
		location = _location;
		severity = _severity;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public LocationInfo getLocation() {
		return location;
	}
	public void setLocation(LocationInfo location) {
		this.location = location;
	}
	public boolean[] getSeverity() {
		return severity;
	}
	public void setSeverity(boolean[] severity) {
		this.severity = severity;
	}
	
}
