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
package com.raytheon.uf.edex.hazards.purge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.event.vtec.HazardEventVtec;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventResponse;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IHazardEventServices;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.HazardEventServicesSoapClient;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.HazardVtecServicesSoapClient;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventQueryRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventVtecResponse;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.database.DataAccessLayerException;

/**
 * 
 * Purger implementation for purging hazard events from the registry
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 15, 2016 9307      Ben.Phillippe Initial implementation
 * Jul 27, 2016 20475     Roger.Ferrel  Added checks and prevent ISSUED status events from being purged.
 * Mar 08, 2017 28806     Roger.Ferrel  Fix purge delete to work with registry.
 * Mar 30, 2017 30085     Roger.Ferrel  Purge orphan VTEC entries older then cutoff time.
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class HazardEventPurger {

    /** The logger */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardEventPurger.class);

    /**
     * Get desired retention value and log messages.
     * 
     * @param propKey
     * @param mode
     * @return
     */
    private static final int getRetentionValue(String propKey, String mode) {
        int value = 14;
        String valStr = System.getProperty(propKey, "14");
        try {
            int val = Integer.parseInt(valStr);
            if (val > 0) {
                value = val;
            } else {
                statusHandler.warn(String.format(
                        "Bad %s retention time %d reverting to %d.", mode, val,
                        value));
            }
        } catch (NumberFormatException ex) {
            statusHandler.warn(String.format(
                    "The property %s is a bad integer value \"%s\" reverting to %d.",
                    propKey, valStr, value));
        } finally {
            statusHandler.info(String.format(
                    "Using %s retention time of %d days.", mode, value));
        }
        return value;
    }

    /** Number of days to retain practice Hazard Events */
    private static final int PRACTICE_RETENTION_DAYS = getRetentionValue(
            "hazard.event.practice.retention.time", "practice");

    /** Number of days to retain operational Hazard Events */
    private static final int OPERATIONAL_RETENTION_DAYS = getRetentionValue(
            "hazard.event.operational.retention.time", "operational");

    /** The Hazard Event statuses that are eligible to be purged */
    private static final Set<String> ELIGIBLE_STATUSES_TO_PURGE;

    static {
        String property = System
                .getProperty("hazard.event.purge.eligible.statuses");
        List<String> list = (property == null) ? new ArrayList<String>(0)
                : Arrays.asList(property.toUpperCase().split("\\s+"));
        ELIGIBLE_STATUSES_TO_PURGE = new HashSet<>(list);
        if (ELIGIBLE_STATUSES_TO_PURGE.remove("ISSUED")) {
            statusHandler
                    .warn("hazard.event.purge.eligible.statuses should not include ISSUED. "
                            + "Ignoring purging of ISSUED products.");
        }
        if (ELIGIBLE_STATUSES_TO_PURGE.isEmpty()) {
            statusHandler.warn(
                    "No purging will be done. The property hazard.event.purge.eligible.statuses is missing or is an invalid list.");
        }
        statusHandler.info(
                "Eligible purge status list: " + ELIGIBLE_STATUSES_TO_PURGE);
    }

    /**
     * Event's statuses with no expiration time.
     */
    private static final List<String> NO_EXP_DATE_STATUSES;

    static {
        NO_EXP_DATE_STATUSES = new ArrayList<>(2);
        NO_EXP_DATE_STATUSES.add("PENDING");
        NO_EXP_DATE_STATUSES.add("PROPOSED");
    }

    /**
     * Purges hazard events older than the cut off date with a current status
     * defined by the hazard.event.purge.cron pattern in
     * HazardServices.properties
     * 
     * @throws HazardEventServiceException
     *             If errors occur when issuing the delete requests to the
     *             registry
     * @throws DataAccessLayerException
     */
    public void purgeHazardEvents()
            throws HazardEventServiceException, DataAccessLayerException {
        if (!ELIGIBLE_STATUSES_TO_PURGE.isEmpty()) {
            purgeEvents(true);
            purgeEvents(false);
        }
    }

    /**
     * Purge events for a given mode
     * 
     * @param practice
     *            True for practice mode, false for operational mode
     * @throws HazardEventServiceException
     *             If errors occur while retrieving/deleting from the registry
     * @throws DataAccessLayerException
     *             If errors occur while deleting hazard services VTEC records
     */
    private void purgeEvents(boolean practice)
            throws HazardEventServiceException, DataAccessLayerException {
        /*
         * Determine the purge cutoff time based on the mode and retention time.
         * 
         * This sets the cutoff hour to midnight. Thus the first time the purger
         * is run for a given day is the only time events will be purged. If the
         * overhead is too great may want to consider skipping the event purge
         * if it has already been done for the day.
         */
        Calendar calendar = TimeUtil.newCalendar();
        TimeUtil.minCalendarFields(calendar, Calendar.HOUR_OF_DAY,
                Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND);
        calendar.add(Calendar.DAY_OF_YEAR, (practice ? PRACTICE_RETENTION_DAYS
                : OPERATIONAL_RETENTION_DAYS) * -1);
        Date cutoffDate = calendar.getTime();

        IHazardEventServices services = HazardEventServicesSoapClient
                .getServices(practice);

        HazardEventQueryRequest queryRequest = new HazardEventQueryRequest(
                practice);
        String siteId = System.getenv("AW_SITE_IDENTIFIER");
        Set<String> siteIds = new HashSet<>();
        siteIds.add(siteId);
        queryRequest.and(HazardConstants.SITE_ID, siteIds);

        Map<String, HazardHistoryList> eventHistoryMap = null;
        try {
            HazardEventResponse response = services.retrieve(queryRequest);
            eventHistoryMap = response.getHistoryMap();
        } catch (HazardEventServiceException e) {
            statusHandler.handle(Priority.ERROR,
                    "Error executing query for events.", e);
        }

        /*
         * Determine which History Lists to delete
         */
        List<HazardEvent> eventsToDelete = new ArrayList<>();
        List<String> noExpIds = new ArrayList<>();
        Set<String> eventIds = new HashSet<>(eventHistoryMap.values().size());
        for (HazardHistoryList historyList : eventHistoryMap.values()) {
            IHazardEvent event = historyList.getLatestEvent();
            String status = event.getStatus().getValue().toUpperCase();
            eventIds.add(event.getEventID());

            if (ELIGIBLE_STATUSES_TO_PURGE.contains(status)) {

                Date purgeDate = event.getExpirationTime();
                if (purgeDate == null) {
                    purgeDate = event.getEndTime();

                    if (!NO_EXP_DATE_STATUSES.contains(status)) {
                        // This should never happen.
                        noExpIds.add(event.getEventID());
                    }
                }

                if (purgeDate.before(cutoffDate)) {
                    // Don't purge currently issued events
                    for (IHazardEvent ev : historyList.getEvents()) {
                        eventsToDelete.add(((HazardEvent) ev));
                    }
                }
            }
        }

        if (!noExpIds.isEmpty()) {
            Collections.sort(noExpIds);
            statusHandler.warn(String.format(
                    "The following event%s missing expiration time: %s. Using end time.",
                    (noExpIds.size() == 1 ? " is" : "s are"), noExpIds));
        }
        noExpIds = null;

        int eventsDeleted = 0;
        Set<String> eventIDs = new HashSet<>();
        if (!eventsToDelete.isEmpty()) {
            HazardEventResponse response = services
                    .deleteEventList(eventsToDelete);
            if (response.success()) {
                eventsDeleted = eventsToDelete.size();

                for (HazardEvent e : eventsToDelete) {
                    eventIDs.add(e.getEventID());
                }

                if (statusHandler.isPriorityEnabled(Priority.DEBUG)) {
                    statusHandler.debug("Delete " + practiceToString(practice)
                            + " VTEC entries with eventIDs: " + eventIDs);
                }
                HazardEventVtecResponse resp = HazardVtecServicesSoapClient
                        .getServices(practice)
                        .deleteVtecByQuery(new HazardEventQueryRequest(practice,
                                HazardConstants.EVENT_ID, eventIDs));

                if (!resp.isSuccess()) {
                    statusHandler.warn("Purged " + eventsDeleted + " expired "
                            + practiceToString(practice) + " Hazard Event"
                            + (eventsDeleted <= 1 ? "" : "s")
                            + " with expiration time before " + cutoffDate
                            + ". Purge of the associated VTEC entries failed.");
                    return;
                }
            }
        }

        if (eventsDeleted == 0) {
            statusHandler.info(
                    "No purge, did not find any " + practiceToString(practice)
                            + " Hazard Events with expiration time before "
                            + cutoffDate);
        } else {
            statusHandler.info("Purged " + eventsDeleted + " expired "
                    + practiceToString(practice) + " Hazard Event"
                    + (eventsDeleted == 1 ? "" : "s")
                    + " with expiration time before " + cutoffDate
                    + ". Purged associated VTEC entries.");
        }

        purgeOrphanVtec(practice, eventIds, cutoffDate);
    }

    private String practiceToString(boolean practice) {
        return practice ? "PRACTICE" : "OPERATIONAL";
    }

    /**
     * Purge VTECs with issue time older than cutoff date and not on the list of
     * event ids.
     * 
     * @param practice
     * @param eventIds
     * @param cutoffDate
     */
    private void purgeOrphanVtec(boolean practice, Set<String> eventIds,
            Date cutoffDate) {

        HazardEventQueryRequest request = new HazardEventQueryRequest(practice);
        request.and(HazardConstants.ISSUE_TIME, "<", cutoffDate.getTime());
        HazardEventVtecResponse resp = HazardVtecServicesSoapClient
                .getServices(practice).retrieveVtec(request);

        List<HazardEventVtec> heVtecList = resp.isSuccess()
                ? resp.getVtecRecords() : null;
        if (heVtecList != null && !heVtecList.isEmpty()) {

            Iterator<HazardEventVtec> iter = heVtecList.iterator();
            while (iter.hasNext()) {
                HazardEventVtec heVtec = iter.next();
                if (eventIds.contains(heVtec.getEventID())
                        || !heVtec.getIssueTime().before(cutoffDate)) {
                    iter.remove();
                }
            }

            if (!heVtecList.isEmpty()) {
                HazardEventVtecResponse delResp = HazardVtecServicesSoapClient
                        .getServices(practice).deleteVtecList(heVtecList);
                if (delResp.isSuccess()) {
                    statusHandler.info(String.format(
                            "Deleted %d %s orphan VTEC entries.",
                            heVtecList.size(), practiceToString(practice)));
                } else {
                    List<String> ids = new ArrayList<>(heVtecList.size());
                    for (HazardEventVtec heVtec : heVtecList) {
                        ids.add(heVtec.getEventID());
                    }
                    Collections.sort(ids);
                    statusHandler.warn("Problems attempting to delete orphan "
                            + practiceToString(practice)
                            + " VTEC entries with ids: " + ids);
                }
            }
        }
    }
}
