/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

/**
 * Exception thrown to indicate that an attempt to view or manipulate a stateful
 * megawidget's state has failed in some way.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 24, 2013            Chris.Golden      Initial creation
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IStateful
 * @see IMegawidget
 */
public class MegawidgetStateException extends MegawidgetException {

    // Private Static Constants

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -814802644768798603L;

    // Private Variables

    /**
     * State identifier.
     */
    private final String identifier;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param identifier
     *            State identifier.
     * @param type
     *            Type of the megawidget that failed to have its state
     *            manipulated.
     * @param badState
     *            State that failed to be applied to the megawidget for the
     *            specified <code>identifier</code>.
     * @param message
     *            Description of the problem, or <code>null</code> if none is
     *            required.
     */
    public MegawidgetStateException(String identifier, String type,
            Object badState, String message) {
        this(identifier, type, badState, message, null);
    }

    /**
     * Construct a standard instance that was caused by another throwable.
     * 
     * @param identifier
     *            State identifier.
     * @param type
     *            Type of the megawidget that failed to have its state
     *            manipulated.
     * @param badState
     *            State that failed to be applied to the megawidget for the
     *            specified <code>identifier</code>.
     * @param message
     *            Description of the problem, or <code>null</code> if none is
     *            required.
     * @param cause
     *            Nested cause of this problem, or <code>null</code> if there is
     *            none.
     */
    public MegawidgetStateException(String identifier, String type,
            Object badState, String message, Throwable cause) {
        super(type, badState, message, cause);
        this.identifier = identifier;
    }

    // Public Methods

    /**
     * Get the state identifier.
     * 
     * @return State identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return getClass().getName()
                + " "
                + (getType() != null ? MegawidgetSpecifier.MEGAWIDGET_TYPE
                        + " = " + getType() + ", " : "")
                + "state identifier = \"" + identifier + "\": invalid state \""
                + getBadValue() + "\""
                + (getMessage() != null ? ": " + getMessage() : "");
    }
}
