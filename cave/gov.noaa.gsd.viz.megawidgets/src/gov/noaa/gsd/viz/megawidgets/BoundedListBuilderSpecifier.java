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

import gov.noaa.gsd.viz.megawidgets.validators.MultiFlatChoiceValidatorHelper;

import java.util.Collection;
import java.util.Map;

/**
 * List builder megawidget specifier, used to create megawidgets that allow the
 * user to build up an orderable list from a closed set of choices.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 21, 2013   2168     Chris.Golden      Changed to implement IControlSpecifier
 *                                           and use ControlSpecifierOptionsManager
 *                                           (composition over inheritance), and to
 *                                           implement the new IMultiLineSpecifier
 *                                           interface.
 * Oct 31, 2013   2336     Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * Mar 11, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see BoundedListBuilderMegawidget
 */
public class BoundedListBuilderSpecifier extends
        FlatBoundedChoicesMegawidgetSpecifier<Collection<String>> implements
        IControlSpecifier, IMultiLineSpecifier {

    // Public Static Constants

    /**
     * Megawidget selected items label parameter name; a megawidget may include
     * a value associated with this name, in which case it will be used to label
     * the selected items list. (The {@link #MEGAWIDGET_LABEL} value is used to
     * label the available items list.) Any string is valid as a value.
     */
    public static final String MEGAWIDGET_SELECTED_LABEL = "selectedLabel";

    // Private Variables

    /**
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager optionsManager;

    /**
     * Selected items label.
     */
    private final String selectedLabel;

    /**
     * Number of lines that should be visible.
     */
    private final int numVisibleLines;

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
    public BoundedListBuilderSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, new MultiFlatChoiceValidatorHelper(
                MEGAWIDGET_VALUE_CHOICES, CHOICE_NAME, CHOICE_IDENTIFIER, true));
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                ControlSpecifierOptionsManager.BooleanSource.TRUE);

        /*
         * Ensure that the selected items label, if present, is acceptable.
         */
        try {
            selectedLabel = (String) parameters.get(MEGAWIDGET_SELECTED_LABEL);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_SELECTED_LABEL,
                    parameters.get(MEGAWIDGET_SELECTED_LABEL), "must be string");
        }

        /*
         * Ensure that the visible lines count, if present, is acceptable, and
         * if not present is assigned a default value.
         */
        numVisibleLines = ConversionUtilities
                .getSpecifierIntegerValueFromObject(getIdentifier(), getType(),
                        parameters.get(MEGAWIDGET_VISIBLE_LINES),
                        MEGAWIDGET_VISIBLE_LINES, 6);
        if (numVisibleLines < 1) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_VISIBLE_LINES, numVisibleLines,
                    "must be positive integer");
        }
    }

    // Public Methods

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
    public final int getNumVisibleLines() {
        return numVisibleLines;
    }

    /**
     * Get the selected items label.
     * 
     * @return Selected items label.
     */
    public final String getSelectedLabel() {
        return selectedLabel;
    }
}
