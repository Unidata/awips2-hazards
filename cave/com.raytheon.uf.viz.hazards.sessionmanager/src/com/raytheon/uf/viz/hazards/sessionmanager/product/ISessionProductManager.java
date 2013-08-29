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

import java.util.Collection;

/**
 * Manages product generation for a session.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 19, 2013 1257       bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public interface ISessionProductManager {

    /**
     * Returns all products that can be generated based off the current
     * selection.
     * 
     * @return
     */
    public Collection<ProductInformation> getSelectedProducts();

    /**
     * Generate a product from the given information.
     * 
     * @param information
     *            the information about the product to generate
     * @param issue
     *            whether to immediately issue the product or not
     */
    public void generate(ProductInformation information, boolean issue);

    /**
     * Issue the provided product and all the events associated with it.
     * 
     * @param information
     */
    public void issue(ProductInformation information);

    /**
     * Execute any shutdown needed.
     */
    public void shutdown();
}
