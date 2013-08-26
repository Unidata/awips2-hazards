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

import java.util.List;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

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
 * Aug 26, 2013    1921    Bryon.Lawrence    Removed call to VizApp.runAsync in 
 *                                           jobFinished.   This is already being 
 *                                           called in SessionProductManager 
 *                                           jobFinished().
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class HazardServicesGeneratorJobListener implements
        IPythonJobListener<List<IGeneratedProduct>> {
    private final EventBus eventBus;

    private final String toolID;

    /**
     * For logging...
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesGeneratorJobListener.class);

    /**
     * @param eventBus
     *            Event bus to use to transmit messages.
     * @param toolID
     *            The name of the tool which produced this recommendation.
     */
    public HazardServicesGeneratorJobListener(EventBus eventBus, String toolID) {
        this.eventBus = eventBus;
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

        ToolAction action = new ToolAction(ToolActionEnum.PRODUCTS_GENERATED,
                toolID, result);
        eventBus.post(action);
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
