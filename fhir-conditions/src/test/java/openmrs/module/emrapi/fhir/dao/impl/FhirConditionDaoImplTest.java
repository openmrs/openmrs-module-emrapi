/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package openmrs.module.emrapi.fhir.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import openmrs.module.emrapi.fhir.TestSpringConfiguration;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestSpringConfiguration.class, inheritLocations = false)
public class FhirConditionDaoImplTest extends BaseModuleContextSensitiveTest {

	private static final String CONDITION_INITIAL_DATA_XML = "openmrs/module/emrapi/fhir/dao/impl/FhirConditionDaoImplTest_initial_data.xml";

	private static final String CONDITION_UUID = "2ss6880e-2c46-11e4-9038-a6c5e4d22fb7";

	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;

	private FhirConditionDaoImpl dao;

	@Before
	public void setup() throws Exception {
		dao = new FhirConditionDaoImpl();
		dao.setSessionFactory(sessionFactory);
		executeDataSet(CONDITION_INITIAL_DATA_XML);
	}

	@Test
	public void shouldGetConditionByUuid() {
		Condition condition = dao.get(CONDITION_UUID);
		assertThat(condition, notNullValue());
		assertThat(condition.getUuid(), equalTo(CONDITION_UUID));
	}
}
