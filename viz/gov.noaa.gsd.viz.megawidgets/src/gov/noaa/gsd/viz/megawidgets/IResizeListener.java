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
 * Resize listener, an interface that describes the methods that must be
 * implemented by any class that wishes to be notified when an {@link IResizer}
 * changes its size.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 24, 2014    4010    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IResizer
 */
public interface IResizeListener {

    // Public Methods

    /**
     * Receive notification that the given megawidget has experienced a size
     * change.
     * 
     * @param megawidget
     *            Megawidget that experienced the change.
     */
    public void sizeChanged(IResizer megawidget);
}