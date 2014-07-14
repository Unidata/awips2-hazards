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
 * Exception thrown to indicate that a megawidget specification is not valid in
 * some way.
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
 * @see MegawidgetSpecifier
 */
public class MegawidgetSpecificationException extends MegawidgetException {

    // Private Static Constants

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -8957251278649153793L;

    // Private Constants

    /**
     * Name of the parameter that is missing or faulty in the specification.
     */
    private final String badParamName;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param identifier
     *            Identifier of the megawidget specifier, or <code>null</code>
     *            if this could not be determined.
     * @param type
     *            Type of the megawidget being specified, or <code>null</code>
     *            if this could not be determined.
     * @param badParamName
     *            Name of the parameter with a problematic value within the
     *            faulty specification.
     * @param badParamValue
     *            Value of the parameter that is problematic within the faulty
     *            specification.
     * @param message
     *            Description of the problem, or <code>null</code> if none is
     *            required.
     */
    public MegawidgetSpecificationException(String identifier, String type,
            String badParamName, Object badParamValue, String message) {
        this(identifier, type, badParamName, badParamValue, message, null);
    }

    /**
     * Construct a standard instance that was caused by another throwable.
     * 
     * @param identifier
     *            Identifier of the megawidget specifier, or <code>null</code>
     *            if this could not be determined.
     * @param type
     *            Type of the megawidget being specified, or <code>null</code>
     *            if this could not be determined.
     * @param badParamName
     *            Name of the parameter with a problematic value within the
     *            faulty specification.
     * @param badParamValue
     *            Value of the parameter that is problematic within the faulty
     *            specification.
     * @param message
     *            Description of the problem, or <code>null</code> if none is
     *            required.
     * @param cause
     *            Nested cause of this problem, or <code>null</code> if there is
     *            none.
     */
    public MegawidgetSpecificationException(String identifier, String type,
            String badParamName, Object badParamValue, String message,
            Throwable cause) {
        super(identifier, type, badParamValue, message, cause);
        this.badParamName = badParamName;
    }

    /**
     * Construct a standard instance based upon the specified exception.
     * 
     * @param badParamName
     *            Bad parameter name.
     * @param exception
     *            Exception upon which to base this instance.
     */
    public MegawidgetSpecificationException(String badParamName,
            MegawidgetException exception) {
        super(exception.getIdentifier(), exception.getType(), exception
                .getBadValue(), exception.getMessage(), exception.getCause());
        this.badParamName = badParamName;
    }

    // Public Methods

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getName());
        if ((getIdentifier() != null) && (getType() != null)) {
            builder.append(" (" + MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER
                    + " = \"" + getIdentifier() + "\", "
                    + MegawidgetSpecifier.MEGAWIDGET_TYPE + " = " + getType()
                    + ")");
        } else if (getIdentifier() != null) {
            builder.append(" (" + MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER
                    + " = " + getIdentifier() + ")");
        } else if (getType() != null) {
            builder.append(" (" + MegawidgetSpecifier.MEGAWIDGET_TYPE + " = "
                    + getType() + ")");
        }
        if (badParamName != null) {
            builder.append(": parameter \"" + badParamName + "\" ");
            if (getBadValue() == null) {
                builder.append("missing value");
            } else {
                builder.append("has illegal value \"" + getBadValue()
                        + "\" of type " + getBadValue().getClass().getName());
            }
        }
        if (getMessage() != null) {
            builder.append(": " + getMessage());
        }
        if (getCause() != null) {
            builder.append(" (caused by: " + getCause().toString() + ")");
        }
        return builder.toString();
    }
}
