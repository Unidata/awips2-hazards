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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_CATEGORIES_AND_TYPES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_POSSIBLE_SITES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_SITES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_STATES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_TYPES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_VISIBLE_COLUMNS;

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

import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardCategoryAndTypes;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Choice;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Column;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.MapCenter;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Page;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.SettingsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

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
 * Dec 05, 2014 4124       Chris.Golden Changed to work with newly parameterized
 *                                      config manager.
 * Feb 12, 2015 6456       Dan Schaffer Fixed bug where tools menu was empty after changing settings
 * Feb 23, 2015 3618       Chris.Golden Added possible sites to settings, and fixed a couple of
 *                                      problems with expandHorizontally and expandVertically.
 *                                      Also fixed bugs caused by hazard category and types object
 *                                      sometimes having a list of maps for its children instead of
 *                                      a list of strings.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

@Deprecated
public class MegawidgetSettingsConversionUtils {

    /**
     * 
     */
    private MegawidgetSettingsConversionUtils() {
    }

    /**
     * Given the specified list, which may be of child names (strings) or maps
     * holding child names as identifiers, return a list of strings holding
     * their names.
     * 
     * @param childList
     *            of child names, in one of the forms listed above.
     * @return List of strings holding the names.
     */
    private static List<String> getChildrenNamesFromList(List<?> childList) {
        List<String> result = new ArrayList<>(childList.size());
        for (Object child : childList) {
            if (child instanceof String) {
                result.add((String) child);
            } else {
                Map<?, ?> childMap = (Map<?, ?>) child;
                result.add((String) childMap.get("identifier"));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> settingsPOJOToMap(
            ObservedSettings settings) throws IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Map<String, Object> currentSettingsMap = new LinkedHashMap<String, Object>();

        // Build the 'hazardCategoriesAndTypes' list of maps
        List<Map<String, Object>> hazardCategoriesAndTypesMapList = new LinkedList<Map<String, Object>>();
        for (HazardCategoryAndTypes hazardCategoryAndTypes : settings
                .getHazardCategoriesAndTypes()) {
            Map<String, Object> hazardCategoriesAndTypesMap = new HashMap<String, Object>();

            hazardCategoriesAndTypesMap.put("displayString",
                    hazardCategoryAndTypes.getDisplayString());
            hazardCategoriesAndTypesMap.put("identifier",
                    hazardCategoryAndTypes.getIdentifier());
            if (hazardCategoryAndTypes.getChildren() != null
                    && hazardCategoryAndTypes.getChildren().isEmpty() == false) {
                hazardCategoriesAndTypesMap.put("children",
                        getChildrenNamesFromList(hazardCategoryAndTypes
                                .getChildren()));
            }

            hazardCategoriesAndTypesMapList.add(hazardCategoriesAndTypesMap);
        }
        currentSettingsMap.put(SETTING_HAZARD_CATEGORIES_AND_TYPES,
                hazardCategoriesAndTypesMapList);

        // Build the 'column' map
        Map<String, Object> columnsMap = new LinkedHashMap<String, Object>();
        for (String columnKey : settings.getColumns().keySet()) {
            Column column = settings.getColumns().get(columnKey);
            Map<String, Object> columnMap = BeanUtils.describe(column);
            removeNullValues(columnMap);

            columnsMap.put(columnKey, columnMap);
        }
        currentSettingsMap.put("columns", columnsMap);

        // Build the 'mapCenter' map
        Map<String, Object> mapCenterMap = BeanUtils.describe(settings
                .getMapCenter());
        removeNullValues(mapCenterMap);
        currentSettingsMap.put("mapCenter", mapCenterMap);

        // Add 'displayName' to the map
        currentSettingsMap.put("displayName", settings.getDisplayName());

        // Add 'defaultDuration' to the map
        currentSettingsMap
                .put("defaultDuration", settings.getDefaultDuration());

        // Add 'settingsID' to the map
        currentSettingsMap.put("settingsID", settings.getSettingsID());

        // Add 'defaultCategory' to the map
        currentSettingsMap
                .put("defaultCategory", settings.getDefaultCategory());

        // Add 'visibleTypes' to the map
        currentSettingsMap
                .put(SETTING_HAZARD_TYPES, settings.getVisibleTypes());

        // Add 'visibleStatuses' to the map
        currentSettingsMap.put(SETTING_HAZARD_STATES,
                settings.getVisibleStatuses());

        // Build the 'toolbarTools' list of maps
        List<Map<String, Object>> toolsMapsList = new LinkedList<Map<String, Object>>();
        for (Tool tool : settings.getToolbarTools()) {
            Map<String, Object> toolMap = BeanUtils.describe(tool);
            removeNullValues(toolMap);

            toolsMapsList.add(toolMap);
        }
        currentSettingsMap.put("toolbarTools", toolsMapsList);

        // Add 'defaultTimeDisplayDuration' to the map
        currentSettingsMap.put("defaultTimeDisplayDuration",
                settings.getDefaultTimeDisplayDuration());

        // Add 'possibleSites' to the map
        currentSettingsMap.put(SETTING_HAZARD_POSSIBLE_SITES,
                settings.getPossibleSites());

        // Add 'visibleSites' to the map
        currentSettingsMap
                .put(SETTING_HAZARD_SITES, settings.getVisibleSites());

        // Add 'visibleColumns' to the map
        currentSettingsMap.put(SETTING_VISIBLE_COLUMNS,
                settings.getVisibleColumns());

        // Add 'staticSettingsID' to the map
        currentSettingsMap.put("staticSettingsID",
                settings.getStaticSettingsID());

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
        settingsConfigMap.put("expandVertically",
                settingsConfig.getExpandVertically());

        return settingsConfigMap;
    }

    @SuppressWarnings("unchecked")
    public static ObservedSettings updateSettingsUsingMap(
            ObservedSettings settings, Map<String, Object> settingsMap,
            IOriginator originator) {

        ObservedSettings updatedSettings = settings;

        // Update the visible types
        updatedSettings.setVisibleTypes((Set<String>) settingsMap
                .get(SETTING_HAZARD_TYPES));

        // Update the visible states
        if (settingsMap.get(SETTING_HAZARD_STATES) instanceof List<?>) {
            Set<String> visibleStatusesSet = new HashSet<String>();
            for (Object state : (List<?>) settingsMap
                    .get(SETTING_HAZARD_STATES)) {
                visibleStatusesSet.add(state.toString());
            }
            updatedSettings.setVisibleStatuses(visibleStatusesSet);
        } else {
            updatedSettings.setVisibleStatuses((Set<String>) settingsMap
                    .get(SETTING_HAZARD_STATES));
        }

        // Update the possibleSites
        if (settingsMap.get(SETTING_HAZARD_POSSIBLE_SITES) instanceof List<?>) {
            Set<String> possibleSitesSet = new HashSet<String>();
            for (Object site : (List<?>) settingsMap
                    .get(SETTING_HAZARD_POSSIBLE_SITES)) {
                possibleSitesSet.add(site.toString());
            }
            updatedSettings.setPossibleSites(possibleSitesSet);
        } else {
            updatedSettings.setPossibleSites((Set<String>) settingsMap
                    .get(SETTING_HAZARD_POSSIBLE_SITES));
        }

        // Update the visibleSites
        if (settingsMap.get(SETTING_HAZARD_SITES) instanceof List<?>) {
            Set<String> visibleSitesSet = new HashSet<String>();
            for (Object site : (List<?>) settingsMap.get(SETTING_HAZARD_SITES)) {
                visibleSitesSet.add(site.toString());
            }
            updatedSettings.setVisibleSites(visibleSitesSet);
        } else {
            updatedSettings.setVisibleSites((Set<String>) settingsMap
                    .get(SETTING_HAZARD_SITES));
        }

        // Update the visibleColumns
        updatedSettings.setVisibleColumns((List<String>) settingsMap
                .get(SETTING_VISIBLE_COLUMNS));

        // Update the Hazards Categories & Types
        List<HazardCategoryAndTypes> hazardCategoriesAndTypesList = new ArrayList<HazardCategoryAndTypes>();
        List<Map<String, Object>> hazardCategoriesAndTypesMapList = (List<Map<String, Object>>) settingsMap
                .get(SETTING_HAZARD_CATEGORIES_AND_TYPES);
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
                hazardCategoryAndTypes
                        .setChildren(getChildrenNamesFromList((List<?>) hcatMap
                                .get("children")));
            }
            hazardCategoriesAndTypesList.add(hazardCategoryAndTypes);
        }
        updatedSettings.setHazardCategoriesAndTypes(
                hazardCategoriesAndTypesList
                        .toArray(new HazardCategoryAndTypes[0]), originator);

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
            if (toolbarMap.containsKey("toolType")) {
                String toolTypeAsString = (String) toolbarMap.get("toolType");
                ToolType toolType = ToolType.fromString(toolTypeAsString
                        .toUpperCase());
                tool.setToolType(toolType);
            }
            if (toolbarMap.containsKey("visible")) {
                String visibleAsString = (String) toolbarMap.get("visible");
                Boolean visible = Boolean.valueOf(visibleAsString);
                tool.setVisible(visible);
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

        settings.apply(updatedSettings, originator);
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
            if (fieldMap.containsKey("expandHorizontally")) {
                fieldMap.remove("expandHorizontally");

                fieldMap.put("expandHorizontally", field
                        .getExpandHorizontally().booleanValue());
            }
            if (fieldMap.containsKey("expandVertically")) {
                fieldMap.remove("expandVertically");

                fieldMap.put("expandVertically", field.getExpandVertically()
                        .booleanValue());
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
