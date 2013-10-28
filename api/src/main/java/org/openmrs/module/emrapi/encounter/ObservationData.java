package org.openmrs.module.emrapi.encounter;

public class ObservationData{

    private final String conceptName;
    private final String conceptUUID;
    private final Object value;

    public ObservationData(String conceptUUID, String name, Object value) {
        this.conceptUUID = conceptUUID;
        this.conceptName = name;
        this.value = value;
    }

    public String getConceptUUID() {
        return conceptUUID;
    }

    public String getConceptName() {
        return conceptName;
    }

    public Object getValue() {
        return value;
    }

}