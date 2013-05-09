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

package org.openmrs.module.emrapi.adt;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.TestUtils;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class AdtServiceComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private AdtService service;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    LocationService locationService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("baseTestDataset.xml");
    }

    @Test
    public void integrationTest_ADT_workflow() {
        LocationService locationService = Context.getLocationService();

        Patient patient = Context.getPatientService().getPatient(7);

        // parent location should support visits
        LocationTag supportsVisits = new LocationTag();
        supportsVisits.setName(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS);
        locationService.saveLocationTag(supportsVisits);

        Location parentLocation = locationService.getLocation(2);
        parentLocation.addTag(supportsVisits);
        locationService.saveLocation(parentLocation);

        // add a child location where we'll do the actual check-in
        Location outpatientDepartment = new Location();
        outpatientDepartment.setName("Outpatient Clinic in Xanadu");
        outpatientDepartment.setParentLocation(parentLocation);
        locationService.saveLocation(outpatientDepartment);

        // add another child location for inpatient, that supports admissions
        LocationTag supportsAdmission = new LocationTag();
        supportsAdmission.setName(EmrApiConstants.LOCATION_TAG_SUPPORTS_ADMISSION);
        locationService.saveLocationTag(supportsAdmission);

        Location inpatientWard = new Location();
        inpatientWard.setName("Inpatient Ward in Xanadu");
        inpatientWard.setParentLocation(parentLocation);
        inpatientWard.addTag(supportsAdmission);
        locationService.saveLocation(inpatientWard);

        // step 1: check in the patient (which should create a visit and an encounter)
        Encounter checkInEncounter = service.checkInPatient(patient, outpatientDepartment, null, null, null,
                false);

        assertThat(checkInEncounter.getVisit(), notNullValue());
        assertThat(checkInEncounter.getPatient(), is(patient));
        assertThat(checkInEncounter.getEncounterDatetime(), TestUtils.isJustNow());
        assertThat(checkInEncounter.getVisit().getPatient(), is(patient));
        assertThat(checkInEncounter.getVisit().getStartDatetime(), TestUtils.isJustNow());
        assertThat(checkInEncounter.getAllObs().size(), is(0));

        // step 2: admit the patient (which should create an encounter)
        Date admitDatetime = new Date();
        Admission admission = new Admission();
        admission.setPatient(patient);
        admission.setAdmitDatetime(admitDatetime);
        admission.setLocation(inpatientWard);
        Encounter admitEncounter = service.admitPatient(admission);

        assertThat(admitEncounter.getPatient(), is(patient));
        assertThat(admitEncounter.getVisit(), is(checkInEncounter.getVisit()));
        assertThat(admitEncounter.getEncounterDatetime(), is(admitDatetime));
        assertThat(admitEncounter.getLocation(), is(inpatientWard));
        assertThat(admitEncounter.getAllObs().size(), is(0));
        assertTrue(new VisitDomainWrapper(admitEncounter.getVisit(), emrApiProperties).isAdmitted());

        // TODO once implemented, add Discharge to this test
    }

}
