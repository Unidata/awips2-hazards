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
 * Notification listener, an interface that describes the methods that must be
 * implemented by any class that wishes to be notified when an <code>INotifier
 * </code> is invoked.
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
 * @see INotifier
 */
public interface INotificationListener {

    // Public Methods

    /**
     * Receive notification that the given specifier's megawidget has been
     * invoked or has changed state. Megawidgets will only call this method if
     * they were marked as being notifiers when they were constructed.
     * 
     * @param widget
     *            Megawidget that was invoked.
     * @param extraCallback
     *            Extra callback information associated with this megawidget, or
     *            <code>null</code> if no such extra information is provided.
     */
    public void megawidgetInvoked(INotifier widget, String extraCallback);
}