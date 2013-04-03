/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.hazarddetail;

import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.toolbar.BasicAction;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Hazard detail view, an implementation of <code>IHazardDetailView</code> that
 * provides an SWT-based view.
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
public class HazardDetailView implements
        IHazardDetailView<IActionBars, RCPMainUserInterfaceElement> {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardDetailView.class);

    // Private Variables

    /**
     * Presenter.
     */
    private HazardDetailPresenter presenter = null;

    /**
     * Hazard detail toggle action.
     */
    private Action hazardDetailToggleAction = null;

    /**
     * Hazard detail dialog.
     */
    private HazardDetailDialog hazardDetailDialog = null;

    /**
     * Dialog visiblity listener.
     */
    private final Listener dialogHideListener = new Listener() {
        @Override
        public void handleEvent(Event e) {
            hideHazardDetail();
        }
    };

    /**
     * Flag indicating whether or not actions should be dispatched.
     */
    private boolean doNotForwardActions = false;

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public HazardDetailView() {

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
    public final void initialize(HazardDetailPresenter presenter) {
        this.presenter = presenter;
    }

    /**
     * Prepare for disposal.
     */
    @Override
    public final void dispose() {
        if ((hazardDetailDialog != null)
                && (hazardDetailDialog.getShell() != null)
                && (hazardDetailDialog.getShell().isDisposed() == false)) {
            hazardDetailDialog.close();
        }
        hazardDetailDialog = null;
    }

    /**
     * Contribute to the main UI, if desired. Note that this method may be
     * called multiple times per <code>type
     * </code> to (re)populate the main UI with the specified <code>type</code>;
     * implementations are responsible for cleaning up after contributed items
     * that may exist from a previous call with the same <code>type</code>.
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
            hazardDetailToggleAction = new BasicAction("", "hazardInfo.png",
                    Action.AS_CHECK_BOX, "Hazard Information") {
                @Override
                public void run() {
                    if (isChecked()
                            && ((hazardDetailDialog == null) || (hazardDetailDialog
                                    .getShell().isVisible() == false))) {
                        presenter.showHazardDetail();
                    } else if ((isChecked() == false)
                            && (hazardDetailDialog != null)) {
                        hideHazardDetail();
                    }
                }
            };
            hazardDetailToggleAction.setEnabled(false);

            // Add the actions to the toolbar.
            mainUI.getToolBarManager().add(hazardDetailToggleAction);
            return true;
        }
        return false;
    }

    /**
     * Show the hazard detail subview.
     * 
     * @param jsonGeneralWidgets
     *            JSON string holding a dictionary that specifies the general
     *            widgets for the dialog.
     * @param jsonMetadataWidgets
     *            JSON string holding a list of dictionaries specifying
     *            megawidgets for the metadata specific to each hazard type.
     * @param eventValuesList
     *            List of dictionaries, each holding key-value pairs that
     *            specify a hazard event.
     * @param topEventID
     *            Identifier for the hazard event that should be foregrounded
     *            with respect to other hazard events; must be one of the
     *            identifiers in the hazard events of
     *            <code>jsonEventValues</code>.
     * @param minVisibleTime
     *            Minimum visible time to be shown in the time megawidgets.
     * @param maxVisibleTime
     *            Maximum visible time to be shown in the time megawidgets.
     */
    @Override
    public final void showHazardDetail(String jsonGeneralWidgets,
            String jsonMetadataWidgets, DictList eventValuesList,
            String topEventID, long minVisibleTime, long maxVisibleTime) {

        // If there are no events to be shown, do nothing.
        if (eventValuesList == null) {
            statusHandler
                    .error("HazardDetailView.showHazardDetail(): No event "
                            + "dictionaries, so not opening the view.");
            return;
        }

        // Create the dialog if it is not already created.
        boolean justCreated = false;
        if (hazardDetailDialog == null) {

            // Build the dialog itself.
            try {
                hazardDetailDialog = new HazardDetailDialog(PlatformUI
                        .getWorkbench().getActiveWorkbenchWindow().getShell(),
                        this);
            } catch (Exception e) {
                statusHandler.error(
                        "HazardDetailView.showHazardDetail(): error "
                                + "building dialog:", e);
                return;
            }
            justCreated = true;

            // Get the basic initialization info for the dialog, and
            // initialize it.
            hazardDetailDialog.initialize(jsonGeneralWidgets,
                    jsonMetadataWidgets, minVisibleTime, maxVisibleTime);
        }

        // Set the flag indicating that HID actions should be ignored
        // while setting the dialog info and opening it.
        doNotForwardActions = true;

        // Give the dialog the event information.
        hazardDetailDialog.setHidEventInfo(eventValuesList, topEventID);

        // Open the dialog.
        if (justCreated) {
            hazardDetailDialog.open();

            // Ensure that hide events for the dialog are responded
            // to appropriately.
            hazardDetailDialog.getShell().addListener(SWT.Hide,
                    dialogHideListener);
        } else {
            hazardDetailDialog.setVisible(true);
        }

        // Enable and check the hazard detail checkbox.
        hazardDetailToggleAction.setEnabled(true);
        hazardDetailToggleAction.setChecked(true);

        // Reset the ignore HID actions flag, indicating that actions
        // from the dialog should no longer be ignored.
        doNotForwardActions = false;
    }

    /**
     * Update the hazard detail subview, if it is showing.
     * 
     * @param eventValuesList
     *            List of dictionaries, each holding key-value pairs that
     *            specify a hazard event.
     */
    @Override
    public final void updateHazardDetail(DictList eventValuesList) {

        // If the detail dialog is showing, update it.
        if ((hazardDetailDialog != null)
                && (hazardDetailDialog.getShell().isDisposed() == false)) {

            // Set the flag indicating that HID actions should be ig-
            // nored while setting the dialog info and opening it.
            doNotForwardActions = true;

            // Give the dialog the event information.
            hazardDetailDialog.setHidEventInfo(eventValuesList, null);

            // Reset the ignore HID actions flag, indicating that
            // actions
            // from the dialog should no longer be ignored.
            doNotForwardActions = false;
        }
    }

    /**
     * Hide the hazard detail subview.
     */
    @Override
    public final void hideHazardDetail() {
        if ((hazardDetailDialog != null)
                && (hazardDetailDialog.getShell() != null)
                && (hazardDetailDialog.getShell().isDisposed() == false)
                && (hazardDetailDialog.getShell().isVisible() == true)) {
            hazardDetailDialog.setVisible(false);
        }
        hazardDetailToggleAction.setChecked(false);
    }

    /**
     * Set the visible time range.
     * 
     * @param minVisibleTime
     *            Minimum visible time to be shown in the time megawidgets.
     * @param maxVisibleTime
     *            Maximum visible time to be shown in the time megawidgets.
     */
    @Override
    public final void setVisibleTimeRange(long minVisibleTime,
            long maxVisibleTime) {
        if ((hazardDetailDialog != null)
                && (hazardDetailDialog.getShell() != null)
                && (hazardDetailDialog.getShell().isDisposed() == false)) {
            hazardDetailDialog.setVisibleTimeRange(minVisibleTime,
                    maxVisibleTime);
        }
    }

    // Package Methods

    /**
     * Fire an action event to its listener.
     * 
     * @param action
     *            Action.
     */
    void fireAction(HazardDetailAction action) {
        if (doNotForwardActions == false) {
            presenter.fireAction(action);
        }
    }
}
