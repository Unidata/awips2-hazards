/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import static gov.noaa.gsd.viz.hazards.spatialdisplay.LineStyle.LINE_DASHED_4;
import static gov.noaa.gsd.viz.hazards.spatialdisplay.LineStyle.LINE_SOLID;
import gov.noaa.nws.ncep.ui.pgen.display.FillPatternList.FillPattern;

import java.awt.Color;
import java.util.List;

import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * The PGEN drawing attributes associated with a dot drawn on the Spatial
 * Display in Hazard Services. All drawables in Hazard Services are rendered
 * using PGEN drawing classes.
 * 
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * 
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class StormTrackDotDrawingAttributes extends
        HazardServicesDrawingAttributes {

    /**
     * 
     */
    private static final String STORM_DOT_LABEL = "Drag Me To Storm";

    public static int SMOOTH_FACTOR = 0;

    public static double SIZE_SCALE = 10.5;

    private final boolean filled = true;

    private LineStyle lineStyle = LINE_SOLID;

    public StormTrackDotDrawingAttributes(Shell parShell,
            ISessionManager sessionManager) throws VizException {
        super(parShell, sessionManager.getConfigurationManager());
    }

    /**
     * Returns the smoothing factor used to draw polygons.
     * 
     * @return int
     * 
     */
    @Override
    public int getSmoothFactor() {
        return SMOOTH_FACTOR;
    }

    /**
     * Sets flag indicating whether or not this event drawable should be closed.
     * 
     * @return Boolean
     */
    @Override
    public Boolean isClosedLine() {
        return true;
    }

    /**
     * Returns a flag indicating whether or not to fill a polygon.
     * 
     * @return Boolean
     */
    @Override
    public Boolean isFilled() {
        return filled;
    }

    /**
     * Returns the fill pattern to use in drawing a event polgyon.
     * 
     * @return FillPattern
     * 
     * @see FillPattern
     */
    @Override
    public FillPattern getFillPattern() {
        return FillPattern.FILL_PATTERN_5;
    }

    @Override
    public void setSOLIDLineStyle() {
        this.lineStyle = LINE_SOLID;
    }

    @Override
    public void setDASHEDLineStyle() {
        this.lineStyle = LINE_DASHED_4;
    }

    @Override
    public LineStyle getLineStyle() {
        return lineStyle;
    }

    @Override
    public double getSizeScale() {
        return SIZE_SCALE;
    }

    @Override
    public void setAttributes(int shapeNum, IHazardEvent hazardEvent) {
        super.setAttributes(shapeNum, hazardEvent);
        setPointTime(shapeNum, hazardEvent);
    }

    public List<Coordinate> buildCoordinates() {
        double radius = 3.0;
        double[] centerCoordInPixels = editor.getActiveDisplayPane()
                .getRenderableDisplay().getExtent().getCenter();
        Coordinate centerPointInPixels = new Coordinate(centerCoordInPixels[0],
                centerCoordInPixels[1], 0.0);
        Coordinate centerPointInWorld = pixelToWorld(centerPointInPixels);

        List<Coordinate> result = buildCircleCoordinates(radius,
                centerPointInWorld);
        return result;
    }

    public void setAttributes() {
        setSelected(true);
        setString(new String[] { STORM_DOT_LABEL });
        setSOLIDLineStyle();
        setLineWidth(1.0f);
        Color fillColor = new Color(255, 0, 0);
        Color borderColor = new Color(255, 0, 255);
        Color[] colors = new Color[] { borderColor, fillColor };
        setColors(colors);
    }
}
