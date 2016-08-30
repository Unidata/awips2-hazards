/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea;

import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;

import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Description: Information about the modification of a geometry via
 * select-by-area.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 15, 2016   19537    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SelectByAreaContext {

    // Private Variables

    /**
     * Identifier of the entity being edited using select-by-area; if
     * <code>null</code>, a new geometry is to be created.
     */
    private final IEntityIdentifier identifier;

    /**
     * Select-by-area geometries selected for the entity.
     */
    private final Set<Geometry> selectedGeometries;

    /**
     * Database table name.
     */
    private final String databaseTableName;

    /**
     * Legend text.
     */
    private final String legend;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param identifier
     *            Identifier of the entity being edited using select-by-area; if
     *            <code>null</code>, a new geometry is to be created.
     * @param selectedGeometries
     *            Select-by-area geometries selected for the entity, if any.
     * @param databaseTableName
     *            Database table name.
     * @param legend
     *            Legend text.
     */
    public SelectByAreaContext(IEntityIdentifier identifier,
            Set<Geometry> selectedGeometries, String databaseTableName,
            String legend) {
        this.identifier = identifier;
        this.selectedGeometries = selectedGeometries;
        this.databaseTableName = databaseTableName;
        this.legend = legend;
    }

    // Public Methods

    /**
     * Get the identifier of the entity that is being edited using
     * select-by-area, if editing is occurring.
     * 
     * @return Identifier; if <code>null</code>, no entity is being edited, and
     *         instead, a new geometry is to be created.
     */
    public IEntityIdentifier getIdentifier() {
        return identifier;
    }

    /**
     * Get the select-by-area geometries selected for the entity.
     * 
     * @return Select-by-area geometries selected for the entity.
     */
    public Set<Geometry> getSelectedGeometries() {
        return selectedGeometries;
    }

    /**
     * Get the database table name.
     * 
     * @return Database table name.
     */
    public String getDatabaseTableName() {
        return databaseTableName;
    }

    /**
     * Get the legend text.
     * 
     * @return Legend text.
     */
    public String getLegend() {
        return legend;
    }
}
