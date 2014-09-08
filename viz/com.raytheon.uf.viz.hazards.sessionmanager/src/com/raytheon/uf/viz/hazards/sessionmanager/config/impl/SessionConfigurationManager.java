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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_SITES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_STATES;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
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
import com.raytheon.uf.common.util.FileUtil;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
import com.raytheon.uf.viz.core.jobs.JobPool;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.HazardEventMetadata;
import com.raytheon.uf.viz.hazards.sessionmanager.config.IEventModifyingScriptJobListener;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsLoaded;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardAlertsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardCategories;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardMetaData;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Choice;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.HazardInfoConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Page;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.SettingsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.styles.HazardStyle;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;

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
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionConfigurationManager implements
        ISessionConfigurationManager {

    /**
     * Empty map standing in for an environment map, which will be needed in the
     * future to specify information not specific to a hazard event that affects
     * the generation of metadata for an event.
     */
    private static final Map<String, Serializable> ENVIRONMENT = new HashMap<>();

    public static final String ALERTS_CONFIG_PATH = FileUtil.join(
            "hazardServices", "alerts", "HazardAlertsConfig.xml");

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
                    .error("unexpected error while creating empty megawidget specifier manager",
                            e);
        }
        EMPTY_MEGAWIDGET_SPECIFIER_MANAGER = manager;
    }

    private static final HazardEventMetadata EMPTY_HAZARD_EVENT_METADATA = new HazardEventMetadata(
            EMPTY_MEGAWIDGET_SPECIFIER_MANAGER, null, null);

    private ISessionNotificationSender notificationSender;

    private final JobPool loaderPool = new JobPool(
            "Loading Hazard Services Config", 1);

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

    private ObservedSettings settings;

    private String siteId;

    /**
     * Python job coordinator that handles both metadata fetching scripts and
     * event modifying scripts.
     */
    private final PythonJobCoordinator<ContextSwitchingPythonEval> coordinator = PythonJobCoordinator
            .newInstance(new ConfigScriptFactory());

    private Map<String, ImmutableList<String>> durationChoicesForHazardTypes;

    SessionConfigurationManager() {

    }

    public SessionConfigurationManager(IPathManager pathManager,
            ISessionTimeManager timeManager,
            ISessionNotificationSender notificationSender) {
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

        LocalizationFile file = pathManager
                .getStaticLocalizationFile("hazardServices/startUpConfig/StartUpConfig.py");
        startUpConfig = new ConfigLoader<StartUpConfig>(file,
                StartUpConfig.class);
        loaderPool.schedule(startUpConfig);

        file = pathManager
                .getStaticLocalizationFile("hazardServices/hazardCategories/HazardCategories.py");
        hazardCategories = new ConfigLoader<HazardCategories>(file,
                HazardCategories.class);
        loaderPool.schedule(hazardCategories);

        // THe HazardMetaData needs an include path that includes Hazard
        // Categories.
        StringBuilder metadataIncludes = new StringBuilder();
        metadataIncludes.append(file.getFile().getParent());
        file = pathManager
                .getStaticLocalizationFile("python/VTECutilities/VTECConstants.py");
        metadataIncludes.append(":").append(file.getFile().getParent());
        file = pathManager
                .getStaticLocalizationFile("hazardServices/hazardMetaData/HazardMetaData.py");
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

        file = pathManager
                .getStaticLocalizationFile(HazardsConfigurationConstants.HAZARD_TYPES_PY);
        hazardTypes = new ConfigLoader<HazardTypes>(file, HazardTypes.class);
        loaderPool.schedule(hazardTypes);

        file = pathManager.getStaticLocalizationFile(ALERTS_CONFIG_PATH);
        alertsConfig = new ConfigLoader<HazardAlertsConfig>(file,
                HazardAlertsConfig.class);
        loaderPool.schedule(alertsConfig);

        file = pathManager
                .getStaticLocalizationFile("hazardServices/productGeneratorTable/ProductGeneratorTable.py");
        pgenTable = new ConfigLoader<ProductGeneratorTable>(file,
                ProductGeneratorTable.class);
        loaderPool.schedule(pgenTable);

        file = pathManager
                .getStaticLocalizationFile("python/dataStorage/defaultConfig.py");
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
    public void changeSettings(String settingsId) {
        for (ConfigLoader<Settings> settingsConfig : allSettings) {
            Settings s = settingsConfig.getConfig();
            if (s.getSettingsID().equals(settingsId)) {
                if (settings == null) {
                    settings = new ObservedSettings(this, s);
                    settingsChanged(new SettingsLoaded(this));
                } else {
                    settings.apply(s);
                }
                break;
            }
        }
    }

    @Override
    public Settings getSettings() {
        if (settings != null) {
            return settings;
        } else if (!allSettings.isEmpty()) {
            this.settings = new ObservedSettings(this, allSettings.get(0)
                    .getConfig());
            settingsChanged(new SettingsLoaded(this));
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
            f.write(contents.toString().getBytes());
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
        Settings settings = getSettings();
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
        Settings settings = getSettings();
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
            result.add(settings.getConfig());
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
                String subType = null;
                if (info.length > 2) {
                    subType = info[2];
                }
                Choice child = new Choice();
                String headline = getHeadline(HazardEventUtilities
                        .getHazardType(info[0], info[1], subType));
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
         * Build a megawidget specifier manager out of the metadata. If the file
         * that produced the metadata has an apply-interdependencies entry
         * point, create a side effects applier for it. If it includes a map of
         * event modifiers to the names of scripts that are to be run, remember
         * these.
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
        List<?> specifiersList = (List<?>) result
                .get(HazardConstants.METADATA_KEY);
        if (specifiersList.isEmpty()) {
            return (eventModifyingFunctionNamesForIdentifiers == null ? EMPTY_HAZARD_EVENT_METADATA
                    : new HazardEventMetadata(
                            EMPTY_MEGAWIDGET_SPECIFIER_MANAGER, scriptFile,
                            eventModifyingFunctionNamesForIdentifiers));
        }
        try {
            return new HazardEventMetadata(new MegawidgetSpecifierManager(
                    (List<Map<String, Object>>) specifiersList,
                    IControlSpecifier.class,
                    timeManager.getCurrentTimeProvider(), sideEffectsApplier),
                    scriptFile, eventModifyingFunctionNamesForIdentifiers);
        } catch (MegawidgetSpecificationException e) {
            statusHandler.error("Could not get hazard metadata for event ID = "
                    + hazardEvent.getEventID() + ".", e);
            return EMPTY_HAZARD_EVENT_METADATA;
        }
    }

    @Override
    public void runEventModifyingScript(final IHazardEvent hazardEvent,
            final File scriptFile, final String functionName,
            final IEventModifyingScriptJobListener listener) {

        /*
         * Run the event-modifying script asynchronously.
         */
        IPythonExecutor<ContextSwitchingPythonEval, IHazardEvent> executor = new EventModifyingScriptExecutor(
                hazardEvent, scriptFile, functionName);
        try {
            IPythonJobListener<IHazardEvent> pythonJobListener = new IPythonJobListener<IHazardEvent>() {

                @Override
                public void jobFinished(final IHazardEvent result) {
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
                if (field.getFieldName().equals("hazardCategoriesAndTypes")) {
                    field.setChoices(getHazardInfoConfig()
                            .getHazardCategories());
                }
            }
        }
        return viewConfig;
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
            return LineStyle.DASHED;
        case POTENTIAL:
            return LineStyle.DOTTED;
        default:
            return LineStyle.SOLID;
        }
    }

    @Override
    public String getHeadline(IHazardEvent event) {
        return getHeadline(HazardEventUtilities.getHazardType(
                event.getPhenomenon(), event.getSignificance(),
                event.getSubType()));
    }

    @Override
    public long getDefaultDuration(IHazardEvent event) {
        String type = HazardEventUtilities.getHazardType(event.getPhenomenon(),
                event.getSignificance(), event.getSubType());
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
                durationChoicesForHazardTypes.put(entry.getKey(), ImmutableList
                        .copyOf(entry.getValue().getDurationChoiceList()));
            }
            durationChoicesForHazardTypes.put(null,
                    ImmutableList.copyOf(Collections.<String> emptyList()));
        }

        return durationChoicesForHazardTypes.get(HazardEventUtilities
                .getHazardType(event.getPhenomenon(), event.getSignificance(),
                        event.getSubType()));
    }

    @Override
    public String getHazardCategory(IHazardEvent event) {
        if (event.getPhenomenon() == null) {
            return (String) event
                    .getHazardAttribute(ISessionEventManager.ATTR_HAZARD_CATEGORY);
        }
        for (Entry<String, String[][]> entry : hazardCategories.getConfig()
                .entrySet()) {
            for (String[] str : entry.getValue()) {
                if (str.length >= 2) {
                    if (event.getPhenomenon() == null) {
                        continue;
                    } else if (!event.getPhenomenon().equals(str[0])) {
                        continue;
                    } else if (event.getSignificance() == null) {
                        continue;
                    } else if (!event.getSignificance().equals(str[1])) {
                        continue;
                    }
                    if (str.length >= 3) {
                        if (event.getSubType() == null) {
                            continue;
                        } else if (!event.getSubType().equals(str[2])) {
                            continue;
                        }
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

    @Override
    public void shutdown() {
        coordinator.shutdown();
    }

}
