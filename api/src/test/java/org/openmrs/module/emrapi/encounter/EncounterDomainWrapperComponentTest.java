package org.openmrs.module.emrapi.encounter;

import org.junit.Test;
import org.openmrs.module.emrapi.EmrApiContextSensitiveTest;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class EncounterDomainWrapperComponentTest extends EmrApiContextSensitiveTest {

    @Autowired
    private DomainWrapperFactory factory;

    @Test
    public void testThatBeanCanHavePropertiesAutowired() throws Exception {
        EncounterDomainWrapper encounterDomainWrapper = factory.newEncounterDomainWrapper();
        // currently no beans are actually wired in--adding this so we remember to test it later
    }

}
