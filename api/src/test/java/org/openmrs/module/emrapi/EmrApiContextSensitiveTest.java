package org.openmrs.module.emrapi;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.io.File;

public abstract class EmrApiContextSensitiveTest extends BaseModuleContextSensitiveTest {

    @BeforeClass
    public static void beforeClass() {
        Module mod = new Module("", "providermanagement", "", "", "", "2.5.0");
        mod.setFile(new File(""));
        ModuleFactory.getStartedModulesMap().put(mod.getModuleId(), mod);
    }

    @AfterClass
    public static void afterClass() {
        ModuleFactory.getStartedModulesMap().remove("providermanagement");
    }
}
