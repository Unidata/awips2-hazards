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

import com.raytheon.uf.common.dataplugin.events.hazards.requests.HasConflictsRequest;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.TimeConstraints;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Handler to check if the IHazardEvent in the request has any conflicts with
 * any existing grids.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013 2277       jsanchez     Initial creation
 * Mar 24, 2014 3323       bkowal       Use the mode to retrieve the correct
 *                                      grid when checking for conflicts.
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class HasConflictsHandler implements
        IRequestHandler<HasConflictsRequest> {

    @Override
    public Object handleRequest(HasConflictsRequest request) throws Exception {
        TimeRange timeRange = GFERecordUtil.createGridTimeRange(request
                .getStartTime(), request.getEndTime(), new TimeConstraints(
                TimeUtil.SECONDS_PER_HOUR, TimeUtil.SECONDS_PER_HOUR, 0));
        boolean hasConflicts = GridValidator.hasConflicts(request.getMode(),
                request.getPhenSig(), timeRange, request.getSiteID());
        return hasConflicts;
    }
}