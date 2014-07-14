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
package gov.noaa.gsd.viz.hazards.utilities;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardCategoryAndTypes;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Choice;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Column;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.MapCenter;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Page;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.SettingsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;

/**
 * Various utility methods used to convert Settings and SettingsConfig to and
 * from maps. These methods need to be used until megawidgets are fixed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2014 2915       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@Deprecated
public class MegawidgetSettingsConversionUtils {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(MegawidgetSettingsConversionUtils.class);

    /**
     * 
     */
    private MegawidgetSettingsConversionUtils() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> settingsPOJOToMap(Settings settings)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Map<String, Object> currentSettingsMap = new LinkedHashMap<String, Object>();

        if (settings instanceof ObservedSettings == false) {
            statusHandler
                    .error("Unable to build map of Current Settings based on type: "
                            + settings.getClass().getName());
            return null;
        }
        ObservedSettings observedSettings = (ObservedSettings) settings;

        // Build the 'hazardCategoriesAndTypes' list of maps
        List<Map<String, Object>> hazardCategoriesAndTypesMapList = new LinkedList<Map<String, Object>>();
        for (HazardCategoryAndTypes hazardCategoryAndTypes : observedSettings
                .getHazardCategoriesAndTypes()) {
            Map<String, Object> hazardCategoriesAndTypesMap = new HashMap<String, Object>();

            hazardCategoriesAndTypesMap.put("displayString",
                    hazardCategoryAndTypes.getDisplayString());
            hazardCategoriesAndTypesMap.put("identifier",
                    hazardCategoryAndTypes.getIdentifier());
            if (hazardCategoryAndTypes.getChildren() != null
                    && hazardCategoryAndTypes.getChildren().isEmpty() == false) {
                hazardCategoriesAndTypesMap.put("children",
                        hazardCategoryAndTypes.getChildren());
            }

            hazardCategoriesAndTypesMapList.add(hazardCategoriesAndTypesMap);
        }
        currentSettingsMap.put("hazardCategoriesAndTypes",
                hazardCategoriesAndTypesMapList);

        // Build the 'column' map
        Map<String, Object> columnsMap = new LinkedHashMap<String, Object>();
        for (String columnKey : observedSettings.getColumns().keySet()) {
            Column column = observedSettings.getColumns().get(columnKey);
            Map<String, Object> columnMap = BeanUtils.describe(column);
            removeNullValues(columnMap);

            columnsMap.put(columnKey, columnMap);
        }
        currentSettingsMap.put("columns", columnsMap);

        // Build the 'mapCenter' map
        Map<String, Object> mapCenterMap = BeanUtils.describe(observedSettings
                .getMapCenter());
        removeNullValues(mapCenterMap);
        currentSettingsMap.put("mapCenter", mapCenterMap);

        // Add 'displayName' to the map
        currentSettingsMap
                .put("displayName", observedSettings.getDisplayName());

        // Add 'defaultDuration' to the map
        currentSettingsMap.put("defaultDuration",
                observedSettings.getDefaultDuration());

        // Add 'settingsID' to the map
        currentSettingsMap.put("settingsID", observedSettings.getSettingsID());

        // Add 'defaultCategory' to the map
        currentSettingsMap.put("defaultCategory",
                observedSettings.getDefaultCategory());

        // Add 'visibleTypes' to the map
        currentSettingsMap.put("visibleTypes",
                observedSettings.getVisibleTypes());

        // Add 'visibleStatuses' to the map
        currentSettingsMap.put("visibleStatuses",
                observedSettings.getVisibleStatuses());

        // Build the 'toolbarTools' list of maps
        List<Map<String, Object>> toolsMapsList = new LinkedList<Map<String, Object>>();
        for (Tool tool : observedSettings.getToolbarTools()) {
            Map<String, Object> toolMap = BeanUtils.describe(tool);
            removeNullValues(toolMap);

            toolsMapsList.add(toolMap);
        }
        currentSettingsMap.put("toolbarTools", toolsMapsList);

        // Add 'defaultTimeDisplayDuration' to the map
        currentSettingsMap.put("defaultTimeDisplayDuration",
                observedSettings.getDefaultTimeDisplayDuration());

        // Add 'visibleSites' to the map
        currentSettingsMap.put("visibleSites",
                observedSettings.getVisibleSites());

        // Add 'visibleColumns' to the map
        currentSettingsMap.put("visibleColumns",
                observedSettings.getVisibleColumns());

        // Add 'staticSettingsID' to the map
        currentSettingsMap.put("staticSettingsID",
                observedSettings.getStaticSettingsID());

        return currentSettingsMap;
    }

    public static Map<String, Object> settingsConfigPOJOToMap(
            SettingsConfig settingsConfig) throws Exception {
        Map<String, Object> settingsConfigMap = new HashMap<String, Object>();

        List<Map<?, ?>> pagesMapList = new LinkedList<Map<?, ?>>();
        for (Page page : settingsConfig.getPages()) {
            Map<String, Object> pagesMap = new HashMap<String, Object>();

            pagesMap.put("pageName", page.getPageName());
            pagesMap.put("numColumns", page.getNumColumns());
            List<Map<String, Object>> pageFields = buildFieldMapList(page
                    .getPageFields().toArray(new Field[0]));
            pagesMap.put("pageFields", pageFields);

            pagesMapList.add(pagesMap);
        }
        settingsConfigMap.put("pages", pagesMapList);
        settingsConfigMap.put("fieldName", settingsConfig.getFieldName());
        settingsConfigMap.put("fieldType", settingsConfig.getFieldType());
        settingsConfigMap.put("leftMargin", settingsConfig.getLeftMargin());
        settingsConfigMap.put("rightMargin", settingsConfig.getRightMargin());
        settingsConfigMap.put("topMargin", settingsConfig.getTopMargin());
        settingsConfigMap.put("bottomMargin", settingsConfig.getBottomMargin());
        settingsConfigMap.put("spacing", settingsConfig.getSpacing());
        settingsConfigMap.put("expandHorizontally",
                settingsConfig.getExpandHorizontally());

        return settingsConfigMap;
    }

    @SuppressWarnings("unchecked")
    public static Settings updateSettingsUsingMap(Settings settings,
            Map<String, Object> settingsMap) {
        if (settings instanceof ObservedSettings == false) {
            statusHandler
                    .error("Unable to build map of Current Settings based on type: "
                            + settings.getClass().getName());
        }

        ObservedSettings updatedSettings = (ObservedSettings) settings;

        // Update the visible types
        updatedSettings.setVisibleTypes((Set<String>) settingsMap
                .get("visibleTypes"));

        // Update the visible states
        if (settingsMap.get("visibleStatuses") instanceof List<?>) {
            Set<String> visibleStatusesSet = new HashSet<String>();
            for (Object state : (List<?>) settingsMap.get("visibleStatuses")) {
                visibleStatusesSet.add(state.toString());
            }
            updatedSettings.setVisibleStatuses(visibleStatusesSet);
        } else {
            updatedSettings.setVisibleStatuses((Set<String>) settingsMap
                    .get("visibleStatuses"));
        }

        // Update the visibleSites
        if (settingsMap.get("visibleSites") instanceof List<?>) {
            Set<String> visibleSitesSet = new HashSet<String>();
            for (Object site : (List<?>) settingsMap.get("visibleSites")) {
                visibleSitesSet.add(site.toString());
            }
            updatedSettings.setVisibleSites(visibleSitesSet);
        } else {
            updatedSettings.setVisibleSites((Set<String>) settingsMap
                    .get("visibleSites"));
        }

        // Update the visibleColumns
        updatedSettings.setVisibleColumns((List<String>) settingsMap
                .get("visibleColumns"));

        // Update the Hazards Categories & Types
        List<HazardCategoryAndTypes> hazardCategoriesAndTypesList = new ArrayList<HazardCategoryAndTypes>();
        List<Map<String, Object>> hazardCategoriesAndTypesMapList = (List<Map<String, Object>>) settingsMap
                .get("hazardCategoriesAndTypes");
        for (Map<String, Object> hcatMap : hazardCategoriesAndTypesMapList) {
            HazardCategoryAndTypes hazardCategoryAndTypes = new HazardCategoryAndTypes();
            if (hcatMap.containsKey("displayString")) {
                hazardCategoryAndTypes.setDisplayString(hcatMap.get(
                        "displayString").toString());
            }
            if (hcatMap.containsKey("identifier")) {
                hazardCategoryAndTypes.setIdentifier(hcatMap.get("identifier")
                        .toString());
            }
            if (hcatMap.containsKey("children")) {
                hazardCategoryAndTypes.setChildren((List<String>) hcatMap
                        .get("children"));
            }
            hazardCategoriesAndTypesList.add(hazardCategoryAndTypes);
        }
        updatedSettings
                .setHazardCategoriesAndTypes(hazardCategoriesAndTypesList
                        .toArray(new HazardCategoryAndTypes[0]));

        // Update the Columns
        Map<String, Column> updatedColumnsMap = new HashMap<String, Column>();
        Map<String, Object> columnsMap = (Map<String, Object>) settingsMap
                .get("columns");
        for (String columnMapKey : columnsMap.keySet()) {
            Map<String, Object> columnMap = (Map<String, Object>) columnsMap
                    .get(columnMapKey);
            Column column = new Column();
            if (columnMap.containsKey("width")) {
                column.setWidth(NumberUtils.toInt(columnMap.get("width")
                        .toString()));
            }
            if (columnMap.containsKey("type")) {
                column.setType(columnMap.get("type").toString());
            }
            if (columnMap.containsKey("fieldName")) {
                column.setFieldName(columnMap.get("fieldName").toString());
            }
            if (columnMap.containsKey("hintTextFieldName")) {
                column.setHintTextFieldName(columnMap.get("hintTextFieldName")
                        .toString());
            }
            if (columnMap.containsKey("sortDir")) {
                column.setSortDir(columnMap.get("sortDir").toString());
            }
            if (columnMap.containsKey("displayEmptyAs")) {
                column.setDisplayEmptyAs(columnMap.get("displayEmptyAs")
                        .toString());
            }
            updatedColumnsMap.put(columnMapKey, column);
        }
        updatedSettings.setColumns(updatedColumnsMap);

        // Update the map center
        Map<String, Object> mapCenterMap = (Map<String, Object>) settingsMap
                .get("mapCenter");
        MapCenter mapCenter = new MapCenter();
        if (mapCenterMap.containsKey("lat")) {
            mapCenter.setLat(NumberUtils.toDouble(mapCenterMap.get("lat")
                    .toString()));
        }
        if (mapCenterMap.containsKey("lon")) {
            mapCenter.setLon(NumberUtils.toDouble(mapCenterMap.get("lon")
                    .toString()));
        }
        if (mapCenterMap.containsKey("zoom")) {
            mapCenter.setZoom(NumberUtils.toDouble(mapCenterMap.get("zoom")
                    .toString()));
        }
        updatedSettings.setMapCenter(mapCenter);

        // Update the display name
        updatedSettings.setDisplayName(settingsMap.get("displayName")
                .toString());

        // Update the default duration
        updatedSettings.setDefaultDuration(NumberUtils.toLong(settingsMap.get(
                "defaultDuration").toString()));

        // Update the default category
        updatedSettings.setDefaultCategory(settingsMap.get("defaultCategory")
                .toString());

        // Update the toolbar tools
        List<Tool> toolbarTools = new ArrayList<Tool>();
        List<Map<String, Object>> toolsMapsList = (List<Map<String, Object>>) settingsMap
                .get("toolbarTools");
        for (Map<String, Object> toolbarMap : toolsMapsList) {
            Tool tool = new Tool();
            if (toolbarMap.containsKey("toolName")) {
                tool.setToolName(toolbarMap.get("toolName").toString());
            }
            if (toolbarMap.containsKey("displayName")) {
                tool.setDisplayName(toolbarMap.get("displayName").toString());
            }
            toolbarTools.add(tool);
        }
        updatedSettings.setToolbarTools(toolbarTools);

        // Update defaultTimeDisplayDuration
        updatedSettings.setDefaultTimeDisplayDuration(NumberUtils
                .toLong(settingsMap.get("defaultTimeDisplayDuration")
                        .toString()));

        // Update the staticSettingsID
        if (settingsMap.containsKey("staticSettingsID")
                && settingsMap.get("staticSettingsID") != null) {
            updatedSettings.setStaticSettingsID(settingsMap.get(
                    "staticSettingsID").toString());
        }

        settings.apply(updatedSettings);
        return settings;
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> buildFieldMapList(Field[] fields)
            throws Exception {
        List<Map<String, Object>> fieldMapList = new ArrayList<Map<String, Object>>();
        for (Field field : fields) {
            Map<String, Object> fieldMap = BeanUtils.describe(field);
            removeNullValues(fieldMap);

            /* adapt to limitations of megawidgets */
            if (fieldMap.containsKey("lines")) {
                fieldMap.remove("lines");

                fieldMap.put("lines", field.getLines().intValue());
            }
            if (fieldMap.containsKey("leftMargin")) {
                fieldMap.remove("leftMargin");

                fieldMap.put("leftMargin", field.getLeftMargin().intValue());
            }
            if (fieldMap.containsKey("rightMargin")) {
                fieldMap.remove("rightMargin");

                fieldMap.put("rightMargin", field.getRightMargin().intValue());
            }
            if (fieldMap.containsKey("topMargin")) {
                fieldMap.remove("topMargin");

                fieldMap.put("topMargin", field.getTopMargin().intValue());
            }
            if (fieldMap.containsKey("bottomMargin")) {
                fieldMap.remove("bottomMargin");

                fieldMap.put("bottomMargin", field.getBottomMargin().intValue());
            }

            /* update choices to be a List of Maps */
            if (fieldMap.containsKey("choices")) {
                fieldMap.remove("choices");

                fieldMap.put("choices",
                        generateEmbeddedChoiceMapList(field.getChoices()));
            }
            if (fieldMap.containsKey("fields")) {
                fieldMap.remove("fields");

                fieldMap.put("fields", buildFieldMapList(field.getFields()
                        .toArray(new Field[0])));
            }
            fieldMapList.add(fieldMap);
        }

        return fieldMapList;
    }

    private static void removeNullValues(Map<String, Object> dataMap) {
        List<String> fieldsToRemove = new ArrayList<String>();
        for (String fieldNameKey : dataMap.keySet()) {
            if (dataMap.get(fieldNameKey) == null) {
                fieldsToRemove.add(fieldNameKey);
            }
        }
        for (String fieldToRemove : fieldsToRemove) {
            dataMap.remove(fieldToRemove);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Map<?, ?>> generateEmbeddedChoiceMapList(
            List<Choice> choices) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        List<Map<?, ?>> choicesMapList = new ArrayList<Map<?, ?>>();
        for (Choice choice : choices) {
            Map<String, Object> choiceMap = BeanUtils.describe(choice);
            removeNullValues(choiceMap);
            if (choiceMap.containsKey("children")) {
                choiceMap.remove("children");
            }

            if (choice.getChildren() != null
                    && choice.getChildren().isEmpty() == false) {
                choiceMap.put("children",
                        generateEmbeddedChoiceMapList(choice.getChildren()));
            }
            choicesMapList.add(choiceMap);
        }
        return choicesMapList;
    }
}