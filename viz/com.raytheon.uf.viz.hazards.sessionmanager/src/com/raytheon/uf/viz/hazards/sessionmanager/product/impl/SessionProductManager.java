/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization*.
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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_CHECKED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SELECTED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_MODE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.requests.HasConflictsRequest;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;
import com.raytheon.uf.common.hazards.productgen.ProductGeneration;
import com.raytheon.uf.common.hazards.productgen.ProductUtils;
import com.raytheon.uf.common.hazards.productgen.data.ProductDataUtil;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.SessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.SessionEventUtilities;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.SessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFailed;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductInformation;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.viz.core.mode.CAVEMode;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
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
 * Feb 18, 2014  2702      jsanchez     Used Serializable objects for entries.
 * Mar 24, 2014  3323      bkowal       Use the mode when checking for grid conflicts.
 * Mar 18, 2014 2917       jsanchez     Implemented preview and issue formats.
 * Apr 11, 2014  2819      Chris.Golden      Fixed bugs with the Preview and Issue
 *                                           buttons in the HID remaining grayed out
 *                                           when they should be enabled.
 * Apr 29, 2014 3558       bkowal       The generate method now returns a boolean.
 * Apr 18, 2014  696       dgilling     Add support for selectable VTEC format.
 * Apr 29, 2014 2925       Chris.Golden Added protection against null values for checking
 *                                      the selection state of a hazard event.
 * Jun 12, 2014 1480       jsanchez     Updated the use of product formats.
 * Jul 14, 2014 4187       jsanchez     Check if the generatedProductsList is valid.
 * Aug 07, 2014 3992       Robert.Blum  Removed use of CAVE's localized site when 
 *                                      retrieving UGC's.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionProductManager implements ISessionProductManager {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(SessionProductManager.class);

    private final ISessionTimeManager timeManager;

    /*
     * A full configuration manager is needed to get access to the product
     * generation table, which is not exposed in ISessionConfigurationManager
     */
    private final ISessionConfigurationManager configManager;

    private final ISessionEventManager<ObservedHazardEvent> eventManager;

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

    private final SessionManager sessionManager;

    private final PartsOfGeographicalAreas partsOfCounty;

    private String vtecMode;

    private boolean vtecTestMode;

    public SessionProductManager(SessionManager sessionManager,
            ISessionTimeManager timeManager,
            ISessionConfigurationManager configManager,
            ISessionEventManager<ObservedHazardEvent> eventManager,
            ISessionNotificationSender notificationSender, IMessenger messenger) {
        this.sessionManager = sessionManager;
        this.timeManager = timeManager;
        this.configManager = configManager;
        this.eventManager = eventManager;
        this.notificationSender = notificationSender;
        this.productGen = new ProductGeneration();
        this.messenger = messenger;
        this.partsOfCounty = new PartsOfGeographicalAreas();

        this.vtecMode = "O";
        this.vtecTestMode = false;
    }

    @Override
    public Collection<ProductInformation> getSelectedProducts(boolean issue) {
        List<ProductInformation> result = new ArrayList<ProductInformation>();
        ProductGeneratorTable pgt = configManager.getProductGeneratorTable();

        for (Entry<String, ProductGeneratorEntry> entry : pgt.entrySet()) {
            if (entry.getValue().isReservedNameNotYetImplemented()) {
                continue;
            }
            Set<IHazardEvent> productEvents = new HashSet<IHazardEvent>();
            Set<IHazardEvent> possibleProductEvents = new HashSet<IHazardEvent>();

            for (ObservedHazardEvent e : eventManager.getEvents()) {
                if (e.getPhenomenon() == null || e.getSignificance() == null) {
                    continue;
                }
                String key = HazardEventUtilities.getHazardType(e);
                for (String[] pair : entry.getValue().getAllowedHazards()) {
                    if (pair[0].equals(key)) {
                        if (Boolean.TRUE.equals(e
                                .getHazardAttribute(HAZARD_EVENT_SELECTED))) {
                            productEvents.add(e);
                        } else if (e.getStatus() != HazardStatus.POTENTIAL
                                && e.getStatus() != HazardStatus.ENDED
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

                EventSet<IEvent> eventSet = buildEventSet(info, issue,
                        LocalizationManager.getInstance().getCurrentSite());

                Map<String, Serializable> dialogInfo = productGen
                        .getDialogInfo(entry.getKey(), eventSet);

                info.setDialogInfo(dialogInfo);
                info.setProductFormats(configManager.getProductGeneratorTable()
                        .getProductFormats(info.getProductGeneratorName()));
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

            for (ObservedHazardEvent e : eventManager.getEvents()) {

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

        for (ObservedHazardEvent e : eventManager.getEvents()) {
            String key = HazardEventUtilities.getHazardType(e);
            boolean found = false;
            for (String supported : supportedHazards) {
                if (supported.equals(key)) {
                    found = true;
                    break;
                }
            }
            if (!found
                    && Boolean.TRUE.equals(e
                            .getHazardAttribute(HAZARD_EVENT_SELECTED))) {
                unsupportedHazards.add(key);
            }
        }
        return unsupportedHazards;
    }

    private boolean isCombinable(IHazardEvent e) {
        String type = HazardEventUtilities.getHazardType(e);
        HazardTypes hazardTypes = configManager.getHazardTypes();
        HazardTypeEntry hazardTypeEntry = hazardTypes.get(type);
        boolean result = hazardTypeEntry.isCombinableSegments();
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager
     * #generate(com.raytheon.uf.viz.hazards.sessionmanager.product.
     * ProductInformation, boolean, boolean)
     */
    @Override
    public boolean generate(ProductInformation information, boolean issue,
            boolean confirm) {

        /*
         * Just terminate ongoing operation and return if there is nothing to
         * do.
         */
        if (!validateSelectedHazardsForProductGeneration()
                || !eventManager.clipSelectedHazardGeometries()) {
            if (issue) {
                sessionManager.setIssueOngoing(false);
            } else {
                sessionManager.setPreviewOngoing(false);
            }
            return false;
        }

        if (issue && confirm && !areYouSure()) {
            return false;
        }
        String locMgrSite = LocalizationManager.getInstance().getCurrentSite();
        EventSet<IEvent> events = buildEventSet(information, issue, locMgrSite);

        String product = information.getProductGeneratorName();
        String[] formats = information.getProductFormats().getPreviewFormats()
                .toArray(new String[0]);
        IPythonJobListener<GeneratedProductList> listener = new JobListener(
                issue, notificationSender, information);
        productGen.generate(product, events, information.getDialogSelections(),
                formats, listener);

        return true;
    }/* end generate() method */

    private EventSet<IEvent> buildEventSet(ProductInformation information,
            boolean issue, String locMgrSite) {
        eventManager.clipSelectedHazardGeometries();
        eventManager.reduceSelectedHazardGeometries();

        /*
         * Update the UGC information in the Hazard Event
         */
        eventManager.updateSelectedHazardUGCs();

        EventSet<IEvent> events = new EventSet<IEvent>();
        events.addAttribute(HazardConstants.CURRENT_TIME, timeManager
                .getCurrentTime().getTime());
        events.addAttribute(HazardConstants.SITE_ID, configManager.getSiteID());
        events.addAttribute(HazardConstants.BACKUP_SITEID, locMgrSite);
        String mode = CAVEMode.getMode() == CAVEMode.PRACTICE ? HazardEventManager.Mode.PRACTICE
                .toString() : HazardEventManager.Mode.OPERATIONAL.toString();
        events.addAttribute(HAZARD_MODE, mode);
        String runMode = CAVEMode.getMode().toString();
        events.addAttribute("runMode", runMode);
        events.addAttribute("vtecMode", "O");

        if (issue) {
            events.addAttribute(HazardConstants.ISSUE_FLAG, "True");
        } else {
            events.addAttribute(HazardConstants.ISSUE_FLAG, "False");
        }

        String vtecModeToUse = "O";
        if (CAVEMode.PRACTICE.equals(CAVEMode.getMode())) {
            vtecModeToUse = vtecMode;
        }
        events.addAttribute("vtecMode", vtecModeToUse);

        boolean vtecTestModeToUse = false;
        if (CAVEMode.OPERATIONAL.equals(CAVEMode.getMode())) {
            vtecTestModeToUse = false;
        } else if (CAVEMode.TEST.equals(CAVEMode.getMode())) {
            vtecTestModeToUse = true;
        } else {
            vtecTestModeToUse = vtecTestMode;
        }
        events.addAttribute("vtecTestMode", vtecTestModeToUse);

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

            /* Make an ArrayList of any polygon geometries we encounter. */
            /* Later, if non-zero length, will make GeometryCollection with it. */
            Geometry geometryCollection = null;
            List<Geometry> polygonGeometries = new ArrayList<Geometry>();

            String headline = configManager.getHeadline(event);
            event.addHazardAttribute(HazardConstants.HEADLINE, headline);
            if (event.getHazardAttribute(HazardConstants.FORECAST_POINT) != null) {
                event.addHazardAttribute(HazardConstants.GEO_TYPE,
                        HazardConstants.POINT_TYPE);
            } else {
                geometryCollection = event.getGeometry();

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
                        polygonGeometries.add(geometry);
                    } else {
                        statusHandler
                                .warn("SessionProductManager: Geometry type "
                                        + geometry.getClass()
                                        + " not supported. GEO_TYPE hazard attribute not set.");
                    }

                }/* end loop over geometryCollection */

            }/* if not a polygon event type */
            event.removeHazardAttribute(HazardConstants.HAZARD_EVENT_TYPE);

            /* Make descriptions of portions of counties if we have any polygon */
            /* geometries for this event. */
            if (polygonGeometries.size() > 0) {
                if (polygonGeometries.size() < geometryCollection
                        .getNumGeometries()) {
                    geometryCollection = new GeometryFactory()
                            .buildGeometry(polygonGeometries);
                }
                this.partsOfCounty.addPortionsDescriptionToEvent(
                        geometryCollection, event, configManager.getSiteID());
            }

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
            event.removeHazardAttribute(HAZARD_EVENT_CHECKED);
            event.removeHazardAttribute(HAZARD_EVENT_SELECTED);
            event.removeHazardAttribute(ISessionEventManager.ATTR_HAZARD_CATEGORY);

            events.add(event);
        }/* end loop over information.getProductEvents */
        return events;
    }

    @Override
    public void issue(ProductInformation information) {

        /*
         * Need to look at all events in the SessionManager because some events
         * for which products were generated may not have been selected. For
         * example, two FA.A's, one selected, one not, and the user adds the
         * second one via the product staging dialog.
         */
        for (ObservedHazardEvent sessionEvent : eventManager.getEvents()) {
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

                    if (updatedEvent.getStatus().equals(HazardStatus.ENDED)) {
                        eventManager.endEvent(sessionEvent, Originator.OTHER);
                    } else {
                        eventManager.issueEvent(sessionEvent, Originator.OTHER);
                    }

                    break;
                }

            }
        }

        disseminate(information);
    }

    private boolean areYouSure() {
        boolean answer = messenger.getQuestionAnswerer()
                .getUserAnswerToQuestion(
                        "Are you sure "
                                + "you want to issue the hazard event(s)?",
                        new String[] { "Issue", "Cancel" });
        if (!answer) {
            sessionManager.setIssueOngoing(false);
        }
        return answer;
    }

    @Override
    public void issueCorrection(ProductInformation information) {
        if (areYouSure()) {
            disseminate(information);
            sessionManager.setIssueOngoing(false);
        }
    }

    private void disseminate(ProductInformation information) {
        boolean operational = CAVEMode.getMode() == CAVEMode.OPERATIONAL;
        /*
         * Disseminate the products
         */
        for (IGeneratedProduct product : information.getProducts()) {
            /*
             * This is temporary: issueFormats should be user configurable and
             * will be addressed by Issue #691 -- Clean up Configuration Files
             */
            if (information.getProductFormats() != null
                    && information.getProductFormats().getIssueFormats() != null) {
                for (String format : information.getProductFormats()
                        .getIssueFormats()) {
                    List<Serializable> objs = product.getEntry(format);
                    if (objs != null) {
                        for (Serializable obj : objs) {
                            ProductUtils.disseminate(String.valueOf(obj),
                                    operational);
                        }
                    }
                }
            }
        }

        Date startTime = null;
        boolean ended = false;
        ArrayList<Integer> eventIDs = new ArrayList<Integer>();
        Iterator<IEvent> iterator = information.getProducts().getEventSet()
                .iterator();
        while (iterator.hasNext()) {
            IEvent event = iterator.next();
            if (event instanceof IHazardEvent) {
                IHazardEvent hazardEvent = (IHazardEvent) event;
                String eventID = hazardEvent.getEventID();
                startTime = hazardEvent.getStartTime();
                HazardStatus status = hazardEvent.getStatus();
                if (status == HazardStatus.ENDED) {
                    ended = true;
                }
                eventIDs.add(new Integer(eventID));
            }
        }

        String mode = CAVEMode.getMode().toString();
        String productInfo = information.getProducts().getProductInfo();
        for (IGeneratedProduct product : information.getProducts()) {
            if (ended) {
                ProductDataUtil.deleteProductData(mode, productInfo, eventIDs);
            } else {
                ProductDataUtil.createOrUpdateProductData(mode, productInfo,
                        eventIDs, startTime, product.getData());
            }
        }

    }

    private boolean checkForConflicts(IHazardEvent hazardEvent) {
        HazardEventManager.Mode mode = (CAVEMode.getMode() == CAVEMode.PRACTICE) ? HazardEventManager.Mode.PRACTICE
                : HazardEventManager.Mode.OPERATIONAL;

        boolean hasConflicts = true;
        try {
            // checks if selected events conflicting with existing grids
            // based on time and phensigs
            HasConflictsRequest request = new HasConflictsRequest(mode);
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
                    if (result != null) {
                        info.setProducts(result);
                        info.getProducts()
                                .getEventSet()
                                .addAttribute(HazardConstants.ISSUE_FLAG, issue);
                        if (issue) {
                            issue(info);
                        }
                        notificationSender
                                .postNotification(new ProductGenerated(info));
                    } else {
                        info.setError(new Throwable(
                                "GeneratedProduct result from generator is null."));
                        notificationSender.postNotification(new ProductFailed(
                                info));
                    }
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

        Collection<ObservedHazardEvent> selectedEvents = eventManager
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager
     * #setVTECFormat(java.lang.String, boolean)
     */
    @Override
    public void setVTECFormat(String vtecMode, boolean testMode) {
        if (CAVEMode.PRACTICE.equals(CAVEMode.getMode())) {
            this.vtecMode = vtecMode;
            this.vtecTestMode = testMode;
        }
    }

    @Override
    public void generateProductReview(ProductInformation productInformation,
            List<LinkedHashMap<KeyInfo, Serializable>> updatedDataList) {
        sessionManager.setPreviewOngoing(true);
        String[] formats = productInformation.getProductFormats()
                .getPreviewFormats().toArray(new String[0]);
        UpdateListener listener = new UpdateListener(productInformation,
                notificationSender);
        /*
         * Generating a product review does not need to do any comparisons of a
         * previous version. Instead, the data only needs to be passed to the
         * formatters.
         */
        productGen.update(productInformation.getProductGeneratorName(),
                updatedDataList, null, formats, listener);
    }
}
