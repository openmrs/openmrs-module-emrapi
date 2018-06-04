package org.openmrs.module.emrapi.web.controller;

import org.apache.poi.util.SystemOutLogger;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:moduleApplicationContext.xml"}, inheritLocations = true)
public class EmrConceptSearchControllerTest  extends BaseEmrControllerTest {

	@Autowired
	EmrApiProperties emrApiProperties;

	@Autowired
	@Qualifier("conceptService")
	private ConceptService cs;

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

	@Test
	public void shouldHandleEmptyListOfDiagnosesConceptSource() throws Exception {    			
		ConceptSource source = cs.getConceptSourceByName("ICD-10-WHO");

        source.setName("foobar"); // so that "ICD-10-WHO" can't be found by name - produces empty list of ConceptSources
        cs.saveConceptSource(source);

		Assert.assertNotNull(emrApiProperties.getConceptSourcesForDiagnosisSearch());
        Assert.assertEquals(0, emrApiProperties.getConceptSourcesForDiagnosisSearch().size());

		MockHttpServletRequest getRequest = newGetRequest("/rest/emrapi/concept",new Parameter[]{new Parameter("term", "Diabetes"), new Parameter("limit", "100")});
        @SuppressWarnings("unchecked")
        List<SimpleObject> response = deserialize(handle(getRequest), new TypeReference<List>() {});
        Assert.assertEquals(2, response.size());

        List<String> actualUuids = new ArrayList<String>();
        for(Map simpleObject : response){
			Assert.assertNull(simpleObject.get("code"));
			actualUuids.add((String) simpleObject.get("conceptUuid"));
        }


        source.setName("ICD-10-WHO"); // so that "ICD-10-WHO" conceptSource is not null - produces non-empty list of ConceptSources
        cs.saveConceptSource(source);

		Assert.assertNotNull(emrApiProperties.getConceptSourcesForDiagnosisSearch());
		Assert.assertEquals(1, emrApiProperties.getConceptSourcesForDiagnosisSearch().size());

		response = deserialize(handle(getRequest), new TypeReference<List>() {});
        Assert.assertEquals(2, response.size());

        List<String> expectedUuids = new ArrayList<String>();
        for(Map simpleObject : response){
			Assert.assertNotNull(simpleObject.get("code"));
			expectedUuids.add((String) simpleObject.get("conceptUuid"));
        }

        //both lists shall have the same concepts identified by the unique UUID.
        Assert.assertArrayEquals(expectedUuids.toArray(), actualUuids.toArray());
	}

}
