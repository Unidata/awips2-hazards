/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.events.impl;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_CHECKED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SELECTED;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.Originator;

/**
 * Description: Utilities used by session events code.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 15, 2013            daniel.s.schaffer@noaa.gov      Initial creation
 * Aug 26, 2013  1921      blawrenc    Added removal of "replaces" key from
 *                                     event attributes.
 * Aug 29, 2013  1921      blawrenc    Re-added logic to properly set the state of 
 *                                     the old event. Moved logic to remove 
 *                                     "replaces" information from event.
 * Nov 14, 2013  1472      bkowal      Renamed hazard subtype to subType
 * Aug 17, 2015  9968      Chris.Cody  Changes for processing ENDED/ELAPSED/EXPIRED events
 * Mar 03, 2016 14004      Chris.Golden Added new mergeHazardEvent() method that deals
 *                                      with ObservedHazardEvent merges specifically,
 *                                      allowing the originator of modifications to be
 *                                      passed to the event so that it can send out the
 *                                      appropriate originator in its notifications.
 * Mar 06, 2016 15676      Chris.Golden Added visual features to merging methods.
 * Apr 28, 2016 18267      Chris.Golden Removed unneeded version of mergeHazardEvents()
 *                                      method, and changed remaining method with the
 *                                      same name to use the event manager to set some
 *                                      of an event's properties, as well as simplifying
 *                                      its attribute-setting code. The former needed
 *                                      to be done so that, for example, event time range
 *                                      boundaries are respected (the event itself does
 *                                      not know about these boundaries, but the event
 *                                      manager does; thus going through the latter is
 *                                      better).
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class SessionEventUtilities {

    private static final Set<String> ATTRIBUTES_TO_RETAIN = ImmutableSet.of(
            HAZARD_EVENT_CHECKED, HAZARD_EVENT_SELECTED,
            ISessionEventManager.ATTR_ISSUED);

    /**
     * Merge the contents of the new event into the old event, using the
     * specified originator for any notifications that are sent out as a result.
     * 
     * @param eventManager
     *            Session event manager.
     * @param newEvent
     *            Event to be merged into the old event.
     * @param oldEvent
     *            Event into which to merge the new event.
     * @param forceMerge
     *            If <code>true</code>, the event manager will not be used to
     *            set time range and hazard type, meaning the values from the
     *            new event will not be checked for correctness before being
     *            merged into the old event. If <code>false</code>, such checks
     *            will occur.
     * @param originator
     *            Originator of this action.
     */
    public static void mergeHazardEvents(
            ISessionEventManager<ObservedHazardEvent> eventManager,
            IHazardEvent newEvent, ObservedHazardEvent oldEvent,
            boolean forceMerge, IOriginator originator) {
        oldEvent.setSiteID(newEvent.getSiteID(), originator);
        if (forceMerge) {
            oldEvent.setTimeRange(newEvent.getStartTime(),
                    newEvent.getEndTime(), originator);
        } else {
            eventManager.setEventTimeRange(oldEvent, newEvent.getStartTime(),
                    newEvent.getEndTime(), originator);
        }
        oldEvent.setCreationTime(newEvent.getCreationTime(), originator);
        oldEvent.setGeometry(newEvent.getGeometry(), originator);
        oldEvent.setVisualFeatures(newEvent.getBaseVisualFeatures(),
                newEvent.getSelectedVisualFeatures(), originator);
        if (forceMerge) {
            oldEvent.setHazardType(newEvent.getPhenomenon(),
                    newEvent.getSignificance(), newEvent.getSubType(),
                    originator);
        } else {
            eventManager.setEventType(oldEvent, newEvent.getPhenomenon(),
                    newEvent.getSignificance(), newEvent.getSubType(),
                    originator);
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
        for (String key : ATTRIBUTES_TO_RETAIN) {
            if ((newAttr.containsKey(key) == false) && oldAttr.containsKey(key)) {
                newAttr.put(key, oldAttr.get(key));
            }
        }
        oldEvent.setHazardAttributes(newAttr, originator);

        /*
         * This is relevant when you set the clock back.
         */
        if (isEnded(oldEvent) == false) {
            oldEvent.setStatus(newEvent.getStatus(), true, true,
                    Originator.OTHER);
        }
    }

    /**
     * Determine if the {@link IHazardEvent} has ended based on the time that is
     * being used.
     * 
     * @param event
     * @return
     */
    public static boolean isEnded(IHazardEvent event) {
        if (event.getStatus() == HazardStatus.ENDED) {
            return true;
        }
        return false;
    }

    /**
     * Determine if the {@link IHazardEvent} has been ENDED or has ELAPSED based
     * on the time that is being used.
     * 
     * @param event
     * @return true if the event was set to ELAPSED, set to ENDED or if the
     *         current time is later than the End time (ELAPSED).
     */
    public static boolean isEndedOrElapsed(IHazardEvent event) {
        Date currTime = SimulatedTime.getSystemTime().getTime();
        HazardStatus status = event.getStatus();
        if ((status == HazardStatus.ENDED)
                || (status == HazardStatus.ELAPSED)
                || (HazardStatus.issuedButNotEndedOrElapsed(status) && (event
                        .getEndTime().before(currTime)))) {
            return true;
        }
        return false;
    }

    /**
     * Determine if the {@link IHazardEvent} has ELAPSED based on the time that
     * is being used.
     * 
     * @param event
     * @return true if the event was set to ELAPSED, or if the current time is
     *         later than the End time (ELAPSED).
     */
    public static boolean isElapsed(IHazardEvent event) {
        Date currTime = SimulatedTime.getSystemTime().getTime();
        HazardStatus status = event.getStatus();
        if ((status == HazardStatus.ELAPSED)
                || (HazardStatus.issuedButNotEndedOrElapsed(status) && (event
                        .getEndTime().before(currTime)))) {
            return true;
        }
        return false;
    }

    public static boolean isPastExpirationTime(IHazardEvent event) {
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

}
