/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers;

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
 * Jun 21, 2011            Xiangbao    Initial creation
 * 
 * </pre>
 * 
 * @author Xiangbao Jing
 * @version 1.0
 */
public abstract class AbstractMouseHandler {
    protected ToolLayer drawingLayer;

    public abstract IInputHandler getMouseHandler();

    public void setDrawingLayer(ToolLayer drawingLayer) {
        this.drawingLayer = drawingLayer;
    }

    public ToolLayer getDrawingLayer() {
        return drawingLayer;
    }
}
