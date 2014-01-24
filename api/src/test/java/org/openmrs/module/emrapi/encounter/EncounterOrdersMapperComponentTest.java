package org.openmrs.module.emrapi.encounter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterOrdersMapperComponentTest extends BaseModuleContextSensitiveTest {
    @Mock
    EmrApiProperties emrApiProperties;
    @Mock
    DispositionMapper dispositionMapper;

    @Before
    public void setUp() throws Exception {
        executeDataSet("testOrderWithObservation.xml");
        initMocks(this);
    }

    @Test
    public void shouldMapOrdersOfTypeTestOrderWithObsWhichHasTestOrderAndObsHasAParentObsWithoutAnyOrder() {
        when(emrApiProperties.getDiagnosisMetadata()).thenReturn(new DiagnosisMetadata());
        when(dispositionMapper.isDispositionGroup((Obs) anyObject())).thenReturn(false);

        ConceptService conceptService = Context.getConceptService();
        EncounterObservationsMapper encounterObservationsMapper = new EncounterObservationsMapper(new ObservationMapper(), new DiagnosisMapper() ,dispositionMapper, emrApiProperties);
        EncounterProviderMapper encounterProviderMapper = new EncounterProviderMapper();
        EncounterOrdersMapper encounterOrdersMapper = new EncounterOrdersMapper(new TestOrderMapper(), new DrugOrderMapper(conceptService));
        EncounterTransactionMapper encounterTransactionMapper = new EncounterTransactionMapper(encounterObservationsMapper, encounterOrdersMapper, encounterProviderMapper);

        Encounter encounter = Context.getEncounterService().getEncounterByUuid("7779d653-393b-4118-9c83-a3715b82d4ac");
        EncounterTransaction encounterTransaction = encounterTransactionMapper.map(encounter, false);

        assertEquals(0,encounterTransaction.getDrugOrders().size());
        assertEquals(1,encounterTransaction.getTestOrders().size());
    }


}
