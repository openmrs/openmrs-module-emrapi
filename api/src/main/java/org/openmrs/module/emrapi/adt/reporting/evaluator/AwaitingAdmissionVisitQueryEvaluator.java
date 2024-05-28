package org.openmrs.module.emrapi.adt.reporting.evaluator;

import org.openmrs.Location;
import org.openmrs.annotation.Handler;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.reporting.query.AwaitingAdmissionVisitQuery;
import org.openmrs.module.emrapi.adt.util.AdtUtil;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.db.EmrApiDAO;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.context.VisitEvaluationContext;
import org.openmrs.module.reporting.query.visit.VisitQueryResult;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;
import org.openmrs.module.reporting.query.visit.evaluator.VisitQueryEvaluator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Handler(supports = AwaitingAdmissionVisitQuery.class)
public class AwaitingAdmissionVisitQueryEvaluator implements VisitQueryEvaluator {

    @Autowired
    AdtService adtService;

    @Autowired
    DispositionService dispositionService;

    @Autowired
    EmrConceptService emrConceptService;

    @Autowired
    EmrApiProperties emrApiProperties;

    @Autowired
    EmrApiDAO visitDAO;

    @Override
    public VisitQueryResult evaluate(VisitQuery visitQuery, EvaluationContext evaluationContext) throws EvaluationException {

        AwaitingAdmissionVisitQuery eq = (AwaitingAdmissionVisitQuery) visitQuery;
        Location location = eq.getLocation();

        Location visitLocation = null ;
        if (location != null ) {
            visitLocation = adtService.getLocationThatSupportsVisits(location);
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("dispositionConcept", dispositionService.getDispositionDescriptor().getDispositionConcept());
        parameters.put("admissionDispositions", AdtUtil.getAdmissionDispositionsConcepts(emrConceptService, dispositionService));
        parameters.put("visitLocation", visitLocation);
        parameters.put("admissionEncounterType", emrApiProperties.getAdmissionEncounterType());
        parameters.put("admissionDecisionConcept", emrApiProperties.getAdmissionDecisionConcept());
        parameters.put("denyAdmissionConcept", emrApiProperties.getDenyAdmissionConcept());
        parameters.put("patientIds", null);
        parameters.put("visitIds", null);
        if (evaluationContext.getBaseCohort() != null) {
            parameters.put("patientIds", evaluationContext.getBaseCohort().getMemberIds());
        }
        if (evaluationContext instanceof VisitEvaluationContext) {
            VisitEvaluationContext visitEvaluationContext = (VisitEvaluationContext) evaluationContext;
            if (visitEvaluationContext.getBaseVisits() != null) {
                parameters.put("visitIds", visitEvaluationContext.getBaseVisits().getMemberIds());
            }
        }
        List<Integer> results = visitDAO.executeHqlFromResource("hql/visits_awaiting_admission.hql", parameters, Integer.class);
        VisitQueryResult result = new VisitQueryResult(visitQuery, evaluationContext);
        result.add(results.toArray(new Integer[results.size()]));
        return result;

    }

}
