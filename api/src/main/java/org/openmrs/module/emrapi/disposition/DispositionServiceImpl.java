package org.openmrs.module.emrapi.disposition;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

public class DispositionServiceImpl extends BaseOpenmrsService implements DispositionService  {

    private ConceptService conceptService;

    private EmrConceptService emrConceptService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    // TODO inject this in some better way than using a setter to override?
    private String dispositionConfig = "dispositionConfig.json";

    public DispositionServiceImpl(ConceptService conceptService, EmrConceptService emrConceptService) {
        this.conceptService = conceptService;
        this.emrConceptService = emrConceptService;
    }

    @Override
    public DispositionDescriptor getDispositionDescriptor() {
        // TODO handle this better--this property is only used to allow use to inject a mock disposition descriptor
        if (dispositionDescriptor != null) {
            return dispositionDescriptor;
        }
        return new DispositionDescriptor(conceptService);
    }

    @Override
    public List<Disposition> getDispositions() {
        return getDispositionsFrom(dispositionConfig);
    }

    @Override
    @Transactional(readOnly = true)
    public Disposition getDispositionByUniqueId(String uniqueId) {
        for (Disposition candidate : getDispositions()) {
            if (candidate.getUuid().equals(uniqueId)) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Disposition getDispositionFromObs(Obs obs)  {
        for (Disposition candidate : getDispositions()) {
            if (emrConceptService.getConcept(candidate.getConceptCode()).equals(obs.getValueCoded())) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Disposition getDispositionFromObsGroup(Obs obsGroup)  {
        Obs dispositionObs = getDispositionDescriptor().getDispositionObs(obsGroup);

        if (dispositionObs != null) {
            return getDispositionFromObs(dispositionObs);
        }
        return null;
    }

    @Override
    public void setDispositionConfig(String dispositionConfig) {
        this.dispositionConfig = dispositionConfig;
    }

    private List<Disposition> getDispositionsFrom(String configFile)  {

        try {
            Resource[] dispositionDefinitions = resourceResolver.getResources("classpath*:/" + configFile);
            for (Resource dispositionDefinition : dispositionDefinitions) {
                return objectMapper.readValue(dispositionDefinition.getInputStream(), new TypeReference<List<Disposition>>() {});
            }
            return null;
        }
        catch (IOException e) {
            throw new RuntimeException ("Unable to read disposition file " + configFile, e);
        }

    }

    // TODO handle this better--this property is only used to allow use to inject a mock disposition descriptor
    private DispositionDescriptor dispositionDescriptor;

    protected void setDispositionDescriptor(DispositionDescriptor dispositionDescriptor) {
        this.dispositionDescriptor = dispositionDescriptor;
    }
}
