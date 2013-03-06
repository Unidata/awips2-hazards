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
package com.raytheon.uf.common.hazards.productgen.product;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jep.JepException;

import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardEventSet;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.python.controller.PythonScriptController;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.util.FileUtil;

/**
 * Provides to execute methods in the product generator.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2013            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductScript extends PythonScriptController {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductScript.class);

    /** Class name in the python modules */
    private static final String PYTHON_CLASS = "Product";

    /** Parameter name in the execute method */
    private static final String HAZARD_EVENT_SET = "hazardEventSet";

    /** Executing method in the python module */
    private static final String METHOD_NAME = "execute";

    private static String PRODUCTS_DIRECTORY = "productgen/products";

    private static String FORMATS_DIRECTORY = "productgen/formats";

    private static String PYTHON_INTERFACE = "ProductInterface";

    /** python/productgen/products directory */
    protected static LocalizationFile productsDir;

    protected ProductScript() throws JepException {
        super(buildFilePath(), buildIncludePath(), ProductScript.class
                .getClassLoader(), PYTHON_CLASS);

        productsDir.addFileUpdatedObserver(this);

        String scriptPath = buildDirectoryPath(PRODUCTS_DIRECTORY);
        jep.eval(INTERFACE + " = " + PYTHON_INTERFACE + "('" + scriptPath
                + "')");
        List<String> errors = getStartupErrors();
        if (errors.size() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("Error importing the following product generators:\n");
            for (String s : errors) {
                sb.append(s);
                sb.append("\n");
            }

            statusHandler.error(sb.toString());
        }

        jep.eval("import sys");
        jep.eval("sys.argv = ['" + PYTHON_INTERFACE + "']");
    }

    /**
     * Builds the path for the directory in python/events
     * 
     * @param directory
     * 
     * @return the full path to the base, site, and user directories
     */
    private String buildDirectoryPath(String directory) {
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        LocalizationContext baseContext = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        LocalizationContext siteContext = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.SITE);
        LocalizationContext userContext = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.USER);

        String fileLoc = "python" + File.separator + "events" + File.separator
                + directory;

        String userPath = pathMgr.getLocalizationFile(userContext, fileLoc)
                .getFile().getPath();
        String sitePath = pathMgr.getLocalizationFile(siteContext, fileLoc)
                .getFile().getPath();
        String basePath = pathMgr.getLocalizationFile(baseContext, fileLoc)
                .getFile().getPath();

        return PyUtil.buildJepIncludePath(userPath, sitePath, basePath);
    }

    /**
     * 
     * @return the file path to the product generator
     */
    private static String buildFilePath() {
        IPathManager pathMgr = PathManagerFactory.getPathManager();
        LocalizationContext baseContext = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);

        String fileLoc = "python" + File.separator + "events" + File.separator
                + PRODUCTS_DIRECTORY + File.separator + PYTHON_INTERFACE
                + ".py";

        productsDir = pathMgr.getLocalizationFile(baseContext, "python"
                + File.separator + "events" + File.separator
                + PRODUCTS_DIRECTORY);

        String productScriptPath = pathMgr
                .getLocalizationFile(baseContext, fileLoc).getFile().getPath();

        return productScriptPath;
    }

    /**
     * 
     * @return paths of other libraries to help generate a product
     */
    private static String buildIncludePath() {
        IPathManager manager = PathManagerFactory.getPathManager();
        LocalizationContext baseContext = manager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        LocalizationFile formatsDir = manager.getLocalizationFile(baseContext,
                "python" + File.separator + "events" + File.separator
                        + FORMATS_DIRECTORY);

        String pythonPath = manager.getFile(baseContext, "python").getPath();
        String dataAccessPath = FileUtil.join(pythonPath, "dataaccess");
        String dataTimePath = FileUtil.join(pythonPath, "time");
        String productDirPath = productsDir.getFile().getPath();
        String formatsDirPath = formatsDir.getFile().getPath();
        String eventsPath = FileUtil.join(pythonPath, "events");
        String utilitiesPath = FileUtil.join(eventsPath, "utilities");

        String includePath = PyUtil.buildJepIncludePath(pythonPath,
                productDirPath, formatsDirPath, dataAccessPath, dataTimePath,
                eventsPath, utilitiesPath);
        return includePath;
    }

    /**
     * Generates a product from the hazardEventSet
     * 
     * @param product
     * @param hazardEventSet
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<IGeneratedProduct> generateProduct(String product,
            HazardEventSet hazardEventSet, String[] formats) {

        Map<String, Object> args = new HashMap<String, Object>(
                getStarterMap(product));
        args.put(HAZARD_EVENT_SET, hazardEventSet);
        args.put("formats", Arrays.asList(formats));
        List<IGeneratedProduct> retVal = null;
        try {
            if (!isInstantiated(product)) {
                instantiatePythonScript(product);
            }

            retVal = (List<IGeneratedProduct>) execute(METHOD_NAME, INTERFACE,
                    args);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to execute product generator", e);
        }

        return retVal;
    }

    /**
     * This method may do different things depending on the implementation.
     * Subclasses retrieve information about a possible dialog, or possibly read
     * from a file if no dialog should be present.
     * 
     * @param product
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getDialogInfo(String moduleName) {
        Map<String, Object> args = new HashMap<String, Object>(
                getStarterMap(moduleName));
        Map<String, String> retVal = null;
        try {
            retVal = (Map<String, String>) execute("getDialogInfo", INTERFACE,
                    args);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to get info from getDialogInfo", e);
        }

        return retVal;
    }
}
