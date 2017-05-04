/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package gov.noaa.gsd.viz.hazards.contextmenu;

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.hazards.UIOriginator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionSelectionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Give the context menus for different places in Hazard Services.
 * 
 * TODO: It would perhaps be better in terms of adhering to the H.S. MVP
 * architecture to separate the SWT-specific (i.e. view-related) code out of
 * this and end up with two classes, one that generates abstract menu items, the
 * other that is an SWT helper that turns the abstract ones into concrete SWT
 * menu items.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 10, 2014            mnash        Initial creation
 * May 05, 2014    2925    Chris.Golden Removed requirement that an
 *                                      issued event be unmodified to
 *                                      warrant inclusion of the
 *                                      end-selected-hazards menu item.
 * Dec 05, 2014    4124    Chris.Golden Changed to work with newly
 *                                      parameterized config manager.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * Jan 29, 2015 4375       Dan Schaffer Console initiation of RVS product generation
 * Feb  7, 2015 4375       Dan Schaffer Fixed duplicate context menu entries bug
 * Feb 12, 2015 4959       Dan Schaffer Modify MB3 add/remove UGCs to match Warngen
 * Apr 10, 2015 6898       Chris.Cody   Refactored async messaging
 * May 21, 2015 7730       Chris.Cody   Move Add/Delete Vertex to top of Context Menu
 * Sep 15, 2015 7629       Robert.Blum  Added new context menus for saving pending hazards.
 * Jun 23, 2016 19537      Chris.Golden Removed option of adding/removing areas if a
 *                                      hazard event is of a non-hatching type.
 * Jul 25, 2016 19537      Chris.Golden Completely revamped, including the removal of any
 *                                      spatial-display-specific menu item creation or
 *                                      handling; these menu items are now created and
 *                                      handled within the spatial display components.
 * Feb 01, 2017 15556      Chris.Golden Cleaned up, added revert to latest saved copy
 *                                      menu item, changed to use new selection manager,
 *                                      and added handling of selected historical versions
 *                                      of hazard events.
 * Feb 16, 2017 29138      Chris.Golden Changed to remove notion of visibility of events
 *                                      in the history list, since all events in the
 *                                      history list are now visible. Also changed to
 *                                      not persist events upon status changes when they
 *                                      should not be saved to the database.
 * Mar 30, 2017 15528      Chris.Golden Changed to use new version of saveEvents().
 * May 04, 2017 15561      Chris.Golden Fixed ConcurrentModificationException that occurred
 *                                      when deleting more than one event at once.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class ContextMenuHelper {

    // Public Enumerated Types

    /**
     * Context menu selections.
     * 
     * TODO: This should probably be private, but some test classes need access
     * to it. If that requirement gets refactored away in the future, make this
     * private.
     */
    public enum ContextMenuSelections {
        END_ALL_SELECTED_HAZARDS(),

        REVERT_ALL_SELECTED_HAZARDS(),

        REVERT_THIS_HAZARD_TO_LAST_SAVED("Revert Selected to Last Saved"),

        END_THIS_HAZARD(appendThis(EventCommand.END)),

        REVERT_THIS_HAZARD(appendThis(EventCommand.REVERT)),

        DELETE_THIS_HAZARD(appendThis(EventCommand.DELETE)),

        PROPOSE_THIS_HAZARD(appendThis(EventCommand.PROPOSE)),

        REMOVE_POTENTIAL_HAZARDS("Remove Potential"),

        SAVE_THIS_HAZARD(appendThis(EventCommand.SAVE)),

        SAVE_ALL_PENDING_HAZARDS("Save All Pending");

        private final String value;

        private ContextMenuSelections() {
            this.value = "Error: undefined";
        }

        private static String appendThis(EventCommand command) {
            return command.value + " This";
        }

        private ContextMenuSelections(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // Private Enumerated Types

    /**
     * Event command.
     */
    private enum EventCommand {
        DELETE("Delete"), PROPOSE("Propose"), END("End"), REVERT("Revert"), SAVE(
                "Save");

        private String value;

        private EventCommand(String value) {
            this.value = value;
        }
    }

    // Public Classes

    /**
     * Context menu creator.
     */
    private class ContextMenuCreator implements IMenuCreator {

        private final IContributionItem[] actions;

        private ContextMenuCreator(final IContributionItem... actions) {
            this.actions = actions;
        }

        Menu menu = null;

        @Override
        public Menu getMenu(Menu parent) {
            if (menu == null) {
                menu = new Menu(parent);
                fill();
            }
            return menu;
        }

        @Override
        public Menu getMenu(Control parent) {
            if (menu == null) {
                menu = new Menu(parent);
                fill();
            }
            return menu;
        }

        @Override
        public void dispose() {
            if (menu != null && menu.isDisposed() == false) {
                menu.dispose();
            }
        }

        private void fill() {
            for (IContributionItem action : actions) {
                action.fill(menu, -1);
            }
        }
    }

    // Private Variables

    /**
     * Session event manager.
     */
    private final ISessionEventManager<ObservedHazardEvent> eventManager;

    /**
     * Session selection manager.
     */
    private final ISessionSelectionManager<ObservedHazardEvent> selectionManager;

    /**
     * Runnable asynchronous scheduler, used to schedule the execution of any
     * actions that are invoked via the context menu.
     */
    private final IRunnableAsynchronousScheduler scheduler;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param sessionManager
     *            Session manager.
     * @param scheduler
     *            Runnable asynchronous scheduler, used to schedule the
     *            execution of any actions that are invoked via the context
     *            menu.
     */
    public ContextMenuHelper(
            ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager,
            IRunnableAsynchronousScheduler scheduler) {
        this.eventManager = sessionManager.getEventManager();
        this.selectionManager = sessionManager.getSelectionManager();
        this.scheduler = scheduler;
    }

    // Public Methods

    /**
     * Get the items in the menu that apply to managing selected hazards.
     * 
     * @param originator
     *            Originator of any actions for the contribution items to be
     *            created.
     * @return Created contribution items.
     */
    public List<IContributionItem> getSelectedHazardManagementItems(
            IOriginator originator) {
        List<IContributionItem> items = new ArrayList<>();

        if (originator == UIOriginator.CONSOLE) {
            Set<String> selectedEventIdentifiers = selectionManager
                    .getSelectedEventIdentifiers();
            if (selectedEventIdentifiers.size() == 1) {
                String identifier = selectedEventIdentifiers.iterator().next();
                boolean enabled = (selectionManager
                        .isSelected(new Pair<String, Integer>(identifier, null)) && (eventManager
                        .getHistoricalVersionCountForEvent(identifier) > 0));
                addContributionItem(items,
                        ContextMenuSelections.REVERT_THIS_HAZARD_TO_LAST_SAVED
                                .getValue(), enabled, originator);
            }
        }

        boolean saveAllPendingAdded = false;
        ObservedHazardEvent currentEvent = null;
        if (eventManager.isCurrentEvent()) {
            currentEvent = eventManager.getCurrentEvent();
            if (selectionManager.isSelected(currentEvent)) {
                HazardStatus status = currentEvent.getStatus();
                switch (status) {

                case PENDING:
                    addContributionItem(
                            items,
                            ContextMenuSelections.DELETE_THIS_HAZARD.getValue(),
                            originator);
                    if (currentEvent.getHazardType() != null) {
                        addContributionItem(items,
                                ContextMenuSelections.PROPOSE_THIS_HAZARD
                                        .getValue(), originator);
                        addContributionItem(items,
                                ContextMenuSelections.SAVE_THIS_HAZARD
                                        .getValue(), originator);
                    }
                    Collection<ObservedHazardEvent> pendingEvents = eventManager
                            .getEventsByStatus(HazardStatus.PENDING, false);
                    if (pendingEvents.size() > 1) {
                        addContributionItem(items,
                                ContextMenuSelections.SAVE_ALL_PENDING_HAZARDS
                                        .getValue(), originator);
                        saveAllPendingAdded = true;
                    }
                    break;

                case ISSUED:
                    addContributionItem(items,
                            ContextMenuSelections.END_THIS_HAZARD.getValue(),
                            originator);
                    break;

                case ENDING:
                    addContributionItem(
                            items,
                            ContextMenuSelections.REVERT_THIS_HAZARD.getValue(),
                            originator);
                    break;

                case PROPOSED:
                    addContributionItem(
                            items,
                            ContextMenuSelections.DELETE_THIS_HAZARD.getValue(),
                            originator);
                    break;

                default:
                    break;

                }
            }
        }

        EnumSet<HazardStatus> states = EnumSet.noneOf(HazardStatus.class);
        EnumSet<HazardStatus> saveableStates = EnumSet
                .noneOf(HazardStatus.class);
        for (ObservedHazardEvent event : selectionManager.getSelectedEvents()) {

            /*
             * Do not consider the current event when tallying states since the
             * user can already apply operations to the current event from the
             * logic above. Also do not consider hazard events for which the
             * current version is not selected.
             */
            if ((currentEvent != null) && event.equals(currentEvent)) {
                continue;
            }
            if ((originator == UIOriginator.CONSOLE)
                    && (selectionManager.isSelected(new Pair<String, Integer>(
                            event.getEventID(), null)) == false)) {
                continue;
            }
            states.add(event.getStatus());
            if (event.getHazardType() != null) {
                saveableStates.add(event.getStatus());
            }
        }
        if (states.contains(HazardStatus.PENDING)) {
            int numEvents = getNumberOfSelectedEventsForStatus(HazardStatus.PENDING);
            String textWithoutCommand = String.format(" %d Selected Pending",
                    numEvents);
            String text = EventCommand.DELETE.value + textWithoutCommand;
            addContributionItem(items, text, originator);
            boolean areProposableEvents = false;
            for (ObservedHazardEvent event : selectionManager
                    .getSelectedEvents()) {
                if (event.getStatus().equals(HazardStatus.PENDING)
                        && event.getHazardType() != null) {
                    areProposableEvents = true;
                    break;
                }
            }
            if (areProposableEvents) {
                text = EventCommand.PROPOSE.value + textWithoutCommand;
                addContributionItem(items, text, originator);
            }
            if (saveableStates.contains(HazardStatus.PENDING)) {
                text = EventCommand.SAVE.value + textWithoutCommand;
                addContributionItem(items, text, originator);
            }
            if (saveAllPendingAdded == false) {
                Collection<ObservedHazardEvent> pendingEvents = eventManager
                        .getEventsByStatus(HazardStatus.PENDING, false);
                if (pendingEvents.size() > 1) {
                    addContributionItem(items,
                            ContextMenuSelections.SAVE_ALL_PENDING_HAZARDS
                                    .getValue(), originator);
                }
            }
        }
        if (states.contains(HazardStatus.ISSUED)) {
            int numEvents = getNumberOfSelectedEventsForStatus(HazardStatus.ISSUED);
            String text = String.format("%s %d Selected Issued",
                    EventCommand.END.value, numEvents);
            addContributionItem(items, text, originator);
        }

        if (states.contains(HazardStatus.ENDING)) {
            int numEvents = getNumberOfSelectedEventsForStatus(HazardStatus.ENDING);
            String text = String.format("%s %d Selected Ending",
                    EventCommand.REVERT.value, numEvents);
            addContributionItem(items, text, originator);
        }

        if (states.contains(HazardStatus.PROPOSED)) {
            int numEvents = getNumberOfSelectedEventsForStatus(HazardStatus.PROPOSED);
            String text = String.format("%s %d Selected Proposed",
                    EventCommand.DELETE.value, numEvents);
            addContributionItem(items, text, originator);
        }

        if (eventManager.getEventsByStatus(HazardStatus.POTENTIAL, true)
                .isEmpty() == false) {
            items.add(newAction(
                    ContextMenuHelper.ContextMenuSelections.REMOVE_POTENTIAL_HAZARDS
                            .getValue(), originator));
        }

        return items;
    }

    /**
     * Create a menu with the specified text for the specified contribution
     * items.
     * 
     * @param menuText
     *            Label for the new menu.
     * @param actions
     *            Contribution items to be placed in the new menu.
     * @return Created menu.
     */
    public IAction createMenu(String menuText,
            final IContributionItem... actions) {
        if (actions.length != 0) {

            return new Action(menuText, Action.AS_DROP_DOWN_MENU) {
                @Override
                public IMenuCreator getMenuCreator() {
                    return new ContextMenuCreator(actions);
                }
            };
        }
        return null;
    }

    // Private Methods

    /**
     * Create a new action contribution item with the specified text.
     * 
     * @param text
     *            Text label for the new action.
     * @param originator
     *            Originator of the action.
     * @return New action.
     */
    private IContributionItem newAction(String text,
            final IOriginator originator) {
        return newAction(text, true, originator);
    }

    /**
     * Create a new action contribution item with the specified text and enabled
     * state.
     * 
     * @param text
     *            Text label for the new action.
     * @param enabled
     *            Flag indicating whether or not the action should be enabled.
     * @param originator
     *            Originator of the action.
     * @return New action.
     */
    private IContributionItem newAction(String text, boolean enabled,
            final IOriginator originator) {
        IAction action = new Action(text) {
            @Override
            public void run() {
                executeAction(getText(), originator);
            }
        };
        action.setEnabled(enabled);
        return new ActionContributionItem(action);
    }

    /**
     * Execute an action with the specified textx label using the appropriate
     * thread.
     * 
     * @param label
     *            Label of the action to be executed.
     * @param originator
     *            Originator of the action.
     */
    private void executeAction(final String label, final IOriginator originator) {
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                handleAction(label, originator);
            }
        });
    }

    /**
     * Get the number of selected events with the specified status.
     * 
     * @param status
     *            Status for which to count selected events.
     * @return Number of selected events with the specified status.
     */
    private int getNumberOfSelectedEventsForStatus(HazardStatus status) {
        int count = 0;
        for (ObservedHazardEvent event : selectionManager.getSelectedEvents()) {
            if (event.getStatus().equals(status)) {
                count += 1;
            }
        }
        return count;
    }

    /**
     * Add the a contribution item with the specified label to the specified
     * list.
     * 
     * @param items
     *            List to which to add the new contribution item.
     * @param label
     *            Text label of the new contribution item.
     * @param originator
     *            Originator of the action for the created contribution item.
     */
    private void addContributionItem(List<IContributionItem> items,
            String label, IOriginator originator) {
        items.add(newAction(label, originator));
    }

    /**
     * Add the a contribution item with the specified label and enabled state to
     * the specified list.
     * 
     * @param items
     *            List to which to add the new contribution item.
     * @param label
     *            Text label of the new contribution item.
     * @param enabled
     *            Flag indicating whether or not the new contribution item
     *            should be enabled.
     * @param originator
     *            Originator of the action for the created contribution item.
     */
    private void addContributionItem(List<IContributionItem> items,
            String label, boolean enabled, IOriginator originator) {
        items.add(newAction(label, enabled, originator));
    }

    /**
     * Handle the action represented by the specified menu label.
     * 
     * @param menuLabel
     *            Label string of the selected context menu item.
     * @param originator
     *            Originator of the action.
     */
    private void handleAction(String menuLabel, IOriginator originator) {
        if (menuLabel.equals(ContextMenuSelections.END_THIS_HAZARD.getValue())) {
            ObservedHazardEvent event = eventManager.getCurrentEvent();
            initiateEndingProcess(event, originator);
            selectionManager.setSelectedEvents(Sets.newHashSet(event),
                    originator);
        } else if (menuLabel
                .equals(ContextMenuSelections.REVERT_THIS_HAZARD_TO_LAST_SAVED
                        .getValue())) {
            Set<String> eventIdentifiers = selectionManager
                    .getSelectedEventIdentifiers();
            if (eventIdentifiers.size() == 1) {
                eventManager.revertEventToLastSaved(eventIdentifiers.iterator()
                        .next());
            }
        } else if (menuLabel
                .contains(ContextMenuSelections.REMOVE_POTENTIAL_HAZARDS
                        .getValue())) {
            for (ObservedHazardEvent event : eventManager.getEventsByStatus(
                    HazardStatus.POTENTIAL, true)) {
                eventManager.removeEvent(event, originator);
            }
        } else if (menuLabel.contains(EventCommand.END.value)
                && menuLabel.toLowerCase().contains(
                        HazardStatus.ISSUED.getValue())) {
            for (ObservedHazardEvent event : selectionManager
                    .getSelectedEvents()) {

                /*
                 * It's possible, for example, for the selected hazards to be a
                 * pending and an issued. We only want to end the issued one.
                 */
                if (event.getStatus().equals(HazardStatus.ISSUED)) {
                    initiateEndingProcess(event, originator);
                }
            }
            selectionManager.setSelectedEvents(
                    selectionManager.getSelectedEvents(), originator);

        } else if (menuLabel.contains(ContextMenuSelections.REVERT_THIS_HAZARD
                .getValue())) {
            ObservedHazardEvent event = eventManager.getCurrentEvent();
            revertEndingProcess(event, originator);

        } else if (menuLabel.contains(EventCommand.REVERT.value)
                && menuLabel.toLowerCase().contains(
                        HazardStatus.ENDING.getValue())) {
            for (ObservedHazardEvent event : selectionManager
                    .getSelectedEvents()) {
                if (event.getStatus().equals(HazardStatus.ENDING)) {
                    revertEndingProcess(event, originator);
                }
            }

        } else if (menuLabel.equals(ContextMenuSelections.DELETE_THIS_HAZARD
                .getValue())) {
            ObservedHazardEvent event = eventManager.getCurrentEvent();
            eventManager.removeEvent(event, originator);

        } else if (menuLabel.contains(EventCommand.DELETE.value)
                && menuLabel.toLowerCase().contains(
                        HazardStatus.PENDING.getValue())) {
            List<ObservedHazardEvent> selectedEvents = new ArrayList<>(
                    selectionManager.getSelectedEvents());
            for (ObservedHazardEvent event : selectedEvents) {
                if (event.getStatus().equals(HazardStatus.PENDING)) {
                    eventManager.removeEvent(event, originator);
                }
            }

        } else if (menuLabel.equals(ContextMenuSelections.PROPOSE_THIS_HAZARD
                .getValue())) {
            ObservedHazardEvent event = eventManager.getCurrentEvent();
            eventManager.proposeEvent(event, originator);

        } else if (menuLabel.contains(EventCommand.PROPOSE.value)
                && menuLabel.toLowerCase().contains(
                        HazardStatus.PENDING.getValue())) {
            eventManager.proposeEvents(selectionManager.getSelectedEvents(),
                    originator);

        } else if (menuLabel.equals(ContextMenuSelections.SAVE_THIS_HAZARD
                .getValue())) {
            List<IHazardEvent> events = Lists
                    .<IHazardEvent> newArrayList(eventManager.getCurrentEvent());
            eventManager.saveEvents(events, true, false);
        } else if (menuLabel.contains(EventCommand.SAVE.value)
                && menuLabel.toLowerCase().contains(
                        HazardStatus.PENDING.getValue())) {
            List<IHazardEvent> events = new ArrayList<IHazardEvent>(
                    eventManager.getEventsByStatus(HazardStatus.PENDING, false));
            eventManager.saveEvents(events, true, false);
        }
    }

    /**
     * Initiate the ending process for the specified hazard event.
     * 
     * @param event
     *            Event to be altered.
     * @param originator
     *            Originator of the change.
     */
    private void initiateEndingProcess(ObservedHazardEvent event,
            IOriginator originator) {
        event.setStatus(HazardStatus.ENDING, false, originator);
    }

    /**
     * Revert the ending process for the specified hazard event.
     * 
     * @param event
     *            Event to be altered.
     * @param originator
     *            Originator of the change.
     */
    private void revertEndingProcess(ObservedHazardEvent event,
            IOriginator originator) {
        event.setStatus(HazardStatus.ISSUED, false, originator);
    }
}
