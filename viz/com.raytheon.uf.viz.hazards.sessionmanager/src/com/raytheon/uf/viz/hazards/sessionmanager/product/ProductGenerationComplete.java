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

import com.google.common.collect.ImmutableList;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;

import gov.noaa.gsd.common.utilities.IMergeable;
import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Notification that is sent when the generation of all products for a
 * particular request have been completed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 10, 2014    2890    bkowal       Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Implemented merge() method and
 *                                      removed superfluous implementation
 *                                      of interface.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
public class ProductGenerationComplete implements ISessionNotification {

    // Private Variables

    /**
     * Flag indicating whether or not the generation was in response to an
     * issuance.
     */
    private boolean issued;

    /**
     * Generated products.
     */
    private List<GeneratedProductList> generatedProducts;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param issued
     *            Flag indicating whether or not the generation was in response
     *            to an issuance.
     * @param generatedProducts
     *            Generated products.
     */
    public ProductGenerationComplete(boolean issued,
            List<GeneratedProductList> generatedProducts) {
        this.issued = issued;
        this.generatedProducts = ImmutableList.copyOf(generatedProducts);
    }

    // Public Methods

    /**
     * Determine whether or not the generation was in response to an issuance.
     * 
     * @return <code>true</code> if the generation was in response to an
     *         issuance, <code>false</code> otherwise.
     */
    public boolean isIssued() {
        return issued;
    }

    /**
     * Get the generated products. Note that the returned list is not
     * modifiable.
     * 
     * @return Generated products.
     */
    public List<GeneratedProductList> getGeneratedProducts() {
        return this.generatedProducts;
    }

    @Override
    public MergeResult<ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {
        return IMergeable.getFailureResult();
    }
}