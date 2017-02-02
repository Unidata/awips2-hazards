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
import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.common.visuals.VisualFeature;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.contextmenu.ContextMenuHelper;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialEntityManager.EventDisplayChange;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.HazardEventVisualFeatureEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IHazardEventEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.ToolVisualFeatureEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaContext;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
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
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.configuration.types.HatchingStyle;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionSelectionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventGeometryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventMetadataModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTimeRangeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTypeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventVisualFeaturesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsOrderingModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionHatchingToggled;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTime;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTimeChanged;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Spatial presenter, used to mediate between the model and the spatial view.
 * <p>
 * The presenter interacts with the associated view via widget abstractions such
 * as {@link IStateChanger}, {@link IListStateChanger}, etc, as per the standard
 * Hazard Services MVP practice.
 * </p>
 * <p>
 * The presenter responds to changes in hazard events, tool-specified visual
 * features, etc. and turns all visual elements into {@link SpatialEntity}
 * instances. These are then passed to the {@link ISpatialView} for actual
 * display.
 * </p>
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
 * Aug 15, 2016 18376      Chris.Golden      Added code to make garbage collection of the session
 *                                           manager and app builder more likely.
 * Aug 23, 2016 19537      Chris.Golden      Continued extensive spatial display refactor, including
 *                                           the use of MVP widgets and delegates to decouple the
 *                                           presenter and view, and the replacement of the brute
 *                                           force "redraw entire display" approach with a much more
 *                                           fine-grained scheme of rebuilding spatial entities only
 *                                           when they may have changed.
 * Sep 12, 2016 15934      Chris.Golden      Changed to work with advanced geometries.
 * Sep 23, 2016 15934      Chris.Golden      Fixed bug that caused geometries added to the selected
 *                                           hazard event to not trigger a hazard area recalculation.
 * Oct 19, 2016 21873      Chris.Golden      Adjusted selected time rounding to work with different
 *                                           hazard event types' different time resolutions.
 * Feb 01, 2017 15556      Chris.Golden      Changed to use new selection manager. Also changed to
 *                                           take advantage of new finer-grained settings change
 *                                           messages. Also moved bring to front and send to back
 *                                           sorting here from session manager. Also added setting
 *                                           of view's selected time when initializing, to avoid
 *                                           exceptions that could occur if the user added or
 *                                           removed vertices from a spatial entity soon after
 *                                           starting Hazard Services.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SpatialPresenter extends
        HazardServicesPresenter<ISpatialView<?, ?>> implements IOriginator {

    // Public Enumerated Types

    /**
     * Commands.
     */
    public enum Command {
        UNDO, REDO, CLOSE
    }

    /**
     * Toggles.
     */
    public enum Toggle {
        ADD_CREATED_GEOMETRY_TO_SELECTED_EVENT, ADD_CREATED_EVENTS_TO_SELECTED, ADD_CREATED_EVENTS_TO_SELECTED_OVERRIDE
    }

    /**
     * Types of spatial entities.
     */
    public enum SpatialEntityType {
        HATCHING, UNSELECTED, SELECTED, TOOL
    }

    /**
     * Position of a point within the sequence of one or more points that are in
     * the process of being created.
     */
    public enum SequencePosition {
        FIRST, INTERIOR, LAST
    }

    // Private Enumerated Types

    /**
     * Geometry resolutions.
     */
    private enum GeometryResolution {
        LOW, HIGH
    };

    // Package-Private Static Constants

    /**
     * Starting size for a list of spatial entities of a particular type.
     */
    static final Map<SpatialEntityType, Integer> INITIAL_SIZES_FOR_SPATIAL_ENTITIES_LISTS;
    static {
        Map<SpatialEntityType, Integer> map = new EnumMap<>(
                SpatialEntityType.class);
        map.put(SpatialEntityType.HATCHING, 2000);
        map.put(SpatialEntityType.UNSELECTED, 1000);
        map.put(SpatialEntityType.SELECTED, 1000);
        map.put(SpatialEntityType.TOOL, 100);
        INITIAL_SIZES_FOR_SPATIAL_ENTITIES_LISTS = Collections
                .unmodifiableMap(map);
    }

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
     * Selected entity identifiers change handler. The identifier is ignored.
     */
    private final IStateChangeHandler<Object, Set<IEntityIdentifier>> selectedEntityIdentifiersChangeHandler = new IStateChangeHandler<Object, Set<IEntityIdentifier>>() {

        @Override
        public void stateChanged(Object identifier,
                Set<IEntityIdentifier> selectedSpatialEntityIdentifiers) {
            handleUserSpatialEntitySelectionChange(selectedSpatialEntityIdentifiers);
        }

        @Override
        public void statesChanged(
                Map<Object, Set<IEntityIdentifier>> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("selected entity identifiers");
        }
    };

    /**
     * Create shape command invocation handler. The identifier is the geometry
     * to be created.
     */
    private final ICommandInvocationHandler<IAdvancedGeometry> createShapeInvocationHandler = new ICommandInvocationHandler<IAdvancedGeometry>() {

        @Override
        public void commandInvoked(IAdvancedGeometry identifier) {
            handleUserShapeCreation(identifier);
        }
    };

    /**
     * Modify geometry command invocation handler. The identifier provides
     * information about the modification.
     */
    private final ICommandInvocationHandler<EntityGeometryModificationContext> modifyGeometryInvocationHandler = new ICommandInvocationHandler<EntityGeometryModificationContext>() {

        @Override
        public void commandInvoked(EntityGeometryModificationContext identifier) {
            handleUserModificationOfSpatialEntity(identifier);
        }
    };

    /**
     * Select-by-area command invocation handler.
     */
    private final ICommandInvocationHandler<SelectByAreaContext> selectByAreaInvocationHandler = new ICommandInvocationHandler<SelectByAreaContext>() {

        @Override
        public void commandInvoked(SelectByAreaContext identifier) {
            handleUserSelectByAreaCreationOrModification(identifier);
        }
    };

    /**
     * Select location command invocation handler. The identifier is the
     * location.
     */
    private final ICommandInvocationHandler<Coordinate> selectLocationInvocationHandler = new ICommandInvocationHandler<Coordinate>() {

        @Override
        public void commandInvoked(Coordinate identifier) {
            handleUserLocationSelection(identifier);
        }
    };

    /**
     * Gage action command invocation handler. The identifier is that of the
     * gage.
     */
    private final ICommandInvocationHandler<String> gageActionInvocationHandler = new ICommandInvocationHandler<String>() {

        @Override
        public void commandInvoked(String identifier) {
            handleUserInvocationOfGageAction(identifier);
        }
    };

    /**
     * Miscellaneous toggle change handler. The identifier is the toggle.
     */
    private final IStateChangeHandler<Toggle, Boolean> toggleChangeHandler = new IStateChangeHandler<Toggle, Boolean>() {

        @Override
        public void stateChanged(Toggle identifier, Boolean value) {
            if (identifier == Toggle.ADD_CREATED_GEOMETRY_TO_SELECTED_EVENT) {
                getModel()
                        .getConfigurationManager()
                        .getSettings()
                        .setAddGeometryToSelected(value,
                                UIOriginator.SPATIAL_DISPLAY);
            } else if (identifier == Toggle.ADD_CREATED_EVENTS_TO_SELECTED) {
                getModel().getConfigurationManager().getSettings()
                        .setAddToSelected(value, UIOriginator.SPATIAL_DISPLAY);
            } else {
                getModel().getEventManager().setAddCreatedEventsToSelected(
                        value);
            }
        }

        @Override
        public void statesChanged(Map<Toggle, Boolean> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("toggle states");
        }
    };

    /**
     * Miscellaneous command invocation handler. The identifier is the command.
     */
    private final ICommandInvocationHandler<Command> commandInvocationHandler = new ICommandInvocationHandler<Command>() {

        @Override
        public void commandInvoked(Command identifier) {
            if (identifier == Command.UNDO) {
                handleUndoCommand();
            } else if (identifier == Command.REDO) {
                handleRedoCommand();
            } else {
                handleSpatialDisplayClosed();
            }
        }
    };

    /**
     * Last recorded elected time.
     */
    private SelectedTime lastSelectedTime;

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

    /**
     * Comparator to be used to send selected events to the front of the list.
     */
    private final Comparator<ObservedHazardEvent> sendSelectedToFront = new Comparator<ObservedHazardEvent>() {

        @Override
        public int compare(ObservedHazardEvent o1, ObservedHazardEvent o2) {
            boolean s1 = selectedEventIdentifiers.contains(o1.getEventID());
            boolean s2 = selectedEventIdentifiers.contains(o2.getEventID());
            if (s1) {
                if (s2) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (s2) {
                return -1;
            }
            return 0;
        }
    };

    /**
     * Comparator to be used to send selected events to the back of the list.
     */
    private final Comparator<ObservedHazardEvent> sendSelectedToBack = Collections
            .reverseOrder(sendSelectedToFront);

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

        /*
         * If nothing has changed in terms of selection of current versions of
         * events, do nothing.
         */
        if (change.getEventIdentifiers().isEmpty()) {
            return;
        }

        setUndoRedoEnableState();
        updateSelectedEvents();

        /*
         * Iterate through the events that have changed selection state, and
         * regenerate and replace each of their spatial entities.
         */
        ISessionEventManager<ObservedHazardEvent> eventManager = getModel()
                .getEventManager();
        for (String eventIdentifier : change.getEventIdentifiers()) {
            ObservedHazardEvent event = eventManager
                    .getEventById(eventIdentifier);
            if (event != null) {
                spatialEntityManager.replaceEntitiesForEvent(event, false,
                        false);
            }
        }

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
        spatialEntityManager.replaceEntitiesForEvent(change.getEvent(), false,
                false);
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
        spatialEntityManager.replaceEntitiesForEvent(change.getEvent(), false,
                false);
    }

    /**
     * Respond to an event's geometry changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventGeometryModified(SessionEventGeometryModified change) {
        setUndoRedoEnableState();
        spatialEntityManager.replaceEntitiesForEvent(change.getEvent(), false,
                false);
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
        spatialEntityManager.replaceEntitiesForEvent(change.getEvent(), false,
                false);
    }

    /**
     * Respond to an event's status changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventStatusModified(SessionEventStatusModified change) {
        setUndoRedoEnableState();
        spatialEntityManager.replaceEntitiesForEvent(change.getEvent(), false,
                false);
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
        spatialEntityManager.replaceEntitiesForEvent(change
                .<SessionEventModified> getMessage().getEvent(), false, false);
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
        updateAllDisplayables();
    }

    /**
     * Respond to events' ordering being changed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventsOrderingModified(
            SessionEventsOrderingModified change) {
        updateAllDisplayables();
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
         * Update the displayables if appropriate.
         */
        if (change.getChanged().contains(ObservedSettings.Type.FILTERS)) {
            updateAllDisplayables();
        }
    }

    /**
     * Respond to a change in the selected time.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void selectedTimeChanged(SelectedTimeChanged change) {

        /*
         * Tell the view about the new selected time.
         */
        SelectedTime newSelectedTime = getModel().getTimeManager()
                .getSelectedTime();
        getView().setSelectedTime(new Date(newSelectedTime.getLowerBound()));

        /*
         * Iterate through all the events, determining for each what needs to be
         * done with respect to adding, removing, or regenerating spatial
         * entities, or do nothing for an event if the time change does not
         * warrant it.
         */
        ISessionEventManager<ObservedHazardEvent> eventManager = getModel()
                .getEventManager();
        for (ObservedHazardEvent event : eventManager.getEvents()) {
            EventDisplayChange action = spatialEntityManager
                    .getSpatialEntityActionForEventWithChangedSelectedTime(
                            event, lastSelectedTime, newSelectedTime);
            if (action == EventDisplayChange.ADD_OR_REPLACE) {
                spatialEntityManager.replaceEntitiesForEvent(event, false,
                        false);
            } else if (action == EventDisplayChange.REMOVE) {
                spatialEntityManager.removeEntitiesForEvent(event);
            }
        }

        /*
         * Remember the selected time for next time.
         */
        lastSelectedTime = newSelectedTime;

        /*
         * If the selected time change may change spatial generation from visual
         * features, update the tool visual feature spatial entities.
         */
        if (spatialEntityManager.isChangedSelectedTimeAffectingVisualFeatures(
                lastSelectedTime, newSelectedTime)) {
            spatialEntityManager.updateEntitiesForToolVisualFeatures(
                    toolVisualFeatures, spatialInfoCollectingToolType,
                    spatialInfoCollectingToolIdentifier);
        }
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
        if (getModel() == null) {
            return;
        }

        /*
         * Ensure undo and redo are enabled as is appropriate.
         */
        setUndoRedoEnableState();

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
        getView().initialize(this,
                spatialEntityManager.getSelectedSpatialEntityIdentifiers());
        spatialEntityManager.setView(view);

        /*
         * Tell the view about the selected time.
         */
        lastSelectedTime = getModel().getTimeManager().getSelectedTime();
        getView().setSelectedTime(new Date(lastSelectedTime.getLowerBound()));

        /*
         * Bind the invocation and change handlers to the appropriate invokers
         * and changers in the view.
         */
        getView().getSelectedSpatialEntityIdentifiersChanger()
                .setStateChangeHandler(selectedEntityIdentifiersChangeHandler);
        getView().getCreateShapeInvoker().setCommandInvocationHandler(
                createShapeInvocationHandler);
        getView().getModifyGeometryInvoker().setCommandInvocationHandler(
                modifyGeometryInvocationHandler);
        getView().getSelectByAreaInvoker().setCommandInvocationHandler(
                selectByAreaInvocationHandler);
        getView().getSelectLocationInvoker().setCommandInvocationHandler(
                selectLocationInvocationHandler);
        getView().getGageActionInvoker().setCommandInvocationHandler(
                gageActionInvocationHandler);
        getView().getToggleChanger().setStateChangeHandler(toggleChangeHandler);
        getView().getCommandInvoker().setCommandInvocationHandler(
                commandInvocationHandler);

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

    // Package-Private Methods

    /**
     * Get the list of context menu actions that this presenter can contribute,
     * given the specified spatial entity as the item chosen for the context
     * menu.
     * <p>
     * TODO: This method needs to be run from some new subclass of
     * {@link ICommandInvocationHandler} that returns a result. Currently this
     * is being called directly from the view, which is incorrect; only time
     * crunches prevent the implementation of the necessary changes.
     * Furthermore, this is used by the view to get necessary menu items, and it
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
     * @deprecated The method itself is not deprecated, but its visibility is;
     *             it must be invoked by the subclass of
     *             <code>ICommandInvocationHandler</code> mentioned in the to-do
     *             discussion.
     */
    @Deprecated
    List<IAction> getContextMenuActions(IEntityIdentifier entityIdentifier,
            IRunnableAsynchronousScheduler scheduler) {

        /*
         * If a spatial entity identifier was chosen and it is for a hazard
         * event, use that event as the current event; otherwise, use no event.
         */
        final ISessionEventManager<ObservedHazardEvent> eventManager = getModel()
                .getEventManager();
        ISessionSelectionManager<ObservedHazardEvent> selectionManager = getModel()
                .getSelectionManager();
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
        List<ObservedHazardEvent> selectedEvents = selectionManager
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
        if (selectionManager.getSelectedEvents().isEmpty() == false) {
            sendToItems.add(createContributionItem(CONTEXT_MENU_SEND_TO_BACK,
                    new Runnable() {

                        @Override
                        public void run() {
                            eventManager.sortEvents(sendSelectedToBack,
                                    UIOriginator.SPATIAL_DISPLAY);
                        }
                    }, scheduler));
            sendToItems.add(createContributionItem(CONTEXT_MENU_BRING_TO_FRONT,
                    new Runnable() {

                        @Override
                        public void run() {
                            eventManager.sortEvents(sendSelectedToFront,
                                    UIOriginator.SPATIAL_DISPLAY);
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
     * Handle the user creation of a new shape.
     * 
     * @param geometry
     *            New geometry that has been created.
     */
    private IHazardEvent handleUserShapeCreation(IAdvancedGeometry geometry) {
        return buildHazardEvent(geometry, true);
    }

    /**
     * Handle user modification of the geometry of the specified spatial entity.
     * 
     * @param context
     *            Context for the modification, including the entity identifier,
     *            the new geometry, and the selected time.
     */
    private void handleUserModificationOfSpatialEntity(
            EntityGeometryModificationContext context) {

        /*
         * Modify the visual feature that has had its geometry changed. If the
         * visual feature that was modified was created by a tool to collect
         * spatial information, run the tool with the modified visual feature.
         * Otherwise, find the event that goes with the visual feature and give
         * it the new version.
         */
        if (context.getIdentifier() instanceof ToolVisualFeatureEntityIdentifier) {

            /*
             * Ensure the visual feature's associated tool is the same as the
             * one that the current tool visual features are for.
             */
            ToolVisualFeatureEntityIdentifier toolIdentifier = (ToolVisualFeatureEntityIdentifier) context
                    .getIdentifier();
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
                    feature.setGeometry(context.getSelectedTime(),
                            context.getGeometry());
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
            IHazardEventEntityIdentifier hazardIdentifier = (IHazardEventEntityIdentifier) context
                    .getIdentifier();
            ISessionEventManager<ObservedHazardEvent> eventManager = getModel()
                    .getEventManager();
            ObservedHazardEvent event = eventManager
                    .getEventById(hazardIdentifier.getEventIdentifier());
            if (event != null) {

                /*
                 * If the spatial entity represents a visual feature's state,
                 * set the visual feature's geometry to that specified, rounding
                 * the selected time down if the current resolution demands it,
                 * since it could be anything (i.e. not necessarily on a minute
                 * boundary). Otherwise, the spatial entity is the default
                 * representation of the hazard event's geometry, so set the
                 * latter's geometry to match.
                 */
                if (hazardIdentifier instanceof HazardEventVisualFeatureEntityIdentifier) {
                    VisualFeature feature = event
                            .getVisualFeature(((HazardEventVisualFeatureEntityIdentifier) hazardIdentifier)
                                    .getVisualFeatureIdentifier());
                    if (feature != null) {
                        feature.setGeometry(
                                getTimeAtResolutionForEvent(event.getEventID(),
                                        context.getSelectedTime()), context
                                        .getGeometry());
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
                     * rejected, redraw the event containing the geometry.
                     */
                    if (eventManager
                            .setEventGeometry(event, context.getGeometry(),
                                    UIOriginator.SPATIAL_DISPLAY) == false) {
                        spatialEntityManager.replaceEntitiesForEvent(event,
                                false, true);
                    }
                }
            }
        }
    }

    /**
     * Handle the completion of a select-by-area creation or modification,
     * creating a new polygonal shape or modifying an existing shape.
     * 
     * @param context
     *            Context indicating whether this is a creation or modification,
     *            what database table and legend were used, and which geometries
     *            were selected.
     */
    private void handleUserSelectByAreaCreationOrModification(
            SelectByAreaContext context) {

        /*
         * Merge the provided geometries into one to determine what the new
         * geometry is.
         */
        Geometry newBaseGeometry = geometryFactory.createMultiPolygon(null);
        for (Geometry geometry : context.getSelectedGeometries()) {
            newBaseGeometry = newBaseGeometry.union(geometry);
        }
        IAdvancedGeometry newGeometry = AdvancedGeometryUtilities
                .createGeometryWrapper(newBaseGeometry, 0);

        /*
         * If this is a modification of an existing spatial entity, assume it is
         * a spatial representation of a hazard event and tell the model about
         * the change. Otherwise, create a new hazard event.
         */
        String eventIdentifier = null;
        ISessionEventManager<ObservedHazardEvent> eventManager = getModel()
                .getEventManager();
        if (context.getIdentifier() != null) {
            eventIdentifier = ((IHazardEventEntityIdentifier) context
                    .getIdentifier()).getEventIdentifier();
            eventManager.setEventGeometry(
                    eventManager.getEventById(eventIdentifier), newGeometry,
                    UIOriginator.SPATIAL_DISPLAY);
        } else {

            /*
             * Create the event.
             */
            IHazardEvent hazardEvent = buildHazardEvent(newGeometry, false);
            if (hazardEvent == null) {
                return;
            }
            eventIdentifier = hazardEvent.getEventID();

            /*
             * Store the database table name and the legend of the
             * select-by-area viz resource used to generate this hazard event,
             * as well as the add/remove shapes menu item as a context menu
             * possibility.
             */
            hazardEvent.addHazardAttribute(
                    HazardConstants.GEOMETRY_REFERENCE_KEY,
                    context.getDatabaseTableName());
            hazardEvent.addHazardAttribute(
                    HazardConstants.GEOMETRY_MAP_NAME_KEY, context.getLegend());
            hazardEvent
                    .addHazardAttribute(
                            HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY,
                            Lists.newArrayList(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES));
        }

        selectByAreaGeometriesForEventIdentifiers.put(eventIdentifier,
                context.getSelectedGeometries());
    }

    /**
     * Handle an attempt by the user to change the selection set of spatial
     * entities on the spatial display to those specified.
     * 
     * @param identifiers
     *            Identifiers of the spatial entities selected in the spatial
     *            display that the user is attempting to make the selection set.
     */
    private void handleUserSpatialEntitySelectionChange(
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

        /*
         * Ensure that the model is still around before attempting to select the
         * event identifiers, since this method may be called as Hazard Services
         * is closing.
         */
        ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager = getModel();
        if (sessionManager != null) {
            getModel().getSelectionManager().setSelectedEventIdentifiers(
                    eventIdentifiers, UIOriginator.SPATIAL_DISPLAY);
        }
    }

    /**
     * Handle the user selection of a location (not a hazard event).
     * 
     * @param location
     *            Location selected by the user.
     */
    private void handleUserLocationSelection(Coordinate location) {
        getModel().getEventManager().addOrRemoveEnclosingUGCs(location,
                UIOriginator.SPATIAL_DISPLAY);
    }

    /**
     * Handle the user initiation of a river gage action.
     * 
     * @param gageIdentifier
     *            Identifier of the gage for which the action is being invoked.
     */
    private void handleUserInvocationOfGageAction(String gageIdentifier) {

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
    private void handleUndoCommand() {
        getModel().undo();
    }

    /**
     * Handle a redo command invocation.
     */
    private void handleRedoCommand() {
        getModel().redo();
    }

    /**
     * Handle the closing of the spatial display.
     */
    private void handleSpatialDisplayClosed() {
        if (displayHandler != null) {
            displayHandler.spatialDisplayDisposed();
        }
    }

    /**
     * Set the undo and redo enabled state as appropriate.
     */
    private void setUndoRedoEnableState() {
        ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager = getModel();
        if (sessionManager == null) {
            return;
        }
        getView().getCommandInvoker().setEnabled(Command.UNDO,
                sessionManager.isUndoable());
        getView().getCommandInvoker().setEnabled(Command.REDO,
                sessionManager.isRedoable());
    }

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
        List<ObservedHazardEvent> selectedEvents = getModel()
                .getSelectionManager().getSelectedEvents();
        if (selectedEvents.size() != 1) {
            return null;
        }

        /*
         * If the event's geometry was created using select-by-area, then get
         * the necessary information and return it.
         */
        ObservedHazardEvent event = selectedEvents.get(0);
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
     * Build a hazard event around the specified geometry.
     * 
     * @param geometry
     *            Geometry with which to build the event.
     * @param checkValidity
     *            Flag indicating whether or not to check the geometry for
     *            validity.
     * @return Hazard event, or <code>null</code> if the geometry was checked
     *         and found to be invalid.
     */
    private IHazardEvent buildHazardEvent(IAdvancedGeometry geometry,
            boolean checkValidity) {
        if (checkValidity && (geometry.isValid() == false)) {
            statusHandler.handle(
                    Priority.WARN,
                    "Invalid geometry: "
                            + geometry.getValidityProblemDescription()
                            + "; new geometry will not be used.");
            return null;
        }
        IHazardEvent hazardEvent = new BaseHazardEvent();
        hazardEvent.setGeometry(geometry);
        hazardEvent.setCreationTime(getModel().getTimeManager()
                .getCurrentTime());
        return addEvent(hazardEvent);
    }

    /**
     * Add the specified hazard event to the event manager, or add its geometry
     * to an already selected event if appropriate.
     * 
     * @param event
     *            Event to be added.
     * @return Resulting new (or if adding geometry to selected, existingt)
     *         event from the event manager.
     */
    private ObservedHazardEvent addEvent(IHazardEvent event) {

        /*
         * Update the event user and workstation based on who created the event.
         */
        event.setUserName(LocalizationManager.getInstance().getCurrentUser());
        event.setWorkStation(VizApp.getHostName());

        /*
         * If the geometry is to be added to the selected hazard, do this and do
         * nothing with the new event.
         */
        if ((Boolean.TRUE.equals(getModel().getConfigurationManager()
                .getSettings().getAddGeometryToSelected()))
                && (event.getHazardType() == null)
                && (getModel().getSelectionManager().getSelectedEvents().size() == 1)) {

            ObservedHazardEvent existingEvent = getModel()
                    .getSelectionManager().getSelectedEvents().get(0);

            IAdvancedGeometry existingGeometries = existingEvent.getGeometry();
            IAdvancedGeometry newGeometries = event.getGeometry();

            getModel().getEventManager().setEventGeometry(
                    existingEvent,
                    AdvancedGeometryUtilities.createCollection(
                            existingGeometries, newGeometries),
                    UIOriginator.SPATIAL_DISPLAY);

            /*
             * Remove the context menu contribution key so that the now-modified
             * hazard event will not allow the use of select-by-area to modify
             * its geometry.
             */
            existingEvent
                    .removeHazardAttribute(HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY);
            return existingEvent;
        }

        return getModel().getEventManager().addEvent(event,
                UIOriginator.SPATIAL_DISPLAY);
    }

    /**
     * Get the specified time rounded down for the resolution appropriate to the
     * specified event.
     * 
     * @param eventIdentifier
     *            Identifier of the event for which the time is to be used.
     * @param time
     *            Time to be rounded down.
     * @return Time rounded down as appropriate.
     */
    private Date getTimeAtResolutionForEvent(String eventIdentifier, Date time) {
        return DateUtils.truncate(time,
                HazardConstants.TRUNCATION_UNITS_FOR_TIME_RESOLUTIONS
                        .get(getModel().getEventManager()
                                .getTimeResolutionsForEventIds()
                                .get(eventIdentifier)));
    }

    /**
     * Center and zoom the display given the currently selected hazard events.
     */
    private void updateCenteringAndZoomLevel() {
        List<ObservedHazardEvent> selectedEvents = getModel()
                .getSelectionManager().getSelectedEvents();
        if (selectedEvents.isEmpty() == false) {
            List<Geometry> geometriesOfSelected = new ArrayList<>();
            for (ObservedHazardEvent selectedEvent : selectedEvents) {
                geometriesOfSelected.add(selectedEvent.getFlattenedGeometry());
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
         * Get the set of selected events.
         */
        selectedEventIdentifiers.clear();
        selectedEventIdentifiers.addAll(getModel().getSelectionManager()
                .getSelectedEventIdentifiers());

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
            getView().getToggleChanger().setEnabled(
                    Toggle.ADD_CREATED_GEOMETRY_TO_SELECTED_EVENT, true);
            getView().getToggleChanger().setState(
                    Toggle.ADD_CREATED_GEOMETRY_TO_SELECTED_EVENT,
                    addNewGeometryToSelected);
        } else {
            if (addNewGeometryToSelected) {
                settings.setAddGeometryToSelected(addNewGeometryToSelected);
            }
            getView().getToggleChanger().setEnabled(
                    Toggle.ADD_CREATED_GEOMETRY_TO_SELECTED_EVENT, false);
            getView().getToggleChanger().setState(
                    Toggle.ADD_CREATED_GEOMETRY_TO_SELECTED_EVENT, false);
        }
    }

    /**
     * Throw an unsupported operation exception for attempts to change multiple
     * states that are not appropriate.
     * 
     * @param description
     *            Description of the element for which an attempt to change
     *            multiple states was made.
     * @throws UnsupportedOperationException
     *             Whenever this method is called.
     */
    private void handleUnsupportedOperationAttempt(String description)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "cannot change multiple states associated with spatial view "
                        + description);
    }
}
