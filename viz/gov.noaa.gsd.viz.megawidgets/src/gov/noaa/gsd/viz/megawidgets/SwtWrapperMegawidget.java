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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * Description: Megawidget that acts as a wrapper for non-megawidget widgets
 * (user interface elements).
 * <p>
 * TODO: Consider making this non-SWT-specific. However, this should be part of
 * separating megawidget specifiers and abstract, non-widget-toolkit-specific
 * megawidget classes out from SWT-based megawidget subclasses of the latter,
 * which is not a small job.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 19, 2014    4098    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SwtWrapperMegawidget extends Megawidget implements IResizer,
        IControl {

    // Private Variables

    /**
     * Composite acting as the wrapper (container) for widgets.
     */
    private final Composite composite;

    /**
     * Resize listener.
     */
    private final IResizeListener resizeListener;

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
     * @param parent
     *            Parent of the megawidget.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    protected SwtWrapperMegawidget(SwtWrapperSpecifier specifier,
            Composite parent, Map<String, Object> paramMap)
            throws MegawidgetException {
        super(specifier);
        helper = new ControlComponentHelper(specifier);
        resizeListener = (IResizeListener) paramMap.get(RESIZE_LISTENER);

        /*
         * Create the composite that will act as the wrapper (container), and
         * grid it.
         */
        composite = new Composite(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL,
                specifier.isHorizontalExpander(),
                specifier.isVerticalExpander());
        gridData.horizontalSpan = specifier.getWidth();
        gridData.verticalIndent = specifier.getSpacing();
        composite.setLayoutData(gridData);
    }

    // Public Methods

    /**
     * Get the composite acting as the wrapper (container).
     * <p>
     * <strong>NOTE</strong>: The composite that is returned must not be
     * disposed of, have its layout data changed by modifying the object
     * returned by {@link Control#getLayoutData()} or by using
     * {@link Control#setLayoutData(Object)}, or have its parent or other
     * ancestors manipulated in any way. It will fill its parent in both
     * dimensions, that is, {@link SWT#FILL} for its horizontal and vertical
     * alignment, and will expand into extra space given by its parent
     * horizontally and/or vertically if its specifier indicates that it should
     * do so. This method should only be used to add children to, or remove them
     * from, the composite, or to set or configure its {@link Layout}. Any such
     * changes that could resize the wrapper directly or indirectly must be
     * followed by an invocation of {@link #sizeChanged()}.
     * </p>
     * 
     * @return Composite acting as the wrapper.
     */
    public Composite getWrapperComposite() {
        return composite;
    }

    /**
     * Notify the megawidget that the wrapper composite's size may have changed.
     * This must be called whenever the composite returned by
     * {@link #getWrapperComposite()} experiences a size change, including
     * changes to the sizes of any children it has, or to their sizes.
     */
    public void sizeChanged() {
        if (resizeListener != null) {
            resizeListener.sizeChanged(this);
        }
    }

    @Override
    public boolean isEditable() {
        return helper.isEditable();
    }

    @Override
    public void setEditable(boolean editable) {
        helper.setEditable(editable);
    }

    @Override
    public int getLeftDecorationWidth() {
        return 0;
    }

    @Override
    public void setLeftDecorationWidth(int width) {

        /*
         * No action.
         */
    }

    @Override
    public int getRightDecorationWidth() {
        return 0;
    }

    @Override
    public void setRightDecorationWidth(int width) {

        /*
         * No action.
         */
    }

    // Protected Methods

    @Override
    protected void doSetEnabled(boolean enable) {

        /*
         * No action.
         */
    }
}
