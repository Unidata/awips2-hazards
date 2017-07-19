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
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.utilities.PythonBuildPaths;
import com.raytheon.uf.common.hazards.configuration.HazardsConfigurationConstants;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;
import com.raytheon.uf.common.localization.FileUpdatedMessage;
import com.raytheon.uf.common.localization.FileUpdatedMessage.FileChangeType;
import com.raytheon.uf.common.localization.ILocalizationFileObserver;
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

import jep.JepException;

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
 * Jan 20, 2014 2766       bkowal       Updated to use the Python Overrider
 * Mar 19, 2014 3293       bkowal       Code cleanup.
 * Sep 23, 2014 3790       Robert.Blum  Updated the inventory and reloaded
 *                                      the module on update.
 * 1/15/2015    5109       bphillip		Changes to accomodate running generators and formatters separately
 * 2/12/2015    5071       Robert.Blum  Changes for reloading python files without
 *                                      closing Cave.
 * 2/25/2015    6599       Robert.Blum  Adding fileObserver to TextUtility dir to allowing overrides.
 * 02/25/2015   6306       mduff        Pass the selected site.
 * 04/16/2015   7579       Robert.Blum  Changed executeFrom method to take a KeyInfo object.
 * 05/07/2015   6979       Robert.Blum  Added a method to call the new updateDataList method in the product
 *                                      generators.
 * Nov 17, 2015 3473      Robert.Blum   Moved all python files under HazardServices localization dir.
 * Feb 12, 2016 14923     Robert.Blum   Picking up overrides of EventUtilities directory
 * Mar 02, 2016 14032     Ben.Phillippe Added geoSpatial directory to python path
 * Mar 21, 2016 15640     Robert.Blum   Fixed custom edits not getting put in final product.
 * Mar 30, 2016  8837     Robert.Blum   Passed the current site to the ProductInterface.
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
public class ProductScript extends PythonScriptController {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductScript.class);

    public static final String DEFAULT_PRODUCT_GENERATION_JOB_COORDINATOR = "ProductGenerators";

    private static final String GET_SCRIPT_METADATA = "getScriptMetadata";

    private static final String GET_DIALOG_INFO = "getDialogInfo";

    private static final String DATA_LIST = "dataList";

    private static final String KEY_INFO = "keyInfo";

    /** Class name in the python modules */
    private static final String PYTHON_CLASS = "Product";

    /** Parameter name in the execute method */
    private static final String EVENT_SET = "eventSet";

    private static final String SITE = "site";

    private static final String DIALOG_INPUT_MAP = "dialogInputMap";

    private static final String FORMATS = "formats";

    private static final String GENERATED_PRODUCT = "generatedProductList";

    private static final String OVERRIDE_PRODUCT_TEXT = "overrideProductText";

    /** Method in ProductInterface.py for executing the generators */
    private static final String GENERATOR_EXECUTE_METHOD = "executeGenerator";

    private static final String GENERATOR_EXECUTE_FROM_METHOD = "executeGeneratorFrom";

    private static final String GENERATOR_UPDATE_METHOD = "executeGeneratorUpdate";

    /** Method in ProductInterface.py for executing the formatters */
    private static final String FORMATTER_METHOD = "executeFormatter";

    private static final String PRODUCTS_DIRECTORY = "productgen/products";

    private static final String FORMATS_DIRECTORY = "productgen/formats";

    private static final String GEOSPATIAL_DIRECTORY = "productgen/geoSpatial";

    private static final String PYTHON_INTERFACE = "ProductInterface";

    private static final String PYTHON_FILE_EXTENSION = ".py";

    /* python/events/productgen/products directory */
    protected static LocalizationFile productsDir;

    /* python/events/productgen/products directory */
    protected static LocalizationFile formatsDir;

    /* python/textUtilities directory */
    protected static LocalizationFile textUtilDir;

    /* python/events/utilities directory */
    protected static LocalizationFile eventUtilDir;

    private final ILocalizationFileObserver formatsDirObserver;

    private final ILocalizationFileObserver textUtilDirObserver;

    private final ILocalizationFileObserver eventUtilDirObserver;

    private boolean pendingFormatterUpdates = false;

    private boolean pendingTextUtilitiesUpdates = false;

    private boolean pendingEventUtilitiesUpdates = false;

    /**
     * Instantiates a ProductScript object.
     * 
     * @param jepIncludePath
     *            - A jep include path containing python utilities specific to
     *            Hazard Services.
     * @throws JepException
     */
    protected ProductScript(final String jepIncludePath, String site)
            throws JepException {
        super(PythonBuildPaths.buildPythonInterfacePath(PRODUCTS_DIRECTORY,
                PYTHON_INTERFACE),
                PyUtil.buildJepIncludePath(
                        PythonBuildPaths.buildDirectoryPath(FORMATS_DIRECTORY,
                                site),
                        PythonBuildPaths.buildDirectoryPath(PRODUCTS_DIRECTORY,
                                site),
                        PythonBuildPaths
                                .buildDirectoryPath(GEOSPATIAL_DIRECTORY, site),
                        PythonBuildPaths.buildIncludePath(), jepIncludePath),
                ProductScript.class.getClassLoader(), PYTHON_CLASS);

        productsDir = PythonBuildPaths
                .buildLocalizationDirectory(PRODUCTS_DIRECTORY);
        productsDir.addFileUpdatedObserver(this);

        formatsDir = PythonBuildPaths
                .buildLocalizationDirectory(FORMATS_DIRECTORY);
        formatsDirObserver = new FormatsDirectoryUpdateObserver();
        formatsDir.addFileUpdatedObserver(formatsDirObserver);

        IPathManager manager = PathManagerFactory.getPathManager();
        LocalizationContext baseContext = manager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        textUtilDir = manager.getLocalizationFile(baseContext,
                HazardsConfigurationConstants.TEXT_UTILITIES_LOCALIZATION_DIR);
        textUtilDirObserver = new TextUtilitiesDirectoryUpdateObserver();
        textUtilDir.addFileUpdatedObserver(textUtilDirObserver);

        eventUtilDir = manager.getLocalizationFile(baseContext,
                HazardsConfigurationConstants.EVENT_UTILITIES_LOCALIZATION_DIR);
        eventUtilDirObserver = new EventUtilitiesDirectoryUpdateObserver();
        eventUtilDir.addFileUpdatedObserver(eventUtilDirObserver);

        String scriptPath = PythonBuildPaths
                .buildDirectoryPath(PRODUCTS_DIRECTORY, site);
        jep.eval(INTERFACE + " = " + PYTHON_INTERFACE + "('" + scriptPath
                + "', '" + HazardsConfigurationConstants.PYTHON_EVENTS_DIRECTORY
                + File.separator + PRODUCTS_DIRECTORY + "', '" + site + "')");
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
     *            Product ID of the product type being generated
     * @param eventSet
     *            The set of events used by the generator to create the product
     * @param dialogValues
     *            The values extracted from the dialog, if any
     * @param formats
     *            Optional array of formatters to be run after the generator
     * @return GeneratedProductList object containing all products produced by
     *         the generator
     */
    public GeneratedProductList generateProduct(String product,
            EventSet<IEvent> eventSet, Map<String, Serializable> dialogValues,
            String... formats) {

        Map<String, Object> args = new HashMap<String, Object>(
                getStarterMap(product));
        args.put(EVENT_SET, eventSet);
        args.put(DIALOG_INPUT_MAP, dialogValues);
        args.put(FORMATS, Arrays.asList(formats));
        GeneratedProductList retVal = null;
        try {
            if (this.verifyProductGeneratorIsLoaded(product) == false) {
                return new GeneratedProductList();
            }

            // Run the generator and formatters
            retVal = formatProduct(product, formats,
                    (GeneratedProductList) execute(GENERATOR_EXECUTE_METHOD,
                            INTERFACE, args),
                    false);

        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to execute product generator", e);
        }

        return retVal;
    }

    /**
     * Updates the dataList from a previous generator call and generates a list
     * of IGeneratedProducts.
     * 
     * @param product
     *            Product ID of the product type being generated
     * @param eventSet
     *            The set of events used by the generator to create the product
     * @param dataList
     *            The dictionarys from the previous genenrator call
     * @param formats
     *            Optional array of formatters to be run after the generator
     * @return GeneratedProductList object containing all products produced by
     *         the generator
     */
    public GeneratedProductList updateProduct(String product,
            EventSet<IEvent> eventSet, List<Map<String, Serializable>> dataList,
            String... formats) {
        Map<String, Object> args = new HashMap<String, Object>(
                getStarterMap(product));
        args.put(EVENT_SET, eventSet);
        args.put(DATA_LIST, dataList);
        args.put(FORMATS, Arrays.asList(formats));
        GeneratedProductList retVal = null;
        try {
            if (this.verifyProductGeneratorIsLoaded(product) == false) {
                return new GeneratedProductList();
            }

            // Run the generator update method and formatters
            retVal = formatProduct(product, formats,
                    (GeneratedProductList) execute(GENERATOR_UPDATE_METHOD,
                            INTERFACE, args),
                    true);

        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to execute product generator update method", e);
        }

        return retVal;
    }

    /**
     * Passes the updatedDataList to the python formatters to return a new
     * GeneratedProductList.
     * 
     * @param product
     * @param eventSet
     * @param updatedDataList
     * @param keyInfo
     * @param formats
     * @return
     */
    public GeneratedProductList generateProductFrom(String product,
            GeneratedProductList generatedProducts, KeyInfo keyInfo,
            String[] formats) {

        Map<String, Object> args = new HashMap<String, Object>(
                getStarterMap(product));
        args.put(GENERATED_PRODUCT, generatedProducts);
        args.put(KEY_INFO, keyInfo);
        args.put(FORMATS, Arrays.asList(formats));
        GeneratedProductList retVal = null;
        try {
            if (this.verifyProductGeneratorIsLoaded(product) == false) {
                return new GeneratedProductList();
            }

            // Run the generator executeFrom method and formatters
            retVal = formatProduct(product, formats,
                    (GeneratedProductList) execute(
                            GENERATOR_EXECUTE_FROM_METHOD, INTERFACE, args),
                    false);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to update the generated products", e);
        }

        return retVal;
    }

    private GeneratedProductList formatProduct(String product, String[] formats,
            GeneratedProductList retVal, boolean overrideProductText) {
        Map<String, Object> args = new HashMap<String, Object>(
                getStarterMap(product));

        try {
            if (formats != null && formats.length > 0) {
                args = new HashMap<String, Object>(getStarterMap(product));
                args.put(GENERATED_PRODUCT, retVal);
                args.put(FORMATS, Arrays.asList(formats));
                args.put(OVERRIDE_PRODUCT_TEXT, overrideProductText);
                retVal = (GeneratedProductList) execute(FORMATTER_METHOD,
                        INTERFACE, args);
            }
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to execute product formatter", e);
        }
        return retVal;
    }

    /**
     * Retrieves the information to define a dialog from the product.
     * 
     * @param product
     * @param eventSet
     * @return
     */
    public Map<String, Serializable> getDialogInfo(String product,
            EventSet<IEvent> eventSet) {
        return getInfo(product, GET_DIALOG_INFO, eventSet);
    }

    /**
     * Retrieves the metadata of the product.
     * 
     * @param product
     * @param eventSet
     * @return
     */
    public Map<String, Serializable> getScriptMetadata(String product,
            EventSet<IEvent> eventSet) {
        return getInfo(product, GET_SCRIPT_METADATA, eventSet);
    }

    /**
     * Executes the method of the module.
     * 
     * @param moduleName
     *            name of the python module to execute
     * @param methodName
     *            name of the method to execute
     * @param eventSet
     * @return
     */
    @SuppressWarnings("unchecked")
    private Map<String, Serializable> getInfo(String moduleName,
            String methodName, EventSet<IEvent> eventSet) {
        Map<String, Object> args = new HashMap<String, Object>(
                getStarterMap(moduleName));
        args.put(EVENT_SET, eventSet);
        Map<String, Serializable> retVal = null;
        try {
            if (this.verifyProductGeneratorIsLoaded(moduleName) == false) {
                return new HashMap<String, Serializable>();
            }

            retVal = (Map<String, Serializable>) execute(methodName, INTERFACE,
                    args);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to get info from " + methodName, e);
        }

        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.python.controller.PythonScriptController#
     * fileUpdated (com.raytheon.uf.common.localization.FileUpdatedMessage)
     */
    @Override
    public void fileUpdated(FileUpdatedMessage message) {
        String[] dirs = message.getFileName().split(File.separator);
        String name = dirs[dirs.length - 1];
        String filename = resolveCorrectName(name);

        if (message.getChangeType() == FileChangeType.DELETED) {
            IPathManager pm = PathManagerFactory.getPathManager();
            LocalizationFile lf = pm.getLocalizationFile(message.getContext(),
                    message.getFileName());
            if (lf != null) {
                File toDelete = lf.getFile();
                toDelete.delete();
            }
            pendingRemoves.add(filename);

            // Check to see if a another level needs loaded
            Map<LocalizationLevel, LocalizationFile> map = pm
                    .getTieredLocalizationFile(LocalizationType.COMMON_STATIC,
                            message.getFileName());

            // Remove the deleted level
            if (map.containsKey(lf.getContext().getLocalizationLevel())) {
                map.remove(lf.getContext().getLocalizationLevel());
            }

            // If another level exists load it
            if (!map.isEmpty()) {
                pendingAdds.add(filename);
            }

        } else {
            super.fileUpdated(message);
        }
    }

    /**
     * Checks to see if the product generator is already set in the inventory.
     * Otherwise, a new one is initialized and added to the inventory.
     * 
     * @param productGeneratorName
     * @return
     */
    public boolean verifyProductGeneratorIsLoaded(String productGeneratorName) {
        processFileUpdates();
        // If there are pending formatter updates reload the formatters
        if (pendingFormatterUpdates) {
            try {
                reloadFormatters();
                pendingFormatterUpdates = false;
            } catch (JepException e) {
                statusHandler.handle(Priority.WARN,
                        "Product Formatters were unable to be imported", e);
            }
        }

        // If there are pending TextUtilities updates reload the modules
        if (pendingTextUtilitiesUpdates) {
            try {
                reloadTextUtilities();
                pendingTextUtilitiesUpdates = false;
            } catch (JepException e) {
                statusHandler.handle(Priority.WARN,
                        "Text Utilities were unable to be imported", e);
            }
        }

        // If there are pending EventUtilities updates reload the modules
        if (pendingEventUtilitiesUpdates) {
            try {
                reloadEventUtilities();
                pendingEventUtilitiesUpdates = false;
            } catch (JepException e) {
                statusHandler.handle(Priority.WARN,
                        "Event Utilities were unable to be imported", e);
            }
        }

        return this.initializeProductGenerator(productGeneratorName);
    }

    private boolean initializeProductGenerator(String productGeneratorName) {
        LocalizationFile localizationFile = this
                .lookupProductGeneratorLocalization(productGeneratorName);
        if (localizationFile == null) {
            statusHandler.handle(Priority.PROBLEM,
                    "Unable to find Product Generator: " + productGeneratorName
                            + "!");
            return false;
        }
        // load the product generator.
        ProductInfo productInfo = setMetadata(localizationFile);
        if (productInfo == null) {
            statusHandler.handle(Priority.PROBLEM,
                    "Failed to initialize Product Generator: "
                            + productGeneratorName + "!");
            return false;
        }
        return true;
    }

    private LocalizationFile lookupProductGeneratorLocalization(
            final String productGeneratorName) {
        IPathManager manager = PathManagerFactory.getPathManager();
        LocalizationFile[] lFiles = manager.listStaticFiles(
                productsDir.getName(),
                new String[] { PYTHON_FILE_EXTENSION.substring(1) }, false,
                true);

        for (LocalizationFile lFile : lFiles) {
            final String modName = resolveCorrectName(
                    lFile.getFile().getName());
            if (productGeneratorName.equals(modName)) {
                return lFile;
            }
        }
        return null;
    }

    /**
     * Removes .py from the filename
     * 
     * @param name
     * @return
     */

    private static String resolveCorrectName(String name) {
        if (name.endsWith(PYTHON_FILE_EXTENSION)) {
            name = name.replace(PYTHON_FILE_EXTENSION, "");
        }
        return name;
    }

    /**
     * Reloads the updated formatter modules in the interpreter's "cache".
     * 
     * @throws JepException
     *             If an Error is thrown during python execution.
     */
    protected void reloadFormatters() throws JepException {
        execute("importFormatters", INTERFACE, null);
    }

    /**
     * Reloads the updated textUtilities modules in the interpreter's "cache".
     * 
     * @throws JepException
     *             If an Error is thrown during python execution.
     */
    protected void reloadTextUtilities() throws JepException {
        execute("importTextUtility", INTERFACE, null);
    }

    /**
     * Reloads the updated eventUtilities modules in the interpreter's "cache".
     * 
     * @throws JepException
     *             If an Error is thrown during python execution.
     */
    protected void reloadEventUtilities() throws JepException {
        execute("importEventUtility", INTERFACE, null);
    }

    /**
     * Retrieves the metadata of the product generator and sets it in the
     * product info object.
     * 
     * @param file
     * @return
     */

    @SuppressWarnings("unchecked")
    private ProductInfo setMetadata(LocalizationFile file) {
        final String modName = resolveCorrectName(file.getFile().getName());
        Map<String, Serializable> results = null;
        try {
            reloadModule(modName);
            instantiatePythonScript(modName);
            Map<String, Object> args = getStarterMap(modName);
            results = (Map<String, Serializable>) execute(GET_SCRIPT_METADATA,
                    INTERFACE, args);
        } catch (JepException e) {
            statusHandler.handle(Priority.WARN, "Product Generator " + modName
                    + " is unable to be instantiated", e);
            return null;
        }
        ProductInfo productInfo = new ProductInfo();
        productInfo.setName(modName);
        productInfo.setFile(file);
        if (results != null) {
            Object auth = results.get(ProductInfo.AUTHOR);
            Object desc = results.get(ProductInfo.DESCRIPTION);
            Object vers = results.get(ProductInfo.VERSION);
            productInfo.setAuthor(auth != null ? auth.toString() : "");
            productInfo.setDescription(desc != null ? desc.toString() : "");
            productInfo.setVersion(vers != null ? vers.toString() : "");
        }
        return productInfo;
    }

    private class FormatsDirectoryUpdateObserver
            implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            IPathManager pm = PathManagerFactory.getPathManager();
            LocalizationFile lf = pm.getLocalizationFile(message.getContext(),
                    message.getFileName());

            if (message.getChangeType() == FileChangeType.ADDED
                    || message.getChangeType() == FileChangeType.UPDATED) {
                if (lf != null) {
                    lf.getFile();
                }
            } else if (message.getChangeType() == FileChangeType.DELETED) {
                if (lf != null) {
                    File toDelete = lf.getFile();
                    toDelete.delete();
                }

            }
            pendingFormatterUpdates = true;
        }
    }

    private class TextUtilitiesDirectoryUpdateObserver
            implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            IPathManager pm = PathManagerFactory.getPathManager();
            LocalizationFile lf = pm.getLocalizationFile(message.getContext(),
                    message.getFileName());

            if (message.getChangeType() == FileChangeType.ADDED
                    || message.getChangeType() == FileChangeType.UPDATED) {
                if (lf != null) {
                    lf.getFile();
                }
            } else if (message.getChangeType() == FileChangeType.DELETED) {
                if (lf != null) {
                    File toDelete = lf.getFile();
                    toDelete.delete();
                }
            }
            pendingTextUtilitiesUpdates = true;
        }
    }

    private class EventUtilitiesDirectoryUpdateObserver
            implements ILocalizationFileObserver {

        @Override
        public void fileUpdated(FileUpdatedMessage message) {
            IPathManager pm = PathManagerFactory.getPathManager();
            LocalizationFile lf = pm.getLocalizationFile(message.getContext(),
                    message.getFileName());

            if (message.getChangeType() == FileChangeType.ADDED
                    || message.getChangeType() == FileChangeType.UPDATED) {
                if (lf != null) {
                    lf.getFile();
                }
            } else if (message.getChangeType() == FileChangeType.DELETED) {
                if (lf != null) {
                    File toDelete = lf.getFile();
                    toDelete.delete();
                }
            }
            pendingEventUtilitiesUpdates = true;
        }
    }
}
