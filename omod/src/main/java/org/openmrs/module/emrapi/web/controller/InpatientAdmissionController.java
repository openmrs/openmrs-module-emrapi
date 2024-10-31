package org.openmrs.module.emrapi.web.controller;

import org.openmrs.Location;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.InpatientAdmission;
import org.openmrs.module.emrapi.adt.InpatientAdmissionSearchCriteria;
import org.openmrs.module.emrapi.rest.converter.InpatientAdmissionConverter;
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
public class InpatientAdmissionController {

    @Autowired
    private AdtService adtService;

    @RequestMapping(method = RequestMethod.GET, value = "/rest/**/emrapi/inpatient/admission")
    @ResponseBody
    public SimpleObject getInpatientAdmissions(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(required = false, value = "visitLocation") Location visitLocation,
            @RequestParam(required = false, value = "currentInpatientLocation") List<Location> currentInpatientLocations,
            @RequestParam(required = false, value = "includeDischarged") boolean includeDischarged,
            @RequestParam(required = false, value = "patients") List<String> patients,
            @RequestParam(required = false, value = "visits") List<String> visits
    ) {
        RequestContext context = RestUtil.getRequestContext(request, response, Representation.DEFAULT);
        InpatientAdmissionSearchCriteria criteria = new InpatientAdmissionSearchCriteria();
        criteria.setVisitLocation(visitLocation);
        criteria.setCurrentInpatientLocations(currentInpatientLocations);
        criteria.setIncludeDischarged(includeDischarged);
        criteria.setPatients(patients);
        criteria.setVisits(visits);
        List<InpatientAdmission> requests = adtService.getInpatientAdmissions(criteria);
        return new NeedsPaging<>(requests, context).toSimpleObject(new InpatientAdmissionConverter());
    }
}
