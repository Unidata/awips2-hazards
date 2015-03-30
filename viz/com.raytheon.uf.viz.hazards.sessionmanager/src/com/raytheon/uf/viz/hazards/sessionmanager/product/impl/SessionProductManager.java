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
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.MAPDATA_COUNTY;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.NULL_PRODUCT_GENERATOR;
import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.megawidgets.IControlSpecifier;
import gov.noaa.gsd.viz.megawidgets.ISideEffectsApplier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.megawidgets.sideeffects.PythonSideEffectsApplier;

import java.io.File;
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
import java.util.concurrent.atomic.AtomicInteger;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
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
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFormats;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerationComplete;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGeneratorInformation;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductStagingRequired;
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
 * Apr 11, 2014  2819      Chris.Golden Fixed bugs with the Preview and Issue buttons in the HID
 *                                      remaining grayed out when they should be enabled.
 * Apr 29, 2014 3558       bkowal       The generate method now returns a boolean.
 * Apr 18, 2014  696       dgilling     Add support for selectable VTEC format.
 * Apr 29, 2014 2925       Chris.Golden Added protection against null values for checking
 *                                      the selection state of a hazard event.
 * Jun 12, 2014 1480       jsanchez     Updated the use of product formats.
 * Jul 14, 2014 4187       jsanchez     Check if the generatedProductsList is valid.
 * Aug 07, 2014 3992       Robert.Blum  Removed use of CAVE's localized site when 
 *                                      retrieving UGC's.
 * Oct 02, 2014 4042       Chris.Golden Changed to support two-step product staging dialog
 *                                      (first step allows user to select additional events
 *                                      to be included in products, second step allows the
 *                                      inputting of additional product-specific information
 *                                      using megawidgets). Also made many public, interface-
 *                                      specified methods private, as they are only to be
 *                                      used internally by this class.
 * Oct 20, 2014 4818       Chris.Golden Added wrapping of product staging dialog megawidget
 *                                      specifiers in a scrollable megawidget.
 * Dec 04, 2014 2826       dgilling     Ensure proper order of operations for 
 *                                      product dissemination to avoid duplicate events.
 * Dec 05, 2014 2124       Chris.Golden Changed to work with parameterized config manager.
 *                                      Also added better status handler error messages if
 *                                      the defineDialog() method in a product generator
 *                                      gives back something other than a list of maps for
 *                                      the megawidget specifiers under the metadata key.
 * Dec 17, 2014 2826       dgilling     More order of operations fixes on product issue.
 * Jan 15, 2015 4193       rferrel      Implement dissemination ordering.
 * Jan 20, 2015 4476       rferrel      Implement shutdown of ProductGeneration.
 * Jan 22, 2015 4959       Dan Schaffer Ability to right click to add/remove UGCs from hazards
 * Jan 29, 2015 4375       Dan Schaffer Console initiation of RVS product generation
 * Feb 12, 2015 4959       Dan Schaffer Modify MB3 add/remove UGCs to match Warngen
 * Feb 15, 2015 2271       Dan Schaffer Incur recommender/product generator init costs immediately
 * Feb 26, 2015 6306       mduff        Pass site id to product generation. *
 * Mar 23, 2015 7110       hansen       Automatically include all allowedHazards if "includeAll"
 * Mar 26, 2015 7205       Robert.Blum  Fixed writing to the productData Table.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionProductManager implements ISessionProductManager {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(SessionProductManager.class);

    /**
     * Default PIL dissemination order when none provided in Start Up Config.
     */
    private final static String[] DEFAULT_DISSEMINATION_ORDER = new String[] {
            "FFW", "FLW", "FFS", "FLS", "FFA" };

    private final ISessionTimeManager timeManager;

    /*
     * A full configuration manager is needed to get access to the product
     * generation table, which is not exposed in ISessionConfigurationManager
     */
    private final ISessionConfigurationManager<ObservedSettings> configManager;

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

    /*
     * Cache of product generator information associated with the specified
     * issue flag (true or false).
     * 
     * Currently only actually queried for cached info when generating products
     * in HazardServicesMessageHandler.generate() (which is invoked either by
     * the cancellation of the product editor with preview regeneration
     * specified, or else via the HID with the Issue button); and when bringing
     * up the staging dialog (again prompted by the same generate() method).
     */
    private final Map<Boolean, Collection<ProductGeneratorInformation>> productGeneratorInformationForSelectedHazardsCache;

    private final BoundedReceptionEventBus<Object> eventBus;

    /** CAVE's Mode */
    private final CAVEMode caveMode = CAVEMode.getMode();

    /** Operational mode flag */
    private final boolean operationalMode = caveMode == CAVEMode.OPERATIONAL;

    /** String version of CAVEMode */
    private final String caveModeStr = caveMode.toString();

    /** Used to order product dissemination. */
    private final Map<IGeneratedProduct, ProductGeneratorInformation> pgiMap = new HashMap<>();

    public SessionProductManager(SessionManager sessionManager,
            ISessionTimeManager timeManager,
            ISessionConfigurationManager<ObservedSettings> configManager,
            ISessionEventManager<ObservedHazardEvent> eventManager,
            ISessionNotificationSender notificationSender, IMessenger messenger) {
        this.sessionManager = sessionManager;
        this.timeManager = timeManager;
        this.configManager = configManager;
        this.eventManager = eventManager;
        this.notificationSender = notificationSender;
        this.productGen = new ProductGeneration(configManager.getSiteID());
        this.messenger = messenger;
        this.partsOfCounty = new PartsOfGeographicalAreas();

        this.vtecMode = "O";
        this.vtecTestMode = false;
        this.productGenerationAuditManager = new HashMap<>();
        this.productGeneratorInformationForSelectedHazardsCache = new HashMap<>();
        this.eventBus = this.sessionManager.getEventBus();
    }

    /**
     * Create all the product generator information appropriate for the
     * currently selected hazards. This method is shorthand for calling
     * {@link #getPreliminaryProductGeneratorInformationForSelectedHazards(boolean)}
     * and then adding any product-specific input the user may provide to each
     * of the product generator information objects.
     * 
     * @param issue
     *            Flag indicating whether or not the generation that is being
     *            contemplated is for issuance or preview.
     * @return All product generator information.
     */
    private Collection<ProductGeneratorInformation> createAllProductGeneratorInformationForSelectedHazards(
            boolean issue) {
        Collection<ProductGeneratorInformation> result = getPreliminaryProductGeneratorInformationForSelectedHazards(issue);
        addFinalProductGeneratorInformationForSelectedHazards(issue, result);
        return result;
    }

    /**
     * Get all the preliminary product generator information appropriate for the
     * currently selected hazards. The information so gathered must be augmented
     * by adding any product-specific input the user may provide to each of the
     * product generation information objects before it is used for actual
     * product generation. The preliminary product generator information
     * includes, for each information object, the generator name, the events to
     * which it would apply (all of which are selected), and any other possible
     * events to which it could apply (none of which are currently selected).
     * 
     * @param issue
     *            Flag indicating whether or not the generation that is being
     *            contemplated is for issuance or preview.
     * @return All preliminary product generator information.
     */
    private Collection<ProductGeneratorInformation> getPreliminaryProductGeneratorInformationForSelectedHazards(
            boolean issue) {
        List<ProductGeneratorInformation> result = new ArrayList<>();
        ProductGeneratorTable pgt = configManager.getProductGeneratorTable();

        for (Entry<String, ProductGeneratorEntry> entry : pgt.entrySet()) {
            if (entry.getValue().isReservedNameNotYetImplemented()
                    || entry.getValue().getAutoSelect() == false) {
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
                                && isIncludeAll(e)) {
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
                result.add(info);
            }
        }

        /*
         * TODO remove the reverse. Currently removing the reverse breaks the
         * Replace Watch with Warning Story.
         */
        Collections.reverse(result);
        return result;
    }

    /**
     * Add any final information required to the provided product generator
     * information for the currently selected hazards. The specified preliminary
     * product generator information previously compiled by a call to
     * {@link #getPreliminaryProductGeneratorInformationForSelectedHazards(boolean)}
     * is augmented by the addition of any product-specific megawidgets to be
     * shown for further staging if necessary.
     * 
     * @param issue
     *            Flag indicating whether or not the generation that is being
     *            contemplated is for issuance or preview.
     * @param allProductGeneratorInfo
     *            Previously-collected product generator information.
     * @return Staging still required; will be
     *         {@link StagingRequired#NO_APPLICABLE_EVENTS} if any of the
     *         planned product generations have no associated events,
     *         {@link StagingRequired#NONE} if no further staging information
     *         needs to be collected, or
     *         {@link StagingRequired#PRODUCT_SPECIFIC_INFO} if more staging
     *         information is needed (because product-specific megawidgets to be
     *         shown have been added).
     */
    @SuppressWarnings("unchecked")
    private StagingRequired addFinalProductGeneratorInformationForSelectedHazards(
            boolean issue,
            Collection<ProductGeneratorInformation> allProductGeneratorInfo) {

        boolean dialogInfoNeeded = false;
        for (ProductGeneratorInformation info : allProductGeneratorInfo) {

            /*
             * Determine whether or not dialog info (meaning megawidget
             * specifiers) exist for this combination of events and product
             * generator; if they do, create a megawidget specifier manager for
             * them. If no events apply for this generation, return immediately.
             * (Note that as of the merging of the #2826 Redmine issue's code
             * review, this is only done if previewing; if issuing, no check is
             * made for megawidgets to be displayed.)
             */
            EventSet<IEvent> eventSet = buildEventSet(info, issue,
                    LocalizationManager.getInstance().getCurrentSite());
            if (eventSet == null) {
                return StagingRequired.NO_APPLICABLE_EVENTS;
            }
            Map<String, Serializable> dialogInfo = productGen.getDialogInfo(
                    info.getProductGeneratorName(), eventSet);
            if ((dialogInfo != null) && (dialogInfo.isEmpty() == false)) {
                List<Map<String, Serializable>> dialogInfoFields = null;
                try {
                    dialogInfoFields = (List<Map<String, Serializable>>) dialogInfo
                            .get(HazardConstants.METADATA_KEY);
                    ;
                } catch (Exception e) {
                    statusHandler.error(
                            "Could not get product staging megawidgets for "
                                    + info.getProductGeneratorName()
                                    + ": value associated with "
                                    + HazardConstants.METADATA_KEY
                                    + " by product generator must be list of "
                                    + "megawidget specifiers.", e);
                }
                if ((dialogInfoFields != null)
                        && (dialogInfoFields.isEmpty() == false)) {

                    /*
                     * Get the raw specifiers, and ensure they are scrollable.
                     */
                    List<Map<String, Object>> rawSpecifiers = new ArrayList<>(
                            dialogInfoFields.size());
                    for (Map<String, Serializable> rawSpecifier : dialogInfoFields) {
                        rawSpecifiers.add(new HashMap<String, Object>(
                                rawSpecifier));
                    }
                    rawSpecifiers = MegawidgetSpecifierManager
                            .makeRawSpecifiersScrollable(rawSpecifiers, 10, 5,
                                    10, 5);

                    /*
                     * Get the side effects applier, if any.
                     */
                    ISideEffectsApplier sideEffectsApplier = null;
                    File scriptFile = productGen.getScriptFile(info
                            .getProductGeneratorName());
                    if (PythonSideEffectsApplier
                            .containsSideEffectsEntryPointFunction(scriptFile)) {
                        sideEffectsApplier = new PythonSideEffectsApplier(
                                scriptFile);
                    }

                    /*
                     * Create the megawidget specifier manager.
                     */
                    try {
                        info.setStagingDialogMegawidgetSpecifierManager(new MegawidgetSpecifierManager(
                                rawSpecifiers, IControlSpecifier.class,
                                timeManager.getCurrentTimeProvider(),
                                sideEffectsApplier));
                        dialogInfoNeeded = true;
                    } catch (MegawidgetSpecificationException e) {
                        statusHandler.error(
                                "Could not get product staging megawidgets for "
                                        + info.getProductGeneratorName() + ": "
                                        + e, e);
                    }
                }
            }
            info.setProductFormats(configManager.getProductGeneratorTable()
                    .getProductFormats(info.getProductGeneratorName()));
        }

        /*
         * If at least one of the product generations has megawidget specifiers
         * available, then return true.
         */
        return (dialogInfoNeeded ? StagingRequired.PRODUCT_SPECIFIC_INFO
                : StagingRequired.NONE);
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
        synchronized (productGenerationAuditManager) {
            final String productGenerationTrackingID = UUID.randomUUID()
                    .toString();
            ProductGenerationAuditor productGenerationAuditor = new ProductGenerationAuditor(
                    false, productGenerationTrackingID);
            productGeneratorInformation
                    .setGenerationID(productGenerationTrackingID);
            productGenerationAuditor
                    .addProductGeneratorInformation(productGeneratorInformation);
            productGenerationAuditManager.put(productGenerationTrackingID,
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
                        hazardEvent.setEventID(String.valueOf(eventID));
                        hazardEvent.addHazardAttribute("issueTime",
                                productData.getIssueTime());
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

    /**
     * Generate a product from the given product generation information.
     * 
     * @param productGeneratorInformation
     *            Information about the product to generate.
     * @param issue
     *            Flag indicating whether or not the hazard events are to be
     *            issued; if false, they are to be previewed.
     * @param confirm
     *            Flag indicating whether or not to confirm issuance.
     * @return True if generation should continue, false otherwise.
     */
    private boolean generate(
            ProductGeneratorInformation productGeneratorInformation,
            boolean issue, boolean confirm) {

        /*
         * Just terminate ongoing operation and return if there is nothing to
         * do.
         */
        if (!validateSelectedHazardsForProductGeneration()
                || !eventManager.buildSelectedHazardProductGeometries()) {
            setPreviewOrIssueOngoing(issue, false);
            return false;
        }

        if (issue && confirm && !areYouSure()) {
            return false;
        }

        /*
         * Build the event set for the generation. No need to check for a null
         * event set being returned, as this would only occur if there were no
         * events that applied, and the check above for clipping geometries and
         * validating selected hazards has already ensured that the event set
         * will be non-null.
         */
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

    /**
     * Issue the provided product and all the events associated with it.
     * 
     * @param productGeneratorInformation
     *            Information about the generation that is to occur.
     */
    private void issue(ProductGeneratorInformation productGeneratorInformation) {

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
        productGen.shutdown();
    }

    /**
     * Validate the selected events before product generation.
     * 
     * @return True if the the selected events are valid for product generation,
     *         otherwise false.
     */
    private boolean validateSelectedHazardsForProductGeneration() {

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
            showWarningMessage(eventIds,
                    (eventIds.size() > 1 ? "have end times "
                            : "has an end time ") + "before the CAVE time.");
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

    /**
     * Generate the issued product from the given information and the updated
     * data list derived from the database.
     * 
     * @param productGeneratorInformation
     *            Information about the product to be generated.
     * @param updatedDataList
     *            Updated data list from the database.
     */
    private void generateProductReview(
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

    /**
     * Show the specified warning message to indicate an error occurred in
     * product generation, prepending the specified event identifiers about
     * which the message complains.
     * 
     * @param eventIds
     *            List of event identifiers that are problematic.
     * @param message
     *            Message to be shown; should expect to follow the list of one
     *            or more events given by <code>eventIds</code>, plus a space.
     */
    private void showWarningMessage(List<String> eventIds, String message) {
        StringBuffer warningMessage = new StringBuffer();
        int size = eventIds.size();
        warningMessage.append(size > 1 ? "Events " : "Event ");
        for (int j = 0; j < eventIds.size(); j++) {
            warningMessage.append(eventIds.get(j));
            if (j < eventIds.size() - 1) {
                if (j == eventIds.size() - 2) {
                    warningMessage.append(" and ");
                } else {
                    warningMessage.append(", ");
                }
            } else {
                warningMessage.append(" ");
            }
        }
        warningMessage.append(message);
        warningMessage.append("\n\nProduct generation halted.");
        messenger.getWarner().warnUser("Product Generation Error",
                warningMessage.toString());
    }

    /**
     * Set preview or issue ongoing state.
     * 
     * @param issue
     *            Flag indicating whether or not issue is to have its ongoing
     *            state set. If false, preview is to have its ongoing state set.
     * @param ongoing
     *            Flag indicating whether the action is to be set to be ongoing.
     */
    private void setPreviewOrIssueOngoing(boolean issue, boolean ongoing) {
        if (issue) {
            sessionManager.setIssueOngoing(ongoing);
        } else {
            sessionManager.setPreviewOngoing(ongoing);
        }
    }

    @Override
    public void generateProducts(boolean issue) {

        List<ObservedHazardEvent> selectedEvents = eventManager
                .getSelectedEvents();
        if (!areValidEvents(selectedEvents, issue)) {
            return;
        }

        /*
         * Compile the preliminary product generation information and cache it.
         */
        Collection<ProductGeneratorInformation> allProductGeneratorInfo = this
                .getPreliminaryProductGeneratorInformationForSelectedHazards(issue);
        productGeneratorInformationForSelectedHazardsCache.put(issue,
                allProductGeneratorInfo);

        generate(issue, allProductGeneratorInfo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager
     * #generateProducts(java.lang.String)
     */
    @Override
    public void generateProducts(String productGeneratorName) {
        List<ObservedHazardEvent> selectedEvents = eventManager
                .getSelectedEvents();
        if (!productGeneratorName.equals(NULL_PRODUCT_GENERATOR)) {
            if (!areValidEvents(selectedEvents, false)) {
                return;
            }

            boolean matchingAllowedHazards = isAtLeastOneSelectedAllowed(
                    productGeneratorName, selectedEvents);
            if (!matchingAllowedHazards) {
                messenger.getWarner().warnUser("Product Generation Error",
                        "Generation not supported for selected hazards");
                return;
            }
        }
        Collection<ProductGeneratorInformation> allProductGeneratorInfo = productGeneratorInfoFromName(
                productGeneratorName, selectedEvents);
        productGeneratorInformationForSelectedHazardsCache.put(false,
                allProductGeneratorInfo);
        generate(false, allProductGeneratorInfo);
    }

    private boolean isAtLeastOneSelectedAllowed(String productGeneratorName,
            List<ObservedHazardEvent> selectedEvents) {
        ProductGeneratorTable pgTable = configManager
                .getProductGeneratorTable();
        ProductGeneratorEntry pgEntry = pgTable.get(productGeneratorName);
        for (ObservedHazardEvent selectedEvent : selectedEvents) {
            for (String[] allowedHazards : pgEntry.getAllowedHazards()) {
                if (selectedEvent.getHazardType().equals(allowedHazards[0])) {
                    return true;
                }
            }
        }
        return false;
    }

    private Collection<ProductGeneratorInformation> productGeneratorInfoFromName(
            String productGeneratorName,
            List<ObservedHazardEvent> selectedEvents) {
        Collection<ProductGeneratorInformation> allProductGeneratorInfo = new ArrayList<>();
        ProductGeneratorInformation productGeneratorInfo = new ProductGeneratorInformation();
        productGeneratorInfo.setProductGeneratorName(productGeneratorName);

        productGeneratorInfo
                .setPossibleProductEvents(new HashSet<IHazardEvent>());
        productGeneratorInfo.setProductEvents(new HashSet<IHazardEvent>(
                selectedEvents));
        productGeneratorInfo.setProductFormats(configManager
                .getProductGeneratorTable().getProductFormats(
                        productGeneratorName));
        EventSet<IEvent> eventSet = buildEventSet(productGeneratorInfo, false,
                LocalizationManager.getInstance().getCurrentSite());
        Map<String, Serializable> dialogInfo = productGen.getDialogInfo(
                productGeneratorName, eventSet);
        productGeneratorInfo.setDialogSelections(dialogInfo);
        allProductGeneratorInfo.add(productGeneratorInfo);
        return allProductGeneratorInfo;
    }

    private void generate(boolean issue,
            Collection<ProductGeneratorInformation> allProductGeneratorInfo) {
        /*
         * See if staging is required; if it is, request it and do nothing more.
         */
        StagingRequired stagingRequired = getProductStagingRequired(
                allProductGeneratorInfo, issue);
        if (stagingRequired == StagingRequired.NO_APPLICABLE_EVENTS) {
            setPreviewOrIssueOngoing(issue, false);
            return;
        } else if (stagingRequired != StagingRequired.NONE) {
            eventBus.publishAsync(new ProductStagingRequired(issue,
                    stagingRequired));
            return;
        }

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
            message.append("\n\nProduct generation halted.");

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
            runProductGeneration(
                    productGeneratorInformationForSelectedHazardsCache
                            .get(issue),
                    issue);
        } else {
            setPreviewOrIssueOngoing(issue, false);
        }
    }

    /*
     * Ensure selected hazards meet criteria for product generation
     */
    private boolean areValidEvents(List<ObservedHazardEvent> selectedEvents,
            boolean issue) {
        if (selectedEvents.isEmpty()) {
            messenger.getWarner().warnUser("Product Generation Error",
                    "No selected events");
            return false;
        }

        List<String> noTypeEventIds = new ArrayList<>(selectedEvents.size());
        for (ObservedHazardEvent event : selectedEvents) {
            if ((event.getPhenomenon() == null)
                    || (event.getSignificance() == null)) {
                noTypeEventIds.add(event.getEventID());
            }
        }
        if (noTypeEventIds.isEmpty() == false) {
            showWarningMessage(noTypeEventIds,
                    (noTypeEventIds.size() > 1 ? "have " : "has ")
                            + "no event type.");
            setPreviewOrIssueOngoing(issue, false);
            return false;
        }

        return true;
    }

    @Override
    public void createProductsFromHazardEventSets(boolean issue,
            List<GeneratedProductList> generatedProductsList) {
        Collection<ProductGeneratorInformation> allProductGeneratorInformationForSelectedHazards = createAllProductGeneratorInformationForSelectedHazards(issue);

        productGeneratorInformationForSelectedHazardsCache.put(issue,
                allProductGeneratorInformationForSelectedHazards);

        ProductGeneratorInformation matchingProductGeneratorInformation = null;

        Collection<ProductGeneratorInformation> allMatchingProductGeneratorInformation = new ArrayList<>();

        List<String> selectedEventIDs = new ArrayList<>();
        for (ObservedHazardEvent selectedEvent : sessionManager
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

        runProductGeneration(allMatchingProductGeneratorInformation, issue);
    }

    /**
     * Determines if product staging is needed before generation can occur.
     * 
     * @param allProductGeneratorInfo
     * 
     * @param issue
     *            Flag indicating whether or not the hazard events are to be
     *            issued; if false, they are to be previewed.
     * @return Type of staging required before generation can occur.
     */
    private StagingRequired getProductStagingRequired(
            Collection<ProductGeneratorInformation> allProductGeneratorInfo,
            boolean issue) {

        /*
         * If any of the product generation information objects has unselected
         * events associated with it for possible inclusion, return the value
         * indicating that this is the case.
         */
        /*
         * See ticket 7110. Instead of returning flag for calling Product
         * Staging dialog, add the possible product events to the product events
         * and set them all to selected.
         */
        for (ProductGeneratorInformation info : allProductGeneratorInfo) {
            if ((info.getPossibleProductEvents() != null)
                    && (info.getPossibleProductEvents().isEmpty() == false)) {
                info.getProductEvents().addAll(info.getPossibleProductEvents());
                List<ObservedHazardEvent> selectedEvents = eventManager
                        .getSelectedEvents();
                for (IHazardEvent hevent : info.getProductEvents()) {
                    selectedEvents.add((ObservedHazardEvent) hevent);
                }
                eventManager
                        .setSelectedEvents(selectedEvents, Originator.OTHER);
                // return StagingRequired.POSSIBLE_EVENTS;
            }
        }

        /*
         * Add the final product generation information (the megawidget
         * specifiers for each product that may be generated, to be used to
         * collect additional information from the user) and return the result,
         * which will indicate either that no events apply; that no staging is
         * needed; or that product-specific information must be collected via
         * the collected specifiers.
         */
        return addFinalProductGeneratorInformationForSelectedHazards(issue,
                allProductGeneratorInfo);
    }

    @Override
    public boolean createProductsFromPreliminaryProductStaging(
            boolean issue,
            Map<String, List<String>> selectedEventIdentifiersForProductGeneratorNames) {

        /*
         * Get the product generator information from the cache; it will have
         * been placed there by a previous call to isProductStagingRequired().
         */
        Collection<ProductGeneratorInformation> allProductGeneratorInfo = productGeneratorInformationForSelectedHazardsCache
                .get(issue);

        /*
         * Make a list of all product generation information objects that have
         * been staged. For each one, translate any selected event identifiers
         * into the events it should contain, and set those as the events for
         * the product (removing any of these from the possible events set for
         * the product if they are found therein). If a product is not to
         * contain any events, do not generate it.
         */
        Collection<ProductGeneratorInformation> allMatchingProductGeneratorInfo = new ArrayList<>();
        for (ProductGeneratorInformation info : allProductGeneratorInfo) {
            List<String> selectedEventIdentifiers = selectedEventIdentifiersForProductGeneratorNames
                    .get(info.getProductGeneratorName());
            if ((selectedEventIdentifiers == null)
                    || selectedEventIdentifiers.isEmpty()) {
                continue;
            }
            Set<IHazardEvent> selectedEvents = new HashSet<>();
            for (String eventId : selectedEventIdentifiers) {
                IHazardEvent event = eventManager.getEventById(eventId);
                if (event != null) {
                    selectedEvents.add(event);
                }
            }
            info.setProductEvents(selectedEvents);
            info.setPossibleProductEvents(Sets.difference(
                    info.getPossibleProductEvents(), selectedEvents));
            allMatchingProductGeneratorInfo.add(info);
        }

        /*
         * Add any additional product generation information, meaning megawidget
         * specifiers for collecting product-specific staging information, to
         * the product generation information objects. If any such megawidget
         * specifiers are added, then return true, indicating that the
         * generation cannot continue until further staging information is
         * collected; otherwise, start the generation and return false. This
         * method invocation will never return NO_APPLICABLE_EVENTS, since this
         * code will not execute if that was found to be the case earlier (i.e.
         * the call to getProductStagingRequired() will have been executed by
         * the required generateProducts() before this code is executed).
         */
        if (addFinalProductGeneratorInformationForSelectedHazards(issue,
                allMatchingProductGeneratorInfo) == StagingRequired.PRODUCT_SPECIFIC_INFO) {
            return true;
        }
        productGeneratorInformationForSelectedHazardsCache.put(issue,
                allMatchingProductGeneratorInfo);
        runProductGeneration(allMatchingProductGeneratorInfo, issue);
        return false;
    }

    @Override
    public void createProductsFromFinalProductStaging(
            boolean issue,
            Map<String, Map<String, Serializable>> metadataMapsForProductGeneratorNames) {

        Collection<ProductGeneratorInformation> allProductGeneratorInfo = productGeneratorInformationForSelectedHazardsCache
                .get(issue);

        for (ProductGeneratorInformation info : allProductGeneratorInfo) {
            Map<String, Serializable> metadataMap = metadataMapsForProductGeneratorNames
                    .get(info.getProductGeneratorName());
            if (metadataMap == null) {
                continue;
            }
            info.setDialogSelections(metadataMap);
        }

        runProductGeneration(allProductGeneratorInfo, issue);
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
        synchronized (productGenerationAuditManager) {
            if (productGenerationAuditManager.get(generationID)
                    .productGenerated(generatedProducts,
                            productGeneratorInformation) == false) {
                return;
            }

            productGenerationAuditor = productGenerationAuditManager
                    .remove(generationID);
        }

        publishGenerationCompletion(productGenerationAuditor);
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
        synchronized (productGenerationAuditManager) {
            if (productGenerationAuditManager.get(generationID)
                    .productGenerationFailure(productGeneratorInformation) == false) {
                return;
            }

            productGenerationAuditor = productGenerationAuditManager
                    .remove(generationID);
        }

        publishGenerationCompletion(productGenerationAuditor);
        statusHandler.error("Product Generator "
                + failed.getProductGeneratorInformation()
                        .getProductGeneratorName() + " failed.");
    }

    @Override
    public Collection<ProductGeneratorInformation> getAllProductGeneratorInformationForSelectedHazards(
            boolean issue) {
        return productGeneratorInformationForSelectedHazardsCache.get(issue);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    private boolean isIncludeAll(IHazardEvent e) {
        String type = HazardEventUtilities.getHazardType(e);
        HazardTypes hazardTypes = configManager.getHazardTypes();
        HazardTypeEntry hazardTypeEntry = hazardTypes.get(type);
        boolean result = hazardTypeEntry.isIncludeAll();
        return result;
    }

    /**
     * Build the event set for the specified product generation information.
     * 
     * @param productGeneratorInformation
     *            Product generation information for which to build the event
     *            set.
     * @param issue
     *            Flag indicating whether or not this build has been prompted by
     *            an issue attempt; if false, it has been caused by a preview
     *            attempt.
     * @param locMgrSite
     *            Current site.
     * @return Event set, or <code>null</code> if there are no events that
     *         apply.
     */
    private EventSet<IEvent> buildEventSet(
            ProductGeneratorInformation productGeneratorInformation,
            boolean issue, String locMgrSite) {
        if (eventManager.buildSelectedHazardProductGeometries() == false) {
            return null;
        }

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
            HazardTypeEntry hazardTypeEntry = configManager.getHazardTypes()
                    .get(HazardEventUtilities.getHazardType(event));
            String ugcType = hazardTypeEntry.getUgcType();
            if (ugcType.equals(MAPDATA_COUNTY) && polygonGeometries.size() > 0) {
                if (polygonGeometries.size() < geometryCollection
                        .getNumGeometries()) {
                    geometryCollection = new GeometryFactory()
                            .buildGeometry(polygonGeometries);
                }
                partsOfCounty.addPortionsDescriptionToEvent(geometryCollection,
                        event, configManager.getSiteID());
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
         * Disseminate the products for operational mode before writing to
         * database
         */
        if (operationalMode) {
            sendProducts(productGeneratorInformation);
        }

        // Setup the primary keys for the productData Table
        ArrayList<Integer> eventIDs = null;
        String productInfo = productGeneratorInformation.getGeneratedProducts()
                .getProductInfo();
        Date issueTime = null;

        /*
         * For each product store an entry in the productData table.
         */
        for (IGeneratedProduct product : productGeneratorInformation
                .getGeneratedProducts()) {
            eventIDs = new ArrayList<>();
            Iterator<IEvent> iterator = product.getEventSet().iterator();
            while (iterator.hasNext()) {
                IEvent event = iterator.next();
                if (event instanceof IHazardEvent) {
                    IHazardEvent hazardEvent = (IHazardEvent) event;
                    String eventID = hazardEvent.getEventID();
                    eventIDs.add(new Integer(eventID));
                    Map<String, Serializable> attributes = hazardEvent.getHazardAttributes();
                    // Issue time should be the same for all the events
                    issueTime = new Date((Long) attributes.get("issueTime"));
                }
            }

            ProductDataUtil.createOrUpdateProductData(caveModeStr, productInfo,
                    eventIDs, issueTime, product.getData());
        }

        /*
         * Send practice products after writing to database. Moved the practice
         * dissemination here to prevent the phantom hazards from being created
         * by ingest getting the data before it is written to the db
         */
        if (!operationalMode) {
            sendProducts(productGeneratorInformation);
        }
    }

    private void sendProducts(
            ProductGeneratorInformation productGeneratorInformation) {
        ProductFormats formats = productGeneratorInformation
                .getProductFormats();
        if (formats != null) {
            List<String> issueFormats = formats.getIssueFormats();
            if ((issueFormats != null) && !issueFormats.isEmpty()) {
                for (IGeneratedProduct generatedProduct : orderGeneratedProducts(productGeneratorInformation
                        .getGeneratedProducts())) {
                    for (String issueFormat : issueFormats) {
                        List<Serializable> objs = generatedProduct
                                .getEntry(issueFormat);
                        if (objs != null) {
                            for (Serializable obj : objs) {
                                ProductUtils.disseminate(String.valueOf(obj),
                                        operationalMode);
                            }
                        }
                    }
                }
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
                    try {
                        if (result != null) {
                            productGeneratorInformation
                                    .setGeneratedProducts(result);
                            productGeneratorInformation
                                    .getGeneratedProducts()
                                    .getEventSet()
                                    .addAttribute(HazardConstants.ISSUE_FLAG,
                                            issue);

                            if (issue
                                    && !productGeneratorInformation
                                            .getGeneratedProducts().isEmpty()) {
                                for (IGeneratedProduct prod : productGeneratorInformation
                                        .getGeneratedProducts()) {
                                    pgiMap.put(prod,
                                            productGeneratorInformation);
                                }
                            } else {
                                notificationSender
                                        .postNotification(new ProductGenerated(
                                                productGeneratorInformation));
                            }
                        } else {
                            productGeneratorInformation
                                    .setError(new Throwable(
                                            "GeneratedProduct result from generator is null."));
                            notificationSender
                                    .postNotification(new ProductFailed(
                                            productGeneratorInformation));
                        }
                    } finally {
                        synchronized (pgiMap) {
                            pgiMap.notifyAll();
                        }
                    }
                }
            });
        }

        @Override
        public void jobFailed(final Throwable e) {

            VizApp.runAsync(new Runnable() {

                @Override
                public void run() {
                    try {
                        productGeneratorInformation.setError(e);
                        notificationSender.postNotification(new ProductFailed(
                                productGeneratorInformation));
                    } finally {
                        synchronized (pgiMap) {
                            pgiMap.notifyAll();
                        }
                    }
                }
            });
        }

    }

    private void runProductGeneration(
            final Collection<ProductGeneratorInformation> allMatchingProductGeneratorInformation,
            boolean issue) {
        boolean confirm = issue;

        /*
         * Build an audit trail to keep track of the products that have been /
         * will need to be generated.
         */
        synchronized (productGenerationAuditManager) {
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
            productGenerationAuditManager.put(productGenerationTrackingID,
                    productGenerationAuditor);
        }

        synchronized (pgiMap) {
            pgiMap.clear();
            final AtomicInteger processed = new AtomicInteger(0);
            for (ProductGeneratorInformation productGeneratorInformation : allMatchingProductGeneratorInformation) {
                boolean continueGeneration = generate(
                        productGeneratorInformation, issue, confirm);
                confirm = false;
                if (continueGeneration == false) {
                    /*
                     * Halt product generation, the user indicated that they did
                     * not want to issue the product(s).
                     */
                    break;
                }
                processed.incrementAndGet();
            }

            if (processed.get() > 0) {
                Job job = new Job("pgiMap") {

                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        synchronized (pgiMap) {
                            while (processed.get() > 0) {
                                try {
                                    pgiMap.wait();
                                    processed.decrementAndGet();
                                } catch (InterruptedException e) {
                                    break;
                                }
                            }
                        }
                        VizApp.runAsync(new Runnable() {

                            @Override
                            public void run() {
                                Set<ProductGeneratorInformation> pgiSet = new HashSet<>();
                                /*
                                 * This assumes productGeneratorInformation
                                 * should be issued with the highest order
                                 * dissemination.
                                 */
                                for (IGeneratedProduct key : orderGeneratedProducts(new ArrayList<>(
                                        pgiMap.keySet()))) {
                                    ProductGeneratorInformation productGeneratorInformation = pgiMap
                                            .get(key);
                                    if (!pgiSet
                                            .contains(productGeneratorInformation)) {
                                        /*
                                         * FIXME??? We've had sequencing issues
                                         * with these next 2 lines of code in
                                         * the past. We need the affected
                                         * IHazardEvents to always finish
                                         * storage before calling issue()
                                         * otherwise server-side
                                         * interoperability code will not be
                                         * able to tie the decoded
                                         * ActiveTableRecords to an IHazardEvent
                                         * and will instead create an
                                         * unnecessary duplicate event.
                                         */
                                        notificationSender
                                                .postNotification(new ProductGenerated(
                                                        productGeneratorInformation));
                                        issue(productGeneratorInformation);
                                        pgiSet.add(productGeneratorInformation);
                                    }
                                }
                            }
                        });
                        return Status.OK_STATUS;
                    }
                };
                job.schedule();
            }
        }
    }

    /**
     * Order the generator products based on PIL dissemination order.
     * 
     * @param gpList
     * @return ordered
     */
    private List<IGeneratedProduct> orderGeneratedProducts(
            List<IGeneratedProduct> gpList) {
        int gpListSize = gpList.size();
        List<IGeneratedProduct> ordered = new ArrayList<>(gpListSize);
        if (gpListSize == 1) {
            ordered.addAll(gpList);
        } else if (gpListSize > 1) {
            List<IGeneratedProduct> unordered = new ArrayList<>(gpList);
            String[] disseminationOrder = null;
            StartUpConfig startUpConfig = sessionManager
                    .getConfigurationManager().getStartUpConfig();

            if (startUpConfig != null) {
                disseminationOrder = startUpConfig.getDisseminationOrder();
            }

            if ((disseminationOrder == null)
                    || (disseminationOrder.length == 0)) {
                disseminationOrder = SessionProductManager.DEFAULT_DISSEMINATION_ORDER;
            }

            for (String order : disseminationOrder) {
                Iterator<IGeneratedProduct> unorderedIter = unordered
                        .iterator();
                while (unorderedIter.hasNext()) {
                    IGeneratedProduct key = unorderedIter.next();
                    if (order.equals(key.getProductID())) {
                        ordered.add(key);
                        unorderedIter.remove();
                    }
                }
            }

            /*
             * Found PIL's not in the dissemination order list. Report missing
             * and add to the end of the ordered list and hope they get
             * processed in correct order.
             */
            if (!unordered.isEmpty()) {
                if (statusHandler.isPriorityEnabled(Priority.WARN)) {
                    StringBuilder sb = new StringBuilder(
                            "Missing following PIL's from disseminationOrder: ");
                    Set<String> missing = new HashSet<>();
                    for (IGeneratedProduct gp : unordered) {
                        String pil = gp.getProductID();
                        if (!missing.contains(pil)) {
                            missing.add(pil);
                            sb.append(pil).append(", ");
                        }
                    }
                    sb.setLength(sb.length() - 2);
                    statusHandler.warn(sb.toString());
                }
                ordered.addAll(unordered);
            }
        }
        return ordered;
    }

    private void publishGenerationCompletion(
            ProductGenerationAuditor productGenerationAuditor) {
        IProductGenerationComplete productGenerationComplete = new ProductGenerationComplete(
                productGenerationAuditor.isIssue(),
                productGenerationAuditor.getGeneratedProducts());
        eventBus.publishAsync(productGenerationComplete);
    }

}
