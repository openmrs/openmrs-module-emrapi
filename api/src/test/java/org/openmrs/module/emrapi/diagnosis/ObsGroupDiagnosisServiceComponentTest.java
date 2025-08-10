package org.openmrs.module.emrapi.diagnosis;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.module.emrapi.test.builder.ObsBuilder;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;


public class ObsGroupDiagnosisServiceComponentTest extends BaseModuleContextSensitiveTest {

	@Autowired
	ConceptService conceptService;

	@Autowired
	EncounterService encounterService;

	@Autowired
	EmrApiProperties emrApiProperties;

	@Autowired
	PatientService patientService;

	@Autowired
	ObsGroupDiagnosisService diagnosisService;

	@Autowired
	TestDataManager testDataManager;

	DiagnosisMetadata dmd;


	@BeforeEach
	public void setUp() throws Exception {
		dmd = ContextSensitiveMetadataTestUtils.setupDiagnosisMetadata(conceptService, emrApiProperties);

	}
	
	private Date parseYmd(String ymd) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(ymd);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private ObsBuilder buildDiagnosis(Patient patient, String dateYmd, Diagnosis.Order order, Diagnosis.Certainty certainty, Object diagnosis) {
		ObsBuilder builder = new ObsBuilder()
				.setPerson(patient)
				.setObsDatetime(parseYmd(dateYmd))
				.setConcept(dmd.getDiagnosisSetConcept())
				.addMember(dmd.getDiagnosisOrderConcept(), dmd.getConceptFor(order))
				.addMember(dmd.getDiagnosisCertaintyConcept(), dmd.getConceptFor(certainty));
		if (diagnosis instanceof Concept) {
			builder.addMember(dmd.getCodedDiagnosisConcept(), (Concept) diagnosis);
		} else if (diagnosis instanceof String) {
			builder.addMember(dmd.getNonCodedDiagnosisConcept(), (String) diagnosis);
		} else {
			throw new IllegalArgumentException("Diagnosis value must be a Concept or String");
		}
		return builder;
	}

	private Obs createDiagnosisObs() {
		Patient patient = patientService.getPatient(2);
		ObsBuilder obsBuilder = buildDiagnosis(patient, "2013-01-02", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, "non-coded pain").save();
		return obsBuilder.get();
	}

	private Obs getObs(Obs groupObs, Concept concept) {
		Set<Obs> groupMembers = groupObs.getGroupMembers();
		Obs obs = null;
		if ((groupMembers != null) && (groupMembers.size() > 0)) {
			for (Obs groupMember : groupMembers) {
				Concept obsConcept = groupMember.getConcept();
				if (obsConcept == concept) {
					obs = groupMember;
					break;
				}
			}
		}
		return obs;
	}

	@Test
	public void codeNonCodedDiagnosis() {
		//create an ObsGroup with a non-coded diagnosis
		Obs consultationObs = createDiagnosisObs();
		//retrieve the Obs that contains the non-coded diagnosis
		Obs nonCodedObs = getObs(consultationObs, dmd.getNonCodedDiagnosisConcept());
		assertThat(nonCodedObs, notNullValue());
		//code the non-coded diagnosis to coded diagnosis(Malaria)
		Concept malaria = conceptService.getConcept(11);
        /*
		Obs codedObs = diagnosisService.codeNonCodedDiagnosis(nonCodedObs, malaria);
		//verify the obs is a coded diagnosis now
		assertThat(codedObs.getConcept(), is(dmd.getCodedDiagnosisConcept()));
		assertThat(codedObs.getValueCoded(), is(malaria));

		//verify the old that contained the non-coded diagnosis was voided
		nonCodedObs = obsService.getObs(nonCodedObs.getId());
		assertThat(nonCodedObs.getVoided(), is(true));
		*/
	}

	@Test
	public void getDiagnosesShouldReturnEmptyListIfNone() {
		Patient patient = patientService.getPatient(2);
		assertThat(diagnosisService.getDiagnoses(patient, new Date()), is(empty()));
	}

	@Test
	public void getDiagnosesShouldReturnDiagnosesAfterDate() {
		Patient patient = patientService.getPatient(2);
		Obs obs = buildDiagnosis(patient, "2013-09-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, "non-coded pain").save().get();
		buildDiagnosis(patient, "2013-08-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, "non-coded disease").save();

		List<Diagnosis> diagnoses = diagnosisService.getDiagnoses(patient, parseYmd("2013-09-01"));
		assertThat(diagnoses, contains(hasObs(obs)));
	}

    @Test
    public void getDiagnosesShouldReturnDiagnosesInReverseChronologicalOrder() {
        Patient patient = patientService.getPatient(2);

        // don't create them in the "right" order
        Obs expectedSecondObs =  buildDiagnosis(patient, "2013-08-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, "non-coded allergy").save().get();
        Obs expectedThirdObs =  buildDiagnosis(patient, "2013-07-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, "non-coded disease").save().get();
        Obs expectedFirstObs = buildDiagnosis(patient, "2013-09-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, "non-coded pain").save().get();

        List<Diagnosis> diagnoses = diagnosisService.getDiagnoses(patient, parseYmd("2001-09-01"));
		assertThat(diagnoses.size(), is(3));
        assertThat(diagnoses.get(0).getExistingObs(), is(expectedFirstObs));
        assertThat(diagnoses.get(1).getExistingObs(), is(expectedSecondObs));
        assertThat(diagnoses.get(2).getExistingObs(), is(expectedThirdObs));
    }

	@Test
	public void getUniqueDiagnosesShouldReturnNoTextDuplicates() {
		Patient patient = patientService.getPatient(2);
        Obs olderObs = buildDiagnosis(patient, "2013-08-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, "non-coded pain").save().get();
		Obs mostRecentObs = buildDiagnosis(patient, "2013-09-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, "non-coded pain").save().get();

		List<Diagnosis> diagnoses = diagnosisService.getUniqueDiagnoses(patient, parseYmd("2013-01-01"));
        assertThat(diagnoses.size(), is(1));
		assertThat(diagnoses.get(0).getExistingObs(), is(mostRecentObs));
	}

	@Test
	public void getUniqueDiagnosesShouldReturnNoCodedDuplicates() {
		Patient patient = patientService.getPatient(2);
		Concept malaria = conceptService.getConcept(11);
		Obs olderObs = buildDiagnosis(patient, "2013-08-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, malaria).save().get();
        Obs mostRecentObs = buildDiagnosis(patient, "2013-09-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, malaria).save().get();

		List<Diagnosis> diagnoses = diagnosisService.getUniqueDiagnoses(patient, parseYmd("2013-01-01"));
        assertThat(diagnoses.size(), is(1));
        assertThat(diagnoses.get(0).getExistingObs(), is(mostRecentObs));
	}

	@Test
	public void getDiagnoses_shouldReturnDiagnosesMappedToCoreDiagnosesByVisit() {
		Patient patient = patientService.getPatient(2);
		Concept malaria = conceptService.getConcept(11);
		String date1 = "2013-08-10";
		Visit visit = testDataManager.visit().patient(patient).started(date1).visitType(1).save();
		Encounter encounter = testDataManager.encounter().visit(visit).patient(patient).encounterDatetime(date1).encounterType(1).save();
		encounter.addObs(buildDiagnosis(patient, date1, Diagnosis.Order.SECONDARY, Diagnosis.Certainty.PRESUMED, malaria).save().get());
		encounter.addObs(buildDiagnosis(patient, date1, Diagnosis.Order.PRIMARY, Diagnosis.Certainty.CONFIRMED, "Headache").save().get());
		encounterService.saveEncounter(encounter);
		Map<Visit, List<org.openmrs.Diagnosis>> visits = diagnosisService.getDiagnoses(Collections.singletonList(visit));
		assertThat(visits.size(), is(1));
		assertThat(visits.keySet().iterator().next(), is(visit));
		List<org.openmrs.Diagnosis> diagnoses = visits.get(visit);
		assertThat(diagnoses.size(), is(2));
		assertThat(diagnoses.get(0).getPatient(), is(patient));
		assertThat(diagnoses.get(0).getEncounter(), is(encounter));
		assertThat(diagnoses.get(0).getDiagnosis().getNonCoded(), is("Headache"));
		assertThat(diagnoses.get(0).getCertainty(), is(ConditionVerificationStatus.CONFIRMED));
		assertThat(diagnoses.get(0).getRank(), is(1));
		assertThat(diagnoses.get(1).getPatient(), is(patient));
		assertThat(diagnoses.get(1).getEncounter(), is(encounter));
		assertThat(diagnoses.get(1).getDiagnosis().getCoded(), is(malaria));
		assertThat(diagnoses.get(1).getCertainty(), is(ConditionVerificationStatus.PROVISIONAL));
		assertThat(diagnoses.get(1).getRank(), is(2));
	}

	public static Matcher<Diagnosis> hasObs(final Obs obs) {
		return new FeatureMatcher<Diagnosis, Obs>(is(obs), "obs", "obs") {
			@Override
			protected Obs featureValueOf(Diagnosis actual) {
				return actual.getExistingObs();
			}
		};
	}
}
