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
 * Exception thrown to indicate that a megawidget or its specifier has suffered
 * an error of some kind.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 24, 2013            Chris.Golden      Initial creation
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see MegawidgetSpecifier
 * @see IMegawidget
 */
public class MegawidgetException extends Exception {

    // Private Static Constants

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 190357757090377694L;

    // Private Constants

    /**
     * Identifier.
     */
    private final String identifier;

    /**
     * Type of the megawidget or specifier suffering the problem, or <code>null
     * </code> if the type was not able to be determined.
     */
    private final String type;

    /**
     * Invalid value that caused the problem.
     */
    private final Object badValue;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param identifier
     *            Identifier associated with the problem, or <code>null</code>
     *            if the identifier was not able to be determined.
     * @param type
     *            Type of the megawidget or specifier suffering the problem, or
     *            <code>null</code> if the type was not able to be determined.
     * @param badValue
     *            Invalid value that caused the problem.
     * @param message
     *            Description of the problem, or <code>null</code> if none is
     *            required.
     */
    public MegawidgetException(String identifier, String type, Object badValue,
            String message) {
        this(identifier, type, badValue, message, null);
    }

    /**
     * Construct a standard instance that was caused by another throwable.
     * 
     * @param identifier
     *            Identifier associated with the problem, or <code>null</code>
     *            if the identifier was not able to be determined.
     * @param type
     *            Type of the megawidget or specifier suffering the problem, or
     *            <code>null</code> if the type was not able to be determined.
     * @param badValue
     *            Invalid value that caused the problem, or <code>null</code> if
     *            no such value can be identified.
     * @param message
     *            Description of the problem, or <code>null</code> if none is
     *            required.
     * @param cause
     *            Nested cause of this problem, or <code>null</code> if there is
     *            none.
     */
    public MegawidgetException(String identifier, String type, Object badValue,
            String message, Throwable cause) {
        super(message, cause);
        this.identifier = identifier;
        this.type = type;
        this.badValue = badValue;
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

    /**
     * Get the type of the problematic specification.
     * 
     * @return Type of the problematic specification, or <code>null</code> if
     *         one could not be determined.
     */
    public String getType() {
        return type;
    }

    /**
     * Get the invalid value that caused the problem.
     * 
     * @return Invalid value that caused the problem, or or <code>null</code> if
     *         no such value can be identified.
     */
    public Object getBadValue() {
        return badValue;
    }
}
