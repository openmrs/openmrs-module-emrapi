/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.emrapi.procedure;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public Procedure saveProcedure(Procedure procedure) throws APIException {
        validateProcedure(procedure);
        return procedureDAO.saveOrUpdate(procedure);
    }

    /**
     * Validates a procedure before saving.
     *
     * @param procedure the procedure to validate
     * @throws APIException if validation fails
     */
    private void validateProcedure(Procedure procedure) throws APIException {
        if (procedure.getPatient() == null) {
            throw new APIException("Procedure.error.patientRequired", (Object[]) null);
        }
        if (procedure.getProcedureCoded() == null && StringUtils.isBlank(procedure.getProcedureNonCoded())) {
            throw new APIException("Procedure.error.procedureRequired", (Object[]) null);
        }
        if (procedure.getBodySite() == null) {
            throw new APIException("Procedure.error.bodySiteRequired", (Object[]) null);
        }
        if (procedure.getStartDateTime() == null) {
            throw new APIException("Procedure.error.startDateTimeRequired", (Object[]) null);
        }
        if (procedure.getDuration() != null && procedure.getDurationUnit() == null) {
            throw new APIException("Procedure.error.durationUnitRequired", (Object[]) null);
        }
        if (procedure.getStatus() == null) {
            throw new APIException("Procedure.error.statusRequired", (Object[]) null);
        }
        // Validate that end date is not before start date
//        if (procedure.getEndDateTime() != null && procedure.getStartDateTime() != null) {
//            if (procedure.getEndDateTime().before(procedure.getStartDateTime())) {
//                throw new APIException("Procedure.error.endDateBeforeStartDate", (Object[]) null);
//            }
//        }
    }

    @Override
    @Transactional(readOnly = true)
    public Procedure getProcedureByUuid(String uuid) {
        return procedureDAO.getByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public Procedure getProcedure(Integer id) {
        return procedureDAO.getById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Procedure> getProceduresByPatient(Patient patient) {
        return procedureDAO.getProceduresByPatient(patient, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Procedure> getProceduresByPatient(Patient patient, boolean includeVoided) {
        return procedureDAO.getProceduresByPatient(patient, includeVoided);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Procedure> getProceduresByEncounter(Encounter encounter) {
        return procedureDAO.getProceduresByEncounter(encounter);
    }

    @Override
    @Transactional
    public Procedure voidProcedure(Procedure procedure, String voidReason) throws APIException {
        if (StringUtils.isBlank(voidReason)) {
            throw new APIException("Procedure.error.voidReasonRequired", (Object[]) null);
        }
        procedure.setVoided(true);
        procedure.setVoidReason(voidReason);
        procedure.setDateVoided(new Date());
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
}
