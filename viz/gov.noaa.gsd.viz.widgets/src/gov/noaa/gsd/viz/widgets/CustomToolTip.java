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
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

public class CustomToolTip extends PopupDialog {

    // Private Static Constants

    /**
     * Offset value between cursor location and the tooltip.
     */
    private static final int TOOLTIP_OFFSET_FROM_CURSOR = 10;

    // Private Variables

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

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parent
     *            Parent shell.
     * @param style
     *            {@link PopupDialog} style.
     */
    public CustomToolTip(Shell parent, int style) {
        super(parent, style, false, false, false, false, false, null, null);
    }

    // Public Methods

    /**
     * Set the location of the tooltip.
     * 
     * @param location
     *            New location.
     */
    public void setLocation(Point location) {
        this.location = location;
    }

    /**
     * Set the location of the tooltip.
     * 
     * @param x
     *            X coordinate of new location.
     * @param y
     *            Y coordinate of new location.
     */
    public void setLocation(int x, int y) {
        Point point = new Point(x, y);
        setLocation(point);
    }

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
     * Dispose of the tooltip.
     */
    public void dispose() {
        close();
    }

    /**
     * Determine whether or not the tooltip is currently visible.
     * 
     * @return True if the tooltip is visible, false otherwise.
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Set the visibility of the tooltip.
     * 
     * @param visible
     *            Flag indicating whether or not the tooltip is to be visible.
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
     * Set the text to be shown in the tooltip's info area. This method has no
     * effect if there was no info text supplied when the dialog was first
     * opened.
     * 
     * @param text
     *            Text to be shown when the info area is displayed.
     * 
     */
    public void setMessage(String text) {
        setInfoText(text);
    }

    /**
     * Set the text to be shown in the tooltip's title area. This method has no
     * effect if there was no title label specified when the dialog was
     * originally opened.
     * 
     * @param text
     *            Text to be shown when the title area is displayed.
     * 
     */
    public void setText(String text) {
        if ((text == null) || text.equals("")) {
            setTitleText(null);
        } else {
            setTitleText(text);
        }
    }

    /**
     * Get the rectangle describing the boundaries of the area the mouse can
     * hover over to display the tooltip.
     * 
     * @return Bounds.
     */
    public Rectangle getToolTipBounds() {
        return toolTipBounds;
    }

    /**
     * Set the rectangle describing the boundaries of the area the mouse can
     * hover over to display the tooltip.
     * 
     * @param bounds
     *            Bounds.
     */
    public void setToolTipBounds(Rectangle bounds) {
        this.toolTipBounds = bounds;
    }
}
