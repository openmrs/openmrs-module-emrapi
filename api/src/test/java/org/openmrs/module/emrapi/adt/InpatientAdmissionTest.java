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
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.contrib.testdata.TestDataManager;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class InpatientAdmissionTest {

    TestDataManager tdm;
    InpatientAdmission admission;
    Location location1;
    Location location2;
    Encounter encounter1;
    Encounter encounter2;
    Encounter encounter3;
    Encounter encounter4;
    Encounter encounter5;
    Encounter encounter6;

    @Before
    public void setup() throws Exception {
        tdm = new TestDataManager();
        admission = new InpatientAdmission();
        location1 = new Location();
        location2 = new Location();
        encounter1 = createEncounter("2020-08-15 13:00:00", "2020-08-15 13:00:00", 10);
        encounter2 = createEncounter("2020-08-15 14:00:00", "2020-08-15 14:00:00", 20);
        encounter3 = createEncounter("2020-08-15 15:00:00", "2020-08-15 15:00:00", 30);
        encounter4 = createEncounter("2020-08-15 16:00:00", "2020-08-15 16:00:00", 40);
        encounter5 = createEncounter("2020-08-15 16:00:00", "2020-08-15 15:00:00", 50);
        encounter6 = createEncounter("2020-08-15 16:00:00", "2020-08-15 15:00:00", 35);
        admission.getAdmissionEncounters().add(encounter1);
        admission.getAdmissionEncounters().add(encounter2);
        admission.getTransferEncounters().add(encounter3);
        admission.getTransferEncounters().add(encounter4);
        admission.getTransferEncounters().add(encounter5);
        admission.getDischargeEncounters().add(encounter6);
    }

    @Test
    public void shouldGetAdtEncountersInOrder() {
        List<Encounter> encounters = admission.getAdtEncounters();
        assertThat(encounters.size(), equalTo(6));
        assertThat(encounters, contains(encounter1, encounter2, encounter3, encounter6, encounter5, encounter4));
    }

    @Test
    public void shouldGetAdmissionAndTransferEncountersInOrder() {
        List<Encounter> encounters = admission.getAdmissionAndTransferEncounters();
        assertThat(encounters.size(), equalTo(5));
        assertThat(encounters, contains(encounter1, encounter2, encounter3, encounter5, encounter4));
    }

    @Test
    public void shouldGetFirstAdmissionOrTransferEncounter() {
        assertThat(admission.getFirstAdmissionOrTransferEncounter(), equalTo(encounter1));
    }

    @Test
    public void shouldGetLatestAdmissionOrTransferEncounter() {
        assertThat(admission.getLatestAdmissionOrTransferEncounter(), equalTo(encounter4));
    }

    @Test
    public void shouldGetLatestAdtEncounter() {
        assertThat(admission.getLatestAdtEncounter(), equalTo(encounter4));
    }

    @Test
    public void shouldGetCurrentInpatientLocation() {
        assertThat(admission.getCurrentInpatientLocation(), nullValue());
        encounter6.setLocation(location1);
        assertThat(admission.getCurrentInpatientLocation(), nullValue());
        encounter4.setLocation(location1);
        assertThat(admission.getCurrentInpatientLocation(), equalTo(location1));
    }

    @Test
    public void shouldGetEncounterAssigningToCurrentInpatientLocation() {
        assertThat(admission.getEncounterAssigningToCurrentInpatientLocation(), nullValue());
        encounter1.setLocation(location1);
        encounter2.setLocation(location2);
        encounter3.setLocation(location1);
        encounter6.setLocation(location1); // This is a discharge encounter, so not applicable
        encounter5.setLocation(location1);
        encounter4.setLocation(location1);
        assertThat(admission.getEncounterAssigningToCurrentInpatientLocation(), equalTo(encounter3));
        encounter3.setLocation(location2);
        assertThat(admission.getEncounterAssigningToCurrentInpatientLocation(), equalTo(encounter5));
        encounter5.setLocation(location2);
        assertThat(admission.getEncounterAssigningToCurrentInpatientLocation(), equalTo(encounter4));
    }

    @Test
    public void shouldGetIsDischarged() {
        assertThat(admission.isDischarged(), equalTo(false));
        admission.getTransferEncounters().remove(encounter4);
        admission.getTransferEncounters().remove(encounter5);
        assertThat(admission.isDischarged(), equalTo(true));
    }

    private Encounter createEncounter(String encounterDate, String dateCreated, Integer encounterId) {
        Encounter encounter = tdm.encounter().dateCreated(dateCreated).encounterDatetime(encounterDate).get();
        encounter.setEncounterId(encounterId);
        return encounter;
    }
}
