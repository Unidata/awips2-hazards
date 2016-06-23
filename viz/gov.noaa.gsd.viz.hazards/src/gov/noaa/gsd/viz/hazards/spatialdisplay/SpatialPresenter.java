/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.common.visuals.BorderStyle;
import gov.noaa.gsd.common.visuals.IIdentifierGenerator;
import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.common.visuals.VisualFeature;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesText;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.PointDrawingAttributes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers.MouseHandlerFactory;
import gov.noaa.gsd.viz.hazards.utilities.HazardEventBuilder;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.engio.mbassy.listener.Enveloped;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.subscription.MessageEnvelope;

import org.apache.commons.lang.time.DateUtils;

import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.GeometryType;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.configuration.types.HatchingStyle;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.InvalidGeometryException;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventGeometryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventMetadataModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTimeRangeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTypeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventVisualFeaturesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionHatchingToggled;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTime;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * Spatial presenter, used to mediate between the model and the spatial view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jul 12, 2013     585    Chris.Golden      Changed to support loading from bundle,
 *                                           including the passing in of the event
 *                                           bus so that the latter is no longer a
 *                                           singleton.
 * Aug 6, 2013     1265    Bryon.Lawrence    Added support for undo/redo.
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Nov 23, 2013    1462    bryon.lawrence    Added support for drawing hazard
 *                                           hatched areas.
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - eliminated IHazardsIF
 * May 17, 2014 2925       Chris.Golden      Added newly required implementation of
 *                                           reinitialize(), and made initialize()
 *                                           protected as it is called by setView().
 * Aug 28, 2014 2532       Robert.Blum       Commented out zooming when settings are
 *                                           changed.
 * Nov 18, 2014  4124      Chris.Golden      Adapted to new time manager.
 * Dec 05, 2014  4124      Chris.Golden      Changed to work with newly parameterized
 *                                           config manager.
 * Dec 13, 2014 4959       Dan Schaffer      Spatial Display cleanup and other bug fixes
 * Jan  7, 2015 4959       Dan Schaffer      Ability to right click to add/remove UGCs from hazards
 * Feb 12, 2015 4959       Dan Schaffer      Modify MB3 add/remove UGCs to match Warngen
 * Feb 27, 2015 6000       Dan Schaffer      Improved centering behavior
 * Apr 10, 2015 6898       Chris.Cody        Removed modelChanged legacy messaging method
 * May 15, 2015 7935       Chris.Cody        Fixed NPE caused by immediate HS close after opening
 * May 19, 2015 4781       Robert.Blum       Removed filtering of pending hazards they should always
 *                                           be visible on the spatial display.
 * Jun 02, 2015 8500       Chris.Cody        Single click does not select HE on Spatial Display
 * Jun 19, 2015 6760       Robert.Blum       Added SettingsModified handler to update the Spatial
 *                                           Display when settings are changed.
 * Jul 21, 2015 2921       Robert.Blum       Added null check to spatialDisplay to prevent null pointer.
 * Mar 06, 2016 15676      Chris.Golden      Initial support for visual features, and basic cleanup.
 * Mar 22, 2016 15676      Chris.Golden      Added ability to use all border styles for visual features,
 *                                           and for now, have visual features and base geometries both
 *                                           fully recreated each time any sort of refresh is needed.
 *                                           Ugly and brute-force, but pretty much how it was done up
 *                                           until now with base geometries anyway. Future refactor
 *                                           will definitely make this more efficient.
 * Mar 26, 2016 15676      Chris.Golden      Added check of Geometry objects' validity, and also method
 *                                           to allow the presenter to be notified of changes made by
 *                                           the user to visual features.
 * Mar 29, 2016 15676      Chris.Golden      Fixed bug inherited from H.S. operational causing pending
 *                                           events to never be filtered out of the spatial display
 *                                           due to their time ranges not intersecting with the current
 *                                           selected time.
 * Apr 18, 2016 15676      Chris.Golden      Fixed bug with selected time not being rounded down to
 *                                           closest minute, which prevented visual features from
 *                                           showing up.
 * May 03, 2016 15676      Chris.Golden      Fixed another bug with selected time not being rounded
 *                                           down to the closest minute when time-matching caused it
 *                                           to not lie on a minute boundary.
 * Jun 06, 2016 19432      Chris.Golden      Added ability to draw lines and points.
 * Jun 10, 2016 19537      Chris.Golden      Combined base and selected visual feature lists for each
 *                                           hazard event into one, replaced by visibility constraints
 *                                           based upon selection state to individual visual features.
 * Jun 23, 2016 19537      Chris.Golden      Removed storm-track-specific code. Also added hatching for
 *                                           hazard events that have visual features, and added ability
 *                                           to use visual features to request spatial info for
 *                                           recommenders. Also added use of new visual feature
 *                                           properties (topmost and symbol shape), and ability to use
 *                                           "as event" labeling with visual features.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SpatialPresenter extends
        HazardServicesPresenter<ISpatialView<?, ?>> implements IOriginator {

    // Public Enumerated Types

    /**
     * Position of a point within the sequence of one or more points that are in
     * the process of being created.
     */
    public enum SequencePosition {
        FIRST, INTERIOR, LAST
    };

    private static final VisualFeaturesList EMPTY_VISUAL_FEATURES = new VisualFeaturesList();

    /**
     * Status handler for logging.
     */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    /**
     * Geometry factory.
     */
    private final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * Mouse handler factory.
     */
    private MouseHandlerFactory mouseFactory = null;

    /**
     * Set of the currently selected events.
     */
    private final Set<String> selectedEventIDs = new HashSet<>();

    /**
     * Hazard event builder.
     */
    private final HazardEventBuilder hazardEventBuilder;

    private boolean editInProgress = false;

    private ToolType spatialInfoCollectingToolType;

    private String spatialInfoCollectingToolIdentifier;

    private RecommenderExecutionContext spatialInfoCollectingToolContext;

    private VisualFeaturesList toolVisualFeatures = EMPTY_VISUAL_FEATURES;

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            Spatial view to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public SpatialPresenter(
            ISessionManager<ObservedHazardEvent, ObservedSettings> model,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
        this.hazardEventBuilder = new HazardEventBuilder(model);
    }

    // Public Methods

    /**
     * Receive notification of a model change.
     * 
     * @param changes
     *            Set of elements within the model that have changed.
     */
    @Override
    public void modelChanged(EnumSet<HazardConstants.Element> changed) {
        if (changed.contains(HazardConstants.Element.CURRENT_SETTINGS)
                || changed.contains(HazardConstants.Element.SETTINGS)) {

            ObservedSettings settings = getModel().getConfigurationManager()
                    .getSettings();
            getView().setSettings(settings);
            updateAllBaseGeometryDisplayables();
            updateAllVisualFeatureDisplayables();
        } else if (changed
                .contains(HazardConstants.Element.SELECTED_TIME_RANGE)) {
            updateSelectedTime();
            updateAllBaseGeometryDisplayables();
            updateAllVisualFeatureDisplayables();
        }
    }

    @Handler
    @Enveloped(messages = { SessionEventAttributesModified.class,
            SessionEventMetadataModified.class })
    public void sessionEventAttributesModified(MessageEnvelope notification) {
        updateAllBaseGeometryDisplayables();
        updateAllVisualFeatureDisplayables();
    }

    @Handler
    public void sessionSelectedEventsModified(
            SessionSelectedEventsModified notification) {
        updateSelectedEvents();
        updateAllBaseGeometryDisplayables();
        updateAllVisualFeatureDisplayables();
        recenterZoom();
    }

    @Handler
    public void sessionEventTimeRangeModified(
            SessionEventTimeRangeModified notification) {
        updateAllBaseGeometryDisplayables();
        updateAllVisualFeatureDisplayables();
    }

    @Handler
    public void sessionEventTypeModified(SessionEventTypeModified notification) {
        updateAllBaseGeometryDisplayables();
        updateAllVisualFeatureDisplayables();
    }

    @Handler
    public void sessionEventGeometryModified(
            SessionEventGeometryModified notification) {
        updateAllBaseGeometryDisplayables();
        updateAllVisualFeatureDisplayables();
    }

    @Handler
    public void sessionEventVisualFeaturesModified(
            SessionEventVisualFeaturesModified notification) {
        updateAllBaseGeometryDisplayables();
        updateAllVisualFeatureDisplayables();
    }

    @Handler
    public void sessionEventStatusModified(
            SessionEventStatusModified notification) {
        updateAllBaseGeometryDisplayables();
        updateAllVisualFeatureDisplayables();
    }

    @Handler
    public void sessionEventRemoved(SessionEventRemoved notification) {
        updateAllBaseGeometryDisplayables();
        updateAllVisualFeatureDisplayables();
    }

    @Handler
    public void sessionHatchingToggled(SessionHatchingToggled notification) {
        updateAllBaseGeometryDisplayables();
        updateAllVisualFeatureDisplayables();
    }

    /**
     * Handle a change in the selected settings.
     * 
     * @param settingsModified
     *            settingsModified that occurred.
     */
    @Handler
    public void settingsModifiedOccurred(final SettingsModified settingsModified) {
        updateAllBaseGeometryDisplayables();
        updateAllVisualFeatureDisplayables();
    }

    @Handler
    public void sessionEventAdded(SessionEventAdded notification) {
        updateAllBaseGeometryDisplayables();
        updateAllVisualFeatureDisplayables();
    }

    public void updateDisplayables() {
        updateAllBaseGeometryDisplayables();
        updateAllVisualFeatureDisplayables();
    }

    public void setToolVisualFeatures(ToolType toolType, String toolIdentifier,
            RecommenderExecutionContext context,
            VisualFeaturesList visualFeatures) {
        spatialInfoCollectingToolType = toolType;
        spatialInfoCollectingToolIdentifier = toolIdentifier;
        spatialInfoCollectingToolContext = context;
        visualFeatures = (visualFeatures == null ? EMPTY_VISUAL_FEATURES
                : visualFeatures);
        if (toolVisualFeatures.equals(visualFeatures) == false) {
            toolVisualFeatures = visualFeatures;
            updateDisplayables();
        }
    }

    /**
     * Handle user modification of the geometry of the specified hazard event's
     * specified visual feature.
     * 
     * @param identifier
     *            Identifier of the visual feature.
     * @param selectedTime
     *            Selected time for which the geometry is to be changed.
     * @param newGeometry
     *            New geometry to be used by the visual feature.
     */
    public void handleUserModificationOfVisualFeature(
            VisualFeatureSpatialIdentifier identifier, Date selectedTime,
            Geometry newGeometry) {

        /*
         * Modify the visual feature that has had its geometry changed. If the
         * visual feature that was modified was created by a tool to collect
         * spatial information, run the tool with the modified visual feature.
         * Otherwise, find the event that goes with the visual feature and give
         * it the new version.
         */
        if (identifier instanceof ToolVisualFeatureSpatialIdentifier) {

            /*
             * Ensure the visual feature's associated tool is the same as the
             * one that the current tool visual features are for.
             */
            ToolVisualFeatureSpatialIdentifier toolIdentifier = (ToolVisualFeatureSpatialIdentifier) identifier;
            if (toolIdentifier.getToolIdentifier().equals(
                    spatialInfoCollectingToolIdentifier)) {

                /*
                 * Find and modify the visual feature, then run the recommender
                 * with the modified visual feature list.
                 */
                VisualFeature feature = toolVisualFeatures
                        .getByIdentifier(identifier
                                .getVisualFeatureIdentifier());
                if (feature != null) {
                    feature.setGeometry(
                            DateUtils.truncate(selectedTime, Calendar.MINUTE),
                            newGeometry);
                    toolVisualFeatures.replace(feature);
                    getModel().getRecommenderManager().runRecommender(
                            spatialInfoCollectingToolIdentifier,
                            spatialInfoCollectingToolContext,
                            toolVisualFeatures, null);
                }
            }

            /*
             * Remove the visual features from the display.
             */
            setToolVisualFeatures(null, null, null, null);
        } else {

            /*
             * Find the event that goes with the visual feature.
             */
            HazardVisualFeatureSpatialIdentifier hazardIdentifier = (HazardVisualFeatureSpatialIdentifier) identifier;
            ISessionEventManager<ObservedHazardEvent> eventManager = getModel()
                    .getEventManager();
            ObservedHazardEvent event = eventManager
                    .getEventById(hazardIdentifier.getEventIdentifier());
            if (event != null) {

                /*
                 * If a visual feature is found, set its geometry to that
                 * specified. Round the selected time down to the nearest
                 * minute, since it could be anything (i.e. not necessarily on a
                 * minute boundary).
                 */
                VisualFeature feature = event.getVisualFeature(hazardIdentifier
                        .getVisualFeatureIdentifier());
                if (feature != null) {
                    feature.setGeometry(
                            DateUtils.truncate(selectedTime, Calendar.MINUTE),
                            newGeometry);
                    event.setVisualFeature(feature,
                            UIOriginator.SPATIAL_DISPLAY);
                }
            }
        }
    }

    /**
     * Update the event areas drawn in the spatial view.
     * <p>
     * TODO: This is horribly brute-force. A finer-grained approach must be
     * taken when refactoring the spatial presenter/view code to ensure that
     * only those events that have changed in some way meaningful to their
     * display have their displayables changed.
     * </p>
     */
    private void updateAllBaseGeometryDisplayables() {

        /*
         * Ensure the spatial view and session manager are around, since upon
         * perspective switch, this method may be called when they have been
         * deleted.
         */
        ISpatialView<?, ?> spatialView = getSpatialView();
        if (spatialView == null) {
            return;
        }
        ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager = getModel();
        if (sessionManager == null) {
            return;
        }

        spatialView.setUndoEnabled(sessionManager.isUndoable());
        spatialView.setRedoEnabled(sessionManager.isRedoable());

        ISessionEventManager<ObservedHazardEvent> eventManager = sessionManager
                .getEventManager();
        Collection<ObservedHazardEvent> events = eventManager
                .getCheckedEvents();
        ISessionTimeManager timeManager = sessionManager.getTimeManager();

        SelectedTime selectedTime = timeManager.getSelectedTime();
        filterEventsForTime(events, selectedTime);

        Map<String, Boolean> eventEditability = new HashMap<>();
        Set<String> hatchedEventIdentifiers = new HashSet<>();
        boolean hatching = sessionManager.areHatchedAreasDisplayed();
        HazardTypes hazardTypes = sessionManager.getConfigurationManager()
                .getHazardTypes();

        for (ObservedHazardEvent event : events) {
            String eventID = event.getEventID();
            eventEditability.put(event.getEventID(),
                    eventManager.canEventAreaBeChanged(event));
            if (hatching) {
                String hazardType = HazardEventUtilities.getHazardType(event);
                if (hazardType != null) {
                    HazardTypeEntry hazardTypeEntry = hazardTypes
                            .get(hazardType);
                    if (hazardTypeEntry.getHatchingStyle() != HatchingStyle.NONE) {
                        hatchedEventIdentifiers.add(eventID);
                    }
                }
            }
        }

        /*
         * TODO It might be possible to optimize here by checking if the events
         * have changed before displaying.
         */
        spatialView.drawEvents(events, eventEditability,
                hatchedEventIdentifiers);

    }

    /**
     * Update all the visual features' representations in the view.
     * <p>
     * TODO: This is horribly brute-force. A finer-grained approach must be
     * taken when refactoring the spatial presenter/view code to ensure that
     * only those events that have changed in some way meaningful to their
     * display have their displayables changed.
     * </p>
     */
    private void updateAllVisualFeatureDisplayables() {

        /*
         * Ensure the spatial view and session manager are around, since upon
         * perspective switch, this method may be called when they have been
         * deleted.
         */
        ISpatialView<?, ?> spatialView = getSpatialView();
        if (spatialView == null) {
            return;
        }
        ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager = getModel();
        if (sessionManager == null) {
            return;
        }

        /*
         * Iterate through the hazard events, compiling lists of spatial
         * entities from all events that have visual features that are visible
         * at the current selected time.
         */
        List<SpatialEntity<VisualFeatureSpatialIdentifier>> unselectedSpatialEntities = new ArrayList<>();
        List<SpatialEntity<VisualFeatureSpatialIdentifier>> selectedSpatialEntities = new ArrayList<>();
        Date selectedTime = new Date(sessionManager.getTimeManager()
                .getSelectedTime().getLowerBound());
        ISessionConfigurationManager<ObservedSettings> configManager = sessionManager
                .getConfigurationManager();
        double hazardSinglePointTextOffsetLength = 10.0;
        double hazardSinglePointTextOffsetDirection = 90.0;
        double hazardMultiPointTextOffsetLength = 0.0;
        double hazardMultiPointTextOffsetDirection = 0.0;
        int hazardTextPointSize = HazardServicesText.FONT_SIZE;
        for (ObservedHazardEvent event : sessionManager.getEventManager()
                .getCheckedEvents()) {

            /*
             * Get the visual features list; if it is found to be non-empty,
             * compile the various display properties that will be needed by any
             * visual feature that indicates it must mimic the look of a base
             * geometry for this hazard event in one or more ways. Then iterate
             * through the visual features, compiling lists of their spatial
             * entities as appropriate to the selected time.
             */
            VisualFeaturesList visualFeaturesList = event.getVisualFeatures();
            if ((visualFeaturesList != null)
                    && (visualFeaturesList.isEmpty() == false)) {
                final String eventId = event.getEventID();
                boolean selected = selectedEventIDs.contains(eventId);
                Color hazardColor = configManager.getColor(event);
                double hazardBorderWidth = configManager.getBorderWidth(event,
                        selected);
                LineStyle lineStyle = configManager.getBorderStyle(event);
                BorderStyle hazardBorderStyle = (lineStyle == LineStyle.DOTTED ? BorderStyle.DOTTED
                        : (lineStyle == LineStyle.DASHED ? BorderStyle.DASHED
                                : BorderStyle.SOLID));
                double hazardPointDiameter = (selected ? PointDrawingAttributes.OUTER_SELECTED_DIAMETER
                        : PointDrawingAttributes.OUTER_DIAMETER);
                String hazardLabel = HazardServicesDrawingAttributes
                        .getHazardLabel(event);

                /*
                 * Iterate through the visual features, creating spatial
                 * entities for any that are visible at the selected time. Any
                 * such created spatial entities are added to either the list
                 * for unselected hazard events or selected hazard events,
                 * depending upon whether the event with which the entity is
                 * associated is selected or not.
                 */
                addSpatialEntitiesForVisualFeatures(visualFeaturesList,
                        (selected ? selectedSpatialEntities
                                : unselectedSpatialEntities), eventId,
                        selected, selectedTime, hazardColor, hazardBorderWidth,
                        hazardBorderStyle, hazardPointDiameter, hazardLabel,
                        hazardSinglePointTextOffsetLength,
                        hazardSinglePointTextOffsetDirection,
                        hazardMultiPointTextOffsetLength,
                        hazardMultiPointTextOffsetDirection,
                        hazardTextPointSize);
            }
        }

        /*
         * If any spatial-information-collecting visual features are currently
         * in existence for a tool, iterate through them, creating spatial
         * entities for any that are visible at the selected time, and add them
         * to the list of selected spatial entities.
         */
        if (toolVisualFeatures.isEmpty() == false) {
            addSpatialEntitiesForVisualFeatures(toolVisualFeatures,
                    selectedSpatialEntities, spatialInfoCollectingToolType,
                    spatialInfoCollectingToolIdentifier, selectedTime);
        }

        /*
         * Concatenate the lists together into one, and have the view draw them.
         */
        unselectedSpatialEntities.addAll(selectedSpatialEntities);
        getView().drawSpatialEntities(unselectedSpatialEntities,
                selectedEventIDs);
    }

    private ISpatialView<?, ?> getSpatialView() {
        ISpatialView<?, ?> spatialView = getView();
        if (spatialView == null || spatialView.getSpatialDisplay() == null) {
            return null;
        }
        return spatialView;
    }

    private void addSpatialEntitiesForVisualFeatures(
            VisualFeaturesList visualFeaturesList,
            List<SpatialEntity<VisualFeatureSpatialIdentifier>> spatialEntities,
            final String eventId, boolean selected, Date selectedTime,
            Color hazardColor, double hazardBorderWidth,
            BorderStyle hazardBorderStyle, double hazardPointDiameter,
            String hazardLabel, double hazardSinglePointTextOffsetLength,
            double hazardSinglePointTextOffsetDirection,
            double hazardMultiPointTextOffsetLength,
            double hazardMultiPointTextOffsetDirection, int hazardTextPointSize) {

        /*
         * Round the selected time down to the nearest minute.
         */
        selectedTime = DateUtils.truncate(selectedTime, Calendar.MINUTE);

        /*
         * If there are visual features in the list, iterate through them,
         * creating spatial entities for any that are visible at the selected
         * time.
         */
        if (visualFeaturesList != null) {

            /*
             * Put together the identifier generator for the spatial entities
             * that may be created.
             */
            IIdentifierGenerator<VisualFeatureSpatialIdentifier> identifierGenerator = new IIdentifierGenerator<VisualFeatureSpatialIdentifier>() {

                @Override
                public VisualFeatureSpatialIdentifier generate(String base) {
                    return new HazardVisualFeatureSpatialIdentifier(eventId,
                            base);
                }
            };

            /*
             * Iterate through the visual features, adding spatial entities
             * generated for each in turn to the provided list. Any spatial
             * entities that are marked as "topmost" in the z-ordering are added
             * to another list.
             */
            List<SpatialEntity<VisualFeatureSpatialIdentifier>> topmostSpatialEntities = null;
            for (VisualFeature visualFeature : visualFeaturesList) {
                SpatialEntity<VisualFeatureSpatialIdentifier> entity = visualFeature
                        .getStateAtTime(null, identifierGenerator, selected,
                                selectedTime, hazardColor, hazardBorderWidth,
                                hazardBorderStyle, hazardPointDiameter,
                                hazardLabel, hazardSinglePointTextOffsetLength,
                                hazardSinglePointTextOffsetDirection,
                                hazardMultiPointTextOffsetLength,
                                hazardMultiPointTextOffsetDirection,
                                hazardTextPointSize);
                if (entity != null) {
                    if (entity.isTopmost()) {
                        if (topmostSpatialEntities == null) {
                            topmostSpatialEntities = new ArrayList<>();
                        }
                        topmostSpatialEntities.add(entity);
                    } else {
                        spatialEntities.add(entity);
                    }
                }
            }

            /*
             * If at least one topmost spatial entity was generated, append all
             * topmost ones to the main list so that the former end up drawn
             * last, and thus staying on the top of the z-order.
             */
            if (topmostSpatialEntities != null) {
                spatialEntities.addAll(topmostSpatialEntities);
            }
        }
    }

    private void addSpatialEntitiesForVisualFeatures(
            VisualFeaturesList visualFeaturesList,
            List<SpatialEntity<VisualFeatureSpatialIdentifier>> spatialEntities,
            final ToolType toolType, final String toolIdentifier,
            Date selectedTime) {

        /*
         * Round the selected time down to the nearest minute.
         */
        selectedTime = DateUtils.truncate(selectedTime, Calendar.MINUTE);

        /*
         * If there are visual features in the list, iterate through them,
         * creating spatial entities for any that are visible at the selected
         * time.
         */
        if (visualFeaturesList != null) {

            /*
             * Put together the identifier generator for the spatial entities
             * that may be created.
             */
            IIdentifierGenerator<VisualFeatureSpatialIdentifier> identifierGenerator = new IIdentifierGenerator<VisualFeatureSpatialIdentifier>() {

                @Override
                public VisualFeatureSpatialIdentifier generate(String base) {
                    return new ToolVisualFeatureSpatialIdentifier(toolType,
                            toolIdentifier, base);
                }
            };

            /*
             * Iterate through the visual features, adding spatial entities
             * generated for each in turn to the provided list. Any spatial
             * entities that are marked as "topmost" in the z-ordering are added
             * to another list.
             */
            List<SpatialEntity<VisualFeatureSpatialIdentifier>> topmostSpatialEntities = null;
            for (VisualFeature visualFeature : visualFeaturesList) {
                SpatialEntity<VisualFeatureSpatialIdentifier> entity = visualFeature
                        .getStateAtTime(null, identifierGenerator, selectedTime);
                if (entity != null) {
                    if (entity.isTopmost()) {
                        if (topmostSpatialEntities == null) {
                            topmostSpatialEntities = new ArrayList<>();
                        }
                        topmostSpatialEntities.add(entity);
                    } else {
                        spatialEntities.add(entity);
                    }
                }
            }

            /*
             * If at least one topmost spatial entity was generated, append all
             * topmost ones to the main list so that the former end up drawn
             * last, and thus staying on the top of the z-order.
             */
            if (topmostSpatialEntities != null) {
                spatialEntities.addAll(topmostSpatialEntities);
            }
        }
    }

    private void recenterZoom() {
        ISessionEventManager<ObservedHazardEvent> eventManager = getModel()
                .getEventManager();
        List<ObservedHazardEvent> selectedEvents = eventManager
                .getSelectedEvents();
        if (!selectedEvents.isEmpty()) {
            List<Geometry> geometriesOfSelected = new ArrayList<>();
            for (ObservedHazardEvent selectedEvent : selectedEvents) {
                geometriesOfSelected.add(selectedEvent.getGeometry());
            }
            Geometry[] asArray = new Geometry[geometriesOfSelected.size()];
            GeometryCollection gc = geometryFactory
                    .createGeometryCollection(geometriesOfSelected
                            .toArray(asArray));
            Point center = gc.getCentroid();
            Coordinate[] hullCoordinates = gc.convexHull().getCoordinates();
            getView().recenterRezoomDisplay(hullCoordinates,
                    center.getCoordinate());
        }
    }

    private void updateSelectedEvents() {
        selectedEventIDs.clear();
        Collection<ObservedHazardEvent> events = getModel().getEventManager()
                .getSelectedEvents();
        for (ObservedHazardEvent hazardEvent : events) {
            selectedEventIDs.add(hazardEvent.getEventID());
        }
        getView().setEditEventGeometryEnabled(selectedEventIDs.size() == 1);
    }

    /**
     * Updates the time.
     */
    public void updateSelectedTime() {
        getView().manageViewFrames(getSelectedTime());
    }

    /**
     * Get the selected time.
     * 
     * @return Selected time.
     */
    private Date getSelectedTime() {
        return new Date(getModel().getTimeManager().getSelectedTime()
                .getLowerBound());
    }

    @Override
    protected void initialize(ISpatialView<?, ?> view) {
        if (mouseFactory == null) {
            mouseFactory = new MouseHandlerFactory(this);
        }
        getView().initialize(this, mouseFactory);
        updateAllBaseGeometryDisplayables();
        updateAllVisualFeatureDisplayables();
    }

    @Override
    protected void reinitialize(ISpatialView<?, ?> view) {

        /*
         * No action.
         */
    }

    /**
     * Convenience method for testing if the event is contained within the
     * selected time range.
     * 
     * @param
     * @return true if overlap
     */
    private boolean doesEventOverlapSelectedTime(IHazardEvent event,
            SelectedTime selectedRange) {
        return selectedRange.intersects(event.getStartTime().getTime(), event
                .getEndTime().getTime());
    }

    public void handleSelection(String clickedEventId, boolean multipleSelection) {

        /*
         * If this is a multiple selection operation, then check if the user is
         * selecting something that was already selected and treat this as a
         * deselect; otherwise, if this is a multiple selection operation, treat
         * it as a select; and finally, if a single selection, clear the
         * selected set and select only the clicked event.
         */
        if (multipleSelection && selectedEventIDs.contains(clickedEventId)) {
            selectedEventIDs.remove(clickedEventId);
        } else if (multipleSelection) {
            selectedEventIDs.add(clickedEventId);
        } else {
            selectedEventIDs.clear();
            selectedEventIDs.add(clickedEventId);
        }
        updateSelectedEventIds(selectedEventIDs);
    }

    /**
     * When an event is selected on the spatial display, the model is updated.
     * 
     * @param eventIDs
     *            The identifiers of the events selected in the spatial display.
     */
    public void updateSelectedEventIds(Collection<String> eventIDs) {
        getModel().getEventManager().setSelectedEventForIDs(eventIDs,
                UIOriginator.SPATIAL_DISPLAY);
    }

    public void zoneSelected(Coordinate location) {
        getModel().getEventManager().addOrRemoveEnclosingUGCs(location);
    }

    /**
     * Create a point shape using the specified location.
     * 
     * @param loc
     *            Location at which to place the point shape.
     * @param sequencePosition
     *            Position of the point in the sequence of one or more points
     *            being created.
     */
    public void createPointShape(Coordinate loc,
            SequencePosition sequencePosition) {
        IHazardEvent hazardEvent = hazardEventBuilder
                .buildPointHazardEvent(loc);
        hazardEventBuilder.addEvent(hazardEvent, this);
        if (sequencePosition == SequencePosition.FIRST) {
            getModel().getEventManager().setAddCreatedEventsToSelected(true);
        } else if (sequencePosition == SequencePosition.LAST) {
            getModel().getEventManager().setAddCreatedEventsToSelected(false);
        }
    }

    public void drawingActionComplete(GeometryType shapeType,
            List<Coordinate> points) {
        getView().drawingActionComplete();
        if (editInProgress) {
            updateHazardEventFromCollectedPoints(points);
            editInProgress = false;
        } else if (points != null) {
            buildHazardEventFromCollectedPoints(shapeType, points);
        }
    }

    /**
     * Edit the currently selected hazard geometry with the collected points.
     * Note that the direction in which the user draws the replacement points
     * matter. It is assumed they are drawing the replacement points in the same
     * direction as the original polygon. For select by area and as recommended
     * by the recommenders, this direction is clockwise.
     */
    private void updateHazardEventFromCollectedPoints(List<Coordinate> points) {
        ISessionEventManager<?> sessionEventManager = getModel()
                .getEventManager();
        IHazardEvent hazardEvent = sessionEventManager.getSelectedEvents().get(
                0);
        GeometryCollection gc = (GeometryCollection) hazardEvent.getGeometry();
        GeometryCollection polygonCollection = extractPolygons(gc);
        Coordinate[] origCoordinatesAsArray = polygonCollection
                .getCoordinates();
        List<Coordinate> origCoordinates = new ArrayList<>(
                Arrays.asList(origCoordinatesAsArray));
        Utilities.removeDuplicateLastPointAsNecessary(origCoordinates);
        int indexOfFirstPointToRemove = indexOfClosestPoint(origCoordinates,
                points.get(0));
        int indexOfLastPointToRemove = indexOfClosestPoint(origCoordinates,
                points.get(points.size() - 1));

        List<Coordinate> newCoordinates = new ArrayList<>();

        if (indexOfFirstPointToRemove <= indexOfLastPointToRemove) {
            for (int i = 0; i < indexOfFirstPointToRemove; i++) {
                newCoordinates.add(origCoordinates.get(i));
            }
            for (int i = 0; i < points.size(); i++) {
                newCoordinates.add(points.get(i));
            }

            for (int i = indexOfLastPointToRemove + 1; i < origCoordinates
                    .size(); i++) {
                newCoordinates.add(origCoordinates.get(i));
            }
        } else {
            /*
             * This deals with the case when the user chooses a section to
             * replace that bounds the first point of the original polygon (i.e.
             * the replacement section is crossing over an edge condition).
             */
            for (int i = 0; i < points.size(); i++) {
                newCoordinates.add(points.get(i));
            }
            for (int i = indexOfLastPointToRemove + 1; i < indexOfFirstPointToRemove; i++) {
                newCoordinates.add(origCoordinates.get(i));
            }
        }
        /*
         * Only modify the geometry if the result is a polygon.
         * 
         * TODO - Should we put up a status message here?
         */
        if (newCoordinates.size() >= 3) {
            Utilities.closeCoordinatesIfNecessary(newCoordinates);
            Geometry newGeometry = hazardEventBuilder
                    .geometryFromCoordinates(newCoordinates);
            if (getView().getSpatialDisplay()
                    .checkGeometryValidity(newGeometry)) {
                sessionEventManager.setModifiedEventGeometry(
                        hazardEvent.getEventID(), newGeometry);
            }
        }

    }

    /**
     * The River Flood Recommender can generate a collection that includes a
     * point and an inundation polygon. We don't want the editing to get fouled
     * up by the point so skip it.
     * 
     * @param gc
     * @return The polygons of the collection
     */
    private GeometryCollection extractPolygons(GeometryCollection gc) {
        List<Geometry> polygons = new ArrayList<>();
        for (int i = 0; i < gc.getNumGeometries(); i++) {
            Geometry g = gc.getGeometryN(i);
            if (gc.getGeometryN(i).getClass() != Point.class) {
                polygons.add(g);
            }
        }
        GeometryCollection polygonCollection = new GeometryCollection(
                polygons.toArray(new Geometry[0]), geometryFactory);
        return polygonCollection;
    }

    private int indexOfClosestPoint(List<Coordinate> origCoordinates,
            Coordinate coordinate) {
        int result = 0;
        double minDistance = coordinate.distance(origCoordinates.get(0));
        for (int i = 1; i < origCoordinates.size(); i++) {
            double distance = coordinate.distance(origCoordinates.get(i));
            if (distance < minDistance) {
                result = i;
                minDistance = distance;
            }
        }
        return result;
    }

    /**
     * Build a {@link IHazardEvent} with the points that have been collected.
     */
    private void buildHazardEventFromCollectedPoints(GeometryType shapeType,
            List<Coordinate> points) {
        try {

            /*
             * Do nothing if user hasn't drawn enough points to create a path or
             * polygon.
             */
            if (points.size() < (shapeType == GeometryType.LINE ? 2 : 3)) {
                return;
            }

            /*
             * Simplify the number of points in the path or polygon. This will
             * eventually need to be user-configurable.
             */
            IHazardEvent hazardEvent = null;
            if (shapeType == GeometryType.POLYGON) {
                Utilities.closeCoordinatesIfNecessary(points);

                LinearRing linearRing = geometryFactory.createLinearRing(points
                        .toArray(new Coordinate[0]));

                Geometry polygon = geometryFactory.createPolygon(linearRing,
                        null);
                Geometry reducedGeometry = TopologyPreservingSimplifier
                        .simplify(polygon, 0.0001);
                hazardEvent = hazardEventBuilder
                        .buildPolygonHazardEvent(reducedGeometry
                                .getCoordinates());
            } else {
                LineString lineString = geometryFactory.createLineString(points
                        .toArray(new Coordinate[0]));
                Geometry reducedGeometry = TopologyPreservingSimplifier
                        .simplify(lineString, 0.0001);
                hazardEvent = hazardEventBuilder
                        .buildLineHazardEvent(reducedGeometry.getCoordinates());
            }

            /*
             * Create the event for the path or polygon.
             */
            hazardEventBuilder.addEvent(hazardEvent, this);
        } catch (InvalidGeometryException e) {
            statusHandler.handle(Priority.WARN, "Error drawing vertex "
                    + (shapeType == GeometryType.LINE ? "path" : "polygon")
                    + ": " + e.getMessage(), e);
        }
    }

    private void filterEventsForTime(Collection<ObservedHazardEvent> events,
            SelectedTime selectedTime) {
        Iterator<ObservedHazardEvent> it = events.iterator();
        while (it.hasNext()) {
            IHazardEvent event = it.next();
            if (doesEventOverlapSelectedTime(event, selectedTime) == false) {
                it.remove();
            }
        }
    }

    /**
     * Set the flag indicating whether or not an edit of a shape is currently in
     * progress.
     * 
     * @param editInProgress
     *            New value of the flag.
     */
    public void setEditInProgress(boolean editInProgress) {
        this.editInProgress = editInProgress;
    }

}
