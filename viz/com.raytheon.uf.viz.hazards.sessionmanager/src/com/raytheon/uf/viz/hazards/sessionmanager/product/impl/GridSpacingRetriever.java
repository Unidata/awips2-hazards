/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.product.impl;

import com.raytheon.uf.common.dataplugin.warning.config.DialogConfiguration;
import com.raytheon.uf.common.dataplugin.warning.config.GridSpacing;

/**
 * Description: Retrieves {@link GridSpacing} from localization.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 22, 2014            daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class GridSpacingRetriever {

    GridSpacing gridSpacing(String site) {
        try {
            GridSpacing gridSpacing;
            DialogConfiguration dialogConfig = DialogConfiguration
                    .loadDialogConfig(site);
            gridSpacing = dialogConfig.getGridSpacing();
            return gridSpacing;
        } catch (Exception e) {
            throw new RuntimeException("Could not load grid spacing");
        }
    }
}
