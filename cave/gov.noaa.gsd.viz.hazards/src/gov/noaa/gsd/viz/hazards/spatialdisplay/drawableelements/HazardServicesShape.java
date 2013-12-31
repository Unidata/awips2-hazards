/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements;

import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAttributes;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

/**
 * Description: A hazard services drawing shape.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 23, 2013   1264      daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public abstract class HazardServicesShape extends Line implements
        IHazardServicesShape {

    private String id;

    private boolean isEditable = true;

    private boolean isMovable = true;

    private final HazardServicesDrawingAttributes drawingAttributes;

    @Override
    public HazardServicesDrawingAttributes getDrawingAttributes() {
        return drawingAttributes;
    }

    public HazardServicesShape(String id,
            HazardServicesDrawingAttributes drawingAttributes) {
        this.id = id;
        this.drawingAttributes = drawingAttributes;
    }

    @Override
    public void setID(String id) {
        this.id = id;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public abstract LineString getEditableVertices();

    @Override
    public abstract Geometry getGeometry();

    @Override
    public boolean isEditable() {
        return isEditable;
    }

    @Override
    public void setIsEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    @Override
    public boolean isMovable() {
        return isMovable;
    }

    @Override
    public void setMovable(boolean isMovable) {
        this.isMovable = isMovable;
    }

}
