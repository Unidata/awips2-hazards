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
 * Notifier megawidget created by a megawidget specifier.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 23, 2013   2168     Chris.Golden      Replaced erroneous references
 *                                           (variable names, comments, etc.) to
 *                                           "widget" with "megawidget" to avoid
 *                                           confusion.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see INotificationListener
 * @see NotifierMegawidgetSpecifier
 */
public abstract class NotifierMegawidget extends Megawidget implements
        INotifier {

    // Private Variables

    /**
     * Notification listener, or <code>null</code> if no listener is currently
     * being used.
     */
    private final INotificationListener notificationListener;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    protected NotifierMegawidget(NotifierMegawidgetSpecifier specifier,
            Map<String, Object> paramMap) {
        super(specifier);
        notificationListener = (INotificationListener) paramMap
                .get(NOTIFICATION_LISTENER);
    }

    // Protected Methods

    /**
     * Notify the notification listener, if it is appropriate. This method
     * should be called by subclasses whenever the latter are invoked.
     */
    protected final void notifyListener() {
        NotifierMegawidgetSpecifier specifier = getSpecifier();
        if (notificationListener != null) {
            notificationListener.megawidgetInvoked(this,
                    specifier.getCallbackData());
        }
    }
}