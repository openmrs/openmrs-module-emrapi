package org.openmrs.emrapi.diagnosis;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.diagnosis.EmrDiagnosisDAO;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class EmrDiagnosisDAOTest extends BaseModuleContextSensitiveTest {

    private static final String DIAGNOSIS_DATASET = "DiagnosisDataset.xml";

    @Autowired
    private EmrDiagnosisDAO emrDiagnosisDAO;

    private Visit visit = mock(Visit.class);

    @Before
    public void setUp() throws Exception {
        executeDataSet(DIAGNOSIS_DATASET);
        when(visit.getId()).thenReturn(1010);
    }

    @Test
    public void shouldReturnAllNonVoidedDiagnosesFromVisit() {
        List<org.openmrs.Diagnosis> diagnoses = emrDiagnosisDAO.getDiagnoses(visit, false, false);
        assertEquals(4, diagnoses.size());
        assertEquals(Boolean.FALSE, diagnoses.get(0).getVoided());
        assertEquals(Boolean.FALSE, diagnoses.get(1).getVoided());
        assertEquals(Boolean.FALSE, diagnoses.get(2).getVoided());
        assertEquals(Boolean.FALSE, diagnoses.get(3).getVoided());
    }

    @Test
    public void shouldReturnAllPrimaryConfirmedDiagnosesFromVisit() {
        List<org.openmrs.Diagnosis> diagnoses = emrDiagnosisDAO.getDiagnoses(visit, true, true);
        assertEquals(1, diagnoses.size());
        assertEquals(ConditionVerificationStatus.CONFIRMED, diagnoses.get(0).getCertainty());
    }

    @Test
    public void shouldReturnAllPrimaryDiagnosesFromVisit() {
        List<org.openmrs.Diagnosis> diagnoses = emrDiagnosisDAO.getDiagnoses(visit, true, false);
        assertEquals(2, diagnoses.size());
        assertEquals(new Integer(1), diagnoses.get(0).getRank());
        assertEquals(new Integer(1), diagnoses.get(1).getRank());
    }

    @Test
    public void shouldReturnAllConfirmedDiagnosesFromVisit() {
        List<org.openmrs.Diagnosis> diagnoses = emrDiagnosisDAO.getDiagnoses(visit, false, true);
        assertEquals(2, diagnoses.size());
        assertEquals(ConditionVerificationStatus.CONFIRMED, diagnoses.get(0).getCertainty());
        assertEquals(ConditionVerificationStatus.CONFIRMED, diagnoses.get(1).getCertainty());
    }
}
