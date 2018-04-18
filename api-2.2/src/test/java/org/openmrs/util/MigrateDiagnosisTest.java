package org.openmrs.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.module.emrapi.visit.EmrVisitService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
public class MigrateDiagnosisTest extends BaseModuleContextSensitiveTest {

	private static  final String DIAGNOSIS_DATASET = "DiagnosisDataset.xml";

	@Autowired
	ConceptService conceptService;

	@Autowired
	EncounterService encounterService;

	@Autowired
	EmrVisitService emrVisitService;

	@Autowired
	DiagnosisService emrapiDiagnosisService;

	@Autowired
	org.openmrs.api.DiagnosisService diagnosisService;

	@Autowired
	PatientService patientService;

	@Autowired
	EmrApiProperties emrApiProperties;

	private DiagnosisMetadata diagnosisMetadata;

	@Before
	public void setUp() throws Exception {
		executeDataSet(DIAGNOSIS_DATASET);
		diagnosisMetadata = ContextSensitiveMetadataTestUtils.setupDiagnosisMetadata(conceptService, emrApiProperties);
	}

	@Test
	public void getAllPatientsWithDiagnosisShouldReturnListOfPatientIdsWithADiagnosis() {
		diagnosisMetadata.setDiagnosisSetConcept(conceptService.getConcept(19));
		List<Integer> patientIds = emrVisitService.getAllPatientsWithDiagnosis(diagnosisMetadata);
		Patient patient1 = patientService.getPatient(patientIds.get(0));
		assertEquals(patient1.getId(), new Integer(7));
		assertEquals(patient1.getVoided(), false);
	}

	@Test
	public void migrateShouldVoidEmrapiDiagnosisAndCreateAnewCoreDiagnosis() {

		Patient patient = patientService.getPatient(7);

		OldDiagnosisBuilder oldDiagnosisBuilder = new OldDiagnosisBuilder(diagnosisMetadata);

		Obs obs = oldDiagnosisBuilder.buildDiagnosis(patient, "2013-09-10", Diagnosis.Order.SECONDARY, Diagnosis.Certainty.CONFIRMED, "non-coded pain", encounterService.getEncounter(1)).save().get();
		oldDiagnosisBuilder.buildDiagnosis(patient, "2013-08-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, "non-coded disease", encounterService.getEncounter(1)).save().get();

		assertFalse(obs.getVoided());

		List<Diagnosis> emrapiDiagnoses = emrapiDiagnosisService.getDiagnoses(patient, null);
		assertEquals(2, emrapiDiagnoses.size());

		MigrateDiagnosis migrateDiagnosis = new MigrateDiagnosis();

		migrateDiagnosis.migrate(diagnosisMetadata);

		assertTrue(obs.getVoided());

		List<org.openmrs.Diagnosis> coreDiagnoses = diagnosisService.getDiagnoses(patient, null);

		assertEquals(2, coreDiagnoses.size());
	}
}
