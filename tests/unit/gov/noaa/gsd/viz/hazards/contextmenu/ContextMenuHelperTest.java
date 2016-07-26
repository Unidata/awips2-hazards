/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.contextmenu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.hazards.contextmenu.ContextMenuHelper.ContextMenuSelections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.junit.Before;
import org.junit.Test;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;

/**
 * Description: Test of {@link ContextMenuHelper}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 4, 2014            daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ContextMenuHelperTest {

    private ISessionEventManager eventManager;

    private ContextMenuHelper contextMenuHelper;

    private List<IHazardEvent> events;

    private List<String> selections;

    @Before
    public void setup() {
        events = new ArrayList<>();
        ISessionManager sessionManager = mock(ISessionManager.class);
        eventManager = mock(ISessionEventManager.class);
        when(eventManager.getSelectedEvents()).thenReturn(events);
        when(sessionManager.getEventManager()).thenReturn(eventManager);
        contextMenuHelper = new ContextMenuHelper(sessionManager,
                new IRunnableAsynchronousScheduler() {

                    @Override
                    public void schedule(Runnable runnable) {
                        VizApp.runAsync(runnable);
                    }
                });
    }

    @Test
    public void oneSelectedPendingOneCurrentUnselectedIssued() {
        ObservedHazardEvent event0 = mock(ObservedHazardEvent.class);
        when(event0.getStatus()).thenReturn(HazardStatus.PENDING);
        events.add(event0);

        ObservedHazardEvent event1 = mock(ObservedHazardEvent.class);
        when(event1.getStatus()).thenReturn(HazardStatus.ISSUED);
        when(eventManager.getCurrentEvent()).thenReturn(event1);
        when(eventManager.isCurrentEvent()).thenReturn(true);
        selections = buildSelections();
        assertEquals(selections.size(), 1);
        assertTrue(selections.contains("Delete 1 Selected Pending"));
    }

    @Test
    public void multipleSelected() {
        ObservedHazardEvent event0 = buildEvent(HazardStatus.PENDING);
        when(eventManager.isSelected(event0)).thenReturn(true);
        events.add(event0);

        ObservedHazardEvent event1 = buildEvent(HazardStatus.ISSUED);
        when(eventManager.isSelected(event1)).thenReturn(true);
        events.add(event1);

        when(eventManager.getCurrentEvent()).thenReturn(event1);
        when(eventManager.isCurrentEvent()).thenReturn(true);
        selections = buildSelections();
        assertEquals(selections.size(), 2);

        assertTrue(selections.contains(ContextMenuSelections.END_THIS_HAZARD
                .getValue()));
        assertTrue(selections.contains("Delete 1 Selected Pending"));
        when(eventManager.getCurrentEvent()).thenReturn(event0);
        selections = buildSelections();
        assertEquals(selections.size(), 2);
        assertTrue(selections.contains(ContextMenuSelections.DELETE_THIS_HAZARD
                .getValue()));
        assertTrue(selections.contains("End 1 Selected Issued"));

        ObservedHazardEvent event2 = buildEvent(HazardStatus.PENDING);
        when(eventManager.isSelected(event2)).thenReturn(true);
        events.add(event2);

        ObservedHazardEvent event3 = buildEvent(HazardStatus.ISSUED);
        when(eventManager.isSelected(event3)).thenReturn(true);
        events.add(event3);

        selections = buildSelections();
        assertEquals(selections.size(), 3);
        assertTrue(selections.contains(ContextMenuSelections.DELETE_THIS_HAZARD
                .getValue()));
        assertTrue(selections.contains("Delete 2 Selected Pending"));
        assertTrue(selections.contains("End 2 Selected Issued"));
        when(eventManager.getCurrentEvent()).thenReturn(event1);
        selections = buildSelections();
        assertEquals(selections.size(), 3);
        assertTrue(selections.contains(ContextMenuSelections.END_THIS_HAZARD
                .getValue()));
        assertTrue(selections.contains("Delete 2 Selected Pending"));
        assertTrue(selections.contains("End 2 Selected Issued"));
    }

    @Test
    public void oneSelectedHazardIsPending() {
        ObservedHazardEvent event = buildSingleEvent(HazardStatus.PENDING);
        when(eventManager.isSelected(event)).thenReturn(true);
        selections = buildSelections();
        assertEquals(selections.size(), 1);

        assertTrue(selections.contains(ContextMenuSelections.DELETE_THIS_HAZARD
                .getValue()));

        when(event.getHazardType()).thenReturn("FA.A");
        selections = buildSelections();
        assertEquals(selections.size(), 2);
        assertTrue(selections
                .contains(ContextMenuSelections.PROPOSE_THIS_HAZARD.getValue()));

    }

    @Test
    public void oneSelectedHazardIsEnded() {
        buildSingleEvent(HazardStatus.ENDED);
        selections = buildSelections();
        assertEquals(selections.size(), 0);

    }

    @Test
    public void oneSelectedHazardIsIssued() {
        buildSingleEvent(HazardStatus.ISSUED);
        selections = buildSelections();
        assertEquals(selections.size(), 1);

        assertTrue(selections.contains(ContextMenuSelections.END_THIS_HAZARD
                .getValue()));

    }

    @Test
    public void oneSelectedHazardIsProposed() {
        buildSingleEvent(HazardStatus.PROPOSED);
        selections = buildSelections();
        assertEquals(selections.size(), 1);
        assertTrue(selections.contains(ContextMenuSelections.DELETE_THIS_HAZARD
                .getValue()));

    }

    @Test
    public void oneSelectedHazardIsEnding() {
        buildSingleEvent(HazardStatus.ENDING);
        selections = buildSelections();
        assertEquals(selections.size(), 1);

        assertTrue(selections.contains(ContextMenuSelections.REVERT_THIS_HAZARD
                .getValue()));

    }

    private ObservedHazardEvent buildSingleEvent(HazardStatus status) {
        ObservedHazardEvent event = mock(ObservedHazardEvent.class);
        when(event.getStatus()).thenReturn(status);
        events.add(event);
        when(eventManager.getCurrentEvent()).thenReturn(event);
        when(eventManager.isCurrentEvent()).thenReturn(true);
        when(eventManager.isSelected(event)).thenReturn(true);
        return event;
    }

    private ObservedHazardEvent buildEvent(HazardStatus status) {
        ObservedHazardEvent result = mock(ObservedHazardEvent.class);
        when(result.getStatus()).thenReturn(status);
        return result;
    }

    private List<String> buildSelections() {
        List<IContributionItem> items = contextMenuHelper
                .getSelectedHazardManagementItems(Originator.OTHER);
        List<String> selections = selectionsFromItems(items);
        return selections;
    }

    private List<String> selectionsFromItems(List<IContributionItem> items) {
        List<String> result = new ArrayList<>();
        for (IContributionItem item : items) {
            ActionContributionItem aitem = (ActionContributionItem) item;
            result.add(aitem.getAction().getText());
        }
        return result;
    }

    @Test
    public void noSelected() {
        when(eventManager.getSelectedEvents()).thenReturn(
                Collections.EMPTY_LIST);
        List<IContributionItem> items = contextMenuHelper
                .getSelectedHazardManagementItems(Originator.OTHER);
        assertEquals(items.size(), 0);
    }
}
