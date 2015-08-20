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

import java.util.List;
import java.util.Map;

/**
 * Description: Interface describing the methods that must be implemented by
 * classes that are to use a {@link SpinnerAndScaleComponentHelper} to manage
 * their spinner and scale widgets. The generic parameter <code>T</code>
 * specifies the type of value to be manipulated.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 06, 2015    4123    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see SpinnerAndScaleComponentHelper
 */
public interface ISpinnerAndScaleComponentHolder<T extends Number & Comparable<T>> {

    /**
     * Get the minimum value that the component widgets may hold for the
     * specified state identifier.
     * 
     * @param identifier
     *            State identifier.
     * @return Minimum value for the specified state identifier.
     */
    public abstract T getMinimumValue(String identifier);

    /**
     * Get the maximum value that the component widgets may hold for the
     * specified state identifier.
     * 
     * @param identifier
     *            State identifier.
     * @return Maximum value for the specified state identifier.
     */
    public abstract T getMaximumValue(String identifier);

    /**
     * Get the page increment delta to be used by the component widgets.
     * 
     * @return Page increment delta.
     */
    public abstract T getPageIncrementDelta();

    /**
     * Get the precision of the value, that is, the number of decimal places
     * that should come after a decimal point.
     * 
     * @return Non-negative number indicating the precision; if <code>0</code>,
     *         no fractional values will be shown.
     */
    public abstract int getPrecision();

    /**
     * Get the state value associated with the specified identifier.
     * 
     * @param identifier
     *            State identifier.
     * @return Value associated with the state identifier.
     */
    public abstract T getState(String identifier);

    /**
     * Set the state value associated with the specified identifier to the
     * specified value. This may result in other state values changing as well,
     * if for example a lower bound moves beyond an upper bounds, necessitating
     * the latter change as well. Note that implementations should not notify
     * state change listeners of any resulting state changes; this should be
     * done only when the {@link #notifyListener(List)} method is invoked by the
     * helper.
     * 
     * @param identifier
     *            State identifier.
     * @param value
     *            New value.
     * @return List of state identifiers that experienced value changes as a
     *         result.
     */
    public abstract List<String> setState(String identifier, T value);

    /**
     * Set the state values associated with the specified identifiers to the
     * specified values. This may result in other state values changing as well,
     * if for example a lower bound moves beyond an upper bounds, necessitating
     * the latter change as well. Note that implementations should not notify
     * state change listeners of any resulting state changes; this should be
     * done only when the {@link #notifyListener(List)} method is invoked by the
     * helper.
     * 
     * @param valuesForIdentifiers
     *            Map of state identifiers to their values.
     * @return List of state identifiers that experienced value changes as a
     *         result.
     */
    public abstract List<String> setStates(Map<String, T> valuesForIdentifiers);

    /**
     * Notify the state change listener that the specified states changed.
     * 
     * @param identifiersOfChangedStates
     *            List of identifiers associated with states that have been
     *            changed.
     */
    public abstract void notifyListener(List<String> identifiersOfChangedStates);
}
