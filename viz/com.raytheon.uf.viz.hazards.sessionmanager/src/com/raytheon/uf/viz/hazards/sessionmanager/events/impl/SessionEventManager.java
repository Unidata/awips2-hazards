/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
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
package com.raytheon.uf.viz.hazards.sessionmanager.events.impl;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ATTR_HAZARD_CATEGORY;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ATTR_ISSUED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ETNS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.EXPIRATION_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.FORECAST_POINT;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_AREA;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_AREA_ALL;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_AREA_INTERSECTION;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_AREA_NONE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_CHECKED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_END_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SELECTED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_START_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_STATUS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ISSUE_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PILS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.REPLACED_BY;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.TIME_RANGE_MINIMUM_INTERVAL;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.VTEC_CODES;
import gov.noaa.gsd.viz.megawidgets.IParentSpecifier;
import gov.noaa.gsd.viz.megawidgets.ISpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetPropertyException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.megawidgets.TimeMegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.validators.SingleTimeDeltaStringChoiceValidatorHelper;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.time.DateUtils;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.Significance;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardQueryBuilder;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.hazards.hydro.RiverForecastManager;
import com.raytheon.uf.common.hazards.hydro.RiverPointZoneInfo;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.ProductGenerationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.ISimulatedTimeChangeListener;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.HazardEventMetadata;
import com.raytheon.uf.viz.hazards.sessionmanager.config.IEventModifyingScriptJobListener;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ModifiedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventGeometryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventMetadataModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventScriptExtraDataAvailable;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTimeRangeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTypeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsTimeRangeBoundariesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionLastChangedEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventConflictsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.geomaps.GeoMapUtilities;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger.IEventApplier;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger.IRiseCrestFallEditor;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.undoable.IUndoRedoable;
import com.raytheon.viz.core.mode.CAVEMode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;

/**
 * Implementation of ISessionEventManager
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2013 1257       bsteffen    Initial creation
 * Jul 19, 2013 1257       bsteffen    Notification support for session manager.
 * Sep 10, 2013  752       blawrenc    Modified addEvent to check if the event
 *                                     being added already exists.
 * Sep 12, 2013 717        jsanchez    Converted certain hazard events to grids.
 * Oct 21, 2013 2177       blawrenc    Added logic to check for event conflicts.
 * Oct 23, 2013 2277       jsanchez    Removed HazardEventConverter from viz.
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 29, 2013 2378       blawrenc    Changed to not set modified
 *                                     events back to PENDING.
 * Nov 29, 2013 2380       daniel.s.schaffer@noaa.gov Fixing bugs in settings-based filtering
 * Jan 14, 2014 2755       bkowal      No longer create new Event IDs for events that
 *                                     are created EDEX-side for interoperability purposes.
 * Feb 17, 2014 2161       Chris.Golden Added code to change the end time or fall-
 *                                      below time to the "until further notice"
 *                                      value if the corresponding "until further
 *                                      notice" flag is set to true. Also added code
 *                                      to track the set of hazard events that can
 *                                      have "until further notice" applied to
 *                                      them. Added Javadoc comments to appropriate
 *                                      methods (those that post notifications on
 *                                      the event bus) identifying them as potential
 *                                      hooks into addition/removal/modification of
 *                                      events.
 * Mar 3, 2014  3034       bkowal      Constant for GFE interoperability flag
 * Apr 28, 2014 3556       bkowal      Updated to use the new hazards common 
 *                                     configuration plugin.
 * Apr 29, 2014 2925       Chris.Golden Moved business logic that was scattered
 *                                      elsewhere into here where it belongs. Also
 *                                      changed notifications being posted to be
 *                                      asynchronous, added notification posting for
 *                                      for when the allowable "until further notice"
 *                                      set has changed, and changed logic of "until
 *                                      further notice" to use the old value for the
 *                                      corresponding attribute or end time when
 *                                      possible when "until further notice" is
 *                                      toggled off. Also added fetching and caching
 *                                      of megawidget specifier managers for hazard
 *                                      events, in support of class-based metadata
 *                                      work.
 * May 15, 2014 2925       Chris.Golden Added methods to set hazard category, set
 *                                      last modified event, and get set of hazards
 *                                      for which proposal is possible. Also added
 *                                      tracking of which selected events have what
 *                                      conflicts with other events, as opposed to
 *                                      calculating it every time it is asked for via
 *                                      the public method. Made a few additional
 *                                      minor changes to support new HID.
 * Jun 24, 2014 4010       Chris.Golden Fixed bug uncovered by testing use of expand
 *                                      bar megawidgets within the event metadata
 *                                      specifiers that caused "until further notice"
 *                                      functionality to fail and give a warning if
 *                                      a time scale megawidget to be manipulated
 *                                      via an UFN checkbox was not a top-level
 *                                      megawidget, but was instead embedded within
 *                                      a parent megawidget.
 * Jun 25, 2014 4009       Chris.Golden Removed all code related to "until further
 *                                      notice" for arbitrary attribute values; only
 *                                      the end time "until further notice" code
 *                                      belongs here. The rest has been moved to
 *                                      interdependency scripts that go with the
 *                                      appropriate metadata megawidget specifiers.
 * Jul 03, 2014 3512       Chris.Golden Added code to set new events, and those that
 *                                      have undergone a type change, to have end
 *                                      times equal to their start times plus their
 *                                      default durations (by event type). Also
 *                                      changed to erase any recorded interval
 *                                      between start and end time before "until
 *                                      further notice" was turned on for an event
 *                                      if the type of the event changes.
 * Aug 20, 2014 4243       Chris.Golden Added implementation of new method to receive
 *                                      notification of a script command having been
 *                                      invoked.
 * Sep 04, 2014 4560       Chris.Golden Added code to find metadata-reload-triggering
 *                                      megawidgets.
 * Sep 16, 2014 4753       Chris.Golden Changed event script to include mutable
 *                                      properties.
 * Nov 18, 2014 4124       Chris.Golden Changed to work with revamped time manager.
 * Dec  1, 2014 4188       Dan Schaffer Now allowing hazards to be shrunk or expanded
 *                                      when appropriate.
 * Dec 05, 2014 4124       Chris.Golden Changed to work with parameterized config
 *                                      manager, and to properly use ObservedSettings.
 * Dec 13, 2014 4486       Dan Schaffer Eliminating effect of changed CAVE time on
 *                                      hazard status.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * Jan 08, 2015 5700       Chris.Golden Changed to generalize the meaning of a command
 *                                      invocation for a particular event, since it no
 *                                      longer only means that an event-modifying
 *                                      script is to be executed; it may also trigger
 *                                      a metadata refresh. Previously, the latter was
 *                                      only possible on a hazard attribute state
 * Jan 22, 2015 4959       Dan Schaffer Ability to right click to add/remove UGCs from hazards.
 * Jan 26, 2015 5952       Dan Schaffer Fix incorrect hazard area designation.
 * Feb  2, 2015 4930       Dan Schaffer Fixed problem where reduction of multi-polygons 
 *                                      can still yield a geometry with more than 20 points.
 * Feb  3, 2015 2331       Chris.Golden Added code to track the allowable boundaries of
 *                                      all hazard events' start and end times, so that
 *                                      the user will not move them beyond the allowed
 *                                      ranges. Also added code to advance the start and/
 *                                      or end times of events as time ticks forward when
 *                                      appropriate.
 * Feb 12, 2015 4959       Dan Schaffer Modify MB3 add/remove UGCs to match Warngen
 * Feb 17, 2015 3847       Chris.Golden Added edit-rise-crest-fall metadata trigger.
 * Feb 21, 2015 4959       Dan Schaffer Improvements to add/remove UGCs
 * Feb 24, 2015 6499       Dan Schaffer Only allow add/remove UGCs for pending point hazards
 * Feb 24, 2015 2331       Chris.Golden Added check of any event that is added to the
 *                                      list of events to ensure that it does not have
 *                                      until further notice set if such is not allowed.
 * Mar 13, 2015 6090       Dan Schaffer Fixed goosenecks
 * Mar 13, 2015 6922       Chris.Cody   Changes to skip re-query on GraphicalEditor cancel
 * Mar 24, 2015 6090       Dan Schaffer Goosenecks now working as they do in Warngen
 * Mar 25, 2015 7102       Chris.Golden Changed behavior of start time limiting to make
 *                                      start times of some hazard events (those that do not
 *                                      have to have start time be current time) be no
 *                                      longer limited after the event is issued (until it
 *                                      is ending). Also, hazard events that are reissued
 *                                      do not have their start times jumped forward to the
 *                                      current time; the start time they had when first
 *                                      issued is the start time they keep by default. Also
 *                                      put code in to catch the cases where no start time
 *                                      is saved for a previously-issued event; this is a
 *                                      bug that is probably no longer occurring, but since
 *                                      I wasn't able to reproduce it I added this code to
 *                                      ensure that H.S. will not be left in a bad state.
 *                                      Finally, fixed bug that caused events that went
 *                                      directly to ENDED status (no intermediate ENDING)
 *                                      to still allow their start and end times to be
 *                                      changed.
 * Apr 06, 2015   7272     mduff        Adding changes for Guava upgrade.  Last changes lost in merge.
 * Apr 14, 2015   6935     Chris.Golden Fixed bug that caused duration choices for hazard
 *                                      events to lag behind event type (e.g. when event
 *                                      type of unissued FF.W.NonConvective was changed to
 *                                      FF.W.BurnScar).
 * Apr 10, 2015 6898       Chris.Cody   Refactored async messaging
 * Apr 27, 2015 7635       Robert.Blum  Added current config site to list of visible sites for 
 *                                      when settings have not been overridden.
 * May 05, 2015    7624    mduff        Added getEventsById
 * May 14, 2015    7560    mpduff       Trying to get the Time Range to update from Graphical Editor.
 * May 19, 2015    7975    Robert.Blum  Fixed bug that could incorrectly set the hazard status to ended
 *                                      if it was reverted and contained the REPLACED_BY attribute.
 * May 19, 2015    7706    Robert.Blum  Fixed bug when checking for conflicts where it would check hazards
 *                                      that were ended.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionEventManager implements
        ISessionEventManager<ObservedHazardEvent> {

    private static final String POINT_ID = "id";

    // Public Static Constants

    /**
     * Comparator can be used with sortEvents to send selected events to the
     * front of the list.
     */
    public static final Comparator<ObservedHazardEvent> SEND_SELECTED_FRONT = new Comparator<ObservedHazardEvent>() {

        @Override
        public int compare(ObservedHazardEvent o1, ObservedHazardEvent o2) {
            boolean s1 = Boolean.TRUE.equals(o1
                    .getHazardAttribute(HAZARD_EVENT_SELECTED));
            boolean s2 = Boolean.TRUE.equals(o2
                    .getHazardAttribute(HAZARD_EVENT_SELECTED));
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
     * Comparator can be used with sortEvents to send selected events to the
     * back of the list.
     */
    public static final Comparator<ObservedHazardEvent> SEND_SELECTED_BACK = Collections
            .reverseOrder(SEND_SELECTED_FRONT);

    // Private Static Constants

    private static final String GEOMETRY_MODIFICATION_ERROR = "Geometry Modification Error";

    private static final String EMPTY_GEOMETRY_ERROR = "Deleting this UGC would leave the hazard with an empty geometry";

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SessionEventManager.class);

    /**
     * Default distance tolerance and increment for use in geometry point
     * reduction algorithm.
     */
    private static final double DEFAULT_DISTANCE_TOLERANCE = 0.001f;

    private static final double DEFAULT_DISTANCE_TOLERANCE_INCREMENT = 0.001f;

    // Private Variables

    private final ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager;

    /*
     * A full configuration manager is needed to get access to hazard types,
     * which is not exposed in ISessionConfigurationManager
     */
    private final ISessionConfigurationManager<ObservedSettings> configManager;

    private final ISessionTimeManager timeManager;

    private final IHazardEventManager dbManager;

    private final ISessionNotificationSender notificationSender;

    private final List<ObservedHazardEvent> events = new ArrayList<ObservedHazardEvent>();

    private final Deque<String> eventModifications = new LinkedList<String>();

    private Timer eventExpirationTimer = new Timer(true);

    private final Map<String, TimerTask> expirationTasks = new ConcurrentHashMap<String, TimerTask>();

    private ISimulatedTimeChangeListener timeListener;

    private final Set<String> identifiersOfEventsAllowingUntilFurtherNotice = new HashSet<>();

    private final Map<String, Range<Long>> startTimeBoundariesForEventIdentifiers = new HashMap<>();

    private final Map<String, Range<Long>> endTimeBoundariesForEventIdentifiers = new HashMap<>();

    /**
     * Map pairing identifiers of issued events with the start times they had
     * when they were last issued. This information is needed in order to
     * determine what boundaries to use for the start times of issued events;
     * the boundaries should be based upon the start time at last issuance, not
     * what it may have been changed to since issued.
     */
    private final Map<String, Long> startTimesForIssuedEventIdentifiers = new HashMap<>();

    /**
     * Map pairing identifiers of issued events with either the end times they
     * had when they were last issued, or else the durations they had when last
     * issued. Those with relative (duration-type) end times will have the
     * latter stored here, while those with absolute end times will have the
     * former. This information is needed in order to determine what boundaries
     * to use for the end times of issued events; the boundaries should be based
     * upon the end time/duration at last issuance, not what it may have been
     * changed to since issued.
     */
    private final Map<String, Long> endTimesOrDurationsForIssuedEventIdentifiers = new HashMap<>();

    /**
     * Map pairing identifiers of events with lists of duration choices that are
     * valid for the events in their current states. Each of these lists is
     * fetched from the {@link ISessionConfigurationManager} when a hazard event
     * is added, and is then pruned of any choices that are unavailable to the
     * hazard event given its current status. As said status changes, this
     * process is repeated. Any hazard event that does not have a duration will
     * have an empty list associated with its identifier.
     */
    private final Map<String, List<String>> durationChoicesForEventIdentifiers = new HashMap<>();

    /**
     * Duration choice validator, used to convert lists of duration choice
     * strings fetched in order to populate
     * {@link #durationChoicesForEventIdentifiers} into time deltas in
     * milliseconds so that a determination may be made as to which durations
     * are allowed for a particular event given its end time limitations.
     */
    private final SingleTimeDeltaStringChoiceValidatorHelper durationChoiceValidator = new SingleTimeDeltaStringChoiceValidatorHelper(
            null);

    /**
     * Set of identifiers for all events that have "Ending" status.
     */
    private final Set<String> eventIdentifiersWithEndingStatus = new HashSet<>();

    private final Map<String, Collection<IHazardEvent>> conflictingEventsForSelectedEventIdentifiers = new HashMap<>();

    private final Map<String, MegawidgetSpecifierManager> megawidgetSpecifiersForEventIdentifiers = new HashMap<>();

    private final Map<String, Set<String>> metadataReloadTriggeringIdentifiersForEventIdentifiers = new HashMap<>();

    private final Map<String, Set<String>> editRiseCrestFallTriggeringIdentifiersForEventIdentifiers = new HashMap<>();

    private final Map<String, File> scriptFilesForEventIdentifiers = new HashMap<>();

    private final Map<String, Map<String, String>> eventModifyingScriptsForEventIdentifiers = new HashMap<>();

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

    private final GeometryFactory geometryFactory;

    private ObservedHazardEvent currentEvent;

    private final GeoMapUtilities geoMapUtilities;

    /**
     * Listener for event modifying script completions.
     */
    private final IEventModifyingScriptJobListener eventModifyingScriptListener = new IEventModifyingScriptJobListener() {

        @Override
        public void scriptExecutionComplete(String identifier,
                ModifiedHazardEvent hazardEvent) {
            eventModifyingScriptExecutionComplete(hazardEvent);
        }
    };

    private final RiverForecastManager riverForecastManager;

    // Public Constructors

    public SessionEventManager(
            ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager,
            ISessionTimeManager timeManager,
            ISessionConfigurationManager<ObservedSettings> configManager,
            IHazardEventManager dbManager,
            ISessionNotificationSender notificationSender, IMessenger messenger) {
        this.sessionManager = sessionManager;
        this.configManager = configManager;
        this.timeManager = timeManager;
        this.dbManager = dbManager;
        this.notificationSender = notificationSender;
        new SessionHazardNotificationListener(notificationSender);
        SimulatedTime.getSystemTime().addSimulatedTimeChangeListener(
                createTimeListener());
        this.messenger = messenger;
        geometryFactory = new GeometryFactory();
        this.geoMapUtilities = new GeoMapUtilities(this.configManager);
        this.riverForecastManager = new RiverForecastManager();

    }

    // Public Methods

    @Override
    public ObservedHazardEvent getEventById(String eventId) {
        for (ObservedHazardEvent event : getEvents()) {
            if (event.getEventID().equals(eventId)) {
                return event;
            }
        }
        return null;
    }

    @Override
    public List<ObservedHazardEvent> getEventsById(Collection<String> eventIds) {
        List<ObservedHazardEvent> events = new ArrayList<>();
        for (ObservedHazardEvent event : getEvents()) {
            if (eventIds.contains(event.getEventID())) {
                events.add(event);
            }
        }

        return events;
    }

    @Override
    public Collection<ObservedHazardEvent> getEventsByStatus(HazardStatus state) {
        Collection<ObservedHazardEvent> allEvents = getEvents();
        Collection<ObservedHazardEvent> events = new ArrayList<>(
                allEvents.size());
        for (ObservedHazardEvent event : allEvents) {
            if (event.getStatus().equals(state)) {
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public List<ObservedHazardEvent> getSelectedEvents() {

        /*
         * TODO: Consider having getEventsForCurrentSettings() return a list as
         * well. It's questionable as to whether that method should return a
         * list, because maybe implying an inherent ordering of the events is
         * incorrect; however, the order is relied upon (since the code here
         * iterates through all those events) to return a consistently ordered
         * set of events. Perhaps the order should be set via the GUI, since the
         * console allows sorting of events, and maybe the order in which those
         * events occur should provide the order in which the selected events,
         * which are after all in the console list, occur.
         * 
         * TODO: Consider having this list rebuilt each time selection changes
         * (by doing so before sending off a SessionSelectedEventsModified
         * message in SessionEventManager), and then simply returning an
         * unmodifiable version of that list each time this method is called. Or
         * better yet, simply use the same list each time, modifying it as
         * appropriate when the selected events are changing, and again simply
         * returning an unmodifiable view of it to callers of this method. In
         * this case, any caller that wished to cache the selected events list
         * for comparison to updates to it later (i.e. classes such as
         * HazardDetailPresenter) would need to make a copy of what they got
         * from invoking this method.
         */
        Collection<ObservedHazardEvent> allEvents = getEventsForCurrentSettings();
        List<ObservedHazardEvent> events = new ArrayList<>(allEvents.size());
        for (ObservedHazardEvent event : allEvents) {
            if (Boolean.TRUE.equals(event
                    .getHazardAttribute(HAZARD_EVENT_SELECTED))) {
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public boolean isSelected(ObservedHazardEvent event) {
        return getSelectedEvents().contains(event);

    }

    @Override
    public void setSelectedEvents(
            Collection<ObservedHazardEvent> selectedEvents,
            IOriginator originator) {

        for (ObservedHazardEvent event : getSelectedEvents()) {
            if (!selectedEvents.contains(event)) {
                if (Boolean.TRUE.equals(event
                        .getHazardAttribute(HAZARD_EVENT_SELECTED))) {
                    setEventAttributesModified(event, HAZARD_EVENT_SELECTED,
                            false, originator);
                }
            }
        }
        for (ObservedHazardEvent event : selectedEvents) {
            setEventAttributesModified(event, HAZARD_EVENT_SELECTED, true,
                    originator);

            /*
             * Once selected, a potential event or set of events should be set
             * to PENDING.
             */
            if (event.getStatus() == HazardStatus.POTENTIAL) {
                event.setStatus(HazardStatus.PENDING);
                setEventStatus(event, HazardStatus.PENDING, true, originator);

            }
        }
    }

    @Override
    public void setSelectedEventForIDs(Collection<String> selectedEventIDs,
            IOriginator originator) {
        Collection<ObservedHazardEvent> selectedEvents = fromIDs(selectedEventIDs);
        setSelectedEvents(selectedEvents, originator);
    }

    @Override
    public Collection<ObservedHazardEvent> getCheckedEvents() {
        Collection<ObservedHazardEvent> allEvents = getEventsForCurrentSettings();
        Collection<ObservedHazardEvent> events = new ArrayList<>(
                allEvents.size());
        for (ObservedHazardEvent event : allEvents) {
            if (Boolean.TRUE.equals(event
                    .getHazardAttribute(HAZARD_EVENT_CHECKED))) {
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public void setModifiedEventGeometry(String eventID, Geometry geometry,
            boolean checkGeometryValidity) {
        ObservedHazardEvent event = this.getEventById(eventID);
        setModifiedEventGeometry(event, geometry, checkGeometryValidity);
    }

    @Override
    public void setModifiedEventGeometry(ObservedHazardEvent event,
            Geometry geometry, boolean checkGeometryValidity) {
        if (event != null) {
            if (isValidGeometryChange(geometry, event, checkGeometryValidity)) {
                event.setGeometry(geometry);
                updateHazardAreas(event);
            }
        }
    }

    @Override
    public Collection<ObservedHazardEvent> getEventsForCurrentSettings() {
        Collection<ObservedHazardEvent> result = getEvents();

        filterEventsForConfig(result);
        return result;
    }

    @Override
    public void setEventCategory(ObservedHazardEvent event, String category,
            IOriginator originator) {
        if (!event.canChangeType()) {
            throw new IllegalStateException("cannot change type of event "
                    + event.getEventID());
        }
        event.addHazardAttribute(ATTR_HAZARD_CATEGORY, category);
        event.setHazardType(null, null, null);

        updateHazardAreas(event);
        hazardEventModified(new SessionEventTypeModified(event,
                Originator.OTHER));

    }

    @Override
    public boolean setEventType(ObservedHazardEvent event, String phenomenon,
            String significance, String subType, IOriginator originator) {
        ObservedHazardEvent oldEvent = null;

        /*
         * If the event cannot change type, create a new event with the new
         * type.
         */
        if (!event.canChangeType()) {
            oldEvent = event;
            IHazardEvent baseEvent = new BaseHazardEvent(event);
            baseEvent.setEventID("");
            baseEvent.setStatus(HazardStatus.PENDING);
            baseEvent.addHazardAttribute(HazardConstants.REPLACES,
                    configManager.getHeadline(oldEvent));

            /*
             * New event should not have product information.
             */
            baseEvent.removeHazardAttribute(EXPIRATION_TIME);
            baseEvent.removeHazardAttribute(ISSUE_TIME);
            baseEvent.removeHazardAttribute(VTEC_CODES);
            baseEvent.removeHazardAttribute(ETNS);
            baseEvent.removeHazardAttribute(PILS);
            baseEvent.removeHazardAttribute(REPLACED_BY);

            /*
             * The originator should be the session manager, since the addition
             * of a new event is occurring.
             */
            originator = Originator.OTHER;

            /*
             * Add the event, and add it to the selection as well. The old
             * selection is fetched before the addition, because the addition
             * will change the selection.
             */
            Collection<ObservedHazardEvent> selection = getSelectedEvents();
            event = addEvent(baseEvent, originator);
            selection.add(event);
            setSelectedEvents(selection, originator);
        }

        /*
         * Change the event type as specified, whether it is being set or
         * cleared.
         */
        if (phenomenon != null) {

            /*
             * This is tricky, but in replace-by operations you need to make
             * sure that modifications to the old event are completed before
             * modifications to the new event. This puts the new event at the
             * top of the modification queue which ultimately controls things
             * like which event tab gets focus in the HID. The originator is
             * also changed to the session manager, since any changes to the
             * type of the new event are being done by the session manager, not
             * by the original originator.
             */
            if (oldEvent != null) {
                IHazardEvent tempEvent = new BaseHazardEvent();
                tempEvent.setPhenomenon(phenomenon);
                tempEvent.setSignificance(significance);
                tempEvent.setSubType(subType);
                oldEvent.addHazardAttribute(REPLACED_BY,
                        configManager.getHeadline(tempEvent));
                oldEvent.setStatus(HazardStatus.ENDING);

                hazardEventModified(new SessionEventStatusModified(oldEvent,
                        originator));
            }

            /*
             * Assign the new type.
             */
            event.setHazardType(phenomenon, significance, subType);

            /*
             * Make sure the updated hazard type is a part of the visible types
             * in the current setting. If not, add it.
             */
            ISettings modSettings = configManager.getSettings();
            Set<String> visibleTypes = modSettings.getVisibleTypes();
            visibleTypes.add(HazardEventUtilities.getHazardType(event));
            configManager.updateCurrentSettings(modSettings, originator);

            updateHazardAreas(event);
        } else {
            event.setHazardType(null, null, null);
        }

        /*
         * Set the event's end time to have a distance from the start time equal
         * to its default duration. Also remove any recorded interval from
         * before "until further notice" had been turned on, in case it was,
         * since this could lead to the wrong interval being used for the new
         * event type if "until further notice" is subsequently turned off.
         */
        event.setEndTime(new Date(event.getStartTime().getTime()
                + configManager.getDefaultDuration(event)));
        event.removeHazardAttribute(END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE);

        updateEventMetadata(event);
        updateConflictingEventsForSelectedEventIdentifiers(event, false);

        hazardEventModified(new SessionEventTypeModified(event, originator));

        /*
         * Update the time boundaries and the duration choices for the event.
         */
        updateTimeBoundariesForEvents(event, false, false);
        updateDurationChoicesForEvent(event, false);

        return (originator != Originator.OTHER);
    }

    @Override
    public boolean setEventTimeRange(ObservedHazardEvent event, Date startTime,
            Date endTime, IOriginator originator) {

        /*
         * Ensure that the start time falls within its allowable boundaries.
         */
        long start = startTime.getTime();
        Range<Long> startBoundaries = startTimeBoundariesForEventIdentifiers
                .get(event.getEventID());
        Range<Long> endBoundaries = endTimeBoundariesForEventIdentifiers
                .get(event.getEventID());
        if ((start < startBoundaries.lowerEndpoint())
                || (start > startBoundaries.upperEndpoint())) {
            return false;
        }

        /*
         * Ensure the end time is at least the minimum interval distance from
         * the start time.
         */
        long end = endTime.getTime();
        if (end - start < HazardConstants.TIME_RANGE_MINIMUM_INTERVAL) {
            return false;
        }

        /*
         * If the event will now have "until further notice" as its end time, or
         * the event has a duration and the latter has not changed, shift
         * whichever (or both) end time boundaries to accommodate the new end
         * time and (if not "until further notice") whatever other possible
         * durations the event is allowed. This allows duration-equipped hazard
         * events that have their durations limited (they cannot be expanded, or
         * cannot be shrunk, or both) to still have their end times displaced as
         * the user changes the start time, and also allows "until further
         * notice" to be accommodated. Otherwise, ensure the event end time
         * falls within the correct bounds.
         */
        boolean hasDuration = (configManager.getDurationChoices(event)
                .isEmpty() == false);
        if ((end == HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS)
                || (hasDuration && (event.getEndTime().getTime()
                        - event.getStartTime().getTime() == end - start))) {
            updateEndTimeBoundariesForSingleEvent(event, start, end);
        } else if ((end < endBoundaries.lowerEndpoint())
                || (end > endBoundaries.upperEndpoint())) {
            return false;
        }

        /*
         * Set the new time range for the event.
         */
        event.setTimeRange(startTime, endTime);

        hazardEventModified(new SessionEventTimeRangeModified(event, originator));

        return true;
    }

    /**
     * Process (reload events) when Settings have been modified.
     * 
     */
    @Override
    public void reloadEventsForSettings() {
        loadEventsForSettings();
    }

    public void processHazardTypeChanged(IHazardEvent event) {
        updateTimeBoundariesForEvents(event, false, false);
        updateDurationChoicesForEvent(event, false);
        updateEventMetadata(event);
        updateConflictingEventsForSelectedEventIdentifiers(event, false);
    }

    /**
     * Respond to a hazard event's status change by firing off a notification
     * that the event may have new metadata. Also, if the event has changed its
     * status to ending, or reverted from ending back to issued, update its time
     * boundaries and duration choices.
     * 
     * @param change
     *            Change that occurred.
     */
    @Override
    public void processHazardStatusChanged(IHazardEvent event) {
        if ((event.getStatus() == HazardStatus.ENDING)
                || (event.getStatus() == HazardStatus.ENDED)) {
            if (event.getStatus() == HazardStatus.ENDING) {
                eventIdentifiersWithEndingStatus.add(event.getEventID());
            } else {
                eventIdentifiersWithEndingStatus.remove(event.getEventID());
            }
            updateTimeBoundariesForEvents(event, false, false);
            updateDurationChoicesForEvent(event, false);
        } else if ((event.getStatus() == HazardStatus.ISSUED)
                && eventIdentifiersWithEndingStatus
                        .contains(event.getEventID())) {
            eventIdentifiersWithEndingStatus.remove(event.getEventID());
            updateTimeBoundariesForEvents(event, false, false);
            updateDurationChoicesForEvent(event, false);
        }
        updateEventMetadata(event);
    }

    /**
     * Respond to the completion of product generation by updating the
     * associated events' parameters, and by recalculating said events' start
     * and end time boundaries if necessary.
     * 
     * @param productGenerationComplete
     *            Notification that is being received.
     */
    @Override
    public void processProductGenerationComplete(
            IProductGenerationComplete productGenerationComplete) {

        /*
         * If the product generation resulted in issuance, iterate through the
         * generated products, and for each one, iterate through the hazard
         * events used to generate it, updating their states as necessary.
         */
        if (productGenerationComplete.isIssued()) {
            for (GeneratedProductList generatedProductList : productGenerationComplete
                    .getGeneratedProducts()) {
                for (IEvent event : generatedProductList.getEventSet()) {
                    IHazardEvent hazardEvent = (IHazardEvent) event;
                    ObservedHazardEvent oEvent = getEventById(hazardEvent
                            .getEventID());

                    /*
                     * If the hazard is pending or proposed, make it issued;
                     * otherwise, if it needs a change to the ended status, do
                     * this.
                     */
                    HazardStatus hazardStatus = oEvent.getStatus();
                    boolean wasPreIssued = false;
                    if (hazardStatus.equals(HazardStatus.PENDING)
                            || hazardStatus.equals(HazardStatus.PROPOSED)) {
                        oEvent.setStatus(HazardStatus.ISSUED);
                        oEvent.clearUndoRedo();
                        oEvent.setModified(false);
                        wasPreIssued = true;
                    } else if (isChangeToEndedStatusNeeded(hazardEvent)) {
                        oEvent.setStatus(HazardStatus.ENDED);
                    }

                    /*
                     * If the hazard now has just changed to issued status (i.e.
                     * it has not just been changed to ended, or been reissued),
                     * adjust its start and end times and their boundaries. Then
                     * update its duration choices list, if applicable.
                     */
                    if (oEvent.getStatus().equals(HazardStatus.ISSUED)
                            || wasPreIssued) {
                        updateSavedTimesForEventIfIssued(oEvent, false);

                        updateTimeRangeBoundariesOfJustIssuedEvent(
                                oEvent,
                                (Long) hazardEvent
                                        .getHazardAttribute(HazardConstants.ISSUE_TIME));
                        updateDurationChoicesForEvent(oEvent, false);
                    }
                }
            }

            /*
             * Now that issuance is complete, reset the issuance-ongoing flag.
             */
            sessionManager.setIssueOngoing(false);
        }
    }

    /**
     * If an ending hazard is issued or an issued hazard is replaced, we need to
     * change it's state to ended.
     * 
     * @param hazardEvent
     * @return
     */
    private boolean isChangeToEndedStatusNeeded(IHazardEvent hazardEvent) {
        return hazardEvent.getStatus().equals(HazardStatus.ENDING)
                || hazardEvent.getHazardAttribute(REPLACED_BY) != null;
    }

    /**
     * Respond to a CAVE current time tick by updating all the events' start and
     * end time editability boundaries.
     */
    @Override
    public void processCurrentTimeChanged() {
        updateTimeBoundariesForEvents(null, false, true);
    }

    @Override
    public MegawidgetSpecifierManager getMegawidgetSpecifiers(
            ObservedHazardEvent event) {
        MegawidgetSpecifierManager manager = megawidgetSpecifiersForEventIdentifiers
                .get(event.getEventID());
        if (manager != null) {
            return manager;
        }
        updateEventMetadata(event);
        return megawidgetSpecifiersForEventIdentifiers.get(event.getEventID());
    }

    @Override
    public List<String> getDurationChoices(ObservedHazardEvent event) {
        return durationChoicesForEventIdentifiers.get(event.getEventID());
    }

    @Override
    public void eventCommandInvoked(ObservedHazardEvent event,
            String identifier,
            Map<String, Map<String, Object>> mutableProperties) {

        /*
         * If the command that was invoked is a metadata refresh trigger,
         * perform the refresh; otherwise, if the command is meant to trigger
         * the editing of rise-crest-fall information, start the edit.
         */
        if (metadataReloadTriggeringIdentifiersForEventIdentifiers
                .containsKey(event.getEventID())
                && metadataReloadTriggeringIdentifiersForEventIdentifiers.get(
                        event.getEventID()).contains(identifier)) {
            updateEventMetadata(event);
            return;
        } else if (editRiseCrestFallTriggeringIdentifiersForEventIdentifiers
                .containsKey(event.getEventID())
                && editRiseCrestFallTriggeringIdentifiersForEventIdentifiers
                        .get(event.getEventID()).contains(identifier)) {
            startRiseCrestFallEdit(event);
        }

        /*
         * Find the event modifying script that goes with this identifier, if
         * any, and execute it.
         */
        Map<String, String> eventModifyingFunctionNamesForIdentifiers = eventModifyingScriptsForEventIdentifiers
                .get(event.getEventID());
        if (eventModifyingFunctionNamesForIdentifiers == null) {
            return;
        }
        String eventModifyingFunctionName = eventModifyingFunctionNamesForIdentifiers
                .get(identifier);
        if (eventModifyingFunctionName == null) {
            return;
        }
        configManager.runEventModifyingScript(event,
                scriptFilesForEventIdentifiers.get(event.getEventID()),
                eventModifyingFunctionName, mutableProperties,
                eventModifyingScriptListener);
    }

    /**
     * Respond to the completion of an event modifying script execution.
     * 
     * @param event
     *            Hazard event that was returned, indicating what hazard
     *            attributes have changed. If <code>null</code>, no changes were
     *            made.
     */
    private void eventModifyingScriptExecutionComplete(ModifiedHazardEvent event) {
        IHazardEvent hazardEvent = event.getHazardEvent();
        ObservedHazardEvent originalEvent = getEventById(hazardEvent
                .getEventID());
        if (originalEvent != null) {
            if (event.getMutableProperties() != null) {
                notificationSender
                        .postNotificationAsync(new SessionEventScriptExtraDataAvailable(
                                originalEvent, event.getMutableProperties(),
                                Originator.OTHER));
            }
            originalEvent
                    .setHazardAttributes(hazardEvent.getHazardAttributes());
        }
    }

    /**
     * Update the specified event's metadata in response to some sort of change
     * (creation of the event, updating of status or hazard type) that may
     * result in the available metadata changing.
     * 
     * @param event
     *            Event for which metadata may need updating.
     */
    private void updateEventMetadata(IHazardEvent event) {

        /*
         * Get a new megawidget specifier manager for this event, and store it
         * in the cache. Also get the event modifiers map if one was provided,
         * and cache it away as well.
         */
        HazardEventMetadata metadata = configManager
                .getMetadataForHazardEvent(event);
        MegawidgetSpecifierManager manager = metadata
                .getMegawidgetSpecifierManager();

        assert (manager != null);
        assert (event.getStartTime() != null);
        assert (event.getEndTime() != null);

        megawidgetSpecifiersForEventIdentifiers
                .put(event.getEventID(), manager);
        metadataReloadTriggeringIdentifiersForEventIdentifiers
                .put(event.getEventID(),
                        metadata.getRefreshTriggeringMetadataKeys());
        editRiseCrestFallTriggeringIdentifiersForEventIdentifiers.put(
                event.getEventID(),
                metadata.getEditRiseCrestFallTriggeringMetadataKeys());
        Map<String, String> eventModifiers = metadata
                .getEventModifyingFunctionNamesForIdentifiers();
        if (eventModifiers != null) {
            scriptFilesForEventIdentifiers.put(event.getEventID(),
                    metadata.getScriptFile());
            eventModifyingScriptsForEventIdentifiers.put(event.getEventID(),
                    eventModifiers);
        }

        /*
         * Get a copy of the current attributes of the hazard event, so that
         * they may be modified as required to work with the new metadata
         * specifiers. Then add any missing specifiers' starting states (and
         * correct those that are not valid for these specifiers), and assign
         * the modified attributes back to the event.
         * 
         * TODO: ObservedHazardEvent should probably return a defensive copy of
         * the attributes, or better yet, an unmodifiable view (i.e. using
         * Collections.unmodifiableMap()), so that the original within the
         * ObservedHazardEvent cannot be modified. This should be done with any
         * other mutable objects returned by ObservedXXXX instances, since they
         * need to know when their components are modified so that they can send
         * out notifications in response.
         * 
         * TODO: Consider making megawidgets take Serializable states, instead
         * of using states of type Object. This is a bit complex, since those
         * states that are of various types of Collection subclasses are not
         * serializable; in those cases it might be difficult to pull this off.
         * For now, copying back and forth between maps holding Object values
         * and those holding Serializable values must be done.
         */
        ObservedHazardEvent hazardEvent = (ObservedHazardEvent) event;
        Map<String, Serializable> attributes = hazardEvent
                .getHazardAttributes();
        Map<String, Object> newAttributes = new HashMap<>(attributes.size());
        for (String name : attributes.keySet()) {
            newAttributes.put(name, attributes.get(name));
        }
        populateTimeAttributesStartingStates(manager.getSpecifiers(),
                newAttributes, hazardEvent.getStartTime().getTime(),
                hazardEvent.getEndTime().getTime());
        manager.populateWithStartingStates(newAttributes);
        attributes = new HashMap<>(newAttributes.size());
        for (String name : newAttributes.keySet()) {
            attributes.put(name, (Serializable) newAttributes.get(name));
        }
        hazardEvent.setHazardAttributes(attributes);

        /*
         * Fire off a notification that the metadata may have changed for this
         * event.
         */
        notificationSender
                .postNotificationAsync(new SessionEventMetadataModified(event,
                        Originator.OTHER));

    }

    /**
     * Start the edit of rise-crest-fall information for the specified event.
     * 
     * @param event
     *            Event for which to edit the rise-crest-fall information.
     */
    private void startRiseCrestFallEdit(IHazardEvent event) {
        IEventApplier applier = new IEventApplier() {
            @Override
            public void apply(IHazardEvent event) {
                updateEventMetadata(event);
                /*
                 * TODO: Added this line in hopes it would update the start/end
                 * times, but it doesn't
                 */
                updateTimeBoundariesForSingleEvent(event,
                        TimeUtil.currentTimeMillis());
            }

        };
        IRiseCrestFallEditor editor = messenger.getRiseCrestFallEditor(event);
        IHazardEvent evt = editor.getRiseCrestFallEditor(event, applier);
        if (evt != null) {
            if (evt instanceof ObservedHazardEvent) {
                event = evt;
            }
            updateEventMetadata(event);
            /*
             * TODO: Added this line in hopes it would update the start/end
             * times, but it doesn't
             */
            updateTimeBoundariesForSingleEvent(event,
                    TimeUtil.currentTimeMillis());
        }
    }

    /**
     * Find any time-based megawidget specifiers in the specified list and, for
     * each one, if the given attributes map does not include values for all of
     * its state identifiers, fill in default states for those identifiers.
     * 
     * @param specifiers
     *            Megawidget specifiers.
     * @param attributes
     *            Map of hazard attribute names to their values.
     * @param mininumTime
     *            Minimum time to use when coming up with default values.
     * @param maximumTime
     *            Maximum time to use when coming up with default values.
     */
    @SuppressWarnings("unchecked")
    private void populateTimeAttributesStartingStates(
            List<ISpecifier> specifiers, Map<String, Object> attributes,
            long minimumTime, long maximumTime) {

        /*
         * Iterate through the specifiers, looking for any that are time
         * specifiers and filling in default values for those, and for any that
         * are parent specifiers to as to be able to search their descendants
         * for the same reason.
         */
        for (ISpecifier specifier : specifiers) {
            if (specifier instanceof TimeMegawidgetSpecifier) {

                /*
                 * Determine whether or not the attributes handled by this
                 * specifier already have valid values, meaning that they must
                 * have non-null values that are in increasing order.
                 */
                TimeMegawidgetSpecifier timeSpecifier = ((TimeMegawidgetSpecifier) specifier);
                List<String> identifiers = timeSpecifier.getStateIdentifiers();
                long lastValue = -1L;
                boolean populate = false;
                for (String identifier : identifiers) {
                    Number valueObj = (Number) attributes.get(identifier);
                    if ((valueObj == null)
                            || ((lastValue != -1L) && (lastValue >= valueObj
                                    .longValue()))) {
                        populate = true;
                        break;
                    }
                    lastValue = valueObj.longValue();
                }

                /*
                 * If the values are not valid, create default values for them,
                 * equally spaced between the given minimum and maximum times,
                 * unless there is only one attribute for this specifier, in
                 * which case simply make it the same as the minimum time.
                 */
                if (populate) {
                    long interval = (identifiers.size() == 1 ? 0L
                            : (maximumTime - minimumTime)
                                    / (identifiers.size() - 1L));
                    long defaultValue = (identifiers.size() == 1 ? (minimumTime + maximumTime) / 2L
                            : minimumTime);
                    for (int j = 0; j < identifiers.size(); j++, defaultValue += interval) {
                        String identifier = identifiers.get(j);
                        attributes.put(identifier, defaultValue);
                    }
                }
            }
            if (specifier instanceof IParentSpecifier) {

                /*
                 * Ensure that any descendant time specifiers' attributes have
                 * proper default values as well.
                 */
                populateTimeAttributesStartingStates(
                        ((IParentSpecifier<ISpecifier>) specifier)
                                .getChildMegawidgetSpecifiers(),
                        attributes, minimumTime, maximumTime);
            }
        }
    }

    /**
     * Ensure that toggles of end time "until further notice" flags result in
     * the appropriate time being set to "until further notice" or, if the flag
     * has been set to false, an appropriate default time. Also ensure that any
     * metadata state changes that should trigger a metadata reload do so.
     * Finally, generate notifications that the list of selected hazard events
     * has changed if the selected attribute is found to have been altered.
     * 
     * @param change
     *            Change that occurred.
     */
    @Override
    public void processHazardAttributesChanged(IHazardEvent event,
            Map<String, Serializable> attributeMap, IOriginator originator) {

        /*
         * If the hazard selection attribute has changed and it is within the
         * current set of all events being managed, send out a notification of
         * indicating that selected events have changed, and update the
         * conflicts for selected events map. The reason that the check is made
         * for whether the event is currently being managed is to avoid such
         * updates when an event has its attribute changed before it is added or
         * after it is removed.
         */
        if (attributeMap.containsKey(HAZARD_EVENT_SELECTED)
                && getEvents().contains(event)) {
            Set<String> eventIds = Sets.newHashSetWithExpectedSize(1);
            eventIds.add(event.getEventID());
            notificationSender
                    .postNotificationAsync(new SessionSelectedEventsModified(
                            originator, eventIds));
            updateConflictingEventsForSelectedEventIdentifiers(event, false);
        }

        /*
         * If the end time "until further notice" flag has changed value but was
         * not removed, change the end time in a corresponding manner.
         */
        Map<String, Serializable> eventAttributeMap = event
                .getHazardAttributes();
        if (attributeMap
                .containsKey(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)
                && eventAttributeMap
                        .containsKey(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)) {
            setEventEndTimeForUntilFurtherNotice(
                    event,
                    Boolean.TRUE.equals(eventAttributeMap
                            .get(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)));
        }

        /*
         * If any of the attributes changed are metadata-reload triggers, then
         * reload the metadata; otherwise, if any of them are to trigger the
         * editing of rise-crest-fall information, reload that.
         */
        String eventID = event.getEventID();
        Set<String> metadataReloadTriggeringIdentifiers = metadataReloadTriggeringIdentifiersForEventIdentifiers
                .get(eventID);
        Set<String> editRiseCrestFallTriggeringIdentifiers = editRiseCrestFallTriggeringIdentifiersForEventIdentifiers
                .get(eventID);
        if ((metadataReloadTriggeringIdentifiers != null)
                && (Sets.intersection(metadataReloadTriggeringIdentifiers,
                        attributeMap.keySet()).isEmpty() == false)) {
            updateEventMetadata(event);
        } else if ((editRiseCrestFallTriggeringIdentifiers != null)
                && (Sets.intersection(editRiseCrestFallTriggeringIdentifiers,
                        attributeMap.keySet()).isEmpty() == false)) {
            startRiseCrestFallEdit(event);
        }
    }

    /**
     * Ensure that changes to an event's time range cause the selected hazard
     * conflicts map to be updated.
     * 
     * @param change
     *            Change that occurred.
     */
    @Override
    public void processHazardTimeRangeChanged(IHazardEvent event) {
        updateConflictingEventsForSelectedEventIdentifiers(event, false);
    }

    /**
     * Ensure that changes to an event's geometry cause the selected hazard
     * conflicts map to be updated.
     * 
     * @param change
     *            Change that occurred.
     */
    @Override
    public void processHazardGeometryChanged(IHazardEvent event) {
        updateConflictingEventsForSelectedEventIdentifiers(event, false);
    }

    @Override
    public Set<String> getEventIdsAllowingUntilFurtherNotice() {
        return Collections
                .unmodifiableSet(identifiersOfEventsAllowingUntilFurtherNotice);
    }

    @Override
    public Map<String, Range<Long>> getStartTimeBoundariesForEventIds() {
        return Collections
                .unmodifiableMap(startTimeBoundariesForEventIdentifiers);
    }

    @Override
    public Map<String, Range<Long>> getEndTimeBoundariesForEventIds() {
        return Collections
                .unmodifiableMap(endTimeBoundariesForEventIdentifiers);
    }

    /**
     * Set the end time for the specified event with respect to the specified
     * value for "until further notice".
     * 
     * @param event
     *            Event to have its end time set.
     * @param untilFurtherNotice
     *            Flag indicating whether or not the end time should be
     *            "until further notice".
     */
    private void setEventEndTimeForUntilFurtherNotice(IHazardEvent event,
            boolean untilFurtherNotice) {

        /*
         * If "until further notice" has been toggled on for the end time, save
         * the current end time for later (in case it is toggled off again), and
         * change the end time to the "until further notice" value; otherwise,
         * change the end time to be the same interval distant from the start
         * time as it was before "until further notice" was toggled on. If no
         * interval is found to have been saved, perhaps due to a metadata
         * change or a type change, just use the default duration for the event.
         */
        if (untilFurtherNotice) {
            if (event
                    .getHazardAttribute(HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE) == null) {
                long interval = event.getEndTime().getTime()
                        - event.getStartTime().getTime();
                event.addHazardAttribute(
                        HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE,
                        interval);
            }
            updateEndTimeBoundariesForSingleEvent(event, event.getStartTime()
                    .getTime(),
                    HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS);
            event.setEndTime(new Date(
                    HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS));
        } else if ((event.getEndTime() != null)
                && (event.getEndTime().getTime() == HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS)) {
            Long interval = (Long) event
                    .getHazardAttribute(HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE);
            event.removeHazardAttribute(HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE);
            if (interval == null) {
                interval = configManager.getDefaultDuration(event);
            }
            long startTime = event.getStartTime().getTime();
            updateEndTimeBoundariesForSingleEvent(event, startTime, startTime
                    + interval);
            event.setEndTime(new Date(startTime + interval));
        }
    }

    /**
     * Update the set of identifiers of events allowing the toggling of
     * "until further notice" mode. This is to be called whenever one or more
     * events have been added, removed, or had their hazard types changed.
     * 
     * @param event
     *            Event that has been added, removed, or modified.
     * @param removed
     *            Flag indicating whether or not the change is the removal of
     *            the event.
     */
    private boolean isUpdateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(
            IHazardEvent event, boolean removed) {

        boolean notifyAllowsUntilFurtherNotice = false;
        /*
         * Assume the event should be removed from the set unless it is not
         * being removed from the session, and it has a hazard type that allows
         * "until further notice".
         */
        boolean allowsUntilFurtherNotice = false;
        if (removed == false) {
            HazardTypeEntry hazardType = configManager.getHazardTypes().get(
                    HazardEventUtilities.getHazardType(event));
            if ((hazardType != null) && hazardType.isAllowUntilFurtherNotice()) {
                allowsUntilFurtherNotice = true;
            }
        }

        /*
         * Make the change required; if this actually results in a change to the
         * set, fire off a notification.
         */
        boolean changed;
        if (allowsUntilFurtherNotice) {
            changed = identifiersOfEventsAllowingUntilFurtherNotice.add(event
                    .getEventID());
        } else {
            changed = identifiersOfEventsAllowingUntilFurtherNotice
                    .remove(event.getEventID());
        }
        if (changed) {
            notifyAllowsUntilFurtherNotice = true;
        }

        return (notifyAllowsUntilFurtherNotice);
    }

    /**
     * Ensure that the end time "until further notice" mode, if present in the
     * specified event, is appropriate; if it is not, remove it.
     * 
     * @param event
     *            Event to be checked.
     */
    private void ensureEventEndTimeUntilFurtherNoticeAppropriate(
            IHazardEvent event, boolean logErrors) {

        /*
         * If this event cannot have "until further notice", ensure it is not
         * one of its attributes.
         */
        if (identifiersOfEventsAllowingUntilFurtherNotice.contains(event
                .getEventID()) == false) {

            /*
             * If the attributes contains the flag, remove it. If it was set to
             * true, then reset the end time to an appropriate non-"until
             * further notice" value.
             */
            Boolean untilFurtherNotice = (Boolean) event
                    .getHazardAttribute(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
            if (untilFurtherNotice != null) {
                event.removeHazardAttribute(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
                if (untilFurtherNotice.equals(Boolean.TRUE)) {
                    setEventEndTimeForUntilFurtherNotice(event, false);
                    if (logErrors) {
                        statusHandler
                                .error("event "
                                        + event.getEventID()
                                        + " found to have \"until further notice\" set, "
                                        + "which is illegal for events of type "
                                        + event.getHazardType());
                    }
                }
            }
        }
    }

    private void filterEventsForConfig(Collection<? extends IHazardEvent> events) {
        ISettings settings = configManager.getSettings();
        Set<String> siteIDs = settings.getVisibleSites();
        Set<String> phenSigs = settings.getVisibleTypes();
        Set<HazardStatus> statuses = EnumSet.noneOf(HazardStatus.class);
        for (String state : settings.getVisibleStatuses()) {
            statuses.add(HazardStatus.valueOf(state.toUpperCase()));
        }
        Iterator<? extends IHazardEvent> it = events.iterator();
        while (it.hasNext()) {
            IHazardEvent event = it.next();
            if (!statuses.contains(event.getStatus())) {
                it.remove();
            } else if (!siteIDs.contains(event.getSiteID())) {
                it.remove();
            } else {
                String key = HazardEventUtilities.getHazardType(event);
                /*
                 * Check for null key ensures we don't filter out events for
                 * which a type has not yet been defined.
                 */
                if (key != null && !phenSigs.contains(key)) {
                    it.remove();
                }
            }
        }
    }

    private void loadEventsForSettings() {

        /*
         * The originator should be the session manager, since the addition of a
         * new event is occurring.
         */
        IOriginator originator = Originator.OTHER;

        Map<String, List<Object>> filters = new HashMap<String, List<Object>>();
        ObservedSettings settings = configManager.getSettings();
        Set<String> visibleSites = settings.getVisibleSites();

        String configSiteID = configManager.getSiteID();
        // If the settings file has not been overridden for the site,
        // add the currently configured site to the list of visibleSites.
        if (visibleSites.contains(configSiteID) == false) {
            visibleSites.add(configSiteID);
            settings.setVisibleSites(visibleSites);
            /*
             * <pre> This will dispatch a SettingsModified message; ONLY If this
             * is a NEW Site ID. It is a very slim possibility that this change
             * will cause 1 (and only 1) Message loop (that is a single
             * iteration through a rarely encountered loop) for the
             * SettingsModified message. The
             * SessionConfigurationManager.updateCurrentSettings
             * (SettingsModified) Message.
             */
            configManager.updateCurrentSettings(settings, originator);
        }
        if (visibleSites == null || visibleSites.isEmpty()) {
            return;
        }
        filters.put(HazardConstants.SITE_ID,
                new ArrayList<Object>(visibleSites));

        Set<String> visibleTypes = settings.getVisibleTypes();
        if (visibleTypes == null || visibleTypes.isEmpty()) {
            return;
        }
        filters.put(HazardConstants.PHEN_SIG, new ArrayList<Object>(
                visibleTypes));
        Set<String> visibleStatuses = settings.getVisibleStatuses();
        if (visibleStatuses == null || visibleStatuses.isEmpty()) {
            return;
        }
        List<Object> statuses = new ArrayList<Object>(visibleStatuses.size());
        for (String state : visibleStatuses) {
            statuses.add(HazardStatus.valueOf(state.toUpperCase()));
        }
        filters.put(HazardConstants.HAZARD_EVENT_STATUS, statuses);

        Map<String, HazardHistoryList> eventsMap = dbManager
                .getEventsByFilter(filters);

        synchronized (events) {
            for (Entry<String, HazardHistoryList> entry : eventsMap.entrySet()) {
                HazardHistoryList list = entry.getValue();
                IHazardEvent event = list.get(list.size() - 1);
                if (getEventById(event.getEventID()) != null) {
                    // already have this one.
                    continue;
                }
                event = addEvent(event, false, originator);
                for (IHazardEvent histEvent : list) {
                    if (HazardStatus.issuedButNotEnded(histEvent.getStatus())) {
                        event.addHazardAttribute(HazardConstants.ATTR_ISSUED,
                                true);
                        break;
                    }
                }
            }
            for (ObservedHazardEvent event : events) {
                scheduleExpirationTask(event);
            }
        }
    }

    @Override
    public ObservedHazardEvent addEvent(IHazardEvent event,
            IOriginator originator) {
        HazardStatus status = event.getStatus();
        if (status == null || status == HazardStatus.PENDING) {
            return addEvent(event, true, originator);
        } else if (status == HazardStatus.POTENTIAL) {
            return addEvent(event, false, originator);
        } else {
            List<IHazardEvent> list = new ArrayList<IHazardEvent>();
            list.add(event);
            filterEventsForConfig(list);
            if (!list.isEmpty()) {
                return addEvent(event, false, originator);
            } else {
                return null;
            }
        }
    }

    /**
     * Add the specified hazard event.
     * <p>
     * <strong>NOTE</strong>: This method is called whenever an event is added
     * to the current session, regardless of the source of the event. Additional
     * logic (method calls, etc.) may therefore be added to this method's
     * implementation as necessary if said logic must be run whenever an event
     * is added.
     */
    protected ObservedHazardEvent addEvent(IHazardEvent event,
            boolean localEvent, IOriginator originator) {
        ObservedHazardEvent oevent = new ObservedHazardEvent(event);

        ObservedSettings settings = configManager.getSettings();

        /*
         * Need to account for the case where the event being added already
         * exists in the event manager. This can happen with recommender
         * callbacks. For example, the ModifyStormTrackTool will modify
         * information corresponding to an existing event.
         */
        String eventID = oevent.getEventID();

        if (eventID != null && eventID.length() > 0) {
            ObservedHazardEvent existingEvent = getEventById(eventID);

            if (existingEvent != null) {
                SessionEventUtilities.mergeHazardEvents(oevent, existingEvent);
                return existingEvent;
            }
        }

        /*
         * Verify that the hazard was not created server-side to fulfill
         * interoperability requirements.
         */
        if ((event.getStatus() == null
                || event.getStatus() == HazardStatus.PENDING || event
                .getStatus() == HazardStatus.POTENTIAL)
                && event.getHazardAttributes().containsKey(
                        HazardConstants.GFE_INTEROPERABILITY) == false) {

            /*
             * Can only add geometry to selected if the hazard type is empty.
             */
            if ((Boolean.TRUE.equals(settings.getAddGeometryToSelected()))
                    && (event.getHazardType() == null)
                    && (getSelectedEvents().size() == 1)) {
                ObservedHazardEvent existingEvent = getSelectedEvents()
                        .iterator().next();
                Geometry existingGeometries = existingEvent.getGeometry();
                List<Geometry> geometryList = new ArrayList<>();

                for (int i = 0; i < existingGeometries.getNumGeometries(); ++i) {
                    geometryList.add(existingGeometries.getGeometryN(i));
                }

                Geometry newGeometries = oevent.getGeometry();

                for (int i = 0; i < newGeometries.getNumGeometries(); ++i) {
                    geometryList.add(newGeometries.getGeometryN(i));
                }

                GeometryCollection geometryCollection = geometryFactory
                        .createGeometryCollection(geometryList
                                .toArray(new Geometry[geometryList.size()]));

                /*
                 * Combine the geometryCollection together!
                 */
                Geometry geom = geometryCollection.union();
                existingEvent.setGeometry(geom);
                existingEvent
                        .removeHazardAttribute(HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY);
                return existingEvent;

            } else {
                try {
                    oevent.setEventID(HazardEventUtilities.generateEventID(
                            configManager.getSiteID(),
                            CAVEMode.getMode() == CAVEMode.PRACTICE));
                } catch (Exception e) {
                    statusHandler.error("Unable to set event id", e);
                }
            }
        }

        if ((configManager.getHazardCategory(oevent) == null)
                && (oevent
                        .getHazardAttribute(HazardConstants.ATTR_HAZARD_CATEGORY) == null)) {
            oevent.addHazardAttribute(HazardConstants.ATTR_HAZARD_CATEGORY,
                    settings.getDefaultCategory());
        }
        if (oevent.getStartTime() == null) {
            Date timeToUse = timeManager.getCurrentTime();
            Date selectedTime = new Date(timeManager.getSelectedTime()
                    .getLowerBound());
            if (selectedTime.after(timeManager.getCurrentTime())) {
                timeToUse = selectedTime;
            }
            oevent.setStartTime(timeToUse);
        }
        if (oevent.getEndTime() == null) {
            long s = oevent.getStartTime().getTime();
            long d = configManager.getDefaultDuration(oevent);
            oevent.setEndTime(new Date(s + d));
        }
        if (oevent.getStatus() == null) {
            oevent.setStatus(HazardStatus.PENDING);
        }

        if (SessionEventUtilities.isEnded(oevent)) {
            oevent.setStatus(HazardStatus.ENDED);
        }
        String sig = oevent.getSignificance();
        if (sig != null) {
            try {
                /*
                 * Validate significance, since some recommenders use the full
                 * name.
                 */
                HazardConstants.significanceFromAbbreviation(sig);
            } catch (IllegalArgumentException e) {
                /*
                 * This will throw an exception if its not a valid name or
                 * abbreviation.
                 */
                Significance s = Significance.valueOf(sig);
                oevent.setSignificance(s.getAbbreviation());
            }
        }
        oevent.setSiteID(configManager.getSiteID());
        ProductClass productClass;
        switch (CAVEMode.getMode()) {
        case OPERATIONAL:
            productClass = ProductClass.OPERATIONAL;
            break;
        case PRACTICE:
            /*
             * TODO, for now do it this way, maybe need to add user changeable.
             */
            productClass = ProductClass.OPERATIONAL;
            break;
        default:
            productClass = ProductClass.TEST;
        }
        oevent.setHazardMode(productClass);
        synchronized (events) {
            if (localEvent && !Boolean.TRUE.equals(settings.getAddToSelected())) {
                for (IHazardEvent e : events) {
                    e.addHazardAttribute(HAZARD_EVENT_SELECTED, false);
                }
            }
            events.add(oevent);
        }
        oevent.addHazardAttribute(HAZARD_EVENT_SELECTED, false);
        oevent.addHazardAttribute(HAZARD_EVENT_CHECKED, false);
        oevent.addHazardAttribute(HazardConstants.ATTR_ISSUED,
                HazardStatus.issuedButNotEnded(oevent.getStatus()));

        if (localEvent) {
            oevent.addHazardAttribute(HAZARD_EVENT_SELECTED, true);
        }
        oevent.addHazardAttribute(HAZARD_EVENT_CHECKED, true);
        boolean notifyAllowUntilFurtherNoticeSet = isUpdateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(
                oevent, false);
        updateSavedTimesForEventIfIssued(oevent, false);

        boolean isLastChangedEventModified = addModification(oevent
                .getEventID());

        /**
         * Respond to the addition of a hazard event by firing off a
         * notification that the list of selected events has changed, if
         * appropriate, as well as by updating the event's start and end time
         * editability boundaries, and by modifying the event conflict tracking
         * data as appropriate.
         */
        ensureEventEndTimeUntilFurtherNoticeAppropriate(oevent, true);
        updateTimeBoundariesForEvents(oevent, false, false);
        updateDurationChoicesForEvent(oevent, false);
        updateConflictingEventsForSelectedEventIdentifiers(oevent, false);

        List<ObservedHazardEvent> selectedEventList = this.getSelectedEvents();
        this.timeManager.processSelectedEventsModified(selectedEventList);

        notificationSender.postNotificationAsync(new SessionEventAdded(oevent,
                notifyAllowUntilFurtherNoticeSet, isLastChangedEventModified,
                originator));

        return oevent;
    }

    @Override
    public void removeEvent(ObservedHazardEvent event, IOriginator originator) {
        removeEvent(event, true, originator);
    }

    @Override
    public void removeEvents(Collection<ObservedHazardEvent> events,
            IOriginator originator) {
        /*
         * Avoid concurrent modification since events is backed by this.events
         */
        List<ObservedHazardEvent> eventsToRemove = new ArrayList<ObservedHazardEvent>(
                events);
        for (int i = 0; i < events.size(); i++) {
            removeEvent(eventsToRemove.get(i), true, originator);
        }
    }

    /**
     * Remove the specified hazard event.
     * <p>
     * <strong>NOTE</strong>: This method is called whenever an event is removed
     * from the current session, regardless of the source of the change.
     * Additional logic (method calls, etc.) may therefore be added to this
     * method's implementation as necessary if said logic must be run whenever
     * an event is removed.
     */
    private void removeEvent(IHazardEvent event, boolean delete,
            IOriginator originator) {
        synchronized (events) {
            if (events.remove(event)) {
                /*
                 * TODO this should never delete operation issued events.
                 */
                /*
                 * TODO this should not delete the whole list, just any pending
                 * or proposed items on the end of the list.
                 */
                String eventIdentifier = event.getEventID();
                if (delete) {
                    HazardHistoryList histList = dbManager
                            .getByEventID(eventIdentifier);
                    if (histList != null && !histList.isEmpty()) {
                        dbManager.removeEvents(histList);
                    }
                }
                boolean notifyAllowUntilFurtherNoticeSet = isUpdateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(
                        event, true);
                megawidgetSpecifiersForEventIdentifiers.remove(eventIdentifier);
                metadataReloadTriggeringIdentifiersForEventIdentifiers
                        .remove(eventIdentifier);
                editRiseCrestFallTriggeringIdentifiersForEventIdentifiers
                        .remove(eventIdentifier);
                scriptFilesForEventIdentifiers.remove(eventIdentifier);
                eventModifyingScriptsForEventIdentifiers
                        .remove(eventIdentifier);

                updateSavedTimesForEventIfIssued(event, true);
                updateTimeBoundariesForEvents(event, true, false);
                updateDurationChoicesForEvent(event, true);
                updateConflictingEventsForSelectedEventIdentifiers(event, true);

                notificationSender
                        .postNotificationAsync(new SessionEventRemoved(event,
                                notifyAllowUntilFurtherNoticeSet, false,
                                originator));
            }
        }
    }

    @Override
    public void sortEvents(Comparator<ObservedHazardEvent> comparator) {
        synchronized (events) {
            Collections.sort(events, comparator);
        }
    }

    @Override
    public Collection<ObservedHazardEvent> getEvents() {
        synchronized (events) {
            return new ArrayList<ObservedHazardEvent>(events);
        }
    }

    /**
     * Receive notification from an event that it was modified in any way
     * <strong>except</strong> for status changes (for example, Pending to
     * Issued), or the addition or removal of individual attributes.
     * <p>
     * <strong>NOTE</strong>: This method is called whenever an event is
     * modified as detailed above within the current session, regardless of the
     * source of the change. Additional logic (method calls, etc.) may therefore
     * be added to this method's implementation as necessary if said logic must
     * be run whenever an event is so modified.
     */
    public void hazardEventModified(SessionEventModified notification) {
        IHazardEvent event = notification.getEvent();

        boolean isModified = addModification(event.getEventID());
        notification.setIsLastChangedEventModified(isModified);

        if (event instanceof ObservedHazardEvent) {
            ((ObservedHazardEvent) event).setModified(true);
        }

        boolean notifyAllowUntilFurtherNoticeSet = isUpdateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(
                event, false);
        notification
                .setIsAllowingUntilFurtherNoticeSet(notifyAllowUntilFurtherNoticeSet);

        ensureEventEndTimeUntilFurtherNoticeAppropriate(event, false);

        notificationSender.postNotificationAsync(notification);

    }

    /**
     * Receiver notification from an event that the latter experienced the
     * modification of an individual attribute.
     * <p>
     * <strong>NOTE</strong>: This method is called whenever an event is
     * modified as detailed above within the current session, regardless of the
     * source of the change. Additional logic (method calls, etc.) may therefore
     * be added to this method's implementation as necessary if said logic must
     * be run whenever an event is so modified.
     */
    @Override
    public void setEventAttributesModified(IHazardEvent event,
            String identifier, Serializable value, IOriginator originator) {
        Map<String, Serializable> modAttributesMap = new HashMap<String, Serializable>();
        modAttributesMap.put(identifier, value);

        setEventAttributesModified(event, modAttributesMap, originator);
    }

    @Override
    public void setEventAttributesModified(IHazardEvent event,
            Map<String, Serializable> modAttributesMap, IOriginator originator) {

        if (event != null) {
            event.addHazardAttributes(modAttributesMap);
            hazardEventAttributesModified(new SessionEventAttributesModified(
                    event, modAttributesMap, originator));
        }
    }

    public void hazardEventAttributesModified(
            SessionEventAttributesModified notification) {
        IHazardEvent event = notification.getEvent();
        boolean isModified = addModification(event.getEventID());
        notification.setIsLastChangedEventModified(isModified);

        notificationSender.postNotificationAsync(notification);
    }

    @Override
    public void setEventStatus(IHazardEvent event, HazardStatus newStatus,
            boolean persist, IOriginator originator) {

        if (event != null) {
            event.setStatus(newStatus);
            if (persist) {
                boolean needsPersist = false;
                switch (newStatus) {
                case ISSUED:
                    event.addHazardAttribute(ATTR_ISSUED, true);
                    // Remove this incase the hazard was reverted
                    event.removeHazardAttribute(REPLACED_BY);
                    needsPersist = true;
                    break;
                case PROPOSED:
                    needsPersist = true;
                    break;
                case ENDED:
                    event.addHazardAttribute(HAZARD_EVENT_SELECTED, false);
                    ObservedHazardEvent hazardEvent = (ObservedHazardEvent) event;
                    hazardEvent.setModified(false);
                    needsPersist = true;
                    break;
                default:
                    ;// do nothing.
                }
                if (needsPersist) {
                    try {
                        IHazardEvent dbEvent = dbManager.createEvent(event);
                        dbEvent.removeHazardAttribute(ATTR_ISSUED);
                        dbEvent.removeHazardAttribute(HAZARD_EVENT_SELECTED);
                        dbEvent.removeHazardAttribute(HAZARD_EVENT_CHECKED);
                        dbEvent.removeHazardAttribute(ATTR_HAZARD_CATEGORY);
                        dbManager.storeEvent(dbEvent);
                        scheduleExpirationTask(event);
                    } catch (Throwable e) {
                        statusHandler.handle(Priority.PROBLEM,
                                e.getLocalizedMessage(), e);
                    }
                }
            }

            hazardEventStatusModified(new SessionEventStatusModified(event,
                    originator));

        }

    }

    /**
     * Receive notification from an event that the latter experienced a status
     * change (for example, Pending to Issued).
     * <p>
     * <strong>NOTE</strong>: This method is called whenever an event
     * experiences a status change in the current session, regardless of the
     * source of the change. Additional logic (method calls, etc.) may therefore
     * be added to this method's implementation as necessary if said logic must
     * be run whenever an event is so modified.
     */
    public void hazardEventStatusModified(
            SessionEventStatusModified notification) {

        boolean isModified = addModification(notification.getEvent()
                .getEventID());
        notification.setIsLastChangedEventModified(isModified);
        notificationSender.postNotificationAsync(notification);
        updateConflictingEventsForSelectedEventIdentifiers(
                notification.getEvent(), false);
    }

    public void setEventTimeRange(IHazardEvent event, Date startTime,
            Date endTime, IOriginator originator) {

        if (event != null) {
            event.setTimeRange(startTime, endTime);
            hazardEventModified(new SessionEventTimeRangeModified(event,
                    originator));
        }
    }

    /**
     * Schedules the tasks on the {@link Timer} to be executed at a later time,
     * unless they are already past the time necessary at which it will happen
     * immediately then.
     * 
     * @param event
     */
    private void scheduleExpirationTask(final IHazardEvent event) {
        if (eventExpirationTimer != null) {
            if (HazardStatus.issuedButNotEnded(event.getStatus())) {
                final String eventId = event.getEventID();
                TimerTask existingTask = expirationTasks.get(eventId);
                if (existingTask != null) {
                    existingTask.cancel();
                    expirationTasks.remove(eventId);
                }
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        SessionEventManager.this.setEventStatus(event,
                                HazardStatus.ENDED, true, Originator.OTHER);
                        expirationTasks.remove(eventId);
                    }
                };
                Date scheduledTime = event.getEndTime();
                /*
                 * TODO: Need to determine what to do with this, somewhere we
                 * need to be resetting the expiration time if we manually end
                 * the hazard?
                 */
                // if (event.getHazardAttribute(HazardConstants.EXPIRATIONTIME)
                // != null) {
                // scheduledTime = new Date(
                // // TODO, change this when we are getting back
                // // expiration time as a date
                // (Long) event
                // .getHazardAttribute(HazardConstants.EXPIRATIONTIME));
                // }

                /*
                 * Round down to the nearest minute, so we see exactly when it
                 * happens.
                 */
                scheduledTime = DateUtils.truncate(scheduledTime,
                        Calendar.MINUTE);
                long scheduleTimeMillis = Math.max(0, scheduledTime.getTime()
                        - SimulatedTime.getSystemTime().getTime().getTime());
                if (SimulatedTime.getSystemTime().isFrozen() == false
                        || (SimulatedTime.getSystemTime().isFrozen() && scheduleTimeMillis == 0)) {
                    eventExpirationTimer.schedule(task, scheduleTimeMillis);
                    expirationTasks.put(eventId, task);
                }
            }
        }
    }

    /**
     * Creates a time listener so that we can reschedule the {@link TimerTask}
     * when necessary (the Simulated Time has changed or is frozen)
     * 
     * @return
     */
    private ISimulatedTimeChangeListener createTimeListener() {
        timeListener = new ISimulatedTimeChangeListener() {

            @Override
            public void timechanged() {
                for (TimerTask task : expirationTasks.values()) {
                    task.cancel();
                    expirationTasks.clear();
                }

                for (ObservedHazardEvent event : events) {
                    scheduleExpirationTask(event);
                }
            }
        };
        return timeListener;
    }

    /**
     * Add the specified event identifier to the head of the stack of modified
     * events, removing it from elsewhere in the stack if it is found further
     * down.
     * 
     * @param eventId
     *            Identifier to be added.
     */
    private boolean addModification(String eventId) {

        boolean isModified = false;
        if (eventId.equals(eventModifications.peek()) == false) {
            eventModifications.remove(eventId);
            eventModifications.push(eventId);
            isModified = true;
        }

        return (isModified);
    }

    @Override
    public ObservedHazardEvent getLastModifiedSelectedEvent() {
        if (eventModifications.isEmpty()) {
            return null;
        }
        ObservedHazardEvent event = getEventById(eventModifications.peek());
        if (event != null
                && Boolean.TRUE.equals(event
                        .getHazardAttribute(HAZARD_EVENT_SELECTED))) {
            return event;
        } else {
            eventModifications.pop();
            return getLastModifiedSelectedEvent();
        }
    }

    @Override
    public void setLastModifiedSelectedEvent(ObservedHazardEvent event,
            IOriginator originator) {
        boolean isModified = addModification(event.getEventID());

        if (isModified == true) {
            notificationSender
                    .postNotificationAsync(new SessionLastChangedEventModified(
                            originator));
        }
    }

    @Override
    public Map<String, Collection<IHazardEvent>> getConflictingEventsForSelectedEvents() {
        return Collections
                .unmodifiableMap(conflictingEventsForSelectedEventIdentifiers);
    }

    /**
     * Round the specified epoch time in milliseconds down to the nearest
     * minute.
     * 
     * @param time
     *            Time to be rounded down.
     * @return Rounded down time.
     */
    private long roundTimeDownToNearestMinute(Date time) {
        return DateUtils.truncate(time, Calendar.MINUTE).getTime();
    }

    /**
     * Get the allowable end time range for an event with the specified end time
     * that has not yet been issued.
     * 
     * @param endTime
     *            Event end time.
     * @return Allowable range for the event's end times.
     */
    private Range<Long> getEndTimeRangeForPreIssuedEvent(long endTime) {
        boolean untilFurtherNotice = (endTime == UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS);
        return Range.closed((untilFurtherNotice ? endTime : 0L),
                (untilFurtherNotice ? endTime : HazardConstants.MAX_TIME));
    }

    /**
     * Get an allowable end time range for the specified event given the
     * specified end time.
     * 
     * @parma event Event for which to determine the allowable range.
     * @param endTime
     *            Event end time.
     * @return Allowable range for the event's end times.
     */
    private Range<Long> getEndTimeRangeForIssuedEventBasedOnEndTime(
            IHazardEvent event, long endTime) {

        /*
         * If the end time is "until further notice", limit the end times to
         * just that value.
         */
        if (endTime == UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
            return Range.closed(endTime, endTime);
        }

        /*
         * Use the end time as the lower and/or upper bound of the allowable end
         * times, as appropriate given the event's type's ability to be shrunk
         * or expanded after issuance.
         */
        return Range
                .closed((configManager.isAllowTimeShrink(event) ? HazardConstants.MIN_TIME
                        : endTime),
                        (configManager.isAllowTimeExpand(event) ? HazardConstants.MAX_TIME
                                : endTime));
    }

    /**
     * Get the allowable end time range for the specified event with the
     * specified start and end times that has been issued but is not yet ending.
     * 
     * @parma event Event for which to determine the allowable range.
     * @param startTime
     *            Event start time.
     * @param endTime
     *            Event end time.
     * @return Allowable range for the event's end times.
     */
    private Range<Long> getEndTimeRangeForIssuedEvent(IHazardEvent event,
            long startTime, long endTime) {

        /*
         * If the end time is "until further notice", limit the end times to
         * just that value.
         */
        if (endTime == UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
            return getEndTimeRangeForIssuedEventBasedOnEndTime(event, endTime);
        }

        /*
         * If the event has an absolute end time, fin the potential end time
         * boundary one way; if it is a duration-type event, find it another
         * way.
         */
        if (configManager.getDurationChoices(event).isEmpty()) {

            /*
             * Determine the end time of the event when it was last issued, and
             * use that as a potential end time boundary.
             */
            endTime = endTimesOrDurationsForIssuedEventIdentifiers.get(event
                    .getEventID());
        } else {
            /*
             * Determine the duration of the event when it was last issued, then
             * add that as an offset to the event's start time and use the sum
             * as the potential end time boundary. This is different from events
             * with absolute end times (handled in the previous block) because
             * whether a hazard end time can shrink (move backward in time) or
             * expand has a different meaning for duration-type events; for
             * them, the end time boundaries must be adjusted relative to the
             * start time.
             */
            String eventID = event.getEventID();
            endTime = startTime
                    + endTimesOrDurationsForIssuedEventIdentifiers.get(eventID);
        }

        /*
         * Given the modified end time, get the range.
         */
        return getEndTimeRangeForIssuedEventBasedOnEndTime(event, endTime);
    }

    /**
     * Get the allowable range for an event with the specified end time that is
     * ending or has ended.
     * 
     * @param endTime
     *            Event end time.
     * @return Allowable range for the event's end times.
     */
    private Range<Long> getEndTimeRangeForEndingEvent(long endTime) {
        return Range.closed(endTime, endTime);
    }

    /**
     * Update the maps holding the end time allowable range for the specified
     * event based upon whether "until further notice" has been toggled on or
     * off, sending off a notification of the change if one is made to the
     * boundaries.
     * 
     * @param event
     *            Event to have its end time boundaries modified.
     * @param newEndTime
     *            New end time, in epoch time in milliseconds; if this is equal
     *            to
     *            {@link HazardConstants#UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS}
     *            , then "until further notice" has been turned on.
     */
    private void updateEndTimeBoundariesForSingleEvent(IHazardEvent event,
            long newStartTime, long newEndTime) {
        Range<Long> endTimeRange = null;
        switch (event.getStatus()) {
        case POTENTIAL:
        case PENDING:
        case PROPOSED:
            endTimeRange = getEndTimeRangeForPreIssuedEvent(newEndTime);
            break;
        case ISSUED:
            endTimeRange = getEndTimeRangeForIssuedEvent(event, newStartTime,
                    newEndTime);
            break;
        case ENDING:
        case ENDED:
            endTimeRange = getEndTimeRangeForEndingEvent(newEndTime);
        }
        if (endTimeRange.equals(endTimeBoundariesForEventIdentifiers.get(event
                .getEventID())) == false) {
            endTimeBoundariesForEventIdentifiers.put(event.getEventID(),
                    endTimeRange);
            notificationSender
                    .postNotificationAsync(new SessionEventsTimeRangeBoundariesModified(
                            Sets.newHashSet(event.getEventID()),
                            Originator.OTHER));
        }
    }

    /**
     * Set the specified event's start and end time ranges as specified.
     * 
     * @param event
     *            Event to have its ranges modified.
     * @param startTimeRange
     *            New allowable range of start times.
     * @param endTimeRange
     *            New allowable range of end times.
     * @return True if the new ranges are different from the previous ranges,
     *         false otherwise.
     */
    private boolean setEventTimeRangeBoundaries(IHazardEvent event,
            Range<Long> startTimeRange, Range<Long> endTimeRange) {
        boolean changed = false;
        if (startTimeRange.equals(startTimeBoundariesForEventIdentifiers
                .get(event.getEventID())) == false) {
            startTimeBoundariesForEventIdentifiers.put(event.getEventID(),
                    startTimeRange);
            changed = true;
        }
        if (endTimeRange.equals(endTimeBoundariesForEventIdentifiers.get(event
                .getEventID())) == false) {
            endTimeBoundariesForEventIdentifiers.put(event.getEventID(),
                    endTimeRange);
            changed = true;
        }
        return changed;
    }

    /**
     * Update the allowable ranges for start and end time of the specified event
     * to be correct given the event's status and other relevant properties.
     * 
     * @param event
     *            Event for which to update the start and end time allowable
     *            ranges.
     * @param currentTime
     *            Current CAVE time, as far as the event is concerned.
     * @return True if the time boundaries were modified, false otherwise.
     */
    private boolean updateTimeBoundariesForSingleEvent(IHazardEvent event,
            long currentTime) {

        /*
         * Handle pre-issued hazard events differently from ones that have been
         * issued at least once. Pre-issued ones have their start time marching
         * forward with CAVE clock time, and some allow the start time to be
         * after the current CAVE clock time, while some do not. End times can
         * be anything if unissued, but issued ones may be limited by by the
         * hazard type's contraints Finally, ending and ended hazards cannot
         * have their times changed.
         */
        Range<Long> startTimeRange = null;
        Range<Long> endTimeRange = null;
        long startTime = event.getStartTime().getTime();
        long endTime = event.getEndTime().getTime();
        switch (event.getStatus()) {
        case POTENTIAL:
        case PENDING:
        case PROPOSED:
            startTimeRange = Range.closed(currentTime, (configManager
                    .isStartTimeIsCurrentTime(event) ? currentTime
                    : HazardConstants.MAX_TIME));
            endTimeRange = getEndTimeRangeForPreIssuedEvent(endTime);
            break;
        case ISSUED:

            /*
             * TODO: Sometimes startTimesForIssuedEventIdentifiers does not
             * include an entry for an issued event. Chris Golden is attempting
             * to track down the reason why this might occur; it may no longer
             * be a problem as of the code review being submitted 3/25/2015, as
             * one possible cause has been fixed, but since Chris was unable to
             * reproduce the error in any case, it may still be present. In case
             * of the latter, fallback code has been placed here to use the
             * current event start time for the "issued" start time so that this
             * will not leave Hazard Services in a completely broken state. An
             * error is logged as well so that testers may report when this
             * occurs.
             */
            long startTimeWhenLastIssued;
            if (startTimesForIssuedEventIdentifiers.containsKey(event
                    .getEventID())) {
                startTimeWhenLastIssued = startTimesForIssuedEventIdentifiers
                        .get(event.getEventID());
            } else {
                startTimeWhenLastIssued = event.getStartTime().getTime();
                statusHandler.error("Issued hazard event " + event.getEventID()
                        + " with type " + event.getHazardType()
                        + " has no saved start time from first issuance. "
                        + "This should not occur; falling back to using "
                        + "event's current start time as potential time "
                        + "range boundary instead. If reporting this "
                        + "error, please include any relevant context to "
                        + "aid in debugging the problem.");
            }
            boolean startTimeIsCurrentTime = configManager
                    .isStartTimeIsCurrentTime(event);
            startTimeRange = Range.closed(
                    (startTimeIsCurrentTime ? startTimeWhenLastIssued
                            : HazardConstants.MIN_TIME),
                    (startTimeIsCurrentTime ? startTimeWhenLastIssued
                            : HazardConstants.MAX_TIME));
            endTimeRange = getEndTimeRangeForIssuedEvent(event, startTime,
                    endTime);
            break;
        case ENDING:
        case ENDED:
            startTimeRange = Range.closed(startTime, startTime);
            endTimeRange = getEndTimeRangeForEndingEvent(endTime);
        }

        /*
         * Use the generated ranges.
         */
        return setEventTimeRangeBoundaries(event, startTimeRange, endTimeRange);
    }

    /**
     * Post a notification indicating that the specified events have had their
     * time range boundaries modified.
     * 
     * @param eventIdentifiers
     *            Events that have had their time range boundaries modified.
     */
    private void postTimeRangeBoundariesModifiedNotification(
            Set<String> eventIdentifiers) {
        notificationSender
                .postNotificationAsync(new SessionEventsTimeRangeBoundariesModified(
                        eventIdentifiers, Originator.OTHER));
    }

    /**
     * Update the allowable ranges for start and end time of the specified event
     * that has just been issued, as well as its start and end times themselves.
     * A notification is sent off of the changes made if any boundaries are
     * changed.
     * 
     * @param event
     *            Event that has just been issued.
     * @param issueTime
     *            Issue time, as epoch time in milliseconds.
     */
    private void updateTimeRangeBoundariesOfJustIssuedEvent(
            ObservedHazardEvent event, long issueTime) {

        /*
         * Get the old start time, and then get the actual issuance time for the
         * hazard, and round it down to the nearest minute. If the start time is
         * less than the rounded-down issue time, set the former to be the
         * latter, since the start time should never be less than when the event
         * was last issued.
         */
        long startTime = event.getStartTime().getTime();
        issueTime = roundTimeDownToNearestMinute(new Date(issueTime));
        if (startTime < issueTime) {
            startTime = issueTime;
        }

        /*
         * Determine the allowable range for the start time. The minimum must be
         * the issue time from the issuance that occurred (if start time is
         * always current time) or else practically unlimited. The maximum is
         * similar: either the issue time, or unlimited.
         */
        boolean startTimeIsIssueTime = configManager
                .isStartTimeIsCurrentTime(event);
        Range<Long> startTimeRange = Range.closed(
                (startTimeIsIssueTime ? issueTime : HazardConstants.MIN_TIME),
                (startTimeIsIssueTime ? issueTime : HazardConstants.MAX_TIME));

        /*
         * Get the end time as it was previously, and if the event has an
         * absolute end time, only change it if it is too close to the new start
         * time. If the event has a duration instead of an absolute end time,
         * change the end time so that the duration remains the same as it was
         * before.
         */
        long endTime = event.getEndTime().getTime();
        if (endTime != UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
            if (configManager.getDurationChoices(event).isEmpty()) {
                if (endTime - startTime < TIME_RANGE_MINIMUM_INTERVAL) {
                    endTime = startTime + TIME_RANGE_MINIMUM_INTERVAL;
                }
            } else {
                endTime += startTime - event.getStartTime().getTime();
            }
        }

        /*
         * Get the allowable range for the end time.
         */
        Range<Long> endTimeRange = getEndTimeRangeForIssuedEventBasedOnEndTime(
                event, endTime);

        /*
         * Use the new ranges; if these are different from the previous ranges,
         * post a notification to that effect.
         */
        if (setEventTimeRangeBoundaries(event, startTimeRange, endTimeRange)) {
            postTimeRangeBoundariesModifiedNotification(Sets.newHashSet(event
                    .getEventID()));
        }

        /*
         * Set the new start and end times.
         */
        event.setTimeRange(new Date(startTime), new Date(endTime));

        /*
         * Make a record of the event's start and its end time/duration at
         * issuance time, which now becomes the most recent issuance for this
         * event.
         */
        updateSavedTimesForEventIfIssued(event, false);
    }

    /**
     * Update the allowable ranges for start and end times of the specified
     * event, or of all events, as well as the start and end times themselves.
     * This is to be called whenever something that affects any of the events'
     * start/end time boundaries has potentially changed, other than an event
     * having just been issued. A notification is sent off of the changes made
     * if any boundaries are changed.
     * 
     * @param singleEvent
     *            Event that has been added, removed, or modified. If
     *            <code>null</code>, all events should be updated. In this case,
     *            the assumption is made that no events have been removed.
     * @param removed
     *            Flag indicating whether or not the change is the removal of
     *            the event; this is ignored if <code>event</code> is
     *            <code>null</code>.
     */
    private void updateTimeBoundariesForEvents(IHazardEvent singleEvent,
            boolean removed, boolean postChange) {

        /*
         * Get the start of the current minute; this is used in place of the
         * actual current time, since it is assumed that event start times that
         * must be altered should be set to the start of the current minute, at
         * least for user-interface purposes.
         */
        long startOfCurrentMinute = roundTimeDownToNearestMinute(SimulatedTime
                .getSystemTime().getTime());

        /*
         * If all events should be checked, iterate through them, adding any
         * that have their boundaries changed to the set recording changed
         * events. Otherwise, handle the single event's potential change.
         */
        Set<String> identifiersWithChangedBoundaries = new HashSet<>();
        if (singleEvent == null) {
            for (ObservedHazardEvent thisEvent : events) {
                if (updateTimeBoundariesForSingleEvent(thisEvent,
                        startOfCurrentMinute)) {
                    identifiersWithChangedBoundaries
                            .add(thisEvent.getEventID());
                }
            }
        } else {
            if (removed) {
                startTimeBoundariesForEventIdentifiers.remove(singleEvent
                        .getEventID());
                endTimeBoundariesForEventIdentifiers.remove(singleEvent
                        .getEventID());
            } else if (updateTimeBoundariesForSingleEvent(singleEvent,
                    startOfCurrentMinute)) {
                identifiersWithChangedBoundaries.add(singleEvent.getEventID());
            }
        }

        /*
         * If any events' boundaries have changed, send out a notification to
         * that effect, and ensure that those that have changed have their start
         * and end times falling within the new boundaries.
         */
        if (identifiersWithChangedBoundaries.isEmpty() == false) {
            if (postChange == true) {
                postTimeRangeBoundariesModifiedNotification(identifiersWithChangedBoundaries);
            }
            for (String identifier : identifiersWithChangedBoundaries) {
                ObservedHazardEvent thisEvent = getEventById(identifier);
                long startTime = thisEvent.getStartTime().getTime();
                long endTime = thisEvent.getEndTime().getTime();
                long duration = endTime - startTime;

                /*
                 * Determine whether the start time no longer falls within the
                 * allowable range, and if this is the case, move it so that it
                 * is equal to whichever range endpoint it is closest.
                 */
                boolean changed = false;
                Range<Long> startRange = startTimeBoundariesForEventIdentifiers
                        .get(identifier);
                if (startTime < startRange.lowerEndpoint()) {
                    changed = true;
                    startTime = startRange.lowerEndpoint();
                } else if (startTime > startRange.upperEndpoint()) {
                    changed = true;
                    startTime = startRange.upperEndpoint();
                }

                /*
                 * If this event's end time is set to "until further notice", do
                 * not alter it.
                 */
                if (endTime != UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {

                    /*
                     * If this event type uses durations instead of absolute end
                     * times, set the new end time to be the same distance from
                     * the new start time as the old one was from the old start
                     * time. Otherwise, boundary-check the end time.
                     */
                    if (configManager.getDurationChoices(thisEvent).isEmpty() == false) {
                        if (changed) {
                            endTime = startTime + duration;
                        }
                    } else {

                        /*
                         * Ensure that the end time is at least the minimum
                         * interval away from the start time.
                         */
                        if (endTime - startTime < TIME_RANGE_MINIMUM_INTERVAL) {
                            changed = true;
                            endTime = startTime + TIME_RANGE_MINIMUM_INTERVAL;
                        }

                        /*
                         * Ensure that the end time does not fall outside the
                         * allowable boundaries; if it does, move it so that it
                         * is equal to whichever range endpoint it is closest.
                         */
                        Range<Long> endRange = endTimeBoundariesForEventIdentifiers
                                .get(identifier);
                        if (endTime < endRange.lowerEndpoint()) {
                            changed = true;
                            endTime = endRange.lowerEndpoint();
                        } else if (endTime > endRange.upperEndpoint()) {
                            changed = true;
                            endTime = endRange.upperEndpoint();
                        }
                    }
                }

                /*
                 * If the start and/or end time need changing, make the changes.
                 */
                if (changed) {
                    thisEvent.setTimeRange(new Date(startTime), new Date(
                            endTime));
                }
            }
        }
    }

    /**
     * Update the saved absolute or relative end time (the latter being
     * duration) for the specified event if the latter is issued.
     * 
     * @param event
     *            Event that needs its saved end time or duration updated to
     *            reflect its current state.
     * @param removed
     *            Flag indicating whether or not the event has been removed.
     */
    private void updateSavedTimesForEventIfIssued(IHazardEvent event,
            boolean removed) {
        String eventId = event.getEventID();
        if (removed) {
            startTimesForIssuedEventIdentifiers.remove(eventId);
            endTimesOrDurationsForIssuedEventIdentifiers.remove(eventId);
        } else if (event.getStatus() == HazardStatus.ISSUED) {
            startTimesForIssuedEventIdentifiers.put(eventId, event
                    .getStartTime().getTime());
            if (configManager.getDurationChoices(event).isEmpty()) {
                endTimesOrDurationsForIssuedEventIdentifiers.put(eventId, event
                        .getEndTime().getTime());
            } else {
                endTimesOrDurationsForIssuedEventIdentifiers.put(eventId, event
                        .getEndTime().getTime()
                        - event.getStartTime().getTime());
            }
        }
    }

    /**
     * Update the duration choices list associated with the specified event.
     * 
     * @param event
     *            Event for which the duration choices are to be updated.
     * @param removed
     *            Flag indicating whether or not the event has been removed.
     */
    private void updateDurationChoicesForEvent(IHazardEvent event,
            boolean removed) {

        /*
         * If the event has been removed, remove any duration choices associated
         * with it. Otherwise, update the choices.
         */
        if (removed) {
            durationChoicesForEventIdentifiers.remove(event.getEventID());
        } else {

            /*
             * Get all the choices available for this hazard type, and prune
             * them of any that do not fit within the allowable end time range.
             */
            List<String> durationChoices = configManager
                    .getDurationChoices(event);
            if (durationChoices.isEmpty() == false) {

                /*
                 * Get a map of the choice strings to their associated time
                 * deltas in milliseconds. The map will iterate in the order the
                 * choices are specified in the list used to generate it.
                 */
                Map<String, Long> deltasForDurations = null;
                try {
                    deltasForDurations = durationChoiceValidator
                            .convertToAvailableMapForProperty(durationChoices);
                } catch (MegawidgetPropertyException e) {
                    statusHandler
                            .error("invalid list of duration choices for event of type "
                                    + HazardEventUtilities.getHazardType(event),
                                    e);
                    durationChoicesForEventIdentifiers.put(event.getEventID(),
                            Collections.<String> emptyList());
                    return;
                }

                /*
                 * Iterate through the choices, checking each in turn to see if,
                 * when a choice's delta is added to the current event start
                 * time, the sum falls within the allowable end time range. If
                 * it does, add it to the list of approved choices.
                 */
                long startTime = event.getStartTime().getTime();
                Range<Long> endTimeRange = endTimeBoundariesForEventIdentifiers
                        .get(event.getEventID());
                List<String> allowableDurationChoices = new ArrayList<>(
                        durationChoices.size());
                for (Map.Entry<String, Long> entry : deltasForDurations
                        .entrySet()) {
                    long possibleEndTime = startTime + entry.getValue();
                    if (endTimeRange.contains(possibleEndTime)) {
                        allowableDurationChoices.add(entry.getKey());
                    }
                }
                durationChoices = allowableDurationChoices;
            }

            /*
             * Cache the list of approved choices.
             */
            durationChoicesForEventIdentifiers.put(event.getEventID(),
                    durationChoices);
        }
    }

    /**
     * Update the map of selected event identifiers to their collections of
     * conflicting events. This is to be called whenever something that affects
     * the selected events' potential conflicts changes.
     * 
     * @param event
     *            Event that has been added, removed, or modified.
     * @param removed
     *            Flag indicating whether or not the change is the removal of
     *            the event.
     */
    private void updateConflictingEventsForSelectedEventIdentifiers(
            IHazardEvent event, boolean removed) {

        /*
         * TODO: If this is found to take too much time, an optimization could
         * be implemented in which each selected event is checked against the
         * event specified as a parameter. If the latter has been removed, then
         * it would be removed from any conflicts collections, as well as from
         * the map itself if its identifier was a key. Other optimizations could
         * be performed if other changes had occurred.
         * 
         * Currently, however, the entire map is rebuilt from scratch. This is
         * still an improvement over before, when it was rebuilt each time
         * getConflictingEventsForSelectedEvents() was invoked; now it is only
         * rebuilt whenever this method is called in response to a change of
         * some sort.
         */
        Map<String, Collection<IHazardEvent>> oldMap = new HashMap<>(
                conflictingEventsForSelectedEventIdentifiers);
        conflictingEventsForSelectedEventIdentifiers.clear();

        Collection<ObservedHazardEvent> selectedEvents = getSelectedEvents();

        for (IHazardEvent eventToCheck : selectedEvents) {

            Map<IHazardEvent, Collection<String>> conflictingHazards = getConflictingEvents(
                    eventToCheck, eventToCheck.getStartTime(),
                    eventToCheck.getEndTime(), eventToCheck.getGeometry(),
                    HazardEventUtilities.getHazardType(eventToCheck));

            if (!conflictingHazards.isEmpty()) {
                conflictingEventsForSelectedEventIdentifiers.put(eventToCheck
                        .getEventID(), Collections
                        .unmodifiableSet(conflictingHazards.keySet()));
            }

        }

        if (oldMap.equals(conflictingEventsForSelectedEventIdentifiers) == false) {
            notificationSender
                    .postNotificationAsync(new SessionSelectedEventConflictsModified(
                            Originator.OTHER));
        }

    }

    @Override
    public Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> getAllConflictingEvents() {

        Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> conflictingHazardMap = new HashMap<>();
        /*
         * Find the union of the session events and those retrieved from the
         * hazard event manager. Ignore "Ended" events.
         */
        List<IHazardEvent> eventsToCheck = getEventsToCheckForConflicts(new HazardQueryBuilder());

        for (IHazardEvent eventToCheck : eventsToCheck) {

            Map<IHazardEvent, Collection<String>> conflictingHazards = getConflictingEvents(
                    eventToCheck, eventToCheck.getStartTime(),
                    eventToCheck.getEndTime(), eventToCheck.getGeometry(),
                    HazardEventUtilities.getHazardType(eventToCheck));

            if (!conflictingHazards.isEmpty()) {
                conflictingHazardMap.put(eventToCheck, conflictingHazards);
            }

        }

        return conflictingHazardMap;
    }

    @Override
    public Map<IHazardEvent, Collection<String>> getConflictingEvents(
            final IHazardEvent eventToCompare, final Date startTime,
            final Date endTime, final Geometry geometry, String phenSigSubtype) {

        Map<IHazardEvent, Collection<String>> conflictingHazardsMap = new HashMap<>();

        /*
         * A hazard type may not always be assigned to an event yet.
         */
        if (phenSigSubtype != null) {

            /*
             * Retrieve the list of conflicting hazards associated with this
             * type.
             */
            HazardTypes hazardTypes = configManager.getHazardTypes();
            HazardTypeEntry hazardTypeEntry = hazardTypes.get(phenSigSubtype);

            if (hazardTypeEntry != null) {

                List<String> hazardConflictList = hazardTypeEntry
                        .getHazardConflictList();

                if (!hazardConflictList.isEmpty()) {

                    String cwa = configManager.getSiteID();

                    String ugcType = hazardTypeEntry.getUgcType();

                    String ugcLabel = hazardTypeEntry.getUgcLabel();

                    List<IGeometryData> hatchedAreasForEvent = geoMapUtilities
                            .buildHazardAreaForEvent(ugcType, ugcLabel, cwa,
                                    eventToCompare);

                    /*
                     * Retrieve matching events from the Hazard Event Manager
                     * Also, include those from the session state.
                     */
                    HazardQueryBuilder hazardQueryBuilder = new HazardQueryBuilder();

                    hazardQueryBuilder.addKey(HAZARD_EVENT_START_TIME,
                            eventToCompare.getStartTime());
                    hazardQueryBuilder.addKey(HAZARD_EVENT_END_TIME,
                            eventToCompare.getEndTime());
                    for (String conflictPhenSig : hazardConflictList) {
                        hazardQueryBuilder.addKey(HazardConstants.PHEN_SIG,
                                conflictPhenSig);
                    }

                    hazardQueryBuilder.addKey(HAZARD_EVENT_STATUS,
                            HazardStatus.ISSUED);
                    hazardQueryBuilder.addKey(HAZARD_EVENT_STATUS,
                            HazardStatus.ENDING);
                    hazardQueryBuilder.addKey(HAZARD_EVENT_STATUS,
                            HazardStatus.ENDED);
                    hazardQueryBuilder.addKey(HAZARD_EVENT_STATUS,
                            HazardStatus.PROPOSED);

                    List<IHazardEvent> eventsToCheck = getEventsToCheckForConflicts(hazardQueryBuilder);

                    /*
                     * Loop over the existing events.
                     */
                    TimeRange modifiedEventTimeRange = new TimeRange(
                            eventToCompare.getStartTime(),
                            eventToCompare.getEndTime());

                    for (IHazardEvent eventToCheck : eventsToCheck) {

                        /*
                         * Test the events for overlap in time. If they do not
                         * overlap in time, then there is no need to test for
                         * overlap in area.
                         */
                        TimeRange eventToCheckTimeRange = new TimeRange(
                                eventToCheck.getStartTime(),
                                eventToCheck.getEndTime());

                        if (modifiedEventTimeRange
                                .overlaps(eventToCheckTimeRange)) {
                            if (!eventToCheck.getEventID().equals(
                                    eventToCompare.getEventID())) {

                                String otherEventPhenSigSubtype = HazardEventUtilities
                                        .getHazardType(eventToCheck);

                                if (hazardConflictList
                                        .contains(otherEventPhenSigSubtype)) {

                                    HazardTypeEntry otherHazardTypeEntry = hazardTypes
                                            .get(otherEventPhenSigSubtype);
                                    String otherUgcType = otherHazardTypeEntry
                                            .getUgcType();
                                    String otherUgcLabel = otherHazardTypeEntry
                                            .getUgcLabel();

                                    if (hazardTypeEntry != null) {
                                        List<IGeometryData> hatchedAreasEventToCheck = geoMapUtilities
                                                .buildHazardAreaForEvent(
                                                        otherUgcType,
                                                        otherUgcLabel, cwa,
                                                        eventToCheck);

                                        conflictingHazardsMap
                                                .putAll(buildConflictMap(
                                                        eventToCompare,
                                                        eventToCheck,
                                                        hatchedAreasForEvent,
                                                        hatchedAreasEventToCheck,
                                                        ugcLabel, otherUgcLabel));
                                    } else {
                                        statusHandler
                                                .warn("No entry defined in HazardTypes.py for hazard type "
                                                        + phenSigSubtype);
                                    }

                                }
                            }
                        }
                    }
                }
            } else {
                statusHandler
                        .warn("No entry defined in HazardTypes.py for hazard type "
                                + phenSigSubtype);
            }

        }

        return conflictingHazardsMap;
    }

    /**
     * Retrieves events for conflict testing.
     * 
     * These events will include those from the current session and those
     * retrieved from the hazard event manager.
     * 
     * Other sources of hazard event information could be added to this as need.
     * 
     * @param hazardQueryBuilder
     *            Used to filter the the hazards retrieved from the
     *            HazardEventManager
     * @return
     */
    private List<IHazardEvent> getEventsToCheckForConflicts(
            final HazardQueryBuilder hazardQueryBuilder) {

        /*
         * Retrieve matching events from the Hazard Event Manager Also, include
         * those from the session state.
         */
        Map<String, HazardHistoryList> eventMap = this.dbManager
                .getEventsByFilter(hazardQueryBuilder.getQuery());
        List<IHazardEvent> eventsToCheck = new ArrayList<IHazardEvent>(
                getEvents());
        Map<String, IHazardEvent> sessionEventMap = new HashMap<>();

        for (IHazardEvent sessionEvent : new ArrayList<IHazardEvent>(
                eventsToCheck)) {
            if (sessionEvent.getStatus() != HazardStatus.ENDED) {
                sessionEventMap.put(sessionEvent.getEventID(), sessionEvent);
            } else {
                eventsToCheck.remove(sessionEvent);
            }
        }

        for (String eventID : eventMap.keySet()) {
            if (!sessionEventMap.containsKey(eventID)) {
                HazardHistoryList historyList = eventMap.get(eventID);

                // Find the hazard in the history list with the most recent time
                if (historyList != null && historyList.isEmpty() == false) {
                    IHazardEvent eventFromManager = historyList.get(0);
                    Long latestTime = eventFromManager.getInsertTime()
                            .getTime();

                    for (int count = 1; count < historyList.size(); count++) {
                        IHazardEvent hazardEvent = historyList.get(count);
                        Long hazardTime = hazardEvent.getInsertTime().getTime();

                        if (hazardTime > latestTime) {
                            latestTime = hazardTime;
                            eventFromManager = hazardEvent;
                        }
                    }

                    if (eventFromManager.getStatus() != HazardStatus.ENDED) {
                        eventsToCheck.add(eventFromManager);
                    }
                }
            }
        }

        return eventsToCheck;
    }

    @Override
    public void shutdown() {
        eventExpirationTimer.cancel();
        eventExpirationTimer = null;
        SimulatedTime.getSystemTime().removeSimulatedTimeChangeListener(
                timeListener);
    }

    /**
     * Based on the hatched areas associated with two hazard events, build a map
     * of conflicting areas (zones, counties, etc). Polygons are a special case
     * in which the polygon is the hatched area.
     * 
     * @param firstEvent
     *            The first of the two events to compare for conflicts
     * @param secondEvent
     *            The second of the two events to compare for conflicts
     * @param hatchedAreasFirstEvent
     *            The hatched areas associated with the first event
     * @param hatchedAreasSecondEvent
     *            The hatched areas associated with the second event
     * @param firstEventLabelParameter
     *            The label (if any) associated with the first event hazard
     *            area.
     * @param secondEventLabelParameter
     *            The label (if any) associated with the second event hazard
     *            area.
     * @return A map containing conflicting hazard events and associated areas
     *         (counties, zones, etc.) where they conflict (if available).
     * 
     */
    private Map<IHazardEvent, Collection<String>> buildConflictMap(
            IHazardEvent firstEvent, IHazardEvent secondEvent,
            List<IGeometryData> hatchedAreasFirstEvent,
            List<IGeometryData> hatchedAreasSecondEvent,
            String firstEventLabelParameter, String secondEventLabelParameter) {

        Map<IHazardEvent, Collection<String>> conflictingHazardsMap = new HashMap<>();

        List<String> geometryNames = new ArrayList<>();

        if (!geoMapUtilities.isWarngenHatching(firstEvent)
                && !geoMapUtilities.isWarngenHatching(secondEvent)) {

            Set<IGeometryData> commonHatchedAreas = new HashSet<>();
            commonHatchedAreas.addAll(hatchedAreasFirstEvent);
            commonHatchedAreas.retainAll(hatchedAreasSecondEvent);

            if (!commonHatchedAreas.isEmpty()) {

                for (IGeometryData hatchedArea : commonHatchedAreas) {

                    geometryNames.add(hatchedArea
                            .getString(firstEventLabelParameter));
                }

                conflictingHazardsMap.put(secondEvent, geometryNames);
            }
        } else {

            String labelFieldName = null;
            List<IGeometryData> geoWithLabelInfo = null;

            if (!geoMapUtilities.isWarngenHatching(firstEvent)) {
                labelFieldName = firstEventLabelParameter;
                geoWithLabelInfo = hatchedAreasFirstEvent;
            } else if (!geoMapUtilities.isWarngenHatching(secondEvent)) {
                labelFieldName = secondEventLabelParameter;
                geoWithLabelInfo = hatchedAreasSecondEvent;
            }

            boolean conflictFound = false;

            for (IGeometryData hatchedArea : hatchedAreasFirstEvent) {
                for (IGeometryData hatchedAreaToCheck : hatchedAreasSecondEvent) {

                    if (hatchedArea.getGeometry().intersects(
                            hatchedAreaToCheck.getGeometry())) {

                        conflictFound = true;

                        if (labelFieldName != null) {

                            if (geoWithLabelInfo == hatchedAreasFirstEvent) {
                                geometryNames.add(hatchedArea
                                        .getString(labelFieldName));
                            } else {
                                geometryNames.add(hatchedAreaToCheck
                                        .getString(labelFieldName));
                            }

                        }
                    }
                }
            }

            if (conflictFound) {
                conflictingHazardsMap.put(secondEvent, geometryNames);
            }

        }

        return conflictingHazardsMap;
    };

    @Override
    public void endEvent(ObservedHazardEvent event, IOriginator originator) {
        clearUndoRedo(event);
        event.setModified(false);
        setEventStatus(event, HazardStatus.ENDED, true, originator);
    }

    @Override
    public void issueEvent(ObservedHazardEvent event, IOriginator originator) {
        event.clearUndoRedo();
        event.setModified(false);
        setEventStatus(event, HazardStatus.ISSUED, true, originator);
    }

    @Override
    public Set<String> getEventIdsAllowingProposal() {
        List<ObservedHazardEvent> selectedEvents = getSelectedEvents();
        Set<String> set = new HashSet<String>(selectedEvents.size());
        for (ObservedHazardEvent event : selectedEvents) {
            HazardStatus status = event.getStatus();

            if (isProposedStateAllowed(event, status)) {
                set.add(event.getEventID());
            }
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public void proposeEvent(ObservedHazardEvent event, IOriginator originator) {

        /*
         * Only propose events that are not already proposed, and are not issued
         * or ended, and that have a valid type.
         */
        HazardStatus status = event.getStatus();
        if (isProposedStateAllowed(event, status)) {
            setEventStatus(event, HazardStatus.PROPOSED, true, originator);
            clearUndoRedo(event);
            event.setModified(false);
        }
    }

    @Override
    public void proposeEvents(Collection<ObservedHazardEvent> events,
            IOriginator originator) {
        for (ObservedHazardEvent observedHazardEvent : events) {
            proposeEvent(observedHazardEvent, originator);
        }
    }

    private boolean isProposedStateAllowed(ObservedHazardEvent event,
            HazardStatus status) {
        return !HazardStatus.hasEverBeenIssued(status)
                && status != HazardStatus.PROPOSED
                && event.getPhenomenon() != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager
     * #buildSelectedHazardProductGeometries()
     */
    @Override
    public boolean buildSelectedHazardProductGeometries() {
        boolean success = true;

        HazardTypes hazardTypes = configManager.getHazardTypes();
        Collection<ObservedHazardEvent> selectedEvents = getSelectedEvents();

        for (ObservedHazardEvent selectedEvent : selectedEvents) {

            HazardTypeEntry hazardType = hazardTypes.get(selectedEvent
                    .getHazardType());

            if (canBeClipped(selectedEvent, hazardType)) {
                Geometry productGeometry;
                if (geoMapUtilities.isWarngenHatching(selectedEvent)) {
                    productGeometry = geoMapUtilities.warngenClipping(
                            selectedEvent, hazardType);
                    productGeometry = reduceGeometry(productGeometry,
                            hazardType);
                    productGeometry = addGoosenecksAsNecessary(productGeometry);

                } else if (geoMapUtilities.isPointBasedHatching(selectedEvent)) {
                    productGeometry = selectedEvent.getGeometry();
                } else {
                    productGeometry = geoMapUtilities
                            .gfeClipping(selectedEvent);
                }

                if (productGeometry.isEmpty()) {
                    StringBuffer warningMessage = new StringBuffer();
                    warningMessage.append("Event ")
                            .append(selectedEvent.getEventID()).append(" ");
                    warningMessage
                            .append("has no hazard areas inside of the forecast area.\n");
                    messenger.getWarner().warnUser(
                            "Product geometry calculation error",
                            warningMessage.toString());
                    success = false;
                    break;
                }

                selectedEvent.setGeometry(productGeometry);

            }

        }

        return success;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager
     * #updateHazardAreas
     * (com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent)
     */
    @Override
    public void updateHazardAreas(IHazardEvent event) {
        if (event.getHazardType() != null) {
            Map<String, String> ugcHatchingAlgorithms = buildInitialHazardAreas(event);
            event.addHazardAttribute(HAZARD_AREA,
                    (Serializable) ugcHatchingAlgorithms);

            hazardEventModified(new SessionEventGeometryModified(
                    (ObservedHazardEvent) event, Originator.OTHER));
            hazardEventAttributesModified(new SessionEventAttributesModified(
                    (ObservedHazardEvent) event, HAZARD_AREA,
                    (Serializable) ugcHatchingAlgorithms, Originator.OTHER));
        }
    }

    @Override
    public void setCurrentEvent(String eventId) {
        setCurrentEvent(getEventById(eventId));
    }

    @Override
    public void setCurrentEvent(ObservedHazardEvent event) {
        this.currentEvent = event;
    }

    @Override
    public ObservedHazardEvent getCurrentEvent() {
        return currentEvent;
    }

    @Override
    public void noCurrentEvent() {
        this.currentEvent = null;
    }

    @Override
    public boolean isCurrentEvent() {
        return currentEvent != null;
    }

    @Override
    public void updateSelectedHazardUGCs() {

        for (IHazardEvent hazardEvent : getSelectedEvents()) {
            String hazardType = HazardEventUtilities.getHazardType(hazardEvent);

            if (hazardType != null) {
                List<String> ugcs = updateUGCs(hazardEvent);
                if (ugcs.isEmpty()) {
                    throw new ProductGenerationException(
                            "No UGCs included in hazard.  Check inclusions in HazardTypes.py");
                }
                hazardEvent.addHazardAttribute(HazardConstants.UGCS,
                        (Serializable) ugcs);
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager
     * #isValidGeometryChange(com.vividsolutions.jts.geom.Geometry,
     * com.raytheon.
     * uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent, boolean)
     */
    public boolean isValidGeometryChange(Geometry geometry,
            ObservedHazardEvent hazardEvent, boolean checkGeometryValidity) {
        boolean result = true;
        if (checkGeometryValidity && !geometry.isValid()) {
            IsValidOp op = new IsValidOp(geometry);
            statusHandler.warn("Invalid Geometry: "
                    + op.getValidationError().getMessage()
                    + ": Geometry modification undone");
            result = false;

        } else if (hazardEvent.hasEverBeenIssued() == true) {
            HazardTypeEntry hazardTypeEntry = configManager.getHazardTypes()
                    .get(HazardEventUtilities.getHazardType(hazardEvent));
            if (hazardTypeEntry != null) {
                if (!hazardTypeEntry.isAllowAreaChange()) {
                    @SuppressWarnings("unchecked")
                    List<String> oldUGCs = (List<String>) hazardEvent
                            .getHazardAttribute(HazardConstants.UGCS);
                    ObservedHazardEvent eventWithNewGeometry = new ObservedHazardEvent(
                            hazardEvent);
                    eventWithNewGeometry.setGeometry(geometry);
                    List<String> newUGCs = updateUGCs(eventWithNewGeometry);
                    newUGCs.removeAll(oldUGCs);

                    if (!newUGCs.isEmpty()) {
                        statusHandler
                                .warn("This hazard event cannot be expanded in area.  Please create a new hazard event for the new areas.");
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager
     * #buildInitialHatching
     * (com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent,
     * java.util.List)
     */
    @Override
    public Map<String, String> buildInitialHazardAreas(IHazardEvent hazardEvent) {
        List<String> ugcs = buildUGCs(hazardEvent);
        String hazardArea;
        if (geoMapUtilities.isWarngenHatching(hazardEvent)) {
            hazardArea = HAZARD_AREA_INTERSECTION;
        } else {
            hazardArea = HAZARD_AREA_ALL;
        }
        Map<String, String> result = new HashMap<>(ugcs.size());
        for (String ugc : ugcs) {
            result.put(ugc, hazardArea);
        }
        return result;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager
     * #addOrRemoveEnclosingUGCs(com.vividsolutions.jts.geom.Coordinate)
     */
    @Override
    public void addOrRemoveEnclosingUGCs(Coordinate location) {
        try {
            List<ObservedHazardEvent> selectedEvents = getSelectedEvents();
            if (selectedEvents.size() != 1) {
                messenger
                        .getWarner()
                        .warnUser(GEOMETRY_MODIFICATION_ERROR,
                                "Cannot add or remove UGCs unless exactly one hazard event is selected");
                return;
            }
            ObservedHazardEvent hazardEvent = selectedEvents.get(0);
            String hazardType = hazardEvent.getHazardType();
            if (hazardType == null) {
                messenger
                        .getWarner()
                        .warnUser(GEOMETRY_MODIFICATION_ERROR,
                                "Cannot add or remove UGCs for a hazard with an undefined type");
                return;
            }
            if (hazardEvent.getStatus().equals(HazardStatus.ENDED)) {
                messenger.getWarner().warnUser(GEOMETRY_MODIFICATION_ERROR,
                        "Cannot add or remove UGCs for an ended hazard");
                return;
            }

            if (!hazardEvent.getStatus().equals(HazardStatus.PENDING)
                    && geoMapUtilities.isPointBasedHatching(hazardEvent)) {
                messenger
                        .getWarner()
                        .warnUser(GEOMETRY_MODIFICATION_ERROR,
                                "Can only add or remove UGCs for pending point hazards");
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, String> hazardAreas = (Map<String, String>) hazardEvent
                    .getHazardAttribute(HAZARD_AREA);
            hazardAreas = new HashMap<>(hazardAreas);
            String mapDBtableName = geoMapUtilities
                    .getMapDBtableName(hazardEvent);

            String mapLabelParameter = geoMapUtilities
                    .getMapLabelParameter(hazardEvent);

            String cwa = configManager.getSiteID();
            Geometry locationAsGeometry = geometryFactory.createPoint(location);
            Set<IGeometryData> mapGeometryData = geoMapUtilities
                    .getMapGeometries(mapDBtableName, mapLabelParameter, cwa);
            Set<IGeometryData> mapGeometryDataContainingLocation = geoMapUtilities
                    .getContainingMapGeometries(mapGeometryData,
                            locationAsGeometry);

            Map<String, IGeometryData> ugcsEnclosingUserSelectedLocation = geoMapUtilities
                    .getUgcsGeometryDataMapping(mapDBtableName,
                            mapGeometryDataContainingLocation);

            Map<String, IGeometryData> allUGCs = geoMapUtilities
                    .getUgcsGeometryDataMapping(mapDBtableName, mapGeometryData);

            Geometry hazardEventGeometry = hazardEvent.getGeometry();

            Geometry modifiedHazardGeometry = hazardEventGeometry;
            if (!geoMapUtilities.isPointBasedHatching(hazardEvent)) {
                GeometryCollection asGeometryCollection = (GeometryCollection) hazardEventGeometry;
                List<Geometry> geometryAsList = asList(asGeometryCollection);
                modifiedHazardGeometry = asUnion(geometryAsList);
            }

            for (String enclosingUGC : ugcsEnclosingUserSelectedLocation
                    .keySet()) {
                Geometry enclosingUgcGeometry = allUGCs.get(enclosingUGC)
                        .getGeometry();

                String hazardArea = hazardAreas.get(enclosingUGC);
                if (geoMapUtilities.isWarngenHatching(hazardEvent)) {
                    warngenHatchingAddRemove(hazardAreas, locationAsGeometry,
                            modifiedHazardGeometry, enclosingUGC, hazardArea);
                    if (!(hazardAreas.values().contains(HAZARD_AREA_ALL) || hazardAreas
                            .values().contains(HAZARD_AREA_INTERSECTION))) {
                        statusHandler.warn(EMPTY_GEOMETRY_ERROR);
                        return;
                    }
                }

                else if (geoMapUtilities.isPointBasedHatching(hazardEvent)) {
                    pointBasedAddRemove(hazardAreas, enclosingUGC, hazardArea);
                } else {
                    modifiedHazardGeometry = gfeHatchingAddRemove(hazardAreas,
                            modifiedHazardGeometry, enclosingUGC,
                            enclosingUgcGeometry, hazardArea);
                    if (modifiedHazardGeometry.isEmpty()) {
                        statusHandler.warn(EMPTY_GEOMETRY_ERROR);
                        return;
                    }
                }
            }

            hazardEventGeometry = modifiedHazardGeometry;
            hazardEvent.setGeometry(hazardEventGeometry);
            hazardEvent.addHazardAttribute(HAZARD_AREA,
                    (Serializable) hazardAreas);
            hazardEventModified(new SessionEventGeometryModified(hazardEvent,
                    Originator.OTHER));

            hazardEventAttributesModified(new SessionEventAttributesModified(
                    hazardEvent, HAZARD_AREA, (Serializable) hazardAreas,
                    Originator.OTHER));

        } catch (TopologyException e) {
            /*
             * /* TODO Use {@link GeometryPrecisionReducer}?
             */
            statusHandler.error("Encountered topology exception", e);
        }
    }

    /**
     * Clears the undo/redo stack for the hazard event.
     * 
     * @param event
     *            Event for which to clear the undo/redo stack
     * @return
     */
    private void clearUndoRedo(IUndoRedoable event) {
        event.clearUndoRedo();
    }

    private boolean canBeClipped(ObservedHazardEvent selectedEvent,
            HazardTypeEntry hazardType) {
        return hazardType != null
                && (!HazardStatus.hasEverBeenIssued(selectedEvent.getStatus()) || (HazardStatus
                        .issuedButNotEnded(selectedEvent.getStatus()) && selectedEvent
                        .isModified()));
    }

    private Geometry reduceGeometry(Geometry geometry,
            HazardTypeEntry hazardTypeEntry) {

        /*
         * Test if point reduction is necessary...
         */
        int pointLimit = hazardTypeEntry.getHazardPointLimit();

        if (pointLimit > 0) {

            /**
             * TODO: Eventually we want to share the same logic WarnGen uses to
             * reduce points. This is not accessible right not, at least without
             * creating a dependency between Hazard Services and WarnGen.
             */
            if (geometry.getNumPoints() > pointLimit) {

                double distanceTolerance = DEFAULT_DISTANCE_TOLERANCE;

                DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(
                        geometry);

                do {
                    simplifier.setDistanceTolerance(distanceTolerance);
                    geometry = simplifier.getResultGeometry();
                    distanceTolerance += DEFAULT_DISTANCE_TOLERANCE_INCREMENT;
                } while (geometry.getNumPoints() > pointLimit);
            }
        }
        return geometry;

    }

    private List<String> buildUGCs(IHazardEvent hazardEvent) {
        if (geoMapUtilities.isPointBasedHatching(hazardEvent)) {
            return buildFromDBStrategyUGCs(hazardEvent);
        } else {
            return buildIntersectionStrategyUGCs(hazardEvent);
        }
    }

    private List<String> updateUGCs(IHazardEvent hazardEvent) {
        if (geoMapUtilities.isPointBasedHatching(hazardEvent)) {
            return buildPointBasedStrategyUGCs(hazardEvent);
        } else {
            return buildIntersectionStrategyUGCs(hazardEvent);
        }
    }

    private List<String> buildPointBasedStrategyUGCs(IHazardEvent hazardEvent) {
        List<String> result = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Map<String, String> hazardAreas = (Map<String, String>) hazardEvent
                .getHazardAttribute(HAZARD_AREA);
        for (String ugc : hazardAreas.keySet()) {
            String hazardArea = hazardAreas.get(ugc);
            if (!hazardArea.equals(HAZARD_AREA_NONE)) {
                result.add(ugc);
            }
        }
        return result;
    }

    private List<String> buildFromDBStrategyUGCs(IHazardEvent hazardEvent) {
        List<String> result = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Map<String, Serializable> forecastPoint = (Map<String, Serializable>) hazardEvent
                .getHazardAttribute(FORECAST_POINT);
        String hazardEventPointID = (String) forecastPoint.get(POINT_ID);
        RiverPointZoneInfo riverPointZoneInfo = this.riverForecastManager
                .getRiverForecastPointRiverZoneInfo(hazardEventPointID);
        if (riverPointZoneInfo != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(riverPointZoneInfo.getState()).append("Z")
                    .append(riverPointZoneInfo.getZoneNum());
            String ugc = sb.toString();
            result.add(ugc);
        }
        return result;
    }

    private List<String> buildIntersectionStrategyUGCs(IHazardEvent hazardEvent) {
        String mapDBtableName = geoMapUtilities.getMapDBtableName(hazardEvent);

        Set<IGeometryData> hazardArea = geoMapUtilities
                .getIntersectingMapGeometries(true, hazardEvent);

        Set<String> ugcs = geoMapUtilities.getUgcsGeometryDataMapping(
                mapDBtableName, hazardArea).keySet();

        return new ArrayList<>(ugcs);
    }

    private void warngenHatchingAddRemove(
            Map<String, String> enclosingHazardArea,
            Geometry locationAsGeometry, Geometry nonPointGeometry,
            String enclosingUGC, String hazardArea) {
        if (nonPointGeometry.intersects(locationAsGeometry)) {
            /*
             * hazardArea is none when due to thresholding the hatching in this
             * area not defined to begin with.
             */
            if (hazardArea == null || hazardArea.equals(HAZARD_AREA_NONE)) {
                enclosingHazardArea.put(enclosingUGC, HAZARD_AREA_INTERSECTION);
            } else {
                enclosingHazardArea.put(enclosingUGC, HAZARD_AREA_NONE);
            }
        } else {
            /*
             * The hazardArea is null when the user clicks on a UGC that wasn't
             * previously included in the hazardArea
             */
            if (hazardArea == null || !hazardArea.equals(HAZARD_AREA_ALL)) {
                enclosingHazardArea.put(enclosingUGC, HAZARD_AREA_ALL);
            } else {
                enclosingHazardArea.put(enclosingUGC, HAZARD_AREA_NONE);
            }
        }
    }

    private void pointBasedAddRemove(Map<String, String> hazardAreas,
            String enclosingUGC, String enclosingHazardArea) {
        if (enclosingHazardArea == null
                || !enclosingHazardArea.equals(HAZARD_AREA_ALL)) {
            hazardAreas.put(enclosingUGC, HAZARD_AREA_ALL);
        } else {
            hazardAreas.put(enclosingUGC, HAZARD_AREA_NONE);
        }
    }

    private Geometry gfeHatchingAddRemove(Map<String, String> hazardAreas,
            Geometry nonPointGeometry, String enclosingUGC,
            Geometry enclosingUgcGeometry, String enclosingHazardArea) {
        if (enclosingHazardArea == null
                || !enclosingHazardArea.equals(HAZARD_AREA_ALL)) {
            nonPointGeometry = nonPointGeometry.union(enclosingUgcGeometry);
            hazardAreas.put(enclosingUGC, HAZARD_AREA_ALL);

        }

        else {
            nonPointGeometry = nonPointGeometry
                    .difference(enclosingUgcGeometry);

            hazardAreas.put(enclosingUGC, HAZARD_AREA_NONE);
        }
        return nonPointGeometry;
    }

    private Collection<ObservedHazardEvent> fromIDs(Collection<String> eventIDs) {
        Collection<ObservedHazardEvent> events = new ArrayList<>(
                eventIDs.size());
        for (String eventId : eventIDs) {
            events.add(getEventById(eventId));
        }
        return events;
    }

    private List<Geometry> asList(GeometryCollection geometryCollection) {
        List<Geometry> result = new ArrayList<>();
        for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
            Geometry g = geometryCollection.getGeometryN(i);
            result.add(g);
        }
        return result;
    }

    private Geometry asUnion(List<Geometry> geometries) {

        Geometry result = null;
        for (int i = 0; i < geometries.size(); i++) {
            Geometry g = geometries.get(i);
            if (g instanceof Polygon || g instanceof MultiPolygon) {
                if (result == null) {
                    result = g;
                } else {
                    result = result.union(g);
                }
            }
        }
        return result;
    }

    private Geometry addGoosenecksAsNecessary(Geometry productGeometry) {
        if (!(productGeometry instanceof GeometryCollection)) {
            return productGeometry;
        }
        GeometryCollection asMultiPolygon = (GeometryCollection) productGeometry;
        Geometry[] geometries = new Geometry[2 * asMultiPolygon
                .getNumGeometries() - 1];

        int n = 0;
        for (int i = 0; i < asMultiPolygon.getNumGeometries(); i++) {
            geometries[n] = asMultiPolygon.getGeometryN(i);
            n += 1;
            if (i < asMultiPolygon.getNumGeometries() - 1) {
                geometries[n] = buildGooseNeck(asMultiPolygon.getGeometryN(i),
                        asMultiPolygon.getGeometryN(i + 1));
                n += 1;
            }
        }
        GeometryCollection result = geometryFactory
                .createGeometryCollection(geometries);
        return result;
    }

    private Geometry buildGooseNeck(Geometry geometry0, Geometry geometry1) {

        double minDistance = Double.MAX_VALUE;
        Coordinate[] closestCoordinates = new Coordinate[2];
        for (int i = 0; i < geometry0.getCoordinates().length; i++) {
            Coordinate coordinate0 = geometry0.getCoordinates()[i];
            for (int j = 0; j < geometry1.getCoordinates().length; j++) {
                Coordinate coordinate1 = geometry1.getCoordinates()[j];
                double distance = coordinate0.distance(coordinate1);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCoordinates[0] = coordinate0;
                    closestCoordinates[1] = coordinate1;
                }
            }
        }
        return geometryFactory.createLineString(closestCoordinates);
    }

}
