package org.openmrs.module.emrapi.conditionslist.contract;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Concept {
	
	private String uuid;
	
	private String name;
	
	private String shortName;
	
	public Concept() {
	}
	
	public Concept(String uuid, String name) {
		this.uuid = uuid;
		this.name = name;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
}
