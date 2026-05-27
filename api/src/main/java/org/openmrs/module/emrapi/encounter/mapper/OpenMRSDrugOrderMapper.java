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

import org.apache.commons.lang3.StringUtils;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.DosingInstructions;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.OrderMetadataService;
import org.openmrs.util.LocaleUtility;

import java.util.List;

/**
 * OpenMRSDrugOrderMapper. Maps EncounterTransaction DrugOrder to OpenMRS DrugOrders.
 * <p/>
 * Version 1.0
 */
public class OpenMRSDrugOrderMapper {
	
	private OrderService orderService;
	
	private ConceptService conceptService;
	
	private DosingInstructionsMapper dosingInstructionsMapper;
	
	private OrderMetadataService orderMetadataService;
	
	public static final Order.Urgency DEFAULT_URGENCY = Order.Urgency.ROUTINE;
	
	public OpenMRSDrugOrderMapper(OrderService orderService, ConceptService conceptService,
	    DosingInstructionsMapper dosingInstructionsMapper, OrderMetadataService orderMetadataService) {
		this.orderService = orderService;
		this.conceptService = conceptService;
		this.dosingInstructionsMapper = dosingInstructionsMapper;
		this.orderMetadataService = orderMetadataService;
	}
	
	public DrugOrder map(EncounterTransaction.DrugOrder drugOrder, Encounter encounter) {
		Concept conceptByUuid;
		DrugOrder openMRSDrugOrder = createDrugOrder(drugOrder);
		openMRSDrugOrder.setCareSetting(getCareSettingFrom(drugOrder, openMRSDrugOrder));
		
		Drug drug = getDrugFrom(drugOrder, openMRSDrugOrder);
		openMRSDrugOrder.setDrug(drug);
		openMRSDrugOrder.setDrugNonCoded(drugOrder.getDrugNonCoded());
		Concept concept = getConceptFrom(drugOrder, openMRSDrugOrder);
		if (concept != null) {
			openMRSDrugOrder.setConcept(concept);
		}
		openMRSDrugOrder.setEncounter(encounter);
		
		openMRSDrugOrder.setDateActivated(drugOrder.getDateActivated());
		openMRSDrugOrder.setScheduledDate(drugOrder.getScheduledDate());
		openMRSDrugOrder
		        .setUrgency(drugOrder.getScheduledDate() != null ? Order.Urgency.ON_SCHEDULED_DATE : DEFAULT_URGENCY);
		openMRSDrugOrder.setDuration(drugOrder.getDuration());
		openMRSDrugOrder.setSortWeight(drugOrder.getSortWeight());
		
		Concept durationUnitsConcept = orderMetadataService.getDurationUnitsConceptByName(drugOrder.getDurationUnits());
		if (durationUnitsConcept != null) {
			openMRSDrugOrder.setDurationUnits(durationUnitsConcept);
		} else {
			List<Concept> conceptList = conceptService.getConceptsByName(drugOrder.getDurationUnits(),
			    LocaleUtility.getDefaultLocale(), null);
			if (conceptList.size() > 0) {
				openMRSDrugOrder.setDurationUnits(conceptList.get(0));
			}
		}
		
		openMRSDrugOrder.setAutoExpireDate(drugOrder.getAutoExpireDate());
		if (drugOrder.getOrderReasonConcept() != null) {
			conceptByUuid = conceptService.getConceptByUuid(drugOrder.getOrderReasonConcept().getUuid());
			openMRSDrugOrder.setOrderReason(conceptByUuid);
		}
		openMRSDrugOrder.setOrderReasonNonCoded(drugOrder.getOrderReasonText());
		
		try {
			if (drugOrder.getDosingInstructionType() != null) {
				openMRSDrugOrder.setDosingType(
				    (Class<? extends DosingInstructions>) Context.loadClass(drugOrder.getDosingInstructionType()));
			}
		}
		catch (ClassNotFoundException e) {
			throw new APIException("Class not found for : DosingInstructionType " + drugOrder.getDosingInstructionType(), e);
		}
		
		dosingInstructionsMapper.map(drugOrder.getDosingInstructions(), openMRSDrugOrder);
		openMRSDrugOrder.setInstructions(drugOrder.getInstructions());
		Provider provider = encounter.getEncounterProviders().iterator().next().getProvider();
		openMRSDrugOrder.setOrderer(provider);
		return openMRSDrugOrder;
	}
	
	private boolean isNewDrugOrder(EncounterTransaction.DrugOrder drugOrder) {
		return StringUtils.isBlank(drugOrder.getPreviousOrderUuid());
	}
	
	private boolean isDiscontinuationDrugOrder(EncounterTransaction.DrugOrder drugOrder) {
		return drugOrder.getAction() != null && Order.Action.valueOf(drugOrder.getAction()) == Order.Action.DISCONTINUE;
	}
	
	private DrugOrder createDrugOrder(EncounterTransaction.DrugOrder drugOrder) {
		if (isNewDrugOrder(drugOrder)) {
			return new DrugOrder();
		} else if (isDiscontinuationDrugOrder(drugOrder)) {
			return (DrugOrder) orderService.getOrderByUuid(drugOrder.getPreviousOrderUuid()).cloneForDiscontinuing();
		} else {
			return (DrugOrder) orderService.getOrderByUuid(drugOrder.getPreviousOrderUuid()).cloneForRevision();
		}
	}
	
	private CareSetting getCareSettingFrom(EncounterTransaction.DrugOrder drugOrder, DrugOrder openMRSDrugOrder) {
		if (!isNewDrugOrder(drugOrder)) {
			return openMRSDrugOrder.getCareSetting();
		}
		return orderService.getCareSettingByName(drugOrder.getCareSetting().toString());
	}
	
	private Drug getDrugFrom(EncounterTransaction.DrugOrder drugOrder, DrugOrder openMRSDrugOrder) {
		if (!isNewDrugOrder(drugOrder)) {
			return openMRSDrugOrder.getDrug();
		}
		EncounterTransaction.Drug drug = drugOrder.getDrug();
		if (drug == null) {
			return null;
		}
		if (drug.getUuid() == null || drug.getUuid().isEmpty()) {
			return conceptService.getDrug(drug.getName());
		}
		return conceptService.getDrugByUuid(drug.getUuid());
	}
	
	private Concept getConceptFrom(EncounterTransaction.DrugOrder drugOrder, DrugOrder openMRSDrugOrder) {
		if (!isNewDrugOrder(drugOrder)) {
			return openMRSDrugOrder.getConcept();
		}
		
		Concept conceptByUuid;
		EncounterTransaction.Concept concept = drugOrder.getConcept();
		if (concept == null) {
			conceptByUuid = null;
		} else {
			conceptByUuid = conceptService.getConceptByUuid(concept.getUuid());
			if (conceptByUuid == null) {
				throw new APIException("No such Concept : " + drugOrder.getConcept().getName());
			}
		}
		return conceptByUuid;
	}
}
