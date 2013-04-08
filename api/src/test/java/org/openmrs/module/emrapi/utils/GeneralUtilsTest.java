package org.openmrs.module.emrapi.utils;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.util.OpenmrsConstants;

import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GeneralUtilsTest {

    @Test
    public void shouldGetDefaultLocaleForUser() {
        User user = new User();
        user.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE, "ht");
        Assert.assertEquals("ht", GeneralUtils.getDefaultLocale(user).toString());
    }

    @Test
    public void setPropertyIfDifferent_shouldChangePropertyWhenDifferent() throws Exception {
        Date date = new Date();
        Patient p = new Patient();

        boolean changed = GeneralUtils.setPropertyIfDifferent(p, "birthdate", date);
        assertThat(changed, is(true));
        assertThat(p.getBirthdate(), is(date));
    }

    @Test
    public void setPropertyIfDifferent_shouldNotChangePropertyWhenAlreadySet() throws Exception {
        Date date = new Date();
        Patient p = new Patient();
        p.setBirthdate(date);

        boolean changed = GeneralUtils.setPropertyIfDifferent(p, "birthdate", date);
        assertThat(changed, is(false));
        assertThat(p.getBirthdate(), is(date));
    }

}
