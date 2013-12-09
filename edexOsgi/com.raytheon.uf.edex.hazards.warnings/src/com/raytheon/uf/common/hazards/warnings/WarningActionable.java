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
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.raytheon.uf.common.actionregistry.IActionable;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardQueryBuilder;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.warning.AbstractWarningRecord;

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
 * May 22, 2013            mnash       Initial creation
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 14, 2013 1472       bkowal      Renamed hazard subtype to subType
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

                String value = HazardEventUtilities.determineEtn(
                        record.getXxxid(), record.getAct(), record.getEtn(),
                        manager);
                event.setEventID(value);
                event.setEndTime(record.getEndTime().getTime());
                event.setStartTime(record.getStartTime().getTime());
                event.setIssueTime(record.getIssueTime().getTime());
                event.setGeometry(record.getGeometry());
                event.setPhenomenon(record.getPhen());
                event.setSignificance(record.getSig());
                event.setSiteID(record.getXxxid());
                event.addHazardAttribute(HazardConstants.EXPIRATIONTIME, record
                        .getPurgeTime().getTime().getTime());
                event.addHazardAttribute("etns", "[" + record.getEtn() + "]");
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

                if (HazardEventUtilities.isDuplicate(manager, event)) {
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
}