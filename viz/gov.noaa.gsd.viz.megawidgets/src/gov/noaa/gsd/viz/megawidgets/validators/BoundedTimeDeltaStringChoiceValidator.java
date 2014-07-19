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

import gov.noaa.gsd.viz.megawidgets.MegawidgetPropertyException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Description: Validator used to ensure that choice lists are made up of
 * {@link String} instances that represent time deltas, and that potential
 * states are always one of the choices in the list. For information on the
 * format of the time delta strings, see the description of the
 * {@link SingleTimeDeltaStringChoiceValidatorHelper} class.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 01, 2014   3512     Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class BoundedTimeDeltaStringChoiceValidator extends
        BoundedChoiceValidator<String> {

    // Private Variables

    /**
     * Map of available choices to the corresponding time deltas in
     * milliseconds. The iteration order is the same as the list of available
     * choices from which it was derived.
     */
    private Map<String, Long> timeDeltasForAvailableChoices;

    /**
     * Unmodifiable version of map of available choices to the corresponding
     * time deltas in milliseconds.
     */
    private Map<String, Long> unmodifiableTimeDeltasForAvailableChoices;

    // Public Constructors

    /**
     * Construct an uninitialized instance.
     * 
     * @param parameters
     *            Map of parameters used to create the specifier.
     * @param choicesKey
     *            Key within the specifier parameters or mutable properties for
     *            the choices list.
     */
    public BoundedTimeDeltaStringChoiceValidator(
            Map<String, Object> parameters, String choicesKey) {
        super(parameters, new SingleTimeDeltaStringChoiceValidatorHelper(
                choicesKey));
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
    protected BoundedTimeDeltaStringChoiceValidator(
            BoundedTimeDeltaStringChoiceValidator other) {
        super(other);
        timeDeltasForAvailableChoices = new LinkedHashMap<>(
                other.timeDeltasForAvailableChoices);
        unmodifiableTimeDeltasForAvailableChoices = other.unmodifiableTimeDeltasForAvailableChoices;
    }

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StateValidator> V copyOf() {
        return (V) new BoundedTimeDeltaStringChoiceValidator(this);
    }

    /**
     * Get the map of available choices to the corresponding time deltas in
     * milliseconds.
     * 
     * @return Map of available choices to the corresponding time deltas in
     *         milliseconds.
     */
    public final Map<String, Long> getTimeDeltasForAvailableChoices() {
        if (unmodifiableTimeDeltasForAvailableChoices == null) {
            unmodifiableTimeDeltasForAvailableChoices = Collections
                    .unmodifiableMap(timeDeltasForAvailableChoices);
        }
        return unmodifiableTimeDeltasForAvailableChoices;
    }

    /**
     * Set the available choices list to that specified.
     * 
     * @param choices
     *            Object to be used as the new available choices.
     * @throws MegawidgetPropertyException
     *             If the new value is not valid.
     */
    @Override
    public void setAvailableChoices(Object choices)
            throws MegawidgetPropertyException {

        /*
         * Get the ordered map of time delta strings to available choices.
         */
        SingleTimeDeltaStringChoiceValidatorHelper helper = getHelper();
        timeDeltasForAvailableChoices = helper
                .convertToAvailableMapForProperty(choices);
        unmodifiableTimeDeltasForAvailableChoices = null;

        /*
         * If the above did not throw an exception, then this call to get the
         * superclass to get a list from the choices will not either.
         */
        super.setAvailableChoices(choices);
    }

    // Protected Methods

    @Override
    protected void doInitializeBeforeAvailableChoices()
            throws MegawidgetSpecificationException {
        SingleTimeDeltaStringChoiceValidatorHelper helper = getHelper();
        timeDeltasForAvailableChoices = helper
                .convertToAvailableMapForSpecifier(getParameters().get(
                        helper.getChoicesKey()));
    }
}
