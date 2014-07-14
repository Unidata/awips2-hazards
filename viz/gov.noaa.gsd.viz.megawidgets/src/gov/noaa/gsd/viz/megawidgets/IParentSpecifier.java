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
 * Description: Interface describing the methods to be implemented by a
 * megawidget specifier that is a parent of other megawidget specifiers. Any
 * subclasses of {@link MegawidgetSpecifier} must implement this interface if
 * they are to hold other megawidget specifiers. Also, any such subclasses must
 * only produce {@link Megawidget} objects that implement the {@link IParent}
 * interface. The <code>C</code> parameter indicates what type of
 * {@link ISpecifier} each child specifier must be.
 * <p>
 * Note that each instance of this interface should use an instance of
 * {@link ChildSpecifiersManager} to manage its child specifiers.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 24, 2013    2168    Chris.Golden      Initial creation
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IParent
 */
public interface IParentSpecifier<C extends ISpecifier> extends ISpecifier {

    // Public Static Constants

    /**
     * Megawidget specifier factory parameter name; each parent megawidget
     * specifier must contain a reference to an
     * {@link IMegawidgetSpecifierFactory} object associated with this name. The
     * provided factory will be used to construct any child megawidget
     * specifiers of the parent.
     */
    public static final String MEGAWIDGET_SPECIFIER_FACTORY = "megawidgetSpecifierFactory";

    // Public Methods

    /**
     * Get the list of all megawidget specifiers that are children of this
     * specifier.
     * 
     * @return List of child megawidget specifiers; this list must not be
     *         modified by the caller.
     */
    public List<C> getChildMegawidgetSpecifiers();
}
