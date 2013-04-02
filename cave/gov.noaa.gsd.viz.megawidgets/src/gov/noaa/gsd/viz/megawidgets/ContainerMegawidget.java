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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

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
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see ContainerMegawidgetSpecifier
 */
public class ContainerMegawidget extends Megawidget implements IContainer {

    // Protected Variables

    /**
     * Composite.
     */
    protected Composite composite = null;

    /**
     * List of child widgets.
     */
    protected List<Megawidget> children = null;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     */
    protected ContainerMegawidget(MegawidgetSpecifier specifier) {
        super(specifier);
    }

    // Public Methods

    /**
     * Get the list of child megawidgets of this megawidget.
     * 
     * @return List of child megawidgets of this megawidget. The list must not
     *         be modified by the caller.
     */
    @Override
    public final List<Megawidget> getChildren() {
        return children;
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
    @Override
    protected final void doSetEnabled(boolean enable) {
        composite.setEnabled(enable);
    }

    /**
     * Change the component widgets to ensure their state matches that of the
     * editable flag.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     */
    @Override
    protected final void doSetEditable(boolean editable) {

        // No action.
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
        ContainerMegawidgetSpecifier specifier = (ContainerMegawidgetSpecifier) getSpecifier();
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
     * @param childWidgetSpecifiers
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
    protected final List<Megawidget> createChildMegawidgets(
            Composite composite, int columnCount,
            List<MegawidgetSpecifier> childWidgetSpecifiers,
            Map<String, Object> creationParams) throws MegawidgetException {

        // Create the layout manager for the composite, and
        // configure it.
        ContainerMegawidgetSpecifier specifier = (ContainerMegawidgetSpecifier) getSpecifier();
        GridLayout layout = new GridLayout(columnCount, false);
        layout.marginWidth = layout.marginHeight = 0;
        layout.marginLeft = specifier.getLeftMargin();
        layout.marginTop = specifier.getTopMargin();
        layout.marginRight = specifier.getRightMargin();
        layout.marginBottom = specifier.getBottomMargin();
        layout.horizontalSpacing = 15;
        composite.setLayout(layout);

        // Create the child widgets and return the list of
        // them.
        List<Megawidget> childWidgets = new ArrayList<Megawidget>();
        int maxLeftDecorationWidth = 0, maxRightDecorationWidth = 0;
        for (MegawidgetSpecifier childSpecifier : childWidgetSpecifiers) {
            Megawidget megawidget = childSpecifier.createMegawidget(composite,
                    creationParams);
            int leftDecorationWidth = megawidget.getLeftDecorationWidth();
            if (leftDecorationWidth > maxLeftDecorationWidth) {
                maxLeftDecorationWidth = leftDecorationWidth;
            }
            int rightDecorationWidth = megawidget.getRightDecorationWidth();
            if (rightDecorationWidth > maxRightDecorationWidth) {
                maxRightDecorationWidth = rightDecorationWidth;
            }
            childWidgets.add(megawidget);
        }
        for (Megawidget megawidget : childWidgets) {
            megawidget.setLeftDecorationWidth(maxLeftDecorationWidth);
            megawidget.setRightDecorationWidth(maxRightDecorationWidth);
        }
        return childWidgets;
    }
}