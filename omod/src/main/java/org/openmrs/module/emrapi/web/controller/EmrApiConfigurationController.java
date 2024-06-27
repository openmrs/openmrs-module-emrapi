package org.openmrs.module.emrapi.web.controller;

import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "/rest/emrapi/configuration")
public class EmrApiConfigurationController {

    @Autowired
    private EmrApiProperties emrApiProperties;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public SimpleObject getEmrApiConfiguration(HttpServletRequest request, HttpServletResponse response) {
        RequestContext context = RestUtil.getRequestContext(request, response, Representation.REF);
        return (SimpleObject) ConversionUtil.convertToRepresentation(emrApiProperties, context.getRepresentation());
    }
}
