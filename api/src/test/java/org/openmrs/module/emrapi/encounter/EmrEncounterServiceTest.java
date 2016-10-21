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
import org.openmrs.module.emrapi.db.DbSessionDAO;
import org.openmrs.module.emrapi.db.DbSessionUtil;
import org.openmrs.module.emrapi.encounter.builder.EncounterBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.matcher.BaseEncounterMatcher;
import org.openmrs.module.emrapi.encounter.postprocessor.EncounterTransactionHandler;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
    private DbSessionDAO dbSessionDAO;

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
        DbSessionUtil.setDAO(dbSessionDAO);
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
        encTrans.setVisitLocationUuid("login-location-uuid");
        encTrans.setObservations(observations);
        return  encTrans;
    }

    @Test
    public void shouldSaveEncounterInTheSpecifiedVisitInEncounterTransaction() throws Exception {
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
        Location location = new Location();
        location.setUuid("login-location-uuid");
        when(locationService.getLocationByUuid("login-location-uuid")).thenReturn(location);


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
    public void shouldPassInEncounterParametersToEncounterMatcheronSave() {
        EncounterTransaction encounterTransaction = constructEncounterTransaction();
        HashMap<String, Object> context = new HashMap<String, Object>();
        context.put("patientProgramUuid", "17ca76b4-dbb2-11e5-b5d2-0a1d41d68578");
        encounterTransaction.setContext(context);

        BaseEncounterMatcher mockEncounterMatcher = mock(BaseEncounterMatcher.class);
        when(administrationService.getGlobalProperty("emr.encounterMatcher")).thenReturn(mockEncounterMatcher.getClass().getCanonicalName());
        when(Context.getRegisteredComponents(BaseEncounterMatcher.class)).thenReturn(Arrays.asList(mockEncounterMatcher));

        encounterTransactionHandler = mock(EncounterTransactionHandler.class);
        when(Context.getRegisteredComponents(EncounterTransactionHandler.class)).thenReturn(
                Arrays.asList(encounterTransactionHandler));

        emrEncounterService.onStartup();
        emrEncounterService.save(encounterTransaction);

        ArgumentCaptor<EncounterParameters> encounterParametersArguments = ArgumentCaptor.forClass(EncounterParameters.class);
        verify(mockEncounterMatcher).findEncounter(any(Visit.class), encounterParametersArguments.capture());
        assertThat(encounterParametersArguments.getValue().getContext().get("patientProgramUuid"), is(equalTo((Object)"17ca76b4-dbb2-11e5-b5d2-0a1d41d68578")));
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

    @Test
    public void shouldStoreLocationForVisit(){

        EncounterType encounterType = new EncounterType(1);
        encounterType.setUuid("encType-invsgtn-uuid");

        EncounterTransaction encounterTransaction = constructEncounterTransaction();
        encounterTransaction.setVisitLocationUuid("visit-location-uuid");
        encounterTransaction.setVisitUuid(null);
        Encounter encounter = mock(Encounter.class);

        Location location = new Location();
        location.setName("hospital");
        location.setUuid("visit-location-uuid");

        encounterTransactionHandler = mock(EncounterTransactionHandler.class);
        when(Context.getRegisteredComponents(EncounterTransactionHandler.class)).thenReturn(
                Arrays.asList(encounterTransactionHandler));
        when(encounterService.getEncounterByUuid("encounterUuid")).thenReturn(encounter);
        when(locationService.getLocationByUuid("visit-location-uuid")).thenReturn(location);

        emrEncounterService.onStartup();
        emrEncounterService.save(encounterTransaction);
        verify(locationService).getLocationByUuid("visit-location-uuid");
        verify(encounterTransactionHandler).forSave(any(Encounter.class), eq(encounterTransaction));

        ArgumentCaptor<Visit> argumentCaptor = ArgumentCaptor.forClass(Visit.class);
        verify(visitService).saveVisit(argumentCaptor.capture());

        assertEquals(argumentCaptor.getValue().getLocation().getUuid(), "visit-location-uuid");
    }

    @Test
    public void shouldSaveEncounterInActiveVisitIfItIsThereInThatVisitLocation(){

        EncounterType encounterType = new EncounterType(1);
        encounterType.setUuid("encType-invsgtn-uuid");

        EncounterTransaction encounterTransaction = constructEncounterTransaction();
        encounterTransaction.setVisitLocationUuid("visit-location-uuid");
        encounterTransaction.setVisitUuid(null);
        Encounter encounter = new Encounter();
        encounter.setUuid("encounterUuid");

        Location location = new Location();
        location.setName("hospital");
        location.setUuid("visit-location-uuid");

        Visit visit = new Visit();
        visit.setUuid("visit-uuid");
        visit.setLocation(location);
        List visits = new ArrayList();
        visits.add(visit);

        encounterTransactionHandler = mock(EncounterTransactionHandler.class);
        when(Context.getRegisteredComponents(EncounterTransactionHandler.class)).thenReturn(
                Arrays.asList(encounterTransactionHandler));
        when(encounterService.getEncounterByUuid("encounterUuid")).thenReturn(encounter);
        when(locationService.getLocationByUuid("visit-location-uuid")).thenReturn(location);
        when(visitService.getVisitByUuid("visit-uuid")).thenReturn(visit);

        when(visitService.getActiveVisitsByPatient(patient)).thenReturn(visits);

        emrEncounterService.onStartup();
        EncounterTransaction savedEncounterTransaction = emrEncounterService.save(encounterTransaction);
        verify(encounterTransactionHandler).forSave(any(Encounter.class), eq(encounterTransaction));

        assertEquals(savedEncounterTransaction.getVisitUuid(), "visit-uuid");
    }

    @Test
    public void shouldCreateNewVisitAndSetVisitLocationWhenThereIsNoActiveVisitInThatLocation() {

        EncounterType encounterType = new EncounterType(1);
        encounterType.setUuid("encType-invsgtn-uuid");

        EncounterTransaction encounterTransaction = constructEncounterTransaction();
        encounterTransaction.setVisitLocationUuid("visit-location-uuid");
        encounterTransaction.setVisitUuid(null);
        Encounter encounter = mock(Encounter.class);

        Location location = new Location();
        location.setName("hospital");
        location.setUuid("visit-location-uuid-two");

        Location visitLocation = new Location();
        visitLocation.setName("hospital");
        visitLocation.setUuid("visit-location-uuid");

        Visit visit = new Visit();
        visit.setUuid("visit-uuid");
        visit.setLocation(location);
        List visits = new ArrayList();
        visits.add(visit);

        encounterTransactionHandler = mock(EncounterTransactionHandler.class);
        when(Context.getRegisteredComponents(EncounterTransactionHandler.class)).thenReturn(
                Arrays.asList(encounterTransactionHandler));
        when(encounterService.getEncounterByUuid("encounterUuid")).thenReturn(encounter);
        when(locationService.getLocationByUuid("visit-location-uuid")).thenReturn(visitLocation);
        when(visitService.getVisitByUuid("visit-uuid")).thenReturn(visit);
        when(visitService.getActiveVisitsByPatient(patient)).thenReturn(visits);

        emrEncounterService.onStartup();
        EncounterTransaction savedEncounterTransaction = emrEncounterService.save(encounterTransaction);

        ArgumentCaptor<Visit> argumentCaptor = ArgumentCaptor.forClass(Visit.class);
        verify(visitService).saveVisit(argumentCaptor.capture());

        assertEquals(argumentCaptor.getValue().getLocation().getUuid(), "visit-location-uuid");
        assertNotEquals(savedEncounterTransaction.getVisitUuid(), "visit-uuid");
    }

    @Test
    public void shouldUseActiveVisitWithoutLocationToSaveIfEncounterParametersIsNotPassedWithVisitLocation() throws ParseException {
        String encounterTypeUuid = "encounterTypeUuid";
        EncounterType encounterType = new EncounterType();
        encounterType.setUuid(encounterTypeUuid);

        EncounterTransaction encounterTransaction = new EncounterTransaction();
        encounterTransaction.setEncounterTypeUuid(encounterTypeUuid);

        String newPatientUuid = "newPatientUuid";
        encounterTransaction.setPatientUuid(newPatientUuid);
        Patient patient = new Patient();
        patient.setUuid(newPatientUuid);

        Visit visit = new Visit();
        visit.setPatient(patient);
        String visitUuid = "visitUuid";
        visit.setUuid(visitUuid);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = simpleDateFormat.parse("21-10-2016");
        visit.setStartDatetime(date);

        when(patientService.getPatientByUuid(newPatientUuid)).thenReturn(patient);
        when(visitService.getActiveVisitsByPatient(patient)).thenReturn(Arrays.asList(visit));
        when(encounterService.getEncounterTypeByUuid(encounterTypeUuid)).thenReturn(encounterType);

        EncounterTransaction savedEncounterTransaction = emrEncounterService.save(encounterTransaction);
        assertEquals(visitUuid, savedEncounterTransaction.getVisitUuid());
    }

    @Test
    public void shouldCreateANewVisitWithoutLocationIfVisitLocationUuidIsNotPassedInEncounterTransactionAndNoOtherActiveVisitPresent() {
        String encounterTypeUuid = "encounterTypeUuid";
        EncounterType encounterType = new EncounterType();
        encounterType.setUuid(encounterTypeUuid);

        EncounterTransaction encounterTransaction = new EncounterTransaction();
        encounterTransaction.setEncounterTypeUuid(encounterTypeUuid);

        String newPatientUuid = "newPatientUuid";
        encounterTransaction.setPatientUuid(newPatientUuid);
        Patient patient = new Patient();
        patient.setUuid(newPatientUuid);

        when(patientService.getPatientByUuid(newPatientUuid)).thenReturn(patient);
        when(visitService.getActiveVisitsByPatient(patient)).thenReturn(null);
        when(encounterService.getEncounterTypeByUuid(encounterTypeUuid)).thenReturn(encounterType);

        emrEncounterService.save(encounterTransaction);

        ArgumentCaptor<Visit> argumentCaptor = ArgumentCaptor.forClass(Visit.class);
        verify(visitService).saveVisit(argumentCaptor.capture());
        verify(locationService, times(2)).getLocationByUuid(null);

        assertNull(argumentCaptor.getValue().getLocation());
    }
}
