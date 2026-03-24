package org.openmrs.module.emrapi.procedure;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openmrs.api.APIException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openmrs.module.emrapi.procedure.ProcedureUtil.getDateTimeFromEstimatedDate;

public class ProcedureUtilTest {
	
	@Nested
	class GetDateTimeFromEstimatedDate {
		
		private Date toDate(LocalDateTime ldt) {
			return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		}
		
		@Test
		void shouldParseFullDatetime() {
			Date result = getDateTimeFromEstimatedDate("2025-06-15T14:30:00");
			assertEquals(toDate(LocalDateTime.of(2025, 6, 15, 14, 30, 0)), result);
		}
		
		@Test
		void shouldParseDatetimeWithSubSeconds() {
			Date result = getDateTimeFromEstimatedDate("2025-06-15T14:30:45.123");
			assertEquals(toDate(LocalDateTime.of(2025, 6, 15, 14, 30, 45, 123000000)), result);
		}
		
		@Test
		void shouldParseFullDate() {
			Date result = getDateTimeFromEstimatedDate("2025-06-15");
			assertEquals(toDate(LocalDate.of(2025, 6, 15).atStartOfDay()), result);
		}
		
		@Test
		void shouldParseYearMonth() {
			Date result = getDateTimeFromEstimatedDate("2025-06");
			assertEquals(toDate(LocalDate.of(2025, 6, 1).atStartOfDay()), result);
		}
		
		@Test
		void shouldParseYearOnly() {
			Date result = getDateTimeFromEstimatedDate("2025");
			assertEquals(toDate(LocalDate.of(2025, 1, 1).atStartOfDay()), result);
		}
		
		@Test
		void shouldParseLeapYearDate() {
			Date result = getDateTimeFromEstimatedDate("2024-02-29");
			assertEquals(toDate(LocalDate.of(2024, 2, 29).atStartOfDay()), result);
		}
		
		@Test
		void shouldThrowForInvalidLeapYearDate() {
			assertThrows(APIException.class, () ->
					getDateTimeFromEstimatedDate("2025-02-29"));
		}
		
		@Test
		void shouldThrowForMalformedFullDate() {
			assertThrows(APIException.class, () ->
					getDateTimeFromEstimatedDate("2025-13-40"));
		}
		
		@Test
		void shouldThrowForMalformedYearMonth() {
			assertThrows(APIException.class, () ->
					getDateTimeFromEstimatedDate("2025-13"));
		}
		
		@Test
		void shouldThrowForMalformedYear() {
			assertThrows(APIException.class, () ->
					getDateTimeFromEstimatedDate("abcd"));
		}
		
		@Test
		void shouldThrowForMalformedDatetime() {
			assertThrows(APIException.class, () ->
					getDateTimeFromEstimatedDate("2025-06-15Tnotadate"));
		}
		
		@Test
		void shouldThrowForUnrecognizedLength() {
			assertThrows(APIException.class, () ->
					getDateTimeFromEstimatedDate("25-06"));
		}
		
		@Test
		void shouldThrowForGarbageInputWithLength10() {
			assertThrows(APIException.class, () ->
					getDateTimeFromEstimatedDate("abcdefghij"));
		}
	}
	
}
