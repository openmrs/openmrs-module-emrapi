package org.openmrs.module.emrapi.bedmanagement.domain;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.emrapi.bedmanagement.BedLayout;

public class BedLayoutTest {
    @Test
    public void should_be_able_to_return_bed_status() {
        BedLayout bedLayout = new BedLayout();
        bedLayout.setBedPatientAssignmentId(123);
        Assert.assertTrue(bedLayout.isAvailable());
    }
}
