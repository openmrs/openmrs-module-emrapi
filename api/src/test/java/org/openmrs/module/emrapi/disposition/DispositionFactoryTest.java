package org.openmrs.module.emrapi.disposition;


import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.disposition.actions.ClientSideAction;
import org.openmrs.module.emrapi.disposition.actions.FragmentAction;
import org.openmrs.module.emrapi.test.MockMetadataTestUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DispositionFactoryTest {

    private DispositionFactory dispositionFactory;

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
        MockMetadataTestUtil.setupDispositionDescriptor(emrApiProperties, concertService);

        dispositionFactory = new DispositionFactory();
        dispositionFactory.setEmrConceptService(emrConceptService);
        dispositionFactory.setConceptService(concertService);
        dispositionFactory.setEmrApiProperties(emrApiProperties);
    }

    @Test
    public void shouldParseDispositionJsonFromDefaultConfig() throws IOException {
        Disposition deathDisposition = getDeathDisposition();

        Disposition homeDisposition = getHomeDisposition();

        List<Disposition> dispositions = dispositionFactory.getDispositions();

        assertEquals(dispositions.size(), 2);

        assertEquals(deathDisposition, dispositions.get(0));

        assertEquals(homeDisposition, dispositions.get(1));

    }

    @Test
    public void shouldParseDispositionJsonFromSpecifiedConfig() throws IOException {
        List<Disposition> dispositions = dispositionFactory.getDispositionsFrom("specifiedDispositionConfig.json");

        assertEquals(dispositions.size(), 3);
    }

    @Test
    public void shouldGetDispositionByObs()  throws IOException {

        Concept deathDispositionConcept = new Concept();

        Obs dispositionObs = new Obs();
        dispositionObs.setValueCoded(deathDispositionConcept);

        when(emrConceptService.getConcept("SNOMED CT:397709008")).thenReturn(deathDispositionConcept);

        Disposition disposition = dispositionFactory.getDispositionFromObs(dispositionObs);
        assertThat(disposition, is(getDeathDisposition()));
    }

    @Test
    public void shouldGetDispositionByObsGroup() throws IOException {

        Concept deathDispositionConcept = new Concept();

        Obs dispositionObs = new Obs();
        dispositionObs.setConcept(emrApiProperties.getDispositionDescriptor().getDispositionConcept());
        dispositionObs.setValueCoded(deathDispositionConcept);

        Obs dispositionObsGroup = new Obs();
        dispositionObsGroup.setConcept(emrApiProperties.getDispositionDescriptor().getDispositionSetConcept());
        dispositionObsGroup.addGroupMember(dispositionObs);

        when(emrConceptService.getConcept("SNOMED CT:397709008")).thenReturn(deathDispositionConcept);

        Disposition disposition = dispositionFactory.getDispositionFromObsGroup(dispositionObsGroup);
        assertThat(disposition, is(getDeathDisposition()));
    }

    private Disposition getHomeDisposition() {
        return new Disposition("66de7f60-b73a-11e2-9e96-0800200c9a66", "disposition.home", "SNOMED CT:3780001", Collections.<String>emptyList(), Collections.<ClientSideAction>emptyList());
    }

    private Disposition getDeathDisposition() {
        List<String> actions = getActions();

        List<ClientSideAction> clientSideActions = new ArrayList<ClientSideAction>();
        clientSideActions.add(new FragmentAction("emr","field/date", getFragmentConfig()));

        return new Disposition("d2d89630-b698-11e2-9e96-0800200c9a66", "disposition.death", "SNOMED CT:397709008", actions, clientSideActions);
    }

    private List<String> getActions() {
        return asList("closeCurrentVisitAction", "markPatientDeadAction");
    }

    private Map<String, Object> getFragmentConfig() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("label", "mirebalais.deathDate");
        return properties;
    }
}
