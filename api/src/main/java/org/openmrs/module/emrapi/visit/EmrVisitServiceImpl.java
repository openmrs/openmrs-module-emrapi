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
package org.openmrs.module.emrapi.visit;

import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.VisitService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.db.EmrVisitDAO;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.diagnosis.DiagnosisUtils;
import org.openmrs.module.emrapi.diagnosis.EmrDiagnosisDAO;
import org.openmrs.module.emrapi.encounter.exception.VisitNotFoundException;
import org.openmrs.module.emrapi.visit.contract.VisitRequest;
import org.openmrs.module.emrapi.visit.contract.VisitResponse;

import java.util.ArrayList;
import java.util.List;

public class EmrVisitServiceImpl extends BaseOpenmrsService implements EmrVisitService {
    private VisitService visitService;
    private VisitResponseMapper visitResponseMapper;
    private AdministrationService adminService;
    private EmrDiagnosisDAO emrDiagnosisDAO;
    protected EmrVisitDAO dao;

    public EmrVisitDAO getDao() {
      return dao;
    }

    public void setDao(EmrVisitDAO dao) {
       this.dao = dao;
    }

    public void setAdminService(AdministrationService adminService) {
        this.adminService = adminService;
    }

    public void setEmrDiagnosisDAO(EmrDiagnosisDAO emrDiagnosisDAO) {
        this.emrDiagnosisDAO = emrDiagnosisDAO;
    }

    public EmrVisitServiceImpl(VisitService visitService, VisitResponseMapper visitResponseMapper) {
        this.visitService = visitService;
        this.visitResponseMapper = visitResponseMapper;
    }

    @Override
    public VisitResponse find(VisitRequest visitRequest) {
        Visit visit = visitService.getVisitByUuid(visitRequest.getVisitUuid());
        if(visit == null)
            throw new VisitNotFoundException("Visit by uuid "+ visitRequest.getVisitUuid() + " does not exist");
        return visitResponseMapper.map(visit);
    }

   public List<Obs> getDiagnoses(Visit visit, DiagnosisMetadata diagnosisMetadata, Boolean primaryOnly, Boolean confirmedOnly) {
       if (adminService.getGlobalProperty(EmrApiConstants.GP_USE_LEGACY_DIAGNOSIS_SERVICE, "false").equalsIgnoreCase("true")) {
           if (primaryOnly == true) {
               if (confirmedOnly == false) {
                   return dao.getPrimaryDiagnoses(visit, diagnosisMetadata);
               } else {
                   return dao.getConfirmedPrimaryDiagnoses(visit, diagnosisMetadata);
               }
           } else {
               if (confirmedOnly == false) {
                   return dao.getDiagnoses(visit, diagnosisMetadata);
               } else {
                   return dao.getConfirmedDiagnoses(visit, diagnosisMetadata);
               }
           }
       }
       else {
           List<org.openmrs.Diagnosis> diagnoses = emrDiagnosisDAO.getDiagnoses(visit, primaryOnly, confirmedOnly);
           List<Obs> diagnosisList = new ArrayList<Obs>();
           for (Diagnosis diagnosis : DiagnosisUtils.convert(diagnoses)) {
               diagnosisList.add(diagnosisMetadata.buildDiagnosisObsGroup(diagnosis));
           }
           return diagnosisList;
       }
   }

    @Override
    public List<Integer> getAllPatientsWithDiagnosis(DiagnosisMetadata diagnosisMetadata) {
        return dao.getAllPatientsWithDiagnosis(diagnosisMetadata);
    }
}
