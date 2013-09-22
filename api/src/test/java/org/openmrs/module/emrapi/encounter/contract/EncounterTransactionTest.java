package org.openmrs.module.emrapi.encounter.contract;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class EncounterTransactionTest {

    @Test
    public void dateConversion() {
        EncounterTransaction encounterTransaction = new EncounterTransaction();
        encounterTransaction.setEncounterDateTime(new Date(2013, 10, 30, 0, 0, 0));
        assertEquals(new Date(2013, 10, 30, 0, 0, 0), encounterTransaction.getEncounterDateTime());
    }
}
