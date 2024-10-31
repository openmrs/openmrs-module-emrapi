package org.openmrs.module.emrapi.web.controller;

import org.openmrs.Location;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.InpatientRequest;
import org.openmrs.module.emrapi.adt.InpatientRequestSearchCriteria;
import org.openmrs.module.emrapi.disposition.DispositionType;
import org.openmrs.module.emrapi.rest.converter.InpatientRequestConverter;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class InpatientRequestController {

    @Autowired
    private AdtService adtService;

    @RequestMapping(method = RequestMethod.GET, value = "/rest/**/emrapi/inpatient/request")
    @ResponseBody
    public SimpleObject getInpatientRequests(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required = false, value = "visitLocation") Location visitLocation,
            @RequestParam(required = false, value = "dispositionLocation") List<Location> dispositionLocations,
            @RequestParam(required = false, value = "dispositionType") List<DispositionType> dispositionTypes,
            @RequestParam(required = false, value = "patients") List<String> patients,
            @RequestParam(required = false, value = "visits") List<String> visits
    ) {
        RequestContext context = RestUtil.getRequestContext(request, response, Representation.DEFAULT);
        InpatientRequestSearchCriteria criteria = new InpatientRequestSearchCriteria();
        criteria.setVisitLocation(visitLocation);
        criteria.setDispositionLocations(dispositionLocations);
        criteria.setDispositionTypes(dispositionTypes);
        criteria.setPatients(patients);
        criteria.setVisits(visits);
        List<InpatientRequest> requests = adtService.getInpatientRequests(criteria);
        return new NeedsPaging<>(requests, context).toSimpleObject(new InpatientRequestConverter());
    }
}
