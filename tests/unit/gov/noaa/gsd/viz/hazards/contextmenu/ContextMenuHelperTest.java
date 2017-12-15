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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.junit.Before;
import org.junit.Test;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventView;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionSelectionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.hazards.contextmenu.ContextMenuHelper.ContextMenuSelections;

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

    private ISessionSelectionManager selectionManager;

    private ContextMenuHelper contextMenuHelper;

    private List<IHazardEventView> events;

    private List<String> selections;

    @Before
    public void setup() {
        events = new ArrayList<>();
        ISessionManager sessionManager = mock(ISessionManager.class);
        eventManager = mock(ISessionEventManager.class);
        selectionManager = mock(ISessionSelectionManager.class);
        when(selectionManager.getSelectedEvents()).thenReturn(events);
        when(sessionManager.getEventManager()).thenReturn(eventManager);
        contextMenuHelper = new ContextMenuHelper(sessionManager,
                new IRunnableAsynchronousScheduler() {

                    @Override
                    public void schedule(Runnable runnable) {
                        VizApp.runAsync(runnable);
                    }
                }, null);
    }

    @Test
    public void oneSelectedPendingOneCurrentUnselectedIssued() {
        ObservedHazardEvent event0 = mock(ObservedHazardEvent.class);
        when(event0.getStatus()).thenReturn(HazardStatus.PENDING);
        IHazardEventView eventView0 = new HazardEventView(event0);
        events.add(eventView0);

        ObservedHazardEvent event1 = mock(ObservedHazardEvent.class);
        when(event1.getStatus()).thenReturn(HazardStatus.ISSUED);
        IHazardEventView eventView1 = new HazardEventView(event1);
        when(eventManager.getCurrentEvent()).thenReturn(eventView1);
        when(eventManager.isCurrentEvent()).thenReturn(true);
        selections = buildSelections();
        assertEquals(selections.size(), 1);
        assertTrue(selections.contains("Delete 1 Selected Pending"));
    }

    @Test
    public void multipleSelected() {
        IHazardEventView event0 = buildEvent(HazardStatus.PENDING);
        when(selectionManager.isSelected(event0)).thenReturn(true);
        events.add(event0);

        IHazardEventView event1 = buildEvent(HazardStatus.ISSUED);
        when(selectionManager.isSelected(event1)).thenReturn(true);
        events.add(event1);

        when(eventManager.getCurrentEvent()).thenReturn(event1);
        when(eventManager.isCurrentEvent()).thenReturn(true);
        selections = buildSelections();
        assertEquals(selections.size(), 2);

        assertTrue(selections
                .contains(ContextMenuSelections.END_THIS_HAZARD.getValue()));
        assertTrue(selections.contains("Delete 1 Selected Pending"));
        when(eventManager.getCurrentEvent()).thenReturn(event0);
        selections = buildSelections();
        assertEquals(selections.size(), 2);
        assertTrue(selections
                .contains(ContextMenuSelections.DELETE_THIS_HAZARD.getValue()));
        assertTrue(selections.contains("End 1 Selected Issued"));

        IHazardEventView event2 = buildEvent(HazardStatus.PENDING);
        when(selectionManager.isSelected(event2)).thenReturn(true);
        events.add(event2);

        IHazardEventView event3 = buildEvent(HazardStatus.ISSUED);
        when(selectionManager.isSelected(event3)).thenReturn(true);
        events.add(event3);

        selections = buildSelections();
        assertEquals(selections.size(), 3);
        assertTrue(selections
                .contains(ContextMenuSelections.DELETE_THIS_HAZARD.getValue()));
        assertTrue(selections.contains("Delete 2 Selected Pending"));
        assertTrue(selections.contains("End 2 Selected Issued"));
        when(eventManager.getCurrentEvent()).thenReturn(event1);
        selections = buildSelections();
        assertEquals(selections.size(), 3);
        assertTrue(selections
                .contains(ContextMenuSelections.END_THIS_HAZARD.getValue()));
        assertTrue(selections.contains("Delete 2 Selected Pending"));
        assertTrue(selections.contains("End 2 Selected Issued"));
    }

    @Test
    public void oneSelectedHazardIsPending() {
        IHazardEventView event = buildSingleEvent(HazardStatus.PENDING);
        when(selectionManager.isSelected(event)).thenReturn(true);
        selections = buildSelections();
        assertEquals(selections.size(), 1);

        assertTrue(selections
                .contains(ContextMenuSelections.DELETE_THIS_HAZARD.getValue()));

        when(event.getHazardType()).thenReturn("FA.A");
        selections = buildSelections();
        assertEquals(selections.size(), 2);
        assertTrue(selections.contains(
                ContextMenuSelections.PROPOSE_THIS_HAZARD.getValue()));

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

        assertTrue(selections
                .contains(ContextMenuSelections.END_THIS_HAZARD.getValue()));

    }

    @Test
    public void oneSelectedHazardIsProposed() {
        buildSingleEvent(HazardStatus.PROPOSED);
        selections = buildSelections();
        assertEquals(selections.size(), 1);
        assertTrue(selections
                .contains(ContextMenuSelections.DELETE_THIS_HAZARD.getValue()));

    }

    @Test
    public void oneSelectedHazardIsEnding() {
        buildSingleEvent(HazardStatus.ENDING);
        selections = buildSelections();
        assertEquals(selections.size(), 1);

        assertTrue(selections
                .contains(ContextMenuSelections.REVERT_THIS_HAZARD.getValue()));

    }

    private IHazardEventView buildSingleEvent(HazardStatus status) {
        ObservedHazardEvent event = mock(ObservedHazardEvent.class);
        when(event.getStatus()).thenReturn(status);
        IHazardEventView eventView = new HazardEventView(event);
        events.add(eventView);
        when(eventManager.getCurrentEvent()).thenReturn(eventView);
        when(eventManager.isCurrentEvent()).thenReturn(true);
        when(selectionManager.isSelected(eventView)).thenReturn(true);
        return eventView;
    }

    private IHazardEventView buildEvent(HazardStatus status) {
        ObservedHazardEvent result = mock(ObservedHazardEvent.class);
        when(result.getStatus()).thenReturn(status);
        return new HazardEventView(result);
    }

    private List<String> buildSelections() {
        List<IContributionItem> items = contextMenuHelper
                .getSelectedHazardManagementItems(Originator.OTHER, null);
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
        when(selectionManager.getSelectedEvents())
                .thenReturn(Collections.EMPTY_LIST);
        List<IContributionItem> items = contextMenuHelper
                .getSelectedHazardManagementItems(Originator.OTHER, null);
        assertEquals(items.size(), 0);
    }
}
