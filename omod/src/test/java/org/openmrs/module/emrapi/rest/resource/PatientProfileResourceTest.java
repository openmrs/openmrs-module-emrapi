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
import java.util.List;
import java.util.Map;

public class PatientProfileResourceTest extends BaseModuleWebContextSensitiveTest {

    private PatientProfileResource resource;

    @Before
    public void beforeEachTests() throws Exception {
        resource = (PatientProfileResource) Context.getService(RestService.class).getResourceBySupportedClass(PatientProfile.class);
        File personImageDirectory = new File(OpenmrsUtil.getApplicationDataDirectory() + "/person_images");
        personImageDirectory.mkdirs();
    }

    @Test
    public void shouldCreatePatient() throws Exception {
        SimpleObject patientProfileCreateObject = new SimpleObject();
        patientProfileCreateObject.putAll(new ObjectMapper().readValue(getClass().getClassLoader().getResourceAsStream("create_patient_profile.json"), HashMap.class));
        SimpleObject created = (SimpleObject) resource.create(patientProfileCreateObject, new RequestContext());
        Assert.assertEquals("id-B - Ram Kabir", ((Map) created.get("patient")).get("display"));
        List<Object> relationships = (List)created.get("relationships");
        Assert.assertEquals(1,relationships.size());
        Assert.assertEquals("ba1b19c2-3ed6-4f63-b8c0-f762dc8d7562",((Map)(((Map)relationships.get(0)).get("personB"))).get("uuid"));
        Assert.assertEquals("6d9002ea-a96b-4889-af78-82d48c57a110",((Map)(((Map)relationships.get(0)).get("relationshipType"))).get("uuid"));
    }

    @Test
    public void shouldUpdatePatient() throws Exception {
        SimpleObject patientProfileUpdateObject = new SimpleObject();
        patientProfileUpdateObject.putAll(new ObjectMapper().readValue(getClass().getClassLoader().getResourceAsStream("update_patient_profile.json"), HashMap.class));
        SimpleObject created = (SimpleObject) resource.update("da7f524f-27ce-4bb2-86d6-6d1d05312bd5", patientProfileUpdateObject, new RequestContext());
        Assert.assertEquals("101-6 - dull skull", ((Map) created.get("patient")).get("display"));
    }

}