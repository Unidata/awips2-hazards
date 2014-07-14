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
package com.raytheon.uf.viz.recommenders;

import java.io.Serializable;
import java.util.Map;

import jep.JepException;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.recommenders.AbstractRecommenderScriptManager;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * CAVE-side implementation of the {@link AbstractRecommenderScriptManager}. Has
 * methods to runRecommenders start to finish, or run them piece by piece if
 * desired.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 24, 2013            mnash       Initial creation
 * Jul 12, 2013 1257       bsteffen    Convert recommender dialog info to use
 *                                     Serializeables for values instead of
 *                                     Strings.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class CAVERecommenderScriptManager extends
        AbstractRecommenderScriptManager {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CAVERecommenderScriptManager.class);

    /**
     * @throws JepException
     * 
     */
    public CAVERecommenderScriptManager() throws JepException {
        super(buildScriptPath(), buildPythonPath(),
                CAVERecommenderScriptManager.class.getClassLoader(),
                "Recommender");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.recommenders.AbstractRecommenderScriptManager#
     * executeRecommender(java.lang.String)
     */
    @Override
    public EventSet<IEvent> executeEntireRecommender(String recommenderName) {
        if (inventory.containsKey(recommenderName)) {
            statusHandler
                    .handle(Priority.VERBOSE, "Running " + recommenderName);
            try {
                String recName = resolveCorrectName(recommenderName);
                if (isInstantiated(recName) == false) {
                    instantiatePythonScript(recName);
                }
                Map<String, Serializable> dialogValues = getInfo(recName,
                        "getDialogInfo");
                showDialog(dialogValues);
                Map<String, Serializable> spatialValues = getInfo(recName,
                        "getSpatialInfo");
                return executeRecommender(recommenderName,
                        new EventSet<IEvent>(), dialogValues, spatialValues);
            } catch (JepException e) {
                statusHandler.handle(Priority.ERROR,
                        "Unable to execute recommender", e);
            }
        }
        return null;
    }

    private void showDialog(Map<String, Serializable> dialogValues) {
        // TODO, call megawidgets
        // MegawidgetSpecifierFactory factory = new
        // MegawidgetSpecifierFactory();
        // megawidgets need to be refactored into a separate plugin
    }
}
