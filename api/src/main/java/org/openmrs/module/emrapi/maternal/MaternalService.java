package org.openmrs.module.emrapi.maternal;

import java.util.List;

import org.openmrs.api.OpenmrsService;

public interface MaternalService extends OpenmrsService {

    /**
     * Fetches patients in a "Mother-to-Child" relationship, based on the given search criteria.
     *
     * @param criteria search criteria (see class for details)
     * @return a list of mothers and children that match the search criteria
     */
    List<MotherAndChild> getMothersAndChildren(MothersAndChildrenSearchCriteria criteria);

}
