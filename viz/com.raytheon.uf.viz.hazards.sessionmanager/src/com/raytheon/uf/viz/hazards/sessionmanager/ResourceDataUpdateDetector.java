/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager;

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;

import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;

/**
 * Description: Detector of a particular viz resource's data updates.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Apr 27, 2016   18266    Chris.Golden  Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ResourceDataUpdateDetector implements IResourceDataChanged {

    // Private Static Constants

    /**
     * Scheduler to be used to ensure that recommender executions are performed
     * on the main thread. For now, the main thread is the UI thread; when this
     * is changed, this will be rendered obsolete, as at that point there will
     * need to be a blocking queue of {@link Runnable} instances available to
     * allow the new worker thread to be fed jobs. At that point, this should be
     * replaced with an object that enqueues the <code>Runnable</code>s,
     * probably a singleton that may be accessed by the various components in
     * gov.noaa.gsd.viz.hazards and elsewhere (presumably passed to the session
     * manager when the latter is created).
     */
    @Deprecated
    private static final IRunnableAsynchronousScheduler RUNNABLE_ASYNC_SCHEDULER = new IRunnableAsynchronousScheduler() {

        @Override
        public void schedule(Runnable runnable) {

            /*
             * Since the UI thread is currently the thread being used for nearly
             * everything, just run any asynchronous tasks there.
             */
            VizApp.runAsync(runnable);
        }
    };

    // Private Variables

    /**
     * Viz resource for which listening for data updates is being done.
     */
    private final AbstractVizResource<?, ?> resource;

    /**
     * Session configuration manager.
     */
    private final ISessionConfigurationManager<?> configManager;

    /**
     * Time of the latest data available in this resource, as epoch time in
     * milliseconds.
     */
    private long latestDataTime;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param resource
     *            Viz resource for which listening for data updates is being
     *            done.
     * @param configManager
     *            Session configuration manager, used to execute recommenders
     *            when they are triggered by resource data updates.
     */
    public ResourceDataUpdateDetector(AbstractVizResource<?, ?> resource,
            ISessionConfigurationManager<?> configManager) {
        this.resource = resource;
        this.configManager = configManager;
        resource.getResourceData().addChangeListener(this);
        System.err
                .println("For data-time-triggered recommenders: "
                        + "Creating detector for data updates for "
                        + resource.getClass().getSimpleName()
                        + " ("
                        + ((resource.getDataTimes() == null)
                                || (resource.getDataTimes().length == 0) ? "none"
                                : resource.getDataTimes()[resource
                                        .getDataTimes().length - 1]) + ")");
        resourceChanged(ChangeType.DATA_UPDATE, null);
    }

    // Public Methods

    /**
     * Dispose of this detector.
     */
    public void dispose() {
        System.err.println("For data-time-triggered recommenders: "
                + "Removing detector for data updates for "
                + resource.getClass().getSimpleName());
        resource.getResourceData().removeChangeListener(this);
    }

    /**
     * Get the latest data time recorded.
     * 
     * @return Latest data time, in epoch time in milliseconds, or
     *         <code>0</code> if no data times have been recorded for this
     *         resource.
     */
    public long getLatestDataTime() {
        return latestDataTime;
    }

    @Override
    public void resourceChanged(ChangeType type, Object object) {
        if ((type == ChangeType.DATA_UPDATE) && updateLatestDataTime()) {
            System.err
                    .println("For data-time-triggered recommenders: "
                            + "Resource data updated: "
                            + resource.getClass().getSimpleName()
                            + " ("
                            + ((resource.getDataTimes() == null)
                                    || (resource.getDataTimes().length == 0) ? "none"
                                    : resource.getDataTimes()[resource
                                            .getDataTimes().length - 1]) + ")");
            RUNNABLE_ASYNC_SCHEDULER.schedule(new Runnable() {

                @Override
                public void run() {
                    configManager.triggerDataLayerChangeDrivenTool(resource
                            .getClass().getSimpleName());
                }
            });
        }
    }

    // Private Methods

    /**
     * Update the latest data time.
     * 
     * @return True if the latest time has changed from what was recorded
     *         previously.
     */
    private boolean updateLatestDataTime() {
        DataTime[] dataTimes = resource.getDataTimes();
        long lastTime = latestDataTime;
        latestDataTime = ((dataTimes == null) || (dataTimes.length == 0) ? 0
                : dataTimes[dataTimes.length - 1].getMatchRef());
        return (latestDataTime != lastTime);
    }
}