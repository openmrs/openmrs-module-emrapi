package org.openmrs.module.emrapi.web.controller;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:moduleApplicationContext.xml"}, inheritLocations = true)
public class EmrConceptSearchControllerTest  extends BaseEmrControllerTest {

	@Autowired
	EmrApiProperties emrApiProperties;

	@Before
	public void setUp() throws Exception {
		executeDataSet("baseMetaData.xml");
		executeDataSet("diagnosisMetaData.xml");
	}

	@Test
	public void shouldSearchByName() throws Exception {
		List<SimpleObject> response = deserialize(handle(newGetRequest("/rest/emrapi/concept",new Parameter[]{new Parameter("term", "Diabetes"), new Parameter("limit", "100")})), new TypeReference<List>() {});
		assertEquals(2, response.size());
	}


	@Test
	public void shouldSearchByCodeExact() throws Exception {
		List<SimpleObject> response = deserialize(handle(newGetRequest("/rest/emrapi/concept",new Parameter[]{new Parameter("term", "ABC123"), new Parameter("limit", "100")})), new TypeReference<List>() {});
		assertEquals(1, response.size());
		assertEquals("Diabetes", ((Map)response.get(0)).get("conceptName"));
		Map diagnosisResponse = response.get(0);
		assertNull(diagnosisResponse.get("matchedName"));
		assertEquals("ABC123" ,diagnosisResponse.get("code"));
		assertEquals("Diabetes" ,diagnosisResponse.get("conceptName"));
		assertEquals("d102c80f-1yz9-4da3-bb88-8122ce8897de" ,diagnosisResponse.get("conceptUuid"));

	}

	@Test
	public void shouldNotDoLikeSearchByCode() throws Exception {
		List<SimpleObject> response = deserialize(handle(newGetRequest("/rest/emrapi/concept",new Parameter[]{new Parameter("term", "ABC12"), new Parameter("limit", "100")})), new TypeReference<List>() {});
		assertEquals(0, response.size());
	}

}
