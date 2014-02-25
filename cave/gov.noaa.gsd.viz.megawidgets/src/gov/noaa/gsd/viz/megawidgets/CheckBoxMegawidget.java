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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Checkbox megawidget, providing a single checkbox.
 * <p>
 * If multiple checkboxes are desired, grouped together under a label, the
 * {@link CheckBoxesMegawidget} may be more appropriate. However, this
 * megawidget allows the setting of a single option to a boolean value, unlike
 * the <code>CheckBoxesMegawidget</code>, which holds a list of selected choices
 * as its state.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 13, 2014    2161    Chris.Golden      Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see CheckBoxSpecifier
 */
public class CheckBoxMegawidget extends StatefulMegawidget implements IControl {

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
     * Checkbox itself.
     */
    private final Button checkBox;

    /**
     * Current value.
     */
    private Boolean state = null;

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
    protected CheckBoxMegawidget(CheckBoxSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);

        // Create the composite holding the checkbox.
        Composite panel = UiBuilder.buildComposite(parent, 1, SWT.NONE,
                UiBuilder.CompositeType.SINGLE_ROW, specifier);

        // Create the checkbox component.
        checkBox = new Button(panel, SWT.CHECK);
        checkBox.setText(specifier.getLabel() == null ? "" : specifier
                .getLabel());
        checkBox.setEnabled(specifier.isEnabled());
        checkBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button checkBox = (Button) e.widget;
                state = checkBox.getSelection();
                notifyListener(getSpecifier().getIdentifier(), state);
                notifyListener();
            }
        });
        GridData buttonGridData = new GridData(SWT.LEFT, SWT.CENTER, false,
                false);
        checkBox.setLayoutData(buttonGridData);

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
        return 0;
    }

    @Override
    public void setLeftDecorationWidth(int width) {

        // No action.
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
        checkBox.setEnabled(enable);
    }

    @Override
    protected final Object doGetState(String identifier) {
        return state;
    }

    @Override
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {
        Boolean value = null;
        try {
            value = (Boolean) state;
        } catch (Exception e) {
            throw new MegawidgetStateException(identifier, getSpecifier()
                    .getType(), state, "must be boolean");
        }
        if (value == null) {
            value = Boolean.FALSE;
        }
        this.state = value;
        checkBox.setSelection(value);
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
        checkBox.getParent().setEnabled(editable);
        checkBox.setBackground(helper.getBackgroundColor(editable, checkBox,
                null));
    }
}
