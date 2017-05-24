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
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.HazardEventServicesSoapClient;
import com.raytheon.uf.common.dataplugin.events.hazards.request.DeleteAllHazardEventsWithIdRequest;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;

/**
 * 
 * Handler class used to process requests to delete all hazard events with a
 * particular identifier.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 20, 2015 6895     Ben.Phillippe Routing registry requests through request server
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class DeleteAllHazardEventsWithIdHandler implements
        IRequestHandler<DeleteAllHazardEventsWithIdRequest> {

    @Override
    public HazardEventResponse handleRequest(
            DeleteAllHazardEventsWithIdRequest request) throws Exception {
        return HazardEventServicesSoapClient.getServices(request.isPractice())
                .deleteAllWithIdentifier(request.getIdentifier());
    }
}
