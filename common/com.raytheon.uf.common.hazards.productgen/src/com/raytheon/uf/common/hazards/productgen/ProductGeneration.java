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
package com.raytheon.uf.common.hazards.productgen;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.interfaces.IDefineDialog;
import com.raytheon.uf.common.dataplugin.events.interfaces.IProvideMetadata;
import com.raytheon.uf.common.hazards.productgen.executors.ProductDialogInfoExecutor;
import com.raytheon.uf.common.hazards.productgen.executors.ProductMetadataExecutor;
import com.raytheon.uf.common.hazards.productgen.executors.ProductScriptExecutor;
import com.raytheon.uf.common.hazards.productgen.executors.ProductScriptUpdater;
import com.raytheon.uf.common.hazards.productgen.product.ProductScript;
import com.raytheon.uf.common.hazards.productgen.product.ProductScriptFactory;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.concurrent.IPythonExecutor;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.python.concurrent.PythonJobCoordinator;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * 
 * Generates product into different formats based on a eventSet.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 10, 2013            jsanchez     Initial creation
 * Sep 19, 2013 2046       mnash        Update for less dependencies.
 * Nov  5, 2013 2266       jsanchez     Removed unused method and used GeneratedProductList.
 * Apr 23, 2014 1480       jsanchez     Passed correction flag to update method.
 * Oct 03, 2014 4042       Chris.Golden Added ability to get product script file path.
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
public class ProductGeneration implements IDefineDialog, IProvideMetadata {

    private static final String PRODUCT_GENERATOR_RELATIVE_PATH = "python"
            + File.separator + "events" + File.separator + "productgen"
            + File.separator + "products" + File.separator;

    private static final String PYTHON_FILENAME_SUFFIX = ".py";

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductGeneration.class);

    /** Manages ProductScriptExecutor jobs */
    private final PythonJobCoordinator<ProductScript> coordinator = PythonJobCoordinator
            .newInstance(new ProductScriptFactory());

    private final IPathManager pathManager = PathManagerFactory
            .getPathManager();

    /**
     * Generates the eventSet into different formats. The job is performed
     * asynchronously and will be passed to the session manager.
     * 
     * @param productGeneratorName
     *            name of the product generator. "ExampleFFW" refers to the
     *            python class "ExampleFFW.py" which should be in the
     *            /common_static/base/python/events/productgen/products
     *            directory
     * @param eventSet
     *            the EventSet<IEvent> object that will provide the information
     *            for the product generator
     * @param productFormats
     *            array of formats to be generated (i.e. "XML", "ASCII")
     * @param listener
     *            the listener to the aysnc job
     */
    public void generate(String productGeneratorName,
            EventSet<IEvent> eventSet, Map<String, Serializable> dialogInfo,
            String[] productFormats,
            IPythonJobListener<GeneratedProductList> listener) {
        // Validates the parameter values
        validate(productFormats, productGeneratorName, eventSet, listener);

        IPythonExecutor<ProductScript, GeneratedProductList> executor = new ProductScriptExecutor(
                productGeneratorName, eventSet, dialogInfo, productFormats);

        try {
            coordinator.submitAsyncJob(executor, listener);
        } catch (Exception e) {
            statusHandler.error("Error executing async job", e);
        }
    }

    /**
     * Accepts an updated data list and passes it to the 'executeFrom' of the
     * product generator.
     * 
     * @param productGeneratorName
     * @param updatedDataList
     * @param productFormats
     * @param listener
     */
    public void update(String productGeneratorName,
            List<LinkedHashMap<KeyInfo, Serializable>> updatedDataList,
            List<LinkedHashMap<KeyInfo, Serializable>> prevDataList,
            String[] productFormats,
            IPythonJobListener<GeneratedProductList> listener) {
        IPythonExecutor<ProductScript, GeneratedProductList> executor = new ProductScriptUpdater(
                productGeneratorName, updatedDataList, prevDataList,
                productFormats);

        try {
            coordinator.submitAsyncJob(executor, listener);
        } catch (Exception e) {
            statusHandler.error("Error executing async job", e);
        }
    }

    @Override
    public Map<String, Serializable> getDialogInfo(String product,
            EventSet<IEvent> eventSet) {
        IPythonExecutor<ProductScript, Map<String, Serializable>> executor = new ProductDialogInfoExecutor(
                product, eventSet);
        Map<String, Serializable> retVal = null;
        try {
            retVal = coordinator.submitSyncJob(executor);
        } catch (Exception e) {
            statusHandler.error("Error executing job", e);
        }

        return retVal;
    }

    /**
     * Get the file holding the script for the specified product.
     * 
     * @param product
     *            Product for which to find the script file.
     * @return Script file.
     */
    public File getScriptFile(String product) {
        return pathManager.getStaticLocalizationFile(
                PRODUCT_GENERATOR_RELATIVE_PATH + product
                        + PYTHON_FILENAME_SUFFIX).getFile();
    }

    @Override
    public Map<String, Serializable> getMetadata(String product,
            EventSet<IEvent> eventSet) {
        IPythonExecutor<ProductScript, Map<String, Serializable>> executor = new ProductMetadataExecutor(
                product, eventSet);
        Map<String, Serializable> retVal = null;
        try {
            retVal = coordinator.submitSyncJob(executor);
        } catch (Exception e) {
            statusHandler.error("Error executing job", e);
        }

        return retVal;
    }

    /**
     * Validates 1) if 'formats' is not null, 2) if 'product' is not null, and
     * 3) if hazardEvent set is not null and not empty.
     * 
     * @param formats
     * @param product
     * @param eventSet
     */
    private void validate(String[] formats, String product,
            EventSet<IEvent> eventSet,
            IPythonJobListener<GeneratedProductList> listener) {
        Validate.notNull(formats, "'FORMATS' must be set.");
        Validate.notNull(product, "'PRODUCT' must be set.");
        Validate.notNull(eventSet, "'HAZARD EVENT SET' must be set");
        Validate.isTrue(!eventSet.isEmpty(), "HAZARD EVENT SET can't be empty");
        Validate.notNull(listener, "'listener' must be set.");
    }

}
