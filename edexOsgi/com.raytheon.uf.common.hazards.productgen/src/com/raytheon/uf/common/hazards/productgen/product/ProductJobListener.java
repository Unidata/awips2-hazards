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

import java.util.List;

import com.raytheon.uf.common.hazards.productgen.GeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;

/**
 * Listener when the asynchronous job ProductScriptExecutor finishes or fails
 * for generating a product.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 20, 2013            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductJobListener implements
        IPythonJobListener<List<IGeneratedProduct>> {

    @Override
    public void jobFinished(List<IGeneratedProduct> result) {
        // TODO Pass result to the SessionManager via EventBus or
        // use ProductGeneration's listener. In the meantime, a System.out is
        // used to see the results on the console.
        for (IGeneratedProduct generatedProduct : result) {
            System.out.println(generatedProduct.getEntries());
        }
    }

    @Override
    public void jobFailed(Throwable e) {
        GeneratedProduct generatedProduct = new GeneratedProduct(null);
        generatedProduct.setErrors(e.getLocalizedMessage());
        // TODO Pass result to the SessionManager via EventBus or
        // use ProductGeneration's listener
        System.out.println(e.getLocalizedMessage());
    }

}
