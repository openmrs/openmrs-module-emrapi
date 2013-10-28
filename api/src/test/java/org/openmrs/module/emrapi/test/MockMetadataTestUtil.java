package org.openmrs.module.emrapi.test;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.test.builder.ConceptBuilder;

import static org.mockito.Mockito.when;

/**
 * Contains helper methods to set up standard metadata on mock services
 */
public class MockMetadataTestUtil {

    public static void setupMockConceptService(ConceptService conceptService, EmrApiProperties emrApiProperties) {
        ConceptDatatype naDatatype = setupConceptDatatype(conceptService, "N/A", "ZZ", ConceptDatatype.N_A_UUID);
        ConceptDatatype codedDatatype = setupConceptDatatype(conceptService, "Coded", ConceptDatatype.CODED, ConceptDatatype.CODED_UUID);
        ConceptDatatype textDatatype = setupConceptDatatype(conceptService, "Text", ConceptDatatype.TEXT, ConceptDatatype.TEXT_UUID);

        ConceptClass misc = setupConceptClass(conceptService, "Misc");
        ConceptClass convSet = setupConceptClass(conceptService, "ConvSet");

        ConceptSource emrConceptSource = new ConceptSource();
        emrConceptSource.setName(EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);

        when(conceptService.getConceptSourceByName(EmrApiConstants.EMR_CONCEPT_SOURCE_NAME)).thenReturn(emrConceptSource);
        when(emrApiProperties.getEmrApiConceptSource()).thenReturn(emrConceptSource);
    }

    private static ConceptClass setupConceptClass(ConceptService mockConceptService, String name) {
        ConceptClass conceptClass = new ConceptClass();
        conceptClass.setName(name);
        when(mockConceptService.getConceptClassByName(name)).thenReturn(conceptClass);
        return conceptClass;
    }

    private static ConceptDatatype setupConceptDatatype(ConceptService mockConceptService, String name, String hl7Code, String uuid) {
        ConceptDatatype conceptDatatype = mockConceptService.getConceptDatatypeByName(name);
        if (conceptDatatype == null) {
            conceptDatatype = new ConceptDatatype();
            conceptDatatype.setName(name);
            conceptDatatype.setHl7Abbreviation(hl7Code);
            conceptDatatype.setUuid(uuid);
            when(mockConceptService.getConceptDatatypeByName(name)).thenReturn(conceptDatatype);
            when(mockConceptService.getConceptDatatypeByUuid(uuid)).thenReturn(conceptDatatype);
        }
        return conceptDatatype;
    }

    public static DispositionDescriptor setupDispositionDescriptor(ConceptService conceptService) {

        ConceptDatatype naDatatype = conceptService.getConceptDatatypeByName("N/A");
        ConceptDatatype codedDatatype = conceptService.getConceptDatatypeByName("Coded");
        ConceptDatatype dateDatatype = conceptService.getConceptDatatypeByName("Date");
        ConceptDatatype textDatatype = conceptService.getConceptDatatypeByName("Text");

        ConceptClass misc = conceptService.getConceptClassByName("Misc");

        Concept admit = new ConceptBuilder(conceptService, naDatatype, misc).addName("Admit").get();
        Concept home = new ConceptBuilder(conceptService, naDatatype, misc).addName("Home").get();

        Concept disposition = new ConceptBuilder(conceptService, codedDatatype, misc)
                .addName("Disposition")
                .addAnswers(admit, home).get();

        Concept dispositionSet = new ConceptBuilder(conceptService, naDatatype, misc)
                .addName("Disposition Construct")
                .addSetMember(disposition).get();

        Concept admissionLocation = new ConceptBuilder(conceptService, textDatatype, misc)
                .addName("Admission Location").get();

        Concept internalTransferLocation = new ConceptBuilder(conceptService, textDatatype, misc)
                .addName("Transfer Location").get();

        Concept dateOfDeath = new ConceptBuilder(conceptService, dateDatatype, misc)
                .addName("Date of death").get();

        DispositionDescriptor dispositionDescriptor = new DispositionDescriptor();
        dispositionDescriptor.setDispositionSetConcept(dispositionSet);
        dispositionDescriptor.setDispositionConcept(disposition);
        dispositionDescriptor.setAdmissionLocationConcept(admissionLocation);
        dispositionDescriptor.setInternalTransferLocationConcept(internalTransferLocation);
        dispositionDescriptor.setDateOfDeathConcept(dateOfDeath);

        return dispositionDescriptor;
    }

    public static DiagnosisMetadata setupDiagnosisMetadata(EmrApiProperties emrApiProperties, ConceptService conceptService) {

        ConceptDatatype naDatatype = conceptService.getConceptDatatypeByName("N/A");
        ConceptDatatype codedDatatype = conceptService.getConceptDatatypeByName("Coded");
        ConceptDatatype textDatatype = conceptService.getConceptDatatypeByName("Text");

        ConceptClass misc = conceptService.getConceptClassByName("Misc");
        ConceptClass convSet = conceptService.getConceptClassByName("ConvSet");

        ConceptSource emrSource = emrApiProperties.getEmrApiConceptSource();
        ConceptMapType sameAs = new ConceptMapType();

        Concept primary = new ConceptBuilder(conceptService, naDatatype, misc)
                .addName("Primary")
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_PRIMARY).get();

        Concept secondary = new ConceptBuilder(conceptService, naDatatype, misc)
                .addName("Secondary")
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_SECONDARY).get();

        Concept order = new ConceptBuilder(conceptService, codedDatatype, misc)
                .addName("Diagnosis order")
                .addAnswers(primary, secondary)
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER).get();

        Concept confirmed = new ConceptBuilder(conceptService, naDatatype, misc)
                .addName("Confirmed")
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY_CONFIRMED).get();

        Concept presumed = new ConceptBuilder(conceptService, naDatatype, misc)
                .addName("Presumed")
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY_PRESUMED).get();

        Concept certainty = new ConceptBuilder(conceptService, codedDatatype, misc)
                .addName("Diagnosis certainty")
                .addAnswers(confirmed, presumed)
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY).get();

        Concept codedDiagnosis = new ConceptBuilder(conceptService, codedDatatype, misc)
                .addName("Coded diagnosis")
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_CODED_DIAGNOSIS).get();

        Concept nonCodedDiagnosis = new ConceptBuilder(conceptService, textDatatype, misc)
                .addName("Non-coded diagnosis")
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_NON_CODED_DIAGNOSIS).get();

        Concept diagnosisSet = new ConceptBuilder(conceptService, naDatatype, convSet)
                .addName("Visit diagnoses")
                .addSetMembers(order, certainty, codedDiagnosis, nonCodedDiagnosis)
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CONCEPT_SET).get();

        DiagnosisMetadata diagnosisMetadata = new DiagnosisMetadata();
        diagnosisMetadata.setEmrConceptSource(emrSource);
        diagnosisMetadata.setDiagnosisSetConcept(diagnosisSet);
        diagnosisMetadata.setCodedDiagnosisConcept(codedDiagnosis);
        diagnosisMetadata.setNonCodedDiagnosisConcept(nonCodedDiagnosis);
        diagnosisMetadata.setDiagnosisOrderConcept(order);
        diagnosisMetadata.setDiagnosisCertaintyConcept(certainty);

        when(emrApiProperties.getDiagnosisMetadata()).thenReturn(diagnosisMetadata);
        return diagnosisMetadata;
    }

}
