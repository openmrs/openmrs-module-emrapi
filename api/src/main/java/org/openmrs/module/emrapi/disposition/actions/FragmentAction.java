package org.openmrs.module.emrapi.disposition.actions;


import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

public class FragmentAction implements ClientSideAction{

    @JsonProperty
    private String module;

    @JsonProperty
    private String fragment;

    @JsonProperty
    private Map<String, Object> fragmentConfig;

    public FragmentAction(){

    }

    public FragmentAction(String module, String fragment, Map<String, Object> fragmentConfig) {
        this.module = module;
        this.fragment = fragment;
        this.fragmentConfig = fragmentConfig;
    }

    @Override
    public String getModule() {
        return module;
    }

    @Override
    public String getFragment() {
        return fragment;
    }

    @Override
    public Map<String, Object> getFragmentConfig() {
        return fragmentConfig;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public void setFragmentConfig(Map<String, Object> fragmentConfig) {
        this.fragmentConfig = fragmentConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FragmentAction that = (FragmentAction) o;

        if (fragment != null ? !fragment.equals(that.fragment) : that.fragment != null) return false;
        if (fragmentConfig != null ? !fragmentConfig.equals(that.fragmentConfig) : that.fragmentConfig != null)
            return false;
        if (module != null ? !module.equals(that.module) : that.module != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = module != null ? module.hashCode() : 0;
        result = 31 * result + (fragment != null ? fragment.hashCode() : 0);
        result = 31 * result + (fragmentConfig != null ? fragmentConfig.hashCode() : 0);
        return result;
    }
}
