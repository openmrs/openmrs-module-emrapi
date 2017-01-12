package org.openmrs.module.emrapi.diagnosis;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.module.emrapi.test.builder.ObsBuilder;
import org.openmrs.module.metadatamapping.api.MetadataMappingService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
public class DiagnosisServiceComponentTest extends BaseModuleContextSensitiveTest {

	@Autowired
	ConceptService conceptService;

	@Autowired
	ObsService obsService;

	@Autowired
	EmrApiProperties emrApiProperties;

	@Autowired
	PatientService patientService;

	@Autowired
	DiagnosisService diagnosisService;

	DiagnosisMetadata dmd;


	@Before
	public void setUp() throws Exception {
		dmd = ContextSensitiveMetadataTestUtils.setupDiagnosisMetadata(conceptService, emrApiProperties);

	}

	private ObsBuilder buildDiagnosis(Patient patient, String dateYmd, Diagnosis.Order order, Diagnosis.Certainty certainty, Object diagnosis) {
		ObsBuilder builder = new ObsBuilder()
				.setPerson(patient)
				.setObsDatetime(DateUtil.parseDate(dateYmd, "yyyy-MM-dd"))
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

		List<Diagnosis> diagnoses = diagnosisService.getDiagnoses(patient, DateUtil.parseDate("2013-09-01", "yyyy-MM-dd"));
		assertThat(diagnoses, contains(hasObs(obs)));
	}

    @Test
    public void getDiagnosesShouldReturnDiagnosesInReverseChronologicalOrder() {
        Patient patient = patientService.getPatient(2);

        // don't create them in the "right" order
        Obs expectedSecondObs =  buildDiagnosis(patient, "2013-08-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, "non-coded allergy").save().get();
        Obs expectedThirdObs =  buildDiagnosis(patient, "2013-07-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, "non-coded disease").save().get();
        Obs expectedFirstObs = buildDiagnosis(patient, "2013-09-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, "non-coded pain").save().get();

        List<Diagnosis> diagnoses = diagnosisService.getDiagnoses(patient, DateUtil.parseDate("2001-09-01", "yyyy-MM-dd"));
        assertThat(diagnoses.get(0).getExistingObs(), is(expectedFirstObs));
        assertThat(diagnoses.get(1).getExistingObs(), is(expectedSecondObs));
        assertThat(diagnoses.get(2).getExistingObs(), is(expectedThirdObs));
    }

	@Test
	public void getUniqueDiagnosesShouldReturnNoTextDuplicates() {
		Patient patient = patientService.getPatient(2);
        Obs olderObs = buildDiagnosis(patient, "2013-08-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, "non-coded pain").save().get();
		Obs mostRecentObs = buildDiagnosis(patient, "2013-09-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, "non-coded pain").save().get();

		List<Diagnosis> diagnoses = diagnosisService.getUniqueDiagnoses(patient, DateUtil.parseDate("2013-01-01", "yyyy-MM-dd"));
        assertThat(diagnoses.size(), is(1));
		assertThat(diagnoses.get(0).getExistingObs(), is(mostRecentObs));
	}

	@Test
	public void getUniqueDiagnosesShouldReturnNoCodedDuplicates() {
		Patient patient = patientService.getPatient(2);
		Concept malaria = conceptService.getConcept(11);
		Obs olderObs = buildDiagnosis(patient, "2013-08-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, malaria).save().get();
        Obs mostRecentObs = buildDiagnosis(patient, "2013-09-10", Diagnosis.Order.PRIMARY, Diagnosis.Certainty.PRESUMED, malaria).save().get();

		List<Diagnosis> diagnoses = diagnosisService.getUniqueDiagnoses(patient, DateUtil.parseDate("2013-01-01", "yyyy-MM-dd"));
        assertThat(diagnoses.size(), is(1));
        assertThat(diagnoses.get(0).getExistingObs(), is(mostRecentObs));
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
