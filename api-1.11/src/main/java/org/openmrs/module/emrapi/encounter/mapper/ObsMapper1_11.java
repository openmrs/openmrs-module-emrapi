package org.openmrs.module.emrapi.encounter.mapper;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(value = "obsMapper")
@OpenmrsProfile(openmrsVersion = "[1.11.* - 1.12.*]")
public class ObsMapper1_11 extends ObsMapper {

    @Autowired
    public ObsMapper1_11(ConceptService conceptService,
                            EmrApiProperties emrApiProperties,
                            ObsService obsService, OrderService orderService) {
        super(conceptService,emrApiProperties,obsService,orderService);
    }

    @Override
    protected Obs newObservation(Encounter encounter,EncounterTransaction.Observation observationData) {
        Obs obs = super.newObservation(encounter,observationData);
        obs.setFormField(observationData.getFormNamespace(),observationData.getFormFieldPath());
        return obs;
    }
}
