package org.openmrs.module.emrapi.visit;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.EmrApiProperties;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class EmrVisitServiceTest {

    private EmrVisitServiceImpl emrVisitService;

    private EmrApiProperties emrApiProperties;

    @Before
    public void setup() {
        emrApiProperties = mock(EmrApiProperties.class);
        emrVisitService = new EmrVisitServiceImpl(emrApiProperties);
    }

    @Test
    public void shouldCreateVisitDomainWrapperFromVisit() {
        Visit visit = new Visit();
        VisitDomainWrapper visitDomainWrapper = emrVisitService.getVisitDomainWrapper(visit);
        assertThat(visitDomainWrapper.getVisit(), is(visit));
    }

}
