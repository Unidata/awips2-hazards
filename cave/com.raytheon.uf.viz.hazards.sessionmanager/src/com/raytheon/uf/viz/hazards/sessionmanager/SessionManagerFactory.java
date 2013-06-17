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
package com.raytheon.uf.viz.hazards.sessionmanager;

import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.viz.hazards.sessionmanager.impl.SessionManager;

/**
 * This is the preferred method of obtaining a new ISessionManager. This
 * protects any code using an ISessionManager form the details of any specific
 * SessionManager implementation.
 * 
 * Currently there is only a single ISessionManager implementation but the
 * factory may be used in the future to implement spring and/or extension point
 * loading.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 11, 2013 1257       bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SessionManagerFactory {

    public static ISessionManager getSessionManager() {
        return new SessionManager(PathManagerFactory.getPathManager(),
                new HazardEventManager(HazardEventManager.Mode.PRACTICE));
    }
}
