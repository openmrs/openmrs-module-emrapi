package org.openmrs.module.emrapi.disposition;


import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.CareSettingType;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.test.MockMetadataTestUtil;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DispositionServiceTest {

    private DispositionServiceImpl dispositionService;

    private ConceptService concertService;

    private EmrConceptService emrConceptService;

    private EmrApiProperties emrApiProperties;

    private DispositionDescriptor dispositionDescriptor;

    @Before
    public void setUp(){

        emrConceptService = mock(EmrConceptService.class);
        concertService = mock(ConceptService.class);
        emrApiProperties = mock(EmrApiProperties.class);
        MockMetadataTestUtil.setupMockConceptService(concertService, emrApiProperties);
        dispositionDescriptor = MockMetadataTestUtil.setupDispositionDescriptor(concertService);

        dispositionService = new DispositionServiceImpl(concertService, emrConceptService);
        dispositionService.setDispositionDescriptor(dispositionDescriptor);
    }

    @Test
    public void shouldParseDispositionJsonFromDefaultConfig() throws IOException {
        Disposition deathDisposition = getDeathDisposition();

        Disposition homeDisposition = getAdmitDisposition();

        List<Disposition> dispositions = dispositionService.getDispositions();

        assertEquals(3, dispositions.size());

        assertEquals(deathDisposition, dispositions.get(0));
        assertEquals(homeDisposition, dispositions.get(1));

    }

    @Test
    public void shouldParseDispositionJsonFromSpecifiedConfig() throws IOException {
        dispositionService.setDispositionConfig("specifiedDispositionConfig.json");
        List<Disposition> dispositions = dispositionService.getDispositions();

        assertEquals(5, dispositions.size());

        Map<String,Disposition> dispositionMap = new HashMap<String, Disposition>();
        for (Disposition disposition : dispositions) {
            dispositionMap.put(disposition.getUuid(), disposition);
        }

        Disposition death = dispositionMap.get("d2d89630-b698-11e2-9e96-0800200c9a66");
        assertNotNull(death);
        assertThat(death.getName(), is("disposition.death"));
        assertThat(death.getConceptCode(), is("SNOMED CT:397709008"));
        assertNull(death.getKeepsVisitOpen());
        assertNull(death.getType());
        assertNull(death.getCareSettingTypes());
        assertThat(death.getActions().size(), is(2));
        assertTrue(death.getActions().contains("closeCurrentVisitAction"));
        assertTrue(death.getActions().contains("markPatientDeadAction"));

        Disposition home = dispositionMap.get("66de7f60-b73a-11e2-9e96-0800200c9a66");
        assertNotNull(home);
        assertThat(home.getName(), is("disposition.home"));
        assertThat(home.getConceptCode(), is("SNOMED CT:3780001"));
        assertNull(home.getKeepsVisitOpen());
        assertThat(home.getType(), is(DispositionType.DISCHARGE));
        assertThat(home.getCareSettingTypes().size(), is(1));
        assertTrue(home.getCareSettingTypes().contains(CareSettingType.INPATIENT));

        Disposition transfer = dispositionMap.get("799820d0-e02d-11e3-8b68-0800200c9a66");
        assertNotNull(transfer);
        assertThat(transfer.getName(), is("disposition.transfer"));
        assertThat(transfer.getConceptCode(), is("SNOMED CT:3780002"));
        assertNull(transfer.getKeepsVisitOpen());
        assertThat(transfer.getType(), is(DispositionType.TRANSFER));
        assertThat(transfer.getCareSettingTypes().size(), is(1));
        assertTrue(transfer.getCareSettingTypes().contains(CareSettingType.INPATIENT));

        Disposition admit = dispositionMap.get("844436e0-e02d-11e3-8b68-0800200c9a66");
        assertNotNull(admit);
        assertThat(admit.getName(), is("disposition.admit"));
        assertThat(admit.getConceptCode(), is("123"));
        assertTrue(admit.getKeepsVisitOpen());
        assertThat(admit.getType(), is(DispositionType.ADMIT));
        assertThat(admit.getCareSettingTypes().size(), is(1));
        assertTrue(admit.getCareSettingTypes().contains(CareSettingType.OUTPATIENT));
    }

    @Test
    public void shouldGetDispositionByType() throws Exception {

        dispositionService.setDispositionConfig("specifiedDispositionConfig.json");

        List<Disposition> dispositions = dispositionService.getDispositionsByType(DispositionType.TRANSFER);
        assertThat(dispositions.size(), is(2));
        assertThat(dispositions.get(0).getUuid(), is("799820d0-e02d-11e3-8b68-0800200c9a66"));
        assertThat(dispositions.get(1).getUuid(), is("fabe3540-e0ec-11e3-8b68-0800200c9a66"));

        dispositions = dispositionService.getDispositionsByType(DispositionType.DISCHARGE);
        assertThat(dispositions.size(), is(1));
        assertThat(dispositions.get(0).getUuid(), is("66de7f60-b73a-11e2-9e96-0800200c9a66"));
    }

    @Test
    public void shouldGetInpatientDispositions() throws Exception {

        dispositionService.setDispositionConfig("specifiedDispositionConfig.json");

        VisitDomainWrapper visitDomainWrapper = mock(VisitDomainWrapper.class);
        when(visitDomainWrapper.isActive()).thenReturn(true);
        when(visitDomainWrapper.isAdmitted()).thenReturn(true);

        List<Disposition> dispositions = dispositionService.getValidDispositions(visitDomainWrapper);

        assertThat(dispositions.size(), is(3));
        assertThat(dispositions.get(0).getUuid(), is("d2d89630-b698-11e2-9e96-0800200c9a66"));
        assertThat(dispositions.get(1).getUuid(), is("66de7f60-b73a-11e2-9e96-0800200c9a66"));
        assertThat(dispositions.get(2).getUuid(), is("799820d0-e02d-11e3-8b68-0800200c9a66"));
    }

    @Test
    public void shouldGetOutpatientDispositions() throws Exception {

        dispositionService.setDispositionConfig("specifiedDispositionConfig.json");

        VisitDomainWrapper visitDomainWrapper = mock(VisitDomainWrapper.class);
        when(visitDomainWrapper.isActive()).thenReturn(true);
        when(visitDomainWrapper.isAdmitted()).thenReturn(false);

        List<Disposition> dispositions = dispositionService.getValidDispositions(visitDomainWrapper);

        assertThat(dispositions.size(), is(3));
        assertThat(dispositions.get(0).getUuid(), is("d2d89630-b698-11e2-9e96-0800200c9a66"));
        assertThat(dispositions.get(1).getUuid(), is("844436e0-e02d-11e3-8b68-0800200c9a66"));
        assertThat(dispositions.get(2).getUuid(), is("fabe3540-e0ec-11e3-8b68-0800200c9a66"));

    }

    @Test
    public void shouldGetAllDispositions() throws Exception {

        dispositionService.setDispositionConfig("specifiedDispositionConfig.json");

        VisitDomainWrapper visitDomainWrapper = mock(VisitDomainWrapper.class);
        when(visitDomainWrapper.isActive()).thenReturn(false);
        when(visitDomainWrapper.isAdmitted()).thenReturn(false);

        List<Disposition> dispositions = dispositionService.getValidDispositions(visitDomainWrapper);

        assertThat(dispositions.size(), is(5));
        assertThat(dispositions.get(0).getUuid(), is("d2d89630-b698-11e2-9e96-0800200c9a66"));
        assertThat(dispositions.get(1).getUuid(), is("66de7f60-b73a-11e2-9e96-0800200c9a66"));
        assertThat(dispositions.get(2).getUuid(), is("799820d0-e02d-11e3-8b68-0800200c9a66"));
        assertThat(dispositions.get(3).getUuid(), is("844436e0-e02d-11e3-8b68-0800200c9a66"));
        assertThat(dispositions.get(4).getUuid(), is("fabe3540-e0ec-11e3-8b68-0800200c9a66"));

    }

    @Test
    public void shouldGetDispositionByObs()  throws IOException {

        Concept deathDispositionConcept = new Concept();

        Obs dispositionObs = new Obs();
        dispositionObs.setValueCoded(deathDispositionConcept);

        when(emrConceptService.getConcept("org.openmrs.module.emrapi:Death")).thenReturn(deathDispositionConcept);

        Disposition disposition = dispositionService.getDispositionFromObs(dispositionObs);
        assertThat(disposition, is(getDeathDisposition()));
    }

    @Test
    public void shouldGetDispositionByObsGroup() throws IOException {

        Concept deathDispositionConcept = new Concept();

        Obs dispositionObs = new Obs();
        dispositionObs.setConcept(dispositionService.getDispositionDescriptor().getDispositionConcept());
        dispositionObs.setValueCoded(deathDispositionConcept);

        Obs dispositionObsGroup = new Obs();
        dispositionObsGroup.setConcept(dispositionService.getDispositionDescriptor().getDispositionSetConcept());
        dispositionObsGroup.addGroupMember(dispositionObs);

        when(emrConceptService.getConcept("org.openmrs.module.emrapi:Death")).thenReturn(deathDispositionConcept);

        Disposition disposition = dispositionService.getDispositionFromObsGroup(dispositionObsGroup);
        assertThat(disposition, is(getDeathDisposition()));
    }

    private Disposition getAdmitDisposition() {
        return new Disposition("66de7f60-b73a-11e2-9e96-0800200c9a66", "disposition.admit", "org.openmrs.module.emrapi:Admit to hospital", Collections.<String>emptyList(), Collections.<DispositionObs>emptyList());
    }

    private Disposition getDeathDisposition() {
        return new Disposition("d2d89630-b698-11e2-9e96-0800200c9a66", "disposition.death", "org.openmrs.module.emrapi:Death", getActions(), getAdditionalObs());
    }

    private List<String> getActions() {
        return asList("closeCurrentVisitAction", "markPatientDeadAction");
    }

    private List<DispositionObs> getAdditionalObs() {
        List<DispositionObs> additionalObsList = new ArrayList<DispositionObs>();
        DispositionObs additionalObs = new DispositionObs();
        additionalObs.setConceptCode("org.openmrs.module.emrapi:Date of death");
        additionalObs.setLabel("emr.dateOfDeath");
        additionalObsList.add(additionalObs);
        return additionalObsList;
    }

    private Map<String, Object> getFragmentConfig() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("label", "mirebalais.deathDate");
        return properties;
    }
}
