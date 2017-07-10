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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.DrugMapper;
import org.openmrs.module.emrapi.encounter.ObservationMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component(value = "observationMapper")
@OpenmrsProfile(openmrsVersion = "1.12.0 - 2.*")
public class ObservationMapper1_12 extends ObservationMapper {

    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    public  ObservationMapper1_12(ConceptMapper conceptMapper, DrugMapper drugMapper, UserMapper userMapper){
        super(conceptMapper, drugMapper, userMapper);
    }

    @Override
    public EncounterTransaction.Observation map(Obs obs) {
        EncounterTransaction.Observation observation = super.map(obs);
        observation.setFormNamespace(obs.getFormFieldNamespace());
        observation.setFormFieldPath(obs.getFormFieldPath());
        setInterpretationAndStatus(observation, obs);
        return observation;
    }

    /*
       This method uses java reflection to get Interpretation and Status as adding a new module dependency
       for just setting two fields, a lot of duplicate code had to be added.
       This method can be moved to platform 2.1 dependency, once the platform 2.1 dependency is added.
    */
    private void setInterpretationAndStatus(EncounterTransaction.Observation observation, Obs obs) {
        try {
            Method getInterpretation = obs.getClass().getDeclaredMethod("getInterpretation");
            Method getStatus = obs.getClass().getDeclaredMethod("getStatus");

            Enum obsInterpretation = (Enum) getInterpretation.invoke(obs);
            Enum obsStatus = (Enum) getStatus.invoke(obs);

            String interpretation = (obsInterpretation != null) ? obsInterpretation.name() : null;
            String status = (obsStatus != null) ? obsStatus.name() : null;

            observation.setInterpretation(interpretation);
            observation.setStatus(status);
        } catch (IllegalAccessException e) {
            log.warn("Illegal access of methods via reflection", e);
        } catch (NoSuchMethodException e) {
            log.warn("No such method exists", e);
        } catch (InvocationTargetException e) {
            log.warn("Exception during Method invocation", e);
        }
    }
}
