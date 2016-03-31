/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.widgets;

/**
 * Interface describing the methods that must be implemented in order to be a
 * graph control event listener.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 30, 2016   15931    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see Graph
 */
public interface IGraphListener {

    // Public Methods

    /**
     * Receive notification that at least one of the specified graph's plotted
     * points has changed.
     * 
     * @param widget
     *            Control widget whose value or values have changed.
     * @param source
     *            Source of the change.
     */
    public void plottedPointsChanged(Graph widget, Graph.ChangeSource source);
}