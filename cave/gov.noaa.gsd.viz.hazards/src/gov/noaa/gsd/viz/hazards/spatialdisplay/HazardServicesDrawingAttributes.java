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

import gov.noaa.gsd.viz.hazards.utilities.Utilities;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.LineAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

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
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public abstract class HazardServicesDrawingAttributes extends LineAttrDlg {

    public enum BorderStyles {
        SOLID, DASHED, DOTTED, NONE
    };

    // Label to display with the drawable.
    private String[] label = null;

    private long pointID = Long.MIN_VALUE;

    private TextPositioner textPosition = TextPositioner.CENTER;

    protected ISessionConfigurationManager configurationManager;

    private Color[] colors = new Color[] { Color.WHITE, Color.WHITE };

    private float lineWidth;

    private boolean selected = false;

    private final IDescriptor descriptor;

    protected AbstractEditor editor;

    public HazardServicesDrawingAttributes(Shell parShell,
            ISessionConfigurationManager configurationManager)
            throws VizException {
        super(parShell);
        this.configurationManager = configurationManager;
        editor = EditorUtil.getActiveEditorAs(AbstractEditor.class);
        descriptor = editor.getActiveDisplayPane().getDescriptor();
    }

    @Override
    public void setAttrForDlg(IAttribute ia) {
        // TODO Auto-generated method stub

    }

    public void setString(String[] label) {
        this.label = label;
    }

    public abstract gov.noaa.gsd.viz.hazards.spatialdisplay.LineStyle getLineStyle();

    public abstract void setSOLIDLineStyle();

    public abstract void setDASHEDLineStyle();

    public List<Coordinate> buildCoordinates(int shapeNum,
            IHazardEvent hazardEvent) {
        Geometry geometry = hazardEvent.getGeometry().getGeometryN(shapeNum);

        return Lists.newArrayList(geometry.getCoordinates());
    }

    public void setAttributes(int shapeNum, IHazardEvent hazardEvent) {

        Boolean selected = (Boolean) hazardEvent
                .getHazardAttribute(ISessionEventManager.ATTR_SELECTED);

        if (selected != null) {
            setSelected(selected);
        }
        setLabel(hazardEvent);
        setLineStyle(hazardEvent, configurationManager);
        setLineWidth(configurationManager.getBorderWidth(hazardEvent));
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

    protected void setLabel(IHazardEvent hazardEvent) {
        String hazardType = HazardEventUtilities.getPhenSigSubType(hazardEvent);

        StringBuilder sb = new StringBuilder();
        sb.append(hazardEvent.getEventID());

        if (hazardType != null) {
            sb.append(" " + hazardType);
        }
        setString(new String[] { sb.toString() });

    }

    protected void setLineStyle(IHazardEvent hazardEvent,
            ISessionConfigurationManager configManager) {
        String borderStyle = "NONE";
        LineStyle linestyle = configManager.getBorderStyle(hazardEvent);
        if (linestyle != null) {
            borderStyle = linestyle.toString();
        }

        switch (BorderStyles.valueOf(borderStyle)) {

        case SOLID:
            setSOLIDLineStyle();
            break;

        case DASHED:
            setDASHEDLineStyle();
            break;

        case NONE:
            // Nothing to do at the moment.
            break;
        case DOTTED:
            break;
        default:
            break;
        }
    }

    /**
     * TODO Keep a serialization of the Color object in the configuration.
     * 
     * @param
     * @return
     */
    protected Color[] buildHazardEventColors(IHazardEvent hazardEvent,
            ISessionConfigurationManager configManager) {
        com.raytheon.uf.common.colormap.Color color = configManager
                .getColor(hazardEvent);
        String fillcolor = (int) (color.getRed() * 255) + " "
                + (int) (color.getGreen() * 255) + " "
                + (int) (color.getBlue() * 255);
        String borderColor = "255 255 255";

        Color[] colors = new Color[] {

        ToolLayer.convertRGBStringToColor(borderColor),
                ToolLayer.convertRGBStringToColor(fillcolor) };
        return colors;
    }

    protected List<Coordinate> buildCircleCoordinates(double radius,
            Coordinate centerPointInWorld) {
        Coordinate centerCoordInPixels = worldToPixel(new Coordinate(
                centerPointInWorld.x, centerPointInWorld.y));

        Coordinate circumferenceCoordInPixels = new Coordinate(
                centerCoordInPixels.x - radius, centerCoordInPixels.y);

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
        double[] pixelAsArray = new double[] { coords.x, coords.y };
        double[] worldAsArray = descriptor.pixelToWorld(pixelAsArray);
        Coordinate result = new Coordinate(worldAsArray[0], worldAsArray[1]);
        return result;
    }

    protected void setPointTime(int shapeNum, IHazardEvent hazardEvent) {
        @SuppressWarnings("unchecked")
        List<Map<String, Serializable>> shapesList = (List<Map<String, Serializable>>) hazardEvent
                .getHazardAttribute(Utilities.HAZARD_EVENT_SHAPES);
        Map<String, Serializable> shape = shapesList.get(shapeNum);

        Long pointTime = (Long) shape.get(Utilities.POINT_TIME);

        setPointID(pointTime);
    }
}
