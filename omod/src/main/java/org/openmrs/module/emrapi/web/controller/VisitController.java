package org.openmrs.module.emrapi.web.controller;

import org.hibernate.ObjectNotFoundException;
import org.openmrs.module.emrapi.visit.VisitWithDiagnoses;
import org.openmrs.module.emrapi.visit.VisitWithDiagnosesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
public class VisitController {

    @Autowired
    VisitWithDiagnosesService visitWithDiagnosesService;


    @RequestMapping(method = RequestMethod.GET, value = "/rest/**/emrapi/patient/{patientUuid}/visit")
    public ResponseEntity<?> getVisitsByPatientId(@PathVariable String patientUuid) {
        List<VisitWithDiagnoses> visits;
        try {
            visits = visitWithDiagnosesService.getVisitsByPatientId(patientUuid);
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.ok(visits);
    }
}