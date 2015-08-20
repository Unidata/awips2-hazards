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

import gov.noaa.gsd.viz.megawidgets.validators.BoundedNumberValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * Spinner megawidget, allowing the manipulation of one or more numbers. The
 * generic parameter <code>T</code> specifies the type of value to be
 * manipulated.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 23, 2013   2168     Chris.Golden      Initial creation
 * Nov 04, 2013   2336     Chris.Golden      Changed to use multiple bounds on
 *                                           generic wildcard so that T extends
 *                                           both Number and Comparable. Also
 *                                           changed to offer option of not
 *                                           notifying listeners of state
 *                                           changes caused by ongoing thumb
 *                                           drags and spinner button presses.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 04, 2014   2155     Chris.Golden      Changed scale widget to snap to
 *                                           the current value when a mouse up
 *                                           occurs over it.
 * Jun 24, 2014   4010     Chris.Golden      Changed to no longer be a subclass
 *                                           of NotifierMegawidget.
 * Jul 22, 2014   4259     Chris.Golden      Fixed bug that caused negative
 *                                           values to not work correctly when
 *                                           the spinner included a scale bar
 *                                           below it. Also changed to ensure
 *                                           spinner text field is large enough
 *                                           to show all the characters of the
 *                                           largest possible string it could be
 *                                           called upon to display with its
 *                                           original minimum, maximum, and
 *                                           precision parameters.
 * Oct 20, 2014   4818     Chris.Golden      Changed to only stretch across the
 *                                           available horizontal space if it is
 *                                           configured to expand horizontally.
 *                                           If not, and if it is configured to
 *                                           show a scale widget, ensure that
 *                                           the scale bar is not too narrow,
 *                                           but do not stretch across all
 *                                           available space.
 * Oct 22, 2014   5050     Chris.Golden      Minor change: Used "or" instead of
 *                                           addition for SWT flags.
 * Feb 04, 2015   5919     Benjamin.Phillippe Added getRoundedValue function
 * Mar 31, 2015   6873     Chris.Golden      Added code to ensure that mouse
 *                                           wheel events are not processed by
 *                                           the megawidget, but are instead
 *                                           passed up to any ancestor that is a
 *                                           scrolled composite.
 * Jul 06, 2015   8413     mduff             Removed code to setLeftDecorationWidth.  This code
 *                                           does not properly handle widgets that are in multiple
 *                                           columns.
 * Jul 31, 2015   4123     Chris.Golden      Moved much of the code to new
 *                                           spinner-and-scale component helper
 *                                           class, so that the latter may be
 *                                           used for both spinner and range
 *                                           megawidgets.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see SpinnerSpecifier
 */
public abstract class SpinnerMegawidget<T extends Number & Comparable<T>>
        extends BoundedValueMegawidget<T> implements IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = new HashSet<>(
                BoundedValueMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        names.add(SpinnerSpecifier.MEGAWIDGET_PAGE_INCREMENT_DELTA);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Protected Classes

    /**
     * Holder for the spinner and scale helper.
     */
    private class Holder implements ISpinnerAndScaleComponentHolder<T> {

        // Public Methods

        @Override
        public T getMinimumValue(String identifier) {
            return SpinnerMegawidget.this.getMinimumValue();
        }

        @Override
        public T getMaximumValue(String identifier) {
            return SpinnerMegawidget.this.getMaximumValue();
        }

        @Override
        public T getPageIncrementDelta() {
            return SpinnerMegawidget.this.getIncrementDelta();
        }

        @Override
        public int getPrecision() {
            return SpinnerMegawidget.this.getPrecision();
        }

        @Override
        public T getState(String identifier) {
            return state;
        }

        @Override
        public List<String> setState(String identifier, T value) {
            if (value.equals(state) == false) {
                state = value;
                return Lists.newArrayList(identifier);
            } else {
                return null;
            }
        }

        @Override
        public List<String> setStates(Map<String, T> valuesForIdentifiers) {
            T value = valuesForIdentifiers.values().iterator().next();
            if (value.equals(state) == false) {
                state = value;
                return Lists.newArrayList(getSpecifier().getIdentifier());
            } else {
                return null;
            }
        }

        @Override
        public void notifyListener(List<String> identifiersOfChangedStates) {
            SpinnerMegawidget.this.notifyListener(getSpecifier()
                    .getIdentifier(), state);
        }
    }

    // Protected Variables

    /**
     * Control component helper.
     */
    private final ControlComponentHelper controlHelper;

    /**
     * Spinner and scale component helper.
     */
    private final SpinnerAndScaleComponentHelper<T> spinnerAndScaleHelper;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param parent
     *            Parent of the megawidget.
     * @param spinnerAndScaleHelper
     *            Spinner and scale component widgets helper.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    protected SpinnerMegawidget(SpinnerSpecifier<T> specifier,
            Composite parent,
            SpinnerAndScaleComponentHelper<T> spinnerAndScaleHelper,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        controlHelper = new ControlComponentHelper(specifier);
        this.spinnerAndScaleHelper = spinnerAndScaleHelper;
        spinnerAndScaleHelper.setHolder(new Holder());

        /*
         * Build the component widgets.
         */
        spinnerAndScaleHelper.buildParentPanelAndLabel(parent, specifier);
        spinnerAndScaleHelper.buildSpinner(specifier, getMinimumValue(),
                getMaximumValue(), getIncrementDelta(),
                specifier.isShowScale(), specifier.getIdentifier());
        spinnerAndScaleHelper.buildScale(specifier, getMinimumValue(),
                getMaximumValue(), null, null, getIncrementDelta(),
                specifier.isShowScale());

        /*
         * Render the spinner uneditable if necessary.
         */
        if (isEditable() == false) {
            setEditable(false);
        }

        /*
         * Synchronize user-facing widgets to the starting state.
         */
        synchronizeComponentWidgetsToState();
    }

    // Public Methods

    @Override
    public Set<String> getMutablePropertyNames() {
        return MUTABLE_PROPERTY_NAMES;
    }

    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            return isEditable();
        } else if (name
                .equals(SpinnerSpecifier.MEGAWIDGET_PAGE_INCREMENT_DELTA)) {
            return getIncrementDelta();
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            setEditable(ConversionUtilities.getPropertyBooleanValueFromObject(
                    getSpecifier().getIdentifier(), getSpecifier().getType(),
                    value, name, null));
        } else if (name
                .equals(SpinnerSpecifier.MEGAWIDGET_PAGE_INCREMENT_DELTA)) {
            ((BoundedNumberValidator<T>) getStateValidator())
                    .setIncrementDelta(value);
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public final boolean isEditable() {
        return controlHelper.isEditable();
    }

    @Override
    public final void setEditable(boolean editable) {
        controlHelper.setEditable(editable);
        spinnerAndScaleHelper.setEditable(editable, controlHelper);
    }

    @Override
    public int getLeftDecorationWidth() {
        return (spinnerAndScaleHelper.getLabel() == null ? 0 : controlHelper
                .getWidestWidgetWidth(spinnerAndScaleHelper.getLabel()));
    }

    @Override
    public void setLeftDecorationWidth(int width) {
        /*
         * TODO RM 8413 - Turning this off since it does not handle megawidgets
         * in different columns correctly. It is cutting off widgets in the
         * right column. Will revisit this when more time is available.
         */
        // if (spinnerAndScaleHelper.getLabel() != null) {
        // controlHelper.setWidgetsWidth(width,
        // spinnerAndScaleHelper.getLabel());
        // }
    }

    @Override
    public final int getRightDecorationWidth() {
        return 0;
    }

    @Override
    public final void setRightDecorationWidth(int width) {

        /*
         * No action.
         */
    }

    /**
     * Get the increment delta.
     * 
     * @return Increment delta.
     */
    public final T getIncrementDelta() {
        return ((BoundedNumberValidator<T>) getStateValidator())
                .getIncrementDelta();
    }

    /**
     * Set the increment delta.
     * 
     * @param value
     *            New increment delta; must be a positive integer.
     * @throws MegawidgetPropertyException
     *             If the object is not a positive integer.
     */
    public final void setIncrementDelta(Object value)
            throws MegawidgetPropertyException {
        ((BoundedNumberValidator<T>) getStateValidator())
                .setIncrementDelta(value);
        spinnerAndScaleHelper.synchronizeComponentWidgetsToPageIncrementDelta();
    }

    // Protected Methods

    @Override
    protected final void synchronizeComponentWidgetsToBounds() {
        spinnerAndScaleHelper.synchronizeComponentWidgetsToBounds();
    }

    @Override
    protected final void doSynchronizeComponentWidgetsToState() {
        spinnerAndScaleHelper.synchronizeComponentWidgetsToState();
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        spinnerAndScaleHelper.setEnabled(enable);
    }

    @Override
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {
        super.doSetState(identifier, state);
        spinnerAndScaleHelper.handleProgrammaticStateChange();
    }

    /**
     * Get the precision for the spinner, that is, the number of decimal places
     * that should come after a decimal point.
     * 
     * @return Non-negative number indicating the precision; if <code>0</code>,
     *         no decimal point will be shown.
     */
    protected abstract int getPrecision();
}