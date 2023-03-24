package org.openmrs.module.emrapi.conditionslist.contract;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This attempts to match the behavior in the rest webservices ConversionUtil method for dates
 */
public class CustomDateDeserializer extends JsonDeserializer<Date> {

    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private static final Log log = LogFactory.getLog(CustomDateDeserializer.class);

    public static final List<String> SUPPORTED_FORMATS = Arrays.asList(
            ISO_DATE_FORMAT,
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
    );

    @Override
    public Date deserialize(JsonParser jsonparser, DeserializationContext context) throws IOException {
        String dateString = jsonparser.getText();
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
}