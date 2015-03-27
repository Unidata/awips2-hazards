/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.time;

import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;

/**
 * A notification that will be sent out through the SessionManager to notify all
 * components that the visible time range has changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 19, 2014 2925       Chris.Golden    Initial creation.
 * May 10, 2014 2925       Chris.Golden    Added originator.
 * Nov 18, 2014 4124       Chris.Golden    Changed to work with revamped time
 *                                         manager.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class VisibleTimeRangeChanged extends OriginatedSessionNotification {

    public VisibleTimeRangeChanged(IOriginator originator) {
        super(originator);
    }
}
