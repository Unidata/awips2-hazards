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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.ImmutableSet;

/**
 * Detailed combo box megawidget, providing a dropdown combo box allowing the
 * selection of a single choice, and a group-style panel beneath the combo box
 * holding any detail megawidgets associated with the current choice.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 23, 2014    4122    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see DetailedComboBoxSpecifier
 */
public class DetailedComboBoxMegawidget extends SingleBoundedChoiceMegawidget
        implements IControl, IContainer<IControl> {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = new HashSet<>(
                SingleBoundedChoiceMegawidget.MUTABLE_PROPERTY_NAMES_WITHOUT_CHOICES);
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
     * Map of choice identifiers to their detail panels. Any choice that has no
     * detail panel will still have an entry here giving it an empty panel.
     */
    private final Map<String, Composite> choicePanelsForIdentifiers = new HashMap<>();

    /**
     * Details panel, holding whichever choice panel is associated with the
     * currently selected choice.
     */
    private final Composite detailsPanel;

    /**
     * Details layout, the stack layout used to flip between different choice's
     * detail panels.
     */
    private final StackLayout detailsLayout;

    /**
     * All the detail megawidgets for this megawidget.
     */
    private final List<IControl> children;

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
     * @throws MegawidgetException
     *             If a problem occurs while attempting to construct any detail
     *             megawidgets.
     */
    protected DetailedComboBoxMegawidget(DetailedComboBoxSpecifier specifier,
            Composite parent, Map<String, Object> paramMap)
            throws MegawidgetException {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);

        /*
         * Create the composite holding the component widgets. This parent
         * composite uses the form layout in order to position an optional label
         * and a combo box over the top border of a group composite, which in
         * turn holds any detail field megawidgets associated with the currently
         * selected choice.
         */
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new FormLayout());
        GridData mainGridData = new GridData(SWT.FILL, SWT.FILL,
                specifier.isHorizontalExpander(),
                specifier.isVerticalExpander());
        mainGridData.horizontalSpan = specifier.getWidth();
        mainGridData.verticalIndent = specifier.getSpacing();
        panel.setLayoutData(mainGridData);

        /*
         * Create the label if appropriate.
         */
        if ((specifier.getLabel() != null)
                && (specifier.getLabel().length() > 0)) {
            label = new Label(panel, SWT.NONE);
            label.setText(specifier.getLabel());
            label.setEnabled(specifier.isEnabled());
        } else {
            label = null;
        }

        /*
         * Create the combo box, and create bidirectional associations between
         * the choice names and their identifiers before adding the choice names
         * to the combo box.
         */
        Composite comboBoxPanel = new Composite(panel, SWT.NONE);
        GridLayout comboBoxLayout = new GridLayout(1, false);
        comboBoxLayout.marginWidth = comboBoxLayout.marginHeight = 0;
        comboBoxPanel.setLayout(comboBoxLayout);
        comboBoxHelper = new ComboBoxComponentHelper(comboBoxPanel,
                new IComboBoxComponentHolder() {

                    @Override
                    public String getSelection() {
                        return choiceNamesForIdentifiers.get(state);
                    }

                    @Override
                    public void setSelection(String item) {
                        handleSelectionChange(item);
                    }
                }, specifier.isAutocompleteEnabled(), specifier.isEnabled(),
                label, helper);
        List<String> choiceIdentifiers = populateComboBoxWithChoices();
        comboBoxHelper.getComboBox().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));

        /*
         * Create the group that will hold the detail field megawidgets. The
         * group is given a bogus text label because under the target window
         * manager, SWT groups' borders are drawn differently if they have
         * labels (rounded corners) or not (sharp corners), and this group
         * should look like other labeled groups.
         */
        Group group = new Group(panel, SWT.NONE);
        group.setText("-");
        GridLayout groupLayout = new GridLayout(1, false);
        groupLayout.marginWidth = groupLayout.marginHeight = 0;
        group.setLayout(groupLayout);
        group.setEnabled(specifier.isEnabled());

        /*
         * Create the panel within the group that in turn holds the stack layout
         * to allow different detail field panels to be shown, one at a time.
         */
        detailsPanel = new Composite(group, SWT.NONE);
        detailsLayout = new StackLayout();
        detailsPanel.setLayout(detailsLayout);
        detailsPanel
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        /*
         * Determine the sizes of the label (if any) and combo box, and
         * determine the vertical offsets for the label and combo box to ensure
         * that these components are all vertically aligned correctly with
         * respect to one another: the label and combo box lined up to have
         * their text at the same vertical point, and the group's top border
         * running under the vertical center of the label and combo box.
         */
        int labelVerticalOffset = 0, comboBoxVerticalOffset = 0, headerHeight;
        Point labelSize = (label != null ? label.computeSize(SWT.DEFAULT,
                SWT.DEFAULT) : new Point(0, 0));
        Point comboBoxSize = comboBoxHelper.getComboBox().computeSize(
                SWT.DEFAULT, SWT.DEFAULT);
        if (labelSize.y > comboBoxSize.y) {
            comboBoxVerticalOffset = (labelSize.y - comboBoxSize.y) / 2;
            headerHeight = labelSize.y;
        } else {
            labelVerticalOffset = (comboBoxSize.y - labelSize.y) / 2;
            headerHeight = comboBoxSize.y;
        }

        /*
         * Ensure that the group within the panel is vertically offset down far
         * enough to be just below the combo box and label's bottommost area,
         * i.e. so that the latter two do not overlap this panel.
         */
        groupLayout.marginTop = labelVerticalOffset;

        /*
         * If there is a label, configure it to be inset a bit to the right of
         * the leftmost part of the group, and overlaying its border, vertically
         * centered on said border.
         */
        if (label != null) {
            FormData labelFormData = new FormData();
            labelFormData.left = new FormAttachment(0, 5);
            labelFormData.top = new FormAttachment(0, labelVerticalOffset);
            labelFormData.right = new FormAttachment(0, labelSize.x + 5);
            label.setLayoutData(labelFormData);
        }

        /*
         * Configure the combo to be inset a bit to the right of the label (or
         * where the label would be, if there is no label) and overlaying the
         * group's border, vertically centered on said border. If the header is
         * to expand horizontally, make the right side of the combo box align
         * close to the right edge of the grouping.
         */
        FormData comboFormData = new FormData();
        if (label != null) {
            comboFormData.left = new FormAttachment(label, 0);
        } else {
            comboFormData.left = new FormAttachment(0, 5);
        }
        comboFormData.top = new FormAttachment(0, comboBoxVerticalOffset);
        if (specifier.isHeaderHorizontalExpander()) {
            comboFormData.right = new FormAttachment(100, -5);
        } else {
            comboFormData.width = comboBoxSize.x;
        }
        comboFormData.height = comboBoxSize.y;
        comboBoxPanel.setLayoutData(comboFormData);
        // comboBoxHelper.getComboBox().setLayoutData(comboFormData);

        /*
         * Configure the group to take up the whole area of the parent, except
         * for the top, which is vertically offset down to ensure that its
         * border falls under the vertical center of the label and combo box.
         */
        FormData groupFormData = new FormData();
        groupFormData.left = new FormAttachment(0, 0);
        groupFormData.right = new FormAttachment(100, 0);
        groupFormData.top = new FormAttachment(0, labelVerticalOffset);
        groupFormData.bottom = new FormAttachment(100, 0);
        group.setLayoutData(groupFormData);

        /*
         * Iterate through the choices, creating detail megawidget panels for
         * each choice that has associated detail fields. An identity hash map
         * is used to track choice panels that have already been created and
         * their associated specifier lists so that if a specifier list that is
         * a repeat of that of another choice is encountered, the panel created
         * for that other choice is simply reused, instead of recreating the
         * same megawidgets again. While creating the panels, keep track of the
         * largest width and height needed to encompass any of them.
         */
        Map<List<IControlSpecifier>, Composite> choicePanelsForSpecifierLists = new IdentityHashMap<>();
        List<IControl> allChildren = new ArrayList<>();
        Point neededDimensions = new Point(0, 0);
        Composite emptyPanel = new Composite(detailsPanel, SWT.NONE);
        choicePanelsForIdentifiers.put(null, emptyPanel);
        for (String identifier : choiceIdentifiers) {
            List<IControlSpecifier> specifiers = specifier
                    .getDetailFieldsForChoice(identifier);
            if (specifiers == null) {
                choicePanelsForIdentifiers.put(identifier, emptyPanel);
                continue;
            }
            Composite choicePanel = choicePanelsForSpecifierLists
                    .get(specifiers);
            if (choicePanel == null) {
                choicePanel = new Composite(detailsPanel, SWT.NONE);
                List<IControl> children = UiBuilder.createChildMegawidgets(
                        specifier, choicePanel, 1, specifier.isEnabled(),
                        specifier.isEditable(), specifiers, paramMap);
                allChildren.addAll(children);
                Point choicePanelSize = choicePanel.computeSize(SWT.DEFAULT,
                        SWT.DEFAULT);
                if (neededDimensions.x < choicePanelSize.x) {
                    neededDimensions.x = choicePanelSize.x;
                }
                if (neededDimensions.y < choicePanelSize.y) {
                    neededDimensions.y = choicePanelSize.y;
                }
            }
            choicePanelsForSpecifierLists.put(specifiers, choicePanel);
            choicePanelsForIdentifiers.put(identifier, choicePanel);
        }
        this.children = Collections.unmodifiableList(allChildren);

        /*
         * Ensure that the main panel requests a width at least large enough to
         * hold the label and combo box, plus some padding, but also large
         * enough to hold the widest and tallest detail choice panels.
         */
        int groupBorderWidth = group.computeTrim(0, 0, 0, 0).width / 2;
        mainGridData.minimumWidth = mainGridData.widthHint = Math.max(
                (label != null ? labelSize.x + 5 : 0) + 10 + comboBoxSize.x,
                neededDimensions.x + (groupBorderWidth * 2));
        mainGridData.minimumHeight = mainGridData.heightHint = neededDimensions.y
                + groupBorderWidth + headerHeight;

        /*
         * Render the components uneditable if necessary.
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
        return (label != null ? label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x + 5
                : 0);
    }

    @Override
    public void setLeftDecorationWidth(int width) {
        if (label != null) {
            ((FormData) comboBoxHelper.getComboBox().getParent()
                    .getLayoutData()).left = new FormAttachment(0, width + 10);
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
    public List<IControl> getChildren() {
        return children;
    }

    // Protected Methods

    @Override
    protected final boolean isChoicesListMutable() {
        return false;
    }

    @Override
    protected final void prepareForChoicesChange() {
        throwChoicesChangeException();
    }

    @Override
    protected void cancelPreparationForChoicesChange() {
        throwChoicesChangeException();
    }

    @Override
    protected final void synchronizeComponentWidgetsToChoices() {
        throwChoicesChangeException();
    }

    @Override
    protected final void doSynchronizeComponentWidgetsToState() {
        comboBoxHelper.setSelection(choiceNamesForIdentifiers.get(state));
        synchronizeDetailsPanelWithState();
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        comboBoxHelper.setEnabled(enable);
        for (IControl child : children) {
            child.setEnabled(enable);
        }
    }

    // Private Methods

    /**
     * Populate the combo box with choices.
     * 
     * @return List of choice identifiers for the choices available to the combo
     *         box.
     */
    private List<String> populateComboBoxWithChoices() {
        List<?> choices = getStateValidator().getAvailableChoices();
        String[] names = new String[choices.size()];
        List<String> choiceIdentifiers = new ArrayList<>(choices.size());
        DetailedComboBoxSpecifier specifier = getSpecifier();
        int index = 0;
        for (Object choice : choices) {
            String identifier = specifier.getIdentifierOfNode(choice);
            String name = specifier.getNameOfNode(choice);
            choiceNamesForIdentifiers.put(identifier, name);
            choiceIdentifiersForNames.put(name, identifier);
            names[index++] = name;
            choiceIdentifiers.add(identifier);
        }
        comboBoxHelper.getComboBox().setItems(names);
        return choiceIdentifiers;
    }

    /**
     * Handle the combo box's selection having changed to that specified.
     * 
     * @param value
     *            New combo box selection.
     */
    private void handleSelectionChange(String value) {
        state = choiceIdentifiersForNames.get(value);
        synchronizeDetailsPanelWithState();
        notifyListener(getSpecifier().getIdentifier(), state);
    }

    /**
     * Throw an unsupported operation exception indicating that choices cannot
     * be changed.
     * 
     * @throws UnsupportedOperationException
     *             Whenever this method is invoked.
     */
    private void throwChoicesChangeException()
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "cannot change choices for radio buttons megawidget");
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
     * Synchronize the details panel with the current state.
     */
    private void synchronizeDetailsPanelWithState() {
        Composite choicePanel = choicePanelsForIdentifiers.get(state);
        if (detailsLayout.topControl != choicePanel) {
            detailsLayout.topControl = choicePanel;
            detailsPanel.layout();
        }
    }
}