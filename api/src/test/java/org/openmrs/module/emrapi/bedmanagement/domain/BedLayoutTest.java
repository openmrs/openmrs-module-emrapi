package org.openmrs.module.emrapi.bedmanagement.domain;

import org.junit.Test;
import org.openmrs.module.emrapi.bedmanagement.BedLayout;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BedLayoutTest {
    @Test
    public void should_be_able_to_return_bed_status() {
        BedLayout bedLayout = new BedLayout();
        bedLayout.setBedPatientAssignmentId(123);
        bedLayout.setBedId(1);
        assertFalse(bedLayout.isAvailable());
    }

    @Test
    public void should_be_able_to_return_bed_status_for_empty_space() {
        BedLayout bedLayout = new BedLayout();
        bedLayout.setBedPatientAssignmentId(123);
        assertFalse(bedLayout.isAvailable());
    }

    @Test
    public void should_be_able_to_return_status_correctly_for_unassigned_bed() {
        BedLayout bedLayout = new BedLayout();
        bedLayout.setBedId(1);
        assertTrue(bedLayout.isAvailable());
    }
}
