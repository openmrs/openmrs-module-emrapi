package org.openmrs.module.emrapi.web.controller;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.emrapi.EmrApiConfiguration;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.metadatamapping.api.MetadataMappingService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EmrApiConfigurationControllerTest extends BaseModuleWebContextSensitiveTest {

	MockHttpServletRequest request;
	MockHttpServletResponse response;

	@Autowired
	EmrApiProperties emrApiProperties;

	@Autowired
    EmrApiConfigurationController emrApiConfigurationController;

	@Autowired
	MetadataMappingService metadataMappingService;

	@Before
	public void setUp() {
		executeDataSet("baseMetaData.xml");
		executeDataSet("baseTestDataset.xml");
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	public void shouldGetDefaultRepresentation() throws Exception {
		SimpleObject config = emrApiConfigurationController.getEmrApiConfiguration(request, response);
		List<String> propertyNames = new ArrayList<>();
		for (PropertyDescriptor pd : PropertyUtils.getPropertyDescriptors(EmrApiConfiguration.class)) {
			propertyNames.add(pd.getName());
		}
		propertyNames.remove("class");
		assertEquals(41, config.keySet().size());
		assertTrue(config.keySet().containsAll(propertyNames));
		assertEquals("org.openmrs.module.emrapi", config.get("metadataSourceName"));
		assertEquals("50", config.get("lastViewedPatientSizeLimit").toString());
		assertEquals("Unknown Location", mapNode(config, "unknownLocation").get("display"));
	}

	@Test
	public void shouldGetCustomRepresentation() throws Exception {
		request.addParameter("v", "custom:(unknownLocation:(display),admissionEncounterType:full)");
		SimpleObject config = emrApiConfigurationController.getEmrApiConfiguration(request, response);
		assertEquals(2, config.size());
		assertThat(config.keySet(), containsInAnyOrder("unknownLocation", "admissionEncounterType"));
		assertEquals(1, mapNode(config, "unknownLocation").size());
		assertEquals("Unknown Location", mapNode(config, "unknownLocation").get("display"));
		assertEquals("06087111-222-11e3-9c1a-0800200c9a66", mapNode(config, "admissionEncounterType").get("uuid"));
		assertEquals("Admission", mapNode(config, "admissionEncounterType").get("name"));
	}

	private Map<String, Object> mapNode(SimpleObject o, String key) {
		return o.get(key);
	}
}
