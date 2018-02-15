/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.undoable;

/**
 * Description: Interface implemented by objects which support undo/redo
 * operations on their state or a portion of their state.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer       Description
 * ------------ ---------- -------------- --------------------------
 * Aug 05, 2013            Bryon.Lawrence Initial creation.
 * Feb 23, 2017  29170     Chris.Golden   Moved to different plugin.
 * Dec 17, 2017  20739     Chris.Golden   Refactored away access to directly
 *                                        mutable session events.
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public interface IUndoRedoable {

    /**
     * Undo a change to the state or a portion of the state of an implementing
     * object.
     * 
     * @return <code>true<code> if the undo was successful, <code>false</code>
     *         otherwise.
     */
    public boolean undo();

    /**
     * Redo a change to the state or a portion of the state of an implementating
     * object.
     * 
     * @return <code>true<code> if the redo was successful, <code>false</code>
     *         otherwise.
     */
    public boolean redo();

    /**
     * Determine whether or not any portion of the implementing object's state
     * may be redone.
     * 
     * @return <code>true</code> if an undo is possible, <code>false</code>
     *         otherwise.
     */
    public boolean isUndoable();

    /**
     * Determine whether or not any portion of the implementing object's state
     * may be redone.
     * 
     * @return <code>true</code> if a redo is possible, <code>false</code>
     *         otherwise.
     */
    public boolean isRedoable();

    /**
     * Clear the undo/redo operations.
     */
    public void clearUndoRedo();
}
