/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.fhircondition.api.dao.impl;

import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import org.hibernate.Criteria;
import org.openmrs.annotation.Authorized;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.emrapi.conditionslist.PrivilegeConstants;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.dao.impl.BaseFhirDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static org.hibernate.criterion.Restrictions.eq;

@Primary
@Component("fhir.condition.fhirConditionDaoImpl")
@OpenmrsProfile(openmrsPlatformVersion = "2.0.* - 2.1.*")
public class FhirConditionDaoImpl extends BaseFhirDao<org.openmrs.module.emrapi.conditionslist.Condition> implements FhirConditionDao<org.openmrs.module.emrapi.conditionslist.Condition> {

    @Override
    @Authorized(PrivilegeConstants.GET_CONDITIONS)
    public Condition get(@Nonnull String uuid) {
        return super.get(uuid);
    }

    @Override
    @Authorized(PrivilegeConstants.EDIT_CONDITIONS)
    public Condition createOrUpdate(@Nonnull Condition newEntry) {
        return super.createOrUpdate(newEntry);
    }

    @Override
    @Authorized(PrivilegeConstants.EDIT_CONDITIONS)
    public Condition delete(@Nonnull String uuid) {
        return super.delete(uuid);
    }

    @Override
    @Authorized(PrivilegeConstants.GET_CONDITIONS)
    public List<String> getSearchResultUuids(@Nonnull SearchParameterMap theParams) {
        return super.getSearchResultUuids(theParams);
    }

    @Override
    @Authorized(PrivilegeConstants.GET_CONDITIONS)
    public List<Condition> getSearchResults(@Nonnull SearchParameterMap theParams,
                                            @Nonnull List<String> matchingResourceUuids, int firstResult, int lastResult) {
        return super.getSearchResults(theParams, matchingResourceUuids, firstResult, lastResult);
    }

    @Override
    protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
        theParams.getParameters().forEach(entry -> {
            switch (entry.getKey()) {
                case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
                    entry.getValue()
                            .forEach(param -> handlePatientReference(criteria, (ReferenceAndListParam) param.getParam()));
                    break;
                case FhirConstants.CODED_SEARCH_HANDLER:
                    entry.getValue().forEach(param -> handleCode(criteria, (TokenAndListParam) param.getParam()));
                    break;
                case FhirConstants.CONDITION_CLINICAL_STATUS_HANDLER:
                    entry.getValue().forEach(param -> handleClinicalStatus(criteria, (TokenAndListParam) param.getParam()));
                    break;
                case FhirConstants.DATE_RANGE_SEARCH_HANDLER:
                    entry.getValue()
                            .forEach(param -> handleDateRange(param.getPropertyName(), (DateRangeParam) param.getParam())
                                    .ifPresent(criteria::add));
                    break;
            }
        });
    }

    private void handleCode(Criteria criteria, TokenAndListParam code) {
        if (code != null) {
            criteria.createAlias("condition.coded", "cd");
            handleCodeableConcept(criteria, code, "cd", "map", "term").ifPresent(criteria::add);
        }
    }

    private void handleClinicalStatus(Criteria criteria, TokenAndListParam status) {
        handleAndListParam(status, tokenParam -> Optional.of(eq("clinicalStatus", convertStatus(tokenParam.getValue()))))
                .ifPresent(criteria::add);
    }

    private Condition.Status convertStatus(String status) {
        if ("active".equalsIgnoreCase(status)) {
            return Condition.Status.ACTIVE;
        }
        return Condition.Status.INACTIVE;
    }
}
