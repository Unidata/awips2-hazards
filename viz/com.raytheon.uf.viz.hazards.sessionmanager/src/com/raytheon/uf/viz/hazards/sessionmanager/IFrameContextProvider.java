/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager;

import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;

/**
 * Description: Interface describing the methods that must be implemented to act
 * as a provider of temporal frames context.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 27, 2015   12762    Chris.Golden Initial creation.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IFrameContextProvider {

    /**
     * Get the frame context that currently applies.
     * 
     * @return Frame context, or <code>null</code> if none can be found.
     */
    public FramesInfo getFramesInfo();
}
