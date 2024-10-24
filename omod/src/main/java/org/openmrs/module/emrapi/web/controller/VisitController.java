package org.openmrs.module.emrapi.web.controller;

import org.hibernate.ObjectNotFoundException;
import org.openmrs.module.emrapi.visit.VisitWithDiagnoses;
import org.openmrs.module.emrapi.visit.VisitWithDiagnosesService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class VisitController {
    
    @Autowired
    VisitWithDiagnosesService visitWithDiagnosesService;
    
    @RequestMapping(method = RequestMethod.GET, value = "/rest/**/emrapi/patient/{patientUuid}/visit")
    public ResponseEntity<?> getVisitsByPatientId(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable String patientUuid) {
        RequestContext context = RestUtil.getRequestContext(request, response, Representation.DEFAULT);
        List<VisitWithDiagnoses> visits;
        try {
            visits = visitWithDiagnosesService.getVisitsByPatientId(patientUuid, context.getStartIndex(), context.getLimit());
        }
        catch (ObjectNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        
        return new ResponseEntity<>(new NeedsPaging<>(visits, context), HttpStatus.OK);
    }
}
