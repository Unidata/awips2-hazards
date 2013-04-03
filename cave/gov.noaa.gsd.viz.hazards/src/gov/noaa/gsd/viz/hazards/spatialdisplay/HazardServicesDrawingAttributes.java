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

import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.nws.ncep.ui.pgen.attrdialog.LineAttrDlg;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;

import java.util.List;

import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.core.exception.VizException;
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

    public HazardServicesDrawingAttributes(Shell parShell) throws VizException {
        super(parShell);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void setAttrForDlg(IAttribute ia) {
        // TODO Auto-generated method stub

    }

    public void setString(String[] label) {
        this.label = label;
    }

    public abstract List<Coordinate> updateFromEventDict(Dict shapeDict);

    public abstract String getLineStyle();

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

    /**
     * @param pointID
     *            the pointID to set
     */
    public void setPointID(long pointID) {
        this.pointID = pointID;
    }

    /**
     * @return the pointID
     */
    public long getPointID() {
        return pointID;
    }
}
