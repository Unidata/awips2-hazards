/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import gov.noaa.gsd.viz.mvp.IView;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * View that delegates to an Eclipse view part.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 08, 2013            Chris.Golden      Initial creation
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class ViewPartDelegatorView<V extends ViewPart> implements
        IView<IActionBars, RCPMainUserInterfaceElement> {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ViewPartDelegatorView.class);

    // Private Variables

    /**
     * View part identifier.
     */
    private final String viewPartIdentifier;

    /**
     * View part class.
     */
    private final Class<V> viewPartClass;

    /**
     * View part.
     */
    private V viewPart = null;

    /**
     * View part listener.
     */
    private IPartListener partListener = null;

    /**
     * View part listener (expanded version).
     */
    private IPartListener2 partListener2 = null;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param viewPartIdentifier
     *            Identifier of the view part to which to delegate.
     * @param viewPartClass
     *            Class of the view part to which to delegate.
     */
    public ViewPartDelegatorView(String viewPartIdentifier,
            Class<V> viewPartClass) {
        this.viewPartIdentifier = viewPartIdentifier;
        this.viewPartClass = viewPartClass;
    }

    // Public Methods

    @Override
    public void dispose() {

        // Remove the part listener, if any.
        IWorkbenchPage page = getActiveWorkbenchPage(false);
        if (page != null) {
            removePartListener(page);
        }

        // Hide the view part.
        hideViewPart(false);
    }

    @Override
    public abstract boolean contributeToMainUI(IActionBars mainUI,
            RCPMainUserInterfaceElement type);

    // Protected Methods

    /**
     * Show the view part.
     */
    @SuppressWarnings("unchecked")
    protected final void showViewPart() {

        // Get the active workbench page.
        IWorkbenchPage page = getActiveWorkbenchPage(true);
        if (page == null) {
            return;
        }

        // Create and activate the view part.
        try {
            page.showView(viewPartIdentifier, null,
                    IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e) {
            statusHandler.error(
                    "ViewPartDelegatorView.<init>: Unable to show view part.",
                    e);
            return;
        }
        IViewPart viewPart = page.findView(viewPartIdentifier);
        if (viewPart == null) {
            statusHandler
                    .error("ViewPartDelegatorView.<init>: Unable to find view part.");
            return;
        } else if (viewPartClass.isAssignableFrom(viewPart.getClass()) == false) {
            statusHandler
                    .error("ViewPartDelegatorView.<init>: Could not find view part that was "
                            + "of type " + viewPartClass.getName() + ".");
            return;
        }
        this.viewPart = (V) viewPart;
    }

    /**
     * Hide the view part.
     * 
     * @param alreadyClosed
     *            Flag indicating whether or not the view part was already
     *            closed; if <code>true</code>, then this method does internal
     *            cleanup to ensure that this object knows that the close has
     *            occurred.
     */
    protected final void hideViewPart(boolean alreadyClosed) {

        // Remove the view part from the display. Exceptions
        // are caught in case CAVE is closing, since the work-
        // bench, window, page, or view part may not be around
        // at this point.
        if ((alreadyClosed == false) && (viewPart != null)) {
            try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage().hideView(viewPart);
                statusHandler
                        .debug("ViewPartDelegatorView.dispose(): Closing view part.");
            } catch (Exception e) {
                statusHandler
                        .info("ViewPartDelegatorView.dispose(): Could not close view: "
                                + e);
            }
        }

        // Forget the member data.
        viewPart = null;
    }

    /**
     * Set the part listener to that specified.
     * 
     * @param partListener
     *            Part listener to be used to listen for the delegate view
     *            part's changes, or <code>null<code> if no such listener
     *            is required.
     */
    protected final void setPartListener(IPartListener partListener) {
        IWorkbenchPage page = getActiveWorkbenchPage(true);
        if (page == null) {
            return;
        }
        removePartListener(page);
        this.partListener = partListener;
        if (partListener != null) {
            page.addPartListener(partListener);
        }
    }

    /**
     * Set the part listener (expanded edition) to that specified.
     * 
     * @param partListener
     *            Part listener to be used to listen for the delegate view
     *            part's changes, or <code>null<code> if no such listener
     *            is required.
     */
    protected final void setPartListener(IPartListener2 partListener) {
        IWorkbenchPage page = getActiveWorkbenchPage(true);
        if (page == null) {
            return;
        }
        removePartListener(page);
        this.partListener2 = partListener;
        if (partListener != null) {
            page.addPartListener(partListener);
        }
    }

    /**
     * Get the view part being managed
     * 
     * @return View part being managed, or <code>null</code> if no view part is
     *         currently instantiated.
     */
    protected final V getViewPart() {
        return viewPart;
    }

    /**
     * Get the reference to the view part being managed.
     * 
     * @return Reference to the view part being managed, or <code>null</code> if
     *         no view part is currently instantiated.
     */
    protected final IWorkbenchPartReference getViewPartReference() {
        if (viewPart == null) {
            return null;
        }
        return getActiveWorkbenchPage(true).getReference(viewPart);
    }

    /**
     * Get the active workbench page, if any.
     * 
     * @param notifyOfError
     *            Flag indicating whether or not to notify the user of any error
     *            that occurs when attempting to get the workbench page.
     * @return Active workbench page, or <code>null</code> if none is found.
     */
    protected final IWorkbenchPage getActiveWorkbenchPage(boolean notifyOfError) {

        // Get the active window.
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window == null) {
            if (notifyOfError) {
                statusHandler.error(
                        "ViewPartDelegatorView.<init>: PlatformUI.getWorkbench()."
                                + "getActiveWorkbenchWindow() returned null.",
                        new NullPointerException());
            }
            return null;
        }

        // Get the active page.
        IWorkbenchPage page = window.getActivePage();
        if ((page == null) && notifyOfError) {
            statusHandler
                    .error("ViewPartDelegatorView.<init>: PlatformUI.getWorkbench()."
                            + "getActiveWorkbenchWindow().getActivePage() returned null.",
                            new NullPointerException());
        }
        return page;
    }

    // Private Methods

    /**
     * Remove any part listener that has been installed.
     * 
     * @param page
     *            Active workbench page from which to remove the listener.
     * @param notifyOfError
     *            Flag indicating whether or not to notify the user of any error
     *            that occurs when attempting to remove the part listener.
     */
    private void removePartListener(IWorkbenchPage page) {
        if (partListener != null) {
            page.removePartListener(partListener);
        }
        if (partListener2 != null) {
            page.removePartListener(partListener2);
        }
    }
}
