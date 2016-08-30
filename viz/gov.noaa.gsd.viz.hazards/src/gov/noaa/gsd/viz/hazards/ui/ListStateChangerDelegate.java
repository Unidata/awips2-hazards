/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.ui;

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChanger;

import java.util.List;

import org.eclipse.swt.widgets.Widget;

/**
 * A list state changer delegate, used to provide thread-safe access to list
 * state changers that are {@link Widget SWT widgets}, or are composed of SWT
 * widgets. The generic parameter <code>I</code> provides the type of list state
 * changer identifier to be used, <code>E</code> provides the type of elements
 * within the lists, and <code>W</code> is the type of principal this delegate
 * is to represent.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 22, 2016   19537    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ListStateChangerDelegate<I, E, W extends IListStateChanger<I, E>>
        extends WidgetDelegate<I, W> implements IListStateChanger<I, E> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param helper
     *            Widget delegate helper.
     * @param handlerScheduler
     *            Handler invocation scheduler.
     */
    public ListStateChangerDelegate(IWidgetDelegateHelper<I, W> helper,
            IRunnableAsynchronousScheduler handlerScheduler) {
        super(helper, handlerScheduler);
    }

    // Public Methods

    @Override
    public void setEditable(final I identifier, final boolean editable) {
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().setEditable(identifier, editable);
            }
        });
    }

    @Override
    public List<E> get(final I identifier) {
        return callTask(new PrincipalCallableTask<I, W, List<E>>() {

            @Override
            public List<E> call() throws Exception {
                return getPrincipal().get(identifier);
            }
        });
    }

    @Override
    public void clear(final I identifier) {
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().clear(identifier);
            }
        });
    }

    @Override
    public void set(final I identifier, final List<? extends E> elements) {
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().set(identifier, elements);
            }
        });
    }

    @Override
    public void addElement(final I identifier, final E element) {
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().addElement(identifier, element);
            }
        });
    }

    @Override
    public void addElements(final I identifier, final List<? extends E> elements) {
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().addElements(identifier, elements);
            }
        });
    }

    @Override
    public void insertElement(final I identifier, final int index,
            final E element) {
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().insertElement(identifier, index, element);
            }
        });
    }

    @Override
    public void insertElements(final I identifier, final int index,
            final List<? extends E> elements) {
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().insertElements(identifier, index, elements);
            }
        });
    }

    @Override
    public void replaceElement(final I identifier, final int index,
            final E element) {
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().replaceElement(identifier, index, element);
            }
        });
    }

    @Override
    public void replaceElements(final I identifier, final int index,
            final int count, final List<? extends E> elements) {
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().replaceElements(identifier, index, count,
                        elements);
            }
        });
    }

    @Override
    public void removeElement(final I identifier, final int index) {
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().removeElement(identifier, index);
            }
        });
    }

    @Override
    public void removeElements(final I identifier, final int index,
            final int count) {
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().removeElements(identifier, index, count);
            }
        });
    }

    @Override
    public void setListStateChangeHandler(
            final IListStateChangeHandler<I, E> handler) {

        /*
         * Since handlers must be registered with the current view at all times,
         * persist the registration task so that it is executed each time the
         * view is (re)created.
         */
        runOrScheduleTask(new PrincipalRunnableTask<I, W>() {

            @Override
            public void run() {
                getPrincipal().setListStateChangeHandler(
                        new ListStateChangeHandlerDelegate<I, E>(handler,
                                getHandlerInvocationScheduler()));
            }
        }, true);
    }
}
