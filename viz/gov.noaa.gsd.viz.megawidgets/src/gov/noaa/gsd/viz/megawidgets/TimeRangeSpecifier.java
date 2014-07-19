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

import gov.noaa.gsd.viz.megawidgets.validators.BoundedTimeDeltaStringChoiceValidator;
import gov.noaa.gsd.viz.megawidgets.validators.SingleTimeDeltaStringChoiceValidatorHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Time range megawidget specifier, providing specification of a megawidget that
 * allows the selection of two times (that is, a time range). Each time is
 * associated with a separate state identifier; thus, it must have two specified
 * via the colon-separated megawidget specifier identifier. The first time is
 * selected as an absolute time, while the second is selected as an offset from
 * the first.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ------------ --------------------------
 * Jun 27, 2014   3512     Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TimeRangeMegawidget
 */
public class TimeRangeSpecifier extends MultiTimeMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Possible megawidget durations parameter name; a megawidget must include
     * an array of one or more choices associated with this name. For the format
     * of these choices, see the description of the
     * {@link SingleTimeDeltaStringChoiceValidatorHelper} class.
     */
    public static final String MEGAWIDGET_DURATION_CHOICES = "durationChoices";

    // Private Variables

    /**
     * Bounded time delta string choices validator, used to validate the
     * available choices for the duration.
     */
    private final BoundedTimeDeltaStringChoiceValidator durationChoicesValidator;

    /**
     * Duration choices.
     */
    private final List<String> durationChoices;

    /**
     * Map of duration choices to the corresponding time deltas in milliseconds.
     */
    private final Map<String, Long> timeDeltasForDurationChoices;

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
    public TimeRangeSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, null);

        /*
         * Ensure that the duration choices are correct, and make a record of
         * them, and of the time deltas for these choices.
         */
        durationChoicesValidator = new BoundedTimeDeltaStringChoiceValidator(
                parameters, MEGAWIDGET_DURATION_CHOICES);
        durationChoicesValidator.initialize(getType(), getIdentifier());
        List<?> rawDurationChoices = durationChoicesValidator
                .getAvailableChoices();
        List<String> mutableDurationChoices = new ArrayList<>(
                rawDurationChoices.size());
        for (Object rawDuration : rawDurationChoices) {
            mutableDurationChoices.add((String) rawDuration);
        }
        durationChoices = Collections.unmodifiableList(mutableDurationChoices);
        timeDeltasForDurationChoices = durationChoicesValidator
                .getTimeDeltasForAvailableChoices();
    }

    // Public Methods

    /**
     * Get the list of duration choices.
     * 
     * @return List of duration choices.
     */
    public final List<String> getDurationChoices() {
        return durationChoices;
    }

    /**
     * Get the map of duration choices to the corresponding time deltas in
     * milliseconds.
     * 
     * @return Map of duration choices to the corresponding time deltas in
     *         milliseconds.
     */
    public final Map<String, Long> getTimeDeltasForDurationChoices() {
        return timeDeltasForDurationChoices;
    }

    // Protected Methods

    @Override
    protected int getMinimumStateIdentifierCount() {
        return 2;
    }

    @Override
    protected int getMaximumStateIdentifierCount() {
        return 2;
    }

    /**
     * Get the duration choices validator.
     * 
     * @return duration choices validator.
     */
    protected final BoundedTimeDeltaStringChoiceValidator getDurationChoicesValidator() {
        return durationChoicesValidator;
    }
}
