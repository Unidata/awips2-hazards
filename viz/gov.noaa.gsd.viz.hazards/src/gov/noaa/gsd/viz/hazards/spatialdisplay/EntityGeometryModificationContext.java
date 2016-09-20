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

import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;

import java.util.Date;

/**
 * Description: Information about the modification of an entity's geometry.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 29, 2016   19537    Chris.Golden Initial creation.
 * Sep 12, 2016   15934    Chris.Golden Changed to work with advanced
 *                                      geometries.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class EntityGeometryModificationContext {

    // Private Variables

    /**
     * Identifier of the entity that is to have its geometry changed.
     */
    private final IEntityIdentifier identifier;

    /**
     * New geometry for the entity.
     */
    private final IAdvancedGeometry geometry;

    /**
     * Selected time for which this change occurred.
     */
    private final Date selectedTime;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param identifier
     *            Identifier of the entity that is to have its geometry changed.
     * @param geometry
     *            New geometry for the entity.
     * @param selectedTime
     *            Selected time for which this change occurred.
     */
    public EntityGeometryModificationContext(IEntityIdentifier identifier,
            IAdvancedGeometry geometry, Date selectedTime) {
        this.identifier = identifier;
        this.geometry = geometry;
        this.selectedTime = selectedTime;
    }

    // Public Methods

    /**
     * Get the identifier of the entity that is to be have its geometry changed.
     * 
     * @return Identifier of the entity.
     */
    public IEntityIdentifier getIdentifier() {
        return identifier;
    }

    /**
     * Get the new geometry to be used for the entity.
     * 
     * @return New geometry to be used for the entity.
     */
    public IAdvancedGeometry getGeometry() {
        return geometry;
    }

    /**
     * Get the selected time for which this change occurred
     * 
     * @return Selected time for which this change occurred.
     */
    public Date getSelectedTime() {
        return selectedTime;
    }
}
