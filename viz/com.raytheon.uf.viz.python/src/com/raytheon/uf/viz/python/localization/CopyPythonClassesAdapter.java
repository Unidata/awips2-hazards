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

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;

import com.raytheon.uf.viz.localization.adapter.LocalizationPerspectiveAdapter;
import com.raytheon.uf.viz.localization.filetreeview.FileTreeEntryData;
import com.raytheon.uf.viz.localization.filetreeview.LocalizationFileEntryData;

/**
 * Adds a new 'Copy Python Classes ...' menu item to the localization context
 * menu when a Python file is selected.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 18, 2013 2461       bkowal      Initial creation
 * Nov 25, 2013 2461       bkowal      Refactor
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class CopyPythonClassesAdapter extends LocalizationPerspectiveAdapter {

    private static final String PY_SUFFIX = "py";

    @Override
    public boolean addContextMenuItems(IMenuManager menuMgr,
            FileTreeEntryData[] selectedData) {
        if (selectedData.length == 1
                && selectedData[0].getClass() == LocalizationFileEntryData.class) {
            LocalizationFileEntryData localizationFileEntryData = (LocalizationFileEntryData) selectedData[0];
            if (localizationFileEntryData.isDirectory() == false
                    && FilenameUtils.isExtension(
                            localizationFileEntryData.getName(), PY_SUFFIX)) {
                CopyPythonClassesAction copyPythonClassesAction = new CopyPythonClassesAction(
                        localizationFileEntryData.getFile());
                menuMgr.add(copyPythonClassesAction);
                menuMgr.add(new Separator());
            }
        }
        return false;
    }
}