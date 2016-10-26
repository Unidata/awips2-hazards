/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import gov.noaa.gsd.viz.megawidgets.validators.GraphValidator;
import gov.noaa.gsd.viz.widgets.Graph;
import gov.noaa.gsd.viz.widgets.Graph.ChangeSource;
import gov.noaa.gsd.viz.widgets.IGraphListener;
import gov.noaa.gsd.viz.widgets.PlottedPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.ImmutableSet;

/**
 * Description: Graph megawidget.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 31, 2016   15931    Chris.Golden Initial creation.
 * Apr 01, 2016   15931    Chris.Golden Added capability to have user 
 *                                      edit the points via dragging them.
 * Apr 02, 2016   15931    Chris.Golden Changed reference to starting state
 *                                      from specifier to be a copy of the
 *                                      starting state, because the reference
 *                                      meant that the state object was being
 *                                      shared with the source (in this case,
 *                                      an ObservedHazardEvent, meaning
 *                                      updates to the event did not pass the
 *                                      changed() test and thus did not
 *                                      result in attributes-modified
 *                                      notifications.
 * Apr 06, 2016   15931    Chris.Golden Added ability to allow the user
 *                                      to draw points via a click, drag,
 *                                      and release mouse operation, if
 *                                      the graph is empty of points.
 * Oct 26, 2016   25773    Chris.Golden Added height multiplier option,
 *                                      allowing the height the megawidget
 *                                      takes up to be configured.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class GraphMegawidget extends StatefulMegawidget implements IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = new HashSet<>(
                StatefulMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Private Classes

    /**
     * Listener for the graph.
     */
    private class GraphListener implements IGraphListener {

        @Override
        public void plottedPointsChanged(Graph widget, ChangeSource source) {

            /*
             * If the change source is not user-GUI interaction, do nothing.
             */
            if (source == Graph.ChangeSource.METHOD_INVOCATION) {
                return;
            }

            /*
             * If only ending state changes are to result in notifications, and
             * this is the first of an ongoing set of state changes, then copy
             * the state before this change is processed.
             */
            if (onlySendEndStateChanges
                    && (source == Graph.ChangeSource.USER_GUI_INTERACTION_ONGOING)
                    && (lastForwardedState == null)) {
                lastForwardedState = getStateCopy();
            }

            /*
             * Compile the new state.
             */
            state.clear();
            for (PlottedPoint point : graph.getPlottedPoints()) {
                Map<String, Object> map = new HashMap<>(3, 1.0f);
                map.put(GraphValidator.PLOT_POINT_X, point.getX());
                map.put(GraphValidator.PLOT_POINT_Y, point.getY());
                map.put(GraphValidator.PLOT_POINT_EDITABLE, point.isEditable());
                state.add(map);
            }

            /*
             * If all state changes are to result in notifications, or if this
             * is an ending state change and no ongoing state changes occurred
             * beforehand, notify the listener of the change. Otherwise, if only
             * ending state changes are to result in notifications, this is an
             * ending state change, and at least one ongoing state change
             * occurred right before it, see if the state is now different from
             * what it was before the preceding set of ongoing state changes
             * occurred, and if so, notify the listener.
             */
            if ((onlySendEndStateChanges == false)
                    || ((lastForwardedState == null) && (source == Graph.ChangeSource.USER_GUI_INTERACTION_COMPLETE))) {
                notifyListener(getSpecifier().getIdentifier(), getStateCopy());
            } else if ((lastForwardedState != null)
                    && (source == Graph.ChangeSource.USER_GUI_INTERACTION_COMPLETE)) {

                /*
                 * Send a notification if the state has changed since the last
                 * forwarded state. By only doing it in this case, the
                 * megawidget avoids sending a notification when the ending
                 * state change brings the state right back to what it was
                 * before.
                 */
                if (state.equals(lastForwardedState) == false) {
                    notifyListener(getSpecifier().getIdentifier(),
                            getStateCopy());
                }

                /*
                 * Forget about the last forwarded state, as it are not needed
                 * unless another set of ongoing state changes occurs, in which
                 * case it will be recreated at that time.
                 */
                lastForwardedState = null;
            }
        }
    }

    // Private Variables

    /**
     * Label associated with this megawidget, if any.
     */
    private final Label label;

    /**
     * Graph control associated with this megawidget.
     */
    private final Graph graph;

    /**
     * Control component helper.
     */
    private final ControlComponentHelper controlHelper;

    /**
     * Current state.
     */
    private final List<Map<String, Object>> state;

    /**
     * Last state that was forwarded to any listener, or last state set
     * programmatically, whichever happened last.
     */
    private List<Map<String, Object>> lastForwardedState;

    /**
     * State validator.
     */
    private final GraphValidator stateValidator;

    /**
     * Flag indicating whether state changes that occur as a result of an
     * ongoing plotted point drag should be forwarded or not.
     */
    private final boolean onlySendEndStateChanges;

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
    @SuppressWarnings("unchecked")
    protected GraphMegawidget(GraphSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        controlHelper = new ControlComponentHelper(specifier);
        state = new ArrayList<Map<String, Object>>(
                (List<Map<String, Object>>) specifier
                        .getStartingState(specifier.getIdentifier()));
        stateValidator = specifier.getStateValidator();
        onlySendEndStateChanges = (specifier.isSendingEveryChange() == false);

        /*
         * Create a panel in which to place the widgets and a label, if
         * appropriate.
         */
        Composite panel = UiBuilder.buildComposite(parent, 1, SWT.NONE,
                UiBuilder.CompositeType.MULTI_ROW_VERTICALLY_EXPANDING,
                specifier);
        label = UiBuilder.buildLabel(panel, specifier);

        /*
         * Create a table to display the state.
         */
        graph = new Graph(panel, specifier.getMinimumY(),
                specifier.getMaximumY());
        graph.setInsets(10, 10, 10, 5);
        graph.setHatchAndLabelIntervals(specifier.getIntervalHatchX(),
                specifier.getIntervalHatchY(), specifier.getIntervalLabelX(),
                specifier.getIntervalLabelY());
        graph.setLabelSuffixes(specifier.getSuffixLabelX(),
                specifier.getSuffixLabelY());
        graph.setHeightMultiplier(specifier.getHeightMultiplier());
        graph.setIntervalDrawnPointsX(specifier.getDrawnPointsInterval());
        List<Map<String, Double>> colorMaps = specifier.getVerticalColors();
        if (colorMaps.isEmpty() == false) {
            final List<Color> rowColors = new ArrayList<>(colorMaps.size());
            for (Map<String, Double> colorMap : colorMaps) {
                rowColors.add(new Color(Display.getDefault(),
                        getColorComponent(colorMap,
                                ConversionUtilities.COLOR_AS_MAP_RED),
                        getColorComponent(colorMap,
                                ConversionUtilities.COLOR_AS_MAP_GREEN),
                        getColorComponent(colorMap,
                                ConversionUtilities.COLOR_AS_MAP_BLUE)));
            }
            graph.setRowColors(rowColors);
            graph.addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    for (Color color : rowColors) {
                        color.dispose();
                    }
                }
            });
        }
        graph.setEnabled(specifier.isEnabled());
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        graph.setLayoutData(gridData);
        UiBuilder.ensureMouseWheelEventsPassedUpToAncestor(graph);

        /*
         * Set the editability of the megawidget to false if necessary.
         */
        if (isEditable() == false) {
            doSetEditable(false);
        }

        /*
         * Set the graph widget's listener so that state changes will be
         * recorded and notifications to any state change listener sent along.
         */
        graph.addGraphListener(new GraphListener());

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
        doSetEditable(editable);
    }

    @Override
    public final int getLeftDecorationWidth() {
        return 0;
    }

    @Override
    public final void setLeftDecorationWidth(int width) {

        /*
         * No action.
         */
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

    // Protected Methods

    @Override
    protected Object doGetState(String identifier) {
        return getStateCopy();
    }

    @Override
    protected void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        /*
         * Convert the provided state to a valid value, and record it.
         */
        List<Map<String, Object>> newState;
        try {
            newState = stateValidator.convertToStateValue(state);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
        this.state.clear();
        this.state.addAll(newState);

        /*
         * Remember this as the last forwarded state if ongoing state changes
         * are occurring.
         */
        if (lastForwardedState != null) {
            lastForwardedState = getStateCopy();
        }

        /*
         * Synchronize the widgets to the new state.
         */
        synchronizeComponentWidgetsToState();
    }

    @Override
    protected String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {

        /*
         * Ensure the provided state is valid.
         */
        List<Map<String, Object>> list = null;
        try {
            list = stateValidator.convertToStateValue(state);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }

        /*
         * Build up a description of the state and return it.
         */
        StringBuilder description = new StringBuilder();
        for (Map<String, Object> map : list) {
            if (description.length() > 0) {
                description.append("}; ");
            }
            description.append("{");
            boolean firstDone = false;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (firstDone) {
                    description.append("; ");
                } else {
                    firstDone = true;
                }
                description.append(entry.getKey() + ": " + entry.getValue());
            }
        }
        description.append("}");
        return description.toString();
    }

    @Override
    protected void doSynchronizeComponentWidgetsToState() {
        List<PlottedPoint> plottedPoints = new ArrayList<>(state.size());
        for (Map<String, Object> map : state) {
            plottedPoints.add(new PlottedPoint((int) map
                    .get(GraphValidator.PLOT_POINT_X), (int) map
                    .get(GraphValidator.PLOT_POINT_Y), (boolean) map
                    .get(GraphValidator.PLOT_POINT_EDITABLE)));
        }
        graph.setPlottedPoints(plottedPoints);
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        graph.setEnabled(enable);
    }

    // Private Methods

    /**
     * Get the floating-point value (which must be between 0.0 and 1.0
     * inclusive), in the specified map at the specified key, and convert it to
     * a color component between 0 and 255 inclusive.
     * 
     * @param map
     *            Map holding the color component.
     * @param key
     *            Key under which the value is to be found in the map.
     * @return Color component value, between 0 and 255 inclusive.
     */
    private int getColorComponent(Map<String, Double> map, String key) {
        return (int) ((map.get(key) * 255.0) + 0.5);
    }

    /**
     * Get a deep copy of the current state.
     * 
     * @return Copy of the current state.
     */
    private List<Map<String, Object>> getStateCopy() {
        List<Map<String, Object>> copy = new ArrayList<>(state.size());
        for (Map<String, Object> map : state) {
            copy.add(new HashMap<>(map));
        }
        return copy;
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
        graph.getParent().setEnabled(editable);
    }
}
