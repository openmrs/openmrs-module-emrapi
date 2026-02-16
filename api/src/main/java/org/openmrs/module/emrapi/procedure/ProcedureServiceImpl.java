/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.procedure;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

/**
 * Implementation of the ProcedureService interface.
 */
@Transactional
public class ProcedureServiceImpl extends BaseOpenmrsService implements ProcedureService {
   
   private ProcedureDAO procedureDAO;
   
   public void setProcedureDAO(ProcedureDAO procedureDAO) {
      this.procedureDAO = procedureDAO;
   }
   
   @Override
   @Transactional(readOnly = true)
   public Procedure getProcedureByUuid(String uuid) {
      return procedureDAO.getByUuid(uuid);
   }
   
   @Override
   @Transactional(readOnly = true)
   public List<Procedure> getProceduresByPatient(Patient patient) {
      return procedureDAO.getProceduresByPatient(patient, false);
   }
   
   @Override
   @Transactional
   public Procedure saveProcedure(Procedure procedure) throws APIException {
      if (procedure.getEstimatedStartDate() != null) {
         Date calculatedStartDateTime = getDateTimeFromEstimatedDate(procedure.getEstimatedStartDate());
         procedure.setStartDateTime(calculatedStartDateTime);
      }
      return procedureDAO.saveOrUpdate(procedure);
   }
   
   @Override
   @Transactional
   public Procedure unvoidProcedure(Procedure procedure) {
      procedure.setVoided(false);
      procedure.setVoidReason(null);
      procedure.setDateVoided(null);
      procedure.setVoidedBy(null);
      return procedureDAO.saveOrUpdate(procedure);
   }
   
   @Override
   @Transactional
   public void purgeProcedure(Procedure procedure) throws APIException {
      procedureDAO.delete(procedure);
   }
   
   @Override
   public Procedure voidProcedure(Procedure procedure, String reason) {
      return procedureDAO.saveOrUpdate(procedure);
   }

   Date getDateTimeFromEstimatedDate(String estimatedDate) {
      try {
         // Full datetime
         if (estimatedDate.length() > 10) {
            LocalDateTime dateTime = LocalDateTime.parse(estimatedDate);
            return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
         }
         
         // yyyy-MM-dd
         if (estimatedDate.length() == 10) {
            LocalDate date = LocalDate.parse(estimatedDate);
            return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
         }
         
         // yyyy-MM
         if (estimatedDate.length() == 7) {
            YearMonth ym = YearMonth.parse(estimatedDate);
            return Date.from(
                    ym.atDay(1)
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
            );
         }
         
         // yyyy
         if (estimatedDate.length() == 4) {
            Year year = Year.parse(estimatedDate);
            return Date.from(
                    year.atMonth(1)
                            .atDay(1)
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
            );
         }
         
         throw new APIException("Procedure.error.invalidEstimateDate", new Object[] { estimatedDate });
         
      }
      catch (DateTimeParseException e) {
         throw new APIException("Procedure.error.invalidEstimateDate", new Object[] { estimatedDate }, e);
      }
   }
}
