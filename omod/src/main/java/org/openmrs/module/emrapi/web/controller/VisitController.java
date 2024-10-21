package org.openmrs.module.emrapi.web.controller;

import org.openmrs.module.emrapi.visit.VisitWithDiagnoses;
import org.openmrs.module.emrapi.visit.VisitWithDiagnosesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
public class VisitController {

    @Autowired
    VisitWithDiagnosesService customVisitService;


    @RequestMapping(method = RequestMethod.GET, value = "/patient/{patientId}")
    public ResponseEntity<List<VisitWithDiagnoses>> getVisitsByPatientId(@PathVariable Integer patientId) {
        List<VisitWithDiagnoses> visits = customVisitService.getVisitsByPatientId(patientId);
        return ResponseEntity.ok(visits);
    }
}