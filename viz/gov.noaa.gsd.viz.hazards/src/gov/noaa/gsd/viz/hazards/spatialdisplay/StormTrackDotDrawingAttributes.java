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

import java.awt.Color;
import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
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
 * Dec 05, 2014 4124       Chris.Golden        Changed to work with newly parameterized
 *                                             config manager.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class StormTrackDotDrawingAttributes extends
        HazardServicesDrawingAttributes {

    private static final String STORM_DOT_LABEL = "Drag Me To Storm";

    public static double SIZE_SCALE = 10.5;

    public StormTrackDotDrawingAttributes(
            ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager)
            throws VizException {
        super(sessionManager.getConfigurationManager());
        closed = true;
    }

    @Override
    public void setDashedLineStyle() {
        this.lineStyle = LINE_DASHED_4;
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
        setSolidLineStyle();
        setLineWidth(1.0f);
        Color fillColor = new Color(255, 0, 0);
        Color borderColor = new Color(255, 0, 255);
        Color[] colors = new Color[] { borderColor, fillColor };
        setColors(colors);
    }
}
