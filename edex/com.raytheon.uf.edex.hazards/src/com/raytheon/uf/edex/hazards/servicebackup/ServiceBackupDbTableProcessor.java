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

package com.raytheon.uf.edex.hazards.servicebackup;

import java.io.File;

import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.uf.common.hazards.productgen.data.ProductDataUtil;
import com.raytheon.uf.common.hazards.productgen.editable.ProductText;
import com.raytheon.uf.common.hazards.productgen.editable.ProductTextUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 *
 * Imports Hazard Services database tables for service backup.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 10, 2016 22119      Kevin.Bisanz Initial creation
 *
 * </pre>
 *
 * @author kbisanz
 */
public class ServiceBackupDbTableProcessor {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ServiceBackupDbTableProcessor.class);

    public void processEvent(File file) {
        String fileName = file.getName();
        String extension = ".bin";

        statusHandler
                .info("Processing Hazard Services SVCBU database table import "
                        + fileName);

        if (fileName.endsWith(ProductText.class.getSimpleName() + extension)) {
            ProductTextUtil.importProductText(file.getAbsolutePath());
        } else if (fileName
                .endsWith(ProductData.class.getSimpleName() + extension)) {
            ProductDataUtil.importProductData(file.getAbsolutePath());
        } else {
            statusHandler.warn("Unexpected file name of " + fileName);
        }
    }
}