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
package com.raytheon.uf.common.dataplugin.events.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.hazards.configuration.HazardsConfigurationConstants;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.util.FileUtil;

/**
 * Provides static methods to build file paths to python interfaces and include
 * paths for python files.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 1, 2013            jsanchez     Initial creation
 * Jan 20, 2014 2766      bkowal       Re-ordered the paths. Created static
 *                                     variable for the python events directory.
 * Mar 19, 2014 3293      bkowal       Added the REGION localization level.
 * Apr  7, 2014 2917      jsanchez     Added productgen to the include paths.
 * Oct 10, 2014 3790      Robert.Blum  Fixed bug that would put two colons side by side
 *                                     in the JEP include path if the directories path was
 *                                     a empty string.
 * Oct 24, 2014 4934      mpduff       Added metaDataPath to includePath.
 * Feb 26, 2015 6306      mduff        Get available localization levels only.
 * Nov 17, 2015 3473      Robert.Blum  Moved all python files under HazardServices localization dir.
 * Apr 25, 2015 17611     Robert.Blum  Updated so ConfigLoader can utilize.
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class PythonBuildPaths {

    /**
     * Builds the path for the directory in python/events
     * 
     * @param directory
     * 
     * @return the full path to the base, site, and user directories
     */
    public static String buildDirectoryPath(String directory, String site) {
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        List<String> pathList = new ArrayList<>();
        String fileLoc = HazardsConfigurationConstants.PYTHON_EVENTS_DIRECTORY
                + File.separator + directory;

        LocalizationLevel[] levels = pathMgr.getAvailableLevels();
        for (int i = levels.length - 1; i >= 0; i--) {
            LocalizationLevel level = levels[i];
            LocalizationContext lc = pathMgr.getContext(
                    LocalizationType.COMMON_STATIC, level);
            if (site != null
                    && (level == LocalizationLevel.SITE || level == LocalizationLevel.CONFIGURED)) {
                lc.setContextName(site);
            }
            pathList.add(pathMgr.getLocalizationFile(lc, fileLoc).getFile()
                    .getPath());
        }
        return PyUtil.buildJepIncludePath(pathList.toArray(new String[0]));
    }

    /**
     * Builds the file path for the python interface in
     * /python/events/{directory}.
     * 
     * @param directory
     *            directory in python/events where the pythonInterface is kept.
     * @param pythonInterface
     *            filename of the python interface
     * @return
     */
    public static String buildPythonInterfacePath(String directory,
            String pythonInterface) {
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        LocalizationContext baseContext = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);

        String fileLoc = HazardsConfigurationConstants.PYTHON_EVENTS_DIRECTORY
                + File.separator + directory + File.separator + pythonInterface
                + ".py";

        String pythonInterfacePath = pathMgr
                .getLocalizationFile(baseContext, fileLoc).getFile().getPath();

        return pythonInterfacePath;
    }

    public static LocalizationFile buildLocalizationDirectory(String directory) {
        IPathManager manager = PathManagerFactory.getPathManager();
        LocalizationContext baseContext = manager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        String fileLoc = HazardsConfigurationConstants.PYTHON_EVENTS_DIRECTORY
                + File.separator + directory;
        return manager.getLocalizationFile(baseContext, fileLoc);
    }

    /**
     * Builds the include path for python, python/dataacess, python/time,
     * python/events, python/utilities and adds the passed directories in
     * python/events.
     * 
     * @param directories
     *            directory in python/events to be included
     * @return
     */
    public static String buildIncludePath(String... directories) {
        IPathManager manager = PathManagerFactory.getPathManager();
        LocalizationContext baseContext = manager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);

        String pythonPath = manager.getFile(baseContext,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_DIR)
                .getPath();
        String hazardServicesPythonPath = manager
                .getFile(
                        baseContext,
                        HazardsConfigurationConstants.HAZARD_SERVICES_PYTHON_LOCALIZATION_DIR)
                .getPath();
        String dataAccessPath = FileUtil
                .join(pythonPath,
                        HazardsConfigurationConstants.PYTHON_LOCALIZATION_DATA_ACCESS_DIR);
        String localizationGfePath = FileUtil.join(pythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_GFE_DIR);
        String dataTimePath = FileUtil.join(pythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_TIME_DIR);
        String localizationUtilitiesPath = FileUtil
                .join(hazardServicesPythonPath,
                        HazardsConfigurationConstants.PYTHON_LOCALIZATION_UTILITIES_DIR);
        String vtecUtilitiesPath = FileUtil
                .join(hazardServicesPythonPath,
                        HazardsConfigurationConstants.PYTHON_LOCALIZATION_VTEC_UTILITIES_DIR);
        String logUtilitiesPath = FileUtil
                .join(hazardServicesPythonPath,
                        HazardsConfigurationConstants.PYTHON_LOCALIZATION_LOG_UTILITIES_DIR);
        String eventsPath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_EVENTS_DIR);
        String eventsUtilitiesPath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_EVENTS_DIR,
                HazardsConfigurationConstants.PYTHON_UTILITIES_DIR);
        String geoUtilitiesPath = FileUtil
                .join(hazardServicesPythonPath,
                        HazardsConfigurationConstants.PYTHON_LOCALIZATION_GEO_UTILITIES_DIR);
        String shapeUtilitiesPath = FileUtil
                .join(hazardServicesPythonPath,
                        HazardsConfigurationConstants.PYTHON_LOCALIZATION_SHAPE_UTILITIES_DIR);
        String textUtilitiesPath = FileUtil
                .join(hazardServicesPythonPath,
                        HazardsConfigurationConstants.PYTHON_LOCALIZATION_TEXT_UTILITIES_DIR);
        String dataStoragePath = FileUtil
                .join(hazardServicesPythonPath,
                        HazardsConfigurationConstants.PYTHON_LOCALIZATION_DATA_STORAGE_DIR);
        String bridgePath = FileUtil.join(hazardServicesPythonPath,
                HazardsConfigurationConstants.PYTHON_LOCALIZATION_BRIDGE_DIR);
        String productTextUtilPath = FileUtil
                .join(hazardServicesPythonPath,
                        HazardsConfigurationConstants.PYTHON_LOCALIZATION_EVENTS_DIR,
                        HazardsConfigurationConstants.PYTHON_LOCALIZATION_PRODUCTGEN_DIR,
                        HazardsConfigurationConstants.PYTHON_LOCALIZATION_PRODUCTS_DIR);

        String productGenPath = FileUtil
                .join(eventsPath,
                        HazardsConfigurationConstants.PYTHON_LOCALIZATION_PRODUCTGEN_DIR);
        String metadataPath = manager
                .getFile(
                        baseContext,
                        HazardsConfigurationConstants.HAZARD_SERVICES_PYTHON_LOCALIZATION_DIR
                                + File.separator
                                + HazardsConfigurationConstants.HAZARD_METADATA_DIR)
                .getPath();

        for (int i = 0; i < directories.length; i++) {
            directories[i] = buildLocalizationDirectory(directories[i])
                    .getFile().getPath();
        }
        String directoryPaths = PyUtil.buildJepIncludePath(directories);
        String includePath = PyUtil.buildJepIncludePath(pythonPath,
                hazardServicesPythonPath, dataAccessPath, dataTimePath,
                eventsPath, eventsUtilitiesPath, localizationGfePath,
                metadataPath, productGenPath, bridgePath, dataStoragePath,
                logUtilitiesPath, localizationUtilitiesPath,
                productTextUtilPath, vtecUtilitiesPath, geoUtilitiesPath,
                shapeUtilitiesPath, textUtilitiesPath);
        if (directoryPaths.equals("")) {
            return PyUtil.buildJepIncludePath(includePath);
        }
        return PyUtil.buildJepIncludePath(includePath, directoryPaths);
    }
}