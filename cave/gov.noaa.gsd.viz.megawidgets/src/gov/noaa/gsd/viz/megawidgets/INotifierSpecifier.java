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
 * that creates an <code>INotifier</code> megawidget. Any subclasses of
 * <code>MegawidgetSpecifier</code> must implement this interface if they are to
 * create such megawidgets.
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
 * @see Megawidget
 * @see MegawidgetSpecifier
 */
public interface INotifierSpecifier {

    // Public Static Constants

    /**
     * Megawidget callback parameter name; a megawidget may include an arbitrary
     * string value associated with this name as extra information to be passed
     * back with a notification. If <code>MEGAWIDGET_NOTIFY</code> is false,
     * then any value given for this parameter is ignored. If this parameter is
     * not specified, the default value is <code>null</code>.
     */
    public static final String MEGAWIDGET_CALLBACK_DATA = "callback";

    // Public Methods

    /**
     * Get the extra callback information to be passed back with a notification,
     * if any.
     * 
     * @return Extra callback information to be passed back with a notification,
     *         or <code>null</code> if there is no extra information.
     */
    public String getCallbackData();
}