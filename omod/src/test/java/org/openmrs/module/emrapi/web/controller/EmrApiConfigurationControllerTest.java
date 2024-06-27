package org.openmrs.module.emrapi.web.controller;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.metadatamapping.api.MetadataMappingService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0.LocationResource2_0;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;
import java.util.Set;

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
	public void shouldGetAsJson() throws Exception {
		request.addParameter("v", "full");
		SimpleObject config = emrApiConfigurationController.getEmrApiConfiguration(request, response);
		String jsonString = new ObjectMapper().writeValueAsString(config);
		Assert.assertTrue(jsonString.contains("unknownLocation"));
	}

	@Test
	public void shouldGetDefaultRepresentation() {
		SimpleObject config = emrApiConfigurationController.getEmrApiConfiguration(request, response);
		assertEquals(46, config.keySet().size());
		assertEquals("org.openmrs.module.emrapi", config.get("metadataSourceName"));
		assertEquals("50", config.get("lastViewedPatientSizeLimit").toString());
		Map<String, Object> unknownLocation = mapNode(config, "unknownLocation");
		assertEquals(3, unknownLocation.size());
		assertThat(unknownLocation.keySet(), containsInAnyOrder("uuid", "display", "links"));
		assertEquals("Unknown Location", unknownLocation.get("display"));
	}

	@Test
	public void shouldGetFullRepresentation() {
		request.addParameter("v", "full");
		SimpleObject config = emrApiConfigurationController.getEmrApiConfiguration(request, response);
		Map<String, Object> unknownLocation = mapNode(config, "unknownLocation");
		DelegatingResourceDescription drd = new LocationResource2_0().getRepresentationDescription(Representation.FULL);
		Set<String> expectedProps = drd.getProperties().keySet();
		for (String prop : expectedProps) {
			Assert.assertTrue("Expected property: " + prop, unknownLocation.containsKey(prop));
		}
		for (int i=1; i<=15; i++) {
			assertTrue(unknownLocation.containsKey("address"+i));
		}
		assertEquals("Unknown Location", unknownLocation.get("display"));
	}

	@Test
	public void shouldGetCustomRepresentation() {
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
