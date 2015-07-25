package org.openmrs.module.emrapi.encounter;


import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertNull;

public class DateMapperTest {

    @Test
    public void shouldReturnNullIfDateStringIsNull() throws Exception {
        Date actualDate = new DateMapper().toDate(null);
        assertNull(actualDate);

    }
}