/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.pythonjoblistener;

import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction.ToolActionEnum;
import gov.noaa.gsd.viz.mvp.EventBusSingleton;

import java.util.List;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.VizApp;

/**
 * Description: This listens for the results from an asynchronous run of a
 * python recommender.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 19, 2013            Bryon.Lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class HazardServicesGeneratorJobListener implements
        IPythonJobListener<List<IGeneratedProduct>> {
    private final String toolID;

    /**
     * For logging...
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesGeneratorJobListener.class);

    /**
     * @param toolID
     *            The name of the tool which produced this recommendation.
     */
    public HazardServicesGeneratorJobListener(String toolID) {
        this.toolID = toolID;
    }

    /**
     * Receives the results from a successful run of a recommender and posts
     * them to the event bus.
     * 
     * @param result
     *            A list of recommended events from the recommender.
     */
    @Override
    public void jobFinished(final List<IGeneratedProduct> result) {

        /*
         * Need to place the result on the thread the Session Manager is
         * running. At the moment this is the UI thread.
         */
        VizApp.runAsync(new Runnable() {

            @Override
            public void run() {
                EventBus eventBus = EventBusSingleton.getInstance();

                ToolAction action = new ToolAction(
                        ToolActionEnum.PRODUCTS_GENERATED, toolID, result);
                eventBus.post(action);
            }
        });
    }

    /**
     * Handles the case where a recommender failed.
     * 
     * @param e
     *            The exception describing why the recommender failed.
     */
    @Override
    public void jobFailed(Throwable e) {
        statusHandler.error("Product Generator " + toolID + " failed.", e);
    }
}