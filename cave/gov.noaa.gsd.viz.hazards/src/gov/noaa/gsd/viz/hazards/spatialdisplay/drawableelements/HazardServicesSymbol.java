/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements;

import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAttributes;
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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

/**
 * Base class for Symbols drawn in Hazard Services. This allows Hazard Services
 * to use the rich palette of symbols offered by PGEN.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * April 2011              Bryon.Lawrence      Initial creation
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class HazardServicesSymbol extends Symbol implements
        IHazardServicesShape {

    /**
     * Drawable attributes.
     */
    private final HazardServicesDrawingAttributes drawingAttributes;

    /**
     * Identifier of the hazard event that this symbol is part of. All drawable
     * components of the hazard share this ID.
     */
    private String id;

    /**
     * unique identifier for this symbol. Helps to distinguish it from other
     * shapes in the hazard.
     */
    private final long pointID;

    /**
     * Flag indicating whether or not this symbol is movable.
     */
    private boolean isMovable = true;

    private Geometry geometry = null;

    /**
     * Creates an IHISSymbol.
     * 
     * @param drawingAttributes
     *            The attributes controlling the appearance of this Symbol.
     * @param pgenCategory
     *            The PGEN category of this symbol, e.g.
     * @param pgenType
     *            The PGEN type of this symbol, e.g.
     * @param points
     *            The points in this symbol
     * @param activeLayer
     *            The PGEN layer this symbol will be drawn to.
     * @param id
     *            The ID of this symbol.
     */
    public HazardServicesSymbol(
            HazardServicesDrawingAttributes drawingAttributes,
            String pgenCategory, String pgenType, List<Coordinate> points,
            Layer activeLayer, String id) {
        this.id = id;
        this.drawingAttributes = drawingAttributes;
        setLocation(points.get(0));
        update(drawingAttributes);
        setPgenCategory(pgenCategory);
        setPgenType(pgenType);
        setParent(activeLayer);
        setColors(drawingAttributes.getColors());
        setLineWidth(drawingAttributes.getLineWidth());
        setSizeScale(drawingAttributes.getSizeScale());
        pointID = drawingAttributes.getPointID();
        updateEnclosingGeometry();
    }

    @Override
    public HazardServicesDrawingAttributes getDrawingAttributes() {
        return drawingAttributes;
    }

    @Override
    public void setID(String id) {
        this.id = id;
    }

    @Override
    public String getID() {
        return id;
    }

    public Point getPoint() {
        GeometryFactory gf = new GeometryFactory();
        return gf.createPoint(getPoints().get(0));
    }

    /**
     * Updates the JTS geometry which represents the selectable area associated
     * with this symbol.
     * 
     * @param props
     * @return
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
         * Set the screen to pixel ratio
         */
        Rectangle bounds = displayPane.getBounds();

        double screenToWorldRatio = bounds.width / pe.getWidth();
        double sfactor = this.getSizeScale() * screenToWorldRatio * deviceScale
                * symbolScale;
        double imageSize = SymbolImageUtil.INITIAL_IMAGE_SIZE
                * Math.ceil(sfactor);

        imageSize /= 2;

        /*
         * Translate the center point to screen pixels
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
         * Don't try to draw anything that is outside of the grid extent of the
         * display. Typically, translateClick will return null for a point that
         * is outside of its grid extent. This seems to be mainly a problem in
         * the GFE perspective.
         */

        if (!drawnPoints.contains(null)) {
            LinearRing ls = gf.createLinearRing(drawnPoints
                    .toArray(new Coordinate[0]));
            geometry = gf.createPolygon(ls, null);
        } else {
            geometry = null;
        }
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public LineString getEditableVertices() {
        return null;
    }

    /**
     * @return the pointID associated with this Symbol. The pointID is used in
     *         operations such as tracking.
     */
    public long getPointID() {
        return pointID;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setIsEditable(boolean isEditable) {
        throw new UnsupportedOperationException("Symbols are never editable");
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
