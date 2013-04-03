package org.openmrs.module.emrapi.descriptor;

import org.databene.benerator.gui.Setup;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;

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

        conceptSetDescriptorImpl.setup(conceptService, "someConceptSource", "setConcept", "setConceptCode",
                "firstMemberConcept", "firstMemberConceptCode", "secondMemberConcept", "secondMemberConceptCode");

        assertThat(conceptSetDescriptorImpl.getSetConcept(), is(setConcept));
        assertThat(conceptSetDescriptorImpl.getFirstMemberConcept(), is(firstMemberConcept));
        assertThat(conceptSetDescriptorImpl.getSecondMemberConcept(), is(secondMemberConcept));

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
