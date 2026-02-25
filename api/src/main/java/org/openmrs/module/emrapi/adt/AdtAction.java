package org.openmrs.module.emrapi.adt;

import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.exception.InvalidAdtEncounterException;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Helper class that captures details about admitting, discharging and transfering a patient within the hospital.
 * Use {@link AdtService} to save one of these (which actually creates and returns an encounter).
 * (This is a placeholder for a class representing a proper transfer note. Eventually this class will be extended to
 * include more properties, and support obs and orders too.)
 */
public class AdtAction {

    private Visit visit;
    private Location location;
    private Date actionDatetime;
    private Map<EncounterRole, Set<Provider>> providers;
    private Type type;

    public AdtAction(Visit visit, Location toLocation, Map<EncounterRole, Set<Provider>> providers, Type type) {
        this.visit = visit;
        this.location = toLocation;
        this.providers = providers;
        this.type = type;
    }

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Date getActionDatetime() {
        return actionDatetime;
    }

    public void setActionDatetime(Date actionDatetime) {
        this.actionDatetime = actionDatetime;
    }

    public Map<EncounterRole, Set<Provider>> getProviders() {
        return providers;
    }

    public void setProviders(Map<EncounterRole, Set<Provider>> providers) {
        this.providers = providers;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        ADMISSION {
            @Override
            public EncounterType getEncounterType(EmrApiProperties properties) {
                EncounterType encounterType = properties.getAdmissionEncounterType();
                if (encounterType == null) {
                    throw new IllegalStateException("Configuration required: " + EmrApiConstants.GP_ADMISSION_ENCOUNTER_TYPE);
                }
                return encounterType;

            }

            @Override
            public Form getForm(EmrApiProperties properties) {
                // allowed to be null
                return properties.getAdmissionForm();
            }

            @Override
            public void checkVisitValid(VisitDomainWrapper visit) {
                if (visit.isAdmitted()) {
                    throw new IllegalStateException("Patient is already admitted");
                }
            }

            @Override
            public void checkEncounterValid(VisitDomainWrapper visit, Encounter encounter) {
                Date onDate = encounter.getEncounterDatetime();
                Location toLocation = encounter.getLocation();
                if (visit.isAdmittedAtTimeOfEncounter(encounter)) {
                    throw new InvalidAdtEncounterException(InvalidAdtEncounterException.Type.PATIENT_ALREADY_ADMITTED, toLocation, onDate);
                }
            }
        }, DISCHARGE {
            @Override
            public EncounterType getEncounterType(EmrApiProperties properties) {
                EncounterType encounterType = properties.getExitFromInpatientEncounterType();
                if (encounterType == null) {
                    throw new IllegalStateException("Configuration required: " + EmrApiConstants.GP_EXIT_FROM_INPATIENT_ENCOUNTER_TYPE);
                }
                return encounterType;
            }

            @Override
            public Form getForm(EmrApiProperties properties) {
                // allowed to be null
                return properties.getDischargeForm();
            }

            @Override
            public void checkVisitValid(VisitDomainWrapper visit) {
                if (!visit.isAdmitted()) {
                    throw new IllegalStateException("Patient is not currently admitted");
                }
            }

            @Override
            public void checkEncounterValid(VisitDomainWrapper visit, Encounter encounter) {
                Date onDate = encounter.getEncounterDatetime();
                Location toLocation = encounter.getLocation();
                if (!visit.isAdmittedAtTimeOfEncounter(encounter)) {
                    throw new InvalidAdtEncounterException(InvalidAdtEncounterException.Type.PATIENT_NOT_ADMITTED, toLocation, onDate);
                }
            }
        }, TRANSFER {
            @Override
            public EncounterType getEncounterType(EmrApiProperties properties) {
                EncounterType encounterType = properties.getTransferWithinHospitalEncounterType();
                if (encounterType == null) {
                    throw new IllegalStateException("Configuration required: " + EmrApiConstants.GP_TRANSFER_WITHIN_HOSPITAL_ENCOUNTER_TYPE);
                }
                return encounterType;
            }

            @Override
            public Form getForm(EmrApiProperties properties) {
                // allowed to be null
                return properties.getTransferForm();
            }

            @Override
            public void checkVisitValid(VisitDomainWrapper visit) {
            }

            @Override
            public void checkEncounterValid(VisitDomainWrapper visit, Encounter encounter) {
                Date onDate = encounter.getEncounterDatetime();
                Location toLocation = encounter.getLocation();
                
                if (!visit.isAdmittedAtTimeOfEncounter(encounter)) {
                    throw new InvalidAdtEncounterException(InvalidAdtEncounterException.Type.PATIENT_NOT_ADMITTED, toLocation, onDate);
                }
                Location currentLocation = visit.getInpatientLocationAtTimeOfEncounter(encounter);
                if (toLocation != null && toLocation.equals(currentLocation)) {
                    throw new InvalidAdtEncounterException(InvalidAdtEncounterException.Type.PATIENT_ALREADY_AT_LOCATION, toLocation, onDate);
                }
            }
        };

        public abstract EncounterType getEncounterType(EmrApiProperties properties);
        public abstract Form getForm(EmrApiProperties form);

        /**
         * @deprecated since 3.3.0 - use {@link #checkEncounterValid(VisitDomainWrapper, Encounter)} instead.
         */
        @Deprecated
        public abstract void checkVisitValid(VisitDomainWrapper visit);

        /**
         * Verify that the encounter representing this AdtAction is valid
         * @param visit - the visit associated with the given encounter
         * @param encounter the encounter being validated, which represents the AdtAction and may not have been saved yet.
         */
        public abstract void checkEncounterValid(VisitDomainWrapper visit, Encounter encounter);
    }

}
