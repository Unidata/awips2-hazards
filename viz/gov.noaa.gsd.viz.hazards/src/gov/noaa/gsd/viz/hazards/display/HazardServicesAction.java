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

import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplayResourceData;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
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
        ResourceList rscList = editor.getActiveDisplayPane().getDescriptor()
                .getResourceList();
        List<AbstractVizResource<?, ?>> existingToolLayers = rscList
                .getResourcesByType(SpatialDisplay.class);
        if (existingToolLayers.isEmpty()) {
            try {
                IDescriptor desc = editor.getActiveDisplayPane()
                        .getDescriptor();
                SpatialDisplayResourceData spatialDisplayResourceData = new SpatialDisplayResourceData();
                spatialDisplayResourceData.construct(new LoadProperties(), desc);
            } catch (VizException e1) {
                statusHandler.error("Error creating spatial display", e1);
            }
        } else {
            ((SpatialDisplay) existingToolLayers.get(0)).getAppBuilder()
                    .ensureViewsVisible();
        }
        return null;

    }
}
