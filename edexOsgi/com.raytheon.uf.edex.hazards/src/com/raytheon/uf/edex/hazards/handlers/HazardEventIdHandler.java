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
package com.raytheon.uf.edex.hazards.handlers;

import com.raytheon.uf.common.dataplugin.events.hazards.requests.HazardEventIdRequest;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.edex.database.cluster.ClusterLockUtils;
import com.raytheon.uf.edex.database.cluster.ClusterTask;

/**
 * Retrieves the next available hazard event id for the site given.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 11, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class HazardEventIdHandler implements
        IRequestHandler<HazardEventIdRequest> {

    private static final String LOCK_NAME = "Hazard Services Event Id";

    /**
     * 
     */
    public HazardEventIdHandler() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.serialization.comm.IRequestHandler#handleRequest
     * (com.raytheon.uf.common.serialization.comm.IServerRequest)
     */
    @Override
    public Object handleRequest(HazardEventIdRequest request) throws Exception {
        ClusterTask task = ClusterLockUtils.lookupLock(LOCK_NAME,
                request.getSiteId());

        task = ClusterLockUtils.lock(LOCK_NAME, request.getSiteId(),
                task.getExtraInfo(), 15, true);
        Integer eventId = 0;
        if (task.getExtraInfo() == null || task.getExtraInfo().isEmpty()) {
            // starting at 1
            eventId = 1;
        } else {
            eventId = Integer.parseInt(task.getExtraInfo()) + 1;
        }
        ClusterLockUtils.updateExtraInfo(LOCK_NAME, request.getSiteId(),
                String.valueOf(eventId));
        ClusterLockUtils.unlock(task, false);
        return eventId;
    }
}
