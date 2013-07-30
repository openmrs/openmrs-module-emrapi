package org.openmrs.module.emrapi.diagnosis;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;
import org.openmrs.module.emrapi.concept.EmrConceptService;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class ConceptCodeDeserializer extends JsonDeserializer<Concept> {

    private EmrConceptService emrConceptService;

    public ConceptCodeDeserializer() {
        // I haven't been able to figure out how to wire this into some shared Jackson object
        //emrConceptService = Context.getService(EmrConceptService.class);
        emrConceptService = new EmrConceptService() {
            @Override
            public List<Concept> getConceptsSameOrNarrowerThan(ConceptReferenceTerm term) {
                return null; // not needed here
            }

            @Override
            public Concept getConcept(String mappingOrUuid) {
                Concept concept = new Concept();
                concept.setUuid(mappingOrUuid);
                return concept;
            }

            @Override
            public List<ConceptSearchResult> conceptSearch(String query, Locale locale, Collection<ConceptClass> classes, Collection<Concept> inSets, Collection<ConceptSource> sources, Integer limit) {
                return null; // not needed here
            }
        };
    }

    @Override
    public Concept deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String conceptCode = jp.getText();
        Concept concept = emrConceptService.getConcept(conceptCode);
        if (concept == null) {
            throw ctxt.instantiationException(Concept.class, "No concept with code or uuid: " + conceptCode);
        }
        return concept;
    }

}
