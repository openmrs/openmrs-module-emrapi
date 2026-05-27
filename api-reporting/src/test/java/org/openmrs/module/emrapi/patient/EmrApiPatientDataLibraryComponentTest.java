/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.patient;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.BaseReportingTest;
import org.openmrs.module.emrapi.patient.reporting.library.EmrApiPatientDataLibrary;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class EmrApiPatientDataLibraryComponentTest extends BaseReportingTest {
	
	@Autowired
	private PatientDataService pds;
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private EmrApiPatientDataLibrary library;
	
	@Before
	public void setup() throws Exception {
		executeDataSet("baseTestDataset.xml");
	}
	
	@Test
	public void shouldFetchPrimaryIdentifier() throws Exception {
		test(library.getPrimaryIdentifier(), patientService.getPatientIdentifier(4)); // primary key of patient identifier 6TS-4 in standard test dataset
	}
	
	// TODO:
	
	private Object eval(PatientDataDefinition definition) throws EvaluationException {
		Cohort cohort = new Cohort(Arrays.asList(7));
		
		EvaluationContext context = new EvaluationContext();
		context.setBaseCohort(cohort);
		context.addParameterValue("startDate", DateUtil.parseYmd("2013-01-01"));
		context.addParameterValue("endDate", DateUtil.parseYmd("2013-12-31"));
		EvaluatedPatientData data = pds.evaluate(definition, context);
		return data.getData().get(7);
	}
	
	private void test(PatientDataDefinition definition, Object expectedValue) throws EvaluationException {
		Object actualValue = eval(definition);
		assertThat(actualValue, is(expectedValue));
	}
	
}
