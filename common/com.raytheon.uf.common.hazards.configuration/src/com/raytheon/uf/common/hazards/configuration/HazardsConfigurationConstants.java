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
package com.raytheon.uf.common.hazards.configuration;

import java.io.File;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 28, 2014            bkowal     Initial creation
 * Aug 31, 2015    9757    Robert.Blum Added addtional path constants.
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class HazardsConfigurationConstants {
    /**
     * 
     */
    protected HazardsConfigurationConstants() {
    }

    public static final String HAZARD_TYPES_PY = "hazardServices"
            + File.separator + "hazardTypes" + File.separator
            + "HazardTypes.py";

    public static final String START_UP_CONFIG_PY = "hazardServices"
            + File.separator + "startUpConfig" + File.separator
            + "StartUpConfig.py";

    public static final String HAZARD_CATEGORIES_PY = "hazardServices"
            + File.separator + "hazardCategories" + File.separator
            + "HazardCategories.py";

    public static final String HAZARD_METADATA_PY = "hazardServices"
            + File.separator + "hazardMetaData" + File.separator
            + "HazardMetaData.py";

    public static final String ALERTS_CONFIG_PATH = "hazardServices"
            + File.separator + "alerts" + File.separator
            + "HazardAlertsConfig.xml";

    public static final String EVENT_DRIVEN_TOOLS_PY = "python"
            + File.separator + "events" + File.separator + "recommenders"
            + File.separator + "config" + File.separator
            + "EventDrivenTools.py";

    public static final String PRODUCT_GENERATOR_TABLE_PY = "hazardServices"
            + File.separator + "productGeneratorTable" + File.separator
            + "ProductGeneratorTable.py";

    public static final String DEFAULT_CONFIG_PY = "python" + File.separator
            + "dataStorage" + File.separator + "defaultConfig.py";

    public static final String VTEC_CONSTANTS_PY = "python" + File.separator
            + "VTECutilities" + File.separator + "VTECConstants.py";
}