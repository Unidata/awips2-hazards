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
 * notifies an {@link IResizeListener} when it is invoked. Any subclasses of
 * {@link Megawidget} must implement this interface if they are to issue such
 * notifications.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 24, 2014   4010     Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IResizeListener
 * @see Megawidget
 */
public interface IResizer extends IMegawidget {

    // Public Static Constants

    /**
     * Notification listener megawidget creation time parameter name; if
     * specified in the map passed to
     * {@link ISpecifier#createMegawidget(org.eclipse.swt.widgets.Widget, Class, Map)}
     * , its value must be an object of type <code>IResizeListener</code>.
     */
    public static final String RESIZE_LISTENER = "resizeListener";
}