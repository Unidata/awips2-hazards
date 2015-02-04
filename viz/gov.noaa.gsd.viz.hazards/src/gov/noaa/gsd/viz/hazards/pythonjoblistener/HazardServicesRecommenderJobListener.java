/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.pythonjoblistener;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction.RecommenderActionEnum;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;

/**
 * Description: This listens for the results from an asynchronous run of a
 * python recommender.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 19, 2013            Bryon.Lawrence    Initial creation
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle.
 * Jan 29, 2015 4375       Dan Schaffer      Console initiation of RVS product generation
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class HazardServicesRecommenderJobListener implements
        IPythonJobListener<EventSet<IEvent>> {
    private final BoundedReceptionEventBus<Object> eventBus;

    private final Tool tool;

    /**
     * For logging...
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesRecommenderJobListener.class);

    /**
     * @param eventBus
     *            Event bus to use to transmit messages.
     * @param tool
     *            The tool which produced this recommendation.
     */
    public HazardServicesRecommenderJobListener(
            BoundedReceptionEventBus<Object> eventBus, Tool tool) {
        this.eventBus = eventBus;
        this.tool = tool;
    }

    /**
     * Receives the results from a successful run of a recommender and posts
     * them to the event bus.
     * 
     * @param result
     *            A list of recommended events from the recommender.
     */
    @Override
    public void jobFinished(final EventSet<IEvent> result) {
        ToolAction action = new ToolAction(
                RecommenderActionEnum.RECOMMENDATIONS, result, tool);
        eventBus.publishAsync(action);
    }

    /**
     * Handles the case where a recommender failed.
     * 
     * @param e
     *            The exception describing why the recommender failed.
     */
    @Override
    public void jobFailed(Throwable e) {
        statusHandler.error("Recommender " + tool + " failed.", e);
    }
}
