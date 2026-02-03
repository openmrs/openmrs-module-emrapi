package org.openmrs.module.emrapi.encounter;

import org.openmrs.Encounter;
import org.openmrs.annotation.Handler;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.exception.InvalidAdtEncounterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Handler(supports = {Encounter.class}, order = 50)
public class EmrApiEncounterValidator implements Validator {

    @Autowired
    private AdtService adtService;

    public void setAdtService(AdtService adtService) {
        this.adtService = adtService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Encounter.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Encounter encounter = (Encounter) target;
        validateAdtAction(encounter, errors);
        
    }

    private void validateAdtAction(Encounter encounter, Errors errors) {
        if (encounter.getEncounterType() == null || encounter.getVisit() == null) {
            return;
        }

        try {
            adtService.verifyEncounterForAdtAction(encounter);
        } catch (InvalidAdtEncounterException e) {
            errors.reject(e.getCode(), e.getMessage());
        }
    }
}
