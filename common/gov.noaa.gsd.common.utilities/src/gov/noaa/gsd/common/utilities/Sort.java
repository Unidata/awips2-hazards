/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.utilities;

/**
 * Description: Encapsulation of a sort that may be performed, including the
 * identifier of the attribute used in determining ordering of two different
 * entities, the priority of this sort as compared to others, and the direction
 * in which to sort. They are {@link Comparable} by said priority.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 13, 2017   15556    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class Sort implements Comparable<Sort> {

    // Public Enumerated Types

    /**
     * Sort direction.
     */
    public enum SortDirection {

        // Values

        ASCENDING("Ascending"), DESCENDING("Descending");

        // Private Variables

        /**
         * Description.
         */
        private final String description;

        // Private Constructors

        /**
         * Construct a standard instance.
         */
        private SortDirection(String description) {
            this.description = description;
        }

        // Public Methods

        /**
         * Get a string description of this object.
         * 
         * @return String description.
         */
        @Override
        public String toString() {
            return description;
        }
    };

    // Private Constants

    /**
     * Identifier of the attribute by which to sort.
     */
    private final String attributeIdentifier;

    /**
     * Sort direction to be used.
     */
    private final SortDirection sortDirection;

    /**
     * Priority as compared to other instances.
     */
    private final int priority;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param attributeIdentifier
     *            Identifier of the attribute by which to sort.
     * @param sortDirection
     *            Direction in which to sort.
     * @param priority
     *            Priority as compared to other instances.
     */
    public Sort(String attributeIdentifier, SortDirection sortDirection,
            int priority) {
        this.attributeIdentifier = attributeIdentifier;
        this.sortDirection = sortDirection;
        this.priority = priority;
    }

    // Public Methods

    /**
     * Get the identifier of the attribute by which to sort.
     * 
     * @return Identifier of the attribute.
     */
    public String getAttributeIdentifier() {
        return attributeIdentifier;
    }

    /**
     * Get the direction in which to sort.
     * 
     * @return Sort direction.
     */
    public SortDirection getSortDirection() {
        return sortDirection;
    }

    /**
     * Get the priority of this instance as compared to others.
     * 
     * @return Priority.
     */
    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(Sort o) {
        return priority - o.priority;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Sort == false) {
            return false;
        }
        Sort otherSort = (Sort) other;
        return (attributeIdentifier.equals(otherSort.attributeIdentifier)
                && (sortDirection == otherSort.sortDirection) && (priority == otherSort.priority));
    }

    @Override
    public int hashCode() {
        return (int) ((((long) attributeIdentifier.hashCode())
                + ((long) sortDirection.hashCode()) + priority) % Integer.MAX_VALUE);
    }
}
