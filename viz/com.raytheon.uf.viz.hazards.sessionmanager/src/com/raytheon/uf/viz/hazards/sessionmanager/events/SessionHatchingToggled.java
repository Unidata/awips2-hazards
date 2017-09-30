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

import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;

import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Description: Notification of the toggling of hazard hatching.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 13, 2014    4959    Dan Schaffer Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Implemented merge() method.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class SessionHatchingToggled extends OriginatedSessionNotification {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param originator
     *            Originator of the change.
     */
    public SessionHatchingToggled(IOriginator originator) {
        super(originator);
    }

    // Public Methods

    @Override
    public MergeResult<ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {
        return getMergeResultNullifyingSubjectIfSameClassAndOriginator(original,
                modified);
    }
}
