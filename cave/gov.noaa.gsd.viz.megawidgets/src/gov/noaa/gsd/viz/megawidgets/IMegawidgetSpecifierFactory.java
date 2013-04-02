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
 * 
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
    public MegawidgetSpecifier createMegawidgetSpecifier(
            Map<String, Object> parameters)
            throws MegawidgetSpecificationException;
}