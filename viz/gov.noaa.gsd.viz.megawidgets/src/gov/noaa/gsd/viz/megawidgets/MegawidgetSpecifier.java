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
import java.util.Collections;
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
 * Megawidget specifier abstract base class.
 * <p>
 * All concrete subclasses must have a constructor taking arguments identical to
 * those taken by the constructor of this class. Furthermore, all concrete
 * subclasses of {@link Megawidget} must have the same name as their
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
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 24, 2014   4009     Chris.Golden      Added extra data functionality.
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
    private static Map<Composite, List<Control>> resizeAwareControlListsForParents = new HashMap<>();

    /**
     * Hash table mapping parent composites to their control listeners, if any.
     * These are used to listen for resize events and pass them onto the widgets
     * in the lists associated with the same parents in
     * {@link #resizeAwareControlListsForParents}.
     */
    private static Map<Composite, ControlListener> controlListenersForParents = new HashMap<>();

    /**
     * Hash table mapping parent composites to their dispose listeners, if any.
     * These are used to listen for dispose events and use them to remove the
     * control listeners and lists of widgets associated with the parents when
     * the latter are disposed of.
     */
    private static Map<Composite, DisposeListener> disposeListenersForParents = new HashMap<>();

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

    /**
     * Extra data.
     */
    private final Map<String, Object> extraData;

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
    @SuppressWarnings("unchecked")
    public MegawidgetSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {

        /*
         * Ensure that the identifier is present and acceptable.
         */
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

        /*
         * Ensure that the enabled flag, if present, is acceptable.
         */
        enabled = ConversionUtilities.getSpecifierBooleanValueFromObject(
                identifier, getType(), parameters.get(MEGAWIDGET_ENABLED),
                MEGAWIDGET_ENABLED, true);

        /*
         * Ensure that the label, if present, is acceptable.
         */
        try {
            label = (String) parameters.get(MEGAWIDGET_LABEL);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(identifier, getType(),
                    MEGAWIDGET_LABEL, parameters.get(MEGAWIDGET_LABEL),
                    "must be string");
        }

        /*
         * Ensure that if extra data is specified, it is a map.
         */
        Map<String, Object> extraData = null;
        try {
            extraData = (Map<String, Object>) parameters
                    .get(MEGAWIDGET_EXTRA_DATA);
            if (extraData != null) {
                extraData = Collections.unmodifiableMap(extraData);
            }
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(identifier, getType(),
                    MEGAWIDGET_EXTRA_DATA,
                    parameters.get(MEGAWIDGET_EXTRA_DATA),
                    "must be map of string keys to arbitrary object values");
        }
        if (extraData == null) {
            extraData = Collections.emptyMap();
        }
        this.extraData = extraData;
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

    @Override
    public final Map<String, Object> getExtraData() {
        return extraData;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends Widget, M extends IMegawidget> M createMegawidget(
            P parent, Class<M> superClass, Map<String, Object> creationParams)
            throws MegawidgetException {
        /*
         * Determine the full path and name of the megawidget class of which an
         * instance is to be created.
         */
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

        /*
         * Get the class.
         */
        Class<?> megawidgetClass = null;
        try {
            megawidgetClass = Class.forName(classPathAndName);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(identifier, getType(),
                    null, null, "not a valid megawidget (cannot find class "
                            + classPathAndName + ")");
        }

        /*
         * If the class is not a subclass of the provided superclass, complain.
         */
        if (superClass.isAssignableFrom(megawidgetClass) == false) {
            throw new MegawidgetSpecificationException(identifier, getType(),
                    null, null, "not a valid megawidget (" + megawidgetClass
                            + " is not a subclass of " + superClass + ")");
        }

        /*
         * Iterate through all declared constructors for the megawidget subclass
         * of which an instance is to be created, looking for the one that best
         * fits the parameters available, namely, this specifier's class, the
         * parent widget's class, and the Map class, respectively. the "best"
         * fit is the one that can take these parameters and that takes as its
         * parent parameter an object of a class that is as close as possible
         * (in terms of generational difference) to the class of the supplied
         * parent object.
         */
        Class<?>[] neededArgTypes = { getClass(), parent.getClass(), Map.class };
        Constructor<?> bestConstructor = null;
        int bestMegawidgetClassGenerationalDifference = -1;
        for (Constructor<?> constructor : megawidgetClass
                .getDeclaredConstructors()) {

            /*
             * Do nothing more with this constructor if it cannot take these
             * parameter types.
             */
            Class<?>[] argTypes = constructor.getParameterTypes();
            if ((argTypes.length != neededArgTypes.length)
                    || (argTypes[0].equals(neededArgTypes[0]) == false)
                    || (Widget.class.isAssignableFrom(neededArgTypes[1]) == false)
                    || (argTypes[2].equals(neededArgTypes[2]) == false)) {
                continue;
            }

            /*
             * See how many generations apart the parent object's class is from
             * the parameter type for the constructor.
             */
            int megawidgetClassGenerationalDifference = 0;
            for (Class<?> otherMegawidgetClass = neededArgTypes[1]; otherMegawidgetClass != argTypes[1]; otherMegawidgetClass = otherMegawidgetClass
                    .getSuperclass()) {
                megawidgetClassGenerationalDifference++;
            }

            /*
             * If this is the only fitting constructor so far, or if it is the
             * fewest generations apart from the parent object's class so far,
             * mark it as the best.
             */
            if ((bestConstructor == null)
                    || (megawidgetClassGenerationalDifference < bestMegawidgetClassGenerationalDifference)) {
                bestConstructor = constructor;
                bestMegawidgetClassGenerationalDifference = megawidgetClassGenerationalDifference;
            }

            /*
             * If this constructor's parameter is of the same type as the parent
             * object's class, use this constructor.
             */
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

        /*
         * Construct an instance of the class using the passed-in parameters
         * with the constructor found above.
         */
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

        /*
         * Return the result.
         */
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

            /*
             * Find the list of controls for this parent; if none has been
             * created by now, set one up.
             */
            List<Control> list = resizeAwareControlListsForParents.get(parent);
            if (list == null) {

                /*
                 * Create the list to hold controls that wish to be notified of
                 * parent resizes.
                 */
                list = new ArrayList<>();
                resizeAwareControlListsForParents.put(parent, list);

                /*
                 * Create a control listener for the parent that resizes any
                 * children in its list when the parent itself is resized, and
                 * associate the listener with the parent.
                 */
                ControlListener controlListener = new ControlAdapter() {
                    @Override
                    public void controlResized(ControlEvent e) {

                        /*
                         * Ensure that there are widgets to be notified of the
                         * resize.
                         */
                        List<Control> list = resizeAwareControlListsForParents
                                .get(e.widget);
                        if (list != null) {

                            /*
                             * Change each child's width hint to match that of
                             * the client area of the parent.
                             */
                            Composite widget = (Composite) e.widget;
                            for (Control child : list) {
                                GridLayout layout = (GridLayout) widget
                                        .getLayout();
                                ((GridData) child.getLayoutData()).widthHint = widget
                                        .getClientArea().width
                                        - ((layout.marginWidth * 2)
                                                + layout.marginLeft + layout.marginRight);
                            }

                            /*
                             * Tell the parent to lay itself out again.
                             */
                            ((Composite) e.widget).layout(list
                                    .toArray(new Control[list.size()]));
                        }
                    }
                };
                parent.addControlListener(controlListener);
                controlListenersForParents.put(parent, controlListener);

                /*
                 * Create a disposal listener for the parent that removes all
                 * associations of the parent related to notifying children of
                 * resizes, since after disposal they are no longer needed.
                 */
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
