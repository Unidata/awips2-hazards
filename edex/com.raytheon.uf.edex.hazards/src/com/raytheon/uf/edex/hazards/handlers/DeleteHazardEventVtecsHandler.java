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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.vtec.HazardEventVtec;
import com.raytheon.uf.common.dataplugin.events.hazards.event.vtec.OperationalHazardEventVtec;
import com.raytheon.uf.common.dataplugin.events.hazards.event.vtec.PracticeHazardEventVtec;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.HazardVtecServicesSoapClient;
import com.raytheon.uf.common.dataplugin.events.hazards.request.DeleteHazardEventVtecsRequest;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;

/**
 * 
 * Handler class used to process delete hazard vtecs requests
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2016 20037      Robert.Blum Initial Creation
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */
public class DeleteHazardEventVtecsHandler
        implements IRequestHandler<DeleteHazardEventVtecsRequest> {

    @SuppressWarnings("unchecked")
    @Override
    public HazardEventResponse handleRequest(
            DeleteHazardEventVtecsRequest request)
                    throws HazardEventServiceException {
        boolean practice = request.isPractice();
        List<Object> vtecList = request.getVtecList();
        List<HazardEventVtec> vtecRecords = new ArrayList<>(vtecList.size());
        for (Object vtecEntry : vtecList) {
            Map<String, Object> vtecMap = (HashMap<String, Object>) vtecEntry;
            Object ids = vtecMap.get(HazardConstants.UGC_ID);
            if (ids instanceof HashSet) {
                Set<String> ugcZones = (HashSet<String>) vtecMap
                        .get(HazardConstants.UGC_ID);
                for (String ugcZone : ugcZones) {
                    vtecRecords.add(newVtecRecord(practice, ugcZone, vtecMap));
                }
            } else {
                vtecRecords.add(newVtecRecord(practice, (String) ids, vtecMap));
            }
        }

        return HazardVtecServicesSoapClient.getServices(request.isPractice())
                .deleteVtecList(vtecRecords);
    }

    private HazardEventVtec newVtecRecord(boolean practice, String ugcZone,
            Map<String, Object> attributes) {
        return practice ? new PracticeHazardEventVtec(ugcZone, attributes)
                : new OperationalHazardEventVtec(ugcZone, attributes);
    }
}
