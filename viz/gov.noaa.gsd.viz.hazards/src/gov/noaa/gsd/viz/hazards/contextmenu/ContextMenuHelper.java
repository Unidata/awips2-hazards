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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.*;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;

/**
 * Give the context menus for different places in Hazard Services
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
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class ContextMenuHelper {

    private enum Command {
        DELETE("Delete"), PROPOSE("Propose"), END("End"), REVERT("Revert"), SAVE(
                "Save");

        private String value;

        private Command(String value) {
            this.value = value;
        }
    }

    public enum ContextMenuSelections {
        END_ALL_SELECTED_HAZARDS(),

        REVERT_ALL_SELECTED_HAZARDS(),

        DELETE_ALL_SELECTED_HAZARDS(),

        PROPOSE_ALL_SELECTED_HAZARDS(),

        END_THIS_HAZARD(appendThis(Command.END)),

        REVERT_THIS_HAZARD(appendThis(Command.REVERT)),

        DELETE_THIS_HAZARD(appendThis(Command.DELETE)),

        PROPOSE_THIS_HAZARD(appendThis(Command.PROPOSE)),

        REMOVE_POTENTIAL_HAZARDS("Remove Potential"),

        SAVE_THIS_HAZARD(appendThis(Command.SAVE)),

        SAVE_ALL_PENDING_HAZARDS("Save All Pending"),

        ;

        private final String value;

        private ContextMenuSelections() {
            this.value = "Error: undefined";
        }

        private static String appendThis(Command command) {
            return command.value + " This";
        }

        private ContextMenuSelections(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private final HazardServicesPresenter<?> presenter;

    private final ISessionEventManager<ObservedHazardEvent> eventManager;

    /**
     * 
     */
    public ContextMenuHelper(
            HazardServicesPresenter<?> presenter,
            ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager) {
        this.presenter = presenter;
        this.eventManager = sessionManager.getEventManager();
    }

    /**
     * Get the items in the menu that apply to selected hazards
     * 
     * @param states
     * @return
     */
    public List<IContributionItem> getSelectedHazardManagementItems() {
        List<IContributionItem> items = new ArrayList<>();

        ObservedHazardEvent currentEvent = null;
        if (eventManager.isCurrentEvent()) {
            currentEvent = eventManager.getCurrentEvent();
            if (eventManager.isSelected(currentEvent)) {
                HazardStatus status = currentEvent.getStatus();
                switch (status) {

                case PENDING:
                    addContributionItem(items,
                            ContextMenuSelections.DELETE_THIS_HAZARD.getValue());
                    if (currentEvent.getHazardType() != null) {
                        addContributionItem(items,
                                ContextMenuSelections.PROPOSE_THIS_HAZARD
                                        .getValue());
                    }
                    addContributionItem(items,
                            ContextMenuSelections.SAVE_THIS_HAZARD.getValue());
		    Collection<ObservedHazardEvent> pendingEvents = eventManager
                            .getEventsByStatus(HazardStatus.PENDING);
                    if (pendingEvents.size() > 1) {
                        addContributionItem(items,
                                ContextMenuSelections.SAVE_ALL_PENDING_HAZARDS
                                        .getValue());
                    }
                    break;

                case ISSUED:
                    addContributionItem(items,
                            ContextMenuSelections.END_THIS_HAZARD.getValue());
                    break;

                case ENDING:
                    addContributionItem(items,
                            ContextMenuSelections.REVERT_THIS_HAZARD.getValue());
                    break;

                case PROPOSED:
                    addContributionItem(items,
                            ContextMenuSelections.DELETE_THIS_HAZARD.getValue());
                    break;

                default:
                    break;

                }
            }
        }

        EnumSet<HazardStatus> states = EnumSet.noneOf(HazardStatus.class);
        for (ObservedHazardEvent event : eventManager.getSelectedEvents()) {
            /*
             * Don't consider the current event when tallying states since the
             * user can already apply operations to the current event from the
             * logic above.
             */
            if (currentEvent != null && event.equals(currentEvent)) {
                continue;
            } else {
                states.add(event.getStatus());
            }
        }
        int n;
        String text;
        if (states.contains(HazardStatus.PENDING)) {
            n = numSelectedEventsForStatus(HazardStatus.PENDING);
            String textWithoutCommand = String
                    .format(" %d Selected Pending", n);
            text = Command.DELETE.value + textWithoutCommand;
            addContributionItem(items, text);
            boolean areProposableEvents = false;
            for (ObservedHazardEvent event : eventManager.getSelectedEvents()) {
                if (event.getStatus().equals(HazardStatus.PENDING)
                        && event.getHazardType() != null) {
                    areProposableEvents = true;
                    break;
                }
            }
            if (areProposableEvents) {
                text = Command.PROPOSE.value + textWithoutCommand;
                addContributionItem(items, text);
            }
            text = Command.SAVE.value + textWithoutCommand;
            addContributionItem(items, text);
            Collection<ObservedHazardEvent> pendingEvents = eventManager
                    .getEventsByStatus(HazardStatus.PENDING);
            if (pendingEvents.size() > 1) {
                addContributionItem(items,
                        ContextMenuSelections.SAVE_ALL_PENDING_HAZARDS
                                .getValue());
            }
        }
        if (states.contains(HazardStatus.ISSUED)) {
            n = numSelectedEventsForStatus(HazardStatus.ISSUED);
            text = String.format("%s %d Selected Issued", Command.END.value, n);
            addContributionItem(items, text);
        }

        if (states.contains(HazardStatus.ENDING)) {
            n = numSelectedEventsForStatus(HazardStatus.ENDING);
            text = String.format("%s %d Selected Ending", Command.REVERT.value,
                    n);
            addContributionItem(items, text);
        }

        if (states.contains(HazardStatus.PROPOSED)) {
            n = numSelectedEventsForStatus(HazardStatus.PROPOSED);
            text = String.format("%s %d Selected Proposed",
                    Command.DELETE.value, n);
            addContributionItem(items, text);
        }

        return items;
    }

    private int numSelectedEventsForStatus(HazardStatus status) {
        int count = 0;
        for (ObservedHazardEvent event : eventManager.getSelectedEvents()) {
            if (event.getStatus().equals(status)) {
                count += 1;
            }
        }
        return count;
    }

    private void addContributionItem(List<IContributionItem> items, String label) {
        items.add(newAction(label));
    }

    public List<IContributionItem> getHazardSpatialItems() {
        List<IContributionItem> items = new ArrayList<>();
        if (eventManager.getSelectedEvents().isEmpty() == false) {
            items.add(newAction("Back"));
            items.add(newAction("Front"));
        }
        return items;
    }

    /**
     * Logic to handle hazard-specific contributions to the context menu. This
     * is used, for example, by the "Add/Remove Shapes" entry which applies to
     * hazard geometries created by the draw-by-area tool.
     */
    public List<IContributionItem> getSpatialHazardItems() {

        List<String> itemNames = new ArrayList<>();
        if (eventManager.isCurrentEvent()) {
            addResolutionTogglesForEvent(itemNames,
                    eventManager.getCurrentEvent(),
                    CONTEXT_MENU_HIGH_RESOLUTION_GEOMETRY_FOR_CURRENT_EVENT,
                    CONTEXT_MENU_LOW_RESOLUTION_GEOMETRY_FOR_CURRENT_EVENT);
        }

        for (ObservedHazardEvent event : eventManager.getSelectedEvents()) {
            addResolutionTogglesForEvent(itemNames, event,
                    CONTEXT_MENU_HIGH_RESOLUTION_GEOMETRY_FOR_SELECTED_EVENTS,
                    CONTEXT_MENU_LOW_RESOLUTION_GEOMETRY_FOR_SELECTED_EVENTS);

        }
        for (ObservedHazardEvent event : eventManager.getSelectedEvents()) {
            @SuppressWarnings("unchecked")
            List<String> contextMenuEntries = (List<String>) event
                    .getHazardAttribute(HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY);

            if (contextMenuEntries != null) {
                for (String contextMenuEntry : contextMenuEntries) {

                    if (contextMenuEntry
                            .equals(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES)) {

                        if (eventManager.canEventAreaBeChanged(event) == false) {
                            continue;
                        }

                        addItemIfNotAlreadyIncluded(itemNames,
                                HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES);
                        break;
                    }
                }
            }
        }

        List<IContributionItem> items = new ArrayList<>(itemNames.size());
        for (String itemName : itemNames) {
            items.add(newAction(itemName));
        }
        return items;
    }

    private void addResolutionTogglesForEvent(List<String> itemNames,
            ObservedHazardEvent event, String highResItemName,
            String lowResItemName) {
        if (event.getHazardType() != null) {
            if (event.getHazardAttribute(VISIBLE_GEOMETRY).equals(
                    LOW_RESOLUTION_GEOMETRY_IS_VISIBLE)) {
                addItemIfNotAlreadyIncluded(itemNames, highResItemName);
            } else {
                addItemIfNotAlreadyIncluded(itemNames, lowResItemName);
            }
        }
    }

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

    public class ContextMenuAction extends Action {

        private final IContributionItem[] actions;

        private ContextMenuAction(IContributionItem... actions) {
            this.actions = actions;
        }

        @Override
        public IMenuCreator getMenuCreator() {
            return new ContextMenuCreator(actions);
        }
    }

    public class ContextMenuCreator implements IMenuCreator {

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

        public List<String> menuItemTexts() {
            List<String> result = new ArrayList<String>();
            for (IContributionItem action : actions) {
                ActionContributionItem a = (ActionContributionItem) action;
                result.add(a.getAction().getText());
            }
            return result;
        }

    }

    public IAction newTopLevelAction(String text) {
        IAction action = new Action(text) {
            @Override
            public void run() {
                super.run();
                handleAction(getText());
            }
        };
        return action;
    }

    public IContributionItem newAction(String text) {
        IAction action = new Action(text) {
            @Override
            public void run() {
                super.run();
                handleAction(getText());
            }
        };
        return new ActionContributionItem(action);
    }

    private void addItemIfNotAlreadyIncluded(List<String> itemNames, String item) {
        if (!itemNames.contains(item)) {
            itemNames.add(item);
        }
    }

    /**
     * Handle the action
     * 
     * TODO This method is now a mix of the obsolete and new correct way of
     * handling these actions. The correct way is to update the model directly.
     * 
     * @param menuLabel
     *            The label string of the selected context menu item
     */
    private void handleAction(String menuLabel) {

        if (menuLabel.equals(ContextMenuSelections.END_THIS_HAZARD.getValue())) {
            /*
             * Here we update the model directly.
             */
            ObservedHazardEvent event = eventManager.getCurrentEvent();
            initiateEndingProcess(event);
            eventManager.setSelectedEvents(Lists.newArrayList(event),
                    Originator.OTHER);

        } else if (menuLabel.contains(Command.END.value)
                && menuLabel.toLowerCase().contains(
                        HazardStatus.ISSUED.getValue())) {
            for (ObservedHazardEvent event : eventManager.getSelectedEvents()) {

                /**
                 * It's possible, for example, for the selected hazards to be a
                 * pending and an issued. We only want to end the issued one.
                 */
                if (event.getStatus().equals(HazardStatus.ISSUED)) {
                    initiateEndingProcess(event);
                }
            }
            eventManager.setSelectedEvents(eventManager.getSelectedEvents(),
                    Originator.OTHER);

        } else if (menuLabel.contains(ContextMenuSelections.REVERT_THIS_HAZARD
                .getValue())) {
            ObservedHazardEvent event = eventManager.getCurrentEvent();
            revertEndingProcess(event);

        } else if (menuLabel.contains(Command.REVERT.value)
                && menuLabel.toLowerCase().contains(
                        HazardStatus.ENDING.getValue())) {
            for (ObservedHazardEvent event : eventManager.getSelectedEvents()) {
                if (event.getStatus().equals(HazardStatus.ENDING)) {
                    revertEndingProcess(event);
                }
            }

        } else if (menuLabel.equals(ContextMenuSelections.DELETE_THIS_HAZARD
                .getValue())) {
            ObservedHazardEvent event = eventManager.getCurrentEvent();
            eventManager.removeEvent(event, Originator.OTHER);

        } else if (menuLabel.contains(Command.DELETE.value)
                && menuLabel.toLowerCase().contains(
                        HazardStatus.PENDING.getValue())) {
            for (ObservedHazardEvent event : eventManager.getSelectedEvents()) {
                if (event.getStatus().equals(HazardStatus.PENDING)) {
                    eventManager.removeEvent(event, Originator.OTHER);
                }
            }

        } else if (menuLabel.equals(ContextMenuSelections.PROPOSE_THIS_HAZARD
                .getValue())) {
            ObservedHazardEvent event = eventManager.getCurrentEvent();
            eventManager.proposeEvent(event, Originator.OTHER);

        } else if (menuLabel.contains(Command.PROPOSE.value)
                && menuLabel.toLowerCase().contains(
                        HazardStatus.PENDING.getValue())) {
            eventManager.proposeEvents(eventManager.getSelectedEvents(),
                    Originator.OTHER);

        } else if (menuLabel.contains(Command.SAVE.value)
                && menuLabel.toLowerCase().contains(
                        HazardStatus.PENDING.getValue())) {
            List<IHazardEvent> events = new ArrayList<IHazardEvent>(
                    eventManager.getEventsByStatus(HazardStatus.PENDING));
            eventManager.saveEvents(events);
        } else if (menuLabel.equals(ContextMenuSelections.SAVE_THIS_HAZARD
                .getValue())) {
            List<IHazardEvent> events = new ArrayList<IHazardEvent>(1);
            events.add(eventManager.getCurrentEvent());
            eventManager.saveEvents(events);

        } else {
            /*
             * TODO Handle these actions the correct way.
             */
            SpatialDisplayAction action = new SpatialDisplayAction(
                    SpatialDisplayAction.ActionType.CONTEXT_MENU_SELECTED, 0,
                    menuLabel);
            presenter.publish(action);
        }
    }

    private void initiateEndingProcess(ObservedHazardEvent event) {
        event.setStatus(HazardStatus.ENDING);
    }

    private void revertEndingProcess(ObservedHazardEvent event) {
        event.setStatus(HazardStatus.ISSUED);
    }
}
