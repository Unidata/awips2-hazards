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

import java.util.List;

import com.raytheon.uf.common.python.concurrent.IPythonExecutor;
import com.raytheon.uf.common.recommenders.AbstractRecommenderScriptManager;
import com.raytheon.uf.common.recommenders.EventRecommender;

/**
 * Gets the inventory of the recommenders.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 6, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class RecommenderInventoryExecutor<P extends AbstractRecommenderScriptManager>
        implements IPythonExecutor<P, List<EventRecommender>> {

    /**
     * 
     */
    public RecommenderInventoryExecutor() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.python.concurrent.IPythonExecutor#execute(com.
     * raytheon.uf.common.python.PythonInterpreter)
     */
    @Override
    public List<EventRecommender> execute(P script) {
        return script.getInventory();
    }

}
