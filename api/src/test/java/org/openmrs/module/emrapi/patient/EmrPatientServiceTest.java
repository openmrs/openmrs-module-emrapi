package org.openmrs.module.emrapi.patient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Diagnosis;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class EmrPatientServiceTest extends BaseModuleContextSensitiveTest {

    @Autowired
    PatientService patientService;

    @Autowired
    DiagnosisService diagnosisService;

    @Autowired
    EmrPatientService emrPatientService;

    @BeforeEach
    public void setup() {
        executeDataSet("baseTestDataset.xml");
        executeDataSet("pastVisitSetup.xml");
    }

    @Test
    public void shouldFetchVisitsWithNotesAndDiagnosesByPatientId() {
        String visitNoteEncounterTypeUuid = "d7151f82-c1f3-4152-a605-2f9ea7414a79";

        Patient patient = patientService.getPatient(109);

        List<Visit> visits = emrPatientService.getVisitsForPatient(patient,0,10);
        Map<Visit, List<Obs>> notesByVisit = emrPatientService.getVisitNoteObservations(visits);
        Map<Visit, List<Diagnosis>> diagnosesByVisit = diagnosisService.getDiagnoses(visits);

        assertNotNull(visits);
        assert visits.size() == 3;

        Visit firstVisit = visits.get(2);
        List<Obs> firstVisitNotes = notesByVisit.get(firstVisit);
        List<Diagnosis> firstVisitDiagnoses = diagnosesByVisit.get(firstVisit);

        assert firstVisit.getId() == 1014;
        assert firstVisit.getPatient().getPatientId() == 109;
        assert firstVisitNotes.size() == 2;
        assert firstVisitDiagnoses.size() == 3;

        for (Obs obs : firstVisitNotes) {
            assert obs.getEncounter().getEncounterType().getUuid().equals(visitNoteEncounterTypeUuid);
        }

        Visit secondVisit = visits.get(1);
        List<Obs> secondVisitNotes = notesByVisit.get(secondVisit);
        List<Diagnosis> secondVisitDiagnoses = diagnosesByVisit.get(secondVisit);

        assert secondVisit.getId() == 1015;
        assert secondVisit.getPatient().getPatientId() == 109;
        assert secondVisitNotes.size() == 1;
        assert secondVisitDiagnoses.size() == 2;

        for (Obs obs : secondVisitNotes) {
            assert obs.getEncounter().getEncounterType().getUuid().equals(visitNoteEncounterTypeUuid);
        }

        Visit thirdVisit = visits.get(0);
        List<Obs> thirdVisitNotes = notesByVisit.get(thirdVisit);
        List<Diagnosis> thirdVisitDiagnoses = diagnosesByVisit.get(thirdVisit);

        assert thirdVisit.getId() == 1017;
        assert thirdVisit.getPatient().getPatientId() == 109;
        assert thirdVisitNotes.isEmpty();
        assert thirdVisitDiagnoses.isEmpty();
    }

    @Test
    public void shouldFetchVisitsWithNotesAndDiagnosesWithPagination() {
        Patient patient = new Patient();
        patient.setPatientId(109);

        List<Visit> visits = emrPatientService.getVisitsForPatient(patient,0,1);
        assertNotNull(visits);
        assert visits.size() == 1;

        Visit mostRecentVisit = visits.get(0);
        assert mostRecentVisit.getId() == 1017;
    }

    /**
     * Patient 111 has two visits, one voided and one not voided (1019). The voided visit has a voided notes encounter.
     */
    @Test
    public void shouldNotFetchVoidedVisits() {
        Patient patient = new Patient();
        patient.setPatientId(111);

        List<Visit> visits = emrPatientService.getVisitsForPatient(patient,0,10);
        assertNotNull(visits);
        assert visits.size() == 1;

        Visit visit = visits.get(0);
        assert visit.getId() == 1019;
    }

    /**
     * Patient 111 has two visits, one voided and one not voided (1019). The voided visit has a voided notes encounter.
     */
    @Test
    public void shouldNotFetchVoidedEncounters() {
        Patient patient = new Patient();
        patient.setPatientId(111);

        List<Visit> visits = emrPatientService.getVisitsForPatient(patient,0,10);
        assertNotNull(visits);
        assert visits.size() == 1;

        Map<Visit, List<Obs>> notesByVisit = emrPatientService.getVisitNoteObservations(visits);
        List<Obs> visitNotes = notesByVisit.get(visits.get(0));
        assert visitNotes.isEmpty();
    }

}
