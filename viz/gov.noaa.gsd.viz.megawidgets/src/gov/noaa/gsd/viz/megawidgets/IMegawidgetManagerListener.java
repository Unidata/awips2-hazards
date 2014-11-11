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
 * Megawidget manager listener, an interface that describes the methods that
 * must be implemented by any class that wishes to be notified of events by a
 * {@link MegawidgetManager}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 24, 2014    4010    Chris.Golden Initial creation.
 * Jun 30, 2014    3512    Chris.Golden Renamed from original interface
 *                                      IManagerResizeListener, with new
 *                                      methods added, in order to simplify
 *                                      megawidget manager usage.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IMegawidgetManagerListener {

    // Public Methods

    /**
     * Respond to a command being issued by a megawidget manager as a result of
     * a megawidget being invoked.
     * 
     * @param manager
     *            Megawidget manager that experienced the invocation.
     * @param identifier
     *            Identifier of the command. If a subcommand was invoked, the
     *            identifier will be of the form
     *            <code>&lt;command&gt;.&lt;subcommand&gt</code>.
     */
    public void commandInvoked(MegawidgetManager manager, String identifier);

    /**
     * Respond to a megawidget manager's state element having been changed as a
     * result of a megawidget's state changing.
     * 
     * @param manager
     *            Megawidget manager that experienced the state change.
     * @param identifier
     *            Identifier of the state element.
     * @param state
     *            New value of the state element.
     */
    public void stateElementChanged(MegawidgetManager manager,
            String identifier, Object state);

    /**
     * Respond to a megawidget manager's state elements having been changed as a
     * result of multiple megawidgets' states changing, a multi-state
     * megawidget's states changing, or some combination thereof.
     * 
     * @param manager
     *            Megawidget manager that experienced the state changes.
     * @param statesForIdentifiers
     *            Map pairing state identifiers with their new values.
     */
    public void stateElementsChanged(MegawidgetManager manager,
            Map<String, Object> statesForIdentifiers);

    /**
     * Receive notification that a megawidget manager has experienced a size
     * change.
     * 
     * @param manager
     *            Megawidget manager that experienced the size change.
     * @param identifier
     *            Identifier of the megawidget that precipitated the change.
     */
    public void sizeChanged(MegawidgetManager manager, String identifier);

    /**
     * Respond to an error occurring as a result of a mutable property change
     * triggered by a megawidget manager's side effects application. This method
     * will only be invoked if a {@link ISideEffectsApplier} was supplied at
     * construction time to the supplied megawidget manager.
     * 
     * @param manager
     *            Megawidget manager that experienced the error.
     * @param exception
     *            Exception that occurred as a result of the error.
     */
    public void sideEffectMutablePropertyChangeErrorOccurred(
            MegawidgetManager manager, MegawidgetPropertyException exception);
}