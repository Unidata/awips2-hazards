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
import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;

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
import java.util.UUID;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
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
import com.raytheon.uf.common.hazards.productgen.data.ProductData;
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
import com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFailed;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerationComplete;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGeneratorInformation;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductStagingInfo;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductStagingInfo.Product;
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

    private final Map<String, ProductGenerationAuditor> productGenerationAuditManager;

    private final Map<Boolean, Collection<ProductGeneratorInformation>> productGeneratorInformationForSelectedHazardsCache;

    private final BoundedReceptionEventBus<Object> eventBus;

    /** CAVE's Mode */
    private final CAVEMode caveMode = CAVEMode.getMode();

    /** Operational mode flag */
    private final boolean operationalMode = caveMode == CAVEMode.OPERATIONAL;
    
    /** String version of CAVEMode */
    private final String caveModeStr = caveMode.toString();
    

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
        this.productGenerationAuditManager = new HashMap<>();
        this.productGeneratorInformationForSelectedHazardsCache = new HashMap<>();
        this.eventBus = this.sessionManager.getEventBus();
    }

    @Override
    public Collection<ProductGeneratorInformation> getAllProductGeneratorInformationForSelectedHazards(
            boolean issue) {
        List<ProductGeneratorInformation> result = new ArrayList<>();
        ProductGeneratorTable pgt = configManager.getProductGeneratorTable();

        for (Entry<String, ProductGeneratorEntry> entry : pgt.entrySet()) {
            if (entry.getValue().isReservedNameNotYetImplemented()) {
                continue;
            }
            Set<IHazardEvent> productEvents = new HashSet<>();
            Set<IHazardEvent> possibleProductEvents = new HashSet<>();

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
                ProductGeneratorInformation info = new ProductGeneratorInformation();
                info.setProductGeneratorName(entry.getKey());
                info.setProductEvents(productEvents);
                info.setPossibleProductEvents(possibleProductEvents);

                EventSet<IEvent> eventSet = buildEventSet(info, issue,
                        LocalizationManager.getInstance().getCurrentSite());

                if (issue == false) {
                    Map<String, Serializable> dialogInfo = productGen
                            .getDialogInfo(entry.getKey(), eventSet);
                    info.setDialogInfo(dialogInfo);
                }
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

    @Override
    public void generateReviewableProduct(List<ProductData> allProductData) {
        ProductGeneratorInformation productGeneratorInformation = new ProductGeneratorInformation();
        synchronized (this.productGenerationAuditManager) {
            final String productGenerationTrackingID = UUID.randomUUID()
                    .toString();
            ProductGenerationAuditor productGenerationAuditor = new ProductGenerationAuditor(
                    false, productGenerationTrackingID);
            productGeneratorInformation
                    .setGenerationID(productGenerationTrackingID);
            productGenerationAuditor
                    .addProductGeneratorInformation(productGeneratorInformation);
            this.productGenerationAuditManager.put(productGenerationTrackingID,
                    productGenerationAuditor);
        }

        if (allProductData.isEmpty() == false) {
            List<LinkedHashMap<KeyInfo, Serializable>> updatedDataList = new ArrayList<>();
            EventSet<IEvent> eventSet = new EventSet<>();
            for (ProductData productData : allProductData) {

                updatedDataList.add(productData.getData());

                if (productGeneratorInformation.getProductGeneratorName() == null) {
                    String productGeneratorName = productData
                            .getProductGeneratorName();
                    productGeneratorInformation
                            .setProductGeneratorName(productGeneratorName);
                    productGeneratorInformation
                            .setProductFormats(sessionManager
                                    .getConfigurationManager()
                                    .getProductGeneratorTable()
                                    .getProductFormats(productGeneratorName));

                    for (Integer eventID : productData.getEventIDs()) {
                        IHazardEvent hazardEvent = new HazardEvent();
                        hazardEvent.setStartTime(productData.getStartTime());
                        hazardEvent.setEventID(String.valueOf(eventID));
                        eventSet.add(hazardEvent);
                    }

                    GeneratedProductList generatedProducts = new GeneratedProductList();
                    generatedProducts.setEventSet(eventSet);
                    productGeneratorInformation
                            .setGeneratedProducts(generatedProducts);
                }
            }

            generateProductReview(productGeneratorInformation, updatedDataList);
        }

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
    public boolean generate(
            ProductGeneratorInformation productGeneratorInformation,
            boolean issue, boolean confirm) {

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
        EventSet<IEvent> events = buildEventSet(productGeneratorInformation,
                issue, locMgrSite);

        String productGeneratorName = productGeneratorInformation
                .getProductGeneratorName();
        String[] productFormats = productGeneratorInformation
                .getProductFormats().getPreviewFormats().toArray(new String[0]);
        IPythonJobListener<GeneratedProductList> listener = new JobListener(
                issue, notificationSender, productGeneratorInformation);
        productGen.generate(productGeneratorName, events,
                productGeneratorInformation.getDialogSelections(),
                productFormats, listener);

        return true;
    }/* end generate() method */

    @Override
    public void issue(ProductGeneratorInformation productGeneratorInformation) {

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
            for (IEvent ev : productGeneratorInformation.getGeneratedProducts()
                    .getEventSet()) {
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

        disseminate(productGeneratorInformation);
    }

    @Override
    public void issueCorrection(
            ProductGeneratorInformation productGeneratorInformation) {
        if (areYouSure()) {
            disseminate(productGeneratorInformation);
            sessionManager.setIssueOngoing(false);
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
        if (CAVEMode.PRACTICE.equals(caveMode)) {
            this.vtecMode = vtecMode;
            this.vtecTestMode = testMode;
        }
    }

    @Override
    public void generateProductReview(
            ProductGeneratorInformation productGeneratorInformation,
            List<LinkedHashMap<KeyInfo, Serializable>> updatedDataList) {
        sessionManager.setPreviewOngoing(true);
        String[] productFormats = productGeneratorInformation
                .getProductFormats().getPreviewFormats().toArray(new String[0]);
        UpdateListener listener = new UpdateListener(
                productGeneratorInformation, notificationSender);
        /*
         * Generating a product review does not need to do any comparisons of a
         * previous version. Instead, the data only needs to be passed to the
         * formatters.
         */
        productGen.update(
                productGeneratorInformation.getProductGeneratorName(),
                updatedDataList, null, productFormats, listener);
    }

    @Override
    public boolean isProductGenerationRequired(boolean issue) {
        boolean result = true;
        Collection<ProductGeneratorInformation> allProductGeneratorInformationForSelectedHazards = getAllProductGeneratorInformationForSelectedHazards(issue);
        productGeneratorInformationForSelectedHazardsCache.put(issue,
                allProductGeneratorInformationForSelectedHazards);
        for (ProductGeneratorInformation productGeneratorInformation : allProductGeneratorInformationForSelectedHazards) {
            if (productGeneratorInformation.getDialogInfo() != null
                    && !productGeneratorInformation.getDialogInfo().isEmpty()) {
                result = false;
            } else if (productGeneratorInformation.getPossibleProductEvents() != null
                    && !productGeneratorInformation.getPossibleProductEvents()
                            .isEmpty()) {
                result = false;
            }
            if (!result) {
                break;
            }
        }
        return result;
    }

    @Override
    public void generateProducts(boolean issue) {
        List<String> unsupportedHazards = getUnsupportedHazards();

        Collection<ProductGeneratorInformation> allProductGeneratorInformationForSelectedHazards = productGeneratorInformationForSelectedHazardsCache
                .get(issue);

        boolean continueWithGeneration = true;

        if (!unsupportedHazards.isEmpty()) {
            StringBuilder message = new StringBuilder(
                    "Products for the following hazard types are not yet supported: ");
            for (String type : unsupportedHazards) {
                message.append(type);
                message.append(" ");
            }

            if (!allProductGeneratorInformationForSelectedHazards.isEmpty()) {
                message.append("\nPress Continue to generate products for the supported hazard types.");
                continueWithGeneration = messenger.getContinueCanceller()
                        .getUserAnswerToQuestion("Unsupported HazardTypes",
                                message.toString());
            } else {
                messenger.getWarner().warnUser("Unsupported HazardTypes",
                        message.toString());
                continueWithGeneration = false;
            }

        }

        if (continueWithGeneration) {
            this.runProductGeneration(
                    productGeneratorInformationForSelectedHazardsCache
                            .get(issue), issue);
        } else {
            if (issue) {
                sessionManager.setIssueOngoing(false);
            } else {
                sessionManager.setPreviewOngoing(false);
            }
        }
    }

    @Override
    public void createProductsFromHazardEventSets(boolean issue,
            List<GeneratedProductList> generatedProductsList) {
        Collection<ProductGeneratorInformation> allProductGeneratorInformationForSelectedHazards = getAllProductGeneratorInformationForSelectedHazards(issue);

        productGeneratorInformationForSelectedHazardsCache.put(issue,
                allProductGeneratorInformationForSelectedHazards);

        ProductGeneratorInformation matchingProductGeneratorInformation = null;

        Collection<ProductGeneratorInformation> allMatchingProductGeneratorInformation = new ArrayList<>();

        List<String> selectedEventIDs = new ArrayList<>();
        for (ObservedHazardEvent selectedEvent : this.sessionManager
                .getEventManager().getSelectedEvents()) {
            selectedEventIDs.add(selectedEvent.getEventID());
        }

        for (GeneratedProductList productList : generatedProductsList) {
            for (ProductGeneratorInformation productGeneratorInformation : allProductGeneratorInformationForSelectedHazards) {
                if (productList.getProductInfo().equals(
                        productGeneratorInformation.getProductGeneratorName())) {
                    matchingProductGeneratorInformation = productGeneratorInformation;
                    break;
                }
            }

            Set<IHazardEvent> selectedEvents = new HashSet<>();
            for (IHazardEvent hazardEvent : matchingProductGeneratorInformation
                    .getProductEvents()) {
                if (selectedEventIDs.contains(hazardEvent.getEventID())) {
                    selectedEvents.add(hazardEvent);
                }
            }
            for (IHazardEvent hazardEvent : matchingProductGeneratorInformation
                    .getPossibleProductEvents()) {
                if (selectedEventIDs.contains(hazardEvent.getEventID())) {

                    selectedEvents.add(hazardEvent);
                }
            }

            matchingProductGeneratorInformation
                    .setProductEvents(selectedEvents);
            allMatchingProductGeneratorInformation
                    .add(matchingProductGeneratorInformation);
        }

        this.runProductGeneration(allMatchingProductGeneratorInformation, issue);
    }

    @Override
    public void createProductsFromProductStagingInfo(boolean issue,
            ProductStagingInfo productStagingInfo) {
        Collection<ProductGeneratorInformation> allProductGeneratorInformationForSelectedHazards = productGeneratorInformationForSelectedHazardsCache
                .get(issue);

        Collection<ProductGeneratorInformation> allMatchingProductGeneratorInformation = new ArrayList<>();

        for (Product stagedProduct : productStagingInfo.getProducts()) {
            for (ProductGeneratorInformation productGeneratorInformation : allProductGeneratorInformationForSelectedHazards) {
                if (stagedProduct.getProductGenerator().equals(
                        productGeneratorInformation.getProductGeneratorName())) {

                    Set<IHazardEvent> selectedEvents = new HashSet<>();
                    for (String eventID : stagedProduct.getSelectedEventIDs()) {
                        for (IHazardEvent event : productGeneratorInformation
                                .getProductEvents()) {
                            if (event.getEventID().equals(eventID)) {
                                selectedEvents.add(event);
                                break;
                            }
                        }
                        for (IHazardEvent event : productGeneratorInformation
                                .getPossibleProductEvents()) {
                            if (event.getEventID().equals(eventID)) {
                                selectedEvents.add(event);
                                break;
                            }
                        }
                    }
                    productGeneratorInformation
                            .setProductEvents(selectedEvents);
                    productGeneratorInformation
                            .setDialogSelections(stagedProduct
                                    .getDialogSelections());
                    allMatchingProductGeneratorInformation
                            .add(productGeneratorInformation);
                }
            }
        }
        this.runProductGeneration(allMatchingProductGeneratorInformation, issue);
    }

    /**
     * Called when product generation is complete.
     * 
     * @param generated
     *            Successful product generation message
     */
    @Handler
    public void auditProductGeneration(ProductGenerated generated) {
        ProductGenerationAuditor productGenerationAuditor = null;
        ProductGeneratorInformation productGeneratorInformation = generated
                .getProductGeneratorInformation();
        final String generationID = productGeneratorInformation
                .getGenerationID();
        final GeneratedProductList generatedProducts = productGeneratorInformation
                .getGeneratedProducts();
        synchronized (this.productGenerationAuditManager) {
            if (this.productGenerationAuditManager.get(generationID)
                    .productGenerated(generatedProducts,
                            productGeneratorInformation) == false) {
                return;
            }

            productGenerationAuditor = this.productGenerationAuditManager
                    .remove(generationID);
        }

        this.publishGenerationCompletion(productGenerationAuditor);
    }

    /**
     * Called when product generation fails.
     * 
     * @param failed
     *            Product generation failed message
     */
    @Handler
    public void handleProductGeneratorResult(ProductFailed failed) {
        ProductGenerationAuditor productGenerationAuditor = null;
        ProductGeneratorInformation productGeneratorInformation = failed
                .getProductGeneratorInformation();
        final String generationID = productGeneratorInformation
                .getGenerationID();
        synchronized (this.productGenerationAuditManager) {
            if (this.productGenerationAuditManager.get(generationID)
                    .productGenerationFailure(productGeneratorInformation) == false) {
                return;
            }

            productGenerationAuditor = this.productGenerationAuditManager
                    .remove(generationID);
        }

        this.publishGenerationCompletion(productGenerationAuditor);
        statusHandler.error("Product Generator "
                + failed.getProductGeneratorInformation()
                        .getProductGeneratorName() + " failed.");
    }

    @Override
    public Collection<ProductGeneratorInformation> getAllProductGeneratorInformationForSelectedHazardsCache(
            boolean issue) {
        return productGeneratorInformationForSelectedHazardsCache.get(issue);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    private boolean isCombinable(IHazardEvent e) {
        String type = HazardEventUtilities.getHazardType(e);
        HazardTypes hazardTypes = configManager.getHazardTypes();
        HazardTypeEntry hazardTypeEntry = hazardTypes.get(type);
        boolean result = hazardTypeEntry.isCombinableSegments();
        return result;
    }

    private EventSet<IEvent> buildEventSet(
            ProductGeneratorInformation productGeneratorInformation,
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
        String mode = caveMode == CAVEMode.PRACTICE ? HazardEventManager.Mode.PRACTICE
                .toString() : HazardEventManager.Mode.OPERATIONAL.toString();
        events.addAttribute(HAZARD_MODE, mode);
        events.addAttribute(HazardConstants.RUN_MODE, caveModeStr);

        if (issue) {
            events.addAttribute(HazardConstants.ISSUE_FLAG, "True");
        } else {
            events.addAttribute(HazardConstants.ISSUE_FLAG, "False");
        }

        String vtecModeToUse = "O";
        if (CAVEMode.PRACTICE.equals(caveMode)) {
            vtecModeToUse = vtecMode;
        }
        events.addAttribute(HazardConstants.VTEC_MODE, vtecModeToUse);

        boolean vtecTestModeToUse = false;
        if (CAVEMode.OPERATIONAL.equals(caveMode)) {
            vtecTestModeToUse = false;
        } else if (CAVEMode.TEST.equals(caveMode)) {
            vtecTestModeToUse = true;
        } else {
            vtecTestModeToUse = vtecTestMode;
        }
        events.addAttribute("vtecTestMode", vtecTestModeToUse);

        HashMap<String, String> sessionDict = new HashMap<>();
        // TODO
        // There is no operational database currently.
        // When this is fixed, then the correct CAVEMode needs to
        // be entered into the sessionDict.
        // sessionDict.put(HazardConstants.TEST_MODE, CAVEMode.getMode()
        // .toString());
        sessionDict.put(HazardConstants.TEST_MODE, "PRACTICE");
        events.addAttribute(HazardConstants.SESSION_DICT, sessionDict);

        if (productGeneratorInformation.getDialogSelections() != null) {
            for (Entry<String, Serializable> entry : productGeneratorInformation
                    .getDialogSelections().entrySet()) {
                events.addAttribute(entry.getKey(), entry.getValue());
            }
        }
        for (IHazardEvent event : productGeneratorInformation
                .getProductEvents()) {
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
            List<Geometry> polygonGeometries = new ArrayList<>();

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

    private void disseminate(
            ProductGeneratorInformation productGeneratorInformation) {
        /*
         * Disseminate the products
         */
        for (IGeneratedProduct generatedProduct : productGeneratorInformation
                .getGeneratedProducts()) {
            /*
             * This is temporary: issueFormats should be user configurable and
             * will be addressed by Issue #691 -- Clean up Configuration Files
             */
            if (productGeneratorInformation.getProductFormats() != null
                    && productGeneratorInformation.getProductFormats()
                            .getIssueFormats() != null) {
                for (String format : productGeneratorInformation
                        .getProductFormats().getIssueFormats()) {
                    List<Serializable> objs = generatedProduct.getEntry(format);
                    if (objs != null) {
                        for (Serializable obj : objs) {
                            ProductUtils.disseminate(String.valueOf(obj),
                                    operationalMode);
                        }
                    }
                }
            }
        }

        Date startTime = null;
        boolean ended = false;
        ArrayList<Integer> eventIDs = new ArrayList<>();
        Iterator<IEvent> iterator = productGeneratorInformation
                .getGeneratedProducts().getEventSet().iterator();
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

        String productInfo = productGeneratorInformation.getGeneratedProducts()
                .getProductInfo();
        for (IGeneratedProduct product : productGeneratorInformation
                .getGeneratedProducts()) {
            if (ended) {
                ProductDataUtil.deleteProductData(caveModeStr, productInfo,
                        eventIDs);
            } else {
                ProductDataUtil.createOrUpdateProductData(caveModeStr,
                        productInfo, eventIDs, startTime, product.getData());
            }
        }

    }

    private boolean checkForConflicts(IHazardEvent hazardEvent) {
        HazardEventManager.Mode mode = (caveMode == CAVEMode.PRACTICE) ? HazardEventManager.Mode.PRACTICE
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

        private final ProductGeneratorInformation productGeneratorInformation;

        public JobListener(boolean issue,
                ISessionNotificationSender notificationSender,
                ProductGeneratorInformation productGeneratorInformation) {
            this.issue = issue;
            this.notificationSender = notificationSender;
            this.productGeneratorInformation = productGeneratorInformation;
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
                        productGeneratorInformation
                                .setGeneratedProducts(result);
                        productGeneratorInformation
                                .getGeneratedProducts()
                                .getEventSet()
                                .addAttribute(HazardConstants.ISSUE_FLAG, issue);
                        if (issue) {
                            issue(productGeneratorInformation);
                        }
                        notificationSender
                                .postNotification(new ProductGenerated(
                                        productGeneratorInformation));
                    } else {
                        productGeneratorInformation
                                .setError(new Throwable(
                                        "GeneratedProduct result from generator is null."));
                        notificationSender.postNotification(new ProductFailed(
                                productGeneratorInformation));
                    }
                }
            });
        }

        @Override
        public void jobFailed(final Throwable e) {

            VizApp.runAsync(new Runnable() {

                @Override
                public void run() {
                    productGeneratorInformation.setError(e);
                    notificationSender.postNotification(new ProductFailed(
                            productGeneratorInformation));
                }
            });
        }

    }

    private void runProductGeneration(
            Collection<ProductGeneratorInformation> allMatchingProductGeneratorInformation,
            boolean issue) {
        boolean confirm = issue;

        /*
         * Build an audit trail to keep track of the products that have been /
         * will need to be generated.
         */
        synchronized (this.productGenerationAuditManager) {
            final String productGenerationTrackingID = UUID.randomUUID()
                    .toString();
            ProductGenerationAuditor productGenerationAuditor = new ProductGenerationAuditor(
                    issue, productGenerationTrackingID);
            for (ProductGeneratorInformation productGeneratorInformation : allMatchingProductGeneratorInformation) {
                productGeneratorInformation
                        .setGenerationID(productGenerationTrackingID);
                productGenerationAuditor
                        .addProductGeneratorInformation(productGeneratorInformation);
            }
            this.productGenerationAuditManager.put(productGenerationTrackingID,
                    productGenerationAuditor);
        }

        for (ProductGeneratorInformation productGeneratorInformation : allMatchingProductGeneratorInformation) {
            boolean continueGeneration = generate(productGeneratorInformation,
                    issue, confirm);
            confirm = false;
            if (continueGeneration == false) {
                /*
                 * Halt product generation, the user indicated that they did not
                 * want to issue the product(s).
                 */
                break;
            }
        }
    }

    private void publishGenerationCompletion(
            ProductGenerationAuditor productGenerationAuditor) {
        IProductGenerationComplete productGenerationComplete = new ProductGenerationComplete(
                productGenerationAuditor.isIssue(),
                productGenerationAuditor.getGeneratedProducts());
        this.eventBus.publishAsync(productGenerationComplete);
    }

}
