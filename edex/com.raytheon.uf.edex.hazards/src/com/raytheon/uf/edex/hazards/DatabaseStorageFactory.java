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
package com.raytheon.uf.edex.hazards;

import com.raytheon.uf.edex.hazards.database.DatabaseEventManager;

/**
 * Concrete implementation of {@link IHazardStorageFactory} to return a
 * {@link DatabaseEventManager}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 12, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class DatabaseStorageFactory implements IHazardStorageFactory {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.edex.hazards.IHazardStorageFactory#getStorageManager()
     */
    @Override
    public IHazardStorageManager getStorageManager() {
        return DatabaseEventManager.getInstance();
    }

}
