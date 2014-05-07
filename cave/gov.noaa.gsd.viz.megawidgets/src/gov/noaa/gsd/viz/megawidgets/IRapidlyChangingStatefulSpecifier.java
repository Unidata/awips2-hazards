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
 * Interface describing the methods to be implemented by a megawidget specifier
 * that creates a {@link IStateful} megawidget that allows rapid changing of its
 * state. An example would be a text entry megawidget that changes its state
 * each time the user types something. Any subclasses of
 * {@link MegawidgetSpecifier} must implement this interface if they are to
 * create such megawidgets.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 05, 2013    2336    Chris.Golden      Initial creation.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IStateful
 */
public interface IRapidlyChangingStatefulSpecifier extends IStatefulSpecifier {

    // Public Static Constants

    /**
     * Notify of every change parameter name; a megawidget may include a boolean
     * value associated with this name. The value acts as a flag that indicates
     * whether or not every change that occurs should result in a notification
     * being sent to the state change listener or not. If true, then all changes
     * to state, whether they are occurring during a set of rapid state changes
     * or not, result in notifications; if false, then only state changes at the
     * end of a set of rapidly occurring ones, or state changes that are not
     * part of such a set, cause notifications. If not provided, the default is
     * true.
     */
    public static final String MEGAWIDGET_SEND_EVERY_STATE_CHANGE = "sendEveryChange";

    // Public Methods

    /**
     * Determine whether or not the megawidget is to forward all state changes,
     * whether part of a rapidly occurring set or not, to the state change
     * listener.
     * 
     * @return True if all state changes result in notifications, false
     *         otherwise.
     */
    public boolean isSendingEveryChange();
}