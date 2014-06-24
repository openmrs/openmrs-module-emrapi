package org.openmrs.module.emrapi.adt.reporting.evaluator;

import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.annotation.Handler;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.adt.reporting.definition.MostRecentAdmissionRequestVisitDataDefinition;
import org.openmrs.module.emrapi.adt.util.AdtUtil;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.reporting.data.visit.EvaluatedVisitData;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;
import org.openmrs.module.reporting.data.visit.evaluator.VisitDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluates a MostRecentAdmissionRequestVisitDataDefinition to produce a VisitData
 */
@Handler(supports=MostRecentAdmissionRequestVisitDataDefinition.class, order=50)
public class MostRecentAdmissionRequestVisitDataEvaluator implements VisitDataEvaluator {

    @Autowired
    private LocationService locationService;

    @Autowired
    private DispositionService dispositionService;

    @Autowired
    private DiagnosisService diagnosisService;

    @Autowired
    private EmrConceptService emrConceptService;

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public EvaluatedVisitData evaluate(VisitDataDefinition visitDataDefinition, EvaluationContext evaluationContext) throws EvaluationException {

        EvaluatedVisitData data = new EvaluatedVisitData(visitDataDefinition, evaluationContext);

        HqlQueryBuilder query = new HqlQueryBuilder();

        query.select("encounter.visit.id, encounter").from(Encounter.class, "encounter")
                .innerJoin("encounter.obs", "dispo")
                .whereEqual("dispo.concept", dispositionService.getDispositionDescriptor().getDispositionConcept())
                .whereIn("dispo.valueCoded", AdtUtil.getAdmissionDispositionsConcepts(emrConceptService, dispositionService))
                .whereEqual("dispo.voided", false)
                .whereEqual("encounter.voided", false)
                .whereVisitIn("encounter.visit.id", evaluationContext);


        List<Object[]> result = evaluationService.evaluateToList(query, evaluationContext);

        for (Object[] row : result) {

            Integer visitId = (Integer) row[0];
            Encounter encounter = (Encounter) row[1];

            // if there are multiple admission requests on the visit, we only want the most recent one
            if (data.getData().containsKey(visitId)) {
                Map<String,Object> resultRow = (Map<String,Object>) data.getData().get(visitId);

                if (encounter.getEncounterDatetime().before((Date) resultRow.get("datetime"))) {
                    continue;
                }
            }

            Map<String,Object> resultRow = new HashMap<String, Object>();

            resultRow.put("fromLocation", encounter.getLocation());
            resultRow.put("toLocation", getToLocation(encounter, dispositionService, locationService));
            resultRow.put("datetime", encounter.getEncounterDatetime());
            resultRow.put("provider", getProvider(encounter));
            resultRow.put("diagnoses", diagnosisService.getPrimaryDiagnoses(encounter));

            data.getData().put(visitId, resultRow);
        }


        return data;

    }


    private Provider getProvider(Encounter encounter) {

        // TODO: right now this just returns the first (non-voided) provider on an encounter
        // TODO: need to fix

        for (EncounterProvider encounterProvider : encounter.getEncounterProviders()) {
            if (!encounterProvider.isVoided()) {
                return encounterProvider.getProvider();
            }
        }

        return null;

    }

    private Location getToLocation(Encounter encounter, DispositionService dispositionService, LocationService locationService) {

        // TODO: the assumption here is that there is only one disposition request per encounter
        // TODO and that the disposition is on the top level

        DispositionDescriptor dispositionDescriptor = dispositionService.getDispositionDescriptor();

        for (Obs obs : encounter.getObsAtTopLevel(false)) {
            if (dispositionDescriptor.isDisposition(obs)) {
                return dispositionDescriptor.getAdmissionLocation(obs, locationService);
            }

        }

        return null;
    }

}
