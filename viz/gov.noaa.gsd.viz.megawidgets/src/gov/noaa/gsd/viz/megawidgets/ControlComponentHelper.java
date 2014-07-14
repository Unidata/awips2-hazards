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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Description: Helper class for handling some of the grunt work of configuring
 * the size and color of the components of {@link IControl} megawidgets.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 23, 2013    2168    Chris.Golden      Initial creation.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ControlComponentHelper {

    // Private Variables

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
            Collection<? extends IControl> megawidgets) {

        /*
         * Determine which megawidgets have, respectively, the largest left and
         * right decoration widths, and set all the megawidgets to have those
         * widths for their respective left and right decorations.
         */
        int maxLeftDecorationWidth = 0, maxRightDecorationWidth = 0;
        for (IControl megawidget : megawidgets) {
            int leftDecorationWidth = megawidget.getLeftDecorationWidth();
            if (leftDecorationWidth > maxLeftDecorationWidth) {
                maxLeftDecorationWidth = leftDecorationWidth;
            }
            int rightDecorationWidth = megawidget.getRightDecorationWidth();
            if (rightDecorationWidth > maxRightDecorationWidth) {
                maxRightDecorationWidth = rightDecorationWidth;
            }
        }
        for (IControl megawidget : megawidgets) {
            megawidget.setLeftDecorationWidth(maxLeftDecorationWidth);
            megawidget.setRightDecorationWidth(maxRightDecorationWidth);
        }
    }

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public ControlComponentHelper(IControlSpecifier specifier) {
        editable = specifier.isEditable();
    }

    // Public Methods

    /**
     * Determine whether or not the megawidget is currently editable.
     * 
     * @return True if the megawidget is currently editable, false otherwise.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Render the megawidget editable or read-only.
     * 
     * @param editable
     *            Flag indicating whether the megawidget is to be editable or
     *            read-only.
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

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
    public Color getBackgroundColor(boolean editable, Control control,
            Label label) {

        /*
         * If the colors have not yet been cached, fetch them now. These colors
         * do not need to be disposed of upon widget destruction, since they are
         * being provided by stock SWT widgets and are therefore system colors.
         */
        if (editableBackgroundColor == null) {
            editableBackgroundColor = control.getBackground();
            if (label != null) {
                readOnlyBackgroundColor = label.getBackground();
            } else {
                Label temporaryLabel = new Label(control.getParent(), SWT.NONE);
                readOnlyBackgroundColor = temporaryLabel.getBackground();
                temporaryLabel.dispose();
            }
        }

        /*
         * Return the appropriate color.
         */
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
    public int getWidestWidgetWidth(Control... widgets) {
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
    public void setWidgetsWidth(int width, Control... widgets) {
        for (Control widget : widgets) {
            ((GridData) widget.getLayoutData()).widthHint = width;
        }
    }
}
