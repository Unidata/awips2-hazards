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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_SERVICES_LOCALIZATION_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_TYPES_LOCALIZATION_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_BRIDGE_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_DATA_STORAGE_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_EVENTS_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_GEO_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_LOG_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_SHAPE_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_TEXT_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_LOCALIZATION_VTEC_UTILITIES_DIR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PYTHON_UTILITIES_DIR;

import java.io.File;
import java.util.List;

import jep.JepException;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.python.concurrent.AbstractPythonScriptFactory;
import com.raytheon.uf.common.python.concurrent.PythonJobCoordinator;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.FileUtil;

/**
 * Description: Script factory for the configuration manager.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 19, 2014    4243    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ConfigScriptFactory extends
        AbstractPythonScriptFactory<ContextSwitchingPythonEval> {

    // Public Static Constants

    /**
     * Reference name, for getting an instance in {@link PythonJobCoordinator}.
     */
    public static final String NAME = "configScriptFactory";

    /**
     * Name of the Python function used to wrap invocations of event modifier
     * entry point functions. When invoked, it takes two parameters, the first
     * being the name of the event modifier entry point function to be called,
     * and the second being the hazard event in Java form. It returns either a
     * modified hazard event, again in Java form, or else None if no
     * modifications are needed.
     */
    public static final String EVENT_MODIFIER_FUNCTION_NAME = "_runHazardEventModifier_";

    // Private Static Constants

    /**
     * Definition of the Python function used to wrap invocations of event
     * modifier entry point functions.
     */
    private static final String INVOKE_EVENT_MODIFIER_FUNCTION_DEFINITION = "def "
            + EVENT_MODIFIER_FUNCTION_NAME
            + "(javaModifierFunc, javaHazardEvent):\n"
            + "  hazardEvent = JUtil.javaObjToPyVal(javaHazardEvent)\n"
            + "  modifierFunc = JUtil.javaObjToPyVal(javaModifierFunc)\n"
            + "  hazardEvent = globals()[modifierFunc](hazardEvent)\n"
            + "  if hazardEvent is not None:\n"
            + "    return JUtil.pyValToJavaObj(hazardEvent)\n"
            + "  return None\n\n";

    /**
     * List of pre-evaluations that the Python evaluator must do when built.
     */
    private static final List<String> PYTHON_PRE_EVALS = Lists
            .newArrayList(
                    "import json",
                    "import JavaImporter",
                    "import JUtil",
                    "import HazardServicesMetaDataRetriever",
                    "from HazardEventHandler import javaHazardEventToPyHazardEvent, pyHazardEventToJavaHazardEvent",
                    "JUtil.registerJavaToPython(javaHazardEventToPyHazardEvent)",
                    "JUtil.registerPythonToJava(pyHazardEventToJavaHazardEvent)",
                    INVOKE_EVENT_MODIFIER_FUNCTION_DEFINITION);

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
        this(NAME, 1);
    }

    /**
     * Construct a standard instance.
     * 
     * @param name
     *            Reference name, for getting an instance in
     *            {@link PythonJobCoordinator}.
     * @param maxThreads
     *            Maximum number of threads.
     */
    private ConfigScriptFactory(String name, int maxThreads) {
        super(name, maxThreads);
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
            String pythonPath = pathManager.getFile(localizationContext,
                    PYTHON_LOCALIZATION_DIR).getPath();
            String localizationUtilitiesPath = FileUtil.join(pythonPath,
                    PYTHON_LOCALIZATION_UTILITIES_DIR);
            String vtecUtilitiesPath = FileUtil.join(pythonPath,
                    PYTHON_LOCALIZATION_VTEC_UTILITIES_DIR);
            String logUtilitiesPath = FileUtil.join(pythonPath,
                    PYTHON_LOCALIZATION_LOG_UTILITIES_DIR);
            String eventsPath = FileUtil.join(pythonPath,
                    PYTHON_LOCALIZATION_EVENTS_DIR);
            String eventsUtilitiesPath = FileUtil.join(pythonPath,
                    PYTHON_LOCALIZATION_EVENTS_DIR, PYTHON_UTILITIES_DIR);
            String geoUtilitiesPath = FileUtil.join(pythonPath,
                    PYTHON_LOCALIZATION_GEO_UTILITIES_DIR);
            String shapeUtilitiesPath = FileUtil.join(pythonPath,
                    PYTHON_LOCALIZATION_SHAPE_UTILITIES_DIR);
            String textUtilitiesPath = FileUtil.join(pythonPath,
                    PYTHON_LOCALIZATION_TEXT_UTILITIES_DIR);
            String dataStoragePath = FileUtil.join(pythonPath,
                    PYTHON_LOCALIZATION_DATA_STORAGE_DIR);
            String bridgePath = FileUtil.join(pythonPath,
                    PYTHON_LOCALIZATION_BRIDGE_DIR);
            String hazardServicesPath = pathManager.getFile(
                    localizationContext, HAZARD_SERVICES_LOCALIZATION_DIR)
                    .getPath();
            String hazardTypesPath = FileUtil.join(hazardServicesPath,
                    HAZARD_TYPES_LOCALIZATION_DIR);

            /**
             * TODO This path is used in multiple places elsewhere. Are those
             * cases also due to the micro-engine issue?
             */
            String tbdWorkaroundToUEngineInLocalizationPath = FileUtil.join(
                    File.separator, "awips2", "fxa", "bin", "src");

            String includePath = PyUtil.buildJepIncludePath(pythonPath,
                    localizationUtilitiesPath, logUtilitiesPath,
                    tbdWorkaroundToUEngineInLocalizationPath,
                    vtecUtilitiesPath, geoUtilitiesPath, shapeUtilitiesPath,
                    textUtilitiesPath, dataStoragePath, eventsPath,
                    eventsUtilitiesPath, bridgePath, hazardServicesPath,
                    hazardTypesPath);
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
