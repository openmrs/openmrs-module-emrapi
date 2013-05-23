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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;

public class ParserEncounterIntoSimpleObjects {
	
	private Encounter encounter;
	
	private UiUtils uiUtils;
	
	private EmrApiProperties emrApiProperties;
	
	public ParserEncounterIntoSimpleObjects(Encounter encounter, UiUtils uiUtils, EmrApiProperties emrApiProperties) {
		this.encounter = encounter;
		this.uiUtils = uiUtils;
		this.emrApiProperties = emrApiProperties;
	}
	
	public List<SimpleObject> parseOrders() {
		List<SimpleObject> orders = new ArrayList<SimpleObject>();
		
		for (Order order : encounter.getOrders()) {
			orders.add(SimpleObject.create("concept", uiUtils.format(order.getConcept())));
		}
		return orders;
	}
	
	public ParsedObs parseObservations(Locale locale) {
		DiagnosisMetadata diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
		DispositionDescriptor dispositionDescriptor = emrApiProperties.getDispositionDescriptor();
		
		ParsedObs parsedObs = new ParsedObs();
		
		for (Obs obs : encounter.getObsAtTopLevel(false)) {
			if (diagnosisMetadata.isDiagnosis(obs)) {
				parsedObs.getDiagnoses().add(parseDiagnosis(diagnosisMetadata, obs));
			} else if (dispositionDescriptor.isDisposition(obs)) {
				parsedObs.getDispositions().add(parseDisposition(dispositionDescriptor, obs, locale));
			} else {
				parsedObs.getObs().add(parseObs(obs, locale));
			}
		}
		
		Collections.sort(parsedObs.getDiagnoses(), new Comparator<SimpleObject>() {
			
			@Override
			public int compare(SimpleObject o1, SimpleObject o2) {
				Integer order1 = (Integer) o1.get("order");
				Integer order2 = (Integer) o2.get("order");
				return order1 - order2;
			}
		});
		
		return parsedObs;
	}
	
	private SimpleObject parseObs(Obs obs, Locale locale) {
		SimpleObject simpleObject = SimpleObject.create("obsId", obs.getObsId());
		simpleObject.put("question", capitalizeString(uiUtils.format(obs.getConcept())));
		simpleObject.put("answer", obs.getValueAsString(locale));
		return simpleObject;
	}
	
	private SimpleObject parseDisposition(DispositionDescriptor dispositionDescriptor, Obs obs, Locale locale) {
		Obs dispositionObs = dispositionDescriptor.getDispositionObs(obs);
		List<Obs> additionalObs = dispositionDescriptor.getAdditionalObs(obs);
		
		SimpleObject simpleObject = SimpleObject.create("obsId", obs.getObsId());
		simpleObject.put("disposition", dispositionObs.getValueAsString(locale));
		
		List<SimpleObject> simplifiedAdditionalObs = new ArrayList<SimpleObject>();
		for (Obs additional : additionalObs) {
			simplifiedAdditionalObs.add(parseObs(additional, locale));
		}
		simpleObject.put("additionalObs", simplifiedAdditionalObs);
		
		return simpleObject;
	}
	
	private SimpleObject parseDiagnosis(DiagnosisMetadata diagnosisMetadata, Obs obs) {
		Diagnosis diagnosis = diagnosisMetadata.toDiagnosis(obs);
		
		String answer = "(" + uiUtils.message("emr.Diagnosis.Certainty." + diagnosis.getCertainty()) + ") ";
		answer += diagnosis.getDiagnosis().formatWithCode(uiUtils.getLocale(),
		    emrApiProperties.getConceptSourcesForDiagnosisSearch());
		
		SimpleObject simpleObject = SimpleObject.fromObject(obs, uiUtils, "obsId");
		simpleObject.put("question", formatDiagnosisQuestion(diagnosis.getOrder()));
		simpleObject.put("answer", answer);
		simpleObject.put("order", diagnosis.getOrder().ordinal());
		return simpleObject;
	}
	
	private String formatDiagnosisQuestion(Diagnosis.Order order) {
		return uiUtils.message("emr.patientDashBoard.diagnosisQuestion." + order);
	}
	
	private String capitalizeString(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
	}
	
}
