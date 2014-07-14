/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets.validators;

import gov.noaa.gsd.viz.megawidgets.ConversionUtilities;
import gov.noaa.gsd.viz.megawidgets.IStateful;
import gov.noaa.gsd.viz.megawidgets.IStatefulSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: Validator used to ensure that potential state values are
 * {@link Long} instances, are within fixed boundaries, and are in ascending
 * order for multiple-state {@link IStateful} and {@link IStatefulSpecifier}
 * objects.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 23, 2014   2925     Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class BoundedMultiLongValidator extends MultiStateValidator<Long> {

    // Private Variables

    /**
     * Map of parameters used to create the specifier. This may be
     * <code>null</code> if the validator has been constructed as already
     * initialized.
     */
    private final Map<String, Object> parameters;

    /**
     * Key in {@link #parameters} for the minimum interval parameter.
     */
    private final String minimumIntervalKey;

    /**
     * Lowest allowable value; the minimum value may not be lower than this.
     */
    private final Long lowest;

    /**
     * Highest allowable value; the maximum value may not be higher than this.
     */
    private final Long highest;

    /**
     * Minimum interval allowed between adjacent values.
     */
    private Long minimumInterval;

    /**
     * Range validator helper.
     */
    private RangeValidatorHelper<Long> helper;

    // Public Constructors

    /**
     * Construct an uninitialized instance.
     * 
     * @param parameters
     *            Map of parameters used to create the specifier.
     * @param minimumIntervalKey
     *            Key in <code>parameters</code> for the minimum interval
     *            parameter.
     * @param lowest
     *            Lowest allowable value; the minimum value may not be lower
     *            than this.
     * @param highest
     *            Highest allowable value; the maximum value may not be higher
     *            than this.
     */
    public BoundedMultiLongValidator(Map<String, Object> parameters,
            String minimumIntervalKey, Long lowest, Long highest)
            throws MegawidgetSpecificationException {
        this.parameters = parameters;
        this.minimumIntervalKey = minimumIntervalKey;
        this.lowest = lowest;
        this.highest = highest;
    }

    // Protected Constructors

    /**
     * Construct an instance that is a copy of another already-initialized
     * instance.
     * 
     * @param other
     *            Validator to be copied.
     * @throws IllegalArgumentException
     *             If <code>other</code> has not yet been initialized.
     */
    protected BoundedMultiLongValidator(BoundedMultiLongValidator other) {
        super(other);
        parameters = null;
        minimumIntervalKey = other.minimumIntervalKey;
        lowest = other.lowest;
        highest = other.highest;
        helper = new RangeValidatorHelper<Long>(other.helper);
        minimumInterval = other.minimumInterval;
    }

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StateValidator> V copyOf() {
        return (V) new BoundedMultiLongValidator(this);
    }

    /**
     * Get the minimum allowed value.
     * 
     * @return Minimum allowed value.
     */
    public final Long getMinimumValue() {
        return helper.getMinimumValue();
    }

    /**
     * Get the maximum allowed value.
     * 
     * @return Maximum allowed value.
     */
    public final Long getMaximumValue() {
        return helper.getMaximumValue();
    }

    /**
     * Get the minimum interval.
     * 
     * @return Minimum interval.
     */
    public Long getMinimumInterval() {
        return minimumInterval;
    }

    @Override
    public Long convertToStateValue(String identifier, Object object)
            throws MegawidgetException {
        Long value = ConversionUtilities.getStateLongObjectFromObject(
                identifier, getType(), object, helper.getMinimumValue());
        if ((value < helper.getMinimumValue())
                || (value > helper.getMaximumValue())) {
            throw new MegawidgetStateException(getIdentifiers().get(
                    getIdentifiers().size() - 1), getType(), value,
                    "out of bounds");
        }
        return value;
    }

    @Override
    public Map<String, Long> convertToStateValues(
            Map<String, ?> objectsForIdentifiers) throws MegawidgetException {

        /*
         * If the map is empty, come up with some default values equally spaced
         * along the allowable range.
         */
        if ((objectsForIdentifiers == null) || objectsForIdentifiers.isEmpty()) {
            Map<String, Long> defaultMap = new HashMap<>();
            long range = helper.getMaximumValue() - helper.getMinimumValue();
            long interval = range / (getIdentifiers().size() + 1);
            long value = interval;
            for (String identifier : getIdentifiers()) {
                defaultMap.put(identifier, value);
                value += interval;
            }
            objectsForIdentifiers = defaultMap;
        }

        /*
         * Build a map of the converted values, checking each value in order to
         * ensure that it is within bounds and that all values are in ascending
         * order with respect to the order of the state identifiers, and have at
         * least the minimum interval in between them.
         */
        Map<String, Long> valuesForIdentifiers = new HashMap<>(
                objectsForIdentifiers.size());
        Long lastValue = null;
        for (int j = 0; j < getIdentifiers().size(); j++) {
            String identifier = getIdentifiers().get(j);
            Long value = ConversionUtilities.getStateLongObjectFromObject(
                    identifier, getType(),
                    objectsForIdentifiers.get(identifier), null);
            if ((j == 0) && (value < helper.getMinimumValue())) {
                throw new MegawidgetStateException(identifier, getType(),
                        value, "out of bounds");
            } else if ((j > 0) && (value < lastValue + minimumInterval)) {
                throw new MegawidgetStateException(identifier, getType(),
                        value, "not in ascending order");
            }
            valuesForIdentifiers.put(identifier, value);
            lastValue = value;
        }
        if ((lastValue != null) && (lastValue > helper.getMaximumValue())) {
            throw new MegawidgetStateException(getIdentifiers().get(
                    getIdentifiers().size() - 1), getType(), lastValue,
                    "out of bounds");
        }
        return valuesForIdentifiers;
    }

    // Protected Methods

    @Override
    protected void doInitialize() throws MegawidgetSpecificationException {
        helper = new RangeValidatorHelper<Long>(getType(),
                getMegawidgetIdentifier(), parameters, null, null, Long.class,
                lowest, highest);
        minimumInterval = getMinimumInterval(minimumIntervalKey, parameters);
    }

    // Private Methods

    /**
     * Get the minimum interval from the specified parameters map using the
     * specified key.
     * 
     * @param key
     *            Key with which object to be used as the minimum interval is
     *            associated in <code>parameters</code>.
     * @param parameters
     *            Map holding parameters.
     * @return Minimum interval.
     * @throws MegawidgetSpecificationException
     *             If a valid minimum interval cannot be found.
     */
    private Long getMinimumInterval(String key, Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        long minimumInterval = ConversionUtilities
                .getSpecifierLongValueFromObject(getMegawidgetIdentifier(),
                        getType(), parameters.get(key), key, 0L);
        if (minimumInterval < 0L) {
            throw new MegawidgetSpecificationException(
                    getMegawidgetIdentifier(), getType(), key,
                    parameters.get(key), "must be non-negative long");
        }
        return minimumInterval;
    }
}
