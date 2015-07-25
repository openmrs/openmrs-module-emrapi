package org.openmrs.module.emrapi.encounter;



import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertNull;

public class DateMapperTest {

    DateMapper dateMapper = new DateMapper();

    @Test
    public void shouldReturnNullIfDateStringIsNull() throws Exception {
        Date date = dateMapper.toDate(null);
        assertNull(date);
    }
}