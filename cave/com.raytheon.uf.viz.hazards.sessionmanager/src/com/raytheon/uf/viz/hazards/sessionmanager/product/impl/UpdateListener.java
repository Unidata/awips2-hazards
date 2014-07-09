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
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductInformation;

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

    private ProductInformation information;

    public UpdateListener(ProductInformation information,
            ISessionNotificationSender notificationSender) {
        this.information = information;
        this.notificationSender = notificationSender;
    }

    @Override
    public void jobFinished(GeneratedProductList generatedProductList) {
        generatedProductList.setCorrectable(true);
        generatedProductList.setEventSet(information.getProducts()
                .getEventSet());
        information.setProducts(generatedProductList);
        notificationSender.postNotification(new ProductGenerated(information));
    }

    @Override
    public void jobFailed(Throwable e) {
        statusHandler.error("Error executing the update method", e);
    }

}
