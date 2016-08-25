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
package org.openmrs.module.emrapi.encounter.mapper;

import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.DrugMapper;
import org.openmrs.module.emrapi.encounter.ObservationMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(value = "observationMapper")
@OpenmrsProfile(openmrsVersion = "1.12.0 - 2.*")
public class ObservationMapper1_12 extends ObservationMapper {

    @Autowired
    public  ObservationMapper1_12(ConceptMapper conceptMapper, DrugMapper drugMapper, UserMapper userMapper){
        super(conceptMapper, drugMapper, userMapper);
    }

    @Override
    public EncounterTransaction.Observation map(Obs obs) {
        EncounterTransaction.Observation observation = super.map(obs);
        observation.setFormNamespace(obs.getFormFieldNamespace());
        return observation;
    }
}
