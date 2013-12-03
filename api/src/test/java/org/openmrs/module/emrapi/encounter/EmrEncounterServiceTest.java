package org.openmrs.module.emrapi.encounter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EmrEncounterServiceTest {
    @Mock
    private PatientService patientService;
    @Mock
    private VisitService visitService;
    @Mock
    private EncounterService encounterService;
    @Mock
    private EncounterObservationServiceHelper encounterObservationServiceHelper;
    @Mock
    private EncounterDispositionServiceHelper encounterDispositionServiceHelper;
    @Mock
    private EncounterTestOrderServiceHelper encounterTestOrderServiceHelper;
    @Mock
    private EncounterDrugOrderServiceHelper encounterDrugOrderServiceHelper;
    @Mock
    private LocationService locationService;
    @Mock
    private ProviderService providerService;
    @Mock
    private AdministrationService administrationService;

    @Mock
    private EncounterTransactionMapper encounterTransactionMapper;
    @Mock
    private EncounterProviderServiceHelper encounterProviderServiceHelper;

    private EmrEncounterService emrEncounterService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        emrEncounterService = new EmrEncounterServiceImpl(patientService,visitService,encounterService,locationService,providerService,
                administrationService,encounterObservationServiceHelper,encounterTestOrderServiceHelper,encounterDrugOrderServiceHelper,
                encounterDispositionServiceHelper,encounterTransactionMapper, encounterProviderServiceHelper);

        Patient patient = new Patient(1);
        patient.setUuid("patient-uuid");
        when(patientService.getPatientByUuid("patient-uuid")).thenReturn(patient);

        Visit visit = new Visit(1);
        visit.setUuid("visit-uuid");
        when(visitService.getVisitByUuid("visit-uuid")).thenReturn(visit);

        EncounterType encounterType = new EncounterType(1);
        encounterType.setUuid("encType-invsgtn-uuid");
        when(encounterService.getEncounterTypeByUuid("encType-invsgtn-uuid")).thenReturn(encounterType);
    }

    private EncounterTransaction.Concept getConcept(String conceptUuid) {
        return new EncounterTransaction.Concept(conceptUuid, "concept_name");
    }

    private EncounterTransaction constructEncounterTransaction(){
        EncounterTransaction encTrans = new EncounterTransaction();

        List<EncounterTransaction.Observation> observations = asList(
                new EncounterTransaction.Observation().setConcept(getConcept("radio-result-uuid")).setValue("text value").setComment("overweight").setOrderUuid("order-uuid")
        );

        encTrans.setPatientUuid("patient-uuid");
        encTrans.setVisitUuid("visit-uuid");
        encTrans.setEncounterTypeUuid("encType-invsgtn-uuid");
        encTrans.setObservations(observations);
        return  encTrans;
    }

    @Test
    public void shouldSaveEncounter() throws Exception {
        EncounterTransaction encounterTransaction = emrEncounterService.save(constructEncounterTransaction());
        assertNotNull(encounterTransaction);
        assertEquals("visit-uuid",encounterTransaction.getVisitUuid());
        assertNotNull(encounterTransaction.getEncounterUuid());
    }
}
