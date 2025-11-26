package org.openmrs.module.emrapi.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.module.emrapi.test.builder.ObsBuilder;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    ConceptService conceptService;

    @Autowired
    EncounterService encounterService;

    @Autowired
    EmrApiProperties emrApiProperties;

    @Autowired
    @Qualifier("adminService")
    AdministrationService administrationService;
    
    private MockMvc mockMvc;
    
    @Before
    public void setUp() {
	    // Execute the dataset
        executeDataSet("pastVisitSetup.xml");
        mockMvc = MockMvcBuilders.standaloneSetup(controllerFactory.getObject()).build();
    }

    @Test
    public void shouldGetVisitsWithNotesAndDiagnosesByPatient() throws Exception {

        String patientUuid = "8604d42e-3ca8-11e3-bf2b-0d0c09861e97";
        String firstVisitUuid = "1esd5218-6b78-11e0-93c3-18a905e044dc";
        String secondVisitUuid = "1c72e1ac-9b18-11e0-93c3-18a905e044dc";
        String thirdVisitUuid = "3c72f2bc-9b18-11e0-93c3-18a905e044ec";
        
        MvcResult response = mockMvc.perform(get("/rest/v1/emrapi/patient/" + patientUuid + "/visit")
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
        List<Map<String, Object>> firstVisitNotes = (List<Map<String, Object>>)firstVisitEntry.get("visitNotes");
        List<Map<String, Object>> firstVisitDiagnoses = (List<Map<String, Object>>) firstVisitEntry.get("diagnoses");
        
        assert firstVisit.get("uuid").equals(firstVisitUuid);
        assert firstVisitNotes.size() == 2;
        assert firstVisitDiagnoses.size() == 3;
        
        for (Map<String, Object> note : firstVisitNotes) {
            // All notes obs in test data starts with "Visit Note"
	        assert ((String) note.get("value")).startsWith("Visit Note");
        }
       
        // extract the second visit and check its properties
        Map<String, Object> secondVisitEntry = visitEntries.get(1);
        Map<String, Object> secondVisit = (Map<String, Object>) secondVisitEntry.get("visit");
        List<Map<String, Object>> secondVisitNotes = (List<Map<String, Object>>) secondVisitEntry.get("visitNotes");
        List<Map<String, Object>> secondVisitDiagnoses = (List<Map<String, Object>>) secondVisitEntry.get("diagnoses");
        
        assert secondVisit.get("uuid").equals(secondVisitUuid);
        assert secondVisitNotes.size() == 1;
        assert secondVisitDiagnoses.size() == 2;
        
        for (Map<String, Object> note : secondVisitNotes) {
            // All notes obs in test data starts with "Visit Note"
            assert ((String) note.get("value")).startsWith("Visit Note");
        }
        
        // extract the third visit and check its properties (no notes or diagnoses)
        Map<String, Object> thirdVisitEntry = visitEntries.get(0);
        Map<String, Object> thirdVisit = (Map<String, Object>) thirdVisitEntry.get("visit");
        List<Map<String, Object>> thirdVisitNotes = (List<Map<String, Object>>) thirdVisitEntry.get("visitNotes");
        List<Map<String, Object>> thirdVisitDiagnoses = (List<Map<String, Object>>) thirdVisitEntry.get("diagnoses");
        
        assert thirdVisit.get("uuid").equals(thirdVisitUuid);
        assert thirdVisitNotes.isEmpty();
        assert thirdVisitDiagnoses.isEmpty();
    }

    @Test
    public void shouldGetCustomRepresentation() throws Exception {

        String patientUuid = "8604d42e-3ca8-11e3-bf2b-0d0c09861e97";
        String firstVisitUuid = "1esd5218-6b78-11e0-93c3-18a905e044dc";

        String rep = "custom:(visit:(uuid),diagnoses:(certainty))";

        MvcResult response = mockMvc.perform(get("/rest/v1/emrapi/patient/" + patientUuid + "/visit")
                        .param("v", rep)
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
        List<Map<String, Object>> firstVisitNotes = (List<Map<String, Object>>)firstVisitEntry.get("visitNotes");
        List<Map<String, Object>> firstVisitDiagnoses = (List<Map<String, Object>>) firstVisitEntry.get("diagnoses");

        assert firstVisit.size() == 1;
        assert firstVisit.get("uuid").equals(firstVisitUuid);
        assert firstVisitNotes == null;
        assert firstVisitDiagnoses.size() == 3;

        for (Map<String, Object> diagnosis : firstVisitDiagnoses) {
            assert diagnosis.size() == 1;
            assert diagnosis.get("certainty").equals("CONFIRMED");
        }
    }
    
    @Test
    public void shouldGetVisitsWithNotesAndDiagnosesByPatientWithPagination() throws Exception {
        
        String patientUuid = "8604d42e-3ca8-11e3-bf2b-0d0c09861e97";
        String mostRecentVisitUuid = "3c72f2bc-9b18-11e0-93c3-18a905e044ec";
        
        MvcResult response = mockMvc.perform(get("/rest/v1/emrapi/patient/" + patientUuid+"/visit")
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

    @Test
    public void shouldThrowErrorWhenPatientNotFound() throws Exception {
        
        String patientUuid = "non-existent-uuid";
        
        mockMvc.perform(get("/rest/v1/emrapi/patient/" + patientUuid + "/visit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldGetVisitsWithObsGroupDiagnosesByPatient() throws Exception {

        administrationService.setGlobalProperty(EmrApiConstants.GP_USE_LEGACY_DIAGNOSIS_SERVICE, "true");
        DiagnosisMetadata dmd = ContextSensitiveMetadataTestUtils.setupDiagnosisMetadata(conceptService, emrApiProperties);

        String patientUuid = "8604d42e-3ca8-11e3-bf2b-0d0c09861e97";
        Concept codedDiagnosisConcept = conceptService.getConcept(11);

        Encounter encounter = encounterService.getEncounter(2001);
        encounter.addObs(new ObsBuilder()
                .setPerson(encounter.getPatient())
                .setEncounter(encounter)
                .setConcept(dmd.getDiagnosisSetConcept())
                .addMember(dmd.getNonCodedDiagnosisConcept(), "Headache")
                .addMember(dmd.getDiagnosisOrderConcept(), dmd.getConceptFor(Diagnosis.Order.SECONDARY))
                .addMember(dmd.getDiagnosisCertaintyConcept(), dmd.getConceptFor(Diagnosis.Certainty.CONFIRMED)).get()
        );
        encounter.addObs(new ObsBuilder()
                .setPerson(encounter.getPatient())
                .setEncounter(encounter)
                .setConcept(dmd.getDiagnosisSetConcept())
                .addMember(dmd.getCodedDiagnosisConcept(), codedDiagnosisConcept)
                .addMember(dmd.getDiagnosisOrderConcept(), dmd.getConceptFor(Diagnosis.Order.PRIMARY))
                .addMember(dmd.getDiagnosisCertaintyConcept(), dmd.getConceptFor(Diagnosis.Certainty.PRESUMED)).get()
        );
        encounterService.saveEncounter(encounter);

        MvcResult response = mockMvc.perform(get("/rest/v1/emrapi/patient/" + patientUuid + "/visit")
                        .param("v", "custom:(diagnoses:(diagnosis,certainty,rank))")
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
        List<Map<String, Object>> firstVisitDiagnoses = (List<Map<String, Object>>)visitEntries.get(2).get("diagnoses");
        List<Map<String, Object>> secondVisitDiagnoses = (List<Map<String, Object>>)visitEntries.get(1).get("diagnoses");
        List<Map<String, Object>> thirdVisitDiagnoses = (List<Map<String, Object>>)visitEntries.get(0).get("diagnoses");

        assert firstVisitDiagnoses.size() == 2;
        assert secondVisitDiagnoses.isEmpty();
        assert thirdVisitDiagnoses.isEmpty();

        Map<String, Object> diagnosis1 = firstVisitDiagnoses.get(0);
        Map<String, Object> diagnosisValue1 = (Map<String, Object>) diagnosis1.get("diagnosis");
        assert ((Map)diagnosisValue1.get("coded")).get("uuid").equals(codedDiagnosisConcept.getUuid());
        assert diagnosisValue1.get("nonCoded") == null;
        assert diagnosis1.get("certainty").equals("PROVISIONAL");
        assert diagnosis1.get("rank").equals(1);

        Map<String, Object> diagnosis2 = firstVisitDiagnoses.get(1);
        Map<String, Object> diagnosisValue2 = (Map<String, Object>) diagnosis2.get("diagnosis");
        assert diagnosisValue2.get("coded") == null;
        assert diagnosisValue2.get("nonCoded").equals("Headache");
        assert diagnosis2.get("certainty").equals("CONFIRMED");
        assert diagnosis2.get("rank").equals(2);
    }
}
