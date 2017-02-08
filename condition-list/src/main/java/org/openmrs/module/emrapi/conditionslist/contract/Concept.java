package org.openmrs.module.emrapi.conditionslist.contract;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Concept {
	
	private String uuid;
	
	private String displayString;
	
	public Concept() {
	}
	
	public Concept(String uuid, String displayString) {
		
		this.uuid = uuid;
		this.displayString = displayString;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public String getDisplayString() {
		return displayString;
	}
}
