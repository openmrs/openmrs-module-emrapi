package org.openmrs.module.emrapi.rest.converter;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.emrapi.EmrApiConfiguration;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;

import java.beans.PropertyDescriptor;
import java.util.Map;

@Handler(supports = EmrApiConfiguration.class, order = 0)
public class EmrApiConfigurationConverter implements Converter<EmrApiConfiguration> {

    private final Log log = LogFactory.getLog(getClass());

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
        Map<String, DelegatingResourceDescription.Property> representationProps = null;
        boolean isCustomRep = (representation instanceof CustomRepresentation);
        if (isCustomRep) {
            CustomRepresentation customRep = (CustomRepresentation) representation;
            DelegatingResourceDescription repDesc = ConversionUtil.getCustomRepresentationDescription(customRep);
            representationProps = repDesc.getProperties();
        }
        SimpleObject ret = new SimpleObject();
        for (PropertyDescriptor pd : PropertyUtils.getPropertyDescriptors(EmrApiConfiguration.class)) {
            if (pd.getReadMethod() != null && pd.getReadMethod().getDeclaringClass() == EmrApiConfiguration.class) {
                String propName = pd.getName();
                if (!isCustomRep || representationProps.containsKey(propName)) {
                    Object value = null;
                    // Log exception here in order to return null if properties have not yet been defined, rather than fail
                    try {
                        value = PropertyUtils.getProperty(emrApiConfiguration, pd.getName());
                    }
                    catch (Exception e) {
                        log.debug("Could not get property value " + propName + " from emrapi configuration", e);
                    }
                    Representation rep = isCustomRep ? representationProps.get(propName).getRep() : representation;
                    ret.put(pd.getName(), ConversionUtil.convertToRepresentation(value, rep));
                }
            }
        }
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
