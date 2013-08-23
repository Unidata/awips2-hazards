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

import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;
import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.display.ViewPartDelegatorView;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.toolbar.BasicAction;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.WorkbenchPage;

import com.google.common.collect.Lists;
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
 * May 10, 2013            Chris.Golden      Change to Eclipse view implementation.
 * Jun 25, 2013            Chris.Golden      Changed to allow hazard metadata
 *                                           changes to be pushed to the event bus
 *                                           if so desired by the view part.
 * Jul 12, 2013    585     Chris.Golden      Changed to support loading from bundle.
 * Aug 22, 2013   1921     Bryon.Lawrence    Added accessor method for
 *                                           testing whether or not HID 
 *                                           updates should fire-off
 *                                           messages.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@SuppressWarnings("restriction")
public class HazardDetailView extends
        ViewPartDelegatorView<HazardDetailViewPart> implements
        IHazardDetailView<Action, RCPMainUserInterfaceElement> {

    // Private Static Constants

    /**
     * Name of the file holding the image for the alerts toolbar button icon.
     */
    private static final String HAZARD_DETAIL_TOOLBAR_IMAGE_FILE_NAME = "hazardInfo.png";

    /**
     * Text describing the hazard detail toolbar button.
     */
    private static final String HAZARD_DETAIL_TEXT = "Hazard Information";

    /**
     * Suffix for the preferences key used to determine whether or not to use
     * the previous size and position of the view part when showing it.
     */
    private static final String USE_PREVIOUS_SIZE_AND_POSITION_KEY_SUFFIX = ".usePreviousHazardDetailViewPartSizeAndPosition";

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
     * JSON string holding a dictionary that specifies the general widgets for
     * the dialog.
     */
    private String jsonGeneralWidgets = null;

    /**
     * JSON string holding a list of dictionaries specifying megawidgets for the
     * metadata specific to each hazard type.
     */
    private String jsonMetadataWidgets = null;

    /**
     * Minimum visible time to be shown in the time megawidgets.
     */
    private long minVisibleTime = 0L;

    /**
     * Maximum visible time to be shown in the time megawidgets.
     */
    private long maxVisibleTime = 0L;

    /**
     * View part listener.
     */
    private final IPartListener2 partListener = new IPartListener2() {

        @Override
        public void partActivated(IWorkbenchPartReference partRef) {

            // No action.
        }

        @Override
        public void partBroughtToTop(IWorkbenchPartReference partRef) {

            // No action.
        }

        @Override
        public void partClosed(IWorkbenchPartReference partRef) {
            if (partRef == getViewPartReference()) {
                if (hazardDetailToggleAction != null) {
                    hazardDetailToggleAction.setEnabled(true);
                    hazardDetailToggleAction.setChecked(false);
                }
                viewPartShowing = false;
                hideViewPart(true);
            }
        }

        @Override
        public void partDeactivated(IWorkbenchPartReference partRef) {

            // No action.
        }

        @Override
        public void partOpened(IWorkbenchPartReference partRef) {

            // No action.
        }

        @Override
        public void partHidden(IWorkbenchPartReference partRef) {
            if (partRef == getViewPartReference()) {
                if (hazardDetailToggleAction != null) {
                    hazardDetailToggleAction.setEnabled(true);
                    hazardDetailToggleAction.setChecked(false);
                }
                viewPartShowing = false;
            }
        }

        @Override
        public void partVisible(IWorkbenchPartReference partRef) {
            if (partRef == getViewPartReference()) {
                if (hazardDetailToggleAction != null) {
                    hazardDetailToggleAction.setEnabled(true);
                    hazardDetailToggleAction.setChecked(true);
                }
                viewPartShowing = true;
            }
        }

        @Override
        public void partInputChanged(IWorkbenchPartReference partRef) {

            // No action.
        }
    };

    /**
     * Flag indicating whether or not the view part is showing (the alternative
     * is that it is minimized).
     */
    private boolean viewPartShowing = true;

    /**
     * Flag indicating whether or not to use the previous size and position for
     * the view part from the moment it is created.
     */
    private boolean usePreviousSizeAndPosition;

    /**
     * Flag indicating whether or not actions should be dispatched.
     */
    private boolean doNotForwardActions = false;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param loadedFromBundle
     *            Flag indicating whether or not the view is being instantiated
     *            as a result of a bundle load.
     */
    public HazardDetailView(final boolean loadedFromBundle) {
        super(HazardDetailViewPart.ID, HazardDetailViewPart.class);

        // Determine whether the view part uses its previous size
        // and position by seeing whether the flag indicating this
        // should happen exists. If it does not, create the flag
        // in the preferences so that future invocations of the
        // hazard detail view use previous size and position.
        WorkbenchPage page = (WorkbenchPage) getActiveWorkbenchPage(true);
        String usePreviousSizeAndPositionKey = page.getPerspective().getId()
                + USE_PREVIOUS_SIZE_AND_POSITION_KEY_SUFFIX;
        IPreferenceStore preferenceStore = HazardServicesActivator.getDefault()
                .getPreferenceStore();
        usePreviousSizeAndPosition = preferenceStore
                .contains(usePreviousSizeAndPositionKey);
        if (usePreviousSizeAndPosition == false) {
            preferenceStore.setValue(usePreviousSizeAndPositionKey, true);
        }

        // Show the view part.
        showViewPart();

        // Execute further manipulation of the view part immediately,
        // or delay such execution until the view part is created if
        // it has not yet been created yet.
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {

                // If previous size and position is not to be used,
                // detach the view, as this is the default starting
                // state. It would be nice if this were possible via
                // the plugin.xml perspective extension entry for this
                // view, but apparently it can only be started off de-
                // tached programmatically. If the view is not being
                // detached, then if it is being instantiated as a
                // result of a bundle load, minimize it.
                WorkbenchPage page = (WorkbenchPage) getActiveWorkbenchPage(true);
                if ((usePreviousSizeAndPosition == false) && (page != null)) {
                    page.detachView(page
                            .findViewReference(HazardDetailViewPart.ID));
                } else if (loadedFromBundle && isViewPartDocked()) {
                    setViewPartVisible(false);
                    viewPartShowing = false;
                }

                // Set the use previous size and position flag to true
                // so that any future showings of the view part by
                // this view come up with previous dimensions.
                usePreviousSizeAndPosition = true;

                // Register the part listener for view part events so
                // that the closing of the hazard detail view part may
                // be responded to.
                setPartListener(partListener);
            }
        });
    }

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param presenter
     *            Presenter managing this view.
     * @param jsonGeneralWidgets
     *            JSON string holding a dictionary that specifies the general
     *            widgets for the dialog.
     * @param jsonMetadataWidgets
     *            JSON string holding a list of dictionaries specifying
     *            megawidgets for the metadata specific to each hazard type.
     * @param minVisibleTime
     *            Minimum visible time to be shown in the time megawidgets.
     * @param maxVisibleTime
     *            Maximum visible time to be shown in the time megawidgets.
     */
    @Override
    public final void initialize(HazardDetailPresenter presenter,
            String jsonGeneralWidgets, String jsonMetadataWidgets,
            long minVisibleTime, long maxVisibleTime) {
        this.presenter = presenter;
        this.jsonGeneralWidgets = jsonGeneralWidgets;
        this.jsonMetadataWidgets = jsonMetadataWidgets;
        this.minVisibleTime = minVisibleTime;
        this.maxVisibleTime = maxVisibleTime;
        if (minVisibleTime == maxVisibleTime) {
            this.maxVisibleTime = this.minVisibleTime
                    + TimeUnit.DAYS.toMillis(1);
        }

        // Execute manipulation of the view part immediately, or delay such
        // execution until the view part is created if it has not yet been
        // created yet.
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                // If undocked, hide the view part, since it is empty; other-
                // wise, initialize the view part.
                if (isViewPartDocked() == false) {
                    hideViewPart(false);
                } else {
                    initializeViewPart();
                }
            }
        });
    }

    /**
     * Contribute to the main UI, if desired. Note that this method may be
     * called multiple times per <code>type</code> to (re)populate the main UI
     * with the specified <code>type</code>; implementations are responsible for
     * cleaning up after contributed items that may exist from a previous call
     * with the same <code>type</code>.
     * 
     * @param type
     *            Type of contribution to be made to the main user interface.
     * @return List of contributions; this may be empty if none are to be made.
     */
    @Override
    public final List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        if (type == RCPMainUserInterfaceElement.TOOLBAR) {

            // Create the actions.
            boolean showing = isViewPartVisible();
            hazardDetailToggleAction = new BasicAction("",
                    HAZARD_DETAIL_TOOLBAR_IMAGE_FILE_NAME, Action.AS_CHECK_BOX,
                    HAZARD_DETAIL_TEXT) {
                @Override
                public void run() {
                    boolean showing = isViewPartVisible();
                    if (isChecked() && (showing == false)) {
                        presenter.showHazardDetail(true);
                    } else if ((isChecked() == false) && showing) {
                        hideHazardDetail(true);
                    }
                }
            };
            hazardDetailToggleAction.setEnabled(showing);
            hazardDetailToggleAction.setChecked(showing);
            return Lists.newArrayList(hazardDetailToggleAction);
        }
        return Collections.emptyList();
    }

    /**
     * Show the hazard detail subview.
     * 
     * @param eventValuesList
     *            List of dictionaries, each holding key-value pairs that
     *            specify a hazard event.
     * @param topEventID
     *            Identifier for the hazard event that should be foregrounded
     *            with respect to other hazard events; must be one of the
     *            identifiers in the hazard events of
     *            <code>eventValuesList</code>.
     * @param force
     *            Flag indicating whether or not to force the showing of the
     *            subview. This may be used as a hint by views if they are
     *            considering not showing the subview for whatever reason.
     */
    @Override
    public final void showHazardDetail(final DictList eventValuesList,
            final String topEventID, final boolean force) {

        // If there are no events to be shown, do nothing.
        if ((force == false)
                && ((eventValuesList == null) || (eventValuesList.size() == 0))) {
            statusHandler
                    .error("HazardDetailView.showHazardDetail(): No event "
                            + "dictionaries, so not opening the view.");
            return;
        }

        System.err.println("Show hazard detail called, events = "
                + (eventValuesList == null ? "null" : eventValuesList.size())
                + " (top event = " + topEventID + "), force = " + force);

        // If the view part does not exist, show it.
        final boolean needsInitializing = (getViewPart() == null);
        if (needsInitializing) {
            viewPartShowing = true;
            showViewPart();
        }

        // Execute further manipulation of the view part immediately,
        // or delay such execution until the view part is created if
        // it has not yet been created yet.
        executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {

                // Set the flag indicating that HID actions should be
                // ignored while setting the dialog info and opening
                // it.
                doNotForwardActions = true;

                // Initialize the view part if necessary.
                if (needsInitializing) {
                    initializeViewPart();
                }

                // Give the view part the event information.
                if ((eventValuesList != null) && (eventValuesList.size() > 0)) {
                    getViewPart().setHidEventInfo(eventValuesList, topEventID);
                }
                int numEvents = getViewPart().getEventCount();

                // Ensure that the view part is visible.
                if ((isViewPartVisible() == false)
                        && ((numEvents > 0) || force || isViewPartDocked())) {
                    setViewPartVisible(true);
                }

                // Enable and check the hazard detail checkbox.
                if (hazardDetailToggleAction != null) {
                    hazardDetailToggleAction.setEnabled(true);
                    hazardDetailToggleAction.setChecked(true);
                }

                // Reset the ignore HID actions flag, indicating that
                // actions from the dialog should no longer be ig-
                // nored.
                doNotForwardActions = false;
            }
        });
    }

    /**
     * Update the hazard detail subview, if it is showing.
     * 
     * @param eventValuesList
     *            List of dictionaries, each holding key-value pairs that
     *            specify a hazard event.
     * @param topEventID
     *            Identifier for the hazard event that should be foregrounded
     *            with respect to other hazard events; must be one of the
     *            identifiers in the hazard events of
     *            <code>eventValuesList</code>.
     */
    @Override
    public final void updateHazardDetail(DictList eventValuesList,
            String topEventID) {

        System.err.println("Update hazard detail called, events = "
                + (eventValuesList == null ? "null" : eventValuesList.size())
                + " (top event = " + topEventID + ").");

        // If the view part exists, update it; otherwise, if there
        // is at least one event to show, show the view part.
        if (getViewPart() != null) {

            // Set the flag indicating that HID actions should be ig-
            // nored while setting the view part info and opening it.
            doNotForwardActions = true;

            // Give the view part the event information.
            getViewPart().setHidEventInfo(eventValuesList, topEventID);

            // Reset the ignore HID actions flag, indicating that
            // actions from the view part should no longer be ignored.
            doNotForwardActions = false;

            // If the event values list is empty and the view part is
            // not docked, hide the view.
            if (((eventValuesList == null) || (eventValuesList.size() == 0))
                    && (isViewPartDocked() == false)) {
                hideHazardDetail(false);
            }
        } else if ((eventValuesList != null) && (eventValuesList.size() > 0)) {
            showHazardDetail(eventValuesList, topEventID, true);
        }
    }

    /**
     * Hide the hazard detail subview.
     * 
     * @param force
     *            Flag indicating whether or not to force the hiding of the
     *            subview. This may be used as a hint by views if they are
     *            considering not hiding the subview for whatever reason.
     */
    @Override
    public final void hideHazardDetail(boolean force) {

        System.err.println("Hide hazard detail called, view part (docked = "
                + isViewPartDocked()
                + ", visible = "
                + isViewPartVisible()
                + ") = "
                + (getViewPart() == null ? "null" : Integer
                        .toString(getViewPart().getEventCount())));

        // If the view part is not showing, it may have been meant to
        // be showing, but was unable to because its instantiation was
        // delayed by another view part with the same identifier already
        // existing. If this is the case, clear the jobs queue for the
        // view part, as it should now no longer be brought up once the
        // old view part disappears. Otherwise, if the view part is show-
        // ing but the event count is zero or a forced hide should occur,
        // minimize it if it is docked, or hide it otherwise.
        boolean hidden = true;
        if (getViewPart() == null) {
            hideViewPart(true);
        } else if (isViewPartDocked() == false) {
            hideViewPart(false);
        } else if (force) {
            setViewPartVisible(false);
        } else {
            hidden = false;
            // if ((getViewPart().getEventCount() == 0) || force) {
            // if (isViewPartDocked()) {
            // if (isViewPartVisible()) {
            // setViewPartVisible(false);
            // }
            // } else {
            // hideViewPart(false);
            // }
            // }
        }
        if (hidden) {
            hazardDetailToggleAction.setChecked(false);
        }
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
        this.minVisibleTime = minVisibleTime;
        this.maxVisibleTime = maxVisibleTime;

        // Only set the visible time range of the view part if it exists;
        // no need to schedule an execution of this if the view part is
        // not yet showing, as when it is shown, it will pick up this
        // visible time range from the initialization it undergoes.
        if (getViewPart() != null) {
            getViewPart().setVisibleTimeRange(minVisibleTime, maxVisibleTime);
        }
    }

    // Package Methods

    /**
     * Fire an action event to its listener.
     * 
     * @param action
     *            Action.
     * @param force
     *            Flag indicating whether or not the firing should be forced
     *            even if normally the event would not be forwarded.
     */
    void fireAction(HazardDetailAction action, boolean force) {
        if (force || (doNotForwardActions == false)) {
            presenter.fireAction(action);
        }
    }

    // Private Methods

    /**
     * Initialize the view part.
     */
    private void initializeViewPart() {
        getViewPart().initialize(this, jsonGeneralWidgets, jsonMetadataWidgets,
                minVisibleTime, maxVisibleTime);
    }

    /**
     * Determine whether or not the view part is currently visible.
     * 
     * @return True if the view part is currently visible, false otherwise.
     */
    private boolean isViewPartVisible() {
        return ((getViewPart() != null) && viewPartShowing);
    }

    /**
     * Determine whether or not the view part is docked, or if invisible, was
     * docked when last visible.
     * 
     * @return True if the view part is docked, or if invisible, was docked the
     *         last time it was visible.
     */
    private boolean isViewPartDocked() {
        return (getViewPart() == null ? false : getViewPart().isDocked());
    }

    /**
     * Returns whether or not HID actions should be fired.
     * 
     * @return True - HID actions should not be fired-off. False - HID Actions
     *         should be fired off.
     */
    public boolean doNotForwardActions() {
        return doNotForwardActions;
    }

}
