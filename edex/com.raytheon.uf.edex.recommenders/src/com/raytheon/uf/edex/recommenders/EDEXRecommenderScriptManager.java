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
package com.raytheon.uf.edex.recommenders;

import gov.noaa.gsd.common.visuals.VisualFeaturesList;

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
 * Manages recommenders on the EDEX side. Allows for the execution of entire
 * recommenders without disturbing the user.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 23, 2013            mnash       Initial creation
 * Jul 12, 2013 1257       bsteffen    Convert recommender dialog info to use
 *                                     Serializeables for values instead of
 *                                     Strings.
 * Jan 29, 2015 3626       Chris.Golden Added EventSet to arguments for getting dialog
 *                                      info.
 * Mar 31, 2016  883       Robert.Blum  Removed no arg constructor.
 * Jun 23, 2016 19537      Chris.Golden Changed to use visual features for
 *                                      spatial info gathering.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class EDEXRecommenderScriptManager extends
        AbstractRecommenderScriptManager {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(EDEXRecommenderScriptManager.class);

    /**
     * 
     */
    public EDEXRecommenderScriptManager(String site) throws JepException {
        super(buildScriptPath(), buildPythonPath(site),
                EDEXRecommenderScriptManager.class.getClassLoader(),
                "Recommender", site);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.recommenders.AbstractRecommenderScriptManager#
     * executeEntireRecommender(java.lang.String)
     */
    @Override
    public EventSet<IEvent> executeEntireRecommender(String recommenderName,
            EventSet<IEvent> eventSet) {
        if (inventory.containsKey(recommenderName)) {
            statusHandler
                    .handle(Priority.VERBOSE, "Running " + recommenderName);
            try {
                String recName = resolveCorrectName(recommenderName);
                if (isInstantiated(recName) == false) {
                    instantiatePythonScript(recName);
                }
                // do nothing with these for now, may read from config file
                Map<String, Serializable> dialogValues = null;
                VisualFeaturesList visualFeatures = null;
                // using a new EventSet<IEvent> for now, but will need something
                // different for future
                return executeRecommender(recommenderName, eventSet,
                        dialogValues, visualFeatures);
            } catch (JepException e) {
                statusHandler.handle(Priority.ERROR,
                        "Unable to execute recommender", e);
            }
        }
        return null;
    }
}
