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

import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;

/**
 * Time megawidget, providing the user the ability to select a single date-time
 * value.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 13, 2013    2545    Chris.Golden      Initial creation
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TimeSpecifier
 */
public class TimeMegawidget extends StatefulMegawidget implements IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = Sets
                .newHashSet(NotifierMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Private Variables

    /**
     * Current value in milliseconds since the epoch.
     */
    private long state;

    /**
     * Range of state that are allowed, from minimum to maximum.
     */
    private final Range<Long> bounds;

    /**
     * Current time provider.
     */
    private final ICurrentTimeProvider currentTimeProvider;

    /**
     * Flag indicating whether state changes that occur as a result of a rapidly
     * repeatable action should be forwarded or not.
     */
    private final boolean onlySendEndStateChanges;

    /**
     * Date-time component.
     */
    private final DateTimeComponent dateTime;

    /**
     * Manager of the date-time component.
     */
    private final IDateTimeComponentHolder dateTimeManager = new IDateTimeComponentHolder() {
        @Override
        public long getCurrentTime() {
            return currentTimeProvider.getCurrentTime();
        }

        @Override
        public Range<Long> getAllowableRange(String identifier) {
            return bounds;
        }

        @Override
        public boolean isValueChangeAcceptable(String identifier, long value) {
            return true;
        }

        @Override
        public void valueChanged(String identifier, long value,
                boolean rapidChange) {
            state = value;
            if ((onlySendEndStateChanges == false) || (rapidChange == false)) {
                notifyListeners();
            }
        }
    };

    /**
     * Control component helper.
     */
    private final ControlComponentHelper helper;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param parent
     *            Parent of the megawidget.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    protected TimeMegawidget(TimeSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);

        // Set up the control helper, get the minimum and
        // maximum allowable values and the current time
        // provider, and set the initial state to midway
        // between the bounds.
        helper = new ControlComponentHelper(specifier);
        bounds = Ranges.closed((Long) paramMap.get(TimeSpecifier.MINIMUM_TIME),
                (Long) paramMap.get(TimeSpecifier.MAXIMUM_TIME));
        ICurrentTimeProvider provider = (ICurrentTimeProvider) paramMap
                .get(TimeSpecifier.CURRENT_TIME_PROVIDER);
        if (provider == null) {
            provider = TimeMegawidgetSpecifier.DEFAULT_CURRENT_TIME_PROVIDER;
        }
        currentTimeProvider = provider;
        state = ((bounds.upperEndpoint() - bounds.lowerEndpoint()) / 2L)
                + bounds.lowerEndpoint();

        // Create the date-time component that constructs
        // and manages the component widgets.
        onlySendEndStateChanges = !specifier.isSendingEveryChange();
        dateTime = new DateTimeComponent(null, parent, specifier.getLabel(),
                specifier, state, specifier.isHorizontalExpander(),
                !specifier.isSendingEveryChange(), dateTimeManager);

        // Render the widget uneditable if necessary.
        if (isEditable() == false) {
            doSetEditable(false);
        }
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
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            setEditable(getPropertyBooleanValueFromObject(value, name, null));
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public final boolean isEditable() {
        return helper.isEditable();
    }

    @Override
    public final void setEditable(boolean editable) {
        helper.setEditable(editable);
        doSetEditable(editable);
    }

    @Override
    public int getLeftDecorationWidth() {
        return (dateTime.getLabel() == null ? 0 : helper
                .getWidestWidgetWidth(dateTime.getLabel()));
    }

    @Override
    public void setLeftDecorationWidth(int width) {
        if (dateTime.getLabel() != null) {
            helper.setWidgetsWidth(width, dateTime.getLabel());
        }
    }

    @Override
    public final int getRightDecorationWidth() {
        return 0;
    }

    @Override
    public final void setRightDecorationWidth(int width) {

        // No action.
    }

    // Protected Methods

    @Override
    protected final void doSetEnabled(boolean enable) {
        dateTime.setEnabled(enable);
    }

    @Override
    protected final Object doGetState(String identifier) {
        return state;
    }

    @Override
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        // Ensure the new state is within the allowable value bounds.
        TimeSpecifier specifier = getSpecifier();
        long value = getStateLongValueFromObject(state, identifier,
                bounds.lowerEndpoint());
        if (bounds.contains(value) == false) {
            throw new MegawidgetStateException(identifier, specifier.getType(),
                    value, "out of bounds (minimum = " + bounds.lowerEndpoint()
                            + ", maximum = " + bounds.upperEndpoint()
                            + " (inclusive))");
        }

        // Record the state.
        this.state = value;

        // Notify the date-time component of the updated state.
        dateTime.setState(value);
    }

    @Override
    protected final String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        return (state == null ? null : DateTimeComponent
                .getStateDescription(getStateLongValueFromObject(state,
                        identifier, null)));
    }

    // Private Methods

    /**
     * Change the component widgets to ensure their state matches that of the
     * editable flag.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     */
    private void doSetEditable(boolean editable) {
        dateTime.setEditable(editable, helper);
    }

    /**
     * Notify listeners of a state change.
     */
    private void notifyListeners() {
        notifyListener(getSpecifier().getIdentifier(), state);
        notifyListener();
    }
}