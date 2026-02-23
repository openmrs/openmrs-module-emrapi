package org.openmrs.module.emrapi.procedure;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.openmrs.annotation.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for {@link Procedure} objects.
 * @since 3.3.0
 */
@Handler(supports = { Procedure.class }, order = 50)
@Slf4j
public class ProcedureValidator implements Validator {

   private static final String CURRENT_PROCEDURE_TYPE_UUID = "cce8ea25-ba2c-4dfe-a386-fba606bc2ef2";
   
   @Override
   public boolean supports(Class<?> clazz) {
      return Procedure.class.isAssignableFrom(clazz);
   }
   
   @Override
   public void validate(Object target, Errors errors) {
      log.debug("Validating procedure: {}", target);
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
         if (procedure.getDuration() != null && procedure.getDurationUnit() == null) {
            errors.reject("Procedure.error.durationUnitRequired");
         }
         if (procedure.getStatus() == null) {
            errors.reject("Procedure.error.statusRequired");
         }
         if (procedure.getVoided() && StringUtils.isBlank(procedure.getVoidReason())) {
            errors.reject("Procedure.error.voidReasonRequiredWhenVoided");
         }
         if (procedure.getProcedureType() == null){
            errors.reject("Procedure.error.procedureTypeRequired");
         }
        
         // Rules for new procedures only
         if (procedure.getProcedureId() == null){
            if( procedure.getProcedureType().getRetired()) {
               errors.reject("Procedure.error.procedureTypeRetired");
            }
            if (procedure.getEstimatedStartDate() != null && procedure.getStartDateTime() != null) {
               errors.reject("Procedure.error.startDateTimeAndEstimatedDateMutuallyExclusiveForNewProcedures");
            }
         }
         
         if (procedure.getProcedureType().getUuid().equals(CURRENT_PROCEDURE_TYPE_UUID)){
            
            if (procedure.getEncounter() == null){
               errors.reject("Procedure.error.encounterRequiredForCurrentProcedures");
            }
         }
         if (errors.hasErrors()) {
            log.warn("Validation failed for procedure {}: {}", procedure.getUuid(), errors.getAllErrors());
         }
      }
   }
}
