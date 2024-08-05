package org.openmrs.module.emrapi.maternal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.RelationshipType;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.db.EmrApiDAO;

public class MaternalServiceImpl  extends BaseOpenmrsService implements MaternalService {

    private EmrApiProperties emrApiProperties;

    private EmrApiDAO emrApiDAO;

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public void setEmrApiDAO(EmrApiDAO emrApiDAO) {
        this.emrApiDAO = emrApiDAO;
    }

    public List<Newborn> getNewbornsByMother(Patient mother, Location visitLocation) {

        RelationshipType motherChildRelationshipType = emrApiProperties.getMotherChildRelationshipType();

        if (motherChildRelationshipType == null) {
            throw new APIException("Mother-Child relationship type has not been configured");
        }

        if (mother == null) {
            throw new APIException("Mother cannot be null");
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("mother", mother);
        parameters.put("motherChildRelationshipType", motherChildRelationshipType);
        parameters.put("visitLocation", visitLocation);

        List<?> l = emrApiDAO.executeHqlFromResource("hql/newborns_by_mother.hql", parameters, List.class);

        List<Newborn> ret = new ArrayList<>();

        for (Object req : l) {
            Object[] row = (Object[]) req;
            Newborn newborn = new Newborn();
            newborn.setNewborn((Patient) row[0]);
            newborn.setNewbornVisit((Visit) row[1]);
            ret.add(newborn);
        }

        return ret;
    }
}
