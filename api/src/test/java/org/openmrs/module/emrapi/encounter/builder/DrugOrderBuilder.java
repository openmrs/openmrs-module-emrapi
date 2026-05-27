/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.encounter.builder;

import org.openmrs.CareSetting;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class DrugOrderBuilder {
	
	private final EncounterTransaction.DrugOrder drugOrder;
	
	public DrugOrderBuilder() {
		drugOrder = new EncounterTransaction.DrugOrder();
		drugOrder.setCareSetting(CareSetting.CareSettingType.OUTPATIENT);
		drugOrder.setOrderType("Drug Order");
		withDrugUuid(UUID.randomUUID().toString());
		drugOrder.setDosingInstructionType("org.openmrs.SimpleDosingInstructions");
		EncounterTransaction.DosingInstructions dosingInstructions = DosingInstructionsBuilder.sample();
		drugOrder.setDosingInstructions(dosingInstructions);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, 1);
		drugOrder.setScheduledDate(calendar.getTime());
		calendar.add(Calendar.MONTH, 1);
		EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
		provider.setUuid("331c6bf8-7846-11e3-a96a-0800271c1b75");
		drugOrder.setAction("NEW");
		drugOrder.setDuration(2);
		drugOrder.setDurationUnits("Day");
	}
	
	public EncounterTransaction.DrugOrder build() {
		return drugOrder;
	}
	
	public DrugOrderBuilder withDurationUnits(String durationUnits) {
		drugOrder.setDurationUnits(durationUnits);
		return this;
	}
	
	public DrugOrderBuilder withDrugUuid(String drugUuid) {
		EncounterTransaction.Drug drug = new EncounterTransaction.Drug();
		drug.setUuid(drugUuid);
		drugOrder.setDrug(drug);
		return this;
	}
	
	public DrugOrderBuilder withNonCodedDrug(String freeTextDrug) {
		drugOrder.setDrugNonCoded(freeTextDrug);
		return this;
	}
	
	public DrugOrderBuilder withScheduledDate(Date scheduledDate) {
		drugOrder.setScheduledDate(scheduledDate);
		return this;
	}
	
	public DrugOrderBuilder withFrequency(String frequency) {
		drugOrder.getDosingInstructions().setFrequency(frequency);
		return this;
	}
	
	public DrugOrderBuilder withAction(String action) {
		drugOrder.setAction(action);
		return this;
	}
	
	public DrugOrderBuilder withPreviousOrderUuid(String previousOrderUuid) {
		drugOrder.setPreviousOrderUuid(previousOrderUuid);
		return this;
	}
	
	public DrugOrderBuilder withAutoExpireDate(Date date) {
		drugOrder.setAutoExpireDate(date);
		return this;
	}
}
