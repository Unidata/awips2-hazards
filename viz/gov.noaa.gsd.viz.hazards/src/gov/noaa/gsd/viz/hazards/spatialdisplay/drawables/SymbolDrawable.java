/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.display.SymbolImageUtil;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;
import gov.noaa.nws.ncep.ui.pgen.elements.Symbol;

import java.util.List;

import org.eclipse.swt.graphics.Rectangle;

import com.google.common.collect.Lists;
import com.raytheon.uf.viz.core.IDisplayPane;
import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

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
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class SymbolDrawable extends Symbol implements IDrawable {

    private final IEntityIdentifier identifier;

    private boolean movable = true;

    private Geometry geometry = null;

    private final int geometryIndex;

    /**
     * Creates an IHISSymbol.
     * 
     * @param identifier
     *            Identifier of this symbol.
     * @param drawingAttributes
     *            Attributes controlling the appearance of this symbol.
     * @param pgenType
     *            The PGEN type of this symbol, indicating the symbol geometry.
     * @param location
     *            Lat-lon location of this symbol.
     * @param activeLayer
     *            PGEN layer to which this this symbol will be drawn.
     */
    public SymbolDrawable(IEntityIdentifier identifier,
            DrawableAttributes drawingAttributes, String pgenType,
            Coordinate location, Layer activeLayer) {
        this.identifier = identifier;
        this.geometryIndex = drawingAttributes.getGeometryIndex();
        setLocation(location);
        setPgenCategory(pgenType);
        setPgenType(pgenType);
        setParent(activeLayer);
        setColors(drawingAttributes.getColors());
        setLineWidth(drawingAttributes.getLineWidth());
        setSizeScale(drawingAttributes.getSizeScale());
        updateEnclosingGeometry();
    }

    // Public Methods

    @Override
    public IEntityIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public Geometry getGeometry() {
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

    // Private Methods

    /**
     * Update the geometry, which represents the selectable area associated with
     * this symbol.
     */
    private void updateEnclosingGeometry() {

        /*
         * Build the enclosing geometry for selection purposes.
         */
        double deviceScale;// = 0.3713365;
        double symbolScale = 0.03;

        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());
        IDisplayPane displayPane = editor.getActiveDisplayPane();
        displayPane.getBounds();
        displayPane.getRenderableDisplay().getView();

        IExtent pe = displayPane.getRenderableDisplay().getView().getExtent();
        deviceScale = pe.getHeight() / 300.0;

        /*
         * Set the screen to pixel ratio.
         */
        Rectangle bounds = displayPane.getBounds();

        double screenToWorldRatio = bounds.width / pe.getWidth();
        double sfactor = this.getSizeScale() * screenToWorldRatio * deviceScale
                * symbolScale;
        double imageSize = SymbolImageUtil.INITIAL_IMAGE_SIZE
                * Math.ceil(sfactor);

        imageSize /= 2;

        /*
         * Translate the center point to screen pixels.
         */
        double[] centerPointPixels = editor.translateInverseClick(this
                .getLocation());
        double lowerY = centerPointPixels[1] - imageSize;
        double upperY = centerPointPixels[1] + imageSize;
        double lowerX = centerPointPixels[0] - imageSize;
        double upperX = centerPointPixels[0] + imageSize;

        GeometryFactory gf = new GeometryFactory();

        List<Coordinate> drawnPoints = Lists.newArrayList();
        drawnPoints.add(editor.translateClick(lowerX, lowerY));
        drawnPoints.add(editor.translateClick(upperX, lowerY));
        drawnPoints.add(editor.translateClick(upperX, upperY));
        drawnPoints.add(editor.translateClick(lowerX, upperY));
        drawnPoints.add(drawnPoints.get(0));

        /*
         * Do not attempt to draw anything that is outside of the grid extent of
         * the display. Typically, translateClick() will return null for a point
         * that is outside of its grid extent. This seems to be mainly a problem
         * in the GFE perspective.
         */
        if (!drawnPoints.contains(null)) {
            LinearRing ls = gf.createLinearRing(drawnPoints
                    .toArray(new Coordinate[0]));
            geometry = gf.createPolygon(ls, null);
        } else {
            geometry = null;
        }
    }
}
