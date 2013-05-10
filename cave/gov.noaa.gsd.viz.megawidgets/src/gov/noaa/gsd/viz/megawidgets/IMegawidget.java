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
import java.util.Set;

/**
 * Interface describing the methods to be implemented by all megawidgets.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IMegawidget {

    // Public Methods

    /**
     * Get the megawidget specifier that created this megawidget.
     * 
     * @return Megawidget specifier that created this megawidget.
     */
    public <S extends MegawidgetSpecifier> S getSpecifier();

    /**
     * Get the mutable property names for this megawidget.
     * 
     * @return Set of names for all mutable properties for this megawidget.
     */
    public Set<String> getMutablePropertyNames();

    /**
     * Get the current mutable property value for the specified name.
     * 
     * @param name
     *            Name of the mutable property value to be fetched.
     * @return Mutable property value.
     * @throws MegawidgetPropertyException
     *             If the name specifies a nonexistent property.
     */
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException;

    /**
     * Set the current mutable property value for the specified name.
     * 
     * @param name
     *            Name of the mutable property value to be fetched.
     * @param value
     *            New mutable property value to be used.
     * @throws MegawidgetPropertyException
     *             If the name specifies a nonexistent property, or if the value
     *             is invalid.
     */
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException;

    /**
     * Get the mutable properties of this megawidget.
     * 
     * @return Map of all mutable property names to their current values.
     */
    public Map<String, Object> getMutableProperties();

    /**
     * Set the mutable properties of this megawidget.
     * 
     * @param properties
     *            Map containing keys drawn from the set of all valid property
     *            names, with associated values being the new values for the
     *            properties.
     * @throws MegawidgetPropertyException
     *             If at least one name specifies a nonexistent property, or if
     *             at least one value is invalid.
     */
    public void setMutableProperties(Map<String, Object> properties)
            throws MegawidgetPropertyException;

    /**
     * Determine whether or not the megawidget is currently enabled.
     * 
     * @return True if the widget is currently enabled, false otherwise.
     */
    public boolean isEnabled();

    /**
     * Enable or disable the megawidget.
     * 
     * @param enable
     *            Flag indicating whether the megawidget is to be enabled or
     *            disabled.
     */
    public void setEnabled(boolean enable);

    /**
     * Determine whether or not the megawidget is currently editable.
     * 
     * @return True if the megawidget is currently editable, false otherwise.
     */
    public boolean isEditable();

    /**
     * Render the megawidget editable or read-only.
     * 
     * @param editable
     *            Flag indicating whether the megawidget is to be editable or
     *            read-only.
     */
    public void setEditable(boolean editable);

    /**
     * Determine the left decoration width for this megawidget, if the widget
     * has a decoration (label, etc.) to the left of its main component
     * widget(s). This is used to query all sibling megawidgets to determine
     * which one has the largest left decoration.
     * 
     * @return Width in pixels required for the left decoration of this
     *         megawidget, or 0 if the megawidget has no left decoration.
     */
    public int getLeftDecorationWidth();

    /**
     * Set the left decoration width for this megawidget to that specified, if
     * the widget has a decoration to the left of its main component widget(s).
     * This is used to set sibling megawidgets to all use the width of the
     * largest left decoration used by the siblings, if any.
     * 
     * @param width
     *            Width to be used if this megawidget has a left decoration.
     */
    public void setLeftDecorationWidth(int width);

    /**
     * Determine the right decoration width for this megawidget, if the widget
     * has a decoration (label, etc.) to the right of its main component
     * widget(s). This is used to query all sibling megawidgets to determine
     * which one has the largest right decoration.
     * 
     * @return Width in pixels required for the right decoration of this
     *         megawidget, or 0 if the megawidget has no right decoration.
     */
    public int getRightDecorationWidth();

    /**
     * Set the right decoration width for this megawidget to that specified, if
     * the widget has a decoration to the right of its main component widget(s).
     * This is used to set sibling megawidgets to all use the width of the
     * largest right decoration used by the siblings, if any.
     * 
     * @param width
     *            Width to be used if this megawidget has a right decoration.
     */
    public void setRightDecorationWidth(int width);
}