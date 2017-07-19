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
package com.raytheon.uf.viz.python.localization;

import org.eclipse.jface.action.IMenuManager;

import com.raytheon.uf.viz.localization.perspective.view.FileTreeEntryData;

/**
 * Adds a "New ..." menu action to the localization menu for certain file types.
 * Child classes supply the action that must be an INewBasedVelocity action.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 25, 2013            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractNewActionAdapter
        extends CopyPythonClassesAdapter {

    @Override
    public boolean addContextMenuItems(IMenuManager menuMgr,
            FileTreeEntryData[] selectedData) {
        super.addContextMenuItems(menuMgr, selectedData);
        if (selectedData.length == 1
                && selectedData[0].getClass() == FileTreeEntryData.class) {
            menuMgr.add(this.getLocalizationAction());
        }
        return false;
    }

    abstract protected INewBasedVelocityAction getLocalizationAction();
}