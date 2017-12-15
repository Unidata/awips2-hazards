/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.entities;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;

import gov.noaa.gsd.common.visuals.SpatialEntity;

/**
 * Description: Interface describing the methods that must be implemented by
 * classes acting as identifiers for {@link SpatialEntity} objects used to
 * represent {@link IHazardEventView} objects in the spatial display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 27, 2016   19537    Chris.Golden Initial creation.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IHazardEventEntityIdentifier extends IEntityIdentifier {

    /**
     * Get the event identifier.
     * 
     * @return Event identifier.
     */
    public String getEventIdentifier();
}
