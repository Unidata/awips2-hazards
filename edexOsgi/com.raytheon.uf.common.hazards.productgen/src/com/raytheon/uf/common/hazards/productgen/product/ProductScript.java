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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import jep.JepException;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.utilities.PythonBuildPaths;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.localization.FileUpdatedMessage;
import com.raytheon.uf.common.localization.FileUpdatedMessage.FileChangeType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.python.controller.PythonScriptController;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

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

    private static final String GET_SCRIPT_METADATA = "getScriptMetadata";

    /** Class name in the python modules */
    private static final String PYTHON_CLASS = "Product";

    /** Parameter name in the execute method */
    private static final String EVENT_SET = "eventSet";

    /** Executing method in the python module */
    private static final String METHOD_NAME = "execute";

    private static final String PRODUCTS_DIRECTORY = "productgen/products";

    private static final String FORMATS_DIRECTORY = "productgen/formats";

    private static final String PYTHON_INTERFACE = "ProductInterface";

    protected List<ProductInfo> inventory = null;

    /** python/productgen/events/products directory */
    protected static LocalizationFile productsDir;

    /**
     * Instantiates a ProductScript object.
     * 
     * @param jepIncludePath
     *            - A jep include path containing python utilities specific to
     *            Hazard Services.
     * @throws JepException
     */
    protected ProductScript(final String jepIncludePath) throws JepException {
        super(PythonBuildPaths.buildPythonInterfacePath(PRODUCTS_DIRECTORY,
                PYTHON_INTERFACE), PyUtil.buildJepIncludePath(PythonBuildPaths
                .buildIncludePath(FORMATS_DIRECTORY, PRODUCTS_DIRECTORY),
                jepIncludePath), ProductScript.class.getClassLoader(),
                PYTHON_CLASS);
        inventory = new CopyOnWriteArrayList<ProductInfo>();
        productsDir = PythonBuildPaths
                .buildLocalizationDirectory(PRODUCTS_DIRECTORY);
        productsDir.addFileUpdatedObserver(this);

        String scriptPath = PythonBuildPaths
                .buildDirectoryPath(PRODUCTS_DIRECTORY);
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
     * Generates a list of IGeneratedProducts from the eventSet
     * 
     * @param product
     * @param eventSet
     *            the EventSet<IEvent> o
     * @param formats
     *            an array of the formats the IGeneratedProduct should be in
     *            (i.e. XML)
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<IGeneratedProduct> generateProduct(String product,
            EventSet<IEvent> eventSet, String[] formats) {

        Map<String, Object> args = new HashMap<String, Object>(
                getStarterMap(product));
        args.put(EVENT_SET, eventSet);
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
     * Retrieves the information to define a dialog from the product.
     * 
     * @param product
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getDialogInfo(String product) {
        return getInfo(product, "getDialogInfo");
    }

    /**
     * Retrieves the metadata of the product.
     * 
     * @param product
     * @return
     */
    public Map<String, String> getScriptMetadata(String product) {
        return getInfo(product, "getScriptMetadata");
    }

    /**
     * Executes the method of the module.
     * 
     * @param moduleName
     *            name of the python module to execute
     * @param methodName
     *            name of the method to execute
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> getInfo(String moduleName, String methodName) {
        Map<String, Object> args = new HashMap<String, Object>(
                getStarterMap(moduleName));
        Map<String, String> retVal = null;
        try {
            if (!isInstantiated(moduleName)) {
                instantiatePythonScript(moduleName);
            }

            retVal = (Map<String, String>) execute(methodName, INTERFACE, args);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR, "Unable to get info from "
                    + methodName, e);
        }

        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.python.controller.PythonScriptController#fileUpdated
     * (com.raytheon.uf.common.localization.FileUpdatedMessage)
     */
    @Override
    public void fileUpdated(FileUpdatedMessage message) {
        FileChangeType type = message.getChangeType();
        if (type == FileChangeType.UPDATED) {
            for (ProductInfo pg : inventory) {
                if (pg.getFile().getName().equals(message.getFileName())) {
                    updateMetadata(pg);
                    break;
                }
            }
        } else if (type == FileChangeType.ADDED) {
            for (ProductInfo pg : inventory) {
                if (pg.getFile().getName().equals(message.getFileName())
                        && pg.getFile()
                                .getContext()
                                .getLocalizationLevel()
                                .compareTo(
                                        message.getContext()
                                                .getLocalizationLevel()) < 0) {
                    updateMetadata(pg);
                    break;
                } else {
                    ProductInfo newPG = new ProductInfo();
                    inventory.add(newPG);
                }
            }
        } else if (type == FileChangeType.DELETED) {
            for (ProductInfo pg : inventory) {
                if (pg.getFile().getName().equals(message.getFileName())) {
                    inventory.remove(pg);
                    break;
                }
            }
        }
        super.fileUpdated(message);
    }

    /**
     * Updates a product's metadata.
     * 
     * @param ProductInfo
     */
    private void updateMetadata(ProductInfo ProductInfo) {
        try {
            if (isInstantiated(ProductInfo.getName()) == false) {
                instantiatePythonScript(ProductInfo.getName());
            }
            Map<String, Object> args = getStarterMap(ProductInfo.getName());
            execute(GET_SCRIPT_METADATA, INTERFACE, args);
        } catch (JepException e) {
            statusHandler.handle(
                    Priority.ERROR,
                    "Unable to update metadata on file "
                            + ProductInfo.getName(), e);
        }
    }

    public synchronized List<ProductInfo> getInventory() {
        return inventory;
    }
}
