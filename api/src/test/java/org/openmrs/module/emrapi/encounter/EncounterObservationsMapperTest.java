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
package org.openmrs.module.emrapi.encounter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Obs;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Arrays;
import java.util.HashSet;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterObservationsMapperTest {
    private EncounterObservationsMapper encounterObservationsMapper;
    @Mock
    private DiagnosisMetadata diagnosisMetadata;
    @Mock
    private ObservationMapper observationMapper;
    @Mock
    private DiagnosisMapper diagnosisMapper;
    @Mock
    private DispositionMapper dispositionMapper;
    @Mock
    private EmrApiProperties emrApiProperties;

    @Before
    public void setUp(){
        initMocks(this);
        encounterObservationsMapper = new EncounterObservationsMapper(observationMapper, diagnosisMapper, dispositionMapper, emrApiProperties);
        when(emrApiProperties.getDiagnosisMetadata()).thenReturn(diagnosisMetadata);
    }

    @Test
    public void testUpdateMapsDiagnosis() throws Exception {
        EncounterTransaction encounterTransaction = new EncounterTransaction();
        Obs obs1 = new Obs();
        Obs obs2 = new Obs();
        Obs obs3 = new Obs();
        Obs obs4 = new Obs();
        HashSet<Obs> allObs = new HashSet<Obs>(Arrays.asList(obs1, obs2, obs3, obs4));
        when(diagnosisMetadata.isDiagnosis(obs1)).thenReturn(true);
        when(diagnosisMetadata.isDiagnosis(obs2)).thenReturn(false);
        when(diagnosisMetadata.isDiagnosis(obs3)).thenReturn(true);

        EncounterTransaction.Disposition disposition = new EncounterTransaction.Disposition();
        when(dispositionMapper.isDispositionGroup(obs4)).thenReturn(true);
        when(dispositionMapper.getDisposition(obs4)).thenReturn(disposition);

        encounterObservationsMapper.update(encounterTransaction, allObs);

        Assert.assertEquals(2, encounterTransaction.getDiagnoses().size());
        Assert.assertEquals(disposition, encounterTransaction.getDisposition());
        Assert.assertEquals(1, encounterTransaction.getObservations().size());
    }
}
