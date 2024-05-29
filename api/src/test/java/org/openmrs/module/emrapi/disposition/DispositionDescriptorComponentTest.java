package org.openmrs.module.emrapi.disposition;

import org.junit.Test;
import org.openmrs.module.emrapi.EmrApiContextSensitiveTest;

public class DispositionDescriptorComponentTest extends EmrApiContextSensitiveTest {

    @Test
    public void setupNewDispositionDescriptor() {
        new DispositionDescriptor();

    }

}
