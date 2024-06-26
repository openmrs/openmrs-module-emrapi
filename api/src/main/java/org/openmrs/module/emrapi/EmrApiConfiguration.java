package org.openmrs.module.emrapi;

import org.openmrs.Concept;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptSource;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.VisitType;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;

import java.util.Collection;
import java.util.List;

public class EmrApiConfiguration {

    private EmrApiProperties properties;

    public EmrApiProperties getProperties() {
        return properties;
    }

    public void setProperties(EmrApiProperties properties) {
        this.properties = properties;
    }

    // EMR API Properties
    private String metadataSourceName;
    private Location unknownLocation;
    private Provider unknownProvider;
    private EncounterRole orderingProviderEncounterRole;
    private Role fullPrivilegeLevel;
    private Role highPrivilegeLevel;
    private EncounterType checkInEncounterType;
    private EncounterRole checkInClerkEncounterRole;
    private EncounterType visitNoteEncounterType;
    private EncounterType consultEncounterType;
    private EncounterRole clinicianEncounterRole;
    private EncounterType admissionEncounterType;
    private EncounterType exitFromInpatientEncounterType;
    private EncounterType transferWithinHospitalEncounterType;
    private Form admissionForm;
    private Form dischargeForm;
    private Form transferForm;
    private Integer visitExpireHours;
    private VisitType atFacilityVisitType;
    private LocationTag supportsVisitsLocationTag;
    private LocationTag supportsLoginLocationTag;
    private LocationTag supportsAdmissionLocationTag;
    private LocationTag supportsTransferLocationTag;
    private PersonAttributeType testPatientPersonAttributeType;
    private PersonAttributeType telephoneAttributeType;
    private PersonAttributeType unknownPatientPersonAttributeType;
    private PatientIdentifierType primaryIdentifierType;
    private List<PatientIdentifierType> extraPatientIdentifierTypes;
    private DiagnosisMetadata diagnosisMetadata;
    private List<ConceptSource> conceptSourcesForDiagnosisSearch;
    private ConceptSource emrApiConceptSource;
    private Concept unknownCauseOfDeathConcept;
    private Concept admissionDecisionConcept;
    private Concept denyAdmissionConcept;
    private Concept patientDiedConcept;
    private List<PatientIdentifierType> identifierTypesToSearch;
    private Collection<Concept> diagnosisSets;
    private Collection<Concept> nonDiagnosisConceptSets;
    private Collection<Concept> suppressedDiagnosisConcepts;
    private ConceptMapType sameAsConceptMapType;
    private ConceptMapType narrowerThanConceptMapType;
    private Integer lastViewedPatientSizeLimit;

}