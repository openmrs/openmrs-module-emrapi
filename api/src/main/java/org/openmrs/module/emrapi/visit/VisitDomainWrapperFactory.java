package org.openmrs.module.emrapi.visit;

import org.openmrs.Visit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class VisitDomainWrapperFactory {

    @Autowired
    private ApplicationContext applicationContext;

    public VisitDomainWrapper newVisitDomainWrapper() {
        VisitDomainWrapper visitDomainWrapper = new VisitDomainWrapper();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(visitDomainWrapper);
        return visitDomainWrapper;
    }

    public VisitDomainWrapper newVisitDomainWrapper(Visit visit) {
        VisitDomainWrapper visitDomainWrapper = newVisitDomainWrapper();
        visitDomainWrapper.setVisit(visit);
        return visitDomainWrapper;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
