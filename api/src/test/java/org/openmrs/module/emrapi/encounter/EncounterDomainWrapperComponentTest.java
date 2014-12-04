package org.openmrs.module.emrapi.encounter;

import org.junit.Test;
import org.openmrs.module.emrapi.account.AccountDomainWrapper;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class EncounterDomainWrapperComponentTest extends BaseModuleContextSensitiveTest{

    @Autowired
    private DomainWrapperFactory factory;

    @Test
    public void testThatBeanCanHavePropertiesAutowired() throws Exception {
        EncounterDomainWrapper encounterDomainWrapper = factory.newEncounterDomainWrapper();
        // currently no beans are actually wired in--adding this so we remember to test it later
    }

}
