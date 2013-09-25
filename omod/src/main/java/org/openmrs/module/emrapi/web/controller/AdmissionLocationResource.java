package org.openmrs.module.emrapi.web.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.bedmanagement.BedManagementService;
import org.openmrs.module.emrapi.bedmanagement.domain.AdmissionLocation;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.Arrays;
import java.util.List;

//@Controller
//@RequestMapping(value = "/rest/emrapi/admissionLocations")
//public class AdmissionLocationController extends BaseRestController {
@Resource(name = RestConstants.VERSION_1 + "/admissionLocation", supportedClass = AdmissionLocation.class, supportedOpenmrsVersions = "1.9.*")
public class AdmissionLocationResource extends DelegatingCrudResource<AdmissionLocation> {

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        BedManagementService bedManagementService = (BedManagementService) Context.getModuleOpenmrsServices(BedManagementService.class.getName()).get(0);
        List<AdmissionLocation> admissionLocations = bedManagementService.getAllAdmissionLocations();
        return new AlreadyPaged<AdmissionLocation>(context, admissionLocations, false);
    }

    @Override
    public List<Representation> getAvailableRepresentations() {
        return Arrays.asList(Representation.DEFAULT, Representation.FULL);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("name");
        description.addProperty("description");
        description.addProperty("totalBeds");
        description.addProperty("occupiedBeds");
        return description;
    }

    @Override
    public AdmissionLocation getByUniqueId(String s) {
        throw new ResourceDoesNotSupportOperationException("search of admission location not supported");
    }

    @Override
    protected void delete(AdmissionLocation admissionLocation, String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("delete of admission location not supported");
    }

    @Override
    public void purge(AdmissionLocation admissionLocation, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("purge of admission location not supported");
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        throw new ResourceDoesNotSupportOperationException("create of admission location not supported");
    }

    @Override
    public AdmissionLocation newDelegate() {
        return new AdmissionLocation();
    }

    @Override
    public AdmissionLocation save(AdmissionLocation admissionLocation) {
        throw new ResourceDoesNotSupportOperationException("save of admission location not supported");
    }
//
//    @RequestMapping(method = RequestMethod.GET)
//    @ResponseBody
//    public List<AdmissionLocation> getAdmissionLocations() {
//        return bedManagementService.getAllAdmissionLocations();
//    }

}
