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

import java.io.Serializable;
import java.util.Map;

import com.raytheon.uf.common.hazards.productgen.product.ProductScript;

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

public class ProductDialogInfoExecutor extends
        AbstractProductExecutor<Map<String, Serializable>> {

    public ProductDialogInfoExecutor(String product) {
        this.product = product;
    }

    @Override
    public Map<String, Serializable> execute(ProductScript script) {
        return script.getDialogInfo(product);
    }

}
