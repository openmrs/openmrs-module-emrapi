/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.emrapi.visit;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.ui.framework.SimpleObject;

/**
 * Helper for converting the contents of an encounter to JSON
 */
public class ParsedObs {
	
	private List<SimpleObject> obs = new ArrayList<SimpleObject>();
	
	private List<SimpleObject> diagnoses = new ArrayList<SimpleObject>();
	
	private List<SimpleObject> dispositions = new ArrayList<SimpleObject>();
	
	public ParsedObs() {
	}
	
	public List<SimpleObject> getObs() {
		return obs;
	}
	
	public void setObs(List<SimpleObject> obs) {
		this.obs = obs;
	}
	
	public List<SimpleObject> getDiagnoses() {
		return diagnoses;
	}
	
	public void setDiagnoses(List<SimpleObject> diagnoses) {
		this.diagnoses = diagnoses;
	}
	
	public List<SimpleObject> getDispositions() {
		return dispositions;
	}
	
	public void setDispositions(List<SimpleObject> dispositions) {
		this.dispositions = dispositions;
	}
	
}
