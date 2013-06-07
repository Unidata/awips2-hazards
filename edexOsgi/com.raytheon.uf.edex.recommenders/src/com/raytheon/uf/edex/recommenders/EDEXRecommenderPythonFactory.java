/**
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

import jep.JepException;

import com.raytheon.uf.common.python.concurrent.AbstractPythonScriptFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Creates a new thread pool for recommenders on EDEX.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 23, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class EDEXRecommenderPythonFactory extends
        AbstractPythonScriptFactory<EDEXRecommenderScriptManager> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(EDEXRecommenderPythonFactory.class);

    /**
     * 
     */
    public EDEXRecommenderPythonFactory() {
        super("Recommenders", 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.python.concurrent.AbstractPythonScriptFactory#
     * createPythonScript()
     */
    @Override
    public EDEXRecommenderScriptManager createPythonScript() {
        try {
            return new EDEXRecommenderScriptManager();
        } catch (JepException e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
        return null;
    }
}
