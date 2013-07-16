/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import gov.noaa.gsd.viz.hazards.spatialdisplay.ToolLayer;
import gov.noaa.gsd.viz.hazards.spatialdisplay.ToolLayerResourceData;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.ResourceList;
import com.raytheon.viz.ui.tools.map.AbstractMapTool;

/**
 * Action handler for the Hazards button displayed on the CAVE toolbar.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * June 2011               Bryon.Lawrence    Initial creation
 * Jul 08, 2013    585     Chris.Golden      Changed to support loading from bundle.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class HazardServicesAction extends AbstractMapTool {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardServicesAction.class);

    // Public Methods

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        super.execute(event);

        // Determine if a Hazard Services viz resource already exists, and if
        // so, reuse it; otherwise, create a new one.
        IDescriptor desc = editor.getActiveDisplayPane().getDescriptor();
        ResourceList rscList = desc.getResourceList();
        ToolLayer toolLayer = null;
        for (ResourcePair rp : rscList) {
            AbstractVizResource<?, ?> rsc = rp.getResource();
            if (rsc instanceof ToolLayer) {
                toolLayer = (ToolLayer) rsc;
                break;
            }
        }
        if (toolLayer == null) {
            try {
                ToolLayerResourceData toolLayerResourceData = new ToolLayerResourceData();
                toolLayer = toolLayerResourceData.construct(
                        new LoadProperties(), desc);
            } catch (VizException e1) {
                statusHandler.error("Error creating spatial display", e1);
            }
        } else {
            toolLayer.getAppBuilder().ensureViewsVisible();
        }
        return null;
    }
}
