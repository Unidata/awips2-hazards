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
package com.raytheon.uf.viz.hazards.sessionmanager.product;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jep.JepException;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.python.PythonScript;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Contents of productFormats.xml are converted into this object.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 9, 2013            jsanchez     Initial creation
 * Mar 18, 2014 2917      jsanchez     Separated out issue and preview formats.
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
public class ProductFormats {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductFormats.class);

    private static final String PRODUCT_GENERATOR_TABLE = "hazardServices"
            + File.separator + "productGeneratorTable" + File.separator
            + "ProductGeneratorTable.py";

    private static final String METHOD_NAME = "getProductGeneratorTable";

    private List<String> issueFormats;

    private List<String> previewFormats;

    public List<String> getIssueFormats() {
        return issueFormats;
    }

    public void setIssueFormats(List<String> issueFormats) {
        this.issueFormats = issueFormats;
    }

    public List<String> getPreviewFormats() {
        return previewFormats;
    }

    public void setPreviewFormats(List<String> previewFormats) {
        this.previewFormats = previewFormats;
    }

    /**
     * Returns a map of the ProductGeneratorTable.py.
     * 
     * @param classLoader
     * @return
     */
    public static Map<String, List<Serializable>> getProductGeneratorTable(
            ClassLoader classLoader) {

        Map<String, List<Serializable>> productGeneratorTable = null;
        PythonScript python = null;

        IPathManager pathMgr = PathManagerFactory.getPathManager();
        LocalizationContext commonCx = pathMgr.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        String filePath = pathMgr.getFile(commonCx, PRODUCT_GENERATOR_TABLE)
                .getPath();
        String pythonPath = pathMgr.getFile(commonCx, "python").getPath();

        try {
            List<String> preEvals = new ArrayList<String>();
            preEvals.add("from JUtil import pyDictToJavaMap");
            preEvals.add("def " + METHOD_NAME
                    + "() :\n return pyDictToJavaMap(ProductGeneratorTable)");

            python = new PythonScript(filePath,
                    PyUtil.buildJepIncludePath(pythonPath), classLoader,
                    preEvals);

            productGeneratorTable = (Map<String, List<Serializable>>) python
                    .execute(METHOD_NAME, null);

        } catch (JepException e) {
            statusHandler.handle(Priority.PROBLEM,
                    "Error initializing product generator table", e);
        } finally {
            if (python != null) {
                python.dispose();
            }
        }

        return productGeneratorTable;
    }
}
