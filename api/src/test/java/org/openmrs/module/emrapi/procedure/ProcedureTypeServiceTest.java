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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProcedureTypeServiceImpl}.
 */
class ProcedureTypeServiceTest {

	private ProcedureTypeServiceImpl service;
	private ProcedureTypeDAO procedureTypeDAO;

	@BeforeEach
	void setUp() {
		procedureTypeDAO = mock(ProcedureTypeDAO.class);
		service = new ProcedureTypeServiceImpl();
		service.setProcedureTypeDAO(procedureTypeDAO);
	}

	@Nested
	class SaveProcedureType {

		@Test
		void shouldDelegateToDAO() {
			ProcedureType type = new ProcedureType("Test", "Test type");
			when(procedureTypeDAO.saveOrUpdate(type)).thenReturn(type);

			ProcedureType result = service.saveProcedureType(type);

			assertEquals(type, result);
			verify(procedureTypeDAO).saveOrUpdate(type);
		}
	}

	@Nested
	class GetProcedureTypeByUuid {

		@Test
		void shouldDelegateToDAO() {
			String uuid = "test-uuid";
			ProcedureType expected = new ProcedureType("Test", "Test type");
			when(procedureTypeDAO.getByUuid(uuid)).thenReturn(expected);

			ProcedureType result = service.getProcedureTypeByUuid(uuid);

			assertEquals(expected, result);
			verify(procedureTypeDAO).getByUuid(uuid);
		}
	}

	@Nested
	class GetAllProcedureTypes {

		@Test
		void shouldDelegateToDAOWithIncludeRetiredFalse() {
			List<ProcedureType> expected = Arrays.asList(new ProcedureType("A", "a"), new ProcedureType("B", "b"));
			when(procedureTypeDAO.getAll(false)).thenReturn(expected);

			List<ProcedureType> result = service.getAllProcedureTypes(false);

			assertEquals(expected, result);
			verify(procedureTypeDAO).getAll(false);
		}

		@Test
		void shouldDelegateToDAOWithIncludeRetiredTrue() {
			List<ProcedureType> expected = Arrays.asList(new ProcedureType("A", "a"));
			when(procedureTypeDAO.getAll(true)).thenReturn(expected);

			List<ProcedureType> result = service.getAllProcedureTypes(true);

			assertEquals(expected, result);
			verify(procedureTypeDAO).getAll(true);
		}
	}

	@Nested
	class RetireProcedureType {

		@Test
		void shouldSetRetiredFieldsAndDelegateToDAO() {
			ProcedureType type = new ProcedureType("Test", "Test type");
			when(procedureTypeDAO.saveOrUpdate(any(ProcedureType.class))).thenAnswer(i -> i.getArgument(0));

			ProcedureType result = service.retireProcedureType(type, "no longer needed");

			assertTrue(result.getRetired());
			assertEquals("no longer needed", result.getRetireReason());
			verify(procedureTypeDAO).saveOrUpdate(type);
		}
	}

	@Nested
	class UnretireProcedureType {

		@Test
		void shouldClearRetiredFields() {
			ProcedureType type = new ProcedureType("Test", "Test type");
			type.setRetired(true);
			type.setRetireReason("some reason");
			type.setDateRetired(new Date());
			when(procedureTypeDAO.saveOrUpdate(any(ProcedureType.class))).thenAnswer(i -> i.getArgument(0));

			ProcedureType result = service.unretireProcedureType(type);

			assertFalse(result.getRetired());
			assertNull(result.getRetireReason());
			assertNull(result.getDateRetired());
			assertNull(result.getRetiredBy());
		}

		@Test
		void shouldDelegateToDAO() {
			ProcedureType type = new ProcedureType("Test", "Test type");
			type.setRetired(true);
			when(procedureTypeDAO.saveOrUpdate(any(ProcedureType.class))).thenReturn(type);

			service.unretireProcedureType(type);

			verify(procedureTypeDAO).saveOrUpdate(type);
		}
	}

	@Nested
	class GetProcedureTypeById {

		@Test
		void shouldDelegateToDAO() {
			ProcedureType expected = new ProcedureType();
			when(procedureTypeDAO.getById(1)).thenReturn(expected);

			ProcedureType result = service.getProcedureTypeById(1);

			assertEquals(expected, result);
			verify(procedureTypeDAO).getById(1);
		}

		@Test
		void shouldReturnNullWhenNotFound() {
			when(procedureTypeDAO.getById(999)).thenReturn(null);
			assertNull(service.getProcedureTypeById(999));
		}
	}

	@Nested
	class GetProcedureTypeByName {

		@Test
		void shouldDelegateToDAO() {
			ProcedureType expected = new ProcedureType();
			when(procedureTypeDAO.getByName("Historical")).thenReturn(expected);

			ProcedureType result = service.getProcedureTypeByName("Historical");

			assertEquals(expected, result);
			verify(procedureTypeDAO).getByName("Historical");
		}

		@Test
		void shouldReturnNullWhenNotFound() {
			when(procedureTypeDAO.getByName("Unknown")).thenReturn(null);
			assertNull(service.getProcedureTypeByName("Unknown"));
		}
	}

	@Nested
	class PurgeProcedureType {

		@Test
		void shouldDelegateDeleteToDAO() {
			ProcedureType type = new ProcedureType("Test", "Test type");

			service.purgeProcedureType(type);

			verify(procedureTypeDAO).delete(type);
		}
	}
}
