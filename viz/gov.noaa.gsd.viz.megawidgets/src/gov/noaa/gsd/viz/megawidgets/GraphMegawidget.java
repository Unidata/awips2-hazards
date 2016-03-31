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
import gov.noaa.gsd.viz.widgets.PlottedPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * Description: Graph megawidget.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 31, 2016   15931    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class GraphMegawidget extends StatefulMegawidget implements IControl {

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
     * State validator.
     */
    private final GraphValidator stateValidator;

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
        state = (List<Map<String, Object>>) specifier
                .getStartingState(specifier.getIdentifier());
        stateValidator = specifier.getStateValidator();

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
            graph.SetRowColors(rowColors);
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
         * Synchronize user-facing widgets to the starting state.
         */
        synchronizeComponentWidgetsToState();
    }

    // Public Methods

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
        List<Map<String, Object>> copy = new ArrayList<>(state.size());
        for (Map<String, Object> map : state) {
            copy.add(new HashMap<>(map));
        }
        return copy;
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
