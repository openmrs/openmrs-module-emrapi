package org.openmrs.module.emrapi.descriptor;

import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConceptSetDescriptorTest {

    private ConceptService conceptService;

    private Concept setConcept;

    private Concept firstMemberConcept;

    private Concept secondMemberConcept;

    @Before
    public void setup() {
        conceptService = mock(ConceptService.class);

        setConcept = new Concept();
        firstMemberConcept = new Concept();
        secondMemberConcept = new Concept();
        setConcept.addSetMember(firstMemberConcept);
        setConcept.addSetMember(secondMemberConcept);

        when(conceptService.getConceptByMapping("setConceptCode","someConceptSource")).thenReturn(setConcept);
        when(conceptService.getConceptByMapping("firstMemberConceptCode", "someConceptSource")).thenReturn(firstMemberConcept);
        when(conceptService.getConceptByMapping("secondMemberConceptCode", "someConceptSource")).thenReturn(secondMemberConcept);
    }

    @Test
    public void shouldProperlySetupConcepts() {

        ConceptSetDescriptorImpl conceptSetDescriptorImpl = new ConceptSetDescriptorImpl();

        conceptSetDescriptorImpl.setup(conceptService, "someConceptSource",
                ConceptSetDescriptorField.required("setConcept", "setConceptCode"),
                ConceptSetDescriptorField.required("firstMemberConcept", "firstMemberConceptCode"),
                ConceptSetDescriptorField.required("secondMemberConcept", "secondMemberConceptCode"));

        assertThat(conceptSetDescriptorImpl.getSetConcept(), is(setConcept));
        assertThat(conceptSetDescriptorImpl.getFirstMemberConcept(), is(firstMemberConcept));
        assertThat(conceptSetDescriptorImpl.getSecondMemberConcept(), is(secondMemberConcept));

    }

    @Test(expected = IllegalStateException.class)
    public void shouldRaiseExceptionIfRequiredConceptDoesNotExist() {

        ConceptSetDescriptorImpl conceptSetDescriptorImpl = new ConceptSetDescriptorImpl();

        conceptSetDescriptorImpl.setup(conceptService, "someConceptSource",
                ConceptSetDescriptorField.required("setConcept", "setConceptCode"),
                ConceptSetDescriptorField.required("firstMemberConcept", "nonExistingConceptCode"));
    }

    @Test
    public void shouldNotRaiseExceptionIfOptionalConceptDoesNotExist() {

        ConceptSetDescriptorImpl conceptSetDescriptorImpl = new ConceptSetDescriptorImpl();

        conceptSetDescriptorImpl.setup(conceptService, "someConceptSource",
                ConceptSetDescriptorField.required("setConcept", "setConceptCode"),
                ConceptSetDescriptorField.optional("firstMemberConcept", "nonExistingConceptCode"));

        assertThat(conceptSetDescriptorImpl.getSetConcept(), is(setConcept));
        assertThat(conceptSetDescriptorImpl.getFirstMemberConcept(), IsNull.nullValue());
    }


    public class ConceptSetDescriptorImpl extends ConceptSetDescriptor {

        private Concept setConcept;

        private Concept firstMemberConcept;

        private Concept secondMemberConcept;


        public ConceptSetDescriptorImpl() {

        }

        public Concept getSetConcept() {
            return setConcept;
        }

        public void setSetConcept(Concept setConcept) {
            this.setConcept = setConcept;
        }

        public Concept getFirstMemberConcept() {
            return firstMemberConcept;
        }

        public void setFirstMemberConcept(Concept firstMemberConcept) {
            this.firstMemberConcept = firstMemberConcept;
        }

        public Concept getSecondMemberConcept() {
            return secondMemberConcept;
        }

        public void setSecondMemberConcept(Concept secondMemberConcept) {
            this.secondMemberConcept = secondMemberConcept;
        }
    }
}
