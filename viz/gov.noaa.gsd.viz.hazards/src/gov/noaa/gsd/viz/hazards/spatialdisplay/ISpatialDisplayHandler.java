/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger;

/**
 * Description: Interface describing the methods that must be implemented by
 * classes that are to act as spatial display handlers, dealing with
 * notifications from the spatial display about disposal, etc.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 08, 2016   15937    Chris.Golden Initial creation.
 * Nov 03, 2016   22960    bkowal       Notify the user if a gage is not selected.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ISpatialDisplayHandler extends IMessenger {

    /**
     * Handle notification that the spatial display was disposed of.
     */
    public void spatialDisplayDisposed();
}
