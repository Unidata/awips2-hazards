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

import java.util.Map;

/**
 * Integer spinner specifier. The integer value is always associated with a
 * single state identifier, so the megawidget identifiers for these specifiers
 * must not consist of colon-separated substrings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IntegerSpinnerMegawidget
 */
public class IntegerSpinnerSpecifier extends
        BoundedValueMegawidgetSpecifier<Integer> {

    // Public Static Constants

    /**
     * State value increment parameter name; a megawidget may include an integer
     * as the value associated with this name. If it does, this acts as the
     * delta by which the value can change when a PageUp/Down key is pressed. If
     * not specified, it is assumed to be 1.
     */
    public static final String MEGAWIDGET_INCREMENT_DELTA = "incrementDelta";

    /**
     * Scale usage parameter name; a megawidget may include a boolean as the
     * value associated with this name. The value determines whether or not a
     * scale widget will be shown below the label and spinner to allow the user
     * an alternate method of manipulating the state. If not specified, it is
     * assumed to be false.
     */
    public static final String MEGAWIDGET_SHOW_SCALE = "showScale";

    // Private Variables

    /**
     * Value increment.
     */
    private final int incrementDelta;

    /**
     * Flag indicating whether or not a scale widget should be shown as well.
     */
    private final boolean showScale;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            widget created by this specifier as a set of key-value pairs.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public IntegerSpinnerSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, Integer.class, null, null);

        // If the increment delta is present, ensure that it
        // is a positive integer.
        incrementDelta = getSpecifierIntegerValueFromObject(
                parameters.get(MEGAWIDGET_INCREMENT_DELTA),
                MEGAWIDGET_INCREMENT_DELTA, 1);
        if (incrementDelta < 1) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_INCREMENT_DELTA, incrementDelta,
                    "must be positive integer");
        }

        // If the show-scale flag is present, ensure that it
        // is a boolean value.
        showScale = getSpecifierBooleanValueFromObject(
                parameters.get(MEGAWIDGET_SHOW_SCALE), MEGAWIDGET_SHOW_SCALE,
                false);
    }

    // Public Methods

    /**
     * Get the increment delta.
     * 
     * @return Increment delta.
     */
    public final int getIncrementDelta() {
        return incrementDelta;
    }

    /**
     * Determine whether or not a scale widget is to be shown below the label
     * and spinner widgets.
     * 
     * @return True if a scale widget is to be shown, false otherwise.
     */
    public final boolean isShowScale() {
        return showScale;
    }
}
