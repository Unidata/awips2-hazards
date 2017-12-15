/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.common.dataplugin.events.hazards.event;

/**
 * Description: Interface describing the methods that must be implemented by a
 * describer of a hazard event parameter. Instances of this interface may be
 * passed a hazard event and return a {@link String} description of a particular
 * aspect of the event.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 10, 2015    6393    Chris.Golden Initial creation.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IHazardEventParameterDescriber {

    /**
     * Get the description of whatever parameter from the specified hazard event
     * that this instance is meant to extract.
     * 
     * @param event
     *            Hazard event from which to get the parameter to be described.
     * @return Description of the appropriate parameter, or <code>null</code> if
     *         no such parameter exists.
     */
    public String getDescription(IReadableHazardEvent event);
}
