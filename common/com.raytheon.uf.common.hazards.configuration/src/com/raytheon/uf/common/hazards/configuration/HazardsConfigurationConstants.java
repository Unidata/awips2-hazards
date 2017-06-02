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
 * Configuration file location constants.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 28, 2014            bkowal     Initial creation
 * Aug 31, 2015    9757    Robert.Blum Added addtional path constants.
 * Nov 17, 2015 3473       Robert.Blum Moved all python files under HazardServices localization dir.
 * Feb 12, 2016 14923      Robert.Blum Added text and event utilities path constants.
 * Mar 03, 2016 7452       Robert.Blum Added PYTHON_LOCALIZATION_GEOSPATIAL_DIR.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */
public class HazardsConfigurationConstants {

    /**
     * No need to instantiate
     */
    private HazardsConfigurationConstants() {
    }

    public static final String HAZARD_SERVICES_DIR = "HazardServices";

    public static final String PYTHON_LOCALIZATION_DIR = "python";

    public static final String HAZARD_SERVICES_PYTHON_LOCALIZATION_DIR = HAZARD_SERVICES_DIR
            + File.separator + PYTHON_LOCALIZATION_DIR;

    public static final String PYTHON_LOCALIZATION_CONFIG_DIR = "config";

    public static final String PYTHON_LOCALIZATION_DATA_ACCESS_DIR = "dataaccess";

    public static final String PYTHON_LOCALIZATION_DATA_STORAGE_DIR = "dataStorage";

    public static final String PYTHON_LOCALIZATION_GENERAL_UTILITIES_DIR = "generalUtilities";

    public static final String PYTHON_LOCALIZATION_GFE_DIR = "gfe";

    public static final String PYTHON_LOCALIZATION_GEO_UTILITIES_DIR = "geoUtilities";

    public static final String PYTHON_LOCALIZATION_UTILITIES_DIR = "localizationUtilities";

    public static final String PYTHON_LOCALIZATION_LOG_UTILITIES_DIR = "logUtilities";

    public static final String PYTHON_LOCALIZATION_RECOMMENDERS_DIR = "recommenders";

    public static final String PYTHON_LOCALIZATION_PRODUCTGEN_DIR = "productgen";

    public static final String PYTHON_LOCALIZATION_PRODUCTS_DIR = "products";

    public static final String PYTHON_LOCALIZATION_FORMATS_DIR = "formats";

    public static final String PYTHON_LOCALIZATION_GEOSPATIAL_DIR = "geoSpatial";

    public static final String PYTHON_LOCALIZATION_SHAPE_UTILITIES_DIR = "shapeUtilities";

    public static final String PYTHON_LOCALIZATION_TEXT_UTILITIES_DIR = "textUtilities";

    public static final String PYTHON_LOCALIZATION_TIME_DIR = "time";

    public static final String PYTHON_LOCALIZATION_TRACK_UTILITIES_DIR = "trackUtilities";

    public static final String PYTHON_LOCALIZATION_VTEC_UTILITIES_DIR = "VTECutilities";

    public static final String HAZARD_TYPES_LOCALIZATION_DIR = "hazardTypes";

    public static final String HAZARD_CATEGORIES_LOCALIZATION_DIR = "hazardCategories";

    public static final String HAZARD_METADATA_DIR = "hazardMetaData";

    public static final String PYTHON_LOCALIZATION_BRIDGE_DIR = "bridge";

    public static final String PYTHON_LOCALIZATION_EVENTS_DIR = "events";

    public static final String PYTHON_UTILITIES_DIR = "utilities";

    public static final String STARTUP_CONFIG_DIR = "startUpConfig";

    public static final String ALERTS_DIR = "alerts";

    public static final String PRODUCT_GENERATOR_TABLE_DIR = "productGeneratorTable";

    public static final String PRODUCT_GENERATOR_RELATIVE_PATH = HAZARD_SERVICES_DIR
            + File.separator
            + PYTHON_LOCALIZATION_DIR
            + File.separator
            + PYTHON_LOCALIZATION_EVENTS_DIR
            + File.separator
            + PYTHON_LOCALIZATION_PRODUCTGEN_DIR
            + File.separator
            + PYTHON_LOCALIZATION_PRODUCTS_DIR + File.separator;

    public static final String RECOMMENDERS_LOCALIZATION_DIR = HAZARD_SERVICES_DIR
            + File.separator
            + PYTHON_LOCALIZATION_DIR
            + File.separator
            + PYTHON_LOCALIZATION_EVENTS_DIR
            + File.separator
            + PYTHON_LOCALIZATION_RECOMMENDERS_DIR;

    public static final String RECOMMENDERS_CONFIG_LOCALIZATION_DIR = RECOMMENDERS_LOCALIZATION_DIR
            + File.separator + PYTHON_LOCALIZATION_CONFIG_DIR;

    public static final String PYTHON_EVENTS_DIRECTORY = HAZARD_SERVICES_DIR
            + File.separator + PYTHON_LOCALIZATION_DIR + File.separator
            + PYTHON_LOCALIZATION_EVENTS_DIR + File.separator;

    public static final String HAZARD_TYPES_PY = HAZARD_SERVICES_DIR
            + File.separator + HAZARD_TYPES_LOCALIZATION_DIR + File.separator
            + "HazardTypes.py";

    public static final String START_UP_CONFIG_PY = HAZARD_SERVICES_DIR
            + File.separator + STARTUP_CONFIG_DIR + File.separator
            + "StartUpConfig.py";

    public static final String HAZARD_CATEGORIES_PY = HAZARD_SERVICES_DIR
            + File.separator + HAZARD_CATEGORIES_LOCALIZATION_DIR
            + File.separator + "HazardCategories.py";

    public static final String HAZARD_METADATA_PY = HAZARD_SERVICES_DIR
            + File.separator + HAZARD_METADATA_DIR + File.separator
            + "HazardMetaData.py";

    public static final String ALERTS_CONFIG_PATH = HAZARD_SERVICES_DIR
            + File.separator + ALERTS_DIR + File.separator
            + "HazardAlertsConfig.xml";

    public static final String PRODUCT_GENERATOR_TABLE_PY = HAZARD_SERVICES_DIR
            + File.separator + PRODUCT_GENERATOR_TABLE_DIR + File.separator
            + "ProductGeneratorTable.py";

    public static final String EVENT_DRIVEN_TOOLS_PY = RECOMMENDERS_CONFIG_LOCALIZATION_DIR
            + File.separator + "EventDrivenTools.py";

    public static final String DEFAULT_CONFIG_PY = HAZARD_SERVICES_DIR
            + File.separator + PYTHON_LOCALIZATION_DIR + File.separator
            + PYTHON_LOCALIZATION_DATA_STORAGE_DIR + File.separator
            + "defaultConfig.py";

    public static final String VTEC_CONSTANTS_PY = HAZARD_SERVICES_DIR
            + File.separator + PYTHON_LOCALIZATION_DIR + File.separator
            + PYTHON_LOCALIZATION_VTEC_UTILITIES_DIR + File.separator
            + "VTECConstants.py";

    public static final String TEXT_UTILITIES_LOCALIZATION_DIR = HAZARD_SERVICES_DIR
            + File.separator
            + PYTHON_LOCALIZATION_DIR
            + File.separator
            + PYTHON_LOCALIZATION_TEXT_UTILITIES_DIR;

    public static final String EVENT_UTILITIES_LOCALIZATION_DIR = HAZARD_SERVICES_DIR
            + File.separator
            + PYTHON_LOCALIZATION_DIR
            + File.separator
            + PYTHON_LOCALIZATION_EVENTS_DIR
            + File.separator
            + PYTHON_UTILITIES_DIR;
}