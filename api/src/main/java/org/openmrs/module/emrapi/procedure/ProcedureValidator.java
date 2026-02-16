package org.openmrs.module.emrapi.procedure;

import org.apache.commons.lang.StringUtils;
import org.openmrs.annotation.Handler;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Handler(supports = { Procedure.class }, order = 50)
public class ProcedureValidator implements Validator {
   
   private static final String CURRENT_PROCEDURE_TYPE_UUID = "cce8ea25-ba2c-4dfe-a386-fba606bc2ef2";
   
   @Override
   public boolean supports(Class<?> clazz) {
      return Procedure.class.isAssignableFrom(clazz);
   }
   
   @Override
   public void validate(Object target, Errors errors) {
      if (!(target instanceof Procedure)) {
         errors.reject("ProcedureValidator.onlySupportsProcedure");
      } else {
         Procedure procedure = (Procedure) target;
         if (procedure.getPatient() == null) {
            errors.reject("Procedure.error.patientRequired");
         }
         if (procedure.getProcedureCoded() == null && StringUtils.isBlank(procedure.getProcedureNonCoded())) {
            errors.reject("Procedure.error.procedureRequired");
         }
         if (procedure.getProcedureCoded() != null && StringUtils.isNotBlank(procedure.getProcedureNonCoded())) {
            errors.reject("Procedure.error.procedureCodedAndNonCodedMutuallyExclusive");
         }
         if (procedure.getBodySite() == null) {
            errors.reject("Procedure.error.bodySiteRequired");
         }
         if (procedure.getEstimatedStartDate() == null && procedure.getStartDateTime() == null) {
            errors.reject("Procedure.error.startDateTimeRequired");
         }
         if (procedure.getEstimatedStartDate() != null && procedure.getStartDateTime() != null) {
            errors.reject("Procedure.error.startDateTimeAndEstimatedDateMutuallyExclusive");
         }
         if (procedure.getDuration() != null && procedure.getDurationUnit() == null) {
            errors.reject("Procedure.error.durationUnitRequired");
         }
         if (procedure.getStatus() == null) {
            errors.reject("Procedure.error.statusRequired");
         }
         if (procedure.getVoided() && StringUtils.isBlank(procedure.getVoidReason())) {
            errors.reject("Procedure.error.voidReasonRequiredWhenVoided");
         }
         
         if (procedure.getProcedureType() != null && procedure.getProcedureType().getUuid().equals(CURRENT_PROCEDURE_TYPE_UUID)){
            if (procedure.getProcedureType().getRetired()) {
               errors.reject("Procedure.error.procedureTypeRetired");
            }
            if (procedure.getEncounter() == null){
               errors.reject("Procedure.error.encounterRequiredForCurrentProcedures");
            }
         }
      }
   }
}
