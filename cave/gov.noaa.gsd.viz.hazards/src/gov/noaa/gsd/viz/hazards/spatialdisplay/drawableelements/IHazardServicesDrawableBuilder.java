/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements;

import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.Layer;

import java.util.List;

import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * 
 * Description: Contains factory methods for building the PGEN drawables
 * displayed in Hazard Services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * March 21, 2013          Bryon.Lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public interface IHazardServicesDrawableBuilder {
    /**
     * Builds a PGEN drawable representing a single point.
     * 
     * @param shape
     *            Dictionary containing attributes for the shape to be drawn.
     * @param eventID
     *            The id of the event this shape is a part of.
     * @param points
     *            The coordinates which define this geometry.
     * @param activeLayer
     *            The PGEN active layer to draw to.
     * @param descriptor
     *            The map descriptor, required for projection conversions.
     * 
     * @return a PGEN drawable.
     */
    public AbstractDrawableComponent buildDrawableComponent(Dict shape,
            String eventID, List<Coordinate> points, Layer activeLayer,
            MapDescriptor descriptor);

    /**
     * Builds a PGEN drawable representing a single point.
     * 
     * @param shape
     *            Dictionary containing attributes for the shape to be drawn.
     * @param eventID
     *            The id of the event this shape is a part of.
     * @param points
     *            The coordinates which define this geometry.
     * @param activeLayer
     *            The PGEN active layer to draw to.
     * @return a PGEN drawable.
     */
    public AbstractDrawableComponent buildPoint(Dict shape, String eventID,
            List<Coordinate> points, Layer activeLayer);

    /**
     * Builds a PGEN drawable representing a line.
     * 
     * @param shape
     *            Dictionary containing attributes for the shape to be drawn.
     * @param eventID
     *            The id of the event this shape is a part of.
     * @param points
     *            The coordinates which define this geometry.
     * @param activeLayer
     *            The PGEN active layer to draw to.
     * 
     * @return a PGEN drawable.
     */
    public AbstractDrawableComponent buildLine(Dict shape, String eventID,
            List<Coordinate> points, Layer activeLayer);

    /**
     * Builds a PGEN drawable representing a polygon
     * 
     * @param shape
     *            Dictionary containing attributes for the shape to be drawn.
     * @param eventID
     *            The id of the event this shape is a part of.
     * @param points
     *            The coordinates which define this geometry.
     * @param activeLayer
     *            The PGEN active layer to draw to.
     * 
     * @return a PGEN drawable.
     */
    public AbstractDrawableComponent buildPolygon(Dict shape, String eventID,
            List<Coordinate> points, Layer activeLayer);

    /**
     * Builds a PGEN drawable representing a circle.
     * 
     * @param shape
     *            Dictionary containing attributes for the shape to be drawn.
     * @param eventID
     *            The id of the event this shape is a part of.
     * @param points
     *            The coordinates which define this geometry.
     * @param activeLayer
     *            The PGEN active layer to draw to.
     * 
     * @param descriptor
     *            The map descriptor, required for projection conversions.
     * 
     * @return a PGEN drawable.
     */
    public AbstractDrawableComponent buildCircle(Dict shape, String eventID,
            List<Coordinate> points, Layer activeLayer, MapDescriptor descriptor);

    /**
     * Builds a PGEN drawable representing a dot
     * 
     * @param shape
     *            Dictionary containing attributes for the shape to be drawn.
     * @param eventID
     *            The id of the event this shape is a part of.
     * @param points
     *            The coordinates which define this geometry.
     * @param activeLayer
     *            The PGEN active layer to draw to.
     * 
     * @return a PGEN drawable.
     */
    public AbstractDrawableComponent buildDot(Dict shape, String eventID,
            List<Coordinate> points, Layer activeLayer);

    /**
     * Builds a PGEN drawable representing a star
     * 
     * @param shape
     *            Dictionary containing attributes for the shape to be drawn.
     * @param eventID
     *            The id of the event this shape is a part of.
     * @param points
     *            The coordinates which define this geometry.
     * @param activeLayer
     *            The PGEN active layer to draw to.
     * 
     * @return a PGEN drawable.
     */
    public AbstractDrawableComponent buildStar(Dict shape, String eventID,
            List<Coordinate> points, Layer activeLayer);

    /**
     * Builds a PGEN drawable representing text.
     * 
     * @param shape
     *            Dictionary containing attributes for the shape to be drawn.
     * @param eventID
     *            The id of the event this shape is a part of.
     * @param points
     *            The coordinates which define this geometry.
     * @param activeLayer
     *            The PGEN active layer to draw to.
     * @param geoFactory
     *            Geometry factory for creating points, polygons, etc.
     * 
     * @return a PGEN drawable.
     */
    public AbstractDrawableComponent buildText(Dict shape, String eventID,
            List<Coordinate> points, Layer activeLayer,
            GeometryFactory geoFactory);

}
