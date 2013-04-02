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

/**
 * Interface describing the methods to be implemented by a megawidget that is to
 * act as a container of other megawidgets. Any subclasses of <code>Megawidget
 * </code> must implement this interface if they are to hold other megawidgets.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see Megawidget
 * @see IContainerSpecifier
 */
public interface IContainer extends IMegawidget {

    // Public Methods

    /**
     * Get the list of child megawidgets of this widget.
     * 
     * @return List of child megawidgets of this widget. The list must not be
     *         modified by the caller.
     */
    public List<Megawidget> getChildren();
}