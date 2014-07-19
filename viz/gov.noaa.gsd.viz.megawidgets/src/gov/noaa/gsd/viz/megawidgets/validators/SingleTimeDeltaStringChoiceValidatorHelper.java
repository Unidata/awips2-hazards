/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets.validators;

import gov.noaa.gsd.viz.megawidgets.MegawidgetPropertyException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;
import gov.noaa.gsd.viz.megawidgets.TimeDeltaUnit;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

/**
 * Description: Validator helper used to ensure that potential choices for
 * instances of {@link BoundedChoiceValidator} are valid, and that the available
 * choices are strings, not maps. Furthermore, the string must specify a time
 * delta by including one or more of the following possible substrings. If more
 * than one of these substrings are specified, they must be in the order given
 * here (note that alternate specifiers for the units are enclosed within
 * parentheses and separated by vertical bars (|); note also that case is
 * ignored, so "Days", "days", "DAYS", etc. are all considered to mean the same
 * thing):
 * <dl>
 * <dt><i>num</i> (d|day|days)</dt>
 * <dd>Number of days, with the days given by <i>num</i>.</dd>
 * <dt><i>num</i> (h|hr|hrs|hour|hours)</dt>
 * <dd>Number of hours, with the hours given by <i>num</i>.</dd>
 * <dt><i>num</i> (m|mn|min|mins|minute|minutes)</dt>
 * <dd>Number of minutes, with the minutes given by <i>num</i>.</dd>
 * </dl>
 * Finally, the time deltas that are calculated using the strings must each be
 * unique within the choices list. For example, both "1 day" and "24 hours"
 * evaluate to be the same number of milliseconds, so including both as choices
 * would result in an invalid choice list.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 01, 2014   3512     Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SingleTimeDeltaStringChoiceValidatorHelper extends
        SingleChoiceValidatorHelper {

    // Private Static Constants

    /**
     * Pattern to be used for matching time delta strings.
     */
    private static final Pattern TIME_DELTA_PATTERN = Pattern
            .compile("^ *(?i)(?:|([0-9]+) *(?:d|days?)"
                    + "[ ,]*)(?:|([0-9]+) *(?:h|hrs?|hours?)"
                    + "[ ,]*)(?:|([0-9]+) *(?:mn?|mins?|minutes?)[ ,]*)$");

    /**
     * List of possible time delta units.
     */
    private static final List<TimeDeltaUnit> TIME_DELTA_UNITS = Lists
            .newArrayList(TimeDeltaUnit.DAY, TimeDeltaUnit.HOUR,
                    TimeDeltaUnit.MINUTE);

    // Public Constructors

    /**
     * Construct a standard instance for a {@link BoundedChoiceValidator}.
     * 
     * @param choicesKey
     *            Key within the specifier parameters or mutable properties for
     *            the choices list.
     */
    public SingleTimeDeltaStringChoiceValidatorHelper(String choicesKey) {
        super(choicesKey, null, null);
    }

    // Public Methods

    /**
     * Convert the specified object into a valid map, pairing available time
     * delta string choices with their corresponding time deltas in
     * milliseconds, for a specifier. Note that the map's iteration order is the
     * order in which the choices were specified.
     * 
     * @param choices
     *            Object holding the list of available choices.
     * @return Map pairing available choices with their time deltas in
     *         milliseconds, with iteration order guaranteed to be the same as
     *         the order of the specified choices list.
     * @throws MegawidgetSpecificationException
     *             If the object is not a valid list of available choices, if
     *             any of the choices do not specify positive time deltas, or if
     *             any of the choices duplicate time deltas.
     */
    public final Map<String, Long> convertToAvailableMapForSpecifier(
            Object choices) throws MegawidgetSpecificationException {

        /*
         * Do the basic work of ensuring that the object is a list of unique
         * strings.
         */
        List<?> choicesObj = convertToAvailableForSpecifier(choices);

        /*
         * Take the list of unique strings and make it into the required map.
         */
        try {
            return convertToAvailableMap(choicesObj);
        } catch (InvalidChoicesException e) {
            throw e.toSpecificationException(getIdentifier(), getType(),
                    getChoicesKey());
        }
    }

    /**
     * Convert the specified object into a valid map, pairing available time
     * delta string choices with their corresponding time deltas in
     * milliseconds, for a mutable property. Note that the map's iteration order
     * is the order in which the choices were specified.
     * 
     * @param choices
     *            Object holding the list of available choices.
     * @return Map pairing available choices with their time deltas in
     *         milliseconds, with iteration order guaranteed to be the same as
     *         the order of the specified choices list.
     * @throws MegawidgetPropertyException
     *             If the object is not a valid list of available choices, if
     *             any of the choices do not specify positive time deltas, or if
     *             any of the choices duplicate time deltas.
     */
    public final Map<String, Long> convertToAvailableMapForProperty(
            Object choices) throws MegawidgetPropertyException {

        /*
         * Do the basic work of ensuring that the object is a list of unique
         * strings.
         */
        List<?> choicesObj = convertToAvailableForProperty(choices);

        /*
         * Take the list of unique strings and make it into the required map.
         */
        try {
            return convertToAvailableMap(choicesObj);
        } catch (InvalidChoicesException e) {
            throw e.toPropertyException(getIdentifier(), getType(),
                    getChoicesKey());
        }
    }

    // Protected Methods

    @Override
    protected boolean isMapAllowableChoice() {
        return false;
    }

    // Private Methods

    /**
     * Convert the specified list of objects (assumed to be strings) into a
     * valid map pairing available time delta string choices with the
     * corresponding time deltas in milliseconds, with the map's iteration order
     * being the same as the order of the choices list.
     * 
     * @param choices
     *            List of strings providing the available choices.
     * @return Map pairing available choices with their time deltas in
     *         milliseconds, with iteration order guaranteed to be the same as
     *         the order of the specified choices list.
     * @throws InvalidChoicesException
     *             If the object is not a valid list of available choices.
     */
    private Map<String, Long> convertToAvailableMap(List<?> choices)
            throws InvalidChoicesException {

        /*
         * For each choice, convert it to a time delta in milliseconds, and
         * ensure it is unique.
         */
        Map<String, Long> timeDeltasForChoices = new LinkedHashMap<>(
                choices.size());
        Set<Long> timeDeltas = new HashSet<>();
        for (int j = 0; j < choices.size(); j++) {
            String choice = (String) choices.get(j);
            Matcher matcher = TIME_DELTA_PATTERN.matcher(choice);
            long timeDelta = 0L;
            if (matcher.matches() && (matcher.groupCount() == 3)) {
                for (int k = 0; k < TIME_DELTA_UNITS.size(); k++) {
                    String match = matcher.group(k + 1);
                    if (match != null) {
                        timeDelta += TIME_DELTA_UNITS.get(k)
                                .convertUnitToMilliseconds(
                                        Integer.parseInt(match));
                    }
                }
            }
            if (timeDelta <= 0L) {
                throw new InvalidChoicesException("[" + j + "]", null, choice,
                        "time delta must be non-zero");
            } else if (timeDeltas.contains(timeDelta)) {
                throw new InvalidChoicesException("[" + j + "]", null, choice,
                        "time delta value cannot be duplicate of another");
            } else {
                timeDeltas.add(timeDelta);
                timeDeltasForChoices.put(choice, timeDelta);
            }
        }
        return timeDeltasForChoices;
    }
}
