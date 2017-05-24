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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.raytheon.uf.common.activetable.ActiveTableKey;
import com.raytheon.uf.common.activetable.ActiveTableRecord;
import com.raytheon.uf.common.dataplugin.PluginDataObject;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IHazardEventServices;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.HazardEventServicesSoapClient;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_TYPE;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.HazardInteroperabilityRecord;
import com.raytheon.uf.common.dataplugin.warning.AbstractWarningRecord;
import com.raytheon.uf.common.dataplugin.warning.PracticeWarningRecord;
import com.raytheon.uf.common.site.SiteMap;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.hazards.interop.dao.AbstractActiveTableDao;
import com.raytheon.uf.edex.hazards.interop.dao.HazardInteroperabilityDao;
import com.raytheon.uf.edex.hazards.interop.dao.InteropObjectManager;
import com.raytheon.uf.edex.hazards.notification.HazardNotifier;
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
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 28, 2015   2826     dgilling     Initial creation
 * Aug 20, 2015   6895     Ben.Phillippe Routing registry requests through
 *                                       request server
 * Feb 16, 2017  29138     Chris.Golden Changed to work with new hazard
 *                                      event manager.
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */
@Service
@Transactional
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AbstractLegacyAppInteropSrv {

    protected final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    protected InteropObjectManager interopObjectManager;

    protected HazardNotifier hazardNotifier;

    protected Collection<String> activeSites;

    protected AbstractLegacyAppInteropSrv() {
        this.activeSites = getActiveSites();
    }

    protected abstract void validateWarningRecord(AbstractWarningRecord record);

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
     * @throws HazardEventServiceException
     */
    public void createHazards(final List<PluginDataObject> objects)
            throws HazardsInteroperabilityException,
            HazardEventServiceException {

        setupForInteroperability();

        for (PluginDataObject ob : objects) {

            // Validate the record
            AbstractWarningRecord warningRecord = validate(ob);

            // Determine the mode
            Mode mode = ob instanceof PracticeWarningRecord ? Mode.PRACTICE
                    : Mode.OPERATIONAL;

            /*
             * Retrieve the correct data access objects for the mode
             */
            HazardInteroperabilityDao interopDao = interopObjectManager
                    .getInteropDao();
            AbstractActiveTableDao activeTableDao = interopObjectManager
                    .getActiveTableDao(mode);

            /*
             * Retrieve the active table records and any interoperability
             * records if they exist
             */
            List<ActiveTableRecord> activeTableRecords = activeTableDao
                    .getBySiteIDEtnPhenSig(warningRecord.getXxxid(),
                            warningRecord.getEtn(), warningRecord.getPhen(),
                            warningRecord.getSig());
            List<HazardInteroperabilityRecord> interopRecords = new ArrayList<HazardInteroperabilityRecord>(
                    0);
            if (!activeTableRecords.isEmpty()) {
                List<ActiveTableKey> activeTableIds = new ArrayList<ActiveTableKey>(
                        activeTableRecords.size());
                for (ActiveTableRecord activeTableRecord : activeTableRecords) {
                    activeTableIds.add(activeTableRecord.getKey());
                }
                interopRecords = interopDao.getByActiveTableID(activeTableIds);
            }

            int activeTableRecordsFound = activeTableRecords.size();
            int interopRecordsFound = interopRecords.size();

            StringBuilder msg = new StringBuilder();
            msg.append("\nFound ").append(activeTableRecordsFound)
                    .append(" active table entries for warning record\nFound ")
                    .append(interopRecordsFound)
                    .append(" interoperability records.");
            statusHandler.info(msg.toString());

            /*
             * If no active table records were found, that's a bad deal
             */
            if (activeTableRecordsFound == 0) {
                throw new HazardsInteroperabilityException(
                        "No active table records found for warningRecord!");
            }

            /*
             * If the warning record did not generate the appropriate number of
             * active table records, that's also bad
             */
            if (activeTableRecordsFound != warningRecord.getUgcZones().size()) {
                throw new HazardsInteroperabilityException(
                        "Warning Record did not generate the proper number of active table entries. Expected "
                                + warningRecord.getUgcZones().size()
                                + " but found " + activeTableRecordsFound);
            }

            /*
             * Verify that the active table records match the correct
             * interoperability records
             */

            if (interopRecordsFound == 0) {
                HazardEvent hazardEvent = buildHazardEventFromWarningRecord(
                        mode, warningRecord);
                for (ActiveTableRecord activeTableRecord : activeTableRecords) {
                    HazardInteroperabilityRecord interopRecord = interopObjectManager
                            .createInteroperabilityRecord(mode,
                                    getInteroperabilityType(), hazardEvent,
                                    activeTableRecord);
                    interopDao.create(interopRecord);
                }
                HazardEventServicesSoapClient.getServices(mode).store(
                        hazardEvent);
            } else if (activeTableRecordsFound == interopRecordsFound) {
                for (ActiveTableRecord activeTableRecord : activeTableRecords) {
                    ActiveTableKey activeTableId = activeTableRecord.getKey();
                    boolean found = false;
                    for (HazardInteroperabilityRecord interopRecord : interopRecords) {
                        // FIXME: activeTableId is no longer an integer
                        if (activeTableId.equals(interopRecord
                                .getActiveTableEventID())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // TODO: Handle this case
                        statusHandler
                                .error("No Interoperability record found for active table id ["
                                        + activeTableId + "]");
                    }
                }
            } else if (activeTableRecordsFound < interopRecordsFound) {
                // TODO: Handle this case
            } else if (activeTableRecordsFound > interopRecordsFound) {
                // TODO: Handle this case
            }
        }
        //
        // /*
        // * Verify that there is a hazard event for each unique hazard event
        // * id found in the interoperability records
        // */
        //
        // if (practiceHazardDao.eventExists(record)) {
        // statusHandler
        // .info("Skipping record for "
        // + record.getPhensig()
        // + " from office "
        // + record.getOfficeid()
        // + " because an entry already exists in the interoperability table.");
        // continue;
        // }
        //
        // IHazardEvent eventToStore = null;
        // IHazardEvent existingEvent = practiceHazardDao
        // .associatedExistingHazard(record.getXxxid(),
        // record.getPhen(), record.getSig(),
        // "[" + record.getEtn() + "]");
        // if (existingEvent != null) {
        // statusHandler.info("Match found for etn " + record.getEtn()
        // + " with Hazard Event " + existingEvent.getEventID());
        // eventToStore = existingEvent;
        // } else {
        // statusHandler.info("Could not find matching hazard event for ["
        // + record.getPhensig() + ":" + record.getEtn() + "].");
        //
        // // FIXME: Don't assume PracticeHazardEvent
        // eventToStore = buildHazardEventFromWarningRecord(record);
        // // Create the hazard event
        // practiceHazardDao.create((PracticeHazardEvent) eventToStore);
        //
        // }
        //
        // // Create the interoperability record
        // interopDao.createOrUpdate(new HazardsInteroperability(eventToStore,
        // record.getEtn(), getInteroperabilityType()));
        //
        // HazardNotifier.notify(eventToStore, NotificationType.STORE,
        // Mode.PRACTICE);
        //
        // /*
        // * TODO: add legacy application specific hook to allow app-specific
        // * interop operations based on the hazard event, warning record and
        // * interop record.
        // */
        //
        // }

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
    private AbstractWarningRecord validate(PluginDataObject rec)
            throws HazardsInteroperabilityException {

        if (!(rec instanceof AbstractWarningRecord)) {
            throw new HazardsInteroperabilityException(
                    "Invalid record received. Expected instance of ["
                            + AbstractWarningRecord.class.getCanonicalName()
                            + "] but instead received ["
                            + rec.getClass().getCanonicalName() + "]");
        }
        AbstractWarningRecord record = (AbstractWarningRecord) rec;

        if (!activeSites.contains(record.getOfficeid())) {
            throw new HazardsInteroperabilityException(
                    "Invalid record received. Product [" + record.getPil()
                            + "] was not issued from the current active sites.");
        }

        String phensig = record.getPhensig();
        if ((phensig == null) || (phensig.isEmpty())) {
            throw new HazardsInteroperabilityException(
                    "Invalid record received. Product does not contain a phensig.");
        }

        // Delegate further validation to subclass
        validateWarningRecord(record);

        return record;
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
     * A method for subclasses to implement that allows the subclass to set up
     * any internal variables/data structures necessary for interoperability. If
     * an exception is thrown during this method, the current set of legacy
     * warning records will not be processed.
     * 
     * @throws Exception
     *             If a fatal error occurs during setup that should prevent
     *             processing of the current set of records for hazard services
     *             interoperability.
     * 
     */
    protected abstract void setupForInteroperability()
            throws HazardsInteroperabilityException;

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
    private HazardEvent buildHazardEventFromWarningRecord(Mode mode,
            final AbstractWarningRecord warningRecord)
            throws HazardsInteroperabilityException {

        HazardEvent event = new HazardEvent();
        String value;
        try {
            value = determineEtn(mode, warningRecord.getXxxid(),
                    warningRecord.getAct(), warningRecord.getEtn());
        } catch (Exception e) {
            throw new HazardsInteroperabilityException("Error determining ETN",
                    e);
        }

        Map<String, Serializable> attrs = new HashMap<String, Serializable>();
        event.setEventID(value);
        event.setStartTime(warningRecord.getStartTime().getTime());
        event.setEndTime(warningRecord.getEndTime().getTime());
        event.setCreationTime(warningRecord.getIssueTime().getTime());
        event.setPhenomenon(warningRecord.getPhen());
        event.setSignificance(warningRecord.getSig());
        event.setSiteID(warningRecord.getXxxid());
        event.setHazardMode(HazardConstants
                .productClassFromAbbreviation(warningRecord.getProductClass()));
        event.setStatus(HazardEventUtilities.stateBasedOnAction(warningRecord
                .getAct()));
        attrs.put(HazardConstants.ISSUE_TIME, warningRecord.getIssueTime()
                .getTime().getTime());
        attrs.put(HazardConstants.EXPIRATION_TIME, warningRecord.getPurgeTime()
                .getTime().getTime());
        attrs.put(HazardConstants.ETNS,
                buildSerializableCollection(warningRecord.getEtn()));
        attrs.put(HazardConstants.VTEC_CODES,
                buildSerializableCollection(warningRecord.getAct()));
        attrs.put(HazardConstants.PILS,
                buildSerializableCollection(warningRecord.getPil()));
        attrs.put(HazardConstants.UGCS,
                buildSerializableCollection(warningRecord.getUgcZones()));

        /*
         * these don't apply to everything so they may be blank, but we want to
         * make sure we fill everything out of the warnings into the
         * IHazardEvent object
         */
        Calendar floodBegin = warningRecord.getFloodBegin();
        if (floodBegin != null) {
            attrs.put(HazardConstants.FLOOD_BEGIN_TIME, floodBegin.getTime());
        }
        Calendar floodEnd = warningRecord.getFloodEnd();
        if (floodEnd != null) {
            attrs.put(HazardConstants.FLOOD_END_TIME, floodEnd.getTime());
        }
        Calendar floodCrest = warningRecord.getFloodCrest();
        if (floodCrest != null) {
            attrs.put(HazardConstants.FLOOD_CREST_TIME, floodCrest.getTime());
        }
        String floodSeverity = warningRecord.getFloodSeverity();
        if (floodSeverity != null) {
            attrs.put(HazardConstants.FLOOD_SEVERITY, floodSeverity);
        }
        String floodRecordStatus = warningRecord.getFloodRecordStatus();
        if (floodRecordStatus != null) {
            attrs.put(HazardConstants.FLOOD_RECORD_STATUS, floodRecordStatus);
        }
        String immediateCause = warningRecord.getImmediateCause();
        if (immediateCause != null) {
            attrs.put(HazardConstants.FLOOD_IMMEDIATE_CAUSE, immediateCause);
        }

        attrs.put(HazardConstants.HAZARD_SOURCE_APP, this
                .getInteroperabilityType().toString());

        try {
            addAppSpecificHazardAttributes(event, warningRecord, attrs);
        } catch (Exception e) {
            throw new HazardsInteroperabilityException(
                    "Error adding app specific Hazard Attributes", e);
        }
        event.addHazardAttributes(attrs);

        return event;
    }

    public String determineEtn(Mode mode, String site, String action, String etn)
            throws Exception {

        IHazardEventServices eventServices = HazardEventServicesSoapClient
                .getServices(mode);

        String value = "";
        if (HazardConstants.NEW_ACTION.equals(action)) {
            value = eventServices.requestEventId(site);
        } else {
            List<HazardEvent> events = eventServices.retrieveByParams(
                    HazardConstants.SITE_ID, site).getEvents();
            for (IHazardEvent ev : events) {
                List<String> hazEtns = HazardEventUtilities.parseEtns(String
                        .valueOf(ev.getHazardAttribute(HazardConstants.ETNS)));
                List<String> recEtn = HazardEventUtilities.parseEtns(etn);
                if (compareEtns(hazEtns, recEtn)) {
                    value = ev.getEventID();
                    break;
                }
            }

            if (value.isEmpty()) {
                value = eventServices.requestEventId(site);
            }
        }
        return value;
    }

    /**
     * Comparing if any of the ETNs of the first list match any of the second
     * list. The lists can be different lengths depending on the code that hits
     * this.
     * 
     * @param etns1
     * @param etns2
     * @return
     */
    private boolean compareEtns(List<String> etns1, List<String> etns2) {
        for (String etn1 : etns1) {
            if (etn1 != null && etn1.isEmpty() == false) {
                for (String etn2 : etns2) {
                    if (etn2 != null && etn2.isEmpty() == false) {
                        if (Integer.valueOf(etn1).equals(Integer.valueOf(etn2))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
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
     * @param attrs
     *            Map holding attributes to be added to the event.
     * @throws Exception
     *             If a fatal error occurs that should prevent this new event
     *             from being stored to the hazard event database.
     */
    protected abstract void addAppSpecificHazardAttributes(HazardEvent event,
            final AbstractWarningRecord warningRecord,
            Map<String, Serializable> attrs) throws Exception;

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

    /**
     * @param interopObjectManager
     *            the interopObjectManager to set
     */
    public void setInteropObjectManager(
            InteropObjectManager interopObjectManager) {
        this.interopObjectManager = interopObjectManager;
    }

    /**
     * @param hazardNotifier
     *            the hazardNotifier to set
     */
    public void setHazardNotifier(HazardNotifier hazardNotifier) {
        this.hazardNotifier = hazardNotifier;
    }
}
