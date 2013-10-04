/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.emrapi.rest.resource;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.bedmanagement.Bed;
import org.openmrs.module.emrapi.bedmanagement.BedManagementService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/beds", supportedClass = Bed.class, supportedOpenmrsVersions = "1.9.*")
public class BedResource extends DelegatingCrudResource<Bed> {
    @Override
    public Bed getByUniqueId(String id) {
        throw new ResourceDoesNotSupportOperationException("getByUniqueId of bed not supported");
    }

    @Override
    protected void delete(Bed bed, String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("delete of bed not supported");
    }

    @Override
    public Bed newDelegate() {
        return new Bed();
    }

    @Override
    public Bed save(Bed bed) {
        throw new ResourceDoesNotSupportOperationException("save of bed not supported");
    }

    @Override
    public void purge(Bed bed, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("purge of bed not supported");
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        if ((rep instanceof DefaultRepresentation) || (rep instanceof RefRepresentation)) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("bedId", "id");
            description.addProperty("bedNumber", "number");
            return description;
        }
        if ((rep instanceof FullRepresentation)) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("bedId", "id");
            description.addProperty("bedNumber", "number");
            return description;
        }
        return null;
    }


    @Override
    public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
        BedManagementService bedManagementService = (BedManagementService) Context.getModuleOpenmrsServices(BedManagementService.class.getName()).get(0);
        Patient patient = Context.getPatientService().getPatientByUuid((String) propertiesToUpdate.get("patientId"));
        Bed bed = bedManagementService.getBedById( Integer.parseInt(uuid));
        Bed bedRes = bedManagementService.assignPatientToBed(patient,bed);
        SimpleObject ret = (SimpleObject) ConversionUtil.convertToRepresentation(bedRes, Representation.DEFAULT);
        return ret;
    }
}
