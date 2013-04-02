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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

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
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see MegawidgetSpecifier
 */
public abstract class Megawidget implements IMegawidget {

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
    public final <W extends MegawidgetSpecifier> W getSpecifier() {
        return (W) specifier;
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
}