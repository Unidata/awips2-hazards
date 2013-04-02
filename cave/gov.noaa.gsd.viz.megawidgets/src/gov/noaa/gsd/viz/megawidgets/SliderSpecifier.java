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
 * Slider specifier. The Slider always produces a single value associated with a
 * single state identifier, so the megawidget identifiers for these specifiers
 * must not consist of colon-separated substrings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * 
 * </pre>
 * 
 * @version 1.0
 * @author Bryon.Lawrence
 * @see SliderMegawidget
 */
public class SliderSpecifier extends StatefulMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Minimum megawidget state value parameter name; a megawidget may include
     * an integer as the value associated with this name. If it does, this acts
     * as the minimum value that the state is allowed to take on. If not
     * specified, it is assumed to be 0.
     */
    public static final String MEGAWIDGET_MIN_VALUE = "minValue";

    /**
     * Maximum megawidget state value parameter name; a megawidget may include
     * an integer as the value associated with this name. If it does, this acts
     * as the maximum value that the state is allowed to take on. This value
     * must be greater than the value given for <code>
     * MEGAWIDGET_MIN_VALUE</code> (or greater than 0 if the latter is not
     * specified). If not specified, it is assumed to be 100.
     */
    public static final String MEGAWIDGET_MAX_VALUE = "maxValue";

    /**
     * State value increment parameter name; a megawidget may include an integer
     * as the value associated with this name. If it does, this acts as the
     * minimum delta by which the value can change. If not specified, it is
     * assumed to be 1.
     */
    public static final String MEGAWIDGET_INCREMENT_DELTA = "incrementDelta";

    // Private Variables

    /**
     * Minimum allowable value.
     */
    private final int minimumValue;

    /**
     * Maximum allowable value.
     */
    private final int maximumValue;

    /**
     * Value increment.
     */
    private final int incrementDelta;

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
    public SliderSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);

        // If the minimum value is present, ensure that it
        // is an integer.
        minimumValue = getSpecifierIntegerValueFromObject(
                parameters.get(MEGAWIDGET_MIN_VALUE), MEGAWIDGET_MIN_VALUE, 0);

        // If the maximum value is present, ensure that it
        // is an integer.
        maximumValue = getSpecifierIntegerValueFromObject(
                parameters.get(MEGAWIDGET_MAX_VALUE), MEGAWIDGET_MAX_VALUE, 100);

        // Ensure that the minimum and maximum values are
        // valid relative to one another.
        if (minimumValue >= maximumValue) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), null, null,
                    "minimum value must be less than maximum (minimum = "
                            + minimumValue + ", maximum = " + maximumValue
                            + ")");
        }

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
    }

    // Public Methods

    /**
     * Get the minimum value.
     * 
     * @return Minimum value.
     */
    public final int getMinimumValue() {
        return minimumValue;
    }

    /**
     * Get the maximum value.
     * 
     * @return Maximum value.
     */
    public final int getMaximumValue() {
        return maximumValue;
    }

    /**
     * Get the increment delta.
     * 
     * @return Increment delta.
     */
    public final int getIncrementDelta() {
        return incrementDelta;
    }
}
