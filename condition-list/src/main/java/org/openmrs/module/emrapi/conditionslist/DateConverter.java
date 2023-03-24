package org.openmrs.module.emrapi.conditionslist;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This converter mimics the behavior in the rest webservices ConversionUtil method for dates
 * This ensures that the system timezone is used to interpret dates unless UTC is explicitly indicated
 * This is implemented as a
 */
public class DateConverter {

    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private static final Log log = LogFactory.getLog(DateConverter.class);

    public static final List<String> SUPPORTED_FORMATS = Arrays.asList(
            ISO_DATE_FORMAT,
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
    );

    /**
     * @return the given dateString parsed into a Date, defaulting to using the system timezone
     */
    public static Date deserialize(String dateString) {
        if (StringUtils.isBlank(dateString)) {
            return null;
        }
        for (String dateFormat : SUPPORTED_FORMATS) {
            try {
                return DateTime.parse(dateString, DateTimeFormat.forPattern(dateFormat)).toDate();
            }
            catch (Exception e) {
                if (log.isTraceEnabled()) {
                    log.trace("Unable to parse '" + dateString + "' using format " + dateFormat, e);
                }
            }
        }
        throw new RuntimeException("Unable to parse '" + dateString + "' using any of: " + SUPPORTED_FORMATS);
    }

    /**
     * @return the given date, serialized in ISO format
     */
    public static String serialize(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(ISO_DATE_FORMAT).format(date);
    }
}