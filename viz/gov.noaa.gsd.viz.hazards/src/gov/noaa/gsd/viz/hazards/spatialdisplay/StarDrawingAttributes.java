/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import static gov.noaa.gsd.viz.hazards.spatialdisplay.LineStyle.LINE_DASHED_4;

import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;

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
 * Dec 05, 2014 4124       Chris.Golden        Changed to work with newly parameterized
 *                                             config manager.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class StarDrawingAttributes extends HazardServicesDrawingAttributes {

    public static double SIZE_SCALE = 3.0;

    @SuppressWarnings("unused")
    private String[] label = { "" };

    public StarDrawingAttributes(
            ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager)
            throws VizException {
        super(sessionManager.getConfigurationManager());
        filled = true;
        closed = true;
    }

    @Override
    public void setString(String[] label) {
        this.label = label;
    }

    @Override
    public void setDashedLineStyle() {
        this.lineStyle = LINE_DASHED_4;
    }

    @Override
    public double getSizeScale() {
        return SIZE_SCALE;
    }

}
