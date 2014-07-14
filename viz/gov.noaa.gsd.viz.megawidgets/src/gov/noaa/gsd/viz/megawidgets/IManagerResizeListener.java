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

/**
 * Resize listener, an interface that describes the methods that must be
 * implemented by any class that wishes to be notified when a
 * {@link MegawidgetManager} experiences a resize of a component megawidget.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 24, 2014    4010    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IManagerResizeListener {

    // Public Methods

    /**
     * Receive notification that the manager has experienced a size change.
     * 
     * @param identifier
     *            Identifier of the megawidget that precipitated the change.
     */
    public void sizeChanged(String identifier);
}