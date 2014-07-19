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

import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetPropertyException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Description: Validator used to ensure that potential state values are of
 * generic type <code>T</code>, and are subsets of choice lists.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 24, 2014   2925     Chris.Golden Initial creation.
 * Jun 24, 2014   4023     Chris.Golden Added ability to create a pruned
 *                                      subset.
 * Jul 02, 2014   3512     Chris.Golden Changed to allow subclassing.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class BoundedChoiceValidator<T> extends SingleStateValidator<T> {

    // Private Variables

    /**
     * Map of parameters used to create the specifier. This may be
     * <code>null</code> if the validator has been constructed as already
     * initialized.
     */
    private final Map<String, Object> parameters;

    /**
     * Validator helper.
     */
    private final BoundedChoiceValidatorHelper<T> helper;

    /**
     * List of available choices.
     */
    private List<?> availableChoices;

    /**
     * Unmodifiable version of the available choices.
     */
    private List<?> unmodifiableAvailableChoices;

    // Public Constructors

    /**
     * Construct an uninitialized instance.
     * 
     * @param parameters
     *            Map of parameters used to create the specifier.
     * @param helper
     *            Validator helper, which will do the heavy lifting for this
     *            object.
     */
    public BoundedChoiceValidator(Map<String, Object> parameters,
            BoundedChoiceValidatorHelper<T> helper) {
        this.parameters = parameters;
        this.helper = helper;
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
    protected BoundedChoiceValidator(BoundedChoiceValidator<T> other) {
        super(other);
        parameters = null;
        helper = other.helper;
        availableChoices = new ArrayList<>(other.availableChoices);
        unmodifiableAvailableChoices = other.unmodifiableAvailableChoices;
    }

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StateValidator> V copyOf() {
        return (V) new BoundedChoiceValidator<T>(this);
    }

    /**
     * Get the available choices list.
     * 
     * @return Available choices list.
     */
    public final List<?> getAvailableChoices() {
        if (unmodifiableAvailableChoices == null) {
            unmodifiableAvailableChoices = helper
                    .convertToUnmodifiable(availableChoices);
        }
        return unmodifiableAvailableChoices;
    }

    /**
     * Get the name of the specified choices collection element.
     * 
     * @param node
     *            Choices collection element, one of the elements from the
     *            {@link List} provided by {@link #getAvailableChoices()}.
     * @return Identifier of the choices collection element.
     */
    public final String getNameOfNode(Object node) {
        return helper.getNameOfNode(node);
    }

    /**
     * Get the identifier of the specified choices list element.
     * 
     * @param node
     *            Choices collection element, one of the elements from the
     *            {@link List} provided by {@link #getAvailableChoices()}.
     * @return Identifier of the choices collection element.
     */
    public final String getIdentifierOfNode(Object node) {
        return helper.getIdentifierOfNode(node);
    }

    /**
     * Set the available choices list to that specified.
     * 
     * @param choices
     *            Object to be used as the new available choices.
     * @throws MegawidgetPropertyException
     *             If the new value is not valid.
     */
    public void setAvailableChoices(Object choices)
            throws MegawidgetPropertyException {
        availableChoices = helper.convertToAvailableForProperty(choices);
        unmodifiableAvailableChoices = null;
    }

    @Override
    public T convertToStateValue(Object object) throws MegawidgetException {
        return helper.convertToSubset(availableChoices, object);
    }

    /**
     * Prune the specified state to remove anything that is not an available
     * choice.
     * 
     * @param state
     *            State to be pruned.
     * @return Pruned state.
     */
    public T pruneToStateValue(T state) {
        return helper.getPrunedSubset(state, availableChoices);
    }

    // Protected Methods

    @Override
    protected void doInitialize() throws MegawidgetSpecificationException {
        helper.initialize(getType(), getIdentifier());
        doInitializeBeforeAvailableChoices();
        availableChoices = helper.convertToAvailableForSpecifier(parameters
                .get(helper.getChoicesKey()));
    }

    /**
     * Perform subclass-specific initialization after the helper is initialized,
     * but before the available choices are.
     */
    protected void doInitializeBeforeAvailableChoices()
            throws MegawidgetSpecificationException {

        /*
         * No action.
         */
    }

    /**
     * Get the helper.
     * 
     * @return Helper.
     */
    @SuppressWarnings("unchecked")
    protected final <H extends BoundedChoiceValidatorHelper<T>> H getHelper() {
        return (H) helper;
    }

    /**
     * Get the map of parameters used to create the specifier. The map is
     * unmodifiable. It may be <code>null</code> if the validator has been
     * constructed as already initialized.
     * 
     * @return Map of parameters, or <code>null</code>.
     */
    protected final Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }
}
