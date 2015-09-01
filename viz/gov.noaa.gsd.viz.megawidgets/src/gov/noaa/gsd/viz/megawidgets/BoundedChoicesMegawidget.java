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

import gov.noaa.gsd.viz.megawidgets.validators.BoundedChoiceValidator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Stateful megawidget created by a megawidget specifier that has a closed set
 * of choices available from which the state may be chosen. Subclasses may allow
 * the selection of a single choice, of zero or more choices, etc. The choices
 * list may be flat or hierarchical in nature. The generic parameter
 * <code>T</code> is the type of state.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 31, 2013   2336     Chris.Golden      Initial creation.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 24, 2014   4023     Chris.Golden      Changed to prune old state to new
 *                                           choices when available choices are
 *                                           changed.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see BoundedChoicesMegawidgetSpecifier
 */
public abstract class BoundedChoicesMegawidget<T> extends StatefulMegawidget {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class that do not
     * have mutable choices lists.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES_WITHOUT_CHOICES = StatefulMegawidget.MUTABLE_PROPERTY_NAMES;

    /**
     * Set of all mutable property names for instances of this class that have
     * mutable choices lists.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES_INCLUDING_CHOICES;
    static {
        Set<String> names = new HashSet<>(
                StatefulMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(BoundedChoicesMegawidgetSpecifier.MEGAWIDGET_VALUE_CHOICES);
        MUTABLE_PROPERTY_NAMES_INCLUDING_CHOICES = ImmutableSet.copyOf(names);
    };

    // Private Variables

    /**
     * State validator.
     */
    private final BoundedChoiceValidator<T> stateValidator;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    protected BoundedChoicesMegawidget(
            BoundedChoicesMegawidgetSpecifier<T> specifier,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        stateValidator = specifier.getStateValidator().copyOf();
    }

    // Public Methods

    /**
     * Get the available choices hierarchy.
     * 
     * @return Available choices hierarchy.
     */
    public final List<?> getChoices() {
        return stateValidator.getAvailableChoices();
    }

    @Override
    public Set<String> getMutablePropertyNames() {
        return (isChoicesListMutable() ? MUTABLE_PROPERTY_NAMES_INCLUDING_CHOICES
                : MUTABLE_PROPERTY_NAMES_WITHOUT_CHOICES);
    }

    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (isChoicesListMutable()
                && name.equals(BoundedChoicesMegawidgetSpecifier.MEGAWIDGET_VALUE_CHOICES)) {
            return getChoices();
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (isChoicesListMutable()
                && name.equals(BoundedChoicesMegawidgetSpecifier.MEGAWIDGET_VALUE_CHOICES)) {
            doSetChoices(value);
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public void setMutableProperties(Map<String, Object> properties)
            throws MegawidgetPropertyException {

        /*
         * If the choices list is one of the properties being set, do it first;
         * this ensures that if the state is being set as well, it is set after
         * the choices list.
         */
        Object choicesObj = properties
                .get(BoundedChoicesMegawidgetSpecifier.MEGAWIDGET_VALUE_CHOICES);
        if (choicesObj != null) {
            setMutableProperty(
                    BoundedChoicesMegawidgetSpecifier.MEGAWIDGET_VALUE_CHOICES,
                    choicesObj);
        }

        /*
         * Do what would have been done by the superclass method, except for
         * ignoring any choice setting, as that has already been done above.
         */
        for (String name : properties.keySet()) {
            if (!name
                    .equals(BoundedChoicesMegawidgetSpecifier.MEGAWIDGET_VALUE_CHOICES)) {
                setMutableProperty(name, properties.get(name));
            }
        }
    }

    // Protected Methods

    /**
     * Determine whether or not the subclass allows the choices list to be
     * mutable.
     * 
     * @return True if the choices list is mutable, false otherwise. Subclasses
     *         must implement this method to always return the same value for
     *         that class.
     */
    protected abstract boolean isChoicesListMutable();

    /**
     * Prepare for the choices list to be changed. For any given subclass, this
     * method will never be called unless {@link #isChoicesListMutable()}.
     * returns true.
     */
    protected abstract void prepareForChoicesChange();

    /**
     * Cancel any preparations made for the choices list to be changed by a
     * previous invocation of {@link #prepareForChoicesChange()}.
     */
    protected abstract void cancelPreparationForChoicesChange();

    /**
     * Synchronize the user-facing component widgets making up this megawidget
     * to the current choices. For any given subclass, this method will never be
     * called unless {@link #isChoicesListMutable()} returns true.
     */
    protected abstract void synchronizeComponentWidgetsToChoices();

    /**
     * Get the state validator.
     * 
     * @return State validator.
     */
    protected final BoundedChoiceValidator<T> getStateValidator() {
        return stateValidator;
    }

    /**
     * Set the choices to those specified. If the current state is not a subset
     * of the new choices, the state will be set to a default value. This method
     * is protected so that it may be called by subclasses with mutable choices
     * lists when they implement a method to set choices.
     * 
     * @param value
     *            List of new choices.
     * @throws MegawidgetPropertyException
     *             If the choices are invalid.
     */
    @SuppressWarnings("unchecked")
    protected final void doSetChoices(Object value)
            throws MegawidgetPropertyException {

        /*
         * Do any subclass-specific preparation for the change about to occur.
         */
        prepareForChoicesChange();

        /*
         * Set the choices as specified; if the attempt fails, do any
         * subclass-specific cancellation of the preparations made above.
         */
        try {
            stateValidator.setAvailableChoices(value);
        } catch (MegawidgetPropertyException e) {
            cancelPreparationForChoicesChange();
            throw e;
        }

        /*
         * Prune the state so that anything not found in the new choices is
         * removed.
         */
        String identifier = getSpecifier().getIdentifier();
        try {
            doSetState(identifier,
                    stateValidator.pruneToStateValue((T) getState(identifier)));
        } catch (MegawidgetException e) {
            throw new IllegalStateException(
                    "pruning state to new choices caused internal error", e);
        }

        /*
         * Ensure that the component widgets are synchronized with the new
         * choices.
         */
        synchronizeComponentWidgetsToChoices();
    }

    /**
     * Given the specified choice name, get an identifier that has not yet been
     * used in the specified collection.
     * 
     * @param name
     *            Name for which to generate an identifier.
     * @param identifiers
     *            Existing identifiers.
     * @return Identifier for the name.
     */
    protected final String getUniqueIdentifierForNewName(String name,
            Collection<String> identifiers) {
        for (int j = 0; j < Integer.MAX_VALUE; j++) {
            String identifier = name + (j > 0 ? j : "");
            if (identifiers.contains(identifier) == false) {
                return identifier;
            }
        }
        throw new IllegalStateException("no unique identifier based upon \""
                + name + "\" found");
    }
}
