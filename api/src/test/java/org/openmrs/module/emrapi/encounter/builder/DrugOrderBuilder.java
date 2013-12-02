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
package org.openmrs.module.emrapi.encounter.builder;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;

import java.util.Locale;
import java.util.UUID;

public class DrugOrderBuilder {
    private DrugOrder order;

    public DrugOrderBuilder() {
        this.order = new DrugOrder();
        this.order.setUuid(UUID.randomUUID().toString());
        Drug drug = getDrug();
        this.order.setDrug(drug);
    }

    private Drug getDrug() {
        Drug drug = new Drug();
        drug.setName("Calpol");
        drug.setDoseStrength(125.0);
        drug.setUnits("ml");
        drug.setDosageForm(getConcept("Syrup"));
        return drug;
    }

    private Concept getConcept(String name) {
        Concept concept = new Concept();
        ConceptName conceptName = new ConceptName();
        conceptName.setName(name);
        conceptName.setLocale(Locale.ENGLISH);
        concept.setFullySpecifiedName(conceptName);
        return concept;
    }

    public DrugOrderBuilder withUuid(UUID uuid) {
        order.setUuid(String.valueOf(uuid));
        return this;
    }

    public DrugOrder build() {
        return order;
    }
}
