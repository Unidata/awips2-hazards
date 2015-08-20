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

import gov.noaa.gsd.viz.megawidgets.validators.BoundedMultiNumberValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Range megawidget specifier, providing a base class for a specification of a
 * megawidget that allows the selection of lower and upper bounds. Each of the
 * two bounds is associated with a separate state identifier, the two separated
 * by a colon in the megawidget identifier. The generic parameter <code>T</code>
 * provides the type of the numbers being manipulated.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 06, 2015    4123    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see RangeMegawidget
 */
public class RangeSpecifier<T extends Number & Comparable<T>> extends
        StatefulMegawidgetSpecifier implements ISingleLineSpecifier,
        IRapidlyChangingStatefulSpecifier, IScaleCapableSpecifier {

    // Public Static Constants

    /**
     * Between-bounds label parameter name; a range megawidget may include a
     * value associated with this name. The value must be a {@link String} of
     * text that is to be placed between the lower and upper bound spinner
     * component widgets. If none is specified, no text is placed between the
     * spinners.
     */
    public static final String BETWEEN_LABEL = "betweenLabel";

    /**
     * Minimum allowable value parameter name; a range megawidget may include a
     * value associated with this name. The value must be a {@link Map} pairing
     * state identifiers to numbers of type <code>T</code>. If not provided, it
     * is assumed that neither bound has a practical minimum limit as to what
     * value it may hold, except of course that the maximum value must be
     * greater than or equal to the sum of the minimum value plus the minimum
     * interval. If one is specified for either state identifier, it must be
     * less than or equal to any corresponding entry associated with the
     * {@link #MAXIMUM_ALLOWABLE_VALUE} parameter.
     */
    public static final String MINIMUM_ALLOWABLE_VALUE = "minValue";

    /**
     * Maximum allowable value parameter name; a range megawidget may include a
     * value associated with this name. The value must be a {@link Map} pairing
     * state identifiers to numbers of type <code>T</code>. If not provided, it
     * is assumed that neither bound has a practical maximum limit as to what
     * value it may hold, except of course that the maximum value must be
     * greater than or equal to the sum of the minimum value plus the minimum
     * interval. If one is specified for either state identifier, it must be
     * greater than or equal to any corresponding entry associated with the
     * {@link #MINIMUM_ALLOWABLE_VALUE} parameter.
     */
    public static final String MAXIMUM_ALLOWABLE_VALUE = "maxValue";

    /**
     * Minimum interval parameter name; a range megawidget may include a
     * positive value of type <code>T</code> associated with this name,
     * providing the minimum interval between the two state values. If not
     * specified, the megawidget allows the lower state value to be less than or
     * equal to the upper one.
     */
    public static final String MINIMUM_INTERVAL = "minimumInterval";

    /**
     * State value page increment parameter name; a range megawidget may include
     * a value of type <code>T</code> associated with this name. If it does,
     * this acts as the delta by which the value can change when a PageUp/Down
     * key is pressed. If not specified, it is assumed to be the minimum value
     * that would cause a change in value.
     */
    public static final String PAGE_INCREMENT_DELTA = "incrementDelta";

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
     * Flag indicating whether or not state changes that are part of a group of
     * rapid changes are to result in notifications to the listener.
     */
    private final boolean sendingEveryChange;

    /**
     * Between-bounds text label, or <code>null</code> if none is to be used.
     */
    private final String betweenLabel;

    /**
     * Map pairing state identifier keys with their indices in the list provided
     * by the {@link #getStateIdentifiers()} method.
     */
    private final Map<String, Integer> indicesForIds;

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
     * @param minimumIntervalKey
     *            Key in <code>parameters</code> for the minimum interval
     *            parameter; if <code>null</code>, no minimum interval may be
     *            specified and it is assumed to be 0.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public RangeSpecifier(Map<String, Object> parameters,
            BoundedMultiNumberValidator<T> validator)
            throws MegawidgetSpecificationException {
        super(parameters, validator);

        /*
         * Ensure that the rapid change notification flag, if provided, is
         * appropriate.
         */
        sendingEveryChange = ConversionUtilities
                .getSpecifierBooleanValueFromObject(getIdentifier(), getType(),
                        parameters.get(MEGAWIDGET_SEND_EVERY_STATE_CHANGE),
                        MEGAWIDGET_SEND_EVERY_STATE_CHANGE, true);

        /*
         * Get the horizontal expansion flag if available.
         */
        horizontalExpander = ConversionUtilities
                .getSpecifierBooleanValueFromObject(getIdentifier(), getType(),
                        parameters.get(EXPAND_HORIZONTALLY),
                        EXPAND_HORIZONTALLY, false);

        /*
         * Ensure that the between-bounds label, if present, is acceptable.
         */
        try {
            betweenLabel = (String) parameters.get(BETWEEN_LABEL);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), BETWEEN_LABEL, parameters.get(BETWEEN_LABEL),
                    "must be string");
        }

        /*
         * Compile a mapping of state identifiers to their indices (giving their
         * ordering).
         */
        Map<String, Integer> indicesForIds = new HashMap<>();
        List<String> stateIdentifiers = getStateIdentifiers();
        for (int j = 0; j < stateIdentifiers.size(); j++) {
            indicesForIds.put(stateIdentifiers.get(j), j);
        }
        this.indicesForIds = ImmutableMap.copyOf(indicesForIds);

        /*
         * If the show-scale flag is present, ensure that it is a boolean value.
         */
        showScale = ConversionUtilities.getSpecifierBooleanValueFromObject(
                getIdentifier(), getType(),
                parameters.get(MEGAWIDGET_SHOW_SCALE), MEGAWIDGET_SHOW_SCALE,
                false);

        /*
         * Create the options manager, now that the show-scale flag value is
         * known.
         */
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                (showScale ? ControlSpecifierOptionsManager.BooleanSource.TRUE
                        : ControlSpecifierOptionsManager.BooleanSource.FALSE));
    }

    // Public Methods

    @Override
    public boolean isSendingEveryChange() {
        return sendingEveryChange;
    }

    @Override
    public boolean isEditable() {
        return optionsManager.isEditable();
    }

    @Override
    public int getWidth() {
        return optionsManager.getWidth();
    }

    @Override
    public boolean isFullWidthOfDetailPanel() {
        return optionsManager.isFullWidthOfDetailPanel();
    }

    @Override
    public int getSpacing() {
        return optionsManager.getSpacing();
    }

    @Override
    public boolean isHorizontalExpander() {
        return horizontalExpander;
    }

    /**
     * Get the between-bounds label.
     * 
     * @return Between-bounds label, or <code>null</code> if none is to be used.
     */
    public final String getBetweenBoundsLabel() {
        return betweenLabel;
    }

    /**
     * Get the mapping of state identifier keys to their indices in the list
     * provided by the {@link #getStateIdentifiers()} method.
     * 
     * @return Mapping of state identifier keys to their indices.
     */
    public final Map<String, Integer> getIndicesForStateIdentifiers() {
        return indicesForIds;
    }

    /**
     * Get the minimum possible value for the specified state.
     * 
     * @param identifier
     *            Identifier of the state for which to find the minimum possible
     *            value.
     * @return Minimum possible value for the state.
     */
    @SuppressWarnings("unchecked")
    public final T getMinimumValue(String identifier) {
        return ((BoundedMultiNumberValidator<T>) getStateValidator())
                .getMinimumValue(identifier);
    }

    /**
     * Get the maximum possible value for the specified state.
     * 
     * @param identifier
     *            Identifier of the state for which to find the maximum possible
     *            value.
     * @return Maximum possible value for the state.
     */
    @SuppressWarnings("unchecked")
    public final T getMaximumValue(String identifier) {
        return ((BoundedMultiNumberValidator<T>) getStateValidator())
                .getMaximumValue(identifier);
    }

    /**
     * Get the minimum interval.
     * 
     * @return Minimum interval.
     */
    @SuppressWarnings("unchecked")
    public final T getMinimumInterval() {
        return ((BoundedMultiNumberValidator<T>) getStateValidator())
                .getMinimumInterval();
    }

    /**
     * Get the increment delta.
     * 
     * @return Increment delta.
     */
    @SuppressWarnings("unchecked")
    public final T getIncrementDelta() {
        return ((BoundedMultiNumberValidator<T>) getStateValidator())
                .getIncrementDelta();
    }

    @Override
    public final boolean isShowScale() {
        return showScale;
    }

    // Protected Methods

    @Override
    protected int getMinimumStateIdentifierCount() {
        return 2;
    }

    @Override
    protected int getMaximumStateIdentifierCount() {
        return 2;
    }
}