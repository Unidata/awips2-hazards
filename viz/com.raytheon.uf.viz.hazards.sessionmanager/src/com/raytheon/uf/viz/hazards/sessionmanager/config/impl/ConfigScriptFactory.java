/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config.impl;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.hazards.configuration.HazardsConfigurationConstants;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.python.concurrent.PythonInterpreterFactory;
import com.raytheon.uf.common.python.concurrent.PythonJobCoordinator;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.FileUtil;

import jep.JepException;

/**
 * Description: Script factory for the configuration manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 19, 2014    4243    Chris.Golden Initial creation.
 * Sep 16, 2014    4753    Chris.Golden Changed to include mutable properties.
 * Feb 4,  2015    5691    kmanross     Changed include paths for DataAccess
 * Feb 18, 2015    5071    Robert.Blum  Added productTextUtilPath and hazardCategoriesPath 
 *                                      to the include path.
 * May 13, 2015    8161    mduff        Change for Jep upgrade.
 * Nov 17, 2015    3473    Robert.Blum  Moved all python files under HazardServices
 *                                      localization dir.
 * Mar 01, 2016   15676    Chris.Golden Added visual feature handler functionality to
 *                                      to the include path.
 * Sep 20, 2016   21609    Kevin.Bisanz Add geoSpatial to the include path.
 * Jun 06, 2017   15561    Chris.Golden Added path to general utilities.
 * Feb 13, 2018   44514    Chris.Golden Removed event-modifying script code, as such
 *                                      scripts are not to be used.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ConfigScriptFactory
        implements PythonInterpreterFactory<ContextSwitchingPythonEval> {

    // Public Static Constants

    /**
     * Reference name, for getting an instance in {@link PythonJobCoordinator}.
     */
    public static final String NAME = "configScriptFactory";

    // Private Static Constants

    /**
     * List of pre-evaluations that the Python evaluator must do when built.
     */
    private static final List<String> PYTHON_PRE_EVALS = Lists.newArrayList(
            "import json", "import JUtil",
            "import HazardServicesMetaDataRetriever",
            "from VisualFeaturesHandler import javaVisualFeaturesToPyVisualFeatures, pyVisualFeaturesToJavaVisualFeatures",
            "JUtil.registerJavaToPython(javaVisualFeaturesToPyVisualFeatures)",
            "JUtil.registerPythonToJava(pyVisualFeaturesToJavaVisualFeatures)",
            "from HazardEventHandler import javaHazardEventToPyHazardEvent, pyHazardEventToJavaHazardEvent",
            "JUtil.registerJavaToPython(javaHazardEventToPyHazardEvent)",
            "JUtil.registerPythonToJava(pyHazardEventToJavaHazardEvent)");

    // Private Static Variables

    /**
     * Status handler, for display notifications to the user.
     */
    private static IUFStatusHandler statusHandler = UFStatus
            .getHandler(ConfigScriptFactory.class);

    // Public Constructors

    /**
     * Construct a standard instance with the default name and a maximum of one
     * thread.
     */
    public ConfigScriptFactory() {
    }

    @Override
    public ContextSwitchingPythonEval createPythonScript() {
        try {

            /*
             * Create the various paths that need to be part of the include
             * path.
             */
            IPathManager pathManager = PathManagerFactory.getPathManager();
            LocalizationContext localizationContext = pathManager.getContext(
                    LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
            String pythonPath = pathManager
                    .getFile(localizationContext,
                            HazardsConfigurationConstants.PYTHON_LOCALIZATION_DIR)
                    .getPath();
            String gfePath = pathManager
                    .getFile(localizationContext,
                            HazardsConfigurationConstants.GFE_LOCALIZATION_DIR)
                    .getPath();
            String hazardServicesPythonPath = pathManager
                    .getFile(localizationContext,
                            HazardsConfigurationConstants.HAZARD_SERVICES_PYTHON_LOCALIZATION_DIR)
                    .getPath();
            String localizationAccessPath = FileUtil.join(pythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_DATA_ACCESS_DIR);
            String localizationGfePath = FileUtil.join(pythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_GFE_DIR);
            String localizationDataTimePath = FileUtil.join(pythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_TIME_DIR);
            String localizationUtilitiesPath = FileUtil.join(
                    hazardServicesPythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_UTILITIES_DIR);
            String vtecUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_VTEC_UTILITIES_DIR);
            String logUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_LOG_UTILITIES_DIR);
            String eventsPath = FileUtil.join(hazardServicesPythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_EVENTS_DIR);
            String eventsUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_EVENTS_DIR,
                    HazardsConfigurationConstants.PYTHON_UTILITIES_DIR);
            String geoUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_GEO_UTILITIES_DIR);
            String geoSpatialPath = FileUtil.join(hazardServicesPythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_EVENTS_DIR,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_PRODUCTGEN_DIR,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_GEOSPATIAL_DIR);
            String shapeUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_SHAPE_UTILITIES_DIR);
            String textUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_TEXT_UTILITIES_DIR);
            String generalUtilitiesPath = FileUtil.join(
                    hazardServicesPythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_GENERAL_UTILITIES_DIR);
            String dataStoragePath = FileUtil.join(hazardServicesPythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_DATA_STORAGE_DIR);
            String bridgePath = FileUtil.join(hazardServicesPythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_BRIDGE_DIR);
            String productTextUtilPath = FileUtil.join(hazardServicesPythonPath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_EVENTS_DIR,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_PRODUCTGEN_DIR,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_PRODUCTS_DIR);
            String hazardServicesPath = pathManager
                    .getFile(localizationContext,
                            HazardsConfigurationConstants.HAZARD_SERVICES_PYTHON_LOCALIZATION_DIR)
                    .getPath();
            String hazardCategoriesPath = FileUtil.join(hazardServicesPath,
                    HazardsConfigurationConstants.HAZARD_CATEGORIES_LOCALIZATION_DIR);
            String hazardTypesPath = FileUtil.join(hazardServicesPath,
                    HazardsConfigurationConstants.HAZARD_TYPES_LOCALIZATION_DIR);

            String gfePythonPath = FileUtil.join(gfePath,
                    HazardsConfigurationConstants.PYTHON_LOCALIZATION_DIR);

            /**
             * TODO This path is used in multiple places elsewhere. Are those
             * cases also due to the micro-engine issue?
             */
            String tbdWorkaroundToUEngineInLocalizationPath = FileUtil
                    .join(File.separator, "awips2", "fxa", "bin", "src");

            String includePath = PyUtil.buildJepIncludePath(pythonPath,
                    hazardServicesPythonPath, localizationUtilitiesPath,
                    logUtilitiesPath, localizationAccessPath,
                    localizationDataTimePath, localizationGfePath,
                    tbdWorkaroundToUEngineInLocalizationPath, vtecUtilitiesPath,
                    geoUtilitiesPath, shapeUtilitiesPath, textUtilitiesPath,
                    generalUtilitiesPath, dataStoragePath, eventsPath,
                    eventsUtilitiesPath, bridgePath, hazardServicesPath,
                    hazardTypesPath, productTextUtilPath, hazardCategoriesPath,
                    geoSpatialPath, gfePythonPath);
            ClassLoader classLoader = this.getClass().getClassLoader();
            ContextSwitchingPythonEval result = new ContextSwitchingPythonEval(
                    includePath, classLoader, PYTHON_PRE_EVALS);

            return result;
        } catch (JepException e) {
            statusHandler.error(
                    "Could not initialize config manager Python evaluator.", e);
        }
        return null;
    }
}
