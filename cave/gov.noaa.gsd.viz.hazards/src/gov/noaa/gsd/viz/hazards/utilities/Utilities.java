/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.utilities;

import gov.noaa.gsd.common.utilities.Utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Description: General utility methods specific to Hazard Services.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            daniel.s.schaffer      Initial induction into repo
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
public class Utilities {

    // Public Static Constants

    /**
     * ID of plug-in containing the Hazard Services SessionManager code.
     */
    public static final String SESSION_MANAGER_PLUGIN = "gov.noaa.gsd.viz.hazards.sessionmanager";

    /**
     * GSD Python plugins.
     */
    public static final List<String> GSD_PYTHON_PLUGINS = Lists.newArrayList(
            SESSION_MANAGER_PLUGIN,
            "gov.noaa.gsd.viz.hazards.datatransformation",
            "gov.noaa.gsd.viz.hazards.database");

    /**
     * Minimum time as an epoch time in milliseconds.
     */
    public static final long MIN_TIME = 0L;

    /**
     * Maximum time as an epoch time in milliseconds. This is arbitrarily large,
     * but not so large as to be anywhere close to the limit of what a long
     * integer value can represent, so that accidental overflow during
     * calculations does not occur.
     */
    public static final long MAX_TIME = Long.MAX_VALUE / 2L;

    /**
     * Event identifier key in hazard event dictionary.
     */
    public static final String HAZARD_EVENT_IDENTIFIER = "eventID";

    /**
     * Category key for hazard event dictionary.
     */
    public static final String HAZARD_EVENT_CATEGORY = "hazardCategory";

    /**
     * Type key for hazard event dictionary.
     */
    public static final String HAZARD_EVENT_TYPE = "type";

    /**
     * Phen key for hazard event dictionary.
     */
    public static final String HAZARD_EVENT_PHEN = "phen";

    /**
     * Sig key for hazard event dictionary.
     */
    public static final String HAZARD_EVENT_SIG = "sig";

    /**
     * Sub-type key for hazard event dictionary.
     */
    public static final String HAZARD_EVENT_SUB_TYPE = "subType";

    /**
     * Full type key for hazard event dictionary.
     */
    public static final String HAZARD_EVENT_FULL_TYPE = "fullType";

    /**
     * VTEC mode key for hazard event dictionary.
     */
    public static final String HAZARD_EVENT_VTEC_MODE = "VTECmode";

    /**
     * Start time key in hazard event dictionary.
     */
    public static final String HAZARD_EVENT_START_TIME = "startTime";

    /**
     * End time key in hazard event dictionary.
     */
    public static final String HAZARD_EVENT_END_TIME = "endTime";

    /**
     * Group identifier key in hazard event dictionary.
     */
    public static final String HAZARD_EVENT_GROUP_IDENTIFIER = "groupID";

    /**
     * Shapes key in hazard event dictionary.
     */
    public static final String HAZARD_EVENT_SHAPES = "shapes";

    /**
     * Shape type key in shape dictionary in shapes list in hazard event
     * dictionary.
     */
    public static final String HAZARD_EVENT_SHAPE_TYPE = "shapeType";

    /**
     * Circle shape type value in shape dictionary in shapes list in hazard
     * event dictionary.
     */
    public static final String HAZARD_EVENT_SHAPE_TYPE_CIRCLE = "circle";

    /**
     * Point shape type value in shape dictionary in shapes list in hazard event
     * dictionary.
     */
    public static final String HAZARD_EVENT_SHAPE_TYPE_POINT = "point";

    /**
     * Line shape type value in shape dictionary in shapes list in hazard event
     * dictionary.
     */
    public static final String HAZARD_EVENT_SHAPE_TYPE_LINE = "line";

    /**
     * Polygon shape type value in shape dictionary in shapes list in hazard
     * event dictionary.
     */
    public static final String HAZARD_EVENT_SHAPE_TYPE_POLYGON = "polygon";

    /**
     * Dot shape type value in shape dictionary in shapes list in hazard event
     * dictionary.
     */
    public static final String HAZARD_EVENT_SHAPE_TYPE_DOT = "dot";

    /**
     * Star shape type value in shape dictionary in shapes list in hazard event
     * dictionary.
     */
    public static final String HAZARD_EVENT_SHAPE_TYPE_STAR = "star";

    /**
     * Event-is-checked key in hazard event dictionary.
     */
    public static final String HAZARD_EVENT_CHECKED = "checked";

    /**
     * Color key in hazard event dictionary.
     */
    public static final String HAZARD_EVENT_COLOR = "color";

    /**
     * Event-is-selected key in hazard event dictionary.
     */
    public static final String HAZARD_EVENT_SELECTED = "selected";

    /**
     * State key in hazard event dictionary.
     */
    public static final String HAZARD_EVENT_STATE = "state";

    /**
     * Pending state value in hazard event dictionary.
     */
    public static final String HAZARD_EVENT_STATE_PENDING = "pending";

    /**
     * Potential state value in hazard event dictionary.
     */
    public static final String HAZARD_EVENT_STATE_POTENTIAL = "potential";

    /**
     * Proposed state value in hazard event dictionary.
     */
    public static final String HAZARD_EVENT_STATE_PROPOSED = "proposed";

    /**
     * Issued state value in hazard event dictionary.
     */
    public static final String HAZARD_EVENT_STATE_ISSUED = "issued";

    /**
     * Ended state value in hazard event dictionary.
     */
    public static final String HAZARD_EVENT_STATE_ENDED = "ended";

    /**
     * Column identifier key in column definition dictionary in setting
     * dictionary.
     */
    public static final String SETTING_COLUMN_IDENTIFIER = "fieldName";

    /**
     * Column hint text identifier key in column definition dictionary in
     * setting dictionary.
     */
    public static final String SETTING_COLUMN_HINT_TEXT_IDENTIFIER = "hintTextFieldName";

    /**
     * Column width key in column definition dictionary in setting dictionary.
     */
    public static final String SETTING_COLUMN_WIDTH = "width";

    /**
     * Column type key in column definition dictionary in setting dictionary.
     */
    public static final String SETTING_COLUMN_TYPE = "type";

    /**
     * String column type value in column definition dictionary in setting
     * dictionary.
     */
    public static final String SETTING_COLUMN_TYPE_STRING = "string";

    /**
     * Date column type value in column definition dictionary in setting
     * dictionary.
     */
    public static final String SETTING_COLUMN_TYPE_DATE = "date";

    /**
     * Number column type value in column definition dictionary in setting
     * dictionary.
     */
    public static final String SETTING_COLUMN_TYPE_NUMBER = "number";

    /**
     * Visible columns key in setting dictionary.
     */
    public static final String SETTING_VISIBLE_COLUMNS = "visibleColumns";

    /**
     * Sort direction key in setting dictionary.
     */
    public static final String SETTING_COLUMN_SORT_DIRECTION = "sortDir";

    /**
     * Ascending sort direction value in column definition dictionary in setting
     * dictionary.
     */
    public static final String SETTING_COLUMN_SORT_DIRECTION_ASCENDING = "ascending";

    /**
     * Descending sort direction value in column definition dictionary in
     * setting dictionary.
     */
    public static final String SETTING_COLUMN_SORT_DIRECTION_DESCENDING = "descending";

    /**
     * No sort direction value in column definition dictionary in setting
     * dictionary.
     */
    public static final String SETTING_COLUMN_SORT_DIRECTION_NONE = "none";

    /**
     * Columns key in setting dictionary.
     */
    public static final String SETTING_COLUMNS = "columns";

    /**
     * Hazard categories and types key in setting dictionary.
     */
    public static final String SETTING_HAZARD_CATEGORIES_AND_TYPES = "hazardCategoriesAndTypes";

    /**
     * Hazard categories key setting dictionary.
     */
    public static final String SETTING_HAZARD_CATEGORIES = "hidHazardCategories";

    /**
     * Hazard types key in setting dictionary.
     */
    public static final String SETTING_HAZARD_TYPES = "visibleTypes";

    /**
     * Settings list in settings dictionary.
     */
    public static final String SETTINGS_LIST = "settingsList";

    /**
     * Setting identifier in setting definition in settings list.
     */
    public static final String SETTINGS_LIST_IDENTIFIER = "settingsID";

    /**
     * Current setting identifier in settings dictionary.
     */
    public static final String SETTINGS_CURRENT_IDENTIFIER = "currentSettingsID";

    /**
     * Hazard events list key in temporal display component data dictionary.
     */
    public static final String TEMPORAL_DISPLAY_EVENTS = "events";

    /**
     * Dynamic setting in temporal display component data dictionary.
     */
    public static final String TEMPORAL_DISPLAY_DYNAMIC_SETTING = "dynamicSettings";

    /**
     * General hazard information configuration item key.
     */
    public static final String HAZARD_INFO_GENERAL_CONFIG = "hazardInfoConfig";

    /**
     * General megawidgets key in hazard information dialog dictionary.
     */
    public static final String HAZARD_INFO_GENERAL_CONFIG_WIDGETS = "hazardCategories";

    /**
     * Metadata hazard information configuration item key.
     */
    public static final String HAZARD_INFO_METADATA_CONFIG = "hazardInfoOptions";

    /**
     * Types of hazard events key in hazard metadata megawidgets definitions
     * list.
     */
    public static final String HAZARD_INFO_METADATA_TYPES = "hazardTypes";

    /**
     * Metadata of hazard events key in hazard metadata megawidgets definitions
     * list.
     */
    public static final String HAZARD_INFO_METADATA_MEGAWIDGETS_LIST = "metaData";

    /**
     * Point metadata of hazard events key in hazard metadata megawidgets
     * definitions list.
     */
    public static final String HAZARD_INFO_METADATA_MEGAWIDGETS_POINTS_LIST = "pointOptions";

    /**
     * Start up configuration item key.
     */
    public static final String START_UP_CONFIG = "startUpConfig";

    /**
     * Console key in start up configuration item.
     */
    public static final String START_UP_CONFIG_CONSOLE = "Console";

    /**
     * Timeline navigation key in console dictionary in start up configuration
     * item.
     */
    public static final String START_UP_CONFIG_CONSOLE_TIMELINE_NAVIGATION = "TimeLineNavigation";

    /**
     * Timeline navigation key in console dictionary in start up configuration
     * item.
     */
    public static final String START_UP_CONFIG_CONSOLE_TIMELINE_NAVIGATION_BELOW = "belowTimeLine";

    /**
     * Filter configuration item key.
     */
    public static final String FILTER_CONFIG = "filterConfig";

    /**
     * Setting configuration item key.
     */
    public static final String SETTING_CONFIG = "viewConfig";

    // Private Static Constants

    /**
     * Edex paths.
     */
    private static final List<String> EDEX_PATHS = Lists
            .newArrayList(
                    "com.raytheon.uf.common.dataplugin.events.hazards/utility/common_static/base/python/events",
                    "com.raytheon.uf.common.recommenders/utility/common_static/base/recommenders/utilities",
                    "com.raytheon.uf.common.recommenders/utility/common_static/base/recommenders/events");

    // Private Static Variables

    /**
     * Geometry factory.
     */
    private static final GeometryFactory geometryFactory;

    // Initialize the geometry factory.
    static {
        geometryFactory = new GeometryFactory();
    }

    // Public Static Methods

    public static String getCannedEventsAsJSON() {
        File currentDir = new File(System.getProperty("user.dir"))
                .getParentFile();
        File cannedEventsFile = new File(currentDir, "tests/cannedEvents.json");
        String cannedEventsAsJson = Utils.textFileAsString(cannedEventsFile);
        return cannedEventsAsJson;
    }

    public static String getSessionManagerPlugin() {
        return SESSION_MANAGER_PLUGIN;
    }

    public static String buildPythonUtilitiesPath() {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext context = pm.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        LocalizationFile lfile = pm.getLocalizationFile(context, "python");
        String result = lfile.getFile().getPath();
        return result;
    }

    public static String buildPythonGeometryUtilitiesPath() {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext context = pm.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        LocalizationFile lfile = pm.getLocalizationFile(context,
                "python/events/utilities");
        String result = lfile.getFile().getPath();
        return result;
    }

    public static String buildUtilitiesPath(String locPath) {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext context = pm.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        LocalizationFile lfile = pm.getLocalizationFile(context, locPath);
        String result = lfile.getFile().getPath();
        return result;
    }

    public static String buildPythonEventUtilitiesPath() {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext context = pm.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        LocalizationFile lfile = pm.getLocalizationFile(context,
                "python/events");
        String result = lfile.getFile().getPath();
        return result;
    }

    public static String buildJepIncludePath(List<String> sourcePaths) {
        String[] asArray = sourcePaths.toArray(new String[sourcePaths.size()]);
        String result = PyUtil.buildJepIncludePath(asArray);
        return result;
    }

    public static Geometry geometryFromCoordinates(List<Coordinate> coordinates) {
        Coordinate[] coordinatesAsArray = Utils.listAsArray(coordinates);
        Geometry result = geometryFactory.createPolygon(
                geometryFactory.createLinearRing(coordinatesAsArray), null);
        return result;
    }

    public static List<String> buildPythonSourcePaths(String basePath)
            throws IOException {
        List<String> sourcePaths = Lists.newArrayList();

        for (String plugin : GSD_PYTHON_PLUGINS) {
            sourcePaths.add(String.format(Utils.directoryJoin(basePath, "cave",
                    plugin, "src")));
        }

        for (String edexPath : EDEX_PATHS) {
            sourcePaths.add(String.format(Utils.directoryJoin(basePath,
                    "edexOsgi", edexPath)));
        }

        /**
         * Optional path entries such as python debugger
         */
        String[] pathEntries = System.getenv("PYTHONPATH").split(":");
        for (String pathEntry : pathEntries) {
            sourcePaths.add(pathEntry);
        }

        return sourcePaths;
    }

    /**
     * Builds the JEP include path. This includes the paths to all of the python
     * resources that Hazard Services will need. This removes dependence from
     * the PYTHONPATH environment variable.
     * 
     * @param
     * @return An path like string containing all of the python resources needed
     *         by Hazard Services.
     */
    static String[] pythonLocalizationDirectories = {
            "python",
            "python" + File.separator + "UFStatusHandler.py",
            "python" + File.separator + "bridge",
            "python" + File.separator + "dataStorage",
            "python" + File.separator + "events",
            "python" + File.separator + "geoUtilities",
            "python" + File.separator + "logUtilities",
            "python" + File.separator + "shapeUtilities",
            "python" + File.separator + "textUtilities",
            "python" + File.separator + "VTECutilities",
            "python" + File.separator + "events" + File.separator + "utilities",
            "python" + File.separator + "events" + File.separator
                    + "recommenders" + File.separator
                    + "DamBreakFloodRecommender.py" };

    public static List<String> buildPythonPath() {

        List<String> sourcePaths = Lists.newArrayList();

        for (String locPath : pythonLocalizationDirectories) {
            String path = Utilities.buildUtilitiesPath(locPath);
            sourcePaths.add(path);
        }

        return sourcePaths;
    }
}
