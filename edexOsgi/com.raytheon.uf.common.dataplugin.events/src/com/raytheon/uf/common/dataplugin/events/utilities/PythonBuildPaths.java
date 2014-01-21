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
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class PythonBuildPaths {

    public static final String PYTHON_EVENTS_DIRECTORY = "python"
            + File.separator + "events" + File.separator;

    /**
     * Builds the path for the directory in python/events
     * 
     * @param directory
     * 
     * @return the full path to the base, site, and user directories
     */
    public static String buildDirectoryPath(String directory) {
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        LocalizationContext baseContext = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        LocalizationContext siteContext = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.SITE);
        LocalizationContext userContext = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.USER);

        String fileLoc = PYTHON_EVENTS_DIRECTORY + directory;

        String userPath = pathMgr.getLocalizationFile(userContext, fileLoc)
                .getFile().getPath();
        String sitePath = pathMgr.getLocalizationFile(siteContext, fileLoc)
                .getFile().getPath();
        String basePath = pathMgr.getLocalizationFile(baseContext, fileLoc)
                .getFile().getPath();

        return PyUtil.buildJepIncludePath(basePath, sitePath, userPath);
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

        String fileLoc = "python" + File.separator + "events" + File.separator
                + directory + File.separator + pythonInterface + ".py";

        String pythonInterfacePath = pathMgr
                .getLocalizationFile(baseContext, fileLoc).getFile().getPath();

        return pythonInterfacePath;
    }

    public static LocalizationFile buildLocalizationDirectory(String directory) {
        IPathManager manager = PathManagerFactory.getPathManager();
        LocalizationContext baseContext = manager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        String fileLoc = "python" + File.separator + "events" + File.separator
                + directory;
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

        String pythonPath = manager.getFile(baseContext, "python").getPath();
        String dataAccessPath = FileUtil.join(pythonPath, "dataaccess");
        String dataTimePath = FileUtil.join(pythonPath, "time");
        String eventsPath = FileUtil.join(pythonPath, "events");
        String utilitiesPath = FileUtil.join(eventsPath, "utilities");
        String gfePath = FileUtil.join(pythonPath, "gfe");

        for (int i = 0; i < directories.length; i++) {
            directories[i] = buildLocalizationDirectory(directories[i])
                    .getFile().getPath();
        }
        String directoryPaths = PyUtil.buildJepIncludePath(directories);
        String includePath = PyUtil.buildJepIncludePath(pythonPath,
                dataAccessPath, dataTimePath, eventsPath, utilitiesPath,
                gfePath);
        return PyUtil.buildJepIncludePath(includePath, directoryPaths);
    }
}
