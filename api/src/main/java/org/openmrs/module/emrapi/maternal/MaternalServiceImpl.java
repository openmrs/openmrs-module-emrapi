package org.openmrs.module.emrapi.maternal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openmrs.Patient;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.springframework.transaction.annotation.Transactional;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.InpatientAdmission;
import org.openmrs.module.emrapi.adt.InpatientAdmissionSearchCriteria;
import org.openmrs.module.emrapi.db.EmrApiDAO;

@Transactional(readOnly = true)
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

    public List<MotherAndChild> getMothersAndChildren(MothersAndChildrenSearchCriteria criteria) {

        RelationshipType motherChildRelationshipType = emrApiProperties.getMotherChildRelationshipType();

        if (motherChildRelationshipType == null) {
            throw new APIException("Mother-Child relationship type has not been configured");
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("motherUuids", criteria.getMotherUuids());
        parameters.put("restrictByMothers", criteria.getMotherUuids() != null);
        parameters.put("childUuids", criteria.getChildUuids());
        parameters.put("restrictByChildren", criteria.getChildUuids() != null);
        parameters.put("motherChildRelationshipType", motherChildRelationshipType);
        parameters.put("motherRequiredToHaveActiveVisit", criteria.isMotherRequiredToHaveActiveVisit());
        parameters.put("childRequiredToHaveActiveVisit", criteria.isChildRequiredToHaveActiveVisit());
        parameters.put("childRequiredToBeBornDuringMothersActiveVisit", criteria.isChildRequiredToBeBornDuringMothersActiveVisit());

        List<?> l = emrApiDAO.executeHqlFromResource("hql/mother_child.hql", parameters, List.class);

        List<MotherAndChild> ret = new ArrayList<>();

        for (Object req : l) {
            Object[] row = (Object[]) req;
            MotherAndChild mothreAndChild = new MotherAndChild();
            mothreAndChild.setMother((Patient) row[0]);
            mothreAndChild.setChild((Patient) row[1]);
            ret.add(mothreAndChild);
        }

        // now fetch all the admissions for children in the result set
        InpatientAdmissionSearchCriteria inpatientAdmissionSearchCriteria = new InpatientAdmissionSearchCriteria();
        Set<Integer> patientIds  = ret.stream().map(MotherAndChild::getChild).map(Patient::getId).collect(Collectors.toSet());
        patientIds.addAll(ret.stream().map(MotherAndChild::getMother).map(Patient::getId).collect(Collectors.toSet()));
        inpatientAdmissionSearchCriteria.setPatientIds(new ArrayList<>(patientIds));
        List<InpatientAdmission> admissions = adtService.getInpatientAdmissions(inpatientAdmissionSearchCriteria);
        Map<Patient, InpatientAdmission> admissionsByPatient = new HashMap<>();
        for (InpatientAdmission admission : admissions) {
            admissionsByPatient.put(admission.getVisit().getPatient(), admission);
        }
        for (MotherAndChild motherAndChild : ret) {
            motherAndChild.setMotherAdmission(admissionsByPatient.get(motherAndChild.getMother()));
            motherAndChild.setChildAdmission(admissionsByPatient.get(motherAndChild.getChild()));
        }

        return ret;
    }

}
