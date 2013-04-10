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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.tools.map.AbstractMapTool;

/**
 * Action handler for the Hazards button displayed on the CAVE toolbar.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * June 2011               Bryon.Lawrence    Initial creation
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

    // Private Variables

    /**
     * A reference to an instance of app builder.
     */
    @SuppressWarnings("unused")
    private HazardServicesAppBuilder appBuilder;

    // Public Methods

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        super.execute(event);

        if (CAVEMode.getMode() == CAVEMode.OPERATIONAL) {

            // Set CAVE to run in Displaced Real Time (DRT) Mode.
            Date date = new Date();
            date.setTime(Long.parseLong(HazardServicesAppBuilder.CANNED_TIME));
            Calendar simulatedDate = Calendar.getInstance();
            simulatedDate.setTimeZone(TimeZone.getTimeZone("UTC"));
            simulatedDate.setTime(date);
            SimulatedTime.getSystemTime().setTime(simulatedDate.getTime());
            SimulatedTime.getSystemTime().setFrozen(true);
        }

        // Create or get an instance of the app builder.
        try {
            appBuilder = HazardServicesAppBuilder.getInstance();
        } catch (VizException e) {
            statusHandler.error("Could not create or get the app builder.", e);
        }
        return null;
    }

}
