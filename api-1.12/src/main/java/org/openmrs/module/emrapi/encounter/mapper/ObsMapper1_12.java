package org.openmrs.module.emrapi.encounter.mapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component(value = "obsMapper")
@OpenmrsProfile(openmrsVersion = "1.12.0 - 2.*")
public class ObsMapper1_12 extends ObsMapper {

    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    public ObsMapper1_12(ConceptService conceptService,
                         EmrApiProperties emrApiProperties,
                         ObsService obsService, OrderService orderService) {
        super(conceptService,emrApiProperties,obsService,orderService);
    }

    @Override
    protected Obs newObservation(Encounter encounter,EncounterTransaction.Observation observationData) {
        Obs obs = super.newObservation(encounter,observationData);
        obs.setFormField(observationData.getFormNamespace(),observationData.getFormFieldPath());
        setInterpretationAndStatus(obs, observationData);
        return obs;
    }

    @Override
    protected void mapObservationProperties(EncounterTransaction.Observation observationData, Obs observation) {
        super.mapObservationProperties(observationData, observation);
        setInterpretationAndStatus(observation, observationData);
    }


    /*
       This method uses java reflection to set Interpretation and Status as adding a new module dependency
       for just setting two fields, a lot of duplicate code had to be added.
       This method can be moved to platform 2.1 dependency, once the platform 2.1 dependency is added.
    */
    private void setInterpretationAndStatus(Obs obs,EncounterTransaction.Observation observationData){
        try {
            Class<Enum> interpretationClass = (Class<Enum>) Class.forName("org.openmrs.Obs$Interpretation");
            Class<Enum> statusClass = (Class<Enum>) Class.forName("org.openmrs.Obs$Status");

            Method setInterpretation =
                    obs.getClass().getMethod("setInterpretation", interpretationClass);
            Method setStatus = obs.getClass().getMethod("setStatus", statusClass);


            Enum interpretation = observationData.getInterpretation() != null ?
                    Enum.valueOf(interpretationClass, observationData.getInterpretation()) : null;
            setInterpretation.invoke(obs, interpretation);

            if (observationData.getStatus() != null) {
                Enum status = Enum.valueOf(statusClass, observationData.getStatus());
                setStatus.invoke(obs, status);
            }

        } catch (NoSuchMethodException e) {
            log.warn("No such method exists", e);
        } catch (IllegalAccessException e) {
            log.warn("Illegal access of methods via reflection", e);
        } catch (InvocationTargetException e) {
            log.warn("Exception during Method invocation", e);
        } catch (ClassNotFoundException e) {
            log.warn("No class found", e);
        }
    }
}
