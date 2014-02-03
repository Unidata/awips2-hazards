/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import static gov.noaa.gsd.viz.hazards.spatialdisplay.LineStyle.LINE_DASHED_4;
import static gov.noaa.gsd.viz.hazards.spatialdisplay.LineStyle.LINE_SOLID;
import gov.noaa.nws.ncep.ui.pgen.display.FillPatternList.FillPattern;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;

import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;

/**
 * The PGEN drawing attributes associated with a star drawn on the Spatial
 * Display in Hazard Services.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class StarDrawingAttributes extends HazardServicesDrawingAttributes {

    public static int SMOOTH_FACTOR = 0;

    public static double SIZE_SCALE = 3.0;

    @SuppressWarnings("unused")
    private String[] label = { "" };

    private LineStyle lineStyle = LINE_SOLID;

    public StarDrawingAttributes(Shell parShell, ISessionManager sessionManager)
            throws VizException {
        super(parShell, sessionManager.getConfigurationManager());
    }

    @Override
    public void setAttrForDlg(IAttribute ia) {

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
     * Sets flag indicating whether or not this event drawable shoulf be closed.
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
        return true;
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
    public void setString(String[] label) {
        this.label = label;
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

}
