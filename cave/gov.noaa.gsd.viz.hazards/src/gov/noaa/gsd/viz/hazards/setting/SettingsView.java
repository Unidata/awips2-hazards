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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.*;
import gov.noaa.gsd.common.utilities.JSONConverter;
import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.display.action.CurrentSettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.StaticSettingsAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.toolbar.PulldownAction;
import gov.noaa.gsd.viz.megawidgets.HierarchicalBoundedChoicesMegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;

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
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle.
 * Nov 04, 2013    2336    Chris.Golden      Changed to work with new megawidget
 *                                           class names.
 * Nov 29, 2013    2380    daniel.s.schaffer@noaa.gov Minor cleanup
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SettingsView implements
        ISettingsView<Action, RCPMainUserInterfaceElement> {

    // Private Static Constants

    /**
     * Edit menu item text.
     */
    private static final String EDIT_COMMAND_MENU_TEXT = "&Edit...";

    /**
     * Array of settings dropdown menu items.
     */
    private static final List<String> SETTINGS_DROPDOWN_MENU_ITEMS = ImmutableList
            .of("&Open...", "&Save", "Save &As...", "&Delete...",
                    EDIT_COMMAND_MENU_TEXT);

    /**
     * Settings toolbar menu button text.
     */
    private static final String SETTINGS_TOOLBAR_BUTTON_TEXT = "Settings";

    /**
     * Filters toolbar menu button text.
     */
    private static final String FILTERS_TOOLBAR_BUTTON_TEXT = "Filters";

    private final JSONConverter jsonConverter = new JSONConverter();

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
                if (((MenuItem) event.widget).getText().equals(
                        EDIT_COMMAND_MENU_TEXT)) {
                    presenter.showSettingDetail();
                } else if (event.widget.getData() != null) {

                    // Remember the newly selected setting name and fire off
                    // the action.
                    String settingsID = (String) event.widget.getData();
                    presenter.fireAction(new StaticSettingsAction(
                            StaticSettingsAction.ActionType.SETTINGS_CHOSEN,
                            settingsID));
                } else {
                    Shell shell = PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow().getShell();
                    MessageDialog.openInformation(shell, null,
                            "This feature is not yet implemented.");
                }
            }
        };

        // Public Constructors

        /**
         * Construct a standard instance.
         */
        public SettingsPulldownAction() {
            super(SETTINGS_TOOLBAR_BUTTON_TEXT);
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
            super(FILTERS_TOOLBAR_BUTTON_TEXT);
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
                List<Dict> fieldsList = Lists.newArrayList();
                for (Object field : filterMegawidgetSpecifiers) {
                    fieldsList.add((Dict) field);
                }
                try {
                    megawidgetManager = new MegawidgetManager(menu, fieldsList,
                            settingsAsDict()) {
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
                            // lists in the current setting.
                            Dict settingsAsDict = (Dict) getState();
                            translateHazardCategoriesAndTypesToOldLists(settingsAsDict);
                            Settings updatedSettings = jsonConverter.fromJson(
                                    settingsAsDict.toJSONString(),
                                    Settings.class);
                            SettingsView.this.currentSettings
                                    .apply(updatedSettings);

                            // Forward the current setting change to the
                            // presenter.
                            try {
                                presenter.fireAction(new CurrentSettingsAction(
                                        currentSettings));
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
                    megawidgetManager.setState(settingsAsDict());
                } catch (MegawidgetStateException e) {
                    statusHandler.error(
                            "SettingsView.FiltersPulldownAction.doGetMenu(): Failed to "
                                    + "accept new current setting parameters.",
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
    private final List<String> settingNames = Lists.newArrayList();

    /**
     * Map of setting names to their associated identifiers.
     */
    private final Map<String, String> settingIdentifiersForNames = Maps
            .newHashMap();

    /**
     * List of filter menu-based megawidget specifiers.
     */
    private DictList filterMegawidgetSpecifiers = null;

    /**
     * Current settings.
     */
    private Settings currentSettings;

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
     * current setting.
     * 
     * @param map
     *            Map in which the translation is to occur.
     */
    public static void translateHazardCategoriesAndTypesToOldLists(Dict map) {
        List<Object> treeState = map
                .getDynamicallyTypedValue(SETTING_HAZARD_CATEGORIES_AND_TYPES);
        List<String> categories = Lists.newArrayList();
        Set<String> typesSet = Sets.newHashSet();
        for (int j = 0; j < treeState.size(); j++) {
            Map<?, ?> category = (Map<?, ?>) treeState.get(j);
            categories
                    .add((String) category
                            .get(HierarchicalBoundedChoicesMegawidgetSpecifier.CHOICE_NAME));
            List<?> children = (List<?>) category
                    .get(HierarchicalBoundedChoicesMegawidgetSpecifier.CHOICE_CHILDREN);
            for (Object child : children) {
                if (child instanceof Map) {
                    typesSet.add((String) ((Map<?, ?>) child)
                            .get(HierarchicalBoundedChoicesMegawidgetSpecifier.CHOICE_IDENTIFIER));
                } else {
                    typesSet.add((String) child);
                }
            }
        }
        List<String> types = Lists.newArrayList();
        for (String type : typesSet) {
            types.add(type);
        }
        map.put(SETTING_HAZARD_CATEGORIES, categories);
        map.put(SETTING_HAZARD_TYPES, types);
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
     * @param settings
     * @param jsonFilters
     *            JSON string providing a list of dictionaries, each specifying
     *            a filter megawidget.
     * @param currentSettings
     */
    @Override
    public final void initialize(SettingsPresenter presenter,
            List<Settings> settings, String jsonFilters,
            Settings currentSettings) {
        this.presenter = presenter;
        setSettings(settings);
        setFilterMegawidgets(jsonFilters);
        setCurrentSettings(currentSettings);
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
     * @param type
     *            Type of contribution to be made to the main user interface.
     * @return List of contributions; this may be empty if none are to be made.
     */
    @Override
    public final List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        if (type == RCPMainUserInterfaceElement.TOOLBAR) {
            settingsPulldownAction = new SettingsPulldownAction();
            filtersPulldownAction = new FiltersPulldownAction();
            return Lists.newArrayList(settingsPulldownAction,
                    filtersPulldownAction);
        }
        return Collections.emptyList();
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
     * @param availableSettings
     */
    @Override
    public final void setSettings(List<Settings> availableSettings) {
        if ((availableSettings == null) || (availableSettings.size() < 1)) {
            return;
        }

        // Get the names and identifiers of the settings.
        settingNames.clear();
        settingIdentifiersForNames.clear();
        for (Settings settings : availableSettings) {
            String name = settings.getDisplayName();
            settingNames.add(name);
            String identifier = settings.getSettingsID();
            settingIdentifiersForNames.put(name, identifier);
        }

        // Notify the settings pulldown that the settings have
        // changed.
        if (settingsPulldownAction != null) {
            settingsPulldownAction.settingsChanged();
        }
    }

    /**
     * @param currentSettings
     */
    @Override
    public final void setCurrentSettings(Settings currentSettings) {
        this.currentSettings = currentSettings;
        if (filtersPulldownAction != null) {
            filtersPulldownAction.filtersChanged();
        }
        if (settingDialog != null) {
            settingDialog.setState(settingsAsDict());
        }
    }

    private Dict settingsAsDict() {
        return Dict.getInstance(jsonConverter.toJson(currentSettings));
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

}
