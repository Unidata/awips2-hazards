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

import gov.noaa.gsd.viz.megawidgets.validators.BoundedChoiceValidatorHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description: Base class for megawidget specifiers that include a flat closed
 * list of choices as part of their state, with each choice having zero or more
 * detail fields (child megawidgets) associated with it. Said choices are always
 * associated with a single state identifier, so the megawidget identifiers for
 * these specifiers must not consist of colon-separated substrings. The generic
 * parameter <code>T</code> indicates the type of state.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 24, 2013   2168     Chris.Golden      Initial creation
 * Oct 31, 2013   2336     Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 17, 2014   3982     Chris.Golden      Changed "isFullWidthOfColumn"
 *                                           property to "isFullWidthOfDetailPanel".
 * Jul 23, 2014   4122     Chris.Golden      Changed to allow the detail fields
 *                                           parameter to have as a value the
 *                                           choice identifier of a previously
 *                                           defined choice, indicating that the
 *                                           detail fields for the current choice
 *                                           should be the same as those of the
 *                                           specified choice.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class FlatChoicesWithDetailMegawidgetSpecifier<T> extends
        FlatBoundedChoicesMegawidgetSpecifier<T> implements
        IParentSpecifier<IControlSpecifier>, IControlSpecifier {

    // Public Static Constants

    /**
     * Choice detail fields parameter name; each choice in the list associated
     * with {@link #MEGAWIDGET_VALUE_CHOICES} may contain a reference to a
     * {@link List} object associated with this name. The provided list must
     * contain zero or more child megawidget specifier parameter maps, each in
     * the form of a {@link Map}, from which a megawidget specifier will be
     * constructed.
     */
    public static final String DETAIL_FIELDS = "detailFields";

    // Private Variables

    /**
     * Child megawidget specifiers manager.
     */
    private final ChildSpecifiersManager<IControlSpecifier> childManager;

    /**
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager optionsManager;

    /**
     * Map of choice identifiers to the lists of detail field megawidget
     * specifiers.
     */
    private final Map<String, List<IControlSpecifier>> fieldListsForChoices = new HashMap<>();

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
     * @param repeatDetailFields
     *            Flag indicating whether or not a {@link #DETAIL_FIELDS} value
     *            may be a string instead of a list of detail megawidget
     *            specifiers. If true, the string may specify the choice
     *            identifier of a choice defined earlier in the choices list; if
     *            this is the case, the detail fields for the new choice are to
     *            be the same as those of the choice identifier given as the
     *            <code>DETAIL_FIELDS</code> value.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public FlatChoicesWithDetailMegawidgetSpecifier(
            Map<String, Object> parameters,
            BoundedChoiceValidatorHelper<T> stateValidatorHelper,
            boolean repeatDetailFields) throws MegawidgetSpecificationException {
        super(parameters, stateValidatorHelper);
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                ControlSpecifierOptionsManager.BooleanSource.TRUE);

        /*
         * Ensure that the factory is present and acceptable.
         */
        IMegawidgetSpecifierFactory factory = null;
        try {
            factory = (IMegawidgetSpecifierFactory) parameters
                    .get(MEGAWIDGET_SPECIFIER_FACTORY);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_SPECIFIER_FACTORY,
                    parameters.get(MEGAWIDGET_SPECIFIER_FACTORY),
                    "must be IMegawidgetSpecifierFactory");
        }
        if (factory == null) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_SPECIFIER_FACTORY, null, null);
        }

        /*
         * Create the children manager.
         */
        childManager = new ChildSpecifiersManager<IControlSpecifier>(
                IControlSpecifier.class, factory);

        /*
         * Iterate through the choices, extracting from each one any detail
         * megawidgets.
         */
        List<?> choices = (List<?>) parameters.get(MEGAWIDGET_VALUE_CHOICES);
        Set<String> choiceIdentifiers = new HashSet<>(choices.size());
        for (int j = 0; j < choices.size(); j++) {
            Object choice = choices.get(j);
            String choiceIdentifier = getIdentifierOfNode(choice);
            choiceIdentifiers.add(choiceIdentifier);

            /*
             * If this choice is not a map or has no detail fields, skip it.
             */
            if ((choice instanceof Map) == false) {
                continue;
            }
            Map<?, ?> map = (Map<?, ?>) choice;

            /*
             * Remove the detail fields, as they should not be left in the map,
             * and determine whether the value is valid. If repeat detail fields
             * are allowed, then a string specifying the identifier of a
             * previously-defined choice is allowable. Whether or not repeats
             * are allowed, a list of detail megawidget specifiers is allowable.
             * All other non-null values are invalid.
             */
            Object fieldsObj = map.remove(DETAIL_FIELDS);
            if (fieldsObj == null) {
                continue;
            }
            List<IControlSpecifier> children = null;
            if (repeatDetailFields && (fieldsObj instanceof String)) {

                /*
                 * Make sure that the choice identifier specified has already
                 * been defined earlier in the choices list, and get the
                 * children associated with that choice identifier, if any.
                 */
                String fieldsIdentifier = (String) fieldsObj;
                if (fieldsIdentifier.equals(choiceIdentifier)
                        || (choiceIdentifiers.contains(fieldsIdentifier) == false)) {
                    throw new MegawidgetSpecificationException(getIdentifier(),
                            getType(),
                            MEGAWIDGET_VALUE_CHOICES + "[" + j + "]",
                            fieldsIdentifier, getDetailFieldsDescription(true));
                }
                children = fieldListsForChoices.get(fieldsIdentifier);
            } else if (fieldsObj instanceof List) {

                /*
                 * Convert the maps to child megawidget specifiers.
                 */
                List<?> fields = (List<?>) fieldsObj;
                try {
                    children = getChildManager().createMegawidgetSpecifiers(
                            fields, fields.size());
                } catch (MegawidgetSpecificationException e) {
                    throw new MegawidgetSpecificationException(getIdentifier(),
                            getType(),
                            MEGAWIDGET_VALUE_CHOICES + "[" + j + "]", fields,
                            "bad child megawidget specifier", e);
                }

                /*
                 * Add the child megawidget specifiers for this choice to the
                 * list of all children.
                 */
                getChildManager().addChildMegawidgetSpecifiers(children);
            } else {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), MEGAWIDGET_VALUE_CHOICES + "[" + j + "]",
                        fieldsObj,
                        getDetailFieldsDescription(repeatDetailFields));
            }

            /*
             * Remember the parameters for this choice's detail fields, if any
             * were found.
             */
            if (children != null) {
                fieldListsForChoices.put(choiceIdentifier, children);
            }
        }
    }

    // Public Methods

    /**
     * Get the list of detail field megawidget specifiers for the specified
     * choice.
     * 
     * @param identifier
     *            Choice identifier.
     * @return List of detail field megawidget specifiers for this choice.
     */
    public final List<IControlSpecifier> getDetailFieldsForChoice(
            String identifier) {
        return fieldListsForChoices.get(identifier);
    }

    @Override
    public final boolean isEditable() {
        return optionsManager.isEditable();
    }

    @Override
    public final int getWidth() {
        return optionsManager.getWidth();
    }

    @Override
    public final boolean isFullWidthOfDetailPanel() {
        return optionsManager.isFullWidthOfDetailPanel();
    }

    @Override
    public final int getSpacing() {
        return optionsManager.getSpacing();
    }

    @Override
    public final List<IControlSpecifier> getChildMegawidgetSpecifiers() {
        return childManager.getChildMegawidgetSpecifiers();
    }

    // Protected Methods

    /**
     * Get the container child specifiers manager.
     * 
     * @return Container child specifiers manager.
     */
    protected final ChildSpecifiersManager<IControlSpecifier> getChildManager() {
        return childManager;
    }

    // Private Variables

    /**
     * Get the description of what the {@link #DETAIL_FIELDS} value must be.
     * 
     * @param repeatDetailFields
     *            Flag indicating whether or not repeat detail fields are
     *            allowed.
     * @return Description.
     */
    private String getDetailFieldsDescription(boolean repeatDetailFields) {
        return "detail fields must be list of megawidget specifiers"
                + (repeatDetailFields ? " or previously-defined "
                        + "choice identifier" : "");
    }
}
