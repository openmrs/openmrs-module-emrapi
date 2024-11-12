package org.openmrs.module.emrapi.web.controller;

import org.openmrs.Location;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class LocationThatSupportsVisitsController {

    @Autowired
    private AdtService adtService;

    @RequestMapping(method = RequestMethod.GET, value = "/rest/**/emrapi/locationThatSupportsVisits")
    @ResponseBody
    public SimpleObject getLocationThatSupportsVisits(HttpServletRequest request,
                                                      HttpServletResponse response,
                                                      @RequestParam(required = true, value = "location") Location location) {

        SimpleObject res = new SimpleObject();

        RequestContext context = RestUtil.getRequestContext(request, response, Representation.DEFAULT);
        Location visitLocation = adtService.getLocationThatSupportsVisits(location);

        if (visitLocation != null) {
            res = (SimpleObject) ConversionUtil.convertToRepresentation(visitLocation, context.getRepresentation());
        }

        return res;
    }

}
