package org.openmrs.module.emrapi.encounter;

import org.openmrs.*;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.emrapi.encounter.domain.*;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional
public interface EmrOrderService {
    @Authorized(PrivilegeConstants.EDIT_ENCOUNTERS)
    void save(List<EncounterTransaction.DrugOrder> drugOrders, Encounter encounter);
    @Authorized(PrivilegeConstants.EDIT_ENCOUNTERS)
    void saveOrders(List<EncounterTransaction.Order> orders, Encounter encounter);
}
