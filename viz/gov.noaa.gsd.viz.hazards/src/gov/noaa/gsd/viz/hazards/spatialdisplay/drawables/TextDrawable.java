/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;
import gov.noaa.nws.ncep.ui.pgen.elements.Text;

import java.awt.Color;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Text label drawable in the spatial display.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * April 2011              Bryon.Lawrence      Initial creation
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Nov 23, 2013   1462     Bryon.Lawrence      Set text to bold.
 * Feb 09, 2015 6260       Dan Schaffer        Fixed bugs in multi-polygon handling
 * Mar 16, 2016 15676      Chris.Golden        Added code to support visual features.
 * Mar 24, 2016 15676      Chris.Golden        Added ability to change font size and
 *                                             specify color directly.
 * Mar 26, 2016 15676      Chris.Golden        Added visual feature identifier.
 * Jun 23, 2016 19537      Chris.Golden        Changed to use better identifiers.
 * Jul 25, 2016 19537      Chris.Golden        Renamed, and removed unneeded member
 *                                             data and methods.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class TextDrawable extends Text implements IDrawable {

    // Private Static Constants

    /**
     * Text PGEN category.
     */
    private static final String TEXT = "TEXT";

    // Private Variables

    private final IEntityIdentifier identifier;

    private final int geometryIndex;

    // Public Constructors

    public TextDrawable(IEntityIdentifier identifier,
            DrawableAttributes drawingAttributes, float pointSize, Color color,
            Coordinate location, Layer activeLayer) {
        this.identifier = identifier;
        this.geometryIndex = drawingAttributes.getGeometryIndex();
        setPgenType(TEXT);
        setParent(activeLayer);
        setText(drawingAttributes.getLabel());
        setColors(new Color[] { color });
        setFontSize(pointSize);
        setStyle(FontStyle.BOLD);

        /*
         * Update the position of this text object relative to the centroid of
         * the hazard area.
         */
        setLocation(drawingAttributes.getTextPosition().getLabelPosition(
                location));
    }

    // Public Methods

    @Override
    public DisplayType getDisplayType() {
        return DisplayType.NORMAL;
    }

    @Override
    public Boolean maskText() {
        return false;
    }

    @Override
    public Geometry getGeometry() {
        return null;
    }

    @Override
    public IEntityIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setEditable(boolean editable) {
        throw new UnsupportedOperationException("text is not editable");
    }

    @Override
    public boolean isMovable() {
        return false;
    }

    @Override
    public void setMovable(boolean movable) {
        throw new UnsupportedOperationException("text is not movable.");
    }

    @Override
    public int getGeometryIndex() {
        return geometryIndex;
    }
}
