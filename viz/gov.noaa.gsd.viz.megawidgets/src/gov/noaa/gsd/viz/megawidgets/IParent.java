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
 * act as a parent of other megawidgets. Any subclasses of {@link Megawidget}
 * must implement this interface if they are to hold other megawidgets. The
 * parameter <code>M</code> provides the superclass of all the child megawidgets
 * that this parent may have.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 25, 2013    2168    Chris.Golden      Initial creation
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see Megawidget
 * @see IParentSpecifier
 */
public interface IParent<M extends IMegawidget> extends IMegawidget {

    // Public Methods

    /**
     * Get the list of child megawidgets of this megawidget.
     * 
     * @return List of child megawidgets of this megawidget. The list must not
     *         be modified by the caller.
     */
    public List<M> getChildren();
}