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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
import com.raytheon.uf.common.site.SiteMap;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.hazards.interoperability.util.InteroperabilityUtil;
import com.raytheon.uf.edex.site.SiteAwareRegistry;

/**
 * An abstract base class for a service for managing and creating
 * interoperability records for Hazard Services. If a product comes in from the
 * legacy application, a matching {@code IHazardEvent} will be created to mirror
 * the legacy record. If the hazard product comes from hazard services, an
 * interoperability record will be created to tie the legacy record with the
 * hazard services record.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 28, 2015  #2826     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public abstract class AbstractLegacyAppInteropSrv {

    protected final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    /**
     * Spring/Camel route entry point for this service. Takes the given
     * {@code AbstractWarningRecord}s from a given ingested product, processes
     * and validates them based on both generic and app-specific criteria,
     * creates and persists a new {@code IHazardEvent} if this product was
     * issued from the legacy application and creates an interoperability record
     * to tie the legacy record and hazard services record together.
     * <p>
     * If a given subclass wants to modify the validation behavior, implement
     * the {@code performAppSpecificValidation} method.
     * <p>
     * If a given subclass wants to modify the behavior for creating
     * {@code IHazardEvent} objects, implement the
     * {@code addAppSpecificHazardAttributes} method.
     * 
     * @param objects
     *            The collection of {@code AbstractWarningRecord}s to process
     *            through hazard services interoperability.
     */
    public final void createHazards(final List<PluginDataObject> objects) {
        Collection<String> activeSites = getActiveSites();

        for (PluginDataObject ob : objects) {
            if (ob instanceof AbstractWarningRecord) {
                AbstractWarningRecord record = (AbstractWarningRecord) ob;

                if (!activeSites.contains(record.getOfficeid())) {
                    statusHandler
                            .info("Skipping product "
                                    + record.getPil()
                                    + " from site "
                                    + record.getOfficeid()
                                    + " because it was not issued from the current active sites.");
                    continue;
                }
                if (!isValidRecord(record)) {
                    statusHandler.warn("Skipping product " + record.getPil()
                            + " from site " + record.getOfficeid()
                            + " because it failed validation.");
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
                                    .error("Unable to store converted events to the database with type "
                                            + mode.name().toLowerCase());
                        }
                    }
                }

                if (eventToStore != null) {
                    InteroperabilityUtil.newOrUpdateInteroperabilityRecord(
                            eventToStore, record.getEtn(),
                            getInteroperabilityType());
                }

                /*
                 * TODO: add legacy application specific hook to allow
                 * app-specific interop operations based on the hazard event,
                 * warning record and interop record.
                 */
            }
        }
    }

    /**
     * Returns the list of valid issuing site IDs (4-char site ID) based on the
     * currently activated sites.
     * 
     * @return The list of currently activated sites in 4 character format.
     */
    private Collection<String> getActiveSites() {
        String[] sites3Char = SiteAwareRegistry.getInstance().getActiveSites();

        Collection<String> sites4Char = new HashSet<>();
        for (String siteID3 : sites3Char) {
            sites4Char.add(SiteMap.getInstance().getSite4LetterId(siteID3));
        }
        return sites4Char;
    }

    /**
     * Validates the specified record and ensures it has all the necessary data
     * to create a matching {@code IHazardEvent} for interoperability purposes.
     * Subclasses can modify the validation behavior by implementing
     * {@code performAppSpecificValidation}.
     * 
     * @param record
     *            The {@code AbstractWarningRecord} to validate.
     * @return Whether or not the record passed validation.
     */
    private boolean isValidRecord(final AbstractWarningRecord record) {
        String phensig = record.getPhensig();
        if ((phensig == null) || (phensig.isEmpty())) {
            statusHandler.warn("Skipping product because it has no phensig.");
            return false;
        }

        return performAppSpecificValidation(record);
    }

    /**
     * Validates the specified record and ensures it has all the necessary data
     * to create a matching {@code IHazardEvent} for interoperability purposes.
     * 
     * @param warning
     *            The {@code AbstractWarningRecord} to validate.
     * @return Whether or not the record passed validation.
     */
    public abstract boolean performAppSpecificValidation(
            final AbstractWarningRecord warning);

    /**
     * The {@code INTEROPERABILITY_TYPE} supported by this service.
     * 
     * @return The {@code INTEROPERABILITY_TYPE} supported by this service.
     */
    public abstract INTEROPERABILITY_TYPE getInteroperabilityType();

    /**
     * Builds a new {@code IHazardEvent} object based on the data contained
     * within the specified {@code AbstractWarningRecord}. If subclasses wish to
     * modify this behavior, the {@codeaddAppSpecificHazardAttributes} method
     * should be implemented.
     * 
     * @param warningRecord
     *            {@code AbstractWarningRecord} to build the hazard event object
     *            from.
     * @param manager
     *            {@code IHazardEventManager} instance used to instantiate the
     *            event.
     * @return The {@code IHazardEvent} that matches the information contained
     *         in the specified {@code AbstractWarningRecord}.
     * @throws Exception
     *             If the {@code IHazardEvent} could not be created for any
     *             reason.
     */
    protected final IHazardEvent buildHazardEventFromWarningRecord(
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
        event.setPhenomenon(warningRecord.getPhen());
        event.setSignificance(warningRecord.getSig());
        event.setSiteID(warningRecord.getXxxid());
        event.setHazardMode(HazardConstants
                .productClassFromAbbreviation(warningRecord.getProductClass()));
        event.setStatus(HazardEventUtilities.stateBasedOnAction(warningRecord
                .getAct()));

        Map<String, Serializable> hazardAtrributes = new HashMap<>();
        hazardAtrributes.put(HazardConstants.ISSUE_TIME, warningRecord
                .getIssueTime().getTime().getTime());
        hazardAtrributes.put(HazardConstants.EXPIRATION_TIME, warningRecord
                .getPurgeTime().getTime().getTime());
        hazardAtrributes.put(HazardConstants.ETNS,
                buildSerializableCollection(warningRecord.getEtn()));
        hazardAtrributes.put(HazardConstants.VTEC_CODES,
                buildSerializableCollection(warningRecord.getAct()));
        hazardAtrributes.put(HazardConstants.PILS,
                buildSerializableCollection(warningRecord.getPil()));
        hazardAtrributes.put(HazardConstants.UGCS,
                buildSerializableCollection(warningRecord.getUgcZones()));

        /*
         * these don't apply to everything so they may be blank, but we want to
         * make sure we fill everything out of the warnings into the
         * IHazardEvent object
         */
        Calendar floodBegin = warningRecord.getFloodBegin();
        if (floodBegin != null) {
            hazardAtrributes.put(HazardConstants.FLOOD_BEGIN_TIME,
                    floodBegin.getTime());
        }
        Calendar floodCrest = warningRecord.getFloodCrest();
        if (floodCrest != null) {
            hazardAtrributes.put(HazardConstants.FLOOD_CREST_TIME,
                    floodCrest.getTime());
        }
        Calendar floodEnd = warningRecord.getFloodEnd();
        if (floodEnd != null) {
            hazardAtrributes.put(HazardConstants.FLOOD_END_TIME,
                    floodEnd.getTime());
        }
        String floodSeverity = warningRecord.getFloodSeverity();
        if (floodSeverity != null) {
            hazardAtrributes.put(HazardConstants.FLOOD_SEVERITY, floodSeverity);
        }
        String floodRecordStatus = warningRecord.getFloodRecordStatus();
        if (floodRecordStatus != null) {
            hazardAtrributes.put(HazardConstants.FLOOD_RECORD_STATUS,
                    floodRecordStatus);
        }
        String immediateCause = warningRecord.getImmediateCause();
        if (immediateCause != null) {
            hazardAtrributes.put(HazardConstants.FLOOD_IMMEDIATE_CAUSE,
                    immediateCause);
        }

        event.addHazardAttributes(hazardAtrributes);

        event = addAppSpecificHazardAttributes(event, warningRecord, manager);

        return event;
    }

    /**
     * The method for subclasses to implement to add additional legacy
     * application specific attributes to the {@code IHazardEvent} object
     * created by {@code buildHazardEventFromWarningRecord}.
     * 
     * @param event
     *            The {@code IHazardEvent} to add the app-specific hazard
     *            attributes to.
     * @param warningRecord
     *            {@code AbstractWarningRecord} to build the hazard event object
     *            from.
     * @param manager
     *            {@code IHazardEventManager} instance used to instantiate the
     *            event.
     * @return The {@code IHazardEvent} that matches the information contained
     *         in the specified {@code AbstractWarningRecord}.
     */
    protected abstract IHazardEvent addAppSpecificHazardAttributes(
            IHazardEvent event, final AbstractWarningRecord warningRecord,
            final IHazardEventManager manager);

    /**
     * Uses the specified {@code AbstractWarningRecord} object's data to
     * determine if a matching {@code IHazardEvent} exists in the system.
     * 
     * @param manager
     *            {@code IHazardEventManager} instance.
     * @param record
     *            {@code AbstractWarningRecord} to use as the basis to find the
     *            matching {@code IHazardEvent}.
     * @return {@code true} if a matching event is found. {@code false} if no
     *         matching event is found.
     */
    public final boolean doesEventAlreadyExist(IHazardEventManager manager,
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

    /**
     * A type system hack to ensure that objects that need to be placed in the
     * hazard event's attributes map as a {@code List} are of type
     * {@code Serializable}.
     * 
     * @param objects
     *            Objects to build into a list and cast as {@code Serializable}.
     * @return The passed in Objects in a {@code List} instance that implements
     *         {@code Serializable}.
     */
    public static final Serializable buildSerializableCollection(
            Serializable... objects) {
        return new ArrayList<>(Arrays.asList(objects));
    }

    /**
     * A type system hack to ensure that {@code Collection} objects that need to
     * be placed in the hazard event's attributes map are of type
     * {@code Serializable}.
     * 
     * @param objects
     *            {@code Collection} to cast as {@code Serializable}.
     * @return The passed in {@code Collection} instance in a new list that
     *         implements {@code Serializable}.
     */
    public static final Serializable buildSerializableCollection(
            Collection<? extends Serializable> objects) {
        if (objects instanceof Serializable) {
            return (Serializable) objects;
        } else {
            return new ArrayList<>(objects);
        }
    }
}
