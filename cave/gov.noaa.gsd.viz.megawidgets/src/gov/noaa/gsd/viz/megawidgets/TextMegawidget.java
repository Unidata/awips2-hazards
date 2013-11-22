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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Text megawidget.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Oct 22, 2013   2168     Chris.Golden      Replaced some GUI creation code with
 *                                           calls to UiBuilder methods to avoid
 *                                           code duplication and encourage uni-
 *                                           form look, and changed to implement
 *                                           new IControl interface.
 * Nov 04, 2013   2336     Chris.Golden      Added multi-line option. Also added
 *                                           option of not notifying listeners of
 *                                           state changes caused by ongoing text
 *                                           alerations, instead saving notifi-
 *                                           cations for when the megawidget
 *                                           loses focus. Also changed to use main
 *                                           label as state label if there is only
 *                                           one state identifier and it has no
 *                                           associated state label.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TextSpecifier
 */
public class TextMegawidget extends StatefulMegawidget implements IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = Sets
                .newHashSet(StatefulMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Private Variables

    /**
     * Label associated with this megawidget, if any.
     */
    private final Label label;

    /**
     * Text component associated with this megawidget.
     */
    private final Text text;

    /**
     * Current value.
     */
    private String state = null;

    /**
     * Flag indicating whether state changes that occur as a result of a text
     * entry change without a focus loss or text validation invocation should be
     * forwarded or not.
     */
    private final boolean onlySendEndStateChanges;

    /**
     * Control component helper.
     */
    private final ControlComponentHelper helper;

    /**
     * Last text value that the state change listener knows about.
     */
    private String lastForwardedValue;

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
    protected TextMegawidget(TextSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);

        // Create the composite holding the components, and
        // the label if appropriate.
        boolean multiLine = (specifier.getNumVisibleLines() > 1);
        Composite panel = UiBuilder
                .buildComposite(
                        parent,
                        (multiLine ? 1 : 2),
                        SWT.NONE,
                        (multiLine ? UiBuilder.CompositeType.MULTI_ROW_VERTICALLY_EXPANDING
                                : UiBuilder.CompositeType.SINGLE_ROW),
                        specifier);
        label = UiBuilder.buildLabel(panel, specifier);

        // Create the text component.
        onlySendEndStateChanges = !specifier.isSendingEveryChange();
        text = new Text(panel, SWT.BORDER
                + (multiLine ? SWT.MULTI : SWT.SINGLE)
                + (multiLine ? SWT.WRAP + SWT.V_SCROLL : SWT.NONE));
        int limit = specifier.getMaxTextLength();
        if (limit > 0) {
            text.setTextLimit(limit);
        }
        text.setEnabled(specifier.isEnabled());

        // Place the text component in the grid.
        GridData gridData = new GridData((multiLine
                || specifier.isHorizontalExpander() ? SWT.FILL : SWT.LEFT),
                (multiLine ? SWT.FILL : SWT.CENTER), true, multiLine);
        gridData.horizontalSpan = ((multiLine == false) && (label == null) ? 2
                : 1);
        GC gc = new GC(text);
        FontMetrics fontMetrics = gc.getFontMetrics();
        gridData.widthHint = text.computeSize(
                (specifier.getVisibleTextLength() + 1)
                        * fontMetrics.getAverageCharWidth(), SWT.DEFAULT).x;
        if (multiLine) {
            gridData.heightHint = text.computeSize(SWT.DEFAULT,
                    specifier.getNumVisibleLines() * fontMetrics.getHeight()).y;
        }
        gc.dispose();
        text.setLayoutData(gridData);

        // If only ending state changes are to result
        // in notifications, bind entry field focus
        // loss and default selection (Enter key) to
        // trigger a notification if the value has
        // changed in such a way that the state change
        // listener was not notified.
        if (onlySendEndStateChanges) {
            text.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    notifyListenersOfEndingStateChange();
                }
            });
            text.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    notifyListenersOfEndingStateChange();
                }
            });
        }

        // Bind the text's change event to trigger a
        // change in the record of the state for the
        // widget, and a change in the scale component
        // to match.
        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String value = text.getText();
                state = value;
                notifyListenersOfRapidStateChange();
            }
        });

        // Set the editability of the megawidget to false
        // if necessary.
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
        return (((text.getStyle() & SWT.MULTI) != 0) || (label == null) ? 0
                : helper.getWidestWidgetWidth(label));
    }

    @Override
    public void setLeftDecorationWidth(int width) {
        if (((text.getStyle() & SWT.MULTI) == 0) && (label != null)) {
            helper.setWidgetsWidth(width, label);
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
        if (label != null) {
            label.setEnabled(enable);
        }
        text.setEnabled(enable);
    }

    @Override
    protected final Object doGetState(String identifier) {
        return state;
    }

    @Override
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {
        String value = null;
        try {
            value = (String) state;
        } catch (Exception e) {
            throw new MegawidgetStateException(identifier, getSpecifier()
                    .getType(), state, "must be string");
        }
        if (value == null) {
            value = "";
        }
        this.state = value;
        text.setText(value.toString());
        recordLastNotifiedState();
    }

    @Override
    protected final String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        return (state == null ? null : state.toString());
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
        text.getParent().setEnabled(editable);
        text.setBackground(helper.getBackgroundColor(editable, text, label));
    }

    /**
     * Record the current state as one of which the state change listener is
     * assumed to be aware.
     */
    private void recordLastNotifiedState() {
        lastForwardedValue = state;
    }

    /**
     * Notify the state change and notification listeners of a state change that
     * is part of a set of rapidly-occurring changes if necessary.
     */
    private void notifyListenersOfRapidStateChange() {
        if (onlySendEndStateChanges == false) {
            notifyListeners();
        }
    }

    /**
     * Notify the state change and notification listeners of a state change if
     * the current state is not the same as the last state of which the state
     * change listener is assumed to be aware.
     */
    private void notifyListenersOfEndingStateChange() {
        if (((lastForwardedValue != null) && (lastForwardedValue.equals(state) == false))
                || ((lastForwardedValue == null) && (lastForwardedValue != state))) {
            recordLastNotifiedState();
            notifyListeners();
        }
    }

    /**
     * Notify listeners of a state change.
     */
    private void notifyListeners() {
        notifyListener(getSpecifier().getIdentifier(), state);
        notifyListener();
    }
}
