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
package com.raytheon.uf.edex.hazards.interop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Include;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_KEYS;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_TYPE;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityRecordManager;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardsInteroperability;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.IHazardsInteroperabilityRecord;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.query.HazardEventQueryRequest;

/**
 * A utility for common interoperability functions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 08, 2014           bkowal        Initial creation
 * Apr 22, 2014   3357    bkowal        Implemented ETN comparison to compare hazard events.
 * Dec 18, 2014   2826    dgilling      Change fields used in interoperability.
 * Feb 22, 2015   6561    mpduff        Use insertTime to find latest events.
 * May 29, 2015   6895    Ben.Phillippe Refactored Hazard Service data access
 * Oct 14, 2015  12494    Chris Golden  Reworked to allow hazard types to include
 *                                      only phenomenon (i.e. no significance) where
 *                                      appropriate.
 * Feb 16, 2017  29138    Chris.Golden  Changed to work with new hazard event manager.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public final class InteroperabilityUtil {
    private static final int DESIRED_ETN_LENGTH = 4;

    private static final String ETN_PAD_CHARACTER = "0";

    /**
     * 
     */
    protected InteroperabilityUtil() {
    }

    public static IHazardEvent queryInteroperabilityByETNForHazard(
            IHazardEventManager manager, final String siteID,
            final String phenomnenon, final String significance,
            final String etn, final INTEROPERABILITY_TYPE type) {
        List<HazardEvent> hazardEvents = queryInteroperabilityByETNForHazards(
                manager, siteID, phenomnenon, significance, etn, type);
        if (hazardEvents == null || hazardEvents.isEmpty()) {
            return null;
        }

        return hazardEvents.get(0);
    }

    public static List<HazardEvent> queryInteroperabilityByETNForHazards(
            IHazardEventManager manager, final String siteID,
            final String phenomenon, final String significance,
            final String etn, final INTEROPERABILITY_TYPE type) {
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(INTEROPERABILITY_KEYS.SITE_ID, siteID);
        if (phenomenon != null) {
            parameters.put(INTEROPERABILITY_KEYS.PHENOMENON, phenomenon);
        }
        if (significance != null) {
            parameters.put(INTEROPERABILITY_KEYS.SIGNIFICANCE, significance);
        }
        parameters.put(INTEROPERABILITY_KEYS.ETN, etn);
        if (type != null) {
            parameters
                    .put(HazardInteroperabilityConstants.INTEROPERABILITY_TYPE,
                            type);
        }
        List<IHazardsInteroperabilityRecord> records = HazardInteroperabilityRecordManager
                .queryForRecord(HazardsInteroperability.class, parameters);
        if (records == null || records.isEmpty()) {
            return null;
        }

        List<HazardEvent> retrievedHazardEvents = new ArrayList<>();
        for (IHazardsInteroperabilityRecord record : records) {
            final String hazardEventID = record.getHazardEventID();

            HazardEventQueryRequest request = new HazardEventQueryRequest(
                    HazardConstants.HAZARD_EVENT_IDENTIFIER, hazardEventID);
            request.setInclude(Include.LATEST_OR_MOST_RECENT_HISTORICAL_EVENTS);
            Map<String, HazardEvent> hazardEventsMap = manager
                    .queryLatest(request);
            if (hazardEventsMap != null && hazardEventsMap.isEmpty() == false
                    && hazardEventsMap.containsKey(hazardEventID)) {
                retrievedHazardEvents.add(hazardEventsMap.get(hazardEventID));
            }
        }

        if (retrievedHazardEvents.isEmpty()) {
            return null;
        }

        return retrievedHazardEvents;
    }

    public static void newOrUpdateInteroperabilityRecord(
            final IHazardEvent hazardEvent, final String etn,
            final INTEROPERABILITY_TYPE type) {
        IHazardsInteroperabilityRecord record = HazardInteroperabilityRecordManager
                .constructInteroperabilityRecord(hazardEvent, etn, type);
        HazardInteroperabilityRecordManager.storeRecord(record);
    }

    public static IHazardEvent associatedExistingHazard(
            IHazardEventManager manager, final String siteID,
            final String phen, final String sig, final String newETNs) {
        Map<String, HazardHistoryList> events = manager
                .queryHistory(new HazardEventQueryRequest(
                        HazardConstants.SITE_ID, siteID).and(
                        HazardConstants.PHENOMENON, phen).and(
                        HazardConstants.SIGNIFICANCE, sig));
        if (events == null || events.isEmpty()) {
            return null;
        }

        /*
         * Compare the Hazard ETNs.
         */
        Iterator<String> eventIDIterator = events.keySet().iterator();
        while (eventIDIterator.hasNext()) {
            String eventID = eventIDIterator.next();
            int index = events.get(eventID).size() - 1;
            IHazardEvent existingEvent = events.get(eventID).get(index);

            if ((existingEvent.getSiteID().equals(siteID) == false)
                    || (existingEvent.getPhenomenon().equals(phen) == false)
                    || ((existingEvent.getSignificance() == null) && (sig != null))
                    || ((existingEvent.getSignificance() != null) && (existingEvent
                            .getSignificance().equals(sig) == false))) {
                continue;
            }

            List<String> existingEtns = HazardEventUtilities
                    .parseEtns(existingEvent.getHazardAttributes()
                            .get(HazardConstants.ETNS).toString());
            List<String> etns = HazardEventUtilities.parseEtns(newETNs);
            if (existingEtns == null || etns == null
                    || existingEtns.size() != etns.size()) {
                continue;
            }

            boolean match = true;
            for (int i = 0; i < existingEtns.size(); i++) {
                String existingETN = padEtnString(existingEtns.get(i));
                String newETN = padEtnString(etns.get(i));
                if (existingETN.equals(newETN) == false) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return existingEvent;
            }
        }

        return null;
    }

    public static String padEtnString(String etn) {
        return StringUtils.leftPad(etn, DESIRED_ETN_LENGTH, ETN_PAD_CHARACTER);
    }
}