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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ETNS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.EVENT_ID_DISPLAY_TYPE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.EXPIRATION_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.FORECAST_POINT;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_AREA;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_AREA_ALL;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_AREA_INTERSECTION;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_AREA_NONE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SELECTED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HIGH_RESOLUTION_GEOMETRY_IS_VISIBLE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ISSUE_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.LOW_RESOLUTION_GEOMETRY;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.LOW_RESOLUTION_GEOMETRY_IS_VISIBLE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.PILS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.REPLACED_BY;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_HAZARD_SITES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.VISIBLE_GEOMETRY;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.VTEC_CODES;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.time.DateUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Include;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventView;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardServicesEventIdUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent.Source;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardEventServiceException;
import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventQueryRequest;
import com.raytheon.uf.common.dataplugin.events.locks.LockInfo;
import com.raytheon.uf.common.dataplugin.events.locks.LockInfo.LockStatus;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.hazards.hydro.CountyStateData;
import com.raytheon.uf.common.hazards.hydro.RiverForecastManager;
import com.raytheon.uf.common.hazards.hydro.RiverPointZoneInfo;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.ProductGenerationException;
import com.raytheon.uf.common.message.WsId;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.time.ISimulatedTimeChangeListener;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.HazardEventMetadata;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsLoaded;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.AbstractSessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventAllowUntilFurtherNoticeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventAttributesModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventGeometryModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventMetadataModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventStatusModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventTimeRangeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventTypeModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.EventUnsavedChangesModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.IEventModification;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventCheckedStateModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventHistoryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsLockStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsOrderingModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsTimeRangeBoundariesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventConflictsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.geomaps.GeoMapUtilities;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender.IIntraNotificationHandler;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger.IEventApplier;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger.IQuestionAnswerer;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger.IRiseCrestFallEditor;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.RevertOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerationComplete;
import com.raytheon.uf.viz.hazards.sessionmanager.time.CurrentTimeChanged;
import com.raytheon.uf.viz.hazards.sessionmanager.time.CurrentTimeMinuteTicked;
import com.raytheon.uf.viz.hazards.sessionmanager.time.CurrentTimeReset;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.viz.core.mode.CAVEMode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.common.visuals.VisualFeature;
import gov.noaa.gsd.common.visuals.VisualFeaturesList;
import gov.noaa.gsd.viz.megawidgets.IParentSpecifier;
import gov.noaa.gsd.viz.megawidgets.ISpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetPropertyException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.megawidgets.TimeMegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.validators.SingleTimeDeltaStringChoiceValidatorHelper;

/**
 * Session event manager, responsible for tracking and manipulating the state of
 * all hazard events within the session.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2013 1257       bsteffen     Initial creation.
 * Jul 19, 2013 1257       bsteffen     Notification support for session manager.
 * Sep 10, 2013  752       blawrenc     Modified addEvent to check if the event
 *                                      being added already exists.
 * Sep 12, 2013 717        jsanchez     Converted certain hazard events to grids.
 * Oct 21, 2013 2177       blawrenc     Added logic to check for event conflicts.
 * Oct 23, 2013 2277       jsanchez     Removed HazardEventConverter from viz.
 * Nov 04, 2013 2182       Dan Schaffer Started refactoring.
 * Nov 29, 2013 2378       blawrenc     Changed to not set modified events back to
 *                                      PENDING.
 * Nov 29, 2013 2380       Dan Schaffer Fixing bugs in settings-based filtering.
 * Jan 14, 2014 2755       bkowal       No longer create new Event IDs for events
 *                                      that are created EDEX-side for
 *                                      interoperability purposes.
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
 * Mar 3, 2014  3034       bkowal       Constant for GFE interoperability flag
 * Apr 28, 2014 3556       bkowal       Updated to use the new hazards common 
 *                                      configuration plugin.
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
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes.
 * Jan 08, 2015 5700       Chris.Golden Changed to generalize the meaning of a command
 *                                      invocation for a particular event, since it no
 *                                      longer only means that an event-modifying
 *                                      script is to be executed; it may also trigger
 *                                      a metadata refresh. Previously, the latter was
 *                                      only possible on a hazard attribute state
 * Jan 22, 2015 4959       Dan Schaffer Ability to right click to add/remove UGCs from
 *                                      hazards.
 * Jan 26, 2015 5952       Dan Schaffer Fix incorrect hazard area designation.
 * Feb  2, 2015 4930       Dan Schaffer Fixed problem where reduction of multi-polygons 
 *                                      can still yield a geometry with more than 20
 *                                      points.
 * Feb  3, 2015 2331       Chris.Golden Added code to track the allowable boundaries of
 *                                      all hazard events' start and end times, so that
 *                                      the user will not move them beyond the allowed
 *                                      ranges. Also added code to advance the start and/
 *                                      or end times of events as time ticks forward when
 *                                      appropriate.
 * Feb 12, 2015 4959       Dan Schaffer Modify MB3 add/remove UGCs to match Warngen.
 * Feb 17, 2015 3847       Chris.Golden Added edit-rise-crest-fall metadata trigger.
 * Feb 21, 2015 4959       Dan Schaffer Improvements to add/remove UGCs.
 * Feb 24, 2015 6499       Dan Schaffer Only allow add/remove UGCs for pending point
 *                                      hazards.
 * Feb 24, 2015 2331       Chris.Golden Added check of any event that is added to the
 *                                      list of events to ensure that it does not have
 *                                      until further notice set if such is not allowed.
 * Mar 13, 2015 6090       Dan Schaffer Fixed goosenecks.
 * Mar 13, 2015 6922       Chris.Cody   Changes to skip re-query on GraphicalEditor cancel.
 * Mar 24, 2015 6090       Dan Schaffer Goosenecks now working as they do in Warngen.
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
 * Apr 06, 2015   7272     mduff        Adding changes for Guava upgrade.  Last changes
 *                                      lost in merge.
 * Apr 14, 2015   6935     Chris.Golden Fixed bug that caused duration choices for hazard
 *                                      events to lag behind event type (e.g. when event
 *                                      type of unissued FF.W.NonConvective was changed to
 *                                      FF.W.BurnScar).
 * Apr 10, 2015   6898     Chris.Cody   Refactored async messaging.
 * Apr 27, 2015   7635     Robert.Blum  Added current config site to list of visible sites
 *                                      for when settings have not been overridden.
 * May 14, 2015    7560    mpduff       Trying to get the Time Range to update from
 *                                      Graphical Editor.
 * May 19, 2015    7975    Robert.Blum  Fixed bug that could incorrectly set the hazard
 *                                      status to ended if it was reverted and contained the
 *                                      REPLACED_BY attribute.
 * May 19, 2015    7706    Robert.Blum  Fixed bug when checking for conflicts where it would
 *                                      check hazards that were ended.
 * May 28, 2015    7709    Chris.Cody   Add Reference name of forecast zone in the
 *                                      conflicting hazards.
 * May 29, 2015    6895   Ben.Phillippe Refactored Hazard Service data access.
 * Jun 02, 2015    7138    Robert.Blum  RVS can now be issued without changing the
 *                                      status/state of the hazard events.
 * Jun 11, 2015    8191    Robert.Blum  Fixed apply on Rise/Crest/Fall editor to correctly
 *                                      update HID/Console times.
 * Jun 17, 2015    8543   Ben.Phillippe Catch error when creating geometry outside of
 *                                      forecast area.
 * Jun 17, 2015    6730    Robert.Blum  Fixed messages bug that was preventing the display
 *                                      from updating correctly if the hazardType is not set.
 * Jun 26, 2015    7919    Robert.Blum  Changed for issuing EXP when hazard has ended.
 * Jul 06, 2015    7514    Robert.Blum  Retaining the start/end time when automatically replacing hazards. Note -
 *                                      the endTime may not be exact, it will be the duration that is the closest
 *                                      to the previous endTime.
 * Jul 07, 2015    8966    Robert.Blum  Fixed start/end time from being incorrectly adjusted based on issue time.
 * Jul 08, 2015    8529    Robert.Blum  Removing invalid attributes when the event type is changed.
 * Jul 22, 2015    9490    Robert.Blum  Fixed Add/Remove county from polygon via right click for WarnGen hazards.
 * Jul 28, 2015    9737    Chris.Golden Fixed bug that caused a switch to a different setting to still show and
 *                                      generate products for events that should have been hidden by the new
 *                                      setting's filters.
 * Jul 29, 2015    9306    Chris.Cody   Add processing for HazardSatus.ELAPSED
 * Aug 03, 2015    8836    Chris.Cody   Changes for a configurable Event Id
 * Aug 17, 2015    9968    Chris.Cody   Changes for processing ENDED/ELAPSED/EXPIRED events
 * Aug 20, 2015    6895    Ben.Phillippe Routing registry requests through request server
 * Sep 04, 2015    7514    Chris.Golden Fixed bug introduced by July 6th check-in for this issue that caused
 *                                      exceptions to be thrown when upgrading certain watches to warnings.
 * Sep 09  2015   10207    Chris.Cody   Switched Polygon Point reduction to use Visvalingam-Whyatt algorithm.
 * Sep 15, 2015    7629    Robert.Blum  Changes for saving pending hazards.
 * Oct 07, 2015    7308    Robert.Blum  WarnGen movement of issued hazards.
 * Oct 14, 2015   12494    Chris Golden Reworked to allow hazard types to include only phenomenon (i.e. no
 *                                      significance) where appropriate.
 * Sep 28, 2015 10302,8167 hansen       Added calls to "getSettingsValue"
 * Oct 01, 2015    7629    Robert.Blum  Fixing bug from first commit that allowed event to not be assigned an
 *                                      event ID.
 * Oct 21, 2015    7308    Robert.Blum  Added buffer when checking if current geometry is covered by the
 *                                      issuedGeometry, when checking if the current geometry is valid.
 * Nov 10, 2015   12762    Chris.Golden Added recommender running in response to hazard event metadata or
 *                                      other attribute changes.
 * Dec 01, 2015   13172    Robert.Blum  Changed to use WarnGenPolygonSimplifier.
 * Dec 08, 2015    8765    Robert.Blum  Only hazardTypes with a point limit defined in hazardTypes.py are
 *                                      reduced.
 * Jan 15, 2016    9387    Robert.Blum  Changes for Graphical Editor to correctly update the HID.
 * Jan 28, 2016   12762    Chris.Golden Changed to only run recommender a single time in response to multiple
 *                                      trigger-type hazard event attributes changing simultaneously, and also
 *                                      added the fetching of metadata specifiers for an event when the latter
 *                                      is added, not later on when the HID comes up (the latter behavior was
 *                                      a bug).
 * Feb 10, 2016   15561    Chris.Golden Removed hard-coded UGC that had been put in for testing.
 * Feb 10, 2016   13279    Chris.Golden Fixed bugs in calculation of new end time when a replacement event
 *                                      uses duration-based end times and the original event does not.
 * Feb 19, 2016   15014    Robert.Blum  Fixed determination of ugcs for point hazards.
 * Feb 24, 2016   14667    Robert.Blum  Limiting Flash Flood Recommender to basins inside the CWA.
 * Mar 03, 2016   14004    Chris.Golden Changed to pass originator when merging hazard events, and to
 *                                      only run event-triggered recommenders when they are not triggered
 *                                      by modifications to events caused by those same recommenders.
 * Mar 06, 2016   15676    Chris.Golden Added specification of origin ("user" or "other") to recommender
 *                                      execution context, so that recommenders know when they are triggered
 *                                      by a hazard event modification whether the user made the change or
 *                                      not.
 * Mar 14, 2016   12145    mduff        Handle the case where a new event identifier cannot be generated,
 *                                      and clean up error handling.
 * Mar 24, 2016   15676    Chris.Golden Fixed bug that caused null pointer exceptions if no visible sites
 *                                      were specified in the settings (but rather, in the startup config).
 *                                      Also changed setModifiedEventGeometry() to return true if it succeeds
 *                                      in changing the geometry, false otherwise.
 * Mar 26, 2016   15676    Chris.Golden Removed geometry validity checks (that is, checks to see if Geometry
 *                                      objects pass the isValid() test), as the session event manager
 *                                      shouldn't be policing this; it should assume it gets valid geometries.
 *                                      Also added handler for visual feature change notifications to trigger
 *                                      recommenders if appropriate.
 * Apr 04, 2016   17467    Chris.Golden Fixed public addEvent() method to no longer filter out added/modified
 *                                      events by current settings filters, and changed the other addEvent()
 *                                      to give its second parameter a more reasonable name than "localEvent".
 * Apr 04, 2016   15192    Robert.Blum  Added new copyEvents() method.
 * Apr 05, 2016   17328    mduff        Ensure that events are never filtered by "pending" status.
 * Apr 06, 2016    8837    Robert.Blum  Updated addEvent(...) to only site the siteId on local events.
 * Apr 23, 2016   18094    Chris.Golden Added code to ensure VTEC check does not throw exception if the event
 *                                      has no VTEC code (e.g. a PHI event).
 * Apr 28, 2016   18267    Chris.Golden Added support for unrestricted event start times. Also cleaned up and
 *                                      simplified the code handling an event's time range boundaries when
 *                                      said event is issued.
 * May 02, 2016   18235    Chris.Golden Changing the event type no longer changes the end time of the event to
 *                                      the default for the new type if the event's source is a recommender.
 * May 04, 2016   18411    Chris.Golden Changed to persist reissued hazard events to the database.
 * May 06, 2016   18202    Robert.Blum  Changes for operational mode.
 * May 10, 2016   18515    Chris.Golden Changed to optionally deselect a hazard event after the latter is
 *                                      issued, if the current setting specifies this option.
 * May 13, 2016   15676    Chris.Golden Changed to strip visual features from the copy of a hazard event that
 *                                      is being persisted to the database, so that visual features are not
 *                                      stored (shrinking the size of the hazard events). Also changed to
 *                                      handle database-sourced event additions by treating them as if they
 *                                      changed the visual features, so as to trigger recommenders where
 *                                      appropriate.
 * Jun 06, 2016   19432    Chris.Golden Added method to set a flag indicating whether newly-created (by the
 *                                      user) hazard events should be added to the selected set or not. This
 *                                      flag, when set to true, overrides the behavior specified by the
 *                                      current setting with regard to add-to-selected mode.
 * Jun 10, 2016   19537    Chris.Golden Combined base and selected visual feature lists for each hazard
 *                                      event into one, replaced by visibility constraints based upon
 *                                      selection state to individual visual features.
 * Jun 23, 2016   19537    Chris.Golden More combining of base and selected visual feature lists. Also
 *                                      added support for no-hatching type hazard events.
 * Jun 24, 2016   19138   Thomas.Gurney Always save the previous end time value when "Until Further Notice"
 *                                      is checked.
 * Jul 25, 2016   19537    Chris.Golden Changed collections of events that were returned into lists, since
 *                                      the unordered nature of the collections was inappropriate. Added
 *                                      originator parameters for methods for setting high- and low-res
 *                                      geometries for hazard events. Removed obsolete set-geometry method.
 * Jul 26, 2016   20755    Chris.Golden Fixed bug with saving events to database; the saving did not strip
 *                                      out visual features, unlike the storing of events when issued.
 *                                      Also added ability to avoid saving any events with "potential"
 *                                      status.
 * Aug 15, 2016   18376    Chris.Golden Added code to make garbage collection of the messenger instance
 *                                      passed in (which is the app builder) more likely. Also added
 *                                      implementation of new temporary method from interface that indicates
 *                                      whether or not the manager has been shut down.
 * Aug 18, 2016   19537    Chris.Golden Added originator to sortEvents() method so that notifications of
 *                                      ordering changes could be posted. Also changed use of selected
 *                                      events modified notifications to include the identifiers of the
 *                                      events that experienced a selection state change. Also added checks
 *                                      to ensure that notifications do not go out about hazard events that
 *                                      are not currently being managed by the session event manager.
 * Sep 12, 2016   15934    Chris.Golden Changed to work with advanced geometries now used by hazard events.
 *                                      Also removed code that was redundant from addEvent() that added
 *                                      a geometry to an existing event, since this is not the job of the
 *                                      session manager and was already being done by the spatial presenter.
 * Sep 26, 2016   21758    Chris.Golden Changed removeEvent() and removeEvents() to include a boolean
 *                                      parameter indicating whether confirmation should be done or not.
 *                                      If the parameter is true, then any proposed-status events to be
 *                                      removed must be confirmed by the user.
 * Oct 04, 2016   21777    mduff        Optimized the hazard conflict query by not checking ended, elapsed,
 *                                      or proposed hazards.
 * Oct 04, 2016   22573    Chris.Golden Added method to clear CWA geometry.
 * Oct 06, 2016   22894    Chris.Golden Changed persist-event methods to remove any session attributes for
 *                                      a hazard's type from that hazard event prior to persisting it.
 * Oct 12, 2016   21873    Chris.Golden Added code to track the time resolutions of all managed hazard
 *                                      events, and to ensure that the start and end times of said events
 *                                      always lie on the unit boundaries for their particular resolutions
 *                                      (e.g. an event with minute-level resolution must have its start
 *                                      and end times lie on the minute boundaries). Also added code to
 *                                      remove session attributes from a hazard before its type is changed.
 *                                      Also added ability to only react to CAVE clock changes when said
 *                                      changes are resets minutes ticking forward, not seconds (in case
 *                                      the time manager is sending out tick-forward notifications each
 *                                      second).
 * Nov 02, 2016   26024    Chris.Golden Removed metadata validation code from issue #8529, as it should
 *                                      not be needed (product generators should ignore any attributes
 *                                      that don't apply for the hazard types being issued), and it was
 *                                      causing metadata generation to occur multiple times for a single
 *                                      event type change. Changed the triggering of metadata reloads
 *                                      due to attribute changes to only occur if the attribute was set
 *                                      from non-null to some value (so that when the attribute is
 *                                      initialized during metadata generation, it does not trigger a
 *                                      pointless reload of the metadata).
 * Nov 04, 2016   20626    bkowal       Exclude ended events when searching for conflicts to better
 *                                      match what had been originally implemented.
 * Nov 09, 2016   23086    Chris.Golden Prevent geometry edits of point-identifier-requiring hazard
 *                                      events if the edits would cause the point to no longer fall
 *                                      within at least one polygon.
 * Nov 14, 2016   21675    Chris.Golden Fixed bug introduced in previous commit that caused hazard
 *                                      attribute changes that may trigger a metadata reload, but
 *                                      (correctly) do not end up doing so, to not be checked to see if
 *                                      they may instead trigger a recommender execution.
 * Nov 15, 2016   22592    Kevin.Bisanz Prevent modifying geometry of event from other site unless in
 *                                      service backup mode.
 * Nov 17, 2016   26313    Chris.Golden Changed to support multiple UGC types per hazard type, and to
 *                                      work with revamped GeoMapUtilities, moving some code that was
 *                                      previously here into that class as it was poorly placed.
 * Nov 23, 2016   26433    Robert.Blum  Removed Add/Remove shapes menu from hazards created by
 *                                      replacements.
 * Dec 16, 2016   27006    bkowal       Set the replaced hazard type attribute on events that are
 *                                      replacing other hazard events.
 * Dec 19, 2016   21504    Robert.Blum  Adapted to hazard locking.
 * Jan 05, 2017   21504    Robert.Blum  Changed response to lock break notification if the event
 *                                      has elapsed.
 * Feb 01, 2017   15556    Chris.Golden Added methods to get the history count and visible history
 *                                      count for a hazard event. Also moved selection methods to
 *                                      new selection manager, while removing use of the old
 *                                      selected attribute in the hazard event. Revamped the fetching
 *                                      of hazard events for current settings to be more thorough.
 *                                      Added method for reverting to the most recent version of an
 *                                      event that is found in the database. Added handling of the
 *                                      "visible in history list" flag that hazard events now have.
 *                                      Added ability to get duration choices and metadata specifiers
 *                                      for historical snapshots of events. Fixed bug that did not
 *                                      remove an event from the selection set if its status changed
 *                                      to one that was invisible under the current settings.
 * Feb 17, 2017   21676    Chris.Golden Moved SessionEventUtilities methods into this class, making
 *                                      them member methods instead of statics, since the former class
 *                                      has no reason to have its methods separate from this class's
 *                                      methods. Also changed the newly-brought-in merge method to
 *                                      update the recorded end time/duration of a hazard event that
 *                                      has changed status to issued, to avoid exceptions when a
 *                                      hazard event arrives from the database already issued and the
 *                                      clock ticks over to the next minute.
 * Feb 17, 2017   29138    Chris.Golden Removed notion of visible history list (since all events
 *                                      in history list are now visible). Also changed to work with
 *                                      new hazard event manager (interface to the database). Added
 *                                      support for saving to "latest version" set in database, not
 *                                      just to history list. Also changed to avoid persisting to
 *                                      the database upon status changes that should never cause such
 *                                      persistence.
 * Feb 21, 2017   29138    Chris.Golden Changed to pass runnable asynchronous scheduler to notification
 *                                      listener, and to remove latest version of hazard event from
 *                                      database when removing historical versions. Also changed to
 *                                      not select events added as a result of a database notification,
 *                                      and ensured that events saved as a result of the user or a
 *                                      recommender requesting persistence have no issueTime attribute,
 *                                      whereas events being saved because they were just issued do.
 * Mar 15, 2017   29138    Chris.Golden Fixed bug with database query requests that included particular
 *                                      statuses in the query parameters and that returned earlier
 *                                      versions of hazard events with the requested statuses, even
 *                                      though the latest versions of said hazard events have the wrong
 *                                      statuses.
 * Mar 16, 2017   29138    Chris.Golden Added workaround code to differentiate historical versions of
 *                                      events from latest versions; this is needed until the latest
 *                                      version saving is fixed.
 * Mar 17, 2017   15528    Chris.Golden Removed "checked" as an attribute of hazard events, and made
 *                                      checked state instead tracked by this object in a set. Also
 *                                      removed synchronized blocks, as synchronization should not be
 *                                      needed if all work is done on the same thread. Added code to
 *                                      ensure that hazard events are marked as modified when
 *                                      appropriate, and have their modified flag reset when not.
 *                                      Finally, added tracking of which event attributes should not
 *                                      alter an event's modified flag when their values are changed.
 * Mar 30, 2017   15528    Chris.Golden Changed to reset modified flag when asked to do so during the
 *                                      persistence of a hazard event. The modified flag for each
 *                                      hazard event is now part of the state of the event saved to
 *                                      the registry. Also fixed merged hazard events to include
 *                                      setting of workstation and user name to that of new version of
 *                                      event. Also made isEventChecked() only return true if the
 *                                      specified event is visible given the current filtering.
 * Apr 13, 2017   33142    Chris.Golden Changed removeEvent() to remove all copies of the hazard
 *                                      event from the database, instead of having to painstakingly
 *                                      go through and try to find all copies to remove. Also added
 *                                      new method allowing the handling of notifications from the
 *                                      database that all copies of a hazard event were removed. Also
 *                                      added use of new method in session manager to remember the
 *                                      identifiers of removed hazard events.
 * Apr 25, 2017   33376    Chris.Golden Fixed bug introduced when time resolution was added that
 *                                      mistakenly rounded end times that were set to the "until
 *                                      further notice" magic number, causing all sorts of UFN
 *                                      problems. Also fixed changing of hazard type so that if
 *                                      "until further notice" was on for the old type, it is
 *                                      turned off in the new type.
 * May 24, 2017   15561    Chris.Golden Removed unneeded validation of significance for new events
 *                                      (as such things should not be hardcoded).
 * May 31, 2017   34684    Chris.Golden Added invocation of recommenders in response to selection
 *                                      changes for events.
 * Jun 01, 2017   23056    Chris.Golden Added code to the metadata refreshing method to allow any
 *                                      hazard attributes whose metadata definitions specify that
 *                                      they should always use the default values from their
 *                                      metadata definitions, rather than old values that may be
 *                                      found in the event, to use said default values.
 * Jun 01, 2017   15561    Chris.Golden Fixed bug introduced in changeset for #34684 that caused
 *                                      an exception when events that are selected are removed.
 * Jun 05, 2017   15561    Chris.Golden Added code to respond to a hazard event undoing or redoing
 *                                      a geometry change by updating its hazard area.
 * Jun 21, 2017   18375    Chris.Golden Added setting of potential events to pending status when
 *                                      they are modified. Also added use of new flag in observed
 *                                      hazard event that prevents its modified flag from changing,
 *                                      so that addition of an event, or changing its time range
 *                                      due to a time boundary change, does not cause the event to
 *                                      be marked as modified.
 * Sep 11, 2017   29138    Chris.Golden Removed temporary code that prevented hazard events from
 *                                      having their visual features saved to and restored from
 *                                      the registry (because the serialization/deserialization
 *                                      process would choke on the visual features until now;
 *                                      changes made in 18-Hazard_Services that were merged into
 *                                      this repo now allow visual features to be saved properly.)
 * Sep 27, 2017   38072    Chris.Golden Switched over to receiving intra-managerial notifications
 *                                      instead of listening on the event bus for notifications.
 *                                      Also now making use of batching of messages. Also removed
 *                                      all recommender-triggering code from here, so that it
 *                                      could be placed in the session recommender manager where
 *                                      it belongs.
 * Oct 23, 2017   21730    Chris.Golden Added method to set a hazard event to the default hazard
 *                                      type as configured, if any. Also adjusted implementations
 *                                      of IIntraNotificationHander to adjust their isSynchronous()
 *                                      methods to take the new parameter. Also fixed metadata
 *                                      fetching to occur asynchronously when triggered by a status
 *                                      change or by an attribute change.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * Dec 13, 2017   40923    Chris.Golden Added handling of returned modified hazard event from
 *                                      metadata fetch by merging it into session hazard event.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly mutable session events
 *                                      for all but the classes in this package. Also added
 *                                      extensive checking to ensure that locks are acquired, etc.
 *                                      as necessary. All changing of session events now happens
 *                                      via this class, and other classes must request such
 *                                      changes, not make them directly. Also changed addEvent()
 *                                      to no longer accept the job of merging hazard events; this
 *                                      is more properly the reason mergeHazardEvents() exists.
 *                                      Also fixed bug in mergeHazardEvents() that used an origin
 *                                      of OTHER regardless of the actual origin.
 * Jan 17, 2018   33428    Chris.Golden Changed to use new version of method to get union of
 *                                      polygonal elements of geometry. Also added use of new
 *                                      first-class attribute of hazard event, issuance count.
 * Jan 31, 2018   25765    Chris.Golden Fixed erroneously displayed error messages when an unlock
 *                                      of a hazard event could not occur because the hazard event
 *                                      was (rightfully) not locked to begin with.
 * Feb 06, 2018   46258    Chris.Golden Changed revert to last saved to use the RevertOriginator
 *                                      instead of Originator.OTHER for the mergeHazardEvents()
 *                                      invocation. Also fixed a bug that caused null pointer
 *                                      exceptions when checking for conflicting hazards.
 * Feb 08, 2018   44464    Chris.Golden Fixed annoying "Are you sure you want to override high-
 *                                      resolution geometry?" messages that were appearing at
 *                                      times when they made no sense.
 * Feb 13, 2018   20595    Chris.Golden Made setting of event type always have an origin of OTHER
 *                                      to avoid running recommenders multiple times due to
 *                                      triggering once by a user-based origin, and another time
 *                                      by the knock-on other-based origin changes. Also made
 *                                      event type changes result in visual features being cleared.
 * Feb 13, 2018   44514    Chris.Golden Removed event-modifying script code, as such scripts are
 *                                      not to be used.
 * Feb 21, 2018   46736    Chris.Golden Added code to make non-persisting visual features and
 *                                      session attributes be kept firewalled from the database,
 *                                      so that they are maintained in the session regardless of
 *                                      what the database holds. Also changed the behavior of
 *                                      changeEventProperty() to not lock a hazard event if the
 *                                      changed property is session-specific. Also simplified
 *                                      mergeHazardEvents().
 * Mar 19, 2018   48027    Chris.Golden Changed to allow recommenders, etc. to change status of
 *                                      hazard events from ENDED to ELAPSED. Also changed to
 *                                      check for geometry validity when merging hazard events
 *                                      from a recommender run's result event set.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SessionEventManager implements ISessionEventManager {

    // Private Static Constants

    private static final Set<String> ATTRIBUTES_TO_RETAIN_ON_MERGE = ImmutableSet
            .of(HazardConstants.ISSUED, HazardConstants.ISSUE_TIME);

    private static final String POINT_ID = "id";

    private static final String GEOMETRY_MODIFICATION_ERROR = "Geometry Modification Error";

    private static final double GEOMETRY_BUFFER_DISTANCE = .000001;

    private static final IOriginator USER_ORIGINATOR = new IOriginator() {

        @Override
        public boolean isDirectResultOfUserInput() {
            return true;
        }

        @Override
        public boolean isNotLockedByOthersRequired() {
            return true;
        }
    };

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SessionEventManager.class);

    // Private Variables

    private final ISessionManager<ObservedSettings> sessionManager;

    private final ISessionTimeManager timeManager;

    private final ISessionConfigurationManager<ObservedSettings> configManager;

    private final IHazardEventManager dbManager;

    private final ISessionNotificationSender notificationSender;

    private final List<ObservedHazardEvent> allEvents = new ArrayList<>();

    private final List<IHazardEventView> allEventViews = new ArrayList<>();

    private final BiMap<ObservedHazardEvent, HazardEventView> viewsForHazardEvents = HashBiMap
            .create();

    /*
     * TODO: Decide whether expiration is to be done in Java code or not. For
     * now, this code is commented out; not expiration of events will occur.
     * This is not a permanent solution.
     */
    // private Timer eventExpirationTimer = new Timer(true);
    //
    // private final Map<String, TimerTask> expirationTasks = new
    // ConcurrentHashMap<String, TimerTask>();

    private ISimulatedTimeChangeListener timeListener;

    private final Set<IHazardEventView> eventViewsUndergoingMetadataFetch = new HashSet<>();

    private final Set<String> identifiersOfEventsAllowingUntilFurtherNotice = new HashSet<>();

    private final Map<String, Range<Long>> startTimeBoundariesForEventIdentifiers = new HashMap<>();

    private final Map<String, Range<Long>> endTimeBoundariesForEventIdentifiers = new HashMap<>();

    private final Map<String, TimeResolution> timeResolutionsForEventIdentifiers = new HashMap<>();

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
     * Map pairing event identifiers with the "latest version" of the hazard
     * events as last fetched from the database.
     */
    private final Map<String, HazardEvent> latestVersionsFromDatabaseForEventIdentifiers = new HashMap<>();

    /**
     * Map pairing event identifiers for all the {@link #allEvents} with
     * historical versions with the number of historical versions. If an event
     * identifier has no associated value within this map, the event has no
     * historical versions.
     */
    private final Map<String, Integer> historicalVersionCountsForEventIdentifiers = new HashMap<>();

    /**
     * Identifiers of events that are in the database.
     */
    private final Set<String> identifiersOfPersistedEvents = new HashSet<>();

    /**
     * Set of identifiers for all events that are currently checked.
     */
    private final Set<String> checkedEventIdentifiers = new HashSet<>();

    /**
     * Set of identifiers for all events that have "Ending" status.
     */
    private final Set<String> eventIdentifiersWithEndingStatus = new HashSet<>();

    private final Map<String, Collection<IReadableHazardEvent>> conflictingEventsForSelectedEventIdentifiers = new HashMap<>();

    private final Map<String, MegawidgetSpecifierManager> megawidgetSpecifiersForEventIdentifiers = new HashMap<>();

    private final Map<String, Set<String>> metadataReloadTriggeringIdentifiersForEventIdentifiers = new HashMap<>();

    private final Map<String, Set<String>> metadataIdentifiersAffectingModifyFlagsForEventIdentifiers = new HashMap<>();

    private final Map<String, Map<String, String>> recommendersForTriggerIdentifiersForEventIdentifiers = new HashMap<>();

    private final Map<String, Set<String>> editRiseCrestFallTriggeringIdentifiersForEventIdentifiers = new HashMap<>();

    /**
     * The messenger for displaying questions and warnings to the user and
     * retrieving answers. This allows the viz side (App Builder) to be
     * responsible for these dialogs, but gives the event manager and other
     * managers access to them without creating a dependency on the
     * <code>gov.noaa.gsd.viz.hazards</code> plugin. Since all parts of Hazard
     * Services can use the same code for creating these dialogs, it makes it
     * easier for them to be stubbed for testing.
     */
    private IMessenger messenger;

    private final GeometryFactory geometryFactory;

    private ObservedHazardEvent currentEvent;

    private final GeoMapUtilities geoMapUtilities;

    private final RiverForecastManager riverForecastManager;

    private boolean addCreatedEventsToSelected;

    /**
     * Intra-managerial notification handler for session event additions.
     */
    private IIntraNotificationHandler<SessionEventsAdded> eventsAddedHandler = new IIntraNotificationHandler<SessionEventsAdded>() {

        @Override
        public void handleNotification(SessionEventsAdded notification) {
            sessionEventsAdded(notification);
        }

        @Override
        public boolean isSynchronous(SessionEventsAdded notification) {
            return true;
        }
    };

    /**
     * Intra-managerial notification handler for session event removals.
     */
    private IIntraNotificationHandler<SessionEventsRemoved> eventsRemovedHandler = new IIntraNotificationHandler<SessionEventsRemoved>() {

        @Override
        public void handleNotification(SessionEventsRemoved notification) {
            sessionEventsRemoved(notification);
        }

        @Override
        public boolean isSynchronous(SessionEventsRemoved notification) {
            return true;
        }
    };

    /**
     * Intra-managerial notification handler for session event changes.
     */
    private IIntraNotificationHandler<SessionEventModified> eventChangeHandler = new IIntraNotificationHandler<SessionEventModified>() {

        @Override
        public void handleNotification(SessionEventModified notification) {
            for (IEventModification modification : notification
                    .getModifications()) {
                if (modification instanceof EventTypeModification) {
                    sessionEventTypeModified(notification.getEvent());
                } else if (modification instanceof EventTimeRangeModification) {
                    sessionEventTimeRangeModified(notification.getEvent(),
                            notification.getOriginator());
                } else if (modification instanceof EventStatusModification) {
                    sessionEventStatusModified(notification.getEvent(),
                            notification.getOriginator());
                } else if (modification instanceof EventGeometryModification) {
                    sessionEventGeometryModified(notification.getEvent(),
                            notification.getOriginator());
                } else if (modification instanceof EventAttributesModification) {
                    sessionEventAttributesModified(notification.getEvent(),
                            (EventAttributesModification) modification,
                            notification.getOriginator());
                }
            }
        }

        @Override
        public boolean isSynchronous(SessionEventModified notification) {
            return true;
        }
    };

    /**
     * Intra-managerial notification handler for hazard event lock status
     * changes.
     */
    private IIntraNotificationHandler<SessionEventsLockStatusModified> eventLockChangeHandler = new IIntraNotificationHandler<SessionEventsLockStatusModified>() {

        @Override
        public void handleNotification(
                SessionEventsLockStatusModified notification) {
            sessionEventsLockStatusModified(notification.getEventIdentifiers(),
                    notification.getOriginator());
        }

        @Override
        public boolean isSynchronous(
                SessionEventsLockStatusModified notification) {
            return true;
        }
    };

    /**
     * Intra-managerial notification handler for product generation completions.
     */
    private IIntraNotificationHandler<ProductGenerationComplete> productGenerationCompletionHandler = new IIntraNotificationHandler<ProductGenerationComplete>() {

        @Override
        public void handleNotification(ProductGenerationComplete notification) {
            productGenerationComplete(notification);
        }

        @Override
        public boolean isSynchronous(ProductGenerationComplete notification) {
            return true;
        }
    };

    /**
     * Intra-managerial notification handler for settings changes.
     */
    private IIntraNotificationHandler<SettingsModified> settingsChangeHandler = new IIntraNotificationHandler<SettingsModified>() {

        @Override
        public void handleNotification(SettingsModified notification) {
            settingsModified(notification);
        }

        @Override
        public boolean isSynchronous(SettingsModified notification) {
            return true;
        }
    };

    /**
     * Intra-managerial notification handler for current time changes.
     */
    private IIntraNotificationHandler<CurrentTimeChanged> currentTimeChangeHandler = new IIntraNotificationHandler<CurrentTimeChanged>() {

        @Override
        public void handleNotification(CurrentTimeChanged notification) {
            currentTimeChanged(notification);
        }

        @Override
        public boolean isSynchronous(CurrentTimeChanged notification) {
            return true;
        }
    };

    /**
     * Flag indicating whether the manager has been shut down.
     * 
     * @deprecated This should be removed once garbage collection problems have
     *             been sorted out; see Redmine issue #21271.
     */
    @Deprecated
    private boolean shutDown = false;

    // Public Constructors

    @SuppressWarnings("unchecked")
    public SessionEventManager(ISessionManager<ObservedSettings> sessionManager,
            ISessionTimeManager timeManager,
            ISessionConfigurationManager<ObservedSettings> configManager,
            IHazardEventManager dbManager,
            ISessionNotificationSender notificationSender,
            IMessenger messenger) {
        this.sessionManager = sessionManager;
        this.configManager = configManager;
        this.timeManager = timeManager;
        this.dbManager = dbManager;
        this.notificationSender = notificationSender;
        new SessionHazardNotificationListener(this,
                sessionManager.getRunnableAsynchronousScheduler());
        SimulatedTime.getSystemTime()
                .addSimulatedTimeChangeListener(createTimeListener());
        this.messenger = messenger;
        geometryFactory = new GeometryFactory();
        this.geoMapUtilities = new GeoMapUtilities(this.configManager);
        this.riverForecastManager = new RiverForecastManager();

        notificationSender.registerIntraNotificationHandler(
                SessionEventsAdded.class, eventsAddedHandler);
        notificationSender.registerIntraNotificationHandler(
                SessionEventsRemoved.class, eventsRemovedHandler);
        notificationSender.registerIntraNotificationHandler(
                SessionEventModified.class, eventChangeHandler);
        notificationSender.registerIntraNotificationHandler(
                SessionEventsLockStatusModified.class, eventLockChangeHandler);
        notificationSender.registerIntraNotificationHandler(
                ProductGenerationComplete.class,
                productGenerationCompletionHandler);
        notificationSender.registerIntraNotificationHandler(
                Sets.newHashSet(SettingsModified.class, SettingsLoaded.class),
                settingsChangeHandler);
        notificationSender.registerIntraNotificationHandler(
                Sets.<Class<? extends CurrentTimeChanged>> newHashSet(
                        CurrentTimeReset.class, CurrentTimeMinuteTicked.class),
                currentTimeChangeHandler);
    }

    // Public Methods

    @Override
    public IHazardEventView getEventById(String identifier) {
        for (IHazardEventView eventView : allEventViews) {
            if (eventView.getEventID().equals(identifier)) {
                return eventView;
            }
        }
        return null;
    }

    @Override
    public List<IHazardEventView> getEventHistoryById(String identifier) {

        /*
         * TODO: History lists should be cached; this is wasteful.
         */
        HazardHistoryList hazardHistoryList = dbManager
                .getHistoryByEventID(identifier, false);
        if (hazardHistoryList == null) {
            return null;
        }
        List<IHazardEventView> viewList = new ArrayList<>(
                hazardHistoryList.size());
        for (IHazardEvent event : hazardHistoryList) {
            viewList.add(new HazardEventView(event));
        }
        return viewList;
    }

    @Override
    public int getHistoricalVersionCountForEvent(String identifier) {
        Integer count = historicalVersionCountsForEventIdentifiers
                .get(identifier);
        return (count == null ? 0 : count);
    }

    @Override
    public Collection<IHazardEventView> getEventsByStatus(HazardStatus status,
            boolean includeUntyped) {
        Collection<? extends IHazardEventView> allEventViews = getEvents();
        Collection<IHazardEventView> eventViews = new ArrayList<>(
                allEventViews.size());
        for (IHazardEventView eventView : allEventViews) {
            if (eventView.getStatus().equals(status) && (includeUntyped
                    || (eventView.getHazardType() != null))) {
                eventViews.add(eventView);
            }
        }
        return eventViews;
    }

    @Override
    public List<IHazardEventView> getCheckedEvents() {
        Collection<IHazardEventView> allEventViews = getEventsForCurrentSettings();
        List<IHazardEventView> eventViews = new ArrayList<>(
                allEventViews.size());
        for (IHazardEventView eventView : allEventViews) {
            if (checkedEventIdentifiers.contains(eventView.getEventID())) {
                eventViews.add(eventView);
            }
        }
        return eventViews;
    }

    /**
     * Determine whether or not the two strings are equivalent; equivalence
     * includes each string being either <code>null</code> or empty, as well as
     * both strings having the same characters.
     * 
     * @param string1
     *            First string to compare.
     * @param string2
     *            Second string to compare.
     * @return True if equivalent, false otherwise.
     */
    private boolean areEquivalent(String string1, String string2) {
        return ((((string1 == null) || string1.isEmpty())
                && ((string2 == null) || string2.isEmpty()))
                || ((string1 != null) && string1.equals(string2)));
    }

    private boolean userConfirmationAsNecessary(IReadableHazardEvent event) {
        if (event.getHazardAttribute(VISIBLE_GEOMETRY)
                .equals(LOW_RESOLUTION_GEOMETRY_IS_VISIBLE)) {
            IQuestionAnswerer answerer = messenger.getQuestionAnswerer();
            return answerer.getUserAnswerToQuestion(
                    "Are you sure you want to overwrite the high resolution geometry?");
        }
        return true;
    }

    @Override
    public List<IHazardEventView> getEventsForCurrentSettings() {
        List<IHazardEventView> result = getEvents();
        filterEventsForConfig(result);
        return result;
    }

    @Override
    public <T> EventPropertyChangeResult changeEventProperty(
            IHazardEventView event, EventPropertyChange<T> propertyChange,
            T parameters) {
        return changeEventProperty(event, propertyChange, parameters,
                Originator.OTHER);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> EventPropertyChangeResult changeEventProperty(
            IHazardEventView event, EventPropertyChange<T> propertyChange,
            T parameters, IOriginator originator) {

        /*
         * Ensure the event is being managed.
         */
        ObservedHazardEvent sessionEvent = getSessionEventForView(event);
        if (sessionEvent == null) {
            return EventPropertyChangeResult.FAILURE_DUE_TO_EVENT_NOT_FOUND;
        }

        /*
         * If the change generally requires locking, and the event is in the
         * database, then see if the change actually needs locking. (Changes
         * that are solely to session attributes or to non-persisting visual
         * features do not require a lock.) If it does, get a lock on the event,
         * aborting the change attempt and notifying the user if the lock cannot
         * be acquired.
         */
        if (originator.isNotLockedByOthersRequired()
                && isEventInDatabase(event)) {

            boolean requiresLock = true;
            if ((propertyChange == ADD_EVENT_ATTRIBUTE)
                    || (propertyChange == ADD_EVENT_ATTRIBUTES)
                    || (propertyChange == SET_EVENT_ATTRIBUTES)
                    || (propertyChange == REMOVE_EVENT_ATTRIBUTE)) {
                Set<String> sessionAttributes = new HashSet<>(configManager
                        .getSessionAttributes(sessionEvent.getHazardType()));
                Set<String> changedAttributes = (propertyChange == ADD_EVENT_ATTRIBUTE
                        ? Sets.newHashSet(
                                ((Pair<String, Serializable>) parameters)
                                        .getFirst())
                        : (propertyChange == ADD_EVENT_ATTRIBUTES
                                ? ((Map<String, Serializable>) parameters)
                                        .keySet()
                                : (propertyChange == SET_EVENT_ATTRIBUTES
                                        ? Sets.union(
                                                ((Map<String, Serializable>) parameters)
                                                        .keySet(),
                                                sessionEvent
                                                        .getHazardAttributes()
                                                        .keySet())
                                        : Sets.newHashSet(
                                                (String) parameters))));
                if (Sets.difference(changedAttributes, sessionAttributes)
                        .isEmpty()) {
                    requiresLock = false;
                }

            } else if (propertyChange == REPLACE_EVENT_VISUAL_FEATURE) {
                VisualFeature newFeature = (VisualFeature) parameters;
                VisualFeature oldFeature = sessionEvent
                        .getVisualFeature(newFeature.getIdentifier());
                if (((newFeature == null) || (newFeature.isPersist() == false))
                        && ((oldFeature == null)
                                || (oldFeature.isPersist() == false))) {
                    requiresLock = false;
                }

            } else if (propertyChange == SET_EVENT_VISUAL_FEATURES) {
                VisualFeaturesList oldFeatures = getPersistentVisualFeatures(
                        sessionEvent.getVisualFeatures());
                VisualFeaturesList newFeatures = getPersistentVisualFeatures(
                        (VisualFeaturesList) parameters);
                if (oldFeatures.equals(newFeatures)) {
                    requiresLock = false;
                }
            }

            if (requiresLock && (sessionManager.getLockManager()
                    .lockHazardEvent(event.getEventID()) == false)) {
                showWarningMessageAboutLockedEvents("Cannot Modify", "modified",
                        originator, event.getEventID());
                return EventPropertyChangeResult.FAILURE_DUE_TO_LOCK_STATUS;
            }
        }

        /*
         * Attempt to change the event property as directed.
         */
        if (propertyChange == SET_EVENT_CATEGORY) {
            setEventCategory(sessionEvent, (String) parameters, originator);
        } else if (propertyChange == SET_EVENT_TYPE) {
            EventType type = (EventType) parameters;
            setEventType(sessionEvent, type.getPhenomenon(),
                    type.getSignificance(), type.getSubType(), originator);
        } else if (propertyChange == SET_EVENT_TYPE_TO_DEFAULT) {
            if (setEventTypeToDefault(sessionEvent, originator) == false) {
                return EventPropertyChangeResult.FAILURE_DUE_TO_BAD_VALUE;
            }
        } else if (propertyChange == SET_EVENT_CREATION_TIME) {
            sessionEvent.setCreationTime((Date) parameters, originator);
        } else if (propertyChange == SET_EVENT_START_TIME) {
            sessionEvent.setStartTime((Date) parameters, originator);
        } else if (propertyChange == SET_EVENT_END_TIME) {
            sessionEvent.setEndTime((Date) parameters, originator);
        } else if (propertyChange == SET_EVENT_TIME_RANGE) {
            Pair<Date, Date> startAndEndTimes = (Pair<Date, Date>) parameters;
            if (setEventTimeRange(sessionEvent, startAndEndTimes.getFirst(),
                    startAndEndTimes.getSecond(), originator) == false) {
                return EventPropertyChangeResult.FAILURE_DUE_TO_BAD_VALUE;
            }
        } else if (propertyChange == SET_EVENT_GEOMETRY) {
            if (setEventGeometry(sessionEvent, (IAdvancedGeometry) parameters,
                    originator) == false) {
                return EventPropertyChangeResult.FAILURE_DUE_TO_BAD_VALUE;
            }
        } else if (propertyChange == REPLACE_EVENT_VISUAL_FEATURE) {
            if (sessionEvent.setVisualFeature((VisualFeature) parameters,
                    originator) == false) {
                return EventPropertyChangeResult.FAILURE_DUE_TO_BAD_VALUE;
            }
        } else if (propertyChange == SET_EVENT_VISUAL_FEATURES) {
            sessionEvent.setVisualFeatures((VisualFeaturesList) parameters,
                    originator);
        } else if (propertyChange == SET_EVENT_SOURCE) {
            sessionEvent.setSource((Source) parameters, originator);
        } else if (propertyChange == SET_EVENT_WORKSTATION_IDENTIFIER) {
            sessionEvent.setWsId((WsId) parameters, originator);
        } else if (propertyChange == SET_EVENT_ATTRIBUTES) {
            sessionEvent.setHazardAttributes(
                    (Map<String, Serializable>) parameters, originator);
        } else if (propertyChange == ADD_EVENT_ATTRIBUTE) {
            Pair<String, Serializable> keyAndValue = (Pair<String, Serializable>) parameters;
            sessionEvent.addHazardAttribute(keyAndValue.getFirst(),
                    keyAndValue.getSecond(), originator);
        } else if (propertyChange == ADD_EVENT_ATTRIBUTES) {
            sessionEvent.addHazardAttributes(
                    (Map<String, Serializable>) parameters, originator);
        } else if (propertyChange == REMOVE_EVENT_ATTRIBUTE) {
            sessionEvent.removeHazardAttribute((String) parameters, originator);
        } else {
            throw new IllegalArgumentException(
                    "unknown property change \"" + parameters + "\"");
        }
        return EventPropertyChangeResult.SUCCESS;
    }

    /**
     * Get the persistent visual features from the specified list.
     * 
     * @param visualFeatures
     *            List of visual features from which to extract a list of those
     *            which are persistent.
     * @return Persistent visual features.
     */
    private VisualFeaturesList getPersistentVisualFeatures(
            VisualFeaturesList visualFeatures) {
        VisualFeaturesList persistentVisualFeatures = new VisualFeaturesList();
        if (visualFeatures == null) {
            return persistentVisualFeatures;
        }
        for (VisualFeature visualFeature : visualFeatures) {
            if (visualFeature.isPersist()) {
                persistentVisualFeatures.add(visualFeature);
            }
        }
        return persistentVisualFeatures;
    }

    /**
     * Set the specified event to have the specified category. As a side effect,
     * the event is changed to have no type.
     * 
     * @param event
     *            Event to be modified.
     * @param category
     *            Category for the event.
     * @param originator
     *            Originator of this change.
     */
    private void setEventCategory(ObservedHazardEvent event, String category,
            IOriginator originator) {
        if (!canEventTypeBeChanged(event)) {
            throw new IllegalStateException(
                    "cannot change type of event " + event.getEventID());
        }
        sessionManager.startBatchedChanges();
        event.addHazardAttribute(HazardConstants.HAZARD_EVENT_CATEGORY,
                category, originator);
        event.setHazardType(null, null, null, Originator.OTHER);
        sessionManager.finishBatchedChanges();
    }

    /**
     * Set the specified event to have the specified type. If the former cannot
     * change its type, a new event will be created as a result.
     * 
     * @param event
     *            Event to be modified.
     * @param phenomenon
     *            Phenomenon, or <code>null</code> if the event is to have no
     *            type.
     * @param significance
     *            Phenomenon, or <code>null</code> if the event is to have no
     *            type.
     * @param subType
     *            Phenomenon, or <code>null</code> if the event is to have no
     *            subtype.
     * @param originator
     *            Originator of this change.
     * @return True if the event type was set, or false if the attempt resulted
     *         in the creation of a new event with the new type, and the
     *         original event has not had its type changed.
     */
    private boolean setEventType(ObservedHazardEvent event, String phenomenon,
            String significance, String subType, IOriginator originator) {

        /*
         * If nothing new is being set, do nothing.
         */
        if (areEquivalent(event.getPhenomenon(), phenomenon)
                && areEquivalent(event.getSignificance(), significance)
                && areEquivalent(event.getSubType(), subType)) {
            return true;
        }

        sessionManager.startBatchedChanges();

        /*
         * If the event cannot change type, create a new event with the new
         * type. Otherwise, record the old time resolution for this event, as it
         * may change.
         */
        TimeResolution oldTimeResolution = configManager
                .getTimeResolution(event);
        ObservedHazardEvent oldEvent = null;
        if (canEventTypeBeChanged(event) == false) {
            oldEvent = event;
            IHazardEvent baseEvent = new BaseHazardEvent(event);
            baseEvent.setEventID("");
            baseEvent.setStatus(HazardStatus.PENDING);
            baseEvent.addHazardAttribute(HazardConstants.REPLACES,
                    configManager.getHeadline(oldEvent));
            baseEvent.addHazardAttribute(HazardConstants.REPLACED_HAZARD_TYPE,
                    oldEvent.getHazardType());

            /*
             * Remove any type-specific session attributes from the copy.
             */
            for (String sessionAttribute : configManager
                    .getSessionAttributes(event.getHazardType())) {
                baseEvent.removeHazardAttribute(sessionAttribute);
            }

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
             * Need to remove the add/remove shapes context menu for the new
             * event.
             */
            removeAddRemoveShapesContextMenuOption(baseEvent);

            /*
             * The originator should be the session manager, since the addition
             * of a new event is occurring.
             */
            originator = Originator.OTHER;

            /*
             * Add the event, and add it to the selection as well.
             */
            try {
                event = getSessionEventForView(
                        addEvent(baseEvent, false, originator));
            } catch (HazardEventServiceException e) {
                statusHandler
                        .error("Could not add new event that was to replace event "
                                + "that was having its type changed but which could "
                                + "not change type.", e);
                sessionManager.finishBatchedChanges();
                return false;
            }
            sessionManager.getSelectionManager()
                    .addEventToSelectedEvents(event.getEventID(), originator);
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
                        configManager.getHeadline(tempEvent), originator);
                oldEvent.setStatus(HazardStatus.ENDING);
            }

            /*
             * Assign the new type.
             * 
             * TODO: This is given an originator of OTHER at all times in order
             * to get around running triggered recommenders (that is, those
             * triggered by event type changes and by other knock-on effects of
             * event type changes, like attributes being set) without combining
             * them. The problem is that the SessionRecommenderManager cannot
             * merge two recommender execution requests together that are from
             * different origins, so having this be a user-triggered type
             * change, and then the knock-on effects being other-triggered
             * attribute changes, for example, makes the recommender manager run
             * the recommender twice, once for the type change (of origin user),
             * and once for the attribute changes (of origin other). This is a
             * temporary fix to make them batch nicely. A more permanent
             * solution is to make the recommender manager able to run a
             * triggered recommender with multiple origins, with each origin
             * having the things that changed. But that means some reworking of
             * recommenders so it is not something to be done until there is
             * time.
             */
            event.setHazardType(phenomenon, significance, subType,
                    Originator.OTHER);

            /*
             * Make sure the updated hazard type is a part of the visible types
             * in the current setting. If not, add it.
             */
            Set<String> visibleTypes = configManager.getSettings()
                    .getVisibleTypes();
            visibleTypes.add(HazardEventUtilities.getHazardType(event));
            configManager.getSettings().setVisibleTypes(visibleTypes,
                    Originator.OTHER);
        } else {
            event.setHazardType(null, null, null, originator);
        }

        /*
         * Remember the time resolution for the hazard with the new type.
         */
        TimeResolution newTimeResolution = configManager
                .getTimeResolution(event);
        timeResolutionsForEventIdentifiers.put(event.getEventID(),
                newTimeResolution);

        /*
         * If the time resolution has been reduced, the start and end times may
         * need truncation (though in the case of the end time, not if it is
         * "until further notice"). Additionally, if this is not a new event and
         * it is not from a recommender, change the end time to be offset from
         * the start time by the default duration.
         */
        boolean timeResolutionReduced = ((oldTimeResolution == TimeResolution.SECONDS)
                && (newTimeResolution == TimeResolution.MINUTES));
        Date oldStartTime = event.getStartTime();
        Date newStartTime = (timeResolutionReduced
                ? roundDateDownToNearestMinute(oldStartTime) : oldStartTime);
        Date oldEndTime = event.getEndTime();
        Date newEndTime = ((oldEvent == null)
                && (event.getSource() != IHazardEventView.Source.RECOMMENDER)
                        ? new Date(newStartTime.getTime()
                                + configManager.getDefaultDuration(event))
                        : (oldEndTime
                                .getTime() != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS
                                        ? roundDateDownToNearestMinute(
                                                oldEndTime)
                                        : oldEndTime));

        /*
         * If this event is changing type (as opposed to a new event created
         * because the type cannot change on the original), and it is not from a
         * recommender, remove any recorded interval from before
         * "until further notice" had been turned on, in case it was, since this
         * could lead to the wrong interval being used for the new event type if
         * "until further notice" is subsequently turned off.
         */
        if ((oldEvent == null)
                && (event.getSource() != IHazardEventView.Source.RECOMMENDER)) {
            event.removeHazardAttribute(
                    HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE);
            event.removeHazardAttribute(
                    HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
        }

        /*
         * If the new start or end times differ from the old ones, change the
         * event's time range.
         */
        if ((newStartTime.equals(oldStartTime) == false)
                || (newEndTime.equals(oldEndTime) == false)) {
            event.setTimeRange(newStartTime, newEndTime);
        }

        /*
         * Remove any visual features.
         */
        event.setVisualFeatures(null);

        /*
         * Update the time boundaries and duration choices, if any.
         */
        IHazardEventView eventView = getViewForSessionEvent(event);
        updateTimeBoundariesForEvents(eventView, false);
        updateDurationChoicesForEvent(eventView, false);

        /*
         * If this is a new event, convert its end time to a duration if it does
         * indeed have a duration. This handles the case where the replacement
         * has a duration-based end time, whereas the original event has an
         * absolute end time.
         */
        if (oldEvent != null) {
            convertEndTimeToDuration(event);
        }

        sessionManager.finishBatchedChanges();

        return (oldEvent == null);
    }

    /**
     * Set the specified event to have the default event type, if one has been
     * specified in the configuration.
     * 
     * @param event
     *            Event to be modified.
     * @param originator
     *            Originator of this change.
     * @return <code>true</code> if the event type was set, <code>false</code>
     *         if no default type was found.
     */
    private boolean setEventTypeToDefault(ObservedHazardEvent event,
            IOriginator originator) {

        /*
         * Ensure that the event's status is appropriate.
         */
        HazardStatus status = event.getStatus();
        if ((status != null) && (status != HazardStatus.POTENTIAL)
                && (status != HazardStatus.PENDING)) {
            statusHandler.error(
                    "Cannot set event type to default for events that have been issued or proposed.",
                    new IllegalStateException());
            return false;
        }

        /*
         * Do nothing unless a default type is available.
         */
        String defaultType = configManager.getSettingsValue(
                HazardConstants.HAZARD_EVENT_TYPE, configManager.getSettings());
        if ((defaultType != null) && (defaultType.isEmpty() == false)) {

            /*
             * Do nothing if the default type is not defined.
             */
            if (configManager.getHazardTypes()
                    .containsKey(defaultType) == false) {
                statusHandler.error("Bad configuration: default hazard type \""
                        + defaultType
                        + "\" has no definition. Please ensure that HazardTypes.py "
                        + "has an entry for the default hazard type.");
                return false;
            }

            sessionManager.startBatchedChanges();

            /*
             * Set the event's type to the default type, and associate its
             * category
             */
            String[] typeComponents = HazardEventUtilities
                    .getHazardPhenSigSubType(defaultType);
            setEventType(event, typeComponents[0], typeComponents[1],
                    typeComponents[2], originator);
            String category = configManager.getHazardCategory(event);
            if ((category == null) || category.isEmpty()) {
                statusHandler.error("Bad configuration: default hazard type \""
                        + defaultType
                        + "\" has no associated hazard category. Please "
                        + "ensure that HazardCategories.py has an entry for "
                        + "the default hazard type.");
            } else {
                event.addHazardAttribute(HazardConstants.HAZARD_EVENT_CATEGORY,
                        category, originator);
            }

            sessionManager.finishBatchedChanges();

            return true;
        }
        return false;
    }

    /**
     * Set the specified event's time range.
     * 
     * @param event
     *            Event to be modified.
     * @param startTime
     *            New start time.
     * @param endTime
     *            New end time.
     * @param originator
     *            Originator of this change.
     * @return True if the new time range is now in use, false if it was
     *         rejected because one or both values fell outside their allowed
     *         boundaries.
     */
    private boolean setEventTimeRange(ObservedHazardEvent event, Date startTime,
            Date endTime, IOriginator originator) {

        /*
         * Ensure that the start and end times are rounded down to the most
         * recent minute boundary if the time resolution for this event is
         * minute-level.
         */
        if (timeResolutionsForEventIdentifiers
                .get(event.getEventID()) == TimeResolution.MINUTES) {
            startTime = roundDateDownToNearestMinute(startTime);
            if (endTime
                    .getTime() != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
                endTime = roundDateDownToNearestMinute(endTime);
            }
        }

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
         * the event has a duration and the latter has not changed, remember to
         * shift whichever (or both) end time boundaries to accommodate the new
         * end time and (if not "until further notice") whatever other possible
         * durations the event is allowed. This allows duration-equipped hazard
         * events that have their durations limited (they cannot be expanded, or
         * cannot be shrunk, or both) to still have their end times displaced as
         * the user changes the start time, and also allows "until further
         * notice" to be accommodated. Otherwise, ensure the event end time
         * falls within the correct bounds.
         */
        boolean hasDuration = (configManager.getDurationChoices(event)
                .isEmpty() == false);
        boolean updateEndTimeBoundaries = false;
        if ((end == HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS)
                || (hasDuration && (event.getEndTime().getTime()
                        - event.getStartTime().getTime() == end - start))) {
            updateEndTimeBoundaries = true;
        } else if ((end < endBoundaries.lowerEndpoint())
                || (end > endBoundaries.upperEndpoint())) {
            return false;
        }

        sessionManager.startBatchedChanges();

        /*
         * If the end time boundaries need shifting, do so now.
         */
        if (updateEndTimeBoundaries) {
            updateEndTimeBoundariesForSingleEvent(getViewForSessionEvent(event),
                    start, end);
        }

        /*
         * Set the new time range for the event.
         */
        event.setTimeRange(startTime, endTime, originator);

        sessionManager.finishBatchedChanges();

        return true;
    }

    /**
     * Set the specified event's geometry. It is assumed that the specified
     * geometry is valid, that is, that {@link Geometry#isValid()} would return
     * <code>true</code>.
     * 
     * @param event
     *            Event to be modified.
     * @param geometry
     *            New geometry.
     * @param originator
     *            Originator of this change.
     * @return True if the new geometry is now in use, false if it was rejected.
     */
    private boolean setEventGeometry(ObservedHazardEvent event,
            IAdvancedGeometry geometry, IOriginator originator) {

        /*
         * If the geometry is unchanged, do nothing.
         */
        if (event.getGeometry().equals(geometry)) {
            return true;
        }

        /*
         * If the geometry change is valid and the user confirms (if necessary),
         * change the geometry and update the hazard areas. Otherwise, reject
         * the change.
         */
        if (isValidGeometryChange(geometry, event, true, originator)
                && ((originator.isDirectResultOfUserInput() == false)
                        || (originator instanceof RevertOriginator)
                        || userConfirmationAsNecessary(event))) {
            sessionManager.startBatchedChanges();
            makeHighResolutionVisible(event, originator);
            event.setGeometry(geometry, originator);
            updateHazardAreas(getViewForSessionEvent(event));
            sessionManager.finishBatchedChanges();
            return true;
        }

        return false;
    }

    private boolean isEventNotLockedByOther(IHazardEventView eventView) {
        return (sessionManager.getLockManager().getHazardEventLockStatus(
                eventView.getEventID()) != LockStatus.LOCKED_BY_OTHER);
    }

    @Override
    public boolean isUndoable(IHazardEventView event) {
        ObservedHazardEvent sessionEvent = getSessionEventForView(event);
        if (sessionEvent == null) {
            return false;
        }
        if (isEventNotLockedByOther(event)) {
            return sessionEvent.isUndoable();
        }
        return false;
    }

    @Override
    public boolean isRedoable(IHazardEventView event) {
        ObservedHazardEvent sessionEvent = getSessionEventForView(event);
        if (sessionEvent == null) {
            return false;
        }
        if (isEventNotLockedByOther(event)) {
            return sessionEvent.isRedoable();
        }
        return false;
    }

    @Override
    public EventPropertyChangeResult undo(IHazardEventView event) {
        ObservedHazardEvent sessionEvent = getSessionEventForView(event);
        if (sessionEvent == null) {
            return EventPropertyChangeResult.FAILURE_DUE_TO_EVENT_NOT_FOUND;
        }
        if (isEventInDatabase(event) && sessionManager.getLockManager()
                .lockHazardEvent(event.getEventID()) == false) {
            showWarningMessageAboutLockedEvents("Cannot Undo", "undone",
                    USER_ORIGINATOR, event.getEventID());
            return EventPropertyChangeResult.FAILURE_DUE_TO_LOCK_STATUS;
        }
        return (sessionEvent.undo() ? EventPropertyChangeResult.SUCCESS
                : EventPropertyChangeResult.FAILURE_DUE_TO_BAD_VALUE);
    }

    @Override
    public EventPropertyChangeResult redo(IHazardEventView event) {
        ObservedHazardEvent sessionEvent = getSessionEventForView(event);
        if (sessionEvent == null) {
            return EventPropertyChangeResult.FAILURE_DUE_TO_EVENT_NOT_FOUND;
        }
        if (isEventInDatabase(event) && (sessionManager.getLockManager()
                .lockHazardEvent(event.getEventID()) == false)) {
            showWarningMessageAboutLockedEvents("Cannot Redo", "redone",
                    USER_ORIGINATOR, event.getEventID());
            return EventPropertyChangeResult.FAILURE_DUE_TO_LOCK_STATUS;
        }
        return (sessionEvent.redo() ? EventPropertyChangeResult.SUCCESS
                : EventPropertyChangeResult.FAILURE_DUE_TO_BAD_VALUE);
    }

    /**
     * Remove the add/remove shapes context menu option from the attributes of
     * the specified event, if said menu option is found there.
     * 
     * @param event
     *            Event from which to remove the option.
     */
    private void removeAddRemoveShapesContextMenuOption(IHazardEvent event) {
        Serializable attribute = event.getHazardAttribute(
                HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY);
        if ((attribute != null) && attribute instanceof List<?>) {
            List<?> contextMenuList = (List<?>) attribute;
            contextMenuList
                    .remove(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES);
        }
    }

    /**
     * Convert the specified hazard event's end time to a duration.
     * 
     * @param event
     *            Event to have its end time converted to a duration.
     */
    private void convertEndTimeToDuration(ObservedHazardEvent event) {

        /*
         * Do nothing unless this event has duration choices.
         */
        if (durationChoicesForEventIdentifiers.containsKey(event.getEventID())
                && (durationChoicesForEventIdentifiers.get(event.getEventID())
                        .isEmpty() == false)) {

            /*
             * Get the time deltas corresponding to the available duration
             * choices.
             */
            Map<String, Long> deltasForDurations = null;
            long startTime = event.getStartTime().getTime();
            long endTime = event.getEndTime().getTime();
            try {
                deltasForDurations = durationChoiceValidator
                        .convertToAvailableMapForProperty(
                                durationChoicesForEventIdentifiers
                                        .get(event.getEventID()));
            } catch (MegawidgetPropertyException e) {
                statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
                        e);

                /*
                 * If a problem occurred, use the default duration to avoid
                 * leaving the hazard event in an invalid state.
                 */
                event.setTimeRange(new Date(startTime), new Date(
                        startTime + configManager.getDefaultDuration(event)));
                return;
            }

            /*
             * Iterate through the duration choices, looking for the choice for
             * which its time delta summed with the start time is the smallest
             * distance from the existing end time of all the choices, and make
             * a record of that time delta.
             */
            long minDifference = Long.MAX_VALUE;
            long minDifferenceTimeDelta = 0L;
            for (Long timeDelta : deltasForDurations.values()) {
                long thisEndTime = startTime + timeDelta;
                long difference = Math.abs(thisEndTime - endTime);
                if (difference < minDifference) {
                    minDifference = difference;
                    minDifferenceTimeDelta = timeDelta;
                } else {
                    break;
                }
            }

            /*
             * Set the end time to match the duration choice that yields an end
             * time closest to the original.
             */
            event.setTimeRange(new Date(startTime),
                    new Date(startTime + minDifferenceTimeDelta));
        }
    }

    /**
     * Handle a change to the settings.
     * 
     * @param notification
     */
    private void settingsModified(SettingsModified notification) {
        if (notification.getChanged().contains(
                ObservedSettings.Type.EVENT_IDENTIFIER_DISPLAY_TYPE)) {
            reloadHazardServicesEventId();
        }
        if (notification.getChanged().contains(ObservedSettings.Type.FILTERS)) {
            loadEventsForSettings(notification.getSettings());
        }
    }

    /**
     * Update Hazard Event Id Display Type on Settings change.
     * 
     * This method should be run before refreshing Console and Spatial displays.
     * 
     */
    private void reloadHazardServicesEventId() {
        ISettings currentSettings = sessionManager.getConfigurationManager()
                .getSettings();
        String eventIdDisplayTypeString = sessionManager
                .getConfigurationManager()
                .getSettingsValue(EVENT_ID_DISPLAY_TYPE, currentSettings);

        if ((eventIdDisplayTypeString != null)
                && (eventIdDisplayTypeString.isEmpty() == false)) {
            HazardServicesEventIdUtil
                    .setIdDisplayType(HazardServicesEventIdUtil.IdDisplayType
                            .valueOf(eventIdDisplayTypeString));
        }
    }

    /**
     * Respond to the addition of hazard events.
     * 
     * @param change
     *            Change that occurred.
     */
    private void sessionEventsAdded(SessionEventsAdded change) {
        sessionManager.startBatchedChanges();
        for (IHazardEventView event : change.getEvents()) {
            ensureEventEndTimeUntilFurtherNoticeAppropriate(event, true);
            updateTimeBoundariesForEvents(event, false);
            updateDurationChoicesForEvent(event, false);
            updateConflictingEventsForSelectedEventIdentifiers(event, false);
        }
        sessionManager.finishBatchedChanges();
    }

    /**
     * Respond to the removal of hazard events.
     * 
     * @param change
     *            Change that occurred.
     */
    private void sessionEventsRemoved(SessionEventsRemoved change) {
        sessionManager.startBatchedChanges();
        for (IHazardEventView event : change.getEvents()) {
            timeResolutionsForEventIdentifiers.remove(event.getEventID());
            updateSavedTimesForEventIfIssued(event, true);
            updateTimeBoundariesForEvents(event, true);
            updateDurationChoicesForEvent(event, true);
            updateConflictingEventsForSelectedEventIdentifiers(event, true);
        }
        sessionManager.finishBatchedChanges();
    }

    /**
     * Respond to an event's type change.
     * 
     * @param eventView
     *            View of the event that experienced the change.
     */
    private void sessionEventTypeModified(final IHazardEventView eventView) {
        updateConflictingEventsForSelectedEventIdentifiers(eventView, false);

        /*
         * Event metadata must be updated asynchronously because the event type
         * modification needs to set the time range before fetching metadata.
         */
        sessionManager.getRunnableAsynchronousScheduler()
                .schedule(new Runnable() {
                    @Override
                    public void run() {
                        updateEventMetadata(eventView);
                    }
                });
    }

    /**
     * Respond to an event's status change.
     * 
     * @param eventView
     *            Event that experienced the change.
     * @param originator
     *            Originator of the change.
     */
    private void sessionEventStatusModified(final IHazardEventView eventView,
            final IOriginator originator) {
        if ((eventView.getStatus() == HazardStatus.ELAPSED)
                || (eventView.getStatus() == HazardStatus.ENDING)
                || (eventView.getStatus() == HazardStatus.ENDED)) {
            if (eventView.getStatus() == HazardStatus.ENDING) {
                eventIdentifiersWithEndingStatus.add(eventView.getEventID());
            } else {
                eventIdentifiersWithEndingStatus.remove(eventView.getEventID());
            }
            updateTimeBoundariesForEvents(eventView, false);
            updateDurationChoicesForEvent(eventView, false);
        } else if ((eventView.getStatus() == HazardStatus.ISSUED)
                && eventIdentifiersWithEndingStatus
                        .contains(eventView.getEventID())) {
            eventIdentifiersWithEndingStatus.remove(eventView.getEventID());
            updateTimeBoundariesForEvents(eventView, false);
            updateDurationChoicesForEvent(eventView, false);
        }

        /*
         * Event metadata must be updated asynchronously in order to avoid
         * problems with the new status not being fully digested before metadata
         * is fetched.
         */
        sessionManager.getRunnableAsynchronousScheduler()
                .schedule(new Runnable() {
                    @Override
                    public void run() {
                        updateEventMetadata(eventView);
                        if ((originator != Originator.DATABASE) && ((eventView
                                .getStatus() == HazardStatus.ELAPSED)
                                || (eventView.getStatus() == HazardStatus.ENDED)
                                || (eventView
                                        .getStatus() == HazardStatus.ISSUED)
                                || (eventView
                                        .getStatus() == HazardStatus.PROPOSED))) {
                            if ((sessionManager.getLockManager()
                                    .getHazardEventLockStatus(eventView
                                            .getEventID()) == LockStatus.LOCKED_BY_ME)
                                    && (sessionManager.getLockManager()
                                            .unlockHazardEvent(eventView
                                                    .getEventID()) == false)
                                    && originator.isDirectResultOfUserInput()) {
                                messenger.getWarner().warnUser("Cannot Unlock",
                                        "Cannot unlock event "
                                                + eventView.getEventID()
                                                + " following metatdata update.");
                            }
                        }
                    }
                });
    }

    /**
     * Respond to the completion of product generation.
     * 
     * @param productGenerationComplete
     *            Notification that is being received.
     */
    private void productGenerationComplete(
            ProductGenerationComplete productGenerationComplete) {

        ProductGeneratorTable pgTable = configManager
                .getProductGeneratorTable();
        /*
         * If the product generation resulted in issuance, iterate through the
         * generated products, and for each one, iterate through the hazard
         * events used to generate it, updating their states as necessary.
         */
        if (productGenerationComplete.isIssued()) {
            for (GeneratedProductList generatedProductList : productGenerationComplete
                    .getGeneratedProducts()) {
                ProductGeneratorEntry pgEntry = pgTable
                        .get(generatedProductList.getProductInfo());
                if (pgEntry.getChangeHazardStatus()) {
                    EventSet<IEvent> eventSet = generatedProductList
                            .getEventSet();
                    Set<String> newlyDeselectedEventIdentifiers = new HashSet<>(
                            eventSet.size(), 1.0f);
                    for (IEvent event : eventSet) {
                        BaseHazardEvent hazardEvent = (BaseHazardEvent) event;
                        IHazardEventView oEventView = getEventById(
                                hazardEvent.getEventID());
                        ObservedHazardEvent oEvent = getSessionEventForView(
                                oEventView);

                        /*
                         * If the hazard is pending or proposed, make it issued;
                         * otherwise, if it needs a change to the ended status,
                         * do this.
                         */
                        HazardStatus hazardStatus = oEvent.getStatus();
                        boolean wasPreIssued = false;
                        boolean wasReissued = false;
                        if (hazardStatus.equals(HazardStatus.PENDING)
                                || hazardStatus.equals(HazardStatus.PROPOSED)) {
                            oEvent.setIssuanceCount(1);
                            oEvent.setStatus(HazardStatus.ISSUED);
                            wasPreIssued = true;
                        } else if (isChangeToEndedStatusNeeded(hazardEvent)) {
                            oEvent.setStatus(HazardStatus.ENDED);
                        } else if (hazardStatus.equals(HazardStatus.ISSUED)) {
                            wasReissued = true;
                            oEvent.setIssuanceCount(
                                    oEvent.getIssuanceCount() + 1);
                        }

                        /*
                         * TODO: Allow product generators to specify whether the
                         * (re)issued events are to be selected or not. For now,
                         * just select or deselect them based upon the current
                         * setting (Redmine issue #18515).
                         */
                        if ((wasPreIssued || wasReissued) && Boolean.TRUE
                                .equals(configManager.getSettings()
                                        .getDeselectAfterIssuing())) {
                            newlyDeselectedEventIdentifiers
                                    .add(oEvent.getEventID());
                        }

                        /*
                         * If the hazard now has just changed to issued status
                         * (i.e. it has not just been changed to ended, nor has
                         * it been reissued), save its end time as it is now in
                         * case it needs restoration later, and adjust its start
                         * and end times and their boundaries. Then update its
                         * duration choices list, if applicable. Note that
                         * reissued hazard events must not do any of this
                         * because their time range boundaries and their
                         * duration choices should not change. If they were to
                         * change, then the VTEC engine would generate EXTs
                         * instead of CONs since the end time of the hazard
                         * would change. However, reissued events do need to be
                         * stored to the database, since only events that have
                         * changed status normally get stored after generation.
                         */
                        if (wasPreIssued) {
                            updateSavedTimesForEventIfIssued(oEventView, false);

                            /*
                             * TODO Uncomment this as part of Redmine issue
                             * #18386. (Tracy: For some reason the Hazard Event
                             * for PHI does not have it's issue time set at this
                             * juncture, and results in an exception.)
                             */
                            // updateTimeRangeBoundariesOfJustIssuedEvent(
                            // oEventView,
                            // (Long) hazardEvent
                            // .getHazardAttribute(HazardConstants.ISSUE_TIME));
                            // updateDurationChoicesForEvent(oEvent, false);
                        } else if (wasReissued) {
                            persistEvent(oEvent);
                            if ((sessionManager.getLockManager()
                                    .getHazardEventLockStatus(
                                            oEvent.getEventID()) != LockStatus.LOCKED_BY_ME)
                                    || (sessionManager.getLockManager()
                                            .unlockHazardEvent(oEvent
                                                    .getEventID()) == false)) {
                                messenger.getWarner().warnUser("Cannot Unlock",
                                        "Cannot unlock event "
                                                + oEvent.getEventID()
                                                + " following reissuance.");
                            }
                        }
                    }

                    /*
                     * If there are any events to be deselected, do so.
                     */
                    if (newlyDeselectedEventIdentifiers.isEmpty() == false) {
                        sessionManager.getSelectionManager()
                                .removeEventsFromSelectedEvents(
                                        newlyDeselectedEventIdentifiers,
                                        Originator.OTHER);
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
     */
    private boolean isChangeToEndedStatusNeeded(IReadableHazardEvent event) {
        return event.getStatus().equals(HazardStatus.ENDING)
                || event.getHazardAttribute(REPLACED_BY) != null;
    }

    /**
     * Respond to a CAVE current time change by updating all the events' start
     * and end time editability boundaries.
     * 
     * @param change
     *            Change that occurred.
     */
    private void currentTimeChanged(CurrentTimeChanged change) {
        updateTimeBoundariesForEvents(null, false);
    }

    @Override
    public MegawidgetSpecifierManager getMegawidgetSpecifiers(
            IHazardEventView event) {

        /*
         * If the specified event is an observed hazard event, then it is an
         * active, modifiable event being managed by this manager, so its
         * megawidget specifier manager may be found in the cache, and if not,
         * the manager should be fetched and allowed to potentially modify the
         * event. Otherwise, the event is not an active event; it should be
         * treated as an uneditable historical event snapshot, and thus the
         * megawidget specifier manager should simply be fetched and returned,
         * not cached and not given a chance to modify the hazard event.
         */
        if (getSessionEventForView(event) != null) {
            MegawidgetSpecifierManager manager = megawidgetSpecifiersForEventIdentifiers
                    .get(event.getEventID());
            if (manager != null) {
                return manager;
            }
            updateEventMetadata(event);
            return megawidgetSpecifiersForEventIdentifiers
                    .get(event.getEventID());
        } else {
            return configManager
                    .getMetadataForHazardEvent(new BaseHazardEvent(event))
                    .getMegawidgetSpecifierManager();
        }
    }

    /**
     * Get the session event for which the specified object acts as a view.
     * 
     * @param eventView
     *            View of the event.
     * @return Session event with which the view is associated, or
     *         <code>null</code> if there is no associated event.
     */
    private ObservedHazardEvent getSessionEventForView(
            IHazardEventView eventView) {
        return viewsForHazardEvents.inverse().get(eventView);
    }

    /**
     * Get the view of the specified session event.
     * 
     * @param event
     *            Session event.
     * @return View of the session event, or <code>null</code> if there is no
     *         associated view.
     */
    IHazardEventView getViewForSessionEvent(ObservedHazardEvent event) {
        return viewsForHazardEvents.get(event);
    }

    @Override
    public List<String> getDurationChoices(IHazardEventView event) {

        /*
         * Get the duration choices for the hazard event that are cached. If
         * nothing is found that way, and the hazard event is not an editable
         * one, get the list of possible duration choices for the hazard event's
         * type, and prune it down to the single choice that matches the event's
         * current duration.
         */
        List<String> durationChoices = durationChoicesForEventIdentifiers
                .get(event.getEventID());
        if ((durationChoices == null)
                && (getSessionEventForView(event) == null)) {
            durationChoices = configManager.getDurationChoices(event);
            if (durationChoices.isEmpty() == false) {

                /*
                 * Get a map of the choice strings to their associated time
                 * deltas in milliseconds. The map will iterate in the order the
                 * choices are specified in the list used to generate it.
                 */
                Map<String, Long> deltasForDurations = getDeltasForDurationChoices(
                        event, durationChoices);
                if (deltasForDurations == null) {
                    return null;
                }

                /*
                 * Iterate through the choices, checking each in turn to see if
                 * its associated delta is the current event duration. If one is
                 * found, use that as the sole choice.
                 */
                long eventDelta = event.getEndTime().getTime()
                        - event.getStartTime().getTime();
                for (Map.Entry<String, Long> entry : deltasForDurations
                        .entrySet()) {
                    if (entry.getValue() == eventDelta) {
                        return Lists.newArrayList(entry.getKey());
                    }
                }
                durationChoices = Collections.emptyList();
            }
        }
        return durationChoices;
    }

    /**
     * Get the map of the specified duration choices to their time deltas in
     * milliseconds.
     * 
     * @param eventView
     *            View of the event for which the deltas are being fetched.
     * @param durationChoices
     *            List of duration choices to be turned into deltas.
     * @return Map of the duration choices to their associated time deltas in
     *         milliseconds, or <code>null</code> if an error occurs.
     */
    private Map<String, Long> getDeltasForDurationChoices(
            IHazardEventView eventView, List<String> durationChoices) {

        /*
         * Get a map of the choice strings to their associated time deltas in
         * milliseconds. The map will iterate in the order the choices are
         * specified in the list used to generate it.
         */
        Map<String, Long> deltasForDurations = null;
        try {
            deltasForDurations = durationChoiceValidator
                    .convertToAvailableMapForProperty(durationChoices);
        } catch (MegawidgetPropertyException e) {
            statusHandler
                    .error("invalid list of duration choices for event of type "
                            + HazardEventUtilities.getHazardType(eventView), e);
            return null;
        }
        return deltasForDurations;
    }

    @Override
    public void eventCommandInvoked(IHazardEventView event, String identifier) {

        /*
         * If the command that was invoked is a metadata refresh trigger, and
         * the event is not already undergoing a metadata refresh, perform the
         * refresh; otherwise, if the command is meant to trigger the editing of
         * rise-crest-fall information, start the edit.
         */
        String eventId = event.getEventID();
        if (metadataReloadTriggeringIdentifiersForEventIdentifiers
                .containsKey(eventId)
                && (eventViewsUndergoingMetadataFetch.contains(event) == false)
                && metadataReloadTriggeringIdentifiersForEventIdentifiers
                        .get(eventId).contains(identifier)) {
            updateEventMetadata(event);
            return;
        } else if (editRiseCrestFallTriggeringIdentifiersForEventIdentifiers
                .containsKey(event.getEventID())
                && editRiseCrestFallTriggeringIdentifiersForEventIdentifiers
                        .get(event.getEventID()).contains(identifier)) {
            startRiseCrestFallEdit(event);
        }
    }

    @Override
    public Map<String, String> getRecommendersForTriggerIdentifiers(
            String identifier) {
        Map<String, String> map = recommendersForTriggerIdentifiersForEventIdentifiers
                .get(identifier);
        return (map == null ? Collections.<String, String> emptyMap() : map);
    }

    /**
     * Update the specified event's metadata in response to some sort of change
     * (creation of the event, updating of status or hazard type) that may
     * result in the available metadata changing.
     * 
     * @param eventView
     *            View of the event for which metadata may need updating.
     */
    private void updateEventMetadata(IHazardEventView eventView) {

        /*
         * Get a new megawidget specifier manager for this event, and store it
         * in the cache.
         */
        HazardEventMetadata metadata = configManager
                .getMetadataForHazardEvent(new BaseHazardEvent(eventView));
        MegawidgetSpecifierManager manager = metadata
                .getMegawidgetSpecifierManager();

        assert (manager != null);
        assert (eventView.getStartTime() != null);
        assert (eventView.getEndTime() != null);

        megawidgetSpecifiersForEventIdentifiers.put(eventView.getEventID(),
                manager);
        metadataReloadTriggeringIdentifiersForEventIdentifiers.put(
                eventView.getEventID(),
                metadata.getRefreshTriggeringMetadataKeys());
        metadataIdentifiersAffectingModifyFlagsForEventIdentifiers.put(
                eventView.getEventID(),
                metadata.getAffectingModifyFlagMetadataKeys());
        recommendersForTriggerIdentifiersForEventIdentifiers.put(
                eventView.getEventID(),
                metadata.getRecommendersTriggeredForMetadataKeys());
        editRiseCrestFallTriggeringIdentifiersForEventIdentifiers.put(
                eventView.getEventID(),
                metadata.getEditRiseCrestFallTriggeringMetadataKeys());

        sessionManager.startBatchedChanges();

        /*
         * Make a note of this event undergoing a metadata fetch, so that any
         * changes to the event that usually result in a metadata reload do not
         * do so.
         */
        eventViewsUndergoingMetadataFetch.add(eventView);

        /*
         * Fire off a notification that the metadata may have changed for this
         * event if this event is currently one of the session events (it may
         * not be if it has not yet been added).
         */
        if (allEventViews.contains(eventView)) {
            notificationSender.postNotificationAsync(
                    new SessionEventModified(this, eventView,
                            new EventMetadataModification(), Originator.OTHER));
        }

        /*
         * If the event was modified by the metadata refresh process, merge the
         * changes into the canonical session event.
         */
        IHazardEvent modifiedHazardEvent = metadata.getModifiedHazardEvent();
        if (modifiedHazardEvent != null) {
            mergeHazardEvents(modifiedHazardEvent, eventView, false, false,
                    false, Originator.OTHER);
        }

        /*
         * Get a copy of the current attributes of the hazard event, so that
         * they may be modified as required to work with the new metadata
         * specifiers. Remove any entries for attributes that are marked as
         * always taking the default values specified in the metadata
         * definitions instead of old values; this will ensure that the next
         * step uses the default values for those attributes. Then add any
         * missing specifiers' starting states (and correct those that are not
         * valid for these specifiers), and assign the modified attributes back
         * to the event.
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
        boolean eventModified = eventView.isModified();
        Map<String, Serializable> attributes = eventView.getHazardAttributes();
        Map<String, Object> newAttributes = new HashMap<String, Object>(
                attributes);
        newAttributes.keySet()
                .removeAll(metadata.getOverrideOldValuesMetadataKeys());
        populateTimeAttributesStartingStates(manager.getSpecifiers(),
                newAttributes, eventView.getStartTime().getTime(),
                eventView.getEndTime().getTime());
        manager.populateWithStartingStates(newAttributes);
        attributes = new HashMap<>(newAttributes.size());
        for (String name : newAttributes.keySet()) {
            attributes.put(name, (Serializable) newAttributes.get(name));
        }

        ObservedHazardEvent event = getSessionEventForView(eventView);
        event.setHazardAttributes(attributes);

        event.setModified(eventModified);

        /*
         * Remove this event from the set of events undergoing a metadata fetch.
         */
        eventViewsUndergoingMetadataFetch.remove(eventView);

        sessionManager.finishBatchedChanges();
    }

    /**
     * Start the edit of rise-crest-fall information for the specified event.
     * 
     * @param eventView
     *            View of the event for which to edit the rise-crest-fall
     *            information.
     */
    private void startRiseCrestFallEdit(final IHazardEventView eventView) {
        IEventApplier applier = new IEventApplier() {
            @Override
            public void apply(IHazardEventView eventView) {
                updateEventMetadata(eventView);
                updateTimeBoundariesForEvents(eventView, false);
            }
        };
        IRiseCrestFallEditor editor = messenger
                .getRiseCrestFallEditor(eventView);
        IHazardEventView evt = editor.getRiseCrestFallEditor(eventView,
                applier);
        if (evt != null) {
            updateEventMetadata(eventView);
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
                    if ((valueObj == null) || ((lastValue != -1L)
                            && (lastValue >= valueObj.longValue()))) {
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
                    long defaultValue = (identifiers.size() == 1
                            ? (minimumTime + maximumTime) / 2L : minimumTime);
                    for (int j = 0; j < identifiers
                            .size(); j++, defaultValue += interval) {
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
     * Respond to an event's attributes being changed.
     * 
     * @param eventView
     *            View of the event that experienced the change.
     * @param modification
     *            Modification that occurred.
     * @param originator
     *            Originator of the change.
     */
    private void sessionEventAttributesModified(
            final IHazardEventView eventView,
            EventAttributesModification modification, IOriginator originator) {

        /*
         * If the end time "until further notice" flag has changed value but was
         * not removed, change the end time in a corresponding manner.
         */
        if (modification.containsAttribute(
                HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)
                && eventView.getHazardAttributes().containsKey(
                        HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)) {
            setEventEndTimeForUntilFurtherNotice(eventView,
                    Boolean.TRUE.equals(eventView.getHazardAttribute(
                            HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)));
        }

        /*
         * If any of the attributes changed are metadata-reload triggers, and
         * the event is not already undergoing a metadata refresh, see if
         * metadata needs to be reloaded; otherwise, if any of them are to
         * trigger the editing of rise-crest-fall information, reload that.
         */
        Set<String> metadataReloadTriggeringIdentifiers = metadataReloadTriggeringIdentifiersForEventIdentifiers
                .get(eventView.getEventID());
        Set<String> editRiseCrestFallTriggeringIdentifiers = editRiseCrestFallTriggeringIdentifiersForEventIdentifiers
                .get(eventView.getEventID());
        if ((metadataReloadTriggeringIdentifiers != null)
                && (eventViewsUndergoingMetadataFetch
                        .contains(eventView) == false)) {

            /*
             * Get the subset of trigger identifiers that changed, and then
             * iterate through them. If at least one is found that had a
             * non-null value before the change, reload the metadata. Otherwise,
             * do no metadata reloading. This avoids having reloads triggered
             * when a hazard event is given a new type and the attributes are
             * initialized, in which case the attributes will go from null to
             * non-null.
             */
            Set<String> triggers = Sets.intersection(
                    metadataReloadTriggeringIdentifiers,
                    modification.getAttributeKeys());
            for (String trigger : triggers) {
                if (modification.getOldAttribute(trigger) != null) {

                    /*
                     * Event metadata must be updated asynchronously in order to
                     * avoid problems with the new attribute values not being
                     * fully digested before metadata is fetched.
                     */
                    sessionManager.getRunnableAsynchronousScheduler()
                            .schedule(new Runnable() {
                                @Override
                                public void run() {
                                    updateEventMetadata(eventView);
                                }
                            });
                    break;
                }
            }
        }
        if ((editRiseCrestFallTriggeringIdentifiers != null)
                && (Sets.intersection(editRiseCrestFallTriggeringIdentifiers,
                        modification.getAttributeKeys()).isEmpty() == false)) {
            startRiseCrestFallEdit(eventView);
        }
    }

    /**
     * Respond to an event's time range being changed.
     * 
     * @param eventView
     *            View of the event that experienced the change.
     * @param originator
     *            Originator of the change.
     */
    private void sessionEventTimeRangeModified(IHazardEventView eventView,
            IOriginator originator) {
        updateConflictingEventsForSelectedEventIdentifiers(eventView, false);
    }

    /**
     * Respond to an event's geometry being changed.
     * 
     * @param eventView
     *            View of the event that experienced the change.
     * @param originator
     *            Originator of the change.
     */
    private void sessionEventGeometryModified(IHazardEventView eventView,
            IOriginator originator) {
        updateConflictingEventsForSelectedEventIdentifiers(eventView, false);
    }

    /**
     * Respond to one or more events' lock statuses being changed.
     * 
     * @param eventIdentifiers
     *            Identifiers of the events that experienced the change.
     * @param originator
     *            Originator of the change.
     */
    private void sessionEventsLockStatusModified(Set<String> eventIdentifiers,
            IOriginator originator) {
        for (String eventIdentifier : eventIdentifiers) {
            IHazardEventView eventView = getEventById(eventIdentifier);
            if (eventView != null) {
                updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(
                        eventView, false);
            }
        }
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

    @Override
    public Map<String, TimeResolution> getTimeResolutionsForEventIds() {
        return Collections.unmodifiableMap(timeResolutionsForEventIdentifiers);
    }

    /**
     * Set the end time for the specified event with respect to the specified
     * value for "until further notice".
     * 
     * @param eventView
     *            View of the event to have its end time set.
     * @param untilFurtherNotice
     *            Flag indicating whether or not the end time should be
     *            "until further notice".
     */
    private void setEventEndTimeForUntilFurtherNotice(
            IHazardEventView eventView, boolean untilFurtherNotice) {

        /*
         * If "until further notice" has been toggled on for the end time, save
         * the current end time for later (in case it is toggled off again), and
         * change the end time to the "until further notice" value; otherwise,
         * change the end time to be the same interval distant from the start
         * time as it was before "until further notice" was toggled on. If no
         * interval is found to have been saved, perhaps due to a metadata
         * change or a type change, just use the default duration for the event.
         */
        ObservedHazardEvent event = getSessionEventForView(eventView);
        if (untilFurtherNotice) {
            long interval = eventView.getEndTime().getTime()
                    - eventView.getStartTime().getTime();
            event.addHazardAttribute(
                    HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE,
                    interval);
            updateEndTimeBoundariesForSingleEvent(eventView,
                    eventView.getStartTime().getTime(),
                    HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS);
            event.setEndTime(new Date(
                    HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS));
        } else if ((eventView.getEndTime() != null) && (eventView.getEndTime()
                .getTime() == HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS)) {
            Long interval = (Long) eventView.getHazardAttribute(
                    HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE);
            event.removeHazardAttribute(
                    HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE);
            if (interval == null) {
                interval = configManager.getDefaultDuration(eventView);
            }
            long startTime = eventView.getStartTime().getTime();
            updateEndTimeBoundariesForSingleEvent(eventView, startTime,
                    startTime + interval);
            event.setEndTime(new Date(startTime + interval));
        }
    }

    /**
     * Update the set of identifiers of events allowing the toggling of
     * "until further notice" mode. This is to be called whenever one or more
     * events have been added, removed, or had their hazard types changed.
     * 
     * @param eventView
     *            View of the event that has been added, removed, or modified.
     * @param removed
     *            Flag indicating whether or not the change is the removal of
     *            the event.
     */
    private void updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(
            IHazardEventView eventView, boolean removed) {

        /*
         * Assume the event should be removed from the set unless it is not
         * being removed from the session, it is not locked by someone else, and
         * it has a hazard type that allows "until further notice".
         */
        boolean allowsUntilFurtherNotice = false;
        if (removed == false) {
            HazardTypeEntry hazardType = configManager.getHazardTypes()
                    .get(HazardEventUtilities.getHazardType(eventView));
            if (isEventNotLockedByOther(eventView) && (hazardType != null)
                    && hazardType.isAllowUntilFurtherNotice()) {
                allowsUntilFurtherNotice = true;
            }
        }

        /*
         * Make the change required; if this actually results in a change to the
         * set, fire off a notification.
         */
        boolean changed;
        if (allowsUntilFurtherNotice) {
            changed = identifiersOfEventsAllowingUntilFurtherNotice
                    .add(eventView.getEventID());
        } else {
            changed = identifiersOfEventsAllowingUntilFurtherNotice
                    .remove(eventView.getEventID());
        }
        if (changed && allEventViews.contains(eventView)) {
            notificationSender.postNotificationAsync(
                    new SessionEventModified(this, eventView,
                            new EventAllowUntilFurtherNoticeModification(),
                            Originator.OTHER));
        }
    }

    /**
     * Ensure that the end time "until further notice" mode, if present in the
     * specified event, is appropriate; if it is not, remove it.
     * 
     * @param eventView
     *            View of the event to be checked.
     */
    private void ensureEventEndTimeUntilFurtherNoticeAppropriate(
            IHazardEventView eventView, boolean logErrors) {

        /*
         * If this event cannot have "until further notice", ensure it is not
         * one of its attributes.
         */
        if (identifiersOfEventsAllowingUntilFurtherNotice
                .contains(eventView.getEventID()) == false) {

            /*
             * If the attributes contains the flag, remove it. If it was set to
             * true, then reset the end time to an appropriate non-"until
             * further notice" value.
             */
            Boolean untilFurtherNotice = (Boolean) eventView.getHazardAttribute(
                    HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
            if (untilFurtherNotice != null) {
                ObservedHazardEvent event = getSessionEventForView(eventView);
                event.removeHazardAttribute(
                        HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
                if (untilFurtherNotice.equals(Boolean.TRUE)) {
                    setEventEndTimeForUntilFurtherNotice(eventView, false);
                    if (logErrors) {
                        statusHandler.error("event " + eventView.getEventID()
                                + " found to have \"until further notice\" set, "
                                + "which is illegal for events of type "
                                + eventView.getHazardType());
                    }
                }
            }
        }
    }

    private void filterEventsForConfig(
            Collection<? extends IHazardEventView> eventViews) {
        ISettings settings = configManager.getSettings();
        Set<String> siteIDs = configManager
                .getSettingsValue(SETTING_HAZARD_SITES, settings);
        Set<String> phenSigs = settings.getVisibleTypes();
        Set<HazardStatus> statuses = EnumSet.noneOf(HazardStatus.class);
        for (String state : settings.getVisibleStatuses()) {
            statuses.add(HazardStatus.valueOf(state.toUpperCase()));
        }

        /*
         * TODO: If ever undoing changes from issue #17328 (making pending
         * status not available for filtering out), delete the line below that
         * adds the pending status to the set of statuses.
         */
        statuses.add(HazardStatus.PENDING);
        Iterator<? extends IHazardEventView> iterator = eventViews.iterator();
        while (iterator.hasNext()) {
            IHazardEventView eventView = iterator.next();
            if (!statuses.contains(eventView.getStatus())) {
                iterator.remove();
            } else if (!siteIDs.contains(eventView.getSiteID())) {
                iterator.remove();
            } else {
                String key = HazardEventUtilities.getHazardType(eventView);
                /*
                 * Check for null key ensures we don't filter out events for
                 * which a type has not yet been defined.
                 */
                if (key != null && !phenSigs.contains(key)) {
                    iterator.remove();
                }
            }
        }
    }

    private void loadEventsForSettings(ObservedSettings settings) {

        /*
         * Create the query to be used to get the events for the current
         * settings.
         */
        HazardEventQueryRequest queryRequest = new HazardEventQueryRequest(
                (CAVEMode.OPERATIONAL.equals(CAVEMode.getMode()) == false));
        /*
         * If the settings file has not been overridden for the site, add the
         * currently configured site to the list of visible sites, and include
         * visible sites as part of the query filter.
         */
        Set<String> visibleSites = configManager
                .getSettingsValue(SETTING_HAZARD_SITES, settings);
        if (visibleSites == null) {
            visibleSites = Collections.emptySet();
        }
        String configSiteID = configManager.getSiteID();
        if (visibleSites.contains(configSiteID) == false) {
            visibleSites = new HashSet<>(visibleSites);
            visibleSites.add(configSiteID);
            settings.setVisibleSites(visibleSites);
        }

        /*
         * Include visible types in the query filter.
         */
        Set<String> visibleTypes = settings.getVisibleTypes();
        if (visibleTypes == null || visibleTypes.isEmpty()) {
            return;
        }

        /*
         * Add the filters to the query, and make the request.
         * 
         * Note that the allowable statuses cannot be included in the query,
         * because this would result in hazard events that have a latest version
         * with a currently invisible status to be included in the results if
         * earlier versions of the same event had currently visible statuses.
         * Instead, those with invisible statuses are weeded out in the
         * iteration through the returned events that follows.
         */
        queryRequest.and(HazardConstants.SITE_ID, visibleSites)
                .and(HazardConstants.PHEN_SIG, visibleTypes);
        Map<String, HazardHistoryList> eventsMap = null;
        try {
            eventsMap = dbManager.queryHistory(queryRequest);
        } catch (HazardEventServiceException e) {
            statusHandler.error(
                    "Could not query hazard events with appropriate site IDs and types.",
                    e);
            return;
        }

        sessionManager.startBatchedChanges();

        /*
         * Get the identifiers of the events that were selected before this
         * change.
         */
        Set<String> oldSelectedEventIdentifiers = new HashSet<>(sessionManager
                .getSelectionManager().getSelectedEventIdentifiers());

        /*
         * Determine which statuses events may have in order to be visible.
         * 
         * TODO: If ever undoing changes from issue #17328 (making pending
         * status not available for filtering out), make the check for null also
         * check for an empty set, and if the condition is true, do not do the
         * filtering; no hazard events will be available.
         */
        Set<String> visibleStatuses = settings.getVisibleStatuses();
        if (visibleStatuses == null) {
            visibleStatuses = Collections.emptySet();
        }
        Set<HazardStatus> statuses = EnumSet.noneOf(HazardStatus.class);
        for (String state : visibleStatuses) {
            statuses.add(HazardStatus.valueOf(state.toUpperCase()));
        }

        /*
         * TODO: If ever undoing changes from issue #17328 (making pending
         * status not available for filtering out), delete the line below that
         * adds the pending status to the set of statuses.
         */
        statuses.add(HazardStatus.PENDING);

        /*
         * Iterate through the events, adding any that have visible statuses and
         * that were not part of the event list before. Also track which events
         * that were being managed previously are not included in the events
         * returned from the query.
         */
        Set<IHazardEventView> leftoverEventViews = new HashSet<>(allEventViews);
        Set<String> selectedEventIdentifiers = new HashSet<>();
        for (Entry<String, HazardHistoryList> entry : eventsMap.entrySet()) {

            /*
             * Get the history list for the event, and ensure that the event has
             * an allowable status.
             */
            HazardHistoryList historyList = entry.getValue();
            HazardEvent event = historyList.get(historyList.size() - 1);
            if (statuses.contains(event.getStatus()) == false) {
                continue;
            }

            /*
             * If the event is already in the session events list, do nothing
             * more with it besides adding it to the set of selected events if
             * it was selected before.
             */
            String eventIdentifier = event.getEventID();
            IHazardEventView oldEventView = getEventById(eventIdentifier);
            if (oldEventView != null) {
                leftoverEventViews.remove(oldEventView);
                if (oldSelectedEventIdentifiers.contains(eventIdentifier)) {
                    selectedEventIdentifiers.add(eventIdentifier);
                }
                continue;
            }

            /*
             * If the event has ever been issued, set its has-been-issued flag.
             * Then add the event.
             */
            try {
                for (IHazardEvent historicalEvent : historyList) {
                    if (HazardStatus.issuedButNotEndedOrElapsed(
                            historicalEvent.getStatus())) {
                        event.addHazardAttribute(HazardConstants.ISSUED, true);
                        break;
                    }
                }
                addEvent(event, false, Originator.DATABASE);
            } catch (HazardEventServiceException e) {
                statusHandler.error(
                        "Should never occur since hazard event did not need "
                                + "new identifier: could not add hazard event.",
                        e);
            }

            /*
             * Remember the hazard event's history list's size (subtracting 1 if
             * the last item in the history list is not a historical snapshot).
             * Also associate the event identifier with the non-historical event
             * at the end of the history list for later reference if, again, the
             * last item in the history list is not a historical snapshot.
             */
            if (event.isLatestVersion()) {
                historicalVersionCountsForEventIdentifiers.put(eventIdentifier,
                        historyList.size() - 1);
                latestVersionsFromDatabaseForEventIdentifiers
                        .put(eventIdentifier, event);
            } else {
                historicalVersionCountsForEventIdentifiers.put(eventIdentifier,
                        historyList.size());
            }
            identifiersOfPersistedEvents.add(eventIdentifier);
        }

        /*
         * For the remaining old events (those not included in the ones returned
         * by the query), filter them for the current settings.
         */
        filterEventsForConfig(leftoverEventViews);
        for (IHazardEventView eventView : leftoverEventViews) {
            String eventIdentifier = eventView.getEventID();
            if (oldSelectedEventIdentifiers.contains(eventIdentifier)) {
                selectedEventIdentifiers.add(eventIdentifier);
            }
        }

        /*
         * Schedule expiration tasks for the events.
         */
        for (ObservedHazardEvent event : allEvents) {
            scheduleExpirationTask(event);
        }

        /*
         * Set the new selected events
         */
        sessionManager.getSelectionManager().setSelectedEventIdentifiers(
                selectedEventIdentifiers, Originator.OTHER);

        sessionManager.finishBatchedChanges();
    }

    @Override
    public IHazardEventView addEvent(IReadableHazardEvent event,
            IOriginator originator) throws HazardEventServiceException {
        HazardStatus status = event.getStatus();
        if (((status == null) || (status == HazardStatus.PENDING))
                && (originator != Originator.DATABASE)) {
            return addEvent(event, true, originator);
        } else {
            return addEvent(event, false, originator);
        }
    }

    /**
     * Add the specified hazard event to the session; must be a brand new hazard
     * event with no identifier, or else an event that is from the database, but
     * does not have an identifier equal to any existing events being managed by
     * this session.
     * <p>
     * <strong>NOTE</strong>: This method is called whenever an event is added
     * to the current session, regardless of the source of the event. Additional
     * logic (method calls, etc.) may therefore be added to this method's
     * implementation as necessary if said logic must be run whenever an event
     * is added.
     * </p>
     * 
     * @param event
     *            Event to be added.
     * @param select
     *            Flag indicating whether or not it should be selected.
     * @param originator
     *            Originator of the addition.
     * @return Event that was added or modified.
     * @throws HazardEventServiceException
     *             If a problem occurred while attempting to add the event.
     */
    private IHazardEventView addEvent(IReadableHazardEvent event,
            boolean select, IOriginator originator)
            throws HazardEventServiceException {

        /*
         * Create the new event and ensure that its modify flag does not change
         * its value as a result of any changes made to the event within this
         * method.
         */
        ObservedHazardEvent oevent = new ObservedHazardEvent(event, this);
        oevent.setModifiedNotAllowedToChange(true);
        oevent.setInsertTime(null);

        /*
         * Need to account for the case where the event being added already
         * exists in the event manager. (This can happen with recommender
         * callbacks, for example.) If it has an event identifier, see if it
         * there is already an existing event, and if so, merge it into the
         * existing one. If it does not have a valid identifier, create one.
         * Otherwise, set the event identifier; if it cannot be set, an
         * exception will be thrown by getNewEventID().
         */
        String eventID = oevent.getEventID();
        if ((eventID != null) && (eventID.length() > 0)) {
            IHazardEventView existingEventView = getEventById(eventID);
            if (existingEventView != null) {
                throw new IllegalArgumentException(
                        "attempted to add an event that is "
                                + "already being managed by session; use mergeHazardEvents() instead");
            }
        } else {
            oevent.setEventID(HazardServicesEventIdUtil.getNewEventID());
        }

        /*
         * If modifying the visible sites, make a copy first, as the original is
         * from the configuration manager.
         * 
         * TODO: Better defensive copying elsewhere!
         */
        ObservedSettings settings = configManager.getSettings();
        Set<String> visibleSites = configManager.getSettingsValue(
                SETTING_HAZARD_SITES, configManager.getSettings());
        if (visibleSites.contains(configManager.getSiteID()) == false) {
            visibleSites = new HashSet<>(visibleSites);
            visibleSites.add(configManager.getSiteID());
            configManager.getSettings().setVisibleSites(visibleSites,
                    Originator.OTHER);
        }
        if (configManager.getHazardCategory(oevent) == null
                && oevent.getHazardAttribute(
                        HazardConstants.HAZARD_EVENT_CATEGORY) == null) {
            oevent.addHazardAttribute(HazardConstants.HAZARD_EVENT_CATEGORY,
                    settings.getDefaultCategory(), false, originator);
        }

        /*
         * Set the event start and end times if they are not already set. If
         * they are set, ensure they are at minute boundaries if this event uses
         * minute-level time resolution, excepting the end time if it is "until
         * further notice".
         */
        TimeResolution eventTimeResolution = configManager
                .getTimeResolution(oevent);
        timeResolutionsForEventIdentifiers.put(oevent.getEventID(),
                eventTimeResolution);
        if (oevent.getStartTime() == null) {
            Date timeToUse = roundDateDownToNearestMinuteIfAppropriate(
                    timeManager.getCurrentTime(), eventTimeResolution);
            Date selectedTime = roundDateDownToNearestMinuteIfAppropriate(
                    new Date(timeManager.getSelectedTime().getLowerBound()),
                    eventTimeResolution);
            if (selectedTime.after(timeManager.getCurrentTime())) {
                timeToUse = selectedTime;
            }
            oevent.setStartTime(timeToUse, false, originator);
        } else {
            oevent.setStartTime(roundDateDownToNearestMinuteIfAppropriate(
                    oevent.getStartTime(), eventTimeResolution));
        }
        if (oevent.getEndTime() == null) {
            long s = oevent.getStartTime().getTime();
            long d = configManager.getDefaultDuration(oevent);
            oevent.setEndTime(new Date(s + d), false, originator);
        } else {
            oevent.setEndTime(oevent.getEndTime()
                    .getTime() != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS
                            ? roundDateDownToNearestMinuteIfAppropriate(
                                    oevent.getEndTime(), eventTimeResolution)
                            : oevent.getEndTime());
        }

        /*
         * Set the status to pending if none is found. Make it ended or elapsed
         * if appropriate.
         */
        if (oevent.getStatus() == null) {
            oevent.setStatus(HazardStatus.PENDING, false, false, originator);
        }
        if (isEnded(oevent)) {
            oevent.setStatus(HazardStatus.ENDED, false, originator);
        }
        if (isElapsed(oevent)) {
            oevent.setStatus(HazardStatus.ELAPSED, false, originator);
        }

        /*
         * Set the site identifier to the current one if this is locally
         * generated.
         */
        if (originator != Originator.DATABASE) {
            oevent.setSiteID(configManager.getSiteID(), false, originator);
        }

        /*
         * Set the hazard mode as appropriate.
         */
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
        oevent.setHazardMode(productClass, false, originator);

        /*
         * Add the event to the set of checked events.
         */
        checkedEventIdentifiers.add(oevent.getEventID());

        /*
         * Determine whether this event allows until-further-notice and record
         * this fact if it does, and record any saved times for the event if it
         * has been issued.
         */
        HazardEventView eventView = new HazardEventView(oevent);
        updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(eventView,
                false);
        updateSavedTimesForEventIfIssued(eventView, false);

        /*
         * Add the event to the collection of managed events.
         */
        allEvents.add(oevent);
        allEventViews.add(eventView);

        /*
         * Create a view for the new hazard event, and associate the two.
         */
        viewsForHazardEvents.put(oevent, eventView);

        /*
         * Notify listeners that the event has been added.
         */
        notificationSender.postNotificationAsync(
                new SessionEventsAdded(this, eventView, originator));

        /*
         * Set the issued flag as appropriate.
         */
        oevent.addHazardAttribute(HazardConstants.ISSUED,
                HazardStatus.issuedButNotEndedOrElapsed(oevent.getStatus()),
                false, originator);

        /*
         * If the event is to be selected, either select only it, or add it to
         * the current selection, as appropriate.
         */
        if (select) {
            if ((addCreatedEventsToSelected == false) && (Boolean.TRUE
                    .equals(settings.getAddToSelected()) == false)) {
                sessionManager.getSelectionManager()
                        .setSelectedEventIdentifiers(
                                Sets.newHashSet(oevent.getEventID()),
                                Originator.OTHER);
            } else {
                sessionManager.getSelectionManager().addEventToSelectedEvents(
                        oevent.getEventID(), Originator.OTHER);
            }
        }

        /*
         * Create the event metadata for the new event.
         */
        updateEventMetadata(eventView);

        oevent.setModifiedNotAllowedToChange(false);
        return eventView;
    }

    @Override
    public EventPropertyChangeResult mergeHazardEvents(
            IReadableHazardEvent newEvent, IHazardEventView oldEvent,
            boolean forceMerge, boolean persistOnStatusChange,
            boolean useModifiedValue, IOriginator originator) {

        ObservedHazardEvent sessionEvent = getSessionEventForView(oldEvent);
        if (sessionEvent == null) {
            return EventPropertyChangeResult.FAILURE_DUE_TO_EVENT_NOT_FOUND;
        }

        /*
         * Make sure that the event is locked if it needs to be.
         */
        if (originator.isNotLockedByOthersRequired()
                && isEventInDatabase(sessionEvent)
                && (sessionManager.getLockManager()
                        .lockHazardEvent(oldEvent.getEventID()) == false)) {
            showWarningMessageAboutLockedEvents("Cannot Modify", "modified",
                    originator, oldEvent.getEventID());
            return EventPropertyChangeResult.FAILURE_DUE_TO_LOCK_STATUS;
        }

        sessionManager.startBatchedChanges();

        sessionEvent.setSiteID(newEvent.getSiteID(), originator);
        sessionEvent.setWsId(newEvent.getWsId(), originator);

        /*
         * Set the hazard type, time range, and geometry via the session manager
         * if not a forced merge.
         */
        if (forceMerge) {
            sessionEvent.setHazardType(newEvent.getPhenomenon(),
                    newEvent.getSignificance(), newEvent.getSubType(),
                    originator);
            sessionEvent.setTimeRange(newEvent.getStartTime(),
                    newEvent.getEndTime(), originator);
            sessionEvent.setGeometry(newEvent.getGeometry(), originator);
        } else {
            setEventType(sessionEvent, newEvent.getPhenomenon(),
                    newEvent.getSignificance(), newEvent.getSubType(),
                    originator);
            setEventTimeRange(sessionEvent, newEvent.getStartTime(),
                    newEvent.getEndTime(), originator);
            setEventGeometry(sessionEvent, newEvent.getGeometry(), originator);
        }

        sessionEvent.setCreationTime(newEvent.getCreationTime(), originator);

        /*
         * If the merge is from the database or the result of a revert, augment
         * the new version of the event's visual features list to include any
         * visual features from the old version of the event that are not to be
         * persisted. (Because this means they are session-specific, and thus
         * should be retained when a new version of the event arrives.)
         */
        VisualFeaturesList oldVisualFeatures = sessionEvent.getVisualFeatures();
        VisualFeaturesList newVisualFeatures = newEvent.getVisualFeatures();
        if (((originator == Originator.DATABASE)
                || (originator instanceof RevertOriginator))
                && (oldVisualFeatures != null)) {
            if (newVisualFeatures == null) {
                newVisualFeatures = new VisualFeaturesList();
            } else {
                newVisualFeatures = new VisualFeaturesList(newVisualFeatures);
            }
            for (VisualFeature visualFeature : oldVisualFeatures) {
                if (visualFeature.isPersist() == false) {
                    newVisualFeatures.add(visualFeature);
                }
            }
        }
        sessionEvent.setVisualFeatures(
                ((newVisualFeatures == null) || newVisualFeatures.isEmpty()
                        ? null : newVisualFeatures),
                originator);

        sessionEvent.setHazardMode(newEvent.getHazardMode(), originator);

        /*
         * Get a copy of the old attributes, and the new ones, then transfer any
         * attributes that are to be retained (if they are not already in the
         * new attributes) from the old to the new. Then, if the origin is the
         * database, transfer any attributes that are session attributes from
         * the old to the new -- unlike the ones to be retained, the old
         * attribute values are always used in place of the new ones. Finally,
         * set the resulting map as the old hazard's attributes.
         */
        Map<String, Serializable> oldAttr = sessionEvent.getHazardAttributes();
        if (oldAttr == null) {
            oldAttr = Collections.emptyMap();
        }
        Map<String, Serializable> newAttr = newEvent.getHazardAttributes();
        newAttr = (newAttr != null ? new HashMap<>(newAttr)
                : new HashMap<String, Serializable>());
        for (String key : ATTRIBUTES_TO_RETAIN_ON_MERGE) {
            if ((newAttr.containsKey(key) == false)
                    && oldAttr.containsKey(key)) {
                newAttr.put(key, oldAttr.get(key));
            }
        }
        if ((originator == Originator.DATABASE)
                || (originator instanceof RevertOriginator)) {
            for (String key : configManager
                    .getSessionAttributes(sessionEvent.getHazardType())) {
                if (oldAttr.containsKey(key)) {
                    newAttr.put(key, oldAttr.get(key));
                }
            }
        }
        sessionEvent.setHazardAttributes(newAttr, originator);

        /*
         * Change the status only if either the event is already ended but the
         * new status is elapsed, or if the event is not already ended (this
         * could be relevant if the CAVE clock is set back) and it is not the
         * same as the previous status. If the status of the old event is
         * changed, update its saved end time/duration if it is issued.
         */
        if ((isEnded(sessionEvent)
                && newEvent.getStatus() == HazardStatus.ELAPSED)
                || ((isEnded(sessionEvent) == false) && (sessionEvent
                        .getStatus().equals(newEvent.getStatus()) == false))) {
            sessionEvent.setStatus(newEvent.getStatus(), true,
                    persistOnStatusChange, originator);
            updateSavedTimesForEventIfIssued(oldEvent, false);
        }
        sessionEvent.setIssuanceCount(newEvent.getIssuanceCount(), originator);

        if (useModifiedValue) {
            sessionEvent.setModified(newEvent.isModified());
        }

        sessionManager.finishBatchedChanges();

        return EventPropertyChangeResult.SUCCESS;
    }

    /**
     * Determine if the specified hazard event has ended.
     * 
     * @param event
     *            Event to be checked.
     * @return <code>true</code> if the event has ended, <code>false</code>
     *         otherwise.
     */
    private boolean isEnded(IReadableHazardEvent event) {
        return (event.getStatus() == HazardStatus.ENDED);
    }

    /**
     * Determine if the specified hazard event has elapsed based on its end time
     * and the current CAVE clock time.
     * 
     * @param event
     *            Event to be checked.
     * @return <code>true</code> if the event is set to a status of
     *         {@link HazardStatus#ELAPSED} or if the current CAVE clock time is
     *         later than the event's end time, <code>false</code> otherwise.
     */
    private boolean isElapsed(IReadableHazardEvent event) {
        Date currTime = SimulatedTime.getSystemTime().getTime();
        HazardStatus status = event.getStatus();
        if ((status == HazardStatus.ELAPSED)
                || (HazardStatus.issuedButNotEndedOrElapsed(status)
                        && (event.getEndTime().before(currTime)))) {
            return true;
        }
        return false;
    }

    private boolean isPastExpirationTime(IHazardEventView eventView) {
        long currTimeLong = SimulatedTime.getSystemTime().getMillis();

        Long expirationTimeLong = (Long) eventView
                .getHazardAttribute(HazardConstants.EXPIRATION_TIME);
        if ((expirationTimeLong != null)
                && (expirationTimeLong < currTimeLong)) {
            long expirationTime = expirationTimeLong.longValue();
            if (expirationTime < currTimeLong) {
                return true;
            }
        }

        return false;
    }

    /**
     * Handle the addition or modification of an event in the database.
     * 
     * @param event
     *            Hazard event added to or modified in the database.
     */
    protected void handleEventAdditionToDatabase(HazardEvent event) {

        /*
         * TODO: Remove once the HISTORICAL attribute is no longer being used.
         */
        boolean historical = Boolean.TRUE.equals(
                event.getHazardAttribute(HazardEventManager.HISTORICAL));
        event.removeHazardAttribute(HazardEventManager.HISTORICAL);

        /*
         * If the event is being managed already, ensure that if
         * interoperability is responsible, the event's lock (if any) is broken.
         * Also merge the new version with the existing one, and if it is a
         * historical snapshot, post a notification indicating that the history
         * list for this event has changed. If it is not already being managed,
         * add it to the session.
         */
        String eventIdentifier = event.getEventID();
        IHazardEventView oldEventView = getEventById(eventIdentifier);
        if (oldEventView != null) {

            /*
             * TODO: What happens if a Hazard Services client that locked an
             * event is not running? Its lock will still be considered active,
             * and since it's not around to receive notification that the event
             * was changed elsewhere, it will not release its lock. This needs
             * to be resolved, as it's a potentially big hole in the
             * implementation.
             */
            LockStatus status = sessionManager.getLockManager()
                    .getHazardEventLockStatus(eventIdentifier);
            if (status == LockStatus.LOCKED_BY_ME) {
                WsId eventWsId = event.getWsId();
                if (eventWsId.getProgName().equals(HazardConstants.INGEST)) {
                    messenger.getWarner().warnUser("Hazard Lock Broken",
                            "Interoperability has updated hazard event "
                                    + eventIdentifier
                                    + ". This has broken your hazard lock and any unsaved "
                                    + "changes may have be lost.");
                    if (sessionManager.getLockManager()
                            .unlockHazardEvent(eventIdentifier) == false) {
                        statusHandler.warn(
                                "Could not unlock events in response to interoperability breakage of lock.");
                    }
                } else if (event.getStatus() == HazardStatus.ELAPSED) {
                    messenger.getWarner().warnUser("Hazard Lock Broken",
                            "The hazard event " + eventIdentifier
                                    + " has elapsed. This has broken your hazard lock and "
                                    + "any unsaved changes may have be lost.");
                    if (sessionManager.getLockManager()
                            .unlockHazardEvent(eventIdentifier) == false) {
                        statusHandler.warn(
                                "Could not unlock events in response to elapsing of event causing breakage of lock.");
                    }
                } else if (HazardEventUtilities.compareWsIds(event.getWsId(),
                        VizApp.getWsId()) == false) {
                    statusHandler
                            .error("Received persistence notification for hazard event "
                                    + eventIdentifier
                                    + " that originated from some other source than "
                                    + "interoperability. That event should have been locked.");
                }
            }

            mergeHazardEvents(event, oldEventView, true, false, true,
                    Originator.DATABASE);
            if (event.isLatestVersion() == false) {
                notificationSender.postNotificationAsync(
                        new SessionEventHistoryModified(this, oldEventView,
                                Originator.DATABASE));
            }
        } else {
            try {
                sessionManager.startBatchedChanges();
                addEvent(event, Originator.DATABASE);
            } catch (HazardEventServiceException e) {
                statusHandler.error(
                        "Should never occur since hazard event did not need "
                                + "new identifier: could not add hazard event.",
                        e);
            } finally {
                sessionManager.finishBatchedChanges();
            }
        }

        /*
         * TODO: Uncomment the isLatestVersion() code once the HISTORICAL
         * attribute is no longer being used.
         * 
         * If the added event is not a historical snapshot, associate it with
         * its event identifier so that it may be referenced later; otherwise,
         * update the history list size record for the hazard event, and if it
         * has an issue time, assume it is not modified.
         */
        if (historical == false) {
            // if (event.isLatestVersion()) {
            latestVersionsFromDatabaseForEventIdentifiers.put(eventIdentifier,
                    event);
        } else {
            historicalVersionCountsForEventIdentifiers.put(eventIdentifier,
                    dbManager.getHistorySizeByEventID(eventIdentifier, false));
        }
        identifiersOfPersistedEvents.add(eventIdentifier);
    }

    /**
     * Handle the removal of an event from the database.
     * 
     * @param event
     *            Hazard event removed from the database.
     */
    protected void handleEventRemovalFromDatabase(HazardEvent event) {

        /*
         * TODO: Remove once the HISTORICAL attribute is no longer being used.
         */
        boolean historical = Boolean.TRUE.equals(
                event.getHazardAttribute(HazardEventManager.HISTORICAL));
        event.removeHazardAttribute(HazardEventManager.HISTORICAL);

        /*
         * TODO: Uncomment the isLatestVersion() code once the HISTORICAL
         * attribute is no longer being used.
         * 
         * If the database is merely indicating that a latest version of a
         * hazard event has been removed, disassociate it from its event
         * identifier, but do nothing else.
         */
        if (historical == false) {
            // if (event.isLatestVersion()) {
            latestVersionsFromDatabaseForEventIdentifiers
                    .remove(event.getEventID());
            return;
        }

        /*
         * TODO: There is no capability to remove individual historical
         * snapshots of hazard events yet.
         */
        statusHandler.error(
                "Cannot handle removal of historical snapshot of hazard event "
                        + event.getEventID() + " from database.",
                new UnsupportedOperationException("not yet implemented"));
    }

    /**
     * Handle the removal of all copies of an event from the database.
     * 
     * @param eventIdentifier
     *            Identifier of the event for which all copies were removed from
     *            the database.
     */
    protected void handleEventRemovalAllCopiesFromDatabase(
            String eventIdentifier) {
        IHazardEventView oldEventView = getEventById(eventIdentifier);
        if (oldEventView != null) {
            sessionManager.startBatchedChanges();
            deleteEvent(oldEventView, false, Originator.DATABASE);
            sessionManager.finishBatchedChanges();
        }
    }

    /**
     * Handle the specified hazard event changing its geometry as a result of an
     * undo or redo.
     * 
     * @param event
     *            Event that has changed its geometry.
     */
    protected void handleEventGeometryChangeFromUndoOrRedo(
            ObservedHazardEvent event) {
        updateHazardAreas(getViewForSessionEvent(event));
    }

    @Override
    public void removeEvent(IHazardEventView event, boolean confirm,
            IOriginator originator) {
        if ((confirm == false)
                || (getEventsToBeDeleted(Lists.newArrayList(event))
                        .isEmpty() == false)) {
            sessionManager.startBatchedChanges();
            deleteEvent(event, true, originator);
            sessionManager.finishBatchedChanges();
        }
    }

    @Override
    public void removeEvents(Collection<? extends IHazardEventView> events,
            boolean confirm, IOriginator originator) {

        /*
         * Avoid concurrent modification since events is backed by the
         * object-scoped events.
         */
        Collection<? extends IHazardEventView> eventsToRemove = (confirm
                ? getEventsToBeDeleted(events)
                : new ArrayList<IHazardEventView>(events));
        sessionManager.startBatchedChanges();
        for (IHazardEventView eventView : eventsToRemove) {
            deleteEvent(eventView, true, originator);
        }
        sessionManager.finishBatchedChanges();
    }

    @Override
    public void resetEvents(IOriginator originator) {
        for (IHazardEventView event : getEvents()) {
            deleteEvent(event, true, Originator.OTHER);
        }
    }

    /**
     * Confirm deletion of the specified hazard events, if necessary.
     * 
     * @param events
     *            Views of the events for which to confirm deletion if
     *            necessary.
     * @return Views of the events to be deleted.
     */
    private Collection<? extends IHazardEventView> getEventsToBeDeleted(
            Collection<? extends IHazardEventView> events) {
        Map<IHazardEventView, String> identifiersForEventsRequiringConfirmation = new HashMap<>(
                events.size(), 1.0f);
        Collection<IHazardEventView> eventsToDelete = new LinkedHashSet<>(
                events.size(), 1.0f);
        for (IHazardEventView eventView : events) {
            if (eventView.getStatus().equals(HazardStatus.PROPOSED)) {
                identifiersForEventsRequiringConfirmation.put(eventView,
                        eventView.getDisplayEventID());
            } else if ((eventView.getStatus().equals(HazardStatus.POTENTIAL))
                    || (eventView.getStatus().equals(HazardStatus.PENDING))) {
                eventsToDelete.add(eventView);
            }
        }
        if ((identifiersForEventsRequiringConfirmation.isEmpty() == false)
                && messenger.getContinueCanceller().getUserAnswerToQuestion(
                        "Delete Proposed Event Confirmation",
                        "Are you sure you want to delete the following proposed event(s)?\n\n"
                                + Joiner.on(", ")
                                        .join(identifiersForEventsRequiringConfirmation
                                                .values()))) {
            eventsToDelete
                    .addAll(identifiersForEventsRequiringConfirmation.keySet());
        }
        return eventsToDelete;
    }

    /**
     * Remove the specified hazard event. If lock status needs to be checked and
     * the event is locked by another workstation, the user is notified.
     * <p>
     * <strong>NOTE</strong>: This method is called whenever an event is removed
     * from the current session, regardless of the source of the change.
     * Additional logic (method calls, etc.) may therefore be added to this
     * method's implementation as necessary if said logic must be run whenever
     * an event is removed. If lock status is specified as to be checked, the
     * event is locked by another workstation, and the user initiated this
     * attempt, the user is notified of the failure.
     * </p>
     * 
     * @param eventView
     *            View of the event to be deleted.
     * @param deleteFromDatabase
     *            Flag indicating whether or not the event should also be
     *            deleted from the database.
     * @param originator
     *            Originator of the change.
     */
    private EventPropertyChangeResult deleteEvent(IHazardEventView eventView,
            boolean deleteFromDatabase, IOriginator originator) {
        EventPropertyChangeResult result = EventPropertyChangeResult.SUCCESS;
        if (allEventViews.contains(eventView)) {
            if (originator.isNotLockedByOthersRequired()
                    && (isEventNotLockedByOther(eventView) == false)) {
                showWarningMessageAboutLockedEvents("Cannot Remove", "removed",
                        originator, eventView.getEventID());
                return EventPropertyChangeResult.FAILURE_DUE_TO_LOCK_STATUS;
            }

            /*
             * Deselect the event if it was selected, remove it from the checked
             * event identifiers set if it was there, and remove it.
             */
            String eventIdentifier = eventView.getEventID();
            sessionManager.getSelectionManager().removeEventFromSelectedEvents(
                    eventIdentifier, Originator.OTHER);
            checkedEventIdentifiers.remove(eventIdentifier);
            ObservedHazardEvent event = getSessionEventForView(eventView);
            allEvents.remove(event);
            allEventViews.remove(eventView);

            /*
             * Remove the view for the deleted hazard event.
             */
            viewsForHazardEvents.remove(event);

            /*
             * TODO this should never delete operation issued events.
             */
            /*
             * TODO this should not delete the whole list, just any pending or
             * proposed items on the end of the list.
             */
            if (deleteFromDatabase) {
                dbManager.removeAllCopiesOfEvent(eventIdentifier);
            }
            updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(eventView,
                    true);
            megawidgetSpecifiersForEventIdentifiers.remove(eventIdentifier);
            metadataReloadTriggeringIdentifiersForEventIdentifiers
                    .remove(eventIdentifier);
            metadataIdentifiersAffectingModifyFlagsForEventIdentifiers
                    .remove(eventIdentifier);
            recommendersForTriggerIdentifiersForEventIdentifiers
                    .remove(eventIdentifier);
            editRiseCrestFallTriggeringIdentifiersForEventIdentifiers
                    .remove(eventIdentifier);
            notificationSender.postNotificationAsync(
                    new SessionEventsRemoved(this, eventView, originator));
        } else {
            result = EventPropertyChangeResult.FAILURE_DUE_TO_EVENT_NOT_FOUND;
        }

        /*
         * Remove the history list size record and the latest version record for
         * the hazard event, since they are no longer needed.
         */
        latestVersionsFromDatabaseForEventIdentifiers
                .remove(eventView.getEventID());
        historicalVersionCountsForEventIdentifiers
                .remove(eventView.getEventID());
        identifiersOfPersistedEvents.remove(eventView.getEventID());
        return result;
    }

    @Override
    public void sortEvents(Comparator<IReadableHazardEvent> comparator,
            IOriginator originator) {
        Collections.sort(allEvents, comparator);
        Collections.sort(allEventViews, comparator);
        notificationSender.postNotificationAsync(
                new SessionEventsOrderingModified(this, originator));
    }

    @Override
    public List<IHazardEventView> getEvents() {
        return new ArrayList<>(allEventViews);
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
     * 
     * @param notification
     *            Notification to be sent out about the modification.
     */
    protected void hazardEventModified(
            AbstractSessionEventModified notification) {
        IHazardEventView eventView = notification.getEvent();
        ObservedHazardEvent event = getSessionEventForView(eventView);
        sessionManager.getSelectionManager().setLastAccessedSelectedEvent(
                event.getEventID(), notification.getOriginator());
        updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(eventView,
                false);
        ensureEventEndTimeUntilFurtherNoticeAppropriate(eventView, false);
        if (allEventViews.contains(eventView)) {
            notificationSender.postNotificationAsync(notification);
        }
    }

    /**
     * Receive notification that the specified event that its unsaved changes
     * (modified) flag has changed.
     * 
     * @param event
     *            Event that has experienced a change.
     */
    protected void hazardEventModifiedFlagChanged(ObservedHazardEvent event) {
        if (allEvents.contains(event)) {
            notificationSender.postNotificationAsync(new SessionEventModified(
                    this, getViewForSessionEvent(event),
                    new EventUnsavedChangesModification(), Originator.OTHER));
            if (event.isModified()
                    && (event.getStatus() == HazardStatus.POTENTIAL)) {
                event.setStatus(HazardStatus.PENDING, false, Originator.OTHER);
            }
        }
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
    protected void hazardEventAttributeModified(
            SessionEventModified notification) {
        IHazardEventView eventView = notification.getEvent();
        sessionManager.getSelectionManager().setLastAccessedSelectedEvent(
                eventView.getEventID(), notification.getOriginator());
        if (allEventViews.contains(eventView)) {
            notificationSender.postNotificationAsync(notification);
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
    protected void hazardEventStatusModified(SessionEventModified notification,
            boolean persist) {

        /*
         * Persist and deselect the event as necessary.
         */
        persistAndDeselectEventDueToStatusChange(
                getSessionEventForView(notification.getEvent()), persist,
                notification.getOriginator());

        /*
         * If the event is being managed, post the notification about the status
         * change.
         */
        if (allEventViews.contains(notification.getEvent())) {
            notificationSender.postNotificationAsync(notification);
        }

        updateConflictingEventsForSelectedEventIdentifiers(
                notification.getEvent(), false);
    }

    /**
     * Persist and deselect the specified event as appropriate.
     * 
     * @param event
     *            Event to be persisted and/or deselected.
     * @param persist
     *            Flag indicating whether or not the event is to be persisted.
     * @param originator
     *            Originator of the change triggering this invocation.
     */
    @SuppressWarnings("unchecked")
    private void persistAndDeselectEventDueToStatusChange(
            ObservedHazardEvent event, boolean persist,
            IOriginator originator) {

        /*
         * Determine whether or not the event should be persisted, and deselect
         * it if it is ended and, VTEC-wise, canceled.
         */
        boolean removedFromSelection = false;
        HazardStatus newStatus = event.getStatus();
        if (persist) {

            boolean needsPersist = false;
            switch (newStatus) {
            case ISSUED:
                event.addHazardAttribute(HazardConstants.ISSUED, true);
                needsPersist = true;
                break;
            case PROPOSED:
                needsPersist = true;
                break;
            case ELAPSED:
                needsPersist = false;
                break;
            case ENDED:
                List<String> vtecCodes = (List<String>) event
                        .getHazardAttribute(HazardConstants.VTEC_CODES);

                /*
                 * Only deselect if canceled.
                 */
                if ((vtecCodes != null) && vtecCodes.contains("CAN")) {
                    removedFromSelection = true;
                    sessionManager.getSelectionManager()
                            .removeEventFromSelectedEvents(event.getEventID(),
                                    Originator.OTHER);
                }
                needsPersist = true;

                break;
            default:

                /*
                 * No action.
                 */
            }
            if (needsPersist) {
                persistEvent(event);
            }
        }

        /*
         * If the new status is visible in the settings (with pending ones
         * always being visible), make this event the last accessed event;
         * otherwise, deselect the event if it has not already been deselected,
         * as it should no longer be visible in the UI.
         * 
         * TODO: If ever undoing changes from issue #17328 (making pending
         * status not available for filtering out), delete the first part of the
         * conditional below so that pending status is not checked for.
         */
        if ((newStatus == HazardStatus.PENDING) || configManager.getSettings()
                .getVisibleStatuses().contains(newStatus.getValue())) {
            sessionManager.getSelectionManager().setLastAccessedSelectedEvent(
                    event.getEventID(), originator);
        } else if (removedFromSelection == false) {
            sessionManager.getSelectionManager().removeEventFromSelectedEvents(
                    event.getEventID(), Originator.OTHER);
        }
    }

    /**
     * Persist the specified event by storing it to the database, placing it on
     * the history list.
     * 
     * @param event
     *            Event to be persisted.
     */
    private void persistEvent(ObservedHazardEvent event) {
        try {

            /*
             * If there is a "latest version" stored in the database, then
             * remove it before adding to the history list. Note that the entry
             * in the map for latest versions is not removed at this point, as
             * it will be removed when the notification comes from the database
             * that a removal occurred.
             * 
             * TODO: Remove the use of the HISTORICAL attribute once saving
             * latest version capability is restored.
             */
            HazardEvent latestVersion = latestVersionsFromDatabaseForEventIdentifiers
                    .get(event.getEventID());
            if (latestVersion != null) {
                dbManager.removeEvents(latestVersion);
            }
            event.setModified(false);
            HazardEvent eventCopy = createEventCopyToBePersisted(event, true,
                    true);
            eventCopy.addHazardAttribute(HazardEventManager.HISTORICAL, true);
            dbManager.storeEvents(eventCopy);
            identifiersOfPersistedEvents.add(event.getEventID());
            scheduleExpirationTask(event);
        } catch (Throwable e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
    }

    /**
     * Get the set of hazard attribute names for the specified hazard event
     * that, when modified, should not alter said event's modified flag.
     * 
     * @param eventIdentifier
     *            Identifier of the event for which to fetch the set of
     *            attribute names.
     * @return Set of attribute names; may be empty.
     */
    protected Set<String> getHazardAttributesAffectingModifyFlag(
            String eventIdentifier) {
        Set<String> result = metadataIdentifiersAffectingModifyFlagsForEventIdentifiers
                .get(eventIdentifier);
        return (result != null ? result : Collections.<String> emptySet());
    }

    /**
     * Schedules the tasks on the {@link Timer} to be executed at a later time,
     * unless they are already past the time necessary at which it will happen
     * immediately then.
     * 
     * @param event
     */
    private void scheduleExpirationTask(final ObservedHazardEvent event) {

        /*
         * TODO: Decide whether expiration is to be done in Java code or not.
         * For now, this code is commented out; not expiration of events will
         * occur. This is not a permanent solution.
         */

        // if (eventExpirationTimer != null) {
        // if (HazardStatus.issuedButNotEndedOrElapsed(event.getStatus())) {
        // final String eventId = event.getEventID();
        // TimerTask existingTask = expirationTasks.get(eventId);
        // if (existingTask != null) {
        // existingTask.cancel();
        // expirationTasks.remove(eventId);
        // }
        // TimerTask task = new TimerTask() {
        // @Override
        // public void run() {
        // event.setStatus(HazardStatus.ELAPSED, true, true,
        // Originator.OTHER);
        // expirationTasks.remove(eventId);
        // }
        // };
        // Date scheduledTime = event.getEndTime();
        // /*
        // * TODO: Need to determine what to do with this, somewhere we
        // * need to be resetting the expiration time if we manually end
        // * the hazard?
        // */
        // // if (event.getHazardAttribute(HazardConstants.EXPIRATIONTIME)
        // // != null) {
        // // scheduledTime = new Date(
        // // // TODO, change this when we are getting back
        // // // expiration time as a date
        // // (Long) event
        // // .getHazardAttribute(HazardConstants.EXPIRATIONTIME));
        // // }
        //
        // /*
        // * Round down to the nearest minute, so we see exactly when it
        // * happens.
        // */
        // scheduledTime = DateUtils.truncate(scheduledTime,
        // Calendar.MINUTE);
        // long scheduleTimeMillis = Math.max(0, scheduledTime.getTime()
        // - SimulatedTime.getSystemTime().getTime().getTime());
        // if (SimulatedTime.getSystemTime().isFrozen() == false
        // || (SimulatedTime.getSystemTime().isFrozen() && scheduleTimeMillis ==
        // 0)) {
        // eventExpirationTimer.schedule(task, scheduleTimeMillis);
        // expirationTasks.put(eventId, task);
        // }
        // }
        // }
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

                /*
                 * TODO: Decide whether expiration is to be done in Java code or
                 * not. For now, this code is commented out; not expiration of
                 * events will occur. This is not a permanent solution.
                 */

                // for (TimerTask task : expirationTasks.values()) {
                // task.cancel();
                // expirationTasks.clear();
                // }
                //
                // for (ObservedHazardEvent event : events) {
                // scheduleExpirationTask(event);
                // }
            }
        };
        return timeListener;
    }

    @Override
    public boolean canEventTypeBeChanged(IReadableHazardEvent event) {
        return (hasEverBeenIssued(event) == false);
    }

    private boolean hasEverBeenIssued(IReadableHazardEvent event) {
        return Boolean.TRUE
                .equals(event.getHazardAttribute(HazardConstants.ISSUED));
    }

    @Override
    public Map<String, Collection<IReadableHazardEvent>> getConflictingEventsForSelectedEvents() {
        return Collections
                .unmodifiableMap(conflictingEventsForSelectedEventIdentifiers);
    }

    /**
     * Round the specified date down to the nearest minute if the specified time
     * resolution is {@link TimeResolution#MINUTES}.
     * 
     * @param date
     *            Date-time to be rounded down if appropriate.
     * @param timeResolution
     *            Time resolution.
     * @return Date-time rounded down if appropriate.
     */
    private Date roundDateDownToNearestMinuteIfAppropriate(Date date,
            TimeResolution timeResolution) {
        if (timeResolution == TimeResolution.MINUTES) {
            return roundDateDownToNearestMinute(date);
        }
        return date;
    }

    /**
     * Round the specified date down to the nearest minute.
     * 
     * @param date
     *            Date-time to be rounded down.
     * @return Rounded down time.
     */
    private Date roundDateDownToNearestMinute(Date date) {
        return DateUtils.truncate(date, Calendar.MINUTE);
    }

    /**
     * Round the specified epoch time in milliseconds down to the nearest
     * minute.
     * 
     * @param date
     *            Date-time to be rounded down.
     * @return Rounded down time.
     */
    private long roundTimeDownToNearestMinute(Date date) {
        return roundDateDownToNearestMinute(date).getTime();
    }

    /**
     * Round the specified epoch time in milliseconds down to the nearest
     * minute.
     * 
     * @param time
     *            Time to be rounded down.
     * @return Rounded down time.
     */
    private long roundTimeDownToNearestMinute(long time) {
        return roundTimeDownToNearestMinute(new Date(time));
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
        boolean untilFurtherNotice = (endTime == HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS);
        return Range.closed((untilFurtherNotice ? endTime : 0L),
                (untilFurtherNotice ? endTime : HazardConstants.MAX_TIME));
    }

    /**
     * Get an allowable end time range for the specified event given the
     * specified end time.
     * 
     * @parma eventView View of the event for which to determine the allowable
     *        range.
     * @param endTime
     *            Event end time.
     * @return Allowable range for the event's end times.
     */
    private Range<Long> getEndTimeRangeForIssuedEventBasedOnEndTime(
            IHazardEventView eventView, long endTime) {

        /*
         * If the end time is "until further notice", limit the end times to
         * just that value.
         */
        if (endTime == HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
            return Range.singleton(endTime);
        }

        /*
         * Use the end time as the lower and/or upper bound of the allowable end
         * times, as appropriate given the event's type's ability to be shrunk
         * or expanded after issuance.
         */
        return Range.closed(
                (configManager.isAllowTimeShrink(eventView)
                        ? HazardConstants.MIN_TIME : endTime),
                (configManager.isAllowTimeExpand(eventView)
                        ? HazardConstants.MAX_TIME : endTime));
    }

    /**
     * Get the allowable end time range for the specified event with the
     * specified start and end times that has been issued but is not yet ending.
     * 
     * @parma eventView View of the event for which to determine the allowable
     *        range.
     * @param startTime
     *            Event start time.
     * @param endTime
     *            Event end time.
     * @return Allowable range for the event's end times.
     */
    private Range<Long> getEndTimeRangeForIssuedEvent(
            IHazardEventView eventView, long startTime, long endTime) {

        /*
         * If the end time is "until further notice", limit the end times to
         * just that value.
         */
        if (endTime == HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
            return getEndTimeRangeForIssuedEventBasedOnEndTime(eventView,
                    endTime);
        }

        /*
         * If the event has an absolute end time, fin the potential end time
         * boundary one way; if it is a duration-type event, find it another
         * way.
         */
        if (configManager.getDurationChoices(eventView).isEmpty()) {

            /*
             * Determine the end time of the event when it was last issued, and
             * use that as a potential end time boundary.
             */
            endTime = endTimesOrDurationsForIssuedEventIdentifiers
                    .get(eventView.getEventID());
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
            endTime = startTime + endTimesOrDurationsForIssuedEventIdentifiers
                    .get(eventView.getEventID());
        }

        /*
         * Given the modified end time, get the range.
         */
        return getEndTimeRangeForIssuedEventBasedOnEndTime(eventView, endTime);
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
     * @param eventView
     *            Event to have its end time boundaries modified.
     * @param newStartTime
     *            New start time, in epoch time in milliseconds.
     * @param newEndTime
     *            New end time, in epoch time in milliseconds; if this is equal
     *            to
     *            {@link HazardConstants#UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS}
     *            , then "until further notice" has been turned on.
     */
    private void updateEndTimeBoundariesForSingleEvent(
            IHazardEventView eventView, long newStartTime, long newEndTime) {
        Range<Long> endTimeRange = null;
        switch (eventView.getStatus()) {
        case POTENTIAL:
        case PENDING:
        case PROPOSED:
            endTimeRange = getEndTimeRangeForPreIssuedEvent(newEndTime);
            break;
        case ISSUED:
            endTimeRange = getEndTimeRangeForIssuedEvent(eventView,
                    newStartTime, newEndTime);
            break;
        case ELAPSED:
        case ENDING:
        case ENDED:
            endTimeRange = getEndTimeRangeForEndingEvent(newEndTime);
        }
        if (endTimeRange.equals(endTimeBoundariesForEventIdentifiers
                .get(eventView.getEventID())) == false) {
            endTimeBoundariesForEventIdentifiers.put(eventView.getEventID(),
                    endTimeRange);
            if (allEventViews.contains(eventView)) {
                notificationSender.postNotificationAsync(
                        new SessionEventsTimeRangeBoundariesModified(this,
                                Sets.newHashSet(eventView.getEventID()),
                                Originator.OTHER));
            }
        }
    }

    /**
     * Set the specified event's start and end time ranges as specified.
     * 
     * @param eventView
     *            View of the event to have its ranges modified.
     * @param startTimeRange
     *            New allowable range of start times.
     * @param endTimeRange
     *            New allowable range of end times.
     * @return True if the new ranges are different from the previous ranges,
     *         false otherwise.
     */
    private boolean setEventTimeRangeBoundaries(IHazardEventView eventView,
            Range<Long> startTimeRange, Range<Long> endTimeRange) {
        boolean changed = false;
        String eventID = eventView.getEventID();
        if (startTimeRange.equals(
                startTimeBoundariesForEventIdentifiers.get(eventID)) == false) {
            startTimeBoundariesForEventIdentifiers.put(eventID, startTimeRange);
            changed = true;
        }
        if (endTimeRange.equals(
                endTimeBoundariesForEventIdentifiers.get(eventID)) == false) {
            endTimeBoundariesForEventIdentifiers.put(eventID, endTimeRange);
            changed = true;
        }
        return changed;
    }

    /**
     * Update the allowable ranges for start and end time of the specified event
     * to be correct given the event's status and other relevant properties.
     * 
     * @param eventView
     *            View of the event for which to update the start and end time
     *            allowable ranges.
     * @param currentTime
     *            Current CAVE time, as far as the event is concerned.
     * @return True if the time boundaries were modified, false otherwise.
     */
    private boolean updateTimeBoundariesForSingleEvent(
            IHazardEventView eventView, long currentTime) {

        /*
         * Handle pre-issued hazard events differently from ones that have been
         * issued at least once. Pre-issued ones have their start time marching
         * forward with CAVE clock time, and some allow the start time to be
         * before or after the current CAVE clock time, while some do not. End
         * times can be anything if unissued, but issued ones may be limited by
         * by the hazard type's contraints Finally, ending and ended hazards
         * cannot have their times changed.
         */
        Range<Long> startTimeRange = null;
        Range<Long> endTimeRange = null;
        long startTime = eventView.getStartTime().getTime();
        long endTime = eventView.getEndTime().getTime();
        switch (eventView.getStatus()) {
        case POTENTIAL:
        case PENDING:
        case PROPOSED:
            startTimeRange = Range.closed(
                    (configManager.isAllowAnyStartTime(eventView)
                            ? HazardConstants.MIN_TIME : currentTime),
                    (configManager.isStartTimeIsCurrentTime(eventView)
                            ? currentTime : HazardConstants.MAX_TIME));
            endTimeRange = getEndTimeRangeForPreIssuedEvent(endTime);
            break;
        case ISSUED:
            boolean startTimeIsCurrentTime = configManager
                    .isStartTimeIsCurrentTime(eventView);
            startTimeRange = Range.closed(
                    (startTimeIsCurrentTime ? startTime
                            : HazardConstants.MIN_TIME),
                    (startTimeIsCurrentTime ? startTime
                            : HazardConstants.MAX_TIME));
            endTimeRange = getEndTimeRangeForIssuedEvent(eventView, startTime,
                    endTime);
            break;
        case ELAPSED:
        case ENDING:
        case ENDED:
            startTimeRange = Range.closed(startTime, startTime);
            endTimeRange = getEndTimeRangeForEndingEvent(endTime);
        }

        /*
         * Use the generated ranges.
         */
        return setEventTimeRangeBoundaries(eventView, startTimeRange,
                endTimeRange);
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
        notificationSender.postNotificationAsync(
                new SessionEventsTimeRangeBoundariesModified(this,
                        eventIdentifiers, Originator.OTHER));
    }

    /**
     * Update the allowable ranges for start and end time of the specified event
     * that has just been issued, as well as its start and end times themselves.
     * A notification is sent off of the changes made if any boundaries are
     * changed.
     * 
     * @param eventView
     *            Event that has just been issued.
     * @param issueTime
     *            Issue time, as epoch time in milliseconds.
     */
    private void updateTimeRangeBoundariesOfJustIssuedEvent(
            IHazardEventView eventView, long issueTime) {

        /*
         * Get the old start time, and then get the actual issuance time for the
         * hazard, and round it down to the nearest minute. If the start time is
         * less than the rounded-down issue time, set the former to be the
         * latter, since the start time should never be less than when the event
         * was last issued.
         */
        long startTime = eventView.getStartTime().getTime();
        issueTime = roundTimeDownToNearestMinute(new Date(issueTime));
        if (startTime < issueTime) {
            startTime = issueTime;
        }

        /*
         * Determine the allowable range for the start time. The minimum must be
         * the issue time from the issuance that occurred (if start time is
         * always current time) or else is practically unlimited. The maximum is
         * similar: either the issue time, or unlimited.
         */
        boolean startTimeIsIssueTime = configManager
                .isStartTimeIsCurrentTime(eventView);
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
        long endTime = eventView.getEndTime().getTime();
        if (endTime != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
            if (configManager.getDurationChoices(eventView).isEmpty()) {
                if (endTime
                        - startTime < HazardConstants.TIME_RANGE_MINIMUM_INTERVAL) {
                    endTime = startTime
                            + HazardConstants.TIME_RANGE_MINIMUM_INTERVAL;
                }
            } else {
                endTime += startTime - eventView.getStartTime().getTime();
            }
        }

        /*
         * Get the allowable range for the end time.
         */
        Range<Long> endTimeRange = getEndTimeRangeForIssuedEventBasedOnEndTime(
                eventView, endTime);

        /*
         * Use the new ranges; if these are different from the previous ranges,
         * post a notification to that effect.
         */
        if (setEventTimeRangeBoundaries(eventView, startTimeRange,
                endTimeRange)) {
            postTimeRangeBoundariesModifiedNotification(
                    Sets.newHashSet(eventView.getEventID()));
        }

        /*
         * Set the new start and end times.
         */
        getSessionEventForView(eventView).setTimeRange(new Date(startTime),
                new Date(endTime), Originator.OTHER);

        /*
         * Make a record of the event's start and its end time/duration at
         * issuance time, which now becomes the most recent issuance for this
         * event.
         */
        updateSavedTimesForEventIfIssued(eventView, false);
    }

    /**
     * Update the allowable ranges for start and end times of the specified
     * event, or of all events, as well as the start and end times themselves.
     * This is to be called whenever something that affects any of the events'
     * start/end time boundaries has potentially changed, other than an event
     * having just been issued. A notification is sent off of the changes made
     * if any boundaries are changed.
     * 
     * @param singleEventView
     *            View of the event that has been added, removed, or modified.
     *            If <code>null</code>, all events should be updated. In this
     *            case, the assumption is made that no events have been removed.
     * @param removed
     *            Flag indicating whether or not the change is the removal of
     *            the event; this is ignored if <code>event</code> is
     *            <code>null</code>.
     */
    private void updateTimeBoundariesForEvents(IHazardEventView singleEventView,
            boolean removed) {

        /*
         * If all events should be checked, iterate through them, adding any
         * that have their boundaries changed to the set recording changed
         * events. Otherwise, handle the single event's potential change.
         */
        long currentTime = SimulatedTime.getSystemTime().getTime().getTime();
        Set<String> identifiersWithChangedBoundaries = new HashSet<>();
        if (singleEventView == null) {
            Set<String> identifiersWithExpiredTimes = new HashSet<>();
            for (IHazardEventView eventView : allEventViews) {

                /*
                 * Round the current time down to the previous minute if this
                 * event has minute-level time resolution.
                 */
                long eventCurrentTime = (timeResolutionsForEventIdentifiers
                        .get(eventView.getEventID()) == TimeResolution.MINUTES
                                ? roundTimeDownToNearestMinute(currentTime)
                                : currentTime);
                if (updateTimeBoundariesForSingleEvent(eventView,
                        eventCurrentTime)) {
                    identifiersWithChangedBoundaries
                            .add(eventView.getEventID());
                }
                if (isPastExpirationTime(eventView) == true) {
                    identifiersWithExpiredTimes.add(eventView.getEventID());
                }
            }

            /*
             * If at least one event was found with an expired time, remove any
             * such events from the selection set, and send out a notification
             * of said events' time range boundaries having changed.
             */
            if (identifiersWithExpiredTimes.isEmpty() == false) {
                sessionManager.getSelectionManager()
                        .removeEventsFromSelectedEvents(
                                identifiersWithExpiredTimes, Originator.OTHER);
                if (identifiersWithChangedBoundaries.isEmpty()) {
                    postTimeRangeBoundariesModifiedNotification(
                            identifiersWithExpiredTimes);
                }
            }
        } else {

            /*
             * Remove the time boundaries if this event was removed, otherwise,
             * update its time boundaries, rounding the current time down to the
             * previous minute when doing so if the event has minute-level time
             * resolution.
             */
            if (removed) {
                startTimeBoundariesForEventIdentifiers
                        .remove(singleEventView.getEventID());
                endTimeBoundariesForEventIdentifiers
                        .remove(singleEventView.getEventID());
            } else if (updateTimeBoundariesForSingleEvent(singleEventView,
                    (timeResolutionsForEventIdentifiers.get(singleEventView
                            .getEventID()) == TimeResolution.MINUTES
                                    ? roundTimeDownToNearestMinute(currentTime)
                                    : currentTime))) {
                identifiersWithChangedBoundaries
                        .add(singleEventView.getEventID());
            }
        }

        /*
         * If any events' boundaries have changed, send out a notification to
         * that effect, and ensure that those that have changed have their start
         * and end times falling within the new boundaries.
         */
        if (identifiersWithChangedBoundaries.isEmpty() == false) {
            postTimeRangeBoundariesModifiedNotification(
                    identifiersWithChangedBoundaries);
            for (String identifier : identifiersWithChangedBoundaries) {
                ObservedHazardEvent thisEvent = getSessionEventForView(
                        getEventById(identifier));
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
                if (endTime != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {

                    /*
                     * If this event type uses durations instead of absolute end
                     * times, set the new end time to be the same distance from
                     * the new start time as the old one was from the old start
                     * time. Otherwise, boundary-check the end time.
                     */
                    if (configManager.getDurationChoices(thisEvent)
                            .isEmpty() == false) {
                        if (changed) {
                            endTime = startTime + duration;
                        }
                    } else {

                        /*
                         * Ensure that the end time is at least the minimum
                         * interval away from the start time.
                         */
                        if (endTime
                                - startTime < HazardConstants.TIME_RANGE_MINIMUM_INTERVAL) {
                            changed = true;
                            endTime = startTime
                                    + HazardConstants.TIME_RANGE_MINIMUM_INTERVAL;
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
                 * Set the modified-not-allowed-to-change flag so that a time
                 * range change caused by time boundaries being altered is not
                 * considered a modification.
                 */
                if (changed) {
                    boolean modifiedNotAllowedToChange = thisEvent
                            .isModifiedNotAllowedToChange();
                    if (modifiedNotAllowedToChange == false) {
                        thisEvent.setModifiedNotAllowedToChange(true);
                    }
                    thisEvent.setTimeRange(new Date(startTime),
                            new Date(endTime), Originator.OTHER);
                    if (modifiedNotAllowedToChange == false) {
                        thisEvent.setModifiedNotAllowedToChange(false);
                    }
                }
            }
        }
    }

    /**
     * Update the saved absolute or relative end time (the latter being
     * duration) for the specified event if the latter is issued.
     * 
     * @param eventView
     *            View of the event that needs its saved end time or duration
     *            updated to reflect its current state.
     * @param removed
     *            Flag indicating whether or not the event has been removed.
     */
    private void updateSavedTimesForEventIfIssued(IHazardEventView eventView,
            boolean removed) {
        String eventId = eventView.getEventID();
        if (removed) {
            endTimesOrDurationsForIssuedEventIdentifiers.remove(eventId);
        } else if (eventView.getStatus() == HazardStatus.ISSUED) {
            if (configManager.getDurationChoices(eventView).isEmpty()) {
                endTimesOrDurationsForIssuedEventIdentifiers.put(eventId,
                        eventView.getEndTime().getTime());
            } else {
                endTimesOrDurationsForIssuedEventIdentifiers.put(eventId,
                        eventView.getEndTime().getTime()
                                - eventView.getStartTime().getTime());
            }
        }
    }

    /**
     * Update the duration choices list associated with the specified event.
     * 
     * @param eventView
     *            View of the event for which the duration choices are to be
     *            updated.
     * @param removed
     *            Flag indicating whether or not the event has been removed.
     */
    private void updateDurationChoicesForEvent(IHazardEventView eventView,
            boolean removed) {

        /*
         * If the event has been removed, remove any duration choices associated
         * with it. Otherwise, update the choices.
         */
        if (removed) {
            durationChoicesForEventIdentifiers.remove(eventView.getEventID());
        } else {

            /*
             * Get all the choices available for this hazard type, and prune
             * them of any that do not fit within the allowable end time range.
             */
            List<String> durationChoices = configManager
                    .getDurationChoices(eventView);
            if (durationChoices.isEmpty() == false) {

                /*
                 * Get a map of the choice strings to their associated time
                 * deltas in milliseconds. The map will iterate in the order the
                 * choices are specified in the list used to generate it.
                 */
                Map<String, Long> deltasForDurations = getDeltasForDurationChoices(
                        eventView, durationChoices);
                if (deltasForDurations == null) {
                    return;
                }

                /*
                 * Iterate through the choices, checking each in turn to see if,
                 * when a choice's delta is added to the current event start
                 * time, the sum falls within the allowable end time range. If
                 * it does, add it to the list of approved choices.
                 */
                long startTime = eventView.getStartTime().getTime();
                Range<Long> endTimeRange = endTimeBoundariesForEventIdentifiers
                        .get(eventView.getEventID());
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
            durationChoicesForEventIdentifiers.put(eventView.getEventID(),
                    durationChoices);
        }
    }

    /**
     * Update the map of selected event identifiers to their collections of
     * conflicting events. This is to be called whenever something that affects
     * the selected events' potential conflicts changes.
     * <p>
     * TODO: Currently, the parameters are not used; the map is simply rebuilt.
     * This could certainly benefit from a less brute-force approach; see inline
     * comments within the method.
     * </p>
     * 
     * @param eventView
     *            Event that has been added, removed, or modified.
     * @param removed
     *            Flag indicating whether or not the change is the removal of
     *            the event.
     */
    protected void updateConflictingEventsForSelectedEventIdentifiers(
            IHazardEventView eventView, boolean removed) {

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
        Map<String, Collection<IReadableHazardEvent>> oldMap = new HashMap<>(
                conflictingEventsForSelectedEventIdentifiers);
        conflictingEventsForSelectedEventIdentifiers.clear();

        Collection<? extends IHazardEventView> selectedEventViews = sessionManager
                .getSelectionManager().getSelectedEvents();

        for (IHazardEventView eventToCheck : selectedEventViews) {

            Map<IReadableHazardEvent, Collection<String>> conflictingHazards = null;
            try {
                conflictingHazards = getConflictingEvents(eventToCheck,
                        eventToCheck.getStartTime(), eventToCheck.getEndTime(),
                        eventToCheck.getFlattenedGeometry(),
                        HazardEventUtilities.getHazardType(eventToCheck));
            } catch (HazardEventServiceException e) {
                statusHandler.error("Could not get conflicting events for "
                        + eventToCheck.getEventID(), e);
                continue;
            }
            if (conflictingHazards.isEmpty() == false) {
                conflictingEventsForSelectedEventIdentifiers
                        .put(eventToCheck.getEventID(), Collections
                                .unmodifiableSet(conflictingHazards.keySet()));
            }
        }

        if (oldMap.equals(
                conflictingEventsForSelectedEventIdentifiers) == false) {
            notificationSender.postNotificationAsync(
                    new SessionSelectedEventConflictsModified(this,
                            Originator.OTHER));
        }

    }

    @Override
    public Map<IReadableHazardEvent, Map<IReadableHazardEvent, Collection<String>>> getAllConflictingEvents()
            throws HazardEventServiceException {

        Map<IReadableHazardEvent, Map<IReadableHazardEvent, Collection<String>>> conflictingHazardMap = new HashMap<>();
        /*
         * Find the union of the session events and those retrieved from the
         * hazard event manager. Ignore "Ended" events.
         */
        List<IReadableHazardEvent> eventsToCheck = getEventsToCheckForConflicts(
                new HazardEventQueryRequest(CAVEMode.OPERATIONAL
                        .equals(CAVEMode.getMode()) == false),
                EnumSet.allOf(HazardStatus.class));

        for (IReadableHazardEvent eventToCheck : eventsToCheck) {

            Map<IReadableHazardEvent, Collection<String>> conflictingHazards = getConflictingEvents(
                    eventToCheck, eventToCheck.getStartTime(),
                    eventToCheck.getEndTime(),
                    eventToCheck.getFlattenedGeometry(),
                    HazardEventUtilities.getHazardType(eventToCheck));

            if (!conflictingHazards.isEmpty()) {
                conflictingHazardMap.put(eventToCheck, conflictingHazards);
            }

        }

        return conflictingHazardMap;
    }

    @Override
    public Map<IReadableHazardEvent, Collection<String>> getConflictingEvents(
            final IReadableHazardEvent event, final Date startTime,
            final Date endTime, final Geometry geometry, String phenSigSubtype)
            throws HazardEventServiceException {

        Map<IReadableHazardEvent, Collection<String>> conflictingHazardsMap = new HashMap<>();

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

                    String ugcLabel = hazardTypeEntry.getUgcLabel();

                    List<IGeometryData> hatchedAreasForEvent = new ArrayList<>(
                            geoMapUtilities.buildHazardAreaForEvent(event)
                                    .values());

                    /*
                     * Retrieve matching events from the Hazard Event Manager
                     * Also, include those from the session state.
                     */
                    HazardEventQueryRequest queryRequest = new HazardEventQueryRequest(
                            (CAVEMode.getMode()
                                    .equals(CAVEMode.OPERATIONAL) == false),
                            HazardConstants.HAZARD_EVENT_START_TIME, ">",
                            event.getStartTime())
                                    .and(HazardConstants.HAZARD_EVENT_END_TIME,
                                            "<", event.getEndTime())
                                    .and(HazardConstants.PHEN_SIG,
                                            hazardConflictList);
                    Set<HazardStatus> allowableStatuses = EnumSet
                            .of(HazardStatus.ISSUED, HazardStatus.ENDING);

                    List<IReadableHazardEvent> eventsToCheck = getEventsToCheckForConflicts(
                            queryRequest, allowableStatuses);

                    /*
                     * Loop over the existing events.
                     */
                    TimeRange modifiedEventTimeRange = new TimeRange(
                            event.getStartTime(), event.getEndTime());

                    for (IReadableHazardEvent eventToCheck : eventsToCheck) {

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
                            if (!eventToCheck.getEventID()
                                    .equals(event.getEventID())) {

                                String otherEventPhenSigSubtype = HazardEventUtilities
                                        .getHazardType(eventToCheck);

                                if (hazardConflictList
                                        .contains(otherEventPhenSigSubtype)) {

                                    HazardTypeEntry otherHazardTypeEntry = hazardTypes
                                            .get(otherEventPhenSigSubtype);
                                    String otherUgcLabel = otherHazardTypeEntry
                                            .getUgcLabel();

                                    if (hazardTypeEntry != null) {
                                        List<IGeometryData> hatchedAreasEventToCheck = new ArrayList<>(
                                                geoMapUtilities
                                                        .buildHazardAreaForEvent(
                                                                eventToCheck)
                                                        .values());

                                        conflictingHazardsMap
                                                .putAll(buildConflictMap(event,
                                                        eventToCheck,
                                                        hatchedAreasForEvent,
                                                        hatchedAreasEventToCheck,
                                                        ugcLabel,
                                                        otherUgcLabel));
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
     * @param queryRequest
     *            Query to be submitted to the hazard event manager to get the
     *            events.
     * @param allowableStatuses
     *            Allowable statuses of the events to be retrieved. Note that
     *            this cannot, unfortunately, be a part of the query, since
     *            making it so would mean that for a given event, the latest
     *            version would be returned from the hazard event manager that
     *            had one of the allowable statuses, even when the event had a
     *            newer version that had a disallowed status.
     * @return Events to be checked for conflict testing.
     * @throws HazardEventServiceException
     *             If a problem occurs while attempting to fetch the events to
     *             be checked.
     */
    private List<IReadableHazardEvent> getEventsToCheckForConflicts(
            final HazardEventQueryRequest queryRequest,
            Set<HazardStatus> allowableStatuses)
            throws HazardEventServiceException {

        /*
         * Iterate through all the events being managed in this session,
         * compiling a list of those that have not ended or elapsed, as well as
         * set of all these non-ended/elapsed event's identifiers.
         */
        List<IHazardEventView> sessionEventsToCheck = new ArrayList<>(
                getEvents());
        Set<String> eventIdentifiersToBeChecked = new HashSet<>(
                sessionEventsToCheck.size(), 1.0f);

        for (IHazardEventView sessionEventView : new ArrayList<>(
                sessionEventsToCheck)) {
            if ((sessionEventView.getStatus() != HazardStatus.ENDED)
                    && (sessionEventView.getStatus() != HazardStatus.ELAPSED)) {
                eventIdentifiersToBeChecked.add(sessionEventView.getEventID());
            } else {
                sessionEventsToCheck.remove(sessionEventView);
            }
        }

        List<IReadableHazardEvent> eventsToCheck = new ArrayList<>(
                sessionEventsToCheck.size());
        eventsToCheck.addAll(sessionEventsToCheck);

        /*
         * Retrieve the latest versions of matching events from the database
         * manager, and for any that are not ended or elapsed, with allowable
         * statuses, and with identifiers that are not already in session, add
         * them to the list of events to be checked.
         */
        queryRequest
                .setInclude(Include.LATEST_OR_MOST_RECENT_HISTORICAL_EVENTS);
        Map<String, HazardEvent> eventMap = dbManager.queryLatest(queryRequest);
        for (Map.Entry<String, HazardEvent> entry : eventMap.entrySet()) {
            if ((eventIdentifiersToBeChecked.contains(entry.getKey()) == false)
                    && (entry.getValue() != null)
                    && (entry.getValue().getStatus() != HazardStatus.ENDED)
                    && (entry.getValue().getStatus() != HazardStatus.ELAPSED)
                    && allowableStatuses
                            .contains(entry.getValue().getStatus())) {
                eventsToCheck.add(entry.getValue());
            }
        }

        return eventsToCheck;
    }

    @Override
    public void shutdown() {

        notificationSender
                .unregisterIntraNotificationHandler(eventsAddedHandler);
        notificationSender
                .unregisterIntraNotificationHandler(eventsRemovedHandler);
        notificationSender
                .unregisterIntraNotificationHandler(eventChangeHandler);
        notificationSender
                .unregisterIntraNotificationHandler(eventLockChangeHandler);
        notificationSender.unregisterIntraNotificationHandler(
                productGenerationCompletionHandler);
        notificationSender
                .unregisterIntraNotificationHandler(settingsChangeHandler);
        notificationSender
                .unregisterIntraNotificationHandler(currentTimeChangeHandler);

        /*
         * TODO: Decide whether expiration is to be done in Java code or not.
         * For now, this code is commented out; no expiration of events will
         * occur. This is not a permanent solution.
         */
        // eventExpirationTimer.cancel();
        // eventExpirationTimer = null;
        SimulatedTime.getSystemTime()
                .removeSimulatedTimeChangeListener(timeListener);
        messenger = null;
        shutDown = true;
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
    @SuppressWarnings("unchecked")
    private Map<IReadableHazardEvent, Collection<String>> buildConflictMap(
            IReadableHazardEvent firstEvent, IReadableHazardEvent secondEvent,
            List<IGeometryData> hatchedAreasFirstEvent,
            List<IGeometryData> hatchedAreasSecondEvent,
            String firstEventLabelParameter, String secondEventLabelParameter) {

        Map<IReadableHazardEvent, Collection<String>> conflictingHazardsMap = new HashMap<>();

        if (geoMapUtilities.isNonHatching(firstEvent)
                || geoMapUtilities.isNonHatching(secondEvent)) {
            return conflictingHazardsMap;
        }

        Set<String> forecastZoneSet = new HashSet<>();

        if (!geoMapUtilities.isWarngenHatching(firstEvent)
                && !geoMapUtilities.isWarngenHatching(secondEvent)) {

            Set<IGeometryData> commonHatchedAreas = new HashSet<>();
            commonHatchedAreas.addAll(hatchedAreasFirstEvent);
            commonHatchedAreas.retainAll(hatchedAreasSecondEvent);

            if (!commonHatchedAreas.isEmpty()) {
                HashMap<String, Serializable> firstHazardAreaMap = (HashMap<String, Serializable>) firstEvent
                        .getHazardAttribute(HAZARD_AREA);
                HashMap<String, Serializable> secondHazardAreaMap = (HashMap<String, Serializable>) secondEvent
                        .getHazardAttribute(HAZARD_AREA);
                forecastZoneSet.addAll(firstHazardAreaMap.keySet());
                forecastZoneSet.retainAll(secondHazardAreaMap.keySet());

                conflictingHazardsMap.put(secondEvent, forecastZoneSet);
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

                    if (hatchedArea.getGeometry()
                            .intersects(hatchedAreaToCheck.getGeometry())) {

                        conflictFound = true;

                        if (labelFieldName != null) {
                            HashMap<String, Serializable> hazardAreaMap = null;
                            if (geoWithLabelInfo == hatchedAreasFirstEvent) {
                                hazardAreaMap = (HashMap<String, Serializable>) firstEvent
                                        .getHazardAttribute(HAZARD_AREA);
                            } else {
                                hazardAreaMap = (HashMap<String, Serializable>) secondEvent
                                        .getHazardAttribute(HAZARD_AREA);
                            }

                            if ((hazardAreaMap != null)
                                    && (hazardAreaMap.isEmpty() == false)) {
                                forecastZoneSet.addAll(hazardAreaMap.keySet());
                            }
                        }
                    }
                }
            }

            if (conflictFound) {
                conflictingHazardsMap.put(secondEvent, forecastZoneSet);
            }

        }

        return conflictingHazardsMap;
    };

    /*
     * TODO: This method is probably not necessary since it does absolutely
     * nothing. It would also be elegant to remove it when endEvent() is removed
     * (see below).
     */
    @Override
    public void issueEvent(IHazardEventView event, IOriginator originator) {
    }

    @Override
    public EventPropertyChangeResult initiateEventEndingProcess(
            IHazardEventView event, IOriginator originator) {
        ObservedHazardEvent sessionEvent = getSessionEventForView(event);
        if (sessionEvent == null) {
            return EventPropertyChangeResult.FAILURE_DUE_TO_EVENT_NOT_FOUND;
        }
        if (event.getStatus() != HazardStatus.ISSUED) {
            return EventPropertyChangeResult.FAILURE_DUE_TO_BAD_VALUE;
        }

        /*
         * Lock the event, as ending status is an interim (modified) state.
         */
        if ((originator.isNotLockedByOthersRequired() == false)
                || (isEventNotLockedByOther(event)
                        && sessionManager.getLockManager()
                                .lockHazardEvent(event.getEventID()))) {
            sessionEvent.setStatus(HazardStatus.ENDING, false, originator);
            return EventPropertyChangeResult.SUCCESS;
        } else {
            showWarningMessageAboutLockedEvents("Cannot End", "ended",
                    originator, event.getEventID());
            return EventPropertyChangeResult.FAILURE_DUE_TO_LOCK_STATUS;
        }
    }

    @Override
    public EventPropertyChangeResult revertEventEndingProcess(
            IHazardEventView event, IOriginator originator) {
        ObservedHazardEvent sessionEvent = getSessionEventForView(event);
        if (sessionEvent == null) {
            return EventPropertyChangeResult.FAILURE_DUE_TO_EVENT_NOT_FOUND;
        }
        if (event.getStatus() != HazardStatus.ENDING) {
            return EventPropertyChangeResult.FAILURE_DUE_TO_BAD_VALUE;
        }
        if (originator.isNotLockedByOthersRequired()
                && isEventNotLockedByOther(event)) {
            sessionEvent.setStatus(HazardStatus.ISSUED, false, originator);
            return EventPropertyChangeResult.SUCCESS;
        } else {
            showWarningMessageAboutLockedEvents("Cannot Revert", "reverted",
                    originator, event.getEventID());
            return EventPropertyChangeResult.FAILURE_DUE_TO_LOCK_STATUS;
        }
    }

    /*
     * TODO: This method is probably not necessary. See the to-do comments
     * inside the method for details.
     */
    @Override
    public void endEvent(IHazardEventView event, IOriginator originator) {
        ObservedHazardEvent sessionEvent = getSessionEventForView(event);
        if (sessionEvent == null) {
            return;
        }
        sessionManager.startBatchedChanges();

        /*
         * TODO: Is this necessary?
         */
        sessionManager.getSelectionManager().removeEventFromSelectedEvents(
                event.getEventID(), Originator.OTHER);

        /*
         * TODO: Is this call necessary? This method is only called from
         * SessionProductManager.issue(), which already should, via the
         * mergeHazardEvents() call it makes a few lines up from the call to
         * this method, have changed the status to ended.
         */
        sessionEvent.setStatus(HazardStatus.ENDED, true, true, originator);
        sessionManager.finishBatchedChanges();
    }

    @Override
    public Set<String> getSelectedEventIdsAllowingProposal() {
        List<? extends IHazardEventView> selectedEventViews = sessionManager
                .getSelectionManager().getSelectedEvents();
        Set<String> set = new HashSet<String>(selectedEventViews.size());
        for (IHazardEventView eventView : selectedEventViews) {
            if (isProposedStateAllowed(eventView)) {
                set.add(eventView.getEventID());
            }
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public void setPotentialEventsToPending(
            Collection<? extends IHazardEventView> events) {
        for (IHazardEventView event : events) {
            if (event.getStatus() == HazardStatus.POTENTIAL) {
                ObservedHazardEvent sessionEvent = getSessionEventForView(
                        event);
                if (sessionEvent == null) {
                    continue;
                }
                sessionEvent.setStatus(HazardStatus.PENDING, false,
                        Originator.OTHER);
            }
        }
    }

    private EventPropertyChangeResult doProposeEvent(IHazardEventView event,
            IOriginator originator) {

        /*
         * Propose the event if allowed to do so. If it was already proposed,
         * the status setting will return false, and in that case, it must be
         * persisted manually (since no status change means it was not persisted
         * by the setStatus() invocation).
         */
        if (isProposedStateAllowed(event)) {
            ObservedHazardEvent sessionEvent = getSessionEventForView(event);
            if (sessionEvent == null) {
                return EventPropertyChangeResult.FAILURE_DUE_TO_EVENT_NOT_FOUND;
            }
            sessionEvent.setStatus(HazardStatus.PROPOSED, true, true,
                    originator);
            return EventPropertyChangeResult.SUCCESS;
        } else if (isEventNotLockedByOther(event)) {
            return EventPropertyChangeResult.FAILURE_DUE_TO_BAD_VALUE;
        } else {
            return EventPropertyChangeResult.FAILURE_DUE_TO_LOCK_STATUS;
        }
    }

    /**
     * If the specified origin of the specified abortive action is user input,
     * show the user a warning message about the specified events being unable
     * to have the specified action performed upon them because they were
     * locked.
     * 
     * @param title
     *            Title of the warning message.
     * @param abortedAction
     *            Action that could not be completed due to the lock status, in
     *            the form of a past-tense verb, such as "proposed".
     * @param originator
     *            Originator of the aborted action.
     * @param eventIdentifiers
     *            Identifiers of the events that were found to be locked.
     */
    private void showWarningMessageAboutLockedEvents(String title,
            String abortedAction, IOriginator originator,
            String... eventIdentifiers) {
        if ((originator.isDirectResultOfUserInput() == false)
                || (eventIdentifiers.length == 0)) {
            return;
        }
        if (eventIdentifiers.length == 1) {
            messenger.getWarner().warnUser(title,
                    "Event " + eventIdentifiers[0] + " could not be "
                            + abortedAction
                            + " because it has been locked by another user.");
        } else {
            messenger.getWarner().warnUser(title,
                    "The following events could not be " + abortedAction
                            + " because they were locked by other user(s):\n"
                            + Joiner.on("\n").join(eventIdentifiers));
        }
    }

    /**
     * If the specified origin of the specified abortive action is user input,
     * show the user a warning message about any specified events associated
     * with a failure due to lock status in the specified map.
     * 
     * @param title
     *            Title of the warning message.
     * @param abortedAction
     *            Action that could not be completed due to the lock status, in
     *            the form of a past-tense verb, such as "proposed".
     * @param originator
     *            Originator of the aborted action.
     * @param resultsForEventIdentifiers
     *            Map pairing identifiers of the events that had the action
     *            attempted with the results of those attempts.
     */
    private void showWarningAboutAnyEventIdentifierActionsAbortedDueToLocks(
            String title, String abortedAction, IOriginator originator,
            Map<String, EventPropertyChangeResult> resultsForEventIdentifiers) {

        /*
         * If at least one event could not be proposed to due the fact that it
         * was found to be locked, compile a list of all the events that were
         * found to be locked, and inform the user.
         */
        if (resultsForEventIdentifiers.containsValue(
                EventPropertyChangeResult.FAILURE_DUE_TO_LOCK_STATUS)) {
            List<String> identifiersOfLockedEvents = new ArrayList<>(
                    resultsForEventIdentifiers.size());
            for (Map.Entry<String, EventPropertyChangeResult> entry : resultsForEventIdentifiers
                    .entrySet()) {
                if (entry
                        .getValue() == EventPropertyChangeResult.FAILURE_DUE_TO_LOCK_STATUS) {
                    identifiersOfLockedEvents.add(entry.getKey());
                }
            }
            showWarningMessageAboutLockedEvents(title, abortedAction,
                    originator, identifiersOfLockedEvents.toArray(
                            new String[identifiersOfLockedEvents.size()]));
        }
    }

    @Override
    public EventPropertyChangeResult proposeEvent(IHazardEventView event,
            IOriginator originator) {
        EventPropertyChangeResult result = doProposeEvent(event, originator);
        if (result == EventPropertyChangeResult.FAILURE_DUE_TO_LOCK_STATUS) {
            showWarningMessageAboutLockedEvents("Cannot Propose", "proposed",
                    originator, event.getEventID());
        }
        return result;
    }

    @Override
    public Map<String, EventPropertyChangeResult> proposeEvents(
            Collection<? extends IHazardEventView> events,
            IOriginator originator) {

        /*
         * Attempt to propose each event, recording the result of each attempt.
         */
        sessionManager.startBatchedChanges();
        Map<String, EventPropertyChangeResult> resultsForEventIdentifiers = new HashMap<>(
                events.size(), 1.0f);
        for (IHazardEventView eventView : events) {
            resultsForEventIdentifiers.put(eventView.getEventID(),
                    doProposeEvent(eventView, originator));
        }
        sessionManager.finishBatchedChanges();

        showWarningAboutAnyEventIdentifierActionsAbortedDueToLocks(
                "Cannot Propose", "proposed", originator,
                resultsForEventIdentifiers);

        return resultsForEventIdentifiers;
    }

    @Override
    public boolean isProposedStateAllowed(IHazardEventView event) {
        return ((HazardStatus.hasEverBeenIssued(event.getStatus()) == false)
                && HazardEventUtilities.isHazardTypeValid(event)
                && isEventNotLockedByOther(event));
    }

    @Override
    public void breakEventLock(IHazardEventView event) {

        /*
         * Notify the user if there is no lock to break and do nothing more.
         */
        LockInfo info = sessionManager.getLockManager()
                .getHazardEventLockInfo(event.getEventID());
        if (info.getLockStatus() != LockStatus.LOCKED_BY_OTHER) {
            messenger.getWarner().warnUser("Cannot Break Lock",
                    (info.getLockStatus() == LockStatus.LOCKABLE
                            ? "There is no lock to break for hazard event "
                                    + event.getEventID() + "."
                            : "Cannot break the lock for hazard event "
                                    + event.getEventID()
                                    + " as it is held by you."));
            return;
        }

        /*
         * Get user confirmation, since breaking the lock has potentially
         * non-trivial consequences if done by mistake.
         */
        if (messenger.getQuestionAnswerer().getUserAnswerToQuestion(
                "Are you sure you want to break this hazard lock that is currently held by "
                        + info.getWorkstation().getUserName() + "?")) {
            if (sessionManager.getLockManager()
                    .breakHazardEventLock(event.getEventID()) == false) {
                messenger.getWarner().warnUser("Cannot Break Lock",
                        "Could not break the lock for event "
                                + event.getEventID() + ".");
            }
        }
    }

    @Override
    public void setHighResolutionGeometriesVisibleForSelectedEvents(
            IOriginator originator) {
        List<String> identifiersOfLockedEvents = new ArrayList<>();
        for (IHazardEventView event : sessionManager.getSelectionManager()
                .getSelectedEvents()) {
            if (isVisibleGeometrySetToHighResolution(event) == false) {
                if ((isEventInDatabase(event) == false) || sessionManager
                        .getLockManager().lockHazardEvent(event.getEventID())) {
                    makeHighResolutionVisible(getSessionEventForView(event),
                            originator);
                } else {
                    identifiersOfLockedEvents.add(event.getEventID());
                }
            }
        }
        showWarningMessageAboutLockedEvents("Cannot Modify", "modified",
                originator, identifiersOfLockedEvents
                        .toArray(new String[identifiersOfLockedEvents.size()]));
    }

    @Override
    public void setHighResolutionGeometryVisibleForCurrentEvent(
            IOriginator originator) {
        ObservedHazardEvent event = getSessionEventForView(getCurrentEvent());
        if ((event != null)
                && (isVisibleGeometrySetToHighResolution(event) == false)) {
            if ((isEventInDatabase(event) == false) || sessionManager
                    .getLockManager().lockHazardEvent(event.getEventID())) {
                makeHighResolutionVisible(event, originator);
            } else {
                showWarningMessageAboutLockedEvents("Cannot Modify", "modified",
                        originator, event.getEventID());
            }
        }
    }

    @Override
    public boolean setLowResolutionGeometriesVisibleForSelectedEvents(
            IOriginator originator) {
        List<String> identifiersOfLockedEvents = new ArrayList<>();
        boolean result = true;
        for (IHazardEventView event : sessionManager.getSelectionManager()
                .getSelectedEvents()) {
            Geometry lowResolutionGeometry;
            try {
                lowResolutionGeometry = buildLowResolutionEventGeometry(event);
            } catch (HazardGeometryOutsideCWAException e) {
                warnUserOfGeometryOutsideCwa(event);
                result = false;
                continue;
            }
            if (isVisibleGeometrySetToLowResolutionAndGeometry(event,
                    lowResolutionGeometry) == false) {
                if ((isEventInDatabase(event) == false) || sessionManager
                        .getLockManager().lockHazardEvent(event.getEventID())) {
                    setLowResolutionGeometry(getSessionEventForView(event),
                            lowResolutionGeometry, originator);
                } else {
                    identifiersOfLockedEvents.add(event.getEventID());
                    result = false;
                }
            }
        }
        showWarningMessageAboutLockedEvents("Cannot Modify", "modified",
                originator, identifiersOfLockedEvents
                        .toArray(new String[identifiersOfLockedEvents.size()]));
        return result;
    }

    @Override
    public boolean setLowResolutionGeometryVisibleForCurrentEvent(
            IOriginator originator) {
        IHazardEventView eventView = getCurrentEvent();
        ObservedHazardEvent event = getSessionEventForView(eventView);
        if (event != null) {
            Geometry lowResolutionGeometry;
            try {
                lowResolutionGeometry = buildLowResolutionEventGeometry(
                        eventView);
            } catch (HazardGeometryOutsideCWAException e) {
                warnUserOfGeometryOutsideCwa(eventView);
                return false;
            }
            if (isVisibleGeometrySetToLowResolutionAndGeometry(event,
                    lowResolutionGeometry) == false) {
                if ((isEventInDatabase(event) == false) || sessionManager
                        .getLockManager().lockHazardEvent(event.getEventID())) {
                    setLowResolutionGeometry(getSessionEventForView(eventView),
                            lowResolutionGeometry, originator);
                } else {
                    showWarningMessageAboutLockedEvents("Cannot Modify",
                            "modified", originator, event.getEventID());
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Update the hazard areas of the specified event.
     * 
     * @param event
     *            Event for which to update the areas.
     */
    protected void updateHazardAreas(IHazardEventView event) {
        if (event.getHazardType() != null) {
            getSessionEventForView(event).addHazardAttribute(HAZARD_AREA,
                    (Serializable) buildInitialHazardAreas(event));
        }
    }

    @Override
    public boolean canEventAreaBeChanged(IReadableHazardEvent event) {
        return ((event.getStatus() != HazardStatus.ELAPSED)
                && (event.getStatus() != HazardStatus.ENDED));
    }

    @Override
    public void setCurrentEvent(String eventId) {
        setCurrentEvent(eventId == null ? null : getEventById(eventId));
    }

    @Override
    public void setCurrentEvent(IHazardEventView event) {
        this.currentEvent = getSessionEventForView(event);
    }

    @Override
    public IHazardEventView getCurrentEvent() {
        return getViewForSessionEvent(currentEvent);
    }

    @Override
    public boolean isCurrentEvent() {
        return currentEvent != null;
    }

    @Override
    public boolean isEventHistorical(IHazardEventView event) {
        return (getSessionEventForView(event) == null);
    }

    @Override
    public boolean isEventChecked(IHazardEventView event) {
        for (IHazardEventView filteredEventView : getEventsForCurrentSettings()) {
            if (filteredEventView.getEventID().equals(event.getEventID())) {
                return checkedEventIdentifiers.contains(event.getEventID());
            }
        }
        return false;
    }

    @Override
    public boolean isEventModified(IHazardEventView event) {
        ObservedHazardEvent sessionEvent = getSessionEventForView(event);
        if (sessionEvent == null) {
            return false;
        }
        return sessionEvent.isModified();
    }

    @Override
    public void setEventChecked(IHazardEventView event, boolean checked,
            IOriginator originator) {
        String eventIdentifier = event.getEventID();
        boolean oldChecked = checkedEventIdentifiers.contains(eventIdentifier);
        if (oldChecked != checked) {
            if (checked) {
                checkedEventIdentifiers.add(eventIdentifier);
            } else {
                checkedEventIdentifiers.remove(eventIdentifier);
            }
            notificationSender.postNotificationAsync(
                    new SessionEventCheckedStateModified(this,
                            getEventById(eventIdentifier), Originator.OTHER));
        }
    }

    @Override
    public void updateSelectedHazardUgcs() {

        for (IHazardEventView hazardEventView : sessionManager
                .getSelectionManager().getSelectedEvents()) {
            String hazardType = HazardEventUtilities
                    .getHazardType(hazardEventView);

            if (hazardType != null) {
                List<String> ugcs = updateUgcs(hazardEventView);
                if (ugcs.isEmpty()) {
                    throw new ProductGenerationException(
                            "No UGCs included in hazard.  Check inclusions in HazardTypes.py");
                }
                ObservedHazardEvent hazardEvent = getSessionEventForView(
                        hazardEventView);
                if (hazardEvent != null) {
                    hazardEvent.addHazardAttribute(HazardConstants.UGCS,
                            (Serializable) ugcs);
                }
            }
        }
    }

    @Override
    public boolean isValidGeometryChange(IAdvancedGeometry geometry,
            IReadableHazardEvent event, boolean checkGeometryValidity,
            IOriginator originator) {
        if (checkGeometryValidity && (geometry.isValid() == false)) {
            statusHandler.warn("Ignoring geometry provided by " + originator
                    + ": Invalid geometry for event " + event.getEventID()
                    + ": " + geometry.getValidityProblemDescription());
            return false;
        } else if (isEventForThisSite(event) == false) {
            statusHandler.warn("Ignoring geometry provided by " + originator
                    + ": Event " + event.getEventID()
                    + " is not for the active site and cannot be modified.");
            return false;
        }

        /*
         * If the event requires a point identifier, ensure that the new
         * geometry contains a single point, and that said point is contained by
         * at least one of its non-point geometries.
         */
        if (configManager.isPointIdentifierRequired(event.getHazardType())) {
            List<Geometry> jtsGeometries = AdvancedGeometryUtilities
                    .getJtsGeometryList(geometry);
            Point point = null;
            Collection<Geometry> nonPoints = new HashSet<>(
                    jtsGeometries.size() - 1, 1.0f);
            for (Geometry jtsGeometry : jtsGeometries) {
                if (jtsGeometry instanceof Point) {
                    if (point != null) {
                        statusHandler.warn("Ignoring geometry provided by "
                                + originator + ": Event " + event.getEventID()
                                + " has more than one point geometry; geometry modification undone.");
                        return false;
                    }
                    point = (Point) jtsGeometry;
                } else {
                    nonPoints.add(jtsGeometry);
                }
            }
            if (point == null) {
                statusHandler.warn("Ignoring geometry provided by " + originator
                        + ": Event " + event.getEventID()
                        + " has no point geometry; geometry modification undone.");
                return false;
            }
            boolean pointContained = false;
            for (Geometry nonPoint : nonPoints) {
                if (nonPoint.contains(point)) {
                    pointContained = true;
                }
            }
            if (pointContained == false) {
                statusHandler.warn("Ignoring geometry provided by " + originator
                        + ": Event " + event.getEventID()
                        + " must have at least "
                        + "one areal geometry containing its point; geometry modification undone.");
                return false;
            }
        }

        /*
         * If the hazard has been issued and does not allow area change, ensure
         * that the new geometry is no larger than the previous one.
         */
        if (hasEverBeenIssued(event)) {
            HazardTypeEntry hazardTypeEntry = configManager.getHazardTypes()
                    .get(HazardEventUtilities.getHazardType(event));
            if ((hazardTypeEntry != null)
                    && (hazardTypeEntry.isAllowAreaChange() == false)) {
                Geometry issuedGeometry = AdvancedGeometryUtilities
                        .getUnionOfGeometryElements(
                                event.getFlattenedGeometry(),
                                AdvancedGeometryUtilities.GeometryTypesForUnion.POLYGONAL)
                        .buffer(GEOMETRY_BUFFER_DISTANCE);
                Geometry currentGeometry = AdvancedGeometryUtilities
                        .getUnionOfGeometryElements(
                                AdvancedGeometryUtilities
                                        .getJtsGeometry(geometry),
                                AdvancedGeometryUtilities.GeometryTypesForUnion.POLYGONAL);
                if (issuedGeometry.covers(currentGeometry) == false) {
                    statusHandler.warn("Ignoring geometry provided by "
                            + originator
                            + ": This hazard event cannot be expanded in area.  "
                            + "Please create a new hazard event for the new areas.");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Map<String, String> buildInitialHazardAreas(
            IReadableHazardEvent event) {
        if (geoMapUtilities.isNonHatching(event)) {
            return Collections.emptyMap();
        }
        List<String> ugcs = buildUgcs(event);
        String hazardArea;
        if (geoMapUtilities.isWarngenHatching(event)) {
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

    @Override
    public void addOrRemoveEnclosingUgcs(Coordinate location,
            IOriginator originator) {
        List<? extends IHazardEventView> selectedEventViews = sessionManager
                .getSelectionManager().getSelectedEvents();
        if (selectedEventViews.size() != 1) {

            /*
             * TODO: This message is annoying to have pop up all the time when
             * one accidentally right-clicks. Explore having it pop up only when
             * the user has two or more selected events, not when there are no
             * events selected. Would that be better from a UX perspective?
             */
            messenger.getWarner().warnUser(GEOMETRY_MODIFICATION_ERROR,
                    "Cannot add or remove UGCs unless exactly one hazard event is selected.");
            return;
        }
        IHazardEventView hazardEventView = selectedEventViews.get(0);
        String hazardType = hazardEventView.getHazardType();
        if (hazardType == null) {
            messenger.getWarner().warnUser(GEOMETRY_MODIFICATION_ERROR,
                    "Cannot add or remove UGCs for a hazard with an undefined type.");
            return;
        }
        if (HazardStatus.endingEndedOrElapsed(hazardEventView.getStatus())) {
            messenger.getWarner().warnUser(GEOMETRY_MODIFICATION_ERROR,
                    "Cannot add or remove UGCs for an ending, ended, or elapsed hazard.");
            return;
        }

        if ((hazardEventView.getStatus().equals(HazardStatus.PENDING) == false)
                && geoMapUtilities.isPointBasedHatching(hazardEventView)) {
            messenger.getWarner().warnUser(GEOMETRY_MODIFICATION_ERROR,
                    "Can only add or remove UGCs for point hazards when they are pending.");
            return;
        }

        /*
         * Ensure that the event is for this site.
         */
        if (isEventForThisSite(hazardEventView) == false) {
            statusHandler.error("Event " + hazardEventView.getEventID()
                    + " is not for the active site and cannot be modified.");
            return;
        }

        if (userConfirmationAsNecessary(hazardEventView) == false) {
            return;
        }

        if (isEventInDatabase(hazardEventView)
                && (sessionManager.getLockManager().lockHazardEvent(
                        hazardEventView.getEventID()) == false)) {
            showWarningMessageAboutLockedEvents("Cannot Modify", "modified",
                    originator, hazardEventView.getEventID());
            return;
        }
        ObservedHazardEvent event = getSessionEventForView(hazardEventView);
        makeHighResolutionVisible(event, originator);

        /*
         * Get the modified hazard areas and geometry resulting from adding or
         * removing UGCs enclosing this location; if nothing is returned, an
         * error occurred during the toggling, so do nothing more.
         */
        Pair<Map<String, String>, Geometry> newHazardAreasAndGeometry = geoMapUtilities
                .addOrRemoveEnclosingUgcs(hazardEventView, location);
        if (newHazardAreasAndGeometry == null) {
            return;
        }

        /*
         * If a modified geometry was returned, ensure the changed geometry is
         * valid. If it is, use it as the event's geometry.
         */
        Geometry modifiedGeometry = newHazardAreasAndGeometry.getSecond();
        if (modifiedGeometry != null) {
            if (isValidGeometryChange(
                    AdvancedGeometryUtilities
                            .createGeometryWrapper(modifiedGeometry, 0),
                    hazardEventView, true, originator) == false) {
                return;
            }
            event.setGeometry(AdvancedGeometryUtilities
                    .createGeometryWrapper(modifiedGeometry, 0), originator);
        }

        /*
         * Use the returned hazard areas as the new ones for the hazard event.
         */
        event.addHazardAttribute(HAZARD_AREA,
                (Serializable) newHazardAreasAndGeometry.getFirst(), true,
                originator);
    }

    private class HazardGeometryOutsideCWAException extends RuntimeException {

        private static final long serialVersionUID = -3178272501617218427L;

    }

    private Geometry buildLowResolutionEventGeometry(
            IHazardEventView eventView) {

        /*
         * Get the hazard type.
         */
        HazardTypes hazardTypes = configManager.getHazardTypes();
        HazardTypeEntry hazardType = hazardTypes.get(eventView.getHazardType());

        /*
         * By default, just set the low-resolution geometry to the be the
         * original flattened.
         */
        Geometry result = eventView.getFlattenedGeometry();

        /*
         * No clipping if no low-resolution computation is needed, or for
         * National and for non-hatching.
         */
        if ((hazardType != null)
                && (configManager.getSiteID()
                        .equals(HazardConstants.NATIONAL) == false)
                && (geoMapUtilities.isNonHatching(eventView) == false)) {

            /*
             * Clip WarnGen style or GFE style if appropriate.
             */
            if (geoMapUtilities.isWarngenHatching(eventView)) {
                result = geoMapUtilities.applyWarngenClipping(eventView,
                        hazardType);
            } else if (geoMapUtilities
                    .isPointBasedHatching(eventView) == false) {
                result = geoMapUtilities.applyGfeClipping(eventView);
            }
            result = (Geometry) result.clone();

            /*
             * If a reasonable point limit was found, reduce the geometry.
             */
            if (hazardType != null) {
                Integer max = hazardType.getHazardPointLimit();
                if (max != null) {
                    result = WarnGenPolygonSimplifier.reduceGeometry(result,
                            max);
                    if (result.isEmpty() == false) {
                        result = addGoosenecksAsNecessary(result);
                    }
                }
            }

            /*
             * If the result is an empty geometry, an error has occurred.
             */
            if (result.isEmpty()) {
                throw new HazardGeometryOutsideCWAException();
            }

            /*
             * Convert the result to a collection if it is not already.
             */
            if (result instanceof GeometryCollection == false) {
                result = geometryFactory
                        .createGeometryCollection(new Geometry[] { result });
            }
        }

        return result;
    }

    private void warnUserOfGeometryOutsideCwa(IHazardEventView eventView) {
        StringBuffer warningMessage = new StringBuffer();
        warningMessage.append("Event ").append(eventView.getEventID())
                .append(" ");
        warningMessage
                .append("has no hazard areas inside of the forecast area.\n");
        messenger.getWarner().warnUser("Product geometry calculation error",
                warningMessage.toString());
    }

    private boolean isVisibleGeometrySetToLowResolutionAndGeometry(
            IReadableHazardEvent hazardEvent, Geometry lowResolutionGeometry) {
        return (LOW_RESOLUTION_GEOMETRY_IS_VISIBLE
                .equals(hazardEvent.getHazardAttribute(VISIBLE_GEOMETRY))
                && lowResolutionGeometry.equals(hazardEvent
                        .getHazardAttribute(LOW_RESOLUTION_GEOMETRY)));
    }

    private void setLowResolutionGeometry(ObservedHazardEvent selectedEvent,
            Geometry lowResolutionGeometry, IOriginator originator) {
        selectedEvent.addHazardAttribute(VISIBLE_GEOMETRY,
                LOW_RESOLUTION_GEOMETRY_IS_VISIBLE, originator);
        selectedEvent.addHazardAttribute(LOW_RESOLUTION_GEOMETRY,
                lowResolutionGeometry, originator);
    }

    private boolean isVisibleGeometrySetToHighResolution(
            IReadableHazardEvent hazardEvent) {
        return HIGH_RESOLUTION_GEOMETRY_IS_VISIBLE
                .equals(hazardEvent.getHazardAttribute(VISIBLE_GEOMETRY));
    }

    private void makeHighResolutionVisible(ObservedHazardEvent hazardEvent,
            IOriginator originator) {
        hazardEvent.addHazardAttribute(VISIBLE_GEOMETRY,
                HIGH_RESOLUTION_GEOMETRY_IS_VISIBLE, originator);
    }

    private List<String> buildUgcs(IReadableHazardEvent event) {
        if (geoMapUtilities.isPointBasedHatching(event)) {
            return buildFromDbStrategyUgcs(event);
        } else {
            return buildIntersectionStrategyUgcs(event);
        }
    }

    private List<String> updateUgcs(IHazardEventView eventView) {
        if (geoMapUtilities.isPointBasedHatching(eventView)) {
            return buildPointBasedStrategyUgcs(eventView);
        } else {
            return buildIntersectionStrategyUgcs(eventView);
        }
    }

    private List<String> buildPointBasedStrategyUgcs(
            IHazardEventView hazardEvent) {
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

    private List<String> buildFromDbStrategyUgcs(
            IReadableHazardEvent hazardEvent) {
        List<String> result = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Map<String, Serializable> forecastPoint = (Map<String, Serializable>) hazardEvent
                .getHazardAttribute(FORECAST_POINT);
        String hazardEventPointID = (String) forecastPoint.get(POINT_ID);

        // Determine if county or zone
        HazardTypes hazardTypes = sessionManager.getConfigurationManager()
                .getHazardTypes();
        if (hazardTypes != null) {

            /*
             * If the UGC types include zone, handle the building one way;
             * otherwise, handle it another.
             * 
             * TODO: Is this right, assuming that the first way should be used
             * if UGC types contains "zone"? What if UGC types includes both
             * "zone" and non-"zone" types?
             */
            Set<String> ugcTypes = hazardTypes
                    .get(HazardEventUtilities.getHazardType(hazardEvent))
                    .getUgcTypes();
            if (ugcTypes.contains(HazardConstants.UGC_ZONE)) {
                List<RiverPointZoneInfo> riverPointZoneInfoList = riverForecastManager
                        .getRiverForecastPointRiverZoneInfo(hazardEventPointID);
                // There could be multiple zones/ugcs
                for (RiverPointZoneInfo data : riverPointZoneInfoList) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(data.getState());
                    sb.append(HazardConstants.UGC_ZONE_ABBREVIATION);
                    sb.append(data.getZoneNum());
                    result.add(sb.toString());
                }
            } else {
                List<CountyStateData> countyDataList = riverForecastManager
                        .getRiverForecastPointCountyStateList(
                                hazardEventPointID);

                // There could be multiple counties/ugcs
                for (CountyStateData data : countyDataList) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(data.getState())
                            .append(HazardConstants.UGC_COUNTY_ABBREVIATION)
                            .append(data.getCountyNum());
                    result.add(sb.toString());
                }
            }
        }
        return result;
    }

    private List<String> buildIntersectionStrategyUgcs(
            IReadableHazardEvent hazardEvent) {
        return new ArrayList<>(geoMapUtilities
                .getIntersectingMapGeometriesForUgcs(hazardEvent).keySet());
    }

    private Geometry addGoosenecksAsNecessary(Geometry productGeometry) {
        if ((!(productGeometry instanceof GeometryCollection))
                || (productGeometry.getNumGeometries() == 0)) {
            return productGeometry;
        }
        GeometryCollection asMultiPolygon = (GeometryCollection) productGeometry;
        Geometry[] geometries = new Geometry[2
                * asMultiPolygon.getNumGeometries() - 1];

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

    @Override
    public Map<String, EventPropertyChangeResult> saveEvents(
            List<? extends IHazardEventView> events, boolean addToHistory,
            boolean keepLocked, boolean treatAsIssuance,
            IOriginator originator) {

        /*
         * TODO: Remove this when "persistenceBehavior" is removed from startup
         * config, once testing and debugging yield the correct solution to
         * handling persistence.
         * 
         * If no persistence is desired, do nothing; otherwise, if only history
         * list persistesnce is desired, remember this for later.
         */
        boolean forceHistorical = false;
        String persistenceBehavior = configManager.getStartUpConfig()
                .getPersistenceBehavior();
        if ("none".equals(persistenceBehavior)) {
            return Collections.emptyMap();
        } else if ("history".equals(persistenceBehavior)) {
            forceHistorical = true;
        }

        /*
         * Get copies of the events to be saved that are database-friendly.
         */
        List<HazardEvent> dbEvents = new ArrayList<>(events.size());
        Map<HazardEvent, IHazardEventView> viewsForDbEvents = new IdentityHashMap<>(
                events.size());
        Set<String> eventIdentifiers = new HashSet<>(events.size(), 1.0f);
        Map<String, EventPropertyChangeResult> resultsForEventIdentifiers = new HashMap<>(
                events.size(), 1.0f);
        for (IHazardEventView eventView : events) {

            /*
             * Ensure the event is being managed.
             */
            ObservedHazardEvent event = getSessionEventForView(eventView);
            if (event == null) {
                resultsForEventIdentifiers.put(eventView.getEventID(),
                        EventPropertyChangeResult.FAILURE_DUE_TO_EVENT_NOT_FOUND);
                continue;
            }

            /*
             * If the event has a status of "potential", log an error and skip
             * it.
             */
            if (HazardStatus.POTENTIAL.equals(eventView.getStatus())) {
                statusHandler.warn("Attempted to save hazard event "
                        + eventView.getEventID() + " to database, but "
                        + "cannot due to its status of \"potential\".");
                resultsForEventIdentifiers.put(eventView.getEventID(),
                        EventPropertyChangeResult.FAILURE_DUE_TO_BAD_VALUE);
                continue;
            }

            /*
             * If the save is to be treated as an issuance, reset the modified
             * flags of the event.
             */
            if (treatAsIssuance) {
                event.setModified(false);
            }

            /*
             * Make a copy of the event of the right type, and strip out
             * whatever cannot or should not be saved. Also ensure that it is
             * added to the history list if such is asked for.
             * 
             * TODO: Remove the "|| forceHistorical" from here once true save
             * latest version capability is restored.
             */
            HazardEvent persistableEvent = createEventCopyToBePersisted(event,
                    (addToHistory || forceHistorical), false);
            dbEvents.add(persistableEvent);
            viewsForDbEvents.put(persistableEvent, eventView);
            eventIdentifiers.add(persistableEvent.getEventID());
        }

        /*
         * TODO: Remove this once latest event version saving is possible again.
         * 
         * Add the historical attribute to the hazard event copies that are to
         * be persisted if they really are meant to be historical.
         */
        if (addToHistory) {
            for (HazardEvent event : dbEvents) {
                event.addHazardAttribute(HazardEventManager.HISTORICAL, true);
            }
        } else {
            for (HazardEvent event : dbEvents) {
                event.removeHazardAttribute(HazardEventManager.HISTORICAL);
            }
        }

        /*
         * Save the events to the database, deleting the latest versions of the
         * same events if the events to be persisted are being added to their
         * respective history lists. Note that there is no need to remove the
         * latest version from the map at this point, since it will be removed
         * when the notification of the deletion arrives.
         */
        if (dbEvents.isEmpty() == false) {
            if (addToHistory) {
                List<HazardEvent> eventsToBeRemoved = new ArrayList<>(
                        dbEvents.size());
                for (HazardEvent dbEvent : dbEvents) {
                    HazardEvent latestVersion = latestVersionsFromDatabaseForEventIdentifiers
                            .get(dbEvent.getEventID());
                    if (latestVersion != null) {
                        eventsToBeRemoved.add(latestVersion);
                    }
                }
                if (eventsToBeRemoved.isEmpty() == false) {
                    dbManager.removeEvents(eventsToBeRemoved);
                }
            }
            dbManager.storeEvents(dbEvents);
            for (HazardEvent event : dbEvents) {
                resultsForEventIdentifiers.put(event.getEventID(),
                        (isEventNotLockedByOther(viewsForDbEvents.get(event))
                                ? EventPropertyChangeResult.SUCCESS
                                : EventPropertyChangeResult.FAILURE_DUE_TO_LOCK_STATUS));
                identifiersOfPersistedEvents.add(event.getEventID());
            }
        }
        showWarningAboutAnyEventIdentifierActionsAbortedDueToLocks(
                "Cannot Save", "saved", originator, resultsForEventIdentifiers);

        /*
         * If adding to the history list or if the keep-locked flag is false,
         * unlock the hazard events just saved. Otherwise, make sure all events
         * that are to be kept locked are already locked, and lock any that are
         * not.
         */
        if (addToHistory || (keepLocked == false)) {
            boolean atLeastOneNeedsUnlocking = false;
            for (String eventIdentifier : eventIdentifiers) {
                if (sessionManager.getLockManager().getHazardEventLockStatus(
                        eventIdentifier) == LockStatus.LOCKED_BY_ME) {
                    atLeastOneNeedsUnlocking = true;
                    break;
                }
            }
            if (atLeastOneNeedsUnlocking && (sessionManager.getLockManager()
                    .unlockHazardEvents(eventIdentifiers) == false)) {
                if (originator.isDirectResultOfUserInput()) {
                    messenger.getWarner().warnUser("Cannot Unlock",
                            (eventIdentifiers.size() == 1
                                    ? "Event "
                                            + eventIdentifiers.iterator().next()
                                            + " could not be unlocked."
                                    : "The following events could not be unlocked:\n"
                                            + Joiner.on("\n")
                                                    .join(eventIdentifiers)));
                }
            }
        } else {
            Set<String> eventIdentifiersToBeLocked = new HashSet<>(
                    eventIdentifiers.size(), 1.0f);
            for (String eventIdentifier : eventIdentifiers) {
                if (sessionManager.getLockManager().getHazardEventLockStatus(
                        eventIdentifier) == LockStatus.LOCKABLE) {
                    eventIdentifiersToBeLocked.add(eventIdentifier);
                }
            }
            if (eventIdentifiersToBeLocked.isEmpty() == false) {
                if (sessionManager.getLockManager().lockHazardEvents(
                        eventIdentifiersToBeLocked) == false) {
                    if (originator.isDirectResultOfUserInput()) {
                        messenger.getWarner().warnUser("Cannot Lock",
                                (eventIdentifiersToBeLocked.size() == 1
                                        ? "Event " + eventIdentifiersToBeLocked
                                                .iterator().next()
                                                + " could not be re-locked."
                                        : "The following events could not be re-locked:\n"
                                                + Joiner.on("\n").join(
                                                        eventIdentifiersToBeLocked)));
                    }
                }
            }
        }

        return resultsForEventIdentifiers;
    }

    @Override
    public void copyEvents(List<? extends IHazardEventView> events) {

        sessionManager.startBatchedChanges();

        /*
         * Create a copy of each of the events.
         */
        Set<String> eventIdentifiers = new HashSet<>(events.size(), 1.0f);
        for (IHazardEventView eventView : events) {

            /*
             * Create a copy of the event, with no identifier, type, or visual
             * features, with pending status, and with the current client's user
             * name and workstation.
             */
            BaseHazardEvent newEvent = new BaseHazardEvent(eventView);
            newEvent.setEventID(null);
            newEvent.setStatus(HazardStatus.PENDING);
            newEvent.setPhenomenon(null);
            newEvent.setSignificance(null);
            newEvent.setSubType(null);
            newEvent.setVisualFeatures(null);
            newEvent.setWsId(VizApp.getWsId());

            /*
             * Remove any type-specifc session attributes from the copy.
             */
            for (String sessionAttribute : configManager
                    .getSessionAttributes(eventView.getHazardType())) {
                newEvent.removeHazardAttribute(sessionAttribute);
            }

            /*
             * New event should not have product information.
             */
            newEvent.removeHazardAttribute(HazardConstants.EXPIRATION_TIME);
            newEvent.removeHazardAttribute(HazardConstants.ISSUE_TIME);
            newEvent.removeHazardAttribute(HazardConstants.VTEC_CODES);
            newEvent.removeHazardAttribute(HazardConstants.ETNS);
            newEvent.removeHazardAttribute(HazardConstants.PILS);
            newEvent.removeHazardAttribute(HazardConstants.REPLACED_BY);
            newEvent.removeHazardAttribute(HazardConstants.REPLACES);
            newEvent.removeHazardAttribute(
                    HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE);
            newEvent.removeHazardAttribute(
                    HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);

            removeAddRemoveShapesContextMenuOption(newEvent);

            try {
                eventIdentifiers.add(addEvent(newEvent, false, Originator.OTHER)
                        .getEventID());
            } catch (HazardEventServiceException e) {
                statusHandler.error("Error attempting to copy hazard event "
                        + eventView.getEventID() + ".", e);
            }
        }

        /*
         * Select the created events.
         */
        sessionManager.getSelectionManager().setSelectedEventIdentifiers(
                eventIdentifiers, Originator.OTHER);

        sessionManager.finishBatchedChanges();
    }

    @Override
    public EventPropertyChangeResult revertEventToLastSaved(String identifier,
            IOriginator originator) {
        IHazardEventView event = getEventById(identifier);
        if (event == null) {
            return EventPropertyChangeResult.FAILURE_DUE_TO_EVENT_NOT_FOUND;
        }
        if (originator.isNotLockedByOthersRequired()
                && (sessionManager.getLockManager().getHazardEventLockStatus(
                        identifier) != LockStatus.LOCKED_BY_ME)) {
            if (originator.isDirectResultOfUserInput()) {
                messenger.getWarner().warnUser("Cannot Revert",
                        "Event " + identifier
                                + " could not be reverted to last saved because "
                                + "you do not have a lock on it.");
            }
            return EventPropertyChangeResult.FAILURE_DUE_TO_LOCK_STATUS;
        }
        if (getHistoricalVersionCountForEvent(identifier) > 0) {
            List<IHazardEventView> list = getEventHistoryById(identifier);
            mergeHazardEvents(list.get(list.size() - 1), event, false, false,
                    true, (originator.isDirectResultOfUserInput()
                            ? RevertOriginator.USER : RevertOriginator.OTHER));
            if (sessionManager.getLockManager()
                    .unlockHazardEvent(identifier) == false) {
                if (originator.isDirectResultOfUserInput()) {
                    messenger.getWarner().warnUser("Cannot Unlock",
                            "Cannot unlock event " + identifier
                                    + "after reverting to last saved.");
                    return EventPropertyChangeResult.FAILURE_DUE_TO_LOCK_STATUS;
                }
            }
            return EventPropertyChangeResult.SUCCESS;
        } else {
            return EventPropertyChangeResult.FAILURE_DUE_TO_BAD_VALUE;
        }
    }

    @Override
    public boolean isEventInDatabase(IReadableHazardEvent event) {
        return identifiersOfPersistedEvents.contains(event.getEventID());
    }

    /**
     * Create a persistence-friendly copy of the specified event.
     * 
     * @param event
     *            Event to be copied.
     * @param addToHistory
     *            Flag indicating whether or not the copy is intended for
     *            addition to the history list for the hazard.
     * @param justIssued
     *            Flag indicating whether or not the copy is to be saved because
     *            the event was just issued.
     * @return Persistence-friendly copy of the event.
     */
    private HazardEvent createEventCopyToBePersisted(IReadableHazardEvent event,
            boolean addToHistory, boolean justIssued) {
        HazardEvent dbEvent = dbManager.createEvent(event);

        /*
         * Strip out attributes as per this hazard type's configuration.
         */
        for (String sessionAttribute : configManager
                .getSessionAttributes(event.getHazardType())) {
            dbEvent.removeHazardAttribute(sessionAttribute);
        }

        /*
         * Strip out other hard-coded attributes that should not be persisted.
         * 
         * TODO: The fact that the Java code is worrying about hazard attributes
         * like this, and is using them instead of just keeping them in their
         * map for the focal-point-configurable Python side to use, is not a
         * good thing.
         * 
         * The map of hazard attributes that is part of each hazard event is
         * meant to store attributes that only recommenders, metadata
         * generators, product generators, and other tools use. The Java core's
         * job with these generic attributes is to simply ensure they are kept
         * associated with their hazard event, not to use them to store data of
         * semantic value to the Java code.
         * 
         * Any attributes currently used by Java code should either be promoted
         * to first-class fields within the hazard event, or otherwise removed.
         */
        if (justIssued == false) {
            dbEvent.removeHazardAttribute(HazardConstants.ISSUED);
        }
        dbEvent.removeHazardAttribute(HazardConstants.HAZARD_EVENT_CATEGORY);

        /*
         * TODO: Remove this once the HAZARD_EVENT_SELECTED attribute has been
         * entirely done away with.
         */
        dbEvent.removeHazardAttribute(HAZARD_EVENT_SELECTED);

        /*
         * Weed out any visual features that are not to be persisted.
         */
        VisualFeaturesList visualFeatures = dbEvent.getVisualFeatures();
        if (visualFeatures != null) {
            for (Iterator<VisualFeature> iterator = visualFeatures
                    .iterator(); iterator.hasNext();) {
                VisualFeature visualFeature = iterator.next();
                if (visualFeature.isPersist() == false) {
                    iterator.remove();
                }
            }
        }

        /*
         * If the copy is not intended to be added to the history list, mark it
         * as a latest version.
         */
        if (addToHistory == false) {
            dbEvent.setLatestVersion();
        }

        return dbEvent;
    }

    @Override
    public void setAddCreatedEventsToSelected(
            boolean addCreatedEventsToSelected) {
        this.addCreatedEventsToSelected = addCreatedEventsToSelected;
    }

    @Override
    public Geometry getCwaGeometry() {
        return geoMapUtilities.getCwaGeometry();
    }

    @Override
    public void clearCwaGeometry() {
        geoMapUtilities.clearCWAGeometry();
    }

    /*
     * TODO: Remove once garbage collection is sorted out; see Redmine issue
     * #21271.
     */
    @Override
    @Deprecated
    public boolean isShutDown() {
        return shutDown;
    }

    /**
     *
     * @param event
     * @return True if the event's site ID matches the session's current site
     *         ID, false otherwise
     */
    private boolean isEventForThisSite(IReadableHazardEvent event) {
        return event.getSiteID().equals(configManager.getSiteID());
    }
}
