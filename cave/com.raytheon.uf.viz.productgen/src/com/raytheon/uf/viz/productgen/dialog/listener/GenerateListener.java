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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.custom.StyledText;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.productgen.dialog.ProductGenerationDialog;
import com.raytheon.uf.viz.productgen.dialog.formats.AbstractFormatTab;
import com.raytheon.uf.viz.productgen.dialog.formats.TextFormatTab;

/**
 * Updates the generated product list storage with updated generated product
 * list.
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

public class GenerateListener implements
        IPythonJobListener<GeneratedProductList> {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(GenerateListener.class);

    private ProductGenerationDialog dialog;

    public GenerateListener(ProductGenerationDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void jobFinished(final GeneratedProductList productList) {
        VizApp.runAsync(new Runnable() {
            public void run() {
                int offset = 0;
                for (GeneratedProductList products : dialog
                        .getGeneratedProductListStorage()) {
                    if (products.getProductInfo().equals(
                            productList.getProductInfo())) {
                        int index = 0;
                        for (IGeneratedProduct product : productList) {
                            products.set(index, product);
                            Set<String> formats = product.getEntries().keySet();
                            Map<String, AbstractFormatTab> formatTabMap = dialog
                                    .getFormatTabMap(offset + index);
                            for (String format : formats) {
                                List<Serializable> entries = product
                                        .getEntry(format);
                                int counter = 0;
                                for (Serializable entry : entries) {
                                    String label = format;
                                    if (entries.size() > 2) {
                                        label = String
                                                .format(ProductGenerationDialog.TAB_LABEL_FORMAT,
                                                        format, counter);
                                    }
                                    AbstractFormatTab tab = formatTabMap
                                            .get(label);

                                    if (tab instanceof TextFormatTab) {
                                        TextFormatTab textTab = (TextFormatTab) tab;
                                        StyledText styledText = textTab
                                                .getText();

                                        String finalProduct = String
                                                .valueOf(entry);
                                        styledText.setText(finalProduct);
                                    }
                                    counter++;
                                }
                            }
                            index++;
                        }
                    }
                    offset += products.size();
                }

                // reset the Formatted tab
                dialog.resetFormattedKeyCombos();
            };
        });

    }

    @Override
    public void jobFailed(Throwable e) {
        handler.error("Unable to run product generation", e);

    }

}
