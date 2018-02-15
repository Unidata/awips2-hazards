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
package com.raytheon.uf.common.hazards.productgen.product;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.hazards.configuration.HazardsConfigurationConstants;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.python.concurrent.PythonInterpreterFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.util.FileUtil;

import jep.JepException;

/**
 * Factory to create a ProductScript object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2013            jsanchez     Initial creation
 * May 23, 2014 3790       jsanchez     Used DEFAULT_PRODUCT_GENERATION_JOB_COORDINATOR.
 * Nov 05, 2014 4042       Chris.Golden Added new directories to Python include path.
 * Feb 19, 2015 5071       Robert.Blum  Added new directories to include path
 * Feb 26, 2015 6306       mduff        Pass site to product script.
 * Nov 17, 2015 3473       Robert.Blum  Moved all python files under HazardServices
 *                                      localization dir.
 * Dec 16, 2015 14019      Robert.Blum  Updates for new PythonJobCoordinator API.
 * Mar 31, 2016  8837      Robert.Blum  Added the site name to the ProductScriptFactory so that
 *                                      PythonJobCoordinator.newInstance() actually creates a
 *                                      new instance when the site is changed for service
 *                                      backup.
 * May 03, 2016 18376      Chris.Golden Changed to support reuse of Jep instance between H.S.
 *                                      sessions in the same CAVE session, since stopping and
 *                                      starting the Jep instances when the latter use numpy is
 *                                      dangerous.
 * Jun 06, 2017 15561      Chris.Golden Changed to use HazardsConfigurationConstants constants
 *                                      instead of string literals.
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductScriptFactory
        implements PythonInterpreterFactory<ProductScript> {

    /**
     * All of the utility directories off of the
     * {@link HazardsConfigurationConstants#GFE_LOCALIZATION_DIR} directory
     * which must be added to the Jep path for the product generation framework
     * and product generators.
     */
    private final static String[] GFE_UTILITY_SUBDIRECTORIES = {
            HazardsConfigurationConstants.PYTHON_LOCALIZATION_DIR };

    /**
     * All of the utility directories off of the
     * {@link HazardsConfigurationConstants#HAZARD_SERVICES_PYTHON_LOCALIZATION_DIR}
     * directory which must be added to the Jep path for the product generation
     * framework and product generators.
     */
    private final static String[] PYTHON_UTILITY_SUBDIRECTORIES = {
            HazardsConfigurationConstants.PYTHON_LOCALIZATION_BRIDGE_DIR,
            HazardsConfigurationConstants.PYTHON_LOCALIZATION_DATA_STORAGE_DIR,
            HazardsConfigurationConstants.PYTHON_LOCALIZATION_LOG_UTILITIES_DIR,
            HazardsConfigurationConstants.PYTHON_LOCALIZATION_SHAPE_UTILITIES_DIR,
            HazardsConfigurationConstants.PYTHON_LOCALIZATION_TEXT_UTILITIES_DIR,
            HazardsConfigurationConstants.PYTHON_LOCALIZATION_VTEC_UTILITIES_DIR,
            HazardsConfigurationConstants.PYTHON_LOCALIZATION_GEO_UTILITIES_DIR,
            HazardsConfigurationConstants.PYTHON_LOCALIZATION_GENERAL_UTILITIES_DIR,
            HazardsConfigurationConstants.PYTHON_LOCALIZATION_UTILITIES_DIR };

    /**
     * All of the utility directories off of the
     * {@link HazardsConfigurationConstants#HAZARD_SERVICES_DIR} directory which
     * must be added to the Jep path for the product generation framework and
     * product generators.
     */
    private final static String[] HAZARD_SERVICES_UTILITY_SUBDIRECTORIES = {
            HazardsConfigurationConstants.HAZARD_METADATA_DIR,
            HazardsConfigurationConstants.HAZARD_CATEGORIES_LOCALIZATION_DIR,
            HazardsConfigurationConstants.HAZARD_TYPES_LOCALIZATION_DIR };

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductScriptFactory.class);

    private String site;

    /**
     * Default Constructor.
     */
    public ProductScriptFactory() {
    }

    /**
     * Set the site identifier.
     * 
     * @param site
     *            Site identifier.
     */
    public void setSite(String site) {
        this.site = site;
    }

    @Override
    public ProductScript createPythonScript() {
        try {
            /*
             * Add Hazard Services specific utilities which must be added to the
             * Python path.
             */
            IPathManager manager = PathManagerFactory.getPathManager();
            LocalizationContext baseContext = manager.getContext(
                    LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
            List<String> utilityPathList = new ArrayList<String>();

            String gfePath = manager
                    .getFile(baseContext,
                            HazardsConfigurationConstants.GFE_LOCALIZATION_DIR)
                    .getPath();
            utilityPathList.add(gfePath);
            for (String utilityDir : GFE_UTILITY_SUBDIRECTORIES) {
                utilityPathList.add(FileUtil.join(gfePath, utilityDir));
            }

            String pythonPath = manager
                    .getFile(baseContext,
                            HazardsConfigurationConstants.HAZARD_SERVICES_PYTHON_LOCALIZATION_DIR)
                    .getPath();
            utilityPathList.add(pythonPath);
            for (String utilityDir : PYTHON_UTILITY_SUBDIRECTORIES) {
                utilityPathList.add(FileUtil.join(pythonPath, utilityDir));
            }

            String hazardServicesPath = manager
                    .getFile(baseContext,
                            HazardsConfigurationConstants.HAZARD_SERVICES_DIR)
                    .getPath();
            utilityPathList.add(hazardServicesPath);
            for (String utilityDir : HAZARD_SERVICES_UTILITY_SUBDIRECTORIES) {
                utilityPathList
                        .add(FileUtil.join(hazardServicesPath, utilityDir));
            }

            String includePath = PyUtil.buildJepIncludePath(
                    utilityPathList.toArray(new String[0]));

            return new ProductScript(includePath, site);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to create product script", e);
        }
        return null;
    }
}
