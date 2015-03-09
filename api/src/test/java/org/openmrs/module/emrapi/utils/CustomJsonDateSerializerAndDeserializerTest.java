package org.openmrs.module.emrapi.utils;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.impl.WriterBasedGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class CustomJsonDateSerializerAndDeserializerTest {
    @Test
    public void serializeAndDeserializeDateInExpectedFormat() throws Exception {
        DateSerializeExample dateSerializeExample = new DateSerializeExample();
        dateSerializeExample.setExampleDate(new Date());

        String jsonString = new ObjectMapper().writeValueAsString(dateSerializeExample);

        DateSerializeExample deserializedData = new ObjectMapper().readValue(jsonString, DateSerializeExample.class);

        assertEquals(dateSerializeExample, deserializedData);
    }
}

class DateSerializeExample {
    private Date exampleDate;

    @JsonSerialize(using = CustomJsonDateSerializer.class)
    public Date getExampleDate() {
        return exampleDate;
    }

    @JsonDeserialize(using = CustomJsonDateDeserializer.class)
    public void setExampleDate(Date exampleDate) {
        this.exampleDate = exampleDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateSerializeExample that = (DateSerializeExample) o;

        return exampleDate.equals(that.exampleDate);

    }

    @Override
    public int hashCode() {
        return exampleDate.hashCode();
    }
}
