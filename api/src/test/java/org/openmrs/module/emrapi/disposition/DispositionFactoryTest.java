package org.openmrs.module.emrapi.disposition;


import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.emrapi.disposition.actions.Action;
import org.openmrs.module.emrapi.disposition.actions.ClientSideAction;
import org.openmrs.module.emrapi.disposition.actions.CloseCurrentVisitAction;
import org.openmrs.module.emrapi.disposition.actions.FragmentAction;
import org.openmrs.module.emrapi.disposition.actions.MarkPatientDeadAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class DispositionFactoryTest {

    private DispositionFactory dispositionFactory;


    @Before
    public void setUp(){
        dispositionFactory = new DispositionFactory();
    }

    @Test
    public void shouldParserJsonIntoObjects() throws IOException {
        Disposition deathDisposition = getDeathDisposition();

        Disposition homeDisposition = getHomeDisposition();

        List<Disposition> dispositions = dispositionFactory.getDispositions();

        assertEquals(dispositions.size(), 2);

        assertEquals(deathDisposition, dispositions.get(0));

        assertEquals(homeDisposition, dispositions.get(1));

    }

    private Disposition getHomeDisposition() {
        return new Disposition("66de7f60-b73a-11e2-9e96-0800200c9a66", "disposition.home", Collections.<Action>emptyList(), Collections.<ClientSideAction>emptyList());
    }

    private Disposition getDeathDisposition() {
        List<Action> actions = getActions();

        List<ClientSideAction> clientSideActions = new ArrayList<ClientSideAction>();
        clientSideActions.add(new FragmentAction("emr/field/date", getFragmentConfig()));

        return new Disposition("d2d89630-b698-11e2-9e96-0800200c9a66", "disposition.death", actions, clientSideActions);
    }

    private List<Action> getActions() {
        CloseCurrentVisitAction closeCurrentVisitAction = new CloseCurrentVisitAction();
        closeCurrentVisitAction.setName("Close Current Visit Action");

        MarkPatientDeadAction markPatientDeadAction = new MarkPatientDeadAction();
        markPatientDeadAction.setName("Mark Patient as Dead Action");

        return asList(closeCurrentVisitAction, markPatientDeadAction);
    }

    private Map<String, String> getFragmentConfig() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("label", "mirebalais.deathDate");
        return properties;
    }
}
