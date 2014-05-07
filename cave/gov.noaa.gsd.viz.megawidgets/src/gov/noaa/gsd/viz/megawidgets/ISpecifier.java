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

import org.eclipse.swt.widgets.Widget;

/**
 * Description: Interface describing the methods that must be implemented by a
 * megawidget specifier. The latter allows the specification of megawidgets for
 * later creation; the type and configuration of each such megawidget is
 * specified via a {@link Map} containing key-value pairs, with each key being a
 * string chosen from one of the string constants defined within this class or
 * within a subclass, and the value being something appropriate to that key, as
 * specified by that key's description. Some key-value pairs are mandatory, and
 * others are optional, again as described.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013    2168    Chris.Golden      Initial creation
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ISpecifier {

    // Public Static Constants

    /**
     * Megawidget type parameter name; each megawidget must include a value
     * associated with this name. Valid values include any string matching the
     * name of an instantiable subclass minus the "Specifier"; for example, if
     * an instantiable subclass named "LabelSpecifier" exists, a valid value
     * would be "Label".
     */
    public static final String MEGAWIDGET_TYPE = "fieldType";

    /**
     * Megawidget class package parameter name; each megawidget may include a
     * value associated with this name. A valid value is the fully-qualified
     * package name (for example, "gov.noaa.gsd.viz.megawidgets") of which the
     * megawidget specifier for this {@link #MEGAWIDGET_TYPE} is a part. If not
     * specified, it defaults to this class's package.
     */
    public static final String MEGAWIDGET_CLASS_PACKAGE = "classPackage";

    /**
     * Megawidget identifier parameter name; each megawidget must include a
     * value associated with this name. Any string is valid as a value, as long
     * as it is unique within the set of all megawidgets that may be
     * instantiated at any given time.
     */
    public static final String MEGAWIDGET_IDENTIFIER = "fieldName";

    /**
     * Megawidget enabled parameter name; a megawidget may include a boolean
     * value associated with this name, in order to indicate whether or not the
     * megawidget should be enabled when it is first created. If not specified,
     * the megawidget is created in an enabled state.
     */
    public static final String MEGAWIDGET_ENABLED = "enable";

    /**
     * Megawidget label parameter name; a megawidget may include a value
     * associated with this name. Any string is valid as a value.
     */
    public static final String MEGAWIDGET_LABEL = "label";

    // Public Methods

    /**
     * Get the identifier.
     * 
     * @return Identifier.
     */
    public String getIdentifier();

    /**
     * Get the type.
     * 
     * @return Type.
     */
    public String getType();

    /**
     * Get the flag indicating whether or not the megawidget is to be created in
     * an enabled state.
     * 
     * @return True if the megawidget is to be created as enabled, false
     *         otherwise.
     */
    public boolean isEnabled();

    /**
     * Get the label.
     * 
     * @return Label.
     */
    public String getLabel();

    /**
     * Create the GUI components making up the specified megawidget.
     * 
     * @param parent
     *            Parent widget in which to place the megawidget.
     * @param superClass
     *            Class that must be the superclass of the created megawidget.
     *            This allows megawidgets of only a certain subclass of
     *            {@link IMegawidget} to be required.
     * @param creationParams
     *            Hash table mapping identifiers to values that subclasses might
     *            require when creating a megawidget.
     * @return Created megawidget.
     * @throws MegawidgetException
     *             If an exception occurs during creation or initialization of
     *             the megawidget.
     */
    public <P extends Widget, M extends IMegawidget> M createMegawidget(
            P parent, Class<M> superClass, Map<String, Object> creationParams)
            throws MegawidgetException;
}
