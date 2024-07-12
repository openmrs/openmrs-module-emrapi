package org.openmrs.module.emrapi.rest.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.emrapi.adt.InpatientRequest;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

@Handler(supports = InpatientRequest.class, order = 0)
public class InpatientRequestConverter extends SimpleBeanConverter<InpatientRequest> {

    private final Log log = LogFactory.getLog(getClass());

    @Override
    public DelegatingResourceDescription getResourceDescription(InpatientRequest req, Representation representation) {
        DelegatingResourceDescription ret = super.getResourceDescription(req, representation);
        if (representation instanceof DefaultRepresentation) {
            for (String property : ret.getProperties().keySet()) {
                if (!property.equals("visit")) {
                    ret.addProperty(property, Representation.REF);
                }
            }
        }
        else if (representation instanceof FullRepresentation) {
            for (String property : ret.getProperties().keySet()) {
                if (!property.equals("visit")) {
                    ret.addProperty(property, Representation.DEFAULT);
                }
            }
        }
        return ret;
    }
}
