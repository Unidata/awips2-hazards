/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers;

import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.ToolLayer;

import com.raytheon.uf.viz.core.rsc.IInputHandler;

/**
 * The mouse handler for an action from main menu button selecting.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 21, 2011            Xiangbao         Initial creation
 * Jul 15, 2013      585   Chris.Golden     Changed to support subclasses
 *                                          no longer being singletons.
 * </pre>
 * 
 * @author Xiangbao Jing
 * @version 1.0
 */
public abstract class AbstractMouseHandler {
    private ToolLayer toolLayer;

    private SpatialPresenter spatialPresenter;

    private IInputHandler mouseHandler;

    protected abstract IInputHandler createMouseHandler();

    public IInputHandler getMouseHandler() {
        if (mouseHandler == null) {
            mouseHandler = createMouseHandler();
        }
        return mouseHandler;
    }

    protected final void setMouseHandler(IInputHandler mouseHandler) {
        this.mouseHandler = mouseHandler;
    }

    public ToolLayer getToolLayer() {
        return toolLayer;
    }

    public void setSpatialPresenter(SpatialPresenter spatialPresenter) {
        this.spatialPresenter = spatialPresenter;
        this.toolLayer = spatialPresenter.getView().getSpatialDisplay();
    }

    public SpatialPresenter getSpatialPresenter() {
        return spatialPresenter;
    }
}
