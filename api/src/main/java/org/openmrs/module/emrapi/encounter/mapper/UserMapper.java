package org.openmrs.module.emrapi.encounter.mapper;

import org.openmrs.User;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.stereotype.Component;

@Component("userMapper")
public class UserMapper {
    public EncounterTransaction.User map(User openmrsUser) {
        EncounterTransaction.User user = new EncounterTransaction.User();
        user.setUuid(openmrsUser.getUuid());
        user.setPersonName(openmrsUser.getPersonName().toString());
        return user;
    }
}
