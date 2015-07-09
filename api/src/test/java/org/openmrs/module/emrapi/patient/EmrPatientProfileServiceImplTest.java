package org.openmrs.module.emrapi.patient;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.module.emrapi.person.image.EmrPersonImageService;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmrPatientProfileServiceImplTest extends BaseModuleContextSensitiveTest {

    private EmrPatientProfileServiceImpl emrPatientProfileService;

    private PatientService patientService;

    private PersonService personService;

    private EmrPersonImageService emrPersonImageService;

    @Before
    public void setup() {
        emrPatientProfileService = new EmrPatientProfileServiceImpl();
        patientService = mock(PatientService.class);
        personService = mock(PersonService.class);
        emrPersonImageService = mock(EmrPersonImageService.class);

        emrPatientProfileService.setPatientService(patientService);
        emrPatientProfileService.setPersonService(personService);
        emrPatientProfileService.setEmrPersonImageService(emrPersonImageService);

        when(patientService.getPatientByUuid("patient_uuid")).thenReturn(constructPatient());
        Person person = constructPerson();
        when(personService.getPerson(1)).thenReturn(person);
        when(personService.getRelationshipsByPerson(person)).thenReturn(constructRelationships());
    }

    @Test
    public void shouldSavePatientProfile() throws Exception {
        PatientProfile patientProfile = emrPatientProfileService.save(constructPatientProfile());
        assertNotNull(patientProfile);
        assertEquals(patientProfile.getRelationships().size(), 1);
    }

    @Test
    public void shouldGetPatientProfile() throws Exception {
        PatientProfile patientProfile = emrPatientProfileService.get("patient_uuid");
        assertNotNull(patientProfile.getPatient());
        assertEquals(patientProfile.getPatient().getUuid(), "patient_uuid");
        assertEquals(patientProfile.getRelationships().size(), 1);
    }

    private PatientProfile constructPatientProfile() {
        PatientProfile patientProfile = new PatientProfile();
        List<Relationship> relationships = new ArrayList<Relationship>();
        relationships.add(new Relationship());
        patientProfile.setRelationships(relationships);

        return patientProfile;
    }

    private Patient constructPatient() {
        Patient patient = new Patient();
        patient.setUuid("patient_uuid");
        patient.setPersonId(1);

        return patient;
    }

    private Person constructPerson() {
        Person person = new Person();
        person.setId(1);
        person.setUuid("person_uuid");

        return person;
    }

    private List<Relationship> constructRelationships() {
        List<Relationship> relationships = new ArrayList<Relationship>();
        relationships.add(new Relationship());

        return relationships;
    }
}