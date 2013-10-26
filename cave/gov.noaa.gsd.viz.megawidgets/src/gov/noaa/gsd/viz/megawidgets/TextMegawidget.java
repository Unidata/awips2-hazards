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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
    protected TextMegawidget(TextSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);

        // Create the composite holding the components, and
        // the label if appropriate.
        Composite panel = UiBuilder.buildComposite(parent, 2, SWT.NONE,
                UiBuilder.CompositeType.SINGLE_ROW, specifier);
        label = UiBuilder.buildLabel(panel, specifier);

        // Create the text component.
        text = new Text(panel, SWT.BORDER);
        text.setTextLimit(specifier.getMaxTextLength());
        text.setEnabled(specifier.isEnabled());

        // Place the text component in the grid.
        GridData gridData = new GridData(
                (specifier.isHorizontalExpander() ? SWT.FILL : SWT.LEFT),
                SWT.CENTER, true, false);
        gridData.horizontalSpan = (label == null ? 2 : 1);
        GC gc = new GC(text);
        FontMetrics fontMetrics = gc.getFontMetrics();
        gridData.widthHint = text.computeSize(
                (specifier.getVisibleTextLength() + 1)
                        * fontMetrics.getAverageCharWidth(), SWT.DEFAULT).x;
        gc.dispose();
        text.setLayoutData(gridData);

        // Bind the text's change event to trigger a
        // change in the record of the state for the
        // widget, and a change in the scale component
        // to match.
        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String value = text.getText();
                state = value;
                notifyListener(getSpecifier().getIdentifier(), state);
                notifyListener();
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
        return (label == null ? 0 : helper.getWidestWidgetWidth(label));
    }

    @Override
    public void setLeftDecorationWidth(int width) {
        if (label != null) {
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
}
