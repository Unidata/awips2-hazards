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

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;

/**
 * Notification that is sent out when a product is successfully generated.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 19, 2013 1257       bsteffen    Initial creation
 * Nov  5, 2013 2266       jsanchez    Used GeneratedProductList.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class ProductGenerated extends ProductModified implements
        ISessionNotification {

    public ProductGenerated(ProductGeneratorInformation productGeneratorInformation) {
        super(productGeneratorInformation);
    }

    public GeneratedProductList getGeneratedProducts() {
        return getProductGeneratorInformation().getGeneratedProducts();
    }

}
