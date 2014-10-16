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

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.annotations.DataURIUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_TYPE;
import com.raytheon.uf.common.dataplugin.message.PracticeDataURINotificationMessage;
import com.raytheon.uf.common.dataplugin.warning.AbstractWarningRecord;
import com.raytheon.uf.common.dataplugin.warning.PracticeWarningRecord;
import com.raytheon.uf.common.dataquery.requests.DbQueryRequest;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.responses.DbQueryResponse;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
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
 * Jun 11, 2013            mnash     Initial creation
 * Jan 15, 2014 2755       bkowal      Exception handling for failed hazard event
 *                                     id generation.
 * Apr 08, 2014 3357       bkowal    Updated to use the new interoperability tables.
 * Apr 23, 2014 3357       bkowal    Improved interoperability hazard comparison to prevent duplicate hazard creation.
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

    public void createHazardsFromBytes(byte[] bytes) {
        Map<String, RequestConstraint> vals = new HashMap<String, RequestConstraint>();
        try {
            PracticeDataURINotificationMessage value = SerializationUtil
                    .transformFromThrift(
                            PracticeDataURINotificationMessage.class, bytes);
            String[] uris = value.getDataURIs();
            if (uris.length > 0) {
                vals.putAll(RequestConstraint.toConstraintMapping(DataURIUtil
                        .createDataURIMap(uris[0])));
            } else {
                statusHandler
                        .warn("Empty Practice Data URI Notification Received!");
                return;
            }
            DbQueryRequest request = new DbQueryRequest(vals);
            DbQueryResponse response = (DbQueryResponse) RequestRouter
                    .route(request);
            PluginDataObject[] pdos = response
                    .getEntityObjects(PluginDataObject.class);
            if (pdos.length != 0) {
                createHazards(Arrays.asList(pdos));
            }
        } catch (Exception e) {
            statusHandler.error("Unable to create hazards for pdos", e);
        }
    }

    public void createHazards(List<PluginDataObject> objects) {
        if (objects.isEmpty() == false) {
            GeometryFactory gf = new GeometryFactory();
            for (PluginDataObject ob : objects) {
                if (ob instanceof AbstractWarningRecord) {
                    Mode mode = ob instanceof PracticeWarningRecord ? Mode.PRACTICE
                            : Mode.OPERATIONAL;
                    IHazardEventManager manager = new HazardEventManager(mode);
                    if (mode == Mode.OPERATIONAL) {
                        statusHandler
                                .info("Encountered an operational hazard, skipping");
                        continue;
                    }
                    AbstractWarningRecord record = null;
                    if (ob instanceof AbstractWarningRecord) {
                        record = (AbstractWarningRecord) ob;
                        // throw out no geometry, no phensig too
                        if (record.getGeometry() == null
                                || record.getPhensig() == null
                                || record.getPhensig().isEmpty()) {
                            continue;
                        }
                    } else {
                        continue;
                    }

                    // Determine if an event already exists.
                    if (this.doesEventAlreadyExist(manager, record)) {
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
                         * TODO: previously exceptions were ignored - should
                         * this record be skipped when an event id cannot be
                         * generated? should the entire process be halted?
                         */
                    }
                    event.setEventID(value);
                    event.setEndTime(record.getEndTime().getTime());
                    event.setStartTime(record.getStartTime().getTime());
                    event.setCreationTime(record.getIssueTime().getTime());
                    event.setGeometry(gf
                            .createGeometryCollection(new Geometry[] { record
                                    .getGeometry() }));
                    event.setPhenomenon(record.getPhen());
                    event.setSignificance(record.getSig());
                    event.setSiteID(record.getXxxid());
                    event.addHazardAttribute(HazardConstants.ISSUE_TIME, record
                            .getIssueTime().getTime().getTime());
                    event.addHazardAttribute(HazardConstants.EXPIRATION_TIME,
                            record.getPurgeTime().getTime().getTime());
                    event.addHazardAttribute(HazardConstants.ETNS,
                            "[" + record.getEtn() + "]");
                    event.setHazardMode(HazardConstants
                            .productClassFromAbbreviation(record
                                    .getProductClass()));
                    event.setStatus(HazardEventUtilities
                            .stateBasedOnAction(record.getAct()));

                    // these don't apply to everything so the may be blank, but
                    // we
                    // want to make sure we fill everything out of the warnings
                    // into
                    // the IHazardEvent object
                    Calendar floodBegin = record.getFloodBegin();
                    Calendar floodCrest = record.getFloodCrest();
                    Calendar floodEnd = record.getFloodEnd();
                    String floodSeverity = record.getFloodSeverity();
                    String floodRecordStatus = record.getFloodRecordStatus();
                    String immediateCause = record.getImmediateCause();

                    if (floodBegin != null) {
                        event.addHazardAttribute(
                                HazardConstants.FLOOD_BEGIN_TIME,
                                floodBegin.getTime());
                    }
                    if (floodCrest != null) {
                        event.addHazardAttribute(
                                HazardConstants.FLOOD_CREST_TIME,
                                floodCrest.getTime());
                    }
                    if (floodEnd != null) {
                        event.addHazardAttribute(
                                HazardConstants.FLOOD_END_TIME,
                                floodEnd.getTime());
                    }
                    if (floodSeverity != null) {
                        event.addHazardAttribute(
                                HazardConstants.FLOOD_SEVERITY, floodSeverity);
                        // this only applies to FF.W
                        String subType = this.determineSubType(record);
                        if (subType != null) {
                            event.setSubType(subType);
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

                    IHazardEvent existingEvent = InteroperabilityUtil
                            .associatedExistingHazard(
                                    manager,
                                    event.getSiteID(),
                                    event.getPhenomenon(),
                                    event.getSignificance(),
                                    event.getHazardAttributes()
                                            .get(HazardConstants.ETNS)
                                            .toString());
                    if (existingEvent != null) {
                        statusHandler.info("Match found for etn "
                                + record.getEtn() + " with Hazard Event "
                                + existingEvent.getEventID());
                        InteroperabilityUtil.newOrUpdateInteroperabilityRecord(
                                existingEvent, record.getEtn(), null);
                        continue;
                    }

                    boolean stored = manager.storeEvent(event);
                    statusHandler.info("Created Hazard " + event.getEventID());
                    if (stored) {
                        InteroperabilityUtil.newOrUpdateInteroperabilityRecord(
                                event, record.getEtn(),
                                INTEROPERABILITY_TYPE.WARNGEN);
                    } else {
                        throw new RuntimeException(
                                "Unable to store converted events to the database  with type "
                                        + mode.name().toLowerCase());
                    }
                }
            }
        }
    }

    private boolean doesEventAlreadyExist(IHazardEventManager manager,
            AbstractWarningRecord record) {
        final String siteID = record.getXxxid();
        final String etn = record.getEtn();
        String phen = record.getPhen();
        String sig = record.getSig();
        String subType = this.determineSubType(record);
        final String hazardType = HazardEventUtilities.getHazardType(phen, sig,
                subType);
        IHazardEvent hazardEvent = InteroperabilityUtil
                .queryInteroperabilityByETNForHazard(manager, siteID,
                        hazardType, etn, INTEROPERABILITY_TYPE.WARNGEN);

        return (hazardEvent != null);
    }

    private String determineSubType(AbstractWarningRecord record) {
        if (record.getFloodSeverity() == null) {
            return null;
        }

        final String phen = record.getPhen();
        final String sig = record.getSig();
        if (PHEN_FF.equals(phen) == false) {
            return null;
        }

        if (SIG_W.equals(sig) == false) {
            return null;
        }

        return CONVECTIVE_FL_SEVERITY.equals(record.getFloodSeverity()) ? SUBTYPE_CONVECTIVE
                : SUBTYPE_NONCONVECTIVE;
    }
}