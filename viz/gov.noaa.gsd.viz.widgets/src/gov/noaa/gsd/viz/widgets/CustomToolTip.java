/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package gov.noaa.gsd.viz.widgets;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 * A {@link PopupDialog} that acts as a tooltip. This class was created to get
 * around the multiple monitor bug with tooltips.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 11, 2014 1283       Robert.Blum Initial creation
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

public class CustomToolTip extends PopupDialog {

    /**
     * Offset value between cursor location and the tooltip.
     */
    private static final int TOOLTIP_OFFSET_FROM_CURSOR = 10;

    /**
     * Location of the tooltip.
     */
    private Point location = null;

    /**
     * Boundaries of the area in which a mouse hover may generate a tooltip
     * showing the value under the mouse.
     */
    private Rectangle toolTipBounds = null;

    /**
     * Visibility of the tooltip.
     */
    private boolean isVisible = false;

    /**
     * Constructs a new instance of CustomToolTip.
     * 
     * @param parent
     *            The parent shell.
     * @param style
     *            The PopupDialog style.
     */
    public CustomToolTip(Shell parent, int style) {
        super(parent, style, false, false, false, false, false, null, null);
    }

    /**
     * Sets the location of the tooltip.
     * 
     * @param location
     *            Location to display the tooltip.
     */
    public void setLocation(Point location) {
        this.location = location;
    }

    /**
     * Sets the location of the tooltip.
     * 
     * @param x
     * @param y
     */
    public void setLocation(int x, int y) {
        Point point = new Point(x, y);
        setLocation(point);
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
     */
    @Override
    protected Point getInitialLocation(Point initialSize) {
        if (location == null) {
            return super.getInitialLocation(initialSize);
        }
        Point point = location;
        Rectangle monitor = getShell().getMonitor().getClientArea();
        if (monitor.width < point.x + initialSize.x) {
            point.x = Math.max(0, point.x - initialSize.x
                    - TOOLTIP_OFFSET_FROM_CURSOR);
        } else {
            point.x = point.x + TOOLTIP_OFFSET_FROM_CURSOR;
        }
        if (monitor.height < point.y + initialSize.y) {
            point.y = Math.max(0, point.y - initialSize.y);
        }
        return point;
    }

    /**
     * Disposes the tooltip.
     */
    public void dispose() {
        close();
    }

    /**
     * Sets whether the tooltip is visible.
     * 
     * @param visible
     */
    public void setVisible(boolean visible) {
        isVisible = visible;

        if (isVisible) {
            open();

        } else {
            close();
        }
    }

    /**
     * Return a boolean indicating whether this tooltip is currently being
     * displayed.
     * 
     * @return <code>true</code> if the tooltip is displayed, <code>false</code>
     *         if it is not.
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Set the text to be shown in the tooltip's info area. This message has no
     * effect if there was no info text supplied when the dialog first opened.
     * 
     * @param infoText
     *            the text to be shown when the info area is displayed.
     * 
     */
    public void setMessage(String infoText) {
        setInfoText(infoText);
    }

    /**
     * Set the text to be shown in the tooltip's title area. This message has no
     * effect if there was no title label specified when the dialog was
     * originally opened.
     * 
     * @param titleText
     *            the text to be shown when the title area is displayed.
     * 
     */
    public void setText(String titleText) {
        if (titleText == null || titleText.equals("")) {
            setTitleText(null);
        } else {
            setTitleText(titleText);
        }
    }

    /**
     * The rectangle the mouse can hover over to display the tooltip.
     * 
     * @return
     */
    public Rectangle getToolTipBounds() {
        return toolTipBounds;
    }

    /**
     * Sets the rectangle the mouse can hover over to display the tooltip.
     * 
     * @param toolTipBounds
     *            Rectanlge to set the tooltip bounds.
     */
    public void setToolTipBounds(Rectangle toolTipBounds) {
        this.toolTipBounds = toolTipBounds;
    }
}
