package org.openmrs.module.emrapi.web.controller;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

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
            @RequestParam(required = false, value = "patients") List<Patient> patients,
            @RequestParam(required = false, value = "visits") List<Visit> visits
    ) {
        RequestContext context = RestUtil.getRequestContext(request, response, Representation.DEFAULT);
        InpatientRequestSearchCriteria criteria = new InpatientRequestSearchCriteria();
        criteria.setVisitLocation(visitLocation);
        criteria.setDispositionLocations(dispositionLocations);
        criteria.setDispositionTypes(dispositionTypes);

        if(patients != null) {
            criteria.setPatientIds(patients.stream().map(Patient::getId).collect(Collectors.toList()));
        }
        if(visits != null) {
            criteria.setVisitIds(visits.stream().map(Visit::getId).collect(Collectors.toList()));
        }
        List<InpatientRequest> requests = adtService.getInpatientRequests(criteria);
        return new NeedsPaging<>(requests, context).toSimpleObject(new InpatientRequestConverter());
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<SimpleObject> handleException (Exception ex) {
        SimpleObject error = new SimpleObject();
        error.put("error", ex.getClass().getName());
        error.put("message", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
