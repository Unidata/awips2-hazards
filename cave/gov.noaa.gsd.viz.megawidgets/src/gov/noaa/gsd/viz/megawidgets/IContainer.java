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
 * /** Interface describing the methods to be implemented by a megawidget that
 * is to act as a container of other megawidgets. Any subclasses of <code>
 * Megawidget</code> must implement this interface if they are to hold other
 * megawidgets. The parameter <code>M</code> provides the superclass of all the
 * child megawidgets that this parent may have.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Sep 25, 2013    2168    Chris.Golden      Refactored to inherit from new
 *                                           interface IParent.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see Megawidget
 * @see IContainerSpecifier
 */
public interface IContainer<M extends IMegawidget> extends IParent<M> {

    // Empty interface for now...
}
