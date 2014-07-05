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
import java.util.EnumSet;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
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

    private final ISessionManager<ObservedHazardEvent> manager;

    private final HazardServicesPresenter<?> presenter;

    /**
     * 
     */
    public ContextMenuHelper(HazardServicesPresenter<?> presenter,
            ISessionManager<ObservedHazardEvent> manager) {
        this.presenter = presenter;
        this.manager = manager;
    }

    /**
     * Get the items in the menu that apply to selected hazards
     * 
     * @param states
     * @param includeGlobal
     * @return
     */
    public List<IContributionItem> getSelectedHazardManagementItems(
            boolean includeGlobal) {
        List<IContributionItem> items = new ArrayList<>();

        EnumSet<HazardStatus> states = EnumSet.noneOf(HazardStatus.class);
        for (ObservedHazardEvent event : manager.getEventManager()
                .getSelectedEvents()) {
            states.add(event.getStatus());
        }
        if (manager.getEventManager().getSelectedEvents().isEmpty() == false) {
            if (states.contains(HazardStatus.PROPOSED) == false) {
                items.add(newAction(HazardConstants.PROPOSE_SELECTED_HAZARDS));
            }
            if (states.contains(HazardStatus.ISSUED) == false) {
                items.add(newAction("Issue Selected..."));
                items.add(new Separator());
                items.add(newAction("Delete Selected"));
            }
            if (states.contains(HazardStatus.ISSUED)) {
                items.add(newAction(HazardConstants.END_SELECTED_HAZARDS));
            }
        }
        if (includeGlobal) {
            if (manager.getEventManager()
                    .getEventsByStatus(HazardStatus.POTENTIAL).isEmpty() == false) {
                items.add(newAction(HazardConstants.REMOVE_POTENTIAL_HAZARDS));
            }
        }
        return items;
    }

    public List<IContributionItem> getHazardSpatialItems() {
        List<IContributionItem> items = new ArrayList<>();
        if (manager.getEventManager().getSelectedEvents().isEmpty() == false) {
            items.add(newAction("Back"));
            items.add(newAction("Front"));
        }
        return items;
    }

    public List<IContributionItem> getSpatialHazardItems(boolean drawing,
            boolean moving) {
        List<IContributionItem> items = new ArrayList<>();
        for (ObservedHazardEvent event : manager.getEventManager()
                .getSelectedEvents()) {
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

                        if (manager.getEventManager().canEventAreaBeChanged(
                                event) == false) {
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
            final IMenuCreator creator = new IMenuCreator() {
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
            };
            return new Action(menuText, Action.AS_DROP_DOWN_MENU) {
                @Override
                public IMenuCreator getMenuCreator() {
                    return creator;
                }
            };
        }
        return null;
    }

    private IContributionItem newAction(String text) {
        IAction action = new Action(text) {
            @Override
            public void run() {
                super.run();
                fireContextMenuItemSelected(getText());
            }
        };
        return new ActionContributionItem(action);
    }

    /**
     * When an item is selected from the right click CAVE context menu notify
     * all listeners.
     * 
     * @param menuLabel
     *            The label string of the selected context menu item
     */
    private void fireContextMenuItemSelected(String menuLabel) {
        SpatialDisplayAction action = new SpatialDisplayAction(
                SpatialDisplayAction.ActionType.CONTEXT_MENU_SELECTED, 0,
                menuLabel);
        presenter.fireAction(action);
    }
}
