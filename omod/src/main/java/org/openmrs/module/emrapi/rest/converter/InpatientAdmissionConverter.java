package org.openmrs.module.emrapi.rest.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.emrapi.adt.InpatientAdmission;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

@Handler(supports = InpatientAdmission.class, order = 0)
public class InpatientAdmissionConverter extends SimpleBeanConverter<InpatientAdmission> {

    private final Log log = LogFactory.getLog(getClass());

    @Override
    public DelegatingResourceDescription getResourceDescription(InpatientAdmission req, Representation representation) {
        DelegatingResourceDescription ret = super.getResourceDescription(req, representation);
        if (representation instanceof DefaultRepresentation) {
            DelegatingResourceDescription rep = new DelegatingResourceDescription();
            rep.addProperty("visit", Representation.DEFAULT);
            rep.addProperty("currentInpatientLocation", Representation.REF);
            rep.addProperty("firstAdmissionOrTransferEncounter", getEncounterRepresentation());
            rep.addProperty("latestAdmissionOrTransferEncounter", getEncounterRepresentation());
            rep.addProperty("encounterAssigningToCurrentInpatientLocation", getEncounterRepresentation());
            rep.addProperty("discharged");
            return rep;
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

    public Representation getEncounterRepresentation() {
        return new CustomRepresentation("uuid,display,encounterDatetime,location:ref,encounterType:ref");
    }
}
