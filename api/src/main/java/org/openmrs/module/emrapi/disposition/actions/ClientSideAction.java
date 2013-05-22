package org.openmrs.module.emrapi.disposition.actions;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.util.Map;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "action")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FragmentAction.class, name = "include-fragment")
         })
public interface ClientSideAction {
    String getFragment();

    Map<String, Object> getFragmentConfig();

    String getModule();
}
