package org.openmrs.module.emrapi.encounter;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(PowerMockRunner.class)
public class DiagnosisMapperTest {

    @Test
    public void shouldMapEmrapiDiagnosisToEncounterTransactionDiagnosis() throws Exception {
        DiagnosisMapper diagnosisMapper = new DiagnosisMapper();
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setCertainty(Diagnosis.Certainty.CONFIRMED);
        diagnosis.setOrder(Diagnosis.Order.PRIMARY);
        CodedOrFreeTextAnswer freeTextAnswer = new CodedOrFreeTextAnswer();
        freeTextAnswer.setNonCodedAnswer("cold");
        diagnosis.setDiagnosis(freeTextAnswer);
        Obs existingObs = new Obs();
        existingObs.setEncounter(new Encounter());
        existingObs.setComment("comment");
        diagnosis.setExistingObs(existingObs);

        EncounterTransaction.Diagnosis etDiagnosis = diagnosisMapper.convert(diagnosis);

        assertEquals(Diagnosis.Certainty.CONFIRMED.toString(), etDiagnosis.getCertainty());
        assertEquals(Diagnosis.Order.PRIMARY.toString(), etDiagnosis.getOrder());
        assertEquals("cold", etDiagnosis.getFreeTextAnswer());
        assertNull(etDiagnosis.getCodedAnswer());
        assertEquals("comment", etDiagnosis.getComments());
    }
}