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
import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.localization.BundleScanner;
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
 * Apr 04, 2013            daniel.s.schaffer  Initial induction into repo
 * Aug 01, 2013            bryon.lawrence     Commented out the body of the 
 *                                            getHazardFillColor method until
 *                                            HAZARDS("HazardStyleRules.xml") is
 *                                            added to the StyleType enum in the
 *                                            baseline class StyleManager. Nobody
 *                                            calls this method at the moment. It 
 *                                            will always return white.
 * Aug 09, 2013    1936    Chris.Golden       Added console countdown timers.
 * Nov 04, 2013    2182    daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 25, 2013    2336    Chris.Golden       Altered to handle new location of
 *                                            utility classes.
 * Nov 29, 2013 2380    daniel.s.schaffer@noaa.gov More consolidation to {@link HazardConstants}
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
public class Utilities {

    /**
     * Logging mechanism.
     */
    @SuppressWarnings("unused")
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(Utilities.class);

    // Public Static Constants

    /**
     * Default hazard color.
     */
    public static final Color WHITE = new Color(255f, 255f, 255f);

    /**
     * ID of plug-in containing the Hazard Services SessionManager code.
     */
    public static final String SESSION_MANAGER_PLUGIN = "gov.noaa.gsd.viz.hazards.sessionmanager";

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
     * Key into {@link IHazardEvent} attributes for the time associated with a
     * point in storm track, for example.
     */
    public static final String POINT_TIME = "pointTime";

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

    /**
     * Persistent shape key.
     */
    public static final String PERSISTENT_SHAPE = "persistentShape";

    /**
     * Drag drop dot identifier.
     */
    public static final String DRAG_DROP_DOT = "DragDropDot";

    // Private Static Constants

    /**
     * Weight of red luminance component of a color.
     */
    private static final float RED_LUMINANCE_WEIGHT = 0.299f;

    /**
     * Weight of green luminance component of a color.
     */
    private static final float GREEN_LUMINANCE_WEIGHT = 0.587f;

    /**
     * Weight of blue luminance component of a color.
     */
    private static final float BLUE_LUMINANCE_WEIGHT = 0.114f;

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

        File srcPath = BundleScanner.searchInBundle(SESSION_MANAGER_PLUGIN,
                "src", File.separator);
        if (srcPath != null) {
            sourcePaths.add(srcPath.getAbsolutePath());
        }

        // NOT SURE THIS IS USED, BUT WILL KEEP IT IN
        IPathManager manager = PathManagerFactory.getPathManager();
        LocalizationContext context = manager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        sourcePaths.add(manager
                .getLocalizationFile(context,
                        "python" + File.separator + "events").getFile()
                .getAbsolutePath());
        sourcePaths
                .add(manager
                        .getLocalizationFile(
                                context,
                                "python" + File.separator + "events"
                                        + File.separator + "recommenders"
                                        + File.separator + "utilities")
                        .getFile().getAbsolutePath());
        sourcePaths.add(manager.getLocalizationFile(context, "python")
                .getFile().getAbsolutePath());

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
            "python" + File.separator + "localizationUtilities",
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

    /**
     * Searches the Style Rule Sets associated with Hazard Services for the
     * specified phen.sig.subtype key and returns the fill color for the closest
     * matching phen.sig.subtype.
     * 
     * @param hazardType
     *            The phen, sig, subtype to find the fill color for
     * @return The fill color as a Color object.
     */
    public static Color getHazardFillColor(final String hazardType) {

        /*
         * The hazard display preferences map is built as preferences for
         * specific hazard types are requested.
         */
        Color bestHazardColor = WHITE;

        /*
         * Hazard type may be null. This occurs in the case where the hazard has
         * been drawn, but it has not yet been assigned as hazard type. So, we
         * need to check for it here.
         */
        /*
         * Commented out this block until HAZARDS("HazardStyleRules.xml") is
         * added to the StyleType enum in the AWIPS II baseline StyleManager
         * class. This method will always return the color white for now.
         */
        // if (hazardType != null) {
        // Assert.isTrue(hazardType.length() > 0);
        //
        // if (hazardDisplayPreferencesMap.containsKey(hazardType)) {
        // bestHazardColor = hazardDisplayPreferencesMap.get(hazardType)
        // .getColor();
        // } else {
        // /*
        // * Attempt to load the requested hazard type's color from the
        // * style rules. If it cannot be loaded, set it to white by
        // * default.
        // */
        // ParamLevelMatchCriteria match = new ParamLevelMatchCriteria();
        // match.setParameterName(Arrays.asList(hazardType));
        //
        // try {
        // StyleRule styleRule = StyleManager
        // .getInstance()
        // .getStyleRule(StyleManager.StyleType.HAZARDS, match);
        //
        // HazardStyle pref = (HazardStyle) styleRule.getPreferences();
        //
        // if (pref != null) {
        // bestHazardColor = pref.getColor();
        // pref.setColor(bestHazardColor);
        // hazardDisplayPreferencesMap.put(hazardType, pref);
        // }
        //
        // } catch (VizStyleException e) {
        // statusHandler.error(
        // "Error loading style preferences for hazard type: "
        // + hazardType, e);
        // }
        // }
        //
        // }

        return bestHazardColor;
    }

    /**
     * Get the color, black or white, that offers the best contrast with the
     * specified color.
     * 
     * @param color
     *            Color for which a contrasting color is to be calculated.
     * @return Color that contrasts best with the specified color.
     */
    public static Color getContrastingColor(Color color) {

        // Weight the components of the color by perceptive luminance; the human
        // eye notices green more than the others.
        float component = (1f - ((RED_LUMINANCE_WEIGHT * color.getRed())
                + (GREEN_LUMINANCE_WEIGHT * color.getGreen()) + (BLUE_LUMINANCE_WEIGHT * color
                .getBlue())) < 0.5f ? 0f : 1f);
        return new Color(component, component, component);
    }
}
