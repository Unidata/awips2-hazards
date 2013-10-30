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
package com.raytheon.uf.edex.hazards.gfe;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jep.JepException;

import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.ParmID;
import com.raytheon.uf.common.dataplugin.gfe.discrete.DiscreteKey;
import com.raytheon.uf.common.dataplugin.gfe.python.GfePyIncludeUtil;
import com.raytheon.uf.common.dataplugin.gfe.slice.DiscreteGridSlice;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.PyUtil;
import com.raytheon.uf.common.python.PythonScript;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SingleTypeJAXBManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.TimeRange;

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

    private static final String MERGE_HAZARDS_FILE = "gfe" + File.separator
            + "userPython" + File.separator + "procedures" + File.separator
            + "MergeHazards.py";

    private static final List<String> preEvals = Arrays
            .asList(new String[] {
                    "import JUtil",
                    "def getHazardsConflictDict() :\n     return JUtil.pyValToJavaObj(HazardsConflictDict)" });

    private static Map<String, List<String>> hazardsConflictDict;

    /**
     * Checks to see if hazardEvent conflicts with existing discrete grid
     * slices.
     * 
     * @param phenSig
     * @param timeRange
     * @param siteID
     * @return
     * @throws Exception
     * @throws JepException
     */
    @SuppressWarnings("unchecked")
    public static boolean hasConflicts(String phenSig, TimeRange timeRange,
            String siteID) {
        try {
            // get HazardsConflictDict from MergeHazards.py
            if (hazardsConflictDict == null) {
                IPathManager pm = PathManagerFactory.getPathManager();
                File scriptFile = pm.getStaticFile(MERGE_HAZARDS_FILE);
                String python = GfePyIncludeUtil.getCommonPythonIncludePath();
                String utilities = GfePyIncludeUtil.getUtilitiesIncludePath();
                String gfe = GfePyIncludeUtil.getCommonGfeIncludePath();
                String vtec = GfePyIncludeUtil.getVtecIncludePath();

                PythonScript script = new PythonScript(
                        scriptFile.getPath(),
                        PyUtil.buildJepIncludePath(python, utilities, gfe, vtec),
                        GridValidator.class.getClassLoader(), preEvals);
                hazardsConflictDict = (Map<String, List<String>>) script
                        .execute("getHazardsConflictDict", null);
            }

            ParmID parmID = new ParmID(String.format(
                    GridRequestHandler.PARAM_ID_FORMAT, siteID));
            List<GFERecord> potentialRecords = GridRequestHandler
                    .findIntersectedGrid(parmID, timeRange);
            // test if hazardEvent will conflict with existing grids
            if (hazardsConflictDict != null
                    && hazardsConflictDict.get(phenSig) != null) {
                List<String> hazardsConflictList = hazardsConflictDict
                        .get(phenSig);
                for (GFERecord record : potentialRecords) {
                    DiscreteGridSlice gridSlice = (DiscreteGridSlice) record
                            .getMessageData();
                    for (DiscreteKey discreteKey : gridSlice.getKey()) {
                        for (String key : discreteKey.getSubKeys()) {
                            if (hazardsConflictList.contains(key)) {
                                return true;
                            }
                        }
                    }
                }
            }

        } catch (JepException e) {
            statusHandler
                    .error("Error trying to retrieve the HazardsConflictDict from MergeHazards.py",
                            e);
        } catch (Exception e) {
            statusHandler.error(
                    "Error trying to retrieve intersecting gfe records", e);
        }
        return false;
    }

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
