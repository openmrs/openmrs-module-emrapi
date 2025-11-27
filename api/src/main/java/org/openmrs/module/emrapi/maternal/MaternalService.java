package org.openmrs.module.emrapi.maternal;

import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.util.PrivilegeConstants;

public interface MaternalService extends OpenmrsService {

    /**
     * Fetches patients in a "Mother-to-Child" relationship, based on the given search criteria.
     *
     * @param criteria search criteria (see class for details)
     * @return a list of mothers and children that match the search criteria
     */
    @Authorized(PrivilegeConstants.VIEW_PATIENTS)
    List<MotherAndChild> getMothersAndChildren(MothersAndChildrenSearchCriteria criteria);

}
