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

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SELECTED;
import gov.noaa.nws.ncep.ui.pgen.display.FillPatternList.FillPattern;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;
import gov.noaa.nws.ncep.ui.pgen.display.ILine;

import java.awt.Color;
import java.util.List;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * This is the base class for most of the Hazard Services renderables.
 * 
 * This class adds a label to each renderable, something that was not easily
 * available in the Base PGEN classes.
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
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public abstract class HazardServicesDrawingAttributes implements IAttribute,
        ILine {

    public enum BorderStyles {
        SOLID, DASHED, DOTTED, NONE
    };

    private static final int DEFAULT_SMOOTH_FACTOR = 0;

    // Label to display with the drawable.
    private String[] label = null;

    private long pointID = Long.MIN_VALUE;

    private TextPositioner textPosition = TextPositioner.CENTERED;

    protected ISessionConfigurationManager<ObservedSettings> configurationManager;

    private Color[] colors = new Color[] { Color.WHITE, Color.WHITE };

    private boolean selected = false;

    protected LineStyle lineStyle;

    private float lineWidth = 1.0f;

    private boolean closed = false;

    private boolean filled = false;

    private FillPattern fillPattern = FillPattern.FILL_PATTERN_5;

    private IDescriptor descriptor;

    protected AbstractEditor editor;

    public HazardServicesDrawingAttributes(
            ISessionConfigurationManager<ObservedSettings> configurationManager)
            throws VizException {
        this.configurationManager = configurationManager;
        editor = EditorUtil.getActiveEditorAs(AbstractEditor.class);
        if (editor != null) {
            descriptor = editor.getActiveDisplayPane().getDescriptor();
        }
        lineStyle = LineStyle.LINE_SOLID;
        lineWidth = 2.0f;
    }

    public void setString(String[] label) {
        this.label = label;
    }

    public abstract void setDottedLineStyle();

    public abstract void setDashedLineStyle();

    public void setAttributes(int shapeNum, IHazardEvent hazardEvent) {

        Boolean selected = (Boolean) hazardEvent
                .getHazardAttribute(HAZARD_EVENT_SELECTED);

        if (selected != null) {
            setSelected(selected);
        }
        setLabel(hazardEvent);
        setLineStyle(hazardEvent, configurationManager);
        setLineWidth(configurationManager.getBorderWidth(hazardEvent, selected));
        setColors(buildHazardEventColors(hazardEvent, configurationManager));
    }

    public String[] getString() {
        return label;
    }

    /**
     * @param textPosition
     *            the textPosition to set
     */
    public void setTextPosition(TextPositioner textPosition) {
        this.textPosition = textPosition;
    }

    /**
     * @return the textPosition
     */
    public TextPositioner getTextPosition() {
        return textPosition;
    }

    public void setPointID(long pointID) {
        this.pointID = pointID;
    }

    public long getPointID() {
        return pointID;
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

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public Boolean isClosedLine() {
        return closed;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
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

    protected void setLabel(IHazardEvent hazardEvent) {
        setString(new String[] { getHazardLabel(hazardEvent) });

    }

    public static String getHazardLabel(IHazardEvent hazardEvent) {
        StringBuilder sb = new StringBuilder();
        sb.append(hazardEvent.getDisplayEventID());
        String hazardType = HazardEventUtilities.getHazardType(hazardEvent);
        if (hazardType != null) {
            sb.append(" ");
            sb.append(hazardType);
        }
        return sb.toString();
    }

    protected void setLineStyle(IHazardEvent hazardEvent,
            ISessionConfigurationManager<ObservedSettings> configManager) {
        String borderStyle = "NONE";
        com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle linestyle = configManager
                .getBorderStyle(hazardEvent);
        if (linestyle != null) {
            borderStyle = linestyle.toString();
        }

        switch (BorderStyles.valueOf(borderStyle)) {

        case SOLID:
            setSolidLineStyle();
            break;
        case DOTTED:
            setDottedLineStyle();
            break;
        case DASHED:
            setDashedLineStyle();
            break;
        default:
            break;
        }
    }

    /*
     * TODO Keep a serialization of the Color object in the configuration.
     */
    public Color[] buildHazardEventColors(IHazardEvent hazardEvent,
            ISessionConfigurationManager<ObservedSettings> configManager) {
        com.raytheon.uf.common.colormap.Color color = configManager
                .getColor(hazardEvent);
        Color fillColor = new Color((int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255),
                (int) (color.getAlpha() * 255));
        Color[] colors = new Color[] { fillColor, fillColor };
        return colors;
    }

    public List<Coordinate> buildCircleCoordinates(double radius,
            Coordinate centerPointInWorld) {
        Coordinate centerCoordInPixels = worldToPixel(new Coordinate(
                centerPointInWorld.x, centerPointInWorld.y, 0.0));

        Coordinate circumferenceCoordInPixels = new Coordinate(
                centerCoordInPixels.x - radius, centerCoordInPixels.y, 0.0);

        Coordinate circumferenceCoordInWorld = pixelToWorld(circumferenceCoordInPixels);

        List<Coordinate> result = Lists.newArrayList();
        result.add(centerPointInWorld);
        result.add(circumferenceCoordInWorld);
        return result;
    }

    protected Coordinate worldToPixel(Coordinate coords) {
        double[] worldAsArray = new double[] { coords.x, coords.y };
        double[] pixelAsArray = descriptor.worldToPixel(worldAsArray);
        Coordinate result = new Coordinate(pixelAsArray[0], pixelAsArray[1]);
        return result;
    }

    protected Coordinate pixelToWorld(Coordinate coords) {
        double[] pixelAsArray = new double[] { coords.x, coords.y, coords.z };
        double[] worldAsArray = descriptor.pixelToWorld(pixelAsArray);
        Coordinate result = new Coordinate(worldAsArray[0], worldAsArray[1],
                worldAsArray[2]);
        return result;
    }

    @Override
    public int getSmoothFactor() {
        return DEFAULT_SMOOTH_FACTOR;
    }

    @Override
    public FillPattern getFillPattern() {
        return fillPattern;
    }

    public void setFillPattern(FillPattern pattern) {
        this.fillPattern = pattern;
    }

    public void setSolidLineStyle() {
        this.lineStyle = LineStyle.LINE_SOLID;
    }

    public LineStyle getLineStyle() {
        return lineStyle;
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
