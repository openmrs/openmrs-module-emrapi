/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

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

