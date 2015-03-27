/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Description: Notification of a change to an event's metadata, meaning the set
 * of attribute keys that apply to the event, the megawidget specifiers for
 * those attributes, and/or the side effects script used for megawidget
 * interdependency. This generally indicates a more sweeping change than that
 * signaled by a {@link SessionEventAttributesModified} notification.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 23, 2014  2925      Chris.Golden      Initial creation.
 * Apr 10, 2015  6898      Chris.Cody  Refactored async messaging
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionEventMetadataModified extends SessionEventModified {

    public SessionEventMetadataModified(IHazardEvent event,
            IOriginator originator) {
        super(event, originator);
    }
}
