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

import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;

/**
 * Description: Notification of a change related to the selection set.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 09, 2017   15556    Chris.Golden Initial creation.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SessionSelectionModified extends OriginatedSessionNotification {

    // Private Variables

    /**
     * Session selection manager.
     */
    private final ISessionSelectionManager selectionManager;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param selectionManager
     *            Selection manager.
     * @param originator
     *            Originator of the event.
     */
    public SessionSelectionModified(ISessionSelectionManager selectionManager,
            IOriginator originator) {
        super(originator);
        this.selectionManager = selectionManager;
    }

    // Public Methods

    /**
     * Get the selection manager.
     * 
     * @return Selection manager.
     */
    public ISessionSelectionManager getSelectionManager() {
        return selectionManager;
    }
}
