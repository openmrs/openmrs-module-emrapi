package org.openmrs.module.emrapi.encounter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.Provider;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.Iterator;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class EncounterProviderServiceHelperTest {

    private EncounterProviderServiceHelper encounterProviderServiceHelper;

    @Mock
    private ProviderService providerService;

    @Mock
    private EncounterService encounterService;

    @Before
    public void setUp() {

        initMocks(this);

        Provider provider = new Provider();
        provider.setName("provider-name");
        provider.setUuid("provider-uuid");
        when(providerService.getProviderByUuid("provider-uuid")).thenReturn(provider);

        Provider anotherProvider = new Provider();
        anotherProvider.setName("another-provider-name");
        anotherProvider.setUuid("another-provider-uuid");
        when(providerService.getProviderByUuid("another-provider-uuid")).thenReturn(anotherProvider);

        EncounterRole unknownRole = new EncounterRole();
        unknownRole.setUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID);
        when(encounterService.getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID)).thenReturn(unknownRole);

        EncounterRole role = new EncounterRole();
        role.setUuid("role-uuid");
        when(encounterService.getEncounterRoleByUuid("role-uuid")).thenReturn(role);


        EncounterRole anotherRole = new EncounterRole();
        anotherRole.setUuid("another-role-uuid");
        when(encounterService.getEncounterRoleByUuid("another-role-uuid")).thenReturn(anotherRole);

        PowerMockito.mockStatic(Context.class);

        encounterProviderServiceHelper = new EncounterProviderServiceHelper(providerService, encounterService);
    }

    @Test
    public void shouldAddProvider() {
        Encounter encounter = new Encounter();
        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setUuid("provider-uuid");
        provider.setName("provider-name");
        provider.setEncounterRoleUuid("role-uuid");

        encounterProviderServiceHelper.update(encounter, Collections.singleton(provider));

        assertThat(encounter.getEncounterProviders().size(), is(equalTo(1)));

        EncounterProvider encounterProvider = encounter.getEncounterProviders().iterator().next();
        assertThat(encounterProvider.getProvider().getUuid(), is(equalTo("provider-uuid")));
        assertThat(encounterProvider.getProvider().getName(), is(equalTo("provider-name")));
        assertThat(encounterProvider.getEncounterRole().getUuid(), is(equalTo("role-uuid")));
    }

    @Test
    public void shouldSetEncounterRoleToUknownIfNotSpecified() {
        Encounter encounter = new Encounter();
        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setUuid("provider-uuid");
        provider.setName("provider-name");

        encounterProviderServiceHelper.update(encounter, Collections.singleton(provider));

        assertThat(encounter.getEncounterProviders().size(), is(equalTo(1)));

        EncounterProvider encounterProvider = encounter.getEncounterProviders().iterator().next();
        assertThat(encounterProvider.getProvider().getUuid(), is(equalTo("provider-uuid")));
        assertThat(encounterProvider.getProvider().getName(), is(equalTo("provider-name")));
        assertThat(encounterProvider.getEncounterRole().getUuid(), is(equalTo(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID)));
    }

    @Test
    public void shoulAddSecondProvider() {

        Encounter encounter = new Encounter();

        Provider provider = new Provider();
        provider.setUuid("provider-uuid");

        EncounterRole role = new EncounterRole();
        role.setUuid("role-uuid");
        encounter.addProvider(role, provider);

        EncounterTransaction.Provider encounterTransactionProvider = new EncounterTransaction.Provider();
        encounterTransactionProvider.setUuid("another-provider-uuid");
        encounterTransactionProvider.setEncounterRoleUuid("role-uuid");

        // sanity check
        assertThat(encounter.getEncounterProviders().size(), is(equalTo(1)));

        encounterProviderServiceHelper.update(encounter, Collections.singleton(encounterTransactionProvider));

        assertThat(encounter.getEncounterProviders().size(), is(equalTo(2)));

        Iterator<EncounterProvider> i = encounter.getEncounterProviders().iterator();
        EncounterProvider encounterProvider1 = i.next();
        EncounterProvider encounterProvider2 = i.next();

        assertTrue(encounterProvider1.getProvider().getUuid().equals("provider-uuid") && encounterProvider2.getProvider().getUuid().equals("another-provider-uuid") ||
                encounterProvider1.getProvider().getUuid().equals("another-provider-uuid") && encounterProvider2.getProvider().getUuid().equals("provider-uuid"));

        assertThat(encounterProvider1.getEncounterRole().getUuid(), is(equalTo("role-uuid")));
        assertThat(encounterProvider2.getEncounterRole().getUuid(), is(equalTo("role-uuid")));
    }

    @Test
    public void shouldNotAddProviderIfAlreadyAssociatedWithEncounterWithSpecifiedEncounterRole() {

        Encounter encounter = new Encounter();

        Provider provider = new Provider();
        provider.setUuid("provider-uuid");

        EncounterRole role = new EncounterRole();
        role.setUuid("role-uuid");
        encounter.addProvider(role, provider);

        EncounterTransaction.Provider encounterTransactionProvider = new EncounterTransaction.Provider();
        encounterTransactionProvider.setUuid("provider-uuid");
        encounterTransactionProvider.setEncounterRoleUuid("role-uuid");

        // sanity check
        assertThat(encounter.getEncounterProviders().size(), is(equalTo(1)));

        encounterProviderServiceHelper.update(encounter, Collections.singleton(encounterTransactionProvider));

        assertThat(encounter.getEncounterProviders().size(), is(equalTo(1)));

        EncounterProvider encounterProvider = encounter.getEncounterProviders().iterator().next();
        assertThat(encounterProvider.getProvider().getUuid(), is(equalTo("provider-uuid")));
        assertThat(encounterProvider.getEncounterRole().getUuid(), is(equalTo("role-uuid")));
    }

    @Test
    public void shouldAddProviderIfAlreadyAssociatedWithEncounterButWithDifferentEncounterRole() {

        Encounter encounter = new Encounter();

        Provider provider = new Provider();
        provider.setUuid("provider-uuid");

        EncounterRole role = new EncounterRole();
        role.setUuid("role-uuid");
        encounter.addProvider(role, provider);

        EncounterTransaction.Provider encounterTransactionProvider = new EncounterTransaction.Provider();
        encounterTransactionProvider.setUuid(provider.getUuid());
        encounterTransactionProvider.setEncounterRoleUuid("another-role-uuid");

        // sanity check
        assertThat(encounter.getEncounterProviders().size(), is(equalTo(1)));

        encounterProviderServiceHelper.update(encounter, Collections.singleton(encounterTransactionProvider));

        assertThat(encounter.getEncounterProviders().size(), is(equalTo(2)));

        Iterator<EncounterProvider> i = encounter.getEncounterProviders().iterator();
        EncounterProvider encounterProvider1 = i.next();
        EncounterProvider encounterProvider2 = i.next();

        assertThat(encounterProvider1.getProvider().getUuid(), is(equalTo("provider-uuid")));
        assertThat(encounterProvider2.getProvider().getUuid(), is(equalTo("provider-uuid")));

        EncounterRole role1 = encounterProvider1.getEncounterRole();
        EncounterRole role2 = encounterProvider2.getEncounterRole();

        assertTrue(role1.getUuid().equals("role-uuid") && role2.getUuid().equals("another-role-uuid") ||
                role1.getUuid().equals("another-role-uuid") && role2.getUuid().equals("role-uuid"));
    }
}
