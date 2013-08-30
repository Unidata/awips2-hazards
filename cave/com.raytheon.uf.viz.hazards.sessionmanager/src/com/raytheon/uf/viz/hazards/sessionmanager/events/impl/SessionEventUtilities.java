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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;

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
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class SessionEventUtilities {

    /**
     * Merge the contents of the newEvent into the oldEvent
     */
    public static void mergeHazardEvents(IHazardEvent newEvent,
            IHazardEvent oldEvent) {
        oldEvent.setSiteID(newEvent.getSiteID());
        oldEvent.setEndTime(newEvent.getEndTime());
        oldEvent.setStartTime(newEvent.getStartTime());
        oldEvent.setIssueTime(newEvent.getIssueTime());
        oldEvent.setGeometry(newEvent.getGeometry());
        oldEvent.setPhenomenon(newEvent.getPhenomenon());
        oldEvent.setSignificance(newEvent.getSignificance());
        oldEvent.setSubtype(newEvent.getSubtype());
        oldEvent.setHazardMode(newEvent.getHazardMode());
        Map<String, Serializable> newAttr = newEvent.getHazardAttributes();
        Map<String, Serializable> oldAttr = oldEvent.getHazardAttributes();
        if (oldAttr != null) {
            oldAttr = new HashMap<String, Serializable>(oldAttr);
        } else {
            oldAttr = new HashMap<String, Serializable>();
        }
        if (newAttr != null) {
            for (Entry<String, Serializable> entry : newAttr.entrySet()) {
                oldEvent.addHazardAttribute(entry.getKey(), entry.getValue());
                oldAttr.remove(entry.getKey());
            }
        } else {
            newAttr = Collections.emptyMap();
        }
        oldAttr.remove(ISessionEventManager.ATTR_CHECKED);
        oldAttr.remove(ISessionEventManager.ATTR_SELECTED);
        oldAttr.remove(ISessionEventManager.ATTR_ISSUED);

        for (String key : oldAttr.keySet()) {
            oldEvent.removeHazardAttribute(key);
        }

        if (oldEvent instanceof ObservedHazardEvent) {
            ObservedHazardEvent obEvent = ((ObservedHazardEvent) oldEvent);
            obEvent.setState(newEvent.getState(), true, false);
        } else {
            oldEvent.setState(newEvent.getState());
        }
    }
}
