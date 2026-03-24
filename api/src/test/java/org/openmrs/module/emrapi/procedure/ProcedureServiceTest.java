/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under the terms
 * of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.procedure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.APIException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProcedureServiceImpl}.
 */
class ProcedureServiceTest {
	
	private ProcedureServiceImpl procedureService;
	
	private ProcedureDAO procedureDAO;
	
	@BeforeEach
	void setUp() {
		procedureDAO = mock(ProcedureDAO.class);
		procedureService = new ProcedureServiceImpl();
		procedureService.setProcedureDAO(procedureDAO);
	}
	
	@Test
	void getProcedureByUuid_shouldReturnProcedureFromDAO() {
		String uuid = "test-uuid-123";
		Procedure expectedProcedure = new Procedure();
		expectedProcedure.setUuid(uuid);
		
		when(procedureDAO.getProcedureByUuid(uuid)).thenReturn(expectedProcedure);
		
		Procedure result = procedureService.getProcedureByUuid(uuid);
		
		assertEquals(expectedProcedure, result);
		verify(procedureDAO).getProcedureByUuid(uuid);
	}
	
	
	@Nested
	class SaveProcedure {
		
		@BeforeEach
		void setUp() {
			when(procedureDAO.saveOrUpdateProcedureType(any(Procedure.class))).thenAnswer(i -> i.getArgument(0));
		}
		
		private Date toDate(LocalDateTime ldt) {
			return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		}
		
		@Test
		void shouldSetStartDateTimeFromEstimatedDate() {
			Procedure procedure = new Procedure();
			procedure.setPatient(mock(Patient.class));
			procedure.setProcedureCoded(mock(Concept.class));
			procedure.setBodySite(mock(Concept.class));
			procedure.setStatus(mock(Concept.class));
			procedure.setEstimatedStartDate("2025-06-15");
			
			Procedure saved = procedureService.saveProcedure(procedure);
			
			assertNotNull(saved.getStartDateTime());
			assertEquals(toDate(LocalDate.of(2025, 6, 15).atStartOfDay()), saved.getStartDateTime());
		}
		
		@Test
		void shouldNotOverwriteStartDateTimeWhenEstimatedDateIsNull() {
			Procedure procedure = new Procedure();
			procedure.setPatient(mock(Patient.class));
			procedure.setProcedureCoded(mock(Concept.class));
			procedure.setBodySite(mock(Concept.class));
			procedure.setStatus(mock(Concept.class));
			procedure.setStartDateTime(new Date());
			
			Date originalDate = procedure.getStartDateTime();
			Procedure saved = procedureService.saveProcedure(procedure);
			
			assertEquals(originalDate, saved.getStartDateTime());
		}
		
		@Test
		void shouldDelegateToDAO() {
			Procedure procedure = new Procedure();
			procedure.setPatient(mock(Patient.class));
			procedure.setProcedureCoded(mock(Concept.class));
			procedure.setBodySite(mock(Concept.class));
			procedure.setStatus(mock(Concept.class));
			procedure.setStartDateTime(new Date());
			
			procedureService.saveProcedure(procedure);
			
			verify(procedureDAO).saveOrUpdateProcedureType(procedure);
		}
	}
	
	@Nested
	class GetProcedureById {
		
		@Test
		void shouldDelegateToDAO() {
			Procedure expected = new Procedure();
			when(procedureDAO.getProcedure(42)).thenReturn(expected);
			
			Procedure result = procedureService.getProcedure(42);
			
			assertEquals(expected, result);
			verify(procedureDAO).getProcedure(42);
		}
		
		@Test
		void shouldReturnNullWhenDAOReturnsNull() {
			when(procedureDAO.getProcedure(999)).thenReturn(null);
			assertNull(procedureService.getProcedure(999));
		}
	}
	
	@Nested
	class GetProcedureCountByPatient {
		
		@Test
		void shouldDelegateToDAO() {
			Patient patient = mock(Patient.class);
			when(procedureDAO.getProcedureCountByPatient(patient, false)).thenReturn(3L);
			
			Long result = procedureService.getProcedureCountByPatient(patient, false);
			
			assertEquals(3L, result);
			verify(procedureDAO).getProcedureCountByPatient(patient, false);
		}
	}
	
	@Nested
	class GetProceduresByPatient {
		
		@Test
		void shouldReturnProceduresFromDAO() {
			Patient patient = mock(Patient.class);
			List<Procedure> expected = Arrays.asList(new Procedure(), new Procedure());
			when(procedureDAO.getProceduresByPatient(patient, false, null, null)).thenReturn(expected);
			
			List<Procedure> result = procedureService.getProceduresByPatient(patient, false, null, null);
			
			assertEquals(expected, result);
			verify(procedureDAO).getProceduresByPatient(patient, false, null, null);
		}
		
		@Test
		void shouldPassPaginationParamsToDAO() {
			Patient patient = mock(Patient.class);
			List<Procedure> expected = Collections.singletonList(new Procedure());
			when(procedureDAO.getProceduresByPatient(patient, false, 5, 10)).thenReturn(expected);
			
			List<Procedure> result = procedureService.getProceduresByPatient(patient, false, 5, 10);
			
			assertEquals(expected, result);
			verify(procedureDAO).getProceduresByPatient(patient, false, 5, 10);
		}
		
		@Test
		void shouldPassNullPaginationParamsToDAOWhenNotProvided() {
			Patient patient = mock(Patient.class);
			when(procedureDAO.getProceduresByPatient(patient, false, null, null)).thenReturn(Collections.emptyList());
			
			procedureService.getProceduresByPatient(patient, false, null, null);
			
			verify(procedureDAO).getProceduresByPatient(patient, false, null, null);
		}
		
		@Test
		void shouldPassIncludeVoidedToDAO() {
			Patient patient = mock(Patient.class);
			when(procedureDAO.getProceduresByPatient(patient, true, null, null)).thenReturn(Collections.emptyList());
			
			procedureService.getProceduresByPatient(patient, true, null, null);
			
			verify(procedureDAO).getProceduresByPatient(patient, true, null, null);
		}
	}
	
	@Nested
	class VoidProcedure {
		
		@Test
		void shouldDelegateToDAO() {
			Procedure procedure = new Procedure();
			when(procedureDAO.saveOrUpdateProcedureType(procedure)).thenReturn(procedure);
			
			Procedure result = procedureService.voidProcedure(procedure, "test reason");
			
			assertEquals(procedure, result);
			verify(procedureDAO).saveOrUpdateProcedureType(procedure);
		}
	}
	
	@Nested
	class UnvoidProcedure {
		
		@Test
		void shouldClearVoidFields() {
			Procedure procedure = new Procedure();
			procedure.setVoided(true);
			procedure.setVoidReason("some reason");
			procedure.setDateVoided(new Date());
			when(procedureDAO.saveOrUpdateProcedureType(any(Procedure.class))).thenAnswer(i -> i.getArgument(0));
			
			Procedure result = procedureService.unvoidProcedure(procedure);
			
			assertFalse(result.getVoided());
			assertNull(result.getVoidReason());
			assertNull(result.getDateVoided());
			assertNull(result.getVoidedBy());
		}
		
		@Test
		void shouldDelegateToDAO() {
			Procedure procedure = new Procedure();
			procedure.setVoided(true);
			when(procedureDAO.saveOrUpdateProcedureType(any(Procedure.class))).thenReturn(procedure);
			
			procedureService.unvoidProcedure(procedure);
			
			verify(procedureDAO).saveOrUpdateProcedureType(procedure);
		}
	}
	
	@Nested
	class PurgeProcedure {
		
		@Test
		void shouldDelegateDeleteToDAO() {
			Procedure procedure = new Procedure();
			
			procedureService.purgeProcedure(procedure);
			
			verify(procedureDAO).deleteProcedure(procedure);
		}
	}
	
	@Nested
	class SaveProcedureType {
		
		@Test
		void shouldDelegateToDAO() {
			ProcedureType type = new ProcedureType("Test", "Test type");
			when(procedureDAO.saveOrUpdateProcedureType(type)).thenReturn(type);
			
			ProcedureType result = procedureService.saveProcedureType(type);
			
			assertEquals(type, result);
			verify(procedureDAO).saveOrUpdateProcedureType(type);
		}
	}
	
	@Nested
	class GetProcedureTypeByUuid {
		
		@Test
		void shouldDelegateToDAO() {
			String uuid = "test-uuid";
			ProcedureType expected = new ProcedureType("Test", "Test type");
			when(procedureDAO.getProcedureTypeByUuid(uuid)).thenReturn(expected);
			
			ProcedureType result = procedureService.getProcedureTypeByUuid(uuid);
			
			assertEquals(expected, result);
			verify(procedureDAO).getProcedureTypeByUuid(uuid);
		}
	}
	
	@Nested
	class GetAllProcedureTypes {
		
		@Test
		void shouldDelegateToDAOWithIncludeRetiredFalse() {
			List<ProcedureType> expected = Arrays.asList(new ProcedureType("A", "a"), new ProcedureType("B", "b"));
			when(procedureDAO.getAllProcedureTypes(false)).thenReturn(expected);
			
			List<ProcedureType> result = procedureService.getAllProcedureTypes(false);
			
			assertEquals(expected, result);
			verify(procedureDAO).getAllProcedureTypes(false);
		}
		
		@Test
		void shouldDelegateToDAOWithIncludeRetiredTrue() {
			List<ProcedureType> expected = Arrays.asList(new ProcedureType("A", "a"));
			when(procedureDAO.getAllProcedureTypes(true)).thenReturn(expected);
			
			List<ProcedureType> result = procedureService.getAllProcedureTypes(true);
			
			assertEquals(expected, result);
			verify(procedureDAO).getAllProcedureTypes(true);
		}
	}
	
	@Nested
	class RetireProcedureType {
		
		@Test
		void shouldSetRetiredFieldsAndDelegateToDAO() {
			ProcedureType type = new ProcedureType("Test", "Test type");
			when(procedureDAO.saveOrUpdateProcedureType(any(ProcedureType.class))).thenAnswer(i -> i.getArgument(0));
			
			ProcedureType result = procedureService.retireProcedureType(type, "no longer needed");
			
			assertTrue(result.getRetired());
			assertEquals("no longer needed", result.getRetireReason());
			verify(procedureDAO).saveOrUpdateProcedureType(type);
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
			when(procedureDAO.saveOrUpdateProcedureType(any(ProcedureType.class))).thenAnswer(i -> i.getArgument(0));
			
			ProcedureType result = procedureService.unretireProcedureType(type);
			
			assertFalse(result.getRetired());
			assertNull(result.getRetireReason());
			assertNull(result.getDateRetired());
			assertNull(result.getRetiredBy());
		}
		
		@Test
		void shouldDelegateToDAO() {
			ProcedureType type = new ProcedureType("Test", "Test type");
			type.setRetired(true);
			when(procedureDAO.saveOrUpdateProcedureType(any(ProcedureType.class))).thenReturn(type);
			
			procedureService.unretireProcedureType(type);
			
			verify(procedureDAO).saveOrUpdateProcedureType(type);
		}
	}
	
	@Nested
	class GetProcedureTypeById {
		
		@Test
		void shouldDelegateToDAO() {
			ProcedureType expected = new ProcedureType();
			when(procedureDAO.getProcedureType(1)).thenReturn(expected);
			
			ProcedureType result = procedureService.getProcedureType(1);
			
			assertEquals(expected, result);
			verify(procedureDAO).getProcedureType(1);
		}
		
		@Test
		void shouldReturnNullWhenNotFound() {
			when(procedureDAO.getProcedureType(999)).thenReturn(null);
			assertNull(procedureService.getProcedureType(999));
		}
	}
	
	@Nested
	class GetProcedureTypesByName {
		
		@Test
		void shouldDelegateToDAO() {
			List<ProcedureType> expected = Collections.singletonList(new ProcedureType());
			when(procedureDAO.getProcedureTypesByName("Historical")).thenReturn(expected);
			
			List<ProcedureType> result = procedureService.getProcedureTypesByName("Historical");
			
			assertEquals(expected, result);
			verify(procedureDAO).getProcedureTypesByName("Historical");
		}
	}
	
	@Nested
	class PurgeProcedureType {
		
		@Test
		void shouldDelegateDeleteToDAO() {
			ProcedureType type = new ProcedureType("Test", "Test type");
			
			procedureService.purgeProcedureType(type);
			
			verify(procedureDAO).deleteProcedureType(type);
		}
	}
}
