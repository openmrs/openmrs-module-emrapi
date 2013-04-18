package org.openmrs.module.emrapi.encounter;


import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.User;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncounterDomainWrapperTest {

    private EncounterDomainWrapper encounterDomainWrapper;
    private Encounter encounter;

    @Before
    public void setUp(){
        encounter = mock(Encounter.class);
        encounterDomainWrapper = new EncounterDomainWrapper(encounter);
    }


    @Test
    public void shouldReturnTrueWhenTheCurrentUserCreatedTheEncounter(){
        User currentUser = createUserWithUuid("585812f0-a860-11e2-9e96-0800200c9a66");

        User encounterCreator = createUserWithUuid("585812f0-a860-11e2-9e96-0800200c9a66");

        when(encounter.getCreator()).thenReturn(encounterCreator);

        assertTrue(encounterDomainWrapper.participatedInEncounter(currentUser));
    }


    @Test
    public void shouldReturnTrueWhenTheCurrentUserIsOneOfTheProviders(){
        User currentUser = createUserWithUuid("585812f0-a860-11e2-9e96-0800200c9a66");

        Person providerPerson = createPersonWithUuid("585812f0-a860-11e2-9e96-0800200c9a66");
        Person providerPerson1 = createPersonWithUuid("12345678-a860-11e2-9e96-0800200c9a66");
        Person providerPerson2 = createPersonWithUuid("87654321-a860-11e2-9e96-0800200c9a66");

        Set<EncounterProvider> encounterProviders = createListWithEncounterProviders(providerPerson, providerPerson1, providerPerson2);

        when(encounter.getEncounterProviders()).thenReturn(encounterProviders);

        User encounterCreator = createUserWithUuid("aaaaaaaa-a860-11e2-9e96-0800200c9a66");
        when(encounter.getCreator()).thenReturn(encounterCreator);

        assertTrue(encounterDomainWrapper.participatedInEncounter(currentUser));
    }

    @Test
    public void shouldReturnFalseWhenTheCurrentUserDoesNotParticipateInTheEncounter(){
        User currentUser = createUserWithUuid("585812f0-a860-11e2-9e96-0800200c9a66");

        Person providerPerson = createPersonWithUuid("99999999-a860-11e2-9e96-0800200c9a66");
        Person providerPerson1 = createPersonWithUuid("12345678-a860-11e2-9e96-0800200c9a66");
        Person providerPerson2 = createPersonWithUuid("87654321-a860-11e2-9e96-0800200c9a66");

        Set<EncounterProvider> encounterProviders = createListWithEncounterProviders(providerPerson, providerPerson1, providerPerson2);

        when(encounter.getEncounterProviders()).thenReturn(encounterProviders);

        User encounterCreator = createUserWithUuid("aaaaaaaa-a860-11e2-9e96-0800200c9a66");

        when(encounter.getCreator()).thenReturn(encounterCreator);

        assertFalse(encounterDomainWrapper.participatedInEncounter(currentUser));
    }


    private Set<EncounterProvider> createListWithEncounterProviders(Person... persons) {

        Set<EncounterProvider> encounterProviders = new HashSet<EncounterProvider>();

        for (Person person : persons) {
            EncounterProvider encounterProvider = createEncounterProvider(person);
            encounterProviders.add(encounterProvider);
        }

        return encounterProviders;
    }

    private EncounterProvider createEncounterProvider(Person providerPerson) {
        Provider provider = new Provider();
        provider.setPerson(providerPerson);

        EncounterProvider encounterProvider = new EncounterProvider();
        encounterProvider.setProvider(provider);
        return encounterProvider;
    }

    private User createUserWithUuid(String uuid) {
        Person person = createPersonWithUuid(uuid);

        User encounterCreator = new User(person);
        encounterCreator.setUuid(uuid);
        return encounterCreator;
    }

    private Person createPersonWithUuid(String uuid) {
        Person person = new Person();
        person.setUuid(uuid);
        return person;
    }

}
