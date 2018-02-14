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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_SORT_DIRECTION_ASCENDING;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_SORT_DIRECTION_DESCENDING;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_SORT_DIRECTION_NONE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_SORT_PRIORITY_NONE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_TYPE_BOOLEAN;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_TYPE_COUNTDOWN;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_TYPE_DATE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_TYPE_NUMBER;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_TYPE_STRING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.IContributionItem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.uf.common.hazards.productgen.data.ProductDataUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardAlertsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.HazardEventExpirationConsoleTimer;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsLoaded;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SiteChanged;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings.Type;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardCategoryAndTypes;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Column;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Console;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventAllowUntilFurtherNoticeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventAttributesModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventOriginModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventStatusModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventTimeRangeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventTypeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventUnsavedChangesModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.IEventModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventHistoryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsLockStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsTimeRangeBoundariesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.time.CurrentTimeChanged;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTime;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTimeChanged;
import com.raytheon.uf.viz.hazards.sessionmanager.time.VisibleTimeRangeChanged;
import com.raytheon.viz.core.mode.CAVEMode;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.common.utilities.JsonConverter;
import gov.noaa.gsd.common.utilities.Sort;
import gov.noaa.gsd.common.utilities.Sort.SortDirection;
import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.alerts.CountdownTimer;
import gov.noaa.gsd.viz.hazards.console.ConsoleColumns.ColumnDefinition;
import gov.noaa.gsd.viz.hazards.contextmenu.ContextMenuHelper;
import gov.noaa.gsd.viz.hazards.contextmenu.ContextMenuHelper.IContributionItemUpdater;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;
import net.engio.mbassy.listener.Handler;

/**
 * Console presenter, used to manage the console view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle,
 *                                           including the passing in of the event
 *                                           bus so that the latter is no longer a
 *                                           singleton.
 * Aug 16, 2013    1325    daniel.s.schaffer Alerts integration
 * Aug 22, 2013    1936    Chris.Golden      Added console countdown timers.
 * Nov 04, 2013    2182    daniel.s.schaffer Started refactoring
 * Dec 03, 2013    2182    daniel.s.schaffer Refactoring - eliminated IHazardsIF
 * Jan 28, 2014    2161    Chris.Golden      Fixed bug that caused equality checks
 *                                           of old and new hazard events to always
 *                                           return false. Also added passing of
 *                                           set of events allowing "until further
 *                                           notice" to the view during initialization.
 * May 17, 2014    2925    Chris.Golden      Added newly required implementation of
 *                                           reinitialize(), and made initialize()
 *                                           protected as it is called by setView().
 * Nov 18, 2014    4124    Chris.Golden      Changed to use a handler method to watch
 *                                           for selected time changes, and adapted to
 *                                           new time manager.
 * Dec 05, 2014    4124    Chris.Golden      Changed to use a handler method to watch
 *                                           for settings changes, and to work with
 *                                           newly parameterized config manager. Also
 *                                           added in code to ignore changes that
 *                                           originated with this presenter.
 * Jan 08, 2015    2394    Chris.Golden      Added code to ensure that the river mile
 *                                           hazard attribute is always included in
 *                                           the dictionaries sent to the view when it
 *                                           is present in the original events.
 * Dec 13, 2015    4959    Dan Schaffer      Spatial Display cleanup and other bug
 *                                           fixes
 * Jan 30, 2015    2331    Chris.Golden      Changed to receive notifications of time
 *                                           change using handlers instead of via the
 *                                           modelChanged() method, since said
 *                                           notifications now come from the session
 *                                           time manager. Also changed to use time
 *                                           range boundaries for the events.
 * Mar 15, 2015   15676    Chris.Golden      Removed visible time range reaction, as
 *                                           the notification is obsolete.
 * Nov 18, 2015   13279    Chris.Golden      Fixed bug that caused temporal display's
 *                                           event time range sliders to not always
 *                                           have the correct possible-value boundaries.
 * Nov 23, 2015    3473    Robert.Blum       Removed code for importing service backup.
 * Mar 14, 2016   12145    mduff             Handle error thrown by event manager.
 * Aug 15, 2016   18376    Chris.Golden      Removed unregistering for notification
 *                                           at shutdown, since this is already done
 *                                           by the session manager (and it was
 *                                           asymmetric to have it done here, since
 *                                           the preceding registering for notification
 *                                           was done by the session manager, not this
 *                                           object).
 * Sep 12, 2016   15934    Chris.Golden      Changed to work with new version of
 *                                           JsonConverter.
 * Oct 04, 2016   22573    Robert.Blum       Clearing CWA geometry when site changes.
 * Oct 11, 2016   21824    Robert.Blum       Fixed site in console title when switching
 *                                           perspectives.
 * Oct 19, 2016   21873    Chris.Golden      Added time resolution tracking tied to
 *                                           settings.
 * Oct 27, 2016   22956    Ben.Phillippe     Update console on site ID change
 * Dec 14, 2016   22119    Kevin.Bisanz      Add Export Product Edits menu.
 * Dec 19, 2016   21504    Robert.Blum       Adapted to hazard locking.
 * Feb 01, 2017   15556    Chris.Golden      Complete refactoring to address MVP
 *                                           design concerns, untangle spaghetti, and
 *                                           add history list viewing.
 * Mar 16, 2017   15528    Chris.Golden      Added notification handler for the unsaved
 *                                           changes flag of a hazard event changing.
 * Mar 28, 2017   32487    Chris.Golden      Added handler for the new
 *                                           SessionEventOriginModified notification
 *                                           to update the console row when a hazard
 *                                           event's site identifier, workstation, or
 *                                           user name are modified.
 * Apr 05, 2017   32733    Robert.Blum       Changed lock status modified handler to
 *                                           deal with new version of notification that
 *                                           notifies of one or more lock statuses
 *                                           changing.
 * Jun 08, 2017   16373    Chris.Golden      Removed product viewer selection dialog 
 *                                           usage, as the product view and presenter
 *                                           take care of this now.
 * Jun 26, 2017   19207    Chris.Golden      Removed obsolete product viewer selection
 *                                           code.
 * Jun 30, 2017   19223    Chris.Golden      Added ability to change the text and
 *                                           enabled state of a row menu's menu item
 *                                           after it is displayed.
 * Aug 08, 2017   22583    Chris.Golden      Add service backup banner.
 * Sep 27, 2017   38072    Chris.Golden      Changed to use new SessionEventModified
 *                                           notification.
 * Dec 07, 2017   41886    Chris.Golden      Removed Java 8/JDK 1.8 usage.
 * Dec 17, 2017   20739    Chris.Golden      Refactored away access to directly
 *                                           mutable session events.
 * Jan 17, 2018   33428    Chris.Golden      Changed to work with new, more flexible
 *                                           toolbar contribution code.
 * Feb 06, 2018   46258    Chris.Golden      Fixed null pointer exception bug when
 *                                           checking for hazard conflicts.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ConsolePresenter
        extends HazardServicesPresenter<IConsoleView<?, ?, ?>> {

    // Public Enumerated Types

    /**
     * Types of time ranges that may be manipulated bidirectionally between the
     * presenter and view.
     */
    public enum TimeRangeType {
        VISIBLE, SELECTED
    }

    /**
     * Commands.
     */
    public enum Command {
        RESET, CLOSE, EXPORT_SITE_CONFIG, EXPORT_SITE_PRODUCT_EDITS, CHECK_FOR_CONFLICTS
    }

    /**
     * Toggles.
     */
    public enum Toggle {
        AUTO_CHECK_FOR_CONFLICTS, SHOW_HATCHED_AREAS, SHOW_HISTORY_LISTS
    }

    /**
     * VTEC format mode.
     */
    public enum VtecFormatMode {

        // Values
        NORMAL_NO_VTEC("Normal: NoVTEC", null, false), NORMAL_O_VTEC(
                "Normal: O-Vtec", "O", false), NORMAL_E_VTEC("Normal: E-Vtec",
                        "E", false), NORMAL_X_VTEC("Normal: X-Vtec", "X",
                                false), TEST_NO_VTEC("Test: NoVTEC", null,
                                        true), TEST_T_VTEC("Test: T-Vtec", "T",
                                                true);

        // Private Variables

        /**
         * Display string.
         */
        private final String displayString;

        /**
         * VTEC mode.
         */
        private final String vtecMode;

        /**
         * Flag indicating whether test mode applies.
         */
        private final boolean testMode;

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param displayString
         *            Display string.
         * @param vtecMode
         *            VTEC mode.
         * @param testMode
         *            Flag indicating whether or not test mode applies.
         */
        private VtecFormatMode(String displayString, String vtecMode,
                boolean testMode) {
            this.displayString = displayString;
            this.vtecMode = vtecMode;
            this.testMode = testMode;
        }

        // Public Methods

        @Override
        public String toString() {
            return displayString;
        }

        /**
         * Get the VTEC mode.
         * 
         * @return VTEC mode.
         */
        public String getVtecMode() {
            return vtecMode;
        }

        /**
         * Determine whether or not test mode applies.
         * 
         * @return <code>true</code> if test mode applies, <code>false</code>
         *         otherwise.
         */
        public boolean isTestMode() {
            return testMode;
        }
    }

    // Private Classes

    /**
     * Review key, used for collating {@link ProductData} lists when generating
     * review menus.
     */
    private class ReviewKey {

        // Private Variables

        /**
         * Product generator name.
         */
        private final String productGeneratorName;

        /**
         * Event identifiers.
         */
        private final List<String> eventIdentifiers;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param productGeneratorName
         *            Product generator name.
         * @param eventIdentifiers
         *            Event identifiers.
         */
        public ReviewKey(String productGeneratorName,
                List<String> eventIdentifiers) {
            this.productGeneratorName = productGeneratorName;
            this.eventIdentifiers = eventIdentifiers;
        }

        // Public Methods

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((eventIdentifiers == null) ? 0
                    : eventIdentifiers.hashCode());
            result = prime * result + ((productGeneratorName == null) ? 0
                    : productGeneratorName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ReviewKey other = (ReviewKey) obj;
            if (eventIdentifiers == null) {
                if (other.eventIdentifiers != null) {
                    return false;
                }
            } else if (eventIdentifiers
                    .equals(other.eventIdentifiers) == false) {
                return false;
            }
            if (productGeneratorName == null) {
                if (other.productGeneratorName != null) {
                    return false;
                }
            } else if (productGeneratorName
                    .equals(other.productGeneratorName) == false) {
                return false;
            }
            return true;
        }
    }

    // Private Static Constants

    /**
     * Map pairing modification classes of interest for the handler of the
     * {@link SessionEventModified} notification with flags indicating whether
     * or not each such modification should only trigger a refresh of the
     * modified event's tabular entity if the originator was not the console.
     */
    private static final Map<Class<? extends IEventModification>, Boolean> CHECK_ORIGINATOR_FLAGS_FOR_EVENT_MODIFICATION_CLASSES;

    static {
        Map<Class<? extends IEventModification>, Boolean> map = new HashMap<>(7,
                1.0f);
        map.put(EventTypeModification.class, false);
        map.put(EventStatusModification.class, false);
        map.put(EventTimeRangeModification.class, true);
        map.put(EventAllowUntilFurtherNoticeModification.class, false);
        map.put(EventOriginModification.class, false);
        map.put(EventUnsavedChangesModification.class, false);
        map.put(EventAttributesModification.class, true);
        CHECK_ORIGINATOR_FLAGS_FOR_EVENT_MODIFICATION_CLASSES = ImmutableMap
                .copyOf(map);
    }

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ConsolePresenter.class);

    // Private Variables

    /**
     * List of sorts being applied to event ordering, with the sorts themselves
     * ordered within the list by priority (highest first).
     */
    private final List<Sort> sorts = new ArrayList<>(2);

    /**
     * Map of sort identifiers to the comparators to be used for sorting when
     * sorting by those identifiers. If a comparator for a particular column is
     * <code>null</code>, it should be sorted as a countdown timer.
     */
    private final Map<String, Comparator<?>> comparatorsForSortIdentifiers = new HashMap<>();

    /**
     * Map of sort identifiers to the types of the values to be used for sorting
     * when sorting by those identifiers. An entry exists in this map for every
     * key from {@link #comparatorsForSortIdentifiers}.
     */
    private final Map<String, Class<?>> typesForSortIdentifiers = new HashMap<>();

    /**
     * Time range change handler. The identifier is the type of the time range.
     */
    private final IStateChangeHandler<TimeRangeType, Range<Long>> timeRangeChangeHandler = new IStateChangeHandler<TimeRangeType, Range<Long>>() {

        @Override
        public void stateChanged(TimeRangeType identifier, Range<Long> value) {
            if (identifier == TimeRangeType.VISIBLE) {
                getModel().getTimeManager()
                        .setVisibleTimeRange(
                                new TimeRange(value.lowerEndpoint(),
                                        value.upperEndpoint()),
                                UIOriginator.CONSOLE);
            } else {
                getModel().getTimeManager()
                        .setSelectedTime(
                                new SelectedTime(value.lowerEndpoint(),
                                        value.upperEndpoint()),
                                UIOriginator.CONSOLE);
            }
        }

        @Override
        public void statesChanged(
                Map<TimeRangeType, Range<Long>> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("time range");
        }
    };

    /**
     * Tree columns change handler. The identifier is ignored.
     */
    private final IStateChangeHandler<String, ConsoleColumns> columnsChangeHandler = new IStateChangeHandler<String, ConsoleColumns>() {

        @Override
        public void stateChanged(String identifier, ConsoleColumns value) {
            ObservedSettings currentSettings = getModel()
                    .getConfigurationManager().getSettings();
            currentSettings.setColumns(
                    value.getModifiedColumnsForNames(
                            currentSettings.getColumns()),
                    UIOriginator.CONSOLE);
            currentSettings.setVisibleColumns(value.getVisibleColumnNames(),
                    UIOriginator.CONSOLE);
        }

        @Override
        public void statesChanged(
                Map<String, ConsoleColumns> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("tree columns");
        }
    };

    /**
     * Tree column-based filters change handler. The identifier is the name of
     * the column.
     */
    private final IStateChangeHandler<String, Object> columnFiltersChangeHandler = new IStateChangeHandler<String, Object>() {

        @SuppressWarnings("unchecked")
        @Override
        public void stateChanged(String identifier, Object value) {
            ObservedSettings currentSettings = getModel()
                    .getConfigurationManager().getSettings();
            if (identifier.equals(
                    HazardConstants.SETTING_HAZARD_CATEGORIES_AND_TYPES)) {
                HazardCategoriesAndTypes hazardCategoriesAndTypes = null;
                try {
                    hazardCategoriesAndTypes = JsonConverter.fromJson(
                            JsonConverter.toJson(value),
                            HazardCategoriesAndTypes.class);
                } catch (IOException e) {
                    statusHandler
                            .error("Unexpected error when converting List<Map<String, Object>> "
                                    + "to List<HazardCategoryAndTypes>", e);
                }
                currentSettings
                        .setHazardCategoriesAndTypes(
                                hazardCategoriesAndTypes.toArray(
                                        new HazardCategoryAndTypes[hazardCategoriesAndTypes
                                                .size()]),
                        UIOriginator.CONSOLE);
            } else if (identifier
                    .equals(HazardConstants.SETTING_HAZARD_STATES)) {
                currentSettings.setVisibleStatuses(
                        new HashSet<String>((Collection<String>) value),
                        UIOriginator.CONSOLE);
            } else if (identifier
                    .equals(HazardConstants.SETTING_HAZARD_SITES)) {
                currentSettings.setVisibleSites(
                        new HashSet<String>((Collection<String>) value),
                        UIOriginator.CONSOLE);
            } else {
                statusHandler.error(
                        "Unexpected state change with identifier \""
                                + identifier + "\"",
                        new IllegalStateException());
            }
        }

        @Override
        public void statesChanged(Map<String, Object> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("tree column filters");
        }
    };

    /**
     * The sort invocation handler. The identifier is the sort to be performed.
     */
    private final ICommandInvocationHandler<Sort> sortInvocationHandler = new ICommandInvocationHandler<Sort>() {

        @Override
        public void commandInvoked(Sort identifier) {
            handleSortChange(identifier);
        }
    };

    /**
     * Command invocation handler. The identifier is the command itself.
     */
    private final ICommandInvocationHandler<Command> commandInvocationHandler = new ICommandInvocationHandler<Command>() {

        @Override
        public void commandInvoked(Command identifier) {
            if (identifier == Command.RESET) {
                getModel().reset();
            } else if (identifier == Command.CLOSE) {
                handleConsoleClosed();
            } else if (identifier == Command.EXPORT_SITE_CONFIG) {
                exportApplicationBackupSiteData();
            } else if (identifier == Command.EXPORT_SITE_PRODUCT_EDITS) {
                exportApplicationBackupSiteProductEdits();
            } else if (identifier == Command.CHECK_FOR_CONFLICTS) {
                checkForConflicts();
            }
        }
    };

    /**
     * Toggle change handler. The identifier is the toggle itself.
     */
    private final IStateChangeHandler<Toggle, Boolean> toggleStateChangeHandler = new IStateChangeHandler<Toggle, Boolean>() {

        @Override
        public void stateChanged(Toggle identifier, Boolean value) {
            if (identifier == Toggle.AUTO_CHECK_FOR_CONFLICTS) {
                getModel().toggleAutoHazardChecking();
            } else if (identifier == Toggle.SHOW_HATCHED_AREAS) {
                getModel().toggleHatchedAreaDisplay();
            } else if (identifier == Toggle.SHOW_HISTORY_LISTS) {
                handleShowHistoryListToggle(value);
            }
        }

        @Override
        public void statesChanged(Map<Toggle, Boolean> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("toggle");
        }
    };

    /**
     * VTEC mode change handler. The identifier is ignored.
     */
    private final IStateChangeHandler<String, VtecFormatMode> vtecModeStateChangeHandler = new IStateChangeHandler<String, VtecFormatMode>() {

        @Override
        public void stateChanged(String identifier, VtecFormatMode value) {
            getModel().getProductManager().setVTECFormat(value.getVtecMode(),
                    value.isTestMode());
        }

        @Override
        public void statesChanged(
                Map<String, VtecFormatMode> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("VTEC mode");
        }

    };

    /**
     * Site change handler. The identifier is ignored.
     */
    private final IStateChangeHandler<String, String> siteStateChangeHandler = new IStateChangeHandler<String, String>() {

        @Override
        public void stateChanged(String identifier, String value) {

            /*
             * Ensure the site is one of the visible sites in the current
             * setting's filters.
             */
            ISessionConfigurationManager<ObservedSettings> configManager = getModel()
                    .getConfigurationManager();
            ObservedSettings currentSetting = configManager.getSettings();
            Set<String> visibleSites = currentSetting.getVisibleSites();
            visibleSites.add(value);
            currentSetting.setVisibleSites(visibleSites, UIOriginator.CONSOLE);

            /*
             * Set the site to be the current one, and clear the CWA.
             */
            configManager.setSiteID(value, UIOriginator.CONSOLE);
            getModel().getEventManager().clearCwaGeometry();
        }

        @Override
        public void statesChanged(Map<String, String> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("site");
        }
    };

    /**
     * Handler to be notified when the console is disposed of.
     */
    private IConsoleHandler consoleHandler;

    /**
     * Tabular entity manager.
     */
    private final TabularEntityManager tabularEntityManager;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param consoleHandler
     *            Handler to be notified when the console is disposed of.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public ConsolePresenter(ISessionManager<ObservedSettings> model,
            IConsoleHandler consoleHandler,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
        this.consoleHandler = consoleHandler;
        this.tabularEntityManager = new TabularEntityManager(model,
                Collections.unmodifiableList(sorts),
                Collections.unmodifiableMap(comparatorsForSortIdentifiers),
                Collections.unmodifiableMap(typesForSortIdentifiers));
    }

    // Public Methods

    @Override
    @Deprecated
    public final void modelChanged(EnumSet<HazardConstants.Element> changed) {

        /*
         * No action.
         */
    }

    /**
     * Respond to the current CAVE time changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void currentTimeChanged(CurrentTimeChanged change) {
        getView().setCurrentTime(change.getTimeManager().getCurrentTime());
    }

    /**
     * Respond to the selected time range changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void selectedTimeChanged(SelectedTimeChanged change) {
        if (change.getOriginator() != UIOriginator.CONSOLE) {
            SelectedTime selectedTime = getModel().getTimeManager()
                    .getSelectedTime();
            getView().getTimeRangeChanger().setState(TimeRangeType.SELECTED,
                    Range.closed(selectedTime.getLowerBound(),
                            selectedTime.getUpperBound()));
        }
    }

    /**
     * Respond to the visible time range changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void visibleTimeChanged(VisibleTimeRangeChanged change) {
        if (change.getOriginator() != UIOriginator.CONSOLE) {
            TimeRange visibleTime = getModel().getTimeManager()
                    .getVisibleTimeRange();
            getView().getTimeRangeChanger().setState(TimeRangeType.VISIBLE,
                    Range.closed(visibleTime.getStart().getTime(),
                            visibleTime.getEnd().getTime()));
        }
    }

    /**
     * Respond to the current settings changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void settingsModified(SettingsModified change) {

        /*
         * If the the settings has been loaded, forward its name to the view.
         */
        ObservedSettings settings = getModel().getConfigurationManager()
                .getSettings();
        if (change instanceof SettingsLoaded) {
            if (settings != null) {
                getView().setSettingsName(settings.getDisplayName());
            }
        }

        /*
         * Update other aspects of the view if the originator is not the console
         * itself.
         */
        Set<Type> changed = change.getChanged();
        boolean filtersChanged = changed.contains(Type.FILTERS);
        boolean sortChanged = false;
        if (change.getOriginator() != UIOriginator.CONSOLE) {

            /*
             * If the time resolution has changed, notify the view.
             */
            if (changed.contains(Type.TIME_RESOLUTION)
                    && (change.getOriginator() != UIOriginator.CONSOLE)) {
                getView().setTimeResolution(
                        (TimeResolution) getModel().getConfigurationManager()
                                .getSettingsValue(
                                        HazardConstants.TIME_RESOLUTION,
                                        change.getSettings()),
                        getModel().getTimeManager().getCurrentTime());
            }

            /*
             * If columns have changed, notify the view, as well as handling
             * sorting changes.
             */
            if (changed.contains(Type.COLUMN_DEFINITIONS)
                    || changed.contains(Type.VISIBLE_COLUMNS)) {
                ConsoleColumns columns = new ConsoleColumns(
                        settings.getColumns(), settings.getVisibleColumns());
                getView().getColumnsChanger().setState(null, columns);

                /*
                 * Determine which is the appropriate comparator and class for
                 * the data associated with each sort identifier.
                 */
                comparatorsForSortIdentifiers.clear();
                typesForSortIdentifiers.clear();
                for (Map.Entry<String, ColumnDefinition> entry : columns
                        .getColumnDefinitionsForNames().entrySet()) {
                    String sortByType = entry.getValue().getType();
                    Ordering<?> comparator = null;
                    Class<?> type = null;
                    if (sortByType.equals(SETTING_COLUMN_TYPE_STRING)) {
                        comparator = Ordering.<String> natural();
                        type = String.class;
                    } else if (sortByType.equals(SETTING_COLUMN_TYPE_DATE)) {
                        comparator = Ordering.<Date> natural();
                        type = Date.class;
                    } else if (sortByType.equals(SETTING_COLUMN_TYPE_NUMBER)) {
                        comparator = Ordering.<Double> natural();
                        type = Double.class;
                    } else if (sortByType.equals(SETTING_COLUMN_TYPE_BOOLEAN)) {
                        comparator = Ordering.<Boolean> natural();
                        type = Boolean.class;
                    } else if (sortByType
                            .equals(SETTING_COLUMN_TYPE_COUNTDOWN)) {

                        /*
                         * No action; comparator should be null.
                         */
                    } else {
                        statusHandler
                                .error("Do not know how to compare values of type \""
                                        + sortByType
                                        + "\" for event sorting purposes.");
                    }
                    comparatorsForSortIdentifiers.put(
                            entry.getValue().getIdentifier(),
                            (comparator != null ? comparator.nullsFirst()
                                    : null));
                    typesForSortIdentifiers
                            .put(entry.getValue().getIdentifier(), type);
                }

                /*
                 * If the sort algorithms have changed, notify the view of the
                 * new primary sort column and sort direction, and sort the
                 * events.
                 */
                List<Sort> newSorts = createSorts();
                if (newSorts.equals(sorts) == false) {
                    sortChanged = true;
                    sorts.clear();
                    sorts.addAll(newSorts);
                    getView().setSorts(ImmutableList.copyOf(sorts));
                }
            }

            /*
             * If the filters have changed, notify the view.
             */
            if (filtersChanged) {

                /*
                 * Convert the various lists used to generate the filters for
                 * visible hazard events into generic lists and maps.
                 */
                Map<String, Object> valuesForIdentifiers = new HashMap<>(3,
                        1.0f);
                List<Map<String, Object>> hazardCategoriesAndTypes = null;
                try {
                    hazardCategoriesAndTypes = JsonConverter
                            .fromJson(JsonConverter.toJson(
                                    settings.getHazardCategoriesAndTypes()));
                } catch (IOException e) {
                    statusHandler
                            .error("Unexpected error when converting List<HazardCategoryAndTypes> "
                                    + "to List<Map<String, Object>>", e);
                }
                valuesForIdentifiers.put(
                        HazardConstants.SETTING_HAZARD_CATEGORIES_AND_TYPES,
                        hazardCategoriesAndTypes);
                valuesForIdentifiers.put(HazardConstants.SETTING_HAZARD_STATES,
                        new ArrayList<>(settings.getVisibleStatuses()));
                Set<String> visibleSites = getModel().getConfigurationManager()
                        .getSettingsValue(HazardConstants.SETTING_HAZARD_SITES,
                                settings);
                valuesForIdentifiers.put(HazardConstants.SETTING_HAZARD_SITES,
                        new ArrayList<>(visibleSites));

                getView().getColumnFiltersChanger()
                        .setStates(valuesForIdentifiers);
            }
        }

        /*
         * If the filters have changed, regenerate the tabular entities for the
         * events; if sorting has changed, re-sort the existing entities.
         */
        if (filtersChanged) {
            tabularEntityManager.recreateAllEntities();
        } else if (sortChanged) {
            tabularEntityManager.sortHazardEvents();
        }
    }

    /**
     * Respond to the current site changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void siteChanged(SiteChanged change) {
        getView().getSiteChanger().setState(null, change.getSiteIdentifier());
        tabularEntityManager.recreateAllEntities();
    }

    /**
     * Respond to the hazard alerts changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void hazardAlertsModified(HazardAlertsModified change) {
        getView()
                .setActiveCountdownTimers(getCountdownTimersFromActiveAlerts());
        tabularEntityManager
                .setActiveCountdownTimers(getCountdownTimersFromActiveAlerts());
    }

    /**
     * Respond to the selected event set changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionSelectedEventsModified(
            SessionSelectedEventsModified change) {

        /*
         * Ensure that the selection was not changed by the console before
         * updating the tree contents.
         */
        if (change.getOriginator() != UIOriginator.CONSOLE) {

            /*
             * Compile a map of event identifiers to the indices, if any,
             * indicating which historical versions of those events should have
             * their tabular entities replaced, in addition to the current
             * versions of those same events.
             */
            Map<String, Set<Integer>> historicalIndicesForEventIdentifiers = new HashMap<>(
                    change.getCurrentAndHistoricalEventIdentifiers().size(),
                    1.0f);
            for (Pair<String, Integer> identifier : change
                    .getCurrentAndHistoricalEventIdentifiers()) {
                Set<Integer> indices = historicalIndicesForEventIdentifiers
                        .get(identifier.getFirst());
                if (indices == null) {
                    indices = new HashSet<>();
                    historicalIndicesForEventIdentifiers
                            .put(identifier.getFirst(), indices);
                }
                if (identifier.getSecond() != null) {
                    indices.add(identifier.getSecond());
                }
            }

            /*
             * Replace each of the root entities and any associated historical
             * entities that need replacing.
             */
            for (Map.Entry<String, Set<Integer>> entry : historicalIndicesForEventIdentifiers
                    .entrySet()) {
                IHazardEventView event = getModel().getEventManager()
                        .getEventById(entry.getKey());
                if (event != null) {
                    tabularEntityManager.replaceEntitiesForEvent(event,
                            entry.getValue());
                } else {
                    tabularEntityManager.removeEntitiesForEvent(entry.getKey());
                }
            }
        }
    }

    /**
     * Respond to events being added.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventsAdded(SessionEventsAdded change) {
        for (IHazardEventView event : change.getEvents()) {
            tabularEntityManager.addEntitiesForEvent(event);
        }
    }

    /**
     * Respond to events being removed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventsRemoved(SessionEventsRemoved change) {
        for (IHazardEventView event : change.getEvents()) {
            tabularEntityManager.removeEntitiesForEvent(event);
        }
    }

    /**
     * Respond to an event being changed.
     * 
     * @param change
     *            Change that occcurred.
     */
    @Handler
    public void sessionEventModified(SessionEventModified change) {

        /*
         * Get the classes of the modifications that were made, and use them to
         * prune the map that has classes of interest to this method as keys.
         */
        Map<Class<? extends IEventModification>, Boolean> checkOriginatorFlagsForModificationClasses = new HashMap<>(
                CHECK_ORIGINATOR_FLAGS_FOR_EVENT_MODIFICATION_CLASSES);
        checkOriginatorFlagsForModificationClasses.keySet()
                .retainAll(change.getClassesOfModifications());

        /*
         * If the resulting pruned map is not empty, and either at least one of
         * the modifications does not require an originator check, or the
         * originator was not the console, refresh the root tabular entities
         * associated with this event.
         */
        if ((checkOriginatorFlagsForModificationClasses.isEmpty() == false)
                && (checkOriginatorFlagsForModificationClasses
                        .containsValue(Boolean.FALSE)
                        || (change.getOriginator() != UIOriginator.CONSOLE))) {
            tabularEntityManager.replaceRootEntityForEvent(change.getEvent());
        }
    }

    /**
     * Respond to an event's time range's boundaries changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventTimeRangeBoundariesModified(
            SessionEventsTimeRangeBoundariesModified change) {
        for (String eventIdentifier : change.getEventIdentifiers()) {
            tabularEntityManager.replaceRootEntityForEvent(
                    getModel().getEventManager().getEventById(eventIdentifier));
        }
    }

    /**
     * Respond to an event's history changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventHistoryModified(
            SessionEventHistoryModified change) {
        tabularEntityManager.updateChildEntityListForEvent(change.getEvent());
    }

    /**
     * Respond to one or more events' lock statuses changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventsLockStatusModified(
            SessionEventsLockStatusModified change) {
        for (String eventIdentifier : change.getEventIdentifiers()) {
            IHazardEventView event = getModel().getEventManager()
                    .getEventById(eventIdentifier);
            if (event != null) {
                tabularEntityManager.replaceRootEntityForEvent(event);
            }
        }
    }

    // Protected Methods

    @Override
    protected final void initialize(IConsoleView<?, ?, ?> view) {

        /*
         * Determine whether the time line navigation buttons should be in the
         * console toolbar, or below the console's table.
         */
        boolean temporalControlsInToolBar = true;
        StartUpConfig startUpConfig = getModel().getConfigurationManager()
                .getStartUpConfig();
        if (startUpConfig != null) {
            Console console = startUpConfig.getConsole();
            if (console != null) {
                String timeLineNavigation = console.getTimeLineNavigation();
                if (timeLineNavigation != null) {
                    temporalControlsInToolBar = !(timeLineNavigation.equals(
                            HazardConstants.START_UP_CONFIG_CONSOLE_TIMELINE_NAVIGATION_BELOW));
                }
            }
        }

        /*
         * Initialize the view.
         */
        ISessionTimeManager timeManager = getModel().getTimeManager();
        Field[] filterFields = getModel().getConfigurationManager()
                .getFilterConfig();
        List<Map<String, Object>> filterSpecifiers = new ArrayList<>(
                filterFields.length);
        try {
            for (Field field : filterFields) {
                Map<String, Object> map = JsonConverter
                        .fromJson(JsonConverter.toJson(field));
                filterSpecifiers.add(map);
            }
        } catch (IOException e) {
            statusHandler.error(
                    "Could not serialize filter configuration to JSON.", e);
        }
        view.initialize(this,
                new Date(timeManager.getSelectedTime().getLowerBound()),
                timeManager.getCurrentTime(),
                getModel().getConfigurationManager().getSettings()
                        .getDefaultTimeDisplayDuration(),
                (TimeResolution) getModel().getConfigurationManager()
                        .getSettingsValue(HazardConstants.TIME_RESOLUTION,
                                getModel().getConfigurationManager()
                                        .getSettings()),
                ImmutableList.copyOf(filterSpecifiers),
                getModel().getConfigurationManager().getSiteID(),
                getModel().getConfigurationManager().getSiteID(),
                ImmutableList.copyOf(
                        getModel().getConfigurationManager().getBackupSites()),
                temporalControlsInToolBar);

        /*
         * Register the various handlers with the view.
         */
        view.getTreeContentsChanger().setListStateChangeHandler(
                tabularEntityManager.getTreeContentsChangeHandler());
        view.getColumnsChanger().setStateChangeHandler(columnsChangeHandler);
        view.getColumnFiltersChanger()
                .setStateChangeHandler(columnFiltersChangeHandler);
        view.getTimeRangeChanger()
                .setStateChangeHandler(timeRangeChangeHandler);
        view.getSortInvoker()
                .setCommandInvocationHandler(sortInvocationHandler);
        view.setCommandInvocationHandler(commandInvocationHandler);
        view.setToggleChangeHandler(toggleStateChangeHandler);
        view.setVtecModeChangeHandler(vtecModeStateChangeHandler);
        view.getSiteChanger().setStateChangeHandler(siteStateChangeHandler);

        /*
         * Notify the entity manager of the view's existence.
         */
        tabularEntityManager.setView(view);

        /*
         * Update the active alerts.
         */
        view.setActiveCountdownTimers(getCountdownTimersFromActiveAlerts());
        tabularEntityManager
                .setActiveCountdownTimers(getCountdownTimersFromActiveAlerts());

    }

    @Override
    protected final void reinitialize(IConsoleView<?, ?, ?> view) {

        /*
         * No action.
         */
    }

    @Override
    protected void doDispose() {
        consoleHandler = null;
    }

    // Package-Private Methods

    /**
     * Get the list of context menu items that this presenter can contribute,
     * given the specified tabular entity identifier as the item chosen for the
     * context menu.
     * <p>
     * TODO: This method needs to be run from some new subclass of
     * {@link ICommandInvocationHandler} that returns a result. Currently this
     * is being called directly from the view, which is incorrect; only time
     * crunches prevent the implementation of the necessary changes.
     * Furthermore, this is used by the view to get necessary menu items, and it
     * needs to run in the main (worker) thread. This means that when a separate
     * thread is used in the future as a worker thread for presenters and the
     * model, {@link IRunnableAsynchronousScheduler} will need to be augmented
     * to include the ability to synchronously call a method that returns a
     * value. Currently, said interface only includes a method for scheduling
     * asynchronous executions of runnables that do not return anything.
     * </p>
     * 
     * @param identifier
     *            Identifier of the tabular entity that was chosen with the
     *            context menu invocation, or <code>null</code> if none was
     *            chosen.
     * @param persistedTimestamp
     *            Timestamp indicating when the tabular entity was persisted;
     *            may be <code>null</code>.
     * @param scheduler
     *            Runnable asynchronous scheduler used to execute context menu
     *            invoked actions on the appropriate thread.
     * @return List of context menu items.
     * @deprecated The method itself is not deprecated, but its visibility is;
     *             it must be invoked by the subclass of
     *             <code>ICommandInvocationHandler</code> mentioned in the to-do
     *             discussion.
     */
    @Deprecated
    List<IContributionItem> getContextMenuItems(String identifier,
            Date persistedTimestamp, IRunnableAsynchronousScheduler scheduler) {

        /*
         * If an event identifier was chosen, use that event as the current
         * event; otherwise, use no event.
         */
        getModel().getEventManager().setCurrentEvent(
                persistedTimestamp != null ? null : identifier);

        /*
         * Get the menu items and return them.
         */
        ContextMenuHelper helper = new ContextMenuHelper(getModel(), scheduler,
                this);
        return helper.getSelectedHazardManagementItems(UIOriginator.CONSOLE,
                new IContributionItemUpdater() {

                    @Override
                    public void handleContributionItemUpdate(
                            IContributionItem item, String text,
                            boolean enabled) {
                        getView().handleContributionItemUpdate(item, text,
                                enabled);
                    }
                });
    }

    /**
     * Get the list of elements that may be used to create review-product menu
     * items.
     * <p>
     * TODO: This method needs to be run from some new subclass of
     * {@link ICommandInvocationHandler} that returns a result. Currently this
     * is being called directly from the view, which is incorrect; only time
     * crunches prevent the implementation of the necessary changes.
     * Furthermore, this is used by the view to get necessary menu items, and it
     * needs to run in the main (worker) thread. This means that when a separate
     * thread is used in the future as a worker thread for presenters and the
     * model, {@link IRunnableAsynchronousScheduler} will need to be augmented
     * to include the ability to synchronously call a method that returns a
     * value. Currently, said interface only includes a method for scheduling
     * asynchronous executions of runnables that do not return anything.
     * </p>
     * 
     * @return List of elements that may be used to create review-product menu
     *         items, or <code>null</code> if there are no review-product menu
     *         items to be created.
     * @deprecated The method itself is not deprecated, but its visibility is;
     *             it must be invoked by the subclass of
     *             <code>ICommandInvocationHandler</code> mentioned in the to-do
     *             discussion.
     */
    @Deprecated
    List<List<ProductData>> getReviewMenuItems() {

        /*
         * Get the product data that is correctable, if any.
         */
        List<ProductData> correctableProductData = ProductDataUtil
                .retrieveCorrectableProductData(CAVEMode.getMode().toString(),
                        SimulatedTime.getSystemTime().getTime());
        if (correctableProductData.isEmpty()) {
            return null;
        }

        /*
         * Iterate through the correctable product data elements, adding each in
         * turn to a list of product data elements that share the same product
         * generator names and event identifiers.
         */
        Map<ReviewKey, List<ProductData>> map = new HashMap<>();
        for (ProductData productData : correctableProductData) {
            ReviewKey key = new ReviewKey(productData.getProductGeneratorName(),
                    productData.getEventIDs());
            List<ProductData> list = map.get(key);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(new ProductData(productData));
            map.put(key, list);
        }

        return new ArrayList<>(map.values());
    }

    // Private Methods

    /**
     * Get a map of event identifiers to countdown timers representing the
     * currently active alerts.
     * 
     * @return Map of event identifiers to countdown timers.
     */
    private ImmutableMap<String, CountdownTimer> getCountdownTimersFromActiveAlerts() {
        List<IHazardAlert> alerts = getModel().getAlertsManager()
                .getActiveAlerts();
        Map<String, CountdownTimer> countdownTimersForEventIdentifiers = new HashMap<>(
                alerts.size(), 1.0f);
        for (IHazardAlert alert : alerts) {
            if (alert instanceof HazardEventExpirationConsoleTimer == false) {
                continue;
            }
            HazardEventExpirationConsoleTimer consoleAlert = (HazardEventExpirationConsoleTimer) alert;
            countdownTimersForEventIdentifiers.put(consoleAlert.getEventID(),
                    new CountdownTimer(consoleAlert.getHazardExpiration(),
                            consoleAlert.getColor(), consoleAlert.isBold(),
                            consoleAlert.isItalic(),
                            consoleAlert.isBlinking()));
        }
        return ImmutableMap.copyOf(countdownTimersForEventIdentifiers);
    }

    /**
     * Create a list of sorts.
     */
    private List<Sort> createSorts() {

        /*
         * Iterate through the columns, adding a sort for each one that is a
         * sorting column. Columns do not have to be visible to be used for
         * sorting.
         */
        List<Sort> sorts = new ArrayList<>(2);
        for (Column column : getModel().getConfigurationManager().getSettings()
                .getColumns().values()) {
            int priority = column.getSortPriority();
            String sortDirection = column.getSortDir();
            if ((priority > 0) && (sortDirection != null)) {
                sorts.add(new Sort(column.getFieldName(),
                        (sortDirection
                                .equals(SETTING_COLUMN_SORT_DIRECTION_ASCENDING)
                                        ? SortDirection.ASCENDING
                                        : SortDirection.DESCENDING),
                        priority));
            }
        }

        /*
         * Order the sorts by priority, so that the lowest-numbered one happens
         * first when performing a row sort.
         */
        Collections.sort(sorts);

        return sorts;
    }

    /**
     * Handle the change of the sorting algorithms due to the arrival of a new
     * sort.
     * 
     * @param newSort
     *            New sort to be added to the sorting algorithms, or to replace
     *            one of the sorts within said algorithms.
     */
    private void handleSortChange(Sort newSort) {

        /*
         * Change the list of sorts if it needs changing.
         */
        boolean changed = true;
        if (newSort.getPriority() == 1) {
            if (sorts.isEmpty()) {
                sorts.add(newSort);
            } else {
                if (sorts.get(0).equals(newSort)) {
                    changed = false;
                } else {
                    sorts.set(0, newSort);
                }
            }
        } else {
            if (sorts.isEmpty()) {
                sorts.add(newSort);
                sorts.add(newSort);
            } else if (sorts.size() == 1) {
                sorts.add(newSort);
            } else {
                if (sorts.get(1).equals(newSort)) {
                    changed = false;
                } else {
                    sorts.set(1, newSort);
                }
            }
        }

        /*
         * If the sorts changed, notify the view, and update the current
         * settings' columns and sort the hazard events.
         */
        if (changed) {

            getView().setSorts(ImmutableList.copyOf(sorts));

            ObservedSettings settings = getModel().getConfigurationManager()
                    .getSettings();
            Map<String, Column> columnsForNames = settings.getColumns();
            Map<String, Column> columnsForIdentifiers = new HashMap<>(
                    columnsForNames.size(), 1.0f);
            Map<String, Column> newColumnsForNames = new HashMap<>(
                    columnsForNames.size(), 1.0f);

            for (Map.Entry<String, Column> entry : columnsForNames.entrySet()) {
                Column newColumn = new Column(entry.getValue());
                newColumn.setSortPriority(SETTING_COLUMN_SORT_PRIORITY_NONE);
                newColumn.setSortDir(SETTING_COLUMN_SORT_DIRECTION_NONE);
                columnsForIdentifiers.put(newColumn.getFieldName(), newColumn);
                newColumnsForNames.put(entry.getKey(), newColumn);
            }

            for (Sort sort : sorts) {
                Column column = columnsForIdentifiers
                        .get(sort.getAttributeIdentifier());
                column.setSortPriority(sort.getPriority());
                column.setSortDir(
                        sort.getSortDirection() == SortDirection.ASCENDING
                                ? SETTING_COLUMN_SORT_DIRECTION_ASCENDING
                                : SETTING_COLUMN_SORT_DIRECTION_DESCENDING);
            }

            settings.setColumns(newColumnsForNames, UIOriginator.CONSOLE);

            tabularEntityManager.sortHazardEvents();
        }
    }

    /**
     * Handle the toggling of the show history lists flag.
     * 
     * @param value
     *            New value of the flag.
     */
    private void handleShowHistoryListToggle(boolean value) {
        tabularEntityManager.setShowHistoryList(value);
    }

    /**
     * Export Hazard Services localization for the current site identifier.
     */
    private void exportApplicationBackupSiteData() {
        getModel().exportApplicationSiteData(
                getModel().getConfigurationManager().getSiteID(), true, false,
                false);
    }

    /**
     * Export Hazard Services product edits for the current site identifier.
     */
    private void exportApplicationBackupSiteProductEdits() {
        getModel().exportApplicationSiteData(
                getModel().getConfigurationManager().getSiteID(), false, true,
                true);
    }

    /**
     * Examine all hazard events looking for potential conflicts.
     */
    private void checkForConflicts() {
        try {
            Map<IReadableHazardEvent, Map<IReadableHazardEvent, Collection<String>>> conflictMap = getModel()
                    .getEventManager().getAllConflictingEvents();
            if (conflictMap.isEmpty() == false) {
                consoleHandler.showUserConflictingHazardsWarning(conflictMap);
            }
        } catch (HazardEventServiceException e) {
            statusHandler.error("Could not check for hazard conflicts.", e);
        }
    }

    /**
     * Handle the closing of the console.
     */
    private void handleConsoleClosed() {
        if (consoleHandler != null) {
            consoleHandler.consoleDisposed();
        }
    }

    /**
     * Throw an unsupported operation exception for attempts to change multiple
     * states that are not appropriate.
     * 
     * @param description
     *            Description of the element for which an attempt to change
     *            multiple states was made.
     * @throws UnsupportedOperationException
     *             Whenever this method is called.
     */
    private void handleUnsupportedOperationAttempt(String description)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "cannot change multiple states associated with console view "
                        + description);
    }
}
