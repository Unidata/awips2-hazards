/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.utilities;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;

/**
 * Description: TODO
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 09, 2013            daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author Dan Schaffer
 * @version 1.0
 */
public class FileUtilities {
    /**
     * Method to help bring in correct localization files.
     */
    public static void fillFiles() {
        IPathManager manager = PathManagerFactory.getPathManager();
        manager.listFiles(manager.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.BASE), "python", null, true, false);

    }
}
