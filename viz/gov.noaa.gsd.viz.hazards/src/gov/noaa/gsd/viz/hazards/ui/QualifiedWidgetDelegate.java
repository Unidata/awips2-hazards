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

import gov.noaa.gsd.common.utilities.GetterRunnable;
import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedWidget;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Base class for qualified widget delegates, used to provide thread-safe access
 * to {@link Widget SWT widgets} and UI components that are composed of SWT
 * widgets. The generic parameter <code>Q</code> provides the type of widget
 * qualifier to be used, <code>I</code> provides the type of widget identifier
 * to be used, and <code>W</code> is the type of principal this delegate is to
 * represent.
 * <p>
 * Implementations of subclass methods that do not return values should create
 * {@link QualifiedPrincipalRunnableTask} instances that encapsulate whatever
 * task must be run on the principal, and then pass them to
 * {@link #runOrScheduleTask(QualifiedPrincipalRunnableTask)}. This will ensure
 * that such tasks are run on the main UI thread, and only when the principal
 * has become available.
 * </p>
 * <p>
 * Implementations of subclass methods that return values should create
 * {@link QualifiedPrincipalCallableTask} instances that encapsulate whatever
 * task must be run on the principal and whatever value must be returned, and
 * then pass them to {@link #callTask(QualifiedPrincipalCallableTask)}. In
 * contrast to <code>
 * runOrScheduleTask()</code>, <code>callTask()</code> executes the task
 * synchronously on the main UI thread, but only if the principal exists; if it
 * does not, it simply returns <code>null</code> instead of scheduling execution
 * to occur after the principal becomes available.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 15, 2014    4243    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class QualifiedWidgetDelegate<Q, I, W extends IQualifiedWidget<Q, I>>
        implements IQualifiedWidget<Q, I> {

    // Private Static Constants

    /**
     * Status handler.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(QualifiedWidgetDelegate.class);

    // Private Constants

    /**
     * Widget delegate helper.
     */
    private final IQualifiedWidgetDelegateHelper<Q, I, W> helper;

    /**
     * Handler invocation scheduler.
     */
    private final IRunnableAsynchronousScheduler handlerScheduler;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param helper
     *            Widget delegate helper.
     * @param handlerScheduler
     *            Handler invocation scheduler.
     */
    public QualifiedWidgetDelegate(
            IQualifiedWidgetDelegateHelper<Q, I, W> helper,
            IRunnableAsynchronousScheduler handlerScheduler) {
        this.helper = helper;
        this.handlerScheduler = handlerScheduler;
    }

    // Public Methods

    @Override
    public void setEnabled(final Q qualifier, final I identifier,
            final boolean enable) {
        runOrScheduleTask(new QualifiedPrincipalRunnableTask<Q, I, W>() {

            @Override
            public void run() {
                getPrincipal().setEnabled(qualifier, identifier, enable);
            }
        });
    }

    // Protected Methods

    /**
     * Run the specified principal task only after the principal exists, meaning
     * it will be run very soon (though asynchronously with respect to this
     * method invocation) if the principal exists when this method is called, or
     * later on if the principal does not yet exist. Whenever the task is run,
     * the execution occurs on the main UI thread. It is run at most one time.
     * 
     * @param task
     *            Task to be run.
     */
    protected final void runOrScheduleTask(
            final QualifiedPrincipalRunnableTask<Q, I, W> task) {
        runOrScheduleTask(task, false);
    }

    /**
     * Run the specified principal task only after the principal exists, meaning
     * it will be run very soon (though asynchronously with respect to this
     * method invocation) if the principal exists when this method is called, or
     * later on if the principal does not yet exist. Whenever the task is run,
     * the execution occurs on the main UI thread.
     * <p>
     * Additionally, if the task is to persist, attempt to run it each time the
     * view is created; this serves to ensure that tasks that must be executed
     * each time a view is instantiated (for example, the registration of event
     * handlers) will be able to do so.
     * </p>
     * 
     * @param task
     *            Task to be run.
     * @param persist
     *            Flag indicating whether or not the task should be run for each
     *            time a view is created. If <code>false</code>, the task is run
     *            a single time.
     */
    protected final void runOrScheduleTask(
            final QualifiedPrincipalRunnableTask<Q, I, W> task,
            final boolean persist) {

        /*
         * Create a wrapper for the task that can be run synchronously or
         * asynchronously.
         */
        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {

                /*
                 * If the principal is found to exist, run the task immediately,
                 * and if it is to persist, schedule it to be run each time a
                 * view is created. If the principal does not exist, schedule it
                 * to run, either each time the view is created if it should
                 * persist, or just once after the first view creation if not.
                 */
                W principal = helper.getPrincipal();
                if (principal != null) {
                    task.setPrincipal(principal);
                    task.run();
                    if (persist) {
                        helper.scheduleTaskForEachViewCreation(task);
                    }
                } else if (persist) {
                    helper.scheduleTaskForEachViewCreation(task);
                } else {
                    helper.scheduleTask(task);
                }
            }
        };

        /*
         * If this thread is the main UI thread, run the wrapper immediately and
         * synchronously; otherwise, schedule it to be run on the main UI thread
         * asynchronously.
         */
        if (Display.getDefault().getThread() == Thread.currentThread()) {
            taskWrapper.run();
        } else {
            Display.getDefault().asyncExec(taskWrapper);
        }
    }

    /**
     * Call the specified principal task synchronously on the main UI thread,
     * but only if the principal exists.
     * 
     * @param task
     *            Task to be run.
     * @return Return value of the task if the principal exists, or
     *         <code>null</code> if it does not.
     */
    protected final <R> R callTask(
            final QualifiedPrincipalCallableTask<Q, I, W, R> task) {

        /*
         * Create a wrapper for the task that can be run on the main UI thread
         * if that is not the current thread.
         */
        GetterRunnable<R> taskWrapper = new GetterRunnable<R>() {
            @Override
            public void run() {

                /*
                 * If the principal exists, call the task and return the result;
                 * otherwise, return nothing.
                 */
                try {
                    W principal = helper.getPrincipal();
                    if (principal != null) {
                        task.setPrincipal(principal);
                        setResult(task.call());
                    }
                } catch (Exception e) {
                    statusHandler
                            .error("unexpected error when calling task upon principal",
                                    e);
                }
            }
        };

        /*
         * Run the wrapper synchronously on the main UI thread and return the
         * result.
         */
        if (Display.getDefault().getThread() == Thread.currentThread()) {
            taskWrapper.run();
        } else {
            Display.getDefault().syncExec(taskWrapper);
        }
        return taskWrapper.getResult();
    }

    /**
     * Get the runnable asynchronous scheduler that is to be used to schedule
     * invocations of the principal's handler (either
     * {@link ICommandInvocationHandler} or {@link IStateChangeHandler}).
     * 
     * @return Runnable asynchronous scheduler.
     */
    protected final IRunnableAsynchronousScheduler getHandlerInvocationScheduler() {
        return handlerScheduler;
    }
}
