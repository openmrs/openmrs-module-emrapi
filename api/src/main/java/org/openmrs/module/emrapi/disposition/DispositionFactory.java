package org.openmrs.module.emrapi.disposition;


import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class DispositionFactory {

    private ObjectMapper objectMapper = new ObjectMapper();

    private PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();


    public List<Disposition> getDispositions() throws IOException {
        Resource[] dispositionDefinitions = resourceResolver.getResources("classpath*:/dispositionConfig.json");
        for (Resource dispositionDefinition : dispositionDefinitions) {
            return objectMapper.readValue(dispositionDefinition.getInputStream(), new TypeReference<List<Disposition>>() {});
        }
        return null;

    }

}
