package org.openmrs.module.emrapi;

import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.io.File;

public abstract class BaseReportingTest extends BaseModuleContextSensitiveTest {

    public BaseReportingTest() {
        super();
        {
            Module mod = new Module("", "reporting", "", "", "", "1.25.0");
            mod.setFile(new File(""));
            ModuleFactory.getStartedModulesMap().put(mod.getModuleId(), mod);
        }
    }
}
