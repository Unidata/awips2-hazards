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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SELECTED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.MAPDATA_COUNTY;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.NATIONAL;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.hazards.interoperability.registry.services.client.InteroperabilityRequestServices;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.hazards.productgen.GeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;
import com.raytheon.uf.common.hazards.productgen.ProductGeneration;
import com.raytheon.uf.common.hazards.productgen.ProductGenerationException;
import com.raytheon.uf.common.hazards.productgen.ProductUtils;
import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.uf.common.hazards.productgen.data.ProductDataUtil;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SiteChanged;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionSelectionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender.IIntraNotificationHandler;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.SessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFailed;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFormats;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerationComplete;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerationConfirmation;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGeneratorInformation;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductStagingRequired;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.viz.core.mode.CAVEMode;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jts.geom.Puntal;

import gov.noaa.gsd.viz.megawidgets.IControlSpecifier;
import gov.noaa.gsd.viz.megawidgets.ISideEffectsApplier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.megawidgets.sideeffects.PythonSideEffectsApplier;

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
 * Apr 10, 2015 6898       Chris.Cody   Refactored async messaging
 * Apr 27, 2015 7224       Robert.Blum  Added eventIDs and phensigs of products being issued to the
 *                                      product generation confirmation dialog.
 * May 07, 2015 6979       Robert.Blum  Changes for product corrections.
 * May 18, 2015 8227       Chris.Cody   Remove NullRecommender
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * Jun 02, 2015 7138       Robert.Blum  Changes for RVS workflow: Issuing without changing 
 *                                      the state of the hazards and not removing the validation
 *                                      of the selected events before calling ProductGeneration.
 * Jun 26, 2015 7919       Robert.Blum  Changes to be able to issue Ended hazards for EXPs.
 * Jul 01, 2015 6726       Robert.Blum  Changes to be able to return to Product Editor from 
 *                                      confirmation dialog.
 * Jul 06, 2015 7747       Robert.Blum  Start of product validation, can not issue with framed text.
 * Jul 07, 2015 8966       Robert.Blum  Added null check for issueTime.
 * Jul 07, 2015 7747       Robert.Blum  Moving product validation code to the product editor. It was
 *                                      found that the previous location could case the active table
 *                                      to incorrectly update when products failed validation, since
 *                                      the validation was done after the product generation.
 * Jul 23, 2015 9625       Robert.Blum  Fixed issueTime when writing to the productData table for RVS.
 * Jul 28, 2015 9737       Chris.Golden Fixed bug that caused a switch to a different setting to still show and
 *                                      generate products for events that should have been hidden by the new
 *                                      setting's filters.
 * Jul 30, 2015 9681       Robert.Blum  Changes for generating products for the product viewer.
 * Jul 31, 2015 7458       Robert.Blum  Updating userName and workstation fields on events that are
 *                                      being issued.
 * Aug 04, 2015 6895       Ben.Phillippe Finished HS data access refactor
 * Aug 13, 2015 8836       Chris.Cody   Changes for a configurable Event Id
 * Aug 20, 2015 6895       Ben.Phillippe Routing registry requests through request server
 * Oct 08, 2015 12346      Chris.Golden Removed SWT code that was put in as part of issue #7747, replacing
 *                                      raw use of SWT message box with abstract user warning issuance.
 * Oct 14, 2015 12494      Chris Golden Reworked to allow hazard types to include only phenomenon (i.e. no
 *                                      significance) where appropriate.
 * Nov 23, 2015 13017      Chris.Golden Changed to work with IMessenger changes.
 * Dec 01, 2015 12473      Roger.Ferrel Do not allow issue in operational mode with DRT time.
 * Dec 03, 2015 13609      mduff        Default VTEC mode based on CAVE mode.
 * Feb 24, 2016 13929      Robert.Blum  Remove first part of staging dialog.
 * Mar 31, 2016  8837      Robert.Blum  Changes for Service Backup.
 * Apr 28, 2016 18267      Chris.Golden Changed to work with new version of mergeHazardEvents().
 * May 03, 2016 18376      Chris.Golden Changed to support reuse of Jep instance between H.S. sessions in
 *                                      the same CAVE session, since stopping and starting the Jep
 *                                      instances when the latter use numpy is dangerous.
 * May 06, 2016 18202      Robert.Blum  Changes for operational/test mode.
 * May 20, 2016 19073      Robert.Blum  Fixed FL.A incorrectly including FF.A and FA.A hazards.
 * May 27, 2016 16491      Robert.Blum  Improved areValidEvents() to also check for invalid statuses.
 * Jun 08, 2016  9620      Robert.Blum  Fixed null pointer on event type.
 * Jun 23, 2016 19537      Chris.Golden Changed to use new parameter for merging hazard events.
 * Jun 23, 2016 19073      Robert.Blum  Fixed includeAll logic.
 * Jun 24, 2016 16491      Kevin.Bisanz Don't add elapsed events as possibleProductEvents.
 * Jul 06, 2016 18257      Kevin.Bisanz Add final call to ProductGeneration.generateFrom(..)
 *                                      to set issueTime during product correction.
 * Jul 25, 2016 19537      Chris.Golden Changed to use new originator parameter for setting geometry resolution
 *                                      of hazard events.
 * Aug 15, 2016 18376      Chris.Golden Added code to make garbage collection of the messenger instance
 *                                      passed in (which is the app builder) more likely.
 * Aug 19, 2016 16871      Kevin.Bisanz After generation, call areValidEvents(..)
 *                                      to ensure nothing has elapsed while the
 *                                      product editor was open.
 * Oct 04, 2016 22573      Robert.Blum  Fixed being able to issue other sites events.
 * Nov 10, 2016 22119      Kevin.Bisanz Add siteID argument to ProductDataUtil.createOrUpdateProductData(..)
 * Nov 17, 2016 26313      Chris.Golden Changed to work with the new capacity of hazard types to be associated
 *                                      with more than one UGC type.
 * Nov 23, 2016 26423      Robert.Blum  Fixed issue with filtered out events.
 * Feb 01, 2017 15556      Chris.Golden Changed to use new selection manager.
 * Feb 17, 2017 21676      Chris.Golden Changed to use session event manager's new merge method.
 * Feb 17, 2017 29138      Chris.Golden Changed to use more efficient query of database hazards.
 * Mar 15, 2017 29138      Chris.Golden Removed creation of observed hazard event object for
 *                                      merging process, as said type is no longer needed for
 *                                      the parameters to the merge method.
 * Mar 16, 2017 15528      Chris.Golden Removed "checked" as an attribute of hazard events.
 * Mar 30, 2017 15528      Chris.Golden Changed to work with new version of mergeHazardEvents().
 * Jun 21, 2017 18375      Chris.Golden Added setting of potential events to pending status when
 *                                      they are previewed or issued.
 * Jun 26, 2017 19207      Chris.Golden Changes to view products for specific events.
 * Sep 27, 2017 38072      Chris.Golden Added use of intra-managerial notifications, and replaced
 *                                      use of event bus with notification sender.
 * Oct 23, 2017 21730      Chris.Golden Adjusted implementations of IIntraNotificationHander to
 *                                      adjust their isSynchronous() methods to take the new
 *                                      parameter.
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

    private final ISessionSelectionManager<ObservedHazardEvent> selectionManager;

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
    private IMessenger messenger;

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

    /** CAVE's Mode */
    private final CAVEMode caveMode = CAVEMode.getMode();

    /** Operational mode flag */
    private final boolean operationalMode = caveMode == CAVEMode.OPERATIONAL;

    /** String version of CAVEMode */
    private final String caveModeStr = caveMode.toString();

    /** Used to order product dissemination. */
    private final Map<IGeneratedProduct, ProductGeneratorInformation> pgiMap = new HashMap<>();

    /**
     * Intra-managerial notification handler for event time range changes.
     */
    private IIntraNotificationHandler<SiteChanged> siteChangeHandler = new IIntraNotificationHandler<SiteChanged>() {

        @Override
        public void handleNotification(SiteChanged notification) {
            siteChanged(notification);
        }

        @Override
        public boolean isSynchronous(SiteChanged notification) {
            return false;
        }
    };

    /**
     * Intra-managerial notification handler for product generated occurrences.
     */
    private IIntraNotificationHandler<ProductGenerated> productGeneratedHandler = new IIntraNotificationHandler<ProductGenerated>() {

        @Override
        public void handleNotification(ProductGenerated notification) {
            auditProductGeneration(notification);
        }

        @Override
        public boolean isSynchronous(ProductGenerated notification) {
            return false;
        }
    };

    /**
     * Intra-managerial notification handler for product failed occurrences.
     */
    private IIntraNotificationHandler<ProductFailed> productFailedHandler = new IIntraNotificationHandler<ProductFailed>() {

        @Override
        public void handleNotification(ProductFailed notification) {
            handleProductGeneratorResult(notification);
        }

        @Override
        public boolean isSynchronous(ProductFailed notification) {
            return false;
        }
    };

    public SessionProductManager(SessionManager sessionManager,
            ISessionTimeManager timeManager,
            ISessionConfigurationManager<ObservedSettings> configManager,
            ISessionEventManager<ObservedHazardEvent> eventManager,
            ISessionSelectionManager<ObservedHazardEvent> selectionManager,
            ISessionNotificationSender notificationSender,
            IMessenger messenger) {
        this.sessionManager = sessionManager;
        this.timeManager = timeManager;
        this.configManager = configManager;
        this.eventManager = eventManager;
        this.selectionManager = selectionManager;
        this.notificationSender = notificationSender;
        this.productGen = ProductGeneration
                .getInstance(configManager.getSiteID());
        this.messenger = messenger;
        this.partsOfCounty = new PartsOfGeographicalAreas();

        this.vtecMode = "O";
        this.vtecTestMode = false;
        this.productGenerationAuditManager = new HashMap<>();
        this.productGeneratorInformationForSelectedHazardsCache = new HashMap<>();
        setDefaultVtecMode();

        notificationSender.registerIntraNotificationHandler(SiteChanged.class,
                siteChangeHandler);
        notificationSender.registerIntraNotificationHandler(
                ProductGenerated.class, productGeneratedHandler);
        notificationSender.registerIntraNotificationHandler(ProductFailed.class,
                productFailedHandler);
    }

    /**
     * Set the default VTEC mode
     */
    private void setDefaultVtecMode() {
        if (caveMode == CAVEMode.PRACTICE) {
            this.vtecMode = "T";
            this.vtecTestMode = true;
        } else if (caveMode == CAVEMode.TEST) {
            this.vtecMode = "T";
            this.vtecTestMode = true;
        } else {
            this.vtecMode = "O";
            this.vtecTestMode = false;
        }
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
     * @param generatedProductsList
     *            List of GeneratedProductList objects used to created the
     *            ProductGeneratorInformation.
     * @return All product generator information.
     */
    private Collection<ProductGeneratorInformation> createAllProductGeneratorInformationForSelectedHazards(
            boolean issue, List<GeneratedProductList> generatedProductsList) {
        Collection<ProductGeneratorInformation> result = getPreliminaryProductGeneratorInformationForGeneratedProductList(
                issue, generatedProductsList);
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
        String site = configManager.getSiteID();

        Set<String> selectedEventIdentifiers = selectionManager
                .getSelectedEventIdentifiers();
        for (Entry<String, ProductGeneratorEntry> entry : pgt.entrySet()) {
            if (entry.getValue().isReservedNameNotYetImplemented()
                    || entry.getValue().getAutoSelect() == false) {
                continue;
            }

            /*
             * Add all the product events that are selected and are allowed by
             * the product generator.
             */
            boolean includeAll = false;
            Set<IHazardEvent> productEvents = new HashSet<>();
            for (ObservedHazardEvent e : eventManager
                    .getEventsForCurrentSettings()) {
                if (HazardEventUtilities.isHazardTypeValid(e) == false) {
                    continue;
                }

                /*
                 * Event must be for the active site.
                 */
                if (e.getSiteID().equals(site)) {
                    String key = HazardEventUtilities.getHazardType(e);
                    for (String[] pair : entry.getValue().getAllowedHazards()) {
                        if (pair[0].equals(key)) {
                            if (selectedEventIdentifiers
                                    .contains(e.getEventID())) {
                                productEvents.add(e);
                            }
                            if (isIncludeAll(key)) {
                                includeAll = true;
                            }
                        }
                    }
                }
            }

            /*
             * One or more of the selected hazards was defined as includeAll.
             * Find all the hazard types that are includeAll for this product
             * generator. Then add any unselected hazards of those types.
             */
            Set<IHazardEvent> possibleProductEvents = new HashSet<>();
            if (includeAll) {
                Set<String> includeAllHazardTypes = new HashSet<>();
                for (String[] pair : entry.getValue().getAllowedHazards()) {
                    String hazardName = pair[0];
                    if (isIncludeAll(hazardName)) {
                        /*
                         * Save this hazardType so we can auto select unselected
                         * hazard of this type.
                         */
                        includeAllHazardTypes.add(hazardName);
                    }
                }

                /*
                 * Add all possible events from the includeAllHazardTypes that
                 * are not currently selected.
                 */
                for (ObservedHazardEvent e : eventManager
                        .getEventsForCurrentSettings()) {
                    boolean isSelected = selectedEventIdentifiers
                            .contains(e.getEventID());
                    /*
                     * Selected hazards are productEvents not
                     * possibleProductEvents. Also skip events for other sites.
                     */
                    if (isSelected || site.equals(e.getSiteID()) == false) {
                        continue;
                    }
                    String eventType = HazardEventUtilities.getHazardType(e);
                    if (eventType != null) {
                        for (String includeType : includeAllHazardTypes) {
                            if (eventType.equals(includeType)) {
                                if (e.getStatus() != HazardStatus.POTENTIAL
                                        && e.getStatus() != HazardStatus.ELAPSED
                                        && e.getStatus() != HazardStatus.ENDED) {
                                    possibleProductEvents.add(e);
                                }
                            }
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
     * Get all the preliminary product generator information appropriate for the
     * generatedProductsList. The information so gathered must be augmented by
     * adding any product-specific input the user may provide to each of the
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
    private Collection<ProductGeneratorInformation> getPreliminaryProductGeneratorInformationForGeneratedProductList(
            boolean issue, List<GeneratedProductList> generatedProductsList) {
        List<ProductGeneratorInformation> result = new ArrayList<>();
        ProductGeneratorTable pgt = configManager.getProductGeneratorTable();

        Set<String> selectedEventIdentifiers = selectionManager
                .getSelectedEventIdentifiers();
        for (GeneratedProductList genProdList : generatedProductsList) {
            ProductGeneratorEntry entry = pgt.get(genProdList.getProductInfo());

            Set<IHazardEvent> productEvents = new HashSet<>();
            Set<IHazardEvent> possibleProductEvents = new HashSet<>();

            for (ObservedHazardEvent e : eventManager
                    .getEventsForCurrentSettings()) {
                if (HazardEventUtilities.isHazardTypeValid(e) == false) {
                    continue;
                }
                String key = HazardEventUtilities.getHazardType(e);
                for (String[] pair : entry.getAllowedHazards()) {
                    if (pair[0].equals(key)) {
                        if (selectedEventIdentifiers.contains(e.getEventID())) {
                            productEvents.add(e);
                        } else if (e.getStatus() != HazardStatus.POTENTIAL
                                && e.getStatus() != HazardStatus.ELAPSED
                                && e.getStatus() != HazardStatus.ENDED
                                && isIncludeAll(key)) {
                            possibleProductEvents.add(e);
                        }
                    }
                }
            }

            ProductGeneratorInformation info = new ProductGeneratorInformation();
            info.setProductGeneratorName(genProdList.getProductInfo());
            // Could be selected events or all valid events in database
            info.setProductEvents(productEvents);
            info.setPossibleProductEvents(possibleProductEvents);
            result.add(info);
        }
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
            EventSet<IEvent> eventSet = buildEventSet(info, issue);
            if (eventSet == null) {
                return StagingRequired.NO_APPLICABLE_EVENTS;
            }
            Map<String, Serializable> dialogInfo = productGen
                    .getDialogInfo(info.getProductGeneratorName(), eventSet);
            if ((dialogInfo != null) && (dialogInfo.isEmpty() == false)) {
                List<Map<String, Serializable>> dialogInfoFields = null;
                try {
                    dialogInfoFields = (List<Map<String, Serializable>>) dialogInfo
                            .get(HazardConstants.METADATA_KEY);
                    ;
                } catch (Exception e) {
                    statusHandler
                            .error("Could not get product staging megawidgets for "
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
                        rawSpecifiers
                                .add(new HashMap<String, Object>(rawSpecifier));
                    }
                    rawSpecifiers = MegawidgetSpecifierManager
                            .makeRawSpecifiersScrollable(rawSpecifiers, 10, 5,
                                    10, 5);

                    /*
                     * Get the side effects applier, if any.
                     */
                    ISideEffectsApplier sideEffectsApplier = null;
                    File scriptFile = productGen
                            .getScriptFile(info.getProductGeneratorName());
                    if (PythonSideEffectsApplier
                            .containsSideEffectsEntryPointFunction(
                                    scriptFile)) {
                        sideEffectsApplier = new PythonSideEffectsApplier(
                                scriptFile);
                    }

                    /*
                     * Create the megawidget specifier manager.
                     */
                    try {
                        info.setStagingDialogMegawidgetSpecifierManager(
                                new MegawidgetSpecifierManager(rawSpecifiers,
                                        IControlSpecifier.class,
                                        timeManager.getCurrentTimeProvider(),
                                        sideEffectsApplier));
                        dialogInfoNeeded = true;
                    } catch (MegawidgetSpecificationException e) {
                        statusHandler
                                .error("Could not get product staging megawidgets for "
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
    public void generateProductFromProductData(List<ProductData> allProductData,
            boolean correctable, boolean viewOnly) {
        ProductGeneratorInformation productGeneratorInformation = new ProductGeneratorInformation();
        synchronized (productGenerationAuditManager) {
            final String productGenerationTrackingID = UUID.randomUUID()
                    .toString();
            ProductGenerationAuditor productGenerationAuditor = new ProductGenerationAuditor(
                    false, productGenerationTrackingID);
            productGeneratorInformation
                    .setGenerationID(productGenerationTrackingID);
            productGenerationAuditor.addProductGeneratorInformation(
                    productGeneratorInformation);
            productGenerationAuditManager.put(productGenerationTrackingID,
                    productGenerationAuditor);
        }

        if (allProductData.isEmpty() == false) {
            boolean practice = !CAVEMode.OPERATIONAL.equals(CAVEMode.getMode());
            HazardEventManager manager = new HazardEventManager(practice);

            GeneratedProductList generatedProductList = new GeneratedProductList();

            EventSet<IEvent> genProductListEventSet = new EventSet<>();
            Set<IHazardEvent> prodGenInfoHazardEvents = new HashSet<IHazardEvent>();
            for (ProductData productData : allProductData) {
                EventSet<IEvent> productEventSet = new EventSet<>();
                GeneratedProduct product = new GeneratedProduct(
                        (String) productData.getData().get("productID"));
                product.setData(productData.getData());
                product.setEditableEntries(productData.getEditableEntries());

                String productGeneratorName = productData
                        .getProductGeneratorName();
                productGeneratorInformation
                        .setProductGeneratorName(productGeneratorName);
                productGeneratorInformation.setProductFormats(sessionManager
                        .getConfigurationManager().getProductGeneratorTable()
                        .getProductFormats(productGeneratorName));

                for (String eventID : productData.getEventIDs()) {
                    IHazardEvent hazardEvent = manager
                            .getLatestByEventID(eventID, true);
                    if (hazardEvent != null) {
                        productEventSet.add(hazardEvent);
                        genProductListEventSet.add(hazardEvent);
                        prodGenInfoHazardEvents.add(hazardEvent);
                    }
                }
                // Add the events to the product and then
                // add the product to the list.
                product.setEventSet(productEventSet);
                generatedProductList.add(product);
            }
            // Set the eventSet for productList and GeneratorInformation
            generatedProductList.setEventSet(genProductListEventSet);
            generatedProductList.setCorrectable(correctable);
            generatedProductList.setViewOnly(viewOnly);

            productGeneratorInformation
                    .setProductEvents(prodGenInfoHazardEvents);
            productGeneratorInformation
                    .setGeneratedProducts(generatedProductList);

            generateProduct(productGeneratorInformation);
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
     * @param productGenLabel
     *            Label for the ProductGenConfirmationDialog to indicate which
     *            products/events will be issued.
     * @return True if generation should continue, false otherwise.
     */
    private boolean generate(
            ProductGeneratorInformation productGeneratorInformation,
            boolean issue, boolean confirm, String productGenLabel) {

        /*
         * Just terminate ongoing operation and return if there is nothing to
         * do.
         */
        if (!eventManager.setLowResolutionGeometriesVisibleForSelectedEvents(
                Originator.OTHER)) {
            setPreviewOrIssueOngoing(issue, false);
            return false;
        }

        if (issue && confirm && !areYouSure(productGenLabel)) {
            return false;
        }

        /*
         * Build the event set for the generation. No need to check for a null
         * event set being returned, as this would only occur if there were no
         * events that applied, and the check above for clipping geometries and
         * validating selected hazards has already ensured that the event set
         * will be non-null.
         */
        EventSet<IEvent> events = buildEventSet(productGeneratorInformation,
                issue);

        String productGeneratorName = productGeneratorInformation
                .getProductGeneratorName();
        String[] productFormats = productGeneratorInformation
                .getProductFormats().getPreviewFormats().toArray(new String[0]);
        GeneratedProductList productList = productGeneratorInformation
                .getGeneratedProducts();
        IPythonJobListener<GeneratedProductList> listener = new JobListener(
                issue, notificationSender, productGeneratorInformation);
        if (productList != null) {
            // Generator already ran, so update existing product dictionaries
            List<Map<String, Serializable>> dataList = new ArrayList<>(
                    productList.size());
            for (IGeneratedProduct product : productList) {
                dataList.add(product.getData());
            }
            productGen.update(productGeneratorName, events, dataList,
                    productFormats, listener);
        } else {
            // Issuing/Previewing from the HID, run entire Generator
            productGen.generate(productGeneratorName, events,
                    productGeneratorInformation.getDialogSelections(),
                    productFormats, listener);
        }

        // Ensure nothing has elapsed while the product editor was sitting open.
        List<IHazardEvent> eventsToCheck = new ArrayList<>(
                productGeneratorInformation.getProductEvents());
        boolean shouldContinue = areValidEvents(eventsToCheck, issue);

        // Got confirmation to issue above - close the Product Editor
        notificationSender
                .postNotificationAsync(new ProductGenerationConfirmation());

        return shouldContinue;
    }/* end generate() method */

    /**
     * Issue the provided product and all the events associated with it.
     * 
     * @param productGeneratorInformation
     *            Information about the generation that is to occur.
     */
    private void issue(
            ProductGeneratorInformation productGeneratorInformation) {

        ProductGeneratorTable pgTable = configManager
                .getProductGeneratorTable();
        ProductGeneratorEntry pgEntry = pgTable
                .get(productGeneratorInformation.getProductGeneratorName());

        if (pgEntry.getChangeHazardStatus() == true) {

            /*
             * Need to look at all events in the SessionManager because some
             * events for which products were generated may not have been
             * selected. For example, two FA.A's, one selected, one not, and the
             * user adds the second one via the product staging dialog.
             */
            for (ObservedHazardEvent sessionEvent : eventManager.getEvents()) {

                /*
                 * Update Hazard Events with product information returned from
                 * the Product Generators
                 */
                for (IEvent ev : productGeneratorInformation
                        .getGeneratedProducts().getEventSet()) {
                    IHazardEvent updatedEvent = (IHazardEvent) ev;
                    try {
                        if (checkForConflicts(updatedEvent)) {
                            statusHandler.info(
                                    "There is a grid conflict with the hazard event.");
                            // TODO It needs to be decided if we should prevent
                            // the
                            // user
                            // from issuing a hazard if there is a grid
                            // conflict.
                        }
                    } catch (HazardEventServiceException e) {
                        // TODO Auto-generated catch block. Please revise as
                        // appropriate.
                        statusHandler.error("Error checking for conflicts", e);
                    }
                    if (sessionEvent.getEventID()
                            .equals(updatedEvent.getEventID())) {

                        eventManager.mergeHazardEvents(updatedEvent,
                                sessionEvent, true, false, true, false,
                                Originator.OTHER);

                        /*
                         * This ensures that the "replaces" string is removed
                         * for the next generation of a product.
                         */
                        sessionEvent.removeHazardAttribute(
                                HazardConstants.REPLACES);

                        /*
                         * Update the userName and workstation. Should be set to
                         * the last person who issued the product/hazard.
                         */
                        sessionEvent.setUserName(LocalizationManager
                                .getInstance().getCurrentUser());
                        sessionEvent.setWorkStation(VizApp.getHostName());

                        if (updatedEvent.getStatus()
                                .equals(HazardStatus.ENDED)) {
                            eventManager.endEvent(sessionEvent,
                                    Originator.OTHER);
                        } else {
                            eventManager.issueEvent(sessionEvent,
                                    Originator.OTHER);
                        }

                        break;
                    }

                }
            }
        }

        try {
            disseminate(productGeneratorInformation);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void issueCorrection(
            final ProductGeneratorInformation productGeneratorInformation) {
        StringBuilder sb = new StringBuilder();
        buildProductGenLabel(productGeneratorInformation, sb);
        if (areYouSure(sb.toString())) {
            IPythonJobListener<GeneratedProductList> listener = new IPythonJobListener<GeneratedProductList>() {
                @Override
                public void jobFinished(GeneratedProductList result) {

                    issue(productGeneratorInformation);

                    sessionManager.setIssueOngoing(false);

                    // Got confirmation to issue above: close the Product Editor
                    notificationSender.postNotificationAsync(
                            new ProductGenerationConfirmation());
                }

                @Override
                public void jobFailed(Throwable e) {
                    statusHandler.handle(Priority.PROBLEM,
                            e.getLocalizedMessage(), e);
                }
            };

            GeneratedProductList generatedProducts = productGeneratorInformation
                    .getGeneratedProducts();

            String[] productFormats = generatedProducts.get(0).getEntries()
                    .keySet().toArray(new String[0]);

            // A KeyInfo is needed to trigger product correction.
            KeyInfo keyInfo = KeyInfo.createBasicKeyInfo(
                    productGeneratorInformation.getGenerationID());
            /*
             * Call the python one more time to update items such as the
             * issueTime.
             * 
             * This will call the listener's jobFinished(..) callback.
             */
            productGen.generateFrom(generatedProducts.getProductInfo(),
                    generatedProducts, keyInfo, productFormats, listener);
        }
    }

    @Override
    public void shutdown() {
        notificationSender
                .unregisterIntraNotificationHandler(siteChangeHandler);
        notificationSender
                .unregisterIntraNotificationHandler(productGeneratedHandler);
        notificationSender
                .unregisterIntraNotificationHandler(productFailedHandler);
        productGen.shutdown();
        messenger = null;
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
        this.vtecMode = vtecMode;
        this.vtecTestMode = testMode;
    }

    /**
     * Generate the issued product from the given information and the updated
     * data list derived from the database.
     * 
     * @param productGeneratorInformation
     *            Information about the product to be generated.
     */
    private void generateProduct(
            ProductGeneratorInformation productGeneratorInformation) {
        if (productGeneratorInformation.getGeneratedProducts()
                .isCorrectable()) {
            sessionManager.setPreviewOngoing(true);
        }
        String[] productFormats = productGeneratorInformation
                .getProductFormats().getIssueFormats().toArray(new String[0]);
        UpdateListener listener = new UpdateListener(
                productGeneratorInformation, notificationSender);

        GeneratedProductList generatedProductList = productGeneratorInformation
                .getGeneratedProducts();

        /*
         * Generating a product review does not need to do any comparisons of a
         * previous version. Instead, the data only needs to be passed to the
         * formatters.
         */
        productGen.generateFrom(
                productGeneratorInformation.getProductGeneratorName(),
                generatedProductList, null, productFormats, listener);
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

        /*
         * Ensure that the events are valid.
         */
        List<ObservedHazardEvent> selectedEvents = selectionManager
                .getSelectedEvents();
        if (!areValidEvents(selectedEvents, issue)) {
            setPreviewOrIssueOngoing(issue, false);
            return;
        }

        /*
         * Ensure that any events that are potential are made pending.
         */
        for (ObservedHazardEvent event : selectedEvents) {
            if (event.getStatus() == HazardStatus.POTENTIAL) {
                event.setStatus(HazardStatus.PENDING, false, Originator.OTHER);
            }
        }

        /*
         * Compile the preliminary product generation information and cache it.
         */
        Collection<ProductGeneratorInformation> allProductGeneratorInfo = this
                .getPreliminaryProductGeneratorInformationForSelectedHazards(
                        issue);
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
        List<ObservedHazardEvent> selectedEvents = selectionManager
                .getSelectedEvents();
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
        Collection<ProductGeneratorInformation> allProductGeneratorInfo = productGeneratorInfoFromName(
                productGeneratorName, selectedEvents);
        productGeneratorInformationForSelectedHazardsCache.put(false,
                allProductGeneratorInfo);
        generate(false, allProductGeneratorInfo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager
     * #generateProducts(java.lang.String)
     */
    @Override
    public void generateNonHazardProducts(String productGeneratorName) {
        Collection<ProductGeneratorInformation> allProductGeneratorInfo = productGeneratorInfoFromName(
                productGeneratorName, selectionManager.getSelectedEvents());
        productGeneratorInformationForSelectedHazardsCache.put(false,
                allProductGeneratorInfo);
        generate(false, allProductGeneratorInfo);
    }

    private boolean isAtLeastOneSelectedAllowed(String productGeneratorName,
            Collection<ObservedHazardEvent> selectedEvents) {
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
            Collection<ObservedHazardEvent> selectedEvents) {
        Collection<ProductGeneratorInformation> allProductGeneratorInfo = new ArrayList<>();
        ProductGeneratorInformation productGeneratorInfo = new ProductGeneratorInformation();
        productGeneratorInfo.setProductGeneratorName(productGeneratorName);

        productGeneratorInfo
                .setPossibleProductEvents(new HashSet<IHazardEvent>());
        productGeneratorInfo
                .setProductEvents(new HashSet<IHazardEvent>(selectedEvents));
        productGeneratorInfo
                .setProductFormats(configManager.getProductGeneratorTable()
                        .getProductFormats(productGeneratorName));
        EventSet<IEvent> eventSet = buildEventSet(productGeneratorInfo, false);
        Map<String, Serializable> dialogInfo = productGen
                .getDialogInfo(productGeneratorName, eventSet);
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
            notificationSender
                    .postNotificationAsync(new ProductStagingRequired(issue));
            return;
        }
        runProductGeneration(
                productGeneratorInformationForSelectedHazardsCache.get(issue),
                issue);
    }

    /*
     * Ensure selected hazards meet criteria for product generation
     */
    private boolean areValidEvents(
            Collection<? extends IHazardEvent> selectedEvents, boolean issue) {
        String site = configManager.getSiteID();

        if (selectedEvents.isEmpty()) {
            messenger.getWarner().warnUser("Product Generation Error",
                    "No selected events");
            return false;
        }
        List<String> invalidSiteEventIds = new ArrayList<>();
        List<String> invalidTypeEventIds = new ArrayList<>();
        List<String> invalidStatusEventIds = new ArrayList<>();
        for (IHazardEvent event : selectedEvents) {
            if (HazardEventUtilities.isHazardTypeValid(event) == false) {
                invalidTypeEventIds.add(event.getEventID());
            }
            if (event.getStatus() == HazardStatus.ELAPSED
                    || event.getStatus() == HazardStatus.ENDED) {
                invalidStatusEventIds.add(event.getEventID());
            }
            if (event.getSiteID().equals(site) == false) {
                invalidSiteEventIds.add(event.getEventID());
            }
        }

        if (invalidSiteEventIds.isEmpty() == false
                || invalidTypeEventIds.isEmpty() == false
                || invalidStatusEventIds.isEmpty() == false) {
            StringBuilder sb = new StringBuilder();
            sb.append("Invalid Events For Product Generation:\n\n");

            if (invalidSiteEventIds.isEmpty() == false) {
                formatEventIdList(invalidSiteEventIds, sb);
                sb.append(invalidSiteEventIds.size() > 1 ? "are" : "is");
                sb.append(
                        " not for the active site and can not be issued.\n\n");
            }

            if (invalidTypeEventIds.isEmpty() == false) {
                formatEventIdList(invalidTypeEventIds, sb);
                sb.append(invalidTypeEventIds.size() > 1 ? "have" : "has");
                sb.append(" no hazard type set.\n\n");
            }

            if (invalidStatusEventIds.isEmpty() == false) {
                formatEventIdList(invalidStatusEventIds, sb);
                sb.append(invalidStatusEventIds.size() > 1 ? "have" : "has");
                sb.append(" Ended or Elapsed hazard status\n\n.");
            }

            sb.append("Product Generation halted.");
            messenger.getWarner().warnUser("Product Generation Error",
                    sb.toString());
            return false;
        }
        return true;
    }

    private void formatEventIdList(List<String> eventIds, StringBuilder sb) {
        sb.append(eventIds.size() > 1 ? "Events " : "Event ");
        sb.append(joinList(eventIds, ", ", " and "));
        sb.append(" ");
    }

    /**
     * Utility method for joining a list of Strings.
     * 
     * @param items
     *            List of items to be joined together.
     * @param sep
     *            Separator used to join the items in the list.
     * @param conj
     *            Conjunction used to join the last two items in the list.
     */
    public static String joinList(List<String> items, String sep, String conj) {
        String retVal = "";

        int size = items.size();
        if (size > 1) {
            retVal = Joiner.on(sep).skipNulls().join(items.subList(0, size - 1))
                    .concat(conj).concat(items.get(size - 1));
        } else if (size == 1) {
            retVal = items.get(0);
        }
        return retVal;
    }

    @Override
    public void createProductsFromHazardEventSets(boolean issue,
            List<GeneratedProductList> generatedProductsList) {
        Collection<ProductGeneratorInformation> allProductGeneratorInformationForSelectedHazards = createAllProductGeneratorInformationForSelectedHazards(
                issue, generatedProductsList);

        productGeneratorInformationForSelectedHazardsCache.put(issue,
                allProductGeneratorInformationForSelectedHazards);

        ProductGeneratorInformation matchingProductGeneratorInformation = null;

        Collection<ProductGeneratorInformation> allMatchingProductGeneratorInformation = new ArrayList<>();

        Set<String> selectedEventIDs = selectionManager
                .getSelectedEventIdentifiers();

        for (GeneratedProductList productList : generatedProductsList) {
            for (ProductGeneratorInformation productGeneratorInformation : allProductGeneratorInformationForSelectedHazards) {
                if (productList.getProductInfo()
                        .equals(productGeneratorInformation
                                .getProductGeneratorName())) {
                    matchingProductGeneratorInformation = productGeneratorInformation;
                    // Set the Generated Products in the Information
                    matchingProductGeneratorInformation
                            .setGeneratedProducts(productList);
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

    @Override
    public void createProductsFromGeneratedProductList(boolean issue,
            List<GeneratedProductList> generatedProductsList) {
        Collection<ProductGeneratorInformation> allProductGeneratorInformationForGeneratedProductsList = createAllProductGeneratorInformationForSelectedHazards(
                issue, generatedProductsList);

        productGeneratorInformationForSelectedHazardsCache.put(issue,
                allProductGeneratorInformationForGeneratedProductsList);

        ProductGeneratorInformation matchingProductGeneratorInformation = null;

        Collection<ProductGeneratorInformation> allMatchingProductGeneratorInformation = new ArrayList<>();

        for (GeneratedProductList productList : generatedProductsList) {
            for (ProductGeneratorInformation productGeneratorInformation : allProductGeneratorInformationForGeneratedProductsList) {
                if (productList.getProductInfo()
                        .equals(productGeneratorInformation
                                .getProductGeneratorName())) {
                    matchingProductGeneratorInformation = productGeneratorInformation;
                    // Set the Generated Products in the Information
                    matchingProductGeneratorInformation
                            .setGeneratedProducts(productList);
                    break;
                }
            }

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
         * events associated with it for possible inclusion, add the possible
         * products to the product events and make them all selected.
         */
        for (ProductGeneratorInformation info : allProductGeneratorInfo) {
            if ((info.getPossibleProductEvents() != null)
                    && (info.getPossibleProductEvents().isEmpty() == false)) {
                info.getProductEvents().addAll(info.getPossibleProductEvents());
                Set<ObservedHazardEvent> selectedEvents = new HashSet<>(
                        selectionManager.getSelectedEvents());
                for (IHazardEvent hevent : info.getProductEvents()) {
                    selectedEvents.add((ObservedHazardEvent) hevent);
                }
                selectionManager.setSelectedEvents(selectedEvents,
                        Originator.OTHER);
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
    public boolean createProductsFromPreliminaryProductStaging(boolean issue,
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
    public void createProductsFromFinalProductStaging(boolean issue,
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
    private void auditProductGeneration(ProductGenerated generated) {
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
    private void handleProductGeneratorResult(ProductFailed failed) {
        ProductGenerationAuditor productGenerationAuditor = null;
        ProductGeneratorInformation productGeneratorInformation = failed
                .getProductGeneratorInformation();
        final String generationID = productGeneratorInformation
                .getGenerationID();
        synchronized (productGenerationAuditManager) {
            if (productGenerationAuditManager.get(generationID)
                    .productGenerationFailure(
                            productGeneratorInformation) == false) {
                return;
            }

            productGenerationAuditor = productGenerationAuditManager
                    .remove(generationID);
        }

        publishGenerationCompletion(productGenerationAuditor);
        statusHandler.error(
                "Product Generator " + failed.getProductGeneratorInformation()
                        .getProductGeneratorName() + " failed.");
    }

    /**
     * Respond to the current site changing.
     * 
     * @param change
     *            Change that occurred.
     */
    private void siteChanged(SiteChanged change) {
        productGen
                .setSite(sessionManager.getConfigurationManager().getSiteID());
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

    private boolean isIncludeAll(String hazardType) {
        HazardTypes hazardTypes = configManager.getHazardTypes();
        HazardTypeEntry hazardTypeEntry = hazardTypes.get(hazardType);
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
     * @return Event set, or <code>null</code> if there are no events that
     *         apply.
     */
    private EventSet<IEvent> buildEventSet(
            ProductGeneratorInformation productGeneratorInformation,
            boolean issue) {
        if (eventManager.setLowResolutionGeometriesVisibleForSelectedEvents(
                Originator.OTHER) == false) {
            return null;
        }

        /*
         * Update the UGC information in the Hazard Event
         */
        try {
            eventManager.updateSelectedHazardUGCs();
        } catch (ProductGenerationException e) {
            messenger.getWarner().warnUser("Product Generation Error",
                    productGeneratorInformation.getProductGeneratorName()
                            + " unable to run: " + e.getMessage());
            return null;
        }

        EventSet<IEvent> events = new EventSet<IEvent>();
        events.addAttribute(HazardConstants.CURRENT_TIME,
                timeManager.getCurrentTime().getTime());
        events.addAttribute(HazardConstants.SITE_ID, configManager.getSiteID());
        events.addAttribute(HazardConstants.BACKUP_SITEID,
                LocalizationManager.getInstance().getCurrentSite());
        events.addAttribute(HazardConstants.HAZARD_MODE, caveMode.toString());
        events.addAttribute(HazardConstants.RUN_MODE, caveModeStr);

        if (issue) {
            events.addAttribute(HazardConstants.ISSUE_FLAG, "True");
        } else {
            events.addAttribute(HazardConstants.ISSUE_FLAG, "False");
        }

        events.addAttribute(HazardConstants.VTEC_MODE, vtecMode);
        events.addAttribute("vtecTestMode", vtecTestMode);

        HashMap<String, String> sessionDict = new HashMap<>();
        sessionDict.put(HazardConstants.TEST_MODE, caveMode.toString());
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
            for (Entry<String, Serializable> entry : event.getHazardAttributes()
                    .entrySet()) {
                if (entry.getValue() instanceof Date) {
                    entry.setValue(((Date) entry.getValue()).getTime());
                }
            }

            /* Make an ArrayList of any polygon geometries we encounter. */
            /*
             * Later, if non-zero length, will make GeometryCollection with it.
             */
            Geometry geometryCollection = null;
            List<Geometry> polygonGeometries = new ArrayList<>();

            String headline = configManager.getHeadline(event);
            event.addHazardAttribute(HazardConstants.HEADLINE, headline);
            if (event.getHazardAttribute(
                    HazardConstants.FORECAST_POINT) != null) {
                event.addHazardAttribute(HazardConstants.GEO_TYPE,
                        HazardConstants.POINT_TYPE);
            } else {
                geometryCollection = event.getProductGeometry();

                for (int i = 0; i < geometryCollection
                        .getNumGeometries(); ++i) {
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

                } /* end loop over geometryCollection */

            } /* if not a polygon event type */
            event.removeHazardAttribute(HazardConstants.HAZARD_EVENT_TYPE);

            /*
             * Make descriptions of portions of counties if we have any polygon
             * geometries for this event.
             */
            HazardTypeEntry hazardTypeEntry = configManager.getHazardTypes()
                    .get(HazardEventUtilities.getHazardType(event));
            Set<String> ugcTypes = hazardTypeEntry.getUgcTypes();
            if (ugcTypes.contains(MAPDATA_COUNTY)
                    && polygonGeometries.size() > 0) {
                if (polygonGeometries.size() < geometryCollection
                        .getNumGeometries()) {
                    geometryCollection = new GeometryFactory()
                            .buildGeometry(polygonGeometries);
                }
                if (!configManager.getSiteID().equals(NATIONAL)) {
                    partsOfCounty.addPortionsDescriptionToEvent(
                            geometryCollection, event,
                            configManager.getSiteID());
                }
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
            event.removeHazardAttribute(HazardConstants.ISSUED);
            event.removeHazardAttribute(HazardConstants.HAZARD_EVENT_CATEGORY);

            /*
             * TODO: Remove this once the HAZARD_EVENT_SELECTED attribute has
             * been entirely done away with.
             */
            event.removeHazardAttribute(HAZARD_EVENT_SELECTED);

            events.add(event);
        } /* end loop over information.getProductEvents */
        return events;
    }

    private boolean areYouSure(String eventLabel) {
        boolean answer = messenger.getQuestionAnswerer()
                .getUserAnswerToQuestion(
                        "Are you sure you want to issue the following hazard event(s)?",
                        eventLabel, new String[] { "Issue", "Cancel" });
        if (answer) {
            if ((CAVEMode.getMode() == CAVEMode.OPERATIONAL)
                    && (SimulatedTime.getSystemTime().isRealTime() == false)) {
                answer = false;
                messenger.getWarner().warnUser("Operational Issue Hazard",
                        "Must be in real time to issue hazard.");
            }
        }
        if (!answer) {
            sessionManager.setIssueOngoing(false);
        }
        return answer;
    }

    private void disseminate(
            ProductGeneratorInformation productGeneratorInformation)
                    throws Exception {
        /*
         * Disseminate the products for operational mode before writing to
         * database
         */
        if (operationalMode) {
            sendProducts(productGeneratorInformation);
        }

        // Setup the primary keys for the productData Table
        ArrayList<String> eventIDs = null;
        String productInfo = productGeneratorInformation.getGeneratedProducts()
                .getProductInfo();
        Date issueTimeDate = null;
        String siteID = configManager.getSiteID();

        /*
         * For each product store an entry in the productData table.
         */
        for (IGeneratedProduct product : productGeneratorInformation
                .getGeneratedProducts()) {
            eventIDs = new ArrayList<>();
            Long issueTime = null;
            if (product.getEventSet().isEmpty() == false) {
                Iterator<IEvent> iterator = product.getEventSet().iterator();
                while (iterator.hasNext()) {
                    IEvent event = iterator.next();
                    if (event instanceof IHazardEvent) {
                        IHazardEvent hazardEvent = (IHazardEvent) event;
                        String eventID = hazardEvent.getEventID();
                        eventIDs.add(eventID);
                        Map<String, Serializable> attributes = hazardEvent
                                .getHazardAttributes();
                        // Issue time should be the same for all the events
                        issueTime = (Long) attributes
                                .get(HazardConstants.ISSUE_TIME);
                        if (issueTime == null) {
                            throw new Exception(
                                    "Hazard event must contain an issue time after product generation.");
                        }
                    }
                }
            } else {
                issueTime = (Long) product.getData()
                        .get(HazardConstants.ISSUE_TIME);
                if (issueTime == null) {
                    throw new Exception(
                            "Product must contain an issue time after product generation.");
                }
            }

            issueTimeDate = new Date(issueTime);
            ProductDataUtil.createOrUpdateProductData(caveModeStr, productInfo,
                    eventIDs, siteID, issueTimeDate, product.getData(),
                    product.getEditableEntries());
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
                for (IGeneratedProduct generatedProduct : orderGeneratedProducts(
                        productGeneratorInformation.getGeneratedProducts())) {
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

    private boolean checkForConflicts(IHazardEvent hazardEvent)
            throws HazardEventServiceException {
        boolean practice = (CAVEMode.OPERATIONAL.equals(caveMode) == false);
        InteroperabilityRequestServices services = InteroperabilityRequestServices
                .getServices(practice);
        boolean hasConflicts = services.hasConflicts(
                hazardEvent.getPhenomenon() + "."
                        + hazardEvent.getSignificance(),
                hazardEvent.getSiteID(), hazardEvent.getStartTime(),
                hazardEvent.getEndTime());

        return hasConflicts;
    }

    /**
     * Listens for the completion of product generation and notifies the event
     * bus.
     */
    private class JobListener
            implements IPythonJobListener<GeneratedProductList> {

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
                        if (result != null && result.isEmpty() == false) {
                            productGeneratorInformation
                                    .setGeneratedProducts(result);
                            productGeneratorInformation.getGeneratedProducts()
                                    .getEventSet().addAttribute(
                                            HazardConstants.ISSUE_FLAG, issue);

                            if (issue) {
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
                            if (result == null) {
                                productGeneratorInformation
                                        .setError(new Throwable(
                                                "GeneratedProduct result from generator is null."));
                                notificationSender
                                        .postNotification(new ProductFailed(
                                                productGeneratorInformation));
                            } else if (result.isEmpty()) {
                                messenger.getWarner().warnUser(
                                        "Product Generation Error",
                                        productGeneratorInformation
                                                .getProductGeneratorName()
                                                + " completed. No products generated.");
                                sessionManager.setIssueOngoing(false);
                                sessionManager.setPreviewOngoing(false);
                            }
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
                        notificationSender.postNotification(
                                new ProductFailed(productGeneratorInformation));
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
                productGenerationAuditor.addProductGeneratorInformation(
                        productGeneratorInformation);
            }
            productGenerationAuditManager.put(productGenerationTrackingID,
                    productGenerationAuditor);
        }

        synchronized (pgiMap) {
            pgiMap.clear();
            final AtomicInteger processed = new AtomicInteger(0);
            StringBuilder sb = new StringBuilder();
            for (ProductGeneratorInformation productGeneratorInformation : allMatchingProductGeneratorInformation) {
                buildProductGenLabel(productGeneratorInformation, sb);
            }
            for (ProductGeneratorInformation productGeneratorInformation : allMatchingProductGeneratorInformation) {
                boolean continueGeneration = generate(
                        productGeneratorInformation, issue, confirm,
                        sb.toString());
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
                                for (IGeneratedProduct key : orderGeneratedProducts(
                                        new ArrayList<>(pgiMap.keySet()))) {
                                    ProductGeneratorInformation productGeneratorInformation = pgiMap
                                            .get(key);
                                    if (!pgiSet.contains(
                                            productGeneratorInformation)) {
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
                                        notificationSender.postNotification(
                                                new ProductGenerated(
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
        ProductGenerationComplete productGenerationComplete = new ProductGenerationComplete(
                productGenerationAuditor.isIssue(),
                productGenerationAuditor.getGeneratedProducts());
        notificationSender.postNotificationAsync(productGenerationComplete);
    }

    /**
     * Take the ProductGeneratorInformation and creates a label to be used on
     * the Product Generation Confirmation dialog to identify what is being
     * issued.
     * 
     * @param productGeneratorInformation
     * @return String
     */
    private void buildProductGenLabel(
            ProductGeneratorInformation productGeneratorInformation,
            StringBuilder sb) {
        // Get all the events that will be issued and create a String to be
        // used on the confirmation dialog.
        if (productGeneratorInformation.getProductEvents() != null) {
            for (IHazardEvent hazardEvent : productGeneratorInformation
                    .getProductEvents()) {
                sb.append(hazardEvent.getDisplayEventID()).append(" ").append(
                        HazardEventUtilities.getHazardPhenSig(hazardEvent))
                        .append("\n");
            }
        }
    }

    @Override
    public void showUserProductViewerSelection(boolean correction,
            Collection<String> eventIdentifiers) {

        /*
         * Get the appropriate product data.
         */
        String mode = CAVEMode.getMode().toString();
        Date time = SimulatedTime.getSystemTime().getTime();
        List<ProductData> productData = (correction
                ? ProductDataUtil.retrieveCorrectableProductData(mode, time)
                : ((eventIdentifiers == null) || eventIdentifiers.isEmpty()
                        ? ProductDataUtil.retrieveViewableProductData(mode,
                                time)
                        : ProductDataUtil.retrieveViewableProductDataForEvents(
                                mode, time, eventIdentifiers)));
        messenger.getProductViewerChooser()
                .getProductViewerChooser(productData);
    }
}
