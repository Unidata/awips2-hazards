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
import gov.noaa.gsd.viz.hazards.contextmenu.ContextMenuHelper.ContextMenuSelections;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.junit.Before;
import org.junit.Test;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;

/**
 * Description: Test of {@link ContextMenuHelper}
 * 
 * TODO. Complete this as part of the ContextMenu reorganization code review.
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

    @Before
    public void setup() {
        events = new ArrayList<>();
        eventManager = mock(ISessionEventManager.class);
        when(eventManager.getSelectedEvents()).thenReturn(events);
        contextMenuHelper = new ContextMenuHelper(null, eventManager);

    }

    @Test
    public void oneSelectedHazardIsPending() {
        ObservedHazardEvent event = mock(ObservedHazardEvent.class);
        when(event.getStatus()).thenReturn(HazardStatus.PENDING);
        events.add(event);
        when(eventManager.getLastSelectedEvent()).thenReturn(event);
        List<IContributionItem> items = contextMenuHelper
                .getSelectedHazardManagementItems();
        List<String> selections = selectionsFromItems(items);
        assertTrue(selections
                .contains(ContextMenuSelections.PROPOSE_ALL_SELECTED_HAZARDS
                        .getValue()));
        assertTrue(selections
                .contains(ContextMenuSelections.DELETE_ALL_SELECTED_HAZARDS
                        .getValue()));
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
        when(eventManager.getSelectedEvents()).thenReturn(events);
        List<IContributionItem> items = contextMenuHelper
                .getSelectedHazardManagementItems();
        assertEquals(items.size(), 0);
    }
}
