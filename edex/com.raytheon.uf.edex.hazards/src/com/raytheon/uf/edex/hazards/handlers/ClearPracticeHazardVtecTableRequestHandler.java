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

import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.HazardVtecServicesSoapClient;
import com.raytheon.uf.common.dataplugin.events.hazards.request.ClearPracticeHazardVtecTableRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventQueryRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventVtecResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.CollectionUtil;

/**
 * 
 * Handler used to clear Hazard Event VTEC table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 4/5/2016     16577    Ben.Phillippe Initial creation
 * 5/9/2016     18193    Ben.Phillippe Fixed to purge from registry instead of database table.
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class ClearPracticeHazardVtecTableRequestHandler implements
        IRequestHandler<ClearPracticeHazardVtecTableRequest> {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ClearPracticeHazardVtecTableRequestHandler.class);

    @Override
    public Boolean handleRequest(ClearPracticeHazardVtecTableRequest request)
            throws Exception {
        statusHandler.info("Purging practice VTEC records...");

        HazardEventVtecResponse response = HazardVtecServicesSoapClient
                .getServices(true).deleteVtecByQuery(
                        new HazardEventQueryRequest(true));
        if (response.isSuccess()) {
            return Boolean.TRUE;
        } else if (!CollectionUtil.isNullOrEmpty(response.getExceptions())) {
            for (int i = 0; i < response.getExceptions().size(); i++) {
                statusHandler.error(
                        "Error Purging Practice Hazard VTEC Records (Error #"
                                + (i + 1) + ")", response.getExceptions()
                                .get(i));
            }
            // Throw the first exception
            throw new Exception(
                    "Error purging practice VTEC records. See EDEX logs for further details.",
                    response.getExceptions().get(0));
        } else {
            // If no exceptions were included in the response, simply return
            // false
            return Boolean.FALSE;
        }
    }
}
