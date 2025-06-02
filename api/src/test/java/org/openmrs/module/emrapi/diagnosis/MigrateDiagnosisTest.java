package org.openmrs.module.emrapi.diagnosis;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiContextSensitiveTest;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.module.emrapi.test.builder.ObsBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MigrateDiagnosisTest extends EmrApiContextSensitiveTest {

	private static final String DIAGNOSIS_DATASET = "DiagnosisDataset.xml";

	@Autowired
	ConceptService conceptService;

	@Autowired
	EncounterService encounterService;

	@Autowired
	ObsGroupDiagnosisService obsGroupDiagnosisService;

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
		List<Integer> patientIds = obsGroupDiagnosisService.getAllPatientsWithDiagnosis(diagnosisMetadata);
		
		assertEquals(2, patientIds.size());
	}

	@Test
	public void migrate_shouldVoidEmrApiDiagnosisAndCreateAnewCoreDiagnosis() {
		Patient patient = patientService.getPatient(7);
		Encounter encounter = encounterService.getEncounter(1);
		Obs obs1 = buildEmrApiDiagnosis(diagnosisMetadata,patient,encounter, Diagnosis.Order.PRIMARY,Diagnosis.Certainty.PRESUMED, "non-coded disease").save().get();
		Obs obs2 = buildEmrApiDiagnosis(diagnosisMetadata, patient, encounter, Diagnosis.Order.SECONDARY, Diagnosis.Certainty.CONFIRMED, "non-coded pain").save().get();
		
		assertNotNull(obs1);
		assertFalse(obs1.getVoided());
		assertNotNull(obs2);
		assertFalse(obs2.getVoided());
		
		List<Diagnosis> emrApiDiagnoses = MigrateDiagnosis.getDeprecatedDiagnosisService().getDiagnoses(patient, null);
		assertEquals(2, emrApiDiagnoses.size());
		
		// before migration
		// No
		assertEquals(0, MigrateDiagnosis.getNewDiagnosisService().getDiagnoses(patient, null).size());
		
		new MigrateDiagnosis().migrate(diagnosisMetadata, emrApiProperties);
		
		// after migration
		List<Diagnosis> emrApiDiagnosesAfterMigration = MigrateDiagnosis.getDeprecatedDiagnosisService().getDiagnoses(patient, null);
		assertEquals(0, emrApiDiagnosesAfterMigration.size());
		
		List<org.openmrs.Diagnosis> migratedDiagnoses = MigrateDiagnosis.getNewDiagnosisService().getDiagnoses(patient, null);
		assertEquals(2, migratedDiagnoses.size());
		migratedDiagnoses.forEach(md -> assertEquals(patient.getUuid(), md.getPatient().getUuid()));
	}
	
	@Test
	public void migrate_shouldVoidChildObsOfMigratedDiagnosis() {
		Patient patient = patientService.getPatient(7);
		Encounter encounter = encounterService.getEncounter(1);
		
		Obs obs = buildEmrApiDiagnosis(diagnosisMetadata, patient, encounter, Diagnosis.Order.SECONDARY,
				Diagnosis.Certainty.CONFIRMED, "non-coded pain")
				.addMember(Context.getConceptService().getConcept(3), "Some Value").save().get();
		
		// Before migration
		assertEquals(4, obs.getGroupMembers().size());
		
		new MigrateDiagnosis().migrate(diagnosisMetadata, emrApiProperties);
		
		// After migration
		assertEquals(4, obs.getGroupMembers().size());
		// Include voided
		assertEquals(4, obs.getGroupMembers(true).size());
	}
	
	@Test
	public void migrate_shouldMigrateEmrApiDiagnosesToCoreDiagnoses() {
		Patient patient = patientService.getPatient(7);
		Encounter encounter = encounterService.getEncounter(1);
		// Create an emrapi diagnosis
		buildEmrApiDiagnosis(diagnosisMetadata, patient, encounter, Diagnosis.Order.SECONDARY, Diagnosis.Certainty.CONFIRMED, "non-coded pain").save();
		buildEmrApiDiagnosis(diagnosisMetadata, patient, encounter, Diagnosis.Order.SECONDARY, Diagnosis.Certainty.PRESUMED, "Test diagnosis").save();
		
		// Before migration
		// No diagnoses should exist in the new diagnosis table
		assertEquals(0, diagnosisService.getDiagnoses(patient, null).size());
		// Patients with EmrApi diagnosis should be returned
		List<Integer> patientWitDiagnosisIds = obsGroupDiagnosisService.getPatientsWithDiagnosis(diagnosisMetadata, 0, 10);
		assertEquals(1, patientWitDiagnosisIds.size());
		assertTrue(patientWitDiagnosisIds.contains(patient.getId()));
		
		// Migrate the diagnoses
		new MigrateDiagnosis().migrate(diagnosisMetadata, emrApiProperties);
		
		// After migration
		assertEquals(1, diagnosisService.getDiagnoses(patient, null).size());
	}
	
	
	private ObsBuilder buildEmrApiDiagnosis(DiagnosisMetadata diagnosisMetadata, Patient patient, Encounter encounter, Diagnosis.Order order, Diagnosis.Certainty certainty, Object diagnosis) {
		ObsBuilder builder = new ObsBuilder()
				.setPerson(patient)
				.setObsDatetime(new Date())
				.setConcept(diagnosisMetadata.getDiagnosisSetConcept())
				.addMember(diagnosisMetadata.getDiagnosisOrderConcept(), diagnosisMetadata.getConceptFor(order))
				.addMember(diagnosisMetadata.getDiagnosisCertaintyConcept(), diagnosisMetadata.getConceptFor(certainty));
		if (diagnosis instanceof Concept) {
			builder.addMember(diagnosisMetadata.getCodedDiagnosisConcept(), (Concept) diagnosis);
		} else if (diagnosis instanceof String) {
			builder.addMember(diagnosisMetadata.getNonCodedDiagnosisConcept(), (String) diagnosis);
		} else {
			throw new IllegalArgumentException("Diagnosis value must be a Concept or String");
		}
		return builder.setEncounter(encounter);
	}
}
