package org.openmrs.module.emrapi.matcher;

import org.apache.commons.beanutils.PropertyUtils;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;
import org.openmrs.Concept;
import org.openmrs.Obs;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ObsGroupMatcher extends ArgumentMatcher<Obs> {

    private boolean expectVoided = false;
    private Concept expectedGroupingConcept;
    private List<Obs> expected = new ArrayList<Obs>();

    @Override
    public void describeTo(Description description) {
        String s = expectVoided ? "Voided group" : "Group";
        if (expectedGroupingConcept != null) {
            s += " (concept " + expectedGroupingConcept.getId() + ")";
        }
        for (Obs expectedObs : expected) {
            s += " (member";
            if (Boolean.TRUE.equals(expectedObs.getVoided())) {
                s += " voided";
            }
            if (expectedObs.getConcept() != null) {
                s += " concept " + expectedObs.getConcept().getId();
            }
            if (expectedObs.getValueCoded() != null) {
                s += " valueCoded " + expectedObs.getValueCoded().getId();
            }
            if (expectedObs.getValueText() != null) {
                s += " valueText " + expectedObs.getValueText();
            }
            s += ")";
        }
        description.appendText(s);
    }

    @Override
    public boolean matches(Object argument) {
        Obs actual = (Obs) argument;

        if (expectedGroupingConcept != null && !expectedGroupingConcept.equals(actual.getConcept())) {
            return false;
        }

        if (expectVoided != actual.isVoided()) {
            return false;
        }

        for (Obs expectedObs : expected) {
            boolean found = false;
            for (Obs candidate : actual.getGroupMembers(true)) {
                if (matchingObs(expectedObs, candidate)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }

    private boolean matchingObs(Obs expected, Obs actual) {
        try {
            return sameIfSpecified(expected, actual, "concept")
                    && sameIfSpecified(expected, actual, "voided")
                    && sameIfSpecified(expected, actual, "valueCoded")
                    && sameIfSpecified(expected, actual, "valueText");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean sameIfSpecified(Obs expected, Obs actual, String property) throws Exception {
        Object expectedProperty = PropertyUtils.getProperty(expected, property);
        if (expectedProperty == null) {
            return true;
        } else {
            Object actualProperty = PropertyUtils.getProperty(actual, property);
            return actualProperty != null && expectedProperty.equals(actualProperty);
        }
    }

    public ObsGroupMatcher withGroupingConcept(Concept groupingConcept) {
        this.expectedGroupingConcept = groupingConcept;
        return this;
    }

    public ObsGroupMatcher withNonVoidedObs(Concept concept, Concept valueCoded) {
        Obs obs = new Obs();
        obs.setVoided(false);
        obs.setConcept(concept);
        obs.setValueCoded(valueCoded);
        expected.add(obs);
        return this;
    }

    public ObsGroupMatcher withVoidedObs(Concept concept, Concept valueCoded) {
        Obs obs = new Obs();
        obs.setVoided(true);
        obs.setConcept(concept);
        obs.setValueCoded(valueCoded);
        expected.add(obs);
        return this;
    }

    public ObsGroupMatcher withNonVoidedObs(Concept concept, String valueText) {
        Obs obs = new Obs();
        obs.setVoided(false);
        obs.setConcept(concept);
        obs.setValueText(valueText);
        expected.add(obs);
        return this;
    }

    public ObsGroupMatcher withVoidedObs(Concept concept, String valueText) {
        Obs obs = new Obs();
        obs.setVoided(true);
        obs.setConcept(concept);
        obs.setValueText(valueText);
        expected.add(obs);
        return this;
    }

    public ObsGroupMatcher thatIsVoided() {
        expectVoided = true;
        return this;
    }

    public ObsGroupMatcher withObs(Concept concept, Concept valueCoded) {
        Obs obs = new Obs();
        obs.setVoided(null);
        obs.setConcept(concept);
        obs.setValueCoded(valueCoded);
        expected.add(obs);
        return this;
    }

    public ObsGroupMatcher withObs(Concept concept, String valueText) {
        Obs obs = new Obs();
        obs.setVoided(null);
        obs.setConcept(concept);
        obs.setValueText(valueText);
        expected.add(obs);
        return this;
    }
}
