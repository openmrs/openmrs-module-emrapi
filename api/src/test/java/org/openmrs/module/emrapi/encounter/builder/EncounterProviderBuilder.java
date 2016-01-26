package org.openmrs.module.emrapi.encounter.builder;

import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;

import java.util.HashSet;

public class EncounterProviderBuilder {

    private EncounterProvider encounterProvider;

    public EncounterProviderBuilder() {
        encounterProvider = new EncounterProvider();
        Provider provider = new Provider();
        EncounterRole role = new EncounterRole();
        Person person = new Person(1234);
        HashSet<PersonName> names = new HashSet<PersonName>();
        PersonName personName = new PersonName("Yogesh", "", "Jain");
        names.add(personName);
        person.setNames(names);
        provider.setPerson(person);
        encounterProvider.setEncounterRole(role);
        encounterProvider.setProvider(provider);
    }

    public EncounterProvider build() {
        return encounterProvider;
    }
}
