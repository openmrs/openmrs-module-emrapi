package org.openmrs.module.emrapi.patient;

import org.openmrs.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class PatientDomainWrapperFactory {

    @Autowired
    private ApplicationContext applicationContext;

    public PatientDomainWrapper newPatientDomainWrapper() {
        PatientDomainWrapper patientDomainWrapper = new PatientDomainWrapper();
        applicationContext.getAutowireCapableBeanFactory().autowireBean(patientDomainWrapper);
        return patientDomainWrapper;
    }

    public PatientDomainWrapper newPatientDomainWrapper(Patient patient) {
        PatientDomainWrapper patientDomainWrapper = newPatientDomainWrapper();
        patientDomainWrapper.setPatient(patient);
        return patientDomainWrapper;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
