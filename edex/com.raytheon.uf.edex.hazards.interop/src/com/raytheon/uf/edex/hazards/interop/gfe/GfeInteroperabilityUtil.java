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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_KEYS;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityRecordManager;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardsInteroperabilityGFE;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.IHazardsInteroperabilityRecord;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.query.HazardEventQueryRequest;

/**
 * Common utility methods utilized by the GFE interoperability classes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 08, 2014            bkowal       Initial creation
 * Dec 12, 2014  #2826     dgilling     Change fields used for interoperability.
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * 
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

    public static List<IHazardEvent> queryForInteroperabilityHazards(
            String siteID, String phenomenon, String significance,
            Date startDate, Date endDate, HazardEventManager hazardEventManager) {
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters
                .put(HazardInteroperabilityConstants.INTEROPERABILITY_GFE_KEYS.SITE_ID,
                        siteID);
        if (phenomenon != null) {
            parameters.put(INTEROPERABILITY_KEYS.PHENOMENON, phenomenon);
        }
        if (significance != null) {
            parameters.put(INTEROPERABILITY_KEYS.SIGNIFICANCE, significance);
        }
        parameters
                .put(HazardInteroperabilityConstants.INTEROPERABILITY_GFE_KEYS.START_DATE,
                        startDate);
        parameters
                .put(HazardInteroperabilityConstants.INTEROPERABILITY_GFE_KEYS.END_DATE,
                        endDate);

        List<IHazardsInteroperabilityRecord> records = HazardInteroperabilityRecordManager
                .queryForRecord(HazardsInteroperabilityGFE.class, parameters);
        if (records == null) {
            return null;
        }

        // Retrieve the associated hazard events.
        Map<String, HazardHistoryList> associatedEvents = new HashMap<>();
        for (IHazardsInteroperabilityRecord record : records) {
            Map<String, HazardHistoryList> events = hazardEventManager
                    .query(new HazardEventQueryRequest(
                            HazardConstants.HAZARD_EVENT_IDENTIFIER, record
                                    .getHazardEventID()));
            if (events.isEmpty() == false) {
                associatedEvents.putAll(events);
            }
        }

        return evaluateReturnedEvents(associatedEvents);
    }

    /*
     * Builds a list of events to examine while handling the HazardHistoryList.
     */
    public static List<IHazardEvent> evaluateReturnedEvents(
            Map<String, HazardHistoryList> events) {
        List<IHazardEvent> hazardEventsToIterateOver = null;

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
