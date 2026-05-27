/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.encounter.mapper;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.OrderMetadataService;

public class DosingInstructionsMapper {
	
	private ConceptService conceptService;
	
	private OrderMetadataService orderMetadataService;
	
	public DosingInstructionsMapper(ConceptService conceptService, OrderMetadataService orderMetadataService) {
		this.conceptService = conceptService;
		this.orderMetadataService = orderMetadataService;
	}
	
	public DrugOrder map(EncounterTransaction.DosingInstructions dosingInstructions, DrugOrder drugOrder) {
		drugOrder.setDose(dosingInstructions.getDose());
		drugOrder.setDoseUnits(orderMetadataService.getDoseUnitsConceptByName(dosingInstructions.getDoseUnits()));
		drugOrder.setDosingInstructions(dosingInstructions.getAdministrationInstructions());
		drugOrder.setRoute(orderMetadataService.getRouteConceptByName(dosingInstructions.getRoute()));
		drugOrder.setAsNeeded(dosingInstructions.getAsNeeded());
		drugOrder.setFrequency(orderMetadataService.getOrderFrequencyByName(dosingInstructions.getFrequency(), false));
		drugOrder.setQuantity(dosingInstructions.getQuantity());
		drugOrder
		        .setQuantityUnits(orderMetadataService.getDispenseUnitsConceptByName(dosingInstructions.getQuantityUnits()));
		Integer numberOfRefills = dosingInstructions.getNumberOfRefills();
		drugOrder.setNumRefills(numberOfRefills == null ? 0 : numberOfRefills);
		return drugOrder;
	}
	
	private Concept conceptByName(String name) {
		return conceptService.getConceptByName(name);
	}
}
