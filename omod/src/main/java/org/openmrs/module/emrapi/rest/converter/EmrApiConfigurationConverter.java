package org.openmrs.module.emrapi.rest.converter;

import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.module.emrapi.EmrApiConfiguration;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.naming.OperationNotSupportedException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Handler(supports = EmrApiConfiguration.class, order = 0)
public class EmrApiConfigurationConverter implements Converter<EmrApiConfiguration> {

    @Override
    public EmrApiConfiguration newInstance(String s) {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public EmrApiConfiguration getByUniqueId(String s) {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public SimpleObject asRepresentation(EmrApiConfiguration emrApiConfiguration, Representation representation) throws ConversionException {
        SimpleObject ret = new SimpleObject();
        ret.put("properties", ConversionUtil.convertToRepresentation(emrApiConfiguration.getProperties(), representation));
        return ret;
    }

    @Override
    public Object getProperty(EmrApiConfiguration emrApiConfiguration, String s) throws ConversionException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public void setProperty(Object o, String s, Object o1) throws ConversionException {
        throw new ResourceDoesNotSupportOperationException();
    }
}
