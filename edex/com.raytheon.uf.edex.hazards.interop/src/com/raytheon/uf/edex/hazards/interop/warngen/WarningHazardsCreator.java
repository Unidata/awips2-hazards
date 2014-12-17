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
package com.raytheon.uf.edex.hazards.interop.warngen;

import java.util.Calendar;
import java.util.List;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_TYPE;
import com.raytheon.uf.common.dataplugin.warning.AbstractWarningRecord;
import com.raytheon.uf.common.dataplugin.warning.PracticeWarningRecord;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.hazards.interoperability.util.InteroperabilityUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

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
 * Jun 11, 2013            mnash        Initial creation
 * Jan 15, 2014 2755       bkowal       Exception handling for failed hazard event
 *                                      id generation.
 * Apr 08, 2014 3357       bkowal       Updated to use the new interoperability tables.
 * Apr 23, 2014 3357       bkowal       Improved interoperability hazard comparison to prevent duplicate hazard creation.
 * Nov 17, 2014 2826       mpduff       Changed back to pass Warngen for interoperability type of new hazards.
 * Dec 04, 2014 2826       dgilling     Revert previous change, remove unneeded methods.
 * Dec 15, 2014 2826       dgilling     Code cleanup, re-factor.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class WarningHazardsCreator {

    private static final String PHEN_FF = "FF";

    private static final String SIG_W = "W";

    private static final String CONVECTIVE_FL_SEVERITY = "0";

    private static final String SUBTYPE_CONVECTIVE = "Convective";

    private static final String SUBTYPE_NONCONVECTIVE = "NonConvective";

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(WarningHazardsCreator.class);

    public void createHazards(List<PluginDataObject> objects) {
        for (PluginDataObject ob : objects) {
            if (ob instanceof AbstractWarningRecord) {
                AbstractWarningRecord record = (AbstractWarningRecord) ob;

                // TODO skip record if not part of active sites

                if (record.getGeometry() == null || record.getPhensig() == null
                        || record.getPhensig().isEmpty()) {
                    statusHandler
                            .warn("Skipping product "
                                    + record.getPil()
                                    + " from site "
                                    + record.getOfficeid()
                                    + " because it has invalid geometry or invalid phensig.");
                    continue;
                }

                Mode mode = ob instanceof PracticeWarningRecord ? Mode.PRACTICE
                        : Mode.OPERATIONAL;
                IHazardEventManager manager = new HazardEventManager(mode);
                if (mode == Mode.OPERATIONAL) {
                    statusHandler
                            .info("Encountered an operational hazard, skipping");
                    continue;
                }

                // Determine if an event already exists.
                if (doesEventAlreadyExist(manager, record)) {
                    statusHandler
                            .info("Skipping record for "
                                    + record.getPhensig()
                                    + " from office "
                                    + record.getOfficeid()
                                    + " because an entry already exists in the interoperability table.");
                    continue;
                }

                IHazardEvent eventToStore = null;
                IHazardEvent existingEvent = InteroperabilityUtil
                        .associatedExistingHazard(manager, record.getXxxid(),
                                record.getPhen(), record.getSig(),
                                "[" + record.getEtn() + "]");
                if (existingEvent != null) {
                    statusHandler.info("Match found for etn " + record.getEtn()
                            + " with Hazard Event "
                            + existingEvent.getEventID());
                    eventToStore = existingEvent;
                } else {
                    IHazardEvent newEvent = null;
                    try {
                        newEvent = buildHazardEventFromWarningRecord(record,
                                manager);
                    } catch (Exception e) {
                        statusHandler
                                .error("Unable to build hazard event from warning record.",
                                        e);
                    }

                    if (newEvent != null) {
                        boolean stored = manager.storeEvent(newEvent);
                        if (stored) {
                            statusHandler.info("Created Hazard "
                                    + newEvent.getEventID());
                            eventToStore = newEvent;
                        } else {
                            statusHandler
                                    .error("Unable to store converted events to the database  with type "
                                            + mode.name().toLowerCase());
                        }
                    }
                }

                if (eventToStore != null) {
                    InteroperabilityUtil.newOrUpdateInteroperabilityRecord(
                            eventToStore, record.getEtn(),
                            INTEROPERABILITY_TYPE.WARNGEN);
                }
            }
        }
    }

    private static IHazardEvent buildHazardEventFromWarningRecord(
            final AbstractWarningRecord warningRecord,
            IHazardEventManager manager) throws Exception {

        IHazardEvent event = manager.createEvent();

        String value = HazardEventUtilities.determineEtn(
                warningRecord.getXxxid(), warningRecord.getAct(),
                warningRecord.getEtn(), manager);

        event.setEventID(value);
        event.setEndTime(warningRecord.getEndTime().getTime());
        event.setStartTime(warningRecord.getStartTime().getTime());
        event.setCreationTime(warningRecord.getIssueTime().getTime());
        event.setGeometry(new GeometryFactory()
                .createGeometryCollection(new Geometry[] { warningRecord
                        .getGeometry() }));
        event.setPhenomenon(warningRecord.getPhen());
        event.setSignificance(warningRecord.getSig());
        event.setSiteID(warningRecord.getXxxid());
        event.addHazardAttribute(HazardConstants.ISSUE_TIME, warningRecord
                .getIssueTime().getTime().getTime());
        event.addHazardAttribute(HazardConstants.EXPIRATION_TIME, warningRecord
                .getPurgeTime().getTime().getTime());
        event.addHazardAttribute(HazardConstants.ETNS,
                "[" + warningRecord.getEtn() + "]");
        event.setHazardMode(HazardConstants
                .productClassFromAbbreviation(warningRecord.getProductClass()));
        event.setStatus(HazardEventUtilities.stateBasedOnAction(warningRecord
                .getAct()));

        /*
         * these don't apply to everything so the may be blank, but we want to
         * make sure we fill everything out of the warnings into the
         * IHazardEvent object
         */
        Calendar floodBegin = warningRecord.getFloodBegin();
        Calendar floodCrest = warningRecord.getFloodCrest();
        Calendar floodEnd = warningRecord.getFloodEnd();
        String floodSeverity = warningRecord.getFloodSeverity();
        String floodRecordStatus = warningRecord.getFloodRecordStatus();
        String immediateCause = warningRecord.getImmediateCause();

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
            String subType = determineSubType(warningRecord);
            if (subType != null) {
                event.setSubType(subType);
            }
        }
        if (floodRecordStatus != null) {
            event.addHazardAttribute(HazardConstants.FLOOD_RECORD_STATUS,
                    floodRecordStatus);
        }
        if (immediateCause != null) {
            event.addHazardAttribute(HazardConstants.FLOOD_IMMEDIATE_CAUSE,
                    immediateCause);
        }

        return event;
    }

    private boolean doesEventAlreadyExist(IHazardEventManager manager,
            AbstractWarningRecord record) {
        final String siteID = record.getXxxid();
        final String etn = record.getEtn();
        String phen = record.getPhen();
        String sig = record.getSig();
        IHazardEvent hazardEvent = InteroperabilityUtil
                .queryInteroperabilityByETNForHazard(manager, siteID, phen,
                        sig, etn, null);

        return (hazardEvent != null);
    }

    private static String determineSubType(AbstractWarningRecord record) {
        if ((record.getFloodSeverity() == null)
                || (!PHEN_FF.equals(record.getPhen()))
                || (!SIG_W.equals(record.getSig()))) {
            return null;
        }

        return CONVECTIVE_FL_SEVERITY.equals(record.getFloodSeverity()) ? SUBTYPE_CONVECTIVE
                : SUBTYPE_NONCONVECTIVE;
    }
}