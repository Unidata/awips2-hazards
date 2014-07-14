/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.modifiable;

/**
 * Description: Interface implemented by objects whose states can be modified
 * and this information needs to be conveyed to clients.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 29, 2013            Bryon.Lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public interface IModifiable {

    /**
     * Tests whether or not the implementing object has been modified.
     * 
     * @param
     * @return true - A portion of the state of the implementing object has been
     *         modified, false - the state of the implementing object has not
     *         been modified.
     */
    public boolean isModified();

}
