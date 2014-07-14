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

/**
 * Interface describing the methods to be implemented by a {@link IMegawidget}
 * that is composed of controls, that is, a megawidget that manifests itself
 * within the body of windows.
 * <p>
 * All such megawidgets expect that the parent is using an instance of
 * {@link org.eclipse.swt.layout.GridLayout}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013    2168    Chris.Golden      Initial creation.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IControl extends IMegawidget {

    // Public Methods

    /**
     * Determine whether or not the megawidget is currently editable.
     * 
     * @return True if the megawidget is currently editable, false otherwise.
     */
    public boolean isEditable();

    /**
     * Render the megawidget editable or read-only.
     * 
     * @param editable
     *            Flag indicating whether the megawidget is to be editable or
     *            read-only.
     */
    public void setEditable(boolean editable);

    /**
     * Determine the left decoration width for this megawidget, if the
     * megawidget has a decoration (label, etc.) to the left of its main
     * component widget(s). This is used to query all sibling megawidgets to
     * determine which one has the largest left decoration.
     * 
     * @return Width in pixels required for the left decoration of this
     *         megawidget, or 0 if the megawidget has no left decoration.
     */
    public int getLeftDecorationWidth();

    /**
     * Set the left decoration width for this megawidget to that specified, if
     * the megawidget has a decoration to the left of its main component
     * widget(s). This is used to set sibling megawidgets to all use the width
     * of the largest left decoration used by the siblings, if any.
     * 
     * @param width
     *            Width to be used if this megawidget has a left decoration.
     */
    public void setLeftDecorationWidth(int width);

    /**
     * Determine the right decoration width for this megawidget, if the
     * megawidget has a decoration (label, etc.) to the right of its main
     * component widget(s). This is used to query all sibling megawidgets to
     * determine which one has the largest right decoration.
     * 
     * @return Width in pixels required for the right decoration of this
     *         megawidget, or 0 if the megawidget has no right decoration.
     */
    public int getRightDecorationWidth();

    /**
     * Set the right decoration width for this megawidget to that specified, if
     * the megawidget has a decoration to the right of its main component
     * widget(s). This is used to set sibling megawidgets to all use the width
     * of the largest right decoration used by the siblings, if any.
     * 
     * @param width
     *            Width to be used if this megawidget has a right decoration.
     */
    public void setRightDecorationWidth(int width);
}