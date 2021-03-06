/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.fhircondition.api.impl;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.QuantityAndListParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Condition;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirConditionService;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.impl.BaseFhirService;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.HashSet;

@Slf4j
@Primary
@Transactional
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PROTECTED)
@Component("fhir.condition.fhirConditionServiceImpl")
@OpenmrsProfile(openmrsPlatformVersion = "2.0.* - 2.1.*")
public class FhirConditionServiceImpl extends BaseFhirService<org.hl7.fhir.r4.model.Condition, org.openmrs.module.emrapi.conditionslist.Condition> implements FhirConditionService {

    @Autowired
    @Qualifier("fhir.condition.fhirConditionDaoImpl")
    private FhirConditionDao<org.openmrs.module.emrapi.conditionslist.Condition> dao;

    @Autowired
    @Qualifier("fhir.condition.conditionTranslatorImpl")
    private ConditionTranslator<org.openmrs.module.emrapi.conditionslist.Condition> translator;

    @Autowired
    private SearchQueryInclude<Condition> searchQueryInclude;

    @Autowired
    private SearchQuery<org.openmrs.module.emrapi.conditionslist.Condition,
            Condition, FhirConditionDao<org.openmrs.module.emrapi.conditionslist.Condition>,
            ConditionTranslator<org.openmrs.module.emrapi.conditionslist.Condition>, SearchQueryInclude<Condition>> searchQuery;

    @Override
    public Condition create(@Nonnull Condition newResource) {
        if (newResource.getId() == null) {
            newResource.setId(FhirUtils.newUuid());
        }
        return super.create(newResource);
    }

    @Override
    public Condition update(@Nonnull String uuid, @Nonnull Condition updatedResource) {
        return super.update(uuid, updatedResource);
    }

    @Override
    public IBundleProvider searchConditions(ReferenceAndListParam patientParam, TokenAndListParam code,
                                            TokenAndListParam clinicalStatus, DateRangeParam onsetDate, QuantityAndListParam onsetAge,
                                            DateRangeParam recordedDate, TokenAndListParam id, DateRangeParam lastUpdated, @Sort SortSpec sort,
                                            HashSet<Include> includes) {
        SearchParameterMap theParams = new SearchParameterMap()
                .addParameter(FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER, patientParam)
                .addParameter(FhirConstants.CODED_SEARCH_HANDLER, code)
                .addParameter(FhirConstants.CONDITION_CLINICAL_STATUS_HANDLER, clinicalStatus)
                .addParameter(FhirConstants.QUANTITY_SEARCH_HANDLER, onsetAge)
                .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "onsetDate", onsetDate)
                .addParameter(FhirConstants.DATE_RANGE_SEARCH_HANDLER, "dateCreated", recordedDate)
                .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.ID_PROPERTY, id)
                .addParameter(FhirConstants.COMMON_SEARCH_HANDLER, FhirConstants.LAST_UPDATED_PROPERTY, lastUpdated)
                .addParameter(FhirConstants.INCLUDE_SEARCH_HANDLER, includes).setSortSpec(sort);

        return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
    }
}
