package org.openmrs.module.emrapi.rest.resource;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;

import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.patient.PatientProfile;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class PatientProfileResourceTest extends BaseModuleWebContextSensitiveTest {

    private SimpleObject patientProfileSimpleObject = new SimpleObject();

    private PatientProfileResource resource;

    @Before
    public void beforeEachTests() throws Exception {
        patientProfileSimpleObject.putAll(new ObjectMapper().readValue(getClass().getClassLoader().getResourceAsStream("create_patient_profile.json"), HashMap.class));
        resource = (PatientProfileResource) Context.getService(RestService.class).getResourceBySupportedClass(PatientProfile.class);
        File personImageDirectory = new File(OpenmrsUtil.getApplicationDataDirectory() + "/person_images");
        personImageDirectory.mkdirs();
    }

    @Test
    public void shouldCreatePatient() throws Exception {
        SimpleObject created = (SimpleObject) resource.create(patientProfileSimpleObject, new RequestContext());
        Assert.assertEquals("id-B - Ram Kabir", ((Map) created.get("patient")).get("display"));
    }

}