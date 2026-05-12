/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.disposition;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

public class DispositionObs {
	
	@JsonProperty
	private String label;
	
	@JsonProperty
	private String conceptCode;
	
	@JsonProperty
	private Map<String, String> params;
	
	public DispositionObs() {
		
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		
		DispositionObs that = (DispositionObs) o;
		
		if (!this.getLabel().equals(that.getLabel())) {
			return false;
		}
		if (!this.getConceptCode().equals(that.conceptCode)) {
			return false;
		}
		
		if (params != null ? !params.equals(that.params) : that.params != null) {
			return false;
		}
		
		return true;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getConceptCode() {
		return conceptCode;
	}
	
	public void setConceptCode(String conceptCode) {
		this.conceptCode = conceptCode;
	}
	
	public Map<String, String> getParams() {
		return params;
	}
	
	public void setParams(Map<String, String> params) {
		this.params = params;
	}
}
