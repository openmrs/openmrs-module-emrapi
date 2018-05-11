package org.openmrs.module.emrapi;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.ConceptSource;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EmrApiPropertiesTest {
    @Mock
    private AdministrationService administrationService;
    private EmrApiProperties emrApiProperties;
    @Mock
    private ConceptService conceptService;
    
    @Before
    public void setUp() {
        initMocks(this);
        emrApiProperties = new EmrApiProperties();
        emrApiProperties.setAdministrationService(administrationService);
        emrApiProperties.setConceptService(conceptService);
    }

    @Test
    public void visitExpireHours_shouldBeConfiguredValueFromGlobalProperty(){
        when(administrationService.getGlobalProperty(EmrApiConstants.GP_VISIT_EXPIRE_HOURS)).thenReturn("10");

        assertEquals(10, emrApiProperties.getVisitExpireHours());
    }

    @Test
    public void visitExpireHours_shouldBeDefaultValueWhenNotConfigured(){
        when(administrationService.getGlobalProperty(EmrApiConstants.GP_VISIT_EXPIRE_HOURS)).thenReturn(null);

        assertEquals(EmrApiConstants.DEFAULT_VISIT_EXPIRE_HOURS, emrApiProperties.getVisitExpireHours());
    }

    @Test
    public void visitExpireHours_shouldBeDefaultValueWhenNotConfiguredAsNonInteger(){
        when(administrationService.getGlobalProperty(EmrApiConstants.GP_VISIT_EXPIRE_HOURS)).thenReturn("foo");

        assertEquals(EmrApiConstants.DEFAULT_VISIT_EXPIRE_HOURS, emrApiProperties.getVisitExpireHours());
    }
    
    @Test
    public void getConceptSourcesForDiagnosisSearch_shouldNotReturnNull(){
        when(conceptService.getConceptSourceByName("ICD-10-WHO")).thenReturn(null);
        Assert.assertNotNull(emrApiProperties.getConceptSourcesForDiagnosisSearch());
        Assert.assertTrue(emrApiProperties.getConceptSourcesForDiagnosisSearch().size() == 0);
        
        when(conceptService.getConceptSourceByName("ICD-10-WHO")).thenReturn(new ConceptSource());
        Assert.assertNotNull(emrApiProperties.getConceptSourcesForDiagnosisSearch());
        Assert.assertTrue(emrApiProperties.getConceptSourcesForDiagnosisSearch().size() > 0);
        
        
    }
}
