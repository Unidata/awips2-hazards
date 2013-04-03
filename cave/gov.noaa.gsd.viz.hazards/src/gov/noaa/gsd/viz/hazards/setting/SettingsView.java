/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.setting;

import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.display.action.SettingsAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.toolbar.PulldownAction;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;
import gov.noaa.gsd.viz.megawidgets.HierarchicalChoicesMegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Settings view, an implementation of ISettingsView that provides an SWT-based
 * view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SettingsView implements
        ISettingsView<IActionBars, RCPMainUserInterfaceElement> {

    // Private Static Constants

    /**
     * Key into the settings dictionaries that yields the displayable name for
     * each setting.
     */
    private static final String DISPLAY_NAME = "displayName";

    /**
     * Array of settings dropdown menu items.
     */
    private static final String[] SETTINGS_DROPDOWN_MENU_ITEMS = { "&Open...",
            "&Save", "Save &As...", "&Delete...", "&Edit..." };

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SettingsView.class);

    // Private Classes

    /**
     * Settings pulldown menu action.
     */
    private class SettingsPulldownAction extends PulldownAction {

        // Private Variables

        /**
         * Flag indicating whether or not the menu should be repopulated with
         * setting names.
         */
        private boolean settingsChanged = true;

        /**
         * Listener for menu item invocations.
         */
        private final SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (((MenuItem) event.widget).getText().equals("&Edit...")) {
                    presenter.showSettingDetail();
                } else if (event.widget.getData() != null) {

                    // Remember the newly selected setting name and fire off
                    // the action.
                    fireAction("SettingChosen", (String) event.widget.getData());
                }
            }
        };

        // Public Constructors

        /**
         * Construct a standard instance.
         */
        public SettingsPulldownAction() {
            super("Settings");
        }

        // Public Methods

        /**
         * Receive notification that the settings have changed.
         */
        public void settingsChanged() {
            settingsChanged = true;
        }

        // Protected Methods

        /**
         * Get the menu for the specified parent, possibly reusing the specified
         * menu if provided.
         * 
         * @param parent
         *            Parent control.
         * @param menu
         *            Menu that was created previously, if any; this may be
         *            reused, or disposed of completely.
         * @return Menu.
         */
        @Override
        public Menu doGetMenu(Control parent, Menu menu) {

            // If the menu has not yet been created, do so now.
            if (menu == null) {
                menu = new Menu(parent);

                // Iterate through the command items, if any,
                // creating a menu item for each.
                for (String text : SETTINGS_DROPDOWN_MENU_ITEMS) {
                    MenuItem item = new MenuItem(menu, SWT.PUSH);
                    item.setText(text);
                    item.addSelectionListener(listener);
                }

                // Add a separator.
                new MenuItem(menu, SWT.SEPARATOR);
            }

            // If settings have changed, delete the existing settings
            // (if any) and recreate according to the new list. FOR
            // NOW, JUST POPULATE WITH THE FULL SETTINGS LIST INSTEAD
            // OF MOST RECENTLY USED.
            if (settingsChanged) {
                for (MenuItem item : menu.getItems()) {
                    if (item.getData() != null) {
                        item.dispose();
                    }
                }
                for (int j = 0; j < settingNames.size(); j++) {
                    MenuItem item = new MenuItem(menu, SWT.PUSH);
                    item.setText(settingNames.get(j));
                    item.setData(settingIdentifiersForNames.get(settingNames
                            .get(j)));
                    item.addSelectionListener(listener);
                }
                settingsChanged = false;
            }

            // Return the menu.
            return menu;
        }
    }

    /**
     * Filters pulldown menu action.
     */
    private class FiltersPulldownAction extends PulldownAction {

        // Private Variables

        /**
         * Megawidget manager handling the menu megawidgets.
         */
        private MegawidgetManager megawidgetManager = null;

        /**
         * Flag indicating whether or not the filters may have changed.
         */
        private boolean filtersChanged = true;

        // Public Constructors

        /**
         * Construct a standard instance.
         */
        public FiltersPulldownAction() {
            super("Filters");
        }

        // Public Methods

        /**
         * Dispose of the action.
         */
        @Override
        public void dispose() {
            super.dispose();
            megawidgetManager = null;
        }

        /**
         * Receive notification that the filters may have changed.
         */
        public void filtersChanged() {
            filtersChanged = true;
        }

        // Protected Methods

        /**
         * Get the menu for the specified parent, possibly reusing the specified
         * menu if provided.
         * 
         * @param parent
         *            Parent control.
         * @param menu
         *            Menu that was created previously, if any; this may be
         *            reused, or disposed of completely.
         * @return Menu.
         */
        @Override
        public Menu doGetMenu(Control parent, Menu menu) {

            // If the menu has not yet been created, create it now.
            if (menu == null) {
                menu = new Menu(parent);
                List<Dict> fieldsList = new ArrayList<Dict>();
                for (Object field : filterMegawidgetSpecifiers) {
                    fieldsList.add((Dict) field);
                }
                try {
                    megawidgetManager = new MegawidgetManager(menu, fieldsList,
                            dynamicSetting) {
                        @Override
                        protected void commandInvoked(String identifier,
                                String extraCallback) {

                            // No action.
                        }

                        @Override
                        protected void stateElementChanged(String identifier,
                                Object state) {

                            // Special case: A translation has to be made
                            // between the hazard categories and types tree
                            // structure that the user is creating as state,
                            // and the old hazard categories list and hazard
                            // types list. This should be removed if we can get
                            // rid of the visibleTypes and hidHazardCategories
                            // lists in the dynamic setting.
                            translateHazardCategoriesAndTypesToOldLists(dynamicSetting);

                            // Forward the dynamic setting change to the
                            // presenter.
                            try {
                                presenter.fireAction(new SettingsAction(
                                        "DynamicSettingChanged", dynamicSetting
                                                .toJSONString()));
                            } catch (Exception e) {
                                statusHandler
                                        .error("SettingsView.FiltersPulldownAction.MegawidgetManager."
                                                + "stateElementChanged(): Error: Could not serialize JSON.",
                                                e);
                            }
                        }
                    };
                } catch (MegawidgetException e) {
                    statusHandler
                            .error("SettingsView.FiltersPulldownAction.doGetMenu(): Unable to create "
                                    + "megawidget manager due to megawidget construction problem.",
                                    e);
                }
            }

            // If the filters may have changed, update the megawidget
            // states.
            if (filtersChanged) {
                try {
                    megawidgetManager.setState(dynamicSetting);
                } catch (MegawidgetStateException e) {
                    statusHandler.error(
                            "SettingsView.FiltersPulldownAction.doGetMenu(): Failed to "
                                    + "accept new dynamic setting parameters.",
                            e);
                }
                filtersChanged = false;
            }

            // Return the menu.
            return menu;
        }
    }

    // Private Variables

    /**
     * Presenter.
     */
    private SettingsPresenter presenter = null;

    /**
     * List of setting names.
     */
    private final List<String> settingNames = new ArrayList<String>();

    /**
     * Map of setting names to their associated identifiers.
     */
    private final Map<String, String> settingIdentifiersForNames = new HashMap<String, String>();

    /**
     * List of filter menu-based megawidget specifiers.
     */
    private DictList filterMegawidgetSpecifiers = null;

    /**
     * Dynamic setting.
     */
    private Dict dynamicSetting = null;

    /**
     * Settings pulldown action.
     */
    private SettingsPulldownAction settingsPulldownAction = null;

    /**
     * Filters pulldown action.
     */
    private FiltersPulldownAction filtersPulldownAction = null;

    /**
     * Setting dialog.
     */
    private SettingDialog settingDialog = null;

    /**
     * Dialog dispose listener.
     */
    private final DisposeListener dialogDisposeListener = new DisposeListener() {
        @Override
        public void widgetDisposed(DisposeEvent e) {
            settingDialog = null;
        }
    };

    // Public Static Methods

    /**
     * Find the hazard categories and types hierarchical choices state within
     * the specified map, and translate its categories and types into separate
     * lists, placing those lists back in the map. This should be removed if we
     * can get rid of the visibleTypes and hidHazardCategories lists in the
     * dynamic setting.
     * 
     * @param map
     *            Map in which the translation is to occur.
     */
    public static void translateHazardCategoriesAndTypesToOldLists(Dict map) {
        List<Object> treeState = map
                .getDynamicallyTypedValue(Utilities.SETTING_HAZARD_CATEGORIES_AND_TYPES);
        List<String> categories = new ArrayList<String>();
        Set<String> typesSet = new HashSet<String>();
        for (int j = 0; j < treeState.size(); j++) {
            Map<?, ?> category = (Map<?, ?>) treeState.get(j);
            categories.add((String) category
                    .get(HierarchicalChoicesMegawidgetSpecifier.CHOICE_NAME));
            List<?> children = (List<?>) category
                    .get(HierarchicalChoicesMegawidgetSpecifier.CHOICE_CHILDREN);
            for (Object child : children) {
                if (child instanceof Map) {
                    typesSet.add((String) ((Map<?, ?>) child)
                            .get(HierarchicalChoicesMegawidgetSpecifier.CHOICE_IDENTIFIER));
                } else {
                    typesSet.add((String) child);
                }
            }
        }
        List<String> types = new ArrayList<String>();
        for (String type : typesSet) {
            types.add(type);
        }
        map.put(Utilities.SETTING_HAZARD_CATEGORIES, categories);
        map.put(Utilities.SETTING_HAZARD_TYPES, types);
    }

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public SettingsView() {

        // No action.
    }

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param presenter
     *            Presenter managing this view.
     * @param jsonSettings
     *            JSON string providing a dictionary of settings.
     * @param jsonFilters
     *            JSON string providing a list of dictionaries, each specifying
     *            a filter megawidget.
     * @param jsonDynamicSetting
     *            JSON string providing a dictionary defining the dynamic
     *            setting.
     */
    @Override
    public final void initialize(SettingsPresenter presenter,
            String jsonSettings, String jsonFilters, String jsonDynamicSetting) {
        this.presenter = presenter;
        setSettings(jsonSettings);
        setFilterMegawidgets(jsonFilters);
        setDynamicSetting(jsonDynamicSetting);
    }

    /**
     * Prepare for disposal.
     */
    @Override
    public final void dispose() {
        if (settingDialog != null) {
            settingDialog.close();
            settingDialog = null;
        }
    }

    /**
     * Contribute to the main UI, if desired. Note that this method may be
     * called multiple times per <code>type</code> to (re)populate the main UI
     * with the specified <code>type</code>; implementations are responsible for
     * cleaning up after contributed items that may exist from a previous call
     * with the same <code>type</code>.
     * 
     * @param mainUI
     *            Main user interface to which to contribute.
     * @param type
     *            Type of contribution to be made to the main user interface.
     * @return True if items were contributed, otherwise false.
     */
    @Override
    public final boolean contributeToMainUI(IActionBars mainUI,
            RCPMainUserInterfaceElement type) {
        if (type == RCPMainUserInterfaceElement.TOOLBAR) {

            // Create the actions.
            settingsPulldownAction = new SettingsPulldownAction();
            filtersPulldownAction = new FiltersPulldownAction();

            // Add the actions to the toolbar.
            IToolBarManager toolBarManager = mainUI.getToolBarManager();
            toolBarManager.add(settingsPulldownAction);
            toolBarManager.add(filtersPulldownAction);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Show the settings detail subview.
     * 
     * @param fields
     *            List of dictionaries, each providing a field to be displayed
     *            in the subview.
     * @param values
     *            Dictionary pairing keys found as the field names in
     *            <code>fields</code> with their values.
     */
    @Override
    public final void showSettingDetail(DictList fields, Dict values) {
        settingDialog = new SettingDialog(presenter, PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(), fields, values);
        settingDialog.open();
        settingDialog.getShell().addDisposeListener(dialogDisposeListener);
    }

    /**
     * Set the settings to those specified.
     * 
     * @param jsonSettings
     *            JSON string holding a dictionary an entry for the list of
     *            settings, and another entry for the current setting
     *            identifier.
     */
    @Override
    public final void setSettings(String jsonSettings) {

        // Get the dictionary from the JSON, and the list of settings
        // from that.
        Dict settingDict = Dict.getInstance(jsonSettings);
        ArrayList<Dict> settings = settingDict
                .getDynamicallyTypedValue(Utilities.SETTINGS_LIST);
        if ((settings == null) || (settings.size() < 1)) {
            return;
        }

        // Get the names and identifiers of the settings.
        settingNames.clear();
        settingIdentifiersForNames.clear();
        for (int j = 0; j < settings.size(); j++) {
            Dict setting = settings.get(j);
            String name = setting.getDynamicallyTypedValue(DISPLAY_NAME);
            settingNames.add(name);
            String identifier = setting
                    .getDynamicallyTypedValue(Utilities.SETTINGS_LIST_IDENTIFIER);
            settingIdentifiersForNames.put(name, identifier);
        }

        // Notify the settings pulldown that the settings have
        // changed.
        if (settingsPulldownAction != null) {
            settingsPulldownAction.settingsChanged();
        }
    }

    /**
     * Set the dynamic setting to that specified.
     * 
     * @param jsonSetting
     *            JSON string holding a dictionary defining the dynamic setting
     *            to be used.
     */
    @Override
    public final void setDynamicSetting(String jsonSetting) {
        dynamicSetting = Dict.getInstance(jsonSetting);
        if (filtersPulldownAction != null) {
            filtersPulldownAction.filtersChanged();
        }
        if (settingDialog != null) {
            settingDialog.setState(dynamicSetting);
        }
    }

    // Private Methods

    /**
     * Set the filter megawidgets to those specified.
     * 
     * @param jsonFilters
     *            JSON string providing a list of dictionaries, each specifying
     *            a filter megawidget.
     */
    private void setFilterMegawidgets(String jsonFilters) {
        filterMegawidgetSpecifiers = DictList.getInstance(jsonFilters);
    }

    /**
     * Fire an action event to its listener.
     * 
     * @param action
     *            Action.
     * @param detail
     *            Detail.
     */
    private void fireAction(String action, String detail) {
        presenter.fireAction(new SettingsAction(action, detail));
    }
}
