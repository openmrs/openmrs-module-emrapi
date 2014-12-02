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

package org.openmrs.module.emrapi.patient;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.reporting.query.AwaitingAdmissionVisitQuery;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.module.reporting.query.visit.VisitQueryResult;
import org.openmrs.module.reporting.query.visit.service.VisitQueryService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class PatientDomainWrapperComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EmrConceptService emrConceptService;

    @Autowired
    private DispositionService dispositionService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    private TestDataManager testDataManager;

    @Autowired
    private PatientDomainWrapperFactory factory;

    private DispositionDescriptor dispositionDescriptor;

    @Before
    public void setup() throws Exception {
        executeDataSet("baseTestDataset.xml");
        dispositionDescriptor = ContextSensitiveMetadataTestUtils.setupDispositionDescriptor(conceptService, dispositionService);
        ContextSensitiveMetadataTestUtils.setupAdmissionDecisionConcept(conceptService, emrApiProperties);

    }

    @Test
    public void testThatBeanCanHavePropertiesAutowired() throws Exception {
        PatientDomainWrapper patientDomainWrapper = factory.newPatientDomainWrapper();
        assertThat(patientDomainWrapper.emrApiProperties, notNullValue());
    }

    @Test
    public void shouldFindVisitAwaitingAdmission() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        Location visitLocation = testDataManager.location().name("Visit Location")
                .tag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS).save();

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .location(visitLocation)
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new Date())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .save();

        PatientDomainWrapper patientDomainWrapper = factory.newPatientDomainWrapper(patient);
        assertThat(patientDomainWrapper.isAwaitingAdmission(visitLocation), is(true));
    }

    @Test
    public void shouldNotCountDispositionOnVoidedEncounter() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        Location visitLocation = testDataManager.location().name("Visit Location")
                .tag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS).save();

        // a visit with a single *voided* visit note encounter with dispo = ADMIT
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .location(visitLocation)
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new Date())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .voided(true)
                                .dateVoided(new Date())
                                .voidReason("test")
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .save();

        PatientDomainWrapper patientDomainWrapper = factory.newPatientDomainWrapper(patient);
        assertThat(patientDomainWrapper.isAwaitingAdmission(visitLocation), is(false));
    }


    @Test
    public void shouldNotFindVisitIfAtAnotherLocation() throws Exception {

        Patient patient = testDataManager.randomPatient().save();
        Location visitLocation = testDataManager.location().name("Visit Location")
                .tag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS).save();
        Location queryLocation = testDataManager.location().name("Query Location")
                .tag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS).save();

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .location(visitLocation)
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new Date())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .save();

        PatientDomainWrapper patientDomainWrapper = factory.newPatientDomainWrapper(patient);
        assertThat(patientDomainWrapper.isAwaitingAdmission(queryLocation), is(false));
    }

    @Test
    public void shouldFindVisitIfAtSameLocation() throws Exception {

        Patient patient = testDataManager.randomPatient().save();
        Location visitLocation = testDataManager.location().name("Visit Location")
                .tag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS).save();
        Location queryLocation = visitLocation;

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .location(visitLocation)
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new Date())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .save();

        PatientDomainWrapper patientDomainWrapper = factory.newPatientDomainWrapper(patient);
        assertThat(patientDomainWrapper.isAwaitingAdmission(visitLocation), is(true));

    }
}
