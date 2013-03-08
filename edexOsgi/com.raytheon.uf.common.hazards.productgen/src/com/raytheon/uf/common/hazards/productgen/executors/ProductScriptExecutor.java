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
package com.raytheon.uf.common.hazards.productgen.executors;

import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardEventSet;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.product.ProductScript;

/**
 * Executes the generateProduct method of ProductScript
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

public class ProductScriptExecutor extends
        AbstractProductExecutor<List<IGeneratedProduct>> {

    /** provide the information for the product generator */
    private HazardEventSet hazardEventSet;

    /** String array of formats */
    private String[] formats;

    /** Name of the product generator */
    private String product;

    /**
     * Constructor.
     * 
     * @param product
     *            name of the product generator
     * @param hazardEventSet
     *            the HazardEventSet object that will provide the information
     *            for the product generator
     */
    public ProductScriptExecutor(String product, HazardEventSet hazardEventSet,
            String[] formats) {
        this.product = product;
        this.hazardEventSet = hazardEventSet;
        this.formats = formats;
    }

    @Override
    public List<IGeneratedProduct> execute(ProductScript script) {
        return script.generateProduct(product, hazardEventSet, formats);
    }

}
