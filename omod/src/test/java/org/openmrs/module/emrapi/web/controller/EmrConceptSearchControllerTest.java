package org.openmrs.module.emrapi.web.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSearchResult;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.when;


public class EmrConceptSearchControllerTest extends BaseEmrControllerTest {
    @Mock
    EmrConceptService emrConceptService;
    @Mock
    EmrApiProperties emrApiProperties;
    @Autowired
    ConceptService conceptService;

    @InjectMocks
    EmrConceptSearchController emrConceptSearchController;

    @Before
    public void setUp() throws Exception {
        initMocks();
        executeDataSet("emrConceptSearchMetaData.xml");
    }

    @Test
    public void shouldFetchDefaultLocaleFullySpecifiedName() throws Exception {

        ConceptName conceptName = conceptService.getConceptNameByUuid("d102c80f-wety-4da3-99tt-8122ce8868df");
        ConceptSearchResult conceptSearchResult = new ConceptSearchResult("", conceptService.getConceptByUuid("1002c80f-1yz9-4da3-bb88-8122ce8868df"), conceptName);

        List<ConceptSearchResult> results = new ArrayList<ConceptSearchResult>();
        results.add(conceptSearchResult);

        when(emrApiProperties.getDiagnosisSets()).thenReturn(null);
        when(emrConceptService.conceptSearch("ang", Locale.ENGLISH, null,null, null, 10)).thenReturn(results);
        Object searchResult = emrConceptSearchController.search("ang", 10);
        Assert.assertNotNull(searchResult);
    }


    @Test
    public void shouldFetchDefaultLocaleShortName() throws Exception {
        List<SimpleObject> simpleObjects = new ArrayList<SimpleObject>();
        SimpleObject simpleObject = new SimpleObject();
        simpleObject.add("conceptName","fever" );
        simpleObject.add("conceptUuid","1002c80f-1yz9-4da3-9999-8122ce8868df" );
        simpleObject.add("matchedName", "fever");

        simpleObjects.add(simpleObject);

        ConceptName conceptNameShort = conceptService.getConceptNameByUuid("d1010003-wety-4da3-99tt-8122ce886834");

        ConceptSearchResult conceptSearchResult = new ConceptSearchResult("", conceptService.getConceptByUuid("1002c80f-1yz9-4da3-9999-8122ce8868df"), conceptNameShort);

        List<ConceptSearchResult> results = new ArrayList<ConceptSearchResult>();
        results.add(conceptSearchResult);

        when(emrApiProperties.getDiagnosisSets()).thenReturn(null);
        when(emrConceptService.conceptSearch("fev", Locale.ENGLISH, null,null, null, 10)).thenReturn(results);
        Object searchResult = emrConceptSearchController.search("fev", 10);
        Assert.assertEquals(searchResult, simpleObjects);
    }

    @Test
    public void shouldFetchLocaleSpecifiedShortNameIfPresent() throws Exception {
        List<SimpleObject> simpleObjects = new ArrayList<SimpleObject>();
        SimpleObject simpleObject = new SimpleObject();
        simpleObject.add("conceptName","Tuberculosis French" );
        simpleObject.add("conceptUuid","1002c80f-1yz9-4da3-2222-8122ce8868df" );
        simpleObject.add("matchedName", "Tuberculosis French");

        simpleObjects.add(simpleObject);

        ConceptName conceptNameShort = conceptService.getConceptNameByUuid("d1010003-wety-4da3-fren-8122ce886834");

        ConceptSearchResult conceptSearchResult = new ConceptSearchResult("", conceptService.getConceptByUuid("1002c80f-1yz9-4da3-2222-8122ce8868df"), conceptNameShort);

        List<ConceptSearchResult> results = new ArrayList<ConceptSearchResult>();
        results.add(conceptSearchResult);

        when(emrApiProperties.getDiagnosisSets()).thenReturn(null);
        Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "fr" );
        when(emrConceptService.conceptSearch("tub", Locale.FRENCH, null,null, null, 10)).thenReturn(results);
        Object searchResult = emrConceptSearchController.search("tub", 10);
        Assert.assertEquals(searchResult, simpleObjects);

    }

    @Test
    public void shouldFetchLocaleSpecifiedFullySpecifiedName() throws Exception {
        List<SimpleObject> simpleObjects = new ArrayList<SimpleObject>();
        SimpleObject simpleObject = new SimpleObject();
        simpleObject.add("conceptName","Malaria French Full" );
        simpleObject.add("conceptUuid","1005c80f-1yz9-4da3-2222-8122ce8868df" );
        simpleObject.add("matchedName", "Malaria French Full");

        simpleObjects.add(simpleObject);

        ConceptName conceptNameFull = conceptService.getConceptNameByUuid("d1010005-wety-4da3-shrt-8122ce886834");

        ConceptSearchResult conceptSearchResult = new ConceptSearchResult("", conceptService.getConceptByUuid("1005c80f-1yz9-4da3-2222-8122ce8868df"), conceptNameFull);

        List<ConceptSearchResult> results = new ArrayList<ConceptSearchResult>();
        results.add(conceptSearchResult);

        when(emrApiProperties.getDiagnosisSets()).thenReturn(null);
        Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "fr" );
        when(emrConceptService.conceptSearch("Mal", Locale.FRENCH, null,null, null, 10)).thenReturn(results);
        Object searchResult = emrConceptSearchController.search("Mal", 10);
        Assert.assertEquals(searchResult, simpleObjects);

    }


    @Test
    public void shouldFetchLocaleMatachedFullySpecifiedNameWithNonMatchedShortName() throws Exception {
        List<SimpleObject> simpleObjects = new ArrayList<SimpleObject>();
        SimpleObject simpleObject = new SimpleObject();
        simpleObject.add("conceptName","Mamaria Fr Short" );
        simpleObject.add("conceptUuid","1006c80f-1yz9-4da3-2222-8122ce8868df" );
        simpleObject.add("matchedName", "Makaria French Full");

        simpleObjects.add(simpleObject);

        ConceptName conceptNameFull = conceptService.getConceptNameByUuid("d1010006-wety-4da3-shrt-8122ce886834");

        ConceptSearchResult conceptSearchResult = new ConceptSearchResult("", conceptService.getConceptByUuid("1006c80f-1yz9-4da3-2222-8122ce8868df"), conceptNameFull);

        List<ConceptSearchResult> results = new ArrayList<ConceptSearchResult>();
        results.add(conceptSearchResult);

        when(emrApiProperties.getDiagnosisSets()).thenReturn(null);
        Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "fr" );
        when(emrConceptService.conceptSearch("Mak", Locale.FRENCH, null,null, null, 10)).thenReturn(results);
        Object searchResult = emrConceptSearchController.search("Mak", 10);
        Assert.assertEquals(searchResult, simpleObjects);
    }


    @Test
    public void shouldFetchLocaleMatachedFullySpecifiedNameWithNonMatchedSynonim() throws Exception {
        List<SimpleObject> simpleObjects = new ArrayList<SimpleObject>();
        SimpleObject simpleObject = new SimpleObject();
        simpleObject.add("conceptName","Bookar Hindhi Full" );
        simpleObject.add("conceptUuid","1007c80f-1yz9-4da3-2222-8122ce8868df" );
        simpleObject.add("matchedName", "Sardhi Hi Synonim");

        simpleObjects.add(simpleObject);

        ConceptName conceptNameFull = conceptService.getConceptNameByUuid("d1010007-wety-4da3-fren-8122ce886834");

        ConceptSearchResult conceptSearchResult = new ConceptSearchResult("", conceptService.getConceptByUuid("1007c80f-1yz9-4da3-2222-8122ce8868df"), conceptNameFull);

        List<ConceptSearchResult> results = new ArrayList<ConceptSearchResult>();
        results.add(conceptSearchResult);

        when(emrApiProperties.getDiagnosisSets()).thenReturn(null);
        Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "fr" );
        when(emrConceptService.conceptSearch("Sar", Locale.FRENCH, null,null, null, 10)).thenReturn(results);
        Object searchResult = emrConceptSearchController.search("Sar", 10);
        Assert.assertEquals(simpleObjects,searchResult);
    }
}