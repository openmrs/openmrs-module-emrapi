package org.openmrs.module.emrapi.web.controller;

import lombok.Setter;
import org.openmrs.Diagnosis;
import org.openmrs.module.emrapi.visit.VisitWithDiagnoses;
import org.openmrs.module.emrapi.visit.EmrApiVisitService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Setter
@Controller
public class VisitController {
    
    EmrApiVisitService emrApiVisitService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/rest/**/emrapi/patient/{patientUuid}/visitWithNotesAndDiagnoses")
    public ResponseEntity<?> getVisitsWithDiagnosesByPatient(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("patientUuid") String patientUuid) {
        RequestContext context = RestUtil.getRequestContext(request, response, Representation.DEFAULT);
        List<VisitWithDiagnoses> visitsEntries;
        visitsEntries = emrApiVisitService.getVisitsWithNotesAndDiagnosesByPatient(patientUuid, context.getStartIndex(), context.getLimit());
        
        // Convert the visits and diagnoses to SimpleObjects
        List<SimpleObject> convertedVisits = new ArrayList<>();
        for (VisitWithDiagnoses visitEntry : visitsEntries) {
            SimpleObject visitEntryObject = new SimpleObject();
            SimpleObject visitObject = (SimpleObject) ConversionUtil.convertToRepresentation(visitEntry.getVisit(), context.getRepresentation());
            List<SimpleObject> convertedDiagnoses = new ArrayList<>();
            
            for (Diagnosis diagnosis : visitEntry.getDiagnoses()) {
                convertedDiagnoses.add((SimpleObject) ConversionUtil.convertToRepresentation(diagnosis, context.getRepresentation()));
            }
            visitEntryObject.put("visit", visitObject);
            visitEntryObject.put("diagnoses", convertedDiagnoses);
            
            convertedVisits.add(visitEntryObject);
        }
        
        return new ResponseEntity<>(new NeedsPaging<>(convertedVisits, context), HttpStatus.OK);
    }
}
