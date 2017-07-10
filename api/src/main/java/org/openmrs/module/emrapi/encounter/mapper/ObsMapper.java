package org.openmrs.module.emrapi.encounter.mapper;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Set;

import static org.openmrs.module.emrapi.utils.GeneralUtils.getCurrentDateIfNull;

@Component("obsMapper")
@OpenmrsProfile(openmrsPlatformVersion = "[1.9.* - 1.10.*]")
public class ObsMapper {

    private ConceptService conceptService;
    private EmrApiProperties emrApiProperties;
    private ObsService obsService;
    private OrderService orderService;

    @Autowired
    public ObsMapper(ConceptService conceptService,
                     EmrApiProperties emrApiProperties,
                     ObsService obsService, OrderService orderService) {
        this.conceptService = conceptService;
        this.emrApiProperties = emrApiProperties;
        this.obsService = obsService;
        this.orderService = orderService;
    }

    public Obs transformEtObs(Encounter encounter,Obs observation, EncounterTransaction.Observation observationData) {
        if (observation == null) {
            observation = newObservation(encounter,observationData);
        }

        mapObservationProperties(observationData, observation);

        for (EncounterTransaction.Observation member : observationData.getGroupMembers()) {
            Obs nextLevelObs = getMatchingObservation(observation.getGroupMembers(), member.getUuid());
            observation.addGroupMember(transformEtObs(encounter,nextLevelObs, member));
        }
        return observation;
    }

    protected Obs newObservation(Encounter encounter,EncounterTransaction.Observation observationData) {
        Obs observation = new Obs();
        if (!StringUtils.isBlank(observationData.getUuid())) {
            observation.setUuid(observationData.getUuid());
        }
        Date observationDateTime = getCurrentDateIfNull(observationData.getObservationDateTime());
        Concept concept = conceptService.getConceptByUuid(observationData.getConceptUuid());
        if (concept == null) {
            throw new ConceptNotFoundException("Observation concept does not exist" + observationData.getConceptUuid());
        }
        observation.setConcept(concept);
        observation.setPerson(encounter.getPatient());
        observation.setObsDatetime(observationDateTime);
        setVoidedObs(observationData, observation);

        return observation;
    }


    private boolean setVoidedObs(EncounterTransaction.Observation observationData, Obs observation) {
        if (observationData.getVoided()) {
            observation.setVoided(true);
            observation.setVoidReason(observationData.getVoidReason());
        }
        return observationData.getVoided();
    }

    protected void mapObservationProperties(EncounterTransaction.Observation observationData, Obs observation) {
        if(setVoidedObs(observationData, observation))
            return;
        observation.setComment(observationData.getComment());
        if (observationData.getValue() != null) {
            if (observation.getConcept().getDatatype().isCoded()) {
                String uuid = getUuidOfCodedObservationValue(observationData.getValue());
                Concept conceptByUuid = conceptService.getConceptByUuid(uuid);
                if (conceptByUuid == null) {
                    Drug drug = conceptService.getDrugByUuid(uuid);
                    observation.setValueDrug(drug);
                    observation.setValueCoded(drug.getConcept());
                } else {
                    observation.setValueCoded(conceptByUuid);
                }
            } else if (observation.getConcept().isComplex()) {
                observation.setValueComplex(observationData.getValue().toString());
                Concept conceptComplex = observation.getConcept();
                if (conceptComplex instanceof HibernateProxy) {
                    Hibernate.initialize(conceptComplex);
                    conceptComplex = (ConceptComplex) ((HibernateProxy) conceptComplex).getHibernateLazyInitializer().getImplementation();
                }
                obsService.getHandler(((ConceptComplex) conceptComplex).getHandler()).saveObs(observation);
            } else if (!observation.getConcept().getDatatype().getUuid().equals(ConceptDatatype.N_A_UUID)) {
                try {
                    observation.setValueAsString(observationData.getValue().toString());
                } catch (ParseException pe) {
                    throw new IllegalArgumentException("Obs value for the concept uuid [" + observationData.getConceptUuid() + "] cannot be parsed");
                }
            }
        }
        if (observationData.getOrderUuid() != null && !observationData.getOrderUuid().isEmpty()) {
            observation.setOrder(getOrderByUuid(observationData.getOrderUuid()));
        }
        observation.setObsDatetime(getCurrentDateIfNull(observationData.getObservationDateTime()));
    }

    private String getUuidOfCodedObservationValue(Object codeObsVal) {
        if (codeObsVal instanceof LinkedHashMap) return (String) ((LinkedHashMap) codeObsVal).get("uuid");
        return (String) codeObsVal;
    }

    private Order getOrderByUuid(String orderUuid) {
        return orderService.getOrderByUuid(orderUuid);
    }

    public Obs getMatchingObservation(Set<Obs> existingObservations, String observationUuid) {
        if (existingObservations == null) return null;
        for (Obs obs : existingObservations) {
            if (StringUtils.equals(obs.getUuid(), observationUuid)) return obs;
        }
        return null;
    }
}
