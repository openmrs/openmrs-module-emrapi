package org.openmrs.module.emrapi.procedure;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.api.APIException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;

@Slf4j
public class ProcedureUtil {
	
	public static Date getDateTimeFromEstimatedDate(String estimatedDate) {
		try {
			// Full datetime
			if (estimatedDate.length() > 10) {
				LocalDateTime dateTime = LocalDateTime.parse(estimatedDate);
				return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
			}
			
			// yyyy-MM-dd
			if (estimatedDate.length() == 10) {
				LocalDate date = LocalDate.parse(estimatedDate);
				return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
			}
			
			// yyyy-MM
			if (estimatedDate.length() == 7) {
				YearMonth ym = YearMonth.parse(estimatedDate);
				return Date.from(
						ym.atDay(1)
								.atStartOfDay(ZoneId.systemDefault())
								.toInstant()
				);
			}
			
			// yyyy
			if (estimatedDate.length() == 4) {
				Year year = Year.parse(estimatedDate);
				return Date.from(
						year.atMonth(1)
								.atDay(1)
								.atStartOfDay(ZoneId.systemDefault())
								.toInstant()
				);
			}
			
			throw new APIException("Procedure.error.invalidEstimateDate", new Object[] { estimatedDate });
			
		}
		catch (DateTimeParseException e) {
			log.warn("Failed to parse estimated date: {}, error: {}", estimatedDate, e.getMessage());
			throw new APIException("Procedure.error.invalidEstimateDate", new Object[] { estimatedDate }, e);
		}
	}
}
