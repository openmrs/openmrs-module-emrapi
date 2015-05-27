package org.openmrs.module.emrapi.disposition;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.CareSettingType;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.descriptor.MissingConceptException;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
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
    public boolean dispositionsSupported() {
        try {
            if (getDispositionDescriptor() == null) {
                return false;
            }
        }
        catch (MissingConceptException ex) {
            return false;
        }
        return true;
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
    public List<Disposition> getDispositionsByType(DispositionType dispositionType) {
        List<Disposition> dispositions = new ArrayList<Disposition>();
        for (Disposition candidate : getDispositions()) {
            if (dispositionType.equals(candidate.getType())) {  // null-safe since type can be null
                dispositions.add(candidate);
            }
        }
        return dispositions;
    }

    @Override
    public List<Disposition> getValidDispositions(VisitDomainWrapper visitDomainWrapper) {

        // just return all dispositions if the visit isn't active
        if (!visitDomainWrapper.isActive()) {
            return getDispositions();
        }
        else {
            List<Disposition> dispositions = new ArrayList<Disposition>();

            boolean isAdmitted = visitDomainWrapper.isAdmitted();

            for (Disposition candidate : getDispositions()) {
                List<CareSettingType> careSettingTypes = candidate.getCareSettingTypes();

                if (careSettingTypes == null
                        || (isAdmitted && careSettingTypes.contains(CareSettingType.INPATIENT))
                        || (!isAdmitted && careSettingTypes.contains(CareSettingType.OUTPATIENT)) )  {
                    dispositions.add(candidate);
                }
            }
            return dispositions;
        }
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
