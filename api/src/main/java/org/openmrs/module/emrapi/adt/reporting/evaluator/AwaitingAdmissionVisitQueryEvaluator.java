package org.openmrs.module.emrapi.adt.reporting.evaluator;

import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.annotation.Handler;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.domain.AdtEventType;
import org.openmrs.module.emrapi.adt.domain.AdtVisit;
import org.openmrs.module.emrapi.adt.reporting.query.AwaitingAdmissionVisitQuery;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.context.VisitEvaluationContext;
import org.openmrs.module.reporting.query.visit.VisitIdSet;
import org.openmrs.module.reporting.query.visit.VisitQueryResult;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;
import org.openmrs.module.reporting.query.visit.evaluator.VisitQueryEvaluator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Handler(supports = AwaitingAdmissionVisitQuery.class)
public class AwaitingAdmissionVisitQueryEvaluator implements VisitQueryEvaluator {

    @Autowired
    AdtService adtService;

    @Override
    public VisitQueryResult evaluate(VisitQuery visitQuery, EvaluationContext evaluationContext) throws EvaluationException {

        AwaitingAdmissionVisitQuery eq = (AwaitingAdmissionVisitQuery) visitQuery;
        Location location = eq.getLocation();

        Location visitLocation = null ;
        if (location != null ) {
            visitLocation = adtService.getLocationThatSupportsVisits(location);
        }

        // distinct *active* visits with a disposition of type ADMIT (on any encounter from that visit)
        // that is NOT followed by a admission decision obs with a value of "DENY" and with no Admission Encounters

        VisitQueryResult result = new VisitQueryResult(visitQuery, evaluationContext);
        List<AdtVisit> adtVisits = adtService.getAdtEventsForActiveVisits(visitLocation);
        for (AdtVisit adtVisit : adtVisits) {
            if (adtVisit.hasLatestEventWithType(AdtEventType.ADMISSION_REQUEST) || adtVisit.hasLatestEventWithType(AdtEventType.ADMISSION_DECISION_ADMIT)) {
                if (allowedForContext(adtVisit, evaluationContext)) {
                    result.add(adtVisit.getVisit().getVisitId());
                }
            }
        }
        return result;

    }

    protected boolean allowedForContext(AdtVisit adtVisit, EvaluationContext context) {
        Cohort cohort = context.getBaseCohort();
        if (cohort != null && !cohort.isEmpty() && !cohort.contains(adtVisit.getVisit().getPatient().getId())) {
            return false;
        }
        if (context instanceof VisitEvaluationContext) {
            VisitEvaluationContext visitContext = (VisitEvaluationContext) context;
            VisitIdSet baseVisits = visitContext.getBaseVisits();
            if (baseVisits != null && !baseVisits.isEmpty() && !baseVisits.contains(adtVisit.getVisit().getVisitId())) {
                return false;
            }
        }
        return true;
    }

}
