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

package org.openmrs.module.emrapi.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptSource;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.OrderType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Provider;
import org.openmrs.VisitType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Helper class that lets modules centralize their configuration details.
 * Deprecated, replaced with {@link org.openmrs.module.metadatamapping.util.ModuleProperties which supports mappings}
 */
@Deprecated
@SuppressWarnings("SpringJavaAutowiringInspection")
public abstract class ModuleProperties {

    private static final Log log = LogFactory.getLog(ModuleProperties.class);

    @Autowired
    @Qualifier("conceptService")
    protected ConceptService conceptService;

    @Autowired
    @Qualifier("encounterService")
    protected EncounterService encounterService;

    @Autowired
    @Qualifier("visitService")
    protected VisitService visitService;

    @Autowired
    @Qualifier("orderService")
    protected OrderService orderService;

    @Autowired
    @Qualifier("adminService")
    protected AdministrationService administrationService;

    @Autowired
    @Qualifier("locationService")
    protected LocationService locationService;

    @Autowired
    @Qualifier("userService")
    protected UserService userService;

    @Autowired
    @Qualifier("patientService")
    protected PatientService patientService;

    @Autowired
    @Qualifier("personService")
    protected PersonService personService;

    @Autowired
    @Qualifier("providerService")
    protected ProviderService providerService;

    @Autowired
    @Qualifier("formService")
    protected FormService formService;

    public void setConceptService(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public void setAdministrationService(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    public void setEncounterService(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    public void setVisitService(VisitService visitService) {
        this.visitService = visitService;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }

    protected ConceptClass getConceptClassByGlobalProperty(String globalPropertyName) {
        String globalProperty = getGlobalProperty(globalPropertyName, true);
        ConceptClass conceptClass = conceptService.getConceptClassByUuid(globalProperty);
        if (conceptClass == null) {
            throw new IllegalStateException("Configuration required: " + globalPropertyName);
        }
        return conceptClass;
    }

    protected Concept getConceptByGlobalProperty(String globalPropertyName) {
        String globalProperty = administrationService.getGlobalProperty(globalPropertyName);
        Concept concept = conceptService.getConceptByUuid(globalProperty);
        if (concept == null) {
            throw new IllegalStateException("Configuration required: " + globalPropertyName);
        }
        return concept;
    }

    protected Concept getSingleConceptByMapping(ConceptSource conceptSource, String code) {
        List<Concept> candidates = conceptService.getConceptsByMapping(code, conceptSource.getName(), false);
        if (candidates.size() == 0) {
            throw new IllegalStateException("Configuration required: can't find a concept by mapping " + conceptSource.getName() + ":" + code);
        } else if (candidates.size() == 1) {
            return candidates.get(0);
        } else {
            throw new IllegalStateException("Configuration required: found more than one concept mapped as " + conceptSource.getName() + ":" + code);
        }
    }

    protected ConceptSource getConceptSourceByGlobalProperty(String globalPropertyName) {
        String globalProperty = administrationService.getGlobalProperty(globalPropertyName);
        ConceptSource conceptSource = conceptService.getConceptSourceByUuid(globalProperty);
        if (conceptSource == null) {
            throw new IllegalStateException("Configuration required: " + globalPropertyName);
        }
        return conceptSource;
    }

    protected EncounterType getEncounterTypeByUuid(String uuid, boolean required) {
        EncounterType encounterType = encounterService.getEncounterTypeByUuid(uuid);
        if (required && encounterType == null) {
            throw new IllegalStateException("Cannot find required EncounterType with uuid = " + uuid);
        }
        return encounterType;
    }

    protected EncounterType getEncounterTypeByGlobalProperty(String globalPropertyName) {
        return getEncounterTypeByGlobalProperty(globalPropertyName, true);
    }

    protected EncounterType getEncounterTypeByGlobalProperty(String globalPropertyName, boolean required) {
        String globalProperty = administrationService.getGlobalProperty(globalPropertyName);
        EncounterType encounterType = encounterService.getEncounterTypeByUuid(globalProperty);
        if (required && encounterType == null) {
            throw new IllegalStateException("Configuration required: " + globalPropertyName);
        }
        return encounterType;
    }

    protected EncounterRole getEncounterRoleByGlobalProperty(String globalPropertyName) {
        String globalProperty = administrationService.getGlobalProperty(globalPropertyName);
        EncounterRole encounterRole = encounterService.getEncounterRoleByUuid(globalProperty);
        if (encounterRole == null) {
            throw new IllegalStateException("Configuration required: " + globalPropertyName);
        }
        return encounterRole;
    }

    protected VisitType getVisitTypeByGlobalProperty(String globalPropertyName) {
        String globalProperty = administrationService.getGlobalProperty(globalPropertyName);
        VisitType visitType = visitService.getVisitTypeByUuid(globalProperty);
        if (visitType == null) {
            throw new IllegalStateException("Configuration required: " + globalPropertyName);
        }
        return visitType;
    }

    protected VisitType getVisitTypeByUuid(String uuid, boolean required) {
        VisitType visitType = visitService.getVisitTypeByUuid(uuid);
        if (required && visitType == null) {
            throw new IllegalStateException("Cannot find required VisitType with uuid = " + uuid);
        }
        return visitType;
    }

    protected OrderType getOrderTypeByGlobalProperty(String globalPropertyName) {
        String globalProperty = administrationService.getGlobalProperty(globalPropertyName);
        OrderType orderType = orderService.getOrderTypeByUuid(globalProperty);
        if (orderType == null) {
            throw new IllegalStateException("Configuration required: " + globalPropertyName);
        }
        return orderType;
    }

    protected Location getLocationByGlobalProperty(String globalPropertyName) {
        String globalProperty = administrationService.getGlobalProperty(globalPropertyName);
        Location location = locationService.getLocationByUuid(globalProperty);
        if (location == null) {
            throw new IllegalStateException("Configuration required: " + globalPropertyName);
        }
        return location;
    }

    protected Provider getProviderByGlobalProperty(String globalPropertyName) {
        String globalProperty = administrationService.getGlobalProperty(globalPropertyName);
        Provider provider = providerService.getProviderByUuid(globalProperty);
        if (provider == null) {
            throw new IllegalStateException("Configuration required: " + globalPropertyName);
        }
        return provider;
    }

    protected Form getFormByGlobalProperty(String globalPropertyName) {
        // note that we are allowing forms to be null at this point
        String globalProperty = administrationService.getGlobalProperty(globalPropertyName);
        return formService.getFormByUuid(globalProperty);
    }

    protected PatientIdentifierType getPatientIdentifierTypeByGlobalProperty(String globalPropertyName, boolean required) {
        String globalProperty = getGlobalProperty(globalPropertyName, required);
        PatientIdentifierType patientIdentifierType = GeneralUtils.getPatientIdentifierType(globalProperty, patientService);
        if (required && patientIdentifierType == null) {
            throw new IllegalStateException("Configuration required: " + globalPropertyName);
        }
        return patientIdentifierType;
    }

    protected List<PatientIdentifierType> getPatientIdentifierTypesByGlobalProperty(String globalPropertyName, boolean required) {
        List<PatientIdentifierType> types = new ArrayList<PatientIdentifierType>();
        String globalProperty = getGlobalProperty(globalPropertyName, required);
        if (StringUtils.isNotEmpty(globalProperty)) {
            for (String type : globalProperty.split(",")) {
                PatientIdentifierType patientIdentifierType = patientService.getPatientIdentifierTypeByUuid(type);
                if (patientIdentifierType != null) {
                    types.add(patientIdentifierType);
                } else {
                    log.warn("Global property " + globalPropertyName + " specifies an unknown patient identifier type: " + type);
                }
            }
        }
        return types;
    }

    protected Integer getIntegerByGlobalProperty(String globalPropertyName) {
        String globalProperty = getGlobalProperty(globalPropertyName, true);
        try {
            return Integer.valueOf(globalProperty);
        }
        catch (Exception e) {
            throw new IllegalStateException("Global property " + globalPropertyName + " value of " + globalProperty + " is not parsable as an Integer");
        }
    }

    protected String getGlobalProperty(String globalPropertyName, boolean required) {
        String globalProperty = administrationService.getGlobalProperty(globalPropertyName);
        if (required && StringUtils.isEmpty(globalProperty)) {
            throw new IllegalStateException("Configuration required: " + globalPropertyName);
        }
        return globalProperty;
    }

    protected Collection<Concept> getConceptsByGlobalProperty(String gpName) {
        String gpValue = getGlobalProperty(gpName, false);

        if (!org.springframework.util.StringUtils.hasText(gpValue)) {
            return Collections.emptyList();
        }

        List<Concept> result = new ArrayList<Concept>();

        String[] concepts = gpValue.split("\\,");
        for (String concept : concepts) {
            Concept foundConcept = conceptService.getConceptByUuid(concept);
            if (foundConcept == null) {
                String[] mapping = concept.split("\\:");
                if (mapping.length == 2) {
                    foundConcept = conceptService.getConceptByMapping(mapping[0], mapping[1]);
                }
            }

            if (foundConcept != null) {
                result.add(foundConcept);
            } else {
                throw new IllegalStateException("Invalid configuration: concept '" + concept + "' defined in " + gpName + " does not exist");
            }
        }

        return result;
    }
}
