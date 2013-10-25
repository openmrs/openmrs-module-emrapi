package org.openmrs.module.emrapi.visit;

import org.openmrs.Visit;

public interface EmrVisitService {

    /**
     * Takes passed visit and wraps in the VisitDomainWrapper
     *
     */
    VisitDomainWrapper getVisitDomainWrapper(Visit visit);

}
