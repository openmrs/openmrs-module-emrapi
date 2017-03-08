package org.openmrs.module.emrapi.rest.resource;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.DateMapper;
import org.openmrs.module.emrapi.patient.PatientProfile;
import org.openmrs.module.emrapi.rest.EmrModuleContext;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PersonResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.RelationShipTypeResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.RelationshipResource1_8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Resource(name = RestConstants.VERSION_1 + "/patientprofile", supportedClass = PatientProfile.class, supportedOpenmrsVersions = {"1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*"})
public class PatientProfileResource extends DelegatingCrudResource<PatientProfile> {

    @Override
    public List<Representation> getAvailableRepresentations() {
        return Arrays.asList(Representation.DEFAULT, Representation.FULL);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("patient", Representation.FULL);
        description.addProperty("image");
        description.addProperty("relationships", Representation.DEFAULT);
        return description;
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("patient", Representation.DEFAULT);
        description.addProperty("image", Representation.DEFAULT);
        description.addProperty("relationships", Representation.DEFAULT);
        return description;
    }

    @Override
    public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("patient", Representation.DEFAULT);
        description.addProperty("image", Representation.DEFAULT);
        description.addProperty("relationships", Representation.DEFAULT);
        return description;
    }

    @Override
    public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
        final Object patientProperty = propertiesToCreate.get("patient");
        if (propertiesToCreate.get("patient") == null || !(propertiesToCreate.get("patient") instanceof Map)) {
            throw new ConversionException("The patient property is missing");
        }

        PatientProfile delegate = new PatientProfile();
        PatientResource1_8 patientResource1_9 = (PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(Patient.class);
        delegate.setPatient(patientResource1_9.getPatient(new SimpleObject() {{
            putAll((Map<String, Object>) patientProperty);
        }}));

        propertiesToCreate.removeProperty("patient");
        delegate.setRelationships(getRelationships(propertiesToCreate, delegate.getPatient()));
        propertiesToCreate.removeProperty("relationships");

        setConvertedProperties(delegate, propertiesToCreate, getCreatableProperties(), true);
        delegate = save(delegate);
        return ConversionUtil.convertToRepresentation(delegate, Representation.FULL);
    }

    private List<Relationship> getRelationships(SimpleObject propertiesToCreate, Person currentPerson) {
        Object relationshipsList = propertiesToCreate.get("relationships");
        List<Relationship> relationships = new ArrayList<Relationship>();
        List<Map<String, Object>> relationshipProperties = (List<Map<String, Object>>) relationshipsList;
        for (final Map<String, Object> relationshipProperty : relationshipProperties) {
            String uuid = getValueFromMap(relationshipProperty, "uuid");
            Relationship relationship;
            if (StringUtils.isBlank(uuid)) {
                relationship = createRelationship(currentPerson, relationshipProperty);
            } else {
                relationship = updateRelationship(relationshipProperty);
            }
            relationships.add(relationship);
        }
        return relationships;
    }

    private Relationship updateRelationship(final Map<String, Object> relationshipJson) {
        String relationshipUuid = getValueFromMap(relationshipJson, "uuid");

        if (StringUtils.isBlank(relationshipUuid)) {
            throw new ConversionException("The relationshipUuid is not present");
        }

        RelationshipResource1_8 relationshipResource = (RelationshipResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(Relationship.class);
        Relationship relationship = relationshipResource.getByUniqueId(relationshipUuid);

        if (null == relationship) {
            throw new ConversionException("Invalid relationship for relationshipUuid " + relationshipUuid);
        }

        relationshipResource.setConvertedProperties(relationship, relationshipJson, relationshipResource.getUpdatableProperties(), true);

        RelationshipType updatedRelationshipType = getRelationshipType((Map<String, Object>) relationshipJson.get("relationshipType"));
        relationship.setRelationshipType(updatedRelationshipType);

        return relationship;
    }

    private Relationship createRelationship(Person currentPerson, Map<String, Object> relationshipJson) {
        Relationship relationship = new Relationship(currentPerson,
                getPerson((Map<String, Object>) relationshipJson.get("personB")),
                getRelationshipType((Map<String, Object>) relationshipJson.get("relationshipType")));
        relationship.setEndDate(new DateMapper().convertUTCToDate(getValueFromMap(relationshipJson, "endDate")));

        return relationship;
    }

    private String getValueFromMap(Map<String, Object> jsonMap, String key) {
        Object value = jsonMap.get(key);
        return ObjectUtils.toString(value);
    }

    private Person getPerson(Map<String, Object> personJson) {
        String personUuid = getValueFromMap(personJson, "uuid");

        if (StringUtils.isBlank(personUuid)) {
            throw new ConversionException("The personUuid is not present.");
        }

        return getPersonFromUuid(personUuid);
    }

    private Person getPersonFromUuid(String personUuid) {
        PersonResource1_8 personResource = (PersonResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(Person.class);
        Person person = personResource.getByUniqueId(personUuid);

        if (person == null) {
            throw new ConversionException("The person does not exist.");
        }
        return person;
    }

    private RelationshipType getRelationshipType(Map<String, Object> relationshipTypeJson) {

        String relationshipTypeUuid = getValueFromMap(relationshipTypeJson, "uuid");

        if (StringUtils.isBlank(relationshipTypeUuid)) {
            throw new ConversionException("The relationshipTypeUuid is not present");
        }

        RelationShipTypeResource1_8 relationshipResource = (RelationShipTypeResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(RelationshipType.class);
        RelationshipType relationshipType = relationshipResource.getByUniqueId(relationshipTypeUuid);

        if (relationshipType == null) {
            throw new ConversionException("The relationship type does not exist.");
        }

        return relationshipType;
    }

    @Override
    public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {

        if (propertiesToUpdate.get("patient") == null || !(propertiesToUpdate.get("patient") instanceof Map)) {
            throw new ConversionException("The patient property is missing");
        }

        PatientProfile delegate = new PatientProfile();

        PatientResource1_8 patientResource1_9 = (PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(Patient.class);
        Patient patient = patientResource1_9.getPatientForUpdate(uuid, (Map<String, Object>) propertiesToUpdate.get("patient"));
        delegate.setPatient(patient);

        propertiesToUpdate.removeProperty("patient");
        setConvertedProperties(delegate, propertiesToUpdate, getUpdatableProperties(), true);
        delegate.setRelationships(getRelationships(propertiesToUpdate, delegate.getPatient()));
        delegate = save(delegate);
        return ConversionUtil.convertToRepresentation(delegate, Representation.FULL);
    }

    @Override
    public PatientProfile newDelegate() {
        return new PatientProfile();
    }

    @Override
    public PatientProfile save(PatientProfile patientProfile) {
        return EmrModuleContext.getEmrPatientProfileService().save(patientProfile);
    }

    @Override
    public PatientProfile getByUniqueId(String patientUuid) {
        throw new ResourceDoesNotSupportOperationException("delete of patient profile not supported");
    }

    @Override
    public Object retrieve(String uuid, RequestContext context) throws ResponseException {
        PatientProfile delegate = EmrModuleContext.getEmrPatientProfileService().get(uuid);

        if (delegate.getPatient() == null) {
            throw new ConversionException("The patient does not exist.");
        }

        return ConversionUtil.convertToRepresentation(delegate, Representation.FULL);
    }

    @Override
    protected void delete(PatientProfile delegate, String reason, RequestContext context) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("delete of patient profile not supported");
    }

    @Override
    public void purge(PatientProfile delegate, RequestContext context) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("delete forever of patient profile not supported");
    }

}