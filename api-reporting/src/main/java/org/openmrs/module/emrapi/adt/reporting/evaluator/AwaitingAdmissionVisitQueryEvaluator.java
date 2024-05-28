package org.openmrs.module.emrapi.adt.reporting.evaluator;

import org.openmrs.Location;
import org.openmrs.Visit;
import org.openmrs.annotation.Handler;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.reporting.query.AwaitingAdmissionVisitQuery;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.context.VisitEvaluationContext;
import org.openmrs.module.reporting.query.visit.VisitQueryResult;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;
import org.openmrs.module.reporting.query.visit.evaluator.VisitQueryEvaluator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

@Handler(supports = AwaitingAdmissionVisitQuery.class)
public class AwaitingAdmissionVisitQueryEvaluator implements VisitQueryEvaluator {

    @Autowired
    AdtService adtService;

    @Override
    public VisitQueryResult evaluate(VisitQuery visitQuery, EvaluationContext evaluationContext) throws EvaluationException {
        AwaitingAdmissionVisitQuery eq = (AwaitingAdmissionVisitQuery) visitQuery;
        Location location = eq.getLocation();
        Collection<Integer> patientIds = null;
        Collection<Integer> visitIds = null;
        if (evaluationContext.getBaseCohort() != null) {
            patientIds = evaluationContext.getBaseCohort().getMemberIds();
        }
        if (evaluationContext instanceof VisitEvaluationContext) {
            VisitEvaluationContext visitEvaluationContext = (VisitEvaluationContext) evaluationContext;
            if (visitEvaluationContext.getBaseVisits() != null) {
                visitIds = visitEvaluationContext.getBaseVisits().getMemberIds();
            }
        }
        List<Visit> results = adtService.getVisitsAwaitingAdmission(location, patientIds, visitIds);
        VisitQueryResult result = new VisitQueryResult(visitQuery, evaluationContext);
        for (Visit v : results) {
            result.add(v.getVisitId());
        }
        return result;
    }
}
