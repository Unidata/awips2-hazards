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

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param name
     *            Name of the recommender that is the origin; must not be
     *            <code>null</code>.
     */
    public RecommenderOriginator(String name) {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }
        this.name = name;
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
    public boolean equals(Object other) {
        return ((other instanceof RecommenderOriginator) && name
                .equals(((RecommenderOriginator) other).name));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "RecommenderOriginator(" + name + ")";
    }
}
