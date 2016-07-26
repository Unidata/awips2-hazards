/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.nws.ncep.ui.pgen.display.FillPatternList.FillPattern;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;
import gov.noaa.nws.ncep.ui.pgen.display.ILine;

import java.awt.Color;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * This is the base class for most of the Hazard Services renderables.
 * <p>
 * This class adds a label to each renderable, something that is not easily
 * available in the base PGEN classes.
 * </p>
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 23, 2013    1462    bryon.lawrence      Set hazard border color to hazard fill color.
 * Dec 05, 2014    4124    Chris.Golden        Changed to work with newly parameterized
 *                                             config manager.
 * Feb 03, 2015    3865    Chris.Cody          Check for valid Active Editor class
 * Feb 09, 2015 6260       Dan Schaffer        Fixed bugs in multi-polygon handling
 * Aug 03, 2015 8836       Chris.Cody          Changes for a configurable Event Id
 * Mar 16, 2016 15676      Chris.Golden        Changed to not be a subclass of a PGEN class,
 *                                             and modified to work with spatial entities.
 * Mar 24, 2016 15676      Chris.Golden        Added dotted line style and varying fill patterns.
 * Jun 23, 2016 19537      Chris.Golden        Removed storm-track-specific code, and added
 *                                             more flexible text label positioning.
 * Jul 25, 2016 19537      Chris.Golden        Renamed, and removed unneeded member data and
 *                                             methods.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public abstract class DrawableAttributes implements IAttribute, ILine {

    // Package-Private Enumerated Types

    /**
     * Line style, each with its associated PGEN line style identifier.
     */
    enum LineStyle {

        LINE_SOLID("LINE_SOLID"), LINE_DASHED_2("LINE_DASHED_2"), LINE_DASHED_3(
                "LINE_DASHED_3"), LINE_DASHED_4("LINE_DASHED_4");

        private final String name;

        private LineStyle(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    // Private Static Constants

    /**
     * Default PGEN smoothing factor.
     */
    private static final int DEFAULT_SMOOTH_FACTOR = 0;

    // Private Variables

    /**
     * Label.
     */
    private String[] label = null;

    /**
     * Position of the <code>label</code> text, if the latter contains any.
     */
    private TextPositioner textPosition = TextPositioner.CENTERED;

    /**
     * Colors to be used for the drawable.
     */
    private Color[] colors = new Color[] { Color.WHITE, Color.WHITE };

    /**
     * Line style.
     */
    private LineStyle lineStyle;

    /**
     * Line width.
     */
    private float lineWidth = 1.0f;

    /**
     * Flag indicating whether or not the shape is closed; only used by lines
     * and polygons.
     */
    private boolean closed = false;

    /**
     * Flag indicating whether or not the shape is filled; only used by lines
     * and polygons.
     */
    private boolean filled = false;

    /**
     * Fill pattern to be used; only used by lines and polygons.
     */
    private FillPattern fillPattern = FillPattern.FILL_PATTERN_5;

    /**
     * Index of the {@link Geometry} returned by
     * {@link SpatialEntity#getGeometry()} that the shape with these attributes
     * represents. The spatial entity might have N geometries, with each one
     * having a corresponding drawable, each of the latter configured as per its
     * own instance of this class.
     */
    private int geometryIndex = -1;

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public DrawableAttributes() {
        lineStyle = LineStyle.LINE_SOLID;
        lineWidth = 2.0f;
    }

    // Public Methods

    public String[] getLabel() {
        return label;
    }

    public void setLabel(String[] label) {
        this.label = label;
    }

    public int getGeometryIndex() {
        return geometryIndex;
    }

    public void setGeometryIndex(int index) {
        this.geometryIndex = index;
    }

    public TextPositioner getTextPosition() {
        return textPosition;
    }

    public void setTextPosition(TextPositioner textPosition) {
        this.textPosition = textPosition;
    }

    @Override
    public Color[] getColors() {
        return colors;
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
    }

    @Override
    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    @Override
    public Boolean isClosedLine() {
        return closed;
    }

    public void setClosedLine(boolean closed) {
        this.closed = closed;
    }

    @Override
    public Boolean isFilled() {
        return filled;
    }

    public void setFilled(Boolean filled) {
        this.filled = filled;
    }

    @Override
    public abstract double getSizeScale();

    public LineStyle getLineStyle() {
        return lineStyle;
    }

    public void setSolidLineStyle() {
        this.lineStyle = LineStyle.LINE_SOLID;
    }

    public void setDottedLineStyle() {
        this.lineStyle = LineStyle.LINE_DASHED_2;
    }

    public void setDashedLineStyle() {
        this.lineStyle = LineStyle.LINE_DASHED_3;
    }

    @Override
    public FillPattern getFillPattern() {
        return fillPattern;
    }

    public void setFillPattern(FillPattern pattern) {
        this.fillPattern = pattern;
    }

    @Override
    public int getSmoothFactor() {
        return DEFAULT_SMOOTH_FACTOR;
    }

    /*
     * This method is only implemented to fulfill the ILine interface; it will
     * never be called, since instances of this class are only used to hold
     * attributes, not geometries.
     */
    @Override
    public Coordinate[] getLinePoints() {
        return null;
    }

    /*
     * This method is only implemented to fulfill the ILine interface; it will
     * never be called, since instances of this class are only used to hold
     * attributes, not geometries.
     */
    @Override
    public String getPatternName() {
        return null;
    }
}
