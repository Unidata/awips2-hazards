/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.EVENT_ID_DISPLAY_TYPE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_CHECKED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_END_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_FULL_TYPE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_IDENTIFIER;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_START_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.NATIONAL;
import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.contextmenu.ContextMenuHelper;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.display.action.CurrentSettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.display.action.HazardServicesCloseAction;
import gov.noaa.gsd.viz.hazards.display.action.ModifyStormTrackAction;
import gov.noaa.gsd.viz.hazards.display.action.ProductEditorAction;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;
import gov.noaa.gsd.viz.hazards.display.action.StaticSettingsAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.product.ReviewAction;
import gov.noaa.gsd.viz.hazards.servicebackup.ChangeSiteAction;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAction;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesMouseHandlers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.engio.mbassy.listener.Handler;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.GeometryType;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardServicesEventIdUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.SessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFormats;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerationConfirmation;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGeneratorInformation;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductStagingRequired;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.ISessionRecommenderManager;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTime;
import com.raytheon.viz.ui.perspectives.VizPerspectiveListener;

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
 * Mar 16, 2016 15676      Chris.Golden       Removed obsolete notifications.
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

    private final ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager;

    private final ISessionEventManager<ObservedHazardEvent> sessionEventManager;

    private final ISessionTimeManager sessionTimeManager;

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
     * @param currentTime
     *            The current time, based on the CAVE current time.
     * @param state
     *            Saved session state to initialize this session from the
     *            previous session.
     * 
     */
    public HazardServicesMessageHandler(HazardServicesAppBuilder appBuilder,
            Date currentTime) {
        this.appBuilder = appBuilder;
        this.sessionManager = appBuilder.getSessionManager();
        this.sessionProductManager = sessionManager.getProductManager();
        this.sessionRecommenderManager = sessionManager.getRecommenderManager();
        this.sessionEventManager = sessionManager.getEventManager();
        this.sessionTimeManager = sessionManager.getTimeManager();
        this.sessionConfigurationManager = sessionManager
                .getConfigurationManager();
        this.eventBus = appBuilder.getEventBus();

        this.eventBus.subscribe(this);

        if (sessionConfigurationManager.getStartUpConfig().isNational()) {
            sessionConfigurationManager.setSiteID(NATIONAL);
        } else {
            sessionConfigurationManager.setSiteID(LocalizationManager
                    .getInstance().getCurrentSite());
        }

        String staticSettingID = getSettingForCurrentPerspective();

        sessionConfigurationManager.changeSettings(staticSettingID,
                Originator.OTHER);
    }

    // Methods

    @Handler
    public void handleProductGenerationCompletion(
            IProductGenerationComplete productGenerationComplete) {
        if (productGenerationComplete.isIssued() == false) {

            /*
             * Check to see if the Product(s) should be displayed in the editor
             * or the viewer.
             */
            boolean viewOnly = false;
            List<GeneratedProductList> list = productGenerationComplete
                    .getGeneratedProducts();
            if (list.isEmpty() == false) {
                GeneratedProductList productList = list.get(0);
                viewOnly = productList.isViewOnly();
            }
            if (viewOnly) {
                appBuilder.showProductViewer(productGenerationComplete
                        .getGeneratedProducts());
            } else {
                appBuilder.showProductEditorView(productGenerationComplete
                        .getGeneratedProducts());
            }
        }
    }

    /**
     * Shuts down the Hazard Services session.
     */
    private void closeHazardServices() {
        appBuilder.dispose();
    }

    /**
     * This method is called when events are deleted. This operation can only be
     * performed on Pending or Proposed events.
     * 
     * @param eventIDs
     *            Identifiers of events to be deleted.
     */
    private void deleteEvent(Collection<ObservedHazardEvent> events) {
        statusHandler.debug("HazardServicesMessageHandler: deleteEvent: "
                + events);

        sessionEventManager.removeEvents(events, null);

        appBuilder.hideHazardDetail();
    }

    /**
     * Changes the state of the selected events to the state given by the state
     * parameter
     */
    private void changeSelectedEventsToProposedState(IOriginator originator) {

        Collection<ObservedHazardEvent> events = sessionEventManager
                .getSelectedEvents();

        for (ObservedHazardEvent event : events) {
            sessionEventManager.proposeEvent(event, originator);
        }

        appBuilder.closeProductEditorView();
    }

    /**
     * Issues the events upon user confirmation.
     */
    private void issueEvents() {
        if (continueIfThereAreHazardConflicts()) {
            sessionManager.generate(true);
        }
    }

    /**
     * This method is called when a storm track point is moved on the Spatial
     * Display
     * 
     * Appropriate adjustments are made to the event and then the Spatial
     * Display is re-drawn
     * 
     * @param action
     */

    @Handler
    public void handleStormTrackModification(ModifyStormTrackAction action) {
        sessionRecommenderManager.runRecommender(
                HazardConstants.MODIFY_STORM_TRACK_TOOL,
                RecommenderExecutionContext.getEmptyContext(),
                action.getParameters(), null);
    }

    /**
     * This method is called when the user clicks "Reset" on the Tool bar to
     * reset the hazards to the canned case for the given setting.
     * 
     * @param type
     *            Type of entities to reset. *
     */
    private void reset(String type) {
        sessionManager.reset();
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

        appBuilder.notifyModelChanged(EnumSet.of(
                HazardConstants.Element.SETTINGS,
                HazardConstants.Element.VISIBLE_TIME_DELTA,
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
     * Update the selected time range.
     * 
     * @param selectedTimeStart_ms
     *            Start of the selected time range, or -1 if no range exists.
     * @param selectedTimeEnd_ms
     *            End of the selected time range, or -1 if no range exists.
     */
    private void updateSelectedTimeRange(String selectedTimeStart_ms,
            String selectedTimeEnd_ms) {
        SelectedTime selectedRange = new SelectedTime(
                Long.valueOf(selectedTimeStart_ms),
                Long.valueOf(selectedTimeEnd_ms));
        sessionTimeManager.setSelectedTime(selectedRange, UIOriginator.CONSOLE);
        appBuilder.notifyModelChanged(
                EnumSet.of(HazardConstants.Element.SELECTED_TIME_RANGE),
                UIOriginator.CONSOLE);
    }

    /**
     * This method is called when the visible time range is changed in the
     * Temporal Window.
     */
    private void updateVisibleTimeRange(long startTime, long endTime) {
        TimeRange visibleRange = new TimeRange(startTime, endTime);
        sessionTimeManager.setVisibleTimeRange(visibleRange,
                UIOriginator.CONSOLE);
    }

    /**
     * Set the add to selected mode as specified.
     * 
     * @param state
     *            New state of the add-to-selected mode.
     */
    private void setAddToSelected(SpatialDisplayAction.ActionIdentifier state) {
        sessionConfigurationManager.getSettings().setAddToSelected(
                state.equals(SpatialDisplayAction.ActionIdentifier.ON));
    }

    /**
     * Set the add geometry to selected mode as specified.
     * 
     * @param state
     *            New state of the add-geometry-to-selected mode.
     * @return
     */
    private void setAddGeometryToSelected(
            SpatialDisplayAction.ActionIdentifier state) {
        sessionConfigurationManager.getSettings().setAddGeometryToSelected(
                state.equals(SpatialDisplayAction.ActionIdentifier.ON));

    }

    /**
     * Updates information for an event taking into consideration the
     * UIOriginator.
     * 
     * @param map
     *            The portions of the event which are being updated.
     * @param isUserInitiated
     *            Flag indicating whether or not the updated data are the result
     *            of a user-edit.
     * @return
     */
    private void updateEventData(Map<String, Serializable> map,
            boolean isUserInitiated, IOriginator originator) {
        _updateEventData(map, isUserInitiated, originator);
    }

    /**
     * Update event data. This event should no longer be required once we move
     * away from map representations of hazard events and use POJOs instead.
     * (Also, all of this code will be directly executed by the presenters
     * anyway when we change over to direct manipulation of the model by
     * presenters.)
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    private void _updateEventData(Map<String, Serializable> map,
            Boolean isUserInitiated, IOriginator originator) {
        ObservedHazardEvent oEvent = sessionEventManager
                .getEventById((String) map
                        .get(HazardConstants.HAZARD_EVENT_IDENTIFIER));
        if (oEvent == null) {
            return;
        }
        Date newStartTime = null, newEndTime = null;
        for (String key : map.keySet()) {
            if (HazardConstants.HAZARD_EVENT_IDENTIFIER.equals(key)) {
                ;
            } else if (HAZARD_EVENT_FULL_TYPE.equals(key)) {
                String fullType = (String) map.get(key);
                String[] phenSigSubType = HazardEventUtilities
                        .getHazardPhenSigSubType(fullType);
                sessionEventManager.setEventType(oEvent, phenSigSubType[0],
                        phenSigSubType[1], phenSigSubType[2], originator);
            } else if (HAZARD_EVENT_START_TIME.equals(key)) {
                newStartTime = new Date(((Number) map.get(key)).longValue());
            } else if (HAZARD_EVENT_END_TIME.equals(key)) {
                newEndTime = new Date(((Number) map.get(key)).longValue());
            } else if (map.get(key) instanceof Collection) {
                List<String> stringList = new ArrayList<>(
                        (Collection<String>) map.get(key));

                /*
                 * Do no pass data as arrays. It is better to pass them as
                 * lists. Using arrays causes problems. For instance, an event
                 * passed to the the Product Generation Framework will have its
                 * cta array converted to a Python list. When returned to the
                 * HMI, this Python list is converted to a Java list. So, arrays
                 * are not consistently handled. The type is not preserved.
                 */
                oEvent.addHazardAttribute(key, (Serializable) stringList);
            } else {
                Object primitive = map.get(key);
                if (primitive.getClass() == String.class) {
                    oEvent.addHazardAttribute(key, (String) primitive);
                } else if (primitive.getClass() == Boolean.class) {
                    oEvent.addHazardAttribute(key, (Boolean) primitive);
                } else if (primitive.getClass() == Float.class) {
                    oEvent.addHazardAttribute(key, (Float) primitive);
                } else if (primitive.getClass() == Double.class) {
                    oEvent.addHazardAttribute(key, (Double) primitive);
                } else if (primitive instanceof Number) {
                    Object currentVal = oEvent.getHazardAttribute(key);
                    if (currentVal instanceof Integer) {
                        oEvent.addHazardAttribute(key,
                                ((Number) primitive).intValue());
                    } else if (currentVal instanceof Long) {
                        oEvent.addHazardAttribute(key,
                                ((Number) primitive).longValue());
                    } else if (currentVal instanceof Date) {
                        oEvent.addHazardAttribute(key, new Date(
                                ((Number) primitive).longValue()));
                    }
                } else {
                    throw new UnsupportedOperationException(
                            "not implemented for key = "
                                    + key
                                    + " with value \""
                                    + primitive
                                    + "\" of type "
                                    + (primitive == null ? "null" : primitive
                                            .getClass().getSimpleName()));
                }
            }
        }

        /*
         * Set the start and end time atomically, since setting one before the
         * other could result in a rejection of the first of the two because the
         * resulting range in between the actions of setting them each would be
         * invalid.
         * 
         * TODO: When this method goes away and its functionality taken on by
         * the ConsolePresenter (i.e. when refactoring of the console happens to
         * bring it into line with the MVP architecture), a false result from
         * the setEventTimeRange() invocation should merely result in the
         * setting of the console's copy of the time range back to the original
         * value, without having to explicitly do so asynchronously, since that
         * will all be taken care of by the various IStateChanger
         * thread-boundary-crossing classes.
         */
        if ((newStartTime != null) || (newEndTime != null)) {
            if (newStartTime == null) {
                newStartTime = oEvent.getStartTime();
            }
            if (newEndTime == null) {
                newEndTime = oEvent.getEndTime();
            }
            if (sessionEventManager.setEventTimeRange(oEvent, newStartTime,
                    newEndTime, originator) == false) {
                VizApp.runAsync(new Runnable() {
                    @Override
                    public void run() {
                        appBuilder.notifyModelChanged(EnumSet
                                .of(HazardConstants.Element.EVENTS));
                    }
                });
            }
        }

        if (isUserInitiated) {
            oEvent.setModified(true);
        }

    }

    /**
     * Respond to the current setting having changed.
     * 
     * @param settings
     * @param originator
     */
    public void changeCurrentSettings(ISettings settings, IOriginator originator) {
        sessionConfigurationManager.getSettings().apply(settings, originator);
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.CURRENT_SETTINGS));
    }

    private void updateSite(String site) {
        sessionConfigurationManager.getSettings().getVisibleSites().add(site);
        appBuilder.notifyModelChanged(EnumSet.of(HazardConstants.Element.SITE));
    }

    /**
     * Generates products for preview.
     */
    private void preview() {
        sessionManager.generate(false);
    }

    /**
     * Handles the selection from the right click context menu on the Hazard
     * Services Spatial Display.
     * 
     * @param label
     *            The label of the selected menu item.
     */
    private void handleContextMenuSelection(String label) {
        if (label
                .equals(ContextMenuHelper.ContextMenuSelections.PROPOSE_ALL_SELECTED_HAZARDS
                        .getValue())) {
            changeSelectedEventsToProposedState(null);
        } else if (label.equals(HazardConstants.CONTEXT_MENU_DELETE_VERTEX)) {
            appBuilder.modifyShape(HazardServicesDrawingAction.DELETE_VERTEX);
        } else if (label.equals(HazardConstants.CONTEXT_MENU_ADD_VERTEX)) {
            appBuilder.modifyShape(HazardServicesDrawingAction.ADD_VERTEX);
        } else if (label
                .equals(ContextMenuHelper.ContextMenuSelections.DELETE_ALL_SELECTED_HAZARDS
                        .getValue())) {
            deleteEvent(sessionEventManager.getSelectedEvents());
        } else if (label
                .contains(HazardConstants.CONTEXT_MENU_HAZARD_INFORMATION_DIALOG)) {
            /*
             * Save off any changes the user has made in the HID. Otherwise,
             * this would be lost when selecting different events.
             */
            appBuilder.showHazardDetail();
        } else if (label
                .contains(ContextMenuHelper.ContextMenuSelections.REMOVE_POTENTIAL_HAZARDS
                        .getValue())) {
            removeEventsWithState(HazardConstants.HazardStatus.POTENTIAL
                    .getValue());
        } else if (label.equals(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES)) {
            appBuilder.loadGeometryOverlayForSelectedEvent();
        } else if (label.equals(HazardConstants.CONTEXT_MENU_SEND_TO_BACK)) {
            sessionEventManager
                    .sortEvents(SessionEventManager.SEND_SELECTED_BACK);
        } else if (label.equals(HazardConstants.CONTEXT_MENU_BRING_TO_FRONT)) {
            sessionEventManager
                    .sortEvents(SessionEventManager.SEND_SELECTED_FRONT);
        } else if (label
                .equals(HazardConstants.CONTEXT_MENU_HIGH_RESOLUTION_GEOMETRY_FOR_SELECTED_EVENTS)) {
            sessionEventManager
                    .setHighResolutionGeometriesVisibleForSelectedEvents();

        } else if (label
                .equals(HazardConstants.CONTEXT_MENU_LOW_RESOLUTION_GEOMETRY_FOR_SELECTED_EVENTS)) {
            sessionEventManager
                    .setLowResolutionGeometriesVisibleForSelectedEvents();

        } else if (label
                .equals(HazardConstants.CONTEXT_MENU_HIGH_RESOLUTION_GEOMETRY_FOR_CURRENT_EVENT)) {
            sessionEventManager
                    .setHighResolutionGeometryVisibleForCurrentEvent();

        } else if (label
                .equals(HazardConstants.CONTEXT_MENU_LOW_RESOLUTION_GEOMETRY_FOR_CURRENT_EVENT)) {
            sessionEventManager
                    .setLowResolutionGeometryVisibleForCurrentEvent();

        } else {
            throw new IllegalArgumentException("Unexpected label " + label);
        }
    }

    private void removeEventsWithState(String stateValue) {
        HazardStatus state = HazardStatus.valueOf(stateValue.toUpperCase());
        for (ObservedHazardEvent event : sessionEventManager
                .getEventsByStatus(state)) {
            sessionEventManager.removeEvent(event, null);
        }
    }

    /**
     * Changes the state of an event to "Issued".
     */
    private void setIssuedState() {
        issueEvents();
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
        case PROPOSE:
            changeSelectedEventsToProposedState(action.getOriginator());
            break;
        case ISSUE:
            if (this.continueIfThereAreHazardConflicts()) {

                // Check for Non Hazard Generator types
                ProductGeneratorTable pgTable = sessionManager
                        .getConfigurationManager().getProductGeneratorTable();

                boolean nonHazardGeneratorType = false;
                for (GeneratedProductList genProdList : action
                        .getGeneratedProductsList()) {
                    ProductGeneratorEntry pgEntry = pgTable.get(genProdList
                            .getProductInfo());
                    ToolType generatorType = pgEntry.getGeneratorType();
                    if (generatorType == ToolType.NON_HAZARD_PRODUCT_GENERATOR) {
                        nonHazardGeneratorType = true;
                        break;
                    }
                }

                if (nonHazardGeneratorType) {
                    sessionProductManager
                            .createProductsFromGeneratedProductList(true,
                                    action.getGeneratedProductsList());
                } else {
                    sessionProductManager.createProductsFromHazardEventSets(
                            true, action.getGeneratedProductsList());
                }
            } else {
                sessionManager.setIssueOngoing(false);
            }
            break;
        case CORRECT:
            for (GeneratedProductList products : action
                    .getGeneratedProductsList()) {
                ProductGeneratorInformation productGeneratorInformation = new ProductGeneratorInformation();
                productGeneratorInformation.setProductGeneratorName(products
                        .getProductInfo());
                ProductFormats productFormats = sessionConfigurationManager
                        .getProductGeneratorTable().getProductFormats(
                                productGeneratorInformation
                                        .getProductGeneratorName());
                productGeneratorInformation.setProductFormats(productFormats);
                productGeneratorInformation.setGeneratedProducts(products);
                Set<IHazardEvent> events = new HashSet<IHazardEvent>(products
                        .getEventSet().size());
                for (IEvent event : products.getEventSet()) {
                    events.add((IHazardEvent) event);
                }
                productGeneratorInformation.setProductEvents(events);
                sessionManager.getProductManager().issueCorrection(
                        productGeneratorInformation);
            }

            break;
        default:
            // do nothing
        }
    }

    /**
     * Handles an undo action from the Console.
     * 
     * @param
     * @return
     */
    private void handleUndoAction() {
        this.sessionManager.undo();
    }

    /**
     * Handles a redo action from the Console.
     * 
     * @param
     * @return
     */
    private void handleRedoAction() {
        this.sessionManager.redo();
    }

    /**
     * Update the visible time delta.
     */
    void updateConsoleVisibleTimeDelta() {
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.VISIBLE_TIME_DELTA));
    }

    /**
     * Request that a mouse handler be loaded.
     * 
     * @param mouseHandler
     *            Mouse handler to be loaded.
     * @param args
     *            Additional optional arguments.
     */
    private void requestMouseHandler(HazardServicesMouseHandlers mouseHandler,
            String... args) {
        appBuilder.requestMouseHandler(mouseHandler, args);
    }

    /**
     * Examines all hazards looking for potential conflicts.
     * 
     * @param
     * @return
     */
    private void checkHazardConflicts() {

        ISessionEventManager<ObservedHazardEvent> sessionEventManager = sessionManager
                .getEventManager();

        Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> conflictMap = sessionEventManager
                .getAllConflictingEvents();

        if (!conflictMap.isEmpty()) {
            launchConflictingHazardsDialog(conflictMap, false);
        }

    }

    /**
     * Toggles on/off automatic conflict checking.
     */
    private void toggleAutoCheckConflicts() {
        sessionManager.toggleAutoHazardChecking();
    }

    /**
     * Toggle on/off the display of hazard hatch areas.
     * 
     * @param
     * @return
     */
    private void toggleHatchedAreaDisplay() {
        sessionManager.toggleHatchedAreaDisplay();
    }

    private void exportApplicationSiteData() {
        String siteId = getSiteId();
        sessionManager.exportApplicationSiteData(siteId);
    }

    /**
     * Process a user request (ConsoleAction) to import Hazard Services
     * Localization for a list of backed up Site Id values.
     */
    private void importApplicationBackupSiteData() {
        List<String> backupSiteIdList = retrieveBackupSiteIdList(false);
        sessionManager.importApplicationBackupSiteData(backupSiteIdList);
    }

    /**
     * Examines all hazards looking for potential conflicts. Returns the user's
     * decision as to whether or not to continue with the conflicts.
     * 
     * @param
     * @return The user's decision to continue (true) or not (false) if there
     *         are existing
     */
    private Boolean continueIfThereAreHazardConflicts() {

        Boolean userResponse = true;

        ISessionEventManager<ObservedHazardEvent> sessionEventManager = sessionManager
                .getEventManager();

        Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> conflictMap = sessionEventManager
                .getAllConflictingEvents();

        if (!conflictMap.isEmpty()) {
            userResponse = launchConflictingHazardsDialog(conflictMap, true);
        }

        return userResponse;
    }

    /**
     * This will no longer be needed once presenters listen directly for session
     * events.
     */
    @Handler
    @Deprecated
    public void settingsModified(final SettingsModified notification) {
        appBuilder.notifyModelChanged(EnumSet
                .of(HazardConstants.Element.CURRENT_SETTINGS));
    }

    /**
     * Handle a received spatial display action. This method is called
     * implicitly by the event bus when actions of this type are sent across the
     * latter.
     * 
     * @param spatialDisplayAction
     *            Action received.
     */
    @Handler
    public void spatialDisplayActionOccurred(
            final SpatialDisplayAction spatialDisplayAction) {
        SpatialDisplayAction.ActionType actionType = spatialDisplayAction
                .getActionType();
        statusHandler.debug("SpatialDisplayActionOccurred actionType: "
                + actionType);
        switch (actionType) {
        case ADD_PENDING_TO_SELECTED:
            setAddToSelected(spatialDisplayAction.getActionIdentifier());
            break;

        case ADD_GEOMETRY_TO_SELECTED:
            setAddGeometryToSelected(spatialDisplayAction.getActionIdentifier());

        case DRAWING:
            switch (spatialDisplayAction.getActionIdentifier()) {

            case SELECT_EVENT:
                requestMouseHandler(HazardServicesMouseHandlers.SINGLE_SELECTION);
                break;

            case DRAW_POLYGON:
                // Activate the hazard drawing mouse handler.
                appBuilder.getSpatialPresenter().setEditInProgress(false);
                requestMouseHandler(HazardServicesMouseHandlers.VERTEX_DRAWING,
                        GeometryType.POLYGON.getValue());
                break;

            case EDIT_POLYGON:
                appBuilder.getSpatialPresenter().setEditInProgress(true);
                requestMouseHandler(HazardServicesMouseHandlers.VERTEX_DRAWING,
                        GeometryType.LINE.getValue());
                break;

            case DRAW_FREE_HAND_POLYGON:
                appBuilder.getSpatialPresenter().setEditInProgress(false);
                requestMouseHandler(
                        HazardServicesMouseHandlers.FREEHAND_DRAWING,
                        GeometryType.POLYGON.getValue());
                break;

            case EDIT_POLYGON_FREE_HAND:
                appBuilder.getSpatialPresenter().setEditInProgress(true);
                requestMouseHandler(
                        HazardServicesMouseHandlers.FREEHAND_DRAWING,
                        GeometryType.POLYGON.getValue());
                break;

            case SELECT_BY_AREA:
                String tableName = spatialDisplayAction.getMapsDbTableName();
                String displayName = spatialDisplayAction.getLegendName();
                requestMouseHandler(HazardServicesMouseHandlers.DRAW_BY_AREA,
                        tableName, displayName);
                break;

            default:
                break;

            }
            break;

        case CONTEXT_MENU_SELECTED:
            String label = spatialDisplayAction.getContextMenuLabel();
            handleContextMenuSelection(label);
            break;

        case DISPLAY_DISPOSED:
            eventBus.unsubscribe(this);
            closeHazardServices();
            break;

        case FRAME_CHANGED:
            appBuilder.sendFrameInformationToSessionManager();
            break;

        case RUN_TOOL:
            sessionRecommenderManager.runRecommender(
                    spatialDisplayAction.getToolName(),
                    RecommenderExecutionContext.getEmptyContext(),
                    spatialDisplayAction.getToolParameters(), null);
            break;

        case UPDATE_EVENT_METADATA:
            updateEventData(spatialDisplayAction.getToolParameters(), true,
                    spatialDisplayAction.getOriginator());
            break;

        case UNDO:
            handleUndoAction();
            break;

        case REDO:
            handleRedoAction();
            break;

        default:
            throw new UnsupportedOperationException(String.format(
                    "ActionType %s not handled", actionType));
        }

    }

    /**
     * Handle a received console display action. This method is called
     * implicitly by the event bus when actions of this type are sent across the
     * latter.
     * 
     * @param consoleAction
     *            Action received.
     */
    @Handler
    public void consoleActionOccurred(final ConsoleAction consoleAction) {
        switch (consoleAction.getActionType()) {
        case RESET:
            reset(consoleAction.getId());
            break;

        case CHANGE_MODE:
            if (consoleAction.getId().equals(ConsoleAction.CHECK_CONFLICTS)) {
                checkHazardConflicts();
            } else if (consoleAction.getId().equals(
                    ConsoleAction.AUTO_CHECK_CONFLICTS)) {
                toggleAutoCheckConflicts();
            } else if (consoleAction.getId().equals(
                    ConsoleAction.SHOW_HATCHED_AREA)) {
                toggleHatchedAreaDisplay();
            }
            break;

        case VISIBLE_TIME_RANGE_CHANGED:

            long startTime = Long.parseLong(consoleAction.getStartTime());
            long endTime = Long.parseLong(consoleAction.getEndTime());
            updateVisibleTimeRange(startTime, endTime);
            break;

        case SELECTED_TIME_RANGE_CHANGED:
            updateSelectedTimeRange(consoleAction.getStartTime(),
                    consoleAction.getEndTime());
            break;

        case CHECK_BOX: {
            Map<String, Serializable> eventInfo = new HashMap<>();
            eventInfo.put(HAZARD_EVENT_IDENTIFIER, consoleAction.getId());
            eventInfo.put(HAZARD_EVENT_CHECKED, consoleAction.getChecked());
            updateEventData(eventInfo, true, consoleAction.getOriginator());

            break;
        }

        case SELECTED_EVENTS_CHANGED:

            List<String> selectedEventIDs = Lists.newArrayList(consoleAction
                    .getSelectedEventIDs());
            sessionEventManager.setSelectedEventForIDs(selectedEventIDs,
                    UIOriginator.CONSOLE);

            break;

        case EVENT_TIME_RANGE_CHANGED: {
            Map<String, Serializable> eventInfo = new HashMap<>();
            eventInfo.put(HAZARD_EVENT_IDENTIFIER, consoleAction.getId());
            eventInfo.put(HAZARD_EVENT_START_TIME,
                    Long.parseLong(consoleAction.getStartTime()));
            eventInfo.put(HAZARD_EVENT_END_TIME,
                    Long.parseLong(consoleAction.getEndTime()));
            updateEventData(eventInfo, true, consoleAction.getOriginator());
            break;
        }

        case EVENT_END_TIME_UNTIL_FURTHER_NOTICE_CHANGED: {
            Map<String, Serializable> eventInfo = new HashMap<>();
            eventInfo.put(HAZARD_EVENT_IDENTIFIER, consoleAction.getId());
            eventInfo.put(HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE,
                    consoleAction.getChecked());
            updateEventData(eventInfo, true, consoleAction.getOriginator());
            break;
        }

        case SITE_CHANGED:
            updateSite(consoleAction.getId());
            break;

        case CLOSE:
            eventBus.unsubscribe(this);
            closeHazardServices();
            break;

        case RUN_AUTOMATED_TESTS:
        case RUN_PRODUCT_GENERATION_TESTS:
            /*
             * Nothing to do here
             */
            break;

        case SITE_DATA_OPERATION:

            if (consoleAction.getId().equals(ConsoleAction.EXPORT_SITE_CONFIG)) {
                exportApplicationSiteData();
            } else if (consoleAction.getId().equals(
                    ConsoleAction.IMPORT_SITE_CONFIG)) {

                importApplicationBackupSiteData();
            }
            break;
        default:
            throw new IllegalArgumentException("Unexpected action type "
                    + consoleAction.getActionType());
        }

    }

    /**
     * Handle a received hazard detail action. This method is called implicitly
     * by the event bus when actions of this type are sent across the latter.
     * 
     * @param hazardDetailAction
     *            Action received.
     */
    @Handler
    public void hazardDetailActionOccurred(
            final HazardDetailAction hazardDetailAction) {
        switch (hazardDetailAction.getActionType()) {
        case PREVIEW:
            preview();
            break;

        case PROPOSE:
            changeSelectedEventsToProposedState(hazardDetailAction
                    .getOriginator());
            break;

        case ISSUE:
            setIssuedState();
            break;

        case REVIEW:
            Map<String, Serializable> parameters = hazardDetailAction
                    .getParameters();
            ArrayList<ProductData> productData = (ArrayList<ProductData>) parameters
                    .get(ReviewAction.PRODUCT_DATA_PARAM);
            sessionProductManager.generateProductFromProductData(productData,
                    true, false);
            break;
        case VIEW:
            Map<String, Serializable> params = hazardDetailAction
                    .getParameters();
            ArrayList<ProductData> viewableProductData = (ArrayList<ProductData>) params
                    .get(ReviewAction.PRODUCT_DATA_PARAM);
            sessionProductManager.generateProductFromProductData(
                    viewableProductData, false, true);
            break;
        default:
            throw new IllegalArgumentException("Unsupported actionType "
                    + hazardDetailAction.getActionType());
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
    public void productStagingRequired(final ProductStagingRequired notification) {
        appBuilder.showProductStagingView(notification.isIssue(),
                notification.getStagingRequired());
    }

    @Handler
    public void productGenerationConfirmation(
            final ProductGenerationConfirmation productGenerationConfirmation) {
        // Product Editor needs closed if issuing from it.
        this.sessionManager.setPreviewOngoing(false);
        this.appBuilder.closeProductEditorView();
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
    public void settingsActionOccurred(final StaticSettingsAction settingsAction) {
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
            case RUN_RECOMENDER:
                sessionRecommenderManager.runRecommender(
                        toolAction.getToolName(), toolAction.getContext());
                break;

            case RUN_RECOMMENDER_WITH_PARAMETERS:
                sessionRecommenderManager.runRecommender(
                        toolAction.getToolName(), toolAction.getContext(),
                        null, toolAction.getAuxiliaryDetails());
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
            sessionProductManager.generateNonHazardProducts(toolAction
                    .getToolName());
            break;

        default:
            statusHandler.debug("Unrecognized tool type :"
                    + toolAction.getToolType());
            break;
        }

    }

    @Handler
    public void changeSiteOccurred(ChangeSiteAction action) {
        sessionManager.getConfigurationManager().setSiteID(action.getSite());
        ObservedSettings currentSetting = sessionManager
                .getConfigurationManager().getSettings();
        Set<String> visibleSites = currentSetting.getVisibleSites();
        visibleSites.add(action.getSite());
        currentSetting.setVisibleSites(visibleSites);
        ConsoleAction cAction = new ConsoleAction(
                ConsoleAction.ActionType.SITE_CHANGED);
        cAction.setId(action.getSite());
        consoleActionOccurred(cAction);
    }

    /**
     * Handle a received shut down action.
     * 
     * @param closeAction
     *            The Hazard Services shutdown notification.
     */
    @Handler
    public void closeActionOccurred(final HazardServicesCloseAction closeAction) {
        eventBus.unsubscribe(this);
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

    /**
     * Launches a dialog displaying conflicting hazards. It is up to the user as
     * to whether or not to fix them.
     * 
     * @param conflictingHazardMap
     *            A map of hazards and hazards which conflict with them.
     * @param requiresConfirmation
     *            Indicates whether or not this dialog should require user
     *            confirmation (Yes or No).
     * @return The return value from the dialog based on the user's selection.
     */
    private Boolean launchConflictingHazardsDialog(
            final Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> conflictingHazardMap,
            final Boolean requiresConfirmation) {

        Boolean userSelection = true;

        if (!conflictingHazardMap.isEmpty()) {
            StringBuffer message = new StringBuffer(
                    "Conflicting Hazards: The following hazard conflicts exist: ");

            if (requiresConfirmation) {
                message.append("Continue?\n");
            } else {
                message.append("\n");
            }

            for (IHazardEvent hazardEvent : conflictingHazardMap.keySet()) {

                String phenSig = HazardEventUtilities
                        .getHazardType(hazardEvent);
                message.append("Event ID:" + hazardEvent.getEventID() + "("
                        + phenSig + ") Conflicts With: ");

                Map<IHazardEvent, Collection<String>> conflictingHazards = conflictingHazardMap
                        .get(hazardEvent);

                for (IHazardEvent conflictingHazard : conflictingHazards
                        .keySet()) {
                    String conflictingPhenSig = HazardEventUtilities
                            .getHazardType(conflictingHazard);
                    message.append("Event ID:" + conflictingHazard.getEventID()
                            + "(" + conflictingPhenSig + ") ");

                    Collection<String> conflictingAreas = conflictingHazards
                            .get(conflictingHazard);

                    /*
                     * TODO - Future work to be done under RM 7306. The below
                     * label needs to be updated based on the ugcType of the
                     * hazard. It could be a county, forecast zone, or fire wx
                     * zone.
                     */
                    if (!conflictingAreas.isEmpty()) {
                        message.append("\n\tForecast Zones:");

                        for (String area : conflictingAreas) {
                            message.append(" " + area);
                        }
                    }

                }

                message.append("\n");
            }

            if (requiresConfirmation) {
                userSelection = appBuilder.getUserAnswerToQuestion(message
                        .toString());

            } else {
                appBuilder.warnUser("Conflicting Hazards", message.toString());
            }
        }

        return userSelection;
    }

    /**
     * Update Hazard Event Id Display Type on Settings change.
     * 
     * This method should be run before refreshing Console and Spatial displays.
     * 
     */
    private void reloadHazardServidesEventId() {
        ISettings currentSettings = sessionManager.getConfigurationManager()
                .getSettings();
        String eventIdDisplayTypeString = sessionManager
                .getConfigurationManager().getSettingsValue(
                        EVENT_ID_DISPLAY_TYPE, currentSettings);

        if ((eventIdDisplayTypeString != null)
                && (eventIdDisplayTypeString.isEmpty() == false)) {
            HazardServicesEventIdUtil
                    .setIdDisplayType(HazardServicesEventIdUtil.IdDisplayType
                            .valueOf(eventIdDisplayTypeString));
        }
    }

    /**
     * Return the currently configured Site Id value.
     * 
     * @return 3 character Site Id
     */
    private String getSiteId() {
        return sessionConfigurationManager.getSiteID();
    }

    /**
     * Return the list of Hazard Services Backed Up Site Id values.
     * 
     * Backed Up Site Id values are pulled from Localization:
     * cave_static/.../hazardServces/backupSites.xml The "..." could be "base"
     * or "site/<SITE_ID>, or user based.
     * 
     * @param includeLocalSite
     *            boolean flag. When True; will include the current Site Id
     * @param defaultToBase
     *            boolean flag. When True and no Site specific values are found
     *            that are configured for the current site; it will use the
     *            backupSites.xml values from the base file.
     * @return
     */
    private List<String> retrieveBackupSiteIdList(boolean includeLocalSite) {

        List<String> backupSiteIdList = Arrays.asList(sessionManager
                .getConfigurationManager().getStartUpConfig().getBackupSites());

        if (includeLocalSite == true) {
            String site = getSiteId();
            backupSiteIdList.add(site);
        }
        if ((backupSiteIdList == null) || (backupSiteIdList.isEmpty())) {
            statusHandler
                    .warn("No configured Hazard Services Back up Site Id values found in StartUpConfig.");
        }

        return backupSiteIdList;
    }
}
