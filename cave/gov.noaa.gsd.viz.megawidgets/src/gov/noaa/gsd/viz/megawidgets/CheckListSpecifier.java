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
 * Checklist megawidget specifier.
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
 *                                           (composition over inheritance). Also
 *                                           changed to implement new interfaces
 *                                           IMultiSelectableSpecifier and
 *                                           IMultiLineSpecifier.
 * Oct 31, 2013   2336     Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * Jan 28, 2014   2161     Chris.Golden      Changed to support use of collections
 *                                           instead of only lists for the state.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see CheckListMegawidget
 */
public class CheckListSpecifier extends
        FlatBoundedChoicesMegawidgetSpecifier<Collection<String>> implements
        IControlSpecifier, IMultiSelectableSpecifier, IMultiLineSpecifier {

    // Private Variables

    /**
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager optionsManager;

    /**
     * Number of lines that should be visible.
     */
    private final int numVisibleLines;

    /**
     * Flag indicating whether or not the All and None buttons should be
     * included.
     */
    private final boolean showAllNoneButtons;

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
    public CheckListSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters,
                new MultiFlatChoiceValidatorHelper(MEGAWIDGET_VALUE_CHOICES,
                        CHOICE_NAME, CHOICE_IDENTIFIER, false));
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                ControlSpecifierOptionsManager.BooleanSource.TRUE);

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

        /*
         * Record the value of the show all/none buttons flag.
         */
        showAllNoneButtons = ConversionUtilities
                .getSpecifierBooleanValueFromObject(getIdentifier(), getType(),
                        parameters.get(MEGAWIDGET_SHOW_ALL_NONE_BUTTONS),
                        MEGAWIDGET_SHOW_ALL_NONE_BUTTONS, true);
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

    @Override
    public final boolean shouldShowAllNoneButtons() {
        return showAllNoneButtons;
    }
}
