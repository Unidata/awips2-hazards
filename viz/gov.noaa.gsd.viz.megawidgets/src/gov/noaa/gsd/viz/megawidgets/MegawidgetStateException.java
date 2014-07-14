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
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Feb 12, 2014   2161     Chris.Golden      Added nested cause's description to
 *                                           toString().
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
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
        super(identifier, type, badState, message, cause);
    }

    /**
     * Construct a standard instance based upon the specified exception.
     * 
     * @param exception
     *            Exception upon which to base this instance.
     */
    public MegawidgetStateException(MegawidgetException exception) {
        super(exception.getIdentifier(), exception.getType(), exception
                .getBadValue(), exception.getMessage(), exception.getCause());
    }

    // Public Methods

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getName() + " ");
        if (getType() != null) {
            builder.append(MegawidgetSpecifier.MEGAWIDGET_TYPE + " = "
                    + getType() + ", ");
        }
        builder.append("state identifier = \"" + getIdentifier()
                + "\": invalid state \"" + getBadValue() + "\"");
        if (getMessage() != null) {
            builder.append(": " + getMessage());
        }
        if (getCause() != null) {
            builder.append(" (caused by: " + getCause().toString() + ")");
        }
        return builder.toString();
    }
}
