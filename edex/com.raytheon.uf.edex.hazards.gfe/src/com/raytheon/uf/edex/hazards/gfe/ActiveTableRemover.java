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
package com.raytheon.uf.edex.hazards.gfe;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardNotification;
import com.raytheon.uf.common.serialization.SerializationUtil;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 12, 2014            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class ActiveTableRemover {

    private static final ActiveTableRemover instance = new ActiveTableRemover();

    private ActiveTableRemover() {
    }

    public static synchronized ActiveTableRemover getInstance() {
        return instance;
    }

    public void handleNotification(byte[] bytes) throws Exception {
        HazardNotification notification = SerializationUtil
                .transformFromThrift(HazardNotification.class, bytes);

        switch (notification.getType()) {
        // case DELETE_ALL:
        // ClearPracticeVTECTableRequest request = new
        // ClearPracticeVTECTableRequest();
        // RequestRouter.route(request);
        // break;
        default:
            break;
        }
    }
}
