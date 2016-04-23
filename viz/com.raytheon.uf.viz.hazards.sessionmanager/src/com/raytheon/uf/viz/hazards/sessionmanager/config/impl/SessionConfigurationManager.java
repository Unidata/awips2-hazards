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
package com.raytheon.uf.viz.hazards.sessionmanager.config.impl;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.EVENT_ID_DISPLAY_TYPE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.MAP_CENTER;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_FIELD_TYPE_GROUP;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_CATEGORIES_AND_TYPES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_POSSIBLE_SITES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_SITES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_STATES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_VISIBLE_COLUMNS;
import gov.noaa.gsd.viz.megawidgets.GroupSpecifier;
import gov.noaa.gsd.viz.megawidgets.IControlSpecifier;
import gov.noaa.gsd.viz.megawidgets.ISideEffectsApplier;
import gov.noaa.gsd.viz.megawidgets.ISpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.megawidgets.sideeffects.PythonSideEffectsApplier;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.eclipse.swt.widgets.Display;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardEventFirstClassAttribute;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.configuration.ConfigLoader;
import com.raytheon.uf.common.hazards.configuration.HazardsConfigurationConstants;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.localization.FileUpdatedMessage;
import com.raytheon.uf.common.localization.ILocalizationFileObserver;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.python.concurrent.IPythonExecutor;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.python.concurrent.PythonJobCoordinator;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.style.MatchCriteria;
import com.raytheon.uf.common.style.ParamLevelMatchCriteria;
import com.raytheon.uf.common.style.StyleException;
import com.raytheon.uf.common.style.StyleManager;
import com.raytheon.uf.common.style.StyleRule;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
import com.raytheon.uf.viz.core.jobs.JobPool;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl.AllHazardsFilterStrategy;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.impl.HazardEventExpirationAlertStrategy;
import com.raytheon.uf.viz.hazards.sessionmanager.config.HazardEventMetadata;
import com.raytheon.uf.viz.hazards.sessionmanager.config.IEventModifyingScriptJobListener;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ModifiedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsLoaded;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardAlertsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardCategories;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardMetaData;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Choice;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.EventDrivenToolEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.EventDrivenTools;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.HazardInfoConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.MapCenter;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Page;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.SettingsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.uf.viz.hazards.sessionmanager.styles.HazardStyle;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.viz.core.mode.CAVEMode;

/**
 * Implementation of ISessionConfigurationManager with asynchronous config file
 * loading.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2013  1257      bsteffen    Initial creation
 * Aug 01, 2013  1325      daniel.s.schaffer@noaa.gov     Added support for alerting
 * Nov 14, 2013  1472      bkowal      Renamed hazard subtype to subType
 * Nov 23, 2013  1462      blawrenc    Changed default polygon border width from 1 to 3.
 * Nov 29, 2013  2380      daniel.s.schaffer@noaa.gov Minor cleanup
 * Nov 30, 2013            blawrenc    Added hazard color retrieval from style rules.
 * Feb 24, 2014  2161      Chris.Golden Added VTECutilities to Python include path.
 * Apr 28, 2014  3556      bkowal      Updated to use the new hazards common 
 *                                     configuration plugin.
 * Apr 29, 2014  2925      Chris.Golden Changed to support loading of class-based metadata
 *                                      for HID dynamically, instead of all at once at
 *                                      startup.
 * May 15, 2014  2925      Chris.Golden Added missing Python path for Jep that was messing
 *                                      up H.S. startup on some machines.
 * May 15, 2014  2925      Chris.Golden Added supplying of current time provider to
 *                                      megawidget specifier manager, and added some
 *                                      optimizations for getting megawidget specifier
 *                                      managers and hazard categories. Also removed
 *                                      hazard info options fetcher.
 * Jul 03, 2014  4084      Chris.Golden Added displaying of exceptions for errors while
 *                                      trying to retrieve hazard event metadata.
 * Jul 03, 2014  3512      Chris.Golden Added ability to fetch duration choices for hazard
 *                                      events, and also default durations.
 * Aug 15, 2014  4243      Chris.Golden Changed to look for any interdependency script's
 *                                      entry point in a hazard metadata file, and to send
 *                                      the file onto the Python side effects applier if
 *                                      one is found. Also added ability to run arbitrary
 *                                      event-modifying scripts when prompted to do so.
 * Aug 28, 2014  3768      Robert.Blum  Modified the deleteSetting() function to correclty
 *                                      remove the settings.
 * Sep 04, 2014  4560      Chris.Golden Added code to find metadata-reload-triggering
 *                                      megawidgets.
 * Sep 16, 2014  4753      Chris.Golden Changed event script to include mutable properties.
 * Sep 23, 2014  3790      Robert.Blum  Added a file observer to the Hazard Metadata directory
 *                                      to reload the localization files when needed so that 
 *                                      cave does not need to be restarted.
 * Oct 20, 2014  4818      Chris.Golden Added wrapping of metadata megawidget specifiers in
 *                                      a scrollable megawidget.
 * Dec 05, 2014  4124      Chris.Golden Changed to work with parameterized interface that it
 *                                      implements, and to use ObservedSetttings.
 * Jan 21, 2014  3626      Chris.Golden Added method to retrieve hazard-type-first recommender
 *                                      based upon hazard type.
 * Jan 29, 2015  4375      Dan Schaffer Console initiation of RVS product generation
 * Feb 01, 2015  2331      Chris.Golden Added methods to determine the value of flags
 *                                      indicating the constraints that a hazard event type
 *                                      puts on start and end time editability.
 * Feb 17, 2015  5071      Robert.Blum  Reverted some changes done under 3790.
 * Feb 17, 2015  3847      Chris.Golden Added edit-rise-crest-fall metadata trigger.
 * Feb 23, 2015  3618      Chris.Golden Changed settings filter megawidget definitions to
 *                                      use possible sites as backing choices for visible
 *                                      sites, which allows possible sites to be localized
 *                                      (and specified per setting). Also changed possible
 *                                      columns to be taken from the settings as well, thus
 *                                      eliminating another non-localized list from default
 *                                      config (which is important, since the possible
 *                                      columns for a setting are defined within the setting
 *                                      itself, and are not universal).
 * Mar 06, 2015  3850      Chris.Golden Added ability to determine if a hazard type
 *                                      requires a point identifier, and which hazard types
 *                                      can be used to replace a particular hazard event.
 * Mar 25, 2015  7102      Chris.Golden Fixed bug that caused null pointer exceptions when
 *                                      attempting to compile the duration choices lists.
 * Oct 13, 2015 12494      Chris Golden Reworked to allow hazard types to include
 *                                      only phenomenon (i.e. no significance) where
 *                                      appropriate.
 * Nov 10, 2015 12762      Chris.Golden Added recommender running in response to
 *                                      hazard event metadata changes, as well as the
 *                                      use of the new recommender manager.
 * Aug 31, 2015 9757       Robert.Blum  Added Observers to config files so overrides are
 *                                      picked up.
 * Sep 28, 2015 10302,8167 hansen       Added "getSettingsValue"
 * Mar 04, 2016 15933      Chris.Golden Added ability to run multiple recommenders in
 *                                      sequence in response to a time interval trigger,
 *                                      instead of just one recommender.
 * Mar 22, 2016 15676      Chris.Golden Changed line styles returned to make more sense
 *                                      (dotted was referred to as dashed).
 * Apr 01, 2016 16225      Chris.Golden Added ability to cancel tasks that are scheduled
 *                                      to run at regular intervals.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionConfigurationManager implements
        ISessionConfigurationManager<ObservedSettings> {

    /**
     * Empty map standing in for an environment map, which will be needed in the
     * future to specify information not specific to a hazard event that affects
     * the generation of metadata for an event.
     */
    private static final Map<String, Serializable> ENVIRONMENT = new HashMap<>();

    /**
     * Metadata group megawidget type.
     */
    private static final String METADATA_GROUP_TYPE = "Group";

    /**
     * Metadata group megawidget label.
     */
    private static final String METADATA_GROUP_TEXT = "Details";

    /**
     * Specifier parameters for the metadata group megawidget, used to wrap
     * metadata megawidgets.
     */
    private static final ImmutableMap<String, Object> METADATA_GROUP_SPECIFIER_PARAMETERS;
    static {
        Map<String, Object> map = new HashMap<>();
        map.put(GroupSpecifier.MEGAWIDGET_TYPE, METADATA_GROUP_TYPE);
        map.put(GroupSpecifier.MEGAWIDGET_LABEL, METADATA_GROUP_TEXT);
        map.put(GroupSpecifier.LEFT_MARGIN, 10);
        map.put(GroupSpecifier.RIGHT_MARGIN, 10);
        map.put(GroupSpecifier.TOP_MARGIN, 2);
        map.put(GroupSpecifier.BOTTOM_MARGIN, 8);
        map.put(GroupSpecifier.EXPAND_HORIZONTALLY, true);
        map.put(GroupSpecifier.EXPAND_VERTICALLY, true);
        METADATA_GROUP_SPECIFIER_PARAMETERS = ImmutableMap.copyOf(map);
    }

    static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SessionConfigurationManager.class);

    private static final Color WHITE = new Color(1.0f, 1.0f, 1.0f);

    private static final MegawidgetSpecifierManager EMPTY_MEGAWIDGET_SPECIFIER_MANAGER;
    static {
        MegawidgetSpecifierManager manager = null;
        try {
            manager = new MegawidgetSpecifierManager(
                    Collections.<Map<String, Object>> emptyList(),
                    ISpecifier.class);
        } catch (Exception e) {
            statusHandler.error(
                    "unexpected error while creating empty megawidget specifier manager: "
                            + e, e);
        }
        EMPTY_MEGAWIDGET_SPECIFIER_MANAGER = manager;
    }

    private static final HazardEventMetadata EMPTY_HAZARD_EVENT_METADATA = new HazardEventMetadata(
            EMPTY_MEGAWIDGET_SPECIFIER_MANAGER,
            Collections.<String> emptySet(),
            Collections.<String, String> emptyMap(),
            Collections.<String> emptySet(), null, null);

    private ISessionNotificationSender notificationSender;

    private final JobPool loaderPool = new JobPool(
            "Loading Hazard Services Config", 1);

    private ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager;

    private IPathManager pathManager;

    private ISessionTimeManager timeManager;

    private List<ConfigLoader<Settings>> allSettings;

    private ConfigLoader<StartUpConfig> startUpConfig;

    private ConfigLoader<HazardCategories> hazardCategories;

    private ConfigLoader<HazardMetaData> hazardMetaData;

    private ConfigLoader<ProductGeneratorTable> pgenTable;

    private ConfigLoader<HazardTypes> hazardTypes;

    private ConfigLoader<HazardAlertsConfig> alertsConfig;

    private ConfigLoader<SettingsConfig[]> settingsConfig;

    private ConfigLoader<EventDrivenTools> eventDrivenTools;

    private ObservedSettings settings;

    private String siteId;

    /**
     * Python job coordinator that handles both metadata fetching scripts and
     * event modifying scripts.
     */
    private final PythonJobCoordinator<ContextSwitchingPythonEval> coordinator = PythonJobCoordinator
            .newInstance(new ConfigScriptFactory());

    private Map<String, ImmutableList<String>> durationChoicesForHazardTypes;

    private Map<String, ImmutableList<String>> replaceByTypesForHazardTypes;

    private Map<String, String> typeFirstRecommendersForHazardTypes;

    private Map<String, Boolean> startTimeIsCurrentTimeForHazardTypes;

    private Map<String, Boolean> allowTimeExpandForHazardTypes;

    private Map<String, Boolean> allowTimeShrinkForHazardTypes;

    private Map<String, Map<HazardEventFirstClassAttribute, String>> recommendersForTriggersForHazardTypes;

    private Set<String> typesRequiringPointIds;

    private Map<Runnable, Integer> minuteIntervalsForEventDrivenToolExecutors;

    private boolean runRecommendersAtRegularIntervals = false;

    SessionConfigurationManager() {

    }

    public SessionConfigurationManager(
            ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager,
            IPathManager pathManager, ISessionTimeManager timeManager,
            ISessionNotificationSender notificationSender) {
        this.sessionManager = sessionManager;
        this.pathManager = pathManager;
        this.timeManager = timeManager;
        this.notificationSender = notificationSender;

        LocalizationContext commonStaticBase = pathManager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        LocalizationFile settingsDir = pathManager.getLocalizationFile(
                commonStaticBase, "hazardServices/settings/");
        settingsDir
                .addFileUpdatedObserver(new SettingsDirectoryUpdateObserver());

        loadAllSettings();

        // Add observer to base file
        LocalizationFile file = pathManager.getLocalizationFile(
                commonStaticBase,
                HazardsConfigurationConstants.START_UP_CONFIG_PY);
        file.addFileUpdatedObserver(new StartUpConfigObserver());

        file = pathManager
                .getStaticLocalizationFile(HazardsConfigurationConstants.START_UP_CONFIG_PY);
        startUpConfig = new ConfigLoader<StartUpConfig>(file,
                StartUpConfig.class);
        loaderPool.schedule(startUpConfig);

        // Add observer to base file
        file = pathManager.getLocalizationFile(commonStaticBase,
                HazardsConfigurationConstants.HAZARD_CATEGORIES_PY);
        file.addFileUpdatedObserver(new HazardCategoriesObserver());

        file = pathManager
                .getStaticLocalizationFile(HazardsConfigurationConstants.HAZARD_CATEGORIES_PY);
        hazardCategories = new ConfigLoader<HazardCategories>(file,
                HazardCategories.class);
        loaderPool.schedule(hazardCategories);

        // THe HazardMetaData needs an include path that includes Hazard
        // Categories.
        StringBuilder metadataIncludes = new StringBuilder();
        metadataIncludes.append(file.getFile().getParent());
        file = pathManager
                .getStaticLocalizationFile(HazardsConfigurationConstants.VTEC_CONSTANTS_PY);
        metadataIncludes.append(":").append(file.getFile().getParent());

        // Add observer to base file
        file = pathManager.getLocalizationFile(commonStaticBase,
                HazardsConfigurationConstants.HAZARD_METADATA_PY);
        file.addFileUpdatedObserver(new HazardMetaDataObserver());

        file = pathManager
                .getStaticLocalizationFile(HazardsConfigurationConstants.HAZARD_METADATA_PY);
        metadataIncludes.append(":").append(file.getFile().getParent());
        for (LocalizationFile f : pathManager.listStaticFiles(
                "hazardServices/hazardMetaData/", new String[] { ".py" },
                false, true)) {
            // Force download the file so python has it
            f.getFile();
        }
        hazardMetaData = new ConfigLoader<HazardMetaData>(file,
                HazardMetaData.class, null, metadataIncludes.toString());
        loaderPool.schedule(hazardMetaData);

        // Add observer to base file
        file = pathManager.getLocalizationFile(commonStaticBase,
                HazardsConfigurationConstants.HAZARD_TYPES_PY);
        file.addFileUpdatedObserver(new HazardTypesObserver());

        file = pathManager
                .getStaticLocalizationFile(HazardsConfigurationConstants.HAZARD_TYPES_PY);
        hazardTypes = new ConfigLoader<HazardTypes>(file, HazardTypes.class);
        loaderPool.schedule(hazardTypes);

        /*
         * Load any event-driven tool specifications from configuration, and
         * create the executors for them, saving them along with their minute
         * intervals. Then schedule them to run at the specified intervals if
         * appropriate.
         */
        file = pathManager
                .getStaticLocalizationFile(HazardsConfigurationConstants.EVENT_DRIVEN_TOOLS_PY);
        eventDrivenTools = new ConfigLoader<EventDrivenTools>(file,
                EventDrivenTools.class);
        loaderPool.schedule(eventDrivenTools);
        EventDrivenTools tools = eventDrivenTools.getConfig();
        minuteIntervalsForEventDrivenToolExecutors = new IdentityHashMap<>(
                tools.size());
        for (final EventDrivenToolEntry entry : tools) {
            minuteIntervalsForEventDrivenToolExecutors.put(new Runnable() {

                @Override
                public void run() {
                    List<String> identifiers = entry.getIdentifiers();
                    Set<String> setOfIdentifiers = new HashSet<>(identifiers);
                    if (setOfIdentifiers.size() != entry.getIdentifiers()
                            .size()) {
                        statusHandler
                                .warn("List of recommenders to be executed sequentially ("
                                        + Joiner.on(", ").join(identifiers)
                                        + ") at regular time intervals cannot include "
                                        + "any duplicate entries; these recommenders "
                                        + "will not be executed.");
                    } else {
                        SessionConfigurationManager.this.sessionManager
                                .runTools(entry.getType(), entry
                                        .getIdentifiers(),
                                        RecommenderExecutionContext
                                                .getTimeIntervalContext());
                    }
                }
            }, entry.getIntervalMinutes());
        }
        setEventDrivenToolRunningEnabled(true);

        // Add observer to base file
        file = pathManager.getLocalizationFile(commonStaticBase,
                HazardsConfigurationConstants.ALERTS_CONFIG_PATH);
        file.addFileUpdatedObserver(new HazardAlertsConfigObserver());

        file = pathManager
                .getStaticLocalizationFile(HazardsConfigurationConstants.ALERTS_CONFIG_PATH);
        alertsConfig = new ConfigLoader<HazardAlertsConfig>(file,
                HazardAlertsConfig.class);
        loaderPool.schedule(alertsConfig);

        // Add observer to base file
        file = pathManager.getLocalizationFile(commonStaticBase,
                HazardsConfigurationConstants.PRODUCT_GENERATOR_TABLE_PY);
        file.addFileUpdatedObserver(new ProductGeneratorTableObserver());

        file = pathManager
                .getStaticLocalizationFile(HazardsConfigurationConstants.PRODUCT_GENERATOR_TABLE_PY);
        pgenTable = new ConfigLoader<ProductGeneratorTable>(file,
                ProductGeneratorTable.class);
        loaderPool.schedule(pgenTable);

        // Add observer to base file
        file = pathManager.getLocalizationFile(commonStaticBase,
                HazardsConfigurationConstants.DEFAULT_CONFIG_PY);
        file.addFileUpdatedObserver(new DefaultConfigObserver());

        file = pathManager
                .getStaticLocalizationFile(HazardsConfigurationConstants.DEFAULT_CONFIG_PY);
        settingsConfig = new ConfigLoader<SettingsConfig[]>(file,
                SettingsConfig[].class, "viewConfig");
        loaderPool.schedule(settingsConfig);
    }

    protected void loadAllSettings() {
        Settings previousPersisted = null;
        if (settings != null) {
            for (ConfigLoader<Settings> settingsConfig : allSettings) {
                Settings s = settingsConfig.getConfig();
                if (s.getSettingsID().equals(settings.getSettingsID())) {
                    previousPersisted = s;
                    break;
                }
            }
        }
        LocalizationFile[] files = pathManager
                .listStaticFiles("hazardServices/settings/",
                        new String[] { ".py" }, false, true);
        List<ConfigLoader<Settings>> allSettings = new ArrayList<ConfigLoader<Settings>>();
        for (LocalizationFile file : files) {
            ConfigLoader<Settings> loader = new ConfigLoader<Settings>(file,
                    Settings.class);
            if (file.getFile().length() != 0) {
                allSettings.add(loader);
                loaderPool.schedule(loader);
                if (previousPersisted != null) {
                    try {
                        String fileName = file.getFile(false).getName();
                        if (fileName.startsWith(previousPersisted
                                .getSettingsID() + ".")) {
                            Settings s = loader.getConfig();
                            if (s.getSettingsID().equals(
                                    previousPersisted.getSettingsID())) {
                                settings.applyPersistedChanges(
                                        previousPersisted, s);
                                previousPersisted = null;
                            }
                        }
                    } catch (LocalizationException e) {
                        statusHandler.handle(Priority.PROBLEM,
                                e.getLocalizedMessage(), e);
                    }
                }
            }
        }
        this.allSettings = allSettings;
    }

    @Override
    public void changeSettings(String settingsId, IOriginator originator) {
        for (ConfigLoader<Settings> settingsConfig : allSettings) {
            Settings s = settingsConfig.getConfig();
            if (s.getSettingsID().equals(settingsId)) {
                if (settings == null) {
                    settings = new ObservedSettings(this, s);
                    settingsChanged(new SettingsLoaded(this, originator));
                } else {
                    settings.apply(s, originator);
                }
                break;
            }
        }
    }

    @Override
    /** 
     * Retrieve a clone of the current settings instance.
     * Note: Settings must be changed (saved) before modifications persist. 
     * @return A clone copy of the currently set Settings instance
     */
    public ObservedSettings getSettings() {
        if (settings != null) {
            return settings;
        } else if (!allSettings.isEmpty()) {
            this.settings = new ObservedSettings(this, allSettings.get(0)
                    .getConfig());
            settingsChanged(new SettingsLoaded(this, Originator.OTHER));
        }
        return settings;
    }

    @Override
    public void saveSettings() {
        LocalizationFile f = getUserSettings();
        StringBuilder contents = new StringBuilder();
        contents.append(settings.getSettingsID());
        contents.append(" = ");
        ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializationConfig().setSerializationInclusion(
                Inclusion.NON_NULL);
        try {
            contents.append(mapper.defaultPrettyPrintingWriter()
                    .writeValueAsString(settings));
            String contentsAsString = contents.toString();
            contentsAsString = contentsAsString.replaceAll("true", "True")
                    .replaceAll("false", "False");
            f.write(contentsAsString.getBytes());
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, "Unable to save settings.",
                    e);
        }
        if (allSettings.contains(settings) == false) {
            allSettings.add(new ConfigLoader<>(f, Settings.class));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.viz.hazards.sessionmanager.config.
     * ISessionConfigurationManager#deleteSettings()
     */
    @Override
    public void deleteSettings() {
        ISettings settings = getSettings();
        Map<LocalizationLevel, LocalizationFile> map = pathManager
                .getTieredLocalizationFile(LocalizationType.COMMON_STATIC,
                        "hazardServices/settings/" + settings.getSettingsID()
                                + ".py");
        // since we can't delete base settings, we will write them as empty so
        // they don't show up anywhere
        try {
            if (map.containsKey(LocalizationLevel.USER) && map.size() == 1) {
                map.get(LocalizationLevel.USER).delete();
            } else {
                getUserSettings().write("".getBytes());
            }
            Iterator<ConfigLoader<Settings>> iter = allSettings.iterator();
            while (iter.hasNext()) {
                Settings available = iter.next().getConfig();
                String id = available.getSettingsID();
                if (id.equals(settings.getSettingsID())) {
                    iter.remove();
                }
            }
        } catch (LocalizationException e) {
            statusHandler.handle(Priority.PROBLEM, "Unable to delete "
                    + settings.getDisplayName() + " setting.", e);
        }
    }

    private LocalizationFile getUserSettings() {
        LocalizationContext context = pathManager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.USER);
        ISettings settings = getSettings();
        return pathManager.getLocalizationFile(context,
                "hazardServices/settings/" + settings.getSettingsID() + ".py");
    }

    @Override
    public String getSiteID() {
        return siteId;
    }

    @Override
    public void setSiteID(String siteID) {
        this.siteId = siteID;
    }

    @Override
    public List<Settings> getAvailableSettings() {
        List<Settings> result = new ArrayList<Settings>(allSettings.size());
        for (ConfigLoader<Settings> settings : allSettings) {
            Settings setting = settings.getConfig();
            /*
             * Ensure that display name is not empty before including the
             * setting.
             */
            if ((setting.getDisplayName() == null)
                    || setting.getDisplayName().isEmpty()) {
                continue;
            }
            result.add(setting);
        }
        return result;
    }

    @Override
    public StartUpConfig getStartUpConfig() {
        return startUpConfig.getConfig();
    }

    @Override
    public HazardInfoConfig getHazardInfoConfig() {
        HazardInfoConfig config = new HazardInfoConfig();
        config.setDefaultCategory(getSettings().getDefaultCategory());
        List<Choice> categories = new ArrayList<Choice>();
        for (Entry<String, String[][]> entry : hazardCategories.getConfig()
                .entrySet()) {
            Choice cat = new Choice();

            cat.setDisplayString(entry.getKey());
            cat.setIdentifier(entry.getKey());
            List<Choice> children = new ArrayList<Choice>();
            for (String[] info : entry.getValue()) {
                String significance = null, subType = null;
                if (info.length > 1) {
                    significance = info[1];
                }
                if (info.length > 2) {
                    subType = info[2];
                }
                Choice child = new Choice();
                String headline = getHeadline(HazardEventUtilities
                        .getHazardType(info[0], significance, subType));
                StringBuilder id = new StringBuilder();
                for (String str : info) {
                    if (str != info[0]) {
                        id.append(".");
                    }
                    id.append(str);
                }
                child.setIdentifier(id.toString());
                if (headline == null) {
                    child.setDisplayString(child.getIdentifier());
                } else {
                    child.setDisplayString(id + " (" + headline + ")");
                }
                children.add(child);
            }
            cat.setChildren(children);
            categories.add(cat);
        }
        config.setHazardCategories(categories);
        return config;
    }

    @SuppressWarnings("unchecked")
    @Override
    public HazardEventMetadata getMetadataForHazardEvent(
            IHazardEvent hazardEvent) {

        /*
         * Get the metadata, which is a map with at least one entry holding the
         * list of megawidget specifiers that applies, as well as an optional
         * entry for a map of event modifier identifiers to script function
         * names. For now, just pass an empty environment map.
         * 
         * TODO: Substitute an actual map of environmental parameters for the
         * empty placeholder.
         */
        IPythonExecutor<ContextSwitchingPythonEval, Map<String, Object>> executor = new MetaDataScriptExecutor(
                hazardEvent, ENVIRONMENT);
        Map<String, Object> result = null;
        try {
            result = coordinator.submitSyncJob(executor);
        } catch (Exception e) {
            statusHandler.error("Error executing metadata-fetching job.", e);
            return EMPTY_HAZARD_EVENT_METADATA;
        }

        /*
         * Build a megawidget specifier manager out of the metadata, as well as
         * getting a set of metadata-reload-triggering metadata keys, and a map
         * of recommender-running metadata keys to their associated
         * recommenders. If the file that produced the metadata has an
         * apply-interdependencies entry point, create a side effects applier
         * for it. If it includes a map of event modifiers to the names of
         * scripts that are to be run, remember these.
         */
        ISideEffectsApplier sideEffectsApplier = null;
        File scriptFile = null;
        Map<String, String> eventModifyingFunctionNamesForIdentifiers = null;
        if (result.containsKey(HazardConstants.FILE_PATH_KEY)) {
            scriptFile = PathManagerFactory
                    .getPathManager()
                    .getStaticLocalizationFile(
                            (String) result.get(HazardConstants.FILE_PATH_KEY))
                    .getFile();
            if (PythonSideEffectsApplier
                    .containsSideEffectsEntryPointFunction(scriptFile)) {
                sideEffectsApplier = new PythonSideEffectsApplier(scriptFile);
            }
            if (result.containsKey(HazardConstants.EVENT_MODIFIERS_KEY)) {
                eventModifyingFunctionNamesForIdentifiers = (Map<String, String>) result
                        .get(HazardConstants.EVENT_MODIFIERS_KEY);
            }
        }
        List<Map<String, Object>> specifiersList = (List<Map<String, Object>>) result
                .get(HazardConstants.METADATA_KEY);
        if (specifiersList.isEmpty()) {
            return (eventModifyingFunctionNamesForIdentifiers == null ? EMPTY_HAZARD_EVENT_METADATA
                    : new HazardEventMetadata(
                            EMPTY_MEGAWIDGET_SPECIFIER_MANAGER,
                            Collections.<String> emptySet(),
                            Collections.<String, String> emptyMap(),
                            Collections.<String> emptySet(), scriptFile,
                            eventModifyingFunctionNamesForIdentifiers));
        }
        specifiersList = MegawidgetSpecifierManager
                .makeRawSpecifiersScrollable(specifiersList,
                        METADATA_GROUP_SPECIFIER_PARAMETERS);

        Set<String> refreshTriggeringMetadataKeys = getMegawidgetIdentifiersWithParameter(
                specifiersList, HazardConstants.METADATA_RELOAD_TRIGGER);
        Map<String, String> recommendersTriggeredForMetadataKeys = getValuesForMegawidgetIdentifiersWithParameter(
                specifiersList, HazardConstants.RECOMMENDER_RUN_TRIGGER);
        Set<String> editRiseCrestFallMetadataKeys = getMegawidgetIdentifiersWithParameter(
                specifiersList, HazardConstants.METADATA_EDIT_RISE_CREST_FALL);

        try {
            return new HazardEventMetadata(new MegawidgetSpecifierManager(
                    specifiersList, IControlSpecifier.class,
                    timeManager.getCurrentTimeProvider(), sideEffectsApplier),
                    refreshTriggeringMetadataKeys,
                    recommendersTriggeredForMetadataKeys,
                    editRiseCrestFallMetadataKeys, scriptFile,
                    eventModifyingFunctionNamesForIdentifiers);
        } catch (MegawidgetSpecificationException e) {
            statusHandler.error("Could not get hazard metadata for event ID = "
                    + hazardEvent.getEventID() + ":" + e, e);
            return EMPTY_HAZARD_EVENT_METADATA;
        }
    }

    /**
     * Get the set of megawidget identifiers from the specified list, which may
     * contain raw specifiers and their descendants, of any megawidget
     * specifiers that include the specified parameter name.
     * 
     * @param list
     *            List to be checked.
     * @param parameterName
     *            Parameter name for which to search.
     * @return Set of megawidget identifiers that contain the specified
     *         parameter.
     */
    private Set<String> getMegawidgetIdentifiersWithParameter(List<?> list,
            String parameterName) {
        Map<String, Boolean> valuesForTriggerIdentifiers = new HashMap<>();
        addMegawidgetIdentifiersIncludingParameterToMap(list, parameterName,
                valuesForTriggerIdentifiers);
        return valuesForTriggerIdentifiers.keySet();
    }

    /**
     * Get the map of megawidget identifiers to associated values from the
     * specified list, which may contain raw specifiers and their descendants,
     * of any megawidget specifiers that include the specified parameter name.
     * 
     * @param list
     *            List to be checked.
     * @param parameterName
     *            Parameter name for which to search.
     * @return Map of megawidget identifiers that contain the specified
     *         parameter to the corresponding parameter values.
     */
    private Map<String, String> getValuesForMegawidgetIdentifiersWithParameter(
            List<?> list, String parameterName) {
        Map<String, String> valuesForTriggerIdentifiers = new HashMap<>();
        addMegawidgetIdentifiersIncludingParameterToMap(list, parameterName,
                valuesForTriggerIdentifiers);
        return valuesForTriggerIdentifiers;
    }

    /**
     * Find any raw megawidget specifiers in the specified object, which may be
     * a list of some sort (the items within which must be checked recursively);
     * a map of some sort (in which case it itself may be a raw specifier,
     * and/or its values must be checked recursively), or a primitive (which
     * never has any raw specifiers in it), and add any found specifiers that
     * include the given parameter name to the specified map.
     * 
     * @param object
     *            Object to be checked.
     * @param parameterName
     *            Parameter name for which to search.
     * @param valuesForTriggerIdentifiers
     *            Map holding megawidget identifiers that qualify paired with
     *            their associated parameter values.
     */
    @SuppressWarnings("unchecked")
    private <V> void addMegawidgetIdentifiersIncludingParameterToMap(
            Object object, String parameterName,
            Map<String, V> valuesForTriggerIdentifiers) {

        /*
         * Iterate through the list, recursively calling this method on any
         * sublist found, and treating any map as a potential megawidget
         * specifier and, regardless of whether it is found to be or not.
         */
        if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            if (map.containsKey(parameterName)
                    && map.containsKey(HazardConstants.FIELD_NAME)) {
                valuesForTriggerIdentifiers.put(
                        map.get(HazardConstants.FIELD_NAME).toString(),
                        (V) map.get(parameterName));
            }
            for (Object value : map.values()) {
                addMegawidgetIdentifiersIncludingParameterToMap(value,
                        parameterName, valuesForTriggerIdentifiers);
            }
        } else if (object instanceof List) {
            for (Object item : (List<?>) object) {
                addMegawidgetIdentifiersIncludingParameterToMap(item,
                        parameterName, valuesForTriggerIdentifiers);
            }
        }
    }

    @Override
    public void runEventModifyingScript(final IHazardEvent hazardEvent,
            final File scriptFile, final String functionName,
            Map<String, Map<String, Object>> mutableProperties,
            final IEventModifyingScriptJobListener listener) {

        /*
         * Run the event-modifying script asynchronously.
         */
        IPythonExecutor<ContextSwitchingPythonEval, ModifiedHazardEvent> executor = new EventModifyingScriptExecutor(
                hazardEvent, scriptFile, functionName, mutableProperties);
        try {
            IPythonJobListener<ModifiedHazardEvent> pythonJobListener = new IPythonJobListener<ModifiedHazardEvent>() {

                @Override
                public void jobFinished(final ModifiedHazardEvent result) {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            listener.scriptExecutionComplete(functionName,
                                    result);
                        }
                    });
                }

                @Override
                public void jobFailed(final Throwable e) {
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            handleEventModifyingScriptExecutionError(
                                    hazardEvent, functionName, e);
                        }
                    });
                }
            };
            coordinator.submitAsyncJob(executor, pythonJobListener);
        } catch (Exception e) {
            handleEventModifyingScriptExecutionError(hazardEvent, functionName,
                    e);
        }
    }

    /**
     * Handle an error that occurred during an event modifying script execution
     * attempt.
     * 
     * @param hazardEvent
     *            Hazard event to which the script was being applied.
     * @param identifier
     *            Identifier of the script that was running.
     * @param e
     *            Error that occcurred.
     */
    private void handleEventModifyingScriptExecutionError(
            IHazardEvent hazardEvent, String identifier, Throwable e) {
        statusHandler.error("Error executing async event "
                + "modifying script job for button identifier " + identifier
                + " on event " + hazardEvent.getEventID() + ".", e);
    }

    @Override
    public HazardAlertsConfig getAlertConfig() {
        return alertsConfig.getConfig();
    }

    @Override
    public Field[] getFilterConfig() {
        Field[] config = new Field[3];
        SettingsConfig viewConfig = getSettingsConfig();
        for (Page page : viewConfig.getPages()) {
            for (Field field : page.getPageFields()) {
                if (field.getFieldName().equals("hazardCategoriesAndTypes")) {
                    config[0] = new Field(field);
                    config[0].setFieldType("HierarchicalChoicesMenu");
                    config[0].setLabel("Hazard &Types");
                } else if (field.getFieldName().equals(SETTING_HAZARD_SITES)) {
                    boolean found = false;
                    String currSite = LocalizationManager
                            .getContextName(LocalizationLevel.SITE);
                    // this will get around the fact that we are a certain site,
                    // and it may not be in the list
                    for (Choice choice : field.getChoices()) {
                        if (choice.getDisplayString().equals(currSite)) {
                            found = true;
                            break;
                        }
                    }
                    if (found == false) {
                        field.addChoice(new Choice(currSite, currSite));
                    }
                    config[1] = new Field(field);
                    config[1].setFieldType("CheckBoxesMenu");
                    config[1].setLabel("Site &IDs");
                } else if (field.getFieldName().equals(SETTING_HAZARD_STATES)) {
                    config[2] = new Field(field);
                    config[2].setFieldType("CheckBoxesMenu");
                    config[2].setLabel("&Statuses");
                }
            }
        }
        return config;
    }

    @Override
    public SettingsConfig getSettingsConfig() {
        SettingsConfig viewConfig = this.settingsConfig.getConfig()[0];
        for (Page page : viewConfig.getPages()) {
            for (Field field : page.getPageFields()) {
                if (field.getFieldType().equals(SETTING_FIELD_TYPE_GROUP)) {
                    field = field.getFields().get(0);
                }
                if (field.getFieldName().equals(
                        SETTING_HAZARD_CATEGORIES_AND_TYPES)) {
                    field.setChoices(getHazardInfoConfig()
                            .getHazardCategories());
                } else if (field.getFieldName().equals(SETTING_HAZARD_SITES)) {
                    Set<String> pSites = getSettingsValue(
                            SETTING_HAZARD_POSSIBLE_SITES, settings);
                    List<String> possibleSites = new ArrayList<String>(pSites);
                    Collections.sort(possibleSites);
                    List<Choice> choices = new ArrayList<>(possibleSites.size());
                    for (String site : possibleSites) {
                        choices.add(new Choice(site, site));
                    }
                    field.setChoices(choices);
                } else if (field.getFieldName().equals(SETTING_VISIBLE_COLUMNS)) {
                    List<String> possibleColumns = new ArrayList<>(settings
                            .getColumns().keySet());
                    Collections.sort(possibleColumns);
                    List<Choice> choices = new ArrayList<>(
                            possibleColumns.size());
                    for (String column : possibleColumns) {
                        choices.add(new Choice(column, column));
                    }
                    field.setChoices(choices);
                }
            }
        }
        return viewConfig;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getSettingsValue(String identifier, ISettings settings) {
        Object value = null;
        if (identifier.equals(SETTING_HAZARD_SITES)) {
            Set<String> list = settings.getVisibleSites();
            if ((list == null) || list.isEmpty()) {
                list = startUpConfig.getConfig().getVisibleSites();
            }
            value = list;
        } else if (identifier.equals(SETTING_HAZARD_POSSIBLE_SITES)) {
            Set<String> list = settings.getPossibleSites();
            if ((list == null) || list.isEmpty()) {
                list = startUpConfig.getConfig().getPossibleSites();
            }
            value = list;
        } else if (identifier.equals(EVENT_ID_DISPLAY_TYPE)) {
            String s = settings.getEventIdDisplayType();
            if ((s == null) || s.isEmpty()) {
                s = startUpConfig.getConfig().getEventIdDisplayType();
            }
            value = s;
        } else if (identifier.equals(MAP_CENTER)) {
            MapCenter mapCenter = settings.getMapCenter();
            if ((mapCenter == null)) {
                mapCenter = startUpConfig.getConfig().getMapCenter();
            }
            value = mapCenter;
        }
        return (T) value;
    }

    @Override
    public ProductGeneratorTable getProductGeneratorTable() {
        return pgenTable.getConfig();
    }

    @Override
    public HazardTypes getHazardTypes() {
        return hazardTypes.getConfig();
    }

    @Override
    public EventDrivenTools getEventDrivenTools() {
        return eventDrivenTools.getConfig();
    }

    @Override
    public boolean isEventDrivenToolRunningEnabled() {
        return runRecommendersAtRegularIntervals;
    }

    @Override
    public void setEventDrivenToolRunningEnabled(boolean enable) {
        if (enable == runRecommendersAtRegularIntervals) {
            return;
        }
        runRecommendersAtRegularIntervals = enable;
        for (Map.Entry<Runnable, Integer> entry : minuteIntervalsForEventDrivenToolExecutors
                .entrySet()) {
            if (enable) {
                timeManager.runAtRegularIntervals(
                        entry.getKey(),
                        ISessionTimeManager.MINUTE_AS_MILLISECONDS
                                * entry.getValue());
            } else {
                timeManager.cancelRunAtRegularIntervals(entry.getKey());
            }
        }
    }

    @Override
    public Color getColor(IHazardEvent event) {

        StyleRule styleRule = null;

        try {
            styleRule = StyleManager.getInstance().getStyleRule(
                    StyleManager.StyleType.GEOMETRY, getMatchCriteria(event));
        } catch (StyleException e) {
            statusHandler.error("Error retrieving hazard style rules: ", e);
        }

        if (styleRule != null) {
            HazardStyle hazardStyle = (HazardStyle) styleRule.getPreferences();
            Color hazardColor = hazardStyle.getColor();
            return hazardColor;
        }

        return WHITE;
    }

    /**
     * Builds the Style Rule matching criteria for retrieving hazard colors.
     * 
     * @param event
     *            The event for which to retrieve color information.
     * 
     * @return The criteria to use in searching the Hazard Services Style Rules.
     */
    private MatchCriteria getMatchCriteria(IHazardEvent event) {
        ParamLevelMatchCriteria match = new ParamLevelMatchCriteria();
        List<String> paramList = Lists.newArrayList(HazardEventUtilities
                .getHazardType(event));
        match.setParameterName(paramList);
        return match;
    }

    @Override
    public int getBorderWidth(IHazardEvent event, boolean selected) {
        if (selected) {
            return 5;
        } else {
            return 3;
        }
    }

    @Override
    public LineStyle getBorderStyle(IHazardEvent event) {
        switch (event.getStatus()) {
        case PENDING:
            return null;
        case PROPOSED:
            return LineStyle.DOTTED;
        default:
            return LineStyle.SOLID;
        }
    }

    @Override
    public String getHeadline(IHazardEvent event) {
        return getHeadline(HazardEventUtilities.getHazardType(event));
    }

    @Override
    public long getDefaultDuration(IHazardEvent event) {
        String type = HazardEventUtilities.getHazardType(event);
        return (type != null ? hazardTypes.getConfig().get(type)
                .getDefaultDuration() : getSettings().getDefaultDuration());
    }

    @Override
    public List<String> getDurationChoices(IHazardEvent event) {

        /*
         * If the duration choices for hazard types map has not yet been
         * initialized, do so now. Include an entry of an empty list for the
         * null hazard type, so that hazard events without a type have an entry.
         */
        if (durationChoicesForHazardTypes == null) {
            durationChoicesForHazardTypes = new HashMap<>();
            for (Map.Entry<String, HazardTypeEntry> entry : hazardTypes
                    .getConfig().entrySet()) {
                List<String> list = entry.getValue().getDurationChoiceList();
                if (list != null) {
                    durationChoicesForHazardTypes.put(entry.getKey(),
                            ImmutableList.copyOf(list));
                }
            }
            durationChoicesForHazardTypes.put(null,
                    ImmutableList.copyOf(Collections.<String> emptyList()));
        }

        return durationChoicesForHazardTypes.get(HazardEventUtilities
                .getHazardType(event));
    }

    @Override
    public String getRecommenderTriggeredByChange(IHazardEvent event,
            HazardEventFirstClassAttribute change) {

        /*
         * If the recommenders for triggers for hazard types map has not yet
         * been initialized, do so now. Include an entry of an empty map for the
         * null hazard type, so that hazard events without a type have an entry.
         */
        if (recommendersForTriggersForHazardTypes == null) {
            recommendersForTriggersForHazardTypes = new HashMap<>();
            for (Map.Entry<String, HazardTypeEntry> entry : hazardTypes
                    .getConfig().entrySet()) {
                Map<HazardEventFirstClassAttribute, String> map = new HashMap<>();
                Map<String, List<String>> attributeNameListsForRecommenders = entry
                        .getValue().getModifyRecommenders();
                if (attributeNameListsForRecommenders != null) {
                    for (Map.Entry<String, List<String>> subEntry : attributeNameListsForRecommenders
                            .entrySet()) {
                        for (String attributeName : subEntry.getValue()) {
                            HazardEventFirstClassAttribute attribute = HazardEventFirstClassAttribute
                                    .getInstanceWithIdentifier(attributeName);
                            if (attribute != null) {
                                map.put(attribute, subEntry.getKey());
                            } else {
                                statusHandler
                                        .warn("\""
                                                + attributeName
                                                + "\" is not a valid hazard event attribute "
                                                + "(found in 'modifyRecommenders' entry for hazard type "
                                                + entry.getKey()
                                                + "); skipping.");
                            }
                        }
                    }
                }
                recommendersForTriggersForHazardTypes.put(entry.getKey(), map);
            }
            recommendersForTriggersForHazardTypes.put(null, Collections
                    .<HazardEventFirstClassAttribute, String> emptyMap());
        }

        return recommendersForTriggersForHazardTypes.get(
                HazardEventUtilities.getHazardType(event)).get(change);
    }

    @Override
    public boolean isStartTimeIsCurrentTime(IHazardEvent event) {

        /*
         * If the flags for hazard types map has not yet been initialized, do so
         * now. Include an entry of an empty list for the null hazard type, so
         * that hazard events without a type have an entry.
         */
        if (startTimeIsCurrentTimeForHazardTypes == null) {
            startTimeIsCurrentTimeForHazardTypes = new HashMap<>();
            for (Map.Entry<String, HazardTypeEntry> entry : hazardTypes
                    .getConfig().entrySet()) {
                startTimeIsCurrentTimeForHazardTypes.put(entry.getKey(), entry
                        .getValue().isStartTimeIsCurrentTime());
            }
            startTimeIsCurrentTimeForHazardTypes.put(null, false);
        }

        return startTimeIsCurrentTimeForHazardTypes.get(HazardEventUtilities
                .getHazardType(event));
    }

    @Override
    public boolean isAllowTimeExpand(IHazardEvent event) {

        /*
         * If the flags for hazard types map has not yet been initialized, do so
         * now. Include an entry of an empty list for the null hazard type, so
         * that hazard events without a type have an entry.
         */
        if (allowTimeExpandForHazardTypes == null) {
            allowTimeExpandForHazardTypes = new HashMap<>();
            for (Map.Entry<String, HazardTypeEntry> entry : hazardTypes
                    .getConfig().entrySet()) {
                allowTimeExpandForHazardTypes.put(entry.getKey(), entry
                        .getValue().isAllowTimeExpand());
            }
            allowTimeExpandForHazardTypes.put(null, true);
        }

        return allowTimeExpandForHazardTypes.get(HazardEventUtilities
                .getHazardType(event));
    }

    @Override
    public boolean isAllowTimeShrink(IHazardEvent event) {

        /*
         * If the flags for hazard types map has not yet been initialized, do so
         * now. Include an entry of an empty list for the null hazard type, so
         * that hazard events without a type have an entry.
         */
        if (allowTimeShrinkForHazardTypes == null) {
            allowTimeShrinkForHazardTypes = new HashMap<>();
            for (Map.Entry<String, HazardTypeEntry> entry : hazardTypes
                    .getConfig().entrySet()) {
                allowTimeShrinkForHazardTypes.put(entry.getKey(), entry
                        .getValue().isAllowTimeShrink());
            }
            allowTimeShrinkForHazardTypes.put(null, true);
        }

        return allowTimeShrinkForHazardTypes.get(HazardEventUtilities
                .getHazardType(event));
    }

    @Override
    public String getTypeFirstRecommender(String hazardType) {

        /*
         * If the type-first recommenders for hazard types map has not yet been
         * initialized, do so now.
         */
        if (typeFirstRecommendersForHazardTypes == null) {
            typeFirstRecommendersForHazardTypes = new HashMap<>();
            for (Map.Entry<String, HazardTypeEntry> entry : hazardTypes
                    .getConfig().entrySet()) {
                String recommender = entry.getValue()
                        .getHazardTypeFirstRecommender();
                if ((recommender != null) && (recommender.isEmpty() == false)) {
                    typeFirstRecommendersForHazardTypes.put(entry.getKey(),
                            recommender);
                }
            }
        }
        return typeFirstRecommendersForHazardTypes.get(hazardType);
    }

    @Override
    public boolean isPointIdentifierRequired(String hazardType) {

        /*
         * If the require-point-identifiers set has not yet been initialized, do
         * so now.
         */
        if (typesRequiringPointIds == null) {
            typesRequiringPointIds = new HashSet<>();
            for (Map.Entry<String, HazardTypeEntry> entry : hazardTypes
                    .getConfig().entrySet()) {
                if (entry.getValue().isRequirePointId()) {
                    typesRequiringPointIds.add(entry.getKey());
                }
            }
        }
        return typesRequiringPointIds.contains(hazardType);
    }

    @Override
    public List<String> getReplaceByTypes(String hazardType) {

        /*
         * If the replace-by types for hazard types map has not yet been
         * initialized, do so now.
         */
        if (replaceByTypesForHazardTypes == null) {
            replaceByTypesForHazardTypes = new HashMap<>();
            for (Map.Entry<String, HazardTypeEntry> entry : hazardTypes
                    .getConfig().entrySet()) {
                replaceByTypesForHazardTypes.put(entry.getKey(),
                        ImmutableList.copyOf(entry.getValue().getReplacedBy()));
            }
        }

        return replaceByTypesForHazardTypes.get(hazardType);
    }

    @Override
    public String getHazardCategory(IHazardEvent event) {
        if (HazardEventUtilities.isHazardTypeValid(event) == false) {
            return (String) event
                    .getHazardAttribute(ISessionEventManager.ATTR_HAZARD_CATEGORY);
        }
        for (Entry<String, String[][]> entry : hazardCategories.getConfig()
                .entrySet()) {
            for (String[] str : entry.getValue()) {
                if (str.length > 0) {
                    if (event.getPhenomenon() == null) {
                        continue;
                    } else if (!event.getPhenomenon().equals(str[0])) {
                        continue;
                    }
                    if (str.length > 1) {
                        if (event.getSignificance() == null) {
                            continue;
                        } else if (!event.getSignificance().equals(str[1])) {
                            continue;
                        }
                        if (str.length > 2) {
                            if (event.getSubType() == null) {
                                continue;
                            } else if (!event.getSubType().equals(str[2])) {
                                continue;
                            }
                        }
                    } else if (event.getSignificance() != null) {
                        continue;
                    }
                    return entry.getKey();
                }
            }
        }
        return (String) event
                .getHazardAttribute(ISessionEventManager.ATTR_HAZARD_CATEGORY);
    }

    protected void settingsChanged(SettingsModified notification) {
        notificationSender.postNotificationAsync(notification);
    }

    private String getHeadline(String id) {
        HazardTypeEntry type = hazardTypes.getConfig().get(id);
        if (type == null) {
            return null;
        }
        return type.getHeadline();
    }

    public static Color getColor(String str) {
        int r = Integer.valueOf(str.substring(1, 3), 16);
        int g = Integer.valueOf(str.substring(3, 5), 16);
        int b = Integer.valueOf(str.substring(5, 7), 16);
        return new Color(r / 255f, g / 255f, b / 255f);
    }

    private class SettingsDirectoryUpdateObserver implements
            ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            loadAllSettings();
        }

    }

    private class StartUpConfigObserver implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            LocalizationFile file = pathManager
                    .getStaticLocalizationFile(HazardsConfigurationConstants.START_UP_CONFIG_PY);
            startUpConfig = new ConfigLoader<StartUpConfig>(file,
                    StartUpConfig.class);
            loaderPool.schedule(startUpConfig);
        }
    }

    private class HazardCategoriesObserver implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            LocalizationFile file = pathManager
                    .getStaticLocalizationFile(HazardsConfigurationConstants.HAZARD_CATEGORIES_PY);
            hazardCategories = new ConfigLoader<HazardCategories>(file,
                    HazardCategories.class);
            loaderPool.schedule(hazardCategories);
        }
    }

    private class HazardMetaDataObserver implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            StringBuilder metadataIncludes = new StringBuilder();

            LocalizationFile file = pathManager
                    .getStaticLocalizationFile(HazardsConfigurationConstants.HAZARD_CATEGORIES_PY);

            metadataIncludes.append(file.getFile().getParent());
            file = pathManager
                    .getStaticLocalizationFile(HazardsConfigurationConstants.VTEC_CONSTANTS_PY);
            metadataIncludes.append(":").append(file.getFile().getParent());

            file = pathManager
                    .getStaticLocalizationFile(HazardsConfigurationConstants.HAZARD_METADATA_PY);
            metadataIncludes.append(":").append(file.getFile().getParent());

            hazardMetaData = new ConfigLoader<HazardMetaData>(file,
                    HazardMetaData.class, null, metadataIncludes.toString());
            loaderPool.schedule(hazardMetaData);
        }
    }

    private class HazardTypesObserver implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            LocalizationFile file = pathManager
                    .getStaticLocalizationFile(HazardsConfigurationConstants.HAZARD_TYPES_PY);
            hazardTypes = new ConfigLoader<HazardTypes>(file, HazardTypes.class);
            loaderPool.schedule(hazardTypes);
        }
    }

    private class HazardAlertsConfigObserver implements
            ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            LocalizationFile file = pathManager
                    .getStaticLocalizationFile(HazardsConfigurationConstants.ALERTS_CONFIG_PATH);
            alertsConfig = new ConfigLoader<HazardAlertsConfig>(file,
                    HazardAlertsConfig.class);
            loaderPool.schedule(alertsConfig);

            Mode mode = CAVEMode.getMode() == CAVEMode.PRACTICE ? Mode.PRACTICE
                    : Mode.OPERATIONAL;
            sessionManager.getAlertsManager().addAlertGenerationStrategy(
                    HazardNotification.class,
                    new HazardEventExpirationAlertStrategy(sessionManager
                            .getAlertsManager(), timeManager,
                            SessionConfigurationManager.this,
                            new HazardEventManager(mode),
                            new AllHazardsFilterStrategy()));
        }

    }

    private class ProductGeneratorTableObserver implements
            ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            LocalizationFile file = pathManager
                    .getStaticLocalizationFile(HazardsConfigurationConstants.PRODUCT_GENERATOR_TABLE_PY);
            pgenTable = new ConfigLoader<ProductGeneratorTable>(file,
                    ProductGeneratorTable.class);
            loaderPool.schedule(pgenTable);
        }
    }

    private class DefaultConfigObserver implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            LocalizationFile file = pathManager
                    .getStaticLocalizationFile(HazardsConfigurationConstants.DEFAULT_CONFIG_PY);
            settingsConfig = new ConfigLoader<SettingsConfig[]>(file,
                    SettingsConfig[].class, "viewConfig");
            loaderPool.schedule(settingsConfig);
        }
    }

    @Override
    public void shutdown() {
        coordinator.shutdown();
    }

}
