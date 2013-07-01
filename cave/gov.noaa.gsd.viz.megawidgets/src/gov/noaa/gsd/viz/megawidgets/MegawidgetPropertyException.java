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
 * Exception thrown to indicate that an attempt to view or manipulate a
 * megawidget's property has failed in some way.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 24, 2013   1277     Chris.Golden      Initial creation
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IMegawidget
 */
public class MegawidgetPropertyException extends MegawidgetException {

    // Private Static Constants

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -1163827416377645338L;

    // Private Variables

    /**
     * Property name.
     */
    private final String name;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param identifier
     *            Identifier associated with the problem, or <code>null</code>
     *            if the identifier was not able to be determined.
     * @param name
     *            Property name.
     * @param type
     *            Type of the megawidget that failed to have its property
     *            manipulated.
     * @param badValue
     *            Property value that failed to be applied to the megawidget for
     *            the specified <code>name</code>, or <code>null</code> if no
     *            such value was a part of the problem.
     * @param message
     *            Description of the problem, or <code>null</code> if none is
     *            required.
     */
    public MegawidgetPropertyException(String identifier, String name,
            String type, Object badValue, String message) {
        this(identifier, name, type, badValue, message, null);
    }

    /**
     * Construct a standard instance that was caused by another throwable.
     * 
     * @param identifier
     *            Identifier associated with the problem, or <code>null</code>
     *            if the identifier was not able to be determined.
     * @param name
     *            Property name.
     * @param type
     *            Type of the megawidget that failed to have its property
     *            manipulated.
     * @param badValue
     *            Property value that failed to be applied to the megawidget for
     *            the specified <code>name</code>, or <code>null</code> if no
     *            such value was a part of the problem.
     * @param message
     *            Description of the problem, or <code>null</code> if none is
     *            required.
     * @param cause
     *            Nested cause of this problem, or <code>null</code> if there is
     *            none.
     */
    public MegawidgetPropertyException(String identifier, String name,
            String type, Object badValue, String message, Throwable cause) {
        super(identifier, type, badValue, message, cause);
        this.name = name;
    }

    // Public Methods

    /**
     * Get the property name.
     * 
     * @return Property name.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getName());
        if ((getIdentifier() != null) && (getType() != null)) {
            builder.append(" (" + MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER
                    + " = \"" + getIdentifier() + "\", "
                    + MegawidgetSpecifier.MEGAWIDGET_TYPE + " = " + getType()
                    + ",");
        } else if (getIdentifier() != null) {
            builder.append(" (" + MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER
                    + " = " + getIdentifier() + ",");
        } else if (getType() != null) {
            builder.append(" (" + MegawidgetSpecifier.MEGAWIDGET_TYPE + " = "
                    + getType() + ",");
        }
        builder.append(" property name = \"" + name + "\"");
        if (getBadValue() != null) {
            builder.append(": invalid value \"" + getBadValue() + "\"");
        }
        if (getMessage() != null) {
            builder.append(": " + getMessage());
        }
        return builder.toString();
    }
}
