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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.LocaleUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.web.bind.annotation.ValueConstants.DEFAULT_NONE;

@Controller
@RequestMapping(method = RequestMethod.GET, value = "/rest/emrapi/concept")
public class EmrConceptSearchController {
    @Autowired
    EmrApiProperties emrApiProperties;
    @Autowired
    EmrConceptService emrService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object search(@RequestParam("term") String query, @RequestParam Integer limit,
                         @RequestParam(required = false, defaultValue = DEFAULT_NONE) String locale) throws Exception {
        Collection<Concept> diagnosisSets = emrApiProperties.getDiagnosisSets();
        List<ConceptSource> conceptSources = emrApiProperties.getConceptSourcesForDiagnosisSearch();
        Locale searchLocale = getSearchLocale(locale);
        List<ConceptSearchResult> conceptSearchResults =
                emrService.conceptSearch(query, searchLocale, null, diagnosisSets, conceptSources, limit);
        ConceptSource conceptSource = conceptSources.isEmpty() ? null: conceptSources.get(0);
        return createListResponse(conceptSearchResults, conceptSource, searchLocale);
    }

    private List<SimpleObject> createListResponse(List<ConceptSearchResult> resultList, ConceptSource conceptSource, Locale searchLocale) {
        List<SimpleObject> allDiagnoses = new ArrayList<SimpleObject>();

        for (ConceptSearchResult diagnosis : resultList) {
            SimpleObject diagnosisObject = new SimpleObject();
            ConceptName conceptName = diagnosis.getConcept().getName(searchLocale);
            if (conceptName == null) {
                conceptName = diagnosis.getConcept().getName();
            }
            diagnosisObject.add("conceptName", conceptName.getName());
            diagnosisObject.add("conceptUuid", diagnosis.getConcept().getUuid());
            if(diagnosis.getConceptName()!=null) {
                diagnosisObject.add("matchedName", diagnosis.getConceptName().getName());
            }
            ConceptReferenceTerm term = getConceptReferenceTermByConceptSource(diagnosis.getConcept(), conceptSource);
            if(term != null) {
                diagnosisObject.add("code", term.getCode());
            }
            allDiagnoses.add(diagnosisObject);
        }
        return allDiagnoses;
    }

    private ConceptReferenceTerm getConceptReferenceTermByConceptSource(Concept concept, ConceptSource conceptSource) {
        Collection<ConceptMap> conceptMappings = concept.getConceptMappings();
        if(conceptMappings != null && conceptSource != null) {
            for (ConceptMap cm : conceptMappings) {
                ConceptReferenceTerm term = cm.getConceptReferenceTerm();
                if (conceptSource.equals(term.getConceptSource())) {
                    return term;
                }
            }
        }
        return null;
    }

    private Locale getSearchLocale(String localeStr) {
        if (localeStr == null) {
            return Context.getLocale();
        }
        Locale locale;
        try {
            locale = LocaleUtils.toLocale(localeStr);
        }  catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(localeErrorMessage("emrapi.conceptSearch.invalidLocale", localeStr));
        }
        if (allowedLocale(locale)) {
            return locale;
        } else {
            throw new IllegalArgumentException(localeErrorMessage("emrapi.conceptSearch.unsupportedLocale", localeStr));
        }
    }

    private boolean allowedLocale(Locale locale) {
        Set<Locale> allowedLocales = new HashSet<Locale>(Context.getAdministrationService().getAllowedLocales());
        return allowedLocales.contains(locale);
    }

    private String localeErrorMessage(String msgKey, String localeStr) {
        return Context.getMessageSourceService().getMessage(msgKey, new Object[] { localeStr }, Context.getLocale());
    }
}
