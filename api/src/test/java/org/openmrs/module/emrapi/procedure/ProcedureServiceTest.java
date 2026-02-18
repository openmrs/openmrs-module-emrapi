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
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.APIException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

        when(procedureDAO.getByUuid(uuid)).thenReturn(expectedProcedure);

        Procedure result = procedureService.getProcedureByUuid(uuid);

        assertEquals(expectedProcedure, result);
        verify(procedureDAO).getByUuid(uuid);
    }

    @Nested
    class GetDateTimeFromEstimatedDate {

        private Date toDate(LocalDateTime ldt) {
            return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        }

        @Test
        void shouldParseFullDatetime() {
            Date result = procedureService.getDateTimeFromEstimatedDate("2025-06-15T14:30:00");
            assertEquals(toDate(LocalDateTime.of(2025, 6, 15, 14, 30, 0)), result);
        }

        @Test
        void shouldParseDatetimeWithSubSeconds() {
            Date result = procedureService.getDateTimeFromEstimatedDate("2025-06-15T14:30:45.123");
            assertEquals(toDate(LocalDateTime.of(2025, 6, 15, 14, 30, 45, 123000000)), result);
        }

        @Test
        void shouldParseFullDate() {
            Date result = procedureService.getDateTimeFromEstimatedDate("2025-06-15");
            assertEquals(toDate(LocalDate.of(2025, 6, 15).atStartOfDay()), result);
        }

        @Test
        void shouldParseYearMonth() {
            Date result = procedureService.getDateTimeFromEstimatedDate("2025-06");
            assertEquals(toDate(LocalDate.of(2025, 6, 1).atStartOfDay()), result);
        }

        @Test
        void shouldParseYearOnly() {
            Date result = procedureService.getDateTimeFromEstimatedDate("2025");
            assertEquals(toDate(LocalDate.of(2025, 1, 1).atStartOfDay()), result);
        }

        @Test
        void shouldParseLeapYearDate() {
            Date result = procedureService.getDateTimeFromEstimatedDate("2024-02-29");
            assertEquals(toDate(LocalDate.of(2024, 2, 29).atStartOfDay()), result);
        }

        @Test
        void shouldThrowForInvalidLeapYearDate() {
            assertThrows(APIException.class, () ->
                    procedureService.getDateTimeFromEstimatedDate("2025-02-29"));
        }

        @Test
        void shouldThrowForMalformedFullDate() {
            assertThrows(APIException.class, () ->
                    procedureService.getDateTimeFromEstimatedDate("2025-13-40"));
        }

        @Test
        void shouldThrowForMalformedYearMonth() {
            assertThrows(APIException.class, () ->
                    procedureService.getDateTimeFromEstimatedDate("2025-13"));
        }

        @Test
        void shouldThrowForMalformedYear() {
            assertThrows(APIException.class, () ->
                    procedureService.getDateTimeFromEstimatedDate("abcd"));
        }

        @Test
        void shouldThrowForMalformedDatetime() {
            assertThrows(APIException.class, () ->
                    procedureService.getDateTimeFromEstimatedDate("2025-06-15Tnotadate"));
        }

        @Test
        void shouldThrowForUnrecognizedLength() {
            assertThrows(APIException.class, () ->
                    procedureService.getDateTimeFromEstimatedDate("25-06"));
        }

        @Test
        void shouldThrowForGarbageInputWithLength10() {
            assertThrows(APIException.class, () ->
                    procedureService.getDateTimeFromEstimatedDate("abcdefghij"));
        }
    }

    @Nested
    class SaveProcedure {

        @BeforeEach
        void setUp() {
            when(procedureDAO.saveOrUpdate(any(Procedure.class))).thenAnswer(i -> i.getArgument(0));
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

            verify(procedureDAO).saveOrUpdate(procedure);
        }
    }

    @Nested
    class GetProceduresByPatient {

        @Test
        void shouldReturnProceduresFromDAO() {
            Patient patient = mock(Patient.class);
            List<Procedure> expected = Arrays.asList(new Procedure(), new Procedure());
            when(procedureDAO.getProceduresByPatient(patient, false)).thenReturn(expected);

            List<Procedure> result = procedureService.getProceduresByPatient(patient);

            assertEquals(expected, result);
            verify(procedureDAO).getProceduresByPatient(patient, false);
        }
    }

    @Nested
    class VoidProcedure {

        @Test
        void shouldDelegateToDAO() {
            Procedure procedure = new Procedure();
            when(procedureDAO.saveOrUpdate(procedure)).thenReturn(procedure);

            Procedure result = procedureService.voidProcedure(procedure, "test reason");

            assertEquals(procedure, result);
            verify(procedureDAO).saveOrUpdate(procedure);
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
            when(procedureDAO.saveOrUpdate(any(Procedure.class))).thenAnswer(i -> i.getArgument(0));

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
            when(procedureDAO.saveOrUpdate(any(Procedure.class))).thenReturn(procedure);

            procedureService.unvoidProcedure(procedure);

            verify(procedureDAO).saveOrUpdate(procedure);
        }
    }

    @Nested
    class PurgeProcedure {

        @Test
        void shouldDelegateDeleteToDAO() {
            Procedure procedure = new Procedure();

            procedureService.purgeProcedure(procedure);

            verify(procedureDAO).delete(procedure);
        }
    }
}
