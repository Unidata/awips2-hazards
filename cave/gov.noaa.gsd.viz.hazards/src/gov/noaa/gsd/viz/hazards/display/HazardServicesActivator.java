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

import gov.noaa.nws.ncep.common.staticdata.IStaticDataProvider;
import gov.noaa.nws.ncep.staticdataprovider.StaticDataProvider;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class, which controls the plug-in lifecycle.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Xiangbao Jing      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Xiangbao Jing
 */
public class HazardServicesActivator extends AbstractUIPlugin {

    // Public Static Constants

    /**
     * Plugin identifier.
     */
    public static final String PLUGIN_ID = "gov.noaa.gsd.viz.hazards";

    // Private Static Variables

    /**
     * Shared instance.
     */
    private static HazardServicesActivator plugin;

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public HazardServicesActivator() {
        plugin = this;
    }

    // Public Methods

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        context.registerService(IStaticDataProvider.class.getName(),
                StaticDataProvider.getInstance(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Get the shared instance.
     * 
     * @return Shared instance.
     */
    public static HazardServicesActivator getDefault() {
        return plugin;
    }
}
