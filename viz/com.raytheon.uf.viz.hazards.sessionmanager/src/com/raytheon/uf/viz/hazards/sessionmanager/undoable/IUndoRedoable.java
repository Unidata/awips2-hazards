/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.undoable;

/**
 * Description: Interface implemented by objects which support undo/redo
 * operations on their state or a portion of their state.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 5, 2013            Bryon.Lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public interface IUndoRedoable {

    /**
     * Undo a change to the state or a portion of the state of an implementing
     * object
     * 
     * @param
     * @return
     */
    public void undo();

    /**
     * Redo a change to the state or a portion of the state of an implementating
     * object.
     * 
     * @param
     * @return
     */
    public void redo();

    /**
     * Method for testing if any portion of the implementing object's state may
     * be undone.
     * 
     * @param
     * @return Whether or not undo operations are available.
     */
    public Boolean isUndoable();

    /**
     * Method for testing if any portion of the implementing object's state may
     * be redone.
     * 
     * @param
     * @return Whether or not redo operations are available.
     */
    public Boolean isRedoable();

    /**
     * Method for clearing the undo/redo operations.
     * 
     * @param
     * @return
     */
    public void clearUndoRedo();
}
