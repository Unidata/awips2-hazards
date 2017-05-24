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

package com.raytheon.uf.edex.hazards.interop.handlers;

import com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.HazardInteroperabilityResponse;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.services.client.HazardEventInteropServicesSoapClient;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.requests.UpdateInteroperabilityRecordRequest;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;

/**
 * 
 * Handler class used to process update interoperability record requests
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
public class UpdateInteroperabilityRecordHandler implements
        IRequestHandler<UpdateInteroperabilityRecordRequest> {

    @Override
    public HazardInteroperabilityResponse handleRequest(
            UpdateInteroperabilityRecordRequest request) throws Exception {
        HazardInteroperabilityResponse response = new HazardInteroperabilityResponse();
        HazardEventInteropServicesSoapClient.getServices(request.isPractice())
                .updateEventList(request.getEvents());
        return response;
    }

}
