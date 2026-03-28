package org.openmrs.module.emrapi.adt.reporting.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.emrapi.adt.reporting.definition.MostRecentAdmissionRequestVisitDataDefinition;
import org.openmrs.module.emrapi.adt.reporting.query.AwaitingAdmissionVisitQuery;
import org.openmrs.module.reporting.data.visit.EvaluatedVisitData;
import org.openmrs.module.reporting.data.visit.service.VisitDataService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.query.visit.VisitQueryResult;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;
import org.openmrs.module.reporting.query.visit.evaluator.VisitQueryEvaluator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

@Handler(supports = AwaitingAdmissionVisitQuery.class)
@OpenmrsProfile(modules = { "reporting:*" })
public class AwaitingAdmissionVisitQueryEvaluator implements VisitQueryEvaluator {

    @Autowired
    private VisitDataService visitDataService;

    @Override
    public VisitQueryResult evaluate(VisitQuery visitQuery, EvaluationContext evaluationContext) throws EvaluationException {
        VisitQueryResult result = new VisitQueryResult(visitQuery, evaluationContext);
        AwaitingAdmissionVisitQuery query = (AwaitingAdmissionVisitQuery) visitQuery;
        MostRecentAdmissionRequestVisitDataDefinition dataDef = new MostRecentAdmissionRequestVisitDataDefinition();
        dataDef.setVisitLocation(query.getLocation());
        EvaluatedVisitData visitData = visitDataService.evaluate(dataDef, evaluationContext);
        result.addAll(visitData.getData().keySet());
        return result;
    }
}
