package org.openmrs.module.emrapi.account;

import org.junit.jupiter.api.Test;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

public class AccountDomainWrapperComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private DomainWrapperFactory factory;

    @Test
    public void testThatBeanCanHavePropertiesAutowired() throws Exception {
        AccountDomainWrapper accountDomainWrapper = factory.newAccountDomainWrapper();
        assertThat(accountDomainWrapper.accountService, notNullValue());
        assertThat(accountDomainWrapper.personService, notNullValue());
        assertThat(accountDomainWrapper.providerService, notNullValue());
        assertThat(accountDomainWrapper.userService, notNullValue());
    }
}
