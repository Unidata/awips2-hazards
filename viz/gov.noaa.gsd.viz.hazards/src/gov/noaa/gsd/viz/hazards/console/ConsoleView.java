/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.common.utilities.Sort;
import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.viz.hazards.alerts.CountdownTimer;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter.Command;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter.TimeRangeType;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter.Toggle;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter.VtecFormatMode;
import gov.noaa.gsd.viz.hazards.console.ITemporalDisplay.SelectedTimeMode;
import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;
import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.toolbar.BasicAction;
import gov.noaa.gsd.viz.hazards.toolbar.ComboAction;
import gov.noaa.gsd.viz.hazards.toolbar.IContributionManagerAware;
import gov.noaa.gsd.viz.hazards.toolbar.SeparatorAction;
import gov.noaa.gsd.viz.hazards.ui.BasicWidgetDelegateHelper;
import gov.noaa.gsd.viz.hazards.ui.CommandInvokerDelegate;
import gov.noaa.gsd.viz.hazards.ui.ListStateChangerDelegate;
import gov.noaa.gsd.viz.hazards.ui.StateChangerDelegate;
import gov.noaa.gsd.viz.hazards.ui.ViewPartDelegateView;
import gov.noaa.gsd.viz.hazards.ui.ViewPartWidgetDelegateHelper;
import gov.noaa.gsd.viz.mvp.IMainUiContributor;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.WorkbenchPage;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.views.PartAdapter2;

/**
 * Console view, an implementation of IConsoleView that provides an Eclipse
 * ViewPart-based view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * May 08, 2013            Chris.Golden      Moved view-part-managing code
 *                                           to new superclass.
 * Jul 12, 2013     585    Chris.Golden      Changed to support loading from bundle.
 * Aug 22, 2013     787    Bryon.Lawrence    Added references to constants for 
 *                                           RESET_EVENTS, RESET_SETTINGS, and 
 *                                           RESET_ACTION
 * Aug 22, 2013    1936    Chris.Golden      Added console countdown timers.
 * Oct 22, 2013    1463    Bryon.Lawrence    Added menu options for hazard 
 *                                           conflict detection.
 * Oct 22, 2013    1462    Bryon.Lawrence    Added menu options for hatched
 *                                           area display options.
 * Feb 19, 2014    2161    Chris.Golden      Added passing of set of events allowing
 *                                           "until further notice" to the view part.
 * Apr 15, 2014     696    David.Gillingham  Add ChangeVtecFormatAction to menu.
 * Apr 23, 2014    1480    jsanchez          Added a Correct menu to the console.
 * Nov 18, 2014    4124    Chris.Golden      Adapted to new time manager.
 * Dec 05, 2014    4124    Chris.Golden      Changed to use ObservedSettings.
 * Dec 13, 2014    4959    Dan Schaffer      Spatial Display cleanup and other bug fixes
 * Feb 06, 2015    2331    Chris.Golden      Removed bogus debug message, and also
 *                                           changed to use time range boundaries for
 *                                           the events.
 * May 05, 2015    6898    Chris.Cody        Pan & Scale Visible and Selected Time
 * Jul 30, 2015    9681    Robert.Blum       Added new ViewProductsAction to the console.
 * Sep 14, 2015    3473    Chris.Cody        Implement Hazard Services Import/Export
 *                                           through Central Registry server.
 * Nov 17, 2015    3473    Robert.Blum       Changed code handling backup sites to sort
 *                                           the list, and to handle null sites.
 * Nov 23, 2015    3473    Robert.Blum       Removed code for importing service backup.
 * Dec 03, 2015   13609    mduff             Set VTEC mode options based on CAVE mode.
 * Jul 25, 2016   19537    Chris.Golden      Fixed bug that sometimes manifested when
 *                                           Hazard Services was closing so that another
 *                                           session of Hazard Services could open via
 *                                           a bundle load, causing an exception.
 * Aug 15, 2016   18376    Chris.Golden      Added code to remove any actions created
 *                                           for the menu or toolbar in order to aid
 *                                           garbage collection. However, this does not
 *                                           appear to be allowing said actions to be
 *                                           garbage-collected; additional work will
 *                                           be needed under the dedicated ticket
 *                                           #21271.
 * Aug 29, 2016   19537    Chris.Golden      Changed to make show hatched areas menu
 *                                           item start in checked state.
 * Oct 19, 2016   21873    Chris.Golden      Added time resolution tracking tied to
 *                                           settings.
 * Feb 01, 2017   15556    Chris.Golden      Complete refactoring to address MVP
 *                                           design concerns, untangle spaghetti, and
 *                                           add history list viewing.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@SuppressWarnings("restriction")
public class ConsoleView extends ViewPartDelegateView<ConsoleViewPart>
        implements IConsoleView<Action, RCPMainUserInterfaceElement> {

    // Package-Private Static Constants

    /**
     * Zoom out button identifier.
     */
    static final String BUTTON_ZOOM_OUT = "zoomOut";

    /**
     * Page back button identifier.
     */
    static final String BUTTON_PAGE_BACKWARD = "backwardDay";

    /**
     * Pan back button identifier.
     */
    static final String BUTTON_PAN_BACKWARD = "backward";

    /**
     * Center on current time button identifier.
     */
    static final String BUTTON_CURRENT_TIME = "currentTime";

    /**
     * Pan forward button identifier.
     */
    static final String BUTTON_PAN_FORWARD = "forward";

    /**
     * Page forward button identifier.
     */
    static final String BUTTON_PAGE_FORWARD = "forwardDay";

    /**
     * Zoom in button identifier.
     */
    static final String BUTTON_ZOOM_IN = "zoomIn";

    /**
     * List of button identifiers, each of which is also the name of the image
     * file (without its type specifier suffix), for the command (not toolbar)
     * buttons.
     */
    static final ImmutableList<String> BUTTON_IDENTIFIERS = ImmutableList.of(
            BUTTON_ZOOM_OUT, BUTTON_PAGE_BACKWARD, BUTTON_PAN_BACKWARD,
            BUTTON_CURRENT_TIME, BUTTON_PAN_FORWARD, BUTTON_PAGE_FORWARD,
            BUTTON_ZOOM_IN);

    /**
     * Descriptions of the buttons (whether the ones below the tree widget, or
     * those on the toolbar), each of which corresponds to the file name of the
     * button at the same index in {@link #TOOLBAR_BUTTON_IMAGE_FILE_NAMES}.
     */
    static final ImmutableList<String> BUTTON_DESCRIPTIONS = ImmutableList.of(
            "Zoom Out Timeline", "Page Back Timeline", "Pan Back Timeline",
            "Show Current Time", "Pan Forward Timeline",
            "Page Forward Timeline", "Zoom In Timeline");

    /**
     * Selected time mode tooltip text.
     */
    static final String SELECTED_TIME_MODE_TEXT = "Selected Time Mode";

    // Private Static Constants

    /**
     * Toolbar button icon image file names.
     */
    private static final ImmutableList<String> TOOLBAR_BUTTON_IMAGE_FILE_NAMES = ImmutableList
            .of("timeZoomOut.png", "timeJumpBackward.png", "timeBackward.png",
                    "timeCurrent.png", "timeForward.png",
                    "timeJumpForward.png", "timeZoomIn.png");

    /**
     * Suffix for the preferences key used to determine whether or not to detach
     * the view part when next showing it (assuming that it is not being shown
     * as a result of a bundle load).
     */
    private static final String FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX = ".forceDetachConsoleWhenNextShowing";

    /**
     * Suffix for the preferences key holding the X value of the bounds of the
     * last saved detached view; this is only relevant if the value for the
     * preference key {@link #FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX} for
     * the same perspective is true.
     */
    private static final String LAST_DETACHED_BOUNDS_X_SUFFIX = ".lastDetachedBoundsX";

    /**
     * Suffix for the preferences key holding the Y value of the bounds of the
     * last saved detached view; this is only relevant if the value for the
     * preference key {@link #FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX} for
     * the same perspective is true.
     */
    private static final String LAST_DETACHED_BOUNDS_Y_SUFFIX = ".lastDetachedBoundsY";

    /**
     * Suffix for the preferences key holding the width value of the bounds of
     * the last saved detached view; this is only relevant if the value for the
     * preference key {@link #FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX} for
     * the same perspective is true.
     */
    private static final String LAST_DETACHED_BOUNDS_WIDTH_SUFFIX = ".lastDetachedBoundsWidth";

    /**
     * Suffix for the preferences key holding the height value of the bounds of
     * the last saved detached view; this is only relevant if the value for the
     * preference key {@link #FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX} for
     * the same perspective is true.
     */
    private static final String LAST_DETACHED_BOUNDS_HEIGHT_SUFFIX = ".lastDetachedBoundsHeight";

    /**
     * Reset events command menu item text.
     */
    private static final String RESET_EVENTS_COMMAND_MENU_TEXT = "Reset Events";

    /**
     * Check hazard conflicts command menu item text.
     */
    private static final String CHECK_HAZARD_CONFLICTS_MENU_TEXT = "Check Hazard Conflicts";

    /**
     * Export site configuration data command menu item text.
     */
    private static final String EXPORT_HAZARD_SITE_MENU_TEXT = "Export Hazard Site";

    /**
     * View product command menu item text.
     */
    private static final String VIEW_PRODUCT_MENU_TEXT = "View Product...";

    /**
     * Change VTEC mode menu header text.
     */
    private static final String CHANGE_VTEC_MODE_HEADER_TEXT = "Change VTEC Mode";

    /**
     * Change site menu header text.
     */
    private static final String CHANGE_SITE_HEADER_TEXT = "Change Site";

    /**
     * Auto check hazard conflicts toggle menu item text.
     */
    private static final String AUTO_CHECK_HAZARD_CONFLICTS_MENU_TEXT = "Auto Check Hazard Conflicts";

    /**
     * Show hazard area toggle menu item text.
     */
    private static final String SHOW_HATCHED_AREAS_MENU_TEXT = "Show Hatched Areas";

    /**
     * Show history lists toggle menu item text.
     */
    private static final String SHOW_HISTORY_LISTS_MENU_TEXT = "Show History Lists";

    /**
     * Scheduler to be used to make runnables get executed on the main thread.
     * For now, the main thread is the UI thread; when this is changed, this
     * will be rendered obsolete, as at that point there will need to be a
     * blocking queue of {@link Runnable} instances available to allow the new
     * worker thread to be fed jobs. At that point, this should be replaced with
     * an object that enqueues the <code>Runnable</code>s, probably a singleton
     * that may be accessed by the various components in
     * gov.noaa.gsd.viz.hazards and perhaps elsewhere.
     */
    @Deprecated
    private static final IRunnableAsynchronousScheduler RUNNABLE_ASYNC_SCHEDULER = new IRunnableAsynchronousScheduler() {

        @Override
        public void schedule(Runnable runnable) {

            /*
             * Since the UI thread is currently the thread being used for nearly
             * everything, just run any asynchronous tasks there.
             */
            VizApp.runAsync(runnable);
        }
    };

    // Package-Private Interfaces

    /**
     * Interface that must be implemented by actions in order to allow them to
     * manipulate temporal properties.
     */
    interface ITemporallyAware {

        // Public Methods

        /**
         * Set the temporal display to that specified. This is to be called
         * following construction, so that invocation of this action causes the
         * temporal command receiver to be manipulated.
         * 
         * @param temporalDisplay
         *            Temporal display.
         */
        public void setTemporalDisplay(ITemporalDisplay temporalDisplay);
    }

    // Private Classes

    /**
     * Standard console menu or toolbar command action.
     */
    private class CommandConsoleAction extends BasicAction {

        /**
         * Command to be executed for this action.
         */
        private final Command command;

        /**
         * Construct a standard instance.
         * 
         * @param text
         *            Text to be displayed.
         * @param iconFileName
         *            File name of the icon to be displayed, or
         *            <code>null</code> if no icon is to be associated with this
         *            action.
         * @param toolTipText
         *            Tool tip text, or <code>null</code> if none is required.
         * @param command
         *            Command to be executed for this action.
         */
        private CommandConsoleAction(String text, String iconFileName,
                String toolTipText, Command command) {
            super(text, iconFileName, IAction.AS_PUSH_BUTTON, toolTipText);
            this.command = command;
        }

        @Override
        public void run() {
            RUNNABLE_ASYNC_SCHEDULER.schedule(new Runnable() {

                @Override
                public void run() {
                    if (commandInvocationHandler != null) {
                        commandInvocationHandler.commandInvoked(command);
                    }
                }
            });
        }
    }

    /**
     * Standard console menu or toolbar toggle action.
     */
    private class ToggleConsoleAction extends BasicAction {

        /**
         * Toggle to have its state changed by this action.
         */
        private final Toggle toggle;

        /**
         * Construct a standard instance.
         * 
         * @param text
         *            Text to be displayed.
         * @param iconFileName
         *            File name of the icon to be displayed, or
         *            <code>null</code> if no icon is to be associated with this
         *            action.
         * @param toolTipText
         *            Tool tip text, or <code>null</code> if none is required.
         * @param toggle
         *            Toggle to have its state changed by this action.
         */
        private ToggleConsoleAction(String text, String iconFileName,
                String toolTipText, Toggle toggle) {
            super(text, iconFileName, IAction.AS_CHECK_BOX, toolTipText);
            this.toggle = toggle;
        }

        @Override
        public void run() {
            RUNNABLE_ASYNC_SCHEDULER.schedule(new Runnable() {

                @Override
                public void run() {
                    if (toggleStateChangeHandler != null) {
                        toggleStateChangeHandler.stateChanged(toggle,
                                isChecked());
                    }
                }
            });
        }
    }

    /**
     * Review/correct products menu command action.
     */
    private class ReviewAndCorrectProductsAction extends BasicAction {

        // Private Classes

        /**
         * Menu creator used to create the individual menu items that allow the
         * user to begin the review and/or correction process of particular
         * products.
         */
        private class MenuCreator implements IMenuCreator {

            // Private Variables

            /**
             * Menu to be populated.
             */
            private Menu menu;

            /**
             * Map of product data elements to the text used to represent them
             * in their menu items. This is used during the menu creation
             * process, and is otherwise empty.
             */
            private final Map<List<ProductData>, String> textForProductData = new IdentityHashMap<>();

            /**
             * Menu listener used to determine when this menu is to be shown and
             * to repopulate with the latest product data at that time.
             */
            private final MenuListener listener = new MenuAdapter() {

                @Override
                public void menuShown(MenuEvent e) {

                    /*
                     * Clear the old menu items out, then rebuild them.
                     */
                    for (MenuItem item : menu.getItems()) {
                        item.dispose();
                    }
                    createMenuItems();
                }
            };

            // Public Methods

            @Override
            public void dispose() {
                menu.removeMenuListener(listener);
                menu.dispose();
                menu = null;
            }

            @Override
            public Menu getMenu(Control parent) {
                return getMenu(parent.getMenu());
            }

            @Override
            public Menu getMenu(Menu parent) {
                if (menu != null) {
                    menu.dispose();
                }
                menu = new Menu(parent);
                menu.addMenuListener(listener);
                createMenuItems();
                return menu;
            }

            // Private Methods

            /**
             * Fill in the menu with the menu items appropriate to the current
             * product data.
             */
            private void createMenuItems() {

                /*
                 * Get the product data elements from which to build the menu
                 * items. If none are available, create a single "no entry" menu
                 * item that is disabled; otherwise, create a menu item for each
                 * element.
                 */
                List<List<ProductData>> productData = presenter
                        .getReviewMenuItems();
                if (productData != null) {

                    /*
                     * Precalculate the text to be used to represent the product
                     * data elements, since it is used in the sort process
                     * below, and the actual menu item generation as well. This
                     * avoids having to recalculate each one two or more times.
                     */
                    for (List<ProductData> list : productData) {
                        textForProductData.put(list, createMenuItemText(list));
                    }

                    /*
                     * Sort the product data so that the text strings shown in
                     * the menu items are in alphabetical order.
                     */
                    Collections.sort(productData,
                            new Comparator<List<ProductData>>() {

                                @Override
                                public int compare(List<ProductData> o1,
                                        List<ProductData> o2) {
                                    String text1 = textForProductData.get(o1);
                                    String text2 = textForProductData.get(o2);
                                    return text1.compareTo(text2);
                                }
                            });

                    /*
                     * Create the menu items.
                     */
                    for (List<ProductData> list : productData) {
                        addReviewAndCorrectProductMenuItem(
                                textForProductData.get(list), list);
                    }

                    /*
                     * Clear out the map of product data to text.
                     */
                    textForProductData.clear();
                } else {
                    addEmptyMenuItem();
                }
            }

            /**
             * Add an empty menu item indicating that there are no reviewable
             * and correctable products.
             */
            private void addEmptyMenuItem() {
                MenuItem item = new MenuItem(menu, SWT.PUSH);
                item.setText("None available");
                item.setEnabled(false);
            }

            /**
             * Add a review-and-correct-product menu item.
             * 
             * @param text
             *            Text to be shown in the menu item.
             * @param productData
             *            Products to be reviewed and/or corrected if the menu
             *            item is invoked.
             */
            private void addReviewAndCorrectProductMenuItem(String text,
                    List<ProductData> productData) {
                IContributionItem item = new ActionContributionItem(
                        new ReviewAndCorrectProductsAction(text, productData));
                item.fill(menu, -1);
            }

            /**
             * Create the text for a review-and-correct-product menu item.
             * 
             * @param productData
             *            Product data from which to generate the text.
             * @return Text to be used.
             */
            private String createMenuItemText(List<ProductData> productData) {
                ProductData first = productData.get(0);
                String productIdentifier = first.getProductGeneratorName()
                        .replace("_ProductGenerator", "");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(productIdentifier);
                stringBuilder.append(" - ");
                stringBuilder.append(Joiner.on(",").join(first.getEventIDs()));
                return stringBuilder.toString();
            }
        };

        // Private Variables

        /**
         * Product data to be reviewed and/or corrected when this item is
         * invoked. If <code>null</code>, there is nothing to be done when
         * invoked.
         */
        private final List<ProductData> productData;

        // Public Constructors

        /**
         * Construct an instance for holding the menu item acting as the header,
         * from which the submenu pops up listing whichever
         * review-and-correct-product menu items are appropriate.
         */
        public ReviewAndCorrectProductsAction() {
            super("Review/Correct Product(s)", null, Action.AS_DROP_DOWN_MENU,
                    null);
            this.productData = null;
            setMenuCreator(new MenuCreator());
        }

        /**
         * Construct an instance that when invoked begins the review and
         * correction process for the specified product data.
         * 
         * @param text
         *            Text to be displayed for the menu item.
         * @param productData
         *            Product data to be reviewed and/or corrected if this
         *            action is invoked.
         */
        public ReviewAndCorrectProductsAction(String text,
                List<ProductData> productData) {
            super(text, null, Action.AS_PUSH_BUTTON, null);
            this.productData = productData;
        }

        // Public Methods

        @Override
        public void run() {
            RUNNABLE_ASYNC_SCHEDULER.schedule(new Runnable() {

                @Override
                public void run() {
                    if (reviewAndCorrectProductsInvocationHandler != null) {
                        reviewAndCorrectProductsInvocationHandler
                                .commandInvoked(productData);
                    }
                }
            });
        }
    }

    /**
     * Change VTEC mode action.
     */
    private class ChangeVtecFormatAction extends BasicAction {

        // Private Classes

        /**
         * Menu creator used to create the individual menu items that allow the
         * user to change the VTEC mode.
         */
        private class MenuCreator implements IMenuCreator {

            // Private Variables

            /**
             * Menu to be populated.
             */
            private Menu menu;

            // Public Methods

            @Override
            public void dispose() {
                menu.dispose();
            }

            @Override
            public Menu getMenu(Control parent) {
                return getMenu(parent.getMenu());
            }

            @Override
            public Menu getMenu(Menu parent) {
                menu = new Menu(parent);
                createMenuItems();
                return menu;
            }

            /**
             * Fill in the menu with the menu items appropriate to the current
             * product data.
             */
            private void createMenuItems() {

                CAVEMode mode = CAVEMode.getMode();
                List<VtecFormatMode> list = new ArrayList<>();
                if (mode == CAVEMode.PRACTICE) {
                    list.add(VtecFormatMode.TEST_T_VTEC);
                    list.add(VtecFormatMode.NORMAL_X_VTEC);
                    list.add(VtecFormatMode.NORMAL_E_VTEC);
                } else if (mode == CAVEMode.TEST) {
                    list.add(VtecFormatMode.TEST_T_VTEC);
                } else {
                    list.add(VtecFormatMode.NORMAL_O_VTEC);
                    list.add(VtecFormatMode.TEST_T_VTEC);
                }

                for (VtecFormatMode vtecFormat : list) {
                    IContributionItem item = new ActionContributionItem(
                            new ChangeVtecFormatAction(vtecFormat));
                    item.fill(menu, -1);
                }
            }
        }

        // Private Variables

        /**
         * VTEC format mode.
         */
        private final VtecFormatMode vtecFormatMode;

        // Public Constructors

        /**
         * Construct an instance for holding the menu item acting as the header,
         * from which the submenu pops up listing whichever VTEC mode menu items
         * are appropriate.
         */
        public ChangeVtecFormatAction() {
            super(CHANGE_VTEC_MODE_HEADER_TEXT, null, Action.AS_DROP_DOWN_MENU,
                    null);
            vtecFormatMode = null;
            setMenuCreator(new MenuCreator());
        }

        /**
         * Construct an instance that when invoked changes the VTEC mode to that
         * specified.
         * 
         * @param mode
         *            New VTEC format mode.
         */
        public ChangeVtecFormatAction(VtecFormatMode mode) {
            super(mode.toString(), null, Action.AS_RADIO_BUTTON, null);
            vtecFormatMode = mode;
            if (vtecFormatMode == VtecFormatMode.NORMAL_O_VTEC) {
                setChecked(true);
            }
        }

        // Public Methods

        @Override
        public void run() {
            if (isChecked() && (vtecModeStateChangeHandler != null)) {
                vtecModeStateChangeHandler.stateChanged(null, vtecFormatMode);
            }
        }
    }

    /**
     * Change site action.
     */
    private class ChangeSiteAction extends BasicAction {

        // Private Classes

        /**
         * Menu creator used to create the individual menu items that allow the
         * user to change the site.
         */
        private class MenuCreator implements IMenuCreator {

            // Private Variables

            /**
             * Menu to be populated.
             */
            private Menu menu;

            /**
             * Actions within the menu.
             */
            private List<ChangeSiteAction> actions;

            // Public Methods

            @Override
            public void dispose() {
                menu.dispose();
            }

            @Override
            public Menu getMenu(Control parent) {
                return getMenu(parent.getMenu());
            }

            @Override
            public Menu getMenu(Menu parent) {
                menu = new Menu(parent);
                actions = new ArrayList<>();
                createMenuItems();
                return menu;
            }

            /**
             * Set the chosen action to be that specified.
             * 
             * @param choice
             *            Identifier of the action that should be the current
             *            choice.
             */
            public void setChosenAction(String choice) {
                if (actions != null) {
                    for (ChangeSiteAction action : actions) {
                        action.setChecked(action.site.equals(choice));
                    }
                }
            }

            // Private Methods

            /**
             * Create the menu items.
             */
            private void createMenuItems() {
                for (String site : backupSites) {
                    addAction(site);
                }
            }

            /**
             * Add an action for the specified site to the menu.
             * 
             * @param site
             *            Site to be added.
             */
            private void addAction(String site) {
                ChangeSiteAction action = new ChangeSiteAction(site);
                actions.add(action);
                IContributionItem item = new ActionContributionItem(action);
                item.fill(menu, -1);
            }
        };

        // Private Variables

        /**
         * Site.
         */
        private final String site;

        // Public Constructors

        /**
         * Construct an instance for holding the menu item acting as the header,
         * from which the submenu pops up listing the backup sites that are
         * appropriate.
         */
        public ChangeSiteAction() {
            super(CHANGE_SITE_HEADER_TEXT, null, Action.AS_DROP_DOWN_MENU, null);
            this.site = null;
            setMenuCreator(new MenuCreator());
        }

        /**
         * Construct an instance that when invoked changes the site to that
         * specified.
         * 
         * @param site
         *            New site.
         */
        public ChangeSiteAction(String site) {
            super(site, null, Action.AS_RADIO_BUTTON, null);
            this.site = site;
            if (site.equals(currentSite)) {
                setChecked(true);
            }
        }

        @Override
        public void run() {
            if (isChecked() && (siteStateChangeHandler != null)) {
                siteStateChangeHandler.stateChanged(null, site);
            }
        }

        /**
         * Set the currently chosen site to that specified.
         * 
         * @param site
         *            Currently chosen site.
         */
        public void setChoice(String site) {
            MenuCreator menuCreator = (MenuCreator) getMenuCreator();
            if (menuCreator != null) {
                menuCreator.setChosenAction(site);
            }
        }
    }

    /**
     * Timeline navigation action. Each instance is for one of the navigation
     * buttons in the toolbar.
     */
    private class NavigationAction extends BasicAction implements
            ITemporallyAware {

        // Private Variables

        /**
         * Temporal display to be manipulated by changes in this action's state.
         * Note that it is possible that this could be set to a non-
         * <code>null</code> value only after other member methods are called,
         * which is why a check for <code>null</code> is performed in those
         * cases.
         */
        private ITemporalDisplay temporalDisplay;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param iconFileName
         *            File name of the icon to be displayed.
         * @param toolTipText
         *            Tool tip text, or <code>null</code> if none is required.
         */
        public NavigationAction(String iconFileName, String toolTipText) {
            super("", iconFileName, Action.AS_PUSH_BUTTON, toolTipText);
        }

        // Public Methods

        @Override
        public void setTemporalDisplay(ITemporalDisplay temporalDisplay) {
            this.temporalDisplay = temporalDisplay;
        }

        @Override
        public void run() {
            if (getToolTipText().equals(BUTTON_ZOOM_OUT)) {
                temporalDisplay.zoomTimeOut();
            } else if (getToolTipText().equals(BUTTON_PAGE_BACKWARD)) {
                temporalDisplay.pageTimeBack();
            } else if (getToolTipText().equals(BUTTON_PAN_BACKWARD)) {
                temporalDisplay.panTimeBack();
            } else if (getToolTipText().equals(BUTTON_CURRENT_TIME)) {
                temporalDisplay.showCurrentTime();
            } else if (getToolTipText().equals(BUTTON_PAN_FORWARD)) {
                temporalDisplay.panTimeForward();
            } else if (getToolTipText().equals(BUTTON_PAGE_FORWARD)) {
                temporalDisplay.pageTimeForward();
            } else if (getToolTipText().equals(BUTTON_ZOOM_IN)) {
                temporalDisplay.zoomTimeIn();
            }
        }
    }

    /**
     * Selected time mode combo action.
     */
    private class SelectedTimeModeAction extends ComboAction implements
            ITemporallyAware {

        // Private Variables

        /**
         * Temporal display to be manipulated by changes in this action's state.
         * Note that it is possible that this could be set to a non-
         * <code>null</code> value only after other member methods are called,
         * which is why a check for <code>null</code> is performed in those
         * cases.
         */
        private ITemporalDisplay temporalDisplay;

        /**
         * Listener for menu item invocations.
         */
        private final SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {

                /*
                 * If the menu item has been selected (not deselected), then a
                 * selected time mode has been chosen.
                 */
                MenuItem item = (MenuItem) event.widget;
                if (item.getSelection()) {

                    /*
                     * Update the visuals to indicate the new time mode name.
                     */
                    setSelectedChoice(item.getText());

                    /*
                     * Remember the newly selected time mode name and fire off
                     * the action.
                     */
                    if (temporalDisplay != null) {
                        temporalDisplay.setSelectedTimeMode(SelectedTimeMode
                                .valueOf(item.getText().toUpperCase()));
                    }
                }
            }
        };

        // Public Constructors

        /**
         * Construct a standard instance.
         */
        public SelectedTimeModeAction() {
            super(SELECTED_TIME_MODE_TEXT);
        }

        // Public Methods

        @Override
        public void setTemporalDisplay(ITemporalDisplay temporalDisplay) {
            this.temporalDisplay = temporalDisplay;
        }

        // Protected Methods

        @Override
        protected Menu doGetMenu(Control parent, Menu menu) {

            /*
             * If the menu has not yet been created, do so now; otherwise, just
             * update it to ensure the right menu item is selected.
             */
            if (menu == null) {
                menu = new Menu(parent);
                for (int j = 0; j < SelectedTimeMode.values().length; j++) {
                    MenuItem item = new MenuItem(menu, SWT.RADIO, j);
                    item.setText(SelectedTimeMode.values()[j].getName());
                    item.addSelectionListener(listener);
                    if ((temporalDisplay != null)
                            && SelectedTimeMode.values()[j].getName().equals(
                                    temporalDisplay.getSelectedTimeMode()
                                            .getName())) {
                        item.setSelection(true);
                    }
                }
            } else {
                String selectedTimeMode = (temporalDisplay != null ? temporalDisplay
                        .getSelectedTimeMode().getName() : null);
                for (MenuItem item : menu.getItems()) {
                    item.setSelection(item.getText().equals(selectedTimeMode));
                }
            }

            return menu;
        }
    }

    // Private Variables

    /**
     * Console presenter.
     * 
     * @deprecated Should no longer be needed once decoupling from presenter is
     *             complete.
     */
    @Deprecated
    private ConsolePresenter presenter;

    /**
     * Command invocation handler.
     */
    private ICommandInvocationHandler<Command> commandInvocationHandler;

    /**
     * Review and correct products invocation handler.
     */
    private ICommandInvocationHandler<List<ProductData>> reviewAndCorrectProductsInvocationHandler;

    /**
     * Toggle state change handler.
     */
    private IStateChangeHandler<Toggle, Boolean> toggleStateChangeHandler;

    /**
     * VTEC mode state change handler.
     */
    private IStateChangeHandler<String, VtecFormatMode> vtecModeStateChangeHandler;

    /**
     * Site state change handler.
     */
    private IStateChangeHandler<String, String> siteStateChangeHandler;

    /**
     * Flag indicating whether or not the temporal controls should be in the
     * toolbar.
     */
    private boolean temporalControlsInToolBar;

    /**
     * Current site.
     */
    private String currentSite;

    /**
     * Backup sites, used to populate the change site menu.
     */
    private ImmutableList<String> backupSites;

    /**
     * Change site action.
     */
    private ChangeSiteAction changeSiteAction;

    /**
     * Selected time mode combo action.
     */
    private SelectedTimeModeAction selectedTimeModeAction;

    /**
     * Map of button identifiers to the associated toolbar navigation actions.
     * These are constructed if necessary as contributions to the main user
     * interface and then passed to the view part when it comes into existence.
     */
    private final Map<String, Action> actionsForButtonIdentifiers = new HashMap<>();

    /**
     * Sort invoker delegate. The identifier is the sort to be performed.
     */
    private final ICommandInvoker<Sort> sortInvokerDelegate = new CommandInvokerDelegate<>(
            new ViewPartWidgetDelegateHelper<>(
                    new Callable<ICommandInvoker<Sort>>() {

                        @Override
                        public ICommandInvoker<Sort> call() throws Exception {
                            return getViewPart().getSortInvoker();
                        }
                    }, this), RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Site state changer. The identifier is ignored.
     */
    private final IStateChanger<String, String> siteChanger = new IStateChanger<String, String>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            throw new UnsupportedOperationException(
                    "cannot change enabled state of site changer");
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            throw new UnsupportedOperationException(
                    "cannot change enabled state of site changer");
        }

        @Override
        public String getState(String identifier) {
            return currentSite;
        }

        @Override
        public void setState(String identifier, final String value) {
            currentSite = value;
            changeSiteAction.setChoice(value);
            executeOnCreatedViewPart(new Runnable() {
                @Override
                public void run() {
                    getViewPart().siteChanged(value);
                }
            });
        }

        @Override
        public void setStates(Map<String, String> valuesForIdentifiers) {
            throw new UnsupportedOperationException(
                    "cannot change multiple states for site changer");
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, String> handler) {
            siteStateChangeHandler = handler;
        }
    };

    /**
     * Site state changer delegate.
     */
    private final IStateChanger<String, String> siteChangerDelegate = new StateChangerDelegate<>(
            new BasicWidgetDelegateHelper<>(siteChanger),
            RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Time range state changer delegate.
     */
    private final IStateChanger<TimeRangeType, Range<Long>> timeRangeChangerDelegate = new StateChangerDelegate<>(
            new ViewPartWidgetDelegateHelper<>(
                    new Callable<IStateChanger<TimeRangeType, Range<Long>>>() {

                        @Override
                        public IStateChanger<TimeRangeType, Range<Long>> call()
                                throws Exception {
                            return getViewPart().getTimeRangeChanger();
                        }
                    }, this), RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Columns state changer delegate.
     */
    private final IStateChanger<String, ConsoleColumns> columnsChangerDelegate = new StateChangerDelegate<>(
            new ViewPartWidgetDelegateHelper<>(
                    new Callable<IStateChanger<String, ConsoleColumns>>() {

                        @Override
                        public IStateChanger<String, ConsoleColumns> call()
                                throws Exception {
                            return getViewPart().getColumnsChanger();
                        }
                    }, this), RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Column-based filters state changer delegate.
     */
    private final IStateChanger<String, Object> columnFiltersChangerDelegate = new StateChangerDelegate<>(
            new ViewPartWidgetDelegateHelper<>(
                    new Callable<IStateChanger<String, Object>>() {

                        @Override
                        public IStateChanger<String, Object> call()
                                throws Exception {
                            return getViewPart().getColumnFiltersChanger();
                        }
                    }, this), RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Tree contents state changer delegate.
     */
    private final IListStateChanger<String, TabularEntity> treeContentsChangerDelegate = new ListStateChangerDelegate<>(
            new ViewPartWidgetDelegateHelper<>(
                    new Callable<IListStateChanger<String, TabularEntity>>() {

                        @Override
                        public IListStateChanger<String, TabularEntity> call()
                                throws Exception {
                            return getViewPart().getTreeContentsChanger();
                        }
                    }, this), RUNNABLE_ASYNC_SCHEDULER);

    /**
     * View part listener.
     */
    private final IPartListener2 partListener = new PartAdapter2() {

        @Override
        public void partClosed(IWorkbenchPartReference partRef) {
            if (partRef == getViewPartReference()) {
                if (commandInvocationHandler != null) {
                    commandInvocationHandler.commandInvoked(Command.CLOSE);
                }
            }
        }

        @Override
        public void partVisible(IWorkbenchPartReference partRef) {
            if (partRef == getViewPartReference()) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        detachIfForcedDetachIsRequired();
                    }
                });
            }
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param loadedFromBundle
     *            Flag indicating whether or not the view is being instantiated
     *            as a result of a bundle load.
     */
    public ConsoleView(final boolean loadedFromBundle) {
        super(ConsoleViewPart.ID, ConsoleViewPart.class);

        /*
         * Show the view part.
         */
        showViewPart();

        /*
         * Execute further manipulation of the view part immediately, or delay
         * such execution until the view part is created if it has not yet been
         * created yet.
         */
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {

                /*
                 * If this view is being created as the result of a bundle load,
                 * determine whether the view part should be started off as
                 * hidden, and if so, hide it.
                 */
                boolean potentialDetach = true;
                if (loadedFromBundle) {
                    potentialDetach = false;
                    if (getViewPart().isDocked() == false) {
                        WorkbenchPage page = (WorkbenchPage) getActiveWorkbenchPage(true);
                        IPreferenceStore preferenceStore = HazardServicesActivator
                                .getDefault().getPreferenceStore();
                        preferenceStore
                                .setValue(
                                        page.getPerspective().getId()
                                                + FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX,
                                        true);
                        Rectangle bounds = getViewPart().getShell().getBounds();
                        preferenceStore.setValue(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_X_SUFFIX, bounds.x);
                        preferenceStore.setValue(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_Y_SUFFIX, bounds.y);
                        preferenceStore.setValue(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_WIDTH_SUFFIX,
                                bounds.width);
                        preferenceStore.setValue(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_HEIGHT_SUFFIX,
                                bounds.height);
                        page.attachView(page
                                .findViewReference(ConsoleViewPart.ID));
                    }
                    setViewPartVisible(false);
                }

                /*
                 * If this view needs to be forcibly detached, detach it and use
                 * the previously saved bounds as its shell's boundaries.
                 */
                if (potentialDetach) {
                    detachIfForcedDetachIsRequired();
                }
            }
        });

        /*
         * Register the part listener for view part events so that the closing
         * of the console view part may be responded to.
         */
        setPartListener(partListener);
    }

    // Public Methods

    @Override
    public final void initialize(final ConsolePresenter presenter,
            final Date selectedTime, final Date currentTime,
            final long visibleTimeRange, final TimeResolution timeResolution,
            final ImmutableList<Map<String, Object>> filterSpecifiers,
            final String currentSite, final ImmutableList<String> backupSites,
            final boolean temporalControlsInToolBar) {
        this.presenter = presenter;
        this.temporalControlsInToolBar = temporalControlsInToolBar;
        this.currentSite = currentSite;
        List<String> sortedBackupSites = (backupSites == null ? new ArrayList<String>()
                : new ArrayList<>(backupSites));
        Collections.sort(sortedBackupSites);
        this.backupSites = ImmutableList.copyOf(sortedBackupSites);
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().initialize(ConsoleView.this, selectedTime,
                        currentTime, visibleTimeRange, timeResolution,
                        filterSpecifiers, currentSite,
                        temporalControlsInToolBar);
            }
        });
    }

    @Override
    public final void acceptContributionsToMainUI(
            List<? extends IMainUiContributor<Action, RCPMainUserInterfaceElement>> contributors,
            final RCPMainUserInterfaceElement type) {

        /*
         * Iterate through the contributors, asking each in turn for its
         * contributions and adding them to the list of total contributions.
         * When at least one contribution is made and the last contribution
         * specified is not a separator, a separator is placed after the
         * contributions to render them visually distinct from what comes next.
         */
        final List<Action> totalContributions = new ArrayList<>();
        for (IMainUiContributor<Action, RCPMainUserInterfaceElement> contributor : contributors) {
            List<? extends Action> contributions = contributor
                    .contributeToMainUI(type);
            totalContributions.addAll(contributions);
            if ((contributions.size() > 0)
                    && ((contributions.get(contributions.size() - 1) instanceof SeparatorAction) == false)) {
                totalContributions.add(new SeparatorAction());
            }
        }

        /*
         * Do the rest only when there is a view part ready to take the
         * contributions.
         */
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {

                /*
                 * Remove all toolbar or menubar items first, since there may be
                 * ones left over from a previous invocation of this method.
                 */
                IActionBars actionBars = getViewPart()
                        .getMainActionBarsManager();
                IContributionManager contributionManager = (type
                        .equals(RCPMainUserInterfaceElement.TOOLBAR) ? actionBars
                        .getToolBarManager() : actionBars.getMenuManager());
                contributionManager.removeAll();

                /*
                 * Iterate through the list of total contributions, passing each
                 * in turn to the manager.
                 */
                for (Action contribution : totalContributions) {
                    if (contribution instanceof SeparatorAction) {
                        contributionManager.add(new Separator());
                    } else {
                        contributionManager.add(contribution);
                        if (contribution instanceof IContributionManagerAware) {
                            ((IContributionManagerAware) contribution)
                                    .setContributionManager(contributionManager);
                        }
                    }
                }

                /*
                 * Update the contribution manager in order to work around what
                 * appears to be an Eclipse bug. The latter manifests itself by
                 * not drawing the last action added to the toolbar. This update
                 * seems to force the toolbar to render itself and all its
                 * actions properly.
                 */
                contributionManager.update(true);
            }
        });
    }

    @Override
    public final List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        if (type == RCPMainUserInterfaceElement.TOOLBAR) {
            if (temporalControlsInToolBar) {

                /*
                 * Create the selected time mode action.
                 */
                List<Action> list = new ArrayList<>();
                list.add(new SeparatorAction());
                selectedTimeModeAction = new SelectedTimeModeAction();
                list.add(selectedTimeModeAction);

                /*
                 * Create the navigation actions for the toolbar.
                 */
                list.add(new SeparatorAction());
                for (int j = 0; j < TOOLBAR_BUTTON_IMAGE_FILE_NAMES.size(); j++) {
                    Action action = new NavigationAction(
                            TOOLBAR_BUTTON_IMAGE_FILE_NAMES.get(j),
                            BUTTON_DESCRIPTIONS.get(j));
                    actionsForButtonIdentifiers.put(BUTTON_IDENTIFIERS.get(j),
                            action);
                    list.add(action);
                }

                /*
                 * Pass these to the view part when it is ready.
                 */
                executeOnCreatedViewPart(new Runnable() {
                    @Override
                    public void run() {
                        getViewPart().setToolBarActions(
                                actionsForButtonIdentifiers,
                                selectedTimeModeAction);
                    }
                });
                return list;
            }
            return Collections.emptyList();
        } else {
            CommandConsoleAction resetEventsCommandAction = new CommandConsoleAction(
                    RESET_EVENTS_COMMAND_MENU_TEXT, null, null, Command.RESET);

            SeparatorAction sep = new SeparatorAction();

            CommandConsoleAction exportHazardConfigAction = new CommandConsoleAction(
                    EXPORT_HAZARD_SITE_MENU_TEXT, null, null,
                    Command.EXPORT_SITE_CONFIG);

            CommandConsoleAction checkHazardConflictsAction = new CommandConsoleAction(
                    CHECK_HAZARD_CONFLICTS_MENU_TEXT, null, null,
                    Command.CHECK_FOR_CONFLICTS);

            ToggleConsoleAction autoCheckHazardConflictsAction = new ToggleConsoleAction(
                    AUTO_CHECK_HAZARD_CONFLICTS_MENU_TEXT, null, null,
                    Toggle.AUTO_CHECK_FOR_CONFLICTS);

            ToggleConsoleAction showHatchedAreaAction = new ToggleConsoleAction(
                    SHOW_HATCHED_AREAS_MENU_TEXT, null, null,
                    Toggle.SHOW_HATCHED_AREAS);
            showHatchedAreaAction.setChecked(true);

            ToggleConsoleAction showHistoryListsAction = new ToggleConsoleAction(
                    SHOW_HISTORY_LISTS_MENU_TEXT, null, null,
                    Toggle.SHOW_HISTORY_LISTS);

            Action reviewAndCorrectProductsAction = new ReviewAndCorrectProductsAction();
            CommandConsoleAction viewProductAction = new CommandConsoleAction(
                    VIEW_PRODUCT_MENU_TEXT, null, null, Command.VIEW_PRODUCT);

            List<Action> actions = Lists.newArrayList(resetEventsCommandAction,
                    sep, exportHazardConfigAction, sep,
                    checkHazardConflictsAction, autoCheckHazardConflictsAction,
                    showHatchedAreaAction, showHistoryListsAction, sep,
                    reviewAndCorrectProductsAction, viewProductAction);

            if (CAVEMode.PRACTICE.equals(CAVEMode.getMode())) {
                ChangeVtecFormatAction changeVtecFormat = new ChangeVtecFormatAction();
                actions.add(changeVtecFormat);
                actions.add(sep);
            }

            changeSiteAction = new ChangeSiteAction();
            actions.add(changeSiteAction);

            return actions;
        }
    }

    @Override
    public void dispose() {

        /*
         * Remove all toolbar and menubar items.
         */
        executeOnCreatedViewPart(new Runnable() {

            @Override
            public void run() {
                IActionBars actionBars = getViewPart()
                        .getMainActionBarsManager();
                actionBars.getToolBarManager().removeAll();
                actionBars.getMenuManager().removeAll();
            }
        });
        super.dispose();
    }

    @Override
    public final void ensureVisible() {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                setViewPartVisible(true);
            }
        });
    }

    @Override
    public final void setCurrentTime(final Date currentTime) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().setCurrentTime(currentTime);
            }
        });
    }

    @Override
    public void setSorts(final ImmutableList<Sort> sorts) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().setSorts(sorts);
            }
        });
    }

    @Override
    public void setActiveCountdownTimers(
            final ImmutableMap<String, CountdownTimer> countdownTimersForEventIdentifiers) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {

                /*
                 * Check to ensure the view part is still around, since the
                 * enclosing method is sometimes called when the view part has
                 * been deleted and the application is closing.
                 */
                ConsoleViewPart viewPart = getViewPart();
                if (viewPart != null) {
                    viewPart.setActiveCountdownTimers(countdownTimersForEventIdentifiers);
                }
            }
        });
    }

    @Override
    public void setTimeResolution(final TimeResolution timeResolution,
            final Date currentTime) {
        executeOnCreatedViewPart(new Runnable() {

            @Override
            public void run() {
                getViewPart().setTimeResolution(timeResolution, currentTime);
            }
        });
    }

    @Override
    public final void setSettingsName(final String settingsName) {
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                getViewPart().setSettingsName(settingsName);
            }
        });
    }

    @Override
    public ICommandInvoker<Sort> getSortInvoker() {
        return sortInvokerDelegate;
    }

    @Override
    public IStateChanger<TimeRangeType, Range<Long>> getTimeRangeChanger() {
        return timeRangeChangerDelegate;
    }

    @Override
    public IStateChanger<String, ConsoleColumns> getColumnsChanger() {
        return columnsChangerDelegate;
    }

    @Override
    public IStateChanger<String, Object> getColumnFiltersChanger() {
        return columnFiltersChangerDelegate;
    }

    @Override
    public IListStateChanger<String, TabularEntity> getTreeContentsChanger() {
        return treeContentsChangerDelegate;
    }

    @Override
    public void setCommandInvocationHandler(
            ICommandInvocationHandler<Command> commandInvocationHandler) {
        this.commandInvocationHandler = commandInvocationHandler;
    }

    @Override
    public void setReviewAndCorrectProductsInvocationHandler(
            ICommandInvocationHandler<List<ProductData>> reviewAndCorrectProductsInvocationHandler) {
        this.reviewAndCorrectProductsInvocationHandler = reviewAndCorrectProductsInvocationHandler;
    }

    @Override
    public void setToggleChangeHandler(
            IStateChangeHandler<Toggle, Boolean> toggleStateChangeHandler) {
        this.toggleStateChangeHandler = toggleStateChangeHandler;
    }

    @Override
    public void setVtecModeChangeHandler(
            IStateChangeHandler<String, VtecFormatMode> vtecModeStateChangeHandler) {
        this.vtecModeStateChangeHandler = vtecModeStateChangeHandler;
    }

    @Override
    public IStateChanger<String, String> getSiteChanger() {
        return siteChangerDelegate;
    }

    // Protected Methods

    /**
     * Respond to an attempt to execute some action via
     * {@link #executeOnCreatedViewPart(Runnable)} upon a view part when the
     * view part is not in existence and no attempt has been made to create it.
     * This should never occur, so an exception is thrown.
     * 
     * @param job
     *            Action for which execution was attempted.
     * @throws IllegalStateException
     *             Whenever this method is invoked.
     */
    @Override
    protected void actionExecutionAttemptedUponNonexistentViewPart(Runnable job) {
        throw new IllegalStateException(
                "view part creation not attempted before invocation of action: "
                        + job);
    }

    // Package-Private Methods

    /**
     * Get the context menu items appropriate to the specified event.
     * 
     * @param identifier
     *            Identifier of the tabular entity that was chosen with the
     *            context menu invocation, or <code>null</code> if none was
     *            chosen.
     * @param persistedTimestamp
     *            Timestamp indicating when the entity was persisted; may be
     *            <code>null</code>.
     * @return Actions for the menu items to be shown.
     * @deprecated See
     *             {@link ConsolePresenter#getContextMenuItems(String, Date, IRunnableAsynchronousScheduler)}
     *             .
     */
    @Deprecated
    List<IContributionItem> getContextMenuItems(String identifier,
            Date persistedTimestamp) {
        return presenter.getContextMenuItems(identifier, persistedTimestamp,
                RUNNABLE_ASYNC_SCHEDULER);
    }

    // Private Methods

    /**
     * Detach the view part if a forced detach is required.
     */
    private void detachIfForcedDetachIsRequired() {
        WorkbenchPage page = (WorkbenchPage) getActiveWorkbenchPage(true);
        IPreferenceStore preferenceStore = HazardServicesActivator.getDefault()
                .getPreferenceStore();
        if (preferenceStore.getBoolean(page.getPerspective().getId()
                + FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX)) {
            preferenceStore.setValue(page.getPerspective().getId()
                    + FORCE_DETACH_CONSOLE_WHEN_NEXT_SHOWING_SUFFIX, false);
            if (getViewPart().isDocked()) {
                page.detachView(page.findViewReference(ConsoleViewPart.ID));
                final Rectangle bounds = new Rectangle(
                        preferenceStore.getInt(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_X_SUFFIX),
                        preferenceStore.getInt(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_Y_SUFFIX),
                        preferenceStore.getInt(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_WIDTH_SUFFIX),
                        preferenceStore.getInt(page.getPerspective().getId()
                                + LAST_DETACHED_BOUNDS_HEIGHT_SUFFIX));

                /*
                 * Set the bounds, but then schedule the setting of the location
                 * to occur later, since immediate execution seems to be
                 * ignored; instead, the location (via either setBounds() or
                 * setLocation()) is simply set to be the center of the display
                 * if the asyncExec() is not used.
                 */
                getViewPart().getShell().setBounds(bounds);
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        getViewPart().getShell()
                                .setLocation(bounds.x, bounds.y);
                    }
                });
            }
        }
    }
}
