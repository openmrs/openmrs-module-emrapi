package org.openmrs.module.emrapi.visit;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Wrapper around a Visit, that provides convenience methods to find particular encounters of interest.
 */
public class VisitDomainWrapper {

    private static final Log log = LogFactory.getLog(VisitDomainWrapper.class);

    @Autowired
    @Qualifier("emrApiProperties")
    EmrApiProperties emrApiProperties;

    private Visit visit;

    public VisitDomainWrapper(Visit visit) {
        this.visit = visit;
    }

    public VisitDomainWrapper(Visit visit, EmrApiProperties emrApiProperties) {
        this(visit);
        this.emrApiProperties = emrApiProperties;
    }

    /**
     * @return the visit
     */
    public Visit getVisit() {
        return visit;
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    /**
     * @return the check-in encounter for this visit, or null if none exists
     */
    public Encounter getCheckInEncounter() {
        for (Encounter e : visit.getEncounters()) {
            if (emrApiProperties.getCheckInEncounterType().equals(e.getEncounterType()))
                return e;
        }
        return null;
    }

    /**
     * @return the most recent encounter in the visit
     */
    public Encounter getLastEncounter() {
        if (visit.getEncounters().size() > 0)
            return visit.getEncounters().iterator().next();
        return null;
    }

    public int getDifferenceInDaysBetweenCurrentDateAndStartDate() {
        Date today = Calendar.getInstance().getTime();

        Calendar startDateVisit = getStartDateVisit();

        int millisecondsInADay = 1000 * 60 * 60 * 24;

        return (int) ((today.getTime() - startDateVisit.getTimeInMillis()) / millisecondsInADay);
    }

    public List<Diagnosis> getPrimaryDiagnoses() {
        List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
        DiagnosisMetadata diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
        for (Encounter encounter : visit.getEncounters()) {
            if (!encounter.isVoided()) {
                for (Obs obs : encounter.getObsAtTopLevel(false)) {
                    if (diagnosisMetadata.isDiagnosis(obs)) {
                        try {
                            Diagnosis diagnosis = diagnosisMetadata.toDiagnosis(obs);
                            if (Diagnosis.Order.PRIMARY == diagnosis.getOrder()) {
                                diagnoses.add(diagnosis);
                            }
                        } catch (Exception ex) {
                            log.warn("malformed diagnosis obs group with obsId " + obs.getObsId(), ex);
                        }
                    }
                }
            }
        }
        return diagnoses;
    }

    private Calendar getStartDateVisit() {
        Date startDatetime = visit.getStartDatetime();
        Calendar startDateCalendar = Calendar.getInstance();
        startDateCalendar.setTime(startDatetime);
        startDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startDateCalendar.set(Calendar.MINUTE, 0);
        startDateCalendar.set(Calendar.SECOND, 0);
        return startDateCalendar;
    }

    public boolean hasEncounterWithoutSubsequentEncounter(EncounterType lookForEncounterType, EncounterType withoutSubsequentEncounterType) {
        // these are sorted by date descending if you get the visit directly from hibernate, but not necessarily otherwise, so
        // we have to go through all encounters
        if (visit.getEncounters() == null) {
            return false;
        }
        Encounter mostRecentRelevant = null;
        for (Encounter encounter : visit.getEncounters()) {
            if (encounter.getEncounterType().equals(lookForEncounterType)
                    || (withoutSubsequentEncounterType != null && encounter.getEncounterType().equals(withoutSubsequentEncounterType))) {
                if (mostRecentRelevant == null || OpenmrsUtil.compare(mostRecentRelevant.getEncounterDatetime(), encounter.getEncounterDatetime()) < 0) {
                    mostRecentRelevant = encounter;
                }
            }
        }
        return mostRecentRelevant != null && mostRecentRelevant.getEncounterType().equals(lookForEncounterType);
    }

    /**
     * @return true if the visit includes an admission encounter with no discharge encounter after it
     */
    public boolean isAdmitted() {
        EncounterType admissionEncounterType = emrApiProperties.getAdmissionEncounterType();
        EncounterType dischargeEncounterType = emrApiProperties.getDischargeEncounterType();
        if (admissionEncounterType == null) {
            return false;
        }
        return hasEncounterWithoutSubsequentEncounter(admissionEncounterType, dischargeEncounterType);
    }

    public Date getStartDatetime() {
        return visit.getStartDatetime();
    }

    public Date getStopDatetime() {
        return visit.getStopDatetime();
    }

    /**
     * @param encounter
     * @return this, for call chaining
     */
    public VisitDomainWrapper addEncounter(Encounter encounter) {
        visit.addEncounter(encounter);
        return this;
    }

    /**
     * Throws an {@link IllegalArgumentException} if checkDatetime is not within the start/stop date bounds of this visit
     * @param checkDatetime
     * @param errorMessage base of the error message to throw (some details may be added)
     */
    public void errorIfOutsideVisit(Date checkDatetime, String errorMessage) throws IllegalArgumentException {
        if (visit.getStartDatetime() != null && OpenmrsUtil.compare(checkDatetime, visit.getStartDatetime()) < 0) {
            throw new IllegalArgumentException(errorMessage + ": visit started at " + visit.getStartDatetime() + " but testing an earlier date");
        }
        if (visit.getStopDatetime() != null && OpenmrsUtil.compare(visit.getStopDatetime(), checkDatetime) < 0) {
            throw new IllegalArgumentException(errorMessage + ": visit stopped at " + visit.getStopDatetime() + " but testing a later date");
        }
    }
}
