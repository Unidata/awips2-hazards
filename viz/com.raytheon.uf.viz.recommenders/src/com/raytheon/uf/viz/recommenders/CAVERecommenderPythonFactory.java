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

import com.raytheon.uf.common.python.concurrent.PythonInterpreterFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

import jep.JepException;

/**
 * Factory for use with the Python concurrency code to allow for multiple
 * recommenders at once.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 5, 2013             mnash       Initial creation
 * Mar 31, 2016 8837       Robert.Blum Changes for Service Backup.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class CAVERecommenderPythonFactory
        implements PythonInterpreterFactory<CAVERecommenderScriptManager> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(CAVERecommenderPythonFactory.class);

    private final String site;

    public CAVERecommenderPythonFactory(String site) {
        this.site = site;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.python.concurrent.IPythonScriptFactory#
     * getPythonScript ()
     */
    @Override
    public CAVERecommenderScriptManager createPythonScript() {
        try {
            return new CAVERecommenderScriptManager(site);
        } catch (JepException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to create new script manager", e);
        }
        return null;
    }
}
