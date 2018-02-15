/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.tools;

import java.io.Serializable;
import java.util.Map;

/**
 * Interface describing the methods that a tool dialog listener must implement.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * May 21, 2018    3782    Chris.Golden  Initial creation.
 *
 * </pre>
 *
 * @author Chris.Golden
 */
public interface IToolDialogListener {

    /**
     * Respond to the initialization of the tool dialog.
     * 
     * @param mutableProperties
     *            Mutable properties of the megawidgets within the tool dialog.
     */
    public void toolDialogInitialized(
            Map<String, Map<String, Object>> mutableProperties);

    /**
     * Respond to a command being issued by a megawidget within the tool dialog.
     * 
     * @param identifier
     *            Identifier of the command. If a subcommand was invoked, the
     *            identifier will be of the form
     *            <code>&lt;command&gt;.&lt;subcommand&gt</code>.
     * @param mutableProperties
     *            Mutable properties of the megawidgets within the tool dialog.
     */
    public void toolDialogCommandInvoked(String identifier,
            Map<String, Map<String, Object>> mutableProperties);

    /**
     * Respond to a megawidget state changing within the tool dialog.
     * 
     * @param identifier
     *            Identifier of the state element.
     * @param state
     *            New value of the state element.
     * @param mutableProperties
     *            Mutable properties of the megawidgets within the tool dialog.
     */
    public void toolDialogStateElementChanged(String identifier, Object state,
            Map<String, Map<String, Object>> mutableProperties);

    /**
     * Respond to multiple megawidget states changing within the tool dialog.
     * 
     * @param statesForIdentifiers
     *            Map pairing state identifiers with their new values.
     * @param mutableProperties
     *            Mutable properties of the megawidgets within the tool dialog.
     */
    public void toolDialogStateElementsChanged(
            Map<String, ?> statesForIdentifiers,
            Map<String, Map<String, Object>> mutableProperties);

    /**
     * Receive notification that a time-associated megawidget has experienced a
     * visible time range change.
     * 
     * @param identifier
     *            Identifier of the megawidget that precipitated the change.
     * @param lower
     *            Lower bound of the new visible time range as an epoch time in
     *            milliseconds.
     * @param upper
     *            Upper bound of the new visible time range as an epoch time in
     *            milliseconds.
     */
    public void toolDialogVisibleTimeRangeChanged(String identifier, long lower,
            long upper);

    /**
     * Receive notification that the dialog has been closed.
     * 
     * @param statesForIdentifiers
     *            Map pairing state identifiers with their values.
     * @param cancelled
     *            Flag indicating whether or not the dialog was cancelled.
     */
    public void toolDialogClosed(Map<String, Serializable> statesForIdentifiers,
            boolean cancelled);
}
