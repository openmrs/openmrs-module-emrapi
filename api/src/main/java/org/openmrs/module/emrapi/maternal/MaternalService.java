package org.openmrs.module.emrapi.maternal;

import java.util.List;

import org.openmrs.api.OpenmrsService;

public interface MaternalService extends OpenmrsService {

    /**
     * Fetches patients who are "children" (personB) of a Mother-Child relationship
     * @param criteria search criteria (see class for details)
     * @return a list of children, with their linked mothers (note this returns a "Child" per mother-child pair, so a child with multiple mothers will appear multiple times)
     */
    List<Child> getChildrenByMothers(ChildrenByMothersSearchCriteria criteria);

    /**
     * Fetches patients who are "mothers" (personA) of a Mother-Child relationship
     * @param criteria search criteria (see class for details)
     * @return a list of mothers, with their linked children (note this returns a "Mother" per mother-child pair, so a mother with multiple children will appear multiple times)
     */
    List<Mother> getMothersByChildren(MothersByChildrenSearchCriteria criteria);
}
