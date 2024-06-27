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

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
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

	@Test
	public void shouldGetDispositions() {
		request.addParameter("v", "custom:(dispositions)");
		SimpleObject config = emrApiConfigurationController.getEmrApiConfiguration(request, response);
		List<Map<String, Object>> dispositions = listNode(config, "dispositions");
		assertThat(dispositions.size(), equalTo(4));
		for (Map<String, Object> d : dispositions) {
			if (d.get("uuid").equals("d2d89630-b698-11e2-9e96-0800200c9a66")) {
				assertThat(d.get("name"), equalTo("disposition.death"));
				assertThat(d.get("conceptCode"), equalTo("org.openmrs.module.emrapi:Death"));
				assertThat(listNode(d, "additionalObs").size(), equalTo(1));
				assertThat(listNode(d, "additionalObs").get(0).get("conceptCode"), equalTo("org.openmrs.module.emrapi:Date of death"));
			}
			else if (d.get("uuid").equals("66de7f60-b73a-11e2-9e96-0800200c9a66")) {
				assertThat(d.get("name"), equalTo("disposition.admit"));
				assertThat(d.get("conceptCode"), equalTo("org.openmrs.module.emrapi:Admit to hospital"));
				assertThat(listNode(d, "additionalObs").size(), equalTo(0));
			}
			else if (d.get("uuid").equals("687d966bb-9c91-4886-b8b0-e63361f495f0")) {
				assertThat(d.get("name"), equalTo("disposition.observation"));
				assertThat(d.get("conceptCode"), equalTo("org.openmrs.module.emrapi:ED Observation"));
				assertThat(listNode(d, "additionalObs").size(), equalTo(0));
			}
			else if (d.get("uuid").equals("12129630-b698-11e2-9e96-0800200c9a66")) {
				assertThat(d.get("name"), equalTo("disposition.discharge"));
				assertThat(d.get("conceptCode"), equalTo("org.openmrs.module.emrapi:Discharged"));
				assertThat(listNode(d, "additionalObs").size(), equalTo(0));
			}
			else {
				Assert.fail("Unexpected disposition uuid: " + d.get("uuid"));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> mapNode(Map<String, Object> o, String key) {
		return (Map<String, Object>)o.get(key);
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> listNode(Map<String, Object> o, String key) {
		return (List<Map<String, Object>>)o.get(key);
	}
}
