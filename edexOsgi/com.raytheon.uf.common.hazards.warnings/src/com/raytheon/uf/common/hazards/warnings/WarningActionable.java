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
package com.raytheon.uf.common.hazards.warnings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.raytheon.uf.common.actionregistry.IActionable;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardQueryBuilder;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.requests.HazardEventIdRequest;
import com.raytheon.uf.common.dataplugin.warning.AbstractWarningRecord;
import com.raytheon.uf.common.serialization.comm.RequestRouter;

/**
 * {@link IActionable} to convert {@link AbstractWarningRecord} objects to
 * {@link IHazardEvent} objects to support interoperability of Warngen -> Hazard
 * Services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 22, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class WarningActionable implements IActionable {

    /**
     * 
     */
    public WarningActionable() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.actionregistry.IActionable#handleAction()
     */
    @Override
    public void handleAction(Object... objects) {
        if (objects.length > 0) {
            // TODO, change this once we turn on the registry as well
            // Mode mode = objects[0] instanceof PracticeWarningRecord ?
            // Mode.PRACTICE
            // : Mode.OPERATIONAL;
            Mode mode = Mode.PRACTICE;
            HazardEventManager manager = new HazardEventManager(mode);
            List<IHazardEvent> events = new ArrayList<IHazardEvent>();
            for (Object ob : objects) {
                AbstractWarningRecord record = null;
                if (ob instanceof AbstractWarningRecord) {
                    record = (AbstractWarningRecord) ob;
                } else {
                    continue;
                }
                IHazardEvent event = manager.createEvent();

                // make a request for the hazard event id from the cluster task
                // table
                HazardEventIdRequest request = new HazardEventIdRequest();
                request.setSiteId(record.getXxxid());
                String value = "";
                try {
                    value = RequestRouter.route(request).toString();
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Unable to make request for hazard event id", e);
                }
                event.setEventID(value);
                event.setEndTime(record.getEndTime().getTime());
                event.setStartTime(record.getStartTime().getTime());
                event.setIssueTime(record.getIssueTime().getTime());
                event.setGeometry(record.getGeometry());
                event.setPhenomenon(record.getPhen());
                event.setSignificance(record.getSig());
                event.setSiteID(record.getXxxid());
                event.setHazardMode(HazardConstants
                        .productClassFromAbbreviation(record.getProductClass()));
                event.setState(stateBasedOnAction(record.getAct()));

                // these don't apply to everything so the may be blank, but we
                // want to make sure we fill everything out of the warnings into
                // the IHazardEvent object
                Calendar floodBegin = record.getFloodBegin();
                Calendar floodCrest = record.getFloodCrest();
                Calendar floodEnd = record.getFloodEnd();
                String floodSeverity = record.getFloodSeverity();
                String floodRecordStatus = record.getFloodRecordStatus();
                String immediateCause = record.getImmediateCause();

                if (floodBegin != null) {
                    event.addHazardAttribute(HazardConstants.FLOOD_BEGIN_TIME,
                            floodBegin.getTime());
                }
                if (floodCrest != null) {
                    event.addHazardAttribute(HazardConstants.FLOOD_CREST_TIME,
                            floodCrest.getTime());
                }
                if (floodEnd != null) {
                    event.addHazardAttribute(HazardConstants.FLOOD_END_TIME,
                            floodEnd.getTime());
                }
                if (floodSeverity != null) {
                    event.addHazardAttribute(HazardConstants.FLOOD_SEVERITY,
                            floodSeverity);
                    // this only applies to FF.W
                    if (event.getPhenomenon().equals("FF")
                            && event.getSignificance().equals("W")) {
                        if (floodSeverity.equals("0")) {
                            event.setSubtype("Convective");
                        } else {
                            event.setSubtype("NonConvective");
                        }
                    }
                }
                if (floodRecordStatus != null) {
                    event.addHazardAttribute(
                            HazardConstants.FLOOD_RECORD_STATUS,
                            floodRecordStatus);
                }
                if (immediateCause != null) {
                    event.addHazardAttribute(
                            HazardConstants.FLOOD_IMMEDIATE_CAUSE,
                            immediateCause);
                }

                HazardQueryBuilder builder = new HazardQueryBuilder();
                builder.addKey(HazardConstants.SITEID, event.getSiteID());
                builder.addKey(HazardConstants.GEOMETRY, event.getGeometry());
                builder.addKey(HazardConstants.PHENOMENON,
                        event.getPhenomenon());
                builder.addKey(HazardConstants.SIGNIFICANCE,
                        event.getSignificance());
                builder.addKey(HazardConstants.STATE, event.getState());
                if (event.getSubtype() != null
                        && event.getSubtype().isEmpty() == false) {
                    builder.addKey(HazardConstants.SUBTYPE, event.getSubtype());
                }
                Map<String, HazardHistoryList> hazards = manager
                        .getEventsByFilter(builder.getQuery());
                boolean toStore = true;
                for (HazardHistoryList list : hazards.values()) {
                    Iterator<IHazardEvent> iter = list.iterator();
                    while (iter.hasNext()) {
                        IHazardEvent ev = iter.next();
                        toStore = compareEvents(ev, event);
                        if (toStore == false) {
                            break;
                        }
                    }
                    if (toStore == false) {
                        break;
                    }
                }

                // TODO, maybe need to fill in more?
                if (toStore) {
                    events.add(event);
                }
            }

            boolean stored = manager.storeEvents(events);
            if (stored == false) {
                throw new RuntimeException(
                        "Unable to store converted events to the database");
            }
        }
    }

    private boolean compareEvents(IHazardEvent event1, IHazardEvent event2) {
        long issueTimeEvent1 = TimeUnit.MILLISECONDS.toMinutes(event1
                .getIssueTime().getTime());
        long issueTimeEvent2 = TimeUnit.MILLISECONDS.toMinutes(event2
                .getIssueTime().getTime());
        long startTimeEvent1 = TimeUnit.MILLISECONDS.toMinutes(event1
                .getStartTime().getTime());
        long startTimeEvent2 = TimeUnit.MILLISECONDS.toMinutes(event2
                .getStartTime().getTime());
        long endTimeEvent1 = TimeUnit.MILLISECONDS.toMinutes(event1
                .getEndTime().getTime());
        long endTimeEvent2 = TimeUnit.MILLISECONDS.toMinutes(event2
                .getEndTime().getTime());

        if (event1.getSiteID().equals(event2.getSiteID()) == false) {
            return true;
        }
        if (issueTimeEvent1 != issueTimeEvent2) {
            return true;
        }
        if (startTimeEvent1 != startTimeEvent2) {
            return true;
        }
        if (endTimeEvent1 != endTimeEvent2) {
            return true;
        }
        if (event1.getPhenomenon().equals(event2.getPhenomenon()) == false) {
            return true;
        }
        if (event1.getSignificance().equals(event2.getSignificance()) == false) {
            return true;
        }
        if (event1.getState() != event2.getState()) {
            return true;
        }
        return false;
    }

    /**
     * Any state for hazard events will be "ISSUED" unless it gets the following
     * VTEC states
     * 
     * @param action
     * @return
     */
    private HazardState stateBasedOnAction(String action) {
        if ("CAN".equals(action) || "EXP".equals(action)) {
            return HazardState.ENDED;
        } else {
            return HazardState.ISSUED;
        }
    }
}