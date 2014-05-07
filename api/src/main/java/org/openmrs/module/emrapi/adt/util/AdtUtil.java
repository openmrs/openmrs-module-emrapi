package org.openmrs.module.emrapi.adt.util;

import org.openmrs.Concept;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.disposition.Disposition;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.disposition.DispositionType;

import java.util.ArrayList;
import java.util.List;

public class AdtUtil {

    public static List<Concept> getAdmissionDispositionsConcepts(EmrConceptService emrConceptService, DispositionService dispositionService) {

        List<Disposition> admissionDispositions = dispositionService.getDispositionsByType(DispositionType.ADMIT);

        List<Concept> admissionDispositionConcepts = new ArrayList<Concept>();

        for (Disposition disposition : admissionDispositions) {
            admissionDispositionConcepts.add(emrConceptService.getConcept(disposition.getConceptCode()));
        }

        return admissionDispositionConcepts;
    }


}
