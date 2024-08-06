package org.openmrs.module.emrapi.maternal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.RelationshipType;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.InpatientAdmission;
import org.openmrs.module.emrapi.adt.InpatientAdmissionSearchCriteria;
import org.openmrs.module.emrapi.db.EmrApiDAO;

public class MaternalServiceImpl  extends BaseOpenmrsService implements MaternalService {

    private EmrApiProperties emrApiProperties;

    private AdtService adtService;

    private EmrApiDAO emrApiDAO;

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public void setEmrApiDAO(EmrApiDAO emrApiDAO) {
        this.emrApiDAO = emrApiDAO;
    }

    public void setAdtService(AdtService adtService) {
        this.adtService = adtService;
    }

    public List<Newborn> getNewbornsByMother(List<Patient> mothers) {

        RelationshipType motherChildRelationshipType = emrApiProperties.getMotherChildRelationshipType();

        if (motherChildRelationshipType == null) {
            throw new APIException("Mother-Child relationship type has not been configured");
        }

        if (mothers == null || mothers.isEmpty()) {
            throw new APIException("No mothers provided");
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("mothers", mothers);
        parameters.put("motherChildRelationshipType", motherChildRelationshipType);

        List<?> l = emrApiDAO.executeHqlFromResource("hql/newborns_by_mother.hql", parameters, List.class);

        List<Newborn> ret = new ArrayList<>();
        List<Visit> visits = new ArrayList<>();

        for (Object req : l) {
            Object[] row = (Object[]) req;
            Newborn newborn = new Newborn();
            newborn.setNewborn((Patient) row[0]);
            newborn.setMother((Patient) row[1]);
            visits.add((Visit) row[2]);
            ret.add(newborn);
        }

        // now fetch all the admissions for newborns in the result set
        InpatientAdmissionSearchCriteria criteria = new InpatientAdmissionSearchCriteria();
        criteria.setVisitIds(visits.stream().map(Visit::getId).collect(Collectors.toList()));
        List<InpatientAdmission> admissions = adtService.getInpatientAdmissions(criteria);
        Map<Patient, InpatientAdmission> admissionsByPatient = new HashMap<>();
        if (admissions != null) {
            for (InpatientAdmission admission : admissions) {
                admissionsByPatient.put(admission.getVisit().getPatient(), admission);
            }
        }
        for (Newborn newborn : ret) {
            newborn.setNewbornAdmission(admissionsByPatient.get(newborn.getNewborn()));
        }

        return ret;
    }

    public List<Mother> getMothersByNewborn(List<Patient> newborns) {
        RelationshipType motherChildRelationshipType = emrApiProperties.getMotherChildRelationshipType();

        if (motherChildRelationshipType == null) {
            throw new APIException("Mother-Child relationship type has not been configured");
        }

        if (newborns == null || newborns.isEmpty()) {
            throw new APIException("No newborns provided");
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("babies", newborns);
        parameters.put("motherChildRelationshipType", motherChildRelationshipType);

        List<?> l = emrApiDAO.executeHqlFromResource("hql/mothers_by_newborn.hql", parameters, List.class);

        List<Mother> ret = new ArrayList<>();
        List<Visit> visits = new ArrayList<>();

        for (Object req : l) {
            Object[] row = (Object[]) req;
            Mother mother = new Mother();
            mother.setMother((Patient) row[0]);
            mother.setNewborn((Patient) row[1]);
            visits.add((Visit) row[2]);
            ret.add(mother);
        }

        // now fetch all the admissions for mothers in the result set
        InpatientAdmissionSearchCriteria criteria = new InpatientAdmissionSearchCriteria();
        criteria.setVisitIds(visits.stream().map(Visit::getId).collect(Collectors.toList()));
        List<InpatientAdmission> admissions = adtService.getInpatientAdmissions(criteria);
        Map<Patient, InpatientAdmission> admissionsByPatient = new HashMap<>();
        if (admissions != null) {
            for (InpatientAdmission admission : admissions) {
                admissionsByPatient.put(admission.getVisit().getPatient(), admission);
            }
        }
        for (Mother mother : ret) {
            mother.setMotherAdmission(admissionsByPatient.get(mother.getMother()));
        }


        return ret;
    }
}
