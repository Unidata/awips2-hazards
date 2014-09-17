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

import jep.JepException;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.python.concurrent.AbstractPythonScriptFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.util.FileUtil;

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
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ProductScriptFactory extends
        AbstractPythonScriptFactory<ProductScript> {

    /**
     * Contains all of the Hazard Services specific utility directories which
     * must be added to the jep path for the product generation framework and
     * product generators
     */
    private final static String[] hazardServicesSpecificUtilityDirs = {
            "bridge", "dataStorage", "logUtilities", "shapeUtilities",
            "textUtilities", "VTECutilities", "geoUtilities",
            "localizationUtilities" };

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductScriptFactory.class);

    /**
     * Default Constructor.
     */
    public ProductScriptFactory() {
        this(ProductScript.DEFAULT_PRODUCT_GENERATION_JOB_COORDINATOR, 1);
    }

    /**
     * @param name
     *            reference name to get instance
     * @param maxThreads
     *            max number of threads
     */
    private ProductScriptFactory(String name, int maxThreads) {
        super(name, maxThreads);
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
            String pythonPath = manager.getFile(baseContext, "python")
                    .getPath();

            List<String> hazardServicesUtilityPathList = new ArrayList<String>();
            hazardServicesUtilityPathList.add(pythonPath);

            for (String utilityDir : hazardServicesSpecificUtilityDirs) {
                hazardServicesUtilityPathList.add(FileUtil.join(pythonPath,
                        utilityDir));
            }

            String includePath = PyUtil
                    .buildJepIncludePath(hazardServicesUtilityPathList
                            .toArray(new String[0]));

            return new ProductScript(includePath);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to create product script", e);
        }
        return null;
    }
}
