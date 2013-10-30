package org.openmrs.module.emrapi.rest.resource;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Resource(name = RestConstants.VERSION_1 + "/patientprofile", supportedClass = PatientProfile.class, supportedOpenmrsVersions = "1.9.*")
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
        return description;
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("patient", Representation.DEFAULT);
        description.addProperty("image", Representation.DEFAULT);
        return description;
    }

    @Override
    public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
        final Object patientProperty = propertiesToCreate.get("patient");
        if (propertiesToCreate.get("patient") == null || !(propertiesToCreate.get("patient") instanceof Map)) {
            throw new ConversionException("The patient property is missing");
        }

        PatientProfile delegate = new PatientProfile();
        PatientResource1_8 patientResource1_8 = (PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(Patient.class);
        delegate.setPatient(patientResource1_8.getPatient(new SimpleObject() {{
            putAll((Map<String, Object>) patientProperty);
        }}));
        propertiesToCreate.removeProperty("patient");

        setConvertedProperties(delegate, propertiesToCreate, getCreatableProperties(), true);
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
        throw new ResourceDoesNotSupportOperationException("retrieve of patient profile not supported");
    }

    @Override
    protected void delete(PatientProfile delegate, String reason, RequestContext context) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("delete of patient profile not supported");
    }

    @Override
    public void purge(PatientProfile delegate, RequestContext context) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("purge of patient profile not supported");
    }

}
