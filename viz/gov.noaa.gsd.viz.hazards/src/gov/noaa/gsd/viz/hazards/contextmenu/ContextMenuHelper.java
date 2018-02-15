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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.common.dataplugin.events.locks.LockInfo;
import com.raytheon.uf.common.dataplugin.events.locks.LockInfo.LockStatus;
import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.uf.common.hazards.productgen.data.ProductDataUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionSelectionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.locks.ISessionLockManager;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.VizWorkbenchManager;

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.detailsviewer.EventDetailsDialog;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.action.ProductAction;

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
 * Apr 04, 2016 15192      Robert.Blum  Added new "Copy This" context menu option.
 * Jun 23, 2016 19537      Chris.Golden Removed option of adding/removing areas if a
 *                                      hazard event is of a non-hatching type.
 * Jul 01, 2016 19212      Thomas.Gurney Fix "Delete N Selected Proposed" entry
 * Jul 25, 2016 19537      Chris.Golden Completely revamped, including the removal of any
 *                                      spatial-display-specific menu item creation or
 *                                      handling; these menu items are now created and
 *                                      handled within the spatial display components.
 * Aug 12, 2016 20386      dgilling     Add ability to delete Proposed, Potential and 
 *                                      Pending events together when selected.
 * Sep 26, 2016 21758      Chris.Golden Changed calls to removeEvent()/removeEvents() to
 *                                      provide new parameter.
 * Oct 12, 2016 21424      Kevin.Bisanz Fixed "1 minutes ago" in Correct This MB3 menu.
 * Dec 12, 2016 21504      Robert.Blum  Updates for hazard locking.
 * Feb 01, 2017 15556      Chris.Golden Cleaned up, added revert to latest saved copy
 *                                      menu item, changed to use new selection manager,
 *                                      and added handling of selected historical versions
 *                                      of hazard events.
 * Feb 16, 2017 29138      Chris.Golden Changed to remove notion of visibility of events
 *                                      in the history list, since all events in the
 *                                      history list are now visible. Also changed to
 *                                      not persist events upon status changes when they
 *                                      should not be saved to the database.
 * Mar 24, 2017 30537      Kevin.Bisanz Fix event detail view to work on pending events.
 * Mar 30, 2017 15528      Chris.Golden Changed to use new version of saveEvents().
 * Apr 03, 2017 32574      bkowal       Confirm that the user would like to end every
 *                                      issued hazard event when all possible issued events
 *                                      have been selected.
 * May 02, 2017 33739      mduff        Display confirmation dialog for ending all visible
 *                                      hazards only if ending all visible.
 * May 04, 2017 15561      Chris.Golden Fixed ConcurrentModificationException that occurred
 *                                      when deleting more than one event at once.
 * May 15, 2017 34069      mduff        Added handling for HazardStatus.ELAPSING.
 * Jun 26, 2017 19207      Chris.Golden Changes to view products for specific events.
 * Jun 30, 2017 19223      Chris.Golden Added ability to change the text and enabled state
 *                                      of a menu item based upon a contribution item made
 *                                      by an instance of this class after said menu item
 *                                      is displayed. Also added "correct selected" menu
 *                                      item.
 * Sep 27, 2017 38072      Chris.Golden Added use of batched messages.
 * Dec 17, 2017 20739      Chris.Golden Refactored away access to directly mutable session
 *                                      events.
 * Apr 23, 2018 15561      Chris.Golden Added ability to delete non-hazardous events.
 * Apr 24, 2018 22308      Chris.Golden Changed product viewer to work with viewing of
 *                                      products coming from text database.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class ContextMenuHelper {

    // Private Static Constants

    /**
     * String to be used to indicate an action A is to be performed on selected
     * events (with the A being filled in using
     * {@link String#format(String, Object...)}).
     */
    private static final String FORMAT_CAPABLE_ACTION_UPON_SELECTED_TEXT = "%s Selected";

    /**
     * String to be used to indicate that a menu item is not enabled because it
     * is in the process of being finalized using long-running queries.
     */
    private static final String QUERYING_TEXT = " (querying...)";

    /**
     * String to be used to indicate that a menu item relies upon the fact that
     * something was issued 1 minute ago.
     */
    private static final String FORMAT_CAPABLE_ISSUED_1_MINUTE_AGO_TEXT = " (issued 1 minute ago)";

    /**
     * String to be used to indicate that a menu item relies upon the fact that
     * something was issued N minutes ago (with the N being filled in using
     * {@link String#format(String, Object...)}).
     */
    private static final String FORMAT_CAPABLE_ISSUED_N_MINUTES_AGO_TEXT = " (issued %d minute(s) ago)";

    /**
     * String used to indicate that a menu item cannot be used because the event
     * lies outside the correction window.
     */
    private static final String OUTSIDE_CORRECTION_WINDOW_TEXT = " (outside correction window)";

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ContextMenuHelper.class);

    // Public Enumerated Types

    /**
     * Context menu selections.
     * 
     * TODO: This should probably be private, but some test classes need access
     * to it. If that requirement gets refactored away in the future, make this
     * private.
     */
    public enum ContextMenuSelections {

        VIEW_DETAILS_FOR_SELECTED_EVENTS(
                "View Details for Selected Event(s)..."),

        END_ALL_SELECTED_HAZARDS(),

        REVERT_ALL_SELECTED_HAZARDS(),

        REVERT_THIS_HAZARD_TO_LAST_SAVED("Revert Selected to Last Saved"),

        END_THIS_HAZARD(appendThis(EventCommand.END)),

        REVERT_THIS_HAZARD(appendThis(EventCommand.REVERT)),

        DELETE_THIS_HAZARD(appendThis(EventCommand.DELETE)),

        PROPOSE_THIS_HAZARD(appendThis(EventCommand.PROPOSE)),

        COPY_THIS_HAZARD(appendThis(EventCommand.COPY)),

        REMOVE_POTENTIAL_HAZARDS("Remove Potential"),

        VIEW_PRODUCTS_FOR_SELECTED_EVENTS("View Products For Selected Events"),

        SAVE_THIS_HAZARD(appendThis(EventCommand.SAVE)),

        SAVE_ALL_PENDING_HAZARDS("Save All Pending"),

        BREAK_LOCK_ON_THIS_HAZARD("Break Hazard Lock");

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
        DELETE("Delete"), PROPOSE("Propose"), END("End"), REVERT(
                "Revert"), SAVE("Save"), COPY("Copy"), CORRECT("Correct");

        private String value;

        private EventCommand(String value) {
            this.value = value;
        }
    }

    // Public Interfaces

    /**
     * Interface describing the methods that must be implemented in order to
     * receive updates about {@link IContributionItem} objects that were
     * previously returned by invocations of
     * {@link #getSelectedHazardManagementItems(IOriginator)}.
     */
    public interface IContributionItemUpdater {

        /**
         * Handle an update to the specified contribution item.
         * 
         * @param contributionItem
         *            Item that has been updated.
         * @param text
         *            New text for the contribution item. #param enabled Flag
         *            indicating whether or not the item should be enabled.
         */
        public void handleContributionItemUpdate(IContributionItem item,
                String text, boolean enabled);
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
     * Session manager.
     */
    private final ISessionManager<ObservedSettings> sessionManager;

    /**
     * Session event manager.
     */
    private final ISessionEventManager eventManager;

    /**
     * Session lock manager.
     */
    private final ISessionLockManager lockManager;

    /**
     * Session selection manager.
     */
    private final ISessionSelectionManager selectionManager;

    /**
     * Session product manager.
     */
    private final ISessionProductManager productManager;

    /**
     * Runnable asynchronous scheduler, used to schedule the execution of any
     * actions that are invoked via the context menu.
     */
    private final IRunnableAsynchronousScheduler scheduler;

    /**
     * Presenter to be used for {@link HazardServicesPresenter#publish(Object)}.
     * 
     * @deprecated This should be removed when there is no longer any need for
     *             use of the <code>publish()</code> method.
     */
    @Deprecated
    private final HazardServicesPresenter<?> presenter;

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
     * @param presenter
     *            Presenter to be used for
     *            {@link HazardServicesPresenter#publish(Object)}.
     */
    public ContextMenuHelper(ISessionManager<ObservedSettings> sessionManager,
            IRunnableAsynchronousScheduler scheduler,
            HazardServicesPresenter<?> presenter) {
        this.sessionManager = sessionManager;
        this.eventManager = sessionManager.getEventManager();
        this.lockManager = sessionManager.getLockManager();
        this.selectionManager = sessionManager.getSelectionManager();
        this.productManager = sessionManager.getProductManager();
        this.scheduler = scheduler;
        this.presenter = presenter;
    }

    // Public Methods

    /**
     * Get the items in the menu that apply to managing selected hazards.
     * 
     * @param originator
     *            Originator of any actions for the contribution items to be
     *            created.
     * @param contributionItemUpdater
     *            If provided, this updater will be notified after this method
     *            returns whenever a contribution item that was created by this
     *            method is updated asynchronously.
     * @return Created contribution items.
     */
    public List<IContributionItem> getSelectedHazardManagementItems(
            IOriginator originator,
            final IContributionItemUpdater contributionItemUpdater) {
        List<IContributionItem> items = new ArrayList<>();

        if (originator == UIOriginator.CONSOLE) {
            Set<String> selectedEventIdentifiers = selectionManager
                    .getSelectedEventIdentifiers();
            if (selectedEventIdentifiers.size() == 1) {
                String identifier = selectedEventIdentifiers.iterator().next();
                LockInfo info = lockManager.getHazardEventLockInfo(identifier);
                LockStatus lockStatus = info.getLockStatus();
                boolean enabled = ((lockStatus == LockStatus.LOCKED_BY_ME)
                        && selectionManager.isSelected(
                                new Pair<String, Integer>(identifier, null))
                        && (eventManager.getHistoricalVersionCountForEvent(
                                identifier) > 0));
                addContributionItem(items,
                        ContextMenuSelections.REVERT_THIS_HAZARD_TO_LAST_SAVED
                                .getValue(),
                        enabled, originator);
            }
        }

        boolean saveAllPendingAdded = false;
        IHazardEventView currentEvent = null;
        if (eventManager.isCurrentEvent()) {
            currentEvent = eventManager.getCurrentEvent();
            boolean hazardous = eventManager.isHazardous(currentEvent);
            LockInfo info = lockManager
                    .getHazardEventLockInfo(currentEvent.getEventID());
            LockStatus lockStatus = info.getLockStatus();
            if (selectionManager.isSelected(currentEvent)) {
                HazardStatus status = currentEvent.getStatus();
                switch (status) {

                case PENDING:
                    if (lockStatus != LockStatus.LOCKED_BY_OTHER) {
                        addContributionItem(items,
                                ContextMenuSelections.DELETE_THIS_HAZARD
                                        .getValue(),
                                originator);

                        if (eventManager.isProposedStateAllowed(currentEvent)) {
                            addContributionItem(items,
                                    ContextMenuSelections.PROPOSE_THIS_HAZARD
                                            .getValue(),
                                    originator);
                        }
                    }
                    break;

                case ISSUED:
                    if (lockStatus != LockStatus.LOCKED_BY_OTHER) {
                        if (hazardous) {
                            addContributionItem(items,
                                    ContextMenuSelections.END_THIS_HAZARD
                                            .getValue(),
                                    originator);
                        } else {
                            addContributionItem(items,
                                    ContextMenuSelections.DELETE_THIS_HAZARD
                                            .getValue(),
                                    originator);
                        }
                    }
                    break;

                case ENDING:
                    if (lockStatus != LockStatus.LOCKED_BY_OTHER) {
                        addContributionItem(items,
                                ContextMenuSelections.REVERT_THIS_HAZARD
                                        .getValue(),
                                originator);
                        if (hazardous == false) {
                            addContributionItem(items,
                                    ContextMenuSelections.DELETE_THIS_HAZARD
                                            .getValue(),
                                    originator);
                        }
                    }
                    break;

                case ELAPSING:
                    if (lockStatus != LockStatus.LOCKED_BY_OTHER) {
                        if (hazardous) {
                            addContributionItem(items,
                                    ContextMenuSelections.END_THIS_HAZARD
                                            .getValue(),
                                    originator);
                        } else {
                            addContributionItem(items,
                                    ContextMenuSelections.DELETE_THIS_HAZARD
                                            .getValue(),
                                    originator);
                        }
                    }
                    break;

                case PROPOSED:
                    if (lockStatus != LockStatus.LOCKED_BY_OTHER) {
                        addContributionItem(items,
                                ContextMenuSelections.DELETE_THIS_HAZARD
                                        .getValue(),
                                originator);
                    }
                    break;

                case POTENTIAL:
                    if (lockStatus != LockStatus.LOCKED_BY_OTHER) {
                        addContributionItem(items,
                                ContextMenuSelections.DELETE_THIS_HAZARD
                                        .getValue(),
                                originator);
                    }
                    break;

                default:
                    break;

                }
                if (selectionManager.getSelectedEvents().size() == 1) {
                    addContributionItem(items,
                            ContextMenuSelections.COPY_THIS_HAZARD.getValue(),
                            originator);
                    if (lockStatus != LockStatus.LOCKED_BY_OTHER) {
                        if (currentEvent.getHazardType() != null) {
                            addContributionItem(items,
                                    ContextMenuSelections.SAVE_THIS_HAZARD
                                            .getValue(),
                                    originator);
                        }
                    } else if (lockStatus == LockStatus.LOCKED_BY_OTHER) {
                        addContributionItem(items,
                                ContextMenuSelections.BREAK_LOCK_ON_THIS_HAZARD
                                        .getValue(),
                                originator);
                    }
                }
            }
        }

        EnumSet<HazardStatus> states = EnumSet.noneOf(HazardStatus.class);
        EnumSet<HazardStatus> notLockedByOthersStates = EnumSet
                .noneOf(HazardStatus.class);
        EnumSet<HazardStatus> saveableStates = EnumSet
                .noneOf(HazardStatus.class);
        for (IHazardEventView event : selectionManager.getSelectedEvents()) {

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
            LockInfo info = lockManager
                    .getHazardEventLockInfo(event.getEventID());
            LockStatus lockStatus = info.getLockStatus();
            if (lockStatus != LockStatus.LOCKED_BY_OTHER) {
                notLockedByOthersStates.add(event.getStatus());
                if (event.getHazardType() != null) {
                    saveableStates.add(event.getStatus());
                }
            }
        }

        if ((states.contains(HazardStatus.PROPOSED))
                || (states.contains(HazardStatus.PENDING))
                || (states.contains(HazardStatus.POTENTIAL))) {
            int numEvents = getNumberOfSelectedEventsForStatus(
                    HazardStatus.PROPOSED);
            numEvents += getNumberOfSelectedEventsForStatus(
                    HazardStatus.PENDING);
            numEvents += getNumberOfSelectedEventsForStatus(
                    HazardStatus.POTENTIAL);
            String text = String.format("%s %d Selected",
                    EventCommand.DELETE.value, numEvents);
            addContributionItem(items, text, originator);
        }
        if (states.contains(HazardStatus.PENDING)) {
            int numEvents = getNumberOfSelectedEventsForStatus(
                    HazardStatus.PENDING);
            String textWithoutCommand = String.format(" %d Selected Pending",
                    numEvents);
            boolean areProposableEvents = false;
            for (IHazardEventView event : selectionManager
                    .getSelectedEvents()) {
                if (event.getStatus().equals(HazardStatus.PENDING)
                        && eventManager.isProposedStateAllowed(event)) {
                    areProposableEvents = true;
                    break;
                }
            }
            if (areProposableEvents) {
                String text = EventCommand.PROPOSE.value + textWithoutCommand;
                addContributionItem(items, text, originator);
            }
            if (saveableStates.contains(HazardStatus.PENDING)) {
                String text = EventCommand.SAVE.value + textWithoutCommand;
                addContributionItem(items, text, originator);
            }
            if (saveAllPendingAdded == false) {
                Collection<IHazardEventView> pendingEvents = eventManager
                        .getEventsByStatus(HazardStatus.PENDING, false);
                if (pendingEvents.size() > 1) {
                    addContributionItem(items,
                            ContextMenuSelections.SAVE_ALL_PENDING_HAZARDS
                                    .getValue(),
                            originator);
                }
            }
        }
        if (notLockedByOthersStates.contains(HazardStatus.ISSUED)) {
            int numEvents = getNumberOfSelectedEventsForStatus(
                    HazardStatus.ISSUED);
            String text = String.format("%s %d Selected Issued",
                    EventCommand.END.value, numEvents);
            addContributionItem(items, text, originator);
        }

        if (notLockedByOthersStates.contains(HazardStatus.ENDING)) {
            int numEvents = getNumberOfSelectedEventsForStatus(
                    HazardStatus.ENDING);
            String text = String.format("%s %d Selected Ending",
                    EventCommand.REVERT.value, numEvents);
            addContributionItem(items, text, originator);
        }

        List<IHazardEventView> selectedEvents = selectionManager
                .getSelectedEvents();
        if ((originator == UIOriginator.CONSOLE) && (selectedEvents.size() == 1)
                && HazardStatus
                        .hasEverBeenIssued(selectedEvents.get(0).getStatus())) {
            if (contributionItemUpdater == null) {
                statusHandler
                        .error("No contribution item updater supplied for originator "
                                + originator
                                + "; no correction menu item will be created.");
            } else {

                /*
                 * Only create a correction menu item if the event is not locked
                 * by someone else.
                 */
                final IHazardEventView event = selectedEvents.get(0);
                LockInfo info = lockManager
                        .getHazardEventLockInfo(event.getEventID());
                LockStatus lockStatus = info.getLockStatus();
                if (lockStatus != LockStatus.LOCKED_BY_OTHER) {

                    /*
                     * Create the correction contribution item, and schedule the
                     * execution of a query to determine whether the item should
                     * be enabled or not, and then to update it with new text
                     * and enabled flag status appropriately.
                     */
                    final String baseText = String.format(
                            FORMAT_CAPABLE_ACTION_UPON_SELECTED_TEXT,
                            EventCommand.CORRECT.value);
                    String text = baseText + QUERYING_TEXT;
                    final IContributionItem item = addContributionItem(items,
                            text, false, originator);

                    scheduler.schedule(new Runnable() {

                        @Override
                        public void run() {

                            List<IHazardEventView> eventsToFilter = new ArrayList<>(
                                    1);
                            eventsToFilter.add(event);

                            Date currentTime = SimulatedTime.getSystemTime()
                                    .getTime();
                            String mode = CAVEMode.getMode().toString();

                            List<ProductData> correctableEvents = ProductDataUtil
                                    .retrieveCorrectableProductDataForEvents(
                                            mode, currentTime, eventsToFilter);

                            long issuedAgoMin = -1;
                            boolean enableMenu = false;

                            /*
                             * If there is a correctable event, enable the
                             * contribution item.
                             */
                            if (correctableEvents != null
                                    && correctableEvents.size() > 0) {
                                ProductData pd = correctableEvents.get(0);

                                long issueMs = pd.getIssueTime().getTime();
                                long issuedAgoMs = currentTime.getTime()
                                        - issueMs;

                                /*
                                 * Convert from milliseconds to minutes,
                                 * truncating any remainder.
                                 */
                                issuedAgoMin = issuedAgoMs
                                        / TimeUtil.MILLIS_PER_MINUTE;

                                enableMenu = true;
                            }

                            /*
                             * Set the text appropriately and update the
                             * contribution item.
                             */
                            String updatedText = baseText + (enableMenu
                                    ? (issuedAgoMin == 1
                                            ? FORMAT_CAPABLE_ISSUED_1_MINUTE_AGO_TEXT
                                            : String.format(
                                                    FORMAT_CAPABLE_ISSUED_N_MINUTES_AGO_TEXT,
                                                    issuedAgoMin))
                                    : OUTSIDE_CORRECTION_WINDOW_TEXT);
                            contributionItemUpdater
                                    .handleContributionItemUpdate(item,
                                            updatedText, enableMenu);
                        }
                    });
                }
            }
        }

        if (eventManager.getEventsByStatus(HazardStatus.POTENTIAL, true)
                .isEmpty() == false) {
            items.add(newAction(
                    ContextMenuHelper.ContextMenuSelections.REMOVE_POTENTIAL_HAZARDS
                            .getValue(),
                    originator));
        }

        for (IHazardEventView event : selectionManager.getSelectedEvents()) {

            /*
             * Do not consider hazard events for which the current version is
             * not selected, or events that have never been issued.
             */
            if ((originator == UIOriginator.CONSOLE)
                    && (selectionManager.isSelected(new Pair<String, Integer>(
                            event.getEventID(), null)) == false)) {
                continue;
            }
            if (HazardStatus.hasEverBeenIssued(event.getStatus()) == false) {
                continue;
            }

            items.add(newAction(
                    ContextMenuHelper.ContextMenuSelections.VIEW_PRODUCTS_FOR_SELECTED_EVENTS
                            .getValue(),
                    originator));
            break;
        }

        if ((originator == UIOriginator.CONSOLE) && (selectionManager
                .getSelectedEventIdentifiers().size() > 0)) {
            addContributionItem(items,
                    ContextMenuSelections.VIEW_DETAILS_FOR_SELECTED_EVENTS
                            .getValue(),
                    originator);
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
    private void executeAction(final String label,
            final IOriginator originator) {
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                handleAction(label, originator);
            }
        });
    }

    /**
     * Get the number of selected events with the specified status and which
     * are, if it matters for that state, not locked by others.
     * 
     * @param status
     *            Status for which to count selected events.
     * @return Number of selected events with the specified status.
     */
    private int getNumberOfSelectedEventsForStatus(HazardStatus status) {
        int count = 0;
        for (IHazardEventView event : selectionManager.getSelectedEvents()) {
            if (event.getStatus().equals(status)) {
                if (status.equals(HazardStatus.ENDING)
                        || status.equals(HazardStatus.ISSUED)
                        || status.equals(HazardStatus.PROPOSED)) {
                    LockInfo info = lockManager
                            .getHazardEventLockInfo(event.getEventID());
                    LockStatus lockStatus = info.getLockStatus();
                    if (lockStatus == LockStatus.LOCKED_BY_OTHER) {
                        continue;
                    }
                }
                count++;
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
     * @return Contribution item that was added.
     */
    private IContributionItem addContributionItem(List<IContributionItem> items,
            String label, IOriginator originator) {
        IContributionItem contributionItem = newAction(label, originator);
        items.add(contributionItem);
        return contributionItem;
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
     * @return Contribution item that was added.
     */
    private IContributionItem addContributionItem(List<IContributionItem> items,
            String label, boolean enabled, IOriginator originator) {
        IContributionItem contributionItem = newAction(label, enabled,
                originator);
        items.add(contributionItem);
        return contributionItem;
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
        if (menuLabel
                .equals(ContextMenuSelections.VIEW_DETAILS_FOR_SELECTED_EVENTS
                        .getValue())) {
            List<IHazardEventView> events = selectionManager
                    .getSelectedEvents();
            if (events.size() > 0) {
                List<List<IHazardEventView>> eventList = new ArrayList<>();
                for (IHazardEventView latestEvent : events) {
                    List<IHazardEventView> historyList = eventManager
                            .getEventHistoryById(latestEvent.getEventID());

                    List<IHazardEventView> tempList = new ArrayList<>();
                    if (historyList != null) {
                        tempList.addAll(historyList);
                    }

                    /*
                     * If nothing was added from the history list (hazard not
                     * yet saved/issued) or the the current event does not equal
                     * the latest saved/issued event, add the current event to
                     * the list.
                     */
                    if (tempList.isEmpty() || (tempList.get(tempList.size() - 1)
                            .equals(latestEvent) == false)) {
                        tempList.add(latestEvent);
                    }

                    eventList.add(tempList);
                }
                EventDetailsDialog detailsDlg = new EventDetailsDialog(
                        VizWorkbenchManager.getInstance().getCurrentWindow()
                                .getShell(),
                        eventList);
                detailsDlg.open();
            }
        } else if (menuLabel
                .equals(ContextMenuSelections.END_THIS_HAZARD.getValue())) {
            IHazardEventView event = eventManager.getCurrentEvent();
            eventManager.initiateEventEndingProcess(event, originator);
            selectionManager.setSelectedEvents(Sets.newHashSet(event),
                    originator);
        } else if (menuLabel
                .equals(ContextMenuSelections.REVERT_THIS_HAZARD_TO_LAST_SAVED
                        .getValue())) {
            Set<String> eventIdentifiers = selectionManager
                    .getSelectedEventIdentifiers();
            if (eventIdentifiers.size() == 1) {
                eventManager.revertEventToLastSaved(
                        eventIdentifiers.iterator().next(), originator);
            }
        } else if (menuLabel.contains(
                ContextMenuSelections.REMOVE_POTENTIAL_HAZARDS.getValue())) {
            sessionManager.startBatchedChanges();
            for (IHazardEventView event : eventManager
                    .getEventsByStatus(HazardStatus.POTENTIAL, true)) {
                eventManager.removeEvent(event, false, originator);
            }
            sessionManager.finishBatchedChanges();
        } else if (menuLabel.startsWith(EventCommand.CORRECT.value)) {
            correctSelectedEvent();
        } else if (menuLabel.contains(
                ContextMenuSelections.VIEW_PRODUCTS_FOR_SELECTED_EVENTS
                        .getValue())) {
            productManager.showUserProductViewerSelection();
        } else if (menuLabel.contains(EventCommand.END.value) && menuLabel
                .toLowerCase().contains(HazardStatus.ISSUED.getValue())) {
            if (eventManager.initiateSelectedEventsEndingProcess(originator)) {
                selectionManager.setSelectedEvents(
                        selectionManager.getSelectedEvents(), originator);
            }
        } else if (menuLabel.contains(
                ContextMenuSelections.REVERT_THIS_HAZARD.getValue())) {
            IHazardEventView event = eventManager.getCurrentEvent();
            eventManager.revertEventEndingProcess(event, originator);

        } else if (menuLabel.contains(EventCommand.REVERT.value) && menuLabel
                .toLowerCase().contains(HazardStatus.ENDING.getValue())) {
            for (IHazardEventView event : selectionManager
                    .getSelectedEvents()) {
                if (event.getStatus().equals(HazardStatus.ENDING)) {
                    eventManager.revertEventEndingProcess(event, originator);
                }
            }

        } else if (menuLabel
                .equals(ContextMenuSelections.DELETE_THIS_HAZARD.getValue())) {
            IHazardEventView event = eventManager.getCurrentEvent();
            eventManager.removeEvent(event, true, originator);

        } else if (menuLabel.contains(EventCommand.DELETE.value)) {
            Collection<IHazardEventView> toBeDeletedHazards = new ArrayList<>();
            for (IHazardEventView event : selectionManager
                    .getSelectedEvents()) {
                if ((event.getStatus().equals(HazardStatus.PROPOSED))
                        || (event.getStatus().equals(HazardStatus.POTENTIAL))
                        || (event.getStatus().equals(HazardStatus.PENDING))) {
                    toBeDeletedHazards.add(event);
                }
            }
            eventManager.removeEvents(toBeDeletedHazards, true, originator);

        } else if (menuLabel
                .equals(ContextMenuSelections.PROPOSE_THIS_HAZARD.getValue())) {
            IHazardEventView event = eventManager.getCurrentEvent();
            eventManager.proposeEvent(event, originator);

        } else if (menuLabel.contains(EventCommand.PROPOSE.value) && menuLabel
                .toLowerCase().contains(HazardStatus.PENDING.getValue())) {
            eventManager.proposeEvents(selectionManager.getSelectedEvents(),
                    originator);

        } else if (menuLabel
                .equals(ContextMenuSelections.SAVE_THIS_HAZARD.getValue())) {
            eventManager.saveEvents(
                    Lists.newArrayList(eventManager.getCurrentEvent()), true,
                    false, false, originator);
        } else if (menuLabel.contains(EventCommand.SAVE.value) && menuLabel
                .toLowerCase().contains(HazardStatus.PENDING.getValue())) {
            eventManager.saveEvents(
                    new ArrayList<>(eventManager
                            .getEventsByStatus(HazardStatus.PENDING, false)),
                    true, false, false, originator);
        } else if (menuLabel
                .equals(ContextMenuSelections.COPY_THIS_HAZARD.getValue())) {
            eventManager.copyEvents(
                    Lists.newArrayList(eventManager.getCurrentEvent()));
        } else if (menuLabel
                .equals(ContextMenuHelper.ContextMenuSelections.BREAK_LOCK_ON_THIS_HAZARD
                        .getValue())) {
            if (eventManager.isCurrentEvent()) {
                eventManager.breakEventLock(eventManager.getCurrentEvent());
            }
        }
    }

    /**
     * Correct the single selected event.
     */
    private void correctSelectedEvent() {
        boolean displayError = true;

        List<IHazardEventView> events = selectionManager.getSelectedEvents();
        if ((events != null) && (events.size() == 1)) {
            IHazardEventView event = events.get(0);

            List<IHazardEventView> eventsToFilter = new ArrayList<>(1);
            eventsToFilter.add(event);
            List<ProductData> correctableEvents = ProductDataUtil
                    .retrieveCorrectableProductDataForEvents(
                            CAVEMode.getMode().toString(),
                            SimulatedTime.getSystemTime().getTime(),
                            eventsToFilter);

            /*
             * If there is a correctable event, open the product editor.
             */
            if (correctableEvents != null && correctableEvents.size() > 0) {

                ProductAction action = new ProductAction(
                        ProductAction.ActionType.REVIEW);
                Map<String, Serializable> parameters = new HashMap<>();
                parameters.put(HazardConstants.PRODUCT_DATA_PARAM,
                        (Serializable) correctableEvents);
                action.setParameters(parameters);
                presenter.publish(action);

                displayError = false;
            }
        }

        if (displayError) {

            /*
             * Code execution might hit this case if the user sits with the
             * context menu open and the product is now outside the correction
             * time window.
             */
            statusHandler.warn("Unable to correct event");
        }
    }
}
