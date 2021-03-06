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

import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.colormap.Color;

/**
 * Description: Conversion utilities, used to convert arbitrary objects into
 * specific types.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 23, 2014   2925     Chris.Golden Initial creation; moved these methods
 *                                      from Megawidget, MegawidgetSpecifier,
 *                                      etc. into this class.
 * Mar 31, 2016  15931     Chris.Golden Added methods to get and sanity check
 *                                      maps representing RGB colors.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ConversionUtilities {

    // Public Static Constants

    /**
     * Red color component key for a {@link Map} representing a color.
     */
    public static final String COLOR_AS_MAP_RED = "red";

    /**
     * Green color component key for a {@link Map} representing a color.
     */
    public static final String COLOR_AS_MAP_GREEN = "green";

    /**
     * Blue color component key for a {@link Map} representing a color.
     */
    public static final String COLOR_AS_MAP_BLUE = "blue";

    // Public Static Methods

    /**
     * Get an integer from the specified object as a specifier parameter. The
     * object must be either <code>null</code> (only allowed if <code>defValue
     * </code> is not <code>null</code>), or an object of type {@link Number}.
     * This method is used to ensure that any value specified as a number, but
     * within the bounds of a standard integer, is properly handled.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the integer value.
     * @param paramName
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Integer value.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameter is invalid.
     */
    public static final int getSpecifierIntegerValueFromObject(
            String identifier, String type, Object object, String paramName,
            Integer defValue) throws MegawidgetSpecificationException {
        try {
            return getIntegerValueFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(paramName, e);
        }
    }

    /**
     * Get an integer from the specified object as a specifier parameter. The
     * object must be either <code>null</code> (only allowed if <code>defValue
     * </code> is not <code>null</code>), or an object of type {@link Number}.
     * This method is used to ensure that any value specified as a number, but
     * within the bounds of a standard integer, is properly handled. If the
     * object is a {@link Integer}, it is simply cast to this type and returned.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the integer value.
     * @param paramName
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Integer object.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameter is invalid.
     */
    public static final Integer getSpecifierIntegerObjectFromObject(
            String identifier, String type, Object object, String paramName,
            Integer defValue) throws MegawidgetSpecificationException {
        try {
            return getIntegerObjectFromObject(identifier, type, object,
                    defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(paramName, e);
        }
    }

    /**
     * Get a long integer from the specified object as a specifier parameter.
     * The object must be either <code>null</code> (only allowed if <code>
     * defValue</code> is not <code>null</code>), or an object of type
     * {@link Number}. This method is used to ensure that any value specified as
     * a number, but within the bounds of a standard long integer, is properly
     * handled.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the long integer value.
     * @param paramName
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Long integer value.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameter is invalid.
     */
    public static final long getSpecifierLongValueFromObject(String identifier,
            String type, Object object, String paramName, Long defValue)
            throws MegawidgetSpecificationException {
        try {
            return getLongValueFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(paramName, e);
        }
    }

    /**
     * Get a long integer from the specified object as a specifier parameter.
     * The object must be either <code>null</code> (only allowed if <code>
     * defValue</code> is not <code>null</code>), or an object of type
     * {@link Number}. This method is used to ensure that any value specified as
     * a number, but within the bounds of a standard long integer, is properly
     * handled. If the object is a {@link Long}, it is simply cast to this type
     * and returned.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the long integer value.
     * @param paramName
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Long integer object.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameter is invalid.
     */
    public static final Long getSpecifierLongObjectFromObject(
            String identifier, String type, Object object, String paramName,
            Long defValue) throws MegawidgetSpecificationException {
        try {
            return getLongObjectFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(paramName, e);
        }
    }

    /**
     * Get a float from the specified object as a specifier parameter. The
     * object must be either <code>null</code> (only allowed if <code>defValue
     * </code> is not <code>null</code>), or an object of type {@link Number}.
     * This method is used to ensure that any value specified as a number, but
     * within the bounds of a standard float, is properly handled.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the float value.
     * @param paramName
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Float value.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameter is invalid.
     */
    public static final float getSpecifierFloatValueFromObject(
            String identifier, String type, Object object, String paramName,
            Float defValue) throws MegawidgetSpecificationException {
        try {
            return getFloatValueFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(paramName, e);
        }
    }

    /**
     * Get a float from the specified object as a specifier parameter. The
     * object must be either <code>null</code> (only allowed if <code>defValue
     * </code> is not <code>null</code>), or an object of type {@link Number}.
     * This method is used to ensure that any value specified as a number, but
     * within the bounds of a standard float, is properly handled. If the object
     * is a {@link Float}, it is simply cast to this type and returned.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the float value.
     * @param paramName
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Float object.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameter is invalid.
     */
    public static final Float getSpecifierFloatObjectFromObject(
            String identifier, String type, Object object, String paramName,
            Float defValue) throws MegawidgetSpecificationException {
        try {
            return getFloatObjectFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(paramName, e);
        }
    }

    /**
     * Get a double from the specified object as a specifier parameter. The
     * object must be either <code>null</code> (only allowed if <code>defValue
     * </code> is not <code>null</code>), or an object of type {@link Number}.
     * This method is used to ensure that any value specified as a number is
     * properly handled.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the double value.
     * @param paramName
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Double value.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameter is invalid.
     */
    public static final double getSpecifierDoubleValueFromObject(
            String identifier, String type, Object object, String paramName,
            Double defValue) throws MegawidgetSpecificationException {
        try {
            return getDoubleValueFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(paramName, e);
        }
    }

    /**
     * Get a double from the specified object as a specifier parameter. The
     * object must be either <code>null</code> (only allowed if <code>
     * defValue</code> is not <code>null</code>), or an object of type
     * {@link Number}. This method is used to ensure that any value specified as
     * a number is properly handled. If the object is a {@link Double}, it is
     * simply cast to this type and returned.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the double value.
     * @param paramName
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Double object.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameter is invalid.
     */
    public static final Double getSpecifierDoubleObjectFromObject(
            String identifier, String type, Object object, String paramName,
            Double defValue) throws MegawidgetSpecificationException {
        try {
            return getDoubleObjectFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(paramName, e);
        }
    }

    /**
     * Get a boolean from the specified object as a specifier parameter. The
     * object must be either <code>null</code> (only allowed if <code>defValue
     * </code> is not <code>null</code>), or an object of type {@link Boolean},
     * {@link Integer} or {@link Long}. This method is used to ensure that any
     * value specified as a boolean, or as a long or integer of either 0 or 1,
     * is properly handled.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the boolean value.
     * @param paramName
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Boolean value.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameter is invalid.
     */
    public static final boolean getSpecifierBooleanValueFromObject(
            String identifier, String type, Object object, String paramName,
            Boolean defValue) throws MegawidgetSpecificationException {
        try {
            return getBooleanValueFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(paramName, e);
        }
    }

    /**
     * Get a boolean object from the specified object as a specifier parameter.
     * The object must be either <code>null</code> (only allowed if <code>
     * defValue</code> is not <code>null</code>), or an object of type
     * {@link Boolean}, {@link Integer} or {@link Long}. This method is used to
     * ensure that any value specified as a boolean, or as a long or integer of
     * either 0 or 1, is properly handled. If the object is a {@link Boolean},
     * it is simply cast to this type and returned.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the boolean value.
     * @param paramName
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Boolean object.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameter is invalid.
     */
    public static final Boolean getSpecifierBooleanObjectFromObject(
            String identifier, String type, Object object, String paramName,
            Boolean defValue) throws MegawidgetSpecificationException {
        try {
            return getBooleanObjectFromObject(identifier, type, object,
                    defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(paramName, e);
        }
    }

    /**
     * Get a map representing a color object from the specified object as a
     * specifier parameter. The object must either be <code>null</code> (only
     * allowed if <code>defValue</code> is not <code>null</code>), or be a map
     * holding values between <code>0.0</code> and <code>1.0</code> for
     * {@link #COLOR_AS_MAP_RED}, {@link #COLOR_AS_MAP_GREEN}, and
     * {@link #COLOR_AS_MAP_BLUE} entries.
     * <p>
     * Note that this method has been deprecated; megawidgets should use
     * {@link Color} objects instead, as returned by
     * {@link #getSpecifierColorFromObject(String, String, Object, String, Color)}
     * .
     * </p>
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the color value.
     * @param paramName
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Map object representing a color.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameter is invalid.
     */
    @Deprecated
    public static final Map<String, Double> getSpecifierColorAsMapFromObject(
            String identifier, String type, Object object, String paramName,
            Map<String, Double> defValue)
            throws MegawidgetSpecificationException {
        try {
            return getColorAsMapFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(paramName, e);
        }
    }

    /**
     * Get a color object from the specified object as a specifier parameter.
     * The object must either be <code>null</code> (only allowed if
     * <code>defValue</code> is not <code>null</code>), or be a map holding
     * values between <code>0.0</code> and <code>1.0</code> for
     * {@link #COLOR_AS_MAP_RED}, {@link #COLOR_AS_MAP_GREEN}, and
     * {@link #COLOR_AS_MAP_BLUE} entries.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the color value.
     * @param paramName
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Color object.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameter is invalid.
     */
    public static final Color getSpecifierColorFromObject(String identifier,
            String type, Object object, String paramName, Color defValue)
            throws MegawidgetSpecificationException {
        try {
            return getColorFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(paramName, e);
        }
    }

    /**
     * Get a dynamically typed object from the specified object as a specifier
     * parameter. The object must be either <code>null</code> (only allowed if
     * <code>defValue</code> is not <code>null</code>), or an object of dynamic
     * type <code>T</code>.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object to be cast or converted.
     * @param paramName
     *            Name of the parameter for which <code>object</code> is the
     *            value.
     * @param requiredClass
     *            Class to which this object must be cast or converted.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Object of the specified dynamic type.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameter is invalid.
     */
    public static final <T> T getSpecifierDynamicallyTypedObjectFromObject(
            String identifier, String type, Object object, String paramName,
            Class<T> requiredClass, T defValue)
            throws MegawidgetSpecificationException {
        try {
            return getDynamicallyTypedObjectFromObject(identifier, type,
                    object, requiredClass, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(paramName, e);
        }
    }

    /**
     * Get an integer from the specified object as a value for the specified
     * property name. The object must be either <code>null</code> (only allowed
     * if <code>defValue</code> is not <code>null</code>), or an object of type
     * {@link Number}. This method is used to ensure that any value specified as
     * a number, but within the bounds of a standard integer, is properly
     * handled.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
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
    public static final int getPropertyIntegerValueFromObject(
            String identifier, String type, Object object, String name,
            Integer defValue) throws MegawidgetPropertyException {
        try {
            return getIntegerValueFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(name, e);
        }
    }

    /**
     * Get an integer object from the specified object as a value for the
     * specified property name. The object must be either <code>null</code>
     * (only allowed if <code>defValue</code> is not <code>null</code>), or an
     * object of type {@link Number}. This method is used to ensure that any
     * value specified as a number, but within the bounds of a standard integer,
     * is properly handled. If the object is a {@link Integer}, it is simply
     * cast to this type and returned.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
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
    public static final Integer getPropertyIntegerObjectFromObject(
            String identifier, String type, Object object, String name,
            Integer defValue) throws MegawidgetPropertyException {
        try {
            return getIntegerObjectFromObject(identifier, type, object,
                    defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(name, e);
        }
    }

    /**
     * Get a long integer from the specified object as a value for the specified
     * property name. The object must be either <code>null</code> (only allowed
     * if <code>defValue</code> is not <code>null</code>), or an object of type
     * {@link Number}. This method is used to ensure that any value specified as
     * a number, but within the bounds of a standard long integer, is properly
     * handled.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
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
    public static final long getPropertyLongValueFromObject(String identifier,
            String type, Object object, String name, Long defValue)
            throws MegawidgetPropertyException {
        try {
            return getLongValueFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(name, e);
        }
    }

    /**
     * Get a long integer object from the specified object as a value for the
     * specified property name. The object must be either <code>null</code>
     * (only allowed if <code>defValue</code> is not <code>null</code>), or an
     * object of type {@link Number}. This method is used to ensure that any
     * value specified as a number, but within the bounds of a standard long
     * integer, is properly handled. If the object is a {@link Long}, it is
     * simply cast to this type and returned.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
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
    public static final Long getPropertyLongObjectFromObject(String identifier,
            String type, Object object, String name, Long defValue)
            throws MegawidgetPropertyException {
        try {
            return getLongObjectFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(name, e);
        }
    }

    /**
     * Get a float from the specified object as a value for the specified
     * property name. The object must be either <code>null</code> (only allowed
     * if <code>defValue</code> is not <code>null</code>), or an object of type
     * {@link Number}. This method is used to ensure that any value specified as
     * a number, but within the bounds of a standard float, is properly handled.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
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
    public static final float getPropertyFloatValueFromObject(
            String identifier, String type, Object object, String name,
            Float defValue) throws MegawidgetPropertyException {
        try {
            return getFloatValueFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(name, e);
        }
    }

    /**
     * Get a float object from the specified object as a value for the specified
     * property name. The object must be either <code>null</code> (only allowed
     * if <code>defValue</code> is not <code> null</code>), or an object of type
     * {@link Number}. This method is used to ensure that any value specified as
     * a number, but within the bounds of a standard float, is properly handled.
     * If the object is a {@link Float}, it is simply cast to this type and
     * returned.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
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
    public static final Float getPropertyFloatObjectFromObject(
            String identifier, String type, Object object, String name,
            Float defValue) throws MegawidgetPropertyException {
        try {
            return getFloatObjectFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(name, e);
        }
    }

    /**
     * Get a double from the specified object as a value for the specified
     * property name. The object must be either <code>null</code> (only allowed
     * if <code>defValue</code> is not <code>null</code>), or an object of type
     * {@link Number}. This method is used to ensure that any value specified as
     * a number is properly handled.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
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
    public static final double getPropertyDoubleValueFromObject(
            String identifier, String type, Object object, String name,
            Double defValue) throws MegawidgetPropertyException {
        try {
            return getDoubleValueFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(name, e);
        }
    }

    /**
     * Get a double object from the specified object as a value for the
     * specified property name. The object must be either <code>null</code>
     * (only allowed if <code>defValue</code> is not <code>null</code>), or an
     * object of type {@link Number}. This method is used to ensure that any
     * value specified as a number is properly handled. If the object is a
     * {@link Double}, it is simply cast to this type and returned.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
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
    public static final Double getPropertyDoubleObjectFromObject(
            String identifier, String type, Object object, String name,
            Double defValue) throws MegawidgetPropertyException {
        try {
            return getDoubleObjectFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(name, e);
        }
    }

    /**
     * Get a boolean from the specified object as a value for the specified
     * property name. The object must be either <code>null</code> (only allowed
     * if <code>defValue</code> is not <code>null</code>), or an object of type
     * {@link Boolean}, {@link Integer} or {@link Long}. This method is used to
     * ensure that any value specified as a boolean, or as a long or integer of
     * either 0 or 1, is properly handled.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
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
    public static final boolean getPropertyBooleanValueFromObject(
            String identifier, String type, Object object, String name,
            Boolean defValue) throws MegawidgetPropertyException {
        try {
            return getBooleanValueFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(name, e);
        }
    }

    /**
     * Get a boolean object from the specified object as a value for the
     * specified property name. The object must be either <code>null</code>
     * (only allowed if <code>defValue</code> is not <code>null</code>), or an
     * object of type {@link Boolean}, {@link Integer} or {@link Long}. This
     * method is used to ensure that any value specified as a boolean, or as a
     * long or integer of either 0 or 1, is properly handled. If the object is a
     * {@link Boolean}, it is simply cast to this type and returned.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
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
    public static final Boolean getPropertyBooleanObjectFromObject(
            String identifier, String type, Object object, String name,
            Boolean defValue) throws MegawidgetPropertyException {
        try {
            return getBooleanObjectFromObject(identifier, type, object,
                    defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(name, e);
        }
    }

    /**
     * Get a map representing a color object from the specified object as a
     * specified property name. The object must either be <code>null</code>
     * (only allowed if <code>defValue</code> is not <code>null</code>), or be a
     * map holding values between <code>0.0</code> and <code>1.0</code> for
     * {@link #COLOR_AS_MAP_RED}, {@link #COLOR_AS_MAP_GREEN}, and
     * {@link #COLOR_AS_MAP_BLUE} entries.
     * <p>
     * Note that this method has been deprecated; megawidgets should use
     * {@link Color} objects instead, as returned by
     * {@link #getPropertyColorFromObject(String, String, Object, String, Color)}
     * .
     * </p>
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the color value.
     * @param name
     *            Property name for which this object could be the value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Map object representing a color.
     * @throws MegawidgetPropertyException
     *             If the property value is invalid.
     */
    @Deprecated
    public static final Map<String, Double> getPropertyColorAsMapFromObject(
            String identifier, String type, Object object, String name,
            Map<String, Double> defValue) throws MegawidgetPropertyException {
        try {
            return getColorAsMapFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(name, e);
        }
    }

    /**
     * Get a color object from the specified object as a specified property
     * name. The object must either be <code>null</code> (only allowed if
     * <code>defValue</code> is not <code>null</code>), or be a map holding
     * values between <code>0.0</code> and <code>1.0</code> for
     * {@link #COLOR_AS_MAP_RED}, {@link #COLOR_AS_MAP_GREEN}, and
     * {@link #COLOR_AS_MAP_BLUE} entries.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the color value.
     * @param name
     *            Property name for which this object could be the value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Color object.
     * @throws MegawidgetPropertyException
     *             If the property value is invalid.
     */
    public static final Color getPropertyColorFromObject(String identifier,
            String type, Object object, String name, Color defValue)
            throws MegawidgetPropertyException {
        try {
            return getColorFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(name, e);
        }
    }

    /**
     * Get a dynamically typed object from the specified object as a value for
     * the specified property name. The object must be either <code>null</code>
     * (only allowed if <code>defValue</code> is not <code>null</code>), or an
     * object of dynamic type <code>T</code>.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
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
    public static final <T> T getPropertyDynamicallyTypedObjectFromObject(
            String identifier, String type, Object object, String name,
            Class<T> requiredClass, T defValue)
            throws MegawidgetPropertyException {
        try {
            return getDynamicallyTypedObjectFromObject(identifier, type,
                    object, requiredClass, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetPropertyException(name, e);
        }
    }

    /**
     * Get an integer from the specified object as a value for the specified
     * state identifier. The object must be either <code>null</code> (only
     * allowed if <code>defValue</code> is not <code>null</code>), or an object
     * of type {@link Number}. This method is used to ensure that any value
     * specified as a number, but within the bounds of a standard integer, is
     * properly handled.
     * 
     * @param identifier
     *            State identifier for which this object could be state.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the integer value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Integer value.
     * @throws MegawidgetStateException
     *             If the state value is invalid.
     */
    public static final int getStateIntegerValueFromObject(String identifier,
            String type, Object object, Integer defValue)
            throws MegawidgetStateException {
        try {
            return getIntegerValueFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
    }

    /**
     * Get an integer object from the specified object as a value for the
     * specified state identifier. The object must be either <code>null
     * </code> (only allowed if <code>defValue</code> is not <code>
     * null</code>), or an object of type {@link Number}. This method is used to
     * ensure that any value specified as a number, but within the bounds of a
     * standard integer, is properly handled. If the object is a {@link Integer}
     * , it is simply cast to this type and returned.
     * 
     * @param identifier
     *            State identifier for which this object could be state.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the integer value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Integer object.
     * @throws MegawidgetStateException
     *             If the state value is invalid.
     */
    public static final Integer getStateIntegerObjectFromObject(
            String identifier, String type, Object object, Integer defValue)
            throws MegawidgetStateException {
        try {
            return getIntegerObjectFromObject(identifier, type, object,
                    defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
    }

    /**
     * Get a long integer from the specified object as a value for the specified
     * state identifier. The object must be either <code>null</code> (only
     * allowed if <code>defValue</code> is not <code>null</code>), or an object
     * of type {@link Number}. This method is used to ensure that any value
     * specified as a number, but within the bounds of a standard long integer,
     * is properly handled.
     * 
     * @param identifier
     *            State identifier for which this object could be state.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the long integer value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Long integer value.
     * @throws MegawidgetStatexception
     *             If the state value is invalid.
     */
    public static final long getStateLongValueFromObject(String identifier,
            String type, Object object, Long defValue)
            throws MegawidgetStateException {
        try {
            return getLongValueFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
    }

    /**
     * Get a long integer object from the specified object as a value for the
     * specified state identifier. The object must be either <code>null</code>
     * (only allowed if <code>defValue</code> is not <code>null</code>), or an
     * object of type {@link Number}. This method is used to ensure that any
     * value specified as a number, but within the bounds of a standard long
     * integer, is properly handled. If the object is a {@link Long}, it is
     * simply cast to this type and returned.
     * 
     * @param identifier
     *            State identifier for which this object could be state.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the long integer value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Long integer object.
     * @throws MegawidgetStateException
     *             If the state value is invalid.
     */
    public static final Long getStateLongObjectFromObject(String identifier,
            String type, Object object, Long defValue)
            throws MegawidgetStateException {
        try {
            return getLongObjectFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
    }

    /**
     * Get a double value from the specified object as a value for the specified
     * state identifier. The object must be either <code>null</code> (only
     * allowed if <code>defValue</code> is not <code>null</code>), or an object
     * of type {@link Number}. This method is used to ensure that any value
     * specified as a number is properly handled.
     * 
     * @param identifier
     *            State identifier for which this object could be state.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the double value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Double value.
     * @throws MegawidgetStatexception
     *             If the state value is invalid.
     */
    public static final double getStateDoubleValueFromObject(String identifier,
            String type, Object object, Double defValue)
            throws MegawidgetStateException {
        try {
            return getDoubleValueFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
    }

    /**
     * Get a double object from the specified object as a value for the
     * specified state identifier. The object must be either <code>null</code>
     * (only allowed if <code>defValue</code> is not <code>null</code>), or an
     * object of type {@link Number}. This method is used to ensure that any
     * value specified as a number is properly handled. If the object is a
     * {@link Double}, it is simply cast to this type and returned.
     * 
     * @param identifier
     *            State identifier for which this object could be state.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the double value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Double object.
     * @throws MegawidgetStateException
     *             If the state value is invalid.
     */
    public static final Double getStateDoubleObjectFromObject(
            String identifier, String type, Object object, Double defValue)
            throws MegawidgetStateException {
        try {
            return getDoubleObjectFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
    }

    /**
     * Get a boolean from the specified object as a value for the specified
     * state identifier. The object must be either <code>null</code> (only
     * allowed if <code>defValue</code> is not <code>null</code>), or an object
     * of type {@link Boolean}, {@link Integer} or {@link Long}. This method is
     * used to ensure that any value specified as a boolean, or as a long or
     * integer of either 0 or 1, is properly handled.
     * 
     * @param identifier
     *            State identifier for which this object could be state.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the boolean value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Boolean value.
     * @throws MegawidgetStateException
     *             If the state value is invalid.
     */
    public static final boolean getStateBooleanValueFromObject(
            String identifier, String type, Object object, Boolean defValue)
            throws MegawidgetStateException {
        try {
            return getBooleanValueFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
    }

    /**
     * Get a boolean object from the specified object as a value for the
     * specified state identifier. The object must be either <code>null</code>
     * (only allowed if <code>defValue</code> is not <code>null</code>), or an
     * object of type {@link Boolean}, {@link Integer} or {@link Long}. This
     * method is used to ensure that any value specified as a boolean, or as a
     * long or integer of either 0 or 1, is properly handled. If the object is a
     * {@link Boolean}, it is simply cast to this type and returned.
     * 
     * @param identifier
     *            State identifier for which this object could be state.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the boolean value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Boolean object.
     * @throws MegawidgetStateException
     *             If the state value is invalid.
     */
    public static final Boolean getStateBooleanObjectFromObject(
            String identifier, String type, Object object, Boolean defValue)
            throws MegawidgetStateException {
        try {
            return getBooleanObjectFromObject(identifier, type, object,
                    defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
    }

    /**
     * Get a map representing a color object from the specified object as a
     * value for the specified state identifier. The object must either be
     * <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or be a map holding values between <code>0.0</code>
     * and <code>1.0</code> for {@link #COLOR_AS_MAP_RED},
     * {@link #COLOR_AS_MAP_GREEN}, and {@link #COLOR_AS_MAP_BLUE} entries.
     * <p>
     * Note that this method has been deprecated; megawidgets should use
     * {@link Color} objects instead, as returned by
     * {@link #getStateColorFromObject(String, String, Object, Color)}.
     * </p>
     * 
     * @param identifier
     *            State identifier for which this object could be state.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the color value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Map object representing a color.
     * @throws MegawidgetStateException
     *             If the state value is invalid.
     */
    @Deprecated
    public static final Map<String, Double> getStateColorAsMapFromObject(
            String identifier, String type, Object object,
            Map<String, Double> defValue) throws MegawidgetStateException {
        try {
            return getColorAsMapFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
    }

    /**
     * Get a color object from the specified object as a value for the specified
     * state identifier. The object must either be <code>null</code> (only
     * allowed if <code>defValue</code> is not <code>null</code>), or be a map
     * holding values between <code>0.0</code> and <code>1.0</code> for
     * {@link #COLOR_AS_MAP_RED}, {@link #COLOR_AS_MAP_GREEN}, and
     * {@link #COLOR_AS_MAP_BLUE} entries.
     * 
     * @param identifier
     *            State identifier for which this object could be state.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the color value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Color object.
     * @throws MegawidgetStateException
     *             If the state value is invalid.
     */
    public static final Color getStateColorFromObject(String identifier,
            String type, Object object, Color defValue)
            throws MegawidgetStateException {
        try {
            return getColorFromObject(identifier, type, object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
    }

    /**
     * Get a dynamically typed object from the specified object as a value for
     * the specified state identifier. The object must be either <code>null
     * </code> (only allowed if <code>defValue</code> is not <code>null</code>),
     * or an object of dynamic type <code>T</code>.
     * 
     * @param identifier
     *            State identifier for which this object could be state.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object to be cast or converted.
     * @param requiredClass
     *            Class to which this object must be cast or converted.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Object of the specified dynamic type.
     * @throws MegawidgetStateException
     *             If the state value is invalid.
     */
    public static final <T> T getStateDynamicallyTypedObjectFromObject(
            String identifier, String type, Object object,
            Class<T> requiredClass, T defValue) throws MegawidgetStateException {
        try {
            return getDynamicallyTypedObjectFromObject(identifier, type,
                    object, requiredClass, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
    }

    /**
     * Get an integer value from the specified object.
     * 
     * @param object
     *            Object from which to fetch the value.
     * @return Integer value.
     * @throws IllegalArgumentException
     *             If the object is not of the correct type.
     */
    public static final int getIntegerValueFromObject(Object object) {
        if ((object instanceof Number) == false) {
            throw new IllegalArgumentException("must be integer");
        }
        Number number = (Number) object;
        long value = number.longValue();
        if ((((number instanceof Double) || (number instanceof Float)) && ((value)
                - number.doubleValue() != 0.0))
                || (value < Integer.MIN_VALUE) || (value > Integer.MAX_VALUE)) {
            throw new IllegalArgumentException("must be integer");
        }
        return (int) value;
    }

    /**
     * Get a long integer value from the specified object.
     * 
     * @param object
     *            Object from which to fetch the value.
     * @return Long integer value.
     * @throws IllegalArgumentException
     *             If the object is not of the correct type.
     */
    public static final long getLongValueFromObject(Object object) {
        if ((object instanceof Number) == false) {
            throw new IllegalArgumentException("must be long integer");
        }
        Number number = (Number) object;
        long value = number.longValue();
        if (((number instanceof Double) || (number instanceof Float))
                && ((value) - number.doubleValue() != 0.0)) {
            throw new IllegalArgumentException("must be long integer");
        }
        return value;
    }

    /**
     * Get a float value from the specified object.
     * 
     * @param object
     *            Object from which to fetch the value.
     * @return Float value.
     * @throws IllegalArgumentException
     *             If the object is not of the correct type.
     */
    public static final float getFloatValueFromObject(Object object) {
        if ((object instanceof Number) == false) {
            throw new IllegalArgumentException("must be float");
        }
        Number number = (Number) object;
        double value = number.doubleValue();
        if ((value < -Float.MIN_VALUE) || (value > Float.MAX_VALUE)) {
            throw new IllegalArgumentException("must be float");
        }
        return (int) value;
    }

    /**
     * Get a double value from the specified object.
     * 
     * @param object
     *            Object from which to fetch the value.
     * @return Double value.
     * @throws IllegalArgumentException
     *             If the object is not of the correct type.
     */
    public static final double getDoubleValueFromObject(Object object) {
        if ((object instanceof Number) == false) {
            throw new IllegalArgumentException("must be double");
        }
        return ((Number) object).doubleValue();
    }

    // Private Static Methods

    /**
     * Get an integer from the specified object. The object must be either
     * <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or an object of type {@link Number}. This method is
     * used to ensure that any value specified as a number, but within the
     * bounds of a standard integer, is properly handled.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the integer value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Integer value.
     * @throws MegawidgetException
     *             If an integer value cannot be obtained from the object.
     */
    private static int getIntegerValueFromObject(String identifier,
            String type, Object object, Integer defValue)
            throws MegawidgetException {
        if (object == null) {
            if (defValue == null) {
                throw new MegawidgetException(identifier, type, null, null);
            } else {
                return defValue.intValue();
            }
        }
        try {
            return getIntegerValueFromObject(object);
        } catch (Exception e) {
            throw new MegawidgetException(identifier, type, object,
                    e.getMessage());
        }
    }

    /**
     * Get an integer object from the specified object. The object must be
     * either <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or an object of type {@link Number}. This method is
     * used to ensure that any value specified as a number, but within the
     * bounds of a standard integer, is properly handled. If the object is a
     * {@link Integer}, it is simply cast to this type and returned.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the integer value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Integer object.
     * @throws MegawidgetException
     *             If an integer object cannot be obtained from the object.
     */
    private static Integer getIntegerObjectFromObject(String identifier,
            String type, Object object, Integer defValue)
            throws MegawidgetException {
        if (object instanceof Integer) {
            return (Integer) object;
        } else {
            return getIntegerValueFromObject(identifier, type, object, defValue);
        }
    }

    /**
     * Get a long integer from the specified object. The object must be either
     * <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or an object of type {@link Number}. This method is
     * used to ensure that any value specified as a number, but within the
     * bounds of a standard long integer, is properly handled.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the long integer value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Long integer value.
     * @throws MegawidgetException
     *             If a long integer value cannot be obtained from the object.
     */
    private static long getLongValueFromObject(String identifier, String type,
            Object object, Long defValue) throws MegawidgetException {
        if (object == null) {
            if (defValue == null) {
                throw new MegawidgetException(identifier, type, null, null);
            } else {
                return defValue.longValue();
            }
        }
        try {
            return getLongValueFromObject(object);
        } catch (Exception e) {
            throw new MegawidgetException(identifier, type, object,
                    e.getMessage());
        }
    }

    /**
     * Get a long integer object from the specified object. The object must be
     * either <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or an object of type {@link Number}. This method is
     * used to ensure that any value specified as a number, but within the
     * bounds of a standard long integer, is properly handled. If the object is
     * a {@link Long}, it is simply cast to this type and returned.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the long integer value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Long integer object.
     * @throws MegawidgetException
     *             If a long integer object cannot be obtained from the object.
     */
    private static Long getLongObjectFromObject(String identifier, String type,
            Object object, Long defValue) throws MegawidgetException {
        if (object instanceof Long) {
            return (Long) object;
        } else {
            return getLongValueFromObject(identifier, type, object, defValue);
        }
    }

    /**
     * Get a float from the specified object. The object must be either <code>
     * null</code> (only allowed if <code>defValue</code> is not <code>
     * null</code>), or an object of type {@link Number}. This method is used to
     * ensure that any value specified as a number, but within the bounds of a
     * standard float, is properly handled.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the float value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Float value.
     * @throws MegawidgetException
     *             If a float value cannot be obtained from the object.
     */
    private static float getFloatValueFromObject(String identifier,
            String type, Object object, Float defValue)
            throws MegawidgetException {
        if (object == null) {
            if (defValue == null) {
                throw new MegawidgetException(identifier, type, null, null);
            } else {
                return defValue.floatValue();
            }
        }
        try {
            return getFloatValueFromObject(object);
        } catch (Exception e) {
            throw new MegawidgetException(identifier, type, object,
                    e.getMessage());
        }
    }

    /**
     * Get a float object from the specified object. The object must be either
     * <code>null</code> (only allowed if <code>defValue</code> is not <code>
     * null</code>), or an object of type {@link Number}. This method is used to
     * ensure that any value specified as a number, but within the bounds of a
     * standard float, is properly handled. If the object is a {@link Float}, it
     * is simply cast to this type and returned.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the float value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Float object.
     * @throws MegawidgetException
     *             If a float object cannot be obtained from the object.
     */
    private static Float getFloatObjectFromObject(String identifier,
            String type, Object object, Float defValue)
            throws MegawidgetException {
        if (object instanceof Float) {
            return (Float) object;
        } else {
            return getFloatValueFromObject(identifier, type, object, defValue);
        }
    }

    /**
     * Get a double from the specified object. The object must be either <code>
     * null</code> (only allowed if <code>defValue</code> is not <code>
     * null</code>), or an object of type {@link Number}. This method is used to
     * ensure that any value specified as a number is properly handled.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the double value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Double value.
     * @throws MegawidgetException
     *             If a double value cannot be obtained from the object.
     */
    private static double getDoubleValueFromObject(String identifier,
            String type, Object object, Double defValue)
            throws MegawidgetException {
        if (object == null) {
            if (defValue == null) {
                throw new MegawidgetException(identifier, type, null, null);
            } else {
                return defValue.doubleValue();
            }
        }
        try {
            return getDoubleValueFromObject(object);
        } catch (Exception e) {
            throw new MegawidgetException(identifier, type, object,
                    e.getMessage());
        }
    }

    /**
     * Get a double object from the specified object. The object must be either
     * <code>null</code> (only allowed if <code>defValue</code> is not <code>
     * null</code>), or an object of type {@link Number}. This method is used to
     * ensure that any value specified as a number is properly handled. If the
     * object is a {@link Double}, it is simply cast to this type and returned.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the double value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Double object.
     * @throws MegawidgetException
     *             If a double object cannot be obtained from the object.
     */
    private static Double getDoubleObjectFromObject(String identifier,
            String type, Object object, Double defValue)
            throws MegawidgetException {
        if (object instanceof Double) {
            return (Double) object;
        } else {
            return getDoubleValueFromObject(identifier, type, object, defValue);
        }
    }

    /**
     * Get a boolean from the specified object. The object must be either
     * <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or an object of type {@link Boolean}, {@link Integer}
     * or {@link Long}. This method is used to ensure that any value specified
     * as a long or integer of either 0 or 1 is properly handled.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the boolean value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Boolean value.
     * @throws MegawidgeException
     *             If a boolean value cannot be obtained from the object.
     */
    private static boolean getBooleanValueFromObject(String identifier,
            String type, Object object, Boolean defValue)
            throws MegawidgetException {
        if (object == null) {
            if (defValue == null) {
                throw new MegawidgetException(identifier, type, null, null);
            } else {
                return defValue.booleanValue();
            }
        } else if (object instanceof Number) {
            Number number = (Number) object;
            long value = number.longValue();
            if ((((number instanceof Double) || (number instanceof Float)) && ((value)
                    - number.doubleValue() != 0.0))
                    || ((value != 0L) && (value != 1L))) {
                throw new MegawidgetException(identifier, type, number,
                        "must be boolean");
            } else {
                return (value == 1L ? true : false);
            }
        } else if (object instanceof Boolean) {
            return ((Boolean) object).booleanValue();
        } else {
            throw new MegawidgetException(identifier, type, object,
                    "must be boolean");
        }
    }

    /**
     * Get a boolean object from the specified object. The object must be either
     * <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or an object of type {@link Boolean}, {@link Integer}
     * or {@link Long}. This method is used to ensure that any value specified
     * as a long or integer of either 0 or 1 is properly handled. If the object
     * is a {@link Boolean}, it is simply cast to this type and returned.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the boolean value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Boolean object.
     * @throws MegawidgetException
     *             If a boolean object cannot be obtained from the object.
     */
    private static Boolean getBooleanObjectFromObject(String identifier,
            String type, Object object, Boolean defValue)
            throws MegawidgetException {
        if (object instanceof Boolean) {
            return (Boolean) object;
        } else {
            return getBooleanValueFromObject(identifier, type, object, defValue);
        }
    }

    /**
     * Get a map representing a color object from the specified object. The
     * object must either be <code>null</code> (only allowed if
     * <code>defValue</code> is not <code>null</code>), or be a map holding
     * values between <code>0.0</code> and <code>1.0</code> for
     * {@link #COLOR_AS_MAP_RED}, {@link #COLOR_AS_MAP_GREEN}, and
     * {@link #COLOR_AS_MAP_BLUE} entries.
     * <p>
     * Note that this method has been deprecated; once megawidgets move over to
     * using the <code>getXXXXColorFromObject()</code> family of methods, this
     * can be removed along with its callers.
     * </p>
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the color value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Map object representing the color.
     * @throws MegawidgetException
     *             If a map object representing a color cannot be obtained from
     *             the object.
     */
    @Deprecated
    private static Map<String, Double> getColorAsMapFromObject(
            String identifier, String type, Object object,
            Map<String, Double> defValue) throws MegawidgetException {
        if (object == null) {
            if (defValue == null) {
                throw new MegawidgetException(identifier, type, null, null);
            } else {
                return new HashMap<>(defValue);
            }
        } else if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            Double red = null, green = null, blue = null;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                if (COLOR_AS_MAP_RED.equals(key)
                        || COLOR_AS_MAP_GREEN.equals(key)
                        || COLOR_AS_MAP_BLUE.equals(key)) {
                    Object value = entry.getValue();
                    if (value instanceof Number == false) {
                        break;
                    }
                    double doubleValue = ((Number) value).doubleValue();
                    if ((doubleValue < 0.0) || (doubleValue > 1.0)) {
                        break;
                    }
                    if (COLOR_AS_MAP_RED.equals(key)) {
                        red = doubleValue;
                    } else if (COLOR_AS_MAP_GREEN.equals(key)) {
                        green = doubleValue;
                    } else {
                        blue = doubleValue;
                    }
                }
            }
            if ((red != null) && (green != null) && (blue != null)) {
                Map<String, Double> result = new HashMap<>(3, 1.0f);
                result.put(COLOR_AS_MAP_RED, red);
                result.put(COLOR_AS_MAP_GREEN, green);
                result.put(COLOR_AS_MAP_BLUE, blue);
                return result;
            }
        }
        throw new MegawidgetException(identifier, type, object,
                "must be map holding values ranging from 0.0"
                        + " to 1.0 for red, green, and blue entries");
    }

    /**
     * Get a color object from the specified object. The latter must either be
     * <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or be a map holding values between <code>0.0</code>
     * and <code>1.0</code> for {@link #COLOR_AS_MAP_RED},
     * {@link #COLOR_AS_MAP_GREEN}, and {@link #COLOR_AS_MAP_BLUE} entries.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object holding the color value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Color object.
     * @throws MegawidgetException
     *             If a map object representing a color cannot be obtained from
     *             the object.
     */
    private static Color getColorFromObject(String identifier, String type,
            Object object, Color defValue) throws MegawidgetException {
        if (object == null) {
            if (defValue == null) {
                throw new MegawidgetException(identifier, type, null, null);
            } else {
                return new Color(defValue.getRed(), defValue.getGreen(),
                        defValue.getBlue(), defValue.getAlpha());
            }
        } else if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            Float red = null, green = null, blue = null;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Number == false) {
                    break;
                }
                float floatValue = ((Number) value).floatValue();
                if ((floatValue < 0.0) || (floatValue > 1.0)) {
                    break;
                }
                if (COLOR_AS_MAP_RED.equals(key)) {
                    red = floatValue;
                } else if (COLOR_AS_MAP_GREEN.equals(key)) {
                    green = floatValue;
                } else if (COLOR_AS_MAP_BLUE.equals(key)) {
                    blue = floatValue;
                }
            }
            if ((red != null) && (green != null) && (blue != null)) {
                return new Color(red, green, blue);
            }
        }
        throw new MegawidgetException(identifier, type, object,
                "must be map holding values ranging from 0.0"
                        + " to 1.0 for red, green, and blue entries");
    }

    /**
     * Get a dynamically typed object from the specified object. The object must
     * be either <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or an object of dynamic type <code>T</code>.
     * 
     * @param identifier
     *            Identifier of the megawidget.
     * @param type
     *            Type of the megawidget.
     * @param object
     *            Object to be cast or converted.
     * @param requiredClass
     *            Class to which this object must be cast or converted.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Object of the specified dynamic type.
     * @throws MegawidgetException
     *             If an object of the dynamic type cannot be obtained from the
     *             original object.
     */
    @SuppressWarnings("unchecked")
    private static <T> T getDynamicallyTypedObjectFromObject(String identifier,
            String type, Object object, Class<T> requiredClass, T defValue)
            throws MegawidgetException {

        /*
         * If no object was supplied, return the default value, or throw an
         * exception if no default was supplied either.
         */
        if (object == null) {
            if (defValue == null) {
                throw new MegawidgetException(identifier, type, null, null);
            } else {
                return defValue;
            }
        }

        /*
         * If the value cannot be cast, throw an exception. Otherwise, if the
         * required dynamic type is extended or implemented by the class of the
         * object, the object itself may be returned. If neither of these are
         * the case, do some conversions for specific types of objects if one of
         * these is desired, or throw an exception if no conversion can be done.
         */
        try {
            T value = (T) object;
            if (requiredClass.isAssignableFrom(value.getClass())) {
                return value;
            } else if (requiredClass.equals(Integer.class)) {
                return (T) getIntegerObjectFromObject(identifier, type, value,
                        null);
            } else if (requiredClass.equals(Long.class)) {
                return (T) getLongObjectFromObject(identifier, type, value,
                        null);
            } else if (requiredClass.equals(Float.class)) {
                return (T) getFloatObjectFromObject(identifier, type, value,
                        null);
            } else if (requiredClass.equals(Double.class)) {
                return (T) getDoubleObjectFromObject(identifier, type, value,
                        null);
            } else if (requiredClass.equals(Boolean.class)) {
                return (T) getBooleanObjectFromObject(identifier, type, value,
                        null);
            } else {
                throw new ClassCastException(value.getClass()
                        + " cannot be cast to " + requiredClass);
            }
        } catch (Exception e) {
            throw new MegawidgetException(identifier, type, object, "must be "
                    + requiredClass.getSimpleName(), new ClassCastException(
                    object.getClass() + " cannot be cast to " + requiredClass));
        }
    }
}
