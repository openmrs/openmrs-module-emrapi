package org.openmrs.module.emrapi.rest.converter;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.disposition.Disposition;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionObs;
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

@Handler(supports = {
        EmrApiProperties.class,
        DiagnosisMetadata.class,
        Disposition.class,
        DispositionObs.class,
        DispositionDescriptor.class
}, order = 0)
public class SimpleBeanConverter<T> implements Converter<T> {

    private final Log log = LogFactory.getLog(getClass());

    /**
     * @return a resource description that represents a custom representation, or one that represents all bean properties in the class
     */
    public DelegatingResourceDescription getResourceDescription(T o, Representation representation) {
        if (representation instanceof CustomRepresentation) {
            return ConversionUtil.getCustomRepresentationDescription((CustomRepresentation) representation);
        }
        DelegatingResourceDescription ret = new DelegatingResourceDescription();
        for (PropertyDescriptor pd : PropertyUtils.getPropertyDescriptors(o.getClass())) {
            if (pd.getReadMethod() != null && pd.getReadMethod().getDeclaringClass() == o.getClass()) {
                String propName = pd.getName();
                ret.addProperty(propName, representation);
            }
        }
        return ret;
    }

    @Override
    public SimpleObject asRepresentation(T o, Representation rep) throws ConversionException {
        SimpleObject ret = new SimpleObject();
        Map<String, DelegatingResourceDescription.Property> props = getResourceDescription(o, rep).getProperties();
        for (String propName : props.keySet()) {
            Object value = null;
            // Log exception rather than fail if an exception is thrown while trying to retrieve a property
            try {
                value = PropertyUtils.getProperty(o, propName);
            }
            catch (Exception e) {
                log.debug("Could not get property value " + propName + " from " + o.getClass(), e);
            }
            ret.put(propName, ConversionUtil.convertToRepresentation(value, props.get(propName).getRep()));
        }
        return ret;
    }

    @Override
    public T newInstance(String s) {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public T getByUniqueId(String s) {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public Object getProperty(T o, String s) throws ConversionException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public void setProperty(Object o, String s, Object o1) throws ConversionException {
        throw new ResourceDoesNotSupportOperationException();
    }
}
