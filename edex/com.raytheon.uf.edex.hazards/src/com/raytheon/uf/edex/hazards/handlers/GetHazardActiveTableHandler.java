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

import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.HazardVtecServicesSoapClient;
import com.raytheon.uf.common.dataplugin.events.hazards.request.GetHazardActiveTableRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventVtecResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * 
 * Handler used to process requests for the active table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 20, 2015 6895     Ben.Phillippe Routing registry requests through request server
 * Aug 25, 2015 6895     Ben.Phillippe VTEC info is now retrieved from the database
 * Oct 07, 2015 6895     Ben.Phillippe  RiverPro Interoperability
 * Apr 05, 2016 16577    Ben.Phillippe Implementing storage of Hazard Event VTEC
 * 5/3/2016     18193    Ben.Phillippe Replication of Hazard VTEC Records
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class GetHazardActiveTableHandler implements
        IRequestHandler<GetHazardActiveTableRequest> {

    /** The logger */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(GetHazardActiveTableHandler.class);

    @Override
    public HazardEventResponse handleRequest(GetHazardActiveTableRequest request)
            throws HazardEventServiceException {
        statusHandler.info("Querying Hazard Event Vtec Table...");
        HazardEventVtecResponse response = HazardVtecServicesSoapClient
                .getServices(request.isPractice()).getHazardVtecTable(
                        request.getSiteID());
        response.setSuccess(true);
        return response;
    }
}
