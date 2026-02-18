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

import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.procedure.Procedure;
import org.openmrs.module.emrapi.procedure.ProcedureService;
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
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/procedure", supportedClass = Procedure.class, supportedOpenmrsVersions = { "2.2 - 9.*" })
public class ProcedureResource extends DelegatingCrudResource<Procedure> {

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("patient", Representation.REF);
			description.addProperty("procedureType", Representation.REF);
			description.addProperty("encounter", Representation.REF);
			description.addProperty("procedureCoded", Representation.REF);
			description.addProperty("procedureNonCoded");
			description.addProperty("bodySite", Representation.REF);
			description.addProperty("startDateTime");
			description.addProperty("estimatedStartDate");
			description.addProperty("endDateTime");
			description.addProperty("duration");
			description.addProperty("durationUnit", Representation.REF);
			description.addProperty("status", Representation.REF);
			description.addProperty("outcomeCoded", Representation.REF);
			description.addProperty("outcomeNonCoded");
			description.addProperty("notes");
			description.addProperty("formNamespace");
			description.addProperty("formFieldPath");
			description.addProperty("voided");
			description.addSelfLink();
			return description;
		}
		return null;
	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("patient");
		description.addProperty("procedureType");
		description.addProperty("encounter");
		description.addProperty("procedureCoded");
		description.addProperty("procedureNonCoded");
		description.addProperty("bodySite");
		description.addProperty("startDateTime");
		description.addProperty("estimatedStartDate");
		description.addProperty("endDateTime");
		description.addProperty("duration");
		description.addProperty("durationUnit");
		description.addProperty("status");
		description.addProperty("outcomeCoded");
		description.addProperty("outcomeNonCoded");
		description.addProperty("notes");
		description.addProperty("formNamespace");
		description.addProperty("formFieldPath");
		return description;
	}

	@Override
	public DelegatingResourceDescription getUpdatableProperties() throws ResourceDoesNotSupportOperationException {
		return getCreatableProperties();
	}

	@Override
	public Procedure newDelegate() {
		return new Procedure();
	}

	@Override
	public Procedure save(Procedure procedure) {
		return Context.getService(ProcedureService.class).saveProcedure(procedure);
	}

	@Override
	public Procedure getByUniqueId(String uuid) {
		return Context.getService(ProcedureService.class).getProcedureByUuid(uuid);
	}

	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		String patientUuid = context.getParameter("patient");
		if (patientUuid != null) {
			Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
			if (patient == null) {
				throw new APIException("Procedure.error.patientNotFound");
			}
			return new NeedsPaging<>(
					Context.getService(ProcedureService.class).getProceduresByPatient(patient), context);
		}
		throw new ResourceDoesNotSupportOperationException("Procedure.error.patientRequired");
	}

	@Override
	protected void delete(Procedure procedure, String reason, RequestContext context) throws ResponseException {
      if(reason == null || reason.trim().isEmpty()) {
         throw new APIException("Procedure.error.voidReasonRequired");
      }
		Context.getService(ProcedureService.class).voidProcedure(procedure, reason);
	}

	@Override
	public void purge(Procedure procedure, RequestContext context) throws ResponseException {
		Context.getService(ProcedureService.class).purgeProcedure(procedure);
	}
}
