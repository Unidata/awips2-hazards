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
 * Description: Interface describing the methods that must be implemented by a
 * megawidget specifier that is scale-capable, that is, one used to manipulate
 * values in a manner that could involve a scale bar equipped with one or more
 * thumbs that may be slid back and forth to change the value(s).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 09, 2015    7382    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IScaleCapableSpecifier extends IControlSpecifier {

    // Public Static Constants

    /**
     * Scale usage parameter name; a megawidget may include a boolean as the
     * value associated with this name. The value determines whether or not a
     * scale widget of some sort will be shown along with the other user
     * interface components to allow the user an alternate method of
     * manipulating the state. If not specified, it is assumed to be false.
     */
    public static final String MEGAWIDGET_SHOW_SCALE = "showScale";

    // Public Methods

    /**
     * Determine whether or not a scale widget is to be shown.
     * 
     * @return True if a scale widget is to be shown, false otherwise.
     */
    public boolean isShowScale();
}
