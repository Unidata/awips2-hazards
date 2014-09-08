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
 * is a qualified stateful widget, meaning that it holds state (a list of
 * choices, a number from within a range, etc.) that, when said state is
 * changed, notifies its {@link IQualifiedStateChangeHandler} of the change. The
 * generic parameter <code>Q</code> provides the type of widget qualifier to be
 * used, <code>I</code> provides the type of widget identifier to be used, and
 * <code>S</code> provides the type of state.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 15, 2014    4243    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IQualifiedStateChanger<Q, I, S> extends IQualifiedWidget<Q, I> {

    // Public Methods

    /**
     * Set the editability of the specified state.
     * 
     * @param qualifier
     *            Qualifier of the stateful widget to have its editability set.
     * @param identifier
     *            Identifier of the stateful widget to have its editability set.
     * @param editable
     *            Flag indicating whether or not the state should be editable.
     */
    public void setEditable(Q qualifier, I identifier, boolean editable);

    /**
     * Get the values of the specified state.
     * 
     * @param qualifier
     *            Qualifier of the stateful widget to have its state fetched.
     * @param identifier
     *            Identifier of the stateful widget to have its state fetched.
     * @return Value for the specified state.
     */
    public S getState(Q qualifier, I identifier);

    /**
     * Set the value of the specified state.
     * 
     * @param qualifier
     *            Qualifier of the stateful widget to have its state set.
     * @param identifier
     *            Identifier of the stateful widget to have its state set.
     * @param value
     *            New value for the specified state. Note that any associated
     *            {@link IStateChangeHandler} should not be notified of values
     *            set in this fashion.
     */
    public void setState(Q qualifier, I identifier, S value);

    /**
     * Set the values of the specified states.
     * 
     * @param qualifier
     *            Qualifier of the stateful widgets to have their states set.
     * @param valuesForIdentifiers
     *            Map of state identifiers to their new values. Note that any
     *            associated {@link IStateChangeHandler} should not be notified
     *            of values set in this fashion.
     */
    public void setStates(Q qualifier, Map<I, S> valuesForIdentifiers);

    /**
     * Set the state change handler to that specified. The handler will be
     * notified when the state changes.
     * 
     * @param handler
     *            Handler to be used.
     */
    public void setStateChangeHandler(
            IQualifiedStateChangeHandler<Q, I, S> handler);
}
