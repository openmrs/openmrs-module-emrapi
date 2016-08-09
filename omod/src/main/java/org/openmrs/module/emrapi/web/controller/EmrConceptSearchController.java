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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSearchResult;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.util.StringUtils;

@Controller
@RequestMapping(method = RequestMethod.GET, value = "/rest/emrapi/concept")
public class EmrConceptSearchController {
    @Autowired
    EmrApiProperties emrApiProperties;
    @Autowired
    EmrConceptService emrService;

    private Locale locale;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object search(@RequestParam("term") String query, @RequestParam Integer limit) throws Exception {

        Collection<Concept> diagnosisSets = emrApiProperties.getDiagnosisSets();
        locale = getUserDefaultLocale(Context.getAuthenticatedUser());
        List<ConceptSearchResult> conceptSearchResults = emrService.conceptSearch(query, locale , null, diagnosisSets, null, limit);
        if(CollectionUtils.isEmpty(conceptSearchResults)){
            locale = getSystemDefaultLocale();
            conceptSearchResults = emrService.conceptSearch(query, locale , null, diagnosisSets, null, limit);
        }
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
            diagnosisObject.add("conceptName", getConceptName(diagnosis.getConcept()));
            diagnosisObject.add("conceptUuid", diagnosis.getConcept().getUuid());
            diagnosisObject.add("matchedName", diagnosis.getName());
            allDiagnoses.add(diagnosisObject);
        }
        return allDiagnoses;
    }

    private String getConceptName(Concept concept){
        if(concept.getShortNameInLocale(locale) !=null)
            return concept.getShortNameInLocale(locale).getName();
        else {
            ConceptName fullySpecifiedName = concept.getFullySpecifiedName(locale);
            if (fullySpecifiedName != null)
                return concept.getFullySpecifiedName(locale).getName();
            return concept.getName().getName();
        }
    }

    private Locale getUserDefaultLocale(User authenticatedUser) {
        String userDefaultLocale = authenticatedUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE);
        return LocaleUtility.fromSpecification(userDefaultLocale);
    }

    private Locale getSystemDefaultLocale() {
        String systemDefaultLocale = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE);
        if (systemDefaultLocale != null) {
            return LocaleUtility.fromSpecification(systemDefaultLocale);
        }
        return Locale.ENGLISH;
    }
}

