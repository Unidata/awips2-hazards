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

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.display.action.StaticSettingsAction;
import gov.noaa.gsd.viz.hazards.ui.BasicDialog;
import gov.noaa.gsd.viz.hazards.utilities.MegawidgetSettingsConversionUtils;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManagerAdapter;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.SettingsConfig;

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
 * Nov 29, 2013   2380     daniel.s.schaffer@noaa.gov Minor cleanup
 * Dec 16, 2013   2545     Chris.Golden    Added current time provider for megawidget
 *                                         use.
 * Feb 19, 2014   2915     bkowal          JSON settings re-factor
 * Apr 14, 2014   2925     Chris.Golden    Minor changes to work with megawidget
 *                                         framework changes.
 * Jun 23, 2014   4010     Chris.Golden    Changed to work with megawidget manager
 *                                         changes.
 * Jun 30, 2014   3512     Chris.Golden    Changed to work with more megawidget
 *                                         manager changes.
 * Aug 27, 2014   3768     Robert.Blum     Added ability to select recommenders as 
 *                                         part of the settings dialog.
 * Dec 05, 2014   4124     Chris.Golden    Changed to work with ObservedSettings.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
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
     * File menu text.
     */
    private static final String FILE_MENU_TEXT = "&File";

    /**
     * File menu item names.
     */
    private static final List<String> FILE_MENU_ITEM_NAMES = Lists
            .newArrayList("&Save", "Save &As", "&Close");;

    // Private Constants

    /**
     * File menu item actions; each is either a {@link StaticSettingsAction},
     * meaning it is forwarded to the presenter; a {@link String}, meaning it is
     * handled internally; a {@link Runnable}, which is is used to implement
     * non-standard behavior; or <code>null</code> for any menu item that has no
     * effect.
     */
    private final List<?> FILE_MENU_ITEM_ACTIONS = Lists.newArrayList(
            new Runnable() {
                @Override
                public void run() {
                    fireAction(new StaticSettingsAction(
                            StaticSettingsAction.ActionType.SAVE,
                            currentSettings));
                }
            }, new Runnable() {
                private final IInputValidator validator = new IInputValidator() {
                    @Override
                    public String isValid(String text) {
                        if (text.length() < 1) {
                            return "The name must contain at least one character";
                        }
                        List<Settings> settings = presenter.getSessionManager()
                                .getConfigurationManager()
                                .getAvailableSettings();
                        for (Settings setting : settings) {
                            if (setting.getDisplayName() == null) {
                                continue;
                            }
                            if (setting.getDisplayName().equals(text)) {
                                return "There is already a setting with this name";
                            }
                        }
                        return null;
                    }
                };

                @Override
                public void run() {
                    InputDialog inputDialog = new InputDialog(getShell(),
                            "Save Setting As", "Enter the new setting name: ",
                            "", validator);
                    if (inputDialog.open() == InputDialog.OK) {
                        currentSettings.setDisplayName(inputDialog.getValue());
                        currentSettings.setSettingsID(inputDialog.getValue());
                        SettingDialog.this.setDialogName(getShell());
                        fireAction(new StaticSettingsAction(
                                StaticSettingsAction.ActionType.SAVE_AS,
                                currentSettings));
                    }
                }
            }, "Close");

    /**
     * Presenter.
     */
    private final SettingsPresenter presenter;

    /**
     * Fields dictionary, used to hold the dialog's fields.
     */
    private final SettingsConfig fields;

    /**
     * Current settings.
     */
    private ObservedSettings currentSettings;

    /**
     * Recommender Composite.
     */
    private Composite recommenderComp;

    /**
     * Current time provider.
     */
    private final ICurrentTimeProvider currentTimeProvider = new ICurrentTimeProvider() {
        @Override
        public long getCurrentTime() {
            return SimulatedTime.getSystemTime().getMillis();
        }
    };

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
            if (action instanceof StaticSettingsAction) {
                fireAction((StaticSettingsAction) action);
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
     * @param currentSettings
     *            Current settings.
     */
    public SettingDialog(SettingsPresenter presenter, Shell parent,
            SettingsConfig fields, ObservedSettings currentSettings) {
        super(parent);
        this.presenter = presenter;
        this.fields = fields;
        this.currentSettings = currentSettings;
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE
                | SWT.RESIZE);
        setBlockOnOpen(false);
    }

    // Public Methods

    /**
     * Set the current state held by the dialog to equal the values given by the
     * specified dictionary pairing field name keys with those fields' values.
     * This may only be called once the dialog has been opened.
     * 
     * @param settings
     *            New settings.
     */
    public void setState(ObservedSettings settings) {
        currentSettings = settings;
        try {
            megawidgetManager.setState(MegawidgetSettingsConversionUtils
                    .settingsPOJOToMap(settings));
        } catch (MegawidgetStateException e) {
            statusHandler.error("SettingDialog.setState(): Error: Failed to "
                    + "accept new setting parameters: " + e, e);
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to convert the Current Settings to a Java Map!", e);
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
        /*
         * createCascadeMenu(menuBar, EDIT_MENU_TEXT, EDIT_MENU_ITEM_NAMES,
         * EDIT_MENU_ITEM_ACTIONS);
         */
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

        /*
         * Let the superclass create the area, and set up its layout.
         */
        Composite top = (Composite) super.createDialogArea(parent);
        top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        /*
         * Create a megawidget manager, which will create the megawidgets and
         * manage their displaying, and allowing of manipulation, of the the
         * dictionary values. Since setting megawidgets are assumed to not be
         * firing off commands, the command invocation response method is empty.
         * State changes cause the entire setting definition to be sent as part
         * of a settings change action.
         */
        try {

            /*
             * convert the SettingsConfig POJO to a Map.
             */
            List<Map<String, Object>> listForMegawidget = new ArrayList<Map<String, Object>>();
            listForMegawidget.add(MegawidgetSettingsConversionUtils
                    .settingsConfigPOJOToMap(this.fields));

            megawidgetManager = new MegawidgetManager(top, listForMegawidget,
                    MegawidgetSettingsConversionUtils
                            .settingsPOJOToMap(this.currentSettings),
                    new MegawidgetManagerAdapter() {

                        @Override
                        public void stateElementChanged(
                                MegawidgetManager manager, String identifier,
                                Object state) {
                            settingChanged();
                        }

                        @Override
                        public void stateElementsChanged(
                                MegawidgetManager manager,
                                Map<String, Object> statesForIdentifiers) {
                            settingChanged();
                        }
                    }, 0L, 0L, currentTimeProvider);
        } catch (MegawidgetException e) {
            statusHandler
                    .error("SettingDialog.createDialogArea(): Unable to create "
                            + "megawidget manager due to megawidget construction problem: "
                            + e, e);
        } catch (Exception e) {
            statusHandler.error(
                    "Failed to convert the Current Settings to a Java Map!", e);
        }
        createRecommenderComposite();
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
        shell.setText(DIALOG_TITLE_PREFIX + currentSettings.getDisplayName());
    }

    /**
     * Respond to the setting being changed.
     */
    private void settingChanged() {
        ISessionConfigurationManager<ObservedSettings> configManager = presenter
                .getSessionManager().getConfigurationManager();
        currentSettings = MegawidgetSettingsConversionUtils
                .updateSettingsUsingMap(currentSettings,
                        megawidgetManager.getState(), configManager,
                        UIOriginator.SETTINGS_DIALOG);
        fireAction(new StaticSettingsAction(
                StaticSettingsAction.ActionType.SETTINGS_MODIFIED,
                currentSettings));
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
     *            {@link String}, in which case the item is handled by the
     *            dialog itself, or a {@link StaticSettingsAction}, in which
     *            case it is passed to the presenter.
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
    private void fireAction(StaticSettingsAction action) {
        presenter.publish(action);
    }

    /**
     * Adds a RecommenderInventoryComposite to the SWTWrapperMegawidget's
     * composite.
     */
    private void createRecommenderComposite() {
        Composite wrapperComposite = megawidgetManager.getSwtWrapper(
                "Recommenders").getWrapperComposite();

        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;

        SashForm sashForm = new SashForm(wrapperComposite, SWT.VERTICAL);
        sashForm.SASH_WIDTH = 5;
        sashForm.setLayout(gl);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        sashForm.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_DARK_GRAY));

        wrapperComposite.setLayout(gl);
        wrapperComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        recommenderComp = new RecommenderInventoryComposite(sashForm, presenter);
        sashForm.setWeights(new int[] { 80, 20 });
        megawidgetManager.getSwtWrapper("Recommenders").sizeChanged();
    }
}
