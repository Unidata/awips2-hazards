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
import java.util.Map.Entry;

import com.google.common.collect.Maps;
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
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class SessionEventUtilities {

    /**
     * Merge the contents of the newEvent into the oldEvent
     * 
     * TODO. This hopefully can go away once we have the history list. Under
     * that scenario, we can use the copy constructor to just create a new
     * hazard and push it on to the history list.
     */
    public static void mergeHazardEvents(IHazardEvent newEvent,
            IHazardEvent oldEvent) {
        oldEvent.setSiteID(newEvent.getSiteID());
        oldEvent.setTimeRange(newEvent.getStartTime(), newEvent.getEndTime());
        oldEvent.setCreationTime(newEvent.getCreationTime());
        oldEvent.setGeometry(newEvent.getGeometry());
        oldEvent.setVisualFeatures(newEvent.getBaseVisualFeatures(),
                newEvent.getSelectedVisualFeatures());
        oldEvent.setPhenomenon(newEvent.getPhenomenon());
        oldEvent.setSignificance(newEvent.getSignificance());
        oldEvent.setSubType(newEvent.getSubType());
        oldEvent.setHazardMode(newEvent.getHazardMode());
        Map<String, Serializable> newAttr = newEvent.getHazardAttributes();
        Map<String, Serializable> oldAttr = oldEvent.getHazardAttributes();
        if (oldAttr != null) {
            oldAttr = new HashMap<String, Serializable>(oldAttr);
        } else {
            oldAttr = new HashMap<String, Serializable>();
        }

        if (newAttr != null) {
            /*
             * Aggregate the changes so that only one notification will occur.
             */
            Map<String, Serializable> modifiedAttributes = Maps.newHashMap();
            for (Entry<String, Serializable> entry : newAttr.entrySet()) {
                modifiedAttributes.put(entry.getKey(), entry.getValue());
                oldAttr.remove(entry.getKey());
            }
            oldEvent.addHazardAttributes(modifiedAttributes);
        } else {
            newAttr = Collections.emptyMap();
        }
        oldAttr.remove(HAZARD_EVENT_CHECKED);
        oldAttr.remove(HAZARD_EVENT_SELECTED);
        oldAttr.remove(ISessionEventManager.ATTR_ISSUED);

        for (String key : oldAttr.keySet()) {
            oldEvent.removeHazardAttribute(key);
        }

        /*
         * This is relevant when you set the clock back.
         */
        if (isEnded(oldEvent) == false) {
            if (oldEvent instanceof ObservedHazardEvent) {
                ObservedHazardEvent obEvent = (ObservedHazardEvent) oldEvent;
                obEvent.setStatus(newEvent.getStatus(), true, true,
                        Originator.OTHER);
            } else {
                oldEvent.setStatus(newEvent.getStatus());
            }
        }
    }

    /**
     * Merge the contents of the new event into the old event, using the
     * specified originator for any notifications that are sent out as a result.
     * 
     * TODO. This hopefully can go away once we have the history list. Under
     * that scenario, we can use the copy constructor to just create a new
     * hazard and push it on to the history list.
     */
    public static void mergeHazardEvents(IHazardEvent newEvent,
            ObservedHazardEvent oldEvent, IOriginator originator) {
        oldEvent.setSiteID(newEvent.getSiteID(), originator);
        oldEvent.setTimeRange(newEvent.getStartTime(), newEvent.getEndTime(),
                originator);
        oldEvent.setCreationTime(newEvent.getCreationTime(), originator);
        oldEvent.setGeometry(newEvent.getGeometry(), originator);
        oldEvent.setVisualFeatures(newEvent.getBaseVisualFeatures(),
                newEvent.getSelectedVisualFeatures(), originator);
        oldEvent.setPhenomenon(newEvent.getPhenomenon(), originator);
        oldEvent.setSignificance(newEvent.getSignificance(), originator);
        oldEvent.setSubType(newEvent.getSubType(), originator);
        oldEvent.setHazardMode(newEvent.getHazardMode(), originator);
        Map<String, Serializable> newAttr = newEvent.getHazardAttributes();
        Map<String, Serializable> oldAttr = oldEvent.getHazardAttributes();
        if (oldAttr != null) {
            oldAttr = new HashMap<String, Serializable>(oldAttr);
        } else {
            oldAttr = new HashMap<String, Serializable>();
        }

        if (newAttr != null) {
            /*
             * Aggregate the changes so that only one notification will occur.
             */
            Map<String, Serializable> modifiedAttributes = Maps.newHashMap();
            for (Entry<String, Serializable> entry : newAttr.entrySet()) {
                modifiedAttributes.put(entry.getKey(), entry.getValue());
                oldAttr.remove(entry.getKey());
            }
            oldEvent.addHazardAttributes(modifiedAttributes, originator);
        } else {
            newAttr = Collections.emptyMap();
        }
        oldAttr.remove(HAZARD_EVENT_CHECKED);
        oldAttr.remove(HAZARD_EVENT_SELECTED);
        oldAttr.remove(ISessionEventManager.ATTR_ISSUED);

        for (String key : oldAttr.keySet()) {
            oldEvent.removeHazardAttribute(key, originator);
        }

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
