/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.alerts;

import gov.noaa.gsd.viz.hazards.dialogs.BasicDialog;
import gov.noaa.gsd.viz.hazards.display.action.AlertsAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.setting.SettingsView;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;

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
 * Alert dialog, allowing the user to create or modify alerts.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            daniel.s.schaffer      Initial induction into repo
 * Jul 18, 2013    585     Chris.Golden           Changed to support loading from bundle.
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
class AlertDialog extends BasicDialog {

    // Private Static Constants

    /**
     * Key into the hazard categories and types dictionaries that yields the
     * displayable name for each type.
     */
    private static final String DISPLAY_NAME = "displayName";

    /**
     * File menu item names.
     */
    private static final String[] FILE_MENU_ITEM_NAMES = { "&New", "&Save",
            "Save &As", "&Delete", null, "&Close" };

    /**
     * Edit menu item names.
     */
    private static final String[] EDIT_MENU_ITEM_NAMES = { "&Revert" };

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(AlertDialog.class);

    // Private Constants

    /**
     * File menu item actions; each is either an <code>AlertsAction</code>,
     * meaning it is forwarded to the presenter; a <code>String</code>, meaning
     * it is handled internally; a <code>Runnable</code>, which is is used to
     * implement non-standard behavior; or <code>null</code> for any menu item
     * that has no effect.
     */
    private final Object[] FILE_MENU_ITEM_ACTIONS = {
            new AlertsAction("New", null), new Runnable() {
                @Override
                public void run() {
                    fireAction(new AlertsAction("Save", getState()));
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
                    InputDialog inputDialog = new InputDialog(getShell(),
                            "Hazard Services", "Enter the new setting name: ",
                            "", validator);
                    if (inputDialog.open() == InputDialog.OK) {
                        values.put(DISPLAY_NAME, inputDialog.getValue());
                        setDialogName(getShell());
                        fireAction(new AlertsAction("Save As", getState()));
                    }
                }
            }, new AlertsAction("Dialog", "Delete"), null, "Close" };

    /**
     * Edit menu item actions; each is either an <code>AlertsAction</code>,
     * meaning it is forwarded to the presenter; a <code>String</code>, meaning
     * it is handled internally; a <code>Runnable</code>, which is is used to
     * implement non-standard behavior; or <code>null</code> for any menu item
     * that has no effect.
     */
    private final Object[] EDIT_MENU_ITEM_ACTIONS = { null };

    // Private Variables

    /**
     * Presenter.
     */
    private final AlertsPresenter presenter;

    /**
     * Fields dictionary, used to hold the dialog's fields.
     */
    private final DictList fields;

    /**
     * Values dictionary, used to hold the dialog's widgets' values.
     */
    private final Dict values;

    /**
     * Megawidget manager.
     */
    @SuppressWarnings("unused")
    private MegawidgetManager megawidgetManager;

    /**
     * Selection listener for the menu bar's items.
     */
    private final SelectionListener menuBarListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            Object action = ((MenuItem) e.widget).getData();
            if (action instanceof AlertsAction) {
                fireAction((AlertsAction) action);
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
     * @param jsonFieldsParams
     *            JSON string giving a list of dictionaries, each of the latter
     *            holding the parameters for the fields making up the dialog.
     *            Within the set of all fields that are defined by these
     *            parameters, all the fields (widget specifiers) must have
     *            unique identifiers.
     * @param jsonValuesParams
     *            JSON string giving the values for each of the fields defined
     *            in <code>jsonFieldsParams</code> as a dictionary, one entry
     *            for each field identifier.
     */
    public AlertDialog(AlertsPresenter presenter, Shell parent,
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

        // Special case: A translation has to be made between the
        // hazard categories and types tree structure that the user
        // is creating as state, and the old hazard categories list
        // and hazard types list.
        SettingsView.translateHazardCategoriesAndTypesToOldLists(values);

        // Translate the state into a string.
        try {
            return values.toJSONString();
        } catch (Exception e) {
            statusHandler.error(
                    "AlertDialog.getState(): Error: Could not serialize JSON.",
                    e);
            return null;
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
        createCascadeMenu(menuBar, "&File", FILE_MENU_ITEM_NAMES,
                FILE_MENU_ITEM_ACTIONS);
        createCascadeMenu(menuBar, "&Edit", EDIT_MENU_ITEM_NAMES,
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

        // Let the superclass create the area, and then set up its
        // layout.
        Composite top = (Composite) super.createDialogArea(parent);
        top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Create a megawidget manager, which will create the mega-
        // widgets and manage their displaying, and allowing of mani-
        // pulation, of the the dictionary values.
        List<Dict> fieldsList = Lists.newArrayList();
        for (Object field : fields) {
            fieldsList.add((Dict) field);
        }
        try {
            megawidgetManager = new MegawidgetManager(parent, fieldsList,
                    values, 0L, 0L, 0L, 0L) {
                @Override
                protected void commandInvoked(String identifier,
                        String extraCallback) {

                    // No action.
                }

                @Override
                protected void stateElementChanged(String identifier,
                        Object state) {

                    // No action.
                }
            };
        } catch (MegawidgetException e) {
            statusHandler
                    .error("AlertDialog.createDialogArea(): Unable to create megawidget "
                            + "manager due to megawidget construction problem.",
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
        shell.setText("Alert Configurations");
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
     *            itemNames</code> array. Each is either a <code>String</code>,
     *            in which case the item is handled by the dialog itself, or a
     *            <code>SettingsAction</code>, in which case it is passed to the
     *            presenter.
     */
    private void createCascadeMenu(Menu menuBar, String name,
            String[] itemNames, Object[] itemActions) {
        MenuItem cascade = new MenuItem(menuBar, SWT.CASCADE);
        cascade.setText(name);
        Menu dropDown = new Menu(menuBar);
        cascade.setMenu(dropDown);
        for (int j = 0; j < itemNames.length; j++) {
            MenuItem item = new MenuItem(dropDown,
                    (itemNames[j] == null ? SWT.SEPARATOR : SWT.PUSH));
            if (itemNames[j] != null) {
                item.setText(itemNames[j]);
                if (itemActions[j] != null) {
                    item.setData(itemActions[j]);
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
    private void fireAction(AlertsAction action) {
        presenter.fireAction(action);
    }
}
