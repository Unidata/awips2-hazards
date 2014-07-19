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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Time delta units.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 27, 2014   3512     Chris.Golden      Initial creation (extracted from
 *                                           TimeDeltaSpecifier).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public enum TimeDeltaUnit {

    // Values

    MILLISECOND("ms", 1L, 100), SECOND("seconds", TimeUnit.SECONDS.toMillis(1),
            10), MINUTE("minutes", TimeUnit.MINUTES.toMillis(1), 60), HOUR(
            "hours", TimeUnit.HOURS.toMillis(1), 24), DAY("days", TimeUnit.DAYS
            .toMillis(1), 7);

    // Private Static Constants

    /**
     * Hash table mapping identifiers to their units with which they are
     * associated.
     */
    private static final Map<String, TimeDeltaUnit> UNITS_FOR_IDENTIFIERS = new HashMap<>();
    static {
        for (TimeDeltaUnit unit : EnumSet.allOf(TimeDeltaUnit.class)) {
            UNITS_FOR_IDENTIFIERS.put(unit.getIdentifier(), unit);
        }
    }

    // Private Variables

    /**
     * Text identifier.
     */
    private String identifier;

    /**
     * Multiplier applied to a value of this unit in order to get a value in
     * milliseconds.
     */
    private long multiplier;

    /**
     * Page increment for this unit.
     */
    private int pageIncrement;

    // Public Static Methods

    /**
     * Get the unit that goes with the specified identifier.
     * 
     * @param identifier
     *            Identifier for which to find the matching unit.
     * @return Unit with the specified identifier, or <code>null</code> if no
     *         such unit can be found.
     */
    public static TimeDeltaUnit get(String identifier) {
        return UNITS_FOR_IDENTIFIERS.get(identifier);
    }

    // Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param identifier
     *            Identifier of this unit.
     * @param multiplier
     *            Multiplier to apply to a value of this unit to get a value in
     *            milliseconds.
     * @param pageIncrement
     *            Page increment for this unit.
     */
    private TimeDeltaUnit(String identifier, long multiplier, int pageIncrement) {
        this.identifier = identifier;
        this.multiplier = multiplier;
        this.pageIncrement = pageIncrement;
    }

    // Public Methods

    /**
     * Get the identifier of this unit.
     * 
     * @return Identifier of this unit.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get the page increment for this unit.
     * 
     * @return Page increment for this unit.
     */
    public int getPageIncrement() {
        return pageIncrement;
    }

    /**
     * Convert the specified value in milliseconds to the same value in this
     * unit. Any remainder will be dropped.
     * 
     * @param value
     *            Value in milliseconds to convert.
     * @return Value converted to this unit, with remainders dropped.
     */
    public int convertMillisecondsToUnit(long value) {
        return (int) (value / multiplier);
    }

    /**
     * Convert the specified value in milliseconds to the same value in this
     * unit. Any remainder will be dropped.
     * 
     * @param value
     *            Value in milliseconds, or <code>null</code>.
     * @return Value converted to this unit, with remainders dropped, or
     *         <code>null</code> if the pre-converted value was also
     *         <code>null</code>.
     */
    public Integer convertMillisecondsToUnit(Number value) {
        if (value == null) {
            return null;
        } else {
            return convertMillisecondsToUnit(value.longValue());
        }
    }

    /**
     * Convert the specified value in this unit to the same value in
     * milliseconds.
     * 
     * @param value
     *            Value in this unit to convert.
     * @return Value converted to milliseconds.
     */
    public long convertUnitToMilliseconds(int value) {
        return value * multiplier;
    }

    /**
     * Convert the specified value in this unit to the same value milliseconds.
     * 
     * @param value
     *            Value in this unit to convert, or <code>null</code>.
     * @return Value converted to milliseconds, or <code>null</code> if the
     *         pre-converted value was also <code>null</code>.
     */
    public Long convertUnitToMilliseconds(Number value) {
        if (value == null) {
            return null;
        } else {
            return convertUnitToMilliseconds(value.intValue());
        }
    }
}