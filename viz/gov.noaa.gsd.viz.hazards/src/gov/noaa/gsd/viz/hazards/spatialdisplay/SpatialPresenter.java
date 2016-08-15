/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CONTEXT_MENU_BRING_TO_FRONT;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CONTEXT_MENU_HIGH_RESOLUTION_GEOMETRY_FOR_CURRENT_EVENT;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CONTEXT_MENU_HIGH_RESOLUTION_GEOMETRY_FOR_SELECTED_EVENTS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CONTEXT_MENU_LOW_RESOLUTION_GEOMETRY_FOR_CURRENT_EVENT;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CONTEXT_MENU_LOW_RESOLUTION_GEOMETRY_FOR_SELECTED_EVENTS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.CONTEXT_MENU_SEND_TO_BACK;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.LOW_RESOLUTION_GEOMETRY_IS_VISIBLE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.VISIBLE_GEOMETRY;
import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.common.visuals.VisualFeature;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.contextmenu.ContextMenuHelper;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.HazardEventVisualFeatureEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IHazardEventEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.ToolVisualFeatureEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaContext;
import gov.noaa.gsd.viz.hazards.utilities.HazardEventBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.engio.mbassy.listener.Enveloped;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.subscription.MessageEnvelope;

import org.apache.commons.lang.time.DateUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.configuration.types.HatchingStyle;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
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
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.SessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTimeChanged;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

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
 * Jul 25, 2016 19537      Chris.Golden      Extensively refactored as the move toward MVP compliance
 *                                           continues. Added Javadoc comments, continued separation of
 *                                           concerns between view, presenter, display, and mouse
 *                                           handlers.
 * Aug 15, 2016  18376     Chris.Golden      Added code to make garbage collection of the session
 *                                           manager and app builder more likely.
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

    // Private Enumerated Types

    /**
     * Geometry resolutions.
     */
    private enum GeometryResolution {
        LOW, HIGH
    };

    // Private Static Constants

    /**
     * Empty visual features list.
     */
    private static final VisualFeaturesList EMPTY_VISUAL_FEATURES = new VisualFeaturesList();

    // Private Variables

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
     * Spatial entity manager.
     */
    private final SpatialEntityManager spatialEntityManager;

    /**
     * Identifiers of currently selected events.
     */
    private final Set<String> selectedEventIdentifiers = new HashSet<>();

    /**
     * Hazard event builder.
     */
    private final HazardEventBuilder hazardEventBuilder;

    /**
     * Type of the tool attempting to collect information via the visual
     * features within {@link #toolVisualFeatures}; if the latter is empty, this
     * is irrelevant.
     */
    private ToolType spatialInfoCollectingToolType;

    /**
     * Identifier of the tool attempting to collect information via the visual
     * features within {@link #toolVisualFeatures}; if the latter is empty, this
     * is irrelevant.
     */
    private String spatialInfoCollectingToolIdentifier;

    /**
     * Execution context of the tool attempting to collect information via the
     * visual features within {@link #toolVisualFeatures}; if the latter is
     * empty, this is irrelevant.
     */
    private RecommenderExecutionContext spatialInfoCollectingToolContext;

    /**
     * Visual features being used by a tool to collect spatial information; if
     * empty, no tool is currently doing so.
     */
    private VisualFeaturesList toolVisualFeatures = EMPTY_VISUAL_FEATURES;

    /**
     * Map of event identifiers to select-by-area geometries for those events.
     * The map will have entries for any events created during this session
     * using the select-by-area process.
     */
    private final Map<String, Set<Geometry>> selectByAreaGeometriesForEventIdentifiers = new HashMap<String, Set<Geometry>>();

    /**
     * Flag indicating whether or not add-new-geometry-to-selected-event is
     * currently in use. This is only used while {@link #settingsNotYetProvided}
     * is <code>true</code>.
     */
    private boolean addNewGeometryToSelected = false;

    /**
     * Flag indicating whether or not settings have not yet been provided by the
     * configuration manager. This would only be <code>true</code> during a
     * short period while starting up.
     */
    private boolean settingsNotYetProvided = true;

    /**
     * Handler to be notified when the spatial display is disposed of, changes
     * frames, etc.
     */
    private ISpatialDisplayHandler displayHandler;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param displayDisposedHandler
     *            Handler of spatial display notifications.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public SpatialPresenter(
            ISessionManager<ObservedHazardEvent, ObservedSettings> model,
            ISpatialDisplayHandler displayDisposedHandler,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
        this.displayHandler = displayDisposedHandler;
        this.hazardEventBuilder = new HazardEventBuilder(model);
        this.spatialEntityManager = new SpatialEntityManager(model,
                Collections.unmodifiableSet(selectedEventIdentifiers));
    }

    // Public Methods

    @Override
    @Deprecated
    public void modelChanged(EnumSet<HazardConstants.Element> changed) {
    }

    /**
     * Respond to the selected event set changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionSelectedEventsModified(
            SessionSelectedEventsModified change) {
        updateSelectedEvents();
        updateAllDisplayables(); // see which events changed selection state,
                                 // and change their spatial entities
        updateCenteringAndZoomLevel();
    }

    /**
     * Respond to an event's type changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventTypeModified(SessionEventTypeModified change) {
        updateAllDisplayables(); // change that event's spatial entity(s)
    }

    /**
     * Respond to an event's time range changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventTimeRangeModified(
            SessionEventTimeRangeModified change) {
        updateAllDisplayables(); // change that event's spatial entity(s)
    }

    /**
     * Respond to an event's geometry changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventGeometryModified(SessionEventGeometryModified change) {
        updateAllDisplayables(); // if the the event has no visual features,
                                 // change it's spatial entity(s)
    }

    /**
     * Respond to an event's visual features changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventVisualFeaturesModified(
            SessionEventVisualFeaturesModified change) {
        updateAllDisplayables(); // update the specified visual features, as
                                 // identified by the identifier set.
    }

    /**
     * Respond to an event's status changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventStatusModified(SessionEventStatusModified change) {
        updateAllDisplayables(); // change that event's spatial entity(s)
    }

    /**
     * Respond to an event's attributes changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    @Enveloped(messages = { SessionEventAttributesModified.class,
            SessionEventMetadataModified.class })
    public void sessionEventAttributesModified(MessageEnvelope change) {
        updateAllDisplayables(); // change that event's spatial entity(s)
    }

    /**
     * Respond to an event being added.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventAdded(SessionEventAdded change) {
        spatialEntityManager.addEntitiesForEvent(change.getEvent());
    }

    /**
     * Respond to an event being removed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventRemoved(SessionEventRemoved change) {
        spatialEntityManager.removeEntitiesForEvent(change.getEvent());
    }

    /**
     * Respond to visible hatching being toggled.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionHatchingToggled(SessionHatchingToggled change) {
        updateAllDisplayables(); // go through all events, turning on or off the
                                 // hatching spatial entities
    }

    /**
     * Respond to the current settings changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void settingsModified(SettingsModified change) {

        /*
         * Ensure that the new settings object has the correct value for add new
         * geometry to selected.
         */
        ObservedSettings settings = getModel().getConfigurationManager()
                .getSettings();
        if (settingsNotYetProvided) {
            settingsNotYetProvided = false;
            settings.setAddGeometryToSelected(addNewGeometryToSelected);
        }

        /*
         * Update the displayables.
         */
        updateAllDisplayables(); // see if filtering has changed, and if so,
                                 // change.
    }

    /**
     * Respond to a change in the selected time.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void selectedTimeChanged(SelectedTimeChanged change) {
        getView().setSelectedTime(
                new Date(getModel().getTimeManager().getSelectedTime()
                        .getLowerBound()));
        updateAllDisplayables(); // iterate through all events, seeing if any
                                 // have changed visibility
    }

    /**
     * Set the tool that is hoping to collect spatial information from the user
     * with the specified visual features, or reset so that no tool is doing so.
     * 
     * @param toolType
     *            Type of the tool; if <code>null</code>, no tool is collecting
     *            spatial information.
     * @param toolIdentifier
     *            Identifier of the tool; if <code>null</code>, no tool is
     *            collecting spatial information.
     * @param context
     *            Context in which the tool is to be run if spatial information
     *            is collected; if <code>null</code>, no tool is collecting
     *            spatial information.
     * @param visualFeatures
     *            Visual features to be used to collect spatial information; if
     *            <code>null</code>, no tool is collecting spatial information.
     */
    public void setToolVisualFeatures(ToolType toolType, String toolIdentifier,
            RecommenderExecutionContext context,
            VisualFeaturesList visualFeatures) {

        /*
         * Remember the tool context for the new visual features.
         */
        spatialInfoCollectingToolType = toolType;
        spatialInfoCollectingToolIdentifier = toolIdentifier;
        spatialInfoCollectingToolContext = context;

        /*
         * If the visual features are different from what they were before,
         * record the new ones, and generate spatial entities for them.
         */
        visualFeatures = (visualFeatures == null ? EMPTY_VISUAL_FEATURES
                : visualFeatures);
        if (toolVisualFeatures.equals(visualFeatures) == false) {
            toolVisualFeatures = visualFeatures;
            spatialEntityManager.updateEntitiesForToolVisualFeatures(
                    toolVisualFeatures, spatialInfoCollectingToolType,
                    spatialInfoCollectingToolIdentifier);
        }
    }

    /**
     * Update all the displayable representations in the view.
     * <p>
     * TODO: This is horribly brute-force. A finer-grained approach must be
     * taken when refactoring the spatial presenter/view code to ensure that
     * only those events that have changed in some way meaningful to their
     * display have their displayables changed.
     * </p>
     */
    public void updateAllDisplayables() {

        /*
         * Ensure the spatial view and session manager are around, since upon
         * perspective switch, this method may be called when they have been
         * deleted.
         */
        ISpatialView<?, ?> spatialView = getView();
        if (spatialView == null) {
            return;
        }
        ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager = getModel();
        if (sessionManager == null) {
            return;
        }

        /*
         * Ensure undo and redo are enabled as is appropriate.
         */
        spatialView.setUndoEnabled(sessionManager.isUndoable());
        spatialView.setRedoEnabled(sessionManager.isRedoable());

        /*
         * Recreate the spatial entities.
         */
        spatialEntityManager.recreateAllEntities(toolVisualFeatures,
                spatialInfoCollectingToolType,
                spatialInfoCollectingToolIdentifier);
    }

    // Protected Methods

    @Override
    protected void initialize(ISpatialView<?, ?> view) {
        getView().initialize(this);
        spatialEntityManager.setView(view);
        updateAllDisplayables();
    }

    @Override
    protected void reinitialize(ISpatialView<?, ?> view) {

        /*
         * No action.
         */
    }

    @Override
    protected void doDispose() {
        spatialEntityManager.dispose();
        displayHandler = null;
    }

    // Package Methods

    /**
     * Handle user modification of the geometry of the specified spatial entity.
     * 
     * @param identifier
     *            Identifier of the spatial entity.
     * @param selectedTime
     *            Selected time for which the geometry is to be changed.
     * @param geometry
     *            New geometry to be used by the spatial entity.
     */
    void handleUserModificationOfSpatialEntity(IEntityIdentifier identifier,
            Date selectedTime, Geometry geometry) {

        /*
         * Modify the visual feature that has had its geometry changed. If the
         * visual feature that was modified was created by a tool to collect
         * spatial information, run the tool with the modified visual feature.
         * Otherwise, find the event that goes with the visual feature and give
         * it the new version.
         */
        if (identifier instanceof ToolVisualFeatureEntityIdentifier) {

            /*
             * Ensure the visual feature's associated tool is the same as the
             * one that the current tool visual features are for.
             */
            ToolVisualFeatureEntityIdentifier toolIdentifier = (ToolVisualFeatureEntityIdentifier) identifier;
            if (toolIdentifier.getToolIdentifier().equals(
                    spatialInfoCollectingToolIdentifier)) {

                /*
                 * Find and modify the visual feature, then run the recommender
                 * with the modified visual feature list.
                 */
                VisualFeature feature = toolVisualFeatures
                        .getByIdentifier(toolIdentifier
                                .getVisualFeatureIdentifier());
                if (feature != null) {
                    feature.setGeometry(
                            DateUtils.truncate(selectedTime, Calendar.MINUTE),
                            geometry);
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
             * Find the event that goes with the spatial entity.
             */
            IHazardEventEntityIdentifier hazardIdentifier = (IHazardEventEntityIdentifier) identifier;
            ISessionEventManager<ObservedHazardEvent> eventManager = getModel()
                    .getEventManager();
            ObservedHazardEvent event = eventManager
                    .getEventById(hazardIdentifier.getEventIdentifier());
            if (event != null) {

                /*
                 * If the spatial entity represents a visual feature's state,
                 * set the visual feature's geometry to that specified, rounding
                 * the selected time down to the nearest minute, since it could
                 * be anything (i.e. not necessarily on a minute boundary).
                 * Otherwise, the spatial entity is the default representation
                 * of the hazard event's geometry, so set the latter's geometry
                 * to match.
                 */
                if (hazardIdentifier instanceof HazardEventVisualFeatureEntityIdentifier) {
                    VisualFeature feature = event
                            .getVisualFeature(((HazardEventVisualFeatureEntityIdentifier) hazardIdentifier)
                                    .getVisualFeatureIdentifier());
                    if (feature != null) {
                        feature.setGeometry(DateUtils.truncate(selectedTime,
                                Calendar.MINUTE), geometry);
                        event.setVisualFeature(feature,
                                UIOriginator.SPATIAL_DISPLAY);
                    }
                } else {

                    /*
                     * TODO: Since base geometries for hazard events are
                     * simplified before being used in spatial entities via
                     * getProcessedBaseGeometry(), setting the event's geometry
                     * to equal the newly-modified spatial entity's geometry
                     * will mean that the high-resolution original geometry is
                     * dumped in favor of the simplified one. This is
                     * problematic, and points to the issue discussed in the
                     * comment for getProcessedBaseGeometry().
                     */

                    /*
                     * Attempt to set the event geometry; if the geometry is
                     * rejected, redraw the events.
                     * 
                     * TODO: A better way to handle rejection would be to update
                     * only the event that is reverting.
                     */
                    if (eventManager.setEventGeometry(event, geometry,
                            UIOriginator.SPATIAL_DISPLAY) == false) {
                        updateAllDisplayables();
                    }
                }
            }
        }
    }

    /**
     * Handle an attempt by the user to change the selection set of spatial
     * entities on the spatial display to those specified.
     * 
     * @param identifiers
     *            Identifiers of the spatial entities selected in the spatial
     *            display that the user is attempting to make the selection set.
     */
    void handleUserSpatialEntitySelectionChange(
            Collection<IEntityIdentifier> identifiers) {

        /*
         * Weed out any spatial entities that are not representing hazard
         * events.
         */
        Set<String> eventIdentifiers = new HashSet<>(identifiers.size(), 1.0f);
        for (IEntityIdentifier identifier : identifiers) {
            if (identifier instanceof IHazardEventEntityIdentifier) {
                eventIdentifiers
                        .add(((IHazardEventEntityIdentifier) identifier)
                                .getEventIdentifier());
            }
        }
        getModel().getEventManager().setSelectedEventForIDs(eventIdentifiers,
                UIOriginator.SPATIAL_DISPLAY);
    }

    /**
     * Handle the user selection of a location (not a hazard event).
     * 
     * @param location
     *            Location selected by the user.
     */
    void handleUserLocationSelection(Coordinate location) {
        getModel().getEventManager().addOrRemoveEnclosingUGCs(location,
                UIOriginator.SPATIAL_DISPLAY);
    }

    /**
     * Handle the the setting of the flag indicating whether or not newly
     * created shapes should be added to the current selection set.
     * 
     * @param state
     *            New state of the flag.
     */
    void handleSetAddCreatedEventsToSelected(boolean state) {
        getModel().getEventManager().setAddCreatedEventsToSelected(state);
    }

    /**
     * Handle the user creation of a new shape.
     * 
     * @param geometry
     *            New geometry that has been created.
     */
    void handleUserShapeCreation(Geometry geometry) {

        /*
         * Create the event.
         */
        IHazardEvent hazardEvent = null;
        try {
            hazardEvent = hazardEventBuilder.buildHazardEvent(geometry, true);
        } catch (InvalidGeometryException e) {
            statusHandler.handle(
                    Priority.WARN,
                    "Error creating new hazard event based on "
                            + geometry.getClass().getSimpleName() + ": "
                            + e.getMessage(), e);
            return;
        }

        /*
         * Add the created event.
         */
        hazardEventBuilder.addEvent(hazardEvent, this);
    }

    /**
     * Handle the completion of a select-by-area creation or modification,
     * creating a new polygonal shape or modifying an existing shape.
     * 
     * @param identifier
     *            Identifier of the entity being edited; if <code>null</code>,
     *            no entity is being edited, and a new geometry is being
     *            created.
     * @param databaseTableName
     *            Name of the database table that was used to get the geometries
     *            within the select-by-area viz resource.
     * @param legend
     *            Legend text of the select-by-area viz resource.
     * @param selectedGeometries
     *            Geometries selected during the select-by-area process; these
     *            may be combined to create the new geometry.
     */
    void handleUserSelectByAreaCreationOrModification(
            IEntityIdentifier identifier, String databaseTableName,
            String legend, Set<Geometry> selectedGeometries) {

        /*
         * Merge the provided geometries into one to determine what the new
         * geometry is.
         */
        Geometry newGeometry = geometryFactory.createMultiPolygon(null);
        for (Geometry geometry : selectedGeometries) {
            newGeometry = newGeometry.union(geometry);
        }

        /*
         * If this is a modification of an existing spatial entity, assume it is
         * a spatial representation of a hazard event and tell the model about
         * the change. Otherwise, create a new hazard event.
         */
        String eventIdentifier = null;
        ISessionEventManager<ObservedHazardEvent> eventManager = getModel()
                .getEventManager();
        if (identifier != null) {
            eventIdentifier = ((IHazardEventEntityIdentifier) identifier)
                    .getEventIdentifier();
            eventManager.setEventGeometry(
                    eventManager.getEventById(eventIdentifier), newGeometry,
                    UIOriginator.SPATIAL_DISPLAY);
        } else {

            /*
             * Create the event.
             */
            IHazardEvent hazardEvent = null;
            try {
                hazardEvent = hazardEventBuilder.buildHazardEvent(newGeometry,
                        false);
            } catch (InvalidGeometryException e) {
                statusHandler.handle(Priority.ERROR,
                        "Error creating new hazard event based on "
                                + newGeometry.getClass().getSimpleName() + ": "
                                + e.getMessage()
                                + " (should never happen because geometries"
                                + "created by select-by-area are to not be "
                                + "checked for validity)", e);
                return;
            }

            /*
             * Store the database table name and the legend of the
             * select-by-area viz resource used to generate this hazard event,
             * as well as the add/remove shapes menu item as a context menu
             * possibility.
             */
            hazardEvent.addHazardAttribute(
                    HazardConstants.GEOMETRY_REFERENCE_KEY, databaseTableName);
            hazardEvent.addHazardAttribute(
                    HazardConstants.GEOMETRY_MAP_NAME_KEY, legend);
            hazardEvent
                    .addHazardAttribute(
                            HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY,
                            Lists.newArrayList(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES));

            /*
             * Add the event to the model.
             */
            ObservedHazardEvent observedHazardEvent = hazardEventBuilder
                    .addEvent(hazardEvent, UIOriginator.SPATIAL_DISPLAY);
            eventIdentifier = observedHazardEvent.getEventID();
        }

        selectByAreaGeometriesForEventIdentifiers.put(eventIdentifier,
                selectedGeometries);
    }

    /**
     * Handle the user initiation of a river gage action.
     * 
     * @param gageIdentifier
     *            Identifier of the gage for which the action is being invoked.
     */
    void handleUserInvocationOfGageAction(String gageIdentifier) {

        /*
         * If the configuration includes a gage-first recommender, run it using
         * the gage identifier as the value for a selected point identifier in a
         * dictionary that is provided as if it were from a recommender-supplied
         * dialog.
         */
        StartUpConfig startupConfig = getModel().getConfigurationManager()
                .getStartUpConfig();
        String gagePointFirstRecommender = startupConfig
                .getGagePointFirstRecommender();
        if ((gagePointFirstRecommender != null)
                && (gagePointFirstRecommender.isEmpty() == false)) {
            Map<String, Serializable> gageInfo = new HashMap<>();
            gageInfo.put(HazardConstants.SELECTED_POINT_ID, gageIdentifier);
            getModel().getRecommenderManager().runRecommender(
                    gagePointFirstRecommender,
                    RecommenderExecutionContext.getEmptyContext(), null,
                    gageInfo);
        }
    }

    /**
     * Handle an undo command invocation.
     */
    void handleUndoCommand() {
        getModel().undo();
    }

    /**
     * Handle a redo command invocation.
     */
    void handleRedoCommand() {
        getModel().redo();
    }

    /**
     * Set the add new events to the selected events set flag as specified.
     * 
     * @param state
     *            New state of the add new events to selected events set flag.
     */
    void handleToggleAddNewEventToSelectedSet(boolean state) {
        getModel().getConfigurationManager().getSettings()
                .setAddToSelected(state, UIOriginator.SPATIAL_DISPLAY);
    }

    /**
     * Set the add geometry to selected event flag as specified.
     * 
     * @param state
     *            New state of the add geometry to selected event flag.
     */
    void handleToggleAddGeometryToSelectedEvent(boolean state) {
        getModel().getConfigurationManager().getSettings()
                .setAddGeometryToSelected(state, UIOriginator.SPATIAL_DISPLAY);
    }

    /**
     * Handle the closing of the spatial display.
     */
    void handleSpatialDisplayClosed() {
        if (displayHandler != null) {
            displayHandler.spatialDisplayDisposed();
        }
    }

    /**
     * Get the list of context menu actions that this presenter can contribute,
     * given the specified spatial entity as the item chosen for the context
     * menu.
     * <p>
     * TODO: This method is used by the view to get necessary menu items, and it
     * needs to run in the main (worker) thread. This means that when a separate
     * thread is used in the future as a worker thread for presenters and the
     * model, {@link IRunnableAsynchronousScheduler} will need to be augmented
     * to include the ability to synchronously call a method that returns a
     * value. Currently, said interface only includes a method for scheduling
     * asynchronous executions of runnables that do not return anything.
     * </p>
     * 
     * @param entityIdentifier
     *            Identifier of the spatial entity that was chosen with the
     *            context menu invocation, or <code>null</code> if none was
     *            chosen.
     * @param scheduler
     *            Runnable asynchronous scheduler used to execute context menu
     *            invoked actions on the appropriate thread.
     * @return List of context menu actions.
     */
    List<IAction> getContextMenuActions(IEntityIdentifier entityIdentifier,
            IRunnableAsynchronousScheduler scheduler) {

        /*
         * If a spatial entity identifier was chosen and it is for a hazard
         * event, use that event as the current event; otherwise, use no event.
         */
        final ISessionEventManager<ObservedHazardEvent> eventManager = getModel()
                .getEventManager();
        if (entityIdentifier instanceof IHazardEventEntityIdentifier) {
            eventManager
                    .setCurrentEvent(((IHazardEventEntityIdentifier) entityIdentifier)
                            .getEventIdentifier());
        } else {
            eventManager.setCurrentEvent((String) null);
        }

        /*
         * Create the context menu helper to generate the menu items.
         */
        ContextMenuHelper helper = new ContextMenuHelper(getModel(), scheduler);

        /*
         * Create the manage hazards menu items, if any, on a submenu.
         */
        List<IAction> actions = new ArrayList<>();
        List<IContributionItem> items = helper
                .getSelectedHazardManagementItems(UIOriginator.SPATIAL_DISPLAY);
        IAction action = helper.createMenu("Manage hazards",
                items.toArray(new IContributionItem[items.size()]));
        if (action != null) {
            actions.add(action);
        }

        /*
         * Create the modify area menu items, if any, on a submenu, starting
         * with high/low resolution geometry toggles for the current event if
         * any,
         */
        List<IContributionItem> spatialItems = new ArrayList<>();
        if (eventManager.isCurrentEvent()) {
            GeometryResolution resolution = getGeometryResolutionForEvent(eventManager
                    .getCurrentEvent());
            if (resolution == GeometryResolution.LOW) {
                spatialItems
                        .add(createContributionItem(
                                CONTEXT_MENU_HIGH_RESOLUTION_GEOMETRY_FOR_CURRENT_EVENT,
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        eventManager
                                                .setHighResolutionGeometryVisibleForCurrentEvent(UIOriginator.SPATIAL_DISPLAY);
                                    }
                                }, scheduler));
            } else if (resolution == GeometryResolution.HIGH) {
                spatialItems.add(createContributionItem(
                        CONTEXT_MENU_LOW_RESOLUTION_GEOMETRY_FOR_CURRENT_EVENT,
                        new Runnable() {

                            @Override
                            public void run() {
                                eventManager
                                        .setLowResolutionGeometryVisibleForCurrentEvent(UIOriginator.SPATIAL_DISPLAY);
                            }
                        }, scheduler));
            }
        }

        /*
         * Include the low resolution toggle if any of the selected events have
         * high resolution geometries, and the high resolution toggle if any of
         * the selected events have low resolution geometries.
         */
        EnumSet<GeometryResolution> resolutionsFound = EnumSet
                .noneOf(GeometryResolution.class);
        List<ObservedHazardEvent> selectedEvents = eventManager
                .getSelectedEvents();
        for (ObservedHazardEvent event : selectedEvents) {
            GeometryResolution resolution = getGeometryResolutionForEvent(event);
            if (resolution != null) {
                resolutionsFound.add(resolution);
            }
            if (resolutionsFound.size() == GeometryResolution.values().length) {
                break;
            }
        }
        if (resolutionsFound.contains(GeometryResolution.LOW)) {
            spatialItems.add(createContributionItem(
                    CONTEXT_MENU_HIGH_RESOLUTION_GEOMETRY_FOR_SELECTED_EVENTS,
                    new Runnable() {

                        @Override
                        public void run() {
                            eventManager
                                    .setHighResolutionGeometriesVisibleForSelectedEvents(UIOriginator.SPATIAL_DISPLAY);
                        }
                    }, scheduler));
        }
        if (resolutionsFound.contains(GeometryResolution.HIGH)) {
            spatialItems.add(createContributionItem(
                    CONTEXT_MENU_LOW_RESOLUTION_GEOMETRY_FOR_SELECTED_EVENTS,
                    new Runnable() {

                        @Override
                        public void run() {
                            eventManager
                                    .setLowResolutionGeometriesVisibleForSelectedEvents(UIOriginator.SPATIAL_DISPLAY);
                        }
                    }, scheduler));
        }

        /*
         * Only add the add/remove shapes item to the submenu if there is only
         * one event selected, and if the event was built via a select-by-area
         * process.
         */
        if (selectedEvents.size() == 1) {
            ObservedHazardEvent event = selectedEvents.get(0);
            List<?> contextMenuEntries = (List<?>) event
                    .getHazardAttribute(HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY);
            if (contextMenuEntries != null) {
                ISessionConfigurationManager<ObservedSettings> configManager = getModel()
                        .getConfigurationManager();
                for (Object contextMenuEntry : contextMenuEntries) {
                    if (contextMenuEntry
                            .equals(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES)) {
                        if (eventManager.canEventAreaBeChanged(event) == false) {
                            continue;
                        }
                        String hazardType = event.getHazardType();
                        if ((hazardType != null)
                                && (configManager.getHazardTypes()
                                        .get(hazardType).getHatchingStyle() == HatchingStyle.NONE)) {
                            continue;
                        }
                        spatialItems.add(createContributionItem(
                                HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES,
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        loadSelectByAreaVizResourceForSelectedEvent();
                                    }
                                }, scheduler));
                        break;
                    }
                }
            }
        }

        /*
         * Create the modify area submenu.
         */
        action = helper.createMenu("Modify area...", spatialItems
                .toArray(new IContributionItem[spatialItems.size()]));
        if (action != null) {
            actions.add(action);
        }

        /*
         * Create the send to menu items, if any, on a submenu.
         */
        List<IContributionItem> sendToItems = new ArrayList<>(2);
        if (eventManager.getSelectedEvents().isEmpty() == false) {
            sendToItems.add(createContributionItem(CONTEXT_MENU_SEND_TO_BACK,
                    new Runnable() {

                        @Override
                        public void run() {
                            eventManager
                                    .sortEvents(SessionEventManager.SEND_SELECTED_BACK);
                        }
                    }, scheduler));
            sendToItems.add(createContributionItem(CONTEXT_MENU_BRING_TO_FRONT,
                    new Runnable() {

                        @Override
                        public void run() {
                            eventManager
                                    .sortEvents(SessionEventManager.SEND_SELECTED_FRONT);
                        }
                    }, scheduler));
        }
        action = helper.createMenu("Send to...",
                sendToItems.toArray(new IContributionItem[sendToItems.size()]));
        if (action != null) {
            actions.add(action);
        }

        return actions;
    }

    // Private Methods

    /**
     * Get the geometry resolution of the specified event.
     * 
     * @param event
     *            Event to be checked.
     * @return Geometry resolution for the event, or <code>null</code> if the
     *         event has no type.
     */
    private GeometryResolution getGeometryResolutionForEvent(
            ObservedHazardEvent event) {
        if (event.getHazardType() == null) {
            return null;
        }
        return (event.getHazardAttribute(VISIBLE_GEOMETRY).equals(
                LOW_RESOLUTION_GEOMETRY_IS_VISIBLE) ? GeometryResolution.LOW
                : GeometryResolution.HIGH);
    }

    /**
     * Create a contribution item with the specified text and runnable.
     * 
     * @param label
     *            Label for the item.
     * @param runnable
     *            Runnable to be executed if the item is invoked.
     * @param scheduler
     *            Runnable asynchronous scheduler to be used to execute the
     *            runnable.
     * @return Created contribution item.
     */
    private IContributionItem createContributionItem(String label,
            final Runnable runnable,
            final IRunnableAsynchronousScheduler scheduler) {
        IAction action = new Action(label) {
            @Override
            public void run() {
                scheduler.schedule(runnable);
            }
        };
        return new ActionContributionItem(action);
    }

    /**
     * Load the select-by-area viz resource for the selected event.
     */
    private void loadSelectByAreaVizResourceForSelectedEvent() {

        /*
         * Get the select-by-area editing context for the selected event; if
         * nothing is returned, then either more or less than one event is
         * selected, or the selected event did not have its geometry created via
         * select-by-area.
         */
        SelectByAreaContext context = getSelectByAreaContextForSelectedEvent();
        if (context == null) {
            return;
        }

        /*
         * Request the loading of the viz resource and the input handler.
         */
        getView().loadSelectByAreaVizResourceAndInputHandler(context);
    }

    /**
     * Get information about the selected hazard event needed to allow the user
     * to modify its geometry using select-by-area.
     * 
     * @return Information about the selected hazard event to allow its geometry
     *         to be modified using select-by-area, or <code>null</code> if
     *         there is not exactly one hazard event selected, or if the
     *         selected hazard event's geometry was not originally constructed
     *         using select-by-area.
     */
    private SelectByAreaContext getSelectByAreaContextForSelectedEvent() {

        /*
         * If there is not exactly one selected event, do nothing.
         */
        Collection<ObservedHazardEvent> selectedEvents = getModel()
                .getEventManager().getSelectedEvents();
        if (selectedEvents.size() != 1) {
            return null;
        }

        /*
         * If the event's geometry was created using select-by-area, then get
         * the necessary information and return it.
         */
        ObservedHazardEvent event = selectedEvents.iterator().next();
        if (event.getHazardAttributes().containsKey(
                HazardConstants.GEOMETRY_REFERENCE_KEY)) {
            String eventIdentifier = event.getEventID();
            SpatialEntity<? extends IEntityIdentifier> spatialEntity = spatialEntityManager
                    .getFirstPolygonalSpatialEntityForEvent(eventIdentifier);
            if (spatialEntity == null) {
                return null;
            }
            Set<Geometry> selectByAreaGeometries = selectByAreaGeometriesForEventIdentifiers
                    .get(eventIdentifier);
            return new SelectByAreaContext(
                    spatialEntity.getIdentifier(),
                    (selectByAreaGeometries == null ? new HashSet<Geometry>()
                            : selectByAreaGeometries),
                    (String) event
                            .getHazardAttribute(HazardConstants.GEOMETRY_REFERENCE_KEY),
                    (String) event
                            .getHazardAttribute(HazardConstants.GEOMETRY_MAP_NAME_KEY));
        }
        return null;
    }

    /**
     * Center and zoom the display given the currently selected hazard events.
     */
    private void updateCenteringAndZoomLevel() {
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
            GeometryCollection geometryCollection = geometryFactory
                    .createGeometryCollection(geometriesOfSelected
                            .toArray(asArray));
            Point center = geometryCollection.getCentroid();
            Coordinate[] hullCoordinates = geometryCollection.convexHull()
                    .getCoordinates();
            getView().centerAndZoomDisplay(Lists.newArrayList(hullCoordinates),
                    center.getCoordinate());
        }
    }

    /**
     * Update the cached information about which hazard events are currently
     * selected.
     */
    private void updateSelectedEvents() {

        /*
         * Compile a set of selected events.
         */
        selectedEventIdentifiers.clear();
        Set<ObservedHazardEvent> selectedEvents = new HashSet<>(getModel()
                .getEventManager().getSelectedEvents());
        for (ObservedHazardEvent hazardEvent : selectedEvents) {
            selectedEventIdentifiers.add(hazardEvent.getEventID());
        }

        /*
         * Enable the edit-multi-point-geometry buttons only if there is exactly
         * one event selected.
         */
        getView().setEditMultiPointGeometryEnabled(
                selectedEventIdentifiers.size() == 1);

        /*
         * Enable the add-geometry-to-selected button only if there is exactly
         * one event selected. This method may be called before settings have
         * been supplied, so the logic here handles null settings objects.
         */
        ObservedSettings settings = getModel().getConfigurationManager()
                .getSettings();
        settingsNotYetProvided = (settings == null);
        addNewGeometryToSelected = (settingsNotYetProvided ? false
                : Boolean.TRUE.equals(settings.getAddGeometryToSelected()));
        if (selectedEventIdentifiers.size() == 1) {
            getView().setAddNewGeometryToSelectedToggleState(true,
                    addNewGeometryToSelected);
        } else {
            if (addNewGeometryToSelected) {
                settings.setAddGeometryToSelected(addNewGeometryToSelected);
            }
            getView().setAddNewGeometryToSelectedToggleState(false, false);
        }
    }
}
