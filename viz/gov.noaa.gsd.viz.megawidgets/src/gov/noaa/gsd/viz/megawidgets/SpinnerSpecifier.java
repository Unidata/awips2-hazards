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

import gov.noaa.gsd.viz.megawidgets.validators.BoundedComparableValidator;
import gov.noaa.gsd.viz.megawidgets.validators.BoundedNumberValidator;

import java.util.Map;

/**
 * Spinner specifier, for manipulating numbers. The generic parameter
 * <code>T</code> provides the type of the numbers being manipulated.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 23, 2013   2168     Chris.Golden      Initial creation
 * Nov 04, 2013   2336     Chris.Golden      Added implementation of new
 *                                           superclass-specified abstract
 *                                           method, and changed to use
 *                                           multiple bounds on generic
 *                                           wildcard so that T extends
 *                                           both Number and Comparable,
 *                                           and changed to offer option of
 *                                           not notifying listeners of
 *                                           state changes caused by
 *                                           ongoing thumb drags or spinner
 *                                           button presses.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 17, 2014   3982     Chris.Golden      Changed "isFullWidthOfColumn"
 *                                           property to "isFullWidthOfDetailPanel".
 * Oct 20, 2014   4818     Chris.Golden      Changed to only stretch across the
 *                                           full width of a details panel if
 *                                           configured to show a scale widget.
 * Apr 09, 2015   7382     Chris.Golden      Changed to implement new scale
 *                                           capable interface.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see SpinnerMegawidget
 */
public abstract class SpinnerSpecifier<T extends Number & Comparable<T>>
        extends BoundedValueMegawidgetSpecifier<T> implements
        ISingleLineSpecifier, IRapidlyChangingStatefulSpecifier,
        IScaleCapableSpecifier {

    // Public Static Constants

    /**
     * State value increment parameter name; a megawidget may include a value of
     * type <code>T</code> associated with this name. If it does, this acts as
     * the delta by which the value can change when a PageUp/Down key is
     * pressed. If not specified, it is assumed to be the minimum value that
     * would cause a change in value.
     */
    public static final String MEGAWIDGET_PAGE_INCREMENT_DELTA = "incrementDelta";

    // Private Variables

    /**
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager optionsManager;

    /**
     * Flag indicating whether or not the megawidget is to expand to fill all
     * available horizontal space within its parent.
     */
    private final boolean horizontalExpander;

    /**
     * Flag indicating whether or not state changes that are part of a group of
     * rapid changes are to result in notifications to the listener.
     */
    private final boolean sendingEveryChange;

    /**
     * Flag indicating whether or not a scale widget should be shown as well.
     */
    private final boolean showScale;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @param stateValidator
     *            State validator.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public SpinnerSpecifier(Map<String, Object> parameters,
            BoundedComparableValidator<T> stateValidator)
            throws MegawidgetSpecificationException {
        super(parameters, stateValidator);

        /*
         * Ensure that the rapid change notification flag, if provided, is
         * appropriate.
         */
        sendingEveryChange = ConversionUtilities
                .getSpecifierBooleanValueFromObject(getIdentifier(), getType(),
                        parameters.get(MEGAWIDGET_SEND_EVERY_STATE_CHANGE),
                        MEGAWIDGET_SEND_EVERY_STATE_CHANGE, true);

        /*
         * Get the horizontal expansion flag if available.
         */
        horizontalExpander = ConversionUtilities
                .getSpecifierBooleanValueFromObject(getIdentifier(), getType(),
                        parameters.get(EXPAND_HORIZONTALLY),
                        EXPAND_HORIZONTALLY, false);

        /*
         * If the show-scale flag is present, ensure that it is a boolean value.
         */
        showScale = ConversionUtilities.getSpecifierBooleanValueFromObject(
                getIdentifier(), getType(),
                parameters.get(MEGAWIDGET_SHOW_SCALE), MEGAWIDGET_SHOW_SCALE,
                false);

        /*
         * Create the options manager, now that the show-scale flag value is
         * known.
         */
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                (showScale ? ControlSpecifierOptionsManager.BooleanSource.TRUE
                        : ControlSpecifierOptionsManager.BooleanSource.FALSE));
    }

    // Public Methods

    /**
     * Get the increment delta.
     * 
     * @return Increment delta.
     */
    @SuppressWarnings("unchecked")
    public final T getIncrementDelta() {
        return ((BoundedNumberValidator<T>) getStateValidator())
                .getIncrementDelta();
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

        /*
         * Unlike most megawidgets, the integer spinner requires the full width
         * of its parent's column if it is showing a scale, but does not
         * otherwise.
         */
        return showScale;
    }

    @Override
    public final int getSpacing() {
        return optionsManager.getSpacing();
    }

    @Override
    public final boolean isSendingEveryChange() {
        return sendingEveryChange;
    }

    @Override
    public final boolean isHorizontalExpander() {
        return horizontalExpander;
    }

    @Override
    public final boolean isShowScale() {
        return showScale;
    }
}
