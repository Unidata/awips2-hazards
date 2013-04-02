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
 * 
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
     * Identifier of the invalid megawidget specification, or <code>null</code>
     * if the identifier was not able to be determined.
     */
    private final String identifier;

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
        super(type, badParamValue, message, cause);
        this.identifier = identifier;
        this.badParamName = badParamName;
    }

    // Public Methods

    /**
     * Get the identifier of the problematic specification.
     * 
     * @return Identifier of the problematic specification, or <code>null</code>
     *         if one could not be found.
     */
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        String typeAndId = ((identifier != null) && (getType() != null) ? " ("
                + MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER + " = \""
                + identifier + "\", " + MegawidgetSpecifier.MEGAWIDGET_TYPE
                + " = " + getType() + ")" : (identifier != null ? " ("
                + MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER + " = "
                + identifier + ")" : (getType() != null ? " ("
                + MegawidgetSpecifier.MEGAWIDGET_TYPE + " = " + getType() + ")"
                : "")));
        return getClass().getName()
                + typeAndId
                + (badParamName != null ? ": parameter \""
                        + badParamName
                        + "\" "
                        + (getBadValue() == null ? "missing value"
                                : "has illegal value \"" + getBadValue()
                                        + "\" of type "
                                        + getBadValue().getClass().getName())
                        : "")
                + (getMessage() != null ? ": " + getMessage() : "");
    }
}
