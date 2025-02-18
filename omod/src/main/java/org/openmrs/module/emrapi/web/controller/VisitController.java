package org.openmrs.module.emrapi.web.controller;

import lombok.Setter;
import org.openmrs.Diagnosis;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.module.emrapi.visit.VisitWithDiagnosesAndNotes;
import org.openmrs.module.emrapi.visit.EmrApiVisitService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
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
public class VisitController extends BaseRestController {
    
    @Autowired
    EmrApiVisitService emrApiVisitService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/rest/**/emrapi/patient/{patientUuid}/visitWithDiagnosesAndNotes")
    public ResponseEntity<?> getVisitsWithDiagnosesAndNotesByPatient(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("patientUuid") String patientUuid) {
        
        CustomRepresentation representation = new CustomRepresentation("uuid,display,startDatetime,stopDatetime,patient:REF,visitType:REF,attributes");
        CustomRepresentation notesRepresentation = new CustomRepresentation("uuid,value,obsDatetime,concept:REF");
        
        RequestContext context = RestUtil.getRequestContext(request, response, representation);
        List<VisitWithDiagnosesAndNotes> visitsEntries;
        try {
            visitsEntries = emrApiVisitService.getVisitsWithNotesAndDiagnosesByPatient(patientUuid, context.getStartIndex(), context.getLimit());
        }catch (APIException e) {
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        
        // Convert the visits and diagnoses to SimpleObjects
        List<SimpleObject> convertedVisits = new ArrayList<>();
        for (VisitWithDiagnosesAndNotes visitEntry : visitsEntries) {
            SimpleObject visitEntryObject = new SimpleObject();
            SimpleObject visitObject = (SimpleObject) ConversionUtil.convertToRepresentation(visitEntry.getVisit(), context.getRepresentation());
            List<SimpleObject> convertedDiagnoses = new ArrayList<>();
            List<SimpleObject> convertedVisitNotes = new ArrayList<>();
            
            for (Diagnosis diagnosis : visitEntry.getDiagnoses()) {
                convertedDiagnoses.add((SimpleObject) ConversionUtil.convertToRepresentation(diagnosis, Representation.DEFAULT));
            }
            for (Obs obs : visitEntry.getVisitNotes()) {
                convertedVisitNotes.add((SimpleObject) ConversionUtil.convertToRepresentation(obs, notesRepresentation));
            }
            visitEntryObject.put("visit", visitObject);
            visitEntryObject.put("diagnoses", convertedDiagnoses);
            visitEntryObject.put("visitNotes", convertedVisitNotes);
            
            convertedVisits.add(visitEntryObject);
        }
        
        return new ResponseEntity<>(new NeedsPaging<>(convertedVisits, context), HttpStatus.OK);
    }
}
