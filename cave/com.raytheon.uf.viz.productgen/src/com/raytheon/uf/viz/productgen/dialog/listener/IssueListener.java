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
package com.raytheon.uf.viz.productgen.dialog.listener;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.productgen.dialog.ProductGenerationDialog;

/**
 * Updates the generated product list storage and saves the modified keys to the
 * database.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 10, 2014            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class IssueListener implements IPythonJobListener<GeneratedProductList> {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(IssueListener.class);

    private ProductGenerationDialog dialog;

    private static int issueCounter = 0;

    public IssueListener(ProductGenerationDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void jobFinished(final GeneratedProductList productList) {
        VizApp.runAsync(new Runnable() {
            public void run() {

                int totalSize = 0;
                for (GeneratedProductList products : dialog
                        .getGeneratedProductListStorage()) {
                    if (products.getProductInfo().equals(
                            productList.getProductInfo())) {
                        int index = 0;
                        for (IGeneratedProduct product : productList) {
                            products.set(index, product);
                            index++;
                            issueCounter++;
                        }
                    }
                    totalSize += products.size();
                }

                // Indicates that all generation is completed
                if (issueCounter == totalSize) {
                    issueCounter = 0;
                    dialog.invokeIssue();
                    // saving the user edits
                    dialog.save();
                    dialog.close();
                }
            };
        });
    }

    @Override
    public void jobFailed(Throwable e) {
        handler.error("Unable to issue", e);
    }

}
