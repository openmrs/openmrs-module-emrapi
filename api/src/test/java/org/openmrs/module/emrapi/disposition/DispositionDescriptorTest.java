package org.openmrs.module.emrapi.disposition;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.LocationService;
import org.openmrs.module.emrapi.concept.EmrConceptService;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DispositionDescriptorTest {

    private Concept dispositionSetConcept = new Concept();
    private Concept dispositionConcept = new Concept();
    private Concept admissionLocationConcept = new Concept();
    private Concept transferLocationConcept = new Concept();
    private Concept dateOfDeathConcept = new Concept();

    private Concept admissionConcept = new Concept();

    private Location admissionLocation = new Location(1);
    private Location transferLocation = new Location(2);

    private EmrConceptService emrConceptService;

    private LocationService locationService;

    private DispositionDescriptor dispositionDescriptor;


    @Before
    public void setup() {
        dispositionDescriptor = new DispositionDescriptor();
        dispositionDescriptor.setDispositionSetConcept(dispositionSetConcept);
        dispositionDescriptor.setDispositionConcept(dispositionConcept);
        dispositionDescriptor.setAdmissionLocationConcept(admissionLocationConcept);
        dispositionDescriptor.setInternalTransferLocationConcept(transferLocationConcept);
        dispositionDescriptor.setDateOfDeathConcept(dateOfDeathConcept);

        emrConceptService = mock(EmrConceptService.class);
        when(emrConceptService.getConcept("ADMISSION")).thenReturn(admissionConcept);

        locationService = mock(LocationService.class);
        when(locationService.getLocation(admissionLocation.getId())).thenReturn(admissionLocation);
        when(locationService.getLocation(transferLocation.getId())).thenReturn(transferLocation);
    }


    @Test
    public void shouldReturnTrueWhenPassedInDispositionObsGroup()  {
        Obs dispositionSet = createDispositionObsGroup();
        assertTrue(dispositionDescriptor.isDisposition(dispositionSet));
    }

    @Test
    public void shouldReturnFalseWhenPassedInNonDispositionObsGroup()  {
        Obs notDispositionSet = new Obs();
        Concept notDispositionConcept = new Concept();
        notDispositionSet.setConcept(notDispositionConcept);
        assertFalse(dispositionDescriptor.isDisposition(notDispositionSet));
    }

    @Test
    public void shouldBuildDispositionObsGroup() {

        Disposition admissionDisposition = new Disposition();
        admissionDisposition.setConceptCode("ADMISSION");

        Obs dispositionObsGroup = dispositionDescriptor.buildObsGroup(admissionDisposition, emrConceptService);

        assertThat(dispositionObsGroup.getConcept(), is(dispositionSetConcept));
        assertTrue(dispositionObsGroup.hasGroupMembers());
        assertThat(dispositionObsGroup.getGroupMembers().size(), is(1));

        Obs dispositionObs = dispositionObsGroup.getGroupMembers().iterator().next();
        assertThat(dispositionObs.getConcept(), is(dispositionConcept));
        assertThat(dispositionObs.getValueCoded(), is(admissionConcept));
    }

    @Test
    public void shouldFetchAdmissionLocationObsOffObsGroup() {

        Obs dispositionObsGroup = createDispositionObsGroup();

        Obs admissionLocation = new Obs();
        admissionLocation.setConcept(admissionLocationConcept);
        dispositionObsGroup.addGroupMember(admissionLocation);

        assertThat(dispositionDescriptor.getAdmissionLocationObs(dispositionObsGroup), is(admissionLocation));
    }

    @Test
    public void shouldNotFailIfNoMatchingObsWhenFetchingAdmissionLocationObs() {
        Obs dispositionObsGroup = createDispositionObsGroup();
        assertNull(dispositionDescriptor.getAdmissionLocationObs(dispositionObsGroup));

    }

    @Test
    public void shouldFetchTransferLocationObsOffObsGroup() {

        Obs dispositionObsGroup = createDispositionObsGroup();

        Obs transferLocation = new Obs();
        transferLocation.setConcept(transferLocationConcept);
        dispositionObsGroup.addGroupMember(transferLocation);

        assertThat(dispositionDescriptor.getInternalTransferLocationObs(dispositionObsGroup), is(transferLocation));
    }

    @Test
    public void shouldNotFailIfNoMatchingObsWhenFetchingTransferLocationOffObsGroup() {
        Obs dispositionObsGroup = createDispositionObsGroup();
        assertNull(dispositionDescriptor.getInternalTransferLocationObs(dispositionObsGroup));

    }

    @Test
    public void shouldFetchDateOfDeathObsOffObsGroup() {

        Obs dispositionObsGroup = createDispositionObsGroup();

        Obs dateOfDeath = new Obs();
        dateOfDeath.setConcept(dateOfDeathConcept);
        dispositionObsGroup.addGroupMember(dateOfDeath);

        assertThat(dispositionDescriptor.getDateOfDeathObs(dispositionObsGroup), is(dateOfDeath));
    }

    @Test
    public void shouldNotFailIfNoMatchingObsWhenDateOfDeath() {
        Obs dispositionObsGroup = createDispositionObsGroup();
        assertNull(dispositionDescriptor.getDateOfDeathObs(dispositionObsGroup));

    }

    @Test
    public void shouldFetchAdmissionLocationOffObsGroup() {

        Obs dispositionObsGroup = createDispositionObsGroup();

        Obs admissionLocationObs = new Obs();
        admissionLocationObs.setConcept(admissionLocationConcept);
        admissionLocationObs.setValueText(admissionLocation.getId().toString());
        dispositionObsGroup.addGroupMember(admissionLocationObs);

        assertThat(dispositionDescriptor.getAdmissionLocation(dispositionObsGroup, locationService), is(admissionLocation));
    }

    @Test
    public void shouldNotFailIfNoMatchingObsWhenFetchingAdmissionLocation() {
        Obs dispositionObsGroup = createDispositionObsGroup();
        assertNull(dispositionDescriptor.getAdmissionLocation(dispositionObsGroup, locationService));
    }

    @Test
    public void shouldFetchTransferLocationOffObsGroup() {

        Obs dispositionObsGroup = createDispositionObsGroup();

        Obs transferLocationObs = new Obs();
        transferLocationObs.setConcept(transferLocationConcept);
        transferLocationObs.setValueText(transferLocation.getId().toString());
        dispositionObsGroup.addGroupMember(transferLocationObs);

        assertThat(dispositionDescriptor.getInternalTransferLocation(dispositionObsGroup, locationService), is(transferLocation));
    }

    @Test
    public void shouldNotFailIfNoMatchingObsWhenFetchingTransferLocation() {
        Obs dispositionObsGroup = createDispositionObsGroup();
        assertNull(dispositionDescriptor.getInternalTransferLocation(dispositionObsGroup, locationService));
    }

    @Test
    public void shouldFetchDateOfDeathOffObsGroup() {

        Obs dispositionObsGroup = createDispositionObsGroup();

        Date dateOfDeath = new Date();

        Obs dateOfDeathObs = new Obs();
        dateOfDeathObs.setConcept(dateOfDeathConcept);
        dateOfDeathObs.setValueDate(dateOfDeath);
        dispositionObsGroup.addGroupMember(dateOfDeathObs);

        assertThat(dispositionDescriptor.getDateOfDeath(dispositionObsGroup), is(dateOfDeath));
    }

    @Test
    public void shouldFetchAdditionalObsOffObsGroup() {

        Obs dispositionObsGroup = createDispositionObsGroup();

        Obs admissionLocationObs = new Obs();
        admissionLocationObs.setConcept(admissionLocationConcept);
        admissionLocationObs.setValueText(admissionLocation.getId().toString());
        dispositionObsGroup.addGroupMember(admissionLocationObs);

        Obs transferLocationObs = new Obs();
        transferLocationObs.setConcept(transferLocationConcept);
        transferLocationObs.setValueText(transferLocation.getId().toString());
        dispositionObsGroup.addGroupMember(transferLocationObs);

        Date dateOfDeath = new Date();
        Obs dateOfDeathObs = new Obs();
        dateOfDeathObs.setConcept(dateOfDeathConcept);
        dateOfDeathObs.setValueDate(dateOfDeath);
        dispositionObsGroup.addGroupMember(dateOfDeathObs);

        Concept additionalObsConcept = new Concept();
        Obs additionalObs = new Obs();
        additionalObs.setConcept(additionalObsConcept);
        additionalObs.setValueText("some value");
        dispositionObsGroup.addGroupMember(additionalObs);

        // additional obs function should only return the single additional obs
        List<Obs> additionalObsList = Collections.singletonList(additionalObs);
        assertThat(dispositionDescriptor.getAdditionalObs(dispositionObsGroup), is(additionalObsList));
    }

    @Test
    public void shouldNotFailIfNoMatchingObsWhenFetchingDateOfDeath() {
        Obs dispositionObsGroup = createDispositionObsGroup();
        assertNull(dispositionDescriptor.getDateOfDeath(dispositionObsGroup));
    }

    /** commenting this out until we know if we actually need to build this functionality
    @Test
    public void shouldBuildDipositionObsWithAdmissionLocation() {

        Disposition admissionDisposition = new Disposition();
        admissionDisposition.setConceptCode("ADMISSION");

        Location admissionLocation = new Location();

        Obs dispositionObsGroup = dispositionDescriptor.buildObsGroup(admissionDisposition, admissionLocation,
                emrConceptService);

        assertThat(dispositionObsGroup.getConcept(), is(dispositionSetConcept));
        assertTrue(dispositionObsGroup.hasGroupMembers());
        assertThat(dispositionObsGroup.getGroupMembers().size(), is(2));

        assertThat(dispositionObsGroup.getGroupMembers(), contains(hasProperty("test")));

    }   **/

    private Obs createDispositionObsGroup() {

        Obs dispositionSet = new Obs();
        dispositionSet.setConcept(dispositionSetConcept);

        Obs disposition = new Obs();
        disposition.setConcept(dispositionConcept);

        dispositionSet.addGroupMember(disposition);

        return dispositionSet;
    }



}
