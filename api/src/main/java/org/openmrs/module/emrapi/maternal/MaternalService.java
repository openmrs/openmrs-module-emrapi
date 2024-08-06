package org.openmrs.module.emrapi.maternal;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.api.OpenmrsService;

public interface MaternalService extends OpenmrsService {

    /**
     * Returns all "newborns" of the specified patients, where "newborn" is defined as a patient who is:
     *  - linked to the specified patient by a relationship of type emrapi.motherChildRelationshipType
     *  - has as an active visit at the same visit location of the mother
     *  - has a birthdate that is on or after the start date of the mother's active visit (at the visitLocation, if specified) (note matches on date, not datetime to account for retrospective data entry or only have a date component of birthdate)
     *
     * @param mothers
     * @return
     */
    public List<Newborn> getNewbornsByMother(List<Patient> mothers);

    /**
     * Returns all mothers of the specified patients(), where "mother" is defined as a patient who is:
     * - linked to the specified patient by a relationship of typ emrapi.motherChildRelationshipType
     * - has an active visit at the same visit location of the newborn
     *
     * @param newborns  (assumption: these are newborns, method does *not* confirm this)
     * @return
     */
    public List<Mother> getMothersByNewborn(List<Patient> newborns);
}
