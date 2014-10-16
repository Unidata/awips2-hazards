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
package com.raytheon.uf.edex.hazards.interoperability.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardQueryBuilder;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_KEYS;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_TYPE;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityRecordManager;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardsInteroperability;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.IHazardsInteroperabilityRecord;

/**
 * A utility for common interoperability functions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 8, 2014            bkowal     Initial creation
 * Apr 22, 2014 3357      bkowal     Implemented ETN comparison to compare hazard events.
 * 
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
            final String hazardType, final String etn,
            final INTEROPERABILITY_TYPE type) {
        List<IHazardEvent> hazardEvents = queryInteroperabilityByETNForHazards(
                manager, siteID, hazardType, etn, type);
        if (hazardEvents == null || hazardEvents.isEmpty()) {
            return null;
        }

        return hazardEvents.get(0);
    }

    public static List<IHazardEvent> queryInteroperabilityByETNForHazards(
            IHazardEventManager manager, final String siteID,
            final String hazardType, final String etn,
            final INTEROPERABILITY_TYPE type) {
        Map<String, Serializable> parameters = new HashMap<>();
        parameters.put(INTEROPERABILITY_KEYS.SITE_ID, siteID);
        if (hazardType != null) {
            parameters.put(INTEROPERABILITY_KEYS.HAZARD_TYPE, hazardType);
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

        List<IHazardEvent> retrievedHazardEvents = new ArrayList<>();
        for (IHazardsInteroperabilityRecord record : records) {
            final String hazardEventID = record.getHazardEventID();

            HazardQueryBuilder builder = new HazardQueryBuilder();
            builder.addKey(HazardConstants.HAZARD_EVENT_IDENTIFIER,
                    hazardEventID);

            Map<String, HazardHistoryList> hazardEventsMap = manager
                    .getEventsByFilter(builder.getQuery());
            if (hazardEventsMap != null && hazardEventsMap.isEmpty() == false
                    && hazardEventsMap.containsKey(hazardEventID)) {

                /*
                 * Retrieve the most recent hazard from the history list.
                 */
                HazardHistoryList historyList = hazardEventsMap
                        .get(hazardEventID);
                if (historyList != null && historyList.isEmpty() == false) {
                    IHazardEvent mostRecentEvent = historyList.get(0);
                    Long latestTime = getLatestTime(mostRecentEvent
                            .getHazardAttribute(HazardConstants.PERSIST_TIME));
                    for (int count = 1; count < historyList.size(); count++) {
                        IHazardEvent hazardEvent = historyList.get(count);
                        Long hazardTime = getLatestTime(hazardEvent
                                .getHazardAttribute(HazardConstants.PERSIST_TIME));
                        if (hazardTime > latestTime) {
                            latestTime = hazardTime;
                            mostRecentEvent = hazardEvent;
                        }
                    }
                    retrievedHazardEvents.add(mostRecentEvent);
                }
            }
        }

        if (retrievedHazardEvents.isEmpty()) {
            return null;
        }

        return retrievedHazardEvents;
    }

    private static Long getLatestTime(Serializable serializable) {
        if (serializable instanceof Date) {
            return ((Date) serializable).getTime();
        } else {
            return (Long) serializable;
        }
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
        HazardQueryBuilder builder = new HazardQueryBuilder();
        builder.addKey(HazardConstants.SITE_ID, siteID);
        builder.addKey(HazardConstants.PHENOMENON, phen);
        builder.addKey(HazardConstants.SIGNIFICANCE, sig);
        Map<String, HazardHistoryList> events = manager
                .getEventsByFilter(builder.getQuery());
        if (events == null || events.isEmpty()) {
            return null;
        }

        /* Compare the Hazard ETNs. */
        Iterator<String> eventIDIterator = events.keySet().iterator();
        while (eventIDIterator.hasNext()) {
            String eventID = eventIDIterator.next();
            int index = events.get(eventID).size() - 1;
            IHazardEvent existingEvent = events.get(eventID).get(index);

            if (existingEvent.getSiteID().equals(siteID) == false
                    || existingEvent.getPhenomenon().equals(phen) == false
                    || existingEvent.getSignificance().equals(sig) == false) {
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