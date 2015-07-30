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
package com.raytheon.uf.viz.hazards.sessionmanager.product.impl;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGeneratorInformation;

/**
 * Listener when passing the product data to the update method of product
 * generation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 17, 2014  1480      jsanchez     Initial creation
 * Jul 30, 2015  9681      Robert.Blum Changes to work with viewOnly
 *                                     products.
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class UpdateListener implements IPythonJobListener<GeneratedProductList> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(UpdateListener.class);

    private final ISessionNotificationSender notificationSender;

    private final ProductGeneratorInformation productGeneratorInformation;

    private final boolean correctable;

    private final boolean viewOnly;

    public UpdateListener(
            ProductGeneratorInformation productGeneratorInformation,
            ISessionNotificationSender notificationSender) {
        this.productGeneratorInformation = productGeneratorInformation;
        this.notificationSender = notificationSender;
        this.correctable = productGeneratorInformation.getGeneratedProducts()
                .isCorrectable();
        this.viewOnly = productGeneratorInformation.getGeneratedProducts()
                .isViewOnly();
    }

    @Override
    public void jobFinished(GeneratedProductList generatedProductList) {
        generatedProductList.setCorrectable(correctable);
        generatedProductList.setViewOnly(viewOnly);
        generatedProductList.setEventSet(productGeneratorInformation
                .getGeneratedProducts().getEventSet());
        productGeneratorInformation.setGeneratedProducts(generatedProductList);
        notificationSender.postNotification(new ProductGenerated(
                productGeneratorInformation));
    }

    @Override
    public void jobFailed(Throwable e) {
        statusHandler.error("Error executing the update method", e);
    }

}
