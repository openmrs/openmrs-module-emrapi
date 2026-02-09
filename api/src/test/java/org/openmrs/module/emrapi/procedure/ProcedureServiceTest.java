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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProcedureServiceImpl}.
 */
public class ProcedureServiceTest {

    private ProcedureServiceImpl procedureService;

    private ProcedureDAO procedureDAO;

    @Before
    public void setUp() {
        procedureDAO = mock(ProcedureDAO.class);
        procedureService = new ProcedureServiceImpl();
        procedureService.setProcedureDAO(procedureDAO);
    }

    @Test
    public void getProcedureByUuid_shouldReturnProcedureFromDAO() {
        String uuid = "test-uuid-123";
        Procedure expectedProcedure = new Procedure();
        expectedProcedure.setUuid(uuid);

        when(procedureDAO.getByUuid(uuid)).thenReturn(expectedProcedure);

        Procedure result = procedureService.getProcedureByUuid(uuid);

        assertEquals(expectedProcedure, result);
        verify(procedureDAO).getByUuid(uuid);
    }
}
