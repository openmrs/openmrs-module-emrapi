package org.openmrs.module.emrapi.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VisitControllerTest extends BaseModuleWebContextSensitiveTest {

    @Autowired
    private ObjectFactory<VisitController> controllerFactory;
    
    private MockMvc mockMvc;
    
    @Before
    public void setUp() {
        executeDataSet("pastVisitSetup.xml");
        mockMvc = MockMvcBuilders.standaloneSetup(controllerFactory.getObject()).build();
    }

    @Test
    public void shouldGetVisitsWithNotesAndDiagnosesByPatient() throws Exception {

        String patientUuid = "8604d42e-3ca8-11e3-bf2b-0d0c09861e97";
        String firstVisitUuid = "1esd5218-6b78-11e0-93c3-18a905e044dc";
        String secondVisitUuid = "1c72e1ac-9b18-11e0-93c3-18a905e044dc";
        String thirdVisitUuid = "3c72f2bc-9b18-11e0-93c3-18a905e044ec";
        
        MvcResult response = mockMvc.perform(get("/rest/v1/emrapi/patient/" + patientUuid + "/visitWithNotesAndDiagnoses?patient="+patientUuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        
        String jsonResponse = response.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> result = objectMapper.readValue(jsonResponse, Map.class);

        assertNotNull(result);
        assert result.get("totalCount").equals(3);
        
        List<Map<String, Object>> visitEntries = (List<Map<String, Object>>) result.get("pageOfResults");
        assert visitEntries.size() == 3;
        
        // extract the first visit and check its properties
        Map<String, Object> firstVisitEntry = visitEntries.get(2);
        Map<String, Object> firstVisit = (Map<String, Object>) firstVisitEntry.get("visit");
        List<Map<String, Object>> firstVisitEncounters = (List<Map<String, Object>>) firstVisit.get("encounters");
        List<Map<String, Object>> firstVisitDiagnoses = (List<Map<String, Object>>) firstVisitEntry.get("diagnoses");
        
        assert firstVisit.get("uuid").equals(firstVisitUuid);
        assert firstVisitEncounters.size() == 2;
        assert firstVisitDiagnoses.size() == 3;
        
        for (Map<String, Object> encounter : firstVisitEncounters) {
            assert ((String) encounter.get("display")).startsWith("Visit Note");
        }
       
        // extract the second visit and check its properties
        Map<String, Object> secondVisitEntry = visitEntries.get(1);
        Map<String, Object> secondVisit = (Map<String, Object>) secondVisitEntry.get("visit");
        List<Map<String, Object>> secondVisitEncounters = (List<Map<String, Object>>) secondVisit.get("encounters");
        List<Map<String, Object>> secondVisitDiagnoses = (List<Map<String, Object>>) secondVisitEntry.get("diagnoses");
        
        assert secondVisit.get("uuid").equals(secondVisitUuid);
        assert secondVisitEncounters.size() == 1;
        assert secondVisitDiagnoses.size() == 2;
        
        for (Map<String, Object> encounter : secondVisitEncounters) {
            assert ((String) encounter.get("display")).startsWith("Visit Note");
        }
        
        // extract the third visit and check its properties (no notes or diagnoses)
        Map<String, Object> thirdVisitEntry = visitEntries.get(0);
        Map<String, Object> thirdVisit = (Map<String, Object>) thirdVisitEntry.get("visit");
        List<Map<String, Object>> thirdVisitEncounters = (List<Map<String, Object>>) thirdVisit.get("encounters");
        List<Map<String, Object>> thirdVisitDiagnoses = (List<Map<String, Object>>) thirdVisitEntry.get("diagnoses");
        
        assert thirdVisit.get("uuid").equals(thirdVisitUuid);
        assert thirdVisitEncounters.isEmpty();
        assert thirdVisitDiagnoses.isEmpty();
    }
    
    @Test
    public void shouldGetVisitsWithNotesAndDiagnosesByPatientWithPagination() throws Exception {
        
        String patientUuid = "8604d42e-3ca8-11e3-bf2b-0d0c09861e97";
        String mostRecentVisitUuid = "3c72f2bc-9b18-11e0-93c3-18a905e044ec";
        
        MvcResult response = mockMvc.perform(get("/rest/v1/emrapi/patient/" + patientUuid+"/visitWithNotesAndDiagnoses")
                        .param("startIndex", "0")
                        .param("limit", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        
        String jsonResponse = response.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> result = objectMapper.readValue(jsonResponse, Map.class);

        assertNotNull(result);
        assert result.get("totalCount").equals(1);
        
        List<Map<String, Object>> visitsEntries = (List<Map<String, Object>>) result.get("pageOfResults");
        assert visitsEntries.size() == 1;
        
        Map<String, Object> recentVisitEntry = visitsEntries.get(0);
        Map<String, Object> recentVisit = (Map<String, Object>) recentVisitEntry.get("visit");
        assert recentVisit.get("uuid").equals(mostRecentVisitUuid);
    }
}