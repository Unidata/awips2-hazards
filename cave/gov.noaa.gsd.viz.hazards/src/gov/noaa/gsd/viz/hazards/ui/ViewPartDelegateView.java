/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.ui;

import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.mvp.IView;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.part.ViewPart;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * View that delegates to an Eclipse view part.
 * <p>
 * The managed view part, specified by the view part identifier and class
 * parameters passed to this class's constructor, is to be shown via the
 * {@link #showViewPart()}, and hidden via the {@link #hideViewPart(boolean)}
 * method. Any manipulation of the view part, including the invocation of this
 * class's protected methods that manipulate said view part, should not be
 * called directly; instead such calls should occur within a {@link Runnable}
 * that is passed to {@link #executeOnCreatedViewPart(Runnable)}.
 * </p>
 * <p>
 * This is the case because only at most one instance of a view part with a
 * particular identifier managed by this view is allowed to exist at any given
 * time, and if two Hazard Services instances exist simultaneously, and one
 * instance has a view part showing, the other instance must wait until that
 * view part disappears before showing its own. By encapsulating any view part
 * manipulation within a {@link Runnable} and passing it to an invocation of
 * <code>executeOnCreatedViewPart()</code>, such manipulation may be delayed
 * until the view part is created for this view, and then executed once it is
 * created.
 * </p>
 * <p>
 * The exception to the above rule is if this view knows for certain that it has
 * a view part it is managing, in which case direct manipulation may occur.
 * </p>
 * <p>
 * TODO: Remove dependency on {@link RCPMainUserInterfaceElement} in order to
 * move this package to its own plugin.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 08, 2013            Chris.Golden Initial creation
 * Jul 12, 2013    585     Chris.Golden Changed to support loading from bundle.
 * Aug 22, 2013   1936     Chris.Golden Added console countdown timers.
 * May 08, 2014   2925     Chris.Golden Added notification callbacks to creation
 *                                      listeners, and moved to its own package.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@SuppressWarnings("restriction")
public abstract class ViewPartDelegateView<V extends ViewPart> implements
        IView<Action, RCPMainUserInterfaceElement> {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ViewPartDelegateView.class);

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
     * View part creation listeners.
     */
    private final Set<IViewPartCreationListener<V>> viewPartCreationListeners = new HashSet<>();

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

    /**
     * Perspective listener; this exists only if {@link #showViewPart()} is
     * called and an already-existing view part with the same identifier as
     * {@link #viewPartIdentifier} is found. In this case, this listener is used
     * to determine when the old view part is closed, so that the new one may be
     * opened. If such a notification occurs, <code>showViewPart()</code> is
     * called, and then any jobs found in the list referenced by
     * {@link #jobsToExecuteWhenViewPartCreated} must then be executed.
     */
    private IPerspectiveListener2 perspectiveListener2 = null;

    /**
     * Queue of runnables to execute if {@link #perspectiveListener2} is
     * notified of an old view part belonging to another instance of Hazard
     * Services closing. This is <code>null</code> if the former member variable
     * is <code>null</code>.
     */
    private Queue<Runnable> jobsToExecuteWhenViewPartCreated = null;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param viewPartIdentifier
     *            Identifier of the view part to which to delegate.
     * @param viewPartClass
     *            Class of the view part to which to delegate.
     */
    public ViewPartDelegateView(String viewPartIdentifier,
            Class<V> viewPartClass) {
        this.viewPartIdentifier = viewPartIdentifier;
        this.viewPartClass = viewPartClass;
    }

    // Public Methods

    @Override
    public void dispose() {

        /*
         * Remove the perspective listener and queued view part jobs, if any.
         */
        removePerspectiveListenerAndQueuedViewPartJobs(false);

        /*
         * Remove any part listener.
         */
        IWorkbenchPage page = getActiveWorkbenchPage(false);
        if (page != null) {
            removePartListener(page);
        }

        /*
         * Hide the view part.
         */
        hideViewPart(false);
    }

    @Override
    public abstract List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type);

    /**
     * Add the specified view part creation listener, to be notified whenever
     * the view part is created.
     * 
     * @param creationListener
     *            View part creation listener, to be notified if a view part
     *            creation occurs.
     */
    public void addCreationListener(
            IViewPartCreationListener<V> creationListener) {
        viewPartCreationListeners.add(creationListener);
    }

    // Protected Methods

    /**
     * Respond to an attempt to execute some action via
     * {@link #executeOnCreatedViewPart(Runnable)} upon a view part when the
     * view part is not in existence and no attempt has been made to create it.
     * Some subclasses may want to disallow such attempts by throwing an
     * exception to indicate that an error occurred; others may not care.
     * 
     * @param job
     *            Action for which execution was attempted.
     */
    protected abstract void actionExecutionAttemptedUponNonexistentViewPart(
            Runnable job);

    /**
     * Show the view part.
     * 
     * @return True if the view part was shown, false otherwise. A view part may
     *         legitimately be left invisible by an invocation of this method if
     *         an existing view part is found, indicating that a previous
     *         instance of Hazard Services is still running. A view part may
     *         also be left invisible if the active workbench window or page
     *         could not be found, in which case an error will have been logged.
     */
    protected final boolean showViewPart() {

        /*
         * Do nothing if the view part is already showing.
         */
        if (viewPart != null) {
            return true;
        }

        /*
         * Remove any perspective listener and queued view part jobs.
         */
        removePerspectiveListenerAndQueuedViewPartJobs(true);

        /*
         * Get the active workbench page.
         */
        IWorkbenchPage page = getActiveWorkbenchPage(true);
        if (page == null) {
            return false;
        }

        /*
         * If an existing view part with the same identifier is found, this
         * means that a previous Hazard Services instance is still around. In
         * this case, register a perspective listener to notify this object when
         * the old view part goes away so that the new view part can be
         * instantiated.
         */
        IViewPart oldViewPart = page.findView(viewPartIdentifier);
        if (oldViewPart != null) {
            IWorkbenchWindow window = getActiveWorkbenchWindow(true);
            perspectiveListener2 = new PerspectiveAdapter() {
                @Override
                public void perspectiveChanged(IWorkbenchPage page,
                        IPerspectiveDescriptor perspective,
                        IWorkbenchPartReference partRef, String changeId) {
                    if (changeId.equals(IWorkbenchPage.CHANGE_VIEW_HIDE)
                            && partRef.getId().equals(viewPartIdentifier)) {

                        /*
                         * Schedule the load of the new view part to occur
                         * later, since the unload of the previous one will not
                         * have been completed when this listener fires.
                         */
                        Display.getCurrent().asyncExec(new Runnable() {
                            @Override
                            public void run() {

                                /*
                                 * Attempt to create the view part, and if it
                                 * gets created, iterate through the jobs to be
                                 * executed now that the view part exists,
                                 * running each in turn. Do the iteration over a
                                 * copy of the queue, in case one of the jobs
                                 * clears the original queue. Then notify the
                                 * listener, if any, of the deferred creation.
                                 */
                                IWorkbenchPage page = getActiveWorkbenchPage(true);
                                if (createViewPart(page)) {
                                    Runnable job;
                                    Queue<Runnable> jobsQueueCopy = new LinkedList<>(
                                            jobsToExecuteWhenViewPartCreated);
                                    while ((job = jobsQueueCopy.poll()) != null) {
                                        job.run();
                                    }
                                    notifyCreationListeners(true);
                                }

                                /*
                                 * Remove the perspective listener and any
                                 * queued view part jobs, since it is no longer
                                 * needed now that the old view part has been
                                 * removed.
                                 */
                                removePerspectiveListenerAndQueuedViewPartJobs(false);
                            }
                        });
                    }
                }
            };
            jobsToExecuteWhenViewPartCreated = new LinkedList<>();
            window.addPerspectiveListener(perspectiveListener2);
            return false;
        }

        /*
         * Create the view part.
         */
        return createViewPart(page);
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

        /*
         * Remove any perspective listener and queued view part jobs.
         */
        removePerspectiveListenerAndQueuedViewPartJobs(true);

        /*
         * Remove the view part from the display. Exceptions are caught in case
         * CAVE is closing, since the workbench, window, page, or view part may
         * not be around at this point.
         */
        if ((alreadyClosed == false) && (viewPart != null)) {
            try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage().hideView(viewPart);
                statusHandler.debug("Closing view part.");
            } catch (Exception e) {
                statusHandler.info("Could not close view: " + e);
            }
        }

        /*
         * Forget the member data.
         */
        viewPart = null;
    }

    /**
     * Execute the specified view-part-related action if the view part has been
     * created, enqueuing it to be executed after creation if the creation has
     * been delayed by an existing view part blocking said creation.
     * 
     * @param job
     *            Runnable to be executed.
     * @throws IllegalStateException
     *             If no view part showing has been attempted by this view.
     */
    protected final void executeOnCreatedViewPart(Runnable job) {
        if (viewPart == null) {
            if (jobsToExecuteWhenViewPartCreated != null) {
                jobsToExecuteWhenViewPartCreated.add(job);
            } else {
                actionExecutionAttemptedUponNonexistentViewPart(job);
            }
        } else if (Display.getDefault().getThread() == Thread.currentThread()) {
            job.run();
        } else {
            Display.getDefault().syncExec(job);
        }
    }

    /**
     * Change the view part's visibility.
     * <p>
     * <strong>Note</strong>: This method should not be called directly; it is
     * meant to be used as a helper method, and should be encapsulated within a
     * {@link Runnable} that is then passed as a parameter to
     * {@link #executeOnCreatedViewPart(Runnable)}.
     * 
     * @param visible
     *            Flag indicating whether or not the view should be visible.
     */
    protected final void setViewPartVisible(final boolean visible) {
        getActiveWorkbenchPage(true).setPartState(
                getViewPartReference(),
                (visible ? WorkbenchPage.STATE_RESTORED
                        : WorkbenchPage.STATE_MINIMIZED));
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
     * Get the view part being managed.
     * <p>
     * <strong>Note</strong>: The return value may be <code>null</code> even if
     * {@link #showViewPart()} has been called, due to delayed instantiation of
     * the view part because another view part with the same identifier was
     * found to exist.
     * 
     * @return View part being managed, or <code>null</code> if no view part is
     *         currently instantiated for this view.
     */
    protected final V getViewPart() {
        return viewPart;
    }

    /**
     * Get the reference to the view part being managed.
     * 
     * @return Reference to the view part being managed, or <code>null</code> if
     *         no view part is currently instantiated for this view.
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

        /*
         * Get the active window.
         */
        IWorkbenchWindow window = getActiveWorkbenchWindow(notifyOfError);
        if (window == null) {
            return null;
        }

        /*
         * Get the active page.
         */
        IWorkbenchPage page = window.getActivePage();
        if ((page == null) && notifyOfError) {
            statusHandler.error("window.getActivePage() returned null.",
                    new NullPointerException());
        }
        return page;
    }

    /**
     * Get the active workbench window, if any.
     * 
     * @param notifyOfError
     *            Flag indicating whether or not to notify the user of any error
     *            that occurs when attempting to get the workbench window.
     * @return Active workbench window, or <code>null</code> if none is found.
     */
    protected final IWorkbenchWindow getActiveWorkbenchWindow(
            boolean notifyOfError) {
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (window == null) {
            if (notifyOfError) {
                statusHandler
                        .error("PlatformUI.getWorkbench().getActiveWorkbenchWindow() returned null.",
                                new NullPointerException());
            }
            return null;
        }
        return window;
    }

    // Private Methods

    /**
     * Create the view part.
     * 
     * @param page
     *            Active workbench page from which to remove the listener.
     * @return True if the view part was created, false otherwise.
     */
    @SuppressWarnings("unchecked")
    private boolean createViewPart(IWorkbenchPage page) {

        /*
         * Create and activate the view part.
         */
        try {
            page.showView(viewPartIdentifier, null,
                    IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e) {
            statusHandler.error("Unable to show view part.", e);
            return false;
        }
        IViewPart viewPart = page.findView(viewPartIdentifier);
        if (viewPart == null) {
            statusHandler.error("Unable to find view part.");
            return false;
        } else if (viewPartClass.isAssignableFrom(viewPart.getClass()) == false) {
            statusHandler.error("Could not find view part that was "
                    + "of type " + viewPartClass.getName() + ".");
            return false;
        }
        this.viewPart = (V) viewPart;
        notifyCreationListeners(false);
        return true;
    }

    /**
     * Notify any registered view part creation listeners.
     * 
     * @param deferred
     *            Flag indicating whether the creation for which the
     *            notification is being generated was deferred.
     */
    private void notifyCreationListeners(boolean deferred) {
        for (IViewPartCreationListener<V> listener : viewPartCreationListeners) {
            listener.viewPartCreated(viewPart, deferred);
        }
    }

    /**
     * Remove any part listener that has been installed.
     * 
     * @param page
     *            Active workbench page from which to remove the listener.
     */
    private void removePartListener(IWorkbenchPage page) {
        if (partListener != null) {
            page.removePartListener(partListener);
        }
        if (partListener2 != null) {
            page.removePartListener(partListener2);
        }
    }

    /**
     * Remove any perspective listener that has been installed.
     * 
     * @param notifyOfError
     *            Flag indicating whether or not to notify the user of any error
     *            that occurs when attempting to get the workbench window.
     */
    private void removePerspectiveListenerAndQueuedViewPartJobs(
            boolean notifyOfError) {
        if (perspectiveListener2 != null) {
            IWorkbenchWindow window = getActiveWorkbenchWindow(notifyOfError);
            if (window != null) {
                window.removePerspectiveListener(perspectiveListener2);
                perspectiveListener2 = null;
            }
            jobsToExecuteWhenViewPartCreated = null;
        }
    }
}
