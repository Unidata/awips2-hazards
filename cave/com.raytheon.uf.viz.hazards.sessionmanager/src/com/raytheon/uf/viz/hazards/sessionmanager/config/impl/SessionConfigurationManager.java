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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.localization.FileUpdatedMessage;
import com.raytheon.uf.common.localization.ILocalizationFileObserver;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.util.FileUtil;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
import com.raytheon.uf.viz.core.jobs.JobPool;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsLoaded;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardAlertsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardCategories;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardMetaData;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardMetaDataEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardTypeEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardTypes;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.IHazardsColorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.JsonHazardsColorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.XmlHazardsColorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Choice;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.HazardInfoConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.HazardInfoOptionEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.HazardInfoOptions;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Page;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.SettingsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;

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
 * May 21, 2013 1257       bsteffen    Initial creation
 * Aug 01, 2013  1325      daniel.s.schaffer@noaa.gov     Added support for alerting
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionConfigurationManager implements
        ISessionConfigurationManager {

    public static final String ALERTS_CONFIG_PATH = FileUtil.join(
            "hazardServices", "alerts", "HazardAlertsConfig.xml");

    static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SessionConfigurationManager.class);

    private static final Color WHITE = new Color(1.0f, 1.0f, 1.0f);

    private final ISessionNotificationSender notificationSender;

    private final JobPool loaderPool = new JobPool(
            "Loading Hazard Services Config", 1);

    private final IPathManager pathManager;

    private List<ConfigLoader<Settings>> allSettings;

    private final ConfigLoader<StartUpConfig> startUpConfig;

    private final ConfigLoader<HazardCategories> hazardCategories;

    private final ConfigLoader<HazardMetaData> hazardMetaData;

    private final ConfigLoader<ProductGeneratorTable> pgenTable;

    private final List<ConfigLoader<? extends IHazardsColorTable>> colorTables;

    private final ConfigLoader<HazardTypes> hazardTypes;

    private final ConfigLoader<HazardAlertsConfig> alertsConfig;

    private final ConfigLoader<SettingsConfig[]> settingsConfig;

    private ObservedSettings settings;

    private String siteId;

    public SessionConfigurationManager(IPathManager pathManager,
            ISessionNotificationSender notificationSender) {
        this.pathManager = pathManager;
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
                .getStaticLocalizationFile("python/dataStorage/ColorTable.xml");
        ConfigLoader<XmlHazardsColorTable> xmlColorTable = new ConfigLoader<XmlHazardsColorTable>(
                file, XmlHazardsColorTable.class);
        loaderPool.schedule(xmlColorTable);

        file = pathManager
                .getStaticLocalizationFile("python/dataStorage/ColorTable.json");
        ConfigLoader<JsonHazardsColorTable> jsonColorTable = new ConfigLoader<JsonHazardsColorTable>(
                file, JsonHazardsColorTable.class);
        loaderPool.schedule(jsonColorTable);

        colorTables = new ArrayList<ConfigLoader<? extends IHazardsColorTable>>(
                2);
        colorTables.add(xmlColorTable);
        colorTables.add(jsonColorTable);

        file = pathManager
                .getStaticLocalizationFile("hazardServices/hazardTypes/HazardTypes.py");
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
        LocalizationFile[] files = pathManager.listStaticFiles(
                "hazardServices/settings/", null, false, true);
        List<ConfigLoader<Settings>> allSettings = new ArrayList<ConfigLoader<Settings>>();
        for (LocalizationFile file : files) {
            ConfigLoader<Settings> loader = new ConfigLoader<Settings>(file,
                    Settings.class);
            allSettings.add(loader);
            loaderPool.schedule(loader);
            if (previousPersisted != null) {
                try {
                    String fileName = file.getFile(false).getName();
                    if (fileName.startsWith(previousPersisted.getSettingsID()
                            + ".")) {
                        Settings s = loader.getConfig();
                        if (s.getSettingsID().equals(
                                previousPersisted.getSettingsID())) {
                            settings.applyPersistedChanges(previousPersisted, s);
                            previousPersisted = null;
                        }
                    }
                } catch (LocalizationException e) {
                    statusHandler.handle(Priority.PROBLEM,
                            e.getLocalizedMessage(), e);
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
        LocalizationContext context = pathManager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.USER);
        Settings settings = getSettings();
        LocalizationFile f = this.pathManager.getLocalizationFile(context,
                "hazardServices/settings/" + settings.getSettingsID() + ".py");
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
    public List<Settings> getSettingsList() {
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
                String headline = getHeadline(info[0], info[1], subType);
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

    @Override
    public HazardInfoOptions getHazardInfoOptions() {
        HazardInfoOptions opt = new HazardInfoOptions();
        for (HazardMetaDataEntry entry : hazardMetaData.getConfig()) {
            HazardInfoOptionEntry optEnt = new HazardInfoOptionEntry();
            List<String> types = new ArrayList<String>();
            for (String[] type : entry.getHazardTypes()) {
                String subType = null;
                if (type.length > 2) {
                    subType = type[2];
                }
                StringBuilder id = new StringBuilder();
                for (String str : type) {
                    if (str != type[0]) {
                        id.append(".");
                    }
                    id.append(str);
                }
                String headline = getHeadline(type[0], type[1], subType);
                if (headline != null) {
                    id.append(" (");
                    id.append(headline);
                    id.append(")");
                }
                types.add(id.toString());
            }
            optEnt.setHazardTypes(types);
            optEnt.setMetaData(entry.getMetaData());
            opt.add(optEnt);
        }
        return opt;
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
                } else if (field.getFieldName().equals("visibleSites")) {
                    config[1] = new Field(field);
                    config[1].setFieldType("CheckBoxesMenu");
                    config[1].setLabel("Site &IDs");
                } else if (field.getFieldName().equals("visibleStates")) {
                    config[2] = new Field(field);
                    config[2].setFieldType("CheckBoxesMenu");
                    config[2].setLabel("&States");
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
        for (ConfigLoader<? extends IHazardsColorTable> colorTable : colorTables) {
            Color color = colorTable.getConfig().getColor(event);
            if (color != null) {
                return color;
            }
        }
        return WHITE;
    }

    @Override
    public int getBorderWidth(IHazardEvent event) {
        if (Boolean.TRUE.equals(event
                .getHazardAttribute(ISessionEventManager.ATTR_SELECTED))) {
            return 4;
        } else {
            return 1;
        }
    }

    @Override
    public LineStyle getBorderStyle(IHazardEvent event) {
        switch (event.getState()) {
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
        return getHeadline(event.getPhenomenon(), event.getSignificance(),
                event.getSubtype());
    }

    @Override
    public String getHazardCategory(IHazardEvent event) {
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
                        if (event.getSubtype() == null) {
                            continue;
                        } else if (!event.getSubtype().equals(str[2])) {
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
        notificationSender.postNotification(notification);
    }

    private String getHeadline(String phen, String sig, String subType) {
        if (phen == null || sig == null) {
            return null;
        }
        String key = phen + "." + sig;
        if (subType != null) {
            key += "." + subType;
        }
        HazardTypeEntry type = hazardTypes.getConfig().get(key);
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
        /**
         * Nothing to do right now.
         */
    }

}
