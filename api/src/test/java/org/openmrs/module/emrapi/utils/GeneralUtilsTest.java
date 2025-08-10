package org.openmrs.module.emrapi.utils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.util.OpenmrsConstants;

import java.util.Date;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class GeneralUtilsTest  {

    private MockedStatic<Context> mockedContext;

    @Before
    public void setup() {
        mockedContext = Mockito.mockStatic(Context.class);
    }

    @After
    public void tearDown() {
        mockedContext.close();
    }

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

    /**
     * @verifies return a list of the patients last viewed by the specified user
     * @see GeneralUtils#getLastViewedPatients(org.openmrs.User)
     */
    @Test
    public void getLastViewedPatients_shouldReturnAListOfThePatientsLastViewedByTheSpecifiedUser() throws Exception {
        User user = new User(1);
        user.setUserProperty(EmrApiConstants.USER_PROPERTY_NAME_LAST_VIEWED_PATIENT_IDS, "2,6,7");
        AdministrationService as = mock(AdministrationService.class);
        PatientService ps = mock(PatientService.class);
        UserService us = mock(UserService.class);
        mockedContext.when(Context::getAdministrationService).thenReturn(as);
        mockedContext.when(Context::getPatientService).thenReturn(ps);
        mockedContext.when(Context::getUserService).thenReturn(us);
        when(as.getGlobalProperty(Mockito.eq(EmrApiConstants.UNKNOWN_PATIENT_PERSON_ATTRIBUTE_TYPE_NAME))).thenReturn("");
        when(ps.getPatient(eq(2))).thenReturn(new Patient(2));
        when(ps.getPatient(eq(6))).thenReturn(new Patient(6));
        when(ps.getPatient(eq(7))).thenReturn(new Patient(7));
        when(us.getUser(eq(user.getId()))).thenReturn(user);

        List<Patient> lastViewed = GeneralUtils.getLastViewedPatients(user);
        Assert.assertEquals(7, lastViewed.get(0).getId().intValue());
        Assert.assertEquals(6, lastViewed.get(1).getId().intValue());
        Assert.assertEquals(2, lastViewed.get(2).getId().intValue());
    }

    /**
     * @verifies not return voided patients
     * @see GeneralUtils#getLastViewedPatients(org.openmrs.User)
     */
    @Test
    public void getLastViewedPatients_shouldNotReturnVoidedPatients() throws Exception {
        Patient voided = new Patient(999);
        voided.setVoided(true);
        User user = new User(1);
        user.setUserProperty(EmrApiConstants.USER_PROPERTY_NAME_LAST_VIEWED_PATIENT_IDS, "2,999,3");
        AdministrationService as = mock(AdministrationService.class);
        PatientService ps = mock(PatientService.class);
        UserService us = mock(UserService.class);
        mockedContext.when(Context::getAdministrationService).thenReturn(as);
        mockedContext.when(Context::getPatientService).thenReturn(ps);
        mockedContext.when(Context::getUserService).thenReturn(us);
        when(as.getGlobalProperty(Mockito.eq(EmrApiConstants.UNKNOWN_PATIENT_PERSON_ATTRIBUTE_TYPE_NAME))).thenReturn("");
        when(ps.getPatient(eq(2))).thenReturn(new Patient(2));
        when(ps.getPatient(eq(3))).thenReturn(new Patient(3));
        when(ps.getPatient(eq(999))).thenReturn(voided);
        when(us.getUser(eq(user.getId()))).thenReturn(user);

        List<Patient> lastViewed = GeneralUtils.getLastViewedPatients(user);
        Assert.assertEquals(2, lastViewed.size());
        Assert.assertEquals(3, lastViewed.get(0).getId().intValue());
        Assert.assertEquals(2, lastViewed.get(1).getId().intValue());
    }

}
