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

import gov.noaa.gsd.viz.megawidgets.validators.PassThroughValidator;

import java.util.Map;

/**
 * Description: Specifier for a hidden field megawidget, which much like a
 * hidden input field in an HTML document allows the tracking of state, with
 * such state being modified only programmatically (directly or via side
 * effects). This megawidget may only track one state value.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 22, 2014    5050    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see HiddenFieldMegawidget
 */
public class HiddenFieldSpecifier extends StatefulMegawidgetSpecifier implements
        IControlSpecifier {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public HiddenFieldSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, new PassThroughValidator());
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public boolean isFullWidthOfDetailPanel() {
        return false;
    }

    @Override
    public int getSpacing() {
        return 0;
    }
}
