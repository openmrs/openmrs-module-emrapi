package org.openmrs.module.emrapi.adt.reporting.evaluator;

import org.hibernate.SessionFactory;
import org.openmrs.Location;
import org.openmrs.Visit;
import org.openmrs.annotation.Handler;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.reporting.query.AwaitingAdmissionVisitQuery;
import org.openmrs.module.emrapi.adt.util.AdtUtil;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.reporting.query.visit.VisitQueryResult;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;
import org.openmrs.module.reporting.query.visit.evaluator.VisitQueryEvaluator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
    EvaluationService evaluationService;

    @Override
    public VisitQueryResult evaluate(VisitQuery visitQuery, EvaluationContext evaluationContext) throws EvaluationException {

        AwaitingAdmissionVisitQuery eq = (AwaitingAdmissionVisitQuery) visitQuery;
        Location location = eq.getLocation();

        Location visitLocation = null ;
        if (location != null ) {
            visitLocation = adtService.getLocationThatSupportsVisits(location);
        }

        HqlQueryBuilder query = new HqlQueryBuilder();

        // distinct *active* visits with a disposition of type ADMIT (on any encounter from that visit)
        // and with no Admisstion Encounters
        query.select("distinct visit.visitId").from(Visit.class, "visit")
                .innerJoin("visit.encounters", "dispoEncounter")
                .innerJoin("dispoEncounter.obs", "dispo")
                .whereEqual("dispo.concept", dispositionService.getDispositionDescriptor().getDispositionConcept())
                .whereIn("dispo.valueCoded", AdtUtil.getAdmissionDispositionsConcepts(emrConceptService, dispositionService))
                .whereEqual("dispo.voided", false)
                .whereEqual("dispoEncounter.voided", false)
                .whereEqual("visit.voided", false)
                .whereEqual("visit.location", visitLocation)
                .whereNull("visit.stopDatetime")   // stopDatetime = null means "active visit"
                .where("(select count(*) from Encounter as admission "
                        + "where admission.visit = visit "
                        + "and admission.voided = false "
                        + "and admission.encounterType = :admissionEncounterType"
                        +") = 0")
                .withValue("admissionEncounterType", emrApiProperties.getAdmissionEncounterType());

        VisitQueryResult result = new VisitQueryResult(visitQuery, evaluationContext);

        List<Integer> results= evaluationService.evaluateToList(query, Integer.class);
        result.add(results.toArray(new Integer[results.size()]));
        return result;

    }

}
