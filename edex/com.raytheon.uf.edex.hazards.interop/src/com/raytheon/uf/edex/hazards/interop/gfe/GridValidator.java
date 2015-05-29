/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.edex.hazards.interop.gfe;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Helper class to determine if a hazard even needs to create a grid.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 27, 2013 2277       jsanchez     Initial creation
 * Mar 24, 2014 3323       bkowal       Use the mode to ensure that the correct
 *                                      grid is accessed.
 * Apr 28, 2014 3556       bkowal       Now retrieves the hazard conflict dictionary
 *                                      from static localization.
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access                                      
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class GridValidator {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(GridValidator.class);

    private static final SingleTypeJAXBManager<HazardEventGrids> jaxb = SingleTypeJAXBManager
            .createWithoutException(HazardEventGrids.class);

    private static final String HAZARD_EVENT_GRIDS_FILE = "hazardServices"
            + File.separator + "hazardEventGrids.xml";

    /**
     * Determines if the HazardEvent should be saved as a grid or if it should
     * be ignored.
     * 
     * @param hazardEvent
     * @return
     */
    public static boolean needsGridConversion(String phenSig) {
        boolean needsConversion = false;

        IPathManager pm = PathManagerFactory.getPathManager();
        File hazardEventGridsXml = pm.getStaticFile(HAZARD_EVENT_GRIDS_FILE);

        try {
            HazardEventGrids hazardEventGrids = jaxb
                    .unmarshalFromXmlFile(hazardEventGridsXml);
            if (hazardEventGrids.getPhenSigs() != null) {
                List<String> phenSigs = Arrays.asList(hazardEventGrids
                        .getPhenSigs());
                needsConversion = phenSigs.contains(phenSig);
            }

        } catch (SerializationException e) {
            statusHandler.error("XML file unable to be read. Hazard event for "
                    + phenSig + " will not be converted at this time.", e);
        }
        return needsConversion;

    }
}