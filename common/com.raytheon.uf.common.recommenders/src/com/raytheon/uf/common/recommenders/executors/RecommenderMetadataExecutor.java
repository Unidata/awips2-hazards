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
package com.raytheon.uf.common.recommenders.executors;

import java.io.Serializable;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.recommenders.AbstractRecommenderScriptManager;

/**
 * Gets the metadata of a given file.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 06, 2013            mnash        Initial creation
 * Jan 29, 2015 3626       Chris.Golden Added EventSet to arguments for getting dialog
 *                                      info.
 * Jun 23, 2016 19537      Chris.Golden Changed to use constants.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class RecommenderMetadataExecutor<P extends AbstractRecommenderScriptManager>
        extends AbstractRecommenderExecutor<P, Map<String, Serializable>> {

    /**
     * 
     */
    public RecommenderMetadataExecutor(String recommenderName) {
        super(recommenderName);
    }

    @Override
    public Map<String, Serializable> execute(P script) {
        return script.getInfo(recommenderName,
                HazardConstants.RECOMMENDER_GET_METADATA_METHOD, null);
    }
}
