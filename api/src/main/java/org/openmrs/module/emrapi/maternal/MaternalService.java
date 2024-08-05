package org.openmrs.module.emrapi.maternal;

import java.util.List;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.OpenmrsService;

public interface MaternalService extends OpenmrsService {

    /**
     * Returns all "newborns" of the specified patient, where "newborn" is defined as a patient who is:
     *  - linked to the specified patient by a relationship of type emrapi.motherChildRelationshipType
     *  - has as an active visit (at the visitLocation, if specified)
     *  - has a birthdate that is on or after the start date of the mother's active visit (at the visitLocation, if specified) (note matches on date, not datetime to account for retrospective data entry or only have a date component of birthdate)
     *
     * @param mother
     * @param visitLocation if not null, restrict matching visits to only those at the specified location
     * @return
     */
    public List<Newborn> getNewbornsByMother(Patient mother, Location visitLocation);

}
