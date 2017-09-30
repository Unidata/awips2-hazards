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
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Description: Notification of a change to the last accessed event in the list
 * of selected events.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 09, 2014    2925    Chris.Golden Initial creation.
 * Feb 01, 2017   15556    Chris.Golden Changed to be based upon new superclass
 *                                      and renamed it to indicate that it
 *                                      means the last accessed event has changed,
 *                                      not the last modified (since sometimes
 *                                      a hazard event is merely topmost in a
 *                                      GUI view, but not modified; it is still
 *                                      the last accessed, which is what is
 *                                      important).
 * Sep 27, 2017   38072    Chris.Golden Implemented merge() method.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionLastAccessedEventModified extends SessionSelectionModified {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param selectionManager
     *            Selection manager.
     * @param originator
     *            Originator of the event.
     */
    public SessionLastAccessedEventModified(
            ISessionSelectionManager<ObservedHazardEvent> selectionManager,
            IOriginator originator) {
        super(selectionManager, originator);
    }

    // Public Methods

    @Override
    public MergeResult<ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {
        return getMergeResultNullifyingSubjectIfSameClassAndOriginator(original,
                modified);
    }
}
