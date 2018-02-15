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
package com.raytheon.uf.common.dataplugin.events.hazards.undoable;

/**
 * Description: Interface implemented to manager multiple IUndoRedoable objects.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 23, 2017 29170      Robert.Blum Initial creation
 *
 * </pre>
 *
 * @author Robert.Blum
 */

public interface IUndoRedoManager extends IUndoRedoable {

    /**
     * Add a undoable change to the manager.
     * 
     * @param undo
     */
    public void addUndo(IUndoRedoable undo);

    /**
     * Add a redoable change to the manager.
     * 
     * @param redo
     */
    public void addRedo(IUndoRedoable redo);

}
