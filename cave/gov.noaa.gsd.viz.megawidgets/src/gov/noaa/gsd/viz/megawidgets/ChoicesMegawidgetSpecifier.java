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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base class for megawidget specifiers that include choices as part of their
 * state. Said choices are always associated with a single state identifier, so
 * the megawidget identifiers for these specifiers must not consist of
 * colon-separated substrings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class ChoicesMegawidgetSpecifier extends
        StatefulMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Possible megawidget state values parameter name; a megawidget must
     * include an array of one or more choices associated with this name. Each
     * such choice may be either a string, meaning that the string value is used
     * as both the choice's description and its identifier, or else a
     * <code>Map</code> holding an entry for <code>CHOICE_NAME</code> and
     * optionally, an entry for <code>CHOICE_IDENTIFIER</code>. Regardless, each
     * string must occur at most once as a choice name and once as a choice
     * identifier.
     */
    public static final String MEGAWIDGET_VALUES = "choices";

    /**
     * Choice name parameter name; each choice in the array of choices
     * associated with <code>MEGAWIDGET_VALUES</code> that is a map must contain
     * a reference to a string associated with this name. The string serves to
     * label the choice, and if there is no entry for <code>CHOICE_IDENTIFIER
     * </code> within the map, as its identifier as well. Each name must be
     * unique in the set of all choice names.
     */
    public static final String CHOICE_NAME = "displayString";

    /**
     * Choice identifier parameter name; each choice in the array of choices
     * associated with <code>MEGAWIDGET_VALUES</code> that is a map may contain
     * a reference to a string associated with this name. The string serves as
     * the identifier of the choice. If not provided, the <code>CHOICE_NAME
     * </code> is used as its identifier instead. Each identifier must be unique
     * in the set of all choice identifiers.
     */
    public static final String CHOICE_IDENTIFIER = "identifier";

    // Package Variables

    /**
     * Choices associated with the state.
     */
    final List<String> choices;

    /**
     * Longer versions of the choices associated with the state.
     */
    final List<String> longChoices;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public ChoicesMegawidgetSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);

        // Ensure that the possible values are present as an
        // array of strings and/or maps. If a choice is a map,
        // it must contain an entry for its name and optionally
        // an entry for its identifier. Also ensure that the
        // choice identifiers are each unique.
        List<?> choicesList = null;
        try {
            choicesList = (List<?>) parameters.get(MEGAWIDGET_VALUES);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_VALUES,
                    parameters.get(MEGAWIDGET_VALUES),
                    "must be list of choices");
        }
        if ((choicesList == null) || choicesList.isEmpty()) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_VALUES, null, null);
        }
        List<String> choices = new ArrayList<String>(choicesList.size());
        List<String> longChoices = new ArrayList<String>(choicesList.size());
        Set<String> choiceIdentifiers = new HashSet<String>();
        for (int j = 0; j < choicesList.size(); j++) {
            if (choicesList.get(j) instanceof String) {
                choices.add((String) choicesList.get(j));
                longChoices.add((String) choicesList.get(j));
            } else {
                Map<?, ?> choiceMap = null;
                try {
                    choiceMap = (Map<?, ?>) choicesList.get(j);
                    if (choiceMap == null) {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    throw new MegawidgetSpecificationException(getIdentifier(),
                            getType(), MEGAWIDGET_VALUES + "[" + j + "]",
                            choicesList.get(j),
                            "must be either string or map holding "
                                    + "choice identifier and description");
                }
                try {
                    String name = null, identifier = null;
                    try {
                        name = (String) choiceMap.get(CHOICE_NAME);
                        if (name == null) {
                            throw new Exception();
                        }
                        longChoices.add(name);
                    } catch (Exception e) {
                        throw new MegawidgetSpecificationException(
                                getIdentifier(), getType(), CHOICE_NAME,
                                choiceMap.get(CHOICE_NAME), "must be string");
                    }
                    try {
                        identifier = (String) choiceMap.get(CHOICE_IDENTIFIER);
                        if (identifier == null) {
                            identifier = name;
                        }
                        choices.add(identifier);
                    } catch (Exception e) {
                        throw new MegawidgetSpecificationException(
                                getIdentifier(), getType(), CHOICE_IDENTIFIER,
                                choiceMap.get(CHOICE_NAME), "must be string");
                    }
                } catch (MegawidgetSpecificationException e) {
                    throw new MegawidgetSpecificationException(getIdentifier(),
                            getType(), MEGAWIDGET_VALUES + "[" + j + "]",
                            choiceMap, "bad choice map", e);
                }
            }
            if (choiceIdentifiers.contains(choices.get(j))) {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), CHOICE_IDENTIFIER + "[" + j + "]",
                        choices.get(j), "choice identifiers must be unique");
            }
            choiceIdentifiers.add(choices.get(j));
        }
        this.choices = Collections.unmodifiableList(choices);
        this.longChoices = Collections.unmodifiableList(longChoices);
    }

    // Public Methods

    /**
     * Get the array of choice identifiers.
     * 
     * @return Array of choice identifiers; this array should be considered
     *         read-only by the caller.
     */
    public final List<String> getChoiceIdentifiers() {
        return choices;
    }

    /**
     * Get the array of choice names.
     * 
     * @return Array of choice names; this array should be considered read-only
     *         by the caller.
     */
    public final List<String> getChoiceNames() {
        return longChoices;
    }

    /**
     * Translate the specified choice to its longer version.
     * 
     * @param choice
     *            Choice.
     * @return Longer version, or <code>null</code> if the choice cannot be
     *         found.
     */
    public final String getLongVersionFromChoice(String choice) {
        int index = choices.indexOf(choice);
        return (index == -1 ? null : longChoices.get(index));
    }

    /**
     * Translate the specified longer version of a choice to the choice itself.
     * 
     * @param choice
     *            Longer version.
     * @return Choice, or <code>null<code> if the choice
     *         cannot be found.
     */
    public final String getChoiceFromLongVersion(String choice) {
        int index = longChoices.indexOf(choice);
        return (index == -1 ? null : choices.get(index));
    }
}
