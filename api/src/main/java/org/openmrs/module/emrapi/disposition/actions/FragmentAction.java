package org.openmrs.module.emrapi.disposition.actions;


import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

public class FragmentAction implements ClientSideAction{

    @JsonProperty
    private String fragment;

    @JsonProperty
    private Map<String, String> fragmentConfig;

    public FragmentAction(){

    }

    public FragmentAction(String fragment, Map<String, String> fragmentConfig) {
        this.fragment = fragment;
        this.fragmentConfig = fragmentConfig;
    }

    public String getFragment() {
        return fragment;
    }

    public Map<String, String> getFragmentConfig() {
        return fragmentConfig;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public void setFragmentConfig(Map<String, String> fragmentConfig) {
        this.fragmentConfig = fragmentConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FragmentAction that = (FragmentAction) o;

        if (!fragment.equals(that.fragment)) return false;
        if (!fragmentConfig.equals(that.fragmentConfig)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fragment.hashCode();
        result = 31 * result + fragmentConfig.hashCode();
        return result;
    }
}
