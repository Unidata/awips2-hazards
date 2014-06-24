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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.ImmutableSet;

/**
 * Combo box megawidget, providing a dropdown combo box allowing the selection
 * of a single choice.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 22, 2013   2168     Chris.Golden      Replaced some GUI creation code with
 *                                           calls to UiBuilder methods to avoid
 *                                           code duplication and encourage uni-
 *                                           form look. Also changed to implement
 *                                           new IControl interface.
 * Oct 31, 2013   2336     Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * May 18, 2014   2925     Chris.Golden      Fixed bug with coloring when changing
 *                                           megawidget from read-only to editable.
 * Jun 24, 2014   4010     Chris.Golden      Changed to no longer be a subclass
 *                                           of NotifierMegawidget.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see ComboBoxSpecifier
 */
public class ComboBoxMegawidget extends SingleBoundedChoiceMegawidget implements
        IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = new HashSet<>(
                SingleBoundedChoiceMegawidget.MUTABLE_PROPERTY_NAMES_INCLUDING_CHOICES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Private Variables

    /**
     * Label associated with this megawidget, if any.
     */
    private final Label label;

    /**
     * Combo box associated with this megawidget.
     */
    private final Combo comboBox;

    /**
     * Map of choice identifiers to their names.
     */
    private final Map<String, String> choiceNamesForIdentifiers = new HashMap<>();

    /**
     * Map of choice names to their identifiers.
     */
    private final Map<String, String> choiceIdentifiersForNames = new HashMap<>();

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
    protected ComboBoxMegawidget(ComboBoxSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);

        /*
         * Create the composite holding the components, and the label if
         * appropriate.
         */
        Composite panel = UiBuilder.buildComposite(parent, 2, SWT.NONE,
                UiBuilder.CompositeType.SINGLE_ROW, specifier);
        boolean expandHorizontally = specifier.isHorizontalExpander();
        ((GridData) panel.getLayoutData()).horizontalAlignment = (expandHorizontally ? SWT.FILL
                : SWT.LEFT);
        ((GridData) panel.getLayoutData()).grabExcessHorizontalSpace = expandHorizontally;
        label = UiBuilder.buildLabel(panel, specifier);

        /*
         * Create the combo box.
         */
        comboBox = new Combo(panel, SWT.READ_ONLY);
        populateComboBoxWithChoices();
        comboBox.setEnabled(specifier.isEnabled());

        /*
         * Place the combo box in the grid.
         */
        GridData gridData = new GridData((expandHorizontally ? SWT.FILL
                : SWT.LEFT), SWT.CENTER, true, false);
        if (expandHorizontally) {
            gridData.minimumWidth = 0;
        }
        gridData.horizontalSpan = (label == null ? 2 : 1);
        comboBox.setLayoutData(gridData);

        /*
         * Bind the combo box selection event to trigger a change in the record
         * of the state for the widget.
         */
        comboBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Combo comboBox = (Combo) e.widget;
                int index = comboBox.getSelectionIndex();
                if (index == -1) {
                    state = null;
                } else {
                    state = choiceIdentifiersForNames.get(comboBox
                            .getItem(index));
                }
                notifyListener(getSpecifier().getIdentifier(), state);
            }
        });

        /*
         * Render the combo box uneditable if necessary.
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

        /*
         * No action.
         */
    }

    /**
     * Set the choices to those specified. If the current state is not a subset
     * of the new choices, the state will be set to <code>null</code>.
     * 
     * @param value
     *            List of new choices.
     * @throws MegawidgetPropertyException
     *             If the choices are invalid.
     */
    public final void setChoices(Object value)
            throws MegawidgetPropertyException {
        doSetChoices(value);
    }

    // Protected Methods

    @Override
    protected final boolean isChoicesListMutable() {
        return true;
    }

    @Override
    protected final void prepareForChoicesChange() {

        /*
         * No action.
         */
    }

    @Override
    protected void cancelPreparationForChoicesChange() {

        /*
         * No action.
         */
    }

    @Override
    protected final void synchronizeComponentWidgetsToChoices() {

        /*
         * Populate the combo box with the new choices.
         */
        populateComboBoxWithChoices();

        /*
         * Ensure that the combo box has the right element selected.
         */
        synchronizeComponentWidgetsToState();
    }

    @Override
    protected final void doSynchronizeComponentWidgetsToState() {
        String selected = choiceNamesForIdentifiers.get(state);
        if (selected != null) {
            for (int j = 0; j < comboBox.getItemCount(); j++) {
                if (comboBox.getItem(j).equals(selected)) {
                    comboBox.select(j);
                    return;
                }
            }
        }
        comboBox.deselectAll();
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        comboBox.setEnabled(enable);
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
        comboBox.getParent().setEnabled(editable);
        comboBox.setBackground(editable ? null : helper.getBackgroundColor(
                editable, comboBox, label));
    }

    /**
     * Populate the combo box with choices.
     */
    private void populateComboBoxWithChoices() {

        /*
         * Create bidirectional associations between the choice names and their
         * identifiers, and compile a list of the names.
         */
        choiceNamesForIdentifiers.clear();
        choiceIdentifiersForNames.clear();
        ComboBoxSpecifier specifier = getSpecifier();
        List<?> choices = getStateValidator().getAvailableChoices();
        String[] names = new String[choices.size()];
        int index = 0;
        for (Object choice : choices) {
            String identifier = specifier.getIdentifierOfNode(choice);
            String name = specifier.getNameOfNode(choice);
            choiceNamesForIdentifiers.put(identifier, name);
            choiceIdentifiersForNames.put(name, identifier);
            names[index++] = name;
        }

        /*
         * Set the combo box's choices to the list of names compiled above.
         */
        comboBox.setItems(names);
    }
}