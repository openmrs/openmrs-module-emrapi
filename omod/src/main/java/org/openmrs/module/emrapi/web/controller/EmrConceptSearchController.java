package org.openmrs.module.emrapi.web.controller;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSearchResult;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/*
* Temporary controller to return all diagnoses
*/

@Controller
@RequestMapping(method = RequestMethod.GET, value = "/rest/emrapi/concept")
public class EmrConceptSearchController {
    @Autowired
    EmrApiProperties emrApiProperties;
    @Autowired
    EmrConceptService emrService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object search(@RequestParam("term") String query, @RequestParam Integer limit) throws Exception {

        Collection<Concept> diagnosisSets = emrApiProperties.getDiagnosisSets();
        Locale locale = Locale.ENGLISH;
        List<ConceptSearchResult> conceptSearchResults = emrService.conceptSearch(query, locale, null, diagnosisSets, null, limit);
        List<ConceptName> matchingConceptNames = new ArrayList<ConceptName>();
        for (ConceptSearchResult searchResult : conceptSearchResults) {
            matchingConceptNames.add(searchResult.getConceptName());
        }
        return createListResponse(matchingConceptNames);
    }

    private List<SimpleObject> createListResponse(List<ConceptName> resultList) {
        List<SimpleObject> allDiagnoses = new ArrayList<SimpleObject>();

        for (ConceptName diagnosis : resultList) {
            SimpleObject diagnosisObject = new SimpleObject();
            diagnosisObject.add("conceptName", diagnosis.getConcept().getName().getName());
            diagnosisObject.add("conceptUuid", diagnosis.getConcept().getUuid());
            diagnosisObject.add("matchedName", diagnosis.getName());
            allDiagnoses.add(diagnosisObject);
        }
        return allDiagnoses;
    }
}

