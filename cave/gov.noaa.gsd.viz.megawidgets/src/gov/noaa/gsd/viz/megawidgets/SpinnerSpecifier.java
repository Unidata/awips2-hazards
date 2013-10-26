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
 * Spinner specifier. Note that the parameter <code>T</code> only extends
 * <code>Comparable</code> because Java's <code>Number</code> is not,
 * unfortunately, an extension of <code>Comparable</code>.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 23, 2013   2168     Chris.Golden      Initial creation
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see SpinnerMegawidget
 */
public abstract class SpinnerSpecifier<T extends Comparable<T>> extends
        BoundedValueMegawidgetSpecifier<T> implements ISingleLineSpecifier {

    // Public Static Constants

    /**
     * State value increment parameter name; a megawidget may include a value of
     * type <code>T</code> associated with this name. If it does, this acts as
     * the delta by which the value can change when a PageUp/Down key is
     * pressed. If not specified, it is assumed to be the minimum value that
     * would cause a change in value.
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
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager optionsManager;

    /**
     * Flag indicating whether or not the megawidget is to expand to fill all
     * available horizontal space within its parent.
     */
    private final boolean horizontalExpander;

    /**
     * Value increment.
     */
    private final T incrementDelta;

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
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * 
     * @param boundedValueClass
     *            Class of the bounded value; required in order to provide
     *            proper exception messages in situations where the bounding
     *            values are illegal.
     * 
     * @param lowest
     *            If not <code>null</code>, the lowest possible value for
     *            <code>MEGAWIDGET_MIN_VALUE</code>.
     * 
     * @param highest
     *            If not <code>null</code>, the highest possible value for
     *            <code>MEGAWIDGET_MAX_VALUE</code>.
     * 
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public SpinnerSpecifier(Map<String, Object> parameters,
            Class<T> boundedValueClass, T lowest, T highest)
            throws MegawidgetSpecificationException {
        super(parameters, boundedValueClass, lowest, highest);
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                ControlSpecifierOptionsManager.BooleanSource.FALSE);

        // Get the horizontal expansion flag if available.
        horizontalExpander = getSpecifierBooleanValueFromObject(
                parameters.get(EXPAND_HORIZONTALLY), EXPAND_HORIZONTALLY, false);

        // If the increment delta is present, ensure that it
        // is a positive integer.
        incrementDelta = getSpecifierIncrementDeltaObjectFromObject(
                parameters.get(MEGAWIDGET_INCREMENT_DELTA),
                MEGAWIDGET_INCREMENT_DELTA);

        // If the show-scale flag is present, ensure that it
        // is a boolean value.
        showScale = getSpecifierBooleanValueFromObject(
                parameters.get(MEGAWIDGET_SHOW_SCALE), MEGAWIDGET_SHOW_SCALE,
                false);
    }

    // Public Methods

    @Override
    public final boolean isEditable() {
        return optionsManager.isEditable();
    }

    @Override
    public final int getWidth() {
        return optionsManager.getWidth();
    }

    @Override
    public final boolean isFullWidthOfColumn() {

        // Unlike most megawidgets, the integer spinner requires the full
        // width of its parent's column if it is showing a scale, but does not
        // otherwise.
        return showScale;
    }

    @Override
    public final int getSpacing() {
        return optionsManager.getSpacing();
    }

    @Override
    public final boolean isHorizontalExpander() {
        return horizontalExpander;
    }

    /**
     * Get the increment delta.
     * 
     * @return Increment delta.
     */
    public final T getIncrementDelta() {
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

    // Protected Methods

    /**
     * Get the specifier increment delta object from the specified object.
     * 
     * @param object
     *            Object holding the increment delta value.
     * @param paramName
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @return Increment delta object.
     * @throws MegawidgetSpecSpinnerificationException
     *             If the megawidget specifier parameter is invalid.
     */
    protected abstract T getSpecifierIncrementDeltaObjectFromObject(
            Object object, String paramName)
            throws MegawidgetSpecificationException;
}
