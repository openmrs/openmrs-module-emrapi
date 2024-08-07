package org.openmrs.module.emrapi.maternal;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.api.OpenmrsService;

public interface MaternalService extends OpenmrsService {

    // TODO update javadoc
    public List<Child> getChildrenByMother(ChildSearchCriteria criteria);

    public List<Mother> getMothersByChild(MotherSearchCriteria criteria);
}
