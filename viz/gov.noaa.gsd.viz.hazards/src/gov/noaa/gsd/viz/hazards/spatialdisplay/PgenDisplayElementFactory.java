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

import gov.noaa.nws.ncep.ui.pgen.PgenRangeRecord;
import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.contours.ContourCircle;
import gov.noaa.nws.ncep.ui.pgen.contours.ContourLine;
import gov.noaa.nws.ncep.ui.pgen.contours.ContourMinmax;
import gov.noaa.nws.ncep.ui.pgen.display.ArcPatternApplicator;
import gov.noaa.nws.ncep.ui.pgen.display.ArrowHead;
import gov.noaa.nws.ncep.ui.pgen.display.ArrowHead.ArrowHeadType;
import gov.noaa.nws.ncep.ui.pgen.display.CornerPatternApplicator;
import gov.noaa.nws.ncep.ui.pgen.display.CornerPatternApplicator.CornerPattern;
import gov.noaa.nws.ncep.ui.pgen.display.CurveFitter;
import gov.noaa.nws.ncep.ui.pgen.display.DisplayElementFactory;
import gov.noaa.nws.ncep.ui.pgen.display.FillDisplayElement;
import gov.noaa.nws.ncep.ui.pgen.display.FillPatternList;
import gov.noaa.nws.ncep.ui.pgen.display.FillPatternList.FillPattern;
import gov.noaa.nws.ncep.ui.pgen.display.IArc;
import gov.noaa.nws.ncep.ui.pgen.display.IAvnText;
import gov.noaa.nws.ncep.ui.pgen.display.IDisplayable;
import gov.noaa.nws.ncep.ui.pgen.display.ILine;
import gov.noaa.nws.ncep.ui.pgen.display.IMidCloudText;
import gov.noaa.nws.ncep.ui.pgen.display.IMultiPoint;
import gov.noaa.nws.ncep.ui.pgen.display.ISinglePoint;
import gov.noaa.nws.ncep.ui.pgen.display.ISymbol;
import gov.noaa.nws.ncep.ui.pgen.display.ISymbolSet;
import gov.noaa.nws.ncep.ui.pgen.display.IText;
import gov.noaa.nws.ncep.ui.pgen.display.IText.FontStyle;
import gov.noaa.nws.ncep.ui.pgen.display.IText.TextRotation;
import gov.noaa.nws.ncep.ui.pgen.display.LinePattern;
import gov.noaa.nws.ncep.ui.pgen.display.LinePatternException;
import gov.noaa.nws.ncep.ui.pgen.display.LinePatternManager;
import gov.noaa.nws.ncep.ui.pgen.display.PatternSegment;
import gov.noaa.nws.ncep.ui.pgen.display.SymbolImageUtil;
import gov.noaa.nws.ncep.ui.pgen.display.SymbolSetElement;
import gov.noaa.nws.ncep.ui.pgen.display.TextDisplayElement;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;
import gov.noaa.nws.ncep.ui.pgen.elements.Symbol;
import gov.noaa.nws.ncep.ui.pgen.elements.SymbolLocationSet;
import gov.noaa.nws.ncep.ui.pgen.elements.Text;
import gov.noaa.nws.ncep.ui.pgen.gfa.GfaClip;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import com.raytheon.uf.common.geospatial.util.WorldWrapCorrector;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.DrawableString;
import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.HorizontalAlignment;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
import com.raytheon.uf.viz.core.IGraphicsTarget.VerticalAlignment;
import com.raytheon.uf.viz.core.PixelExtent;
import com.raytheon.uf.viz.core.data.IRenderedImageCallback;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.IFont;
import com.raytheon.uf.viz.core.drawables.IFont.Style;
import com.raytheon.uf.viz.core.drawables.IImage;
import com.raytheon.uf.viz.core.drawables.IShadedShape;
import com.raytheon.uf.viz.core.drawables.IWireframeShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.IMapDescriptor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.linearref.LengthLocationMap;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

/**
 * Description: Copy of the {@link DisplayElementFactory}, with functionality
 * unneeded by the spatial display stripped out, and generating any line
 * displayables with their color's alpha component honored (meaning lines may be
 * translucent).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 09, 2016   15676    Chris.Golden Initial creation.
 * Mar 22, 2016   15676    Chris.Golden Fixed use of alpha transparency
 *                                      for line style patterns that are
 *                                      filled (e.g. filled circles), and
 *                                      for polygon fills.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class PgenDisplayElementFactory {

    // Private Classes

    /**
     * Line display element that honors its specified color's alpha component by
     * painting itself in a translucent manner if appropriate.
     */
    private class AlphaCapableLineDisplayElement implements IDisplayable {

        // Private Variables

        /**
         * The line segments to be displayed.
         */
        private final IWireframeShape shape;

        /**
         * Color of the line segments.
         */
        private final Color color;

        /**
         * Thickness of the line segments.
         */
        private final float lineWidth;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param shape
         *            Set of line segments.
         * @param color
         *            Color in which to display the line segments.
         * @param lineWidth
         *            Desired line thickness.
         */
        public AlphaCapableLineDisplayElement(IWireframeShape shape,
                Color color, float lineWidth) {
            this.shape = shape;
            this.color = color;
            this.lineWidth = lineWidth;
        }

        // Public Methods

        @Override
        public void draw(IGraphicsTarget target, PaintProperties paintProps) {
            try {
                target.drawWireframeShape(
                        shape,
                        new RGB(color.getRed(), color.getGreen(), color
                                .getBlue()), lineWidth, LineStyle.SOLID, (color
                                .getAlpha()) / 255.0f);
            } catch (VizException e) {
                statusHandler.error("Cannot draw wireframe shape.", e);
            }
        }

        @Override
        public void dispose() {
            shape.dispose();
        }
    }

    /**
     * Arc pattern applicator subclass that allows access to segment points.
     */
    private class PgenArcPatternApplicator extends ArcPatternApplicator {

        public PgenArcPatternApplicator(LengthIndexedLine line,
                double startLoc, double endLoc) {
            super(line, startLoc, endLoc);
        }

        @Override
        public double[][] getSegmentPts() {
            return super.getSegmentPts();
        }
    }

    /**
     * Corner pattern applicator subclass that allows access to segment points.
     */
    private class PgenCornerPatternApplicator extends CornerPatternApplicator {

        public PgenCornerPatternApplicator(LengthIndexedLine line,
                double startLoc, double endLoc) {
            super(line, startLoc, endLoc);
        }

        @Override
        public double[][] getSegmentPts() {
            return super.getSegmentPts();
        }
    }

    /**
     * Symbol image callback.
     */
    private class SymbolImageCallback implements IRenderedImageCallback {
        private final String patternName;

        private final double scale;

        private final float lineWidth;

        private final boolean mask;

        private final Color color;

        public SymbolImageCallback(String patternName, double scale,
                float lineWidth, boolean mask, Color color) {
            super();
            this.patternName = patternName;
            this.scale = scale;
            this.lineWidth = lineWidth;
            this.mask = mask;
            this.color = color;
        }

        @Override
        public RenderedImage getImage() throws VizException {
            return SymbolImageUtil.createBufferedImage(patternName, scale,
                    lineWidth, mask, color);
        }
    }

    /**
     * {@link LinePattern} segment scale types, used to rescale
     * <code>LinePattern</code> objects so that the line ends with a full
     * pattern.
     */
    private enum ScaleType {
        SCALE_ALL_SEGMENTS, SCALE_BLANK_LINE_ONLY
    };

    // Private Static Constants

    /**
     * Symbol scale.
     */
    private static final double SYMBOL_SCALE = 0.65;

    // Private Static Variables

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(PgenDisplayElementFactory.class);

    // Private Variables

    /**
     * Graphics target used to create the wireframe and shaded shapes.
     */
    private final IGraphicsTarget target;

    /**
     * Descriptor used for lat/lon to pixel coordinate transformations.
     */
    private final IDescriptor descriptor;

    /**
     * Geometry factory.
     */
    private final GeometryFactory geometryFactory;

    /**
     * Array of wireframe shapes used to hold all line segments to be drawn.
     */
    private IWireframeShape[] wireframeShapes;

    /**
     * Shaded shape to hold all the filled areas to be drawn.
     */
    private IShadedShape shadedShape;

    /**
     * Wireframe shape used for symbols.
     */
    private IWireframeShape wireframeSymbol;

    /**
     * Line element for which displayables are being generated.
     */
    private ILine element;

    /**
     * Device scale.
     */
    private double deviceScale = 25.0;

    /**
     * Screen to extent conversion factor.
     */
    private double screenToExtent = 1.0;

    /**
     * Screen to world ratio.
     */
    private double screenToWorldRatio = 1.0;

    /**
     * Arrow head.
     */
    private ArrowHead arrow;

    /**
     * Color mode used to draw all elements in a layer.
     */
    private Boolean layerMonoColor = false;

    /**
     * Color used to draw all elements in a layer.
     */
    private Color layerColor = null;

    /**
     * Fill mode used to draw all elements in a layer.
     */
    private Boolean layerFilled = false;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param target
     *            Graphics target.
     * @param mapDescriptor
     *            Map descriptor.
     */
    public PgenDisplayElementFactory(IGraphicsTarget target,
            IMapDescriptor mapDescriptor) {
        this.target = target;
        this.descriptor = mapDescriptor;
        geometryFactory = new GeometryFactory();
    }

    /**
     * Creates a list of displayables for a line object.
     * 
     * @param line
     *            Line for which to create displayables.
     * @param paintProperties
     *            Paint properties associated with the target.
     * @param worldWrap
     *            Flag indicating whether or not the line is to world wrap.
     * @return List of displayables.
     */
    public List<IDisplayable> createDisplayElements(ILine line,
            PaintProperties paintProperties, boolean worldWrap) {

        if (worldWrap) {
            element = line;
            ArrayList<IDisplayable> list = new ArrayList<IDisplayable>();

            WorldWrapCorrector corrector = new WorldWrapCorrector(
                    descriptor.getGridGeometry());

            // put line points in a coordinate array
            Coordinate[] coord;
            if (line.isClosedLine()) {
                coord = new Coordinate[line.getLinePoints().length + 1];
                for (int ii = 0; ii < line.getLinePoints().length; ii++) {
                    coord[ii] = new Coordinate(line.getLinePoints()[ii].x,
                            line.getLinePoints()[ii].y);
                }
                coord[line.getLinePoints().length] = new Coordinate(
                        line.getLinePoints()[0].x, line.getLinePoints()[0].y);
            } else {
                coord = new Coordinate[line.getLinePoints().length];

                for (int ii = 0; ii < line.getLinePoints().length; ii++) {
                    coord[ii] = new Coordinate(line.getLinePoints()[ii].x,
                            line.getLinePoints()[ii].y);
                }

            }

            // apply world wrap.
            // pointsToLineString is in GfaClip. It should be put in a common
            // place
            Geometry geo = null;
            try {
                geo = corrector.correct(GfaClip.getInstance()
                        .pointsToLineString(coord));
            } catch (Exception e) {
                statusHandler.error("World wrap error.", e);
                return list;
            }

            if (geo != null && geo.getNumGeometries() > 1) {
                for (int ii = 0; ii < geo.getNumGeometries(); ii++) {
                    Geometry geo1 = geo.getGeometryN(ii);
                    double[][] pixels = PgenUtil.latlonToPixel(
                            geo1.getCoordinates(), (IMapDescriptor) descriptor);
                    double[][] smoothpts;
                    float density;

                    // Apply parametric smoothing on pixel coordinates, if
                    // required
                    if (line.getSmoothFactor() > 0) {
                        float devScale = 50.0f;
                        if (line.getSmoothFactor() == 1) {
                            density = devScale / 1.0f;
                        } else {
                            density = devScale / 5.0f;
                        }
                        smoothpts = CurveFitter.fitParametricCurve(pixels,
                                density);
                    } else {
                        smoothpts = pixels;
                    }

                    list.addAll(createDisplayElementsForLines(line, smoothpts,
                            paintProperties));

                }

                return list;
            }

        }

        return createDisplayElements(line, paintProperties);

    }

    /**
     * Create the displayables for a text string.
     * 
     * @param text
     *            Text string for which to create displayables.
     * @param paintProperties
     *            Paint properties associated with the target.
     * @return List of displayables.
     */
    public List<IDisplayable> createDisplayElements(IText text,
            PaintProperties paintProperties) {
        setScales(paintProperties);

        /*
         * Create the List to be returned
         */
        ArrayList<IDisplayable> slist = new ArrayList<IDisplayable>();

        /*
         * Skip if the "hide" is true
         */
        if (((Text) text).getHide() != null && ((Text) text).getHide()) {
            return slist;
        }

        /*
         * Skip text labels on contour lines and contour circles - they are
         * drawn all together with the contour line. Also adjust the text
         * position if it is a label for a contour minmax symbol.
         */
        AbstractDrawableComponent tparent = ((Text) text).getParent();
        if (tparent != null) {
            if (tparent instanceof ContourLine
                    || tparent instanceof ContourCircle) {
                return slist;
            } else if (tparent instanceof ContourMinmax) {
                boolean forceAuto = PgenUtil.getContourLabelAutoPlacement();
                if (((Text) text).getAuto() != null && ((Text) text).getAuto()
                        || forceAuto) {
                    Coordinate loc = ((ISinglePoint) ((ContourMinmax) tparent)
                            .getSymbol()).getLocation();
                    double[] pixel = descriptor.worldToPixel(new double[] {
                            loc.x, loc.y, 0.0 });
                    double sfactor = deviceScale
                            * ((ContourMinmax) tparent).getSymbol()
                                    .getSizeScale();

                    pixel[1] = pixel[1] + sfactor * 5;
                    double[] nloc = descriptor.pixelToWorld(new double[] {
                            pixel[0], pixel[1], 0.0 });
                    ((Text) text).setLocationOnly(new Coordinate(nloc[0],
                            nloc[1]));
                    // Only adjust once if auto-place flag in preference is
                    // false.
                    if (!forceAuto) {
                        ((Text) text).setAuto(false);
                    }
                }
            }
        }

        double[] tmp = { text.getPosition().x, text.getPosition().y, 0.0 };
        double[] loc = descriptor.worldToPixel(tmp);

        double horizRatio = paintProperties.getView().getExtent().getWidth()
                / paintProperties.getCanvasBounds().width;
        double vertRatio = paintProperties.getView().getExtent().getHeight()
                / paintProperties.getCanvasBounds().height;
        /*
         * Set background mask and outline
         */
        // TextStyle mask = TextStyle.NORMAL;
        // if ( txt.maskText() && !txt.outlineText() ) {
        // mask = TextStyle.BLANKED;
        // }
        // else if ( txt.maskText() && txt.outlineText() ) {
        // mask = TextStyle.BOXED;
        // }
        // else if ( !txt.maskText() && txt.outlineText() ) {
        // mask = TextStyle.OUTLINE;
        // }

        /*
         * Initialize Font Style[] styles = null; if ( txt.getStyle() != null )
         * { switch ( txt.getStyle() ) { case BOLD: styles = new Style[] {
         * Style.BOLD }; break; case ITALIC: styles = new Style[] {
         * Style.ITALIC}; break; case BOLD_ITALIC: styles = new Style[] {
         * Style.BOLD, Style.ITALIC }; break; } } IFont font =
         * target.initializeFont(txt.getFontName(), txt.getFontSize(), styles);
         */
        IFont font = initializeFont(text.getFontName(), text.getFontSize(),
                text.getStyle());

        /*
         * apply X offset in half-characters
         */
        boolean adjustOffset = false;
        if (text.getXOffset() != 0) {
            double ratio = paintProperties.getView().getExtent().getWidth()
                    / paintProperties.getCanvasBounds().width;
            DrawableString params = new DrawableString(text.getString()[0],
                    null);
            params.font = font;
            Rectangle2D bounds = target.getStringsBounds(params);
            double charSize = ratio * bounds.getWidth()
                    / text.getString()[0].length();
            loc[0] += 0.5 * charSize * text.getXOffset();
            adjustOffset = true;
        }

        /*
         * apply Y offset in half-characters
         */
        if (text.getYOffset() != 0) {
            double ratio = paintProperties.getView().getExtent().getHeight()
                    / paintProperties.getCanvasBounds().height;
            DrawableString params = new DrawableString(text.getString()[0],
                    null);
            params.font = font;
            Rectangle2D bounds = target.getStringsBounds(params);
            double charSize = ratio * bounds.getHeight();
            loc[1] -= 0.5 * charSize * text.getYOffset();
            adjustOffset = true;
        }

        if (adjustOffset) {
            double[] tmp1 = { loc[0], loc[1], 0.0 };
            double[] newloc = descriptor.pixelToWorld(tmp1);
            ((Text) text).setLocationOnly(new Coordinate(newloc[0], newloc[1]));
            ((Text) text).setXOffset(0);
            ((Text) text).setYOffset(0);
        }

        /*
         * Get text color
         */
        Color clr = getDisplayColor(text.getTextColor());
        RGB textColor = new RGB(clr.getRed(), clr.getGreen(), clr.getBlue());

        /*
         * Get angle rotation for text. If rotation is "North" relative,
         * calculate the rotation for "Screen" relative.
         */
        double rotation = text.getRotation();
        if (text.getRotationRelativity() == TextRotation.NORTH_RELATIVE) {
            rotation += getNorthOffsetAngle(text.getPosition());
        }

        /*
         * create drawableString and calculate its bounds
         */
        DrawableString dstring = new DrawableString(text.getString(), textColor);
        dstring.font = font;
        dstring.setCoordinates(loc[0], loc[1]);
        dstring.horizontalAlignment = HorizontalAlignment.CENTER;
        dstring.verticallAlignment = VerticalAlignment.MIDDLE;
        dstring.rotation = rotation;

        Rectangle2D bounds = target.getStringsBounds(dstring);
        double xOffset = (bounds.getWidth() + 1) * horizRatio / 2;
        double yOffset = (bounds.getHeight() + 1) * vertRatio / 2;

        /*
         * Set proper alignment
         */
        HorizontalAlignment align = HorizontalAlignment.CENTER;
        double left = xOffset, right = xOffset;
        if (text.getJustification() != null) {
            switch (text.getJustification()) {
            case RIGHT_JUSTIFY:
                align = HorizontalAlignment.RIGHT;
                left = xOffset * 2;
                right = 0.0;
                break;
            case CENTER:
                align = HorizontalAlignment.CENTER;
                break;
            case LEFT_JUSTIFY:
                align = HorizontalAlignment.LEFT;
                left = 0.0;
                right = xOffset * 2;
                break;
            default:
                align = HorizontalAlignment.CENTER;
                break;
            }
        }

        dstring.horizontalAlignment = align;

        IExtent box = new PixelExtent(dstring.basics.x - left, dstring.basics.x
                + right, dstring.basics.y - yOffset, dstring.basics.y + yOffset);

        /*
         * create new TextDisplayElement and add it to return list
         */
        TextDisplayElement tde = new TextDisplayElement(dstring,
                text.maskText(), text.getDisplayType(), box);
        slist.add(tde);

        return slist;
    }

    /**
     * Create displayable objects for the specified arc.
     * 
     * @param arc
     *            Arc for which to create displayables.
     * @param paintProperties
     *            Paint properties associated with the target.
     * @return List of displayables.
     */
    public List<IDisplayable> createDisplayElements(IArc arc,
            PaintProperties paintProperties) {
        // double sfactor = deviceScale * de.getSizeScale();
        setScales(paintProperties);

        /*
         * Create the List to be returned, and wireframe shape
         */
        ArrayList<IDisplayable> slist = new ArrayList<IDisplayable>();
        IWireframeShape arcpts = target.createWireframeShape(false, descriptor);

        /*
         * Convert center and circumference point from lat/lon to pixel
         * coordinates.
         */
        double[] tmp = { arc.getCenterPoint().x, arc.getCenterPoint().y, 0.0 };
        double[] center = descriptor.worldToPixel(tmp);
        double[] tmp2 = { arc.getCircumferencePoint().x,
                arc.getCircumferencePoint().y, 0.0 };
        double[] circum = descriptor.worldToPixel(tmp2);

        /*
         * calculate angle of major axis
         */
        double axisAngle = Math.toDegrees(Math.atan2((circum[1] - center[1]),
                (circum[0] - center[0])));
        double cosineAxis = Math.cos(Math.toRadians(axisAngle));
        double sineAxis = Math.sin(Math.toRadians(axisAngle));

        /*
         * calculate half lengths of major and minor axes
         */
        double diff[] = { circum[0] - center[0], circum[1] - center[1] };
        double major = Math.sqrt((diff[0] * diff[0]) + (diff[1] * diff[1]));
        double minor = major * arc.getAxisRatio();

        /*
         * Calculate points along the arc
         */
        // TODO - orientation issues
        // double increment = 5.0; //degrees
        double angle = arc.getStartAngle();
        int numpts = (int) Math.round(arc.getEndAngle() - arc.getStartAngle()
                + 1.0);
        double[][] path = new double[numpts][3];
        for (int j = 0; j < numpts; j++) {
            double thisSine = Math.sin(Math.toRadians(angle));
            double thisCosine = Math.cos(Math.toRadians(angle));
            // Can maybe use simpler less expensive calculations for circle,
            // if ever necessary.
            // if ( arc.getAxisRatio() == 1.0 ) {
            // path[j][0] = center[0] + (major * thisCosine );
            // path[j][1] = center[1] + (minor * thisSine );
            // }
            // else {
            path[j][0] = center[0] + (major * cosineAxis * thisCosine)
                    - (minor * sineAxis * thisSine);
            path[j][1] = center[1] + (major * sineAxis * thisCosine)
                    + (minor * cosineAxis * thisSine);
            // }

            angle += 1.0;
        }
        arcpts.addLineSegment(path);

        /*
         * Create new LineDisplayElement from wireframe shapes and add it to
         * return list
         */
        arcpts.compile();
        slist.add(new AlphaCapableLineDisplayElement(arcpts,
                getDisplayColor(arc.getColors()[0]), arc.getLineWidth()));

        slist.addAll(adjustContourCircleLabel(arc, paintProperties, path));

        return slist;
    }

    /**
     * Create a list of displayables for the specified symbol.
     * 
     * @param symbol
     *            Point for which to create the displayables.
     * @param paintProperties
     *            Paint properties associated with the target.
     * @return List of displayables.
     */
    public List<IDisplayable> createDisplayElements(ISymbol symbol,
            PaintProperties paintProperties) {

        if (symbol instanceof Symbol) {
            Coordinate[] loc = new Coordinate[] { symbol.getLocation() };
            SymbolLocationSet sym = new SymbolLocationSet((Symbol) symbol, loc);
            return createDisplayElements(sym, paintProperties);
        } else {
            return new ArrayList<IDisplayable>();
        }
    }

    /**
     * Set display attributes for all elements on a layer.
     * 
     * @param monoColor
     *            Flag indicating whether the layer should all be a single
     *            color.
     * @param color
     *            Color to be used.
     * @param fill
     *            Flag indicating whether the layer should be filled.
     */
    public void setLayerDisplayAttributes(Boolean monoColor, Color color,
            Boolean fill) {
        this.layerMonoColor = monoColor;
        this.layerColor = color;
        this.layerFilled = fill;
    }

    /**
     * Reset the factory.
     */
    public void reset() {
        if (shadedShape != null) {
            shadedShape.reset();
        }
        if (wireframeSymbol != null) {
            wireframeSymbol.reset();
        }
        if (wireframeShapes != null) {
            for (IWireframeShape shape : wireframeShapes) {
                if (shape != null) {
                    shape.reset();
                }
            }
        }
    }

    /**
     * Generate a box that could hold the specified text string.
     * 
     * @param text
     *            Text string.
     * @param paintProperties
     *            The paint properties associated with the target.
     * @return Range record holding the text string.
     */
    public PgenRangeRecord findTextBoxRange(IText text,
            PaintProperties paintProperties) {

        /*
         * For AvnText and MidCloudText, getString() is not defined and may
         * cause exception if the xml is converted from VGF. So a default range
         * record is added here. We may need to write a method to find the true
         * range record for both.
         */
        if ((text instanceof IAvnText || text instanceof IMidCloudText)
                && text.getString() == null) {
            return new PgenRangeRecord();
        }

        setScales(paintProperties);

        double[] tmp = { text.getPosition().x, text.getPosition().y, 0.0 };
        double[] loc = descriptor.worldToPixel(tmp);

        double horizRatio = paintProperties.getView().getExtent().getWidth()
                / paintProperties.getCanvasBounds().width;
        double vertRatio = paintProperties.getView().getExtent().getHeight()
                / paintProperties.getCanvasBounds().height;

        /*
         * Initialize Font Style
         */
        IFont font = initializeFont(text.getFontName(), text.getFontSize(),
                text.getStyle());

        /*
         * apply X offset in half-characters
         */
        boolean adjustOffset = false;
        if (text.getXOffset() != 0) {
            double ratio = paintProperties.getView().getExtent().getWidth()
                    / paintProperties.getCanvasBounds().width;
            DrawableString params = new DrawableString(text.getString()[0],
                    null);
            params.font = font;
            Rectangle2D bounds = target.getStringsBounds(params);
            double charSize = ratio * bounds.getWidth()
                    / text.getString()[0].length();
            loc[0] += 0.5 * charSize * text.getXOffset();
            adjustOffset = true;
        }

        /*
         * apply Y offset in half-characters
         */
        if (text.getYOffset() != 0) {
            double ratio = paintProperties.getView().getExtent().getHeight()
                    / paintProperties.getCanvasBounds().height;
            DrawableString params = new DrawableString(text.getString()[0],
                    null);
            params.font = font;
            Rectangle2D bounds = target.getStringsBounds(params);
            double charSize = ratio * bounds.getHeight();
            loc[1] -= 0.5 * charSize * text.getYOffset();
            adjustOffset = true;
        }

        if (adjustOffset) {
            double[] tmp1 = { loc[0], loc[1], 0.0 };
            double[] newloc = descriptor.pixelToWorld(tmp1);
            ((Text) text).setLocationOnly(new Coordinate(newloc[0], newloc[1]));
            ((Text) text).setXOffset(0);
            ((Text) text).setYOffset(0);
        }

        /*
         * Get text color
         */
        Color clr = getDisplayColor(text.getTextColor());
        RGB textColor = new RGB(clr.getRed(), clr.getGreen(), clr.getBlue());

        /*
         * Get angle rotation for text. If rotation is "North" relative,
         * calculate the rotation for "Screen" relative.
         */
        double rotation = text.getRotation();
        if (text.getRotationRelativity() == TextRotation.NORTH_RELATIVE) {
            rotation += getNorthOffsetAngle(text.getPosition());
        }

        /*
         * create drawableString and calculate its bounds
         */
        DrawableString dstring = new DrawableString(text.getString(), textColor);
        dstring.font = font;
        dstring.setCoordinates(loc[0], loc[1]);
        dstring.horizontalAlignment = HorizontalAlignment.CENTER;
        dstring.verticallAlignment = VerticalAlignment.MIDDLE;
        dstring.rotation = rotation;

        Rectangle2D bounds = target.getStringsBounds(dstring);
        double xOffset = (bounds.getWidth() + 1) * horizRatio / 2;
        double yOffset = (bounds.getHeight() + 1) * vertRatio / 2;

        /*
         * Set proper alignment
         */
        HorizontalAlignment align = HorizontalAlignment.CENTER;
        double left = xOffset, right = xOffset;
        if (text.getJustification() != null) {
            switch (text.getJustification()) {
            case RIGHT_JUSTIFY:
                align = HorizontalAlignment.RIGHT;
                left = xOffset * 2;
                right = 0.0;
                break;
            case CENTER:
                align = HorizontalAlignment.CENTER;
                break;
            case LEFT_JUSTIFY:
                align = HorizontalAlignment.LEFT;
                left = 0.0;
                right = xOffset * 2;
                break;
            default:
                align = HorizontalAlignment.CENTER;
                break;
            }
        }

        dstring.horizontalAlignment = align;

        IExtent box = new PixelExtent(dstring.basics.x - left, dstring.basics.x
                + right, dstring.basics.y - yOffset, dstring.basics.y + yOffset);

        List<Coordinate> rngBox = new ArrayList<Coordinate>();
        rngBox.add(new Coordinate(box.getMinX() - PgenRangeRecord.RANGE_OFFSET,
                box.getMaxY() + PgenRangeRecord.RANGE_OFFSET));
        rngBox.add(new Coordinate(box.getMaxX() + PgenRangeRecord.RANGE_OFFSET,
                box.getMaxY() + PgenRangeRecord.RANGE_OFFSET));
        rngBox.add(new Coordinate(box.getMaxX() + PgenRangeRecord.RANGE_OFFSET,
                box.getMinY() - PgenRangeRecord.RANGE_OFFSET));
        rngBox.add(new Coordinate(box.getMinX() - PgenRangeRecord.RANGE_OFFSET,
                box.getMinY() - PgenRangeRecord.RANGE_OFFSET));

        List<Coordinate> textPos = new ArrayList<Coordinate>();
        textPos.add(new Coordinate(loc[0], loc[1]));

        if (font != null) {
            font.dispose();
        }

        return new PgenRangeRecord(rngBox, textPos, false);
    }

    /**
     * Find the range box that holds the specified symbol.
     * 
     * @param symbol
     *            Symbol with associated lat/lon coordinates.
     * @param paintProperties
     *            Paint properties associated with the target.
     * @return Range record holding the symbol.
     */
    public PgenRangeRecord findSymbolRange(ISymbol symbol,
            PaintProperties paintProperties) {

        Coordinate[] loc = new Coordinate[] { symbol.getLocation() };

        // Set up scale factors
        setScales(paintProperties);
        double sfactor = deviceScale * SYMBOL_SCALE * symbol.getSizeScale();

        /*
         * Get color for creating displayables.
         */
        Color[] dspClr = getDisplayColors(symbol.getColors());

        /*
         * create an AWT BufferedImage from the symbol pattern
         */
        sfactor *= screenToWorldRatio;
        IRenderedImageCallback imageCb = new SymbolImageCallback(
                symbol.getPatternName(), sfactor, symbol.getLineWidth(),
                symbol.isClear(), dspClr[0]);
        /*
         * Initialize raster image for use with graphics target
         */
        IImage pic = null;
        try {
            pic = target.initializeRaster(imageCb);
            pic.stage();
            // pic = target.initializeRaster( new IODataPreparer(image,
            // sym.getPatternName(), 0), null );
        } catch (Exception e) {
            statusHandler.error("SAG:IMAGE CREATION", e);
        }

        /*
         * convert lat/lons to pixel coords
         */
        double[][] pts = PgenUtil.latlonToPixel(loc,
                (IMapDescriptor) descriptor);

        /*
         * Build range
         */
        List<Coordinate> rngBox = new ArrayList<Coordinate>();
        rngBox.add(new Coordinate(pts[0][0] - pic.getWidth() / 2
                - PgenRangeRecord.RANGE_OFFSET, pts[0][1] + pic.getHeight() / 2
                + PgenRangeRecord.RANGE_OFFSET));
        rngBox.add(new Coordinate(pts[0][0] + pic.getWidth() / 2
                + PgenRangeRecord.RANGE_OFFSET, pts[0][1] + pic.getHeight() / 2
                + PgenRangeRecord.RANGE_OFFSET));
        rngBox.add(new Coordinate(pts[0][0] + pic.getWidth() / 2
                + PgenRangeRecord.RANGE_OFFSET, pts[0][1] - pic.getHeight() / 2
                - PgenRangeRecord.RANGE_OFFSET));
        rngBox.add(new Coordinate(pts[0][0] - pic.getWidth() / 2
                - PgenRangeRecord.RANGE_OFFSET, pts[0][1] - pic.getHeight() / 2
                - PgenRangeRecord.RANGE_OFFSET));

        List<Coordinate> symPos = new ArrayList<Coordinate>();
        symPos.add(symbol.getLocation());

        if (pic != null) {
            pic.dispose();
        }

        return new PgenRangeRecord(rngBox, symPos, false);
    }

    // Private Methods

    /**
     * Create displayable objects for the specified line.
     * 
     * @param line
     *            Line for which to creeate the displayables.
     * @param paintProperties
     *            Paint properties associated with the target.
     * @return List of displayables.
     */
    private List<IDisplayable> createDisplayElements(ILine line,
            PaintProperties paintProperties) {

        double[][] smoothpts;
        double[][] pixels;
        float density;

        setScales(paintProperties);

        /*
         * save drawable element
         */
        element = line;

        /*
         * Create the List to be returned, some wireframe shapes and a shaded
         * shape to be used for the IDisplayables
         */
        ArrayList<IDisplayable> list = new ArrayList<IDisplayable>();

        /*
         * Get lat/lon coordinates from drawable element
         */
        Coordinate[] pts = line.getLinePoints();

        /*
         * convert lat/lon coordinates to pixel coordinates
         */
        pixels = PgenUtil.latlonToPixel(pts, (IMapDescriptor) descriptor);

        /*
         * If line is closed, make sure last point is same as first point
         */
        if (line.isClosedLine()) {
            pixels = ensureClosed(pixels);
        }

        /*
         * Apply parametric smoothing on pixel coordinates, if required
         */
        if (line.getSmoothFactor() > 0) {
            float devScale = 50.0f;
            if (line.getSmoothFactor() == 1) {
                density = devScale / 1.0f;
            } else {
                density = devScale / 5.0f;
            }
            smoothpts = CurveFitter.fitParametricCurve(pixels, density);
        } else {
            smoothpts = pixels;
        }

        list.addAll(createDisplayElementsForLines(line, smoothpts,
                paintProperties));

        /*
         * Draw labels for contour lines.
         */
        // ???list.addAll(adjustContourLineLabels(elem, paintProps, smoothpts));

        return list;
    }

    /**
     * Create a list of displayables for the specified symbol set, used to draw
     * one symbol at one or more locations.
     * 
     * @param symbolSet
     *            A symbol with associated lat/lon coordinates.
     * @param paintProperties
     *            The paint properties associated with the target
     * @return List of displayables.
     */
    private List<IDisplayable> createDisplayElements(ISymbolSet symbolSet,
            PaintProperties paintProperties) {

        // Set up scale factors
        setScales(paintProperties);
        double sfactor = deviceScale * SYMBOL_SCALE
                * symbolSet.getSymbol().getSizeScale();

        /*
         * Create the List to be returned
         */
        ArrayList<IDisplayable> slist = new ArrayList<IDisplayable>();

        // get Symbol
        Symbol sym = symbolSet.getSymbol();

        /*
         * Get color for creating displayables.
         */
        Color[] dspClr = getDisplayColors(sym.getColors());

        /*
         * create an AWT BufferedImage from the symbol pattern
         */
        sfactor *= screenToWorldRatio;
        // BufferedImage image =
        // SymbolImageUtil.createBufferedImage(sym.getPatternName(), sfactor,
        // sym.getLineWidth(),
        // sym.isClear(), dspClr[0] );
        IRenderedImageCallback imageCb = new SymbolImageCallback(
                sym.getPatternName(), sfactor, sym.getLineWidth(),
                sym.isClear(), dspClr[0]);
        /*
         * Initialize raster image for use with graphics target
         */
        IImage pic = null;
        try {
            pic = target.initializeRaster(imageCb);
            pic.stage();
            // pic = target.initializeRaster( new IODataPreparer(image,
            // sym.getPatternName(), 0), null );
        } catch (Exception e) {
            statusHandler.error("SAG:IMAGE CREATION", e);
            return slist;
        }

        /*
         * convert lat/lons to pixel coords
         */
        double[][] pts = PgenUtil.latlonToPixel(symbolSet.getLocations(),
                (IMapDescriptor) descriptor);

        /*
         * Create SymbolSetElement and return it
         */
        slist.add(new SymbolSetElement(pic, pts));
        return slist;

    }

    /**
     * Create displayable elements for line elements. This method gets
     * attributes such as colors from the input elements, then applies these
     * attributes on the smoothed(if needed) points to create a list of
     * displayables.
     * 
     * @param line
     *            Line for which to create displayables.
     * @param smoothPoints
     *            Points, smoothed if appropriate, for the line.
     * @param paintProperties
     *            Paint properties associated with the target.
     * @return List of displayables.
     */
    private List<IDisplayable> createDisplayElementsForLines(ILine line,
            double[][] smoothPoints, PaintProperties paintProperties) {

        float drawLineWidth = line.getLineWidth();
        double drawSizeScale = line.getSizeScale();

        /*
         * Get color for creating displayables.
         */
        Color[] dspClr = getDisplayColors(element.getColors());

        /*
         * Find Line Pattern associated with this element, if "Solid Line" was
         * not requested.
         */
        LinePattern pattern = null;
        LinePatternManager lpl = LinePatternManager.getInstance();
        try {
            pattern = lpl.getLinePattern(line.getPatternName());
        } catch (LinePatternException lpe) {
            /*
             * could not find desired line pattern. Used solid line as default.
             */
            statusHandler.warn(lpe.getMessage()
                    + ":  Using Solid Line by default.");
            pattern = null;
        }

        /*
         * If pattern has some segments whose length is set at runtime based on
         * the desired line width, update the pattern now
         */
        if ((pattern != null) && pattern.needsLengthUpdate()) {
            // pattern = pattern.updateLength(screenToExtent * de.getLineWidth()
            // / (de.getSizeScale() * deviceScale));
            pattern = pattern.updateLength(screenToExtent * drawLineWidth
                    / (drawSizeScale * deviceScale));
        }

        /*
         * Flip the side of the pattern along the spine
         */
        if ((element instanceof Line) && ((Line) element).isFlipSide()) {
            pattern = pattern.flipSide();
        }

        /*
         * If a LinePattern is found for the object, apply it. Otherwise, just
         * use solid line.
         */
        ScaleType scaleType = null;
        if ((pattern != null) && (pattern.getNumSegments() > 0)) {
            scaleType = ScaleType.SCALE_ALL_SEGMENTS;
            if (element instanceof Line) {
                Line elementLine = (Line) element;
                // Change scale type for fronts so that only BLANK and LINE
                // segments are scaled.
                // This is done so that size of front pips don't vary with
                // length of front.
                if (elementLine.getPgenCategory().equalsIgnoreCase("Front")) {
                    scaleType = ScaleType.SCALE_BLANK_LINE_ONLY;
                }
            }
        }

        ArrayList<IDisplayable> list = new ArrayList<IDisplayable>();

        list.addAll(createDisplayElementsFromPoints(smoothPoints, dspClr,
                pattern, scaleType, getDisplayFillMode(line.isFilled()),
                drawLineWidth, paintProperties));
        // ((ILine) de).getLineWidth(), isCCFP, ccfp, paintProps));

        /*
         * Draw labels for contour lines.
         */
        list.addAll(createContourLineLabels(element, paintProperties,
                smoothPoints));

        return list;
    }

    /**
     * Create displayable objects based upon the specified input attributes and
     * points of a line.
     * 
     * @param points
     *            Points, smoothed if appropriate, for which to create the
     *            displayables.
     * @param displayColors
     *            Colors to use.
     * @param pattern
     *            Line pattern to use.
     * @param scaleType
     *            Scale type to use.
     * @param isFilled
     *            Flag indicating whether or not the object is filled.
     * @param lineWidth
     *            Width of the line.
     * @param paintProperties
     *            Paint properties of the target.
     * @return List of displayables.
     */
    private List<IDisplayable> createDisplayElementsFromPoints(
            double[][] points, Color[] displayColors, LinePattern pattern,
            ScaleType scaleType, Boolean isFilled, float lineWidth,
            PaintProperties paintProperties) {

        ArrayList<IDisplayable> list = new ArrayList<IDisplayable>();
        wireframeShapes = new IWireframeShape[displayColors.length];
        for (int i = 0; i < displayColors.length; i++) {
            wireframeShapes[i] = target.createWireframeShape(false, descriptor);
        }
        shadedShape = target.createShadedShape(false,
                descriptor.getGridGeometry());

        /*
         * Create arrow head, if needed
         */
        if ((pattern != null) && pattern.hasArrowHead()) {
            /*
             * Get scale size from drawable element.
             */
            double scale = element.getSizeScale();
            if (scale <= 0.0) {
                scale = 1.0;
            }
            double sfactor = deviceScale * scale;

            double pointAngle = 60.0; // Angle of arrow point - defining
                                      // narrowness
            double extent = pattern.getMaxExtent();

            // Consider distance away from center line, the height should be no
            // less than extent * 1.5.
            // Currently we only have extent 1 and 2 available.
            // 3.5 is what we want the size to be.
            double height = sfactor * 3.5;
            if (extent * 1.5 > 3.5) {
                height = sfactor * extent * 1.5;
            }

            int n = points.length - 1;
            // calculate direction of arrow head
            double slope = Math.toDegrees(Math.atan2(
                    (points[n][1] - points[n - 1][1]),
                    (points[n][0] - points[n - 1][0])));

            arrow = new ArrowHead(new Coordinate(points[n][0], points[n][1]),
                    pointAngle, slope, height, pattern.getArrowHeadType());
            Coordinate[] ahead = arrow.getArrowHeadShape();

            if (pattern.getArrowHeadType() == ArrowHeadType.OPEN) {
                // Add to wireframe
                wireframeShapes[0].addLineSegment(toDouble(ahead));
            }
            if (pattern.getArrowHeadType() == ArrowHeadType.FILLED) {
                // Add to shadedshape

                shadedShape.addPolygonPixelSpace(toLineString(ahead), new RGB(
                        displayColors[0].getRed(), displayColors[0].getGreen(),
                        displayColors[0].getBlue()));
            }
        }
        if ((pattern != null) && (pattern.getNumSegments() > 0)) {
            handleLinePattern(pattern, points, scaleType);
        } else {
            wireframeShapes[0].addLineSegment(points);
        }

        if (isFilled) {
            list.add(createFill(points));
        }

        /*
         * Compile each IWireframeShape, create its LineDisplayElement, and add
         * to IDisplayable return list
         */
        for (int k = 0; k < wireframeShapes.length; k++) {

            wireframeShapes[k].compile();
            AlphaCapableLineDisplayElement lde = new AlphaCapableLineDisplayElement(
                    wireframeShapes[k], displayColors[k], lineWidth);
            list.add(lde);
        }

        /*
         * Compile each IShadedShape, create FillDisplayElement, and add to
         * IDisplayable return list
         */
        // TODO - This loop may be needed if we ever have to support different
        // alphas
        // for ( IShadedShape shade : ss ) {
        shadedShape.compile();
        FillDisplayElement fde = new FillDisplayElement(shadedShape,
                element.getColors()[0].getAlpha() / 255.0f);
        list.add(fde);

        // }
        return list;
    }

    /**
     * Apply the specified line pattern to the specified line path.
     * 
     * @param pattern
     *            Line pattern definition.
     * @param points
     *            Data points defining the line path.
     * @param scaleType
     *            Scale type.
     */
    private void handleLinePattern(LinePattern pattern, double[][] points,
            ScaleType scaleType) {

        double start, end;

        /*
         * Get scale size and colors from drawable element.
         */
        double scale = element.getSizeScale();
        if (scale <= 0.0) {
            scale = 1.0;
        }
        double sfactor = deviceScale * scale;
        Color[] clr = getDisplayColors(element.getColors());

        /*
         * create a LineString Geometry from the data points defining the line
         * path
         */
        Coordinate[] coords = new Coordinate[points.length];
        for (int i = 0; i < points.length; i++) {
            coords[i] = new Coordinate(points[i][0], points[i][1]);
        }
        GeometryFactory gf = new GeometryFactory();
        LineString ls = gf.createLineString(coords);

        // Get the total length of the line path
        double totalDist = ls.getLength();

        /*
         * If line contains a FILLED arrow head, decrease total length of line
         * path by the length of the arrow head.
         */
        if (pattern.hasArrowHead()) {
            if (pattern.getArrowHeadType() == ArrowHeadType.FILLED) {
                totalDist -= arrow.getLength();
            }
        }

        /*
         * Create a LengthIndexedLine used to reference points along the path at
         * specific distances
         */
        LengthIndexedLine lil = new LengthIndexedLine(ls);
        LocationIndexedLine lol = new LocationIndexedLine(ls);
        LengthLocationMap llm = new LengthLocationMap(ls);

        /*
         * Calculate number of patterns that can fit on path
         */
        double psize = pattern.getLength() * sfactor;

        // psize = psize * 0.8;

        double numPatterns = Math.floor(totalDist / psize);

        /*
         * Calculate the amount to increase or decrease the pattern length so
         * that the line path ends on a full complete pattern.
         */
        double leftover = totalDist - (numPatterns * psize);
        if (leftover > 0.5 * psize) {
            // Add one more pattern and decrease size of pattern
            numPatterns += 1.0;
            leftover = leftover - psize;
        }
        // Calculate a scale factor that will be used to adjust the size of each
        // segment in the pattern
        // double offset = 1.0 + ( leftover / (numPatterns * psize) );
        if (scaleType == ScaleType.SCALE_BLANK_LINE_ONLY) {
            pattern = pattern.scaleBlankLineToLength(totalDist
                    / (numPatterns * sfactor));
        } else {
            pattern = pattern
                    .scaleToLength(totalDist / (numPatterns * sfactor));
        }

        /*
         * If size of line is less than size of a full pattern, then default to
         * solid line
         */
        if (numPatterns < 1) {
            Coordinate[] ncoords = lil.extractLine(0.0, totalDist)
                    .getCoordinates();
            double[][] npts = toDouble(ncoords);
            wireframeShapes[0].addLineSegment(npts);
            return;
        }

        /*
         * Loop through the number times the pattern will occur along the line
         * path
         */
        double begPat = 0.0, endPat;
        LinearLocation linloc0 = llm.getLocation(begPat);
        for (int n = 0; n < (int) Math.floor(numPatterns); n++) {

            double patlen = pattern.getLength() * sfactor;// * offset;
            endPat = begPat + patlen;
            LinearLocation linloc1 = llm.getLocation(endPat);
            LengthIndexedLine sublil = new LengthIndexedLine(lol.extractLine(
                    linloc0, linloc1));

            /*
             * Loop over each segment in the pattern
             */
            double currDist = 0.0, endLoc;
            for (PatternSegment seg : pattern.getSegments()) {
                int colorNum = seg.getColorLocation();

                // if not enough colors specified, default to first color
                if (colorNum >= wireframeShapes.length) {
                    colorNum = 0;
                }

                // Calculate end location of this segment
                double seglen = seg.getLength() * sfactor; // size of pattern
                                                           // segment
                // seglen *= offset; // resize segment to account for new full
                // pattern size
                endLoc = currDist + seglen;

                /*
                 * Apply specific pattern segment
                 */
                switch (seg.getPatternType()) {

                case BLANK:
                    /*
                     * Do nothing
                     */
                    break;

                case LINE:
                    /*
                     * Extract the data points along this line segment
                     */
                    Geometry section = sublil.extractLine(currDist, endLoc);
                    Coordinate[] newcoords = section.getCoordinates();
                    /*
                     * Add line segment path to appropriate WireframeShape
                     */
                    double[][] newpts = toDouble(newcoords);
                    wireframeShapes[colorNum].addLineSegment(newpts);
                    break;

                case CIRCLE:
                    /*
                     * Use ArcPatternApplicator to calculate the points around
                     * circle and then add them to the appropriate
                     * WireframeShape
                     */
                    start = 0.0;
                    end = 360.0;
                    PgenArcPatternApplicator circ = new PgenArcPatternApplicator(
                            sublil, currDist, endLoc);
                    circ.setArcAttributes(start, end, seg.getNumberInArc());
                    wireframeShapes[colorNum].addLineSegment(circ
                            .calculateLines());
                    break;

                case CIRCLE_FILLED:
                    /*
                     * Use ArcPatternApplicator to calculate the points around
                     * circle and then add them to the appropriate ShadedShape
                     */
                    start = 0.0;
                    end = 360.0;
                    PgenArcPatternApplicator circf = new PgenArcPatternApplicator(
                            sublil, currDist, endLoc);
                    circf.setArcAttributes(start, end, seg.getNumberInArc());
                    Coordinate[] carea = circf.calculateFillArea();
                    LineString[] circle = toLineString(carea);
                    shadedShape.addPolygonPixelSpace(circle,
                            new RGB(clr[seg.getColorLocation()].getRed(),
                                    clr[seg.getColorLocation()].getGreen(),
                                    clr[seg.getColorLocation()].getBlue()));
                    break;

                case ARC_180_DEGREE:
                    if (seg.isReverseSide()) {
                        start = 0.0;
                        end = 180.0;
                    } else {
                        start = 0.0;
                        end = -180.0;
                    }
                    /*
                     * Use ArcPatternApplicator to calculate the points around a
                     * 180 degree arc and then add them to the appropriate
                     * WireframeShape
                     */
                    PgenArcPatternApplicator app180 = new PgenArcPatternApplicator(
                            sublil, currDist, endLoc);
                    app180.setArcAttributes(start, end, seg.getNumberInArc());
                    wireframeShapes[colorNum].addLineSegment(app180
                            .calculateLines());
                    break;

                case ARC_180_DEGREE_FILLED:
                    if (seg.isReverseSide()) {
                        start = 0.0;
                        end = 180.0;
                    } else {
                        start = 0.0;
                        end = -180.0;
                    }
                    /*
                     * Use ArcPatternApplicator to calculate the points around a
                     * 180 degree arc. The addSegmentToFill method ensures
                     * points along path are added to points along the arc,
                     * creating a closed shape
                     */
                    PgenArcPatternApplicator app = new PgenArcPatternApplicator(
                            sublil, currDist, endLoc);
                    app.setArcAttributes(start, end, seg.getNumberInArc());
                    app.addSegmentToFill(true);
                    Coordinate[] area = app.calculateFillArea();
                    LineString[] arc = toLineString(area);
                    /*
                     * Add fill area to the appropriate ShadedShape and add line
                     * segment path to the appropriate WireframeShape.
                     */
                    shadedShape.addPolygonPixelSpace(arc,
                            new RGB(clr[seg.getColorLocation()].getRed(),
                                    clr[seg.getColorLocation()].getGreen(),
                                    clr[seg.getColorLocation()].getBlue()));
                    wireframeShapes[colorNum].addLineSegment(app
                            .getSegmentPts());
                    break;

                case ARC_180_DEGREE_CLOSED:
                    if (seg.isReverseSide()) {
                        start = 0.0;
                        end = 180.0;
                    } else {
                        start = 0.0;
                        end = -180.0;
                    }
                    /*
                     * Use ArcPatternApplicator to calculate the points around a
                     * 180 degree arc
                     */
                    PgenArcPatternApplicator app180c = new PgenArcPatternApplicator(
                            sublil, currDist, endLoc);
                    app180c.setArcAttributes(start, end, seg.getNumberInArc());
                    /*
                     * Add points along arc and line segment path to the
                     * appropriate WireframeShape.
                     */
                    wireframeShapes[colorNum].addLineSegment(app180c
                            .calculateLines());
                    wireframeShapes[colorNum].addLineSegment(app180c
                            .getSegmentPts());
                    break;

                case ARC_90_DEGREE:
                    if (seg.isReverseSide()) {
                        start = 0.0;
                        end = 90.0;
                    } else {
                        start = 0.0;
                        end = -90.0;
                    }
                    /*
                     * Use ArcPatternApplicator to calculate the points around a
                     * 90 degree arc
                     */
                    PgenArcPatternApplicator app90 = new PgenArcPatternApplicator(
                            sublil, currDist, endLoc);
                    app90.setArcAttributes(start, end, seg.getNumberInArc());
                    /*
                     * Add points along arc and line segment path to the
                     * appropriate WireframeShape.
                     */
                    wireframeShapes[colorNum].addLineSegment(app90
                            .calculateLines());
                    wireframeShapes[colorNum].addLineSegment(app90
                            .getSegmentPts());
                    break;

                case ARC_270_DEGREE:
                    if (seg.isReverseSide()) {
                        start = -45.0;
                        end = 225.0;
                    } else {
                        start = 45.0;
                        end = -225.0;
                    }
                    /*
                     * Use ArcPatternApplicator to calculate the points around a
                     * 270 degree arc and then add them to the appropriate
                     * WireframeShape
                     */
                    PgenArcPatternApplicator app270 = new PgenArcPatternApplicator(
                            sublil, currDist, endLoc);
                    app270.setArcAttributes(start, end, seg.getNumberInArc());
                    wireframeShapes[colorNum].addLineSegment(app270
                            .calculateLines());
                    break;

                case ARC_270_DEGREE_WITH_LINE:
                    if (seg.isReverseSide()) {
                        start = -45.0;
                        end = 225.0;
                    } else {
                        start = 45.0;
                        end = -225.0;
                    }
                    /*
                     * Use ArcPatternApplicator to calculate the points around a
                     * 270 degree arc
                     */
                    PgenArcPatternApplicator app270l = new PgenArcPatternApplicator(
                            sublil, currDist, endLoc);
                    app270l.setArcAttributes(start, end, seg.getNumberInArc());
                    /*
                     * Add points along arc and line segment path to the
                     * appropriate WireframeShape.
                     */
                    wireframeShapes[colorNum].addLineSegment(app270l
                            .calculateLines());
                    wireframeShapes[colorNum].addLineSegment(app270l
                            .getSegmentPts());
                    break;

                case BOX:
                    /*
                     * Use CornerPatternApplicator to calculate the coordinates
                     * of the box and add the pattern segments to the
                     * appropriate WireframeShape
                     */
                    CornerPatternApplicator box = new CornerPatternApplicator(
                            sublil, currDist, endLoc);
                    box.setHeight(seg.getOffsetSize() * sfactor);
                    box.setPatternType(CornerPattern.BOX);
                    wireframeShapes[colorNum].addLineSegment(box
                            .calculateLines());
                    break;

                case BOX_FILLED:
                    /*
                     * Use CornerPatternApplicator to calculate the coordinates
                     * of the box and add the pattern segments to the
                     * appropriate ShadedShape
                     */
                    CornerPatternApplicator boxf = new CornerPatternApplicator(
                            sublil, currDist, endLoc);
                    boxf.setHeight(seg.getOffsetSize() * sfactor);
                    boxf.setPatternType(CornerPattern.BOX);
                    Coordinate[] boxarea = boxf.calculateFillArea();
                    LineString[] barea = toLineString(boxarea);
                    shadedShape.addPolygonPixelSpace(barea,
                            new RGB(clr[seg.getColorLocation()].getRed(),
                                    clr[seg.getColorLocation()].getGreen(),
                                    clr[seg.getColorLocation()].getBlue()));
                    break;

                case X_PATTERN:
                    /*
                     * Use CornerPatternApplicator to calculate both slashes of
                     * the "X" pattern
                     */
                    CornerPatternApplicator ex = new CornerPatternApplicator(
                            sublil, currDist, endLoc);
                    ex.setHeight(seg.getOffsetSize() * sfactor);
                    ex.setPatternType(CornerPattern.X_PATTERN);
                    double[][] exes = ex.calculateLines();
                    double[][] slash1 = new double[][] { exes[0], exes[1] };
                    double[][] slash2 = new double[][] { exes[2], exes[3] };
                    /*
                     * Add both slash segments to appropriate WireframeShape
                     */
                    wireframeShapes[colorNum].addLineSegment(slash1);
                    wireframeShapes[colorNum].addLineSegment(slash2);
                    break;

                case Z_PATTERN:
                    /*
                     * Use CornerPatternApplicator to calculate the "Z" pattern
                     * and add the pattern segments to the appropriate
                     * WireframeShape
                     */
                    CornerPatternApplicator ze = new CornerPatternApplicator(
                            sublil, currDist, endLoc);
                    ze.setHeight(seg.getOffsetSize() * sfactor);
                    ze.setPatternType(CornerPattern.Z_PATTERN);
                    wireframeShapes[colorNum].addLineSegment(ze
                            .calculateLines());
                    break;

                case DOUBLE_LINE:
                    /*
                     * Use CornerPatternApplicator to calculate both top and
                     * bottom line segments along either side of line path.
                     */
                    CornerPatternApplicator dl = new CornerPatternApplicator(
                            sublil, currDist, endLoc);
                    dl.setHeight(seg.getOffsetSize() * sfactor);
                    dl.setPatternType(CornerPattern.DOUBLE_LINE);
                    double[][] segs = dl.calculateLines();
                    double[][] top = new double[][] { segs[0], segs[1] };
                    double[][] bottom = new double[][] { segs[2], segs[3] };
                    /*
                     * Add top and bottom line segments to appropriate
                     * WireframeShape
                     */
                    wireframeShapes[colorNum].addLineSegment(top);
                    wireframeShapes[colorNum].addLineSegment(bottom);
                    break;

                case TICK:
                    /*
                     * use CornerPatternApplicator to calculate tick segment
                     */
                    PgenCornerPatternApplicator tick = new PgenCornerPatternApplicator(
                            sublil, currDist, endLoc);
                    tick.setHeight(seg.getOffsetSize() * sfactor);
                    tick.setPatternType(CornerPattern.TICK);
                    /*
                     * Add tick segment and the line path segment to the
                     * appropriate WireframeShape.
                     */
                    wireframeShapes[colorNum].addLineSegment(tick
                            .getSegmentPts());
                    wireframeShapes[colorNum].addLineSegment(tick
                            .calculateLines());
                    break;

                case ARROW_HEAD:
                    start = -120.0;
                    end = 120.0;
                    /*
                     * Use ArcPatternApplicator to calculate the points around
                     * the arc
                     */
                    PgenArcPatternApplicator arrow = new PgenArcPatternApplicator(
                            sublil, currDist, endLoc);
                    arrow.setArcAttributes(start, end, seg.getNumberInArc());
                    /*
                     * Add points along arc and line segment path to the
                     * appropriate WireframeShape.
                     */
                    wireframeShapes[colorNum].addLineSegment(arrow
                            .calculateLines());
                    wireframeShapes[colorNum].addLineSegment(arrow
                            .getSegmentPts());
                    break;

                default:
                    /*
                     * Do nothing.
                     */
                    statusHandler.warn("Pattern definition: "
                            + seg.getPatternType().toString()
                            + " is not found.  Ignoring...");
                    break;
                }

                /*
                 * Update the starting location of the next segment to the
                 * ending location of the current segment.
                 */
                currDist = endLoc;

            }

            begPat = endPat;
            linloc0 = linloc1;

        }

    }

    /**
     * Change format of an array of points from an array of coordinates to a 2D
     * array of doubles.
     * 
     * @param coords
     *            Coordinate array to be converted.
     * @return 2D array of doubles.
     */
    private double[][] toDouble(Coordinate[] coords) {

        double[][] dpts = new double[coords.length][3];

        for (int k = 0; k < coords.length; k++) {
            dpts[k][0] = coords[k].x;
            dpts[k][1] = coords[k].y;
        }

        return dpts;
    }

    /**
     * Change format of an array of points from an array of coordinates to an
     * array of line strings.
     * 
     * @param coords
     *            Coordinate array to be converted.
     * @return Array of line strings.
     */
    private LineString[] toLineString(Coordinate[] coords) {

        LineString[] ls = new LineString[] { geometryFactory
                .createLineString(coords) };
        return ls;
    }

    /**
     * Ensure the last points is the same as the first.
     * 
     * @param points
     *            2D array of doubles providing a list of points.
     * @return 2D array of doubles representing the same points provided as a
     *         parameter, but with an additional end point that is the same as
     *         the first point if this was not already the case.
     */
    private double[][] ensureClosed(double[][] points) {

        int n = points.length - 1;

        /*
         * if first point equals last point, return data
         */
        if ((points[0][0] == points[n][0]) && (points[0][1] == points[n][1])) {
            return points;
        } else {
            /*
             * add first point to end of data, and return new data points
             */
            double[][] newdata = new double[points.length + 1][3];
            for (int i = 0; i < points.length; i++) {
                newdata[i] = points[i];
            }
            newdata[points.length] = newdata[0];
            return newdata;
        }
    }

    /**
     * Apply the current fill pattern to the specified area.
     * 
     * @param area
     *            2D array of doubles holding the list of coordinates defining
     *            the area.
     * @return Filled element with a shaded shape.
     */
    private FillDisplayElement createFill(double[][] area) {

        /*
         * create ShadedShape for fill area
         */
        IShadedShape fillarea = target.createShadedShape(false,
                descriptor.getGridGeometry());

        /*
         * If Requested Fill is not SOLID or TRANSPARENCY, get the fill pattern
         * and apply it to the ShadedShape
         */
        if (element.getFillPattern() != FillPattern.TRANSPARENCY
                && element.getFillPattern() != FillPattern.SOLID) {
            FillPatternList fpl = FillPatternList.getInstance();
            byte[] fpattern = fpl.getFillPattern(element.getFillPattern());
            fillarea.setFillPattern(fpattern);
        }

        /*
         * Convert double[][] to Coordinate[]
         */
        Coordinate[] coords = new Coordinate[area.length];
        for (int i = 0; i < area.length; i++) {
            coords[i] = new Coordinate(area[i][0], area[i][1]);
        }

        /*
         * Create LineString[] from Coordinates[]
         */
        LineString[] ls = toLineString(coords);

        /*
         * Add fill area to Shaded Shape
         */
        Color[] dspClr = getDisplayColors(element.getColors());
        Color fillClr = dspClr[0];
        if (dspClr.length > 1 && dspClr[1] != null) {
            fillClr = dspClr[1];
        }

        fillarea.addPolygonPixelSpace(
                ls,
                new RGB(fillClr.getRed(), fillClr.getGreen(), fillClr.getBlue()));
        fillarea.compile();

        float alpha = (fillClr.getAlpha()) / 255.0f;

        /*
         * return new FillDisplayElement with new ShadedShape
         */
        return new FillDisplayElement(fillarea, alpha);

    }

    /**
     * Determine an appropriate scale factor to use when calculating the the
     * coordinates of the displayables. This method also sets a screen to pixel
     * ratio for use when needing to convert the size of something from screen
     * relative to pixel relative.
     * 
     * @param paintProperties
     *            The paint properties associated with the target
     */
    private void setScales(PaintProperties paintProperties) {

        /*
         * Sets the device scale factor based on the current pixel extent
         */
        IExtent pe = paintProperties.getView().getExtent();
        deviceScale = pe.getHeight() / 300.0;

        /*
         * Set the screen to pixel ratio
         */
        Rectangle bounds = paintProperties.getCanvasBounds();
        screenToExtent = pe.getHeight() / bounds.height;

        screenToWorldRatio = bounds.width / pe.getWidth();
    }

    /**
     * Calculates the angle difference of "north" relative to the screen's
     * y-axis at a given lat/lon location.
     * 
     * @param location
     *            Point location in lat/lon coordinates.
     * @return The angle difference of "north" versus pixel coordinate's y-axis.
     */
    private double getNorthOffsetAngle(Coordinate location) {
        double delta = 0.05;

        /*
         * Calculate points in pixel coordinates just south and north of
         * original location.
         */
        double[] south = { location.x, location.y - delta, 0.0 };
        double[] pt1 = descriptor.worldToPixel(south);

        double[] north = { location.x, location.y + delta, 0.0 };
        double[] pt2 = descriptor.worldToPixel(north);

        // TODO - Orientation issues here!
        return -90.0
                - Math.toDegrees(Math.atan2((pt2[1] - pt1[1]),
                        (pt2[0] - pt1[0])));
    }

    /**
     * Get a font with the specified name, size, and style.
     * 
     * @param fontName
     *            Name.
     * @param fontSize
     *            Size.
     * @param fontStyle
     *            Style.
     * @return Font.
     */
    private IFont initializeFont(String fontName, float fontSize,
            FontStyle fontStyle) {
        Style[] styles = null;
        if (fontStyle != null) {
            switch (fontStyle) {
            case BOLD:
                styles = new Style[] { Style.BOLD };
                break;
            case ITALIC:
                styles = new Style[] { Style.ITALIC };
                break;
            case BOLD_ITALIC:
                styles = new Style[] { Style.BOLD, Style.ITALIC };
                break;
            default:
                break;
            }
        }

        /*
         * set smoothing and scaleFont to false to disable anti-aliasing (which
         * cause the fuzziness of the text).
         */
        IFont font = target.initializeFont(fontName, fontSize, styles);
        font.setSmoothing(false);
        font.setScaleFont(false);

        return font;
    }

    /**
     * Get the colors for displaying an element, given the specified colors and
     * the current layer attributes.
     * 
     * @param colors
     *            Colors for use if the layer color is not mono-color.
     * @return Colors to be used.
     */
    private Color[] getDisplayColors(Color[] colors) {

        Color[] newClr = new Color[colors.length];

        for (int ii = 0; ii < colors.length; ii++) {

            if (layerMonoColor && layerColor != null) {
                newClr[ii] = layerColor;
            } else {
                newClr[ii] = colors[ii];
            }
        }
        return newClr;
    }

    /**
     * Get the color for displaying an element, given the specified colors and
     * the current layer attributes.
     * 
     * @param color
     *            Color for use if the layer color is not mono-color.
     * @return Color to be used.
     */
    private Color getDisplayColor(Color color) {

        if (layerMonoColor && layerColor != null) {
            return layerColor;
        } else {
            return color;
        }

    }

    /**
     * Get the fill mode for displaying an element.
     * 
     * @param filled
     *            Flag indicating if filling is desirable assuming the layer is
     *            filled.
     * @return Flag indicating whether to fill or not.
     */
    private boolean getDisplayFillMode(Boolean filled) {

        /*
         * if (layerFilled) { return layerFilled; } else { return filled; }
         */

        /*
         * TTR 972 - to match NMAP2 behavior, non-filled elements will always be
         * drawn as non-filled. Filled objects should be drawn as filled only
         * when the "filled" flag for its layer is set to "true" or they are on
         * the active layer, so it is necessary to set the "layerFilled" flag to
         * true before generating displayables for such objects (see
         * PgenResource.drawFilledElement()).
         */
        if (filled && layerFilled) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Create a list of displayable objects for the specified contour line's
     * labels.
     * 
     * @param contour
     *            Contour line for which to create the displayables.
     * @param paintProperties
     *            Paint properties applying to the graphics target.
     * @param smoothPoints
     *            2D array of doubles holding the coordinate pairs, smoothed if
     *            appropriate, representing the contour line.
     * @return List of displayables.
     */
    private List<IDisplayable> createContourLineLabels(IMultiPoint contour,
            PaintProperties paintProperties, double[][] smoothPoints) {

        ArrayList<IDisplayable> dlist = new ArrayList<IDisplayable>();

        if (contour instanceof Line
                && ((Line) contour).getParent() instanceof ContourLine) {

            ContourLine cline = (ContourLine) ((Line) element).getParent();

            boolean lineClosed = cline.getLine().isClosedLine();

            boolean forceAuto = PgenUtil.getContourLabelAutoPlacement();

            /*
             * Find the visible part of the line.
             */
            double minx = paintProperties.getView().getExtent().getMinX();
            double miny = paintProperties.getView().getExtent().getMinY();
            double maxx = paintProperties.getView().getExtent().getMaxX();
            double maxy = paintProperties.getView().getExtent().getMaxY();

            double dx = Math.abs(maxx - minx);
            double dy = Math.abs(maxy - miny);
            double dd = Math.min(dx, dy);

            double ratio = 0.02;
            double offset = dd * ratio;

            minx += offset;
            miny += offset;
            maxx -= offset;
            maxy -= offset;

            double[][] visiblePts = new double[smoothPoints.length][smoothPoints[0].length];
            int actualLength = 0;

            for (double[] dl : smoothPoints) {
                if (dl[0] > minx && dl[0] < maxx && dl[1] > miny
                        && dl[1] < maxy) {
                    visiblePts[actualLength][0] = dl[0];
                    visiblePts[actualLength][1] = dl[1];
                    actualLength++;
                }
            }

            int numText2Draw = Math.min(actualLength, cline.getNumOfLabels());
            ArrayList<Coordinate> txtPositions = new ArrayList<Coordinate>();

            /*
             * Determine the number of labels to be drawn and set their
             * locations.
             */
            double xx, yy;
            if (actualLength < cline.getNumOfLabels()) {

                numText2Draw = Math.min(3, numText2Draw);

                if (numText2Draw > 0) {
                    if (numText2Draw == 1) {
                        xx = visiblePts[actualLength / 2][0] - offset / 4;
                        yy = visiblePts[actualLength / 2][1];
                        txtPositions.add(new Coordinate(xx, yy));
                    } else if (numText2Draw == 2) {
                        xx = visiblePts[0][0] - offset / 4;
                        yy = visiblePts[0][1];
                        txtPositions.add(new Coordinate(xx, yy));

                        if (lineClosed) {
                            xx = visiblePts[actualLength / 2][0] + offset / 4;
                            yy = visiblePts[actualLength / 2][1];
                        } else {
                            xx = visiblePts[actualLength - 1][0] + offset / 4;
                            yy = visiblePts[actualLength - 1][1];
                        }
                        txtPositions.add(new Coordinate(xx, yy));
                    } else {

                        xx = visiblePts[0][0] - offset / 4;
                        yy = visiblePts[0][1];
                        txtPositions.add(new Coordinate(xx, yy));

                        if (lineClosed) {
                            int intv = actualLength / numText2Draw;
                            xx = visiblePts[intv][0] + offset / 4;
                            yy = visiblePts[intv][1];
                            txtPositions.add(new Coordinate(xx, yy));

                            xx = visiblePts[intv * 2][0] + offset / 4;
                            yy = visiblePts[intv * 2][1];
                            txtPositions.add(new Coordinate(xx, yy));
                        } else {
                            xx = visiblePts[actualLength / 2][0] + offset / 4;
                            yy = visiblePts[actualLength / 2][1];
                            txtPositions.add(new Coordinate(xx, yy));

                            xx = visiblePts[actualLength - 1][0] + offset / 4;
                            yy = visiblePts[actualLength - 1][1];
                            txtPositions.add(new Coordinate(xx, yy));
                        }
                    }
                }
            } else {

                if (numText2Draw > 0) {
                    if (cline.getNumOfLabels() == 1) {
                        xx = visiblePts[actualLength / 2][0] - offset / 4;
                        yy = visiblePts[actualLength / 2][1];
                        txtPositions.add(new Coordinate(xx, yy));
                    } else if (cline.getNumOfLabels() == 2) {
                        xx = visiblePts[0][0] - offset / 4;
                        yy = visiblePts[0][1];
                        txtPositions.add(new Coordinate(xx, yy));

                        if (lineClosed) {
                            xx = visiblePts[actualLength / 2][0] + offset / 4;
                            yy = visiblePts[actualLength / 2][1];
                        } else {
                            xx = visiblePts[actualLength - 1][0] + offset / 4;
                            yy = visiblePts[actualLength - 1][1];
                        }
                        txtPositions.add(new Coordinate(xx, yy));
                    } else if (cline.getNumOfLabels() == 3) {

                        xx = visiblePts[0][0] - offset / 4;
                        yy = visiblePts[0][1];
                        txtPositions.add(new Coordinate(xx, yy));

                        if (lineClosed) {
                            int intv = actualLength / numText2Draw;
                            xx = visiblePts[intv][0] + offset / 4;
                            yy = visiblePts[intv][1];
                            txtPositions.add(new Coordinate(xx, yy));

                            xx = visiblePts[intv * 2][0] + offset / 4;
                            yy = visiblePts[intv * 2][1];
                            txtPositions.add(new Coordinate(xx, yy));
                        } else {
                            xx = visiblePts[actualLength / 2][0] + offset / 4;
                            yy = visiblePts[actualLength / 2][1];
                            txtPositions.add(new Coordinate(xx, yy));

                            xx = visiblePts[actualLength - 1][0] + offset / 4;
                            yy = visiblePts[actualLength - 1][1];
                            txtPositions.add(new Coordinate(xx, yy));
                        }
                    } else {
                        int interval;
                        if (lineClosed) {
                            interval = actualLength / numText2Draw;
                        } else {
                            interval = actualLength / (numText2Draw - 1);
                        }

                        int nadd = numText2Draw - 1;
                        if (lineClosed) {
                            nadd = numText2Draw;
                        }

                        for (int jj = 0; jj < nadd; jj++) {
                            if (jj == 0) {
                                xx = visiblePts[jj * interval][0] - offset / 4;
                            } else {
                                xx = visiblePts[jj * interval][0] + offset / 4;
                            }
                            yy = visiblePts[jj * interval][1];
                            txtPositions.add(new Coordinate(xx, yy));
                        }

                        if (!lineClosed) {
                            xx = visiblePts[actualLength - 1][0] + offset / 4;
                            yy = visiblePts[actualLength - 1][1];
                            txtPositions.add(new Coordinate(xx, yy));
                        }
                    }
                }
            }

            /*
             * Draw the label Texts - temporarily set their parents to null and
             * adjust the location for drawing.
             */
            double[] tps;
            double[] loc = { 0.0, 0.0, 0.0 };
            for (int kk = 0; kk < numText2Draw; kk++) {
                Text txt = cline.getLabels().get(kk);
                loc[0] = txtPositions.get(kk).x;
                loc[1] = txtPositions.get(kk).y;

                tps = descriptor.pixelToWorld(loc);
                if (txt.getAuto() != null && txt.getAuto() || forceAuto) {
                    txt.setLocationOnly(new Coordinate(tps[0], tps[1]));
                }

                txt.setParent(null);
                dlist.addAll(createDisplayElements(txt, paintProperties));
                txt.setParent(cline);

                if (!forceAuto) {
                    txt.setAuto(false);
                }
            }

        }

        return dlist;
    }

    /**
     * Create the displayable objects for labels of a contour circle.
     * 
     * @param contour
     *            Contour circle.
     * @param paintProperties
     *            Paint properties applying to the graphics target.
     * @param smoothPoints
     *            2D array of doubles holding the pairs of coordinates
     *            representing the circle.
     * @return List of displayables.
     */
    private List<IDisplayable> adjustContourCircleLabel(IArc contour,
            PaintProperties paintProperties, double[][] smoothPoints) {

        ArrayList<IDisplayable> dlist = new ArrayList<IDisplayable>();

        AbstractDrawableComponent parent = ((DrawableElement) contour)
                .getParent();

        if (parent instanceof ContourCircle) {

            Text labelText = ((ContourCircle) parent).getLabel();

            boolean forceAuto = PgenUtil.getContourLabelAutoPlacement();

            if (labelText.getAuto() != null && labelText.getAuto() || forceAuto) {
                /*
                 * Find the visible part of the circle.
                 */
                double minx = paintProperties.getView().getExtent().getMinX();
                double miny = paintProperties.getView().getExtent().getMinY();
                double maxx = paintProperties.getView().getExtent().getMaxX();
                double maxy = paintProperties.getView().getExtent().getMaxY();

                double dx = Math.abs(maxx - minx);
                double dy = Math.abs(maxy - miny);
                double dd = Math.min(dx, dy);

                double ratio = 0.02;
                double offset = dd * ratio;

                minx += offset;
                miny += offset;
                maxx -= offset;
                maxy -= offset;

                double[][] visiblePts = new double[smoothPoints.length][smoothPoints[0].length];
                int actualLength = 0;

                for (double[] dl : smoothPoints) {
                    if (dl[0] > minx && dl[0] < maxx && dl[1] > miny
                            && dl[1] < maxy) {
                        visiblePts[actualLength][0] = dl[0];
                        visiblePts[actualLength][1] = dl[1];
                        actualLength++;
                    }
                }

                /*
                 * Adjust the position - either in the middle or slightly off
                 * the last point.
                 */
                int pp = Math.max(actualLength / 2, actualLength - 5);
                double[] loc = descriptor
                        .pixelToWorld(new double[] {
                                visiblePts[pp][0] - offset / 2,
                                visiblePts[pp][1], 0.0 });
                Coordinate loc1 = new Coordinate(loc[0], loc[1]);
                labelText.setLocationOnly(loc1);
            }

            /*
             * Display.
             */
            labelText.setParent(null);
            dlist.addAll(createDisplayElements(labelText, paintProperties));
            labelText.setParent(parent);

            if (!forceAuto) {
                labelText.setAuto(false);
            }

        }

        return dlist;
    }
}
