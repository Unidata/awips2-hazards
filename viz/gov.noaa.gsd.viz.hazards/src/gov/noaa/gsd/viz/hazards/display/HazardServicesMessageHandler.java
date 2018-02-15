/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.NATIONAL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.StringUtil;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFormats;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerationComplete;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerationConfirmation;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGeneratorInformation;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductStagingRequired;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.ISessionRecommenderManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTimeChanged;
import com.raytheon.viz.ui.perspectives.VizPerspectiveListener;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.display.action.CurrentSettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.ProductAction;
import gov.noaa.gsd.viz.hazards.display.action.ProductEditorAction;
import gov.noaa.gsd.viz.hazards.display.action.StaticSettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import net.engio.mbassy.listener.Handler;

/**
 * Description: Handles messages delegated from the message listener object.
 * These are typically messages received from the presenters.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 29, 2013            bryon.lawrence      Initial creation
 * Jun 24, 2013            bryon.lawrence      Removed the 'Move Entire Element'
 *                                             option from the right-click context
 *                                             menu.
 * Jul 19, 2013   1257     bsteffen            Notification support for session manager.
 * Jul 20, 2013    585     Chris.Golden        Changed to support loading from bundle,
 *                                             including making the model and JEP
 *                                             instances be member variables instead
 *                                             of class-scoped.
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Aug 06, 2013   1265     bryon.lawrence      Added support for undo/redo.
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Aug 21, 2013 1921       daniel.s.schaffer@noaa.gov  Call recommender framework directly
 * Aug 22, 2013    787     bryon.lawrence      Added method to find setting linked to
 *                                             the current CAVE perspective.
 * Aug 29, 2013 1921       bryon.lawrence      Modified to not pass JSON event id list
 *                                             to loadGeometryOverlayForSelectedEvent().
 * Aug 30, 2013 1921       bryon.lawrence      Added code to pass hazard events as a part of
 *                                             the EventSet passed to a recommender when it
 *                                             is run.
 * Oct 22, 2013 1463       bryon.lawrence      Added methods for hazard 
 *                                             conflict detection.
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Nov 20, 2013 2460    daniel.s.schaffer@noaa.gov  Reset now removing all events from practice table
 * Nov 16, 2013  2166       daniel.s.schaffer@noaa.gov    Tidying
 * Nov 27, 2013  1462      bryon.lawrence      Added methods to support display
 *                                             of hazard hatch areas.
 * nov 29, 2013  2378      bryon.lawrence     Cleaned up methods which support proposing and issuing hazards.
 * Dec 3, 2013   1472      bkowal              subtype field is now subType
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - eliminated IHazardsIF
 * Dec 08, 2013 2539       bryon.lawrence     Updated to ensure current time 
 *                                            indicate immediately reflects
 *                                            user changes to CAVE clock.
 * 
 * Dec 08, 2013 2155       bryon.lawrence     Removed logic in runTool which
 *                                            which seemed to be leading
 *                                            to an occasional race condition.
 * Dec 08, 2013 2375       bryon.lawrence     Added code to add updated hazard type to
 *                                            dynamic settings.
 * Feb 03, 2014 2155       Chris.Golden       Fixed bug that caused floating-
 *                                            point values to be interpreted
 *                                            as long integers when doing
 *                                            conversions to/from JSON.
 * Feb 07, 2014  2890      bkowal             Product Generation JSON refactor.
 * Feb 19, 2014  2915      bkowal             JSON settings re-factor
 * Feb 19, 2014  2161      Chris.Golden       Added ability to handle console action
 *                                            indicating that until further notice
 *                                            has changed for an event.
 * Apr 11, 2014  2819      Chris.Golden       Fixed bugs with the Preview and Issue
 *                                            buttons in the HID remaining grayed out
 *                                            when they should be enabled.
 * Apr 12, 2014  2925      Chris.Golden       Moved some business logic into the session
 *                                            manager, and altered to work with class-
 *                                            based metadata, as well as doing general
 *                                            clean-up.
 * May 15, 2014  2925      Chris.Golden       Minor changes to support new HID. Also
 *                                            changed instantiation of presenters to not
 *                                            include view as one of the constructor
 *                                            arguments, since the view is set post-
 *                                            construction now (to avoid having a view
 *                                            be initialized by the Presenter superclass
 *                                            before the subclass has finished being
 *                                            built).
 * Apr 23, 2014  1480      jsanchez           Handled reviewable products.
 * Aug 18, 2014  4243      Chris.Golden       Changed to pass recommender file path as
 *                                            opposed to a Python script when showing a
 *                                            dialog for a recommender.
 * Sep 09, 2014  4042      Chris.Golden       Moved product staging info generation to
 *                                            the product staging presenter.
 * Oct 02, 2014  4042      Chris.Golden       Changed to support two-step product
 *                                            staging dialog (first step allows user to
 *                                            select additional events to be included in
 *                                            products, second step allows the inputting
 *                                            of additional product-specific information
 *                                            using megawidgets). Also continued slow
 *                                            process of moving functionality from here
 *                                            into the session manager as appropriate.
 * Oct 02, 2014  4763      Dan Schaffer       Fixing bug in which issuing proposed hazards
 *                                            fails to change the state to issued.  Also
 *                                            discovered and fixed bug whereby the HID
 *                                            disappears when you propose a hazard.
 * Nov 18, 2014  4124      Chris.Golden       Changed to no longer have both selected
 *                                            time instant and selected time range; only
 *                                            one or the other is used. Also adapted to
 *                                            new time manager.
 * Dec 05, 2014  4124      Chris.Golden       Changed to work with newly parameterized
 *                                            config manager, and with ObservedSettings.
 * Dec 13, 2014  4959      Dan Schaffer       Spatial Display cleanup and other bug fixes
 * Jan 29, 2015  3626      Chris.Golden       Added ability to pass event type when running
 *                                            a recommender.
 * Jan 29, 2015  4375      Dan Schaffer       Console initiation of RVS product generation
 * Feb 03, 2015  3865      Chris.Cody         Check for valid Active Editor class
 * Feb 04, 2015  2331      Chris.Golden       Removed listener for time changes; these are
 *                                            now handled by individual presenters as
 *                                            necessary.
 * Feb 12, 2015 4959       Dan Schaffer       Modify MB3 add/remove UGCs to match Warngen
 * Feb 25, 2015 6600       Dan Schaffer       Fixed bug in spatial display centering
 * Mar 26, 2015 6940       Robert.Blum        Changed Conflicts dialog to say Forecast Zones
 *                                            instead of Areas.
 * Apr 08, 2015 7369       Robert.Blum        Added message box to notify users that a recommender
 *                                            has completed and return no hazard events.
 * Apr 10, 2015  6898       Chris.Cody        Refactored async messaging
 * May 07, 2015  6979      Robert.Blum        Added events to the productGeneratorInformation for 
 *                                            product corrections.
 * May 18, 2015  6898       Chris.Cody        Restored set visible types for recommender completion
 * May 20, 2015  8227      Chris.Cody         Remove NullRecommender
 * Jun 02, 2015  7138      Robert.Blum        Changes for RVS workflow.
 * Jun 24, 2015 6601       Chris.Cody         Change Create by Hazard Type display text
 * Jul 01, 2015 6726       Robert.Blum        Changes to be able to return to Product
 *                                            Editor from confirmation dialog.
 * Jul 30, 2015 9681       Robert.Blum        Changes for new Product Viewer and generating
 *                                            view only products.
 * Aug 03, 2015 8836       Chris.Cody         Update Event Display type when settings change
 * Sep 14, 2015 3473       Chris.Cody         Implement Hazard Services Import/Export through
 *                                            Central Registry server.
 * Sep 28, 2015 10302,8167 hansen             Re-wrote retrieveBackupSiteList to use get backup
 *                                               sites from startup config.
 * Nov 10, 2015 12762      Chris.Golden       Added support for use of new recommender manager,
 *                                            removing a bunch of recommender-managing code that
 *                                            didn't belong here and thus continuing the trend of
 *                                            shrinking this class.
 * Feb 24, 2016 13929      Robert.Blum        Remove first part of staging dialog.
 * Mar 16, 2016 15676      Chris.Golden       Removed obsolete notifications.
 * Apr 01, 2016 16225      Chris.Golden       Added ability to cancel tasks that are scheduled to
 *                                            run at regular intervals.
 * Jun 06, 2016 19432      Chris.Golden       Added ability to initiate drawing of lines and
 *                                            points.
 * Jun 23, 2016 19537      Chris.Golden       Removed storm-track-specific code.
 * Jul 25, 2016 19537      Chris.Golden       Removed a bunch of session-manager-manipulating
 *                                            code that belongs in the spatial presenter or the
 *                                            session manager, in the continuing quest to shrink
 *                                            this class down.
 * Aug 15, 2016 18376      Chris.Golden       Removed unsubscribing from the event bus when H.S.
 *                                            closes, as this is already being done in dispose().
 * Oct 20, 2016 23137      mduff              Check for errors before opening the Product Editor.
 * Jan 09, 2017 21504      Robert.Blum        Corrections now lock hazards.
 * Jan 27, 2017 22308      Robert.Blum        Removed code that is no longer needed.
 * Feb 01, 2017 15556      Chris.Golden       Removed obsolete code that was refactored out of
 *                                            relevance in the ongoing quest to shrink this class
 *                                            down to nothing. Also moved some code into the
 *                                            HazardServicesAppBuilder and presenters, as
 *                                            appropriate.
 * Feb 13, 2017 28892      Chris.Golden       Removed unneeded code.
 * Feb 24, 2017 29170      Robert.Blum        Ensure generationID is set when doing corrections.
 * Mar 21, 2017 29996      Robert.Blum        Removed un-needed parameter.
 * Apr 14, 2017 32733      Robert.Blum        Code clean up.
 * Apr 27, 2017 11853      Chris.Golden       Removed reset of preview-ongoing flag when closing
 *                                            the product editor, as this is now done by the
 *                                            latter's presenter.
 * Jun 08, 2017 16373      Chris.Golden       Corrected RUN_RECOMMENDER constant spelling.
 * Jun 30, 2017 19223      Chris.Golden       Changed to use new HazardConstants constant.
 * Aug 15, 2017 22757      Chris.Golden       Added ability for recommenders to specify either a
 *                                            message to display, or a dialog to display, with
 *                                            their results (that is, within the returned event
 *                                            set).
 * Sep 27, 2017 38072      Chris.Golden       Changed to use callback objects for the recommender
 *                                            manager.
 * Dec 17, 2017 20739      Chris.Golden       Refactored away access to directly mutable session
 *                                            events.
 * May 04, 2018 50032      Chris.Golden       Removed unneeded notifyModelChanged() call.
 * May 22, 2018  3782      Chris.Golden       Refactored tool dialog so that is closer to the MVP
 *                                            design guidelines.
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public final class HazardServicesMessageHandler {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesMessageHandler.class);

    // Private Variables

    /**
     * An instance of the Hazard Services app builder.
     */
    private HazardServicesAppBuilder appBuilder = null;

    private final ISessionManager<ObservedSettings> sessionManager;

    private final ISessionConfigurationManager<ObservedSettings> sessionConfigurationManager;

    private final BoundedReceptionEventBus<Object> eventBus;

    private final ISessionProductManager sessionProductManager;

    private final ISessionRecommenderManager sessionRecommenderManager;

    // Public Constructors
    /**
     * Construct a standard instance.
     * 
     * @param appBuilder
     *            A reference to the Hazard Services app builder
     * @param settings
     *            Settings with which to start; if <code>null</code>, the
     *            default settings for the current perspective are used.
     * 
     */
    public HazardServicesMessageHandler(HazardServicesAppBuilder appBuilder,
            ObservedSettings settings) {
        this.appBuilder = appBuilder;
        this.sessionManager = appBuilder.getSessionManager();
        this.sessionProductManager = sessionManager.getProductManager();
        this.sessionRecommenderManager = sessionManager.getRecommenderManager();
        this.sessionConfigurationManager = sessionManager
                .getConfigurationManager();
        this.eventBus = appBuilder.getEventBus();

        this.eventBus.subscribe(this);

        if (sessionConfigurationManager.getStartUpConfig().isNational()) {
            sessionConfigurationManager.setSiteID(NATIONAL, Originator.OTHER);
        } else {
            sessionConfigurationManager.setSiteID(
                    LocalizationManager.getInstance().getCurrentSite(),
                    Originator.OTHER);
        }

        /*
         * Use the settings that was passed in if one was provided, otherwise
         * use the one that is the default for this perspective.
         */
        String staticSettingID = (settings == null
                ? getSettingForCurrentPerspective() : settings.getSettingsID());

        sessionConfigurationManager.changeSettings(staticSettingID,
                Originator.OTHER);
    }

    // Methods

    @Handler
    public void handleProductGenerationCompletion(
            ProductGenerationComplete productGenerationComplete) {
        if (productGenerationComplete.isIssued() == false) {

            /*
             * Check for errors in generation from the formatters. Then check to
             * see if the Product(s) should be displayed in the editor or the
             * viewer.
             */
            List<GeneratedProductList> list = productGenerationComplete
                    .getGeneratedProducts();
            List<String> errorList = new ArrayList<>();
            if (!list.isEmpty()) {
                for (GeneratedProductList gpl : list) {
                    for (IGeneratedProduct prod : gpl) {
                        if (prod.getErrors() != null
                                && !prod.getErrors().isEmpty()) {
                            errorList.add(prod.getErrors());
                        }
                    }
                }
                GeneratedProductList productList = list.get(0);
            }

            if (!errorList.isEmpty()) {
                StringBuilder buffer = new StringBuilder();
                for (String e : errorList) {
                    buffer.append(e).append(StringUtil.NEWLINE);
                }
                appBuilder.getWarner().warnUser("Error Generating Product",
                        buffer.toString());
                sessionManager.setIssueOngoing(false);
                sessionManager.setPreviewOngoing(false);
            } else {
                appBuilder.showProductEditorView(list);
            }
        }
    }

    /**
     * Shuts down the Hazard Services session.
     */
    void dispose() {
        eventBus.unsubscribe(this);
    }

    /**
     * Generates products for preview.
     */
    private void preview() {
        sessionManager.generate(false);
    }

    /**
     * Changes the setting to the new setting identifier, as requested by the
     * settings dialog.
     * 
     * @param settingID
     *            New setting to be used.
     * @param originator
     *            Originator of the change.
     * @param eventsChanged
     *            Flag indicating whether or not hazard events have changed as
     *            part of this change.
     */
    private void changeSetting(String settingID, IOriginator originator,
            boolean eventsChanged) {

        sessionConfigurationManager.changeSettings(settingID, originator);

        appBuilder
                .notifyModelChanged(EnumSet.of(HazardConstants.Element.SETTINGS,
                        HazardConstants.Element.CURRENT_SETTINGS));

    }

    /**
     * TODO, this method will be moved once this class is refactored, but it
     * should be able to be moved quickly and easily
     */
    private void saveSetting() {
        sessionConfigurationManager.saveSettings();
    }

    private void saveAsSetting(String settingsId, IOriginator originator) {
        Settings settings = new Settings(
                sessionConfigurationManager.getSettings());
        String name = settingsId.replaceAll("\\P{Alnum}", "");
        settings.setSettingsID(name);
        settings.setStaticSettingsID(settingsId);
        settings.setDisplayName(settingsId);
        sessionConfigurationManager.getSettings().apply(settings, originator);
        sessionConfigurationManager.saveSettings();
        List<Settings> availableSettings = sessionConfigurationManager
                .getAvailableSettings();
        availableSettings.add(settings);
    }

    /**
     * Respond to the current setting having changed.
     * 
     * @param settings
     * @param originator
     */
    void changeCurrentSettings(ISettings settings, IOriginator originator) {
        sessionConfigurationManager.getSettings().apply(settings, originator);
        appBuilder.notifyModelChanged(
                EnumSet.of(HazardConstants.Element.CURRENT_SETTINGS));
    }

    /**
     * Changes the state of an event based on an action taken by the user in the
     * Product Display dialog.
     * 
     * @param action
     *            The action the user took on the event (probably needs to be an
     *            enumeration).
     */
    private void handleProductDisplayAction(ProductEditorAction action) {
        switch (action.getHazardAction()) {
        case ISSUE:
            if (appBuilder.shouldContinueIfThereAreHazardConflicts()) {
                sessionProductManager.setupForRunningFinalProductGen(
                        action.getGeneratedProductsList());
            } else {
                sessionManager.setIssueOngoing(false);
            }
            break;
        case CORRECT:
            for (GeneratedProductList products : action
                    .getGeneratedProductsList()) {
                ProductGeneratorInformation productGeneratorInformation = new ProductGeneratorInformation();
                productGeneratorInformation
                        .setGenerationID(UUID.randomUUID().toString());
                productGeneratorInformation
                        .setProductGeneratorName(products.getProductInfo());
                ProductFormats productFormats = sessionConfigurationManager
                        .getProductGeneratorTable()
                        .getProductFormats(productGeneratorInformation
                                .getProductGeneratorName());
                productGeneratorInformation.setProductFormats(productFormats);
                productGeneratorInformation.setGeneratedProducts(products);
                Set<IReadableHazardEvent> events = new HashSet<>(
                        products.getEventSet().size());
                for (IEvent event : products.getEventSet()) {
                    events.add((IReadableHazardEvent) event);
                }
                productGeneratorInformation.setProductEvents(events);
                sessionManager.getProductManager()
                        .issueCorrection(productGeneratorInformation);
            }

            break;
        default:
            // do nothing
        }
    }

    /**
     * Respond to the settings being modified.
     * 
     * @param change
     *            Change that occurred.
     * @deprecated This method will go away once the
     *             {@link HazardServicesAppBuilder} and this class are merged
     *             into an app controller, and <code>notifyModelChanged()</code>
     *             is no longer used anywhere.
     */
    @Handler(priority = 1)
    @Deprecated
    public void settingsModified(final SettingsModified change) {

        /*
         * Tell the app builder about the change. The method being invoked will
         * eventually be a @Handler itself, and will not need to be called here
         * anymore.
         */
        appBuilder.settingsModified(change);
    }

    /**
     * Respond to the selected time being modified.
     * 
     * @param change
     *            Change that occurred.
     * @deprecated This method will go away once the
     *             {@link HazardServicesAppBuilder} and this class are merged
     *             into an app controller, and <code>notifyModelChanged()</code>
     *             is no longer used anywhere.
     */
    @Handler(priority = 1)
    @Deprecated
    public void selectedTimeChanged(final SelectedTimeChanged change) {

        /*
         * Tell the app builder about the change. The method being invoked will
         * eventually be a @Handler itself, and will not need to be called here
         * anymore.
         */
        appBuilder.selectedTimeChanged(change);
    }

    /**
     * Handle a received product action. This method is called implicitly by the
     * event bus when actions of this type are sent across the latter.
     * 
     * @param productAction
     *            Action received.
     */
    @SuppressWarnings("unchecked")
    @Handler
    public void productActionOccurred(final ProductAction productAction) {
        switch (productAction.getActionType()) {
        case PREVIEW:

            /*
             * Called from the hazard detail and product editor dialog.
             */
            preview();
            break;

        case REVIEW:
            Map<String, Serializable> parameters = productAction
                    .getParameters();
            ArrayList<ProductData> productData = (ArrayList<ProductData>) parameters
                    .get(HazardConstants.PRODUCT_DATA_PARAM);
            Set<String> eventsToLock = new HashSet<>(productData.size(), 1.0f);
            for (ProductData data : productData) {
                eventsToLock.addAll(data.getEventIDs());
            }
            boolean success = sessionManager.getLockManager()
                    .lockHazardEventsForProductGeneration(eventsToLock);
            if (!success) {
                appBuilder.getWarner().warnUser("Failed to Lock Hazard",
                        "Failed to locked all the required hazards for the selected "
                                + "product(s). Product correction has been cancelled.");
                return;
            }
            sessionProductManager.generateProductsForCorrection(productData);

            break;

        default:
            throw new IllegalArgumentException(
                    "Unsupported actionType " + productAction.getActionType());
        }

    }

    /**
     * Handle a received product editor action. This method is called implicitly
     * by the event bus when actions of this type are sent across the latter.
     * 
     * @param productEditorAction
     *            Action received.
     */
    @Handler
    public void productEditorActionOccurred(
            final ProductEditorAction productEditorAction) {
        handleProductDisplayAction(productEditorAction);
    }

    /**
     * Handle a product staging required notification.
     * 
     * TODO: When the app builder is turned into an app controller during the
     * last stages of MVP refactoring, this handler should be within the app
     * controller. For now, it resides here with many other handlers that will
     * themselves either be removed or relocated.
     * 
     * @param notification
     *            Notification received.
     */
    @Handler
    public void productStagingRequired(
            final ProductStagingRequired notification) {
        appBuilder.showProductStagingView(notification.isIssue());
    }

    @Handler
    public void productGenerationConfirmation(
            final ProductGenerationConfirmation productGenerationConfirmation) {
        // Product Editor needs closed if issuing from it.
        this.appBuilder.closeProductEditor();
    }

    @Handler
    public void currentSettingsActionOccurred(
            final CurrentSettingsAction settingsAction) {
        changeCurrentSettings(settingsAction.getSettings(),
                settingsAction.getOriginator());
    }

    /**
     * Handle a received settings action. This method is called implicitly by
     * the event bus when actions of this type are sent across the latter.
     * 
     * @param settingsAction
     *            Action received.
     */
    @Handler
    public void settingsActionOccurred(
            final StaticSettingsAction settingsAction) {
        switch (settingsAction.getActionType()) {

        case SETTINGS_MODIFIED:
            changeCurrentSettings(settingsAction.getSettings(),
                    UIOriginator.SETTINGS_DIALOG);
            break;

        case SETTINGS_CHOSEN:
            changeSetting(settingsAction.getSettingID(),
                    UIOriginator.SETTINGS_MENU, true);
            break;
        case SAVE:
            saveSetting();
            break;
        case SAVE_AS:
            saveAsSetting(settingsAction.getSettings().getSettingsID(),
                    UIOriginator.SETTINGS_DIALOG);
            changeSetting(settingsAction.getSettingID(),
                    UIOriginator.SETTINGS_DIALOG, true);
            break;

        }
    }

    /**
     * Handle a received tool action. This method is called implicitly by the
     * event bus when actions of this type are sent across the latter.
     * <p>
     * TODO: When {@link ToolAction} is excised from the system as part of
     * refactoring in the future, most of the functionality below will be
     * handled by a call to {@link SessionManager.runTool(ToolType, String)}.
     * </p>
     * 
     * @param toolAction
     *            Action received.
     */
    @Handler
    public void toolActionOccurred(final ToolAction toolAction) {
        switch (toolAction.getToolType()) {
        case RECOMMENDER:
            switch (toolAction.getRecommenderActionType()) {
            case RUN_RECOMMENDER:
                sessionRecommenderManager.runRecommender(
                        toolAction.getToolName(), toolAction.getContext());
                break;

            case ENABLE_EVENT_DRIVEN_TOOLS:
                sessionManager.getConfigurationManager()
                        .setEventDrivenToolRunningEnabled(
                                toolAction.isEnabled());
                break;
            default:
                statusHandler.debug("Unrecognized tool action :"
                        + toolAction.getRecommenderActionType());
                break;
            }
            break;

        case HAZARD_PRODUCT_GENERATOR:
            sessionProductManager.generateProducts(toolAction.getToolName());
            break;

        case NON_HAZARD_PRODUCT_GENERATOR:
            sessionProductManager
                    .generateNonHazardProducts(toolAction.getToolName());
            break;

        default:
            statusHandler.debug(
                    "Unrecognized tool type :" + toolAction.getToolType());
            break;
        }

    }

    /**
     * Retrieves the setting for the current perspective. If this perspective is
     * not specified by any Setting, then this method defaults to the first
     * setting in the list of available settings.
     * 
     * @param
     * @return The setting identifier
     */
    private String getSettingForCurrentPerspective() {

        String perspectiveID = VizPerspectiveListener
                .getCurrentPerspectiveManager().getPerspectiveId();
        List<Settings> settingsList = sessionManager.getConfigurationManager()
                .getAvailableSettings();

        for (Settings settings : settingsList) {
            Set<String> settingPerspectiveList = settings.getPerspectiveIDs();

            if (settingPerspectiveList != null
                    && settingPerspectiveList.contains(perspectiveID)) {
                return settings.getSettingsID();
            }
        }

        /*
         * It might be better to create a default settings object. It would not
         * be represented in the Localization perspective. Rather, it would be
         * in memory. I'm assuming that there will always be settings
         * information available. Is that dangerous?
         */
        return settingsList.get(0).getSettingsID();
    }
}
