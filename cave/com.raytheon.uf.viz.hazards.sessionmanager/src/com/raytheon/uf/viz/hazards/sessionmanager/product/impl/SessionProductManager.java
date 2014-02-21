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
package com.raytheon.uf.viz.hazards.sessionmanager.product.impl;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_MODE;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.requests.HasConflictsRequest;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.ProductGeneration;
import com.raytheon.uf.common.hazards.productgen.ProductUtils;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardTypeEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardTypes;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.SessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.SessionEventUtilities;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFailed;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFormats;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductInformation;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.undoable.IUndoRedoable;
import com.raytheon.viz.core.mode.CAVEMode;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jts.geom.Puntal;

/**
 * Implementation of ISessionProductManager
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 20, 2013 1257       bsteffen    Initial creation
 * Aug 10, 2013 1265       blawrenc    Added logic to clear 
 *                                     undo/redo information
 *                                     when an event is issued. Also
 *                                     replaced key strings with constants
 *                                     from HazardConstants.py.
 * Aug 12, 2013 1360       hansen      Added logic to handle expiration time
 *                                     other product information from product
 *                                     generators
 * Aug 16, 2013 1325       daniel.s.schaffer@noaa.gov    Alerts integration
 * Aug 20, 2013 1360       blawrenc    Fixed problem with incorrect states showing in console.
 * Aug 26, 2013 1921       blawrenc    Added call to VizApp.runAsync to jobFinished and
 *                                     jobFailed methods. This seems to remedy the 
 *                                     currentModification exception that we were occasionally
 *                                     seeing.
 * Aug 29, 2013 1921       blawrenc    Added logic to issue that the "replaces" information is
 *                                     removed from an event upon issuance.
 * Sep 12, 2013 717        jsanchez    Disseminated the legacy text product.
 * Sep 19, 2013 2046       mnash       Update for product generation.
 * 
 * Sept 16, 2013 1298      thansen     Added popup dialog trying to preview or issue non-supported 
 *                                     hazards
 * Oct 23, 2013 2277       jsanchez    Use thrift request to check for grid conflicts.
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Nov 21, 2013  2446       daniel.s.schaffer@noaa.gov Bug fixes in product staging dialog
 * Nov 29, 2013  2378       blarenc    Simplified state changes when products are issued.
 * Dec 11, 2013  2266      jsanchez     Used GeneratedProductList.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionProductManager implements ISessionProductManager {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(SessionProductManager.class);

    private final ISessionTimeManager timeManager;

    private static final SingleTypeJAXBManager<ProductFormats> jaxb = SingleTypeJAXBManager
            .createWithoutException(ProductFormats.class);

    private static final String PRODUCT_FORMATS_FILE = "hazardServices"
            + File.separator + "productFormats.xml";

    /*
     * A full configuration manager is needed to get access to the product
     * generation table, which is not exposed in ISessionConfigurationManager
     */
    private final ISessionConfigurationManager configManager;

    private final ISessionEventManager eventManager;

    private final ISessionNotificationSender notificationSender;

    private final ProductGeneration productGen;

    /*
     * The messenger for displaying questions and warnings to the user and
     * retrieving answers. This allows the viz side (App Builder) to be
     * responsible for these dialogs, but gives the event manager and other
     * managers access to them without creating a dependency on the
     * gov.noaa.gsd.viz.hazards plugin. Since all parts of Hazard Services can
     * use the same code for creating these dialogs, it makes it easier for them
     * to be stubbed for testing.
     */
    private final IMessenger messenger;

    public SessionProductManager(ISessionTimeManager timeManager,
            ISessionConfigurationManager configManager,
            ISessionEventManager eventManager,
            ISessionNotificationSender notificationSender, IMessenger messenger) {
        this.timeManager = timeManager;
        this.configManager = configManager;
        this.eventManager = eventManager;
        this.notificationSender = notificationSender;
        this.productGen = new ProductGeneration();
        this.messenger = messenger;
    }

    @Override
    public Collection<ProductInformation> getSelectedProducts() {
        List<ProductInformation> result = new ArrayList<ProductInformation>();
        ProductGeneratorTable pgt = configManager.getProductGeneratorTable();

        for (Entry<String, ProductGeneratorEntry> entry : pgt.entrySet()) {
            if (entry.getValue().isReservedNameNotYetImplemented()) {
                continue;
            }
            Set<IHazardEvent> productEvents = new HashSet<IHazardEvent>();
            Set<IHazardEvent> possibleProductEvents = new HashSet<IHazardEvent>();

            for (IHazardEvent e : eventManager.getEvents()) {
                if (e.getPhenomenon() == null || e.getSignificance() == null) {
                    continue;
                }
                String key = HazardEventUtilities.getHazardType(e);
                for (String[] pair : entry.getValue().getAllowedHazards()) {
                    if (pair[0].equals(key)) {
                        if (e.getHazardAttribute(
                                ISessionEventManager.ATTR_SELECTED)
                                .equals(true)) {
                            productEvents.add(e);
                        } else if (e.getState() != HazardState.POTENTIAL
                                && e.getState() != HazardState.ENDED
                                && isCombinable(e)) {
                            possibleProductEvents.add(e);
                        }
                    }
                }
            }
            if (!productEvents.isEmpty()) {
                ProductInformation info = new ProductInformation();
                info.setProductGeneratorName(entry.getKey());
                info.setProductEvents(productEvents);
                info.setPossibleProductEvents(possibleProductEvents);
                Map<String, Serializable> dialogInfo = productGen
                        .getDialogInfo(entry.getKey());

                info.setDialogInfo(dialogInfo);
                info.setFormats(getProductFormats());
                result.add(info);
            }
        }

        // TODO remove the reverse. Currently removing the reverse breaks
        // the Replace Watch with Warning Story.
        Collections.reverse(result);
        return result;
    }

    @Override
    public List<String> getUnsupportedHazards() {
        ProductGeneratorTable pgt = configManager.getProductGeneratorTable();
        List<String> supportedHazards = Lists.newArrayList();
        List<String> unsupportedHazards = Lists.newArrayList();
        for (Entry<String, ProductGeneratorEntry> entry : pgt.entrySet()) {
            if (entry.getValue().isReservedNameNotYetImplemented()) {
                continue;
            }

            String[][] allowedHazards = entry.getValue().getAllowedHazards();

            for (IHazardEvent e : eventManager.getEvents()) {

                if (e.getPhenomenon() == null || e.getSignificance() == null) {
                    continue;
                }

                String key = HazardEventUtilities.getHazardType(e);
                for (String[] pair : allowedHazards) {
                    if (pair[0].equals(key)) {
                        supportedHazards.add(key);
                    }
                }
            }
        }

        for (IHazardEvent e : eventManager.getEvents()) {
            String key = HazardEventUtilities.getHazardType(e);
            boolean found = false;
            for (String supported : supportedHazards) {
                if (supported.equals(key)) {
                    found = true;
                    break;
                }
            }
            if (!found
                    && e.getHazardAttribute(ISessionEventManager.ATTR_SELECTED)
                            .equals(true)) {
                unsupportedHazards.add(key);
            }
        }
        return unsupportedHazards;
    }

    /*
     * Returns a list of product formats from the proudctFormats.xml.
     */
    private static String[] getProductFormats() {
        String[] formats = null;

        IPathManager pm = PathManagerFactory.getPathManager();
        File hazardEventGridsXml = pm.getStaticFile(PRODUCT_FORMATS_FILE);

        try {
            ProductFormats productFormats = jaxb
                    .unmarshalFromXmlFile(hazardEventGridsXml);
            if (productFormats.getProductFormats() != null) {
                formats = productFormats.getProductFormats();
            }

        } catch (SerializationException e) {
            statusHandler
                    .error("XML file unable to be read. Will not be able to create formats!.",
                            e);
        }
        return formats;

    }

    private boolean isCombinable(IHazardEvent e) {
        String type = HazardEventUtilities.getHazardType(e);
        HazardTypes hazardTypes = configManager.getHazardTypes();
        HazardTypeEntry hazardTypeEntry = hazardTypes.get(type);
        boolean result = hazardTypeEntry.isCombinableSegments();
        return result;
    }

    @Override
    public void generate(ProductInformation information, boolean issue,
            boolean confirm) {

        if (validateSelectedHazardsForProductGeneration()
                && eventManager.clipSelectedHazardGeometries()) {

            eventManager.reduceSelectedHazardGeometries();

            /*
             * Update the UGC information in the Hazard Event
             */
            eventManager.updateSelectedHazardUGCs();

            if (issue && confirm) {
                boolean answer = messenger
                        .getQuestionAnswerer()
                        .getUserAnswerToQuestion(
                                "Are you sure "
                                        + "you want to issue the hazard event(s)?");
                if (!answer) {
                    return;
                }
            }
            EventSet<IEvent> events = new EventSet<IEvent>();
            events.addAttribute(HazardConstants.CURRENT_TIME, timeManager
                    .getCurrentTime().getTime());
            events.addAttribute(HazardConstants.SITE_ID,
                    configManager.getSiteID());
            events.addAttribute(HazardConstants.BACKUP_SITEID,
                    LocalizationManager.getInstance().getCurrentSite());
            String mode = CAVEMode.getMode() == CAVEMode.PRACTICE ? HazardEventManager.Mode.PRACTICE
                    .toString() : HazardEventManager.Mode.OPERATIONAL
                    .toString();
            events.addAttribute(HAZARD_MODE, mode);
            String runMode = CAVEMode.getMode().toString();
            events.addAttribute("runMode", runMode);
            events.addAttribute("vtecMode", "O");

            if (issue) {
                events.addAttribute(HazardConstants.ISSUE_FLAG, "True");
            } else {
                events.addAttribute(HazardConstants.ISSUE_FLAG, "False");
            }

            HashMap<String, String> sessionDict = new HashMap<String, String>();
            // TODO
            // There is no operational database currently.
            // When this is fixed, then the correct CAVEMode needs to
            // be entered into the sessionDict.
            // sessionDict.put(HazardConstants.TEST_MODE, CAVEMode.getMode()
            // .toString());
            sessionDict.put(HazardConstants.TEST_MODE, "PRACTICE");
            events.addAttribute(HazardConstants.SESSION_DICT, sessionDict);

            if (information.getDialogSelections() != null) {
                for (Entry<String, Serializable> entry : information
                        .getDialogSelections().entrySet()) {
                    events.addAttribute(entry.getKey(), entry.getValue());
                }
            }
            for (IHazardEvent event : information.getProductEvents()) {
                event = new BaseHazardEvent(event);
                for (Entry<String, Serializable> entry : event
                        .getHazardAttributes().entrySet()) {
                    if (entry.getValue() instanceof Date) {
                        entry.setValue(((Date) entry.getValue()).getTime());
                    }
                }
                String headline = configManager.getHeadline(event);
                event.addHazardAttribute(HazardConstants.HEADLINE, headline);
                if (event.getHazardAttribute(HazardConstants.FORECAST_POINT) != null) {
                    event.addHazardAttribute(HazardConstants.GEO_TYPE,
                            HazardConstants.POINT_TYPE);
                } else {
                    Geometry geometryCollection = event.getGeometry();

                    for (int i = 0; i < geometryCollection.getNumGeometries(); ++i) {
                        Geometry geometry = geometryCollection.getGeometryN(i);

                        if (geometry instanceof Puntal) {
                            event.addHazardAttribute(HazardConstants.GEO_TYPE,
                                    HazardConstants.POINT_TYPE);
                        } else if (geometry instanceof Lineal) {
                            event.addHazardAttribute(HazardConstants.GEO_TYPE,
                                    HazardConstants.LINE_TYPE);
                        } else if (geometry instanceof Polygonal) {
                            event.addHazardAttribute(HazardConstants.GEO_TYPE,
                                    HazardConstants.AREA_TYPE);
                        } else {
                            statusHandler
                                    .warn("SessionProductManager: Geometry type "
                                            + geometry.getClass()
                                            + " not supported. GEO_TYPE hazard attribute not set.");
                        }
                    }
                }
                event.removeHazardAttribute(HazardConstants.HAZARD_EVENT_TYPE);

                /*
                 * Need to re-initialize product information when issuing
                 */
                if (issue) {
                    event.removeHazardAttribute(HazardConstants.EXPIRATION_TIME);
                    event.removeHazardAttribute(HazardConstants.ISSUE_TIME);
                    event.removeHazardAttribute(HazardConstants.VTEC_CODES);
                    event.removeHazardAttribute(HazardConstants.ETNS);
                    event.removeHazardAttribute(HazardConstants.PILS);
                }
                event.removeHazardAttribute(ISessionEventManager.ATTR_ISSUED);
                event.removeHazardAttribute(ISessionEventManager.ATTR_CHECKED);
                event.removeHazardAttribute(ISessionEventManager.ATTR_SELECTED);
                event.removeHazardAttribute(ISessionEventManager.ATTR_HAZARD_CATEGORY);

                events.add(event);
            }

            String product = information.getProductGeneratorName();
            String[] formats = information.getFormats();
            IPythonJobListener<GeneratedProductList> listener = new JobListener(
                    issue, notificationSender, information);
            productGen.generate(product, events,
                    information.getDialogSelections(), formats, listener);
        }
    }

    @Override
    public void issue(ProductInformation information) {

        /*
         * Need to look at all events in the SessionManager because some events
         * for which products were generated may not have been selected. For
         * example, two FA.A's, one selected, one not, and the user adds the
         * second one via the product staging dialog.
         */
        for (IHazardEvent sessionEvent : eventManager.getEvents()) {
            /*
             * Update Hazard Events with product information returned from the
             * Product Generators
             */
            for (IEvent ev : information.getProducts().getEventSet()) {
                IHazardEvent updatedEvent = (IHazardEvent) ev;
                if (checkForConflicts(updatedEvent)) {
                    statusHandler
                            .info("There is a grid conflict with the hazard event.");
                    // TODO It needs to be decided if we should prevent the user
                    // from issuing a hazard if there is a grid conflict.
                }
                if (sessionEvent.getEventID().equals(updatedEvent.getEventID())) {

                    ObservedHazardEvent newEvent = new ObservedHazardEvent(
                            updatedEvent, (SessionEventManager) eventManager);

                    SessionEventUtilities.mergeHazardEvents(newEvent,
                            sessionEvent);
                    /*
                     * This ensures that the "replaces" string is removed for
                     * the next generation of a product.
                     */
                    sessionEvent
                            .removeHazardAttribute(HazardConstants.REPLACES);

                    if (updatedEvent.getState().equals(HazardState.ENDED)) {
                        eventManager.endEvent(sessionEvent);
                    } else {
                        eventManager.issueEvent(sessionEvent);
                    }

                    break;
                }

            }
        }
        /*
         * Disseminate the products
         */
        for (IGeneratedProduct product : information.getProducts()) {
            /*
             * This is temporary: issueFormats should be user configurable and
             * will be addressed by Issue #691 -- Clean up Configuration Files
             */
            if (information.getFormats() != null) {
                for (String format : information.getFormats()) {
                    List<Object> objs = product.getEntry(format);
                    if (objs != null) {
                        for (Object obj : objs) {
                            ProductUtils.disseminate(String.valueOf(obj));
                        }
                    }
                }
            }
        }

    }

    private boolean checkForConflicts(IHazardEvent hazardEvent) {
        boolean hasConflicts = true;
        try {
            // checks if selected events conflicting with existing grids
            // based on time and phensigs
            HasConflictsRequest request = new HasConflictsRequest();
            request.setPhenSig(hazardEvent.getPhenomenon() + "."
                    + hazardEvent.getSignificance());
            request.setSiteID(hazardEvent.getSiteID());
            request.setStartTime(hazardEvent.getStartTime());
            request.setEndTime(hazardEvent.getEndTime());
            hasConflicts = (Boolean) ThriftClient.sendRequest(request);
        } catch (VizException e) {
            statusHandler
                    .error("Unable to check if selected event has any grid conflicts.",
                            e);
        }

        return hasConflicts;
    }

    // @Override
    public void issue_old(ProductInformation information) {

        for (IHazardEvent selectedEvent : information.getProductEvents()) {
            if (selectedEvent.getState() != HazardState.ENDED) {
                Serializable previewState = selectedEvent
                        .getHazardAttribute(HazardConstants.PREVIEW_STATE);
                if (previewState != null
                        && previewState.toString().equalsIgnoreCase(
                                HazardState.ENDED.toString())) {
                    selectedEvent.setState(HazardState.ENDED);
                } else {
                    for (IEvent ev : information.getProducts().getEventSet()) {
                        IHazardEvent event = (IHazardEvent) ev;
                        if (selectedEvent.getEventID().equals(
                                event.getEventID())) {
                            ObservedHazardEvent newEvent = new ObservedHazardEvent(
                                    event, (SessionEventManager) eventManager);
                            SessionEventUtilities.mergeHazardEvents(newEvent,
                                    selectedEvent);
                            break;
                        }
                    }

                    // disseminates the legacy product
                    for (IGeneratedProduct product : information.getProducts()) {
                        List<Object> objs = product.getEntry("Legacy");
                        if (objs != null) {
                            for (Object obj : objs) {
                                ProductUtils.disseminate(String.valueOf(obj));
                            }
                        }
                    }

                    /*
                     * This ensures that the "replaces" string is removed for
                     * the next generation of a product.
                     */
                    selectedEvent
                            .removeHazardAttribute(HazardConstants.REPLACES);
                    selectedEvent.setState(HazardState.ISSUED);
                }
                /*
                 * Clear the undo/redo events.
                 */
                ((IUndoRedoable) selectedEvent).clearUndoRedo();
            }
        }
    }

    /**
     * Listens for the completion of product generation and notifies the event
     * bus.
     */
    private class JobListener implements
            IPythonJobListener<GeneratedProductList> {

        private final boolean issue;

        private final ISessionNotificationSender notificationSender;

        private final ProductInformation info;

        public JobListener(boolean issue,
                ISessionNotificationSender notificationSender,
                ProductInformation info) {
            this.issue = issue;
            this.notificationSender = notificationSender;
            this.info = info;
        }

        @Override
        public void jobFinished(final GeneratedProductList result) {

            /*
             * Need to place the result on the thread the Session Manager is
             * running. At the moment this is the UI thread.
             */
            VizApp.runAsync(new Runnable() {

                @Override
                public void run() {
                    info.setProducts(result);
                    info.getProducts().getEventSet()
                            .addAttribute(HazardConstants.ISSUE_FLAG, issue);
                    if (issue) {
                        issue(info);
                    }
                    notificationSender.postNotification(new ProductGenerated(
                            info));
                }
            });
        }

        @Override
        public void jobFailed(final Throwable e) {

            VizApp.runAsync(new Runnable() {

                @Override
                public void run() {
                    info.setError(e);
                    notificationSender
                            .postNotification(new ProductFailed(info));
                }
            });
        }

    }

    @Override
    public void shutdown() {
        /**
         * Nothing to do right now.
         */
    }

    @Override
    public boolean validateSelectedHazardsForProductGeneration() {

        Collection<IHazardEvent> selectedEvents = eventManager
                .getSelectedEvents();
        Date simulatedTime = SimulatedTime.getSystemTime().getTime();
        List<String> eventIds = Lists.newArrayList();

        for (IHazardEvent selectedEvent : selectedEvents) {

            /*
             * Test if the end time of the selected event is in the past.
             * Products will not be generated for events with end times in the
             * past.
             */
            if (selectedEvent.getEndTime().before(simulatedTime)) {
                eventIds.add(selectedEvent.getEventID());
            }
        }

        if (!eventIds.isEmpty()) {
            StringBuffer warningMessage = new StringBuffer();
            warningMessage.append(eventIds.size() > 1 ? "Events " : "Event ");

            for (String eventId : eventIds) {
                warningMessage.append(eventId);
                warningMessage.append(", ");
            }

            warningMessage.deleteCharAt(warningMessage.lastIndexOf(","));
            warningMessage.append(eventIds.size() > 1 ? "have end times "
                    : "has an end time ");
            warningMessage.append("before the CAVE time.\n");
            warningMessage.append("Product generation halted.");
            messenger.getWarner().warnUser("Product Error",
                    warningMessage.toString());
        }

        return eventIds.isEmpty() ? true : false;
    }

}
