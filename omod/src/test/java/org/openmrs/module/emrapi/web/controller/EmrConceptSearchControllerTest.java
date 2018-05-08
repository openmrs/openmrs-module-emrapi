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


    /**
     * Tests how EmrConceptSearchController handle empty and non-empty lists of ConceptSource.
     * This test method basically gets and manipulates the specific 'ICD-10-WHO' ConceptSource since it will be fetched by
     * {@link org.openmrs.module.emrapi.EmrApiProperties#getConceptSourcesForDiagnosisSearch()} method
     * which is invoked on the {@link EmrConceptSearchController#emrApiProperties} instance injected
     * into {@link EmrConceptSearchController} class
     * @throws Exception
     */
    @Test
    public void shouldHandleEmptyListOfConceptSource() throws Exception {

        ConceptSource source = cs.getConceptSourceByName("ICD-10-WHO");
        source.setName("foobar"); // so that "ICD-10-WHO" can't be found by name - produces empty list of ConceptSources
        cs.saveConceptSource(source);

        @SuppressWarnings("unchecked")
        List<SimpleObject> response1 = deserialize(handle(newGetRequest("/rest/emrapi/concept",new Parameter[]{new Parameter("term", "Diabetes"), new Parameter("limit", "100")})), new TypeReference<List>() {});
        Assert.assertEquals(2, response1.size());

        source.setName("ICD-10-WHO"); // so that "ICD-10-WHO" conceptSource is not null - produces non-empty list of ConceptSources
        cs.saveConceptSource(source);

        response1 = deserialize(handle(newGetRequest("/rest/emrapi/concept",new Parameter[]{new Parameter("term", "Diabetes"), new Parameter("limit", "100")})), new TypeReference<List>() {});
        Assert.assertEquals(2, response1.size());

    }

}
