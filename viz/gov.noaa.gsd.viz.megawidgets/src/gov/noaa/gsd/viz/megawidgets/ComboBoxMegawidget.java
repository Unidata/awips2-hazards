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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
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
 * Jun 24, 2014   4023     Chris.Golden      Changed to prune old state to new
 *                                           choices when available choices are
 *                                           changed.
 * Aug 04, 2014   4122     Chris.Golden      Changed to include autocomplete
 *                                           functionality.
 * Aug 20, 2015   9617     Robert.Blum       Added ability for users to add
 *                                           entries to comboboxes.
 * Aug 28, 2015   9617     Chris.Golden      Added fixes for code added under this
 *                                           ticket.
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
     * Combo box component helper, managing the combo box associated with this
     * megawidget.
     */
    private final ComboBoxComponentHelper comboBoxHelper;

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
        comboBoxHelper = new ComboBoxComponentHelper(panel,
                new IComboBoxComponentHolder() {

                    @Override
                    public String getSelection() {
                        return choiceNamesForIdentifiers.get(state);
                    }

                    @Override
                    public void setSelection(String item) {
                        handleSelectionChange(item);
                    }
                }, specifier.isAutocompleteEnabled(),
                specifier.isAllowNewChoiceEnabled(), specifier.isEnabled(),
                label, helper);
        populateComboBoxWithChoices();

        /*
         * Place the combo box in the grid.
         */
        GridData gridData = new GridData((expandHorizontally ? SWT.FILL
                : SWT.LEFT), SWT.CENTER, true, false);
        if (expandHorizontally) {
            gridData.minimumWidth = 0;
        }
        gridData.horizontalSpan = (label == null ? 2 : 1);
        comboBoxHelper.getComboBox().setLayoutData(gridData);

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
     * Set the choices to those specified. The current state will be pruned of
     * any elements that are not found within the new choices.
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
        comboBoxHelper.setSelection(choiceNamesForIdentifiers.get(state));
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        comboBoxHelper.setEnabled(enable);
    }

    // Private Methods

    /**
     * Handle the combo box's selection having changed to that specified.
     * 
     * @param value
     *            New combo box selection.
     */
    private void handleSelectionChange(String value) {

        /*
         * If allowing new choices, see if a new entry was added, and if so,
         * generate an identifier for it and notify the state validator.
         */
        if (((ComboBoxSpecifier) getSpecifier()).isAllowNewChoiceEnabled()) {
            if (choiceIdentifiersForNames.keySet().contains(value) == false) {
                String identifier = getUniqueIdentifierForNewName(value,
                        choiceIdentifiersForNames.values());
                choiceIdentifiersForNames.put(value, identifier);
                choiceNamesForIdentifiers.put(identifier, value);
                try {
                    getStateValidator()
                            .setAvailableChoices(
                                    new ArrayList<>(choiceIdentifiersForNames
                                            .values()));
                } catch (MegawidgetPropertyException e) {
                    throw new IllegalStateException(
                            "adding new choice caused internal error", e);
                }
            }
        }

        /*
         * Set the state to the new selection and notify the listener.
         */
        state = choiceIdentifiersForNames.get(value);
        notifyListener(getSpecifier().getIdentifier(), state);
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
        comboBoxHelper.setEditable(editable);
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
        comboBoxHelper.getComboBox().setItems(names);
    }
}