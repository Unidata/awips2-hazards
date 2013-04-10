/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.alerts;

import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.toolbar.BasicAction;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Alerts view, an implementation of <code>IAlertsView</code> that provides an
 * SWT-based view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class AlertsView implements
        IAlertsView<IActionBars, RCPMainUserInterfaceElement> {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    @SuppressWarnings("unused")
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(AlertsView.class);

    // Private Variables

    /**
     * Presenter.
     */
    private AlertsPresenter presenter = null;

    /**
     * Alerts toggle action.
     */
    private Action alertsToggleAction = null;

    /**
     * Setting dialog.
     */
    private AlertDialog alertDialog = null;

    /**
     * Dialog dispose listener.
     */
    private final DisposeListener dialogDisposeListener = new DisposeListener() {
        @Override
        public void widgetDisposed(DisposeEvent e) {
            closeAlertDetail();
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public AlertsView() {

        // No action.
    }

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param presenter
     *            Presenter managing this view.
     */
    @Override
    public final void initialize(AlertsPresenter presenter) {
        this.presenter = presenter;
    }

    /**
     * Prepare for disposal.
     */
    @Override
    public final void dispose() {
        closeAlertDetail();
    }

    /**
     * Contribute to the main UI, if desired. Note that this method may be
     * called multiple times per <code>type</code> to (re)populate the main UI
     * with the specified <code>type</code>; implementations are responsible for
     * cleaning up after contributed items that may exist from a previous call
     * with the same <code>type</code>.
     * 
     * @param mainUI
     *            Main user interface to which to contribute.
     * @param type
     *            Type of contribution to be made to the main user interface.
     * @return True if items were contributed, otherwise false.
     */
    @Override
    public final boolean contributeToMainUI(IActionBars mainUI,
            RCPMainUserInterfaceElement type) {
        if (type == RCPMainUserInterfaceElement.TOOLBAR) {

            // Create the actions.
            alertsToggleAction = new BasicAction("", "alerts.png",
                    Action.AS_CHECK_BOX, "Alerts") {
                @Override
                public void run() {
                    if (isChecked() && (alertDialog == null)) {
                        presenter.showAlertDetail();
                    } else if ((isChecked() == false) && (alertDialog != null)) {
                        closeAlertDetail();
                    }
                }
            };
            alertsToggleAction.setEnabled(false);

            // Add the actions to the toolbar.
            mainUI.getToolBarManager().add(alertsToggleAction);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Show the alert detail subview.
     * 
     * @param fields
     *            List of dictionaries, each providing a field to be displayed
     *            in the subview.
     * @param values
     *            Dictionary pairing keys found as the field names in
     *            <code>fields</code> with their values.
     */
    @Override
    public final void showAlertDetail(DictList fields, Dict values) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog(presenter, PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getShell(), fields, values);
            alertDialog.open();
            alertDialog.getShell().addDisposeListener(dialogDisposeListener);
            alertsToggleAction.setChecked(true);
        }
    }

    // Private Methods

    /**
     * Hide the alert dialog.
     */
    private void closeAlertDetail() {
        if (alertDialog != null) {
            Shell shell = alertDialog.getShell();
            if ((shell.isDisposed() == false) && shell.isVisible()) {
                alertDialog.close();
            }
            alertDialog = null;
        }
        alertsToggleAction.setChecked(false);
    }
}
