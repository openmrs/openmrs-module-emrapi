package org.openmrs.module.emrapi.account;

import org.junit.Test;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class AccountDomainWrapperComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private DomainWrapperFactory factory;

    @Test
    public void testThatBeanCanHavePropertiesAutowired() throws Exception {
        AccountDomainWrapper accountDomainWrapper = factory.newAccountDomainWrapper();
        assertThat(accountDomainWrapper.accountService, notNullValue());
        assertThat(accountDomainWrapper.personService, notNullValue());
        assertThat(accountDomainWrapper.providerManagementService, notNullValue());
        assertThat(accountDomainWrapper.providerService, notNullValue());
        assertThat(accountDomainWrapper.userService, notNullValue());
    }
}
