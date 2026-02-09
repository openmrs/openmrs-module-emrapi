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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.APIException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    }
}
