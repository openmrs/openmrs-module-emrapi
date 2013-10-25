package org.openmrs.module.emrapi.visit;

import org.openmrs.Visit;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiProperties;

public class EmrVisitServiceImpl extends BaseOpenmrsService implements EmrVisitService {

    private EmrApiProperties emrApiProperties;

    public EmrVisitServiceImpl(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    @Override
    public VisitDomainWrapper getVisitDomainWrapper(Visit visit) {
        return new VisitDomainWrapper(visit, emrApiProperties);
    }

}
