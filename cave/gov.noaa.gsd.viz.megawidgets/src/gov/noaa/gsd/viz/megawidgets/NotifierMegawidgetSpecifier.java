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
 * Notifier megawidget specifier base class, from which specific types of
 * notifier megawidget specifiers may be derived. A notifier megawidget
 * specifier allows the specification of megawidgets for later creation that may
 * notify listeners (one per widget) when they are invoked.
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
 * @see NotifierMegawidget
 */
public abstract class NotifierMegawidgetSpecifier extends MegawidgetSpecifier
        implements INotifierSpecifier {

    // Private Variables

    /**
     * Flag indicating whether or not the widget should notify listeners
     * whenever it changes state or is invoked by the user.
     */
    private final boolean notify;

    /**
     * Extra callback information, or <code>null</code> if none exists for this
     * specifier.
     */
    private final String extraCallback;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this notifier as a set of key-value
     *            pairs.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public NotifierMegawidgetSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);

        // Ensure that the notify flag, if present, is accep-
        // table, and if not present is assigned a default
        // value.
        notify = getSpecifierBooleanValueFromObject(
                parameters.get(MEGAWIDGET_NOTIFY), MEGAWIDGET_NOTIFY, false);

        // Ensure that the extra callback data, if present, is
        // acceptable.
        try {
            extraCallback = (String) parameters.get(MEGAWIDGET_CALLBACK_DATA);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_CALLBACK_DATA,
                    parameters.get(MEGAWIDGET_CALLBACK_DATA), "must be string");
        }
    }

    // Public Methods

    /**
     * Get the flag indicating whether or not the megawidget should notify its
     * listener (if any) when it is invoked by the user.
     * 
     * @return Flag indicating whether not the megawidget should notify
     *         listeners.
     */
    @Override
    public final boolean isToNotify() {
        return notify;
    }

    /**
     * Get the extra callback information to be passed back with a notification,
     * if any.
     * 
     * @return Extra callback information to be passed back with a notification,
     *         or <code>null</code> if there is no extra information.
     */
    @Override
    public final String getCallbackData() {
        return extraCallback;
    }
}
