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
import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.time.DateUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardEventFirstClassAttribute;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.RecommenderTriggerOrigin;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.Significance;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Include;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardServicesEventIdUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.VisvalingamWhyattSimplifier;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.query.HazardEventQueryRequest;
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
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.HazardEventMetadata;
import com.raytheon.uf.viz.hazards.sessionmanager.config.IEventModifyingScriptJobListener;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ModifiedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorEntry;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAllowUntilFurtherNoticeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAttributesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventCheckedStateModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventGeometryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventHistoryModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventMetadataModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventRemoved;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventScriptExtraDataAvailable;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTimeRangeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventTypeModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventUnsavedChangesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventVisualFeaturesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsOrderingModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsTimeRangeBoundariesModified;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionSelectedEventConflictsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.geomaps.GeoMapUtilities;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger.IEventApplier;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger.IQuestionAnswerer;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger.IRiseCrestFallEditor;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.RecommenderOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;
import com.raytheon.uf.viz.hazards.sessionmanager.time.CurrentTimeMinuteTicked;
import com.raytheon.uf.viz.hazards.sessionmanager.time.CurrentTimeReset;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.viz.core.mode.CAVEMode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Implementation of ISessionEventManager
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
 * Sep 04, 2015    7514    Chris.Golden Fixed bug introduced by July 6th check-in for this issue that caused
 *                                      exceptions to be thrown when upgrading certain watches to warnings.
 * Sep 09  2015   10207    Chris.Cody   Switched Polygon Point reduction to use Visvalingam-Whyatt algorithm.
 * Sep 15, 2015    7629    Robert.Blum  Changes for saving pending hazards.
 * Oct 14, 2015   12494    Chris Golden Reworked to allow hazard types to include only phenomenon (i.e. no
 *                                      significance) where appropriate.
 * Sep 28, 2015 10302,8167 hansen       Added calls to "getSettingsValue"
 * Oct 01, 2015    7629    Robert.Blum  Fixing bug from first commit that allowed event to not be assigned an
 *                                      event ID.
 * Nov 10, 2015   12762    Chris.Golden Added recommender running in response to hazard event metadata or
 *                                      other attribute changes.
 * Jan 28, 2016   12762    Chris.Golden Changed to only run recommender a single time in response to multiple
 *                                      trigger-type hazard event attributes changing simultaneously, and also
 *                                      added the fetching of metadata specifiers for an event when the latter
 *                                      is added, not later on when the HID comes up (the latter behavior was
 *                                      a bug).
 * Feb 10, 2016   15561    Chris.Golden Removed hard-coded UGC that had been put in for testing.
 * Feb 10, 2016   13279    Chris.Golden Fixed bugs in calculation of new end time when a replacement event
 *                                      uses duration-based end times and the original event does not.
 * Mar 03, 2016   14004    Chris.Golden Changed to pass originator when merging hazard events, and to
 *                                      only run event-triggered recommenders when they are not triggered
 *                                      by modifications to events caused by those same recommenders.
 * Mar 06, 2016   15676    Chris.Golden Added specification of origin ("user" or "other") to recommender
 *                                      execution context, so that recommenders know when they are triggered
 *                                      by a hazard event modification whether the user made the change or
 *                                      not.
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
 * Apr 23, 2016   18094    Chris.Golden Added code to ensure VTEC check does not throw exception if the event
 *                                      has no VTEC code (e.g. a PHI event).
 * Apr 28, 2016   18267    Chris.Golden Added support for unrestricted event start times. Also cleaned up and
 *                                      simplified the code handling an event's time range boundaries when
 *                                      said event is issued.
 * May 04, 2016   18411    Chris.Golden Changed to persist reissued hazard events to the database.
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
 * Nov 14, 2016   21675    Chris.Golden Fixed bug introduced in previous commit that caused hazard
 *                                      attribute changes that may trigger a metadata reload, but
 *                                      (correctly) do not end up doing so, to not be checked to see if
 *                                      they may instead trigger a recommender execution.
 * Nov 17, 2016   26313    Chris.Golden Changed to support multiple UGC types per hazard type, and to
 *                                      work with revamped GeoMapUtilities, moving some code that was
 *                                      previously here into that class as it was poorly placed.
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
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SessionEventManager implements
        ISessionEventManager<ObservedHazardEvent> {

    // Private Static Constants

    private static final Set<String> ATTRIBUTES_TO_RETAIN_ON_MERGE = ImmutableSet
            .of(ISessionEventManager.ATTR_ISSUED, ISSUE_TIME);

    private static final String POINT_ID = "id";

    private static final String GEOMETRY_MODIFICATION_ERROR = "Geometry Modification Error";

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SessionEventManager.class);

    // Private Variables

    private final ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager;

    private final ISessionTimeManager timeManager;

    private final ISessionConfigurationManager<ObservedSettings> configManager;

    private final IHazardEventManager dbManager;

    private final ISessionNotificationSender notificationSender;

    private final List<ObservedHazardEvent> events = new ArrayList<>();

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
     * Map pairing event identifiers for all the {@link #events} with historical
     * versions with the number of historical versions. If an event identifier
     * has no associated value within this map, the event has no historical
     * versions.
     */
    private final Map<String, Integer> historicalVersionCountsForEventIdentifiers = new HashMap<>();

    /**
     * Set of identifiers for all events that are currently checked.
     */
    private final Set<String> checkedEventIdentifiers = new HashSet<>();

    /**
     * Set of identifiers for all events that have "Ending" status.
     */
    private final Set<String> eventIdentifiersWithEndingStatus = new HashSet<>();

    private final Map<String, Collection<IHazardEvent>> conflictingEventsForSelectedEventIdentifiers = new HashMap<>();

    private final Map<String, MegawidgetSpecifierManager> megawidgetSpecifiersForEventIdentifiers = new HashMap<>();

    private final Map<String, Set<String>> metadataReloadTriggeringIdentifiersForEventIdentifiers = new HashMap<>();

    private final Map<String, Set<String>> metadataIdentifiersAffectingModifyFlagsForEventIdentifiers = new HashMap<>();

    private final Map<String, Map<String, String>> recommendersForTriggerIdentifiersForEventIdentifiers = new HashMap<>();

    private final Map<String, Set<String>> editRiseCrestFallTriggeringIdentifiersForEventIdentifiers = new HashMap<>();

    private final Map<String, File> scriptFilesForEventIdentifiers = new HashMap<>();

    private final Map<String, Map<String, String>> eventModifyingScriptsForEventIdentifiers = new HashMap<>();

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

    private boolean addCreatedEventsToSelected;

    /**
     * Flag indicating whether the manager has been shut down.
     * 
     * @deprecated This should be removed once garbage collection problems have
     *             been sorted out; see Redmine issue #21271.
     */
    @Deprecated
    private boolean shutDown = false;

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
        new SessionHazardNotificationListener(this,
                sessionManager.getRunnableAsynchronousScheduler());
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
    public HazardHistoryList getEventHistoryById(String eventId) {
        return dbManager.getHistoryByEventID(eventId, false);
    }

    @Override
    public int getHistoricalVersionCountForEvent(String eventIdentifier) {
        Integer count = historicalVersionCountsForEventIdentifiers
                .get(eventIdentifier);
        return (count == null ? 0 : count);
    }

    @Override
    public Collection<ObservedHazardEvent> getEventsByStatus(
            HazardStatus status, boolean includeUntyped) {
        Collection<ObservedHazardEvent> allEvents = getEvents();
        Collection<ObservedHazardEvent> events = new ArrayList<>(
                allEvents.size());
        for (ObservedHazardEvent event : allEvents) {
            if (event.getStatus().equals(status)
                    && (includeUntyped || (event.getHazardType() != null))) {
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public List<ObservedHazardEvent> getCheckedEvents() {
        Collection<ObservedHazardEvent> allEvents = getEventsForCurrentSettings();
        List<ObservedHazardEvent> events = new ArrayList<>(allEvents.size());
        for (ObservedHazardEvent event : allEvents) {
            if (checkedEventIdentifiers.contains(event.getEventID())) {
                events.add(event);
            }
        }
        return events;
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
        return ((((string1 == null) || string1.isEmpty()) && ((string2 == null) || string2
                .isEmpty())) || ((string1 != null) && string1.equals(string2)));
    }

    private boolean userConfirmationAsNecessary(ObservedHazardEvent event) {
        if (event.getHazardAttribute(VISIBLE_GEOMETRY).equals(
                LOW_RESOLUTION_GEOMETRY_IS_VISIBLE)) {
            IQuestionAnswerer answerer = messenger.getQuestionAnswerer();
            return answerer
                    .getUserAnswerToQuestion("Are you sure you want to overwrite the high resolution geometry?");
        }
        return true;
    }

    @Override
    public List<ObservedHazardEvent> getEventsForCurrentSettings() {
        List<ObservedHazardEvent> result = getEvents();
        filterEventsForConfig(result);
        return result;
    }

    @Override
    public void setEventCategory(ObservedHazardEvent event, String category,
            IOriginator originator) {
        if (!canChangeType(event)) {
            throw new IllegalStateException("cannot change type of event "
                    + event.getEventID());
        }
        event.addHazardAttribute(ATTR_HAZARD_CATEGORY, category, originator);
        event.setHazardType(null, null, null, Originator.OTHER);
    }

    @Override
    public boolean setEventType(ObservedHazardEvent event, String phenomenon,
            String significance, String subType, IOriginator originator) {

        /*
         * If nothing new is being set, do nothing.
         */
        if (areEquivalent(event.getPhenomenon(), phenomenon)
                && areEquivalent(event.getSignificance(), significance)
                && areEquivalent(event.getSubType(), subType)) {
            return true;
        }

        /*
         * If the event cannot change type, create a new event with the new
         * type. Otherwise, record the old time resolution for this event, as it
         * may change.
         */
        ObservedHazardEvent oldEvent = null;
        TimeResolution oldTimeResolution = configManager
                .getTimeResolution(event);
        if (canChangeType(event) == false) {
            oldEvent = event;
            IHazardEvent baseEvent = new BaseHazardEvent(event);
            baseEvent.setEventID("");
            baseEvent.setStatus(HazardStatus.PENDING);
            baseEvent.addHazardAttribute(HazardConstants.REPLACES,
                    configManager.getHeadline(oldEvent));

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
             * The originator should be the session manager, since the addition
             * of a new event is occurring.
             */
            originator = Originator.OTHER;

            /*
             * Add the event, and add it to the selection as well.
             */
            event = addEvent(baseEvent, false, originator);
            sessionManager.getSelectionManager().addEventToSelectedEvents(
                    event.getEventID(), originator);
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
             */
            event.setHazardType(phenomenon, significance, subType, originator);

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
         * "until further notice"). Additionally, if this is not a new event,
         * change the end time to be offset from the start time by the default
         * duration.
         */
        boolean timeResolutionReduced = ((oldTimeResolution == TimeResolution.SECONDS) && (newTimeResolution == TimeResolution.MINUTES));
        Date oldStartTime = event.getStartTime();
        Date newStartTime = (timeResolutionReduced ? roundDateDownToNearestMinute(oldStartTime)
                : oldStartTime);
        Date oldEndTime = event.getEndTime();
        Date newEndTime = (oldEvent == null ? new Date(newStartTime.getTime()
                + configManager.getDefaultDuration(event))
                : (oldEndTime.getTime() != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS ? roundDateDownToNearestMinute(oldEndTime)
                        : oldEndTime));

        /*
         * If this event is changing type (as opposed to a new event created
         * because the type cannot change on the original), remove any recorded
         * interval from before "until further notice" had been turned on, in
         * case it was, since this could lead to the wrong interval being used
         * for the new event type if "until further notice" is subsequently
         * turned off.
         */
        if (oldEvent == null) {
            event.removeHazardAttribute(HazardConstants.END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE);
            event.removeHazardAttribute(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
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
         * Update the time boundaries if this is not a new event; if it is new,
         * simply copy the old event's time boundaries (truncated to minute
         * resolution if resolution has been reduced) to this one. Then update
         * the duration choices for the event.
         */
        if (oldEvent == null) {
            updateTimeBoundariesForEvents(event, false);
        } else {
            Range<Long> oldStartTimeBoundaries = startTimeBoundariesForEventIdentifiers
                    .get(oldEvent.getEventID());
            Range<Long> oldEndTimeBoundaries = endTimeBoundariesForEventIdentifiers
                    .get(oldEvent.getEventID());
            Range<Long> newStartTimeBoundaries = oldStartTimeBoundaries;
            Range<Long> newEndTimeBoundaries = oldEndTimeBoundaries;
            if (timeResolutionReduced) {
                newStartTimeBoundaries = Range.closed(
                        roundTimeDownToNearestMinute(oldStartTimeBoundaries
                                .lowerEndpoint()),
                        roundTimeDownToNearestMinute(oldStartTimeBoundaries
                                .upperEndpoint()));
                newEndTimeBoundaries = (newEndTime.getTime() == HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS ? Range
                        .singleton(HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS)
                        : Range.closed(
                                roundTimeDownToNearestMinute(oldEndTimeBoundaries
                                        .lowerEndpoint()),
                                roundTimeDownToNearestMinute(oldEndTimeBoundaries
                                        .upperEndpoint())));
            }
            startTimeBoundariesForEventIdentifiers.put(event.getEventID(),
                    newStartTimeBoundaries);
            endTimeBoundariesForEventIdentifiers.put(event.getEventID(),
                    newEndTimeBoundaries);
        }
        updateDurationChoicesForEvent(event, false);

        /*
         * If this is a new event, convert its end time to a duration if it does
         * indeed have a duration. This handles the case where the replacement
         * has a duration-based end time, whereas the original event has an
         * absolute end time.
         */
        if (oldEvent != null) {
            convertEndTimeToDuration(event);
        }

        return (originator != Originator.OTHER);
    }

    @Override
    public boolean setEventTimeRange(ObservedHazardEvent event, Date startTime,
            Date endTime, IOriginator originator) {

        /*
         * Ensure that the start and end times are rounded down to the most
         * recent minute boundary if the time resolution for this event is
         * minute-level.
         */
        if (timeResolutionsForEventIdentifiers.get(event.getEventID()) == TimeResolution.MINUTES) {
            startTime = roundDateDownToNearestMinute(startTime);
            if (endTime.getTime() != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
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
        event.setTimeRange(startTime, endTime, originator);
        return true;
    }

    @Override
    public boolean setEventGeometry(ObservedHazardEvent event,
            IAdvancedGeometry geometry, IOriginator originator) {

        /*
         * If the geometry change is valid and the user confirms (if necessary),
         * change the geometry and update the hazard areas. Otherwise, reject
         * the change.
         */
        if (isValidGeometryChange(geometry, event, false)
                && userConfirmationAsNecessary(event)) {
            makeHighResolutionVisible(event, originator);
            event.setGeometry(geometry, originator);
            updateHazardAreas(event);
            return true;
        }
        return false;
    }

    /**
     * Convert the specified hazard event's end time to a duration.
     * 
     * @param event
     *            Event to have its end time converted to a duration.
     */
    private void convertEndTimeToDuration(IHazardEvent event) {

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
                        .convertToAvailableMapForProperty(durationChoicesForEventIdentifiers
                                .get(event.getEventID()));
            } catch (MegawidgetPropertyException e) {
                statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(),
                        e);

                /*
                 * If a problem occurred, use the default duration to avoid
                 * leaving the hazard event in an invalid state.
                 */
                event.setTimeRange(new Date(startTime), new Date(startTime
                        + configManager.getDefaultDuration(event)));
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
            event.setTimeRange(new Date(startTime), new Date(startTime
                    + minDifferenceTimeDelta));
        }
    }

    @Handler(priority = 1)
    public void settingsModified(SettingsModified notification) {
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
     * Respond to the addition of a hazard event by updating the event's start
     * and end time editability boundaries, by ensuring that the event end time
     * "until further notice" flag is appropriate, and by modifying the event
     * conflict tracking data as appropriate.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void hazardAdded(SessionEventAdded change) {
        ensureEventEndTimeUntilFurtherNoticeAppropriate(change.getEvent(), true);
        updateTimeBoundariesForEvents(change.getEvent(), false);
        updateDurationChoicesForEvent(change.getEvent(), false);
        updateConflictingEventsForSelectedEventIdentifiers(change.getEvent(),
                false);
    }

    /**
     * Respond to the removal of a hazard event by firing off a notification
     * that the list of selected events has changed, if appropriate, as well as
     * by updating the event's start and end time editability boundaries, and by
     * modifying the event conflict tracking data as appropriate.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void hazardRemoved(SessionEventRemoved change) {
        timeResolutionsForEventIdentifiers.remove(change.getEvent()
                .getEventID());
        updateSavedTimesForEventIfIssued(change.getEvent(), true);
        updateTimeBoundariesForEvents(change.getEvent(), true);
        updateDurationChoicesForEvent(change.getEvent(), true);
        updateConflictingEventsForSelectedEventIdentifiers(change.getEvent(),
                true);
    }

    /**
     * Respond to a hazard event's type change by updating the event's start and
     * end time editability boundaries, and by firing off a notification that
     * the event may have new metadata, as well as modifying the event conflict
     * tracking data as appropriate.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void hazardTypeChanged(SessionEventTypeModified change) {
        updateEventMetadata(change.getEvent());
        updateConflictingEventsForSelectedEventIdentifiers(change.getEvent(),
                false);
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
    @Handler(priority = 1)
    public void hazardStatusChanged(SessionEventStatusModified change) {
        ObservedHazardEvent event = change.getEvent();
        if ((event.getStatus() == HazardStatus.ELAPSED)
                || (event.getStatus() == HazardStatus.ENDING)
                || (event.getStatus() == HazardStatus.ENDED)) {
            if (event.getStatus() == HazardStatus.ENDING) {
                eventIdentifiersWithEndingStatus.add(event.getEventID());
            } else {
                eventIdentifiersWithEndingStatus.remove(event.getEventID());
            }
            updateTimeBoundariesForEvents(event, false);
            updateDurationChoicesForEvent(event, false);
        } else if ((event.getStatus() == HazardStatus.ISSUED)
                && eventIdentifiersWithEndingStatus
                        .contains(event.getEventID())) {
            eventIdentifiersWithEndingStatus.remove(event.getEventID());
            updateTimeBoundariesForEvents(event, false);
            updateDurationChoicesForEvent(event, false);
        }
        updateEventMetadata(event);
        triggerRecommenderForFirstClassAttributeChange(change.getEvent(),
                HazardEventFirstClassAttribute.STATUS, change.getOriginator());
    }

    /**
     * Respond to the completion of product generation by updating the
     * associated events' parameters, and by recalculating said events' start
     * and end time boundaries if necessary.
     * 
     * @param productGenerationComplete
     *            Notification that is being received.
     */

    @Handler(priority = 1)
    public void handleProductGenerationCompletion(
            IProductGenerationComplete productGenerationComplete) {

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
                        IHazardEvent hazardEvent = (IHazardEvent) event;
                        ObservedHazardEvent oEvent = getEventById(hazardEvent
                                .getEventID());

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
                            oEvent.setStatus(HazardStatus.ISSUED);
                            wasPreIssued = true;
                        } else if (isChangeToEndedStatusNeeded(hazardEvent)) {
                            oEvent.setStatus(HazardStatus.ENDED);
                        } else if (hazardStatus.equals(HazardStatus.ISSUED)) {
                            wasReissued = true;
                        }

                        /*
                         * TODO: Allow product generators to specify whether the
                         * (re)issued events are to be selected or not. For now,
                         * just select or deselect them based upon the current
                         * setting (Redmine issue #18515).
                         */
                        if ((wasPreIssued || wasReissued)
                                && Boolean.TRUE.equals(configManager
                                        .getSettings()
                                        .getDeselectAfterIssuing())) {
                            newlyDeselectedEventIdentifiers.add(oEvent
                                    .getEventID());
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
                            updateSavedTimesForEventIfIssued(oEvent, false);

                            /*
                             * TODO Fix this later. (Tracy) For some reason the
                             * Hazard Event for PHI does not have it's issue
                             * time set at this juncture, and results in an
                             * exception.
                             */
                            // updateTimeRangeBoundariesOfJustIssuedEvent(
                            // oEvent,
                            // (Long) hazardEvent
                            // .getHazardAttribute(HazardConstants.ISSUE_TIME));
                            // updateDurationChoicesForEvent(oEvent, false);
                        } else if (wasReissued) {
                            persistEvent(oEvent);
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
     * 
     * @param hazardEvent
     * @return
     */
    private boolean isChangeToEndedStatusNeeded(IHazardEvent hazardEvent) {
        return hazardEvent.getStatus().equals(HazardStatus.ENDING)
                || hazardEvent.getHazardAttribute(REPLACED_BY) != null;
    }

    /**
     * If a change to the specified first class attribute value for the
     * specified hazard event should trigger the running of a recommender, run
     * that recommender now.
     * 
     * @param event
     *            Hazard event that was changed.
     * @param attribute
     *            First-class attribute that experienced a value change.
     * @param originator
     *            Originator of the change.
     */
    private void triggerRecommenderForFirstClassAttributeChange(
            IHazardEvent event, HazardEventFirstClassAttribute attribute,
            IOriginator originator) {

        /*
         * Get the recommender identifier that is to be triggered by this
         * first-class attribute; if there is one, and it is not the same as the
         * recommender (if any) that caused the change, run that recommender.
         */
        String recommenderIdentifier = configManager
                .getRecommenderTriggeredByChange(event, attribute);
        if ((recommenderIdentifier != null)
                && ((originator instanceof RecommenderOriginator == false) || (recommenderIdentifier
                        .equals(((RecommenderOriginator) originator).getName()) == false))) {
            sessionManager
                    .getRecommenderManager()
                    .runRecommender(
                            recommenderIdentifier,
                            RecommenderExecutionContext.getHazardEventModificationContext(
                                    event.getEventID(),
                                    Sets.newHashSet(attribute.toString()),
                                    ((originator instanceof RecommenderOriginator)
                                            || originator
                                                    .equals(Originator.OTHER) ? RecommenderTriggerOrigin.OTHER
                                            : (originator
                                                    .equals(Originator.DATABASE) ? RecommenderTriggerOrigin.DATABASE
                                                    : RecommenderTriggerOrigin.USER))));
        }
    }

    /**
     * Respond to a CAVE current time reset by updating all the events' start
     * and end time editability boundaries.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void currentTimeReset(CurrentTimeReset change) {
        updateTimeBoundariesForEvents(null, false);
    }

    /**
     * Respond to a CAVE current time minute tick by updating all the events'
     * start and end time editability boundaries.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void currentTimeMinuteTicked(CurrentTimeMinuteTicked change) {
        updateTimeBoundariesForEvents(null, false);
    }

    @Override
    public MegawidgetSpecifierManager getMegawidgetSpecifiers(IHazardEvent event) {

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
        if (event instanceof ObservedHazardEvent) {
            MegawidgetSpecifierManager manager = megawidgetSpecifiersForEventIdentifiers
                    .get(event.getEventID());
            if (manager != null) {
                return manager;
            }
            updateEventMetadata((ObservedHazardEvent) event);
            return megawidgetSpecifiersForEventIdentifiers.get(event
                    .getEventID());
        } else {
            return configManager.getMetadataForHazardEvent(event)
                    .getMegawidgetSpecifierManager();
        }
    }

    @Override
    public List<String> getDurationChoices(IHazardEvent event) {

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
                && (event instanceof ObservedHazardEvent == false)) {
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
     * @param event
     *            Event for which the deltas are being fetched.
     * @param durationChoices
     *            List of duration choices to be turned into deltas.
     * @return Map of the duration choices to their associated time deltas in
     *         milliseconds, or <code>null</code> if an error occurs.
     */
    private Map<String, Long> getDeltasForDurationChoices(IHazardEvent event,
            List<String> durationChoices) {

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
            statusHandler.error(
                    "invalid list of duration choices for event of type "
                            + HazardEventUtilities.getHazardType(event), e);
            return null;
        }
        return deltasForDurations;
    }

    @Override
    public void eventCommandInvoked(ObservedHazardEvent event,
            String identifier,
            Map<String, Map<String, Object>> mutableProperties) {

        /*
         * If the command that was invoked is a metadata refresh trigger,
         * perform the refresh; if it is a recommender running trigger, run the
         * recommender; otherwise, if the command is meant to trigger the
         * editing of rise-crest-fall information, start the edit.
         */
        String eventId = event.getEventID();
        if (metadataReloadTriggeringIdentifiersForEventIdentifiers
                .containsKey(eventId)
                && metadataReloadTriggeringIdentifiersForEventIdentifiers.get(
                        eventId).contains(identifier)) {
            updateEventMetadata(event);
            return;
        } else if (recommendersForTriggerIdentifiersForEventIdentifiers
                .containsKey(eventId)
                && recommendersForTriggerIdentifiersForEventIdentifiers.get(
                        eventId).containsKey(identifier)) {
            sessionManager.getRecommenderManager().runRecommender(
                    recommendersForTriggerIdentifiersForEventIdentifiers.get(
                            eventId).get(identifier),
                    RecommenderExecutionContext
                            .getHazardEventModificationContext(eventId,
                                    Sets.newHashSet(identifier),
                                    RecommenderTriggerOrigin.USER));
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
                                this, originalEvent, event
                                        .getMutableProperties(),
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
    private void updateEventMetadata(ObservedHazardEvent event) {

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
        metadataIdentifiersAffectingModifyFlagsForEventIdentifiers.put(
                event.getEventID(),
                metadata.getAffectingModifyFlagMetadataKeys());
        recommendersForTriggerIdentifiersForEventIdentifiers.put(
                event.getEventID(),
                metadata.getRecommendersTriggeredForMetadataKeys());
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
         * Fire off a notification that the metadata may have changed for this
         * event if this event is currently one of the session events (it may
         * not be if it has not yet been added).
         */
        if (events.contains(event)) {
            notificationSender
                    .postNotificationAsync(new SessionEventMetadataModified(
                            this, event, Originator.OTHER));
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

        boolean eventModified = event.isModified();
        Map<String, Serializable> attributes = event.getHazardAttributes();
        Map<String, Object> newAttributes = new HashMap<String, Object>(
                attributes);
        populateTimeAttributesStartingStates(manager.getSpecifiers(),
                newAttributes, event.getStartTime().getTime(), event
                        .getEndTime().getTime());
        manager.populateWithStartingStates(newAttributes);
        attributes = new HashMap<>(newAttributes.size());
        for (String name : newAttributes.keySet()) {
            attributes.put(name, (Serializable) newAttributes.get(name));
        }

        event.setHazardAttributes(attributes);
        event.setModified(eventModified);
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
                updateEventMetadata((ObservedHazardEvent) event);
            }

        };
        IRiseCrestFallEditor editor = messenger.getRiseCrestFallEditor(event);
        IHazardEvent evt = editor.getRiseCrestFallEditor(event, applier);
        if (evt != null) {
            if (evt instanceof ObservedHazardEvent) {
                event = evt;
            }
            updateEventMetadata((ObservedHazardEvent) event);
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
     * metadata state changes that should trigger a metadata reload, or that
     * should trigger a recommender run, do so.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void hazardAttributesChanged(SessionEventAttributesModified change) {

        /*
         * If the end time "until further notice" flag has changed value but was
         * not removed, change the end time in a corresponding manner.
         */
        if (change
                .containsAttribute(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)
                && change
                        .getEvent()
                        .getHazardAttributes()
                        .containsKey(
                                HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)) {
            setEventEndTimeForUntilFurtherNotice(
                    change.getEvent(),
                    Boolean.TRUE
                            .equals(change
                                    .getEvent()
                                    .getHazardAttribute(
                                            HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)));
        }

        /*
         * If any of the attributes changed are metadata-reload triggers, see if
         * metadata needs to be reloaded; if any of them are to trigger the
         * running of recommenders, run the recommenders; otherwise, if any of
         * them are to trigger the editing of rise-crest-fall information,
         * reload that.
         */
        Set<String> metadataReloadTriggeringIdentifiers = metadataReloadTriggeringIdentifiersForEventIdentifiers
                .get(change.getEvent().getEventID());
        Map<String, String> recommendersForTriggerIdentifiers = recommendersForTriggerIdentifiersForEventIdentifiers
                .get(change.getEvent().getEventID());
        Set<String> editRiseCrestFallTriggeringIdentifiers = editRiseCrestFallTriggeringIdentifiersForEventIdentifiers
                .get(change.getEvent().getEventID());
        boolean triggeredReloadOrRecommender = false;
        if (metadataReloadTriggeringIdentifiers != null) {

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
                    change.getAttributeKeys());
            for (String trigger : triggers) {
                if (change.getOldAttribute(trigger) != null) {
                    updateEventMetadata(change.getEvent());
                    triggeredReloadOrRecommender = true;
                    break;
                }
            }
        }
        if ((triggeredReloadOrRecommender == false)
                && (recommendersForTriggerIdentifiers != null)) {

            /*
             * Determine whether or not a recommender triggered this change, and
             * if so, note its name, since recommenders should not be triggered
             * by changes caused by the earlier runs of those same recommenders.
             */
            String cause = null;
            IOriginator originator = change.getOriginator();
            if (originator instanceof RecommenderOriginator) {
                cause = ((RecommenderOriginator) change.getOriginator())
                        .getName();
            }

            /*
             * See if at least one of the attributes that changed is a
             * recommender trigger. If so, put together a mapping of
             * recommenders that need to be run to the set of one or more
             * attributes that triggered them.
             */
            Set<String> triggers = Sets.intersection(
                    recommendersForTriggerIdentifiers.keySet(),
                    change.getAttributeKeys());
            Map<String, Set<String>> triggerSetsForRecommenders = new HashMap<>();
            for (String trigger : triggers) {

                /*
                 * Get the recommender to be triggered by this attribute
                 * identifier; if it is the same as the recommender that caused
                 * the attribute to change, do nothing with it.
                 */
                String recommender = recommendersForTriggerIdentifiers
                        .get(trigger);
                if (recommender.equals(cause)) {
                    continue;
                }

                /*
                 * If the recommender is already associated with a set of
                 * triggers, add this trigger to the set; otherwise, create a
                 * new set holding this trigger and associate it with the
                 * recommender.
                 */
                if (triggerSetsForRecommenders.containsKey(recommender)) {
                    triggerSetsForRecommenders.get(recommender).add(trigger);
                } else {
                    Set<String> triggerSet = Sets.newHashSet(trigger);
                    triggerSetsForRecommenders.put(recommender, triggerSet);
                }
            }

            /*
             * Iterate through the recommenders to be run, executing each in
             * turn.
             */
            for (Map.Entry<String, Set<String>> entry : triggerSetsForRecommenders
                    .entrySet()) {
                sessionManager
                        .getRecommenderManager()
                        .runRecommender(
                                entry.getKey(),
                                RecommenderExecutionContext
                                        .getHazardEventModificationContext(
                                                change.getEvent().getEventID(),
                                                entry.getValue(),
                                                ((originator instanceof RecommenderOriginator)
                                                        || originator
                                                                .equals(Originator.OTHER) ? RecommenderTriggerOrigin.OTHER
                                                        : (originator
                                                                .equals(Originator.DATABASE) ? RecommenderTriggerOrigin.DATABASE
                                                                : RecommenderTriggerOrigin.USER))));
            }
            if (triggerSetsForRecommenders.isEmpty() == false) {
                triggeredReloadOrRecommender = true;
            }
        }
        if ((triggeredReloadOrRecommender == false)
                && (editRiseCrestFallTriggeringIdentifiers != null)
                && (Sets.intersection(editRiseCrestFallTriggeringIdentifiers,
                        change.getAttributeKeys()).isEmpty() == false)) {
            startRiseCrestFallEdit(change.getEvent());
        }
    }

    /**
     * Ensure that changes to an event's time range cause the selected hazard
     * conflicts map to be updated.
     * 
     * @param change
     *            Change that occurred.
     */

    @Handler(priority = 1)
    public void hazardTimeRangeChanged(SessionEventTimeRangeModified change) {
        updateConflictingEventsForSelectedEventIdentifiers(change.getEvent(),
                false);
        triggerRecommenderForFirstClassAttributeChange(change.getEvent(),
                HazardEventFirstClassAttribute.TIME_RANGE,
                change.getOriginator());
    }

    /**
     * Ensure that changes to an event's geometry cause the selected hazard
     * conflicts map to be updated.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler(priority = 1)
    public void hazardGeometryChanged(SessionEventGeometryModified change) {
        updateConflictingEventsForSelectedEventIdentifiers(change.getEvent(),
                false);
        triggerRecommenderForFirstClassAttributeChange(change.getEvent(),
                HazardEventFirstClassAttribute.GEOMETRY, change.getOriginator());
    }

    @Handler(priority = 1)
    public void hazardVisualFeatureChanged(
            SessionEventVisualFeaturesModified change) {

        /*
         * Get the recommender identifier that is to be triggered by visual
         * feature changes; if there is one, and it is not the same as the
         * recommender (if any) that caused the change, run that recommender.
         */
        String recommenderIdentifier = configManager
                .getRecommenderTriggeredByChange(change.getEvent(),
                        HazardEventFirstClassAttribute.VISUAL_FEATURE);
        IOriginator originator = change.getOriginator();
        if ((recommenderIdentifier != null)
                && ((originator instanceof RecommenderOriginator == false) || (recommenderIdentifier
                        .equals(((RecommenderOriginator) originator).getName()) == false))) {
            sessionManager
                    .getRecommenderManager()
                    .runRecommender(
                            recommenderIdentifier,
                            RecommenderExecutionContext
                                    .getHazardEventVisualFeatureChangeContext(
                                            change.getEvent().getEventID(),
                                            change.getVisualFeatureIdentifiers(),
                                            ((originator instanceof RecommenderOriginator)
                                                    || originator
                                                            .equals(Originator.OTHER) ? RecommenderTriggerOrigin.OTHER
                                                    : (originator
                                                            .equals(Originator.DATABASE) ? RecommenderTriggerOrigin.DATABASE
                                                            : RecommenderTriggerOrigin.USER))));
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
    private void updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(
            ObservedHazardEvent event, boolean removed) {

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
        if (changed && events.contains(event)) {
            notificationSender
                    .postNotificationAsync(new SessionEventAllowUntilFurtherNoticeModified(
                            this, event, Originator.OTHER));
        }
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
        Set<String> siteIDs = configManager.getSettingsValue(
                SETTING_HAZARD_SITES, settings);
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

    private void loadEventsForSettings(ObservedSettings settings) {

        /*
         * Create the query to be used to get the events for the current
         * settings.
         */
        HazardEventQueryRequest queryRequest = new HazardEventQueryRequest();

        /*
         * If the settings file has not been overridden for the site, add the
         * currently configured site to the list of visible sites, and include
         * visible sites as part of the query filter.
         */
        Set<String> visibleSites = configManager.getSettingsValue(
                SETTING_HAZARD_SITES, settings);
        if (visibleSites == null) {
            visibleSites = new HashSet<>(1);
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
        queryRequest.and(HazardConstants.SITE_ID, visibleSites).and(
                HazardConstants.PHEN_SIG, visibleTypes);
        Map<String, HazardHistoryList> eventsMap = dbManager
                .queryHistory(queryRequest);

        /*
         * Get the identifiers of the events that were selected before this
         * change.
         */
        Set<String> oldSelectedEventIdentifiers = new HashSet<>(sessionManager
                .getSelectionManager().getSelectedEventIdentifiers());

        /*
         * Determine which statuses events may have in order to be visible.
         */
        Set<String> visibleStatuses = settings.getVisibleStatuses();
        if (visibleStatuses == null || visibleStatuses.isEmpty()) {
            return;
        }
        Set<HazardStatus> statuses = EnumSet.noneOf(HazardStatus.class);
        for (String state : visibleStatuses) {
            statuses.add(HazardStatus.valueOf(state.toUpperCase()));
        }

        /*
         * Iterate through the events, adding any that have visible statuses and
         * that were not part of the event list before. Also track which events
         * that were being managed previously are not included in the events
         * returned from the query.
         */
        Set<ObservedHazardEvent> leftoverEvents = new HashSet<>(events);
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
            ObservedHazardEvent oldEvent = getEventById(eventIdentifier);
            if (oldEvent != null) {
                leftoverEvents.remove(oldEvent);
                if (oldSelectedEventIdentifiers.contains(eventIdentifier)) {
                    selectedEventIdentifiers.add(eventIdentifier);
                }
                continue;
            }

            /*
             * Add the event, and if it has ever been issued, set its
             * has-been-issued flag.
             */
            IHazardEvent addedEvent = addEvent(event, false,
                    Originator.DATABASE);
            for (IHazardEvent historicalEvent : historyList) {
                if (HazardStatus.issuedButNotEndedOrElapsed(historicalEvent
                        .getStatus())) {
                    addedEvent.addHazardAttribute(ATTR_ISSUED, true);
                    break;
                }
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
                latestVersionsFromDatabaseForEventIdentifiers.put(
                        eventIdentifier, event);
            } else {
                historicalVersionCountsForEventIdentifiers.put(eventIdentifier,
                        historyList.size());
            }
        }

        /*
         * For the remaining old events (those not included in the ones returned
         * by the query), filter them for the current settings.
         */
        filterEventsForConfig(leftoverEvents);
        for (ObservedHazardEvent event : leftoverEvents) {
            String eventIdentifier = event.getEventID();
            if (oldSelectedEventIdentifiers.contains(eventIdentifier)) {
                selectedEventIdentifiers.add(eventIdentifier);
            }
        }

        /*
         * Schedule expiration tasks for the events.
         */
        for (ObservedHazardEvent event : events) {
            scheduleExpirationTask(event);
        }

        /*
         * Set the new selected events
         */
        sessionManager.getSelectionManager().setSelectedEventIdentifiers(
                selectedEventIdentifiers, Originator.OTHER);
    }

    @Override
    public ObservedHazardEvent addEvent(IHazardEvent event,
            IOriginator originator) {
        HazardStatus status = event.getStatus();
        if (((status == null) || (status == HazardStatus.PENDING))
                && (originator != Originator.DATABASE)) {
            return addEvent(event, true, originator);
        } else {
            return addEvent(event, false, originator);
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
     * </p>
     * 
     * @param event
     *            Event to be added (or if the event has an identifier that is
     *            already in use by an existing event, to be used in place of
     *            the existing event).
     * @param select
     *            Flag indicating whether or not it should be selected.
     * @param originator
     *            Originator of the addition.
     * @return Event that was added or modified.
     */
    protected ObservedHazardEvent addEvent(IHazardEvent event, boolean select,
            IOriginator originator) {
        ObservedHazardEvent oevent = new ObservedHazardEvent(event, this);
        oevent.setInsertTime(null);

        /*
         * Need to account for the case where the event being added already
         * exists in the event manager. (This can happen with recommender
         * callbacks, for example.) If it has an event identifier, see if it
         * there is already an existing event, and if so, merge it into the
         * existing one. If it does not have a valid identifier, create one.
         */
        String eventID = oevent.getEventID();
        if ((eventID != null) && (eventID.length() > 0)) {
            ObservedHazardEvent existingEvent = getEventById(eventID);
            if (existingEvent != null) {
                mergeHazardEvents(oevent, existingEvent, false, false, true,
                        false, originator);
                return existingEvent;
            }
        } else {
            try {
                oevent.setEventID(HazardServicesEventIdUtil.getNewEventID());
            } catch (Exception e) {
                statusHandler.error("Unable to set event id", e);
            }
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
                && oevent.getHazardAttribute(ATTR_HAZARD_CATEGORY) == null) {
            oevent.addHazardAttribute(ATTR_HAZARD_CATEGORY,
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
            oevent.setEndTime(oevent.getEndTime().getTime() != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS ? roundDateDownToNearestMinuteIfAppropriate(
                    oevent.getEndTime(), eventTimeResolution) : oevent
                    .getEndTime());
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
         * Validate significance, since some recommenders use the full name.
         */
        String sig = oevent.getSignificance();
        if (sig != null) {

            /*
             * This will throw an exception if its not a valid name or
             * abbreviation.
             */
            try {
                HazardConstants.significanceFromAbbreviation(sig);
            } catch (IllegalArgumentException e) {
                Significance s = Significance.valueOf(sig);
                oevent.setSignificance(s.getAbbreviation(), false, originator);
            }
        }

        /*
         * Set the site identifier to the current one.
         */
        oevent.setSiteID(configManager.getSiteID(), false, originator);

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

        events.add(oevent);

        /*
         * Notify listeners that the event has been added.
         */
        notificationSender.postNotificationAsync(new SessionEventAdded(this,
                oevent, originator));

        /*
         * Set the issued flag as appropriate.
         */
        oevent.addHazardAttribute(ATTR_ISSUED,
                HazardStatus.issuedButNotEndedOrElapsed(oevent.getStatus()),
                false, originator);

        /*
         * If the event is to be selected, either select only it, or add it to
         * the current selection, as appropriate.
         */
        if (select) {
            if ((addCreatedEventsToSelected == false)
                    && (Boolean.TRUE.equals(settings.getAddToSelected()) == false)) {
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
         * Add the event to the set of checked events.
         */
        checkedEventIdentifiers.add(oevent.getEventID());

        /*
         * Determine whether this event allows until-further-notice and record
         * this fact if it does, and record any saved times for the event if it
         * has been issued.
         */
        updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(oevent, false);
        updateSavedTimesForEventIfIssued(oevent, false);

        /*
         * Create the event metadata for the new event.
         */
        updateEventMetadata(oevent);

        /*
         * If this event was loaded from the database, it will not have any
         * visual features; this may need to trigger a recommender.
         */
        if (originator == Originator.DATABASE) {
            hazardVisualFeatureChanged(new SessionEventVisualFeaturesModified(
                    this, oevent, Collections.<String> emptySet(), originator));
        }
        return oevent;
    }

    @Override
    public void mergeHazardEvents(IHazardEvent newEvent,
            ObservedHazardEvent oldEvent, boolean forceMerge,
            boolean keepVisualFeatures, boolean persistOnStatusChange,
            boolean useModifiedValue, IOriginator originator) {

        oldEvent.setSiteID(newEvent.getSiteID(), originator);
        oldEvent.setUserName(newEvent.getUserName(), originator);
        oldEvent.setWorkStation(newEvent.getWorkStation(), originator);

        /*
         * Set the hazard type and time range via the session manager if not a
         * forced merge.
         */
        if (forceMerge) {
            oldEvent.setHazardType(newEvent.getPhenomenon(),
                    newEvent.getSignificance(), newEvent.getSubType(),
                    originator);
            oldEvent.setTimeRange(newEvent.getStartTime(),
                    newEvent.getEndTime(), originator);
        } else {
            setEventType(oldEvent, newEvent.getPhenomenon(),
                    newEvent.getSignificance(), newEvent.getSubType(),
                    originator);
            setEventTimeRange(oldEvent, newEvent.getStartTime(),
                    newEvent.getEndTime(), originator);
        }

        oldEvent.setCreationTime(newEvent.getCreationTime(), originator);
        oldEvent.setGeometry(newEvent.getGeometry(), originator);

        /*
         * If the keep visual features flag is set, only use the visual features
         * of the new event if there is at least one, retaining the old event's
         * features if the new one has none. If the flag is not set, always use
         * the new event's visual feature list.
         */
        if ((keepVisualFeatures == false)
                || ((newEvent.getVisualFeatures() != null) && (newEvent
                        .getVisualFeatures().isEmpty() == false))) {
            oldEvent.setVisualFeatures(newEvent.getVisualFeatures(), originator);
        }

        oldEvent.setHazardMode(newEvent.getHazardMode(), originator);

        /*
         * Get a copy of the old attributes, and the new ones, then transfer any
         * attributes that are to be retained (if they are not already in the
         * new attributes) from the old to the new. Then set the resulting map
         * as the old hazard's attributes.
         */
        Map<String, Serializable> oldAttr = oldEvent.getHazardAttributes();
        if (oldAttr == null) {
            oldAttr = Collections.emptyMap();
        }
        Map<String, Serializable> newAttr = newEvent.getHazardAttributes();
        newAttr = (newAttr != null ? new HashMap<>(newAttr)
                : new HashMap<String, Serializable>());
        for (String key : ATTRIBUTES_TO_RETAIN_ON_MERGE) {
            if ((newAttr.containsKey(key) == false) && oldAttr.containsKey(key)) {
                newAttr.put(key, oldAttr.get(key));
            }
        }
        oldEvent.setHazardAttributes(newAttr, originator);

        /*
         * Change the status only if the event is not already ended (this could
         * be relevant if the CAVE clock is set back) and if it is not the same
         * as the previous status. If the status of the old event is changed,
         * update its saved end time/duration if it is issued.
         */
        if ((isEnded(oldEvent) == false)
                && (oldEvent.getStatus().equals(newEvent.getStatus()) == false)) {
            oldEvent.setStatus(newEvent.getStatus(), true,
                    persistOnStatusChange, Originator.OTHER);
            updateSavedTimesForEventIfIssued(oldEvent, false);
        }

        if (useModifiedValue) {
            oldEvent.setModified(newEvent.isModified());
        }
    }

    /**
     * Determine if the specified hazard event has ended.
     * 
     * @param event
     *            Event to be checked.
     * @return <code>true</code> if the event has ended, <code>false</code>
     *         otherwise.
     */
    private boolean isEnded(IHazardEvent event) {
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
    private boolean isElapsed(IHazardEvent event) {
        Date currTime = SimulatedTime.getSystemTime().getTime();
        HazardStatus status = event.getStatus();
        if ((status == HazardStatus.ELAPSED)
                || (HazardStatus.issuedButNotEndedOrElapsed(status) && (event
                        .getEndTime().before(currTime)))) {
            return true;
        }
        return false;
    }

    private boolean isPastExpirationTime(IHazardEvent event) {
        long currTimeLong = SimulatedTime.getSystemTime().getMillis();

        Long expirationTimeLong = (Long) event
                .getHazardAttribute(HazardConstants.EXPIRATION_TIME);
        if ((expirationTimeLong != null) && (expirationTimeLong < currTimeLong)) {
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
    void handleEventAdditionToDatabase(HazardEvent event) {

        /*
         * TODO: Remove once the HISTORICAL attribute is no longer being used.
         */
        boolean historical = Boolean.TRUE.equals(event
                .getHazardAttribute(HazardEventManager.HISTORICAL));
        event.removeHazardAttribute(HazardEventManager.HISTORICAL);

        /*
         * If the event is being managed already, merge the new version with the
         * existing one, and if it is a historical snapshot, post a notification
         * indicating that the history list for this event has changed. If it is
         * not already being managed, add it to the session.
         */
        String eventIdentifier = event.getEventID();
        ObservedHazardEvent oldEvent = getEventById(eventIdentifier);
        if (oldEvent != null) {
            mergeHazardEvents(event, oldEvent, true, true, false, true,
                    Originator.DATABASE);
            if (event.isLatestVersion() == false) {
                notificationSender
                        .postNotificationAsync(new SessionEventHistoryModified(
                                this, getEventById(event.getEventID()),
                                Originator.DATABASE));
            }
        } else {
            addEvent(event, Originator.DATABASE);
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
    }

    /**
     * Handle the removal of an event from the database.
     * 
     * @param event
     *            Hazard event removed from the database.
     */
    void handleEventRemovalFromDatabase(HazardEvent event) {

        /*
         * TODO: Remove once the HISTORICAL attribute is no longer being used.
         */
        boolean historical = Boolean.TRUE.equals(event
                .getHazardAttribute(HazardEventManager.HISTORICAL));
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
            latestVersionsFromDatabaseForEventIdentifiers.remove(event
                    .getEventID());
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
    void handleEventRemovalAllCopiesFromDatabase(String eventIdentifier) {
        ObservedHazardEvent oldEvent = getEventById(eventIdentifier);
        if (oldEvent != null) {
            removeEvent(oldEvent, false, Originator.DATABASE);
        }
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
        sessionManager.rememberRemovedEventIdentifier(event.getEventID());
        if (events.contains(event)) {

            /*
             * Deselect the event if it was selected, remove it from the checked
             * event identifiers set if it was there, and remove it.
             */
            String eventIdentifier = event.getEventID();
            sessionManager.getSelectionManager().removeEventFromSelectedEvents(
                    eventIdentifier, Originator.OTHER);
            checkedEventIdentifiers.remove(eventIdentifier);
            events.remove(event);

            /*
             * TODO this should never delete operation issued events.
             */
            /*
             * TODO this should not delete the whole list, just any pending or
             * proposed items on the end of the list.
             */
            if (delete) {
                dbManager.removeAllCopiesOfEvent(eventIdentifier);
            }
            updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(
                    (ObservedHazardEvent) event, true);
            megawidgetSpecifiersForEventIdentifiers.remove(eventIdentifier);
            metadataReloadTriggeringIdentifiersForEventIdentifiers
                    .remove(eventIdentifier);
            metadataIdentifiersAffectingModifyFlagsForEventIdentifiers
                    .remove(eventIdentifier);
            recommendersForTriggerIdentifiersForEventIdentifiers
                    .remove(eventIdentifier);
            editRiseCrestFallTriggeringIdentifiersForEventIdentifiers
                    .remove(eventIdentifier);
            scriptFilesForEventIdentifiers.remove(eventIdentifier);
            eventModifyingScriptsForEventIdentifiers.remove(eventIdentifier);
            notificationSender.postNotificationAsync(new SessionEventRemoved(
                    this, event, originator));
        }

        /*
         * Remove the history list size record and the latest version record for
         * the hazard event, since they are no longer needed.
         */
        latestVersionsFromDatabaseForEventIdentifiers
                .remove(event.getEventID());
        historicalVersionCountsForEventIdentifiers.remove(event.getEventID());
    }

    @Override
    public void sortEvents(Comparator<ObservedHazardEvent> comparator,
            IOriginator originator) {
        Collections.sort(events, comparator);
        notificationSender
                .postNotificationAsync(new SessionEventsOrderingModified(this,
                        originator));
    }

    @Override
    public List<ObservedHazardEvent> getEvents() {
        return new ArrayList<ObservedHazardEvent>(events);
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
    protected void hazardEventModified(SessionEventModified notification) {
        ObservedHazardEvent event = notification.getEvent();
        sessionManager.getSelectionManager().setLastAccessedSelectedEvent(
                event.getEventID(), notification.getOriginator());
        updateIdentifiersOfEventsAllowingUntilFurtherNoticeSet(event, false);
        ensureEventEndTimeUntilFurtherNoticeAppropriate(event, false);
        if (events.contains(event)) {
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
        if (events.contains(event)) {
            notificationSender
                    .postNotificationAsync(new SessionEventUnsavedChangesModified(
                            this, event, Originator.OTHER));
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
            SessionEventAttributesModified notification) {
        IHazardEvent event = notification.getEvent();
        sessionManager.getSelectionManager().setLastAccessedSelectedEvent(
                event.getEventID(), notification.getOriginator());
        if (events.contains(notification.getEvent())) {
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
    @SuppressWarnings("unchecked")
    protected void hazardEventStatusModified(
            SessionEventStatusModified notification, boolean persist) {

        /*
         * Determine whether or not the event should be persisted, and deselect
         * it if it is ended and, VTEC-wise, canceled.
         */
        boolean removedFromSelection = false;
        ObservedHazardEvent event = notification.getEvent();
        HazardStatus newStatus = event.getStatus();
        if (persist) {

            boolean needsPersist = false;
            switch (newStatus) {
            case ISSUED:
                event.addHazardAttribute(ATTR_ISSUED, true);
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
                ;// do nothing.
            }
            if (needsPersist) {
                persistEvent(event);
            }
        }

        /*
         * If the new status is visible in the settings, make this event the
         * last accessed event; otherwise, deselect the event if it has not
         * already been deselected, as it should no longer be visible in the UI.
         */
        if (configManager.getSettings().getVisibleStatuses()
                .contains(newStatus.getValue())) {
            sessionManager.getSelectionManager().setLastAccessedSelectedEvent(
                    event.getEventID(), notification.getOriginator());
        } else if (removedFromSelection == false) {
            sessionManager.getSelectionManager().removeEventFromSelectedEvents(
                    event.getEventID(), Originator.OTHER);
        }

        /*
         * If the event is being managed, post the notification about the status
         * change.
         */
        if (events.contains(notification.getEvent())) {
            notificationSender.postNotificationAsync(notification);
        }

        updateConflictingEventsForSelectedEventIdentifiers(
                notification.getEvent(), false);
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
             * remove it before adding to the history list.
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
    public boolean canChangeType(ObservedHazardEvent event) {
        if (hasEverBeenIssued(event)) {
            return false;
        }
        return true;
    }

    private boolean hasEverBeenIssued(IHazardEvent event) {
        return Boolean.TRUE.equals(event.getHazardAttribute(ATTR_ISSUED));
    }

    @Override
    public Map<String, Collection<IHazardEvent>> getConflictingEventsForSelectedEvents() {
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
        if (endTime == HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
            return Range.singleton(endTime);
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
        if (endTime == HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
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
            endTime = startTime
                    + endTimesOrDurationsForIssuedEventIdentifiers.get(event
                            .getEventID());
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
        case ELAPSED:
        case ENDING:
        case ENDED:
            endTimeRange = getEndTimeRangeForEndingEvent(newEndTime);
        }
        if (endTimeRange.equals(endTimeBoundariesForEventIdentifiers.get(event
                .getEventID())) == false) {
            endTimeBoundariesForEventIdentifiers.put(event.getEventID(),
                    endTimeRange);
            if (events.contains(event)) {
                notificationSender
                        .postNotificationAsync(new SessionEventsTimeRangeBoundariesModified(
                                this, Sets.newHashSet(event.getEventID()),
                                Originator.OTHER));
            }
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
        String eventID = event.getEventID();
        if (startTimeRange.equals(startTimeBoundariesForEventIdentifiers
                .get(eventID)) == false) {
            startTimeBoundariesForEventIdentifiers.put(eventID, startTimeRange);
            changed = true;
        }
        if (endTimeRange.equals(endTimeBoundariesForEventIdentifiers
                .get(eventID)) == false) {
            endTimeBoundariesForEventIdentifiers.put(eventID, endTimeRange);
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
         * before or after the current CAVE clock time, while some do not. End
         * times can be anything if unissued, but issued ones may be limited by
         * by the hazard type's contraints Finally, ending and ended hazards
         * cannot have their times changed.
         */
        Range<Long> startTimeRange = null;
        Range<Long> endTimeRange = null;
        long startTime = event.getStartTime().getTime();
        long endTime = event.getEndTime().getTime();
        switch (event.getStatus()) {
        case POTENTIAL:
        case PENDING:
        case PROPOSED:
            startTimeRange = Range.closed((configManager
                    .isAllowAnyStartTime(event) ? HazardConstants.MIN_TIME
                    : currentTime), (configManager
                    .isStartTimeIsCurrentTime(event) ? currentTime
                    : HazardConstants.MAX_TIME));
            endTimeRange = getEndTimeRangeForPreIssuedEvent(endTime);
            break;
        case ISSUED:
            boolean startTimeIsCurrentTime = configManager
                    .isStartTimeIsCurrentTime(event);
            startTimeRange = Range.closed((startTimeIsCurrentTime ? startTime
                    : HazardConstants.MIN_TIME),
                    (startTimeIsCurrentTime ? startTime
                            : HazardConstants.MAX_TIME));
            endTimeRange = getEndTimeRangeForIssuedEvent(event, startTime,
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
                        this, eventIdentifiers, Originator.OTHER));
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
         * always current time) or else is practically unlimited. The maximum is
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
        if (endTime != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
            if (configManager.getDurationChoices(event).isEmpty()) {
                if (endTime - startTime < HazardConstants.TIME_RANGE_MINIMUM_INTERVAL) {
                    endTime = startTime
                            + HazardConstants.TIME_RANGE_MINIMUM_INTERVAL;
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
        event.setTimeRange(new Date(startTime), new Date(endTime),
                Originator.OTHER);

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
            boolean removed) {

        /*
         * If all events should be checked, iterate through them, adding any
         * that have their boundaries changed to the set recording changed
         * events. Otherwise, handle the single event's potential change.
         */
        long currentTime = SimulatedTime.getSystemTime().getTime().getTime();
        Set<String> identifiersWithChangedBoundaries = new HashSet<>();
        if (singleEvent == null) {
            Set<String> identifiersWithExpiredTimes = new HashSet<>();
            for (ObservedHazardEvent thisEvent : events) {

                /*
                 * Round the current time down to the previous minute if this
                 * event has minute-level time resolution.
                 */
                long eventCurrentTime = (timeResolutionsForEventIdentifiers
                        .get(thisEvent.getEventID()) == TimeResolution.MINUTES ? roundTimeDownToNearestMinute(currentTime)
                        : currentTime);
                if (updateTimeBoundariesForSingleEvent(thisEvent,
                        eventCurrentTime)) {
                    identifiersWithChangedBoundaries
                            .add(thisEvent.getEventID());
                }
                if (isPastExpirationTime(thisEvent) == true) {
                    identifiersWithExpiredTimes.add(thisEvent.getEventID());
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
                    postTimeRangeBoundariesModifiedNotification(identifiersWithExpiredTimes);
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
                startTimeBoundariesForEventIdentifiers.remove(singleEvent
                        .getEventID());
                endTimeBoundariesForEventIdentifiers.remove(singleEvent
                        .getEventID());
            } else if (updateTimeBoundariesForSingleEvent(
                    singleEvent,
                    (timeResolutionsForEventIdentifiers.get(singleEvent
                            .getEventID()) == TimeResolution.MINUTES ? roundTimeDownToNearestMinute(currentTime)
                            : currentTime))) {
                identifiersWithChangedBoundaries.add(singleEvent.getEventID());
            }
        }

        /*
         * If any events' boundaries have changed, send out a notification to
         * that effect, and ensure that those that have changed have their start
         * and end times falling within the new boundaries.
         */
        if (identifiersWithChangedBoundaries.isEmpty() == false) {
            postTimeRangeBoundariesModifiedNotification(identifiersWithChangedBoundaries);
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
                if (endTime != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {

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
                        if (endTime - startTime < HazardConstants.TIME_RANGE_MINIMUM_INTERVAL) {
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
                 */
                if (changed) {
                    thisEvent.setTimeRange(new Date(startTime), new Date(
                            endTime), Originator.OTHER);
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
            endTimesOrDurationsForIssuedEventIdentifiers.remove(eventId);
        } else if (event.getStatus() == HazardStatus.ISSUED) {
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
                Map<String, Long> deltasForDurations = getDeltasForDurationChoices(
                        event, durationChoices);
                if (deltasForDurations == null) {
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
     * <p>
     * TODO: Currently, the parameters are not used; the map is simply rebuilt.
     * This could certainly benefit from a less brute-force approach; see inline
     * comments within the method.
     * </p>
     * 
     * @param event
     *            Event that has been added, removed, or modified.
     * @param removed
     *            Flag indicating whether or not the change is the removal of
     *            the event.
     */
    void updateConflictingEventsForSelectedEventIdentifiers(IHazardEvent event,
            boolean removed) {

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

        Collection<ObservedHazardEvent> selectedEvents = sessionManager
                .getSelectionManager().getSelectedEvents();

        for (IHazardEvent eventToCheck : selectedEvents) {

            Map<IHazardEvent, Collection<String>> conflictingHazards = getConflictingEvents(
                    eventToCheck, eventToCheck.getStartTime(),
                    eventToCheck.getEndTime(),
                    eventToCheck.getFlattenedGeometry(),
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
                            this, Originator.OTHER));
        }

    }

    @Override
    public Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> getAllConflictingEvents() {

        Map<IHazardEvent, Map<IHazardEvent, Collection<String>>> conflictingHazardMap = new HashMap<>();
        /*
         * Find the union of the session events and those retrieved from the
         * hazard event manager. Ignore "Ended" events.
         */
        List<IHazardEvent> eventsToCheck = getEventsToCheckForConflicts(
                new HazardEventQueryRequest(),
                EnumSet.allOf(HazardStatus.class));

        for (IHazardEvent eventToCheck : eventsToCheck) {

            Map<IHazardEvent, Collection<String>> conflictingHazards = getConflictingEvents(
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

                    String ugcLabel = hazardTypeEntry.getUgcLabel();

                    List<IGeometryData> hatchedAreasForEvent = new ArrayList<>(
                            geoMapUtilities.buildHazardAreaForEvent(
                                    eventToCompare).values());

                    /*
                     * Retrieve matching events from the Hazard Event Manager
                     * Also, include those from the session state.
                     */
                    HazardEventQueryRequest queryRequest = new HazardEventQueryRequest(
                            HazardConstants.HAZARD_EVENT_START_TIME, ">",
                            eventToCompare.getStartTime()).and(
                            HazardConstants.HAZARD_EVENT_END_TIME, "<",
                            eventToCompare.getEndTime()).and(
                            HazardConstants.PHEN_SIG, hazardConflictList);
                    Set<HazardStatus> allowableStatuses = EnumSet.of(
                            HazardStatus.ISSUED, HazardStatus.ELAPSED,
                            HazardStatus.ENDING, HazardStatus.ENDED,
                            HazardStatus.PROPOSED);

                    List<IHazardEvent> eventsToCheck = getEventsToCheckForConflicts(
                            queryRequest, allowableStatuses);

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
                                    String otherUgcLabel = otherHazardTypeEntry
                                            .getUgcLabel();

                                    if (hazardTypeEntry != null) {
                                        List<IGeometryData> hatchedAreasEventToCheck = new ArrayList<>(
                                                geoMapUtilities
                                                        .buildHazardAreaForEvent(
                                                                eventToCheck)
                                                        .values());

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
     */
    private List<IHazardEvent> getEventsToCheckForConflicts(
            final HazardEventQueryRequest queryRequest,
            Set<HazardStatus> allowableStatuses) {

        /*
         * Iterate through all the events being managed in this session,
         * compiling a list of those that have not ended, as well as set of all
         * these non-ended event's identifiers.
         */
        List<IHazardEvent> eventsToCheck = new ArrayList<IHazardEvent>(
                getEvents());
        Set<String> eventIdentifiersToBeChecked = new HashSet<>(
                eventsToCheck.size(), 1.0f);

        for (IHazardEvent sessionEvent : new ArrayList<IHazardEvent>(
                eventsToCheck)) {
            if (sessionEvent.getStatus() != HazardStatus.ENDED) {
                eventIdentifiersToBeChecked.add(sessionEvent.getEventID());
            } else {
                eventsToCheck.remove(sessionEvent);
            }
        }

        /*
         * Retrieve the latest versions of matching events from the database
         * manager, and for any that are not ended, with allowable statuses, and
         * with identifiers that are not already in session, add them to the
         * list of events to be checked.
         */
        queryRequest
                .setInclude(Include.LATEST_OR_MOST_RECENT_HISTORICAL_EVENTS);
        Map<String, HazardEvent> eventMap = dbManager.queryLatest(queryRequest);
        for (Map.Entry<String, HazardEvent> entry : eventMap.entrySet()) {
            if ((eventIdentifiersToBeChecked.contains(entry.getKey()) == false)
                    && (entry.getValue() != null)
                    && (entry.getValue().getStatus() != HazardStatus.ENDED)
                    && allowableStatuses.contains(entry.getValue().getStatus())) {
                eventsToCheck.add(entry.getValue());
            }
        }

        return eventsToCheck;
    }

    @Override
    public void shutdown() {

        /*
         * TODO: Decide whether expiration is to be done in Java code or not.
         * For now, this code is commented out; no expiration of events will
         * occur. This is not a permanent solution.
         */
        // eventExpirationTimer.cancel();
        // eventExpirationTimer = null;
        SimulatedTime.getSystemTime().removeSimulatedTimeChangeListener(
                timeListener);
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
    private Map<IHazardEvent, Collection<String>> buildConflictMap(
            IHazardEvent firstEvent, IHazardEvent secondEvent,
            List<IGeometryData> hatchedAreasFirstEvent,
            List<IGeometryData> hatchedAreasSecondEvent,
            String firstEventLabelParameter, String secondEventLabelParameter) {

        Map<IHazardEvent, Collection<String>> conflictingHazardsMap = new HashMap<>();

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

                    if (hatchedArea.getGeometry().intersects(
                            hatchedAreaToCheck.getGeometry())) {

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

    @Override
    public void endEvent(ObservedHazardEvent event, IOriginator originator) {
        sessionManager.getSelectionManager().removeEventFromSelectedEvents(
                event.getEventID(), Originator.OTHER);
        event.setStatus(HazardStatus.ENDED, true, true, originator);
    }

    @Override
    public void issueEvent(ObservedHazardEvent event, IOriginator originator) {
    }

    @Override
    public Set<String> getEventIdsAllowingProposal() {
        List<ObservedHazardEvent> selectedEvents = sessionManager
                .getSelectionManager().getSelectedEvents();
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
            event.setStatus(HazardStatus.PROPOSED, true, true, originator);
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
        return ((HazardStatus.hasEverBeenIssued(status) == false)
                && (status != HazardStatus.PROPOSED) && HazardEventUtilities
                    .isHazardTypeValid(event));
    }

    @Override
    public void setHighResolutionGeometriesVisibleForSelectedEvents(
            IOriginator originator) {

        for (ObservedHazardEvent selectedEvent : sessionManager
                .getSelectionManager().getSelectedEvents()) {
            makeHighResolutionVisible(selectedEvent, originator);
        }
    }

    @Override
    public void setHighResolutionGeometryVisibleForCurrentEvent(
            IOriginator originator) {
        makeHighResolutionVisible(getCurrentEvent(), originator);

    }

    @Override
    public boolean setLowResolutionGeometriesVisibleForSelectedEvents(
            IOriginator originator) {
        for (ObservedHazardEvent selectedEvent : sessionManager
                .getSelectionManager().getSelectedEvents()) {
            Geometry lowResolutionGeometry;
            try {
                lowResolutionGeometry = buildLowResolutionEventGeometry(selectedEvent);
            } catch (HazardGeometryOutsideCWAException e) {
                warnUserOfGeometryOutsideCWA(selectedEvent);
                return false;
            }
            setLowResolutionGeometry(selectedEvent, lowResolutionGeometry,
                    originator);
        }

        return true;
    }

    @Override
    public boolean setLowResolutionGeometryVisibleForCurrentEvent(
            IOriginator originator) {
        ObservedHazardEvent hazardEvent = getCurrentEvent();
        Geometry lowResolutionGeometry;
        try {
            lowResolutionGeometry = buildLowResolutionEventGeometry(hazardEvent);
        } catch (HazardGeometryOutsideCWAException e) {
            warnUserOfGeometryOutsideCWA(hazardEvent);
            return false;
        }
        setLowResolutionGeometry(hazardEvent, lowResolutionGeometry, originator);
        return true;
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
        }
    }

    @Override
    public boolean canEventAreaBeChanged(ObservedHazardEvent hazardEvent) {
        return ((hazardEvent.getStatus() != HazardStatus.ELAPSED) && (hazardEvent
                .getStatus() != HazardStatus.ENDED));
    }

    @Override
    public void setCurrentEvent(String eventId) {
        setCurrentEvent(eventId == null ? null : getEventById(eventId));
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
    public boolean isCurrentEvent() {
        return currentEvent != null;
    }

    @Override
    public boolean isEventChecked(IHazardEvent event) {
        for (ObservedHazardEvent filteredEvent : getEventsForCurrentSettings()) {
            if (filteredEvent.getEventID().equals(event.getEventID())) {
                return checkedEventIdentifiers.contains(event.getEventID());
            }
        }
        return false;
    }

    @Override
    public void setEventChecked(IHazardEvent event, boolean checked,
            IOriginator originator) {
        String eventIdentifier = event.getEventID();
        boolean oldChecked = checkedEventIdentifiers.contains(eventIdentifier);
        if (oldChecked != checked) {
            if (checked) {
                checkedEventIdentifiers.add(eventIdentifier);
            } else {
                checkedEventIdentifiers.remove(eventIdentifier);
            }
            notificationSender
                    .postNotificationAsync(new SessionEventCheckedStateModified(
                            this, getEventById(eventIdentifier),
                            Originator.OTHER));
        }
    }

    @Override
    public void updateSelectedHazardUGCs() {

        for (IHazardEvent hazardEvent : sessionManager.getSelectionManager()
                .getSelectedEvents()) {
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

    @Override
    @SuppressWarnings("unchecked")
    public boolean isValidGeometryChange(IAdvancedGeometry geometry,
            ObservedHazardEvent hazardEvent, boolean checkGeometryValidity) {
        if (checkGeometryValidity && (geometry.isValid() == false)) {
            statusHandler.warn("Invalid geometry: "
                    + geometry.getValidityProblemDescription()
                    + "; geometry modification undone.");
            return false;
        }
        if (hasEverBeenIssued(hazardEvent)) {
            HazardTypeEntry hazardTypeEntry = configManager.getHazardTypes()
                    .get(HazardEventUtilities.getHazardType(hazardEvent));
            if ((hazardTypeEntry != null)
                    && (hazardTypeEntry.isAllowAreaChange() == false)) {
                List<String> oldUGCs = (List<String>) hazardEvent
                        .getHazardAttribute(HazardConstants.UGCS);
                BaseHazardEvent eventWithNewGeometry = new BaseHazardEvent(
                        hazardEvent);
                eventWithNewGeometry.setGeometry(geometry);
                List<String> newUGCs = updateUGCs(eventWithNewGeometry);
                newUGCs.removeAll(oldUGCs);

                if (!newUGCs.isEmpty()) {
                    statusHandler
                            .warn("This hazard event cannot be expanded in area.  Please create a new hazard event for the new areas.");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Map<String, String> buildInitialHazardAreas(IHazardEvent hazardEvent) {
        if (geoMapUtilities.isNonHatching(hazardEvent)) {
            return Collections.emptyMap();
        }
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

    @Override
    public void addOrRemoveEnclosingUGCs(Coordinate location,
            IOriginator originator) {
        List<ObservedHazardEvent> selectedEvents = sessionManager
                .getSelectionManager().getSelectedEvents();
        if (selectedEvents.size() != 1) {

            /*
             * TODO: This message is annoying to have pop up all the time when
             * one accidentally right-clicks. Explore having it pop up only when
             * the user has two or more selected events, not when there are no
             * events selected. Would that be better from a UX perspective?
             */
            messenger
                    .getWarner()
                    .warnUser(GEOMETRY_MODIFICATION_ERROR,
                            "Cannot add or remove UGCs unless exactly one hazard event is selected.");
            return;
        }
        ObservedHazardEvent hazardEvent = selectedEvents.get(0);
        String hazardType = hazardEvent.getHazardType();
        if (hazardType == null) {
            messenger
                    .getWarner()
                    .warnUser(GEOMETRY_MODIFICATION_ERROR,
                            "Cannot add or remove UGCs for a hazard with an undefined type.");
            return;
        }
        if (HazardStatus.endingEndedOrElapsed(hazardEvent.getStatus())) {
            messenger
                    .getWarner()
                    .warnUser(GEOMETRY_MODIFICATION_ERROR,
                            "Cannot add or remove UGCs for an ending, ended, or elapsed hazard.");
            return;
        }

        if ((hazardEvent.getStatus().equals(HazardStatus.PENDING) == false)
                && geoMapUtilities.isPointBasedHatching(hazardEvent)) {
            messenger
                    .getWarner()
                    .warnUser(GEOMETRY_MODIFICATION_ERROR,
                            "Can only add or remove UGCs for point hazards when they are pending.");
            return;
        }
        if (userConfirmationAsNecessary(hazardEvent) == false) {
            return;
        }
        makeHighResolutionVisible(hazardEvent, originator);

        /*
         * Get the modified hazard areas and geometry resulting from adding or
         * removing UGCs enclosing this location; if nothing is returned, an
         * error occurred during the toggling, so do nothing more.
         */
        Pair<Map<String, String>, Geometry> newHazardAreasAndGeometry = geoMapUtilities
                .addOrRemoveEnclosingUgcs(hazardEvent, location);
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
                    AdvancedGeometryUtilities.createGeometryWrapper(
                            modifiedGeometry, 0), hazardEvent, true) == false) {
                return;
            }
            hazardEvent.setGeometry(AdvancedGeometryUtilities
                    .createGeometryWrapper(modifiedGeometry, 0), originator);
        }

        /*
         * Use the returned hazard areas as the new ones for the hazard event.
         */
        hazardEvent.addHazardAttribute(HAZARD_AREA,
                (Serializable) newHazardAreasAndGeometry.getFirst(), true,
                originator);
    }

    private class HazardGeometryOutsideCWAException extends RuntimeException {

        private static final long serialVersionUID = -3178272501617218427L;

    }

    private Geometry buildLowResolutionEventGeometry(
            ObservedHazardEvent selectedEvent) {
        HazardTypes hazardTypes = configManager.getHazardTypes();

        HazardTypeEntry hazardType = hazardTypes.get(selectedEvent
                .getHazardType());

        /*
         * By default, just set the low res to the high res
         */
        Geometry result = selectedEvent.getFlattenedGeometry();

        if (isLowResComputationNeeded(selectedEvent, hazardType)) {

            /*
             * No clipping for National and for non-hatching.
             */
            if ((configManager.getSiteID().equals(HazardConstants.NATIONAL) == false)
                    && (geoMapUtilities.isNonHatching(selectedEvent) == false)) {
                if (geoMapUtilities.isWarngenHatching(selectedEvent)) {
                    result = geoMapUtilities.applyWarngenClipping(
                            selectedEvent, hazardType);
                    result = reduceGeometry(result, hazardType);
                    if (!result.isEmpty()) {
                        result = addGoosenecksAsNecessary(result);
                    }
                } else {
                    result = geoMapUtilities.applyGfeClipping(selectedEvent);
                }
            }

            if (result.isEmpty()) {
                throw new HazardGeometryOutsideCWAException();
            }
            if (result instanceof GeometryCollection == false) {
                result = geometryFactory
                        .createGeometryCollection(new Geometry[] { result });
            }
            return result;
        }
        return result;
    }

    private void warnUserOfGeometryOutsideCWA(ObservedHazardEvent selectedEvent) {
        StringBuffer warningMessage = new StringBuffer();
        warningMessage.append("Event ").append(selectedEvent.getEventID())
                .append(" ");
        warningMessage
                .append("has no hazard areas inside of the forecast area.\n");
        messenger.getWarner().warnUser("Product geometry calculation error",
                warningMessage.toString());
    }

    private void setLowResolutionGeometry(ObservedHazardEvent selectedEvent,
            Geometry lowResolutionGeometry, IOriginator originator) {
        selectedEvent.addHazardAttribute(VISIBLE_GEOMETRY,
                LOW_RESOLUTION_GEOMETRY_IS_VISIBLE, originator);
        selectedEvent.addHazardAttribute(LOW_RESOLUTION_GEOMETRY,
                lowResolutionGeometry, originator);
    }

    private void makeHighResolutionVisible(ObservedHazardEvent hazardEvent,
            IOriginator originator) {
        hazardEvent.addHazardAttribute(VISIBLE_GEOMETRY,
                HIGH_RESOLUTION_GEOMETRY_IS_VISIBLE, originator);

    }

    private boolean isLowResComputationNeeded(
            ObservedHazardEvent selectedEvent, HazardTypeEntry hazardType) {
        return hazardType != null
                && !geoMapUtilities.isPointBasedHatching(selectedEvent);
    }

    /**
     * Reduce Geometry to the Point Limit (20) if possible.
     * 
     * This method uses a VisvalingamWhyattSimplifier algorithm to reduce the
     * number of points used to render a polygon. This method is called from
     * {@link #buildLowResolutionEventGeometry(ObservedHazardEvent)}. Not all
     * event geometries are reduced to 20 points.
     * 
     * @param geometry
     * @param hazardTypeEntry
     * @return Geometry with reduced number of points.
     */
    private Geometry reduceGeometry(Geometry geometry,
            HazardTypeEntry hazardTypeEntry) {

        /*
         * Test if point reduction is necessary...
         */
        int pointLimit = hazardTypeEntry.getHazardPointLimit();

        if ((pointLimit > 0) && (geometry.getNumPoints() > pointLimit)) {

            if (geometry instanceof GeometryCollection) {
                geometry = VisvalingamWhyattSimplifier
                        .reduceGeometryCollection(
                                (GeometryCollection) geometry, pointLimit);
            } else {
                geometry = VisvalingamWhyattSimplifier.reducePolygon(
                        (Polygon) geometry, pointLimit);
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
        return new ArrayList<>(geoMapUtilities
                .getIntersectingMapGeometriesForUgcs(hazardEvent).keySet());
    }

    private Geometry addGoosenecksAsNecessary(Geometry productGeometry) {
        if ((!(productGeometry instanceof GeometryCollection))
                || (productGeometry.getNumGeometries() == 0)) {
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

    @Override
    public void saveEvents(List<IHazardEvent> events, boolean addToHistory,
            boolean treatAsIssuance) {

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
            return;
        } else if ("history".equals(persistenceBehavior)) {
            forceHistorical = true;
        }

        /*
         * Get copies of the events to be saved that are database-friendly.
         */
        List<HazardEvent> dbEvents = new ArrayList<>(events.size());
        for (IHazardEvent event : events) {

            /*
             * If the event has a status of "potential", log an error and skip
             * it.
             */
            if (HazardStatus.POTENTIAL.equals(event.getStatus())) {
                statusHandler.warn("Attempted to save hazard event "
                        + event.getEventID() + " to database, but "
                        + "cannot due to its status of \"potential\".");
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
         * respective history lists.
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
        }
    }

    @Override
    public void revertEventToLastSaved(String eventIdentifier) {
        if (getHistoricalVersionCountForEvent(eventIdentifier) > 0) {
            HazardHistoryList list = getEventHistoryById(eventIdentifier);
            mergeHazardEvents(list.get(list.size() - 1),
                    getEventById(eventIdentifier), false, false, false, true,
                    Originator.OTHER);
        }
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
    private HazardEvent createEventCopyToBePersisted(IHazardEvent event,
            boolean addToHistory, boolean justIssued) {
        HazardEvent dbEvent = dbManager.createEvent(event);

        /*
         * TODO: Visual features should not need to be removed, but they cause
         * thus-far-impossible-to-diagnose SOAP errors if there are too many of
         * them of too great a size and they are persisted. So for now, they are
         * being stripped out.
         */
        dbEvent.setVisualFeatures(null);

        /*
         * Strip out attributes as per this hazard type's configuration.
         */
        for (String sessionAttribute : configManager.getSessionAttributes(event
                .getHazardType())) {
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
            dbEvent.removeHazardAttribute(ATTR_ISSUED);
        }
        dbEvent.removeHazardAttribute(ATTR_HAZARD_CATEGORY);

        /*
         * TODO: Remove this once the HAZARD_EVENT_SELECTED attribute has been
         * entirely done away with.
         */
        dbEvent.removeHazardAttribute(HAZARD_EVENT_SELECTED);

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
    public void setAddCreatedEventsToSelected(boolean addCreatedEventsToSelected) {
        this.addCreatedEventsToSelected = addCreatedEventsToSelected;
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
}
