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
package com.raytheon.uf.edex.hazards.warnings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.warning.AbstractWarningRecord;
import com.raytheon.uf.common.dataplugin.warning.PracticeWarningRecord;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Allows for warning compatibility with interoperability. Creates IHazardEvent
 * objects from AbstractWarningRecords.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 11, 2013            mnash     Initial creation
 * Jan 15, 2014 2755       bkowal      Exception handling for failed hazard event
 *                                     id generation.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class WarningHazardsCreator {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(WarningHazardsCreator.class);

    public void createHazards(List<PluginDataObject> objects) {
        if (objects.isEmpty() == false) {
            Mode mode = objects.get(0) instanceof PracticeWarningRecord ? Mode.PRACTICE
                    : Mode.OPERATIONAL;
            // TODO, use mode above
            HazardEventManager manager = new HazardEventManager(Mode.PRACTICE);
            List<IHazardEvent> events = new ArrayList<IHazardEvent>();
            for (PluginDataObject ob : objects) {
                AbstractWarningRecord record = null;
                if (ob instanceof AbstractWarningRecord) {
                    record = (AbstractWarningRecord) ob;
                    if (record.getGeometry() == null) {
                        continue;
                    }
                } else {
                    continue;
                }
                IHazardEvent event = manager.createEvent();

                String value = null;
                try {
                    value = HazardEventUtilities.determineEtn(
                            record.getXxxid(), record.getAct(),
                            record.getEtn(), manager);
                } catch (Exception e) {
                    statusHandler.handle(Priority.PROBLEM,
                            "Failed to generate Hazard Event ID!", e);
                    /*
                     * TODO: previously exceptions were ignored - should this
                     * record be skipped when an event id cannot be generated?
                     * should the entire process be halted?
                     */
                }
                event.setEventID(value);
                event.setEndTime(record.getEndTime().getTime());
                event.setStartTime(record.getStartTime().getTime());
                event.setIssueTime(record.getIssueTime().getTime());
                event.setGeometry(record.getGeometry());
                event.setPhenomenon(record.getPhen());
                event.setSignificance(record.getSig());
                event.setSiteID(record.getXxxid());
                event.addHazardAttribute(HazardConstants.EXPIRATION_TIME,
                        record.getPurgeTime().getTime().getTime());
                event.addHazardAttribute(HazardConstants.ETNS,
                        "[" + record.getEtn() + "]");
                event.setHazardMode(HazardConstants
                        .productClassFromAbbreviation(record.getProductClass()));
                event.setState(HazardEventUtilities.stateBasedOnAction(record
                        .getAct()));

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
                            event.setSubType("Convective");
                        } else {
                            event.setSubType("NonConvective");
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

                if (HazardEventUtilities.isDuplicate(manager, event) == false) {
                    events.add(event);
                }
            }
            if (events.isEmpty() == false) {
                boolean stored = manager.storeEvents(events);
                if (stored == false) {
                    throw new RuntimeException(
                            "Unable to store converted events to the database");
                }
            }
        }
    }
}
