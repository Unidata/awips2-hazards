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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Stateful megawidget created by a megawidget specifier that has a set choices
 * available from which the state may be chosen. Subclasses may allow the
 * selection of a single choice, of zero or more choices, etc. The choices list
 * may be flat or hierarchical in nature.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 25, 2013   1277     Chris.Golden      Initial creation
 * Sep 25, 2013   2168     Chris.Golden      Minor cleanup.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see ChoicesMegawidgetSpecifier
 */
public abstract class ChoicesMegawidget extends StatefulMegawidget {

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
        Set<String> names = Sets
                .newHashSet(StatefulMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(ChoicesMegawidgetSpecifier.MEGAWIDGET_VALUE_CHOICES);
        MUTABLE_PROPERTY_NAMES_INCLUDING_CHOICES = ImmutableSet.copyOf(names);
    };

    // Protected Variables

    /**
     * Choices list.
     */
    protected final List<Object> choices;

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
    protected ChoicesMegawidget(ChoicesMegawidgetSpecifier specifier,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        choices = specifier.createChoicesCopy(specifier.getChoices());
    }

    // Public Methods

    @Override
    public Set<String> getMutablePropertyNames() {
        return (isChoicesListMutable() ? MUTABLE_PROPERTY_NAMES_INCLUDING_CHOICES
                : MUTABLE_PROPERTY_NAMES_WITHOUT_CHOICES);
    }

    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (isChoicesListMutable()
                && name.equals(ChoicesMegawidgetSpecifier.MEGAWIDGET_VALUE_CHOICES)) {
            return doGetChoices();
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (isChoicesListMutable()
                && name.equals(ChoicesMegawidgetSpecifier.MEGAWIDGET_VALUE_CHOICES)) {
            doSetChoices(value);
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public void setMutableProperties(Map<String, Object> properties)
            throws MegawidgetPropertyException {

        // If the choices list is one of the properties being set, do it first;
        // this ensures that if the state is being set as well, it is set after
        // the choices list.
        Object choicesObj = properties
                .get(ChoicesMegawidgetSpecifier.MEGAWIDGET_VALUE_CHOICES);
        if (choicesObj != null) {
            setMutableProperty(
                    ChoicesMegawidgetSpecifier.MEGAWIDGET_VALUE_CHOICES,
                    choicesObj);
        }

        // Do what would have been done by the superclass method, except for
        // ignoring any choice setting, as that has already been done above.
        for (String name : properties.keySet()) {
            if (!name
                    .equals(ChoicesMegawidgetSpecifier.MEGAWIDGET_VALUE_CHOICES)) {
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
     * method will never be called unless <code>isChoicesListMutable()</code>
     * returns true.
     */
    protected abstract void prepareForChoicesChange();

    /**
     * Synchronize the user-facing widgets making up this megawidget to the
     * current choices. For any given subclass, this method will never be called
     * unless <code>isChoicesListMutable()</code> returns true.
     */
    protected abstract void synchronizeWidgetsToChoices();

    /**
     * Synchronize the user-facing widgets making up this megawidget to the
     * current state.
     */
    protected abstract void synchronizeWidgetsToState();

    /**
     * Get the available choices hierarchy. This method is protected so that it
     * may be called by subclasses with mutable choices lists when they
     * implement a method such as <code>getChoices()</code>.
     * 
     * @return Available choices hierarchy.
     */
    protected final List<?> doGetChoices() {
        return ((ChoicesMegawidgetSpecifier) getSpecifier())
                .createChoicesCopy(choices);
    }

    /**
     * Set the choices to those specified. If the current state is not a subset
     * of the new choices, the state will be set to <code>null</code>. This
     * method is protected so that it may be called by subclasses with mutable
     * choices lists when they implement a method such as <code>
     * setChoices()</code>.
     * 
     * @param value
     *            List of new choices.
     * @throws MegawidgetPropertyException
     *             If the choices are invalid.
     */
    protected final void doSetChoices(Object value)
            throws MegawidgetPropertyException {

        // Ensure that the possible values are present as an array of
        // choices.
        List<?> choicesList = null;
        ChoicesMegawidgetSpecifier specifier = (ChoicesMegawidgetSpecifier) getSpecifier();
        try {
            choicesList = specifier.getChoicesFromObject(value);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(e.getIdentifier(),
                    ChoicesMegawidgetSpecifier.MEGAWIDGET_VALUE_CHOICES,
                    e.getType(), e.getBadValue(), e.getMessage());
        }

        // Evaluate the legality of the choices hierarchy.
        ChoicesMegawidgetSpecifier.IllegalChoicesProblem eval = specifier
                .evaluateChoicesLegality(choicesList);
        if (eval != null) {
            throw new MegawidgetPropertyException(specifier.getIdentifier(),
                    ChoicesMegawidgetSpecifier.MEGAWIDGET_VALUE_CHOICES
                            + eval.getBadElementLocation(),
                    specifier.getType(), eval.getSubParameterValue(),
                    (eval.getDepth() == 0 ? "" : "parameter \""
                            + eval.getSubParameterName() + "\" ")
                            + eval.getProblem());
        }

        // Ensure that the current state is a subset of the new choices;
        // if not, set it to nothing.
        try {
            Object state = getState(specifier.getIdentifier());
            List<?> list = null;
            if (state instanceof String) {
                List<Object> objectList = Lists.newArrayList();
                objectList.add(state);
                list = objectList;
            } else {
                list = (List<?>) state;
            }
            if (specifier.isSubset(list, choicesList) == false) {
                setState(specifier.getIdentifier(), null);
            }
        } catch (MegawidgetStateException e) {
            throw new IllegalStateException(
                    "querying or setting valid mutable property \""
                            + StatefulMegawidgetSpecifier.MEGAWIDGET_STATE_VALUES
                            + "\" caused internal error", e);
        }

        // Change the choices to those specified, and alter the component
        // widgets to match.
        prepareForChoicesChange();
        choices.clear();
        choices.addAll(choicesList);
        synchronizeWidgetsToChoices();
    }

    /**
     * Get a text description of the list of all possible choice identifiers.
     * This method is not used within this class, but is provided for subclasses
     * that may require it. Note that if said subclasses have hierarchical lists
     * of choices, they will need to override this method before using it.
     * 
     * @return List of choice identifiers as a comma-separated string of text.
     */
    protected final String getChoicesAsString() {
        ChoicesMegawidgetSpecifier specifier = getSpecifier();
        StringBuilder stringBuilder = new StringBuilder();
        for (Object choice : choices) {
            String identifier = specifier.getIdentifierOfNode(choice);
            if (stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(identifier);
        }
        return stringBuilder.toString();
    }

    /**
     * Get a list of all possible choice identifiers. This method is not used
     * within this class, but is provided for subclasses that may require it.
     * Note that if said subclasses have hierarchical lists of choices, they
     * will need to override this method before using it.
     * 
     * @return List of choice identifiers.
     */
    protected final List<String> getChoiceIdentifiers() {
        ChoicesMegawidgetSpecifier specifier = getSpecifier();
        List<String> identifiers = Lists.newArrayList();
        for (Object choice : choices) {
            identifiers.add(specifier.getIdentifierOfNode(choice));
        }
        return identifiers;
    }
}
