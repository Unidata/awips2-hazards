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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ATTR_ISSUED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_CHECKED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SELECTED;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.time.SimulatedTime;

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
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class SessionEventUtilities {

    /**
     * Merge the contents of the newEvent into the oldEvent
     * <p>
     * NOTE: This method does NOT send Event Status Messages.
     * <p>
     * TODO. This hopefully can go away once we have the history list. Under
     * that scenario, we can use the copy constructor to just create a new
     * hazard and push it on to the history list.
     */
    public static void mergeHazardEvents(IHazardEvent newEvent,
            IHazardEvent oldEvent) {
        oldEvent.setSiteID(newEvent.getSiteID());
        oldEvent.setEndTime(newEvent.getEndTime());
        oldEvent.setStartTime(newEvent.getStartTime());
        oldEvent.setCreationTime(newEvent.getCreationTime());
        oldEvent.setGeometry(newEvent.getGeometry());
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
        oldAttr.remove(ATTR_ISSUED);

        for (String key : oldAttr.keySet()) {
            oldEvent.removeHazardAttribute(key);
        }

        /*
         * This is relevant when you set the clock back.
         */
        if (isEnded(oldEvent) == false) {
            oldEvent.setStatus(newEvent.getStatus());
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
        Date currTime = SimulatedTime.getSystemTime().getTime();
        if (event.getStatus() == HazardStatus.ENDED
                || (HazardStatus.issuedButNotEnded(event.getStatus()) && (event
                        .getEndTime().before(currTime)))) {
            return true;
        }
        return false;
    }
}
