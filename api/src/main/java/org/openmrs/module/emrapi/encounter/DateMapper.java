package org.openmrs.module.emrapi.encounter;

import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateMapper {
    public Date toDate(String date) {
        if (!StringUtils.isBlank(date)) {
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                simpleDateFormat.setLenient(false);
                return simpleDateFormat.parse(date);
            } catch (ParseException e) {
                throw new RuntimeException("Date format needs to be 'yyyy-MM-dd'. Incorrect Date:" + date + ".", e);
            }
        }
        return new Date(0);
    }
}
