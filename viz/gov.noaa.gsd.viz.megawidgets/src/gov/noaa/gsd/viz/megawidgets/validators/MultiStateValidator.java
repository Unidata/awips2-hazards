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

import gov.noaa.gsd.viz.megawidgets.IStateful;
import gov.noaa.gsd.viz.megawidgets.IStatefulSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;

import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 * Description: Class to be used as the basis for multiple state validator
 * classes. Such a class is used to validate potential values by
 * {@link IStateful} and {@link IStatefulSpecifier} instances that have multiple
 * state identifiers and values. The generic parameter <code>T</code> provides
 * the type of the state values.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 23, 2014   2925     Chris.Golden Initial creation.
 * Aug 12, 2015   4123     Chris.Golden Added method to fetch associated
 *                                      megawidget identifier.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class MultiStateValidator<T> extends StateValidator {

    // Private Variables

    /**
     * List of state identifiers.
     */
    private ImmutableList<String> identifiers;

    /**
     * Megawidget identifier.
     */
    private String megawidgetIdentifier;

    // Protected Constructors

    /**
     * Construct an uninitialized instance.
     */
    protected MultiStateValidator() {
    }

    /**
     * Construct an instance that is a copy of another already-initialized
     * instance.
     * 
     * @param other
     *            Validator to be copied.
     * @throws IllegalArgumentException
     *             If <code>other</code> has not yet been initialized.
     */
    protected MultiStateValidator(MultiStateValidator<T> other) {
        super(other);
        this.identifiers = other.identifiers;
        this.megawidgetIdentifier = other.megawidgetIdentifier;
    }

    // Public Methods

    /**
     * Initialize the validator for a specifier. This method should only be
     * called if the object has not already been initialized, that is, if it was
     * created using a public constructor.
     * 
     * @param type
     *            Type of the megawidget; must not be <code>null</code>.
     * @param identifiers
     *            List of state identifiers for the megawidget; must hold at
     *            least one identifier.
     * @throws IllegalArgumentException
     *             If either parameter is <code>null</code> or if
     *             <code>identifiers</code> is an empty list.
     * @throws IllegalStateException
     *             If this validator has already been initialized.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specification is incorrect.
     */
    public final void initialize(String type, List<String> identifiers)
            throws MegawidgetSpecificationException {
        initializeType(type);
        if ((identifiers == null) || identifiers.isEmpty()) {
            throw new IllegalArgumentException(
                    "must include one or more identifiers");
        }
        this.identifiers = ImmutableList.copyOf(identifiers);
        this.megawidgetIdentifier = Joiner.on(":").join(identifiers);
        doInitialize();
    }

    /**
     * Get the overall identifier.
     * 
     * @return Identifier.
     */
    public final String getIdentifier() {
        return Joiner.on(":").join(identifiers);
    }

    /**
     * Get the state identifiers.
     * 
     * @return State identifiers.
     */
    public final ImmutableList<String> getStateIdentifiers() {
        return identifiers;
    }

    /**
     * Convert the specified object to a valid state value if possible,
     * providing a default value if no object is specified. Note that this
     * method cannot check the validity of the specified object as a value
     * against the values associated with the other state identifiers of the
     * megawidget, since those are unknown to it. Thus, a value that is returned
     * as valid by this method may be invalid when considered in the context of
     * other state identifiers' values, if the states are interdependent.
     * 
     * @param identifier
     *            State identifier for this value.
     * @param object
     *            Object to be converted. If <code>null</code, a default value
     *            will be returned.
     * @return Valid state value, subject to the conditions specified above.
     * @throws MegawidgetException
     *             If the object is not a valid state value.
     */
    public abstract T convertToStateValue(String identifier, Object object)
            throws MegawidgetException;

    /**
     * Convert the objects in the specified map to valid state values if
     * possible, providing default values if no map is specified, or the map is
     * empty.
     * 
     * @param objectsForIdentifiers
     *            Map pairing state identifiers with the objects to be converted
     *            to valid state values for those identifiers. If
     *            <code>null</code> or empty, default values will be used to
     *            populate the map that is returned.
     * @return Map of state identifiers to the converted state values.
     * @throws MegawidgetException
     *             If any of the objects are not individually valid state
     *             values, or if some interdependency between the objects
     *             renders one or more of them invalid.
     */
    public abstract Map<String, T> convertToStateValues(
            Map<String, ?> objectsForIdentifiers) throws MegawidgetException;

    // Protected Methods

    /**
     * Get the megawidget identifier.
     * 
     * @return megawidget identifier.
     */
    protected final String getMegawidgetIdentifier() {
        return megawidgetIdentifier;
    }
}
