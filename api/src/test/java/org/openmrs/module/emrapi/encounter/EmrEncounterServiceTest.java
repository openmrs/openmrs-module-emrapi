package org.openmrs.module.emrapi.encounter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.builder.EncounterBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.postprocessor.EncounterTransactionHandler;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
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
    private LocationService locationService;
    @Mock
    private ProviderService providerService;
    @Mock
    private AdministrationService administrationService;

    @Mock
    private EncounterTransactionMapper encounterTransactionMapper;
    @Mock
    private EncounterProviderServiceHelper encounterProviderServiceHelper;

    @Mock
    private EmrOrderService orderService;

    private EmrEncounterServiceImpl emrEncounterService;

    private EncounterTransactionHandler encounterTransactionHandler;

    private Patient patient;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        emrEncounterService = new EmrEncounterServiceImpl(patientService,visitService,encounterService,locationService,providerService,
                administrationService,encounterObservationServiceHelper,
                encounterDispositionServiceHelper,encounterTransactionMapper, encounterProviderServiceHelper, orderService);

        patient = new Patient(1);
        patient.setUuid("patient-uuid");
        when(patientService.getPatientByUuid("patient-uuid")).thenReturn(patient);

        Visit visit = new Visit(1);
        visit.setUuid("visit-uuid");
        when(visitService.getVisitByUuid("visit-uuid")).thenReturn(visit);

        EncounterType encounterType = new EncounterType(1);
        encounterType.setUuid("encType-invsgtn-uuid");
        when(encounterService.getEncounterTypeByUuid("encType-invsgtn-uuid")).thenReturn(encounterType);

        PowerMockito.mockStatic(Context.class);
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
        assertEquals("visit-uuid", encounterTransaction.getVisitUuid());
        assertNotNull(encounterTransaction.getEncounterUuid());
    }

    @Test
    public void shouldDelegateSavingOrdersToOrderService() {
        EncounterTransaction encounterTransaction = new EncounterTransaction("visit-uuid", "encounter-uuid");
        encounterTransaction.setPatientUuid("patient-uuid");
        encounterTransaction.setEncounterTypeUuid("encType-invsgtn-uuid");
        List<EncounterTransaction.DrugOrder> drugOrders = Arrays.asList(new EncounterTransaction.DrugOrder());
        encounterTransaction.setDrugOrders(drugOrders);

        emrEncounterService.save(encounterTransaction);

        verify(orderService).save(same(drugOrders), any(Encounter.class));
    }

    @Test
    public void shouldFetchEncounterTransactionByUuid() throws Exception {
        Encounter encounter = new EncounterBuilder().build();
        when(encounterService.getEncounterByUuid("encounterUuid")).thenReturn(encounter);
        when(encounterTransactionMapper.map(encounter, false)).thenReturn(new EncounterTransaction());
        EncounterTransaction encounterTransaction = emrEncounterService.getEncounterTransaction("encounterUuid", false);
        verify(encounterTransactionMapper).map(encounter, false);

        assertNotNull(encounterTransaction);
    }

    @Test
    public void shouldSaveNewEncounterWhenEarlierEncounterForSameTypeIsVoided() throws Exception {
        EncounterType encounterType = new EncounterType(1);
        encounterType.setUuid("encType-invsgtn-uuid");

        Encounter voidedEncounter = new Encounter(1);
        voidedEncounter.setEncounterType(encounterType);
        voidedEncounter.setVoided(true);
        voidedEncounter.setUuid("someRandomUUID");

        Visit visit = new Visit(1);
        visit.setUuid("visit-uuid");
        visit.addEncounter(voidedEncounter);
        when(visitService.getVisitByUuid("visit-uuid")).thenReturn(visit);


        EncounterTransaction encounterTransaction = emrEncounterService.save(constructEncounterTransaction());
        assertNotNull(encounterTransaction);
        assertEquals("visit-uuid",encounterTransaction.getVisitUuid());
        assertNotEquals("someRandomUUID", encounterTransaction.getEncounterUuid());
    }
    @Test
    public void shouldDelegateDetailsFromEncounterSearchParametersToEncounterService() {
        EncounterSearchParameters parameters = new EncounterSearchParameters();
        Date startDate = new Date();
        parameters.setEncounterDateTimeFrom(startDate);
        Date endDate = new Date();
        parameters.setEncounterDateTimeTo(endDate);
        parameters.setEncounterTypeUuids(Arrays.asList("encounter-type-uuid"));
        parameters.setIncludeAll(true);
        parameters.setLocationUuid("location-uuid");
        parameters.setPatientUuid("patient-uuid");
        parameters.setProviderUuids(Arrays.asList("provider-uuid"));
        parameters.setVisitTypeUuids(Arrays.asList("visit-type-uuid"));
        parameters.setVisitUuids(Arrays.asList("visit-uuid"));

        EncounterType encounterType = new EncounterType();
        when(encounterService.getEncounterTypeByUuid("encounter-type-uuid")).thenReturn(encounterType);
        Location location = new Location();
        when(locationService.getLocationByUuid("location-uuid")).thenReturn(location);
        Patient patient = new Patient();
        when(patientService.getPatientByUuid("patient-uuid")).thenReturn(patient);
        Provider provider = new Provider();
        when(providerService.getProviderByUuid("provider-uuid")).thenReturn(provider);
        VisitType visitType = new VisitType();
        when(visitService.getVisitTypeByUuid("visit-type-uuid")).thenReturn(visitType);
        Visit visit = new Visit();
        when(visitService.getVisitByUuid("visit-uuid")).thenReturn(visit);

        emrEncounterService.find(parameters);


        ArgumentCaptor<Patient> patientArgument = ArgumentCaptor.forClass(Patient.class);
        ArgumentCaptor<Location> locationArgument = ArgumentCaptor.forClass(Location.class);
        ArgumentCaptor<Date> startDateArgument = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> endDateArgument = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Collection<EncounterType>> encounterTypesArgument = (ArgumentCaptor)ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Collection<Provider>> providersArgument = (ArgumentCaptor)ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Collection<VisitType>> visitTypesArgument = (ArgumentCaptor)ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Collection<Visit>> visitsArgument = (ArgumentCaptor)ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Boolean> includeAllArgument = ArgumentCaptor.forClass(Boolean.class);


        verify(encounterService).getEncounters(patientArgument.capture(), locationArgument.capture(), startDateArgument.capture(),
                endDateArgument.capture(), Matchers.<Collection<Form>>any(), encounterTypesArgument.capture(), providersArgument.capture(), visitTypesArgument.capture(),
                visitsArgument.capture(), includeAllArgument.capture());

        assertThat(patientArgument.getValue(), is(equalTo(patient)));
        assertThat(locationArgument.getValue(), is(equalTo(location)));
        assertThat(encounterTypesArgument.getValue().iterator().next(), is(equalTo(encounterType)));
        assertThat(providersArgument.getValue().iterator().next(), is(equalTo(provider)));
        assertThat(visitTypesArgument.getValue().iterator().next(), is(equalTo(visitType)));
        assertThat(visitsArgument.getValue().iterator().next(), is(equalTo(visit)));
    }

    @Test
    public void shouldCallEncounterTransactionHandlersOnSave(){

        EncounterType encounterType = new EncounterType(1);
        encounterType.setUuid("encType-invsgtn-uuid");

        EncounterTransaction encounterTransaction = constructEncounterTransaction();
        Encounter encounter = mock(Encounter.class);

        encounterTransactionHandler = mock(EncounterTransactionHandler.class);
        when(Context.getRegisteredComponents(EncounterTransactionHandler.class)).thenReturn(
                Arrays.asList(encounterTransactionHandler));
        when(encounterService.getEncounterByUuid("encounterUuid")).thenReturn(encounter);

        emrEncounterService.onStartup();
        emrEncounterService.save(encounterTransaction);
        verify(encounterTransactionHandler).forSave(any(Encounter.class), eq(encounterTransaction));
    }

    @Test
    public void shouldCallEncounterTransactionHandlerOnRead(){
        EncounterSearchParameters parameters = new EncounterSearchParameters();
        Date startDate = new Date();
        parameters.setEncounterDateTimeFrom(startDate);
        Date endDate = new Date();
        parameters.setEncounterDateTimeTo(endDate);
        parameters.setEncounterTypeUuids(Arrays.asList("encounter-type-uuid"));
        parameters.setIncludeAll(true);
        parameters.setLocationUuid("location-uuid");
        parameters.setPatientUuid("patient-uuid");
        parameters.setProviderUuids(Arrays.asList("provider-uuid"));
        parameters.setVisitTypeUuids(Arrays.asList("visit-type-uuid"));
        parameters.setVisitUuids(Arrays.asList("visit-uuid"));

        EncounterType encounterType = new EncounterType();
        when(encounterService.getEncounterTypeByUuid("encounter-type-uuid")).thenReturn(encounterType);
        Location location = new Location();
        when(locationService.getLocationByUuid("location-uuid")).thenReturn(location);
        Patient patient = new Patient();
        when(patientService.getPatientByUuid("patient-uuid")).thenReturn(patient);
        Provider provider = new Provider();
        when(providerService.getProviderByUuid("provider-uuid")).thenReturn(provider);
        VisitType visitType = new VisitType();
        when(visitService.getVisitTypeByUuid("visit-type-uuid")).thenReturn(visitType);
        Visit visit = new Visit();
        when(visitService.getVisitByUuid("visit-uuid")).thenReturn(visit);

        Encounter encounter = new EncounterBuilder().build();
        when(encounterService.getEncounters(any(Patient.class), any(Location.class), any(Date.class),
                any(Date.class), Matchers.<Collection<Form>>any(), Matchers.<Collection<EncounterType>>any(), Matchers.<Collection<Provider>>any(), Matchers.<Collection<VisitType>>any(),
                Matchers.<Collection<Visit>>any(), any(Boolean.class))).thenReturn(Arrays.asList(encounter));

        emrEncounterService.onStartup();
        emrEncounterService.find(parameters);
    }
}
