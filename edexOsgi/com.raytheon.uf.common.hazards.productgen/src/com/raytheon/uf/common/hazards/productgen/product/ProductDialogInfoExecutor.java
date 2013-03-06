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

import java.util.Map;

import com.raytheon.uf.common.python.concurrent.IPythonExecutor;

/**
 * Executes the getDialogInfo method of ProductScript
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 1, 2013            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductDialogInfoExecutor implements
        IPythonExecutor<ProductScript, Map<String, String>> {

    /** Name of the product generator */
    private String product;

    /**
     * Constructor.
     * 
     * @param product
     */
    public ProductDialogInfoExecutor(String product) {
        this.product = product;
    }

    @Override
    public Map<String, String> execute(ProductScript script) {
        return script.getDialogInfo(product);
    }

}
