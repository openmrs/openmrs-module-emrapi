package org.openmrs.module.emrapi.disposition.actions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.reporting.common.DateUtil;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class MarkPatientDeadDispositionActionTest {

    private MarkPatientDeadDispositionAction action;
    private EmrApiProperties emrApiProperties;
    private PatientService patientService;

    @Before
    public void setUp() throws Exception {
        emrApiProperties = mock(EmrApiProperties.class);

        patientService = mock(PatientService.class);
        when(patientService.savePatient(any(Patient.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
            return invocation.getArguments()[0];
            }
        });

        action = new MarkPatientDeadDispositionAction();
        action.setEmrApiProperties(emrApiProperties);
        action.setPatientService(patientService);
    }

    @Test
    public void testAction() throws Exception {
        Concept unknown = new Concept();
        when(emrApiProperties.getUnknownCauseOfDeathConcept()).thenReturn(unknown);

        String expectedDeathDate = "2013-05-04";

        Map<String, String[]> request = new HashMap<String, String[]>();
        request.put("deathDate", new String[]{expectedDeathDate});
        request.put("something", new String[]{"unrelated"});

        Patient patient = new Patient();

        Encounter encounter = new Encounter();
        encounter.setPatient(patient);

        action.action(new EncounterDomainWrapper(encounter), new Obs(), request);

        assertThat(patient.isDead(), is(true));
        assertThat(patient.getDeathDate(), is(DateUtil.parseDate(expectedDeathDate, "yyyy-MM-dd")));
        assertThat(patient.getCauseOfDeath(), is(unknown));
        verify(patientService).savePatient(patient);
    }

}
