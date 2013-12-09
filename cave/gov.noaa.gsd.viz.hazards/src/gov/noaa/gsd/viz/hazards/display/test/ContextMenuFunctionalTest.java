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
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;

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
 * </pre>
 * 
 * @author blawrenc
 * @version 1.0
 */
public class ContextMenuFunctionalTest extends FunctionalTest {

    private static final double FIRST_EVENT_CENTER_Y = 41.0;

    private static final double FIRST_EVENT_CENTER_X = -96.0;

    private static final double SECOND_EVENT_CENTER_Y = 41.0;

    private static final double SECOND_EVENT_CENTER_X = -96.0;

    /**
     * Steps defining this test.
     */
    private enum Steps {
        CREATE_NEW_DRAW_BY_AREA_HAZARD, CHECK_CONTEXT_MENU_FOR_ADD_REMOVE_SHAPE, CREATE_NEW_NODE_HAZARD_AREA, CHECK_CONTEXT_MENU_FOR_NO_ADD_REMOVE_SHAPE
    }

    /**
     * The current step being tested.
     */
    private Steps step;

    public ContextMenuFunctionalTest(HazardServicesAppBuilder appBuilder) {
        super(appBuilder);
    }

    @Override
    protected void run() {
        try {
            super.run();

            /*
             * Create a new hazard area.
             */
            this.step = Steps.CREATE_NEW_DRAW_BY_AREA_HAZARD;
            autoTestUtilities.createEvent(FIRST_EVENT_CENTER_X,
                    FIRST_EVENT_CENTER_Y);
        } catch (Exception e) {
            handleException(e);
        }

    }

    /**
     * Listens for spatial display actions generated from within Hazard
     * Services. Performs the appropriate tests based on the current test step.
     * 
     * @param spatialDisplayAction
     *            The spatial display action
     * @return
     */
    @Subscribe
    public void spatialDisplayActionOccurred(
            final SpatialDisplayAction spatialDisplayAction) {

        try {
            String actionType = spatialDisplayAction.getActionType();

            if (actionType.equals(HazardConstants.NEW_EVENT_SHAPE)) {
                if (this.step == Steps.CREATE_NEW_DRAW_BY_AREA_HAZARD) {
                    this.step = Steps.CHECK_CONTEXT_MENU_FOR_ADD_REMOVE_SHAPE;

                    /*
                     * Update the event to indicate that it was created using
                     * the draw-by-area tool.
                     */
                    Dict newEventAttributes = new Dict();
                    ArrayList<String> contextMenuList = Lists.newArrayList();
                    contextMenuList
                            .add(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES);
                    newEventAttributes.put(
                            HazardConstants.CONTEXT_MENU_CONTRIBUTION_KEY,
                            contextMenuList);
                    autoTestUtilities
                            .updateSelectedEventAttributes(newEventAttributes);
                } else if (this.step == Steps.CREATE_NEW_NODE_HAZARD_AREA) {
                    this.step = Steps.CHECK_CONTEXT_MENU_FOR_NO_ADD_REMOVE_SHAPE;
                    List<String> contextMenuEntries = this.toolLayer
                            .getContextMenuEntries();

                    assertTrue(!contextMenuEntries
                            .contains(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES));
                    this.testSuccess();
                }
            } else if (actionType.equals(HazardConstants.UPDATE_EVENT_METADATA)) {
                this.step = Steps.CREATE_NEW_NODE_HAZARD_AREA;
                List<String> contextMenuEntries = this.toolLayer
                        .getContextMenuEntries();

                assertTrue(contextMenuEntries
                        .contains(HazardConstants.CONTEXT_MENU_ADD_REMOVE_SHAPES));

                /*
                 * Create a new non-draw-by-area event.
                 */
                autoTestUtilities.createEvent(SECOND_EVENT_CENTER_X,
                        SECOND_EVENT_CENTER_Y);
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