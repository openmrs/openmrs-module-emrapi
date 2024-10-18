package org.openmrs.module.emrapi.disposition;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.openmrs.CareSetting;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.descriptor.MissingConceptException;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DispositionServiceImpl extends BaseOpenmrsService implements DispositionService  {

    private ConceptService conceptService;

    private EmrConceptService emrConceptService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    // TODO inject this in some better way than using a setter to override?
    private String dispositionConfig;

    private final String DEFAULT_DISPOSITION_CONFIG_LOCATION = "file:" + OpenmrsUtil.getApplicationDataDirectory() + "/configuration/dispositions/dispositionConfig.json";

    private final String LEGACY_DEFAULT_DISPOSITION_CONFIG_LOCATION = "classpath*:/dispositionConfig.json";  // prior to 2.1.0 release

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

        if (dispositionConfig !=null) {
            try {
                String path;
                if (!dispositionConfig.contains("file:")) {
                    path = "classpath*:/" + dispositionConfig;
                } else {
                    path = "file:" + OpenmrsUtil.getApplicationDataDirectory() + "/" + dispositionConfig.replace("file:", "");
                }
                return getDispositionsFrom(path);
            } catch (IOException ignored) {
                // if this fails for some reason, we will try the default locations below
            }
        }

        // if no config file specified, try the default locations
        try {
            return getDispositionsFrom(DEFAULT_DISPOSITION_CONFIG_LOCATION);
        } catch (IOException ignored) {
            // exception will be thrown below if needed
        }

        try {
            return getDispositionsFrom(LEGACY_DEFAULT_DISPOSITION_CONFIG_LOCATION);
        } catch (IOException ignored) {
            // exception will be thrown below if needed
        }

        throw new RuntimeException("No disposition file found at: " + dispositionConfig + " or "  + DEFAULT_DISPOSITION_CONFIG_LOCATION + " or " + LEGACY_DEFAULT_DISPOSITION_CONFIG_LOCATION);
    }

    private List<Disposition> getDispositionsFrom(String path) throws IOException {
        Resource[] dispositionDefinitions = resourceResolver.getResources(path);
        for (Resource dispositionDefinition : dispositionDefinitions) {
            return objectMapper.readValue(dispositionDefinition.getInputStream(), new TypeReference<List<Disposition>>() {
            });
        }
        throw new IOException("No disposition file found at " + path);
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
        if (visitDomainWrapper == null || !visitDomainWrapper.isActive()) {
            return getDispositions();
        }
        else {
            List<Disposition> dispositions = new ArrayList<Disposition>();

            boolean isAdmitted = visitDomainWrapper.isAdmitted();

            for (Disposition candidate : getDispositions()) {
                List<CareSetting.CareSettingType> careSettingTypes = candidate.getCareSettingTypes();

                if (careSettingTypes == null
                        || (isAdmitted && careSettingTypes.contains(CareSetting.CareSettingType.INPATIENT))
                        || (!isAdmitted && careSettingTypes.contains(CareSetting.CareSettingType.OUTPATIENT)) )  {
                    dispositions.add(candidate);
                }
            }
            return dispositions;
        }
    }

    @Override
    public List<Disposition> getValidDispositions(VisitDomainWrapper visitDomainWrapper,
            EncounterType encounterType) {

        List<Disposition> dispositions = getValidDispositions(visitDomainWrapper);

        if (encounterType != null) {
            String encounterTypeId = encounterType.getEncounterTypeId() == null ? null : encounterType.getEncounterTypeId().toString();
            String encounterTypeUuid = encounterType.getUuid();
            String encounterTypeName = encounterType.getName();

            for (Iterator<Disposition> it = dispositions.iterator(); it.hasNext(); ) {
                Disposition candidate = it.next();

                List<String> encounterTypes = candidate.getEncounterTypes();
                List<String> excludedEncounterTypes = candidate.getExcludedEncounterTypes();
                if (encounterTypes != null && !((encounterTypeId != null && encounterTypes.contains(encounterTypeId)) ||
                        (encounterTypeUuid != null && encounterTypes.contains(encounterTypeUuid)) ||
                        (encounterTypeName != null && encounterTypes.contains(encounterTypeName)))) {
                    it.remove();
                }
                else if (excludedEncounterTypes != null && ((encounterTypeId != null && excludedEncounterTypes.contains(encounterTypeId)) ||
                        (encounterTypeUuid != null && excludedEncounterTypes.contains(encounterTypeUuid)) ||
                        (encounterTypeName != null && excludedEncounterTypes.contains(encounterTypeName)))) {
                    it.remove();
                }
            }
        }

        return dispositions;
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


    // TODO handle this better--this property is only used to allow use to inject a mock disposition descriptor
    private DispositionDescriptor dispositionDescriptor;

    protected void setDispositionDescriptor(DispositionDescriptor dispositionDescriptor) {
        this.dispositionDescriptor = dispositionDescriptor;
    }
}
