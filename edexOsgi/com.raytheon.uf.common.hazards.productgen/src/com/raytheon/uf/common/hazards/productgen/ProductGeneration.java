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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardEventSet;
import com.raytheon.uf.common.hazards.productgen.product.ProductDialogInfoExecutor;
import com.raytheon.uf.common.hazards.productgen.product.ProductDialogInfoJobListener;
import com.raytheon.uf.common.hazards.productgen.product.ProductJobListener;
import com.raytheon.uf.common.hazards.productgen.product.ProductScript;
import com.raytheon.uf.common.hazards.productgen.product.ProductScriptExecutor;
import com.raytheon.uf.common.hazards.productgen.product.ProductScriptFactory;
import com.raytheon.uf.common.python.concurrent.IPythonExecutor;
import com.raytheon.uf.common.python.concurrent.PythonJobCoordinator;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * 
 * Generates product into different formats based on a HazardEventSet.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 10, 2013            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
public class ProductGeneration {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductGeneration.class);

    /** Manages ProductScriptExecutor jobs */
    private PythonJobCoordinator<ProductScript> coordinator = PythonJobCoordinator
            .newInstance(new ProductScriptFactory());

    /**
     * Generates the hazardEventSet into different formats. The job is performed
     * asynchronously and will be passed to the session manager.
     * 
     * @param product
     *            name of the product generator. "ExampleFFW" refers to the
     *            python class "ExampleFFW.py" which should be in the
     *            /common_static/base/python/productgen/products directory
     * @param hazardEventSet
     *            the HazardEventSet object that will provide the information
     *            for the product generator
     * @param formats
     *            array of formats to be generated (i.e. "XML", "ASCII")
     */
    public void generate(String product, HazardEventSet hazardEventSet,
            String[] formats) {
        // Validates the parameter values
        validate(formats, product, hazardEventSet);

        PythonJobCoordinator<ProductScript> jobCoordinator = (PythonJobCoordinator<ProductScript>) coordinator
                .getInstance(ProductScriptFactory.NAME);
        IPythonExecutor<ProductScript, List<IGeneratedProduct>> executor = new ProductScriptExecutor(
                product, hazardEventSet, formats);

        try {
            jobCoordinator.submitAsyncJob(executor, new ProductJobListener());
        } catch (Exception e) {
            statusHandler.error("Error executing async job", e);
        }
    }

    /**
     * Generates an array of hazardEventSets into different formats.The job is
     * performed asynchronously and will be passed to the session manager.
     * 
     * @param product
     *            name of the product generator. "ExampleFFW" refers to the
     *            python class "ExampleFFW.py" which should be in the
     *            /common_static/base/python/productgen/products directory
     * @param hazardEventSets
     *            an array of HazardEventSet objects that will provide the
     *            information for the product generator
     * @param formats
     *            array of formats to be generated (i.e. "XML", "ASCII")
     */
    public void generate(String product, HazardEventSet[] hazardEventSets,
            String[] formats) {
        for (HazardEventSet hazardEventSet : hazardEventSets) {
            generate(product, hazardEventSet, formats);
        }
    }

    /**
     * Retrieves the dialog info for the product. The job is performed
     * asynchronously and will be passed to the session manager.
     * 
     * @param product
     *            name of the product generator. "ExampleFFW" refers to the
     *            python class "ExampleFFW.py" which should be in the
     *            /common_static/base/python/productgen/products directory
     */
    public void getDialogInfo(String product) {
        PythonJobCoordinator<ProductScript> jobCoordinator = (PythonJobCoordinator<ProductScript>) coordinator
                .getInstance(ProductScriptFactory.NAME);
        IPythonExecutor<ProductScript, Map<String, String>> executor = new ProductDialogInfoExecutor(
                product);
        try {
            jobCoordinator.submitAsyncJob(executor,
                    new ProductDialogInfoJobListener());
        } catch (Exception e) {
            statusHandler.error("Error executing async job", e);
        }
    }

    /**
     * Validates 1) if 'formats' is not null, 2) if 'product' is not null, and
     * 3) if hazardEvent set is not null and not empty.
     * 
     * @param formats
     * @param product
     * @param hazardEventSet
     */
    private void validate(String[] formats, String product,
            HazardEventSet hazardEventSet) {
        Validate.notNull(formats, "'FORMATS' must be set.");
        Validate.notNull(product, "'PRODUCT' must be set.");
        Validate.notNull(hazardEventSet, "'HAZARD EVENT SET' must be set");
        Validate.isTrue(!hazardEventSet.isEmpty(),
                "HAZARD EVENT SET can't be empty");
    }

}
