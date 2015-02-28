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
package gov.noaa.gsd.viz.hazards.risecrestfall;

import java.util.Date;

/**
 * A point object for drawing on the rise/crest/fall editor.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 17, 2015    3847    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class GraphPoint {

    /** The X value, which is a Date in a time series graph */
    private Date x;

    /** The Y value */
    private double y;

    /** The x pixel value */
    private int pixelX;

    /** The y pixel value */
    private int pixely;

    /** Is the point selected */
    private boolean isSelected;

    /**
     * @return the x
     */
    public Date getX() {
        return x;
    }

    /**
     * @param x
     *            the x to set
     */
    public void setX(Date x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @param y
     *            the y to set
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * @return the pixelX
     */
    public int getPixelX() {
        return pixelX;
    }

    /**
     * @param pixelX
     *            the pixelX to set
     */
    public void setPixelX(int pixelX) {
        this.pixelX = pixelX;
    }

    /**
     * @return the pixely
     */
    public int getPixely() {
        return pixely;
    }

    /**
     * @param pixely
     *            the pixely to set
     */
    public void setPixely(int pixely) {
        this.pixely = pixely;
    }

    /**
     * @return the isSelected
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * @param isSelected
     *            the isSelected to set
     */
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "GraphPoint [x=" + x + ", y=" + y + "]";
    }
}
