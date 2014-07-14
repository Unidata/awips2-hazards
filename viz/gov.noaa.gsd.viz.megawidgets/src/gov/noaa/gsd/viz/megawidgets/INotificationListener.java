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
 * implemented by any class that wishes to be notified when an {@link INotifier}
 * is invoked.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Sep 24, 2013    2168    Chris.Golden      Changed erroneous "widget"
 *                                           references to "megawidget"
 *                                           in comments and variable
 *                                           names.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 23, 2014    4010    Chris.Golden      Changed to no longer include the
 *                                           extra callback information in any
 *                                           method invocations.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see INotifier
 */
public interface INotificationListener {

    // Public Methods

    /**
     * Receive notification that the given megawidget has been invoked.
     * 
     * @param megawidget
     *            Megawidget that was invoked.
     */
    public void megawidgetInvoked(INotifier megawidget);
}