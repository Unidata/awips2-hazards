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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource.ResourceStatus;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.tools.GenericToolsResourceData;
import com.raytheon.uf.viz.core.rsc.tools.action.AbstractGenericToolAction;
import com.raytheon.viz.ui.input.EditableManager;

/**
 * Action handler for the Hazards button displayed on the CAVE toolbar.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer       Description
 * ------------ ---------- -------------- --------------------------
 * June 2011               Bryon.Lawrence Initial creation.
 * Jul 08, 2013     585    Chris.Golden   Changed to support loading from bundle.
 * May 29, 2015    6895    Ben.Phillippe  Refactored Hazard Service data access
 * Jul 21, 2015    2921    Robert.Blum    Changes for multi panel displays.
 * Oct 22, 2015    9615    Robert.Blum    Unloading SpatialDisplay resource if
 *                                        it is disposed.
 * Jul 25, 2016   19537    Chris.Golden   Moved loading of registry preferences
 *                                        elsewhere, since loading via a bundle
 *                                        load did not get the registry prefs
 *                                        loaded when it was done here. Also
 *                                        changed to create the correct subclass
 *                                        of resource data.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class HazardServicesAction extends
        AbstractGenericToolAction<SpatialDisplay> {

    // Public Methods

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.tools.AbstractTool#runTool()
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        super.execute(event);
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.awipstools.ui.action.MapToolAction#getResourceData()
     */
    @Override
    protected GenericToolsResourceData<SpatialDisplay> getResourceData() {
        return new SpatialDisplayResourceData();
    }

    @Override
    protected SpatialDisplay getResource(LoadProperties loadProperties,
            IDescriptor descriptor) throws VizException {

        for (IDisplayPane pane : getSelectedPanes()) {
            boolean unloaded = false;
            for (ResourcePair rp : pane.getDescriptor().getResourceList()) {
                if (rp.getResource() instanceof SpatialDisplay) {
                    EditableManager.makeEditable(rp.getResource(), true);
                    SpatialDisplay spatialDisplay = (SpatialDisplay) rp
                            .getResource();
                    if (spatialDisplay.getStatus() == ResourceStatus.DISPOSED) {
                        spatialDisplay.unload();
                        unloaded = true;
                        break;
                    }
                    return spatialDisplay;
                }
            }
            if (unloaded) {
                break;
            }
        }

        SpatialDisplayResourceData spatialDisplayResourceData = new SpatialDisplayResourceData();
        SpatialDisplay spatialDisplay = spatialDisplayResourceData.construct(
                new LoadProperties(), descriptor);
        return spatialDisplay;
    }
}
