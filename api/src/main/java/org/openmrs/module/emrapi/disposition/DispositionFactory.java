package org.openmrs.module.emrapi.disposition;


import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class DispositionFactory {

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EmrConceptService emrConceptService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    private ObjectMapper objectMapper = new ObjectMapper();

    private PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();


    public List<Disposition> getDispositions() throws IOException {
        return getDispositionsFrom("dispositionConfig.json");
    }

    public Disposition getDispositionByUniqueId(String uniqueId) throws IOException {
        for (Disposition candidate : getDispositions()) {
            if (candidate.getUuid().equals(uniqueId)) {
                return candidate;
            }
        }
        return null;
    }

    public List<Disposition> getDispositionsFrom(String configFile) throws IOException {
        Resource[] dispositionDefinitions = resourceResolver.getResources("classpath*:/" + configFile);
        for (Resource dispositionDefinition : dispositionDefinitions) {
            return objectMapper.readValue(dispositionDefinition.getInputStream(), new TypeReference<List<Disposition>>() {});
        }
        return null;
    }

    public Disposition getDispositionFromObs(Obs obs) throws IOException {
        for (Disposition candidate : getDispositions()) {
            if (emrConceptService.getConcept(candidate.getConceptCode()).equals(obs.getValueCoded())) {
                return candidate;
            }
        }
        return null;
    }

    public Disposition getDispositionFromObsGroup(Obs obsGroup) throws IOException {
        Obs dispositionObs = emrApiProperties.getDispositionDescriptor().getDispositionObs(obsGroup);

        if (dispositionObs != null) {
            return getDispositionFromObs(dispositionObs);
        }
        return null;
    }

    // to inject mocks during unit test
    protected void setEmrConceptService(EmrConceptService emrConceptService) {
        this.emrConceptService = emrConceptService;
    }

    protected void setConceptService(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }
}
