package org.openmrs.module.emrapi.patient;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.EmrApiContextSensitiveTest;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.visit.VisitWithDiagnosesAndNotes;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class EmrPatientServiceTest extends EmrApiContextSensitiveTest {

    @Autowired
    AdministrationService adminService;

    @Autowired
    PatientService patientService;

    @Autowired
    EmrPatientService emrPatientService;

    @Before
    public void setup() {
        executeDataSet("baseTestDataset.xml");
        executeDataSet("pastVisitSetup.xml");
        //adminService.setGlobalProperty(EmrApiConstants.GP_USE_LEGACY_DIAGNOSIS_SERVICE, "true");
    }

    @Test
    public void shouldFetchVisitsWithNotesAndDiagnosesByPatientId() {
        String visitNoteEncounterTypeUuid = "d7151f82-c1f3-4152-a605-2f9ea7414a79";

        Patient patient = patientService.getPatient(109);

        List<VisitWithDiagnosesAndNotes> visits = emrPatientService.getVisitsWithDiagnosesAndNotesByPatient(patient,0,10);
        assertNotNull(visits);
        assert visits.size() == 3;

        VisitWithDiagnosesAndNotes firstVisit = visits.get(2);
        List<Obs> firstVisitNotes = firstVisit.getVisitNotes();
        List<Diagnosis> firstVisitDiagnoses = firstVisit.getDiagnoses();

        assert firstVisit.getVisit().getId() == 1014;
        assert firstVisit.getVisit().getPatient().getPatientId() == 109;
        assert firstVisitNotes.size() == 2;
        assert firstVisitDiagnoses.size() == 3;

        for (Obs obs : firstVisitNotes) {
            assert obs.getEncounter().getEncounterType().getUuid().equals(visitNoteEncounterTypeUuid);
        }

        VisitWithDiagnosesAndNotes secondVisit = visits.get(1);
       List<Obs> secondVisitNotes = secondVisit.getVisitNotes();
       List<Diagnosis> secondVisitDiagnoses = secondVisit.getDiagnoses();

        assert secondVisit.getVisit().getId() == 1015;
        assert secondVisit.getVisit().getPatient().getPatientId() == 109;
        assert secondVisitNotes.size() == 1;
        assert secondVisitDiagnoses.size() == 2;

        for (Obs obs : secondVisitNotes) {
            assert obs.getEncounter().getEncounterType().getUuid().equals(visitNoteEncounterTypeUuid);
        }

        VisitWithDiagnosesAndNotes thirdVisit = visits.get(0);
       List<Obs> thirdVisitNotes = thirdVisit.getVisitNotes();
       List<Diagnosis> thirdVisitDiagnoses = thirdVisit.getDiagnoses();

        assert thirdVisit.getVisit().getId() == 1017;
        assert thirdVisit.getVisit().getPatient().getPatientId() == 109;
        assert thirdVisitNotes.isEmpty();
        assert thirdVisitDiagnoses.isEmpty();

    }

    @Test
    public void shouldFetchVisitsWithNotesAndDiagnosesWithPagination() {
        Patient patient = new Patient();
        patient.setPatientId(109);

        List<VisitWithDiagnosesAndNotes> visits = emrPatientService.getVisitsWithDiagnosesAndNotesByPatient(patient,0,1);
        assertNotNull(visits);
        assert visits.size() == 1;

        VisitWithDiagnosesAndNotes mostRecentVisit = visits.get(0);
        assert mostRecentVisit.getVisit().getId() == 1017;
    }

    /**
     * Patient 111 has two visits, one voided and one not voided (1019). The voided visit has a voided notes encounter.
     */
    @Test
    public void shouldNotFetchVoidedVisits() {
        Patient patient = new Patient();
        patient.setPatientId(111);

        List<VisitWithDiagnosesAndNotes> visits = emrPatientService.getVisitsWithDiagnosesAndNotesByPatient(patient,0,10);
        assertNotNull(visits);
        assert visits.size() == 1;

        VisitWithDiagnosesAndNotes visit = visits.get(0);
        assert visit.getVisit().getId() == 1019;
    }

    /**
     * Patient 111 has two visits, one voided and one not voided (1019). The voided visit has a voided notes encounter.
     */
    @Test
    public void shouldNotFetchVoidedEncounters() {
        Patient patient = new Patient();
        patient.setPatientId(111);

        List<VisitWithDiagnosesAndNotes> visits = emrPatientService.getVisitsWithDiagnosesAndNotesByPatient(patient,0,10);
        assertNotNull(visits);
        assert visits.size() == 1;

        VisitWithDiagnosesAndNotes visit = visits.get(0);
        assert visit.getVisitNotes().isEmpty();
    }

}
