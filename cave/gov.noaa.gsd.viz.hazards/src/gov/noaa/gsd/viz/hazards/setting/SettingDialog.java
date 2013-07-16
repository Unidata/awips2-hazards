/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.setting;

import gov.noaa.gsd.viz.hazards.dialogs.BasicDialog;
import gov.noaa.gsd.viz.hazards.display.action.SettingsAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Setting dialog, a dialog allowing the user to create or modify settings.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden    Initial induction into repo
 * Jul 18, 2013    585     Chris Golden    Changed to support loading from bundle.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class SettingDialog extends BasicDialog {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SettingDialog.class);

    /**
     * Dialog title prefix.
     */
    private static final String DIALOG_TITLE_PREFIX = "Settings: ";

    /**
     * Key into a setting dictionary where the displayable name of the setting
     * is found as a value.
     */
    private static final String DISPLAY_NAME = "displayName";

    /**
     * File menu text.
     */
    private static final String FILE_MENU_TEXT = "&File";

    /**
     * Edit menu text.
     */
    private static final String EDIT_MENU_TEXT = "&Edit";

    /**
     * File menu item names.
     */
    private static final List<String> FILE_MENU_ITEM_NAMES = Collections
            .unmodifiableList(Lists.newArrayList("&New", "&Save", "Save &As",
                    "&Delete", null, "&Close"));;

    /**
     * Edit menu item names.
     */
    private static final List<String> EDIT_MENU_ITEM_NAMES = Collections
            .unmodifiableList(Lists.newArrayList("&Revert"));

    // Private Constants

    /**
     * File menu item actions; each is either a <code>SettingsAction</code>,
     * meaning it is forwarded to the presenter; a <code>String</code>, meaning
     * it is handled internally; a <code>Runnable</code>, which is is used to
     * implement non-standard behavior; or <code>null</code> for any menu item
     * that has no effect.
     */
    private final List<?> FILE_MENU_ITEM_ACTIONS = Collections
            .unmodifiableList(Lists.newArrayList(
                    new SettingsAction("New", null), new Runnable() {
                        @Override
                        public void run() {
                            fireAction(new SettingsAction("Save", getState()));
                        }
                    }, new Runnable() {
                        private final IInputValidator validator = new IInputValidator() {
                            @Override
                            public String isValid(String text) {
                                return (text.length() < 1 ? "The name must contain at least one character."
                                        : null);
                            }
                        };

                        @Override
                        public void run() {
                            InputDialog inputDialog = new InputDialog(
                                    getShell(), "Hazard Services",
                                    "Enter the new setting name: ", "",
                                    validator);
                            if (inputDialog.open() == InputDialog.OK) {
                                values.put(DISPLAY_NAME, inputDialog.getValue());
                                setDialogName(getShell());
                                fireAction(new SettingsAction("Save As",
                                        getState()));
                            }
                        }
                    }, new SettingsAction("Dialog", "Delete"), null, "Close"));

    /**
     * Edit menu item actions; each is either a <code>SettingsAction</code>,
     * meaning it is forwarded to the presenter; a <code>String</code>, meaning
     * it is handled internally; a <code>Runnable</code>, which is is used to
     * implement non-standard behavior; or <code>null</code> for any menu item
     * that has no effect.
     */
    private final List<?> EDIT_MENU_ITEM_ACTIONS = Collections
            .unmodifiableList(Lists.newArrayList(new SettingsAction("Revert",
                    null)));

    // Private Variables

    /**
     * Presenter.
     */
    private final SettingsPresenter presenter;

    /**
     * Fields dictionary, used to hold the dialog's fields.
     */
    private final DictList fields;

    /**
     * Values dictionary, used to hold the dialog's widgets' values.
     */
    private Dict values;

    /**
     * Megawidget manager.
     */
    private MegawidgetManager megawidgetManager;

    /**
     * Selection listener for the menu bar's items.
     */
    private final SelectionListener menuBarListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            Object action = ((MenuItem) e.widget).getData();
            if (action instanceof SettingsAction) {
                fireAction((SettingsAction) action);
            } else if (action instanceof String) {
                handleMenuItemInvocationInternally((String) action);
            } else if (action instanceof Runnable) {
                ((Runnable) action).run();
            }
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param presenter
     *            Presenter.
     * @param parent
     *            Parent shell.
     * @param fields
     *            List of dictionaries, each of the latter holding the
     *            parameters for the fields making up the dialog. Within the set
     *            of all fields that are defined by these parameters, all the
     *            fields (widget specifiers) must have unique identifiers.
     * @param values
     *            Values for each of the fields defined in <code>fields</code>
     *            as a dictionary, one entry for each field identifier.
     */
    public SettingDialog(SettingsPresenter presenter, Shell parent,
            DictList fields, Dict values) {
        super(parent);
        this.presenter = presenter;
        this.fields = fields;
        this.values = values;
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE
                | SWT.RESIZE);
        setBlockOnOpen(false);
    }

    // Public Methods

    /**
     * Get the current state held by the dialog in the form a JSON string. The
     * current state is specified as a dictionary that pairs field name keys
     * with those fields' values.
     * 
     * @return Current state held by the dialog as a JSON string.
     */
    public String getState() {

        // Special case: A translation has to be made between the hazard
        // categories and types tree structure that the user is creating as
        // state, and the old hazard categories list and hazard types list.
        // This should be removed if we can get rid of the visibleTypes
        // and hidHazardCategories lists in the dynamic setting.
        SettingsView.translateHazardCategoriesAndTypesToOldLists(values);

        // Translate the state into a string.
        try {
            return values.toJSONString();
        } catch (Exception e) {
            statusHandler
                    .error("SettingDialog.getState(): Error: Could not serialize JSON.",
                            e);
            return null;
        }
    }

    /**
     * Set the current state held by the dialog to equal the values given by the
     * specified dictionary pairing field name keys with those fields' values.
     * This may only be called once the dialog has been opened.
     * 
     * @param newValues
     *            Dictionary of new values to be held by the dialog.
     */
    public void setState(Dict newValues) {
        values = newValues;
        try {
            megawidgetManager.setState(newValues);
        } catch (MegawidgetStateException e) {
            statusHandler.error("SettingDialog.setState(): Error: Failed to "
                    + "accept new setting parameters.", e);
        }
    }

    // Protected Methods

    /**
     * Configure the dialog shell.
     * 
     * @param shell
     *            Shell of the dialog.
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        setDialogName(shell);
        Menu menuBar = new Menu(shell, SWT.BAR);
        createCascadeMenu(menuBar, FILE_MENU_TEXT, FILE_MENU_ITEM_NAMES,
                FILE_MENU_ITEM_ACTIONS);
        createCascadeMenu(menuBar, EDIT_MENU_TEXT, EDIT_MENU_ITEM_NAMES,
                EDIT_MENU_ITEM_ACTIONS);
        shell.setMenuBar(menuBar);
    }

    /**
     * Create the contents of the upper part of this dialog (above the button
     * bar).
     * 
     * @param parent
     *            Parent composite in which to place the area.
     * @return Dialog area that was created.
     */
    @Override
    protected Control createDialogArea(Composite parent) {

        // Let the superclass create the area, and set up its layout.
        Composite top = (Composite) super.createDialogArea(parent);
        top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Create a megawidget manager, which will create the mega-
        // widgets and manage their displaying, and allowing of mani-
        // pulation, of the the dictionary values. Since setting
        // megawidgets are assumed to not be firing off commands,
        // the command invocation response method is empty. State
        // changes cause the entire setting definition to be sent as
        // part of a settings change action.
        List<Dict> fieldsList = Lists.newArrayList();
        for (Object field : fields) {
            fieldsList.add((Dict) field);
        }
        try {
            megawidgetManager = new MegawidgetManager(top, fieldsList, values,
                    0L, 0L, 0L, 0L) {
                @Override
                protected void commandInvoked(String identifier,
                        String extraCallback) {

                    // No action.
                }

                @Override
                protected void stateElementChanged(String identifier,
                        Object state) {
                    fireAction(new SettingsAction("DynamicSettingChanged",
                            SettingDialog.this.getState()));
                }
            };
        } catch (MegawidgetException e) {
            statusHandler
                    .error("SettingDialog.createDialogArea(): Unable to create "
                            + "megawidget manager due to megawidget construction problem.",
                            e);
        }

        // Return the created client area.
        return top;
    }

    /**
     * Create the button bar. This implementation creates an empty composite, as
     * it should have no buttons.
     * 
     * @param parent
     *            Parent of the button bar.
     * @return Button bar.
     */
    @Override
    protected Control createButtonBar(Composite parent) {
        Composite bar = new Composite(parent, SWT.NONE);
        bar.setLayoutData(new GridData(0, 0));
        return bar;
    }

    // Private Methods

    /**
     * Set the title text for the specified shell.
     * 
     * @param shell
     *            Shell for which title text is to be set.
     */
    private void setDialogName(Shell shell) {
        String settingName = values.getDynamicallyTypedValue(DISPLAY_NAME);
        shell.setText(DIALOG_TITLE_PREFIX + settingName);
    }

    /**
     * Create a cascade menu for the specified menu bar with the specified menu
     * items.
     * 
     * @param menuBar
     *            Menu bar.
     * @param name
     *            Name of the cascade menu.
     * @param itemNames
     *            Array of menu item names for the cascade menu. An element of
     *            <code>null</code> indicates that a separator should be used.
     * @param itemActions
     *            Array of menu item actions for the cascade, with each being
     *            paired with the item at the same index in the <code>
     *                    itemNames</code> array. Each is either a
     *            <code>String</code>, in which case the item is handled by the
     *            dialog itself, or a <code>SettingsAction</code>, in which case
     *            it is passed to the presenter.
     */
    private void createCascadeMenu(Menu menuBar, String name,
            List<String> itemNames, List<?> itemActions) {
        MenuItem cascade = new MenuItem(menuBar, SWT.CASCADE);
        cascade.setText(name);
        Menu dropDown = new Menu(menuBar);
        cascade.setMenu(dropDown);
        for (int j = 0; j < itemNames.size(); j++) {
            MenuItem item = new MenuItem(dropDown,
                    (itemNames.get(j) == null ? SWT.SEPARATOR : SWT.PUSH));
            if (itemNames.get(j) != null) {
                item.setText(itemNames.get(j));
                if (itemActions.get(j) != null) {
                    item.setData(itemActions.get(j));
                    item.addSelectionListener(menuBarListener);
                }
            }
        }
    }

    /**
     * Handle the specified menu item invocation internally.
     * 
     * @param action
     *            Action to be taken.
     */
    private void handleMenuItemInvocationInternally(String action) {
        if (action.equals("Close")) {
            close();
        }
    }

    /**
     * Fire the specified action.
     * 
     * @param action
     *            Action to be fired.
     */
    private void fireAction(SettingsAction action) {
        presenter.fireAction(action);
    }
}
