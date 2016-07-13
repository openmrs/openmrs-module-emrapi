package org.openmrs.module.emrapi.web.controller;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller to trigger Visit-related EMR-API functionality RESTfully
 * (Currently only supports "ensureActiveVisit")
 */
@Controller
@RequestMapping(value = "/rest/emrapi/activevisit")
public class ActiveVisitController extends BaseRestController {

    @Autowired
    private AdtService adtService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public SimpleObject ensureActiveVisit(@RequestParam("patient") Patient patient,
                                          @RequestParam("location") Location location) {

        SimpleObject response = new SimpleObject();

        Visit visit = adtService.ensureActiveVisit(patient, location);

        if (visit != null) {
            response = (SimpleObject) ConversionUtil.convertToRepresentation(visit, Representation.DEFAULT);
        }

        return response;
    }


}
