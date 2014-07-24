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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CONTEXT_MENU_ADD_VERTEX;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CONTEXT_MENU_DELETE_VERTEX;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;

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
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class ContextMenuHelper {

    public enum ContextMenuSelections {
        END_ALL_SELECTED_HAZARDS("End Selected"),

        REVERT_ALL_SELECTED_HAZARDS("Revert Selected"),

        DELETE_ALL_SELECTED_HAZARDS("Delete Selected"),

        PROPOSE_ALL_SELECTED_HAZARDS("Propose Selected"),

        END_THIS_HAZARD("End This Hazard"),

        REVERT_THIS_HAZARD("Revert This Hazard"),

        DELETE_THIS_HAZARD("Delete This Hazard"),

        PROPOSE_THIS_HAZARD("Propose This Hazard"),

        REMOVE_POTENTIAL_HAZARDS("Remove Potential"),

        ;

        private final String value;

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
    public ContextMenuHelper(HazardServicesPresenter<?> presenter,
            ISessionEventManager<ObservedHazardEvent> eventManager) {
        this.presenter = presenter;
        this.eventManager = eventManager;
    }

    /**
     * Get the items in the menu that apply to selected hazards
     * 
     * @param states
     * @return
     */
    public List<IContributionItem> getSelectedHazardManagementItems() {
        List<IContributionItem> items = new ArrayList<>();

        EnumSet<HazardStatus> states = EnumSet.noneOf(HazardStatus.class);
        for (ObservedHazardEvent event : eventManager.getSelectedEvents()) {
            states.add(event.getStatus());
        }
        if (eventManager.getSelectedEvents().isEmpty() == false) {
            if (states.contains(HazardStatus.PROPOSED) == false) {
                items.add(newAction(ContextMenuSelections.PROPOSE_ALL_SELECTED_HAZARDS
                        .getValue()));
            }
            if (states.contains(HazardStatus.ISSUED) == false) {
                items.add(newAction(ContextMenuSelections.DELETE_ALL_SELECTED_HAZARDS
                        .getValue()));
            }
            if (states.contains(HazardStatus.ISSUED)) {
                items.add(newAction(ContextMenuSelections.END_ALL_SELECTED_HAZARDS
                        .getValue()));
            }
            if (states.contains(HazardStatus.ENDING)) {
                items.add(newAction(ContextMenuSelections.REVERT_ALL_SELECTED_HAZARDS
                        .getValue()));
            }
        }

        return items;
    }

    public List<IContributionItem> getHazardSpatialItems() {
        List<IContributionItem> items = new ArrayList<>();
        if (eventManager.getSelectedEvents().isEmpty() == false) {
            items.add(newAction("Back"));
            items.add(newAction("Front"));
        }
        return items;
    }

    public List<IContributionItem> getSpatialHazardItems(boolean drawing,
            boolean moving) {
        List<IContributionItem> items = new ArrayList<>();
        for (ObservedHazardEvent event : eventManager.getSelectedEvents()) {
            if (event.getHazardType() != null) {
                items.add(newAction(HazardConstants.CONTEXT_MENU_CLIP_AND_REDUCE_SELECTED_HAZARDS));
            }/*
              * Logic to handle hazard-specific contributions to the context
              * menu. This is used, for example, by the "Add/Remove Shapes"
              * entry which applies to hazard geometries created by the
              * draw-by-area tool.
              */
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

                        items.add(newAction(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES));
                        break;
                    }
                }
            }
        }
        if (moving) {
            items.add(newAction(CONTEXT_MENU_DELETE_VERTEX));
        } else if (drawing) {
            items.add(newAction(CONTEXT_MENU_ADD_VERTEX));
        }

        return items;
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
            ObservedHazardEvent event = eventManager.getLastSelectedEvent();
            initiateEndingProcess(event);

        } else if (menuLabel
                .equals(ContextMenuSelections.END_ALL_SELECTED_HAZARDS
                        .getValue())) {
            for (ObservedHazardEvent event : eventManager.getSelectedEvents()) {
                initiateEndingProcess(event);
            }

        } else if (menuLabel
                .equals(ContextMenuSelections.REVERT_ALL_SELECTED_HAZARDS
                        .getValue())) {
            for (ObservedHazardEvent event : eventManager.getSelectedEvents()) {
                revertEndingProcess(event);
            }

        } else {
            /*
             * TODO Handle these actions the correct way.
             */
            SpatialDisplayAction action = new SpatialDisplayAction(
                    SpatialDisplayAction.ActionType.CONTEXT_MENU_SELECTED, 0,
                    menuLabel);
            presenter.fireAction(action);
        }
    }

    private void initiateEndingProcess(ObservedHazardEvent event) {
        event.setStatus(HazardStatus.ENDING);
    }

    private void revertEndingProcess(ObservedHazardEvent event) {
        event.setStatus(HazardStatus.ISSUED);
    }
}
