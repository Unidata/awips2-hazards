/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.widgets;

/**
 * Description: Plotted point, used by instances of {@link Graph} widgets.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 30, 2016   15931    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class PlottedPoint implements Comparable<PlottedPoint> {

    // Private Variables

    /**
     * X coordinate.
     */
    private int x;

    /**
     * Y coordinate.
     */
    private int y;

    /**
     * Flag indicating whether or not the point is editable by the user.
     */
    private boolean editable;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param x
     *            X coordinate.
     * @param y
     *            Y coordinate.
     * @param editable
     *            Flag indicating whether or not the point is editable by the
     *            user.
     */
    public PlottedPoint(int x, int y, boolean editable) {
        this.x = x;
        this.y = y;
        this.editable = editable;
    }

    /**
     * Construct an instance that is a copy of the specified plotted point.
     * 
     * @param other
     *            Plotted point to be copied.
     */
    public PlottedPoint(PlottedPoint other) {
        this.x = other.x;
        this.y = other.y;
        this.editable = other.editable;
    }

    // Public Methods

    /**
     * Get the X coordinate.
     * 
     * @return X coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Set the X coordinate.
     * 
     * @param x
     *            New X coordinate.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Get the Y coordinate.
     * 
     * @return Y coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Set the Y coordinate.
     * 
     * @param y
     *            New Y coordinate.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Determine whether or not this point is editable by the user.
     * 
     * @return True if the user can edit the point, false otherwise.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Set the flag indicating whether or not this point is editable by the
     * user.
     * 
     * @param editable
     *            Flag indicating whether or not this point is editable by the
     *            user.
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public int compareTo(PlottedPoint other) {
        return x - other.x;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof PlottedPoint == false) {
            return false;
        }
        PlottedPoint point = (PlottedPoint) other;
        return ((x == point.x) && (y == point.y) && (editable == point.editable));
    }

    @Override
    public int hashCode() {
        return (int) ((((long) x) + ((long) y) + (editable ? 1L : 0L)) / Integer.MAX_VALUE);
    }
}
