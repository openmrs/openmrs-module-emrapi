/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.rest.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.procedure.ProcedureTypeService;
import org.openmrs.module.emrapi.procedure.ProcedureType;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/proceduretype", supportedClass = ProcedureType.class, supportedOpenmrsVersions = { "2.2 - 9.*" })
public class ProcedureTypeResource extends DelegatingCrudResource<ProcedureType> {

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("retired");
			description.addSelfLink();
			return description;
		}
		return null;
	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("name");
		description.addProperty("description");
		return description;
	}

	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		return getCreatableProperties();
	}

	@Override
	public ProcedureType newDelegate() {
		return new ProcedureType();
	}

	@Override
	public ProcedureType save(ProcedureType procedureType) {
		return Context.getService(ProcedureTypeService.class).saveProcedureType(procedureType);
	}

	@Override
	public ProcedureType getByUniqueId(String uuid) {
		return Context.getService(ProcedureTypeService.class).getProcedureTypeByUuid(uuid);
	}

	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<>(
				Context.getService(ProcedureTypeService.class).getAllProcedureTypes(context.getIncludeAll()), context);
	}

	@Override
	protected void delete(ProcedureType procedureType, String reason, RequestContext context) throws ResponseException {
		Context.getService(ProcedureTypeService.class).retireProcedureType(procedureType, reason);
	}

	@Override
	public void purge(ProcedureType procedureType, RequestContext context) throws ResponseException {
		Context.getService(ProcedureTypeService.class).purgeProcedureType(procedureType);
	}
}
