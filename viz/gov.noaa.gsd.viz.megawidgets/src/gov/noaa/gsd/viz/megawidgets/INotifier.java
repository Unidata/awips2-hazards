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
 * notifies an {@link INotificationListener} when it is invoked. Any subclasses
 * of {@link Megawidget} must implement this interface if they are to issue such
 * notifications.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see INotificationListener
 * @see Megawidget
 */
public interface INotifier extends IMegawidget {

    // Public Static Constants

    /**
     * Notification listener megawidget creation time parameter name; if
     * specified in the map passed to
     * {@link ISpecifier#createMegawidget(org.eclipse.swt.widgets.Widget, Class, Map)}
     * , its value must be an object of type <code>INotificationListener</code>.
     */
    public static final String NOTIFICATION_LISTENER = "notificationListener";
}