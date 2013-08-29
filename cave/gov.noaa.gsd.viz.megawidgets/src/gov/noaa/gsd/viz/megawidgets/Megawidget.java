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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

/**
 * Base class for any megawidget created by a megawidget specifier.
 * 
 * All concrete subclasses must have a constructor taking the following
 * parameters as arguments, in the given order:
 * <dl>
 * <dt>specifier</dt>
 * <dd>Instance of a subclass of <code>MegawidgetSpecifier</code> that is
 * creating the megawidget. The subclass must have the same name as that of the
 * megawidget subclass, except with "Specifier" appended instead of
 * "Megawidget", and should be in the same package as the megawidget's subclass.
 * </dd>
 * <dt>parent</dt>
 * <dd>Subclass of SWT <code>Widget</code> in which the megawidget is to be
 * placed (such as <code>Composite</code> for window-based megawidgets, or
 * <code>Menu</code> for menu-based ones).</dd>
 * <dt>paramMap</dt>
 * <dd>Map pairing megawidget creation time parameter identifiers with
 * corresponding values.</dd>
 * </dl>
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
 * @see MegawidgetSpecifier
 */
public abstract class Megawidget implements IMegawidget {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        MUTABLE_PROPERTY_NAMES = ImmutableSet.of(
                MegawidgetSpecifier.MEGAWIDGET_EDITABLE,
                MegawidgetSpecifier.MEGAWIDGET_ENABLED);
    };

    // Private Variables

    /**
     * Specifier for this megawidget.
     */
    private final MegawidgetSpecifier specifier;

    /**
     * Flag indicating whether the megawidget is currently enabled.
     */
    private boolean enabled;

    /**
     * Flag indicating whether the megawidget is currently editable.
     */
    private boolean editable;

    /**
     * Cached editable background color, or <code>null
     * </code> if no such color has been cached.
     */
    private Color editableBackgroundColor;

    /**
     * Cached read-only background color, or <code>null
     * </code> if no such color has been cached.
     */
    private Color readOnlyBackgroundColor;

    // Public Static Methods

    /**
     * Align the specified megawidgets' component elements to one another so as
     * to make them presentable.
     * 
     * @param megawidgets
     *            Megawidgets that are to be aligned.
     */
    public static void alignMegawidgetsElements(
            Collection<? extends IMegawidget> megawidgets) {

        // Determine which megawidgets have, respectively, the largest left
        // and right decoration widths, and set all the megawidgets to have
        // those widths for their respective left and right decorations.
        int maxLeftDecorationWidth = 0, maxRightDecorationWidth = 0;
        for (IMegawidget megawidget : megawidgets) {
            int leftDecorationWidth = megawidget.getLeftDecorationWidth();
            if (leftDecorationWidth > maxLeftDecorationWidth) {
                maxLeftDecorationWidth = leftDecorationWidth;
            }
            int rightDecorationWidth = megawidget.getRightDecorationWidth();
            if (rightDecorationWidth > maxRightDecorationWidth) {
                maxRightDecorationWidth = rightDecorationWidth;
            }
        }
        for (IMegawidget megawidget : megawidgets) {
            megawidget.setLeftDecorationWidth(maxLeftDecorationWidth);
            megawidget.setRightDecorationWidth(maxRightDecorationWidth);
        }
    }

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier for this megawidget.
     */
    protected Megawidget(MegawidgetSpecifier specifier) {
        this.specifier = specifier;
        enabled = getSpecifier().isEnabled();
        editable = getSpecifier().isEditable();
    }

    // Public Methods

    /**
     * Get the specifier for this megawidget.
     * 
     * @return Specifier for this megawidget.
     */
    @Override
    @SuppressWarnings("unchecked")
    public final <S extends MegawidgetSpecifier> S getSpecifier() {
        return (S) specifier;
    }

    /**
     * Get the mutable property names for this megawidget. Concrete subclasses
     * must override this method to return the list of all mutable property
     * names for those classes, if their set of mutable properties consists of
     * more than <code>MegawidgetSpecifier.MEGAWIDGET_EDITABLE</code> and <code>
     * MegawidgetSpecifier.MEGAWIDGET_ENABLED</code>.
     * 
     * @return Set of names for all mutable properties for this megawidget.
     */
    @Override
    public Set<String> getMutablePropertyNames() {
        return MUTABLE_PROPERTY_NAMES;
    }

    /**
     * Get the current mutable property value for the specified name. Subclasses
     * must override this if they have any mutable properties beyond <code>
     * MegawidgetSpecifier.MEGAWIDGET_EDITABLE</code> and <code>
     * MegawidgetSpecifier.MEGAWIDGET_ENABLED</code>. When this method is
     * overridden, the subclass implementation should always first determine
     * whether to handle handle the <code>name</code> that was passed in, and if
     * it will not be handling it, to return the result of calling its
     * superclass's implementation of this method.
     * 
     * @param name
     *            Name of the mutable property value to be fetched.
     * @return Mutable property value.
     * @throws MegawidgetPropertyException
     *             If the name specifies a nonexistent property.
     */
    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (name.equals(MegawidgetSpecifier.MEGAWIDGET_EDITABLE)) {
            return isEditable();
        } else if (name.equals(MegawidgetSpecifier.MEGAWIDGET_ENABLED)) {
            return isEnabled();
        } else {
            throw new MegawidgetPropertyException(specifier.getIdentifier(),
                    name, specifier.getType(), null, "nonexistent property");
        }
    }

    /**
     * Set the current mutable property value for the specified name. Subclasses
     * must override this if they have any mutable properties beyond <code>
     * MegawidgetSpecifier.MEGAWIDGET_EDITABLE</code> and <code>
     * MegawidgetSpecifier.MEGAWIDGET_ENABLED</code>. When this method is
     * overridden, the subclass implementation should always first determine
     * whether to handle handle the <code>name</code> that was passed in, and if
     * it will not be handling it, to call its superclass's implementation of
     * this method.
     * 
     * @param name
     *            Name of the mutable property value to be fetched.
     * @param value
     *            New mutable property value to be used.
     * @throws MegawidgetPropertyException
     *             If the name specifies a nonexistent property, or if the value
     *             is invalid.
     */
    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(MegawidgetSpecifier.MEGAWIDGET_EDITABLE)) {
            setEditable(getPropertyBooleanValueFromObject(value, name, null));
        } else if (name.equals(MegawidgetSpecifier.MEGAWIDGET_ENABLED)) {
            setEnabled(getPropertyBooleanValueFromObject(value, name, null));
        } else {
            throw new MegawidgetPropertyException(specifier.getIdentifier(),
                    name, specifier.getType(), null, "nonexistent property");
        }
    }

    /**
     * Get the mutable properties of this megawidget.
     * 
     * @return Map of all mutable property names to their current values.
     */
    @Override
    public final Map<String, Object> getMutableProperties() {
        Map<String, Object> map = Maps.newHashMap();
        try {
            for (String name : getMutablePropertyNames()) {
                map.put(name, getMutableProperty(name));
            }
        } catch (MegawidgetPropertyException e) {
            throw new IllegalStateException(
                    "querying valid mutable property \"" + e.getName()
                            + "\" caused internal error", e);
        }
        return map;
    }

    /**
     * Set the mutable properties of this megawidget.
     * <p>
     * This method must be overridden if the properties being set are
     * interdependent; for example, one property's value's validity depends upon
     * another property's value. In that case, any interdependent properties
     * should be dealt with first, and then the superclass's implementation of
     * this method may be invoked.
     * 
     * @param properties
     *            Map containing keys drawn from the set of all valid property
     *            names, with associated values being the new values for the
     *            properties. Any property with a name-value pair found within
     *            this map is set to the given value; all properties for which
     *            no name-value pairs exist remain as they were before.
     * @throws MegawidgetPropertyException
     *             If at least one name specifies a nonexistent property, or if
     *             at least one value is invalid.
     */
    @Override
    public void setMutableProperties(Map<String, Object> properties)
            throws MegawidgetPropertyException {
        for (String name : properties.keySet()) {
            setMutableProperty(name, properties.get(name));
        }
    }

    /**
     * Determine whether or not the megawidget is currently enabled.
     * 
     * @return True if the megawidget is currently enabled, false otherwise.
     */
    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable the megawidget.
     * 
     * @param enable
     *            Flag indicating whether the megawidget is to be enabled or
     *            disabled.
     */
    @Override
    public final void setEnabled(boolean enable) {
        enabled = enable;
        doSetEnabled(enabled);
    }

    /**
     * Determine whether or not the megawidget is currently editable.
     * 
     * @return True if the megawidget is currently editable, false otherwise.
     */
    @Override
    public final boolean isEditable() {
        return editable;
    }

    /**
     * Render the megawidget editable or read-only.
     * 
     * @param editable
     *            Flag indicating whether the megawidget is to be editable or
     *            read-only.
     */
    @Override
    public final void setEditable(boolean editable) {
        this.editable = editable;
        doSetEditable(editable);
    }

    /**
     * Determine the left decoration width for this megawidget, if the widget
     * has a decoration (label, etc.) to the left of its main component
     * widget(s). This is used to query all sibling megawidgets to determine
     * which one has the largest left decoration.
     * <p>
     * The default implementation does nothing; any subclass that has left
     * decorations should calculate the width of the largest of said decorations
     * and return it. The method <code>getWidestWidgetWidth()
     * </code> may be used for this purpose.
     * 
     * @return Width in pixels required for the left decoration of this
     *         megawidget, or 0 if the megawidget has no left decoration.
     */
    @Override
    public int getLeftDecorationWidth() {
        return 0;
    }

    /**
     * Set the left decoration width for this megawidget to that specified, if
     * the widget has a decoration to the left of its main component widget(s).
     * This is used to set sibling megawidgets to all use the width of the
     * largest left decoration used by the siblings, if any.
     * <p>
     * The default implementation does nothing; any subclass that has left
     * decorations should set their widths to that specified. The utility method
     * <code>setWidgetsWidth()</code> may be used for this purpose.
     * 
     * @param width
     *            Width to be used if this megawidget has a left decoration.
     */
    @Override
    public void setLeftDecorationWidth(int width) {

        // No action.
    }

    /**
     * Determine the right decoration width for this megawidget, if the widget
     * has a decoration (label, etc.) to the right of its main component
     * widget(s). This is used to query all sibling megawidgets to determine
     * which one has the largest right decoration.
     * <p>
     * The default implementation does nothing; any subclass that has right
     * decorations should calculate the width of the largest of said decorations
     * and return it. The method <code>
     * getWidestWidgetWidth()</code> may be used for this purpose.
     * 
     * @return Width in pixels required for the right decoration of this
     *         megawidget, or 0 if the megawidget has no right decoration.
     */
    @Override
    public int getRightDecorationWidth() {
        return 0;
    }

    /**
     * Set the right decoration width for this megawidget to that specified, if
     * the widget has a decoration to the right of its main component widget(s).
     * This is used to set sibling megawidgets to all use the width of the
     * largest right decoration used by the siblings, if any.
     * <p>
     * The default implementation does nothing; any subclass that has right
     * decorations should set their widths to that specified. The utility method
     * <code>setWidgetsWidth()</code> may be used for this purpose.
     * 
     * @param width
     *            Width to be used if this megawidget has a right decoration.
     */
    @Override
    public void setRightDecorationWidth(int width) {

        // No action.
    }

    // Protected Methods

    /**
     * Change the component widgets to ensure their state matches that of the
     * enabled flag.
     * 
     * @param enable
     *            Flag indicating whether the component widgets are to be
     *            enabled or disabled.
     */
    protected abstract void doSetEnabled(boolean enable);

    /**
     * Change the component widgets to ensure their state matches that of the
     * editable flag.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     */
    protected abstract void doSetEditable(boolean editable);

    /**
     * Get the background color providing the user the appropriate visual cue
     * for the specified editable state. Note that this method ensures that the
     * returned color will be disposed of when the megawidget for which it is to
     * be used is destroyed, so the color does not need to be disposed of by the
     * invoker.
     * 
     * @param editable
     *            Flag indicating whether the background color being fetched is
     *            to be used to show that the megawidget is editable or
     *            read-only.
     * @param control
     *            Control that will be using the fetched background color.
     * @param label
     *            Optional label widget from which to fetch the color if
     *            necessary. Providing this widget may allow this method to be
     *            less resource-intensive when executed. If <code>null</code>, a
     *            temporary label will be created and then disposed of once it
     *            has been queried for the background color.
     * @return Background color providing a visual cue appropriate to the
     *         specified editable state.
     */
    protected final Color getBackgroundColor(boolean editable, Control control,
            Label label) {

        // If the colors have not yet been cached, fetch
        // them now.
        if (editableBackgroundColor == null) {
            editableBackgroundColor = control.getBackground();
            if (label != null) {
                readOnlyBackgroundColor = label.getBackground();
            } else {
                Label temporaryLabel = new Label(control.getParent(), SWT.NONE);
                readOnlyBackgroundColor = temporaryLabel.getBackground();
                temporaryLabel.dispose();
            }

            // Attach a dispose listener to the provided
            // control to ensure that the colors are dis-
            // posed of when the control is destroyed.
            control.addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    if (editableBackgroundColor.isDisposed() == false) {
                        editableBackgroundColor.dispose();
                    }
                    if (readOnlyBackgroundColor.isDisposed() == false) {
                        readOnlyBackgroundColor.dispose();
                    }
                }
            });
        }

        // Return the appropriate color.
        return (editable ? editableBackgroundColor : readOnlyBackgroundColor);
    }

    /**
     * Get the width in pixels of the widest of the specified widgets.
     * 
     * @param widgets
     *            Widgets to be measured.
     * @return Number of pixels indicating the width of the widest of the
     *         specified widgets.
     */
    protected final int getWidestWidgetWidth(Control... widgets) {
        int maxWidth = 0;
        for (Control widget : widgets) {
            int width = widget.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
            if (width > maxWidth) {
                maxWidth = width;
            }
        }
        return maxWidth;
    }

    /**
     * Set the width in pixels of the specified widgets to the specified width.
     * The widgets are assumed to be laid out with a grid layout, and to be
     * using grid data as their layout data.
     * 
     * @param width
     *            Width to which to set the widgets.
     * @param widgets
     *            Widgets to have their widths set.
     */
    protected final void setWidgetsWidth(int width, Control... widgets) {
        for (Control widget : widgets) {
            ((GridData) widget.getLayoutData()).widthHint = width;
        }
    }

    /**
     * Get an integer from the specified object as a value for the specified
     * property name. The object must be either <code>null</code> (only allowed
     * if <code>defValue</code> is not <code>null</code>), or an object of type
     * <code>Number</code>. This method is used to ensure that any value
     * specified as a number, but within the bounds of a standard integer, is
     * properly handled.
     * 
     * @param object
     *            Object holding the integer value.
     * @param name
     *            Property name for which this object could be the value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Integer value.
     * @throws MegawidgetPropertyException
     *             If the property value is invalid.
     */
    protected final int getPropertyIntegerValueFromObject(Object object,
            String name, Integer defValue) throws MegawidgetPropertyException {
        try {
            return getSpecifier().getIntegerValueFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(e.getIdentifier(), name,
                    e.getType(), e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Get an integer object from the specified object as a value for the
     * specified property name. The object must be either <code>null</code>
     * (only allowed if <code>defValue</code> is not <code> null</code>), or an
     * object of type <code>Number</code>. This method is used to ensure that
     * any value specified as a number, but within the bounds of a standard
     * integer, is properly handled. If the object is a <code>Integer</code>, it
     * is simply cast to this type and returned.
     * 
     * @param object
     *            Object holding the integer value.
     * @param name
     *            Property name for which this object could be the value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Integer object.
     * @throws MegawidgetPropertyException
     *             If the property value is invalid.
     */
    protected final Integer getPropertyIntegerObjectFromObject(Object object,
            String name, Integer defValue) throws MegawidgetPropertyException {
        try {
            return getSpecifier().getIntegerObjectFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(e.getIdentifier(), name,
                    e.getType(), e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Get a long integer from the specified object as a value for the specified
     * property name. The object must be either <code>null</code> (only allowed
     * if <code>defValue</code> is not <code>null</code>), or an object of type
     * <code>Number</code>. This method is used to ensure that any value
     * specified as a number, but within the bounds of a standard long integer,
     * is properly handled.
     * 
     * @param object
     *            Object holding the long integer value.
     * @param name
     *            Property name for which this object could be the value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Long integer value.
     * @throws MegawidgetPropertyException
     *             If the property value is invalid.
     */
    protected final long getPropertyLongValueFromObject(Object object,
            String name, Long defValue) throws MegawidgetPropertyException {
        try {
            return getSpecifier().getLongValueFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(e.getIdentifier(), name,
                    e.getType(), e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Get a long integer object from the specified object as a value for the
     * specified property name. The object must be either <code>null</code>
     * (only allowed if <code>defValue</code> is not <code>null</code>), or an
     * object of type <code>Number</code>. This method is used to ensure that
     * any value specified as a number, but within the bounds of a standard long
     * integer, is properly handled. If the object is a <code>Long</code>, it is
     * simply cast to this type and returned.
     * 
     * @param object
     *            Object holding the long integer value.
     * @param name
     *            Property name for which this object could be the value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Long integer object.
     * @throws MegawidgetPropertyException
     *             If the property value is invalid.
     */
    protected final Long getPropertyLongObjectFromObject(Object object,
            String name, Long defValue) throws MegawidgetPropertyException {
        try {
            return getSpecifier().getLongObjectFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(e.getIdentifier(), name,
                    e.getType(), e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Get a boolean from the specified object as a value for the specified
     * property name. The object must be either <code>null</code> (only allowed
     * if <code>defValue</code> is not <code>null</code>), or an object of type
     * <code>Boolean</code>, <code>Integer</code> or <code>Long</code>. This
     * method is used to ensure that any value specified as a boolean, or as a
     * long or integer of either 0 or 1, is properly handled.
     * 
     * @param object
     *            Object holding the boolean value.
     * @param name
     *            Property name for which this object could be the value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Boolean value.
     * @throws MegawidgetPropertyException
     *             If the property value is invalid.
     */
    protected final boolean getPropertyBooleanValueFromObject(Object object,
            String name, Boolean defValue) throws MegawidgetPropertyException {
        try {
            return getSpecifier().getBooleanValueFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(e.getIdentifier(), name,
                    e.getType(), e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Get a boolean object from the specified object as a value for the
     * specified property name. The object must be either <code>null</code>
     * (only allowed if <code>defValue</code> is not <code>null</code>), or an
     * object of type <code>Boolean</code>, <code>Integer</code> or <code>
     * Long</code>. This method is used to ensure that any value specified as a
     * boolean, or as a long or integer of either 0 or 1, is properly handled.
     * If the object is a <code>Boolean</code>, it is simply cast to this type
     * and returned.
     * 
     * @param object
     *            Object holding the boolean value.
     * @param name
     *            Property name for which this object could be the value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Boolean object.
     * @throws MegawidgetPropertyException
     *             If the property value is invalid.
     */
    protected final Boolean getPropertyBooleanObjectFromObject(Object object,
            String name, Boolean defValue) throws MegawidgetPropertyException {
        try {
            return getSpecifier().getBooleanObjectFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(e.getIdentifier(), name,
                    e.getType(), e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Get a dynamically typed object from the specified object as a value for
     * the specified property name. The object must be either <code>null</code>
     * (only allowed if <code>defValue</code> is not <code>null</code>), or an
     * object of dynamic type <code>T</code>.
     * 
     * @param object
     *            Object to be cast or converted.
     * @param name
     *            Property name for which this object could be the value.
     * @param requiredClass
     *            Class to which this object must be cast or converted.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Object of the specified dynamic type.
     * @throws MegawidgetPropertyException
     *             If the property value is invalid.
     */
    protected final <T> T getPropertyDynamicallyTypedObjectFromObject(
            Object object, String name, Class<T> requiredClass, T defValue)
            throws MegawidgetPropertyException {
        try {
            return getSpecifier().getDynamicallyTypedObjectFromObject(object,
                    requiredClass, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(e.getIdentifier(), name,
                    e.getType(), e.getBadValue(), e.getMessage(), e.getCause());
        }
    }
}