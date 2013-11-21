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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.encounter.exception.VisitNotFoundException;
import org.openmrs.module.emrapi.visit.contract.VisitRequest;
import org.openmrs.module.emrapi.visit.contract.VisitResponse;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EmrVisitServiceImplTest {
    @Mock
    private VisitService visitService;
    @Mock
    private VisitResponseMapper visitResponseMapper;

    private EmrVisitService emrVisitService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        emrVisitService = new EmrVisitServiceImpl(visitService, visitResponseMapper);
    }

    @Test
    public void shouldFindVisitByUuid() throws Exception {
        VisitRequest visitRequest = new VisitRequest(UUID.randomUUID().toString());
        Visit visit = new Visit();
        when(visitService.getVisitByUuid(visitRequest.getVisitUuid())).thenReturn(visit);
        VisitResponse visitResponse = new VisitResponse(visit.getUuid());
        when(visitResponseMapper.map(visit)).thenReturn(visitResponse);

        VisitResponse visitResponseFromService = emrVisitService.find(visitRequest);

        assertEquals(visitResponse, visitResponseFromService);
        verify(visitService).getVisitByUuid(visitRequest.getVisitUuid());
    }

    @Test(expected = VisitNotFoundException.class)
    public void shouldRaiseExceptionForNonExistingVisit() throws Exception {
        VisitRequest visitRequest = new VisitRequest(UUID.randomUUID().toString());
        when(visitService.getVisitByUuid(visitRequest.getVisitUuid())).thenReturn(null);

        emrVisitService.find(visitRequest);
    }
}
