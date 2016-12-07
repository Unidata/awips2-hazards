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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

import com.google.common.collect.Sets;

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
 * Dec 06, 2016  26855     Chris.Golden      Changed to make resize-aware
 *                                           megawidgets better handle the size
 *                                           changes of their parents, as well as
 *                                           the shrinkage of a parent's enclosing
 *                                           scrollable composite. The latter
 *                                           triggers the resize-aware megawidgets
 *                                           to shrink themselves so that they
 *                                           do not take up so much room in their
 *                                           parent, and thus allow the parent to
 *                                           shrink, which in turn avoids the
 *                                           unnecessary use of scrollbars in the
 *                                           parent's scrollable container.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see Megawidget
 */
public abstract class MegawidgetSpecifier implements ISpecifier {

    // Private Static Variables

    /**
     * Map of parent composites to widgets that must be notified when the
     * parents change size.
     */
    private static Map<Composite, Set<Control>> resizeAwareControlsForParents = new IdentityHashMap<>();

    /**
     * Map of widgets that are notified when their parents change size to their
     * parents.
     */
    private static Map<Control, Composite> parentsForResizeAwareControls = new IdentityHashMap<>();

    /**
     * Map of ancestor scrollable composites to widgets that must be notified
     * when the ancestors change size.
     */
    private static Map<ScrolledComposite, Set<Control>> resizeAwareControlsForScrollableAncestors = new IdentityHashMap<>();

    /**
     * Map of ancestor scrollable composites to their last recorded widths.
     */
    private static Map<ScrolledComposite, Integer> widthsForScrollableAncestors = new IdentityHashMap<>();

    /**
     * Map of widgets that must be notified when their parents change size (that
     * is, those widgets found in the sets that are values within
     * {@link #resizeAwareControlsForParents}) to their maximum widths.
     */
    private static Map<Control, Integer> maxWidthsForControls = new IdentityHashMap<>();

    /**
     * Map of widgets that must be notified when their parents change size (that
     * is, those widgets found in the sets that are values within
     * {@link #resizeAwareControlsForParents}) to the resize handlers that
     * should be run when they themselves change size.
     */
    private static Map<Control, Runnable> resizeHandlersForControls = new IdentityHashMap<>();

    /**
     * Map pairing resize-aware controls with the number of times that they are
     * handling a resize simultaneously. This is used to avoid inappropriate
     * reentrant calls to the resize routines for particular controls.
     */
    private static Map<Control, Integer> resizeCountsForControls = new IdentityHashMap<>();

    /**
     * Map pairing resize-aware controls with the widths they had prior to the
     * last decrease of their ancestors.
     */
    private static Map<Control, Integer> preDecreaseWidthsForControls = new IdentityHashMap<>();

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
     * @param maximumWidth
     *            Maximum width desired by the child widget.
     * @param resizeHandler
     *            Optional runnable that, if supplied, is to be run after this
     *            child resizes itself.
     */
    protected final void ensureChildIsResizedWithParent(Composite parent,
            Control child, int maximumWidth, Runnable resizeHandler) {
        synchronized (resizeAwareControlsForParents) {

            /*
             * Remember the maximum desired width of the control, and the resize
             * handler for the control.
             */
            maxWidthsForControls.put(child, maximumWidth);
            resizeHandlersForControls.put(child, resizeHandler);

            /*
             * Find the collection of controls for this parent; if none has been
             * created by now, set one up.
             */
            Set<Control> resizeAwareControls = resizeAwareControlsForParents
                    .get(parent);
            if (resizeAwareControls == null) {

                /*
                 * Create the collection to hold controls that wish to be
                 * notified of parent resizes.
                 */
                resizeAwareControls = Sets.newIdentityHashSet();
                resizeAwareControlsForParents.put(parent, resizeAwareControls);

                /*
                 * Create a control listener for the parent that resizes any
                 * children in its list when the parent itself is resized, and
                 * associate the listener with the parent.
                 */
                parent.addControlListener(new ControlAdapter() {
                    @Override
                    public void controlResized(ControlEvent e) {
                        ancestorResized((Composite) e.widget,
                                resizeAwareControlsForParents, 0);
                    }
                });

                /*
                 * Create a disposal listener for the parent that removes all
                 * associations of the parent related to notifying children of
                 * resizes, since after disposal they are no longer needed.
                 */
                parent.addDisposeListener(new DisposeListener() {
                    @Override
                    public void widgetDisposed(DisposeEvent e) {
                        Set<Control> resizeAwareControls = resizeAwareControlsForParents
                                .remove(e.widget);
                        if (resizeAwareControls != null) {
                            for (Control control : resizeAwareControls) {
                                maxWidthsForControls.remove(control);
                                resizeHandlersForControls.remove(control);
                                parentsForResizeAwareControls.remove(control);
                                resizeCountsForControls.remove(control);
                                preDecreaseWidthsForControls.remove(control);
                            }
                        }
                    }
                });
            }

            /*
             * Add this control to the resize-aware controls collection that was
             * just found or created, and link the parent to the control.
             */
            resizeAwareControls.add(child);
            parentsForResizeAwareControls.put(child, parent);

            /*
             * Determine whether or not there is a scrollable ancestor (whether
             * the parent, a grandparent, etc.) of the control. If there is, add
             * a control listener that tracks the width of the scrollable
             * ancestor, and when it shrinks, attempts to shrink the
             * resize-aware widgets so that they take up less space, which means
             * the scrollable's child composite may get smaller.
             */
            Composite ancestor = parent;
            while ((ancestor != null)
                    && (ancestor instanceof ScrolledComposite == false)) {
                ancestor = ancestor.getParent();
            }
            final ScrolledComposite scrollable = (ScrolledComposite) ancestor;
            if (scrollable == null) {
                return;
            }

            /*
             * Find the list of controls for this scrollable ancestor; if none
             * has been created by now, set one up.
             */
            resizeAwareControls = resizeAwareControlsForScrollableAncestors
                    .get(scrollable);
            if (resizeAwareControls == null) {

                /*
                 * Create the collection to hold controls that wish to be
                 * notified of scrollable ancestor resizes.
                 */
                resizeAwareControls = Sets.newIdentityHashSet();
                resizeAwareControlsForScrollableAncestors.put(scrollable,
                        resizeAwareControls);

                /*
                 * Create a control listener for the scrollable ancestor that
                 * resizes any children in its list when the ancestor itself is
                 * made narrower, and associate the listener with the ancestor.
                 */
                scrollable.addControlListener(new ControlAdapter() {
                    @Override
                    public void controlResized(ControlEvent e) {
                        Integer oldWidth = widthsForScrollableAncestors
                                .get(scrollable);
                        int newWidth = scrollable.getSize().x;
                        if ((oldWidth == null) || (oldWidth != newWidth)) {
                            widthsForScrollableAncestors.put(scrollable,
                                    newWidth);
                            if ((oldWidth != null) && (oldWidth > newWidth)) {
                                int delta = oldWidth - newWidth;
                                ancestorResized(
                                        (Composite) e.widget,
                                        resizeAwareControlsForScrollableAncestors,
                                        delta);
                            }
                        }
                    }
                });

                /*
                 * Create a disposal listener that removes all associations with
                 * said ancestor when the latter is disposed.
                 */
                scrollable.addDisposeListener(new DisposeListener() {

                    @Override
                    public void widgetDisposed(DisposeEvent e) {
                        resizeAwareControlsForScrollableAncestors
                                .remove(e.widget);
                        widthsForScrollableAncestors.remove(e.widget);
                    }
                });
            }

            /*
             * Add this control to the resize-aware controls collection that was
             * just found or created.
             */
            resizeAwareControls.add(child);
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

    /**
     * Respond to the resizing of the specified ancestor by resizing any
     * resize-aware controls associated with said ancestor in the specified map.
     * 
     * @param ancestor
     *            Ancestor that has been resized.
     * @param resizeAwareControlsForAncestors
     *            Map of ancestors to the resize-aware controls that are to be
     *            resized when the ancestor changes its size.
     * @param decrease
     *            Non-negative number of pixels by which the width of the
     *            ancestor decreased. If this is <code>0</code>, the new size of
     *            any resize-aware controls is calculated by divvying up the
     *            available width of the ancestor between them as appropriate.
     *            If it is a positive number, it indicates the amount by which
     *            all the resize-aware ancestors in a particular row should
     *            collectively shrink their widths if possible.
     */
    private void ancestorResized(
            Composite ancestor,
            Map<? extends Composite, Set<Control>> resizeAwareControlsForAncestors,
            int decrease) {

        /*
         * Ensure that there are widgets to be notified of the resize.
         */
        if (resizeAwareControlsForAncestors.containsKey(ancestor) == false) {
            return;
        }
        Set<Control> resizeAwareControls = new HashSet<>(
                resizeAwareControlsForAncestors.get(ancestor));

        /*
         * If there are no controls that have not been disposed of, do nothing.
         */
        pruneDisposedWidgets(resizeAwareControls);
        if (resizeAwareControls.isEmpty()) {
            return;
        }

        /*
         * Iterate through the resize-aware controls, recording for each one the
         * new reentrant count for that control. The reentrant count is only
         * incremented if this is a decrease and the old count is 0, or if this
         * is not a decrease and the old count is a positive number. This way,
         * reentrancy is only counted if there is a decrease in process.
         */
        for (Control control : resizeAwareControls) {
            Integer count = resizeCountsForControls.get(control);
            if ((count == null) || (count == 0)) {
                if (decrease > 0) {
                    resizeCountsForControls.put(control, 1);
                }
            } else if (count > 0) {
                if (decrease < 1) {
                    resizeCountsForControls.put(control, ++count);
                }
            }
        }

        /*
         * Find the parents of all the resize-aware controls associated with
         * this ancestor (since the ancestor may or may not be the parent of all
         * the controls), and for each such parent, associate a set of its
         * resize-aware children with it. This way, each parent's resize-aware
         * children may be dealt with as a group, separately from the other
         * resize-aware controls that do not share the same parent with that
         * group.
         */
        Map<Composite, Set<Control>> controlsForParents = new IdentityHashMap<>();
        for (Control control : resizeAwareControls) {
            Composite parent = parentsForResizeAwareControls.get(control);
            Set<Control> controls = controlsForParents.get(parent);
            if (controls == null) {
                controls = Sets.newIdentityHashSet();
                controlsForParents.put(parent, controls);
            }
            controls.add(control);
        }

        /*
         * Iterate through each set of resize-aware controls associated with one
         * parent, resizing said controls as appropriate.
         * 
         * TODO: It is assumed that the parents are never gridded in such a
         * manner that they lie side by side. Allowing for the latter would
         * require a much more complex algorithm, but this may be worth doing in
         * the future.
         */
        Set<Control> allResizedControls = new HashSet<>(
                resizeAwareControls.size(), 1.0f);
        for (Map.Entry<Composite, Set<Control>> entry : controlsForParents
                .entrySet()) {
            Set<Control> resizedControls = new HashSet<>(
                    resizeAwareControls.size(), 1.0f);

            /*
             * Iterate through the children of the widget being resized,
             * compiling information about the row of any resize-aware child so
             * that said child's new preferred width may be calculated.
             */
            Composite parent = entry.getKey();
            Set<Control> controls = entry.getValue();
            GridLayout layout = (GridLayout) parent.getLayout();
            int column = 0;
            int row = 0;
            int numResizablesInRow = 0;
            int numColumnsHoldingResizablesInRow = 0;
            Set<Integer> nonResizableColumnIndicesInRow = new HashSet<>();
            Map<Control, Integer> rowsForControls = new IdentityHashMap<>();
            Map<Control, Integer> columnsForControls = (decrease > 0 ? new IdentityHashMap<Control, Integer>()
                    : null);
            List<Set<Integer>> nonResizableColumnIndicesInRows = new ArrayList<>();
            List<Integer> numResizablesInRows = new ArrayList<>();
            List<Integer> numColumnsHoldingResizablesInRows = new ArrayList<>();
            Map<Integer, Integer> widthsForColumns = new HashMap<>(
                    layout.numColumns, 1.0f);
            for (Control child : parent.getChildren()) {

                /*
                 * If this child is one of the resize-aware ones, make a note of
                 * the row it is in, and what column it starts in (it may span
                 * multiple columns) if the width of the ancestor has decreased.
                 * Otherwise, if the child does not attempt to grab excess
                 * horizontal space, divide up its width (minus the padding
                 * between columns) by the number of columns it spans, and for
                 * each such column, see if the resulting width is greater than
                 * the largest width already recorded for that column. If it is,
                 * treat the new width as the largest for the column.
                 */
                GridData gridData = (GridData) child.getLayoutData();
                if (controls.contains(child)) {
                    rowsForControls.put(child, row);
                    if (columnsForControls != null) {
                        columnsForControls.put(child, column);
                    }
                    numColumnsHoldingResizablesInRow += gridData.horizontalSpan;
                    numResizablesInRow++;
                } else {
                    if (gridData.grabExcessHorizontalSpace == false) {
                        int newWidth = (child.getSize().x - (layout.horizontalSpacing * (gridData.horizontalSpan - 1)))
                                / gridData.horizontalSpan;
                        for (int j = 0; j < gridData.horizontalSpan; j++) {
                            nonResizableColumnIndicesInRow.add(column + j);
                            int oldWidth = (widthsForColumns.containsKey(column
                                    + j) ? widthsForColumns.get(column + j)
                                    : -1);
                            if (newWidth > oldWidth) {
                                widthsForColumns.put(column + j, newWidth);
                            }
                        }
                    }
                }

                /*
                 * Increment the column counter, and if this means the row is
                 * complete, go to the next row, saving this row's set of
                 * indices of non-resize-aware controls, and the number of
                 * resize-aware controls in the row.
                 */
                column += ((GridData) child.getLayoutData()).horizontalSpan;
                if (column >= layout.numColumns) {
                    nonResizableColumnIndicesInRows
                            .add(nonResizableColumnIndicesInRow);
                    numResizablesInRows.add(numResizablesInRow);
                    numColumnsHoldingResizablesInRows
                            .add(numColumnsHoldingResizablesInRow);
                    row++;
                    column = 0;
                    numResizablesInRow = 0;
                    numColumnsHoldingResizablesInRow = 0;
                    nonResizableColumnIndicesInRow = new HashSet<>();
                }
            }

            /*
             * Change each resize-aware control's width hint to fill the space
             * appropriate for that row, if the row it is in has no
             * non-resize-aware siblings that expand to fill all available
             * space. Assuming the latter is not the case, the width hint is set
             * to be the total width of the parent, minus the sum of margins,
             * any space between columns, the total width of the
             * non-resize-aware controls; and then divided by the number of
             * resize-aware controls in the row.
             */
            for (Control child : controls) {

                /*
                 * Do nothing if this resize-aware child is already in the
                 * course of being resized both as a result of a decrease and of
                 * multiple non-decrease calls. This avoids bad reentrant
                 * behavior; it's fine for a decrease involving this child to
                 * result in a reentrant call that is not for decrease, but the
                 * latter call must not allow a further reentrant call.
                 */
                Integer reentrantCountObj = resizeCountsForControls.get(child);
                int reentrantCount = (reentrantCountObj == null ? 0
                        : reentrantCountObj);
                if (reentrantCount > 2) {
                    continue;
                }

                /*
                 * Do nothing if there is at least one sibling in this
                 * resize-aware child's row that grabs excess horizontal space.
                 */
                if (rowsForControls.containsKey(child) == false) {
                    continue;
                }
                row = rowsForControls.get(child);
                numResizablesInRow = numResizablesInRows.get(row);
                nonResizableColumnIndicesInRow = nonResizableColumnIndicesInRows
                        .get(row);
                if (numResizablesInRow + nonResizableColumnIndicesInRow.size() < layout.numColumns) {
                    continue;
                }

                /*
                 * Compute the amount of horizontal space taken up by the
                 * non-resize-aware columns in this row. If the total amount is
                 * 0 and there is at least one non-resize-aware sibling in the
                 * row, then this means that the sibling widgets have not had a
                 * chance to size themselves yet, so nothing should be done.
                 */
                int widthOfNonResizablesInRow = 0;
                for (Integer columnIndex : nonResizableColumnIndicesInRow) {
                    widthOfNonResizablesInRow += widthsForColumns
                            .get(columnIndex);
                }
                if ((widthOfNonResizablesInRow == 0)
                        && (layout.numColumns != numResizablesInRow)) {
                    continue;
                }

                /*
                 * If an explicit decrease in width is required, shrink the
                 * width appropriately; otherwise, change the width as
                 * appropriate given the ancestor's new width.
                 */
                int maximumWidth = maxWidthsForControls.get(child);
                GridData gridData = (GridData) child.getLayoutData();
                int newWidth;
                if (decrease > 0) {

                    /*
                     * The amount by which to decrease the width is determined
                     * by taking the total decrease available, dividing it by
                     * the number of columns holding resize-aware controls in
                     * this row, and multiplying the result by the number of
                     * columns this control takes up in this row.
                     */
                    int thisDecrease = decrease * gridData.horizontalSpan
                            / numColumnsHoldingResizablesInRows.get(row);

                    /*
                     * Determine the minimum width for the columns spanned by
                     * this control by seeing what the other non-resizable
                     * widgets in the same columns require.
                     */
                    int minimumWidth = 0;
                    int thisColumn = columnsForControls.get(child);
                    for (int j = 0; j < gridData.horizontalSpan; j++) {
                        minimumWidth += (widthsForColumns
                                .containsKey(thisColumn + j) == false ? 0
                                : widthsForColumns.get(thisColumn + j));
                    }

                    /*
                     * If decreasing the existing width by the appropriate
                     * decrease makes the width smaller than the minimum width
                     * calculated above, use said minimum width, or if that is
                     * larger than the maximum width for this control, the
                     * latter. Otherwise, just decrease the width by the amount
                     * appropriate.
                     */
                    newWidth = gridData.widthHint;
                    if (newWidth - thisDecrease < minimumWidth) {
                        newWidth = (minimumWidth < maximumWidth ? minimumWidth
                                : maximumWidth);
                    } else {
                        newWidth -= thisDecrease;
                    }
                } else {

                    /*
                     * Set the new width to equal the space remaining after
                     * subtracting the margins, the padding between columns, and
                     * the space already used by non-resize-aware siblings, all
                     * divided by the number of resize-aware siblings in this
                     * row.
                     */
                    int availableWidth = (parent.getClientArea().width - ((layout.marginWidth * 2)
                            + layout.marginLeft
                            + layout.marginRight
                            + widthOfNonResizablesInRow + (layout.horizontalSpacing * (layout.numColumns - 1))))
                            / numResizablesInRows.get(row);
                    newWidth = (maximumWidth > availableWidth ? availableWidth
                            : maximumWidth);
                }

                /*
                 * If the newly calculated width is different from what the
                 * child is using now, and either this is not a reentrant call
                 * or the new width is less than the old one, and there any
                 * pre-decrease width for this child that is found is not the
                 * same as the new width (to prevent the child from acquiring
                 * its old, larger width after a decrease), change the child's
                 * width hint.
                 * 
                 * The check of the reentrant count is done to ensure that an
                 * overly-nested call does not attempt a change, as this causes
                 * the widths of child controls to bounce between values when a
                 * decrease is in effect. Likewise, the check to ensure that the
                 * new width is not the same as the pre-decrease width is done
                 * for the same reason; if it was not, then after a decrease, a
                 * child's width might bounce back to the width it had before
                 * the decrease. Essentially the reason that this is so complex
                 * is that nested child controls within a composite that is
                 * acting as a scrollable's content pane are not told that they
                 * should get smaller as the scrollable gets smaller, and
                 * resize-aware widgets need to, as they should attempt to
                 * accommodate the shrinking parent's scrollable parent by
                 * requiring less width themselves, thus allowing their parents
                 * (the scrollable's content pane) to shrink and, if no other
                 * widgets are insisting on a larger size, not require
                 * scrollbars.
                 */
                if ((newWidth != gridData.widthHint)
                        && ((reentrantCount < 2) || (newWidth < gridData.widthHint))
                        && ((decrease > 0)
                                || (preDecreaseWidthsForControls
                                        .containsKey(child) == false) || (preDecreaseWidthsForControls
                                .get(child) != newWidth))) {

                    /*
                     * Record the old width if this is a decrease, so that it
                     * can be compared against future width-setting attempts for
                     * this child to ensure that the child is not trying to
                     * acquire the same width it had before the decrease.
                     */
                    if (decrease > 0) {
                        preDecreaseWidthsForControls.put(child,
                                gridData.widthHint);
                    } else {
                        preDecreaseWidthsForControls.remove(child);
                    }

                    /*
                     * Use the new width, and add the child to the list of
                     * controls that have been resized.
                     */
                    gridData.widthHint = newWidth;
                    resizedControls.add(child);
                }
            }

            /*
             * If at least one control changed size, lay them out.
             */
            if (resizedControls.isEmpty() == false) {
                parent.layout(resizedControls
                        .toArray(new Control[resizedControls.size()]));
                allResizedControls.addAll(resizedControls);
            }

            /*
             * Repack the parent if it is not the content pane of a scrollable
             * composite.
             */
            if (parent.getParent() instanceof ScrolledComposite == false) {
                parent.pack(false);
            }
        }

        /*
         * For any controls that have resize handlers, execute said handlers.
         */
        for (Control child : allResizedControls) {
            Runnable resizeHandler = resizeHandlersForControls.get(child);
            if (resizeHandler != null) {
                resizeHandler.run();
            }
        }

        /*
         * Iterate through the resize-aware controls, recording for each one the
         * new reentrant count for that control. The reentrant count for each is
         * decremented if it is 1 and this is a decrease, or if it is greater
         * than 1 and this is not a decrease.
         */
        for (Control control : resizeAwareControls) {
            Integer count = resizeCountsForControls.get(control);
            if (count == null) {
                continue;
            }
            if (count == 1) {
                if (decrease > 0) {
                    resizeCountsForControls.remove(control);
                }
            } else if (count > 1) {
                if (decrease < 1) {
                    resizeCountsForControls.put(control, --count);
                }
            }
        }
    }

    /**
     * Prune any widgets that are disposed from the specified set.
     * 
     * @param controls
     *            Set of controls from which to prune any widgets that are
     *            disposed.
     */
    private void pruneDisposedWidgets(Set<Control> controls) {
        for (Iterator<Control> iterator = controls.iterator(); iterator
                .hasNext();) {
            Control control = iterator.next();
            if (control.isDisposed()) {
                iterator.remove();
            }
        }
    }
}
