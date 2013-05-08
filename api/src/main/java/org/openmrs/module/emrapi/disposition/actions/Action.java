package org.openmrs.module.emrapi.disposition.actions;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "action")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CloseCurrentVisitAction.class, name = "CloseCurrentVisitAction"),
        @JsonSubTypes.Type(value = MarkPatientDeadAction.class, name = "MarkPatientDeadAction") })
public interface Action {
    void action(EncounterDomainWrapper encounterDomainWrapper);
}
