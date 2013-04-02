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
 * Interface describing the methods to be implemented by a megawidget that
 * notifies an <code>INotificationListener</code> when it is invoked. Any
 * subclasses of <code>Megawidget</code> must implement this interface if they
 * are to issue such notifications.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see INotificationListener
 * @see Megawidget
 * @see INotifierSpecifier
 */
public interface INotifier extends IMegawidget {

    // Public Static Constants

    /**
     * Notification listener megawidget creation time parameter name; if
     * specified in the map passed to <code>createMegawidget()</code>, its value
     * must be an object of type <code>INotificationListener</code>.
     */
    public static final String NOTIFICATION_LISTENER = "notificationListener";
}