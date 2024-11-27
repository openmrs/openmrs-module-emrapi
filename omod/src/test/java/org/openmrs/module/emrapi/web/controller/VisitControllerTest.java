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
    public void setUp() throws Exception {
        executeDataSet("baseMetaData.xml");
        executeDataSet("pastVisitSetup.xml");
        mockMvc = MockMvcBuilders.standaloneSetup(controllerFactory.getObject()).build();
    }

    @Test
    public void shouldGetVisitsByPatientId() throws Exception {

        String visitNoteEncounterTypeUuid = "d7151f82-c1f3-4152-a605-2f9ea7414a79";
        String patientUuid = "8604d42e-3ca8-11e3-bf2b-0d0c09861e97";
        String firstVisitUuid = "1esd5218-6b78-11e0-93c3-18a905e044dc";
        String secondVisitUuid = "1c72e1ac-9b18-11e0-93c3-18a905e044dc";
        
        MvcResult response = mockMvc.perform(get("/rest/v1/emrapi/patient/" + patientUuid + "/visit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        
        String jsonResponse = response.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> result = objectMapper.readValue(jsonResponse, Map.class);

        assertNotNull(result);
        assert result.get("totalCount").equals(2);
        
        List<Map<String, Object>> visits = (List<Map<String, Object>>) result.get("pageOfResults");
        assert visits.size() == 2;
        
        // extract the first visit and check its properties
        Map<String, Object> firstVisit = visits.get(1);
        List<Map<String, Object>> firstVisitEncounters = (List<Map<String, Object>>) firstVisit.get("encounters");
        List<Map<String, Object>> firstVisitDiagnoses = (List<Map<String, Object>>) firstVisit.get("diagnoses");
        
        assert firstVisit.get("uuid").equals(firstVisitUuid);
        assert firstVisitEncounters.size() == 2;
        assert firstVisitDiagnoses.size() == 3;
        
        for (Map<String, Object> encounter : firstVisitEncounters) {
            assert ((String) encounter.get("display")).startsWith("Visit Note");
        }
       
        // extract the second visit and check its properties
        Map<String, Object> secondVisit = visits.get(0);
        List<Map<String, Object>> secondVisitEncounters = (List<Map<String, Object>>) secondVisit.get("encounters");
        List<Map<String, Object>> secondVisitDiagnoses = (List<Map<String, Object>>) secondVisit.get("diagnoses");
        
        assert secondVisit.get("uuid").equals(secondVisitUuid);
        assert secondVisitEncounters.size() == 1;
        assert secondVisitDiagnoses.size() == 2;
        
        for (Map<String, Object> encounter : secondVisitEncounters) {
            assert ((String) encounter.get("display")).startsWith("Visit Note");
        }
    }
    
    @Test
    public void shouldGetVisitsByPatientIdWithPagination() throws Exception {
        
        String patientUuid = "8604d42e-3ca8-11e3-bf2b-0d0c09861e97";
        String mostRecentVisitUuid = "1c72e1ac-9b18-11e0-93c3-18a905e044dc";
        
        MvcResult response = mockMvc.perform(get("/rest/v1/emrapi/patient/" + patientUuid + "/visit")
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
        
        List<Map<String, Object>> visits = (List<Map<String, Object>>) result.get("pageOfResults");
        assert visits.size() == 1;
        
        Map<String, Object> recentVisit = visits.get(0);
        assert recentVisit.get("uuid").equals(mostRecentVisitUuid);
    }
}
