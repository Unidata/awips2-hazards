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
 * Interface describing the methods to be implemented by a class that is to act
 * as a megawidget specifier factory. Such a factory is used to create
 * megawidget specifiers from maps containing said specifiers' parameter names
 * as keys and parameter values as values.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Oct 23, 2013   2168     Chris.Golden      Changed to allow the creation
 *                                           of a specifier to include a
 *                                           restriction on what superclass
 *                                           is expected of which the result
 *                                           should be an instance.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see MegawidgetSpecifier
 */
public interface IMegawidgetSpecifierFactory {

    // Public Methods

    /**
     * Create a megawidget specifier based upon the parameters contained within
     * the given map.
     * <p>
     * <strong>Note</strong>: Any megawidget specifier to be constructed using
     * this method must be an instance of a non-inner class.
     * 
     * @param superClass
     *            Class that must be the superclass of the created megawidget
     *            specifier. This allows specifiers of only a certain subclass
     *            of <code>ISpecifier</code> to be required.
     * @param parameters
     *            Map holding parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs. The megawidget being specified must have a unique
     *            identifier within the set of megawidget specifiers created by
     *            this factory.
     * @return Megawidget specifier that was created.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public <S extends ISpecifier> S createMegawidgetSpecifier(
            Class<S> superClass, Map<String, Object> parameters)
            throws MegawidgetSpecificationException;
}