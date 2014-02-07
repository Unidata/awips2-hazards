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
package com.raytheon.uf.viz.hazards.sessionmanager.product;

import java.util.List;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;

/**
 * Notification that is sent when the generation of all products
 * for a particular request have been completed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 10, 2014 2890       bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class ProductGenerationComplete implements IProductGenerationComplete {
    private boolean issued;

    private List<GeneratedProductList> generatedProducts;

    /**
     * 
     */
    public ProductGenerationComplete(boolean issued,
            List<GeneratedProductList> generatedProducts) {
        this.issued = issued;
        this.generatedProducts = generatedProducts;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete
     * #isIssued()
     */
    @Override
    public boolean isIssued() {
        return this.issued;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete
     * #getGeneratedProducts()
     */
    @Override
    public List<GeneratedProductList> getGeneratedProducts() {
        return this.generatedProducts;
    }
}