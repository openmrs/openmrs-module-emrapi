package org.openmrs.module.emrapi.test;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiActivator;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.test.builder.ConceptBuilder;

/**
 *
 */
public class ContextSensitiveMetadataTestUtils {

    /**
     * Sets up DispositionDescriptor in a context-sensitive test
     * @param conceptService
     * @param emrApiProperties
     * @return
     */
    public static DispositionDescriptor setupDispositionDescriptor(ConceptService conceptService, EmrApiProperties emrApiProperties) {
        ConceptSource emrSource = new EmrApiActivator().createConceptSource(conceptService);
        ConceptMapType sameAs = conceptService.getConceptMapTypeByName("same-as");

        ConceptDatatype naDatatype = conceptService.getConceptDatatypeByName("N/A");
        ConceptDatatype codedDatatype = conceptService.getConceptDatatypeByName("Coded");
        ConceptDatatype textDatatype = conceptService.getConceptDatatypeByName("Text");

        ConceptClass convSet = conceptService.getConceptClassByName("ConvSet");
        ConceptClass misc = conceptService.getConceptClassByName("Misc");

        Concept admit = new ConceptBuilder(conceptService, naDatatype, misc)
                .addName("Admit to hospital").saveAndGet();
        Concept discharge = new ConceptBuilder(conceptService, naDatatype, misc)
                .addName("Discharged").saveAndGet();
        Concept transferOut = new ConceptBuilder(conceptService, naDatatype, misc)
                .addName("Transfer out of hospital").saveAndGet();

        Concept transferTo = new ConceptBuilder(conceptService, textDatatype, misc)
                .addName("Transfer to location").saveAndGet();

        Concept disposition = new ConceptBuilder(conceptService, codedDatatype, convSet)
                .addName("Disposition")
                .addAnswers(admit, discharge, transferOut)
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DISPOSITION).saveAndGet();

        new ConceptBuilder(conceptService, naDatatype, convSet)
                .addName("Disposition Construct")
                .addSetMembers(disposition, transferTo)
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DISPOSITION_CONCEPT_SET).saveAndGet();
        return emrApiProperties.getDispositionDescriptor();
    }

    /**
     * Sets up DiagnosisMetadata in a context-sensitive test
     */
    public static DiagnosisMetadata setupDiagnosisMetadata(ConceptService conceptService, EmrApiProperties emrApiProperties) {
        ConceptSource emrSource = new EmrApiActivator().createConceptSource(conceptService);
        ConceptMapType sameAs = conceptService.getConceptMapTypeByName("same-as");

        ConceptDatatype naDatatype = conceptService.getConceptDatatypeByName("N/A");
        ConceptDatatype codedDatatype = conceptService.getConceptDatatypeByName("Coded");
        ConceptDatatype textDatatype = conceptService.getConceptDatatypeByName("Text");

        ConceptClass convSet = conceptService.getConceptClassByName("ConvSet");
        ConceptClass misc = conceptService.getConceptClassByName("Misc");

        Concept primary = new ConceptBuilder(conceptService, naDatatype, misc)
                .addName("Primary")
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_PRIMARY).saveAndGet();

        Concept secondary = new ConceptBuilder(conceptService, naDatatype, misc)
                .addName("Secondary")
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_SECONDARY).saveAndGet();

        Concept order = new ConceptBuilder(conceptService, codedDatatype, misc)
                .addName("Diagnosis order")
                .addAnswers(primary, secondary)
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER).saveAndGet();

        Concept confirmed = new ConceptBuilder(conceptService, naDatatype, misc)
                .addName("Confirmed")
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY_CONFIRMED).saveAndGet();

        Concept presumed = new ConceptBuilder(conceptService, naDatatype, misc)
                .addName("Presumed")
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY_PRESUMED).saveAndGet();

        Concept certainty = new ConceptBuilder(conceptService, codedDatatype, misc)
                .addName("Diagnosis certainty")
                .addAnswers(confirmed, presumed)
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY).saveAndGet();

        Concept codedDiagnosis = new ConceptBuilder(conceptService, codedDatatype, misc)
                .addName("Coded diagnosis")
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_CODED_DIAGNOSIS).saveAndGet();

        Concept nonCodedDiagnosis = new ConceptBuilder(conceptService, textDatatype, misc)
                .addName("Non-coded diagnosis")
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_NON_CODED_DIAGNOSIS).saveAndGet();

        new ConceptBuilder(conceptService, naDatatype, convSet)
                .addName("Visit diagnoses")
                .addSetMembers(order, certainty, codedDiagnosis, nonCodedDiagnosis)
                .addMapping(sameAs, emrSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CONCEPT_SET).saveAndGet();

        return emrApiProperties.getDiagnosisMetadata();
    }

}
