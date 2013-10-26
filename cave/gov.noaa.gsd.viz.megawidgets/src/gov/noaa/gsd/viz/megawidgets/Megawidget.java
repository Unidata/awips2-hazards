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
 * Oct 23, 2013   2168     Chris.Golden      Removed functionality that belonged
 *                                           in implementations of the new
 *                                           IControl interface, placing it
 *                                           instead in said implementations.
 *                                           Also added helper methods for
 *                                           getting floats and doubles from
 *                                           arbitrary property value objects.
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
        MUTABLE_PROPERTY_NAMES = ImmutableSet
                .of(MegawidgetSpecifier.MEGAWIDGET_ENABLED);
    };

    // Private Variables

    /**
     * Specifier for this megawidget.
     */
    private final ISpecifier specifier;

    /**
     * Flag indicating whether the megawidget is currently enabled.
     */
    private boolean enabled;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier for this megawidget.
     */
    protected Megawidget(ISpecifier specifier) {
        this.specifier = specifier;
        enabled = getSpecifier().isEnabled();
    }

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public final <S extends MegawidgetSpecifier> S getSpecifier() {
        return (S) specifier;
    }

    @Override
    public Set<String> getMutablePropertyNames() {
        return MUTABLE_PROPERTY_NAMES;
    }

    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (name.equals(MegawidgetSpecifier.MEGAWIDGET_ENABLED)) {
            return isEnabled();
        }
        throw new MegawidgetPropertyException(specifier.getIdentifier(), name,
                specifier.getType(), null, "nonexistent property");
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(MegawidgetSpecifier.MEGAWIDGET_ENABLED)) {
            setEnabled(getPropertyBooleanValueFromObject(value, name, null));
        } else {
            throw new MegawidgetPropertyException(specifier.getIdentifier(),
                    name, specifier.getType(), null, "nonexistent property");
        }
    }

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

    @Override
    public void setMutableProperties(Map<String, Object> properties)
            throws MegawidgetPropertyException {
        for (String name : properties.keySet()) {
            setMutableProperty(name, properties.get(name));
        }
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public final void setEnabled(boolean enable) {
        enabled = enable;
        doSetEnabled(enabled);
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
     * Get a float from the specified object as a value for the specified
     * property name. The object must be either <code>null</code> (only allowed
     * if <code>defValue</code> is not <code>null</code>), or an object of type
     * <code>Number</code>. This method is used to ensure that any value
     * specified as a number, but within the bounds of a standard float, is
     * properly handled.
     * 
     * @param object
     *            Object holding the float value.
     * @param name
     *            Property name for which this object could be the value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Float value.
     * @throws MegawidgetPropertyException
     *             If the property value is invalid.
     */
    protected final float getPropertyFloatValueFromObject(Object object,
            String name, Float defValue) throws MegawidgetPropertyException {
        try {
            return getSpecifier().getFloatValueFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(e.getIdentifier(), name,
                    e.getType(), e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Get a float object from the specified object as a value for the specified
     * property name. The object must be either <code>null</code> (only allowed
     * if <code>defValue</code> is not <code> null</code>), or an object of type
     * <code>Number</code>. This method is used to ensure that any value
     * specified as a number, but within the bounds of a standard float, is
     * properly handled. If the object is a <code>Float</code>, it is simply
     * cast to this type and returned.
     * 
     * @param object
     *            Object holding the float value.
     * @param name
     *            Property name for which this object could be the value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Float object.
     * @throws MegawidgetPropertyException
     *             If the property value is invalid.
     */
    protected final Float getPropertyFloatObjectFromObject(Object object,
            String name, Float defValue) throws MegawidgetPropertyException {
        try {
            return getSpecifier().getFloatObjectFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(e.getIdentifier(), name,
                    e.getType(), e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Get a double from the specified object as a value for the specified
     * property name. The object must be either <code>null</code> (only allowed
     * if <code>defValue</code> is not <code>null</code>), or an object of type
     * <code>Number</code>. This method is used to ensure that any value
     * specified as a number is properly handled.
     * 
     * @param object
     *            Object holding the double value.
     * @param name
     *            Property name for which this object could be the value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Double value.
     * @throws MegawidgetPropertyException
     *             If the property value is invalid.
     */
    protected final double getPropertyDoubleValueFromObject(Object object,
            String name, Double defValue) throws MegawidgetPropertyException {
        try {
            return getSpecifier().getDoubleValueFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(e.getIdentifier(), name,
                    e.getType(), e.getBadValue(), e.getMessage(), e.getCause());
        }
    }

    /**
     * Get a double object from the specified object as a value for the
     * specified property name. The object must be either <code>null</code>
     * (only allowed if <code>defValue</code> is not <code>null</code>), or an
     * object of type <code>Number</code>. This method is used to ensure that
     * any value specified as a number is properly handled. If the object is a
     * <code>Double</code>, it is simply cast to this type and returned.
     * 
     * @param object
     *            Object holding the double value.
     * @param name
     *            Property name for which this object could be the value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Double object.
     * @throws MegawidgetPropertyException
     *             If the property value is invalid.
     */
    protected final Double getPropertyDoubleObjectFromObject(Object object,
            String name, Double defValue) throws MegawidgetPropertyException {
        try {
            return getSpecifier().getDoubleObjectFromObject(object, defValue);
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