package org.openmrs.module.emrapi.encounter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.exception.InvalidAdtEncounterException;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link EmrApiEncounterValidator}
 * This validator ensures ADT (Admission, Discharge, Transfer) encounters follow proper business logic.
 */
public class EncounterValidatorTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    private AdtService adtService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private LocationService locationService;

    private EmrApiEncounterValidator validator;

    @BeforeEach
    public void setUp() throws Exception {
        executeDataSet("baseTestDataset.xml");
        executeDataSet("encounterValidatorTestDataset.xml");

        validator = new EmrApiEncounterValidator();
        validator.setAdtService(adtService);
    }

    /**
     * @verifies pass validation for non-ADT encounters
     * @see EmrApiEncounterValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldPassValidationForNonAdtEncounters() {
        // Patient 6 has a check-in encounter (non-ADT)
        Patient patient = patientService.getPatient(6);
        Visit visit = visitService.getVisit(3002);
        Location location = locationService.getLocation(1);

        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setVisit(visit);
        encounter.setEncounterType(emrApiProperties.getCheckInEncounterType());
        encounter.setLocation(location);
        encounter.setEncounterDatetime(new Date());

        Errors errors = new BindException(encounter, "encounter");
        validator.validate(encounter, errors);

        assertFalse(errors.hasErrors(), "Non-ADT encounter should pass validation");
    }

    /**
     * @verifies pass validation for valid admission encounter when patient not admitted
     * @see EmrApiEncounterValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldPassValidationForValidAdmissionWhenPatientNotAdmitted() {
        // Patient 6 is not admitted
        Patient patient = patientService.getPatient(6);
        Visit visit = visitService.getVisit(3002);
        Location location = locationService.getLocation(4001);

        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setVisit(visit);
        encounter.setEncounterType(emrApiProperties.getAdmissionEncounterType());
        encounter.setLocation(location);
        encounter.setEncounterDatetime(new Date());

        // simulate real scenarios where the encounter is not yet saved but is added to the visit's list
        // of encounters in memory. Validation should pass by ignoring the encounter when checking the patient's
        // admission status
        visit.addEncounter(encounter);

        Errors errors = new BindException(encounter, "encounter");
        validator.validate(encounter, errors);

        assertFalse(errors.hasErrors(), "Valid admission encounter should pass validation");
    }

    /**
     * @verifies reject admission encounter when patient already admitted
     * @see EmrApiEncounterValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldRejectAdmissionWhenPatientAlreadyAdmitted() {
        // Patient 2 is already admitted (has admission encounter 5001)
        Patient patient = patientService.getPatient(2);
        Visit visit = visitService.getVisit(3001);
        Location location = locationService.getLocation(4002);

        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setVisit(visit);
        encounter.setEncounterType(emrApiProperties.getAdmissionEncounterType());
        encounter.setLocation(location);
        encounter.setEncounterDatetime(new Date());

        Errors errors = new BindException(encounter, "encounter");
        validator.validate(encounter, errors);

        assertTrue(errors.hasErrors(), "Should reject admission when patient already admitted");
        assertEquals(InvalidAdtEncounterException.Type.PATIENT_ALREADY_ADMITTED.getCode(),
                     errors.getAllErrors().get(0).getCode());
    }

    /**
     * @verifies pass validation for valid discharge encounter when patient admitted
     * @see EmrApiEncounterValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldPassValidationForValidDischargeWhenPatientAdmitted() {
        // Patient 2 is admitted
        Patient patient = patientService.getPatient(2);
        Visit visit = visitService.getVisit(3001);
        Location location = locationService.getLocation(4001);

        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setVisit(visit);
        encounter.setEncounterType(emrApiProperties.getExitFromInpatientEncounterType());
        encounter.setLocation(location);
        encounter.setEncounterDatetime(new Date());

        Errors errors = new BindException(encounter, "encounter");
        validator.validate(encounter, errors);

        assertFalse(errors.hasErrors(), "Valid discharge encounter should pass validation");
    }

    /**
     * @verifies reject discharge encounter when patient not admitted
     * @see EmrApiEncounterValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldRejectDischargeWhenPatientNotAdmitted() {
        // Patient 6 is not admitted
        Patient patient = patientService.getPatient(6);
        Visit visit = visitService.getVisit(3002);
        Location location = locationService.getLocation(1);

        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setVisit(visit);
        encounter.setEncounterType(emrApiProperties.getExitFromInpatientEncounterType());
        encounter.setLocation(location);
        encounter.setEncounterDatetime(new Date());

        Errors errors = new BindException(encounter, "encounter");
        validator.validate(encounter, errors);

        assertTrue(errors.hasErrors(), "Should reject discharge when patient not admitted");
        assertEquals(InvalidAdtEncounterException.Type.PATIENT_NOT_ADMITTED.getCode(),
                     errors.getAllErrors().get(0).getCode());
    }

    /**
     * @verifies reject discharge encounter when patient already discharged
     * @see EmrApiEncounterValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldRejectDischargeWhenPatientAlreadyDischarged() {
        // Patient 7 was admitted then discharged
        Patient patient = patientService.getPatient(7);
        Visit visit = visitService.getVisit(3003);
        Location location = locationService.getLocation(4001);

        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setVisit(visit);
        encounter.setEncounterType(emrApiProperties.getExitFromInpatientEncounterType());
        encounter.setLocation(location);
        encounter.setEncounterDatetime(new Date());

        Errors errors = new BindException(encounter, "encounter");
        validator.validate(encounter, errors);

        assertTrue(errors.hasErrors(), "Should reject discharge when patient already discharged");
        assertEquals(InvalidAdtEncounterException.Type.PATIENT_NOT_ADMITTED.getCode(),
                     errors.getAllErrors().get(0).getCode());
    }

    /**
     * @verifies pass validation for valid transfer encounter when patient admitted to different location
     * @see EmrApiEncounterValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldPassValidationForValidTransferWhenPatientAdmittedToDifferentLocation() {
        // Patient 2 is admitted at Ward A (4001), transferring to Ward B (4002)
        Patient patient = patientService.getPatient(2);
        Visit visit = visitService.getVisit(3001);
        Location newLocation = locationService.getLocation(4002);

        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setVisit(visit);
        encounter.setEncounterType(emrApiProperties.getTransferWithinHospitalEncounterType());
        encounter.setLocation(newLocation);
        encounter.setEncounterDatetime(new Date());

        Errors errors = new BindException(encounter, "encounter");
        validator.validate(encounter, errors);

        assertFalse(errors.hasErrors(), "Valid transfer to different location should pass validation");
    }

    /**
     * @verifies reject transfer encounter when patient not admitted
     * @see EmrApiEncounterValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldRejectTransferWhenPatientNotAdmitted() {
        // Patient 6 is not admitted
        Patient patient = patientService.getPatient(6);
        Visit visit = visitService.getVisit(3002);
        Location location = locationService.getLocation(4001);

        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setVisit(visit);
        encounter.setEncounterType(emrApiProperties.getTransferWithinHospitalEncounterType());
        encounter.setLocation(location);
        encounter.setEncounterDatetime(new Date());

        Errors errors = new BindException(encounter, "encounter");
        validator.validate(encounter, errors);

        assertTrue(errors.hasErrors(), "Should reject transfer when patient not admitted");
        assertEquals(InvalidAdtEncounterException.Type.PATIENT_NOT_ADMITTED.getCode(),
                     errors.getAllErrors().get(0).getCode());
    }

    /**
     * @verifies reject transfer encounter when patient already at location
     * @see EmrApiEncounterValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldRejectTransferWhenPatientAlreadyAtLocation() {
        // Patient 2 is admitted at Ward A (4001), trying to transfer to same location
        Patient patient = patientService.getPatient(2);
        Visit visit = visitService.getVisit(3001);
        Location sameLocation = locationService.getLocation(4001);

        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setVisit(visit);
        encounter.setEncounterType(emrApiProperties.getTransferWithinHospitalEncounterType());
        encounter.setLocation(sameLocation);
        encounter.setEncounterDatetime(new Date());

        Errors errors = new BindException(encounter, "encounter");
        validator.validate(encounter, errors);

        assertTrue(errors.hasErrors(), "Should reject transfer when patient already at location");
        assertEquals(InvalidAdtEncounterException.Type.PATIENT_ALREADY_AT_LOCATION.getCode(),
                     errors.getAllErrors().get(0).getCode());
    }

    /**
     * @verifies pass validation for encounters with null visit (non-ADT encounters)
     * @see EmrApiEncounterValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldPassValidationForEncounterWithNullVisit() {
        Patient patient = patientService.getPatient(2);
        Location location = locationService.getLocation(1);

        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setVisit(null);  // null visit
        encounter.setEncounterType(emrApiProperties.getCheckInEncounterType());  // non-ADT encounter
        encounter.setLocation(location);
        encounter.setEncounterDatetime(new Date());

        Errors errors = new BindException(encounter, "encounter");
        validator.validate(encounter, errors);

        // Non-ADT encounters with null visit should pass validation
        assertFalse(errors.hasErrors());
    }
}
