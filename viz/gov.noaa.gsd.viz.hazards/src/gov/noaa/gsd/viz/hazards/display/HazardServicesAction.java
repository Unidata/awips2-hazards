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

import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.ENCRYPTION_KEY;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.PASSWORD;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.REGISTRY_LOCATION;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.TRUST_STORE_LOCATION;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.TRUST_STORE_PASSWORD;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.USER_NAME;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplayResourceData;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;

import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient;
import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.tools.GenericToolsResourceData;
import com.raytheon.uf.viz.core.rsc.tools.action.AbstractGenericToolAction;
import com.raytheon.viz.ui.input.EditableManager;

/**
 * Action handler for the Hazards button displayed on the CAVE toolbar.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * June 2011               Bryon.Lawrence    Initial creation
 * Jul 08, 2013    585     Chris.Golden      Changed to support loading from bundle.
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * Jul 21, 2015 2921      Robert.Blum        Changes for multi panel displays.
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
        loadRegistryPreferences();
        super.execute(event);
        return null;
    }

    /**
     * Initializes the Hazard Services web services interfaces
     */
    private void loadRegistryPreferences() {
        IPreferenceStore store = HazardServicesActivator.getDefault()
                .getPreferenceStore();
        HazardServicesClient.init(store.getString(REGISTRY_LOCATION),
                store.getString(USER_NAME), store.getString(PASSWORD),
                store.getString(TRUST_STORE_LOCATION),
                store.getString(TRUST_STORE_PASSWORD),
                store.getString(ENCRYPTION_KEY));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.awipstools.ui.action.MapToolAction#getResourceData()
     */
    @Override
    protected GenericToolsResourceData<SpatialDisplay> getResourceData() {
        return new GenericToolsResourceData<SpatialDisplay>("Hazard Services",
                SpatialDisplay.class);
    }

    @Override
    protected SpatialDisplay getResource(LoadProperties loadProperties,
            IDescriptor descriptor) throws VizException {

        for (IDisplayPane pane : getSelectedPanes()) {
            for (ResourcePair rp : pane.getDescriptor().getResourceList()) {
                if (rp.getResource() instanceof SpatialDisplay) {
                    EditableManager.makeEditable(rp.getResource(), true);
                    SpatialDisplay spatialDisplay = (SpatialDisplay) rp
                            .getResource();
                    return spatialDisplay;
                }
            }
        }

        SpatialDisplayResourceData spatialDisplayResourceData = new SpatialDisplayResourceData();
        SpatialDisplay spatialDisplay = spatialDisplayResourceData.construct(
                new LoadProperties(), descriptor);
        return spatialDisplay;
    }
}
