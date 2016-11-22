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

import java.util.List;

import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.visit.contract.VisitRequest;
import org.openmrs.module.emrapi.visit.contract.VisitResponse;

/**
 * <pre>
 * Handy service to find a {@link org.openmrs.Visit}.
 * </pre>
 */
public interface EmrVisitService extends OpenmrsService {
    VisitResponse find(VisitRequest visitRequest);
    
    List<Obs> getDiagnoses(Visit visit, DiagnosisMetadata diagnosisMetadata, Boolean primaryOnly, Boolean confirmedOnly);
}
