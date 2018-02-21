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
 * Recommender originator, used to indicate that a recommender was the origin of
 * a change.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 03, 2016   14004    Chris.Golden Initial creation.
 * Dec 17, 2017   20739    Chris.Golden Added methods to determine whether
 *                                      or not they are the result of direct
 *                                      user input, and whether or not they
 *                                      require hazard events to not be
 *                                      locked by other workstations.
 * Feb 21, 2018   46736    Chris.Golden Changed to allow the "not locked by
 *                                      others required" property to be
 *                                      configurable when created, so that
 *                                      recommenders can merge their changes
 *                                      into the session copies of the events
 *                                      without locking them when appropriate.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class RecommenderOriginator implements IOriginator {

    // Private Variables

    /**
     * Name of the recommender that is the origin.
     */
    private final String name;

    /**
     * Flag indicating whether or not actions with this originator should, when
     * being applied to hazard events, require that the event not be locked by
     * another workstation.
     */
    private final boolean notLockedByOthersRequired;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param name
     *            Name of the recommender that is the origin; must not be
     *            <code>null</code>.
     * @param notLockedByOthersRequired
     *            Flag indicating whether or not actions with this originator
     *            should, when being applied to hazard events, require that the
     *            event not be locked by another workstation.
     */
    public RecommenderOriginator(String name,
            boolean notLockedByOthersRequired) {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }
        this.name = name;
        this.notLockedByOthersRequired = notLockedByOthersRequired;
    }

    // Public Methods

    /**
     * Get the name of the recommender that is the origin.
     * 
     * @return Name.
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean isDirectResultOfUserInput() {
        return false;
    }

    @Override
    public boolean isNotLockedByOthersRequired() {
        return notLockedByOthersRequired;
    }

    @Override
    public boolean equals(Object other) {

        /*
         * The flag concerning locking is ignored for the purposes of this
         * method, since the point is to see whether the names are the same.
         */
        return ((other instanceof RecommenderOriginator)
                && name.equals(((RecommenderOriginator) other).name));
    }

    @Override
    public int hashCode() {

        /*
         * The flag is ignored here; see the comment about this in the equals()
         * implementation.
         */
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "RecommenderOriginator(" + name + ")";
    }
}
