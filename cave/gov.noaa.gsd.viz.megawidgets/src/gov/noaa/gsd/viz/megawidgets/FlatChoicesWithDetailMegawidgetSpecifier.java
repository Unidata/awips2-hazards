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
import java.util.List;
import java.util.Map;

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
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public FlatChoicesWithDetailMegawidgetSpecifier(
            Map<String, Object> parameters,
            BoundedChoiceValidatorHelper<T> stateValidatorHelper)
            throws MegawidgetSpecificationException {
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
        for (int j = 0; j < choices.size(); j++) {
            Object choice = choices.get(j);

            /*
             * If this choice is not a map or has no detail fields, skip it.
             */
            if ((choice instanceof Map) == false) {
                continue;
            }
            Map<?, ?> map = (Map<?, ?>) choice;
            List<?> fields = (List<?>) map.get(DETAIL_FIELDS);
            if (fields == null) {
                continue;
            }

            /*
             * Remove the detail fields, as they should not be left in the map.
             */
            map.remove(DETAIL_FIELDS);

            /*
             * Convert the maps to child megawidget specifiers.
             */
            List<IControlSpecifier> children = null;
            try {
                children = getChildManager().createMegawidgetSpecifiers(fields,
                        fields.size());
            } catch (MegawidgetSpecificationException e) {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), MEGAWIDGET_VALUE_CHOICES + "[" + j + "]",
                        fields, "bad child megawidget specifier", e);
            }

            /*
             * Add the child megawidget specifiers for this choice to the list
             * of all children, and remember the parameters for this choice's
             * detail fields.
             */
            getChildManager().addChildMegawidgetSpecifiers(children);
            fieldListsForChoices.put(getIdentifierOfNode(map), children);
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
    public final boolean isFullWidthOfColumn() {
        return optionsManager.isFullWidthOfColumn();
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
}
