/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.mvp.widgets;

import java.util.Map;

/**
 * Interface describing the methods required in any sort of HMI component that
 * is a stateful widget, meaning that it holds state (a list of choices, a
 * number from within a range, etc.) that, when said state is changed, notifies
 * its {@link IStateChangeHandler} of the change. The generic parameter
 * <code>I</code> provides the type of widget identifier to be used, while
 * <code>S</code> provides the type of state.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 08, 2014    2925    Chris.Golden Initial creation.
 * Jun 30, 2014    3512    Chris.Golden Simplified setting of handler by
 *                                      removing identifier; one handler must
 *                                      be used for all identifiers, since
 *                                      the handler may be called upon to
 *                                      receive notification of multiple
 *                                      simultaneous state changes.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IStateChanger<I, S> extends IWidget<I> {

    // Public Methods

    /**
     * Set the editability of the specified state.
     * 
     * @param identifier
     *            Identifier of the stateful widget to have its editability set.
     *            This may be <code>null</code> if this object only handles one
     *            particular state.
     * @param editable
     *            Flag indicating whether or not the state should be editable.
     */
    public void setEditable(I identifier, boolean editable);

    /**
     * Get the values of the specified state.
     * 
     * @param identifier
     *            Identifier of the stateful widget to have its state set. This
     *            may be <code>null</code> if this object only handles one
     *            particular state.
     * @return Value for the specified state.
     */
    public S getState(I identifier);

    /**
     * Set the value of the specified state.
     * 
     * @param identifier
     *            Identifier of the stateful widget to have its state set. This
     *            may be <code>null</code> if this object only handles one
     *            particular state.
     * @param value
     *            New value for the specified state. Note that any associated
     *            {@link IStateChangeHandler} should not be notified of values
     *            set in this fashion.
     */
    public void setState(I identifier, S value);

    /**
     * Set the values of the specified states.
     * 
     * @param valuesForIdentifiers
     *            Map of state identifiers to their new values. Note that any
     *            associated {@link IStateChangeHandler} should not be notified
     *            of values set in this fashion.
     */
    public void setStates(Map<I, S> valuesForIdentifiers);

    /**
     * Set the state change handler to that specified. The handler will be
     * notified when the state changes.
     * 
     * @param handler
     *            Handler to be used.
     */
    public void setStateChangeHandler(IStateChangeHandler<I, S> handler);
}
