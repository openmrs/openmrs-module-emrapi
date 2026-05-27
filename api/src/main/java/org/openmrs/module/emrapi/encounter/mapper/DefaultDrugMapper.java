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

import org.openmrs.Drug;
import org.openmrs.module.emrapi.encounter.DrugMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.stereotype.Component;

@Component("drugMapper")
public class DefaultDrugMapper implements DrugMapper {
	
	@Override
	public EncounterTransaction.Drug map(Drug drug) {
		EncounterTransaction.Drug encounterTransactionDrug = new EncounterTransaction.Drug();
		encounterTransactionDrug.setName(drug.getDisplayName());
		if (drug.getDosageForm() != null) {
			encounterTransactionDrug.setForm(drug.getDosageForm().getName().getName());
		}
		encounterTransactionDrug.setStrength(drug.getStrength());
		encounterTransactionDrug.setUuid(drug.getUuid());
		return encounterTransactionDrug;
	}
}
