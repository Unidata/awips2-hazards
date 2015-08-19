/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

/**
 * Visible time range listener, an interface that describes the methods that
 * must be implemented by any class that wishes to be notified when a
 * {@link IVisibleTimeRangeChanger} changes the visible time range.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 23, 2015    4245    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IVisibleTimeRangeListener {

    // Public Methods

    /**
     * Receive notification that the given megawidget has experienced a visible
     * time range change.
     * 
     * @param megawidget
     *            Megawidget that experienced the change.
     * @param lower
     *            Lower bound of the new visible time range.
     * @param upper
     *            Upper bound of the new visible time range.
     */
    public void visibleTimeRangeChanged(IVisibleTimeRangeChanger megawidget,
            long lower, long upper);
}