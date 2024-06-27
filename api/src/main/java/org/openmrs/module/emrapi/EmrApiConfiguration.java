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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class EmrApiConfiguration {

    @Autowired
    private EmrApiProperties properties;

    public String getMetadataSourceName() {
        return properties.getMetadataSourceName();
    }

    public Location getUnknownLocation() {
        return properties.getUnknownLocation();
    }

    public Provider getUnknownProvider() {
        return properties.getUnknownProvider();
    }

    public EncounterRole getOrderingProviderEncounterRole() {
        return properties.getOrderingProviderEncounterRole();
    }

    public Role getFullPrivilegeLevel() {
        return properties.getFullPrivilegeLevel();
    }

    public Role getHighPrivilegeLevel() {
        return properties.getHighPrivilegeLevel();
    }

    public EncounterType getCheckInEncounterType() {
        return properties.getCheckInEncounterType();
    }

    public EncounterRole getCheckInClerkEncounterRole() {
        return properties.getCheckInClerkEncounterRole();
    }

    public EncounterType getVisitNoteEncounterType() {
        return properties.getVisitNoteEncounterType();
    }

    public EncounterRole getClinicianEncounterRole() {
        return properties.getClinicianEncounterRole();
    }

    public EncounterType getAdmissionEncounterType() {
        return properties.getAdmissionEncounterType();
    }

    public EncounterType getExitFromInpatientEncounterType() {
        return properties.getExitFromInpatientEncounterType();
    }

    public EncounterType getTransferWithinHospitalEncounterType() {
        return properties.getTransferWithinHospitalEncounterType();
    }

    public Form getAdmissionForm() {
        return properties.getAdmissionForm();
    }

    public Form getDischargeForm() {
        return properties.getDischargeForm();
    }

    public Form getTransferForm() {
        return properties.getTransferForm();
    }

    public int getVisitExpireHours() {
        return properties.getVisitExpireHours();
    }

    public VisitType getAtFacilityVisitType() {
        return properties.getAtFacilityVisitType();
    }

    public LocationTag getSupportsVisitsLocationTag() {
        return properties.getSupportsVisitsLocationTag();
    }

    public LocationTag getSupportsLoginLocationTag() {
        return properties.getSupportsLoginLocationTag();
    }

    public LocationTag getSupportsAdmissionLocationTag() {
        return properties.getSupportsAdmissionLocationTag();
    }

    public LocationTag getSupportsTransferLocationTag() {
        return properties.getSupportsTransferLocationTag();
    }

    public PersonAttributeType getTestPatientPersonAttributeType() {
        return properties.getTestPatientPersonAttributeType();
    }

    public PersonAttributeType getTelephoneAttributeType() {
        return properties.getTelephoneAttributeType();
    }

    public PersonAttributeType getUnknownPatientPersonAttributeType() {
        return properties.getUnknownPatientPersonAttributeType();
    }

    public PatientIdentifierType getPrimaryIdentifierType() {
        return properties.getPrimaryIdentifierType();
    }

    public List<PatientIdentifierType> getExtraPatientIdentifierTypes() {
        return properties.getExtraPatientIdentifierTypes();
    }

    public DiagnosisMetadata getDiagnosisMetadata() {
        return properties.getDiagnosisMetadata();
    }

    public List<ConceptSource> getConceptSourcesForDiagnosisSearch() {
        return properties.getConceptSourcesForDiagnosisSearch();
    }

    public ConceptSource getEmrApiConceptSource() {
        return properties.getEmrApiConceptSource();
    }

    public Concept getUnknownCauseOfDeathConcept() {
        return properties.getUnknownCauseOfDeathConcept();
    }

    public Concept getAdmissionDecisionConcept()  {
        return properties.getAdmissionDecisionConcept();
    }

    public Concept getDenyAdmissionConcept()  {
        return properties.getDenyAdmissionConcept();
    }

    public Concept getPatientDiedConcept() {
        return properties.getPatientDiedConcept();
    }

    public List<PatientIdentifierType> getIdentifierTypesToSearch() {
        return properties.getIdentifierTypesToSearch();
    }

    public Collection<Concept> getDiagnosisSets() {
        return properties.getDiagnosisSets();
    }

    public Collection<Concept> getNonDiagnosisConceptSets() {
        return properties.getNonDiagnosisConceptSets();
    }

    public Collection<Concept> getSuppressedDiagnosisConcepts() {
        return properties.getSuppressedDiagnosisConcepts();
    }

    public ConceptMapType getSameAsConceptMapType() {
        return properties.getSameAsConceptMapType();
    }

    public ConceptMapType getNarrowerThanConceptMapType() {
        return properties.getNarrowerThanConceptMapType();
    }

    public Integer getLastViewedPatientSizeLimit() {
        return properties.getLastViewedPatientSizeLimit();
    }
}