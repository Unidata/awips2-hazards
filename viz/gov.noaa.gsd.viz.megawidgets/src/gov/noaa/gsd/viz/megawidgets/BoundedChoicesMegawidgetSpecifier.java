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
import gov.noaa.gsd.viz.megawidgets.validators.BoundedChoiceValidatorHelper;

import java.util.List;
import java.util.Map;

/**
 * Base class for megawidget specifiers that include closed sets of choices as
 * part of their state. Said choices are always associated with a single state
 * identifier, so the megawidget identifiers for these specifiers must not
 * consist of colon-separated substrings. The generic parameter <code>T</code>
 * indicates the type of the state.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 31, 2013   2336     Chris.Golden      Initial creation.
 * Jan 28, 2014   2161     Chris.Golden      Changed to support use of
 *                                           collections instead of only
 *                                           lists for the state.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Aug 31, 2015   9617     Chris.Golden      Modified to support creation of
 *                                           new choices by the user.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see BoundedChoicesMegawidget
 */
public abstract class BoundedChoicesMegawidgetSpecifier<T> extends
        ChoicesMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Possible megawidget state values parameter name; a megawidget must
     * include an array of one or more choices associated with this name. Each
     * such choice may be either a string, meaning that the string value is used
     * as the choice's name, or else a {@link Map} holding an entry for
     * {@link #CHOICE_NAME} and, optionally, an entry for
     * {@link #CHOICE_IDENTIFIER}. Subclasses may have additional required or
     * optional entries in the map. Regardless, a given string must occur at
     * most once as a choice name.
     */
    public static final String MEGAWIDGET_VALUE_CHOICES = "choices";

    /**
     * Choice identifier parameter name; each choice in the array of choices
     * associated with {@link #MEGAWIDGET_VALUE_CHOICES} that is a map may
     * contain a reference to a string associated with this name. The string
     * serves as the identifier of the choice. If not provided, the
     * {@link #CHOICE_NAME} is used as its identifier instead. Each identifier
     * must be unique in the set of all choice identifiers.
     */
    public static final String CHOICE_IDENTIFIER = "identifier";

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @param stateValidatorHelper
     *            State validator helper.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public BoundedChoicesMegawidgetSpecifier(Map<String, Object> parameters,
            BoundedChoiceValidatorHelper<T> stateValidatorHelper)
            throws MegawidgetSpecificationException {
        super(parameters, new BoundedChoiceValidator<T>(parameters,
                stateValidatorHelper));
    }

    // Protected Methods

    /**
     * Get the list of available choices.
     * 
     * @return List of available choices.
     */
    @SuppressWarnings("unchecked")
    protected final List<?> getChoices() {
        return ((BoundedChoiceValidator<T>) getStateValidator())
                .getAvailableChoices();
    }

    /**
     * Get the name of the specified choices list element.
     * 
     * @param node
     *            Choices list element; must be of type {@link String} or of
     *            type {@link Map}; if the latter, it must have a {@link String}
     *            as a value paired with the key {@link #CHOICE_NAME}.
     * @return Identifier of the state hierarchy node.
     */
    @SuppressWarnings("unchecked")
    protected final String getNameOfNode(Object node) {
        return ((BoundedChoiceValidator<T>) getStateValidator())
                .getNameOfNode(node);
    }

    /**
     * Get the name of the specified choices list element.
     * 
     * @param node
     *            Choices list element; must be of type {@link String} or of
     *            type {@link Map}; if the latter, it must have a {@link String}
     *            as a value paired with the key {@link #CHOICE_NAME}.
     * @return Identifier of the state hierarchy node.
     */
    @SuppressWarnings("unchecked")
    protected final String getIdentifierOfNode(Object node) {
        return ((BoundedChoiceValidator<T>) getStateValidator())
                .getIdentifierOfNode(node);
    }
}
