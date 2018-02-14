/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.originator;

/**
 * Revert originator, used to indicate that a revert of an event was the origin
 * of a change.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------- --------------------------
 * Feb 06, 2018   46258    Chris.Golden  Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public enum RevertOriginator implements IOriginator {
    USER(true), OTHER(false);

    // Private Variables

    /**
     * Flag indicating whether or not the originator is the result of direct
     * user input, and also whether it requires that the hazard event being
     * reverted is not locked by others.
     */
    private final boolean userInitiated;

    // Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param userInitiated
     *            Flag indicating whether or not the originator is the result of
     *            direct user input, and also whether it requires that the
     *            hazard event being reverted is not locked by others.
     */
    private RevertOriginator(boolean userInitiated) {
        this.userInitiated = userInitiated;
    }

    // Public Methods

    @Override
    public boolean isDirectResultOfUserInput() {
        return userInitiated;
    }

    @Override
    public boolean isNotLockedByOthersRequired() {
        return userInitiated;
    }
}
