/**
Qualified * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.ui;

import gov.noaa.gsd.viz.mvp.widgets.IQualifiedWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.ui.part.ViewPart;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Description: Qualified widget delegate helper for widgets used by a
 * {@link ViewPart}, intended for use within a {@link ViewPartDelegateView}
 * object to access the ones in the view part. The generic parameter
 * <code>Q</code> provides the type of widget qualifier to be used,
 * <code>I</code> provides the type of widget identifier to be used,
 * <code>V</code> is the type of view part in which the principal widgets are
 * found, and <code>W</code> is the type of principal this delegate is to
 * represent.
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
public class ViewPartQualifiedWidgetDelegateHelper<Q, I, V extends ViewPart, W extends IQualifiedWidget<Q, I>>
        implements IQualifiedWidgetDelegateHelper<Q, I, W> {

    // Private Static Constants

    /**
     * Status handler.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ViewPartQualifiedWidgetDelegateHelper.class);

    // Private Variables

    /**
     * Fetcher of the principal represented by this delegate.
     */
    private final Callable<W> principalFetcher;

    /**
     * View part delegate that is to use this object.
     */
    private final ViewPartDelegateView<V> delegateView;

    /**
     * List of tasks to be executed whenever notification is received that a
     * view part was created.
     */
    private final List<QualifiedPrincipalRunnableTask<Q, I, W>> tasksForEachViewPartCreation = new ArrayList<>();

    /**
     * View part creation listener used to execute any tasks that are to be run
     * each time a view part is created.
     */
    private final IViewPartCreationListener<V> creationListener = new IViewPartCreationListener<V>() {

        @Override
        public void viewPartCreated(V viewPart, boolean deferred) {
            for (QualifiedPrincipalRunnableTask<Q, I, W> task : tasksForEachViewPartCreation) {
                executeTask(task);
            }
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param principalFetcher
     *            The fetcher for the principal represented by this delegate.
     * @param delegateView
     *            The view part delegate view that is to use this delegate.
     */
    public ViewPartQualifiedWidgetDelegateHelper(Callable<W> principalFetcher,
            ViewPartDelegateView<V> delegateView) {
        this.principalFetcher = principalFetcher;
        this.delegateView = delegateView;
        delegateView.addCreationListener(creationListener);
    }

    // Public Methods

    @Override
    public final W getPrincipal() {
        if (delegateView.getViewPart() != null) {
            try {
                return principalFetcher.call();
            } catch (NullPointerException e) {

                /*
                 * No action; this could occur because the view part disappeared
                 * the call to getViewPart() above and the call to it within the
                 * principalFetcher. The overhead of having to synchronize is
                 * avoided by catching the edge case NullPointerExceptions and
                 * silently falling through to return null.
                 */
            } catch (Exception e) {
                statusHandler
                        .error("unexpected error when fetching principal for delegate",
                                e);
            }
        }
        return null;
    }

    @Override
    public final void scheduleTask(
            final QualifiedPrincipalRunnableTask<Q, I, W> task) {
        delegateView.executeOnCreatedViewPart(new Runnable() {
            @Override
            public void run() {
                executeTask(task);
            }
        });
    }

    @Override
    public void scheduleTaskForEachViewCreation(
            QualifiedPrincipalRunnableTask<Q, I, W> task) {
        tasksForEachViewPartCreation.add(task);
    }

    // Private Methods

    /**
     * Execute the specified task synchronously using the principal. The latter
     * must be accessible via {@link #getPrincipal()}.
     * 
     * @param task
     *            Task to be executed.
     */
    private void executeTask(QualifiedPrincipalRunnableTask<Q, I, W> task) {
        task.setPrincipal(getPrincipal());
        task.run();
    }
}
