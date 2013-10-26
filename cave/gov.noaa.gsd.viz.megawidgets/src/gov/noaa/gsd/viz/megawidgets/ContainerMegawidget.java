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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Container megawidget, a megawidget that itself contains other megawidgets.
 * Examples of concrete subclasses might include a panel or a tabbed folder.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013    1277    Chris.Golden      Added support for mutable
 *                                           properties.
 * Sep 24, 2013    2168    Chris.Golden      Replaced duplicated code to
 *                                           accomplish megawidget element
 *                                           alignment with call to existing
 *                                           superclass method, and implemented
 *                                           new IControl interface.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see ContainerMegawidgetSpecifier
 */
public abstract class ContainerMegawidget extends Megawidget implements
        IContainer<IControl>, IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = Sets.newHashSet(Megawidget.MUTABLE_PROPERTY_NAMES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Protected Variables

    /**
     * Composite.
     */
    protected Composite composite = null;

    /**
     * List of child widgets.
     */
    protected List<IControl> children = null;

    /**
     * Control component helper.
     */
    private final ControlComponentHelper helper;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     */
    protected ContainerMegawidget(
            ContainerMegawidgetSpecifier<IControlSpecifier> specifier) {
        super(specifier);
        helper = new ControlComponentHelper(specifier);
    }

    // Public Methods

    @Override
    public Set<String> getMutablePropertyNames() {
        return MUTABLE_PROPERTY_NAMES;
    }

    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            return isEditable();
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            setEditable(getPropertyBooleanValueFromObject(value, name, null));
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public final boolean isEditable() {
        return helper.isEditable();
    }

    @Override
    public final void setEditable(boolean editable) {
        helper.setEditable(editable);
    }

    @Override
    public final int getLeftDecorationWidth() {
        return 0;
    }

    @Override
    public final void setLeftDecorationWidth(int width) {

        // No action.
    }

    @Override
    public final int getRightDecorationWidth() {
        return 0;
    }

    @Override
    public final void setRightDecorationWidth(int width) {

        // No action.
    }

    @Override
    public final List<IControl> getChildren() {
        return Lists.newArrayList(children);
    }

    // Protected Methods

    @Override
    protected final void doSetEnabled(boolean enable) {
        composite.setEnabled(enable);
    }

    /**
     * Grid the specified container panel so that it behaves as requested by the
     * values specified for its layout when the specifier was constructed. This
     * method must be invoked by subclasses when configuring the composite
     * serving as the parent for any and all child megawidgets.
     * 
     * @param composite
     *            Container panel to be gridded.
     */
    protected final void gridContainerPanel(Composite composite) {

        // Place the widget in the grid.
        @SuppressWarnings("unchecked")
        ContainerMegawidgetSpecifier<IControlSpecifier> specifier = (ContainerMegawidgetSpecifier<IControlSpecifier>) getSpecifier();
        GridData gridData = new GridData(SWT.FILL, SWT.FILL,
                specifier.isHorizontalExpander(),
                specifier.isVerticalExpander());
        gridData.horizontalSpan = specifier.getWidth();
        gridData.verticalIndent = specifier.getSpacing();
        composite.setLayoutData(gridData);
    }

    /**
     * Create the GUI components making up the specified container megawidget's
     * children, as well as properly configuring the specified container
     * megawidget's layout. This method must be invoked by subclasses when
     * filling in a panel with GUI representations of child megawidgets.
     * 
     * @param composite
     *            Composite into which to place the child megawidgets' GUI
     *            representations.
     * @param columnCount
     *            Number of columns that the layout must provide to its
     *            children.
     * @param childMegawidgetSpecifiers
     *            List of child megawidgets for which GUI components are to be
     *            created and placed within the composite.
     * @param creationParams
     *            Hash table mapping identifiers to values that child megawidget
     *            specifiers might require when creating megawidgets.
     * @return List of child megawidgets created.
     * @throws MegawidgetException
     *             If an error occurs while creating or initializing child
     *             megawidgets.
     */
    protected final List<IControl> createChildMegawidgets(Composite composite,
            int columnCount,
            List<? extends IControlSpecifier> childMegawidgetSpecifiers,
            Map<String, Object> creationParams) throws MegawidgetException {

        // Create the layout manager for the composite, and
        // configure it.
        @SuppressWarnings("unchecked")
        ContainerMegawidgetSpecifier<IControlSpecifier> specifier = (ContainerMegawidgetSpecifier<IControlSpecifier>) getSpecifier();
        GridLayout layout = new GridLayout(columnCount, false);
        layout.marginWidth = layout.marginHeight = 0;
        layout.marginLeft = specifier.getLeftMargin();
        layout.marginTop = specifier.getTopMargin();
        layout.marginRight = specifier.getRightMargin();
        layout.marginBottom = specifier.getBottomMargin();
        layout.horizontalSpacing = specifier.getColumnSpacing();
        composite.setLayout(layout);

        // Create the child megawidgets, align their elements, and
        // return the former.
        List<IControl> childMegawidgets = Lists.newArrayList();
        for (IControlSpecifier childSpecifier : childMegawidgetSpecifiers) {
            childMegawidgets.add(childSpecifier.createMegawidget(composite,
                    IControl.class, creationParams));
        }
        ControlComponentHelper.alignMegawidgetsElements(childMegawidgets);
        return childMegawidgets;
    }
}