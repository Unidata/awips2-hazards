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
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_TYPE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.MAP_CENTER;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PRIORITY_FOR_DRAG_AND_DROP_GEOMETRY_EDITS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_FIELD_TYPE_GROUP;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_CATEGORIES_AND_TYPES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_POSSIBLE_SITES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_SITES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_STATES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_VISIBLE_COLUMNS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.TIME_RESOLUTION;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXB;

import org.eclipse.swt.widgets.Display;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardEventFirstClassAttribute;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.utilities.PythonBuildPaths;
import com.raytheon.uf.common.hazards.configuration.ConfigLoader;
import com.raytheon.uf.common.hazards.configuration.HazardsConfigurationConstants;
import com.raytheon.uf.common.hazards.configuration.backup.BackupSites;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.localization.FileUpdatedMessage;
import com.raytheon.uf.common.localization.ILocalizationFileObserver;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManager;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.SaveableOutputStream;
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
import com.raytheon.uf.viz.hazards.sessionmanager.config.SiteChanged;
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
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.TriggerType;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.uf.viz.hazards.sessionmanager.styles.HazardStyle;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.viz.core.mode.CAVEMode;

import gov.noaa.gsd.common.utilities.DragAndDropGeometryEditSource;
import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.common.utilities.Utils;
import gov.noaa.gsd.viz.megawidgets.GroupSpecifier;
import gov.noaa.gsd.viz.megawidgets.IControlSpecifier;
import gov.noaa.gsd.viz.megawidgets.ISideEffectsApplier;
import gov.noaa.gsd.viz.megawidgets.ISpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.megawidgets.sideeffects.PythonSideEffectsApplier;

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
 * Nov 17, 2015 11776      Roger.Ferrel Added {@link #containsUserLevelSettings()}.
 * Aug 31, 2015 9757       Robert.Blum  Added Observers to config files so overrides are
 *                                      picked up.
 * Sep 28, 2015 10302,8167 hansen       Added "getSettingsValue"
 * Nov 17, 2015 3473       Robert.Blum  Moved all python files under HazardServices
 *                                      localization dir.
 * Mar 04, 2016 15933      Chris.Golden Added ability to run multiple recommenders in
 *                                      sequence in response to a time interval trigger,
 *                                      instead of just one recommender.
 * Mar 22, 2016 15676      Chris.Golden Changed line styles returned to make more sense
 *                                      (dotted was referred to as dashed).
 * Mar 29, 2016 16009      Robert.Blum  Fixed NPE that occurred when backupsites.xml was
 *                                      not overridden by the site.
 * Mar 31, 2016  8837      Robert.Blum  Changes for Service Backup.
 * Apr 01, 2016 16225      Chris.Golden Added ability to cancel tasks that are scheduled
 *                                      to run at regular intervals.
 * Apr 06, 2016  8837      Robert.Blum  Changing sites now changes the eventid as well.
 * Apr 25, 2016 17611      Robert.Blum  ConfigLoader can now incrementally override
 *                                      python configuration files.
 * Apr 25, 2016 18129      Chris.Golden Changed time-interval-triggered tasks to be
 *                                      triggered close to the instant when the CAVE
 *                                      current time ticks over to a new minute.
 * Apr 27, 2016 18266      Chris.Golden Added support for event-driven tools triggered
 *                                      by data layer changes.
 * Apr 28, 2016 18267      Chris.Golden Added support for unrestricted event start
 *                                      times.
 * May 04, 2016 18266      Chris.Golden Added passing of data time to method allowing
 *                                      triggering by data layer change, and code to
 *                                      avoid triggering if the data time passed in is
 *                                      not later than the last data time that triggered
 *                                      the same tool.
 * May 06, 2016 18202      Robert.Blum  Changes for operational mode.
 * Jul 08, 2016 13788      Chris.Golden Added validation of hazard events.
 * Jul 27, 2016 19924      Chris.Golden Removed obsolete code related to data layer
 *                                      changes triggering event-driven tools; the
 *                                      configuration of such is now read in within the
 *                                      configuration manager, but the work of tracking
 *                                      data layer changes is done by the app builder
 *                                      where it belongs.
 * Aug 09, 2016 18376      Chris.Golden Changed PythonJobCoordinator instance to be
 *                                      a static instead of a member variable, and
 *                                      removed any shutdown of the coordinator, so that
 *                                      it is reused between H.S. sessions within the
 *                                      same CAVE session. Also added tracking of
 *                                      file observers and their associated files during
 *                                      this class's instances' lifetimes so that the
 *                                      observers may be removed when shutting down,
 *                                      which will help with garbage collecting these
 *                                      class's instances.
 * Sep 27, 2016 15928      Chris.Golden Changed line thickness for hazard events. Also
 *                                      fixed unrelated bug in detection of metadata
 *                                      refresh triggers in megawidget specifiers; code
 *                                      changes from mid-August broke metadata refresh
 *                                      if the value of the specifier parameter was
 *                                      a boolean, for example, instead of a string.
 * Oct 04, 2016 22573      Robert.Blum  Updated validateHazardEvent() to check for null
 *                                      hazard type.
 * Oct 05, 2016 22870      Chris.Golden Added support for event-driven tools triggered
 *                                      by frame changes.
 * Oct 06, 2016 22894      Chris.Golden Added method to get session attributes for a
 *                                      hazard type.
 * Oct 19, 2016 21873      Chris.Golden Added time resolution, both for hazard types
 *                                      and for the current settings (and with the
 *                                      latter, startup-config).
 * Nov 28, 2016 26181      bkowal       Corrected JSON serialization and updated JSON output
 *                                      to be pretty printed. Eliminated a few more warnings.
 * Feb 01, 2017 15556      Chris.Golden Changed to include originator when setting
 *                                      the site identifier.
 * Mar 27, 2017 15528      Chris.Golden Added gathering of set of metadata megawidget
 *                                      identifiers for which modification of their
 *                                      underlying values does not affect their
 *                                      enclosing hazard event's modify flag.
 * Apr 13, 2017 33142      Chris.Golden Changed to use sets holding hazard types
 *                                      in place of mappings of hazard types to
 *                                      booleans, as the set is simpler than the
 *                                      map and is a better choice. Also added a
 *                                      check at startup and whenever the hazard
 *                                      categories or hazard types are reloaded to
 *                                      ensure that the hazard categories' types are
 *                                      all found within the hazard types definition
 *                                      file, and to complain with an error message
 *                                      if they are not.
 * Jun 01, 2017 23056      Chris.Golden Added code to find metadata megawidgets that
 *                                      always use their default values instead of
 *                                      any existing old values.
 * Sep 27, 2017 38072      Chris.Golden Changed to use HazardConstants constant instead
 *                                      of one defined elsewhere. Also changed the
 *                                      metadata fetching process to ensure that a
 *                                      hazard attribute change does not for exammple
 *                                      trigger a metadata refresh and a recommender
 *                                      run; it should do only one of the two.
 * Oct 23, 2017 21730      Chris.Golden Added ability to do a two-level query for
 *                                      default hazard type (check settings, then
 *                                      starup config).
 * Dec 13, 2017 40923      Chris.Golden Added return of modified hazard event from
 *                                      metadata fetch.
 * Dec 17, 2017 20739      Chris.Golden Refactored away access to directly mutable
 *                                      session events.
 * Jan 22, 2018 25765      Chris.Golden Added "priority for drag-and-drop geometry
 *                                      edit" flag to make geometry editing from the
 *                                      spatial display more flexible.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionConfigurationManager
        implements ISessionConfigurationManager<ObservedSettings> {

    /**
     * Metadata group megawidget type.
     */
    private static final String METADATA_GROUP_TYPE = "Group";

    /**
     * Metadata group megawidget label.
     */
    private static final String METADATA_GROUP_TEXT = "Details";

    /*
     * Locaalized directory for setting filters.
     */
    private static final String SETTINGS_DIR = "HazardServices/settings/";

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
            statusHandler
                    .error("unexpected error while creating empty megawidget specifier manager: "
                            + e, e);
        }
        EMPTY_MEGAWIDGET_SPECIFIER_MANAGER = manager;
    }

    private static final HazardEventMetadata EMPTY_HAZARD_EVENT_METADATA = new HazardEventMetadata(
            EMPTY_MEGAWIDGET_SPECIFIER_MANAGER, null,
            Collections.<String> emptySet(), Collections.<String> emptySet(),
            Collections.<String> emptySet(),
            Collections.<String, String> emptyMap(),
            Collections.<String> emptySet(), null, null);

    /**
     * Python job coordinator that handles both metadata fetching scripts and
     * event modifying scripts.
     */
    private static final PythonJobCoordinator<ContextSwitchingPythonEval> PYTHON_JOB_COORDINATOR = new PythonJobCoordinator<>(
            1, "configScriptFactory", new ConfigScriptFactory());

    private ISessionNotificationSender notificationSender;

    private final JobPool loaderPool = new JobPool(
            "Loading Hazard Services Config", 1);

    private ISessionManager<ObservedSettings> sessionManager;

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

    private String siteIdentifier;

    private Map<String, TimeResolution> timeResolutionsForHazardTypes;

    private Map<String, ImmutableList<String>> durationChoicesForHazardTypes;

    private Map<String, ImmutableList<String>> replaceByTypesForHazardTypes;

    private Map<String, ImmutableList<String>> sessionAttributesForHazardTypes;

    private Map<String, String> typeFirstRecommendersForHazardTypes;

    private Set<String> typesRequiringStartTimeIsCurrentTime;

    private Set<String> typesAllowingAnyStartTime;

    private Set<String> typesAllowingTimeExpand;

    private Set<String> typesAllowingTimeShrink;

    private Map<String, Map<HazardEventFirstClassAttribute, String>> recommendersForTriggersForHazardTypes;

    private Set<String> typesRequiringPointIds;

    private BackupSites backupSites;

    private final Map<Runnable, Integer> minuteIntervalsForEventDrivenToolExecutors = new IdentityHashMap<>();

    private Runnable dataLayerChangeTriggeredToolExecutor;

    private Runnable frameChangeTriggeredToolExecutor;

    private boolean runRecommendersAtRegularIntervals;

    private final Map<LocalizationFile, ILocalizationFileObserver> observersForLocalizationFiles = new HashMap<>();

    SessionConfigurationManager() {

    }

    public SessionConfigurationManager(
            ISessionManager<ObservedSettings> sessionManager,
            IPathManager pathManager, ISessionTimeManager timeManager,
            ISessionNotificationSender notificationSender) {
        this.sessionManager = sessionManager;
        this.pathManager = pathManager;
        this.timeManager = timeManager;
        this.notificationSender = notificationSender;

        LocalizationContext commonStaticBase = pathManager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        associateObserverWithLocalizationFile(
                pathManager.getLocalizationFile(commonStaticBase, SETTINGS_DIR),
                new SettingsDirectoryUpdateObserver());

        loadAllSettings();

        Map<String, Object> configLoaderParams = new HashMap<>();
        configLoaderParams.put(HazardConstants.SITE, this.getSiteID());
        configLoaderParams.put(HazardConstants.INCREMENTAL_OVERRIDE, true);

        associateObserverWithLocalizationFile(
                pathManager.getLocalizationFile(commonStaticBase,
                        HazardsConfigurationConstants.START_UP_CONFIG_PY),
                new StartUpConfigObserver());

        LocalizationFile file = pathManager.getStaticLocalizationFile(
                HazardsConfigurationConstants.START_UP_CONFIG_PY);
        configLoaderParams.put(HazardConstants.DATA_TYPE,
                HazardsConfigurationConstants.STARTUP_CONFIG_DIR);
        startUpConfig = new ConfigLoader<StartUpConfig>(file,
                StartUpConfig.class, null, PythonBuildPaths.buildIncludePath(),
                new HashMap<>(configLoaderParams));
        loaderPool.schedule(startUpConfig);

        associateObserverWithLocalizationFile(
                pathManager.getLocalizationFile(commonStaticBase,
                        HazardsConfigurationConstants.HAZARD_CATEGORIES_PY),
                new HazardCategoriesObserver());

        file = pathManager.getStaticLocalizationFile(
                HazardsConfigurationConstants.HAZARD_CATEGORIES_PY);
        configLoaderParams.put(HazardConstants.DATA_TYPE,
                HazardsConfigurationConstants.HAZARD_CATEGORIES_LOCALIZATION_DIR);
        hazardCategories = new ConfigLoader<HazardCategories>(file,
                HazardCategories.class, null,
                PythonBuildPaths.buildIncludePath(),
                new HashMap<>(configLoaderParams));
        loaderPool.schedule(hazardCategories);

        file = pathManager.getStaticLocalizationFile(
                HazardsConfigurationConstants.VTEC_CONSTANTS_PY);

        // Add observer to base file
        associateObserverWithLocalizationFile(
                pathManager.getLocalizationFile(commonStaticBase,
                        HazardsConfigurationConstants.HAZARD_METADATA_PY),
                new HazardMetaDataObserver());

        file = pathManager.getStaticLocalizationFile(
                HazardsConfigurationConstants.HAZARD_METADATA_PY);
        for (LocalizationFile f : pathManager.listStaticFiles(
                "HazardServices/hazardMetaData/", new String[] { ".py" }, false,
                true)) {
            // Force download the file so python has it
            f.getFile();
        }
        configLoaderParams.put(HazardConstants.DATA_TYPE,
                HazardsConfigurationConstants.HAZARD_METADATA_DIR);
        hazardMetaData = new ConfigLoader<HazardMetaData>(file,
                HazardMetaData.class, null, PythonBuildPaths.buildIncludePath(),
                new HashMap<>(configLoaderParams));
        loaderPool.schedule(hazardMetaData);

        // Add observer to base file
        associateObserverWithLocalizationFile(
                pathManager.getLocalizationFile(commonStaticBase,
                        HazardsConfigurationConstants.HAZARD_TYPES_PY),
                new HazardTypesObserver());

        file = pathManager.getStaticLocalizationFile(
                HazardsConfigurationConstants.HAZARD_TYPES_PY);

        configLoaderParams.put(HazardConstants.DATA_TYPE,
                HazardsConfigurationConstants.HAZARD_TYPES_LOCALIZATION_DIR);
        hazardTypes = new ConfigLoader<HazardTypes>(file, HazardTypes.class,
                null, PythonBuildPaths.buildIncludePath(),
                new HashMap<>(configLoaderParams));
        loaderPool.schedule(hazardTypes);

        ensureHazardTypeEntriesExistForAllTypes();

        /*
         * Load any event-driven tool specifications from configuration, and
         * create the executors for them, saving them along with their minute
         * intervals as appropriate.
         * 
         * TODO: Think about how to allow a file observer for event-driven tools
         * to reinitialize said tool running.
         */
        eventDrivenTools = new ConfigLoader<EventDrivenTools>(
                pathManager.getStaticLocalizationFile(
                        HazardsConfigurationConstants.EVENT_DRIVEN_TOOLS_PY),
                EventDrivenTools.class);
        loaderPool.schedule(eventDrivenTools);
        EventDrivenTools tools = eventDrivenTools.getConfig();
        for (final EventDrivenToolEntry entry : tools) {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    List<String> identifiers = entry.getToolIdentifiers();
                    Set<String> setOfIdentifiers = new HashSet<>(identifiers);
                    if (setOfIdentifiers.size() != entry.getToolIdentifiers()
                            .size()) {
                        statusHandler
                                .warn("List of recommenders to be executed sequentially ("
                                        + Joiner.on(", ").join(identifiers)
                                        + ") when triggered by events cannot include "
                                        + "any duplicate entries; these recommenders "
                                        + "will not be executed.");
                    } else {
                        SessionConfigurationManager.this.sessionManager
                                .runTools(entry.getToolType(),
                                        entry.getToolIdentifiers(),
                                        (entry.getTriggerType() == TriggerType.TIME_INTERVAL
                                                ? RecommenderExecutionContext
                                                        .getTimeIntervalContext()
                                                : (entry.getTriggerType() == TriggerType.FRAME_CHANGE
                                                        ? RecommenderExecutionContext
                                                                .getFrameChangeContext()
                                                        : RecommenderExecutionContext
                                                                .getDataLayerUpdateContext())));
                    }
                }
            };
            if (entry.getTriggerType() == TriggerType.TIME_INTERVAL) {
                minuteIntervalsForEventDrivenToolExecutors.put(runnable,
                        entry.getIntervalMinutes());
            } else if (entry.getTriggerType() == TriggerType.FRAME_CHANGE) {
                frameChangeTriggeredToolExecutor = runnable;
            } else {
                dataLayerChangeTriggeredToolExecutor = runnable;
            }
        }

        /*
         * Set the flag for running recommenders at regular intervals to the
         * starting value.
         */
        runRecommendersAtRegularIntervals = false;
        setEventDrivenToolRunningEnabled(true);

        // Add observer to base file
        associateObserverWithLocalizationFile(
                pathManager.getLocalizationFile(commonStaticBase,
                        HazardsConfigurationConstants.ALERTS_CONFIG_PATH),
                new HazardAlertsConfigObserver());

        file = pathManager.getStaticLocalizationFile(
                HazardsConfigurationConstants.ALERTS_CONFIG_PATH);
        configLoaderParams.put(HazardConstants.DATA_TYPE,
                HazardConstants.ALERT_CONFIG_DATA);
        alertsConfig = new ConfigLoader<HazardAlertsConfig>(file,
                HazardAlertsConfig.class, null,
                PythonBuildPaths.buildIncludePath(),
                new HashMap<>(configLoaderParams));
        loaderPool.schedule(alertsConfig);

        // Add observer to base file
        associateObserverWithLocalizationFile(
                pathManager.getLocalizationFile(commonStaticBase,
                        HazardsConfigurationConstants.PRODUCT_GENERATOR_TABLE_PY),
                new ProductGeneratorTableObserver());

        file = pathManager.getStaticLocalizationFile(
                HazardsConfigurationConstants.PRODUCT_GENERATOR_TABLE_PY);
        configLoaderParams.put(HazardConstants.DATA_TYPE,
                HazardsConfigurationConstants.PRODUCT_GENERATOR_TABLE_DIR);
        pgenTable = new ConfigLoader<ProductGeneratorTable>(file,
                ProductGeneratorTable.class, null,
                PythonBuildPaths.buildIncludePath(),
                new HashMap<>(configLoaderParams));
        loaderPool.schedule(pgenTable);

        // Add observer to base file
        associateObserverWithLocalizationFile(
                pathManager.getLocalizationFile(commonStaticBase,
                        HazardsConfigurationConstants.DEFAULT_CONFIG_PY),
                new DefaultConfigObserver());

        file = pathManager.getStaticLocalizationFile(
                HazardsConfigurationConstants.DEFAULT_CONFIG_PY);
        configLoaderParams.put(HazardConstants.DATA_TYPE,
                HazardConstants.VIEW_CONFIG_DATA);
        configLoaderParams.put(HazardConstants.INCREMENTAL_OVERRIDE, false);
        settingsConfig = new ConfigLoader<SettingsConfig[]>(file,
                SettingsConfig[].class, "viewConfig",
                PythonBuildPaths.buildIncludePath(),
                new HashMap<>(configLoaderParams));
        loaderPool.schedule(settingsConfig);

        loadBackupSites();
    }

    /**
     * Associate the specified localization file with the specified observer by
     * assigning the latter as a listener for the former, and by recording the
     * association so that it may be undone later.
     * 
     * @param localizationFile
     *            Localization file to be associated.
     * @param observer
     *            Observer to be assigned with the file.
     */
    private void associateObserverWithLocalizationFile(
            LocalizationFile localizationFile,
            ILocalizationFileObserver observer) {
        localizationFile.addFileUpdatedObserver(observer);
        observersForLocalizationFiles.put(localizationFile, observer);
    }

    protected void loadAllSettings() {
        Map<String, Object> configLoaderParams = new HashMap<>();
        configLoaderParams.put(HazardConstants.SITE, this.getSiteID());
        configLoaderParams.put(HazardConstants.DATA_TYPE,
                HazardConstants.SETTINGS_DATA);
        configLoaderParams.put(HazardConstants.INCREMENTAL_OVERRIDE, true);

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
        LocalizationFile[] files = pathManager.listStaticFiles(SETTINGS_DIR,
                new String[] { ".py" }, false, true);
        List<ConfigLoader<Settings>> allSettings = new ArrayList<ConfigLoader<Settings>>();
        for (LocalizationFile file : files) {
            ConfigLoader<Settings> loader = new ConfigLoader<>(file,
                    Settings.class, null, PythonBuildPaths.buildIncludePath(),
                    configLoaderParams);
            if (file.exists()) {
                allSettings.add(loader);
                loaderPool.schedule(loader);
                if (previousPersisted != null) {
                    String fileName = file.getPath();
                    if (fileName.startsWith(
                            previousPersisted.getSettingsID() + ".")) {
                        Settings s = loader.getConfig();
                        if (s.getSettingsID()
                                .equals(previousPersisted.getSettingsID())) {
                            settings.applyPersistedChanges(previousPersisted,
                                    s);
                            previousPersisted = null;
                        }
                    }
                }
            }
        }
        this.allSettings = allSettings;
    }

    private void loadBackupSites() {
        IPathManager pathMgr = PathManagerFactory.getPathManager();

        Map<LocalizationLevel, LocalizationFile> files = pathMgr
                .getTieredLocalizationFile(LocalizationType.COMMON_STATIC,
                        "HazardServices" + PathManager.SEPARATOR + "settings"
                                + PathManager.SEPARATOR + "backupSites.xml");
        LocalizationFile file = null;
        if (files.containsKey(LocalizationLevel.SITE)) {
            file = files.get(LocalizationLevel.SITE);
        } else {
            file = files.get(LocalizationLevel.BASE);
        }

        try (InputStream is = file.openInputStream()) {
            backupSites = JAXB.unmarshal(is, BackupSites.class);
        } catch (Exception e) {
            statusHandler.error(
                    "Error loading backup sites from backupSites.xml", e);
        }

        /*
         * Use the startup config fallback sites if no backup sites are found.
         */
        if ((backupSites == null) || (backupSites.getSites() == null)
                || (backupSites.getSites().length == 0)) {
            String[] startupConfigBackupSites = startUpConfig.getConfig()
                    .getBackupSites();
            if (startupConfigBackupSites == null) {
                startupConfigBackupSites = new String[0];
            }
            backupSites = new BackupSites();
            backupSites.setSites(startUpConfig.getConfig().getBackupSites());
        }

        /*
         * Ensure the current site is part of the list.
         */
        String currentSite = LocalizationManager
                .getContextName(LocalizationLevel.SITE);
        String[] sites = backupSites.getSites();
        if (Arrays.asList(sites).contains(currentSite) == false) {
            String[] newSites = new String[sites.length + 1];
            System.arraycopy(sites, 0, newSites, 0, sites.length);
            newSites[newSites.length - 1] = currentSite;
            backupSites.setSites(newSites);
        }
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
     * Retrieve a clone of the current settings instance. Note: Settings must be
     * changed (saved) before modifications persist.
     * 
     * @return A clone copy of the currently set Settings instance
     */
    public ObservedSettings getSettings() {
        if (settings != null) {
            return settings;
        } else if (!allSettings.isEmpty()) {
            this.settings = new ObservedSettings(this,
                    allSettings.get(0).getConfig());
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
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try (SaveableOutputStream sos = f.openOutputStream()) {
            contents.append(mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(settings));
            String contentsAsString = contents.toString();
            contentsAsString = contentsAsString.replaceAll("true", "True")
                    .replaceAll("false", "False");
            sos.write(contentsAsString.getBytes());
            sos.save();
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, "Unable to save settings.",
                    e);
        }
        if (allSettings.contains(settings) == false) {
            Map<String, Object> configLoaderParams = new HashMap<>();
            configLoaderParams.put(HazardConstants.SITE, this.getSiteID());
            configLoaderParams.put(HazardConstants.DATA_TYPE,
                    HazardConstants.SETTINGS_DATA);
            configLoaderParams.put(HazardConstants.INCREMENTAL_OVERRIDE, true);
            allSettings.add(new ConfigLoader<>(f, Settings.class, null,
                    PythonBuildPaths.buildIncludePath(), configLoaderParams));
        }
    }

    @Override
    public boolean containsUserLevelSettings() {
        ISettings settings = getSettings();
        Map<LocalizationLevel, LocalizationFile> map = pathManager
                .getTieredLocalizationFile(LocalizationType.COMMON_STATIC,
                        SETTINGS_DIR + settings.getSettingsID() + ".py");
        return map.containsKey(LocalizationLevel.USER);
    }

    @Override
    public void deleteSettings() {
        ISettings settings = getSettings();
        String deleteId = settings.getSettingsID();
        Map<LocalizationLevel, LocalizationFile> map = pathManager
                .getTieredLocalizationFile(LocalizationType.COMMON_STATIC,
                        SETTINGS_DIR + deleteId + ".py");

        /*
         * Only allow deletion of user localized files.
         */
        try {
            if (map.containsKey(LocalizationLevel.USER)) {
                map.get(LocalizationLevel.USER).delete();
            }

            if (map.size() == 1) {

                /*
                 * Remove when setting is just a user file.
                 */
                Iterator<ConfigLoader<Settings>> iter = allSettings.iterator();
                while (iter.hasNext()) {
                    Settings available = iter.next().getConfig();
                    String id = available.getSettingsID();
                    if (id.equals(settings.getSettingsID())) {
                        iter.remove();
                        break;
                    }
                }
            } else {

                /*
                 * Update displays to non-USER localized file.
                 */
                settingsChanged(new SettingsModified(this,
                        EnumSet.allOf(ObservedSettings.Type.class),
                        Originator.OTHER));
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
                SETTINGS_DIR + settings.getSettingsID() + ".py");
    }

    @Override
    public String getSiteID() {
        return siteIdentifier;
    }

    @Override
    public void setSiteID(String siteIdentifier, IOriginator originator) {
        if (Utils.equal(siteIdentifier, this.siteIdentifier) == false) {
            this.siteIdentifier = siteIdentifier;
            try {
                sessionManager.setupEventIdDisplay();
            } catch (HazardEventServiceException e) {
                statusHandler.error(
                        "Unable to setup event identifier to use new site ID.",
                        e);
            }
            notificationSender.postNotificationAsync(
                    new SiteChanged(siteIdentifier, originator));
        }
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

                String hazardType = HazardEventUtilities.getHazardType(info);
                child.setIdentifier(hazardType);
                if (headline == null) {
                    child.setDisplayString(child.getIdentifier());
                } else {
                    child.setDisplayString(hazardType + " (" + headline + ")");
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
    public HazardEventMetadata getMetadataForHazardEvent(IHazardEvent event) {

        /*
         * Create the environment map.
         */
        Map<String, Serializable> environmentMap = new HashMap<String, Serializable>();
        environmentMap.put(HazardConstants.SITE, getSiteID());

        /*
         * Get the metadata, which is a map with at least one entry holding the
         * list of megawidget specifiers that applies, as well as an optional
         * entry for a map of event modifier identifiers to script function
         * names.
         */
        IPythonExecutor<ContextSwitchingPythonEval, Map<String, Object>> executor = new MetaDataScriptExecutor(
                event, environmentMap);
        Map<String, Object> result = null;
        try {
            result = PYTHON_JOB_COORDINATOR.submitJob(executor).get();
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
            scriptFile = PathManagerFactory.getPathManager()
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
        IHazardEvent modifiedHazardEvent = (IHazardEvent) result
                .get(HazardConstants.MODIFIED_HAZARD_EVENT_KEY);
        if (specifiersList.isEmpty()) {
            return ((eventModifyingFunctionNamesForIdentifiers == null)
                    && (modifiedHazardEvent == null)
                            ? EMPTY_HAZARD_EVENT_METADATA
                            : new HazardEventMetadata(
                                    EMPTY_MEGAWIDGET_SPECIFIER_MANAGER,
                                    modifiedHazardEvent,
                                    Collections.<String> emptySet(),
                                    Collections.<String> emptySet(),
                                    Collections.<String> emptySet(),
                                    Collections.<String, String> emptyMap(),
                                    Collections.<String> emptySet(), scriptFile,
                                    eventModifyingFunctionNamesForIdentifiers));
        }
        specifiersList = MegawidgetSpecifierManager.makeRawSpecifiersScrollable(
                specifiersList, METADATA_GROUP_SPECIFIER_PARAMETERS);

        Set<String> overrideOldValuesMetadataKeys = getMegawidgetIdentifiersWithParameter(
                specifiersList, HazardConstants.METADATA_OVERRIDE_OLD_VALUES);
        Set<String> allMetadataKeys = getMegawidgetIdentifiers(specifiersList);
        Set<String> notAffectingModifyFlagMetadataKeys = getMegawidgetIdentifiersWithParameter(
                specifiersList,
                HazardConstants.METADATA_NOT_CONSIDERED_MODIFICATION);
        Set<String> affectingModifyFlagMetadataKeys = new HashSet<>(
                Sets.difference(allMetadataKeys,
                        notAffectingModifyFlagMetadataKeys));

        /*
         * Get the refresh metadata triggering keys, the recommenders triggered
         * for keys, and the edit rise crest fall triggering keys. Each of them
         * are filtered to remove any metadata keys from the previous ones, so
         * that for example an attribute change does not cause a metadata reload
         * and also a recommender execution.
         */
        Set<String> refreshTriggeringMetadataKeys = getMegawidgetIdentifiersWithParameter(
                specifiersList, HazardConstants.METADATA_RELOAD_TRIGGER);
        Map<String, String> recommendersTriggeredForMetadataKeys = getValuesForMegawidgetIdentifiersWithParameter(
                specifiersList, HazardConstants.RECOMMENDER_RUN_TRIGGER);
        recommendersTriggeredForMetadataKeys.keySet()
                .removeAll(refreshTriggeringMetadataKeys);
        Set<String> editRiseCrestFallMetadataKeys = getMegawidgetIdentifiersWithParameter(
                specifiersList, HazardConstants.METADATA_EDIT_RISE_CREST_FALL);
        editRiseCrestFallMetadataKeys.removeAll(refreshTriggeringMetadataKeys);
        editRiseCrestFallMetadataKeys
                .removeAll(recommendersTriggeredForMetadataKeys.keySet());

        try {
            return new HazardEventMetadata(new MegawidgetSpecifierManager(
                    specifiersList, IControlSpecifier.class,
                    timeManager.getCurrentTimeProvider(), sideEffectsApplier),
                    modifiedHazardEvent, refreshTriggeringMetadataKeys,
                    overrideOldValuesMetadataKeys,
                    affectingModifyFlagMetadataKeys,
                    recommendersTriggeredForMetadataKeys,
                    editRiseCrestFallMetadataKeys, scriptFile,
                    eventModifyingFunctionNamesForIdentifiers);
        } catch (MegawidgetSpecificationException e) {
            statusHandler.error("Could not get hazard metadata for event ID = "
                    + event.getEventID() + ":" + e, e);
            return EMPTY_HAZARD_EVENT_METADATA;
        }
    }

    /**
     * Get the set of megawidget identifiers from the specified list, which may
     * contain raw specifiers and their descendants, of any megawidget
     * specifiers.
     * 
     * @param list
     *            List to be checked.
     * @return Set of megawidget identifiers.
     */
    private Set<String> getMegawidgetIdentifiers(List<?> list) {
        Set<String> identifiers = new HashSet<>();
        addMegawidgetIdentifiersToSet(list, identifiers);
        return identifiers;
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
     * never has any raw specifiers in it), and add any found specifiers'
     * identifiers to the specified set.
     * 
     * @param object
     *            Object to be checked.
     * @param identifiers
     *            Set of identifiers to which to add any found specifiers.
     */
    private <V> void addMegawidgetIdentifiersToSet(Object object,
            Set<String> identifiers) {

        /*
         * If the object is a map and it has a field name, add the field name
         * and iterate through the other values, adding any identifiers found
         * therein; otherwise, if it is a list, iterate through the list values,
         * adding any identifiers found therein.
         */
        if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;

            if (map.containsKey(HazardConstants.FIELD_NAME)) {
                identifiers.add((String) map.get(HazardConstants.FIELD_NAME));
            }

            for (Object value : map.values()) {
                addMegawidgetIdentifiersToSet(value, identifiers);
            }
        } else if (object instanceof List) {
            for (Object item : (List<?>) object) {
                addMegawidgetIdentifiersToSet(item, identifiers);
            }
        }
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

            /*
             * If the parameter name being looked for has a non-null value, and
             * that value is either something other than a string, or it is a
             * non-empty string, then treat it as found. (This is done so that
             * an empty string or null as a value associated with this parameter
             * will not be included.
             */
            Object parameterValue = map.get(parameterName);
            if ((parameterValue != null)
                    && ((parameterValue instanceof String == false)
                            || (((String) parameterValue).isEmpty() == false))
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
    public void runEventModifyingScript(final IHazardEvent event,
            final File scriptFile, final String functionName,
            Map<String, Map<String, Object>> mutableProperties,
            final IEventModifyingScriptJobListener listener) {

        /*
         * Run the event-modifying script asynchronously.
         */
        IPythonExecutor<ContextSwitchingPythonEval, ModifiedHazardEvent> executor = new EventModifyingScriptExecutor(
                event, scriptFile, functionName, mutableProperties);
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
                            handleEventModifyingScriptExecutionError(event,
                                    functionName, e);
                        }
                    });
                }
            };
            PYTHON_JOB_COORDINATOR.submitJobWithCallback(executor,
                    pythonJobListener);
        } catch (Exception e) {
            handleEventModifyingScriptExecutionError(event, functionName, e);
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
            IReadableHazardEvent event, String identifier, Throwable e) {
        statusHandler.error(
                "Error executing async event "
                        + "modifying script job for button identifier "
                        + identifier + " on event " + event.getEventID() + ".",
                e);
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
                if (field.getFieldName()
                        .equals(SETTING_HAZARD_CATEGORIES_AND_TYPES)) {
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
                if (field.getFieldName()
                        .equals(SETTING_HAZARD_CATEGORIES_AND_TYPES)) {
                    field.setChoices(
                            getHazardInfoConfig().getHazardCategories());
                } else if (field.getFieldName().equals(SETTING_HAZARD_SITES)) {
                    Set<String> pSites = getSettingsValue(
                            SETTING_HAZARD_POSSIBLE_SITES, settings);
                    List<String> possibleSites = new ArrayList<String>(pSites);
                    Collections.sort(possibleSites);
                    List<Choice> choices = new ArrayList<>(
                            possibleSites.size());
                    for (String site : possibleSites) {
                        choices.add(new Choice(site, site));
                    }
                    field.setChoices(choices);
                } else if (field.getFieldName()
                        .equals(SETTING_VISIBLE_COLUMNS)) {
                    List<String> possibleColumns = new ArrayList<>(
                            settings.getColumns().keySet());
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
        } else if (identifier.equals(HAZARD_EVENT_TYPE)) {
            String s = settings.getDefaultType();
            if ((s == null) || s.isEmpty()) {
                s = startUpConfig.getConfig().getDefaultType();
            }
            value = s;
        } else if (identifier.equals(MAP_CENTER)) {
            MapCenter mapCenter = settings.getMapCenter();
            if ((mapCenter == null)) {
                mapCenter = startUpConfig.getConfig().getMapCenter();
            }
            value = mapCenter;
        } else if (identifier.equals(TIME_RESOLUTION)) {
            TimeResolution timeResolution = settings.getTimeResolution();
            if (timeResolution == null) {
                timeResolution = startUpConfig.getConfig().getTimeResolution();
            }
            value = timeResolution;
        } else if (identifier
                .equals(PRIORITY_FOR_DRAG_AND_DROP_GEOMETRY_EDITS)) {
            DragAndDropGeometryEditSource priorityForDragAndDropGeometryEdits = settings
                    .getPriorityForDragAndDropGeometryEdits();
            if (priorityForDragAndDropGeometryEdits == null) {
                priorityForDragAndDropGeometryEdits = startUpConfig.getConfig()
                        .getPriorityForDragAndDropGeometryEdits();
            }
            value = priorityForDragAndDropGeometryEdits;
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
                timeManager.runAtRegularIntervals(entry.getKey(),
                        entry.getValue());
            } else {
                timeManager.cancelRunAtRegularIntervals(entry.getKey());
            }
        }
    }

    @Override
    public void triggerDataLayerChangeDrivenTool() {

        /*
         * Trigger the tool sequence if there is one to be triggered.
         */
        if (dataLayerChangeTriggeredToolExecutor != null) {
            dataLayerChangeTriggeredToolExecutor.run();
        }
    }

    @Override
    public void triggerFrameChangeDrivenTool() {

        /*
         * Trigger the tool sequence if there is one to be triggered.
         */
        if (frameChangeTriggeredToolExecutor != null) {
            frameChangeTriggeredToolExecutor.run();
        }
    }

    @Override
    public Color getColor(IReadableHazardEvent event) {

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
    private MatchCriteria getMatchCriteria(IReadableHazardEvent event) {
        ParamLevelMatchCriteria match = new ParamLevelMatchCriteria();
        List<String> paramList = Lists
                .newArrayList(HazardEventUtilities.getHazardType(event));
        match.setParameterName(paramList);
        return match;
    }

    @Override
    public double getBorderWidth(IReadableHazardEvent event, boolean selected) {
        if (selected) {
            return 3.5;
        } else {
            return 1.5;
        }
    }

    @Override
    public LineStyle getBorderStyle(IReadableHazardEvent event) {
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
    public String getHeadline(IReadableHazardEvent event) {
        return getHeadline(HazardEventUtilities.getHazardType(event));
    }

    @Override
    public long getDefaultDuration(IReadableHazardEvent event) {
        String type = HazardEventUtilities.getHazardType(event);
        return (type != null
                ? hazardTypes.getConfig().get(type).getDefaultDuration()
                : getSettings().getDefaultDuration());
    }

    @Override
    public List<String> getDurationChoices(IReadableHazardEvent event) {

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

        return durationChoicesForHazardTypes
                .get(HazardEventUtilities.getHazardType(event));
    }

    @Override
    public TimeResolution getTimeResolution(IReadableHazardEvent event) {

        /*
         * If the time resolutions for hazard types map has not yet been
         * initialized, do so now. Include an entry of minutes for the null
         * hazard type, so that hazard events without a type have an entry.
         */
        if (timeResolutionsForHazardTypes == null) {
            timeResolutionsForHazardTypes = new HashMap<>();
            for (Map.Entry<String, HazardTypeEntry> entry : hazardTypes
                    .getConfig().entrySet()) {
                timeResolutionsForHazardTypes.put(entry.getKey(),
                        entry.getValue().getTimeResolution());
            }
            timeResolutionsForHazardTypes.put(null, TimeResolution.MINUTES);
        }
        return timeResolutionsForHazardTypes
                .get(HazardEventUtilities.getHazardType(event));
    }

    @Override
    public String getRecommenderTriggeredByChange(IReadableHazardEvent event,
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
                                statusHandler.warn("\"" + attributeName
                                        + "\" is not a valid hazard event attribute "
                                        + "(found in 'modifyRecommenders' entry for hazard type "
                                        + entry.getKey() + "); skipping.");
                            }
                        }
                    }
                }
                recommendersForTriggersForHazardTypes.put(entry.getKey(), map);
            }
            recommendersForTriggersForHazardTypes.put(null, Collections
                    .<HazardEventFirstClassAttribute, String> emptyMap());
        }

        return recommendersForTriggersForHazardTypes
                .get(HazardEventUtilities.getHazardType(event)).get(change);
    }

    @Override
    public boolean isStartTimeIsCurrentTime(IReadableHazardEvent event) {

        /*
         * If the types-for-which-start-time-is-current-time set has not yet
         * been initialized, do so now.
         */
        if (typesRequiringStartTimeIsCurrentTime == null) {
            typesRequiringStartTimeIsCurrentTime = new HashSet<>();
            for (Map.Entry<String, HazardTypeEntry> entry : hazardTypes
                    .getConfig().entrySet()) {
                if (entry.getValue().isStartTimeIsCurrentTime()) {
                    typesRequiringStartTimeIsCurrentTime.add(entry.getKey());
                }
            }
        }

        return typesRequiringStartTimeIsCurrentTime
                .contains(HazardEventUtilities.getHazardType(event));
    }

    @Override
    public boolean isAllowAnyStartTime(IReadableHazardEvent event) {

        /*
         * If the types-for-which-any-start-time-is-allowed set has not yet been
         * initialized, do so now.
         */
        if (typesAllowingAnyStartTime == null) {
            typesAllowingAnyStartTime = new HashSet<>();
            for (Map.Entry<String, HazardTypeEntry> entry : hazardTypes
                    .getConfig().entrySet()) {
                if (entry.getValue().isAllowAnyStartTime()) {
                    typesAllowingAnyStartTime.add(entry.getKey());
                }
            }
        }

        return typesAllowingAnyStartTime
                .contains(HazardEventUtilities.getHazardType(event));
    }

    @Override
    public boolean isAllowTimeExpand(IReadableHazardEvent event) {

        /*
         * If the types-for-which-end-time-expansion-is-allowed set has not yet
         * been initialized, do so now. Add an entry for the null hazard type
         * too, so that as-yet-untyped hazard events may have their end time
         * changed.
         */
        if (typesAllowingTimeExpand == null) {
            typesAllowingTimeExpand = new HashSet<>();
            for (Map.Entry<String, HazardTypeEntry> entry : hazardTypes
                    .getConfig().entrySet()) {
                if (entry.getValue().isAllowTimeExpand()) {
                    typesAllowingTimeExpand.add(entry.getKey());
                }
            }
            typesAllowingTimeExpand.add(null);
        }

        return typesAllowingTimeExpand
                .contains(HazardEventUtilities.getHazardType(event));
    }

    @Override
    public boolean isAllowTimeShrink(IReadableHazardEvent event) {

        /*
         * If the types-for-which-end-time-shrinkage-is-allowed set has not yet
         * been initialized, do so now. Add an entry for the null hazard type
         * too, so that as-yet-untyped hazard events may have their end time
         * changed.
         */
        if (typesAllowingTimeShrink == null) {
            typesAllowingTimeShrink = new HashSet<>();
            for (Map.Entry<String, HazardTypeEntry> entry : hazardTypes
                    .getConfig().entrySet()) {
                if (entry.getValue().isAllowTimeShrink()) {
                    typesAllowingTimeShrink.add(entry.getKey());
                }
            }
            typesAllowingTimeShrink.add(null);
        }

        return typesAllowingTimeShrink
                .contains(HazardEventUtilities.getHazardType(event));
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
    public List<String> getSessionAttributes(String hazardType) {

        /*
         * If the session attributes for hazard types map has not yet been
         * initialized, do so now.
         */
        if (sessionAttributesForHazardTypes == null) {
            sessionAttributesForHazardTypes = new HashMap<>();
            for (Map.Entry<String, HazardTypeEntry> entry : hazardTypes
                    .getConfig().entrySet()) {
                List<String> sessionAttributes = entry.getValue()
                        .getSessionAttributes();
                sessionAttributesForHazardTypes.put(entry.getKey(),
                        ImmutableList.copyOf(sessionAttributes));
            }

            /*
             * Add an entry for the null hazard type.
             */
            sessionAttributesForHazardTypes.put(null,
                    ImmutableList.copyOf(Collections.<String> emptyList()));
        }

        return sessionAttributesForHazardTypes.get(hazardType);
    }

    @Override
    public String getHazardCategory(IReadableHazardEvent event) {
        if (HazardEventUtilities.isHazardTypeValid(event) == false) {
            return (String) event
                    .getHazardAttribute(HazardConstants.HAZARD_EVENT_CATEGORY);
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
                .getHazardAttribute(HazardConstants.HAZARD_EVENT_CATEGORY);
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

    @Override
    public String[] getBackupSites() {
        return this.backupSites.getSites();
    }

    private class SettingsDirectoryUpdateObserver
            implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            loadAllSettings();
        }

    }

    private class StartUpConfigObserver implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            LocalizationFile file = pathManager.getStaticLocalizationFile(
                    HazardsConfigurationConstants.START_UP_CONFIG_PY);

            Map<String, Object> configLoaderParams = new HashMap<>();
            configLoaderParams.put(HazardConstants.SITE, getSiteID());
            configLoaderParams.put(HazardConstants.DATA_TYPE,
                    HazardsConfigurationConstants.STARTUP_CONFIG_DIR);
            configLoaderParams.put(HazardConstants.INCREMENTAL_OVERRIDE, true);

            startUpConfig = new ConfigLoader<StartUpConfig>(file,
                    StartUpConfig.class, null,
                    PythonBuildPaths.buildIncludePath(), configLoaderParams);

            loaderPool.schedule(startUpConfig);

            /*
             * TODO: Send out notification of change? How to ensure that change
             * propagates?
             */
        }
    }

    private class HazardCategoriesObserver
            implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            LocalizationFile file = pathManager.getStaticLocalizationFile(
                    HazardsConfigurationConstants.HAZARD_CATEGORIES_PY);

            Map<String, Object> configLoaderParams = new HashMap<>();
            configLoaderParams.put(HazardConstants.SITE, getSiteID());
            configLoaderParams.put(HazardConstants.DATA_TYPE,
                    HazardsConfigurationConstants.HAZARD_CATEGORIES_LOCALIZATION_DIR);
            configLoaderParams.put(HazardConstants.INCREMENTAL_OVERRIDE, true);

            hazardCategories = new ConfigLoader<HazardCategories>(file,
                    HazardCategories.class, null,
                    PythonBuildPaths.buildIncludePath(), configLoaderParams);
            loaderPool.schedule(hazardCategories);
            ensureHazardTypeEntriesExistForAllTypes();

            /*
             * TODO: Send out notification of change? How to ensure that change
             * propagates?
             */
        }
    }

    private class HazardMetaDataObserver implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {

            LocalizationFile file = pathManager.getStaticLocalizationFile(
                    HazardsConfigurationConstants.HAZARD_METADATA_PY);

            Map<String, Object> configLoaderParams = new HashMap<>();
            configLoaderParams.put(HazardConstants.SITE, getSiteID());
            configLoaderParams.put(HazardConstants.DATA_TYPE,
                    HazardsConfigurationConstants.HAZARD_METADATA_DIR);
            configLoaderParams.put(HazardConstants.INCREMENTAL_OVERRIDE, true);

            hazardMetaData = new ConfigLoader<HazardMetaData>(file,
                    HazardMetaData.class, null,
                    PythonBuildPaths.buildIncludePath(), configLoaderParams);
            loaderPool.schedule(hazardMetaData);

            /*
             * TODO: Send out notification of change? How to ensure that change
             * propagates?
             */
        }
    }

    private class HazardTypesObserver implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            LocalizationFile file = pathManager.getStaticLocalizationFile(
                    HazardsConfigurationConstants.HAZARD_TYPES_PY);

            Map<String, Object> configLoaderParams = new HashMap<>();
            configLoaderParams.put(HazardConstants.SITE, getSiteID());
            configLoaderParams.put(HazardConstants.DATA_TYPE,
                    HazardsConfigurationConstants.HAZARD_TYPES_LOCALIZATION_DIR);
            configLoaderParams.put(HazardConstants.INCREMENTAL_OVERRIDE, true);

            hazardTypes = new ConfigLoader<HazardTypes>(file, HazardTypes.class,
                    null, PythonBuildPaths.buildIncludePath(),
                    configLoaderParams);
            loaderPool.schedule(hazardTypes);
            ensureHazardTypeEntriesExistForAllTypes();

            /*
             * TODO: Send out notification of change? How to ensure that change
             * propagates?
             */
        }
    }

    private class HazardAlertsConfigObserver
            implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            LocalizationFile file = pathManager.getStaticLocalizationFile(
                    HazardsConfigurationConstants.ALERTS_CONFIG_PATH);

            Map<String, Object> configLoaderParams = new HashMap<>();
            configLoaderParams.put(HazardConstants.SITE, getSiteID());
            configLoaderParams.put(HazardConstants.DATA_TYPE,
                    HazardConstants.ALERT_CONFIG_DATA);
            configLoaderParams.put(HazardConstants.INCREMENTAL_OVERRIDE, true);

            alertsConfig = new ConfigLoader<HazardAlertsConfig>(file,
                    HazardAlertsConfig.class, null,
                    PythonBuildPaths.buildIncludePath(), configLoaderParams);
            loaderPool.schedule(alertsConfig);

            boolean practice = (CAVEMode.getMode() != CAVEMode.OPERATIONAL);
            sessionManager.getAlertsManager().addAlertGenerationStrategy(
                    HazardNotification.class,
                    new HazardEventExpirationAlertStrategy(
                            sessionManager.getAlertsManager(), timeManager,
                            SessionConfigurationManager.this,
                            new HazardEventManager(practice),
                            new AllHazardsFilterStrategy()));
        }

    }

    private class ProductGeneratorTableObserver
            implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            LocalizationFile file = pathManager.getStaticLocalizationFile(
                    HazardsConfigurationConstants.PRODUCT_GENERATOR_TABLE_PY);

            Map<String, Object> configLoaderParams = new HashMap<>();
            configLoaderParams.put(HazardConstants.SITE, getSiteID());
            configLoaderParams.put(HazardConstants.DATA_TYPE,
                    HazardsConfigurationConstants.PRODUCT_GENERATOR_TABLE_DIR);
            configLoaderParams.put(HazardConstants.INCREMENTAL_OVERRIDE, true);

            pgenTable = new ConfigLoader<ProductGeneratorTable>(file,
                    ProductGeneratorTable.class, null,
                    PythonBuildPaths.buildIncludePath(), configLoaderParams);
            loaderPool.schedule(pgenTable);
        }
    }

    private class DefaultConfigObserver implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            LocalizationFile file = pathManager.getStaticLocalizationFile(
                    HazardsConfigurationConstants.DEFAULT_CONFIG_PY);

            Map<String, Object> configLoaderParams = new HashMap<>();
            configLoaderParams.put(HazardConstants.SITE, getSiteID());
            configLoaderParams.put(HazardConstants.DATA_TYPE,
                    HazardConstants.VIEW_CONFIG_DATA);
            configLoaderParams.put(HazardConstants.INCREMENTAL_OVERRIDE, false);

            settingsConfig = new ConfigLoader<SettingsConfig[]>(file,
                    SettingsConfig[].class, "viewConfig",
                    PythonBuildPaths.buildIncludePath(), configLoaderParams);
            loaderPool.schedule(settingsConfig);
        }
    }

    private void ensureHazardTypeEntriesExistForAllTypes() {
        HazardCategories categories = hazardCategories.getConfig();
        HazardTypes types = hazardTypes.getConfig();
        Set<String> missingHazardTypes = null;
        for (String[][] theseTypes : categories.values()) {
            for (String[] thisType : theseTypes) {
                String hazardType = HazardEventUtilities
                        .getHazardType(thisType);
                if (types.containsKey(hazardType) == false) {
                    if (missingHazardTypes == null) {
                        missingHazardTypes = new HashSet<>();
                    }
                    missingHazardTypes.add(hazardType);
                }
            }
        }
        if (missingHazardTypes != null) {
            statusHandler
                    .error("Bad configuration: missing hazard type definitions "
                            + "for the following type(s): "
                            + Joiner.on(", ").join(missingHazardTypes)
                            + ". Please ensure that HazardTypes.py has entries "
                            + "for all of these hazard types.");
        }
    }

    @Override
    public String validateHazardEvent(IReadableHazardEvent event) {
        if (event != null) {
            IPythonExecutor<ContextSwitchingPythonEval, String> executor = new ValidateScriptExecutor(
                    event);
            try {
                return PYTHON_JOB_COORDINATOR.submitJob(executor).get();
            } catch (Exception e) {
                statusHandler
                        .error("Error executing validate job for hazard event: "
                                + event.getEventID(), e);
                return null;
            }
        }
        return null;
    }

    @Override
    public void shutdown() {

        /*
         * Remove the observers for localization file changes, and clear the
         * map.
         */
        for (Entry<LocalizationFile, ILocalizationFileObserver> entry : observersForLocalizationFiles
                .entrySet()) {
            entry.getKey().removeFileUpdatedObserver(entry.getValue());
        }
        observersForLocalizationFiles.clear();

        /*
         * Do nothing with the Python job coordinator. Previously, the
         * PYTHON_JOB_COORDINATOR (which was at that time an instance variable,
         * not a static constant) was shut down, but since Jep and numpy do not
         * play well together when a Jep instance is shut down and then another
         * one started that also uses numpy, the coordinator needs to be kept
         * around and functional in case H.S. starts up again.
         */
    }
}
