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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Megawidget specifier abstract base class.
 * <p>
 * All concrete subclasses must have a constructor taking arguments identical to
 * those taken by the constructor of this class. Furthermore, all concrete
 * subclasses of <code>Megawidget</code> must have the same name as their
 * corresponding specifier classes, except with "Specifier" replaced with
 * "Megawidget", and each such subclass must exist in the same package as its
 * specifier. Thus, for example, if there is a specifier with the path and name
 * <code>org.foo.megawidgets.ExampleSpecifier</code>, the latter will assume
 * that the megawidget it is to construct is an instance of the class <code>
 * org.foo.megawidgets.ExampleMegawidget</code>.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 21, 2013   2168     Chris.Golden      Extracted control-specific options
 *                                           and placed them in a new control
 *                                           specifier options manager class, as
 *                                           the former didn't belong here (since
 *                                           not all megawidgets are controls;
 *                                           some are menus). Also changed to
 *                                           implement new ISpecifier interface.
 *                                           Also added helper methods for
 *                                           getting floats and doubles from
 *                                           arbitrary specifier value objects.
 * Nov 04, 2013   2336     Chris.Golden      Placed number conversion code in
 *                                           separate static methods to make
 *                                           the conversion algorithms readily
 *                                           available to subclasses.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see Megawidget
 */
public abstract class MegawidgetSpecifier implements ISpecifier {

    // Private Static Variables

    /**
     * Hash table mapping parent composites to lists of widgets that must be
     * notified when the parents change size.
     */
    private static Map<Composite, List<Control>> resizeAwareControlListsForParents = Maps
            .newHashMap();

    /**
     * Hash table mapping parent composites to their control listeners, if any.
     * These are used to listen for resize events and pass them onto the widgets
     * in the lists associated with the same parents in <code>
     * resizeAwareControlListsForParents</code>.
     */
    private static Map<Composite, ControlListener> controlListenersForParents = Maps
            .newHashMap();

    /**
     * Hash table mapping parent composites to their dispose listeners, if any.
     * These are used to listen for dispose events and use them to remove the
     * control listeners and lists of widgets associated with the parents when
     * the latter are disposed of.
     */
    private static Map<Composite, DisposeListener> disposeListenersForParents = Maps
            .newHashMap();

    // Private Variables

    /**
     * Identifier.
     */
    private final String identifier;

    /**
     * Flag indicating whether the megawidget should be created in an enabled or
     * a disabled state.
     */
    private final boolean enabled;

    /**
     * Label.
     */
    private final String label;

    // Protected Static Methods

    /**
     * Get an integer value from the specified object.
     * 
     * @param object
     *            Object from which to fetch the value.
     * @return Integer value.
     * @throws IllegalArgumentException
     *             If the object is not of the correct type.
     */
    protected static final int getIntegerValueFromObject(Object object) {
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
    protected static final long getLongValueFromObject(Object object) {
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
    protected static final float getFloatValueFromObject(Object object) {
        if ((object instanceof Number) == false) {
            throw new IllegalArgumentException("must be float");
        }
        Number number = (Number) object;
        double value = number.doubleValue();
        if ((value < Float.MIN_VALUE) || (value > Float.MAX_VALUE)) {
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
    protected static final double getDoubleValueFromObject(Object object) {
        if ((object instanceof Number) == false) {
            throw new IllegalArgumentException("must be float");
        }
        return ((Number) object).doubleValue();
    }

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map containing the parameters to be used to construct the
     *            megawidget specifier.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public MegawidgetSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {

        // Ensure that the identifier is present and accep-
        // table.
        try {
            identifier = (String) parameters.get(MEGAWIDGET_IDENTIFIER);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(null, getType(),
                    MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER,
                    parameters.get(MEGAWIDGET_IDENTIFIER), "must be string");
        }
        if ((identifier == null) || identifier.isEmpty()) {
            throw new MegawidgetSpecificationException(null, getType(),
                    MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER, null, null);
        }

        // Ensure that the enabled flag, if present, is
        // acceptable.
        enabled = getSpecifierBooleanValueFromObject(
                parameters.get(MEGAWIDGET_ENABLED), MEGAWIDGET_ENABLED, true);

        // Ensure that the label, if present, is acceptable.
        try {
            label = (String) parameters.get(MEGAWIDGET_LABEL);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(identifier, getType(),
                    MEGAWIDGET_LABEL, parameters.get(MEGAWIDGET_LABEL),
                    "must be string");
        }
    }

    // Public Methods

    @Override
    public final String getIdentifier() {
        return identifier;
    }

    @Override
    public final String getType() {
        String className = getClass().getSimpleName();
        int endIndex = className.lastIndexOf("Specifier");
        if (endIndex > -1) {
            className = className.substring(0, endIndex);
        }
        return className;
    }

    @Override
    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public final String getLabel() {
        return label;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends Widget, M extends IMegawidget> M createMegawidget(
            P parent, Class<M> superClass, Map<String, Object> creationParams)
            throws MegawidgetException {

        // Determine the full path and name of the megawidget class
        // of which an instance is to be created.
        String specifierClassName = getClass().getSimpleName();
        int index = specifierClassName.lastIndexOf("Specifier");
        if (index == -1) {
            throw new MegawidgetSpecificationException(identifier, getType(),
                    null, null, getClass().getName() + " class name "
                            + "does not end in \"Specifier\"; cannot "
                            + "find corresponding megawidget subclass");
        }
        String classPathAndName = getClass().getPackage().getName() + "."
                + specifierClassName.substring(0, index) + "Megawidget";

        // Get the class.
        Class<?> megawidgetClass = null;
        try {
            megawidgetClass = Class.forName(classPathAndName);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(identifier, getType(),
                    null, null, "not a valid megawidget (cannot find class "
                            + classPathAndName + ")");
        }

        // If the class is not a subclass of the provided
        // superclass, complain.
        if (superClass.isAssignableFrom(megawidgetClass) == false) {
            throw new MegawidgetSpecificationException(identifier, getType(),
                    null, null, "not a valid megawidget (" + megawidgetClass
                            + " is not a subclass of " + superClass + ")");
        }

        // Iterate through all declared constructors for the
        // megawidget subclass of which an instance is to be
        // created, looking for the one that best fits the para-
        // meters available, namely, this specifier's class,
        // the parent widget's class, and the Map class,
        // respectively. the "best" fit is the one that can
        // take these parameters and that takes as its parent
        // parameter an object of a class that is as close as
        // possible (in terms of generational difference) to
        // the class of the supplied parent object.
        Class<?>[] neededArgTypes = { getClass(), parent.getClass(), Map.class };
        Constructor<?> bestConstructor = null;
        int bestMegawidgetClassGenerationalDifference = -1;
        for (Constructor<?> constructor : megawidgetClass
                .getDeclaredConstructors()) {

            // Do nothing more with this constructor if it
            // cannot take these parameter types.
            Class<?>[] argTypes = constructor.getParameterTypes();
            if ((argTypes.length != neededArgTypes.length)
                    || (argTypes[0].equals(neededArgTypes[0]) == false)
                    || (Widget.class.isAssignableFrom(neededArgTypes[1]) == false)
                    || (argTypes[2].equals(neededArgTypes[2]) == false)) {
                continue;
            }

            // See how many generations apart the parent
            // object's class is from the parameter type
            // for the constructor.
            int megawidgetClassGenerationalDifference = 0;
            for (Class<?> otherMegawidgetClass = neededArgTypes[1]; otherMegawidgetClass != argTypes[1]; otherMegawidgetClass = otherMegawidgetClass
                    .getSuperclass()) {
                megawidgetClassGenerationalDifference++;
            }

            // If this is the only fitting constructor so
            // far, or if it is the fewest generations apart
            // from the parent object's class so far, mark
            // it as the best.
            if ((bestConstructor == null)
                    || (megawidgetClassGenerationalDifference < bestMegawidgetClassGenerationalDifference)) {
                bestConstructor = constructor;
                bestMegawidgetClassGenerationalDifference = megawidgetClassGenerationalDifference;
            }

            // If this constructor's parameter is of the
            // same type as the parent object's class, use
            // this constructor.
            if (megawidgetClassGenerationalDifference == 0) {
                break;
            }
        }
        if (bestConstructor == null) {
            throw new MegawidgetSpecificationException(identifier, getType(),
                    null, null, "not a valid megawidget (class "
                            + megawidgetClass
                            + " does not have a constructor taking ("
                            + getClassArrayAsString(neededArgTypes)
                            + ") as arguments)");
        }

        // Construct an instance of the class using the passed-
        // in parameters with the constructor found above.
        Object[] constructorArgValues = { this, parent, creationParams };
        M megawidget = null;
        try {
            megawidget = (M) bestConstructor.newInstance(constructorArgValues);
        } catch (Throwable e) {
            if (e instanceof IllegalAccessException) {
                throw new MegawidgetSpecificationException(
                        identifier,
                        getType(),
                        null,
                        null,
                        "not a valid megawidget (class "
                                + megawidgetClass
                                + " is abstract and thus cannot be instantiated)",
                        e);
            } else if (e instanceof IllegalArgumentException) {
                throw new MegawidgetSpecificationException(identifier,
                        getType(), null, null,
                        "unexpected illegal argument error", e);
            } else if (e instanceof InstantiationException) {
                throw new MegawidgetSpecificationException(identifier,
                        getType(), null, null, "not a valid megawidget (class "
                                + megawidgetClass + " constructor taking "
                                + getClassArrayAsString(neededArgTypes)
                                + " as an argument is inaccessible)", e);
            } else if (e instanceof InvocationTargetException) {
                Throwable cause = e.getCause();
                if (cause instanceof MegawidgetException) {
                    throw (MegawidgetException) cause;
                } else {
                    throw new MegawidgetSpecificationException(identifier,
                            getType(), null, null,
                            "unexpected constructor error", cause);
                }
            } else if (e instanceof ExceptionInInitializerError) {
                throw new MegawidgetSpecificationException(identifier,
                        getType(), null, null,
                        "unexpected static initializer error", e);
            }
        }

        // Return the result.
        return megawidget;
    }

    // Protected Methods

    /**
     * Add a listener to the specified parent that notifies the specified child
     * when the parent's size has been changed.
     * 
     * @param parent
     *            Parent widget.
     * @param child
     *            Child widget.
     */
    protected final void ensureChildIsResizedWithParent(Composite parent,
            Control child) {
        synchronized (resizeAwareControlListsForParents) {

            // Find the list of controls for this parent; if
            // none has been created by now, set one up.
            List<Control> list = resizeAwareControlListsForParents.get(parent);
            if (list == null) {

                // Create the list to hold controls that
                // wish to be notified of parent resizes.
                list = Lists.newArrayList();
                resizeAwareControlListsForParents.put(parent, list);

                // Create a control listener for the parent
                // that resizes any children in its list
                // when the parent itself is resized, and
                // associate the listener with the parent.
                ControlListener controlListener = new ControlAdapter() {
                    @Override
                    public void controlResized(ControlEvent e) {

                        // Ensure that there are widgets to be
                        // notified of the resize.
                        List<Control> list = resizeAwareControlListsForParents
                                .get(e.widget);
                        if (list != null) {

                            // Change each child's width hint
                            // to match that of the client
                            // area of the parent.
                            Composite widget = (Composite) e.widget;
                            for (Control child : list) {
                                GridLayout layout = (GridLayout) widget
                                        .getLayout();
                                ((GridData) child.getLayoutData()).widthHint = widget
                                        .getClientArea().width
                                        - ((layout.marginWidth * 2)
                                                + layout.marginLeft + layout.marginRight);
                            }

                            // Tell the parent to lay itself
                            // out again.
                            ((Composite) e.widget).layout(list
                                    .toArray(new Control[list.size()]));
                        }
                    }
                };
                parent.addControlListener(controlListener);
                controlListenersForParents.put(parent, controlListener);

                // Create a disposal listener for the parent
                // that removes all associations of the par-
                // ent related to notifying children of re-
                // sizes, since after disposal they are no
                // longer needed.
                DisposeListener disposeListener = new DisposeListener() {
                    @Override
                    public void widgetDisposed(DisposeEvent e) {
                        resizeAwareControlListsForParents.remove(e.widget);
                        controlListenersForParents.remove(e.widget);
                        disposeListenersForParents.remove(e.widget);
                    }
                };
                parent.addDisposeListener(disposeListener);
                disposeListenersForParents.put(parent, disposeListener);
            }
            list.add(child);
        }
    }

    /**
     * Get an integer from the specified object as a specifier parameter. The
     * object must be either <code>null</code> (only allowed if <code>defValue
     * </code> is not <code>null</code>), or an object of type <code>
     * Number</code>. This method is used to ensure that any value specified as
     * a number, but within the bounds of a standard integer, is properly
     * handled.
     * 
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
    protected final int getSpecifierIntegerValueFromObject(Object object,
            String paramName, Integer defValue)
            throws MegawidgetSpecificationException {
        try {
            return getIntegerValueFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(e.getIdentifier(),
                    e.getType(), paramName, e.getBadValue(), e.getMessage(),
                    e.getCause());
        }
    }

    /**
     * Get an integer from the specified object as a specifier parameter. The
     * object must be either <code>null</code> (only allowed if <code>defValue
     * </code> is not <code>null</code>), or an object of type <code>
     * Number</code>. This method is used to ensure that any value specified as
     * a number, but within the bounds of a standard integer, is properly
     * handled. If the object is a <code>Integer</code>, it is simply cast to
     * this type and returned.
     * 
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
    protected final Integer getSpecifierIntegerObjectFromObject(Object object,
            String paramName, Integer defValue)
            throws MegawidgetSpecificationException {
        try {
            return getIntegerObjectFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(e.getIdentifier(),
                    e.getType(), paramName, e.getBadValue(), e.getMessage(),
                    e.getCause());
        }
    }

    /**
     * Get a long integer from the specified object as a specifier parameter.
     * The object must be either <code>null</code> (only allowed if <code>
     * defValue</code> is not <code>null</code>), or an object of type <code>
     * Number</code>. This method is used to ensure that any value specified as
     * a number, but within the bounds of a standard long integer, is properly
     * handled.
     * 
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
    protected final long getSpecifierLongValueFromObject(Object object,
            String paramName, Long defValue)
            throws MegawidgetSpecificationException {
        try {
            return getLongValueFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(e.getIdentifier(),
                    e.getType(), paramName, e.getBadValue(), e.getMessage(),
                    e.getCause());
        }
    }

    /**
     * Get a long integer from the specified object as a specifier parameter.
     * The object must be either <code>null</code> (only allowed if <code>
     * defValue</code> is not <code>null</code>), or an object of type <code>
     * Number</code>. This method is used to ensure that any value specified as
     * a number, but within the bounds of a standard long integer, is properly
     * handled. If the object is a <code>Long</code>, it is simply cast to this
     * type and returned.
     * 
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
    protected final Long getSpecifierLongObjectFromObject(Object object,
            String paramName, Long defValue)
            throws MegawidgetSpecificationException {
        try {
            return getLongObjectFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(e.getIdentifier(),
                    e.getType(), paramName, e.getBadValue(), e.getMessage(),
                    e.getCause());
        }
    }

    /**
     * Get a float from the specified object as a specifier parameter. The
     * object must be either <code>null</code> (only allowed if <code>defValue
     * </code> is not <code>null</code>), or an object of type <code>
     * Number</code>. This method is used to ensure that any value specified as
     * a number, but within the bounds of a standard float, is properly handled.
     * 
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
    protected final float getSpecifierFloatValueFromObject(Object object,
            String paramName, Float defValue)
            throws MegawidgetSpecificationException {
        try {
            return getFloatValueFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(e.getIdentifier(),
                    e.getType(), paramName, e.getBadValue(), e.getMessage(),
                    e.getCause());
        }
    }

    /**
     * Get a float from the specified object as a specifier parameter. The
     * object must be either <code>null</code> (only allowed if <code>defValue
     * </code> is not <code>null</code>), or an object of type <code>
     * Number</code>. This method is used to ensure that any value specified as
     * a number, but within the bounds of a standard float, is properly handled.
     * If the object is a <code>Float</code>, it is simply cast to this type and
     * returned.
     * 
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
    protected final Float getSpecifierFloatObjectFromObject(Object object,
            String paramName, Float defValue)
            throws MegawidgetSpecificationException {
        try {
            return getFloatObjectFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(e.getIdentifier(),
                    e.getType(), paramName, e.getBadValue(), e.getMessage(),
                    e.getCause());
        }
    }

    /**
     * Get a double from the specified object as a specifier parameter. The
     * object must be either <code>null</code> (only allowed if <code>defValue
     * </code> is not <code>null</code>), or an object of type <code>
     * Number</code>. This method is used to ensure that any value specified as
     * a number is properly handled.
     * 
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
    protected final double getSpecifierDoubleValueFromObject(Object object,
            String paramName, Double defValue)
            throws MegawidgetSpecificationException {
        try {
            return getDoubleValueFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(e.getIdentifier(),
                    e.getType(), paramName, e.getBadValue(), e.getMessage(),
                    e.getCause());
        }
    }

    /**
     * Get a double from the specified object as a specifier parameter. The
     * object must be either <code>null</code> (only allowed if <code>
     * defValue</code> is not <code>null</code>), or an object of type <code>
     * Number</code>. This method is used to ensure that any value specified as
     * a number is properly handled. If the object is a <code>Double</code>, it
     * is simply cast to this type and returned.
     * 
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
    protected final Double getSpecifierDoubleObjectFromObject(Object object,
            String paramName, Double defValue)
            throws MegawidgetSpecificationException {
        try {
            return getDoubleObjectFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(e.getIdentifier(),
                    e.getType(), paramName, e.getBadValue(), e.getMessage(),
                    e.getCause());
        }
    }

    /**
     * Get a boolean from the specified object as a specifier parameter. The
     * object must be either <code>null</code> (only allowed if <code>defValue
     * </code> is not <code>null</code>), or an object of type <code
     * >Boolean</code>, <code>Integer</code> or <code>Long</code>. This method
     * is used to ensure that any value specified as a boolean, or as a long or
     * integer of either 0 or 1, is properly handled.
     * 
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
    protected final boolean getSpecifierBooleanValueFromObject(Object object,
            String paramName, Boolean defValue)
            throws MegawidgetSpecificationException {
        try {
            return getBooleanValueFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(e.getIdentifier(),
                    e.getType(), paramName, e.getBadValue(), e.getMessage(),
                    e.getCause());
        }
    }

    /**
     * Get a boolean object from the specified object as a specifier parameter.
     * The object must be either <code>null</code> (only allowed if <code>
     * defValue</code> is not <code>null</code>), or an object of type <code>
     * Boolean</code>, <code>Integer</code> or <code>Long</code>. This method is
     * used to ensure that any value specified as a boolean, or as a long or
     * integer of either 0 or 1, is properly handled. If the object is a <code>
     * Boolean</code>, it is simply cast to this type and returned.
     * 
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
    protected final Boolean getSpecifierBooleanObjectFromObject(Object object,
            String paramName, Boolean defValue)
            throws MegawidgetSpecificationException {
        try {
            return getBooleanObjectFromObject(object, defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(e.getIdentifier(),
                    e.getType(), paramName, e.getBadValue(), e.getMessage(),
                    e.getCause());
        }
    }

    /**
     * Get a dynamically typed object from the specified object as a specifier
     * parameter. The object must be either <code>null</code> (only allowed if
     * <code>defValue</code> is not <code>null</code>), or an object of dynamic
     * type <code>T</code>.
     * 
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
    protected final <T> T getSpecifierDynamicallyTypedObjectFromObject(
            Object object, String paramName, Class<T> requiredClass, T defValue)
            throws MegawidgetSpecificationException {
        try {
            return getDynamicallyTypedObjectFromObject(object, requiredClass,
                    defValue);
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(e.getIdentifier(),
                    e.getType(), paramName, e.getBadValue(), e.getMessage(),
                    e.getCause());
        }
    }

    // Package Methods

    /**
     * Get an integer from the specified object. The object must be either
     * <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or an object of type <code>Number</code>. This method
     * is used to ensure that any value specified as a number, but within the
     * bounds of a standard integer, is properly handled.
     * 
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
    int getIntegerValueFromObject(Object object, Integer defValue)
            throws MegawidgetException {
        if (object == null) {
            if (defValue == null) {
                throw new MegawidgetException(identifier, getType(), null, null);
            } else {
                return defValue.intValue();
            }
        }
        try {
            return getIntegerValueFromObject(object);
        } catch (Exception e) {
            throw new MegawidgetException(identifier, getType(), object,
                    e.getMessage());
        }
    }

    /**
     * Get an integer object from the specified object. The object must be
     * either <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or an object of type <code>Number</code>. This method
     * is used to ensure that any value specified as a number, but within the
     * bounds of a standard integer, is properly handled. If the object is a
     * <code>Integer</code>, it is simply cast to this type and returned.
     * 
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
    Integer getIntegerObjectFromObject(Object object, Integer defValue)
            throws MegawidgetException {
        if (object instanceof Integer) {
            return (Integer) object;
        } else {
            return getIntegerValueFromObject(object, defValue);
        }
    }

    /**
     * Get a long integer from the specified object. The object must be either
     * <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or an object of type <code>Number</code>. This method
     * is used to ensure that any value specified as a number, but within the
     * bounds of a standard long integer, is properly handled.
     * 
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
    long getLongValueFromObject(Object object, Long defValue)
            throws MegawidgetException {
        if (object == null) {
            if (defValue == null) {
                throw new MegawidgetException(identifier, getType(), null, null);
            } else {
                return defValue.longValue();
            }
        }
        try {
            return getLongValueFromObject(object);
        } catch (Exception e) {
            throw new MegawidgetException(identifier, getType(), object,
                    e.getMessage());
        }
    }

    /**
     * Get a long integer object from the specified object. The object must be
     * either <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or an object of type <code>Number</code>. This method
     * is used to ensure that any value specified as a number, but within the
     * bounds of a standard long integer, is properly handled. If the object is
     * a <code>Long</code>, it is simply cast to this type and returned.
     * 
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
    Long getLongObjectFromObject(Object object, Long defValue)
            throws MegawidgetException {
        if (object instanceof Long) {
            return (Long) object;
        } else {
            return getLongValueFromObject(object, defValue);
        }
    }

    /**
     * Get a float from the specified object. The object must be either <code>
     * null</code> (only allowed if <code>defValue</code> is not <code>
     * null</code>), or an object of type <code>Number</code>. This method is
     * used to ensure that any value specified as a number, but within the
     * bounds of a standard float, is properly handled.
     * 
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
    float getFloatValueFromObject(Object object, Float defValue)
            throws MegawidgetException {
        if (object == null) {
            if (defValue == null) {
                throw new MegawidgetException(identifier, getType(), null, null);
            } else {
                return defValue.floatValue();
            }
        }
        try {
            return getFloatValueFromObject(object);
        } catch (Exception e) {
            throw new MegawidgetException(identifier, getType(), object,
                    e.getMessage());
        }
    }

    /**
     * Get a float object from the specified object. The object must be either
     * <code>null</code> (only allowed if <code>defValue</code> is not <code>
     * null</code>), or an object of type <code>Number</code>. This method is
     * used to ensure that any value specified as a number, but within the
     * bounds of a standard float, is properly handled. If the object is a
     * <code>Float</code>, it is simply cast to this type and returned.
     * 
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
    Float getFloatObjectFromObject(Object object, Float defValue)
            throws MegawidgetException {
        if (object instanceof Float) {
            return (Float) object;
        } else {
            return getFloatValueFromObject(object, defValue);
        }
    }

    /**
     * Get a double from the specified object. The object must be either <code>
     * null</code> (only allowed if <code>defValue</code> is not <code>
     * null</code>), or an object of type <code>Number</code>. This method is
     * used to ensure that any value specified as a number is properly handled.
     * 
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
    double getDoubleValueFromObject(Object object, Double defValue)
            throws MegawidgetException {
        if (object == null) {
            if (defValue == null) {
                throw new MegawidgetException(identifier, getType(), null, null);
            } else {
                return defValue.doubleValue();
            }
        }
        try {
            return getDoubleValueFromObject(object);
        } catch (Exception e) {
            throw new MegawidgetException(identifier, getType(), object,
                    e.getMessage());
        }
    }

    /**
     * Get a double object from the specified object. The object must be either
     * <code>null</code> (only allowed if <code>defValue</code> is not <code>
     * null</code>), or an object of type <code>Number</code>. This method is
     * used to ensure that any value specified as a number is properly handled.
     * If the object is a <code>Double</code>, it is simply cast to this type
     * and returned.
     * 
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
    Double getDoubleObjectFromObject(Object object, Double defValue)
            throws MegawidgetException {
        if (object instanceof Double) {
            return (Double) object;
        } else {
            return getDoubleValueFromObject(object, defValue);
        }
    }

    /**
     * Get a boolean from the specified object. The object must be either
     * <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or an object of type <code>Boolean</code>, <code>
     * Integer</code> or <code>Long</code>. This method is used to ensure that
     * any value specified as a long or integer of either 0 or 1 is properly
     * handled.
     * 
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
    boolean getBooleanValueFromObject(Object object, Boolean defValue)
            throws MegawidgetException {
        if (object == null) {
            if (defValue == null) {
                throw new MegawidgetException(identifier, getType(), null, null);
            } else {
                return defValue.booleanValue();
            }
        } else if (object instanceof Number) {
            Number number = (Number) object;
            long value = number.longValue();
            if ((((number instanceof Double) || (number instanceof Float)) && ((value)
                    - number.doubleValue() != 0.0))
                    || ((value != 0L) && (value != 1L))) {
                throw new MegawidgetException(identifier, getType(), number,
                        "must be boolean");
            } else {
                return (value == 1L ? true : false);
            }
        } else if (object instanceof Boolean) {
            return ((Boolean) object).booleanValue();
        } else {
            throw new MegawidgetException(identifier, getType(), object,
                    "must be boolean");
        }
    }

    /**
     * Get a boolean object from the specified object. The object must be either
     * <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or an object of type <code>Boolean</code>, <code>
     * Integer</code> or <code>Long</code>. This method is used to ensure that
     * any value specified as a long or integer of either 0 or 1 is properly
     * handled. If the object is a <code>Boolean</code>, it is simply cast to
     * this type and returned.
     * 
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
    Boolean getBooleanObjectFromObject(Object object, Boolean defValue)
            throws MegawidgetException {
        if (object instanceof Boolean) {
            return (Boolean) object;
        } else {
            return getBooleanValueFromObject(object, defValue);
        }
    }

    /**
     * Get a dynamically typed object from the specified object. The object must
     * be either <code>null</code> (only allowed if <code>defValue</code> is not
     * <code>null</code>), or an object of dynamic type <code>T</code>.
     * 
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
    <T> T getDynamicallyTypedObjectFromObject(Object object,
            Class<T> requiredClass, T defValue) throws MegawidgetException {

        // If no object was supplied, return the default value, or throw an
        // exception if no default was supplied either.
        if (object == null) {
            if (defValue == null) {
                throw new MegawidgetException(identifier, getType(), null, null);
            } else {
                return defValue;
            }
        }

        // If the value cannot be cast, throw an exception. Otherwise, if the
        // required dynamic type is extended or implemented by the class of
        // the object, the object itself may be returned. If neither of these
        // are the case, do some conversions for specific types of objects
        // if one of these is desired, or throw an exception if no conversion
        // can be done.
        try {
            T value = (T) object;
            if (requiredClass.isAssignableFrom(value.getClass())) {
                return value;
            } else if (requiredClass.equals(Integer.class)) {
                return (T) getIntegerObjectFromObject(value, null);
            } else if (requiredClass.equals(Long.class)) {
                return (T) getLongObjectFromObject(value, null);
            } else if (requiredClass.equals(Float.class)) {
                return (T) getFloatObjectFromObject(value, null);
            } else if (requiredClass.equals(Double.class)) {
                return (T) getDoubleObjectFromObject(value, null);
            } else if (requiredClass.equals(Boolean.class)) {
                return (T) getBooleanObjectFromObject(value, null);
            } else {
                throw new ClassCastException(value.getClass()
                        + " cannot be cast to " + requiredClass);
            }
        } catch (Exception e) {
            throw new MegawidgetException(identifier, getType(), object,
                    "must be " + requiredClass.getSimpleName(),
                    new ClassCastException(object.getClass()
                            + " cannot be cast to " + requiredClass));
        }
    }

    // Private Methods

    /**
     * Get the specified class array as a string listing the array elements.
     * 
     * @param classArray
     *            Array of classes.
     * @return String listing the array elements.
     */
    private String getClassArrayAsString(Class<?>[] classArray) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Class<?> element : classArray) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(element.getName());
        }
        return stringBuilder.toString();
    }
}
