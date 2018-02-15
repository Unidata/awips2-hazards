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

import org.apache.commons.lang.time.DateUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.SessionHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.locks.LockInfo.LockStatus;
import com.raytheon.uf.common.hazards.configuration.types.HatchingStyle;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.hydro.RiverForecastManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SiteChanged;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventAttributesModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventGeometryModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventMetadataModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventStatusModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventTimeRangeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventTypeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventVisualFeaturesModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.IEventModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionSelectionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventCheckedStateModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsLockStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsOrderingModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionHatchingToggled;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.locks.ISessionLockManager;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTime;
import com.raytheon.uf.viz.hazards.sessionmanager.time.SelectedTimeChanged;
import com.raytheon.uf.viz.hazards.sessionmanager.tools.ToolExecutionIdentifier;
import com.raytheon.viz.core.mode.CAVEMode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.common.utilities.DragAndDropGeometryEditSource;
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
import net.engio.mbassy.listener.Handler;

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
 * Nov 18, 2014 4124       Chris.Golden      Adapted to new time manager.
 * Dec 05, 2014 4124       Chris.Golden      Changed to work with newly parameterized
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
 * Mar 14, 2016 12145      mduff             Handle error thrown by event manager.
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
 * May 10, 2016 18240      Chris.Golden      Changed gage context action to include the gage identifier
 *                                           as part of the recommender execution context, instead of
 *                                           being passed as a pseudo-dialog result to the recommender.
 *                                           Also made the recommender running actually allow the
 *                                           recommender to specify a dialog if it wishes.
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
 * Nov 03, 2016 22960      bkowal            Notify the user if a gage is not selected.
 * Dec 12, 2016 21504      Robert.Blum       Updates for hazard locking.
 * Dec 14, 2016 26813      bkowal            Use the active Hazard Services site when determining
 *                                           which counties to render for "Select by Area".
 * Feb 01, 2017 15556      Chris.Golden      Changed to use new selection manager. Also changed to
 *                                           take advantage of new finer-grained settings change
 *                                           messages. Also moved bring to front and send to back
 *                                           sorting here from session manager. Also added setting
 *                                           of view's selected time when initializing, to avoid
 *                                           exceptions that could occur if the user added or
 *                                           removed vertices from a spatial entity soon after
 *                                           starting Hazard Services.
 * Apr 05, 2017 32733      Robert.Blum       Changed lock status modified handler to deal with new
 *                                           version of notification that notifies of one or more
 *                                           lock statuses changing.
 * Mar 16, 2017 15528      Chris.Golden      Added notification handler for checked-status changes
 *                                           to hazard events, now that checked status is being
 *                                           tracked by the event manager instead of as part of
 *                                           hazard events.
 * Jun 22, 2017 15561      Chris.Golden      Added flag to force recreation of spatial displayables
 *                                           when necessary.
 * Jun 27, 2017 14789      Robert.Blum       Added passing of select by area color to view.
 * Jun 30, 2017 19223      Chris.Golden      Changed to work with new signatures of ContextMenuHelper
 *                                           constructor and method.
 * Jun 30, 2017 21638      Chris.Golden      Only allow gage action menu item to be enabled if there
 *                                           is a recommender configured as the gage-point-first
 *                                           recommender.
 * Sep 27, 2017 38072      Chris.Golden      Changed to use new SessionEventModified notification,
 *                                           and to work with new recommender manager.
 * Oct 23, 2017 21730      Chris.Golden      Added use of default hazard type for manually created
 *                                           hazard events.
 * Dec 07, 2017 41886      Chris.Golden      Removed Java 8/JDK 1.8 usage.
 * Dec 17, 2017 20739      Chris.Golden      Refactored away access to directly mutable session
 *                                           events.
 * Jan 17, 2018 33428      Chris.Golden      Changed to work with new, more flexible toolbar
 *                                           contribution code, and to provide new enhanced
 *                                           geometry-operation-based edits.
 * Jan 22, 2018 25765      Chris.Golden      Added ability for the settings to specify which
 *                                           drag-and-drop manipulation points are to be
 *                                           prioritized.
 * Feb 02, 2018 26712      Chris.Golden      Added the ability to center and zoom on a particular
 *                                           visual feature of a hazard event, instead of always
 *                                           using the base geometry of said event.
 * Feb 23, 2018 28012      Chris.Golden      Ensured that Add/Remove Shapes menu is not used if an
 *                                           event can't have its area increased.
 * Mar 22, 2018 15561      Chris.Golden      Added code to ensure that the spatial display's
 *                                           editability is factored into the editability (and
 *                                           visual cues thereof) of spatial entities, into the
 *                                           enabled state of toolbar buttons, and into whether
 *                                           context menu items are provided.
 * Apr 30, 2018 15561      Chris.Golden      Fixed bug that caused National as a site to cause a
 *                                           failure for select-by-area to work.  Also fixed bug
 *                                           that in certain cases caused null pointer exceptions
 *                                           during unloads of the H.S. layer. Also changed
 *                                           BaseHazardEvent to SessionHazardEvent.
 * May 22, 2018  3782      Chris.Golden      Changed to allow the last tool visual feature, if it
 *                                           has no geometry, to accept a new user-created geometry
 *                                           as its geometry.
 * Jun 06, 2018 15561      Chris.Golden      Added practice flag for hazard event creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SpatialPresenter
        extends HazardServicesPresenter<ISpatialView<?, ?, ?>> {

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
        ADD_CREATED_EVENTS_TO_SELECTED, ADD_CREATED_EVENTS_TO_SELECTED_OVERRIDE
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

    /**
     * Map pairing modification classes of interest for the handler of the
     * {@link SessionEventModified} notification with flags indicating whether
     * or not each such modification should also trigger a setting of the
     * undo/redo state.
     */
    private static final Map<Class<? extends IEventModification>, Boolean> SET_UNDO_REDO_FLAGS_FOR_EVENT_MODIFICATION_CLASSES;

    static {
        Map<Class<? extends IEventModification>, Boolean> map = new HashMap<>(7,
                1.0f);
        map.put(EventTypeModification.class, false);
        map.put(EventTimeRangeModification.class, false);
        map.put(EventGeometryModification.class, true);
        map.put(EventVisualFeaturesModification.class, false);
        map.put(EventStatusModification.class, true);
        map.put(EventAttributesModification.class, false);
        map.put(EventMetadataModification.class, false);
        SET_UNDO_REDO_FLAGS_FOR_EVENT_MODIFICATION_CLASSES = ImmutableMap
                .copyOf(map);
    }

    // Private Variables

    /**
     * Status handler for logging.
     */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    /**
     * Spatial view editability change handler. The identifier is ignored.
     */
    private final IStateChangeHandler<Object, Boolean> editabilityChangeHandler = new IStateChangeHandler<Object, Boolean>() {

        @Override
        public void stateChanged(Object identifier, Boolean value) {
            spatialEntityManager.setEditable(value);
            updateAllDisplayables(false);
        }

        @Override
        public void statesChanged(Map<Object, Boolean> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("editability");
        }
    };

    /**
     * Selected entity identifiers change handler. The identifier is ignored.
     */
    private final IStateChangeHandler<Object, Set<IEntityIdentifier>> selectedEntityIdentifiersChangeHandler = new IStateChangeHandler<Object, Set<IEntityIdentifier>>() {

        @Override
        public void stateChanged(Object identifier,
                Set<IEntityIdentifier> selectedSpatialEntityIdentifiers) {
            handleUserSpatialEntitySelectionChange(
                    selectedSpatialEntityIdentifiers);
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
        public void commandInvoked(
                EntityGeometryModificationContext identifier) {
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
            if ((getModel() == null)
                    || (getModel().getEventManager() == null)) {
                return;
            }
            if (identifier == Toggle.ADD_CREATED_EVENTS_TO_SELECTED) {
                getModel().getConfigurationManager().getSettings()
                        .setAddToSelected(value, UIOriginator.SPATIAL_DISPLAY);
            } else {
                getModel().getEventManager()
                        .setAddCreatedEventsToSelected(value);
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
     * Execution identifier of the tool attempting to collect information via
     * the visual features within {@link #toolVisualFeatures}; if the latter is
     * empty, this is irrelevant.
     */
    private ToolExecutionIdentifier spatialInfoCollectingContext;

    /**
     * Visual features being used by a tool to collect spatial information; if
     * empty, no tool is currently doing so.
     */
    private VisualFeaturesList toolVisualFeatures = EMPTY_VISUAL_FEATURES;

    /**
     * Visual feature from {@link #toolVisualFeatures} that has no geometry and
     * therefore may be used to take a new geometry that the user has drawn, if
     * any. This is meaningless if <code>toolVisualFeatures</code> is empty.
     */
    private VisualFeature toolVisualFeatureAcceptingNewGeometry;

    /**
     * Map of event identifiers to select-by-area geometries for those events.
     * The map will have entries for any events created during this session
     * using the select-by-area process.
     */
    private final Map<String, Set<Geometry>> selectByAreaGeometriesForEventIdentifiers = new HashMap<String, Set<Geometry>>();

    /**
     * Handler to be notified when the spatial display is disposed of, changes
     * frames, etc.
     */
    private ISpatialDisplayHandler displayHandler;

    /**
     * Comparator to be used to send selected events to the front of the list.
     */
    private final Comparator<IReadableHazardEvent> sendSelectedToFront = new Comparator<IReadableHazardEvent>() {

        @Override
        public int compare(IReadableHazardEvent o1, IReadableHazardEvent o2) {
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
    private final Comparator<IReadableHazardEvent> sendSelectedToBack = Collections
            .reverseOrder(sendSelectedToFront);

    /**
     * River forecast manager.
     */
    private final RiverForecastManager riverForecastManager = new RiverForecastManager();

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
    public SpatialPresenter(ISessionManager<ObservedSettings> model,
            ISpatialDisplayHandler displayHandler,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
        this.displayHandler = displayHandler;
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
        ISessionEventManager eventManager = getModel().getEventManager();
        for (String eventIdentifier : change.getEventIdentifiers()) {
            IHazardEventView event = eventManager.getEventById(eventIdentifier);
            if (event != null) {
                spatialEntityManager.replaceEntitiesForEvent(event, false,
                        false);
            }
        }

        updateGeometryEditingActions(false);

        updateCenteringAndZoomLevel();
    }

    /**
     * Respond to an event being changed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventModified(SessionEventModified change) {

        /*
         * Get the classes of the modifications that were made, and use them to
         * prune the map that has classes of interest to this method as keys.
         */
        Map<Class<? extends IEventModification>, Boolean> setUndoRedoFlagsForModificationClasses = new HashMap<>(
                SET_UNDO_REDO_FLAGS_FOR_EVENT_MODIFICATION_CLASSES);
        setUndoRedoFlagsForModificationClasses.keySet()
                .retainAll(change.getClassesOfModifications());

        /*
         * If the resulting pruned map is not empty, then first, if it contains
         * at least one entry that indicates that undo-redo should be set, set
         * undo/redo enabled state; and regardless, replace the spatial entities
         * for the the event that was changed.
         */
        if (setUndoRedoFlagsForModificationClasses.isEmpty() == false) {
            if (setUndoRedoFlagsForModificationClasses
                    .containsValue(Boolean.TRUE)) {
                setUndoRedoEnableState();
            }
            spatialEntityManager.replaceEntitiesForEvent(change.getEvent(),
                    false, false);
        }

        /*
         * Update allowable geometry editing actions.
         */
        updateGeometryEditingActions(true);
    }

    /**
     * Respond to an event's checked state changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventCheckedStateModified(
            SessionEventCheckedStateModified change) {
        spatialEntityManager.replaceEntitiesForEvent(change.getEvent(), false,
                false);
        updateGeometryEditingActions(true);
    }

    /**
     * Respond to one or more events' lock statuses changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventsLockStatusModified(
            SessionEventsLockStatusModified change) {

        for (String eventIdentifier : change.getEventIdentifiers()) {
            IHazardEventView event = getModel().getEventManager()
                    .getEventById(eventIdentifier);
            if (event != null) {
                spatialEntityManager.replaceEntitiesForEvent(event, false,
                        false);
            }
        }

        updateGeometryEditingActions(true);
    }

    /**
     * Respond to events being added.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventsAdded(SessionEventsAdded change) {
        spatialEntityManager.addEntitiesForEvents(change.getEvents());
        updateGeometryEditingActions(true);
    }

    /**
     * Respond to events being removed.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionEventsRemoved(SessionEventsRemoved change) {
        spatialEntityManager.removeEntitiesForEvents(change.getEvents());
    }

    /**
     * Respond to visible hatching being toggled.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void sessionHatchingToggled(SessionHatchingToggled change) {
        updateAllDisplayables(false);
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
        updateAllDisplayables(false);
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
         * Update the displayables if appropriate.
         */
        if (change.getChanged().contains(ObservedSettings.Type.FILTERS)) {
            updateAllDisplayables(false);
        }

        /*
         * Let the view know if the priority for drag-and-drop geometry edits
         * has changed.
         */
        if (change.getChanged().contains(
                ObservedSettings.Type.PRIORITY_FOR_DRAG_AND_DROP_GEOMETRY_EDITS)) {
            updatePriorityForDragAndDropGeometryEdits();
        }
    }

    /**
     * Respond to the current site changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void siteChanged(SiteChanged change) {
        getView().setCurrentSite(change.getSiteIdentifier());
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
        ISessionEventManager eventManager = getModel().getEventManager();
        for (IHazardEventView event : eventManager.getEvents()) {
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
                    toolVisualFeatures, spatialInfoCollectingContext);
        }

        /*
         * Update allowable geometry editing actions.
         */
        updateGeometryEditingActions(true);
    }

    /**
     * Set the tool that is hoping to collect spatial information from the user
     * with the specified visual features, or reset so that no tool is doing so.
     * 
     * @param identifier
     *            Tool execution identifier; if <code>null</code>, no tool is
     *            collecting spatial information.
     * @param visualFeatures
     *            Visual features to be used to collect spatial information; if
     *            <code>null</code>, no tool is collecting spatial information.
     */
    public void setToolVisualFeatures(ToolExecutionIdentifier identifier,
            VisualFeaturesList visualFeatures) {

        /*
         * Remember the tool context for the new visual features.
         */
        spatialInfoCollectingContext = identifier;

        /*
         * If the visual features are different from what they were before,
         * record the new ones, record a geometry-consuming visual feature if
         * one is found, and generate spatial entities for them.
         */
        visualFeatures = (visualFeatures == null ? EMPTY_VISUAL_FEATURES
                : visualFeatures);
        toolVisualFeatureAcceptingNewGeometry = null;
        if (toolVisualFeatures.equals(visualFeatures) == false) {
            toolVisualFeatures = visualFeatures;
            if ((toolVisualFeatures != null)
                    && (toolVisualFeatures.isEmpty() == false)
                    && toolVisualFeatures.get(toolVisualFeatures.size() - 1)
                            .isLackingGeometry()) {
                toolVisualFeatureAcceptingNewGeometry = toolVisualFeatures
                        .get(toolVisualFeatures.size() - 1);
            }
            spatialEntityManager.updateEntitiesForToolVisualFeatures(
                    toolVisualFeatures, spatialInfoCollectingContext);
        }

        /*
         * Update allowable geometry editing actions.
         */
        updateGeometryEditingActions(true);
    }

    /**
     * Update all the displayable representations in the view.
     * 
     * @param force
     *            Flag indicating whether or not to delete all displayables
     *            before updating them. If <code>false</code>, any displayables
     *            that were already created and are identical to the new ones
     *            are not updated.
     */
    public void updateAllDisplayables(boolean force) {

        /*
         * Ensure the spatial view and session manager are around, since upon
         * perspective switch, this method may be called when they have been
         * deleted.
         */
        ISpatialView<?, ?, ?> spatialView = getView();
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
                spatialInfoCollectingContext, force);

        /*
         * Update allowable geometry editing actions.
         */
        updateGeometryEditingActions(true);
    }

    // Protected Methods

    @Override
    protected void initialize(ISpatialView<?, ?, ?> view) {
        getView().initialize(this, LocalizationManager.getInstance().getSite(),
                getModel().getConfigurationManager().getSiteID(),
                spatialEntityManager.getSelectedSpatialEntityIdentifiers(),
                getPriorityForDragAndDropGeometryEdits(),
                getModel().getConfigurationManager()
                        .getColor(HazardConstants.SELECT_BY_AREA_COLOR));
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
        getView().getEditabilityChanger()
                .setStateChangeHandler(editabilityChangeHandler);
        getView().getSelectedSpatialEntityIdentifiersChanger()
                .setStateChangeHandler(selectedEntityIdentifiersChangeHandler);
        getView().getCreateShapeInvoker()
                .setCommandInvocationHandler(createShapeInvocationHandler);
        getView().getModifyGeometryInvoker()
                .setCommandInvocationHandler(modifyGeometryInvocationHandler);
        getView().getSelectByAreaInvoker()
                .setCommandInvocationHandler(selectByAreaInvocationHandler);
        getView().getSelectLocationInvoker()
                .setCommandInvocationHandler(selectLocationInvocationHandler);
        getView().getGageActionInvoker()
                .setCommandInvocationHandler(gageActionInvocationHandler);
        getView().getToggleChanger().setStateChangeHandler(toggleChangeHandler);
        getView().getCommandInvoker()
                .setCommandInvocationHandler(commandInvocationHandler);

        updateAllDisplayables(false);
    }

    @Override
    protected void reinitialize(ISpatialView<?, ?, ?> view) {

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
        final ISessionEventManager eventManager = getModel().getEventManager();
        ISessionSelectionManager selectionManager = getModel()
                .getSelectionManager();
        ISessionLockManager lockManager = getModel().getLockManager();
        if (entityIdentifier instanceof IHazardEventEntityIdentifier) {
            eventManager.setCurrentEvent(
                    ((IHazardEventEntityIdentifier) entityIdentifier)
                            .getEventIdentifier());
        } else {
            eventManager.setCurrentEvent((String) null);
        }

        /*
         * Create the context menu helper to generate the menu items.
         */
        ContextMenuHelper helper = new ContextMenuHelper(getModel(), scheduler,
                this);

        /*
         * Create the manage hazards menu items, if any, on a submenu.
         */
        List<IAction> actions = new ArrayList<>();
        List<IContributionItem> items = helper.getSelectedHazardManagementItems(
                UIOriginator.SPATIAL_DISPLAY, null);
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
        if (eventManager.isCurrentEvent() && (lockManager
                .getHazardEventLockStatus(eventManager.getCurrentEvent()
                        .getEventID()) != LockStatus.LOCKED_BY_OTHER)) {
            GeometryResolution resolution = getGeometryResolutionForEvent(
                    eventManager.getCurrentEvent());
            if (resolution == GeometryResolution.LOW) {
                spatialItems.add(createContributionItem(
                        CONTEXT_MENU_HIGH_RESOLUTION_GEOMETRY_FOR_CURRENT_EVENT,
                        new Runnable() {

                            @Override
                            public void run() {
                                eventManager
                                        .setHighResolutionGeometryVisibleForCurrentEvent(
                                                UIOriginator.SPATIAL_DISPLAY);
                            }
                        }, scheduler));
            } else if (resolution == GeometryResolution.HIGH) {
                spatialItems.add(createContributionItem(
                        CONTEXT_MENU_LOW_RESOLUTION_GEOMETRY_FOR_CURRENT_EVENT,
                        new Runnable() {

                            @Override
                            public void run() {
                                eventManager
                                        .setLowResolutionGeometryVisibleForCurrentEvent(
                                                UIOriginator.SPATIAL_DISPLAY);
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

        List<IHazardEventView> selectedEvents = selectionManager
                .getSelectedEvents();
        for (

        IHazardEventView event : selectedEvents)

        {
            if (lockManager.getHazardEventLockStatus(
                    event.getEventID()) == LockStatus.LOCKED_BY_OTHER) {
                continue;
            }
            GeometryResolution resolution = getGeometryResolutionForEvent(
                    event);
            if (resolution != null) {
                resolutionsFound.add(resolution);
            }
            if (resolutionsFound.size() == GeometryResolution.values().length) {
                break;
            }
        }
        if (resolutionsFound.contains(GeometryResolution.LOW))

        {
            spatialItems.add(createContributionItem(
                    CONTEXT_MENU_HIGH_RESOLUTION_GEOMETRY_FOR_SELECTED_EVENTS,
                    new Runnable() {

                        @Override
                        public void run() {
                            eventManager
                                    .setHighResolutionGeometriesVisibleForSelectedEvents(
                                            UIOriginator.SPATIAL_DISPLAY);
                        }
                    }, scheduler));
        }
        if (resolutionsFound.contains(GeometryResolution.HIGH))

        {
            spatialItems.add(createContributionItem(
                    CONTEXT_MENU_LOW_RESOLUTION_GEOMETRY_FOR_SELECTED_EVENTS,
                    new Runnable() {

                        @Override
                        public void run() {
                            eventManager
                                    .setLowResolutionGeometriesVisibleForSelectedEvents(
                                            UIOriginator.SPATIAL_DISPLAY);
                        }
                    }, scheduler));
        }

        /*
         * Only add the add/remove shapes item to the submenu if there is only
         * one event selected, and if the event was built via a select-by-area
         * process.
         */
        if (selectedEvents.size() == 1)

        {
            IHazardEventView event = selectedEvents.get(0);
            List<?> contextMenuEntries = (List<?>) event.getHazardAttribute(
                    HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY);
            if ((lockManager.getHazardEventLockStatus(
                    event.getEventID()) != LockStatus.LOCKED_BY_OTHER)
                    && contextMenuEntries != null) {
                ISessionConfigurationManager<ObservedSettings> configManager = getModel()
                        .getConfigurationManager();
                for (Object contextMenuEntry : contextMenuEntries) {
                    if (contextMenuEntry.equals(
                            HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES)) {
                        if (eventManager
                                .canEventAreaBeChanged(event) == false) {
                            continue;
                        }
                        String hazardType = event.getHazardType();
                        if (hazardType != null) {
                            HazardTypeEntry hazardTypeInfo = configManager
                                    .getHazardTypes().get(hazardType);
                            if ((hazardTypeInfo
                                    .getHatchingStyle() == HatchingStyle.NONE)
                                    || ((hazardTypeInfo
                                            .isAllowAreaChange() == false)
                                            && HazardStatus
                                                    .issuedButNotEndedOrElapsed(
                                                            event.getStatus()))) {
                                continue;
                            }
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

    /**
     * Determine whether or not a gage action is available.
     * <p>
     * TODO: This method needs to be run from some new subclass of
     * {@link ICommandInvocationHandler} that returns a result. Currently this
     * is being called directly from the view, which is incorrect; only time
     * crunches prevent the implementation of the necessary changes.
     * Furthermore, this is used by the view to configure a menu item, and it
     * needs to run in the main (worker) thread. This means that when a separate
     * thread is used in the future as a worker thread for presenters and the
     * model, {@link IRunnableAsynchronousScheduler} will need to be augmented
     * to include the ability to synchronously call a method that returns a
     * value. Currently, said interface only includes a method for scheduling
     * asynchronous executions of runnables that do not return anything.
     * </p>
     * 
     * @return <code>true</code> if a gage action is available,
     *         <code>false</code> otherwise.
     * @deprecated The method itself is not deprecated, but its visibility is;
     *             it must be invoked by the subclass of
     *             <code>ICommandInvocationHandler</code> mentioned in the to-do
     *             discussion.
     */
    @Deprecated
    boolean isGageActionAvailable() {
        return (getGagePointFirstRecommender() != null);
    }

    // Private Methods

    /**
     * Handle the user creation of a new shape.
     * 
     * @param geometry
     *            New geometry that has been created.
     */
    private void handleUserShapeCreation(IAdvancedGeometry geometry) {

        /*
         * Do nothing with the geometry if it is not valid.
         */
        if (geometry.isValid() == false) {
            statusHandler.handle(Priority.WARN,
                    "Invalid geometry: "
                            + geometry.getValidityProblemDescription()
                            + "; new geometry will not be used.");
            return;
        }

        /*
         * If there is a tool visual feature that is able to accept a new
         * geometry, create a copy of it, but with the new geometry, and replace
         * the old, non-geometried one with the new one. Then notify the
         * recommender manager of the change.
         */
        if (toolVisualFeatureAcceptingNewGeometry != null) {
            VisualFeature newVisualFeature = new VisualFeature(
                    toolVisualFeatureAcceptingNewGeometry, geometry);
            toolVisualFeatureAcceptingNewGeometry = null;
            toolVisualFeatures.set(toolVisualFeatures.size() - 1,
                    newVisualFeature);
            spatialEntityManager.updateEntitiesForToolVisualFeatures(
                    toolVisualFeatures, spatialInfoCollectingContext);
            getModel().getRecommenderManager().spatialParametersChanged(
                    spatialInfoCollectingContext,
                    Sets.newHashSet(newVisualFeature.getIdentifier()),
                    toolVisualFeatures);
            return;
        }

        /*
         * Since the geometry was not consumed by a tool visual feature, make a
         * new hazard event out of it.
         */
        buildHazardEvent(geometry);
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
        if (context
                .getIdentifier() instanceof ToolVisualFeatureEntityIdentifier) {

            /*
             * Find and modify the visual feature, then notify the recommender
             * manager of the change.
             */
            String visualFeatureIdentifier = ((ToolVisualFeatureEntityIdentifier) context
                    .getIdentifier()).getVisualFeatureIdentifier();
            VisualFeature feature = toolVisualFeatures
                    .getByIdentifier(visualFeatureIdentifier);
            if (feature != null) {
                feature.setGeometry(context.getSelectedTime(),
                        context.getGeometry());
                toolVisualFeatures.replace(feature);
                spatialEntityManager.updateEntitiesForToolVisualFeatures(
                        toolVisualFeatures, spatialInfoCollectingContext);
                getModel().getRecommenderManager().spatialParametersChanged(
                        spatialInfoCollectingContext,
                        Sets.newHashSet(visualFeatureIdentifier),
                        toolVisualFeatures);
            }
        } else {

            /*
             * Find the event that goes with the spatial entity.
             */
            IHazardEventEntityIdentifier hazardIdentifier = (IHazardEventEntityIdentifier) context
                    .getIdentifier();
            ISessionEventManager eventManager = getModel().getEventManager();
            IHazardEventView event = eventManager
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
                    VisualFeature feature = event.getVisualFeature(
                            ((HazardEventVisualFeatureEntityIdentifier) hazardIdentifier)
                                    .getVisualFeatureIdentifier());
                    if (feature != null) {
                        feature.setGeometry(
                                getTimeAtResolutionForEvent(event.getEventID(),
                                        context.getSelectedTime()),
                                context.getGeometry());

                        /*
                         * Replace the visual feature; if the replacement is
                         * rejected, redraw the event containing the visual
                         * feature.
                         */
                        if (eventManager.changeEventProperty(event,
                                ISessionEventManager.REPLACE_EVENT_VISUAL_FEATURE,
                                feature,
                                UIOriginator.SPATIAL_DISPLAY) != ISessionEventManager.EventPropertyChangeResult.SUCCESS) {
                            spatialEntityManager.replaceEntitiesForEvent(event,
                                    false, true);
                        }
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
                     * Attempt to set the event geometry; if it works, ensure
                     * that select-by-area cannot be used with this event, and
                     * if instead the geometry is rejected, redraw the event
                     * containing the geometry.
                     */
                    if (eventManager.changeEventProperty(event,
                            ISessionEventManager.SET_EVENT_GEOMETRY,
                            context.getGeometry(),
                            UIOriginator.SPATIAL_DISPLAY) == ISessionEventManager.EventPropertyChangeResult.SUCCESS) {
                        eventManager.changeEventProperty(event,
                                ISessionEventManager.REMOVE_EVENT_ATTRIBUTE,
                                HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY,
                                UIOriginator.SPATIAL_DISPLAY);
                    } else {
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
        ISessionEventManager eventManager = getModel().getEventManager();
        if (context.getIdentifier() != null) {
            eventIdentifier = ((IHazardEventEntityIdentifier) context
                    .getIdentifier()).getEventIdentifier();
            if (eventManager.changeEventProperty(
                    eventManager.getEventById(eventIdentifier),
                    ISessionEventManager.SET_EVENT_GEOMETRY, newGeometry,
                    UIOriginator.SPATIAL_DISPLAY) != ISessionEventManager.EventPropertyChangeResult.SUCCESS) {
                spatialEntityManager.replaceEntitiesForEvent(
                        eventManager.getEventById(eventIdentifier), false,
                        true);
            }
        } else {

            /*
             * Create the event.
             */
            IHazardEventView hazardEvent = buildHazardEvent(newGeometry);
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
            eventManager.changeEventProperty(hazardEvent,
                    ISessionEventManager.ADD_EVENT_ATTRIBUTE,
                    new Pair<String, Serializable>(
                            HazardConstants.GEOMETRY_REFERENCE_KEY,
                            context.getDatabaseTableName()),
                    UIOriginator.SPATIAL_DISPLAY);
            eventManager.changeEventProperty(hazardEvent,
                    ISessionEventManager.ADD_EVENT_ATTRIBUTE,
                    new Pair<String, Serializable>(
                            HazardConstants.GEOMETRY_MAP_NAME_KEY,
                            context.getLegend()),
                    UIOriginator.SPATIAL_DISPLAY);
            eventManager.changeEventProperty(hazardEvent,
                    ISessionEventManager.ADD_EVENT_ATTRIBUTE,
                    new Pair<String, Serializable>(
                            HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY,
                            Lists.newArrayList(
                                    HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES)),
                    UIOriginator.SPATIAL_DISPLAY);
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
                eventIdentifiers.add(((IHazardEventEntityIdentifier) identifier)
                        .getEventIdentifier());
            }
        }

        /*
         * Ensure that the model is still around before attempting to select the
         * event identifiers, since this method may be called as Hazard Services
         * is closing.
         */
        ISessionManager<ObservedSettings> sessionManager = getModel();
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
        getModel().getEventManager().addOrRemoveEnclosingUgcs(location,
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
         * If the gage identifier is not provided, notify the user and do
         * nothing more.
         */
        if (gageIdentifier == null) {
            displayHandler.getWarner().warnUser("Error",
                    "A gage must be selected to use this feature.");
            return;
        }

        /*
         * Ensure that the selected gage is within the Hydrologic Service Area.
         */
        String siteIdentifier = getModel().getSiteId();
        String hydroServiceArea = riverForecastManager
                .getHydrologicServiceAreaIdForGage(gageIdentifier);
        if ((siteIdentifier == null) || (hydroServiceArea == null)
                || (siteIdentifier.equals(hydroServiceArea) == false)) {
            displayHandler.getWarner().warnUser("Error",
                    "The selected gage is not within the current Hydrologic "
                            + "Service Area.\n\nUnable to execute the recommender.");
            return;
        }

        /*
         * If the configuration includes a gage-first recommender, run it using
         * the gage identifier as the value for a selected point identifier in a
         * dictionary that is provided as additional event set attributes.
         */
        String gagePointFirstRecommender = getGagePointFirstRecommender();
        if (gagePointFirstRecommender != null) {
            Map<String, Serializable> gageInfo = new HashMap<>();
            gageInfo.put(HazardConstants.SELECTED_POINT_ID, gageIdentifier);
            getModel().getRecommenderManager().runRecommender(
                    gagePointFirstRecommender,
                    RecommenderExecutionContext.getEmptyContext(gageInfo));
        }
    }

    /**
     * Get the gage-point-first recommender to be used for gage actions, if any.
     * 
     * @return Identifier of the recommender to be used for gage actions, or
     *         <code>null</code> if there is no such recommender.
     */
    private String getGagePointFirstRecommender() {
        StartUpConfig startupConfig = getModel().getConfigurationManager()
                .getStartUpConfig();
        String gagePointFirstRecommender = startupConfig
                .getGagePointFirstRecommender();
        return ((gagePointFirstRecommender == null)
                || gagePointFirstRecommender.isEmpty() ? null
                        : gagePointFirstRecommender);
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
        ISessionManager<ObservedSettings> sessionManager = getModel();
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
            IHazardEventView event) {
        if (event.getHazardType() == null) {
            return null;
        }
        return (event.getHazardAttribute(VISIBLE_GEOMETRY)
                .equals(LOW_RESOLUTION_GEOMETRY_IS_VISIBLE)
                        ? GeometryResolution.LOW : GeometryResolution.HIGH);
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
        List<IHazardEventView> selectedEvents = getModel().getSelectionManager()
                .getSelectedEvents();
        if (selectedEvents.size() != 1) {
            return null;
        }

        /*
         * If the event's geometry was created using select-by-area, then get
         * the necessary information and return it.
         */
        IHazardEventView event = selectedEvents.get(0);
        if (event.getHazardAttributes()
                .containsKey(HazardConstants.GEOMETRY_REFERENCE_KEY)) {
            String eventIdentifier = event.getEventID();
            SpatialEntity<? extends IEntityIdentifier> spatialEntity = spatialEntityManager
                    .getFirstPolygonalSpatialEntityForEvent(eventIdentifier);
            if (spatialEntity == null) {
                return null;
            }
            Set<Geometry> selectByAreaGeometries = selectByAreaGeometriesForEventIdentifiers
                    .get(eventIdentifier);
            return new SelectByAreaContext(spatialEntity.getIdentifier(),
                    (selectByAreaGeometries == null ? new HashSet<Geometry>()
                            : selectByAreaGeometries),
                    (String) event.getHazardAttribute(
                            HazardConstants.GEOMETRY_REFERENCE_KEY),
                    (String) event.getHazardAttribute(
                            HazardConstants.GEOMETRY_MAP_NAME_KEY));
        }
        return null;
    }

    /**
     * Build a hazard event around the specified geometry.
     * 
     * @param geometry
     *            Geometry with which to build the event.
     * @return Hazard event, or <code>null</code> if the geometry was checked
     *         and found to be invalid.
     */
    private IHazardEventView buildHazardEvent(IAdvancedGeometry geometry) {
        IHazardEvent hazardEvent = new SessionHazardEvent(
                CAVEMode.OPERATIONAL.equals(CAVEMode.getMode()) == false);
        hazardEvent.setGeometry(geometry);
        hazardEvent
                .setCreationTime(getModel().getTimeManager().getCurrentTime());
        return addEvent(hazardEvent);
    }

    /**
     * Add the specified hazard event to the event manager, or add its geometry
     * to an already selected event if appropriate.
     * 
     * @param event
     *            Event to be added.
     * @return Resulting new (or if adding geometry to selected, existing) event
     *         from the event manager, or <code>null</code> if a problem
     *         occurred.
     */
    private IHazardEventView addEvent(IHazardEvent event) {

        /*
         * Update the event user and workstation based on who created the event.
         */
        event.setWsId(VizApp.getWsId());

        /*
         * Add the event to the session.
         */
        try {
            getModel().startBatchedChanges();
            IHazardEventView addedEvent = getModel().getEventManager()
                    .addEvent(event, UIOriginator.SPATIAL_DISPLAY);
            getModel().getEventManager().changeEventProperty(addedEvent,
                    ISessionEventManager.SET_EVENT_TYPE_TO_DEFAULT, null,
                    UIOriginator.SPATIAL_DISPLAY);
            return addedEvent;
        } catch (HazardEventServiceException e) {
            statusHandler.error("Could not add new hazard event.", e);
            return null;
        } finally {
            getModel().finishBatchedChanges();
        }
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
    private Date getTimeAtResolutionForEvent(String eventIdentifier,
            Date time) {
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

        /*
         * Do nothing unless at least one hazard event is selected.
         */
        List<IHazardEventView> selectedEvents = getModel().getSelectionManager()
                .getSelectedEvents();
        if (selectedEvents.isEmpty() == false) {

            /*
             * Get the list of geometries taken from the spatial entities for
             * the selected hazard events. Note, however, that some hazard
             * events may have visual features showing, potentially none of
             * which are marked as to be used for centering. To ensure that
             * centering in such cases happens based upon the base geometry of
             * such events, iterate through the selected events, and for each
             * that does not already have a geometry included in the returned
             * geometries, include its base geometry.
             */
            Pair<List<Geometry>, Set<String>> geometriesAndEventIdentifiers = spatialEntityManager
                    .getGeometriesOfSpatialEntitiesToBeUsedForCentering();
            List<Geometry> geometriesOfSelected = geometriesAndEventIdentifiers
                    .getFirst();
            Set<String> eventIdentifiersAlreadyIncluded = geometriesAndEventIdentifiers
                    .getSecond();
            for (IHazardEventView selectedEvent : selectedEvents) {
                if (eventIdentifiersAlreadyIncluded
                        .contains(selectedEvent.getEventID()) == false) {
                    geometriesOfSelected
                            .add(selectedEvent.getFlattenedGeometry());
                }
            }

            /*
             * Create a collection of the geometries, and get its centroid to be
             * used as the centering point and its envelope to be used to
             * determine its bounds.
             */
            GeometryCollection geometryCollection = geometryFactory
                    .createGeometryCollection(geometriesOfSelected.toArray(
                            new Geometry[geometriesOfSelected.size()]));
            Point center = geometryCollection.getCentroid();
            Coordinate[] hullCoordinates = geometryCollection.convexHull()
                    .getCoordinates();

            /*
             * Center and zoom based upon the calculated center point and
             * bounds.
             */
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
        selectedEventIdentifiers.addAll(
                getModel().getSelectionManager().getSelectedEventIdentifiers());
    }

    /**
     * Update the geometry editing actions available in response to the selected
     * event(s) changing in some way. This method should be called only after
     * spatial entities have been brought up to date to match the current state
     * of the events, since it relies upon the latter to determine what types of
     * geometry editing actions are possible.
     * 
     * @param rememberSelectedAction
     *            Flag indicating whether or not to remember the geometry
     *            editing action that was selected prior to this method call,
     *            for potential restoration later. This flag is only meaningful
     *            if this invocation determines that only a restricted number of
     *            geometry editing actions should available, as in such cases it
     *            indicates that the previously selected action should be
     *            recorded so that it may be restored if and when this method is
     *            subsequently invoked and finds that the full suite of geometry
     *            editing actions should be available.
     */
    private void updateGeometryEditingActions(boolean rememberSelectedAction) {

        /*
         * Determine whether or not geometry editing should be allowed.
         */
        boolean editable = (((selectedEventIdentifiers.size() == 1)
                && spatialEntityManager
                        .isAtLeastOneSelectedSpatialEntityEditable())
                || ((selectedEventIdentifiers.size() < 2)
                        && spatialEntityManager
                                .isAtLeastOneToolSpatialEntityEditable()));

        /*
         * Enable the combination of geometries via various operations only if
         * there is exactly one event selected and it has at least one spatial
         * entity that is editable.
         */
        getView().setCombineGeometryOperationsEnabled(editable,
                rememberSelectedAction);
    }

    /**
     * Update the priority for drag-and-drop geometry edits.
     */
    private void updatePriorityForDragAndDropGeometryEdits() {
        getView().setPriorityForDragAndDropGeometryEdits(
                getPriorityForDragAndDropGeometryEdits());
    }

    /**
     * Get the priority for drag-and-drop geometry edits.
     * 
     * @return Priority for drag-and-drop geometry edits.
     */
    private DragAndDropGeometryEditSource getPriorityForDragAndDropGeometryEdits() {
        return getModel().getConfigurationManager()
                .<DragAndDropGeometryEditSource> getSettingsValue(
                        HazardConstants.PRIORITY_FOR_DRAG_AND_DROP_GEOMETRY_EDITS,
                        getModel().getConfigurationManager().getSettings());
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
