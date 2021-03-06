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
package com.raytheon.uf.edex.hazards.interop.gfe;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.HazardEventServicesSoapClient;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventQueryRequest;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.HazardInteroperabilityRecord;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.services.client.HazardEventInteropServicesSoapClient;

/**
 * Common utility methods utilized by the GFE interoperability classes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Apr 08, 2014            bkowal        Initial creation
 * Dec 12, 2014   2826     dgilling      Change fields used for interoperability.
 * May 29, 2015   6895     Ben.Phillippe Refactored Hazard Service data access
 * Aug 20, 2015   6895     Ben.Phillippe Routing registry requests through
 *                                       request server
 * Feb 16, 2017  29138     Chris.Golden  Changed to work with new hazard
 *                                       event manager.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public final class GfeInteroperabilityUtil {

    /**
     * 
     */
    protected GfeInteroperabilityUtil() {
    }

    public static List<HazardEvent> queryForInteroperabilityHazards(
            boolean practice, String siteID, String phenomenon,
            String significance, Date startDate, Date endDate)
            throws HazardEventServiceException {
        HazardEventQueryRequest queryRequest = new HazardEventQueryRequest(
                practice, HazardConstants.HAZARD_EVENT_START_TIME, ">",
                startDate).and(HazardConstants.HAZARD_EVENT_END_TIME, "<",
                endDate).and(HazardConstants.SITE_ID, siteID);
        if (phenomenon != null) {
            queryRequest.and(HazardConstants.PHENOMENON, phenomenon);
        }
        if (significance != null) {
            queryRequest.and(HazardConstants.SIGNIFICANCE, significance);
        }

        List<HazardInteroperabilityRecord> records = HazardEventInteropServicesSoapClient
                .getServices(practice).retrieve(queryRequest)
                .getInteropRecords();
        if (records == null) {
            return null;
        }

        // Retrieve the associated hazard events.
        Map<String, HazardHistoryList> associatedEvents = new HashMap<>();
        for (HazardInteroperabilityRecord record : records) {
            Map<String, HazardHistoryList> events = HazardEventServicesSoapClient
                    .getServices(practice)
                    .retrieveByParams(HazardConstants.HAZARD_EVENT_IDENTIFIER,
                            "=", record.getHazardEventID()).getHistoryMap();
            if (events.isEmpty() == false) {
                associatedEvents.putAll(events);
            }
        }

        return evaluateReturnedEvents(associatedEvents);
    }

    /*
     * Builds a list of events to examine while handling the HazardHistoryList.
     */
    public static List<HazardEvent> evaluateReturnedEvents(
            Map<String, HazardHistoryList> events) {
        List<HazardEvent> hazardEventsToIterateOver = null;

        /*
         * determine how many hazards need to be reviewed.
         */
        int hazardsListCount = events.entrySet().size();
        if (hazardsListCount == 1) {
            hazardEventsToIterateOver = events.entrySet().iterator().next()
                    .getValue().getEvents();
        } else {
            hazardEventsToIterateOver = new LinkedList<>();
            for (String hazardEventID : events.keySet()) {
                hazardEventsToIterateOver.addAll(events.get(hazardEventID)
                        .getEvents());
            }
        }

        return hazardEventsToIterateOver;
    }
}
