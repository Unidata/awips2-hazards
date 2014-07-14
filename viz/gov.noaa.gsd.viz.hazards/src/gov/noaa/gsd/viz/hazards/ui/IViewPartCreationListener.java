/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.ui;

import org.eclipse.ui.part.ViewPart;

/**
 * Description: Interface describing the methods that need to be implemented in
 * order to be notified of a view part's creation. Such creation may be
 * immediate, if an attempt to show the view part via
 * {@link ViewPartDelegateView#showViewPart()} returns <code>true</code>, or if
 * instead <code>false</code> is returned and the view part is subsequently
 * created, it is deferred.
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
public interface IViewPartCreationListener<V extends ViewPart> {

    // Public Methods

    /**
     * Receive notification that the specified view part has been created.
     * 
     * @param viewPart
     *            View part that has been created.
     * @param deferred
     *            Flag indicating whether or not the creation was deferred.
     */
    public void viewPartCreated(V viewPart, boolean deferred);
}
