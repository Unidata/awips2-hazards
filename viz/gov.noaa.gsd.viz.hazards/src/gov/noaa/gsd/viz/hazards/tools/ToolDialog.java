/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.tools;

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.ui.BasicDialog;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManagerAdapter;
import gov.noaa.gsd.viz.megawidgets.MegawidgetPropertyException;
import gov.noaa.gsd.viz.megawidgets.TimeScaleSpecifier;
import gov.noaa.gsd.viz.megawidgets.sideeffects.PythonSideEffectsApplier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * Tool dialog, used to allow the user to specify parameters for tool
 * executions.
 * <p>
 * When instantiated, the dialog is passed a JSON string holding a dictionary
 * which in turn contains the following parameters:
 * <dl>
 * <dt><code>fields</code></dt>
 * <dd>List of dictionaries, with each of the latter defining a megawidget.</dd>
 * <dt><code>valueDict</code></dt>
 * <dd>Dictionary mapping tool parameter identifiers to their starting values.</dd>
 * <dt><code>interdependenciesScript</code></dt>
 * <dd>Optional string which, if provided, is used as the Python script that
 * defines the <code>applyInterdependencies()</code> method required by
 * {@link gov.noaa.gsd.viz.megawidgets.sideeffects.PythonSideEffectsApplier}. If
 * not provided, no interdependencies are applied when megawidgets are invoked
 * or experience state changes.</dd>
 * <dt><code>runToolTriggers</code></dt>
 * <dd>Optional list of megawidget identifier strings indicating which of the
 * megawidgets defined within <code>fields</code> are to trigger tool execution
 * when invoked, if any.</dd>
 * <dt><code>title</code></dt>
 * <dd>Optional string giving the dialog title.</dd>
 * <dt><code>minimumVisibleTime</code></dt>
 * <dd>Minimum visible time as required by
 * {@link gov.noaa.gsd.viz.megawidgets.TimeScaleSpecifier}. This is not required
 * if no megawidgets of the latter type are included in <code>fields</code>.</dd>
 * <dt><code>maximumVisibleTime</code></dt>
 * <dd>Maximum visible time as required by
 * {@link gov.noaa.gsd.viz.megawidgets.TimeScaleSpecifier}. This is not required
 * if no megawidgets of the latter type are included in <code>fields</code>.</dd>
 * </dl>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo.
 * Jun 20, 2013   1277     Chris.Golden      Added code to support the
 *                                           specification of a side
 *                                           effects applier for the
 *                                           megawidgets showing in the
 *                                           tool dialog.
 * Jul 18, 2013    585     Chris Golden      Changed to support loading
 *                                           from bundle.
 * Aug 21, 2013   1921     daniel.s.schaffer@noaa.gov  Call recommender framework directly
 * Dec 16, 2013   2545     Chris.Golden      Added current time provider
 *                                           for megawidget use.
 * Apr 14, 2014   2925     Chris.Golden      Minor changes to work with megawidget
 *                                           framework changes.
 * Jun 17, 2014   3982     Chris.Golden      Changed megawidget "side effects" to
 *                                           "interdependencies".
 * Jun 23, 2014   4010     Chris.Golden      Changed to work with megawidget manager
 *                                           changes.
 * Jun 30, 2014   3512     Chris Golden      Changed to work with more megawidget manager
 *                                           changes.
 * </pre>
 * 
 * @author Chris.Golden
 */
class ToolDialog extends BasicDialog {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ToolDialog.class);

    /**
     * OK button text.
     */
    private static final String OK_BUTTON_TEXT = "Run";

    // Private Variables

    /**
     * Presenter.
     */
    private final ToolsPresenter presenter;

    /**
     * Name of the tool to be executed when the "Run" button is invoked.
     */
    private final String toolName;

    /**
     * Dialog dictionary, used to hold the dialog's parameters.
     */
    private Dict dialogDict = null;

    /**
     * Values dictionary, used to hold the dialog's megawidgets' values.
     */
    private Dict valuesDict = null;

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
     * Flag indicating whether or not the dialog and its megawidgets are
     * currently enabled.
     */
    private boolean enabled = true;

    /**
     * Python side effects applier script for implementing any needed megawidget
     * interdependencies; if <code>null</code>, there is no such script.
     */
    private final String pythonSideEffectsScript;

    /**
     * List of megawidget identifiers that, when their associated megawidgets
     * are invoked, should result in the tool being run; if <code>null</code>,
     * none cause such invocations.
     */
    private final List<String> runToolTriggerIdentifiers;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param presenter
     *            Presenter.
     * @param parent
     *            Parent shell.
     * @param toolName
     *            Name of the tool to be executed.
     * @param jsonParams
     *            JSON string giving the parameters for this dialog. Within the
     *            set of all fields that are defined by these parameters, all
     *            the fields (megawidget specifiers) must have unique
     *            identifiers.
     */
    public ToolDialog(ToolsPresenter presenter, Shell parent, String toolName,
            String jsonParams) {
        super(parent);
        this.presenter = presenter;
        this.toolName = toolName;
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
        setBlockOnOpen(false);

        /*
         * Parse the strings into the dialog dictionary and various values found
         * within the dictionary.
         */
        try {
            dialogDict = Dict.getInstance(jsonParams);
        } catch (Exception e) {
            statusHandler
                    .error("ToolDialog.<init>: Error: Problem parsing JSON for dialog dictionary.",
                            e);
        }
        try {
            valuesDict = dialogDict.getDynamicallyTypedValue("valueDict");
        } catch (Exception e) {
            statusHandler
                    .error("ToolDialog.<init>: Error: Problem parsing JSON for initial values.",
                            e);
        }
        String script = null;
        try {
            script = dialogDict
                    .getDynamicallyTypedValue("interdependenciesScript");
        } catch (Exception e) {
            statusHandler
                    .error("ToolDialog.<init>: Error: Problem parsing JSON for initial values.",
                            e);
        }
        pythonSideEffectsScript = script;
        List<String> triggers = null;
        try {
            triggers = dialogDict.getDynamicallyTypedValue("runToolTriggers");
            if (triggers == null) {
                triggers = new ArrayList<>();
            }
        } catch (Exception e) {
            statusHandler
                    .error("ToolDialog.<init>: Error: Problem parsing JSON for initial values.",
                            e);
        }
        runToolTriggerIdentifiers = triggers;
    }

    // Public Methods

    /**
     * Get the current state held by the dialog. The current state is specified
     * as a dictionary that pairs field name keys with those fields' values.
     * 
     * @return Current state held by the dialog.
     */
    public Map<String, Serializable> getState() {
        return Utilities.asMap(valuesDict);

    }

    /**
     * Determine whether or not the tool dialog is currently enabled or not.
     * 
     * @return True if the tool dialog and its megawidgets are currently
     *         enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable the dialog and its megawidgets.
     * 
     * @param enabled
     *            Flag indicating whether the dialog and its megawidgets are to
     *            be enabled or disabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (megawidgetManager != null) {
            megawidgetManager.setEnabled(enabled);
        }
    }

    // Protected Methods

    @Override
    protected int getDialogBoundsStrategy() {
        return DIALOG_PERSISTLOCATION;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (dialogDict.containsKey("title")) {
            shell.setText((String) dialogDict.get("title"));
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        /*
         * Let the superclass create the area.
         */
        Composite top = (Composite) super.createDialogArea(parent);

        /*
         * Get the list of megawidget specifiers from the parameters.
         */
        Object megawidgetSpecifiersObj = dialogDict.get(HazardConstants.FIELDS);
        DictList megawidgetSpecifiers = null;
        if (megawidgetSpecifiersObj == null) {
            statusHandler.warn("ToolDialog.createDialogArea(): Warning: "
                    + "no megawidgets specified.");
            return top;
        } else if (megawidgetSpecifiersObj instanceof DictList) {
            megawidgetSpecifiers = (DictList) megawidgetSpecifiersObj;
        } else if (megawidgetSpecifiersObj instanceof List) {
            megawidgetSpecifiers = new DictList();
            for (Object megawidgetSpecifier : (List<?>) megawidgetSpecifiersObj) {
                megawidgetSpecifiers.add(megawidgetSpecifier);
            }
        } else {
            megawidgetSpecifiers = new DictList();
            megawidgetSpecifiers.add(megawidgetSpecifiersObj);
        }

        /*
         * Get the minimum and maximum visible times for any time scale
         * megawidgets that might be created.
         */
        long minVisibleTime = 0L;
        try {
            minVisibleTime = ((Number) dialogDict
                    .get(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME)).longValue();
        } catch (Exception e) {
            statusHandler.info("ToolDialog.createDialogArea(): Warning: No "
                    + TimeScaleSpecifier.MINIMUM_VISIBLE_TIME
                    + " specified in " + "dialog dictionary.");
            minVisibleTime = TimeUtil.currentTimeMillis();
        }
        long maxVisibleTime = 0L;
        try {
            maxVisibleTime = ((Number) dialogDict
                    .get(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME)).longValue();
        } catch (Exception e) {
            statusHandler.info("ToolDialog.createDialogArea(): Warning: No "
                    + TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME
                    + " specified in " + "dialog dictionary.");
            maxVisibleTime = minVisibleTime + TimeUnit.DAYS.toMillis(1);
        }

        /*
         * Create a megawidget manager, which will create the megawidgets and
         * manage their displaying, and allowing of manipulation, of the the
         * dictionary values. Invocations are interpreted as tools to be run,
         * with the tool name being contained within the extra callback
         * information. If a Python side effects script was supplied as part of
         * the dialog parameters, create a Python side effects applier object
         * and pass it to the megawidget manager.
         */
        List<Dict> megawidgetSpecifiersList = new ArrayList<>();
        for (Object specifier : megawidgetSpecifiers) {
            megawidgetSpecifiersList.add((Dict) specifier);
        }
        try {
            PythonSideEffectsApplier sideEffectsApplier = (pythonSideEffectsScript == null ? null
                    : new PythonSideEffectsApplier(pythonSideEffectsScript));
            megawidgetManager = new MegawidgetManager(
                    top,
                    megawidgetSpecifiersList,
                    valuesDict,
                    new MegawidgetManagerAdapter() {

                        @Override
                        public void commandInvoked(MegawidgetManager manager,
                                String identifier) {

                            /*
                             * Fire off the action if the invoked megawidget is
                             * a tool-running trigger.
                             */
                            if (runToolTriggerIdentifiers.contains(identifier)) {
                                fireAction(new ToolAction(
                                        ToolAction.ToolActionEnum.RUN_TOOL_WITH_PARAMETERS,
                                        identifier, ToolDialog.this.getState()));
                            }
                        }

                        @Override
                        public void sizeChanged(MegawidgetManager manager,
                                String identifier) {

                            /*
                             * TODO: When scrollbars are added to the tool
                             * dialog's megawidget panel as an option, this
                             * should result in a recalculation of the
                             * scrollable area.
                             */
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public void sideEffectMutablePropertyChangeErrorOccurred(
                                MegawidgetManager manager,
                                MegawidgetPropertyException exception) {
                            statusHandler
                                    .error("ToolDialog.MegawidgetManager error occurred "
                                            + "while attempting to apply megawidget interdependencies",
                                            exception);
                        }

                    }, minVisibleTime, maxVisibleTime, currentTimeProvider,
                    sideEffectsApplier);
        } catch (MegawidgetException e) {
            statusHandler
                    .error("ToolDialog.createDialogArea(): Unable to create megawidget "
                            + "manager due to megawidget construction problem.",
                            e);
        }

        /*
         * Return the created area.
         */
        return top;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        Button ok = getButton(IDialogConstants.OK_ID);
        ok.setText(OK_BUTTON_TEXT);
        setButtonLayoutData(ok);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);
        if (buttonId == IDialogConstants.OK_ID) {
            fireAction(new ToolAction(
                    ToolAction.ToolActionEnum.RUN_TOOL_WITH_PARAMETERS,
                    toolName, getState()));
        }
    }

    // Private Methods

    /**
     * Fire the specified action.
     * 
     * @param action
     *            Action to be fired.
     */
    private void fireAction(ToolAction action) {
        presenter.fireAction(action);
    }
}