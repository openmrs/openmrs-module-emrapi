package org.openmrs.module.emrapi.web.controller;

import lombok.Setter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.module.emrapi.patient.EmrPatientService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Setter
@Controller
public class VisitController extends BaseRestController {

    @Autowired
    PatientService patientService;

    @Autowired
    DiagnosisService diagnosisService;
    
    @Autowired
    EmrPatientService emrPatientService;

    /**
     * Custom representation supported includes:
     * visit:Visit,diagnoses:List<org.openmrs.Diagnosis>,visitNotes:Obs
     */
	@RequestMapping(method = RequestMethod.GET, value = "/rest/**/emrapi/patient/{patientUuid}/visit")
    public ResponseEntity<?> getVisitsWithDiagnosesAndNotesByPatient(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("patientUuid") String patientUuid) {

        RequestContext context = RestUtil.getRequestContext(request, response, null);
        Representation representation = context.getRepresentation();
        Representation defaultRep = (representation instanceof CustomRepresentation) ? null : representation;

        // Determine what representations to render for each property.  Only compute if requested
        Representation visitRepresentation = defaultRep;
        Representation notesRepresentation = defaultRep;
        Representation diagnosisRepresentation = defaultRep;

        if (context.getRepresentation() instanceof CustomRepresentation) {
            CustomRepresentation customRep = (CustomRepresentation) context.getRepresentation();
            DelegatingResourceDescription customProps  = ConversionUtil.getCustomRepresentationDescription(customRep);
            visitRepresentation = getRepresentation(customProps, "visit");
            diagnosisRepresentation = getRepresentation(customProps, "diagnoses");
            notesRepresentation = getRepresentation(customProps, "visitNotes");
        }

        // First, retrieve the appropriate Visits for the patient
        Map<Visit, SimpleObject> visitData = new LinkedHashMap<>();
        try {
            Patient patient = patientService.getPatientByUuid(patientUuid);
            if (patient == null) {
                throw new APIException("Patient " + patientUuid + " not found");
            }
            List<Visit> visits = emrPatientService.getVisitsForPatient(patient, context.getStartIndex(), context.getLimit());
            for (Visit visit : visits) {
                SimpleObject o = new SimpleObject();
                if (visitRepresentation != null) {
                    o.put("visit", ConversionUtil.convertToRepresentation(visit, visitRepresentation));
                }
                visitData.put(visit, o);
            }
        }
        catch (APIException e) {
            return new ResponseEntity<>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
        }

        // Retrieve diagnoses if requested
        if (diagnosisRepresentation != null) {
            Map<Visit, List<org.openmrs.Diagnosis>> diagnoses = diagnosisService.getDiagnoses(visitData.keySet());
            for (Visit visit : visitData.keySet()) {
                List<Object> diagnosisData = new ArrayList<>();
                List<org.openmrs.Diagnosis> visitDiagnoses = diagnoses.get(visit);
                if (visitDiagnoses != null) {
                    for (org.openmrs.Diagnosis diagnosis : visitDiagnoses) {
                        diagnosisData.add(ConversionUtil.convertToRepresentation(diagnosis, diagnosisRepresentation));
                    }
                }
                visitData.get(visit).add("diagnoses", diagnosisData);
            }
        }

        // Retrieve visitNotes if requested
        if (notesRepresentation != null) {
            Map<Visit, List<Obs>> notes = emrPatientService.getVisitNoteObservations(visitData.keySet());
            for (Visit visit : visitData.keySet()) {
                List<Object> notesData = new ArrayList<>();
                List<Obs> visitNotes = notes.get(visit);
                if (visitNotes != null) {
                    for (Obs visitNoteObs : visitNotes) {
                        notesData.add(ConversionUtil.convertToRepresentation(visitNoteObs, notesRepresentation));
                    }
                }
                visitData.get(visit).add("visitNotes", notesData);
            }
        }

        List<SimpleObject> visitDataToReturn = new ArrayList<>(visitData.values());
        return new ResponseEntity<>(new NeedsPaging<>(visitDataToReturn, context), HttpStatus.OK);
    }

    private Representation getRepresentation(DelegatingResourceDescription drd, String propertyName) {
        DelegatingResourceDescription.Property property = drd.getProperties().get(propertyName);
        if (property == null) {
            return null;
        }
        return property.getRep();
    }
}
