/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display.test;

import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.SpatialDisplayAction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;

/**
 * Description: {@link FunctionalTest} of the spatial display context menu.
 * 
 * This tests to ensure that the Add/Remove Shapes option is displayed in the
 * Spatial Display right click context menu when a draw-by-area hazard is
 * created.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 23, 2013 2474       blawrenc    Initial Coding
 * Apr 09, 2014    2925    Chris.Golden Fixed to work with new HID event propagation.
 * May 18, 2014    2925    Chris.Golden More changes to get it to work with the new HID.
 *                                      Also changed to ensure that ongoing preview and
 *                                      ongoing issue flags are set to false at the end
 *                                      of each test, and moved the steps enum into the
 *                                      base class.
 * </pre>
 * 
 * @author blawrenc
 * @version 1.0
 */
public class ContextMenuFunctionalTest extends
        FunctionalTest<ContextMenuFunctionalTest.Steps> {

    @SuppressWarnings("unused")
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    private static final double FIRST_EVENT_CENTER_Y = 41.0;

    private static final double FIRST_EVENT_CENTER_X = -96.0;

    private static final double SECOND_EVENT_CENTER_Y = 41.0;

    private static final double SECOND_EVENT_CENTER_X = -96.0;

    /**
     * Steps defining this test.
     */
    protected enum Steps {
        START, CHECK_CONTEXT_MENU_FOR_ADD_REMOVE_SHAPE,

        CREATE_NEW_NODE_HAZARD_AREA
    }

    public ContextMenuFunctionalTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Override
    protected void runFirstStep() {

        /*
         * Create a new hazard area.
         */
        this.step = Steps.START;
        autoTestUtilities.createEvent(FIRST_EVENT_CENTER_X,
                FIRST_EVENT_CENTER_Y);
    }

    /**
     * Listens for spatial display actions generated from within Hazard
     * Services. Performs the appropriate tests based on the current test step.
     * 
     * @param spatialDisplayAction
     *            The spatial display action
     * @return
     */
    @Handler(priority = -1)
    public void spatialDisplayActionOccurred(
            final SpatialDisplayAction spatialDisplayAction) {

        try {
            stepCompleted();
            this.step = Steps.CREATE_NEW_NODE_HAZARD_AREA;

            /*
             * Create a new non-draw-by-area event.
             */
            autoTestUtilities.createEvent(SECOND_EVENT_CENTER_X,
                    SECOND_EVENT_CENTER_Y);

        } catch (Exception e) {
            handleException(e);
        }
    }

    @Handler(priority = -1)
    public void handleNewHazard(SessionEventAdded action) {
        try {
            switch (step) {

            case START:

                stepCompleted();
                this.step = Steps.CHECK_CONTEXT_MENU_FOR_ADD_REMOVE_SHAPE;

                /*
                 * Update the event to indicate that it was created using the
                 * draw-by-area tool.
                 */
                Map<String, Serializable> newEventAttributes = new HashMap<>();
                ArrayList<String> contextMenuList = new ArrayList<>();
                contextMenuList
                        .add(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES);
                newEventAttributes.put(
                        HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY,
                        contextMenuList);
                autoTestUtilities
                        .updateSelectedEventAttributes(newEventAttributes);
                break;

            case CREATE_NEW_NODE_HAZARD_AREA:
                List<String> contextMenuEntries = convertContextMenuToString(this.toolLayer
                        .getContextMenuActions());

                assertTrue(!contextMenuEntries
                        .contains(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES));
                stepCompleted();
                this.testSuccess();
                break;

            default:
                testError();
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
