package org.openmrs.module.emrapi.adt.reporting.evaluator;

import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Provider;
import org.openmrs.annotation.Handler;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.InpatientRequest;
import org.openmrs.module.emrapi.adt.InpatientRequestSearchCriteria;
import org.openmrs.module.emrapi.adt.reporting.definition.MostRecentAdmissionRequestVisitDataDefinition;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.module.reporting.data.visit.EvaluatedVisitData;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;
import org.openmrs.module.reporting.data.visit.evaluator.VisitDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.context.VisitEvaluationContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluates a MostRecentAdmissionRequestVisitDataDefinition to produce a VisitData
 */
@Handler(supports= MostRecentAdmissionRequestVisitDataDefinition.class, order=50)
@OpenmrsProfile(modules = { "reporting:*" })
public class MostRecentAdmissionRequestVisitDataEvaluator implements VisitDataEvaluator {

    @Autowired
    AdtService adtService;

    @Autowired
    private DiagnosisService diagnosisService;

    @Override
    public EvaluatedVisitData evaluate(VisitDataDefinition visitDataDefinition, EvaluationContext context) throws EvaluationException {
        EvaluatedVisitData data = new EvaluatedVisitData(visitDataDefinition, context);
        InpatientRequestSearchCriteria criteria = new InpatientRequestSearchCriteria();
        if (context instanceof VisitEvaluationContext) {
            VisitEvaluationContext visitEvaluationContext = (VisitEvaluationContext) context;
            if (visitEvaluationContext.getBaseVisits() != null && !visitEvaluationContext.getBaseVisits().isEmpty()) {
                criteria.setVisitIds(new ArrayList<>(visitEvaluationContext.getBaseVisits().getMemberIds()));
            }
        }
        if (context.getBaseCohort() != null && !context.getBaseCohort().isEmpty()) {
            criteria.setPatientIds(new ArrayList<>(context.getBaseCohort().getMemberIds()));
        }
        MostRecentAdmissionRequestVisitDataDefinition definition = (MostRecentAdmissionRequestVisitDataDefinition) visitDataDefinition;
        criteria.setVisitLocation(definition.getVisitLocation());
        criteria.setDispositionLocations(definition.getDispositionLocations());
        criteria.setDispositionTypes(definition.getDispositionTypes());

        List<InpatientRequest> inpatientRequests = adtService.getInpatientRequests(criteria);
        for (InpatientRequest request : inpatientRequests) {
            Integer visitId = request.getVisit().getVisitId();
            Encounter encounter = request.getDispositionEncounter();
            Map<String,Object> resultRow = new HashMap<>();
            resultRow.put("fromLocation", encounter.getLocation());
            resultRow.put("toLocation", request.getDispositionLocation());
            resultRow.put("datetime", encounter.getEncounterDatetime());
            resultRow.put("provider", getProvider(encounter));
            resultRow.put("diagnoses", diagnosisService.getPrimaryDiagnoses(encounter));
            data.getData().put(visitId, resultRow);
        }
        return data;
    }

    // TODO: right now this just returns the first (non-voided) provider on an encounter, need to fix
    private Provider getProvider(Encounter encounter) {
        for (EncounterProvider encounterProvider : encounter.getEncounterProviders()) {
            if (!encounterProvider.isVoided()) {
                return encounterProvider.getProvider();
            }
        }
        return null;
    }
}
