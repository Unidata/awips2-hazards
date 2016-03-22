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
import static gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.LineStyle.LINE_DASHED_2;
import static gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.LineStyle.LINE_DASHED_4;

import java.awt.Color;

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;

/**
 * Description: Drawing attributes for a Hazard Services point.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2013     1264   Chris.Golden      Initial creation
 * Aug  9, 2013 1921       daniel.s.schaffer Support of replacement of JSON with POJOs
 * Dec 05, 2014     4124   Chris.Golden      Changed to work with newly parameterized
 *                                           config manager.
 * Feb 09, 2015 6260       Dan Schaffer      Fixed bugs in multi-polygon handling
 * Oct 13, 2015 12494      Chris Golden      Reworked to allow hazard types to include
 *                                           only phenomenon (i.e. no significance) where
 *                                           appropriate.
 * Mar 16, 2016 15676      Chris.Golden      Moved to more appropriate location.
 * Mar 24, 2016 15676      Chris.Golden      Added dotted line style.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class PointDrawingAttributes extends HazardServicesDrawingAttributes {

    // Public Enumerated Types

    /**
     * Part of the point that a drawing attributes object is intended for.
     */
    public enum Element {
        INNER, OUTER
    };

    // Private Static Constants

    public static final double INNER_DIAMETER = 5.5;

    public static final double OUTER_DIAMETER = 8.5;

    public static final double OUTER_SELECTED_DIAMETER = 10.5;

    // Private Variables

    private double sizeScale;

    private Element element = Element.INNER;

    // Public Constructors

    /**
     * Construct a standard instance for an inner element.
     * 
     * @param parShell
     *            Parent shell.
     * @throws VizException
     *             If a viz exception occurs.
     */
    public PointDrawingAttributes(
            ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager)
            throws VizException {
        this(sessionManager, Element.INNER);
        setClosed(true);
        setFilled(true);
    }

    /**
     * Construct a standard instance.
     * 
     * @param parShell
     *            Parent shell.
     * @param element
     *            Element of a point for which these attributes are intended.
     * @throws VizException
     *             If a viz exception occurs.
     */
    public PointDrawingAttributes(
            ISessionManager<ObservedHazardEvent, ObservedSettings> sessionManager,
            Element element) throws VizException {
        super(sessionManager.getConfigurationManager());
        this.element = element;
    }

    @Override
    public void setDottedLineStyle() {
        this.lineStyle = LINE_DASHED_2;
    }

    @Override
    public void setDashedLineStyle() {
        this.lineStyle = LINE_DASHED_4;
    }

    @Override
    public double getSizeScale() {
        return sizeScale;
    }

    /**
     * Get the element of the point for which these attributes are intended.
     * 
     * @return Element of the point.
     */
    public Element getElement() {
        return element;
    }

    public void setSizeScale(double sizeScale) {
        this.sizeScale = sizeScale;
    }

    @Override
    public void setAttributes(int shapeNum, IHazardEvent hazardEvent) {
        super.setAttributes(shapeNum, hazardEvent);
        Boolean selected = (Boolean) hazardEvent
                .getHazardAttribute(HAZARD_EVENT_SELECTED);

        if (element.equals(Element.INNER)) {
            sizeScale = INNER_DIAMETER;
        } else if (selected) {
            sizeScale = OUTER_SELECTED_DIAMETER;
        } else {
            sizeScale = OUTER_DIAMETER;
        }

        /*
         * Outer components of points are always white. Inner components are
         * either black, if no type has been chosen, or else the color that goes
         * with the chosen type.
         */
        if (element.equals(Element.OUTER)) {
            setColors(new Color[] { Color.WHITE, Color.WHITE });
        } else if (HazardEventUtilities.isHazardTypeValid(hazardEvent)) {
            Color color = getColors()[1];
            setColors(new Color[] { color, color });
        } else {
            setColors(new Color[] { Color.BLACK, Color.BLACK });
        }

        /* Ensure that inner components have no text associated with them. */
        if (element.equals(Element.INNER)) {
            setString(null);
        }

    }
}
