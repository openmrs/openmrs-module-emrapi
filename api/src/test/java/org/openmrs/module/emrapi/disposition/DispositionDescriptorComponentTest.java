package org.openmrs.module.emrapi.disposition;


import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

public class DispositionDescriptorComponentTest extends BaseModuleContextSensitiveTest {

    @Test
    public void setupNewDispositionDescriptor() {
        new DispositionDescriptor();

    }

}
