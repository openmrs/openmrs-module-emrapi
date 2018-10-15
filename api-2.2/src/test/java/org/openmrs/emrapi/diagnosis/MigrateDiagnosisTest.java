package org.openmrs.emrapi.diagnosis;

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
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.module.emrapi.test.builder.ObsBuilder;
import org.openmrs.module.emrapi.visit.EmrVisitService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import openmrs.module.emrapi.diagnosis.MigrateDiagnosis;


@RunWith(SpringJUnit4ClassRunner.class)
public class MigrateDiagnosisTest extends BaseModuleContextSensitiveTest {

	private static final String DIAGNOSIS_DATASET = "DiagnosisDataset.xml";

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
	public void getAllPatientsWithDiagnosis_shouldReturnListOfPatientIdsWithADiagnosis() {
		diagnosisMetadata.setDiagnosisSetConcept(conceptService.getConcept(159965));
		List<Integer> patientIds = emrVisitService.getAllPatientsWithDiagnosis(diagnosisMetadata);
		
		assertEquals(2, patientIds.size());
	}

	@Test
	public void migrate_shouldVoidEmrapiDiagnosisAndCreateAnewCoreDiagnosis() {
		Patient patient = patientService.getPatient(7);
		OldDiagnosisBuilder oldDiagnosisBuilder = new OldDiagnosisBuilder(diagnosisMetadata);
		Obs obs = oldDiagnosisBuilder.buildDiagnosis(patient, "2013-09-10", Diagnosis.Order.SECONDARY, Diagnosis.Certainty.CONFIRMED, "non-coded pain", encounterService.getEncounter(1)).save().get();
		oldDiagnosisBuilder.buildDiagnosis(patient, "2013-08-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, "non-coded disease", encounterService.getEncounter(1)).save();
		assertFalse(obs.getVoided());
		List<Diagnosis> emrapiDiagnoses = emrapiDiagnosisService.getDiagnoses(patient, null);
		assertEquals(2, emrapiDiagnoses.size());
		// before migration
		assertEquals(0, diagnosisService.getDiagnoses(patient, null).size());
		
		new MigrateDiagnosis().migrate(diagnosisMetadata);
		// after migration
		assertEquals(2, diagnosisService.getDiagnoses(patient, null).size());
		assertTrue(obs.getVoided());
		
	}
	
	@Test
	public void migrate_shouldVoidChildObsOfMigratedDiagnosis() {
		Patient patient = patientService.getPatient(7);
		OldDiagnosisBuilder oldDiagnosisBuilder = new OldDiagnosisBuilder(diagnosisMetadata);
		ObsBuilder builder = oldDiagnosisBuilder.buildDiagnosis(patient, "2013-09-10", Diagnosis.Order.SECONDARY, Diagnosis.Certainty.CONFIRMED, "non-coded pain", encounterService.getEncounter(1)).
				addMember(Context.getConceptService().getConcept(3), "Some Value");
		Obs obs = builder.save().get();
		// Before migration
		assertEquals(4, obs.getGroupMembers().size());
		new MigrateDiagnosis().migrate(diagnosisMetadata);
		// After migration
		assertEquals(0, obs.getGroupMembers().size());
		// Include voided
		assertEquals(4, obs.getGroupMembers(true).size());
			
	}
}
