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

import java.util.Map;

/**
 * Interface describing the methods to be implemented by a megawidget that
 * notifies an {@link IVisibleTimeRangeListener} when it experiences a visible
 * time range change. Any subclasses of {@link Megawidget} must implement this
 * interface if they are to issue such notifications.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 23, 2015   4245     Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IVisibleTimeRangeListener
 * @see Megawidget
 */
public interface IVisibleTimeRangeChanger extends IMegawidget {

    // Public Static Constants

    /**
     * Visible time range change listener megawidget creation time parameter
     * name; if specified in the map passed to
     * {@link ISpecifier#createMegawidget(org.eclipse.swt.widgets.Widget, Class, Map)}
     * , its value must be an object of type
     * <code>IVisibleTimeRangeListener</code>.
     */
    public static final String VISIBLE_TIME_RANGE_LISTENER = "visibleTimeRangeListener";
}