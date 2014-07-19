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

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.viz.megawidgets.validators.BoundedMultiLongValidator;
import gov.noaa.gsd.viz.widgets.IMultiValueLinearControlListener;
import gov.noaa.gsd.viz.widgets.ISnapValueCalculator;
import gov.noaa.gsd.viz.widgets.MultiValueLinearControl;
import gov.noaa.gsd.viz.widgets.MultiValueScale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Multi-time megawidget, providing the user the ability to select one or more
 * times.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 27, 2014   3512     Chris.Golden Initial creation (extracted from
 *                                      TimeScaleMegawidget).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see MultiTimeMegawidgetSpecifier
 */
public abstract class MultiTimeMegawidget extends
        ExplicitCommitStatefulMegawidget implements IParent<IControl>, IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = new HashSet<>(
                StatefulMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        names.add(MultiTimeMegawidgetSpecifier.MEGAWIDGET_STATE_EDITABLES);
        names.add(MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME);
        names.add(MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    /**
     * Snap value calculator, used to generate snap-to values for the scale
     * widget.
     */
    protected static final ISnapValueCalculator SNAP_VALUE_CALCULATOR = new ISnapValueCalculator() {
        private final long INTERVAL = TimeUnit.MINUTES.toMillis(1L);

        private final long HALF_INTERVAL = INTERVAL / 2L;

        @Override
        public long getSnapThumbValue(long value, long minimum, long maximum) {
            long remainder = value % INTERVAL;
            if (remainder < HALF_INTERVAL) {
                value -= remainder;
            } else {
                value += INTERVAL - remainder;
            }
            if (value < minimum) {
                value += INTERVAL
                        * (((minimum - value) / INTERVAL) + ((minimum - value)
                                % INTERVAL == 0 ? 0L : 1L));
            } else if (value > maximum) {
                value -= INTERVAL
                        * (((value - maximum) / INTERVAL) + ((value - maximum)
                                % INTERVAL == 0 ? 0L : 1L));
            }
            return value;
        }
    };

    // Private Static Constants

    /**
     * Width in pixels of the time scale thumbs.
     */
    private static final int SCALE_THUMB_WIDTH = 13;

    /**
     * Height in pixels of the time scale thumbs.
     */
    private static final int SCALE_THUMB_HEIGHT = 21;

    /**
     * Thickness in pixels of the time scale tracks.
     */
    private static final int SCALE_TRACK_THICKNESS = 11;

    /**
     * Width of horizontal padding in pixels to the left and right of the scale
     * widget.
     */
    private static final int SCALE_HORIZONTAL_PADDING = 7;

    /**
     * Height of vertical padding in pixels above and below the scale widget.
     */
    private static final int SCALE_VERTICAL_PADDING_TOP = 1;

    /**
     * Height of vertical padding in pixels above and below the scale widget.
     */
    private static final int SCALE_VERTICAL_PADDING_BOTTOM = 6;

    // Private Classes

    /**
     * Listener for the multi-value scale.
     */
    private class MultiValueScaleListener implements
            IMultiValueLinearControlListener {

        @Override
        public void visibleValueRangeChanged(MultiValueLinearControl widget,
                long lowerValue, long upperValue,
                MultiValueLinearControl.ChangeSource source) {

            /*
             * No action.
             */
        }

        @Override
        public void constrainedThumbValuesChanged(
                MultiValueLinearControl widget, long[] values,
                MultiValueLinearControl.ChangeSource source) {

            /*
             * If the change source is not user-GUI interaction, do nothing.
             */
            if ((source != MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_ONGOING)
                    && (source != MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_COMPLETE)) {
                return;
            }

            /*
             * If only ending state changes are to result in notifications, and
             * this is the first of an ongoing set of state changes, then copy
             * the state before this change is processed.
             */
            if (onlySendEndStateChanges
                    && (source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_ONGOING)
                    && (lastForwardedStatesForIds == null)) {
                lastForwardedStatesForIds = new HashMap<>(statesForIds);
            }

            /*
             * See if notification of listeners should occur as the new values
             * are processed. If all state changes are to result in
             * notifications, or if this is an ending state change and no
             * ongoing state changes occurred beforehand, notification should
             * occur.
             */
            boolean notify = (!onlySendEndStateChanges || ((lastForwardedStatesForIds == null) && (source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_COMPLETE)));

            /*
             * Iterate through the thumbs, determining which have changed their
             * values and responding accordingly. Any notification for these
             * values must occur after all the values have been changed, to
             * avoid having a notification of one value change go out that makes
             * that value higher than the next value, even though once all
             * values have been set, they will be in proper ascending order.
             */
            MultiTimeMegawidgetSpecifier specifier = getSpecifier();
            List<String> stateIdentifiers = specifier.getStateIdentifiers();
            Map<String, Object> valuesForChangedStates = new HashMap<>(
                    stateIdentifiers.size());
            for (int j = 0; j < values.length; j++) {

                /*
                 * Get the new value and see if it has changed, and if so, make
                 * a note of the new value and note that it should be forwarded
                 * to any listeners if this is something that should be sent on.
                 */
                String identifier = stateIdentifiers.get(j);
                if ((statesForIds.get(identifier) == null)
                        || (values[j] != statesForIds.get(identifier))) {
                    statesForIds.put(identifier, values[j]);
                    synchronizeTimeComponentToState(identifier);
                    if (notify) {
                        valuesForChangedStates.put(identifier, values[j]);
                    }
                }
            }
            if (scale.isConstrainedThumbIntervalLocked() == false) {
                unlockedScaleThumbChanged();
            }
            notifyListener(valuesForChangedStates);

            /*
             * If only ending state changes are to result in notifications, this
             * is such a state change, and at least one ongoing state change
             * occurred right before it, see if the state is now different from
             * what it was before the preceding set of ongoing state changes
             * occurred.
             */
            if ((lastForwardedStatesForIds != null)
                    && (source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_COMPLETE)) {

                /*
                 * Compare the current state values with the ones from before
                 * the ongoing state change set occurred; for each of these
                 * pairs that is different, send a notification that the
                 * corresponding state identifier's value has changed.
                 */
                valuesForChangedStates = new HashMap<>(stateIdentifiers.size());
                for (String identifier : stateIdentifiers) {
                    if (statesForIds.get(identifier).equals(
                            lastForwardedStatesForIds.get(identifier)) == false) {
                        valuesForChangedStates.put(identifier,
                                statesForIds.get(identifier));
                    }
                }
                notifyListener(valuesForChangedStates);

                /*
                 * Forget about the last forwarded states, as they are not
                 * needed unless another set of ongoing state changes occurs, in
                 * which case they will be recreated at that time.
                 */
                lastForwardedStatesForIds = null;
            }
        }

        @Override
        public void freeThumbValuesChanged(MultiValueLinearControl widget,
                long[] values, MultiValueLinearControl.ChangeSource source) {

            /*
             * No action.
             */
        }
    }

    // Private Variables

    /**
     * Current time provider.
     */
    private final ICurrentTimeProvider currentTimeProvider;

    /**
     * Map pairing state identifier keys with their current values in
     * milliseconds since the epoch as values.
     */
    private final Map<String, Long> statesForIds;

    /**
     * Copy of {@link #statesForIds} made just before the first state change
     * resulting from a thumb drag is processed. This is used only if the
     * specifier indicates that rapidly-changing values resulting in state
     * changes should not prompt listener notifications.
     */
    private Map<String, Long> lastForwardedStatesForIds;

    /**
     * Map pairing state identifier keys with their current editability flags as
     * values.
     */
    private Map<String, Boolean> editabilityForIds;

    /**
     * Main label of the megawidget, if any.
     */
    private Label mainLabel;

    /**
     * List of labels created for the time components.
     */
    private List<Label> labels;

    /**
     * Mapping of state identifier keys to time components as values.
     */
    private ImmutableMap<String, ITimeComponent> timeComponentsForIds;

    /**
     * Multi-thumbed scale component.
     */
    private MultiValueScale scale;

    /**
     * Flag indicating whether state changes that occur as a result of a thumb
     * drag should be forwarded or not.
     */
    private final boolean onlySendEndStateChanges;

    /**
     * Control component helper.
     */
    private final ControlComponentHelper helper;

    /**
     * Detail child megawidget manager.
     */
    private DetailChildrenManager childManager;

    /**
     * List of additional composites created to hold detail megawidgets.
     */
    private List<GridLayout> additionalDetailCompositeLayouts;

    /**
     * State validator.
     */
    private final BoundedMultiLongValidator stateValidator;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     * @throws MegawidgetException
     *             If an error occurs while creating or initializing any
     *             megawidgets acting as detail fields for the various states.
     */
    protected MultiTimeMegawidget(MultiTimeMegawidgetSpecifier specifier,
            Map<String, Object> paramMap) throws MegawidgetException {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);
        stateValidator = specifier.getStateValidator();
        statesForIds = new HashMap<>();
        List<String> stateIdentifiers = specifier.getStateIdentifiers();
        for (String identifier : stateIdentifiers) {
            statesForIds.put(identifier,
                    (Long) specifier.getStartingState(identifier));
        }

        /*
         * Get the current time provider.
         */
        ICurrentTimeProvider provider = (ICurrentTimeProvider) paramMap
                .get(MultiTimeMegawidgetSpecifier.CURRENT_TIME_PROVIDER);
        if (provider == null) {
            provider = TimeMegawidgetSpecifier.DEFAULT_CURRENT_TIME_PROVIDER;
        }
        currentTimeProvider = provider;

        onlySendEndStateChanges = !specifier.isSendingEveryChange();
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
                .equals(MultiTimeMegawidgetSpecifier.MEGAWIDGET_STATE_EDITABLES)) {
            return new HashMap<>(editabilityForIds);
        } else if (name
                .equals(MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME)) {
            return getLowerVisibleTime();
        } else if (name
                .equals(MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME)) {
            return getUpperVisibleTime();
        }
        return super.getMutableProperty(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            setEditable(ConversionUtilities.getPropertyBooleanValueFromObject(
                    getSpecifier().getIdentifier(), getSpecifier().getType(),
                    value, name, null));
        } else if (name
                .equals(MultiTimeMegawidgetSpecifier.MEGAWIDGET_STATE_EDITABLES)) {

            /*
             * Ensure that the value is a map of state identifiers to booleans.
             */
            Map<String, Boolean> map = null;
            try {
                map = (HashMap<String, Boolean>) value;
                if (map == null) {
                    throw new NullPointerException();
                }
            } catch (Exception e) {
                throw new MegawidgetPropertyException(getSpecifier()
                        .getIdentifier(), name, getSpecifier().getType(),
                        value, "bad map of booleans", e);
            }

            /*
             * Set each state's editability in turn.
             */
            for (String identifier : map.keySet()) {
                setStateEditable(identifier, map.get(identifier));
            }
        } else if (name
                .equals(MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME)) {
            setVisibleTimeRange(
                    ConversionUtilities.getPropertyLongValueFromObject(
                            getSpecifier().getIdentifier(), getSpecifier()
                                    .getType(), value, name, null),
                    getUpperVisibleTime());
        } else if (name
                .equals(MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME)) {
            setVisibleTimeRange(getLowerVisibleTime(),
                    ConversionUtilities.getPropertyLongValueFromObject(
                            getSpecifier().getIdentifier(), getSpecifier()
                                    .getType(), value, name, null));
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public void setMutableProperties(Map<String, Object> properties)
            throws MegawidgetPropertyException {

        /*
         * If the minimum or maximum visible times are being set, set them
         * first, ensuring that the two boundaries are set in the order that
         * will allow the set to occur without error (if they are allowable).
         */
        Object minValueObj = properties
                .get(MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME);
        Object maxValueObj = properties
                .get(MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME);
        if ((minValueObj != null) && (maxValueObj != null)) {
            if ((minValueObj instanceof Number) == false) {
                MultiTimeMegawidgetSpecifier specifier = getSpecifier();
                throw new MegawidgetPropertyException(
                        specifier.getIdentifier(),
                        (minValueObj instanceof Number ? MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME
                                : MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME),
                        specifier.getType(),
                        (minValueObj instanceof Number ? maxValueObj
                                : minValueObj), "must be long integer");
            }
            if (((Number) minValueObj).longValue() >= scale
                    .getUpperVisibleValue()) {
                setMutableProperty(
                        MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME,
                        maxValueObj);
                setMutableProperty(
                        MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME,
                        minValueObj);
            } else {
                setMutableProperty(
                        MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME,
                        minValueObj);
                setMutableProperty(
                        MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME,
                        maxValueObj);
            }
        } else if (minValueObj != null) {
            setMutableProperty(
                    MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME,
                    minValueObj);
        } else if (maxValueObj != null) {
            setMutableProperty(
                    MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME,
                    maxValueObj);
        }

        /*
         * Do what would have been done by the superclass method, except for
         * ignoring any minimum or maximum visible time setting, as that has
         * already been done above.
         */
        for (String name : properties.keySet()) {
            if (!name.equals(MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME)
                    && !name.equals(MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME)) {
                setMutableProperty(name, properties.get(name));
            }
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
        return ((labels == null) || (labels.size() == 0) ? 0 : helper
                .getWidestWidgetWidth(labels.toArray(new Label[labels.size()])));
    }

    @Override
    public void setLeftDecorationWidth(int width) {

        /*
         * Iterate through the labels, setting each of them to have the
         * specified width.
         */
        if ((labels != null) && (labels.size() > 0)) {
            helper.setWidgetsWidth(width,
                    labels.toArray(new Label[labels.size()]));
        }

        /*
         * If any additional composites were created to hold detail megawidgets
         * on rows after any of the time component rows, adjust their left
         * margins to match the new width.
         */
        for (GridLayout layout : additionalDetailCompositeLayouts) {
            layout.marginLeft = width;
        }
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

    @Override
    public final List<IControl> getChildren() {
        return (childManager == null ? Collections.<IControl> emptyList()
                : childManager.getDetailMegawidgets());
    }

    /**
     * Get the lower end of the visible time range.
     * 
     * @return Lower end of the visible time range.
     */
    public long getLowerVisibleTime() {
        return scale.getLowerVisibleValue();
    }

    /**
     * Get the upper end of the visible time range.
     * 
     * @return Upper end of the visible time range.
     */
    public long getUpperVisibleTime() {
        return scale.getUpperVisibleValue();
    }

    /**
     * Set the visible time range to that specified.
     * 
     * @param lower
     *            Lower end of the visible time range.
     * @param upper
     *            Upper end of the visible time range.
     */
    public void setVisibleTimeRange(long lower, long upper) {
        scale.setVisibleValueRange(lower, upper);
    }

    /**
     * Determine whether or not the specified state is editable. Note that if
     * {@link #isEditable()} returns false, then the the individual editability
     * flags for different states are ignored, and the entire megawidget is
     * read-only.
     * 
     * @param identifier
     *            Identifier of the state for which the editability is to be
     *            determined.
     * @return True if the state is editable, false otherwise.
     */
    public final boolean isStateEditable(String identifier) {
        return editabilityForIds.get(identifier);
    }

    /**
     * Set the editability of the specified state. Note that if
     * {@link #isEditable()} returns false, then the the individual editability
     * flags for different states are ignored, and the entire megawidget is
     * read-only.
     * 
     * @param identifier
     *            Identifier of the state for which the editability is to be
     *            determined.
     * @param editable
     *            Flag indicating whether or not the state is to be editable.
     */
    public final void setStateEditable(String identifier, boolean editable) {
        if (editable == editabilityForIds.get(identifier)) {
            return;
        }
        editabilityForIds.put(identifier, editable);
        if (isEditable()) {
            timeComponentsForIds.get(identifier).setEditable(editable, helper);
            scale.setConstrainedThumbEditable(
                    ((MultiTimeMegawidgetSpecifier) getSpecifier())
                            .getIndicesForStateIdentifiers().get(identifier),
                    isScaleThumbEditable(identifier));
        }
    }

    // Protected Methods

    /**
     * Create a time component for the specified state.
     * 
     * @param stateIndex
     *            Index into the state identifiers list for which the time
     *            component is to be created.
     * @param parent
     *            Composite into which to insert the new component.
     * @param text
     *            Label text, or <code>null</code> if none is to be shown.
     * @param verticalIndent
     *            Vertical indent of the component.
     * @return Time component.
     */
    protected abstract ITimeComponent createTimeComponent(int stateIndex,
            Composite parent, String text, int verticalIndent);

    /**
     * Synchronize the time component associated with the specified state
     * identifier to show the value currently associated with said state.
     * 
     * @param identifier
     *            State identifier with which the time component to be
     *            synchronized is associated.
     */
    protected abstract void synchronizeTimeComponentToState(String identifier);

    /**
     * Receive notification that a scale thumb may have moved when the scale's
     * intervals are unlocked.
     */
    protected abstract void unlockedScaleThumbChanged();

    /**
     * Create the widget components of this megawidget. This method must be
     * called by subclass's constructors once they have initialized any member
     * variables.
     * 
     * @param specifier
     *            Specifier.
     * @param parent
     *            Parent of the megawidget.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     * @param minimumInterval
     *            Minimum interval to be used for the multi-value scale.
     * @throws MegawidgetException
     *             If an error occurs while creating or initializing any
     *             megawidgets acting as detail fields for the various states.
     */
    protected final void createWidgetComponents(
            MultiTimeMegawidgetSpecifier specifier, Composite parent,
            Map<String, Object> paramMap, long minimumInterval)
            throws MegawidgetException {

        /*
         * Create a panel in which to place the widgets.
         */
        Composite panel = UiBuilder.buildComposite(parent, 1, SWT.NONE,
                UiBuilder.CompositeType.SINGLE_ROW, specifier);
        ((GridData) panel.getLayoutData()).verticalAlignment = SWT.TOP;

        /*
         * Add an overall label if one is specified and if either multiple state
         * identifiers exist, or only one exists but it has its own label. If,
         * however, only one state identifier exists and it has no label, use
         * the main label in its place.
         */
        List<String> stateIdentifiers = specifier.getStateIdentifiers();
        String labelString = specifier.getLabel();
        String firstStateLabelString = specifier.getStateLabel(stateIdentifiers
                .get(0));
        boolean useMainLabelAsStateLabel = false;
        if ((labelString != null) && (labelString.length() > 0)) {
            if ((stateIdentifiers.size() == 1)
                    && ((firstStateLabelString == null) || (firstStateLabelString
                            .length() == 0))) {
                useMainLabelAsStateLabel = true;
                mainLabel = null;
            } else {
                mainLabel = UiBuilder.buildLabel(panel, specifier);
            }
        } else {
            mainLabel = null;
        }

        /*
         * Get the starting value(s) for the different state identifiers, and
         * determine the starting editability states for the state identifiers.
         */
        editabilityForIds = new HashMap<>();
        long[] startingValues = new long[specifier.getStateIdentifiers().size()];
        boolean[] startingEditabilities = new boolean[startingValues.length];
        for (int j = 0; j < startingValues.length; j++) {
            String identifier = specifier.getStateIdentifiers().get(j);
            startingValues[j] = statesForIds.get(identifier);
            startingEditabilities[j] = specifier.isStateEditable(identifier);
            editabilityForIds.put(identifier, startingEditabilities[j]);
        }

        /*
         * Create the child manager for the detail megawidgets that may be
         * associated with the different states if appropriate.
         */
        childManager = (specifier.getChildMegawidgetSpecifiers().size() > 0 ? new DetailChildrenManager(
                paramMap) : null);

        /*
         * Iterate through the state identifiers, creating time components for
         * each.
         */
        Map<String, ITimeComponent> timeComponentsForIds = new HashMap<>();
        labels = new ArrayList<>();
        additionalDetailCompositeLayouts = new ArrayList<>();
        int greatestHeight = 0;
        for (int j = 0; j < specifier.getStateIdentifiers().size(); j++) {
            String identifier = specifier.getStateIdentifiers().get(j);

            /*
             * Determine what text label, if any, to use for this state
             * identifier.
             */
            String text;
            if (useMainLabelAsStateLabel) {
                useMainLabelAsStateLabel = false;
                text = labelString;
            } else {
                text = specifier.getStateLabel(identifier);
            }

            /*
             * Create the time field component and any detail megawidgets that
             * go with them. If the latter are created and they are the greatest
             * height recorded so far, remember their height.
             */
            int height = createTimeFieldComponent(specifier, panel, identifier,
                    j, text, timeComponentsForIds);
            if (height > greatestHeight) {
                greatestHeight = height;
            }
        }
        this.timeComponentsForIds = ImmutableMap.copyOf(timeComponentsForIds);

        /*
         * Determine which detail megawidgets that were created take up the full
         * width of this megawidget, and align all such megawidgets' components
         * to bring some visual order to the widget soup.
         */
        if (childManager != null) {
            List<IControl> fullWidthDetailMegawidgets = new ArrayList<>();
            for (IControl detailMegawidget : childManager
                    .getDetailMegawidgets()) {
                if (((IControlSpecifier) detailMegawidget.getSpecifier())
                        .isFullWidthOfDetailPanel()) {
                    fullWidthDetailMegawidgets.add(detailMegawidget);
                }
            }
            ControlComponentHelper
                    .alignMegawidgetsElements(fullWidthDetailMegawidgets);
        }

        /*
         * If at least one time component has additional megawidgets, ensure
         * that all the time components have the right minimum height so as to
         * not make the highest row look larger than the others.
         */
        if (greatestHeight > 0) {
            for (ITimeComponent timeComponent : timeComponentsForIds.values()) {
                timeComponent.setHeight(greatestHeight);
            }
        }

        /*
         * Create the multi-thumbed scale component.
         */
        createMultiValueScaleComponent(specifier, panel, paramMap,
                minimumInterval, startingValues, startingEditabilities);

        /*
         * Bind the scale component's value change events to trigger a change in
         * the record of the state for the widget, and a change in the
         * corresponding text component.
         */
        scale.addMultiValueLinearControlListener(new MultiValueScaleListener());

        /*
         * Render the widget uneditable if necessary.
         */
        if (isEditable() == false) {
            doSetEditable(false);
        }
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (mainLabel != null) {
            mainLabel.setEnabled(enable);
        }
        for (ITimeComponent timeComponent : timeComponentsForIds.values()) {
            timeComponent.setEnabled(enable);
        }
        scale.setEnabled(enable);
        for (IControl child : getChildren()) {
            child.setEnabled(enable);
        }
    }

    @Override
    protected final Object doGetState(String identifier) {
        return statesForIds.get(identifier);
    }

    @Override
    protected void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        /*
         * Create a copy of the state values, except with the specified
         * identifier's new potential value.
         */
        Map<String, Object> map = new HashMap<>();
        for (String thisIdentifier : statesForIds.keySet()) {
            map.put(thisIdentifier, (identifier.equals(thisIdentifier) ? state
                    : statesForIds.get(thisIdentifier)));
        }

        /*
         * Validate the new state.
         */
        Map<String, Long> validMap;
        try {
            validMap = stateValidator.convertToStateValues(map);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
        statesForIds.put(identifier, validMap.get(identifier));

        /*
         * Synchronize the user-facing widgets for this state identifier with
         * the new state.
         */
        synchronizeComponentWidgetsToState(identifier);
    }

    @Override
    protected void doCommitStateChanges(Map<String, Object> newStatesForIds)
            throws MegawidgetStateException {

        /*
         * Create a copy of the state values, except using the new values for
         * those state identifiers that are found in the new map.
         */
        Map<String, Object> map = new HashMap<>();
        for (String identifier : statesForIds.keySet()) {
            map.put(identifier,
                    (newStatesForIds.containsKey(identifier) ? newStatesForIds
                            .get(identifier) : statesForIds.get(identifier)));
        }

        /*
         * Validate the new values.
         */
        Map<String, Long> validMap;
        try {
            validMap = stateValidator.convertToStateValues(map);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
        statesForIds.clear();
        statesForIds.putAll(validMap);

        /*
         * Synchronize the user-facing component widgets with the new state.
         */
        synchronizeComponentWidgetsToState();
    }

    @Override
    protected final String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        return (state == null ? null : DateTimeComponent
                .getStateDescription(ConversionUtilities
                        .getStateLongValueFromObject(identifier, getSpecifier()
                                .getType(), state, null)));
    }

    @Override
    protected void ensureStateIsValid(String identifier, Object state)
            throws MegawidgetStateException {
        try {
            stateValidator.convertToStateValue(identifier, state);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
    }

    @Override
    protected void doSynchronizeComponentWidgetsToState() {

        /*
         * Commit the values to the state records, and change the text widgets
         * to match.
         */
        MultiTimeMegawidgetSpecifier specifier = getSpecifier();
        for (String identifier : statesForIds.keySet()) {
            Long value = statesForIds.get(identifier);
            setStateInternally(identifier, value);
            synchronizeTimeComponentToState(identifier);
        }

        /*
         * Compile a list of values, one per thumb, using the new values where
         * appropriate, and the old values where no new values are given.
         */
        List<String> stateIdentifiers = specifier.getStateIdentifiers();
        long[] values = new long[stateIdentifiers.size()];
        for (int j = 0; j < values.length; j++) {
            values[j] = statesForIds.get(stateIdentifiers.get(j));
        }

        /*
         * Tell the scale about the new thumb values.
         */
        try {
            scale.setConstrainedThumbValues(values);
        } catch (Exception e) {
            throw new IllegalStateException("set of time scale values failed",
                    e);
        }
    }

    @Override
    protected void doSynchronizeComponentWidgetsToState(String identifier) {

        /*
         * Record the change in the state records.
         */
        long value = statesForIds.get(identifier);
        setStateInternally(identifier, value);

        /*
         * Tell the scale widget and the time component about the change.
         */
        scale.setConstrainedThumbValue(
                ((MultiTimeMegawidgetSpecifier) getSpecifier())
                        .getIndicesForStateIdentifiers().get(identifier), value);
        synchronizeTimeComponentToState(identifier);
    }

    /**
     * Get the current time provider.
     * 
     * @return Current time provider.
     */
    protected final ICurrentTimeProvider getCurrentTimeProvider() {
        return currentTimeProvider;
    }

    /**
     * Get the state validator.
     * 
     * @return State validator.
     */
    protected final BoundedMultiLongValidator getStateValidator() {
        return stateValidator;
    }

    /**
     * Get the time component for the specified identifier.
     * 
     * @param identifier
     *            Identifier associated with the time component to be fetched.
     * @return Time component for the specified identifier.
     */
    @SuppressWarnings("unchecked")
    protected final <T extends ITimeComponent> T getTimeComponent(
            String identifier) {
        return (T) timeComponentsForIds.get(identifier);
    }

    /**
     * Get the multi-thumbed scale widget.
     * 
     * @return Multi-thumbed scale widget.
     */
    protected final MultiValueScale getScale() {
        return scale;
    }

    /**
     * Determine whether or not this megawidget should only send ending state
     * changes.
     * 
     * @return True if the megawidget should only send ending state changes,
     *         false otherwise.
     */
    protected final boolean isOnlySendEndStateChanges() {
        return onlySendEndStateChanges;
    }

    /**
     * Get the state internally for the specified identifier. This method may be
     * used by subclasses instead of {@link #getState(String)} or
     * {@link #doGetState(String)} to avoid pointless typecasting and exception
     * handling.
     * 
     * @param identifier
     *            Identifier of the state to be fetched.
     * @return Value of the specified state.
     */
    protected final Long getStateInternally(String identifier) {
        return statesForIds.get(identifier);
    }

    /**
     * Get the values for all state identifiers.
     * 
     * @return Copy of the map of state identifiers to their values.
     */
    protected final Map<String, Object> getStates() {
        Map<String, Object> map = new HashMap<>(statesForIds.size());
        map.putAll(statesForIds);
        return map;
    }

    /**
     * Set the state internally for the specified identifier. This method may be
     * used by subclasses instead of {@link #setState(String, Object)} or
     * {@link #doSetState(String, Object)} to avoid pointless typecasting and
     * exception handling.
     * 
     * @param identifier
     *            State identifier to be changed.
     * @param value
     *            New value of the state associated with the identifier.
     */
    protected final void setStateInternally(String identifier, Long value) {
        statesForIds.put(identifier, value);
        if (lastForwardedStatesForIds != null) {
            lastForwardedStatesForIds.put(identifier, value);
        }
    }

    /**
     * Convert the specified value to one that would be acceptable as a
     * multi-value scale value. This means that it will be snapped to the
     * closest legitimate value on the multi-value scale widget, and of course
     * between the minimum and maximum allowable values.
     * 
     * @param value
     *            Value to be converted.
     * @return Converted value.
     */
    protected final long convertToValueAcceptableToScale(long value) {
        return SNAP_VALUE_CALCULATOR.getSnapThumbValue(value,
                stateValidator.getMinimumValue(),
                stateValidator.getMaximumValue());
    }

    /**
     * Determine whether or not the specified value is within the range between
     * any neighboring values. The specified value must already have been run
     * through {@link #convertToValueAcceptableToScale(long)}.
     * 
     * @param index
     *            Index of the thumb in the multi-value scale for which this
     *            value is intended.
     * @param value
     *            Value to be checked.
     * @return True if the value is within the range between any neighboring
     *         values, false otherwise.
     */
    protected final boolean isValueBetweenNeighboringValues(int index,
            long value) {
        if ((index < scale.getConstrainedThumbValueCount() - 1)
                && (value > scale.getConstrainedThumbValue(index + 1)
                        - scale.getMinimumDeltaBetweenConstrainedThumbs())) {
            return false;
        } else if ((index > 0)
                && (value < scale.getConstrainedThumbValue(index - 1)
                        + scale.getMinimumDeltaBetweenConstrainedThumbs())) {
            return false;
        }
        return true;
    }

    /**
     * Determine whether or not the specified first value for the multi-value
     * scale would push any of the other values beyond the maximum. The
     * specified value must already have been run through
     * {@link #convertToValueAcceptableToScale(long)}. This method is to be used
     * when the multi-value scale's intervals are locked, and thus moving the
     * first value will also move the other values in concert.
     * 
     * @param value
     *            Value to be checked.
     * @return True if the value keeps all the other values within the allowable
     *         range, false otherwise.
     */
    protected final boolean isFirstValueKeepingAllValuesWithinRange(long value) {
        long delta = scale.getConstrainedThumbValue(scale
                .getConstrainedThumbValueCount() - 1)
                - scale.getConstrainedThumbValue(0);
        return (value + delta <= stateValidator.getMaximumValue());
    }

    /**
     * Set the specified value, supplied by a time component, as the state
     * associated with the specified identifier. If set successfully, the
     * multi-value scale will be updated as well as the internal state record.
     * If the intervals in the multi-value scale are locked, if the state to be
     * changed is the first state in the megawidget, other values will be
     * changed accordingly in order to maintain their intervals.
     * 
     * @param identifier
     *            Identifier of the state to be changed.
     * @param index
     *            Index of the state identifier; must be 0 if the multi-value
     *            scale has its intervals locked.
     * @param value
     *            New value.
     * @return True if the state is changed, false otherwise.
     */
    protected final boolean setValueIfChanged(String identifier, int index,
            long value) {

        /*
         * If the value is actually different from the old value, handle the
         * change. Do it differently depending upon whether or not the scale's
         * intervals are locked and the state being changed is the first one.
         */
        if (scale.getConstrainedThumbValue(index) != value) {
            if (scale.isConstrainedThumbIntervalLocked() && (index == 0)) {

                /*
                 * Compile an array of new values by using the provided value as
                 * the first new value, and offsetting all the other values from
                 * that one by the same offsets they had from the old first
                 * value.
                 */
                List<Long> oldValues = scale.getConstrainedThumbValues();
                long[] values = new long[oldValues.size()];
                values[0] = value;
                long oldFirstValue = oldValues.get(0);
                for (int j = 1; j < values.length; j++) {
                    values[j] = value + oldValues.get(j) - oldFirstValue;
                }

                /*
                 * If the scale accepts the new values, record the new states
                 * internally.
                 */
                if (getScale().setConstrainedThumbValues(values)) {
                    List<String> stateIdentifiers = ((IStatefulSpecifier) getSpecifier())
                            .getStateIdentifiers();
                    for (int j = 0; j < values.length; j++) {
                        setStateInternally(stateIdentifiers.get(j), values[j]);
                    }
                    return true;
                }
            } else {

                /*
                 * If the value has changed and is acceptable to the scale
                 * widget, then change the use it as the new value.
                 */
                if (scale.setConstrainedThumbValue(index, value)) {
                    setStateInternally(identifier, value);
                    return true;
                }
            }
        }

        /*
         * Since no change occurred, reset the time component to show the old
         * value, in case the value that it is presenting is not the same as the
         * one that was passed in here (since the latter will have been
         * processed by the convertToValueAcceptableToScale() method).
         */
        synchronizeTimeComponentToState(identifier);
        return false;
    }

    // Private Methods

    /**
     * Create the time field component for the specified state identifier.
     * 
     * @param specifier
     *            Megawidget specifier.
     * @param parent
     *            Parent composite of any widgets to be created.
     * @param identifier
     *            State identifier for which to create the time field component.
     * @param stateIndex
     *            Index into the state identifiers list for which the time
     *            component is to be created.
     * @param labelText
     *            Text to be used to label the time field.
     * @param timeComponentsForIds
     *            Map of state identifiers to their time components. The time
     *            field component created here will be added to this map.
     * @return Height in pixels of the largest detail field megawidget created
     *         in the same row as the time field widgets, or 0 if no such detail
     *         field megawidget was created.
     * @throw MegawidgetException If an error occurs while creating or
     *        initializing any megawidgets acting as detail fields for this
     *        state.
     */
    private int createTimeFieldComponent(
            MultiTimeMegawidgetSpecifier specifier, Composite parent,
            String identifier, int stateIndex, String labelText,
            Map<String, ITimeComponent> timeComponentsForIds)
            throws MegawidgetException {

        /*
         * If there are additional megawidgets to be placed to the right of the
         * time component, create a composite to act as a parent for both the
         * time component and the additional megawidgets.
         */
        List<IControlSpecifier> detailSpecifiers = specifier
                .getDetailFieldsForState(identifier);
        Composite statePanel = UiBuilder
                .getOrCreateCompositeForComponentWithDetailMegawidgets(
                        detailSpecifiers, parent, SWT.NONE);

        /*
         * Create the time component for this state identifier, and make it
         * read-only if appropriate.
         */
        ITimeComponent timeComponent = createTimeComponent(stateIndex,
                statePanel, labelText,
                (statePanel == parent ? specifier.getSpacing() : 0));
        labels.add(timeComponent.getLabel());
        if (editabilityForIds.get(identifier) == false) {
            timeComponent.setEditable(false, helper);
        }

        /*
         * If there are additional megawidgets, lay out the composite in which
         * the time component and the first row of said megawidgets are found,
         * and create the megawidgets.
         */
        int height = 0;
        if (statePanel != parent) {
            GridData statePanelLayoutData = new GridData(SWT.LEFT, SWT.CENTER,
                    false, false);
            statePanelLayoutData.verticalIndent = specifier.getSpacing();
            statePanel.setLayoutData(statePanelLayoutData);
            List<Composite> additionalComposites = childManager
                    .createDetailChildMegawidgets(statePanel, parent, 0,
                            specifier.isEnabled(), detailSpecifiers)
                    .getComposites();
            for (Composite composite : additionalComposites) {
                additionalDetailCompositeLayouts.add((GridLayout) composite
                        .getLayout());
            }
            height = statePanel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        }

        /*
         * Add the time component to the map.
         */
        timeComponentsForIds.put(identifier, timeComponent);
        return height;
    }

    /**
     * Create the multi-value scale component.
     * 
     * @param specifier
     *            Megawidget specifier.
     * @param parent
     *            Parent composite of any widgets to be created.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     * @param minimumInterval
     *            Minimum interval to be used for the multi-value scale.
     * @param startingValues
     *            Array of values to be used as the initial values.
     * @param startingEditabilities
     *            Array of booleans indicating which of the values in the
     *            corresponding indices of <code>startingValues</code> are to be
     *            editable to begin with.
     */
    private void createMultiValueScaleComponent(
            MultiTimeMegawidgetSpecifier specifier, Composite parent,
            Map<String, Object> paramMap, long minimumInterval,
            long[] startingValues, boolean[] startingEditabilities) {
        scale = new MultiValueScale(parent, stateValidator.getMinimumValue(),
                stateValidator.getMaximumValue());
        scale.setSnapValueCalculator(SNAP_VALUE_CALCULATOR);
        scale.setInsets(SCALE_HORIZONTAL_PADDING, SCALE_VERTICAL_PADDING_TOP,
                SCALE_HORIZONTAL_PADDING, SCALE_VERTICAL_PADDING_BOTTOM);
        scale.setComponentDimensions(SCALE_THUMB_WIDTH, SCALE_THUMB_HEIGHT,
                SCALE_TRACK_THICKNESS);
        scale.setVisibleValueRange((Long) paramMap
                .get(MultiTimeMegawidgetSpecifier.MINIMUM_VISIBLE_TIME),
                (Long) paramMap
                        .get(MultiTimeMegawidgetSpecifier.MAXIMUM_VISIBLE_TIME));
        scale.setMinimumDeltaBetweenConstrainedThumbs(minimumInterval);
        scale.setConstrainedThumbValues(startingValues);
        for (int j = 0; j < startingEditabilities.length; j++) {
            if (startingEditabilities[j] == false) {
                scale.setConstrainedThumbEditable(j, false);
            }
        }
        for (int j = 1; j < startingValues.length; j++) {
            scale.setConstrainedThumbRangeColor(j, Display.getCurrent()
                    .getSystemColor(SWT.COLOR_LIST_SELECTION));
        }
        scale.setEnabled(specifier.isEnabled());
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 2;
        gridData.verticalIndent = specifier.getSpacing();
        scale.setLayoutData(gridData);
    }

    /**
     * Change the component widgets to ensure their state matches that of the
     * editable flag.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     */
    private void doSetEditable(boolean editable) {
        for (String identifier : timeComponentsForIds.keySet()) {
            timeComponentsForIds.get(identifier).setEditable(
                    (editable == false ? false
                            : editabilityForIds.get(identifier)), helper);
        }
        Map<String, Integer> indicesForIds = ((MultiTimeMegawidgetSpecifier) getSpecifier())
                .getIndicesForStateIdentifiers();
        for (String identifier : indicesForIds.keySet()) {
            scale.setConstrainedThumbEditable(indicesForIds.get(identifier),
                    (editable && isScaleThumbEditable(identifier)));
        }
    }

    /**
     * Determine whether or not the specified state's thumb in the scale should
     * be editable. It is assumed that the megawidget as a whole is editable.
     * 
     * @param identifier
     *            State identifier associated with the scale thumb that is being
     *            checked for editability.
     * @return True if the scale thumb should be editable, false otherwise.
     */
    private boolean isScaleThumbEditable(String identifier) {
        return (scale.isConstrainedThumbIntervalLocked() ? editabilityForIds
                .get(((IStatefulSpecifier) getSpecifier())
                        .getStateIdentifiers().get(0)) : editabilityForIds
                .get(identifier));
    }
}