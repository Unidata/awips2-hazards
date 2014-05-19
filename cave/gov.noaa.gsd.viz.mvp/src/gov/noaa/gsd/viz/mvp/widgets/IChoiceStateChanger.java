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

import java.util.List;

/**
 * Interface describing the methods required in any sort of HMI component that
 * is a stateful widget allowing the choosing a one or more choices from within
 * a list of choices, meaning that it holds state that, when said state is
 * changed, notifies its {@link IStateChangeHandler} of the change. The generic
 * parameter <code>I</code> provides the type of widget identifier to be used,
 * <code>S</code> provides the type of state, <code>C</code> provides the type
 * of a choice, and <code>D</code> provides the type of displayable
 * representations for the choices.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 08, 2014    2925    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IChoiceStateChanger<I, S, C, D> extends IStateChanger<I, S> {

    // Public Methods

    /**
     * Set the list of choices.
     * 
     * @param identifier
     *            Identifier of the state to have its choices set. This may be
     *            <code>null</code> if this object only handles one particular
     *            state's changes.
     * @param choices
     *            New list of choices.
     * @param choiceDisplayables
     *            List of displayables associated with the choices; the element
     *            at any given index in this list is used as the visual
     *            representation for the choice at the same index within
     *            <code>choices</code>. If <code>null</code>, the choices are to
     *            act as their own visual representations.
     * @param value
     *            New value for the state.
     */
    public void setChoices(I identifier, List<C> choices,
            List<D> choiceDisplayables, S value);
}
