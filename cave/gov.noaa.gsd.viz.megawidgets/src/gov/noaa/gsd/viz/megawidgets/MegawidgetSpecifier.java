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
import java.util.ArrayList;
import java.util.HashMap;
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

/**
 * Megawidget specifier base class, from which specific types of megawidget
 * specifiers may be derived. A megawidget specifier allows the specification of
 * megawidgets for later creation; the type and configuration of each such
 * megawidget is specified via a <code>Map</code> containing key-value pairs,
 * with each key being a string chosen from one of the string constants defined
 * within this class or within a subclass, and the value being something
 * appropriate to that key, as specified by that key's description. Some
 * key-value pairs are mandatory, and others are optional, again as described.
 * <p>
 * For window-based megawidgets (those that require a <code>Composite</code> as
 * their parent), megawidgets expect that the parent is using a <code>GridLayout
 * </code> with two columns. A suggestion as to how a megawidget positions
 * itself within the layout may be included via its specifier.
 * <p>
 * All subclasses must have a constructor taking arguments identical to those
 * taken by the constructor of this class. Furthermore, all concrete subclasses
 * of <code>Megawidget</code> must have the same name as their corresponding
 * specifier classes, except with "Specifier" replaced with "Megawidget", and
 * each such subclass must exist in the same package as its specifier. Thus, for
 * example, if there is a specifier with the path and name <code>
 * org.foo.megawidgets.ExampleSpecifier</code>, the latter will assume that the
 * megawidget it is to construct is an instance of the class <code>
 * org.foo.megawidgets.ExampleMegawidget</code>.
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
 * @see Megawidget
 */
public abstract class MegawidgetSpecifier {

    // Public Static Constants

    /**
     * Megawidget type parameter name; each widget must include a value
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
     * megawidget specifier for this <code>MEGAWIDGET_TYPE
     * </code> is a part. If not specified, it defaults to this class's package.
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
     * Megawidget editable parameter name; a megawidget may include a boolean
     * value associated with this name, in order to indicate whether or not the
     * megawidget should be editable when it is first created. An editable
     * megawidget is one that contains state that may be changed, or that is
     * used potentially to modify other state. If not specified, it is assumed
     * to be <code>
     * true</code>.
     */
    public static final String MEGAWIDGET_EDITABLE = "editable";

    /**
     * Megawidget label parameter name; a megawidget may include a value
     * associated with this name. Any string is valid as a value.
     */
    public static final String MEGAWIDGET_LABEL = "label";

    /**
     * Megawidget width parameter name; a megawidget may include a value
     * associated with this name. Valid values include any positive integer less
     * than or equal to the number of columns that the parent megawidget
     * contains, or, if the parent is not a megawidget, 1. If not specified, the
     * default is 1.
     */
    public static final String MEGAWIDGET_WIDTH = "width";

    /**
     * Megawidget spacing parameter name; a megawidget may include a
     * non-negative integer associated with this name to indicate that it wishes
     * to be spaced by this many pixels from the megawidget above it (or if at
     * the top of the parent <code>Composite</code>, from the top of the client
     * area of the parent). If not specified, the default is 0 pixels.
     */
    public static final String MEGAWIDGET_SPACING = "spacing";

    // Protected Static Constants

    /**
     * Megawidget parent column count parameter name; a megawidget may include a
     * positive integer associated with this name, indicating how many columns
     * the parent of the megawidget has available within which this megawidget
     * may lay itself out. If not specified, the default is 1.
     */
    protected static final String MEGAWIDGET_PARENT_COLUMN_COUNT = "parentNumColumns";

    // Private Static Variables

    /**
     * Hash table mapping parent composites to lists of widgets that must be
     * notified when the parents change size.
     */
    private static Map<Composite, List<Control>> resizeAwareControlListsForParents = new HashMap<Composite, List<Control>>();

    /**
     * Hash table mapping parent composites to their control listeners, if any.
     * These are used to listen for resize events and pass them onto the widgets
     * in the lists associated with the same parents in <code>
     * resizeAwareControlListsForParents</code>.
     */
    private static Map<Composite, ControlListener> controlListenersForParents = new HashMap<Composite, ControlListener>();

    /**
     * Hash table mapping parent composites to their dispose listeners, if any.
     * These are used to listen for dispose events and use them to remove the
     * control listeners and lists of widgets associated with the parents when
     * the latter are disposed of.
     */
    private static Map<Composite, DisposeListener> disposeListenersForParents = new HashMap<Composite, DisposeListener>();

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
     * Flag indicating whether the megawidget should be created in an editable
     * or a read-only state.
     */
    private final boolean editable;

    /**
     * Label.
     */
    private final String label;

    /**
     * Number of columns which the megawidget should take up within its parent.
     */
    private final int width;

    /**
     * Spacing between this megawidget and whatever is above it.
     */
    private final int spacing;

    /**
     * Number of columns provided by the parent in which this megawidget is to
     * lay itself out.
     */
    private final int parentColumnCount;

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

        // Ensure that the editable flag, if present, is
        // acceptable.
        editable = getSpecifierBooleanValueFromObject(
                parameters.get(MEGAWIDGET_EDITABLE), MEGAWIDGET_EDITABLE, true);

        // Ensure that the label, if present, is acceptable.
        try {
            label = (String) parameters.get(MEGAWIDGET_LABEL);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(identifier, getType(),
                    MEGAWIDGET_LABEL, parameters.get(MEGAWIDGET_LABEL),
                    "must be string");
        }

        // Get the number of columns available within the
        // parent.
        parentColumnCount = getSpecifierIntegerValueFromObject(
                parameters.get(MEGAWIDGET_PARENT_COLUMN_COUNT),
                MEGAWIDGET_PARENT_COLUMN_COUNT, 1);

        // Ensure that the width, if present, is acceptable,
        // and if not present is assigned a default value.
        width = getSpecifierIntegerValueFromObject(
                parameters.get(MEGAWIDGET_WIDTH), MEGAWIDGET_WIDTH, 1);
        if ((width < 1) || (width > parentColumnCount)) {
            throw new MegawidgetSpecificationException(identifier, getType(),
                    MEGAWIDGET_WIDTH, width, "must be between 1 and "
                            + parentColumnCount + " (inclusive)");
        }

        // Ensure that the spacing, if present, is acceptable,
        // and if not present is assigned a default value.
        spacing = getSpecifierIntegerValueFromObject(
                parameters.get(MEGAWIDGET_SPACING), MEGAWIDGET_SPACING, 0);
        if (spacing < 0) {
            throw new MegawidgetSpecificationException(identifier, getType(),
                    MEGAWIDGET_SPACING, spacing, "must be non-negative");
        }
    }

    // Public Methods

    /**
     * Get the identifier.
     * 
     * @return Identifier.
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * Get the type.
     * 
     * @return Type.
     */
    public final String getType() {
        String className = getClass().getSimpleName();
        int endIndex = className.lastIndexOf("Specifier");
        if (endIndex > -1) {
            className = className.substring(0, endIndex);
        }
        return className;
    }

    /**
     * Get the flag indicating whether or not the megawidget is to be created in
     * an enabled state.
     * 
     * @return True if the megawidget is to be created as enabled, false
     *         otherwise.
     */
    public final boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the flag indicating whether or not the megawidget is to be created in
     * an editable state.
     * 
     * @return True if the megawidget is to be created as editable, false
     *         otherwise.
     */
    public final boolean isEditable() {
        return editable;
    }

    /**
     * Get the label.
     * 
     * @return Label.
     */
    public final String getLabel() {
        return label;
    }

    /**
     * Get the width of the megawidget in columns within its parent.
     * 
     * @return Number of columns it should span.
     */
    public final int getWidth() {
        return width;
    }

    /**
     * Get the spacing.
     * 
     * @return Spacing.
     */
    public final int getSpacing() {
        return spacing;
    }

    /**
     * Create the GUI components making up the specified megawidget.
     * 
     * @param parent
     *            Parent widget in which to place the megawidget.
     * @param creationParams
     *            Hash table mapping identifiers to values that subclasses might
     *            require when creating a megawidget.
     * @return Created megawidget.
     * @throws MegawidgetException
     *             If an exception occurs during creation or initialization of
     *             the megawidget.
     */
    public <P extends Widget> Megawidget createMegawidget(P parent,
            Map<String, Object> creationParams) throws MegawidgetException {

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

        // If the class is not a subclass of Megawidget, complain.
        if (Megawidget.class.isAssignableFrom(megawidgetClass) == false) {
            throw new MegawidgetSpecificationException(identifier, getType(),
                    null, null, "not a valid megawidget (" + megawidgetClass
                            + " is not a subclass of " + Megawidget.class + ")");
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
        int bestWidgetClassGenerationalDifference = -1;
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
            int widgetClassGenerationalDifference = 0;
            for (Class<?> widgetClass = neededArgTypes[1]; widgetClass != argTypes[1]; widgetClass = widgetClass
                    .getSuperclass()) {
                widgetClassGenerationalDifference++;
            }

            // If this is the only fitting constructor so
            // far, or if it is the fewest generations apart
            // from the parent object's class so far, mark
            // it as the best.
            if ((bestConstructor == null)
                    || (widgetClassGenerationalDifference < bestWidgetClassGenerationalDifference)) {
                bestConstructor = constructor;
                bestWidgetClassGenerationalDifference = widgetClassGenerationalDifference;
            }

            // If this constructor's parameter is of the
            // same type as the parent object's class, use
            // this constructor.
            if (widgetClassGenerationalDifference == 0) {
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
        Megawidget megawidget = null;
        try {
            megawidget = (Megawidget) bestConstructor
                    .newInstance(constructorArgValues);
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
     * Get the parent column count.
     * 
     * @return Parent column count.
     */
    protected final int getParentColumnCount() {
        return parentColumnCount;
    }

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
                list = new ArrayList<Control>();
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
            throw new MegawidgetSpecificationException(identifier, e.getType(),
                    paramName, e.getBadValue(), e.getMessage(), e.getCause());
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
            throw new MegawidgetSpecificationException(identifier, e.getType(),
                    paramName, e.getBadValue(), e.getMessage(), e.getCause());
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
            throw new MegawidgetSpecificationException(identifier, e.getType(),
                    paramName, e.getBadValue(), e.getMessage(), e.getCause());
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
            throw new MegawidgetSpecificationException(identifier, e.getType(),
                    paramName, e.getBadValue(), e.getMessage(), e.getCause());
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
            throw new MegawidgetSpecificationException(identifier, e.getType(),
                    paramName, e.getBadValue(), e.getMessage(), e.getCause());
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
            throw new MegawidgetSpecificationException(identifier, e.getType(),
                    paramName, e.getBadValue(), e.getMessage(), e.getCause());
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
                throw new MegawidgetException(getType(), null, null);
            } else {
                return defValue.intValue();
            }
        } else if (object instanceof Number) {
            Number number = (Number) object;
            long value = number.longValue();
            if ((((number instanceof Double) || (number instanceof Float)) && ((value)
                    - number.doubleValue() != 0.0))
                    || (value < Integer.MIN_VALUE)
                    || (value > Integer.MAX_VALUE)) {
                throw new MegawidgetException(getType(), number,
                        "must be integer");
            } else {
                return (int) value;
            }
        } else {
            throw new MegawidgetException(getType(), object, "must be integer");
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
                throw new MegawidgetException(getType(), null, null);
            } else {
                return defValue.longValue();
            }
        } else if (object instanceof Number) {
            Number number = (Number) object;
            long value = number.longValue();
            if (((number instanceof Double) || (number instanceof Float))
                    && ((value) - number.doubleValue() != 0.0)) {
                throw new MegawidgetException(getType(), number,
                        "must be long integer");
            } else {
                return value;
            }
        } else {
            throw new MegawidgetException(getType(), object,
                    "must be long integer");
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
                throw new MegawidgetException(getType(), null, null);
            } else {
                return defValue.booleanValue();
            }
        } else if (object instanceof Number) {
            Number number = (Number) object;
            long value = number.longValue();
            if ((((number instanceof Double) || (number instanceof Float)) && ((value)
                    - number.doubleValue() != 0.0))
                    || ((value != 0L) && (value != 1L))) {
                throw new MegawidgetException(getType(), number,
                        "must be boolean");
            } else {
                return (value == 1L ? true : false);
            }
        } else if (object instanceof Boolean) {
            return ((Boolean) object).booleanValue();
        } else {
            throw new MegawidgetException(getType(), object, "must be boolean");
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
