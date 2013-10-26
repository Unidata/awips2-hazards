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
 * notify listeners (one per megawidget) when they are invoked.
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
 * @see NotifierMegawidget
 */
public abstract class NotifierMegawidgetSpecifier extends MegawidgetSpecifier
        implements INotifierSpecifier {

    // Private Variables

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

    @Override
    public final String getCallbackData() {
        return extraCallback;
    }
}
