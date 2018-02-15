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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_CATEGORIES_AND_TYPES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_TYPES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.ImmutableList;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.FilterIconEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.FilterIcons;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.SettingsConfig;

import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.display.RcpMainUiElement;
import gov.noaa.gsd.viz.hazards.display.action.CurrentSettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.StaticSettingsAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.toolbar.BasicAction;
import gov.noaa.gsd.viz.hazards.toolbar.PulldownAction;
import gov.noaa.gsd.viz.hazards.utilities.MegawidgetSettingsConversionUtils;
import gov.noaa.gsd.viz.megawidgets.HierarchicalBoundedChoicesMegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManagerAdapter;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;

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
 * Feb 19, 2014    2915    bkowal            JSON settings re-factor.
 * Apr 09, 2014    2925    Chris.Golden      Changed to ensure that method is called
 *                                           within the UI thread.
 * Jun 23, 2014    4010    Chris.Golden      Changed to work with megawidget manager
 *                                           changes.
 * Jun 30, 2014    3512    Chris.Golden      Changed to work with more megawidget
 *                                           manager changes.
 * Aug 28, 2014    3768    Robert.Blum       Changed to sort the settings menu and 
 *                                           to load a valid setting on a delete.
 * Dec 05, 2014    4124    Chris.Golden      Changed to work with newly parameterized
 *                                           config manager and with ObservedSettings.
 * Dec 13, 2014    4959    Dan Schaffer      Spatial Display cleanup and other bug fixes
 * Jan 09, 2015    5457    Daniel.S.Schaffer Fixed bug in settings deletion.
 * Feb 23, 2015    3618    Chris.Golden      Added ability to close settings dialog
 *                                           from public method.
 * Nov 17, 2015   11776    Roger.Ferrel      Use @{link {@link ISaveAs} to update
 *                                           settings menu.
 * May 11, 2016   16374    mpduff            Add filter indicator toolbar buttons.
 * Oct 27, 2017   36174    Kevin.Bisanz      Remove restriction to hydro products.
 * Feb 01, 2017   15556    Chris.Golden      Changed to work with settings changes
 *                                           as part of console refactor.
 * Jan 17, 2018   33428    Chris.Golden      Changed to work with new, more flexible
 *                                           toolbar contribution code.
 * May 04, 2018   50032    Chris.Golden      Added additional filters to settings.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SettingsView
        implements ISettingsView<String, IAction, RcpMainUiElement> {

    // Private Static Constants

    /**
     * Edit menu item text.
     */
    private static final String EDIT_COMMAND_MENU_TEXT = "&Edit...";

    private static final String DELETE_COMMAND_MENU_TEXT = "&Delete...";

    /**
     * Array of settings dropdown menu items.
     */
    private static final List<String> SETTINGS_DROPDOWN_MENU_ITEMS = ImmutableList
            .of(DELETE_COMMAND_MENU_TEXT, EDIT_COMMAND_MENU_TEXT);

    /**
     * Settings toolbar menu button text.
     */
    private static final String SETTINGS_TOOLBAR_BUTTON_TEXT = "Settings";

    /**
     * Filters toolbar menu button text.
     */
    private static final String FILTERS_TOOLBAR_BUTTON_TEXT = "Filters";

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

        /**
         * Callback to be executed if a save-as is performed.
         */
        private final ISaveAs saveAs = new ISaveAs() {

            @Override
            public void saveAsPerformed() {
                setSettings(presenter.getSessionManager()
                        .getConfigurationManager().getAvailableSettings());
            }
        };

        // Private Variables

        private static final String SETTINGS_ICON = "gear.png";

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
                String text = ((MenuItem) event.widget).getText();
                if (text.equals(EDIT_COMMAND_MENU_TEXT)) {
                    presenter.showSettingDetail(saveAs);
                } else if (event.widget.getData() != null) {
                    /*
                     * Remember the newly selected setting name and fire off the
                     * action.
                     */
                    String settingsID = (String) event.widget.getData();
                    presenter.publish(new StaticSettingsAction(
                            StaticSettingsAction.ActionType.SETTINGS_CHOSEN,
                            settingsID));
                } else if (text.equals(DELETE_COMMAND_MENU_TEXT)) {
                    ISessionConfigurationManager<ObservedSettings> configManager = presenter
                            .getSessionManager().getConfigurationManager();
                    if (configManager.containsUserLevelSettings()) {
                        String displayName = configManager.getSettings()
                                .getDisplayName();
                        boolean answer = MessageDialog.openQuestion(
                                Display.getCurrent().getActiveShell(),
                                "User Settings",
                                "Are you sure you want to delete user settings for \""
                                        + displayName + "\"?");
                        if (answer) {
                            configManager.deleteSettings();
                            List<Settings> availableSettings = configManager
                                    .getAvailableSettings();
                            String newSettingsId = null;

                            /*
                             * Display any non-USER localized settings.
                             */
                            for (Settings settings : availableSettings) {
                                if (displayName
                                        .equals(settings.getDisplayName())) {
                                    newSettingsId = settings.getSettingsID();
                                    break;
                                }
                            }
                            if (newSettingsId == null) {
                                newSettingsId = availableSettings.iterator()
                                        .next().getSettingsID();
                            }
                            /*
                             * Need to select a new setting since the current
                             * one was deleted, fire off the action.
                             */
                            presenter.publish(new StaticSettingsAction(
                                    StaticSettingsAction.ActionType.SETTINGS_CHOSEN,
                                    newSettingsId));
                            setSettings(availableSettings);
                        }
                    } else {
                        MessageDialog
                                .openInformation(
                                        Display.getCurrent().getActiveShell(),
                                        "User settings",
                                        "No user settings to delete for \""
                                                + configManager.getSettings()
                                                        .getDisplayName()
                                                + "\".");
                    }
                }
            }
        };

        // Public Constructors

        /**
         * Construct a standard instance.
         */
        public SettingsPulldownAction() {
            super(SETTINGS_TOOLBAR_BUTTON_TEXT, SETTINGS_ICON);
        }

        // Public Methods

        /**
         * Receive notification that the settings have changed.
         */
        public void settingsChanged() {
            settingsChanged = true;
        }

        // Protected Methods

        @Override
        public Menu doGetMenu(Control parent, Menu menu) {

            /*
             * If the menu has not yet been created, do so now.
             */
            if (menu == null) {
                menu = new Menu(parent);

                /*
                 * Iterate through the command items, if any, creating a menu
                 * item for each.
                 */
                for (String text : SETTINGS_DROPDOWN_MENU_ITEMS) {
                    MenuItem item = new MenuItem(menu, SWT.PUSH);
                    item.setText(text);
                    item.addSelectionListener(listener);
                }

                /*
                 * Add a separator.
                 */
                new MenuItem(menu, SWT.SEPARATOR);
            }

            /*
             * If settings have changed, delete the existing settings (if any)
             * and recreate according to the new list. For now, just populate
             * with the full settings list instead of most recently used.
             * 
             * TODO: Change to populate with most recently used.
             */
            if (settingsChanged) {
                for (MenuItem item : menu.getItems()) {
                    if (item.getData() != null) {
                        item.dispose();
                    }
                }
                for (int j = 0; j < settingNames.size(); j++) {
                    MenuItem item = new MenuItem(menu, SWT.PUSH);
                    item.setText(settingNames.get(j));
                    item.setData(settingIdentifiersForNames
                            .get(settingNames.get(j)));
                    item.addSelectionListener(listener);
                }
                settingsChanged = false;
            }

            /*
             * Return the menu.
             */
            return menu;
        }
    }

    /**
     * Filters pulldown menu action.
     */
    private class FiltersPulldownAction extends PulldownAction {

        // Private Variables

        private static final String FILTER_ICON = "filter.png";

        /**
         * Megawidget manager handling the menu megawidgets.
         */
        private MegawidgetManager megawidgetManager = null;

        /**
         * Flag indicating that the number or types of filters may have changed.
         */
        private boolean filtersChanged = true;

        /**
         * Flag indicating whether or not the filter values may have changed.
         */
        private boolean filterValuesChanged = true;

        // Public Constructors

        /**
         * Construct a standard instance.
         */
        public FiltersPulldownAction() {
            super(FILTERS_TOOLBAR_BUTTON_TEXT, FILTER_ICON);
        }

        // Public Methods

        @Override
        public void dispose() {
            super.dispose();
            megawidgetManager = null;
        }

        /**
         * Receive notification that the number or type of filters may have
         * changed.
         */
        public void filtersChanged() {
            filtersChanged = true;
        }

        /**
         * Receive notification that the filter values may have changed.
         */
        public void filterValuesChanged() {
            filterValuesChanged = true;
        }

        // Protected Methods

        @Override
        public Menu doGetMenu(Control parent, Menu menu) {

            /*
             * If the menu has not yet been created, create it now.
             */
            if ((menu == null) || filtersChanged) {
                if (menu != null) {
                    menu.dispose();
                }
                filtersChanged = false;
                menu = new Menu(parent);
                try {
                    megawidgetManager = new MegawidgetManager(menu,
                            filterMapList,
                            MegawidgetSettingsConversionUtils
                                    .settingsPOJOToMap(currentSettings),
                            new MegawidgetManagerAdapter() {

                                @Override
                                public void stateElementChanged(
                                        MegawidgetManager manager,
                                        String identifier, Object state) {
                                    currentSettings = MegawidgetSettingsConversionUtils
                                            .updateSettingsUsingMap(
                                                    currentSettings,
                                                    manager.getState(),
                                                    UIOriginator.SETTINGS_MENU);

                                    /*
                                     * Forward the current setting change to the
                                     * presenter.
                                     */
                                    try {
                                        presenter.publish(
                                                new CurrentSettingsAction(
                                                        currentSettings,
                                                        UIOriginator.SETTINGS_MENU));
                                    } catch (Exception e) {
                                        statusHandler.error(
                                                "Could not serialize JSON.", e);
                                    }
                                }
                            });
                } catch (MegawidgetException e) {
                    statusHandler.error(
                            "Unable to create megawidget manager due to "
                                    + "megawidget construction problem: " + e,
                            e);
                } catch (Exception e) {
                    statusHandler.error(
                            "Failed to convert the Current Settings to Java map.",
                            e);
                }
            }

            /*
             * If the filters may have changed, update the megawidget states.
             */
            if (filterValuesChanged) {
                try {
                    megawidgetManager.setState(MegawidgetSettingsConversionUtils
                            .settingsPOJOToMap(currentSettings));
                } catch (MegawidgetStateException e) {
                    statusHandler
                            .error("SettingsView.FiltersPulldownAction.doGetMenu(): Failed to "
                                    + "accept new current setting parameters: "
                                    + e, e);
                } catch (Exception e) {
                    statusHandler.error(
                            "Failed to convert the Current Settings to a Java Map!",
                            e);
                }
                filterValuesChanged = false;
            }

            /*
             * Return the menu.
             */
            return menu;
        }
    }

    /**
     * Standard action.
     */
    private class FilterAction extends BasicAction {
        private final String label;

        private final String icon;

        private final String colorIcon;

        private final List<String> hazardTypes;

        private final List<HazardStatus> statuses;

        private String activeIcon;

        private FilterAction(String label, String icon, String coloredIcon,
                String[] hazardTypes, String[] statuses) {
            super(label, icon, SWT.NONE, label);
            this.label = label;
            this.icon = icon;
            this.colorIcon = coloredIcon;

            this.hazardTypes = new ArrayList<>(hazardTypes.length);
            for (String ht : hazardTypes) {
                this.hazardTypes.add(ht);
            }

            this.statuses = new ArrayList<>(statuses.length);
            for (String s : statuses) {
                this.statuses.add(HazardConstants.hazardStatusFromString(s));
            }
        }

        /**
         * Run the action.
         */
        @Override
        public void run() {
            // Currently no-op. May be needed in the future
        }

        public void setActiveIcon(boolean filteringActive) {
            if (filteringActive) {
                activeIcon = this.colorIcon;
            } else {
                activeIcon = this.icon;
            }

            this.setImageDescriptor(getImageDescriptorForFile(activeIcon));
        }

        public String getLabel() {
            return this.label;
        }

        public List<String> getHazardTypes() {
            return hazardTypes;
        }

        public List<HazardStatus> getStatuses() {
            return statuses;
        }

        @Override
        public String toString() {
            return getLabel() + getHazardTypes() + getStatuses();
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
    private final List<String> settingNames = new ArrayList<>();

    /**
     * Map of setting names to their associated identifiers.
     */
    private final Map<String, String> settingIdentifiersForNames = new LinkedHashMap<>();

    /**
     * List of filter maps.
     */
    private List<Map<String, Object>> filterMapList;

    /**
     * Current settings.
     */
    private ObservedSettings currentSettings;

    /**
     * Settings pulldown action.
     */
    private SettingsPulldownAction settingsPulldownAction = null;

    /**
     * Filters pulldown action.
     */
    private FiltersPulldownAction filtersPulldownAction = null;

    /**
     * List of filter actions.
     */
    private List<FilterAction> filterActions = null;

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
     * lists, placing those lists back in the map.
     * <p>
     * TODO: This should be removed if the <code>visibleTypes</code> list can be
     * gotten rid of in settings.
     * </p>
     * 
     * @param map
     *            Map in which the translation is to occur.
     */
    public static void translateHazardCategoriesAndTypesToOldLists(Dict map) {
        List<Object> treeState = map
                .getDynamicallyTypedValue(SETTING_HAZARD_CATEGORIES_AND_TYPES);
        Set<String> typesSet = new HashSet<>();
        for (int j = 0; j < treeState.size(); j++) {
            Map<?, ?> category = (Map<?, ?>) treeState.get(j);
            List<?> children = (List<?>) category.get(
                    HierarchicalBoundedChoicesMegawidgetSpecifier.CHOICE_CHILDREN);
            for (Object child : children) {
                if (child instanceof Map) {
                    typesSet.add((String) ((Map<?, ?>) child).get(
                            HierarchicalBoundedChoicesMegawidgetSpecifier.CHOICE_IDENTIFIER));
                } else {
                    typesSet.add((String) child);
                }
            }
        }
        List<String> types = new ArrayList<>();
        for (String type : typesSet) {
            types.add(type);
        }
        map.put(SETTING_HAZARD_TYPES, types);
    }

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public SettingsView() {

        /*
         * No action.
         */
    }

    // Public Methods

    @Override
    public final void initialize(SettingsPresenter presenter,
            List<Settings> settings, Field[] fields,
            ObservedSettings currentSettings, FilterIcons filterIcons) {
        this.presenter = presenter;
        setSettings(settings);
        setCurrentSettings(currentSettings);

        setFilterFields(fields);

        filterActions = new ArrayList<>(filterIcons.size());

        for (FilterIconEntry entry : filterIcons) {
            FilterAction action = new FilterAction(entry.getLabel(),
                    entry.getNormalIcon(), entry.getColoredIcon(),
                    entry.getHazardTypes(), entry.getStatus());
            filterActions.add(action);
        }
    }

    @Override
    public final void dispose() {
        if (settingDialog != null) {
            settingDialog.close();
            settingDialog = null;
        }
    }

    @Override
    public final Map<? extends String, List<? extends IAction>> contributeToMainUi(
            RcpMainUiElement type) {
        if (type == RcpMainUiElement.TOOLBAR) {
            settingsPulldownAction = new SettingsPulldownAction();
            filtersPulldownAction = new FiltersPulldownAction();
            Map<String, List<? extends IAction>> map = new HashMap<>(2, 1.0f);
            map.put(SETTINGS_PULLDOWN_IDENTIFIER,
                    ImmutableList.of(settingsPulldownAction));
            map.put(FILTERS_PULLDOWN_IDENTIFIER,
                    ImmutableList.of(filtersPulldownAction));
            map.put(FILTER_INDICATORS_IDENTIFIER,
                    ImmutableList.copyOf(filterActions));
            return map;
        }
        return Collections.emptyMap();
    }

    @Override
    public final void showSettingDetail(SettingsConfig settingsConfig,
            ObservedSettings settings, ISaveAs saveAs) {
        if (settingDialog != null) {
            settingDialog.setSaveAs(saveAs);
            settingDialog.open();
            return;
        }

        settingDialog = new SettingDialog(presenter,
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                settingsConfig, settings);
        settingDialog.setSaveAs(saveAs);
        settingDialog.open();
        settingDialog.getShell().addDisposeListener(dialogDisposeListener);
    }

    @Override
    public final void deleteSettingDetail() {
        if (settingDialog != null) {
            if (settingDialog.isDisposed() == false) {
                settingDialog.close();
            }
            settingDialog = null;
        }
    }

    @Override
    public final boolean isSettingDetailExisting() {
        return (settingDialog != null);
    }

    @Override
    public final void setSettings(List<Settings> availableSettings) {
        if ((availableSettings == null) || (availableSettings.size() < 1)) {
            return;
        }

        /*
         * Get the names and identifiers of the settings.
         */
        settingNames.clear();
        settingIdentifiersForNames.clear();
        for (Settings settings : availableSettings) {
            String name = settings.getDisplayName();
            settingNames.add(name);
            String identifier = settings.getSettingsID();
            settingIdentifiersForNames.put(name, identifier);
        }

        /*
         * Alphabetize the settingsNames
         */
        Collections.sort(settingNames);

        /*
         * Notify the settings pulldown that the settings have changed.
         */
        if (settingsPulldownAction != null) {
            settingsPulldownAction.settingsChanged();
        }
    }

    @Override
    public final void setCurrentSettings(
            final ObservedSettings currentSettings) {
        VizApp.runAsync(new Runnable() {
            @Override
            public void run() {
                SettingsView.this.currentSettings = currentSettings;
                if (filtersPulldownAction != null) {
                    filtersPulldownAction.filterValuesChanged();
                }
                if (settingDialog != null) {
                    settingDialog.setState(currentSettings);
                }
            }
        });
    }

    @Override
    public void setFilterFields(Field[] fields) {
        filterMapList = new ArrayList<Map<String, Object>>();
        try {
            filterMapList = MegawidgetSettingsConversionUtils
                    .buildFieldMapList(fields);
            if (filtersPulldownAction != null) {
                filtersPulldownAction.filtersChanged();
            }
        } catch (Exception e) {
            statusHandler.error("Failed to build a list of Map fields!", e);
        }
    }

    @Override
    public void updateFilterSettings(
            Set<? extends IReadableHazardEvent> filteredEvents) {
        updateFilterStates(filteredEvents);
    }

    // Private Methods

    /**
     * Update the state of the filter icons.
     * 
     * @param filteredEvents
     *            The events that have been filtered out
     */
    private void updateFilterStates(
            Set<? extends IReadableHazardEvent> filteredEvents) {
        for (FilterAction action : filterActions) {
            action.setActiveIcon(false);
            List<String> hazardTypes = action.getHazardTypes();
            List<HazardStatus> statuses = action.getStatuses();

            for (IReadableHazardEvent event : filteredEvents) {

                /*
                 * Set the icon if this filtered hazard has both the type and
                 * status to turn on the indicator.
                 */
                if (hazardTypes.contains(event.getHazardType())
                        && statuses.contains(event.getStatus())) {
                    action.setActiveIcon(true);
                    break;
                }
            }
        }
    }
}
