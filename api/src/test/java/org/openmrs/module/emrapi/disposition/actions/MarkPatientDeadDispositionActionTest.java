package org.openmrs.module.emrapi.disposition.actions;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.emrapi.test.AuthenticatedUserTestHelper;

import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class MarkPatientDeadDispositionActionTest extends AuthenticatedUserTestHelper {

    private MarkPatientDeadDispositionAction action;
    private PatientService patientService;
    private DispositionService dispositionService;
    private DispositionDescriptor dispositionDescriptor;
    private EmrApiProperties emrApiProperties;
    private Concept dispositionObsGroupConcept = new Concept();
    private Concept dateOfDeathConcept = new Concept();

    @Before
    public void setUp() throws Exception {
        dispositionService = mock(DispositionService.class);
        dispositionDescriptor = mock(DispositionDescriptor.class);
        emrApiProperties = mock(EmrApiProperties.class);

        when(dispositionService.getDispositionDescriptor()).thenReturn(dispositionDescriptor);

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
        action.setDispositionService(dispositionService);
    }

    @Test
    public void testActionShouldMarkPatientAsDead() throws Exception {
        Concept unknown = new Concept();
        when(emrApiProperties.getUnknownCauseOfDeathConcept()).thenReturn(unknown);

        Date expectedDeathDate = (new DateTime(2013, 05, 10, 20, 26)).toDate();

        Patient patient = new Patient();

        final Visit visit = new Visit();
        final Encounter encounter = new Encounter();
        final Date encounterDate = (new DateTime(2013, 05, 13, 20, 26)).toDate();
        encounter.setVisit(visit);
        encounter.addProvider(new EncounterRole(), new Provider());
        encounter.setEncounterDatetime(encounterDate);
        encounter.setPatient(patient);

        final Obs dispositionObsGroup = new Obs();
        dispositionObsGroup.setConcept(dispositionObsGroupConcept);
        encounter.addObs(dispositionObsGroup);

        when(dispositionDescriptor.getDateOfDeath(dispositionObsGroup)).thenReturn(expectedDeathDate);

        action.action(new EncounterDomainWrapper(encounter), dispositionObsGroup, null);

        assertThat(patient.isDead(), is(true));
        assertThat(patient.getDeathDate(), is(expectedDeathDate));
        assertThat(patient.getCauseOfDeath(), is(unknown));
        verify(patientService).savePatient(patient);
    }

}
