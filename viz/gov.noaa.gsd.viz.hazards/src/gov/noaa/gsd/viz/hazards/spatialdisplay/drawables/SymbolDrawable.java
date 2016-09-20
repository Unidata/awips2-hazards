/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.GeometryWrapper;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.elements.Symbol;

/**
 * Single-lat-lon-location symbol drawable.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * April 2011              Bryon.Lawrence      Initial creation
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Feb 09, 2015 6260       Dan Schaffer        Fixed bugs in multi-polygon handling
 * Mar 16, 2016 15676      Chris.Golden        Added code to support visual features.
 * Mar 26, 2016 15676      Chris.Golden        Added visual feature identifier.
 * Jun 23, 2016 19537      Chris.Golden        Changed to use better identifiers.
 * Jul 25, 2016 19537      Chris.Golden        Renamed, and removed unneeded member
 *                                             data and methods.
 * Aug 22, 2016 19537      Chris.Golden        Removed unneeded layer constructor
 *                                             parameter. Also added toString()
 *                                             method.
 * Sep 12, 2016 15934      Chris.Golden        Changed to work with advanced
 *                                             geometries. Also removed code that
 *                                             was designed to make the drawable's
 *                                             geometry a polygon so that hit tests
 *                                             could be done using it; hit testing
 *                                             is the job of the spatial display, not
 *                                             the individual drawables, which merely
 *                                             supply their true geometries.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class SymbolDrawable extends Symbol implements
        IDrawable<GeometryWrapper> {

    private final IEntityIdentifier identifier;

    private boolean movable = true;

    private GeometryWrapper geometry = null;

    private final int geometryIndex;

    /**
     * Creates a symbol.
     * 
     * @param identifier
     *            Identifier of this symbol.
     * @param drawingAttributes
     *            Attributes controlling the appearance of this symbol.
     * @param pgenType
     *            The PGEN type of this symbol, indicating the symbol geometry.
     * @param geometry
     *            Point geometry for this symbol.
     */
    public SymbolDrawable(IEntityIdentifier identifier,
            DrawableAttributes drawingAttributes, String pgenType,
            GeometryWrapper geometry) {
        this.identifier = identifier;
        this.geometry = geometry;
        this.geometryIndex = drawingAttributes.getGeometryIndex();
        setLocation(AdvancedGeometryUtilities.getCentroid(geometry));
        setPgenCategory(pgenType);
        setPgenType(pgenType);
        setColors(drawingAttributes.getColors());
        setLineWidth(drawingAttributes.getLineWidth());
        setSizeScale(drawingAttributes.getSizeScale());
    }

    // Public Methods

    @Override
    public IEntityIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public GeometryWrapper getGeometry() {
        return geometry;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setEditable(boolean editable) {
        throw new UnsupportedOperationException("symbols cannot be editable");
    }

    @Override
    public boolean isMovable() {
        return movable;
    }

    @Override
    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    @Override
    public int getGeometryIndex() {
        return geometryIndex;
    }

    @Override
    public String toString() {
        return getIdentifier() + " (symbol = \"" + getPgenType() + "\")";
    }
}
