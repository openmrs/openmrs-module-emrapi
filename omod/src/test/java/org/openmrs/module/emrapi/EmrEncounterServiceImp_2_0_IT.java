package org.openmrs.module.emrapi;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.EmrEncounterService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:moduleApplicationContext.xml"}, inheritLocations = true)
public class EmrEncounterServiceImp_2_0_IT extends BaseModuleWebContextSensitiveTest{

    @Autowired
    private EmrEncounterService emrEncounterService;
    private String encounterUuid;
    private String visitUuid;

    @Before
    public void setUp() throws Exception {
        executeDataSet("baseMetaData.xml");
        executeDataSet("encounterTransactionDataset.xml");
        executeDataSet("dispositionMetaData.xml");
        executeDataSet("diagnosisMetaData.xml");
        encounterUuid = "e403fafb-e5e4-42d0-9d11-4f52e89d1477";
        visitUuid = "e1428fea-6b78-11e0-93c3-1811105e044dc";
    }

    @Test
    public void shouldSaveDispositionInEncounter() throws Exception {
        EncounterTransaction encounterTransaction = new EncounterTransaction();
        encounterTransaction.setEncounterUuid(encounterUuid);
        encounterTransaction.setVisitUuid(visitUuid);
        EncounterTransaction.Disposition disposition = new EncounterTransaction.Disposition().setCode("ADMIT");
        encounterTransaction.setDisposition(disposition);

        EncounterTransaction savedEncounterTransaction = emrEncounterService.save(encounterTransaction);

        Context.flushSession();
        Context.clearSession();

        assertEquals(encounterUuid, savedEncounterTransaction.getEncounterUuid());
        Encounter encounter = Context.getEncounterService().getEncounterByUuid(encounterUuid);
        Set<Obs> allObs = encounter.getAllObs(true);
        assertEquals(2, allObs.size());
        Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(true);
        assertEquals(1, obsAtTopLevel.size());
        Obs topLevelObs = obsAtTopLevel.iterator().next();
        assertEquals("My Disposition Concept Set", topLevelObs.getConcept().getName().getName());
        Set<Obs> groupMembers = topLevelObs.getGroupMembers();
        assertEquals(1, groupMembers.size());
        Obs dispositionObs = groupMembers.iterator().next();
        assertEquals("My Disposition", dispositionObs.getConcept().getName().getName());
        assertEquals("My Admit", dispositionObs.getValueCoded().getName().getName());
    }

    @Test
    public void shouldNotRecreateDispositionWhenThereIsNoChange() throws Exception {
        executeDataSet("existingDispositionObs.xml");

        EncounterTransaction encounterTransaction = emrEncounterService.getEncounterTransaction(encounterUuid, true);
        Encounter encounterWithDisposition = Context.getEncounterService().getEncounterByUuid(encounterTransaction.getEncounterUuid());
        int beforeSave = encounterWithDisposition.getAllObs(true).size();
        assertNotNull(encounterTransaction.getDisposition());

        emrEncounterService.save(encounterTransaction);
        Context.flushSession();
        Context.clearSession();

        encounterWithDisposition = Context.getEncounterService().getEncounterByUuid(encounterTransaction.getEncounterUuid());

        assertEquals(beforeSave, encounterWithDisposition.getAllObs(true).size());
    }

    @Test
    public void shouldVoidAndRecreateOnlyNotesObsWhenNotesIsUpdated() throws Exception {
        executeDataSet("existingDispositionObs.xml");

        EncounterTransaction encounterTransaction = emrEncounterService.getEncounterTransaction(encounterUuid, true);
        Encounter encounterWithDisposition = Context.getEncounterService().getEncounterByUuid(encounterTransaction.getEncounterUuid());
        int beforeSave = encounterWithDisposition.getAllObs(true).size();
        assertNotNull(encounterTransaction.getDisposition());

        //editing the disposition notes
        encounterTransaction.getDisposition().getAdditionalObs().get(0).setValue("editing the notes");

        emrEncounterService.save(encounterTransaction);
        Context.flushSession();
        Context.clearSession();

        encounterWithDisposition = Context.getEncounterService().getEncounterByUuid(encounterTransaction.getEncounterUuid());
        assertEquals(beforeSave+1, encounterWithDisposition.getAllObs(true).size());
        Obs topLevelDispositionObs = encounterWithDisposition.getObsAtTopLevel(true).iterator().next();
        assertEquals(3, topLevelDispositionObs.getGroupMembers(true).size());
        assertEquals(2, topLevelDispositionObs.getGroupMembers().size());
        Iterator<Obs> groupMembersIterator = topLevelDispositionObs.getGroupMembers().iterator();
        Obs dispositionCodeObs = groupMembersIterator.next();
        assertEquals(Context.getConceptService().getConceptByName("My Admit"), dispositionCodeObs.getValueCoded());
        assertEquals("editing the notes", groupMembersIterator.next().getValueText());

    }
}
