/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.adt.reporting.evaluator;

import org.openmrs.Location;
import org.openmrs.Visit;
import org.openmrs.annotation.Handler;
import org.openmrs.annotation.OpenmrsProfile;
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
@OpenmrsProfile(modules = { "reporting:*" })
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
