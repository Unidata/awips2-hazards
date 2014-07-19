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

import gov.noaa.gsd.viz.megawidgets.validators.BoundedMultiLongValidator;

import java.util.HashMap;
import java.util.Map;

/**
 * Time scale megawidget specifier, providing specification of a megawidget that
 * allows the selection of one or more times. Each time is associated with a
 * separate state identifier, of which one or more may be specified via the
 * colon-separated megawidget specifier identifier.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 21, 2013   2168     Chris.Golden      Changed to implement IControlSpecifier
 *                                           and use ControlSpecifierOptionsManager
 *                                           (composition over inheritance).
 * Nov 04, 2013   2336     Chris.Golden      Added implementation of new superclass-
 *                                           specified abstract method, and changed
 *                                           to offer option of not notifying
 *                                           listeners of state changes caused by
 *                                           ongoing thumb drags.
 * Dec 13, 2013   2545     Chris.Golden      Made subclass of TimeMegawidgetSpecifier.
 * Jan 31, 2014   2710     Chris.Golden      Added minimum interval parameter, to allow
 *                                           the minimum interval between adjacent state
 *                                           values to be configured.
 * Jan 31, 2014   2161     Chris.Golden      Added option to change editability of
 *                                           each state value individually. Also
 *                                           added ability to include detail mega-
 *                                           widgets for each state value. Also added
 *                                           custom strings to be displayed in place
 *                                           of date-time values for specified values.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 04, 2014   2155     Chris.Golden      Fixed bug that caused specifier to treat
 *                                           time descriptors as mandatory.
 * Jun 27, 2014   3512     Chris.Golden      Refactored functionality common to this
 *                                           and to new TimeRangeSpecifier into base
 *                                           class.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TimeScaleMegawidget
 */
public class TimeScaleSpecifier extends MultiTimeMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Minimum interval parameter name; a time scale megawidget may include a
     * positive long value associated with this name, providing the minimum
     * interval in milliseconds between adjacent thumbs. If not specified, the
     * megawidget allows adjacent values to be a minimum of one minute apart.
     */
    public static final String MEGAWIDGET_MINIMUM_TIME_INTERVAL = "minimumTimeInterval";

    /**
     * Time descriptions parameter name; a time scale megawidget may include a
     * value associated with this name. The value must be a {@link Map} of time
     * values as longs to strings used to describe those values. If provided,
     * then whenever one of the states is set (whether via user interaction or
     * programmatically) to be equal to any of the keys in this map, the
     * corresponding description for that value is used in place of the usual
     * date value in the date-time entry fields.
     */
    public static final String MEGAWIDGET_TIME_DESCRIPTORS = "timeDescriptors";

    // Private Variables

    /**
     * Map pairing long values with descriptive strings to be used in place of
     * the usual date-time strings when a state takes on one of the values.
     */
    private final Map<Long, String> descriptiveTextForValues;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    @SuppressWarnings("unchecked")
    public TimeScaleSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, MEGAWIDGET_MINIMUM_TIME_INTERVAL);

        /*
         * Ensure that the descriptive strings for dates map, if present, is
         * acceptable. Note that a conversion is done from strings to longs for
         * the map keys, since maps are sometimes allowed only strings as keys
         * (e.g. within JSON).
         */
        try {
            Map<?, String> map = (Map<?, String>) parameters
                    .get(TimeScaleSpecifier.MEGAWIDGET_TIME_DESCRIPTORS);
            descriptiveTextForValues = new HashMap<>();
            if (map != null) {
                for (Object key : map.keySet()) {
                    if (key instanceof Number) {
                        descriptiveTextForValues.put(
                                ((Number) key).longValue(), map.get(key));
                    } else {
                        descriptiveTextForValues.put(
                                Long.valueOf((String) key), map.get(key));
                    }
                }
            }
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_TIME_DESCRIPTORS,
                    parameters.get(MEGAWIDGET_TIME_DESCRIPTORS),
                    "must be map of longs (or longs in string form) to strings");
        }
    }

    // Public Methods

    /**
     * Get the minimum interval in milliseconds between adjacent state values.
     * 
     * @return Minimum interval in milliseconds.
     */
    public final long getMinimumInterval() {
        return ((BoundedMultiLongValidator) getStateValidator())
                .getMinimumInterval();
    }

    /**
     * Get the descriptive string for the specified state value, if any.
     * 
     * @param value
     *            State value for which the descriptive string is to be fetched.
     * @return Descriptive string, or <code>null</code> if no such string is
     *         found.
     */
    public final String getStateDescriptiveText(long value) {
        return descriptiveTextForValues.get(value);
    }
}
