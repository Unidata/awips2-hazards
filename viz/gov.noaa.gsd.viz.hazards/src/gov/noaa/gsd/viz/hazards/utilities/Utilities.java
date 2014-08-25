/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.utilities;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.POINTS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SPATIAL_INFO;
import gov.noaa.gsd.common.utilities.Utils;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.vividsolutions.jts.geom.Coordinate;

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

    // Public Static Methods

    public static String getCannedEventsAsJSON() {
        File currentDir = new File(System.getProperty("user.dir"))
                .getParentFile();
        File cannedEventsFile = new File(currentDir, "tests/cannedEvents.json");
        String cannedEventsAsJson = Utils.textFileAsString(cannedEventsFile);
        return cannedEventsAsJson;
    }

    public static String buildUtilitiesPath(String locPath) {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext context = pm.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        LocalizationFile lfile = pm.getLocalizationFile(context, locPath);
        String result = lfile.getFile().getPath();
        return result;
    }

    public static String buildJepIncludePath(List<String> sourcePaths) {
        String[] asArray = sourcePaths.toArray(new String[sourcePaths.size()]);
        String result = PyUtil.buildJepIncludePath(asArray);
        return result;
    }

    public static void closeCoordinatesIfNecessary(List<Coordinate> coordinates) {
        Coordinate firstPoint = coordinates.get(0);
        Coordinate lastPoint = coordinates.get(coordinates.size() - 1);
        if (!firstPoint.equals(lastPoint)) {
            Coordinate copy = (Coordinate) firstPoint.clone();
            coordinates.add(copy);
        }
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

        List<String> sourcePaths = new ArrayList<>();

        for (String locPath : pythonLocalizationDirectories) {
            String path = Utilities.buildUtilitiesPath(locPath);
            sourcePaths.add(path);
        }

        return sourcePaths;
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

    public static Map<String, Serializable> asMap(Dict runData) {
        if (runData == null) {
            return null;
        }
        HashMap<String, Serializable> result = new HashMap<>();
        for (Entry<String, Object> entry : runData.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof Dict) {
                result.put(entry.getKey(), (Serializable) asMap((Dict) val));
            } else if (val instanceof Serializable) {
                result.put(entry.getKey(), (Serializable) val);
            } else {
                throw new RuntimeException(entry + ", "
                        + val.getClass().getSimpleName()
                        + " does not implement Serializable");
            }
        }
        return result;
    }

    public static Map<String, Serializable> buildStormStrackToolDraggedPointParameters(
            double yloc, double xloc, double selectedTime) {
        Map<String, Serializable> toolParameters = new HashMap<>();
        HashMap<String, Serializable> pointsDict = new HashMap<>();
        toolParameters.put(SPATIAL_INFO, pointsDict);
        ArrayList<Serializable> points = new ArrayList<>();
        pointsDict.put(POINTS, points);
        ArrayList<Serializable> outerList = new ArrayList<>();
        points.add(outerList);
        ArrayList<Double> xyLoc = Lists.newArrayList(yloc, xloc);
        Double zLoc = selectedTime;
        outerList.add(xyLoc);
        outerList.add(zLoc);
        return toolParameters;
    }
}