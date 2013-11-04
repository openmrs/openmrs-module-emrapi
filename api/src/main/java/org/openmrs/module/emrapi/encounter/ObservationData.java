package org.openmrs.module.emrapi.encounter;

public class ObservationData{

    private final String conceptName;
    private final String conceptUuid;
    private final Object value;

    public ObservationData(String conceptUuid, String name, Object value) {
        this.conceptUuid = conceptUuid;
        this.conceptName = name;
        this.value = value;
    }

    public String getConceptUuid() {
        return conceptUuid;
    }

    public String getConceptName() {
        return conceptName;
    }

    public Object getValue() {
        return value;
    }

}