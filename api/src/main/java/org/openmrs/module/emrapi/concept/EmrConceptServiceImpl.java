/*
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

package org.openmrs.module.emrapi.concept;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class EmrConceptServiceImpl extends BaseOpenmrsService implements EmrConceptService {

    private final Log log = LogFactory.getLog(getClass());

    private EmrConceptDAO dao;

    private ConceptService conceptService;

    private EmrApiProperties emrApiProperties;

    // This will match "ICD10:A50" or "PIH : Admit"
    // [^:]+? ... anything that is not a colon, reluctantly (so the next thing catches trailing spaces)
    // \s* ... 0 or more whitespaces, greedily
    // .+ ... anything
    private Pattern codePattern = Pattern.compile("([^:]+?)\\s*:\\s*(.+)");

    public void setDao(EmrConceptDAO dao) {
        this.dao = dao;
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public void setConceptService(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    @Override
    public List<Concept> getConceptsSameOrNarrowerThan(ConceptReferenceTerm term) {
        if (term == null) {
            throw new IllegalArgumentException("term is required");
        }
        return dao.getConceptsMappedTo(Arrays.asList(emrApiProperties.getSameAsConceptMapType(), emrApiProperties.getNarrowerThanConceptMapType()), term);
    }

    @Override
    @Transactional(readOnly = true)
    public Concept getConcept(String mappingOrUuid) {
        Concept concept = null;

        Matcher matcher = codePattern.matcher(mappingOrUuid);
        if (matcher.matches()) {
            String sourceName = matcher.group(1);
            String code = matcher.group(2);
            ConceptSource source = conceptService.getConceptSourceByName(sourceName);
            if (source == null) {
                log.warn("Couldn't find concept source named " + sourceName + " while looking up concept by mapping: " + mappingOrUuid);
            }
            else {
                ConceptReferenceTerm referenceTerm = conceptService.getConceptReferenceTermByCode(code, source);
                // TODO ensure we return a SAME-AS mapping if one exists
                if (referenceTerm != null) {
                    List<Concept> concepts = getConceptsSameOrNarrowerThan(referenceTerm);
                    if (concepts.size() > 0) {
                        return concepts.get(0);
                    }
                }
            }
        }

        return conceptService.getConceptByUuid(mappingOrUuid);
    }

    @Override
    public List<ConceptSearchResult> conceptSearch(String query, Locale locale, Collection<ConceptClass> classes, Collection<Concept> inSets, Collection<ConceptSource> sources, Integer limit) {
        if (limit == null) {
            limit = 100;
        }
        return dao.conceptSearch(query, locale, classes, inSets, sources, limit);
    }

}
