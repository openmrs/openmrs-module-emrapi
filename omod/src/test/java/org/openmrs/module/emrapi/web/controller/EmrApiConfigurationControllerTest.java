package org.openmrs.module.emrapi.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class EmrApiConfigurationControllerTest extends BaseModuleWebContextSensitiveTest {

	MockHttpServletRequest request;
	MockHttpServletResponse response;

	@Before
	public void setup() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Autowired
	EmrApiProperties emrApiProperties;

	@Autowired
    EmrApiConfigurationController emrApiConfigController;

	@Before
	public void setUp() throws Exception {
		executeDataSet("baseMetaData.xml");
		executeDataSet("diagnosisMetaData.xml");
	}

	@Test
	public void shouldGetFullRepresentation() throws Exception {
		SimpleObject config = emrApiConfigController.getEmrApiConfig(request, response);
		System.out.println(config);
	}
}
