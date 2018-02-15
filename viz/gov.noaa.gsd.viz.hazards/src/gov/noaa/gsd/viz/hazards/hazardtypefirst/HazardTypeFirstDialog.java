/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.hazardtypefirst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.gsd.viz.hazards.hazardtypefirst.HazardTypeFirstPresenter.Command;
import gov.noaa.gsd.viz.hazards.ui.BasicDialog;
import gov.noaa.gsd.viz.megawidgets.ComboBoxMegawidget;
import gov.noaa.gsd.viz.megawidgets.ComboBoxSpecifier;
import gov.noaa.gsd.viz.megawidgets.ControlComponentHelper;
import gov.noaa.gsd.viz.megawidgets.IControl;
import gov.noaa.gsd.viz.megawidgets.ISingleLineSpecifier;
import gov.noaa.gsd.viz.megawidgets.ISpecifier;
import gov.noaa.gsd.viz.megawidgets.IStateChangeListener;
import gov.noaa.gsd.viz.megawidgets.IStateful;
import gov.noaa.gsd.viz.megawidgets.LabelMegawidget;
import gov.noaa.gsd.viz.megawidgets.LabelSpecifier;
import gov.noaa.gsd.viz.mvp.widgets.IChoiceStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;

/**
 * Description: Hazard type first dialog, allowing the user to choose a hazard
 * type for which to create an event.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 21, 2015    3626    Chris.Golden Initial creation.
 * Aug 12, 2015    4123    Chris.Golden Changed to work with new megawidget manager
 *                                      listener.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardTypeFirstDialog extends BasicDialog
        implements IHazardTypeFirstView {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardTypeFirstDialog.class);

    /**
     * Dialog title.
     */
    private static final String DIALOG_TITLE = "Create Hazard";

    /**
     * Type of the label megawidget.
     */
    private static final String LABEL_MEGAWIDGET_TYPE = "Label";

    /**
     * Type of the combo box megawidget.
     */
    private static final String COMBOBOX_MEGAWIDGET_TYPE = "ComboBox";

    /**
     * Description label megawidget identifier.
     */
    private static final String DESCRIPTION_IDENTIFIER = "description";

    /**
     * Category combo box megawidget identifier.
     */
    private static final String CATEGORY_IDENTIFIER = "category";

    /**
     * Type combo box megawidget identifier.
     */
    private static final String TYPE_IDENTIFIER = "type";

    /**
     * Description label text.
     */
    private static final String DESCRIPTION_TEXT = "Choose the type of the hazard event to be created.\n\n"
            + "Once a type is chosen, the appropriate recommender will be run in order to generate the hazard event.\n";

    /**
     * Description label preferred width in characters.
     */
    private static final int DESCRIPTION_PREFERRED_WIDTH = 100;

    /**
     * Hazard category text.
     */
    private static final String HAZARD_CATEGORY_TEXT = "Category:";

    /**
     * Hazard category text.
     */
    private static final String HAZARD_TYPE_TEXT = "Type:";

    /**
     * Specifier parameters for the description label megawidget.
     */
    private static final ImmutableMap<String, Object> DESCRIPTION_SPECIFIER_PARAMETERS;
    static {
        Map<String, Object> map = new HashMap<>();
        map.put(ISpecifier.MEGAWIDGET_IDENTIFIER, DESCRIPTION_IDENTIFIER);
        map.put(ISpecifier.MEGAWIDGET_TYPE, LABEL_MEGAWIDGET_TYPE);
        map.put(ISpecifier.MEGAWIDGET_LABEL, DESCRIPTION_TEXT);
        map.put(LabelSpecifier.LABEL_PREFERRED_WIDTH,
                DESCRIPTION_PREFERRED_WIDTH);
        map.put(LabelSpecifier.LABEL_WRAP, true);
        DESCRIPTION_SPECIFIER_PARAMETERS = ImmutableMap.copyOf(map);
    }

    /**
     * Specifier parameters for the category combo box megawidget.
     */
    private static final ImmutableMap<String, Object> CATEGORY_SPECIFIER_PARAMETERS;
    static {
        Map<String, Object> map = new HashMap<>();
        map.put(ISpecifier.MEGAWIDGET_IDENTIFIER, CATEGORY_IDENTIFIER);
        map.put(ISpecifier.MEGAWIDGET_TYPE, COMBOBOX_MEGAWIDGET_TYPE);
        map.put(ISpecifier.MEGAWIDGET_LABEL, HAZARD_CATEGORY_TEXT);
        map.put(ISingleLineSpecifier.EXPAND_HORIZONTALLY, true);
        map.put(ComboBoxSpecifier.MEGAWIDGET_VALUE_CHOICES,
                Lists.newArrayList(""));
        CATEGORY_SPECIFIER_PARAMETERS = ImmutableMap.copyOf(map);
    }

    /**
     * Specifier parameters for the type combo box megawidget.
     */
    private static final ImmutableMap<String, Object> TYPE_SPECIFIER_PARAMETERS;
    static {
        Map<String, Object> map = new HashMap<>();
        map.put(ISpecifier.MEGAWIDGET_IDENTIFIER, TYPE_IDENTIFIER);
        map.put(ISpecifier.MEGAWIDGET_TYPE, COMBOBOX_MEGAWIDGET_TYPE);
        map.put(ISpecifier.MEGAWIDGET_LABEL, HAZARD_TYPE_TEXT);
        map.put(ISingleLineSpecifier.EXPAND_HORIZONTALLY, true);
        map.put(ComboBoxSpecifier.MEGAWIDGET_VALUE_CHOICES,
                Lists.newArrayList(""));
        TYPE_SPECIFIER_PARAMETERS = ImmutableMap.copyOf(map);
    }

    /**
     * OK button text.
     */
    private static final String OK_BUTTON_TEXT = "Continue";

    // Private Variables

    /**
     * Megawidget state change listener, used to receive notifications
     * concerning categories and types changing from combo box megawidgets and
     * pass on the changes to the proper state change handler.
     */
    private final IStateChangeListener stateChangeListener = new IStateChangeListener() {

        @Override
        public void megawidgetStateChanged(IStateful megawidget,
                String identifier, Object state) {
            if (identifier.equals(CATEGORY_IDENTIFIER)) {
                if (categoryChangeHandler != null) {
                    categoryChangeHandler.stateChanged(null, (String) state);
                }
            } else if (identifier.equals(TYPE_IDENTIFIER)) {
                if (typeChangeHandler != null) {
                    typeChangeHandler.stateChanged(null, (String) state);
                }
            }
        }

        @Override
        public void megawidgetStatesChanged(IStateful megawidget,
                Map<String, ?> statesForIdentifiers) {
            throw new UnsupportedOperationException(
                    "cannot change multiple states simultaneously");
        }
    };

    /**
     * Megawidget creation time parameter map.
     */
    private final Map<String, Object> megawidgetCreationTimeParams = new HashMap<>(
            1, 1.0f);

    /**
     * Topmost container.
     */
    private Composite top;

    /**
     * Description label megawidget.
     */
    @SuppressWarnings("unused")
    private LabelMegawidget descriptionMegawidget;

    /**
     * Category combo box megawidget.
     */
    private ComboBoxMegawidget categoryMegawidget;

    /**
     * Type combo box megawidget.
     */
    private ComboBoxMegawidget typeMegawidget;

    /**
     * OK button.
     */
    private Button okButton;

    /**
     * Category combo box state change handler. The identifier is ignored.
     */
    private IStateChangeHandler<Object, String> categoryChangeHandler;

    /**
     * Category combo box state changer. The identifier is ignored.
     */
    private final IChoiceStateChanger<Object, String, String, String> categoryChanger = new IChoiceStateChanger<Object, String, String, String>() {

        @Override
        public void setEnabled(Object identifier, boolean enable) {
            if (isAlive()) {
                categoryMegawidget.setEnabled(enable);
            }
        }

        @Override
        public void setEditable(Object identifier, boolean editable) {
            if (isAlive()) {
                categoryMegawidget.setEditable(editable);
            }
        }

        @Override
        public void setChoices(Object identifier, List<String> choices,
                List<String> choiceDisplayables, String value) {
            setCategories(choices);
            setSelectedCategory(value);
        }

        @Override
        public String getState(Object identifier) {
            return getSelectedCategory();
        }

        @Override
        public void setState(Object identifier, String value) {
            setSelectedCategory(value);
        }

        @Override
        public void setStates(Map<Object, String> valuesForIdentifiers) {
            throw new UnsupportedOperationException(
                    "cannot change multiple states for category");
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<Object, String> handler) {
            categoryChangeHandler = handler;
        }
    };

    /**
     * Type combo box state change handler. The identifier is ignored.
     */
    private IStateChangeHandler<Object, String> typeChangeHandler;

    /**
     * Type combo box state changer. The identifier is ignored.
     */
    private final IChoiceStateChanger<Object, String, String, String> typeChanger = new IChoiceStateChanger<Object, String, String, String>() {

        @Override
        public void setEnabled(Object identifier, boolean enable) {
            if (isAlive()) {
                typeMegawidget.setEnabled(enable);
            }
        }

        @Override
        public void setEditable(Object identifier, boolean editable) {
            if (isAlive()) {
                typeMegawidget.setEditable(editable);
            }
        }

        @Override
        public void setChoices(Object identifier, List<String> choices,
                List<String> choiceDisplayables, String value) {
            setTypes(choices, choiceDisplayables);
            setSelectedType(value);
        }

        @Override
        public String getState(Object identifier) {
            return getSelectedType();
        }

        @Override
        public void setState(Object identifier, String value) {
            setSelectedType(value);
        }

        @Override
        public void setStates(Map<Object, String> valuesForIdentifiers) {
            throw new UnsupportedOperationException(
                    "cannot change multiple states for type");
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<Object, String> handler) {
            typeChangeHandler = handler;
        }
    };

    /**
     * Button invocation handler, for the buttons at the bottom of the dialog.
     */
    private ICommandInvocationHandler<Command> buttonInvocationHandler;

    /**
     * Button invoker, for the buttons at the bottom of the dialog.
     */
    private final ICommandInvoker<Command> buttonInvoker = new ICommandInvoker<Command>() {

        @Override
        public void setEnabled(Command identifier, boolean enable) {
            if ((identifier == Command.OK) && (okButton != null)
                    && (okButton.isDisposed() == false)) {
                okButton.setEnabled(enable);
            }
        }

        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler<Command> handler) {
            buttonInvocationHandler = handler;
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parent
     *            Parent shell for this dialog.
     */
    public HazardTypeFirstDialog(Shell parent) {
        super(parent);
        setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        setBlockOnOpen(false);
        megawidgetCreationTimeParams.put(IStateful.STATE_CHANGE_LISTENER,
                stateChangeListener);
    }

    // Public Methods

    @Override
    public IChoiceStateChanger<Object, String, String, String> getCategoryChanger() {
        return categoryChanger;
    }

    @Override
    public IChoiceStateChanger<Object, String, String, String> getTypeChanger() {
        return typeChanger;
    }

    @Override
    public ICommandInvoker<Command> getCommandInvoker() {
        return buttonInvoker;
    }

    // Protected Methods

    @Override
    protected int getDialogBoundsStrategy() {
        return DIALOG_PERSISTLOCATION;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(DIALOG_TITLE);
    }

    @Override
    protected void handleShellCloseEvent() {
        buttonPressed(IDialogConstants.CANCEL_ID);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        top = (Composite) super.createDialogArea(parent);
        top.setLayout(new GridLayout(1, false));

        /*
         * Create the description label megawidget, and the category and type
         * combo box megawidgets.
         */
        List<IControl> megawidgetsToAlign = new ArrayList<>();
        try {
            descriptionMegawidget = new LabelSpecifier(
                    DESCRIPTION_SPECIFIER_PARAMETERS).createMegawidget(top,
                            LabelMegawidget.class,
                            megawidgetCreationTimeParams);
            categoryMegawidget = new ComboBoxSpecifier(
                    CATEGORY_SPECIFIER_PARAMETERS).createMegawidget(top,
                            ComboBoxMegawidget.class,
                            megawidgetCreationTimeParams);
            megawidgetsToAlign.add(categoryMegawidget);
            typeMegawidget = new ComboBoxSpecifier(TYPE_SPECIFIER_PARAMETERS)
                    .createMegawidget(top, ComboBoxMegawidget.class,
                            megawidgetCreationTimeParams);
            megawidgetsToAlign.add(typeMegawidget);
        } catch (Exception e) {
            statusHandler.error("unexpected problem creating megawidgets", e);
        }

        /*
         * Align the created megawidgets' labels to one another.
         */
        ControlComponentHelper.alignMegawidgetsElements(megawidgetsToAlign);
        top.layout();

        return top;
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Control result = super.createButtonBar(parent);
        okButton = getButton(IDialogConstants.OK_ID);
        okButton.setText(OK_BUTTON_TEXT);
        return result;
    }

    @Override
    protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);
        if (buttonInvocationHandler != null) {
            buttonInvocationHandler
                    .commandInvoked(buttonId == IDialogConstants.OK_ID
                            ? Command.OK : Command.CANCEL);
        }
    }

    /**
     * Respond to the OK button being pressed. This implementation does nothing,
     * since the dialog is closed by the controlling view if needed. (The button
     * press still sends a notification of its invocation.)
     */
    @Override
    protected void okPressed() {

        /*
         * No action.
         */
    }

    @Override
    protected Point getInitialSize() {

        /*
         * TODO: Here and in many other places, we use hard-coded pixel values
         * to specify window sizes. It'd be good to redo these eventually to use
         * font-size-relative measurements and convert them into pixel values.
         */
        return new Point(450, 250);
    }

    // Private Methods

    /**
     * Determine whether or not the dialog is currently alive, meaning that it
     * has at least some non-disposed widgets.
     * 
     * @return True if widgets exist, false otherwise.
     */
    private boolean isAlive() {
        return ((top != null) && (top.isDisposed() == false));
    }

    /**
     * Get the selected category.
     * 
     * @return Selected category.
     */
    private String getSelectedCategory() {
        return getComboBoxSelectedChoice(categoryMegawidget,
                CATEGORY_IDENTIFIER);
    }

    /**
     * Get the selected type.
     * 
     * @return Selected type.
     */
    private String getSelectedType() {
        return getComboBoxSelectedChoice(typeMegawidget, TYPE_IDENTIFIER);
    }

    /**
     * Get the specified combo box's selected choice.
     * 
     * @param comboBox
     *            Combo box from which to get the selected choice.
     * @param identifier
     *            Identifier of the state the combo box holds.
     * @return Selected choice.
     */
    private String getComboBoxSelectedChoice(ComboBoxMegawidget comboBox,
            String identifier) {
        if (comboBox != null) {
            try {
                return (String) comboBox.getState(identifier);
            } catch (Exception e) {
                statusHandler.error("unexpected error while getting "
                        + identifier + " megawidget state", e);
            }
        }
        return null;
    }

    /**
     * Set the selected category.
     * 
     * @param category
     *            New category to be used.
     */
    private void setSelectedCategory(String category) {
        setComboBoxSelectedChoice(categoryMegawidget, category,
                CATEGORY_IDENTIFIER);
    }

    /**
     * Set the selected type.
     * 
     * @param type
     *            New type to be used.
     */
    private void setSelectedType(String type) {
        setComboBoxSelectedChoice(typeMegawidget, type, TYPE_IDENTIFIER);
    }

    /**
     * Set the specified combo box's selected choice.
     * 
     * @param comboBox
     *            Combo box to have its choice set.
     * @param choice
     *            Choice for the combo box.
     * @param identifier
     *            Identifier of the state the combo box holds.
     */
    private void setComboBoxSelectedChoice(ComboBoxMegawidget comboBox,
            String choice, String identifier) {
        if (isAlive() && (comboBox != null)) {
            try {
                comboBox.setState(identifier, choice);
            } catch (Exception e) {
                statusHandler.error("unexpected error while setting "
                        + identifier + " megawidget state", e);
            }
        }
    }

    /**
     * Set the category combo box choices.
     * 
     * @param categories
     *            List of categories to be used.
     */
    private void setCategories(List<String> categories) {
        setComboBoxChoices(categoryMegawidget, categories, null,
                CATEGORY_IDENTIFIER);
    }

    /**
     * Set the type combo box choices.
     * 
     * @param types
     *            List of types to be used.
     * @param descriptions
     *            Descriptions for the <code>types</code>; each element in this
     *            list is the description for the element at the corresponding
     *            index in <code>types</code>.
     */
    private void setTypes(List<String> types, List<String> descriptions) {
        setComboBoxChoices(typeMegawidget, types, descriptions, "type");
    }

    /**
     * Set the specified combo box's choices.
     * 
     * @param comboBox
     *            Combo box to have its choices set.
     * @param choices
     *            Choice identifiers for the combo box.
     * @param descriptions
     *            Descriptions for the <code>choices</code>; each element in
     *            this list is the description for the element at the
     *            corresponding index in <code>choices</code>. If
     *            <code>null</code>, the <code>choices</code> list is used for
     *            both identifiers and displayables.
     * @param identifier
     *            Identifier of the state the combo box holds.
     */
    private void setComboBoxChoices(ComboBoxMegawidget comboBox,
            List<String> choices, List<String> descriptions,
            String identifier) {
        if (isAlive() && (comboBox != null) && (choices != null)) {
            List<?> choicesList;
            if (descriptions != null) {
                List<Map<String, Object>> list = new ArrayList<>(
                        choices.size());
                for (int j = 0; j < choices.size(); j++) {
                    Map<String, Object> map = new HashMap<>(2);
                    map.put(ComboBoxSpecifier.CHOICE_IDENTIFIER,
                            choices.get(j));
                    map.put(ComboBoxSpecifier.CHOICE_NAME, descriptions.get(j));
                    list.add(map);
                }
                choicesList = list;
            } else {
                choicesList = choices;
            }
            try {
                comboBox.setChoices(choicesList);
            } catch (Exception e) {
                statusHandler.error("unexpected error while setting "
                        + identifier + " megawidget choices", e);
            }
        }
    }
}
