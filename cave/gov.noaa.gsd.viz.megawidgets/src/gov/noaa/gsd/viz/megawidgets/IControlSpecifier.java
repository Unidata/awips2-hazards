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
 * Interface describing the methods to be implemented by a
 * {@link MegawidgetSpecifier} that creates a control-based megawidgets, that
 * is, megawidgets that manifest themselves within the body of windows. Any
 * subclasses of <code>MegawidgetSpecifier</code> must implement this interface
 * if they are to create such megawidgets.
 * <p>
 * For window-based megawidgets (those that require a <code>Composite</code> as
 * their parent), megawidgets expect that the parent is using an instance of
 * <code>GridLayout</code>.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 21, 2013    2168    Chris.Golden      Initial creation.
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IControlSpecifier extends ISpecifier {

    // Public Static Constants

    /**
     * Megawidget editable parameter name; a megawidget may include a boolean
     * value associated with this name, in order to indicate whether or not the
     * megawidget should be editable when it is first created. An editable
     * megawidget is one that contains state that may be changed, or that is
     * used potentially to modify other state. If not specified, it is assumed
     * to be <code>true</code>.
     */
    public static final String MEGAWIDGET_EDITABLE = "editable";

    /**
     * Megawidget width parameter name; a megawidget may include a value
     * associated with this name. Valid values include any positive integer less
     * than or equal to the number of columns that the parent megawidget
     * contains, or, if the parent is not a megawidget, 1. If not specified, the
     * default is 1.
     */
    public static final String MEGAWIDGET_WIDTH = "width";

    /**
     * Megawidget full width parameter name; any megawidget that allows the
     * specification of this flag may include a boolean value associated with
     * this name. True indicates that the megawidget takes up the full width of
     * its parent megawidget's column, while false indicates it may be able to
     * exist side by side with other megawidgets in the same column. Most
     * megawidget specifiers determine this based upon their class, as it is not
     * configurable, but some allow it to be included in their specification for
     * each instance. If not specified for such a megawidget, the default is
     * <code>true</code>.
     */
    public static final String MEGAWIDGET_FULL_WIDTH_OF_COLUMN = "fullWidthOfColumn";

    /**
     * Megawidget spacing parameter name; a megawidget may include a
     * non-negative integer associated with this name to indicate that it wishes
     * to be spaced by this many pixels from the megawidget above it (or if at
     * the top of the parent <code>Composite</code>, from the top of the client
     * area of the parent). If not specified, the default is 0 pixels.
     */
    public static final String MEGAWIDGET_SPACING = "spacing";

    /**
     * Megawidget parent column count parameter name; a megawidget may include a
     * positive integer associated with this name, indicating how many columns
     * the parent of the megawidget has available within which this megawidget
     * may lay itself out. If not specified, the default is 1.
     */
    public static final String MEGAWIDGET_PARENT_COLUMN_COUNT = "parentNumColumns";

    // Public Methods

    /**
     * Get the flag indicating whether or not the megawidget is to be created in
     * an editable state.
     * 
     * @return True if the megawidget is to be created as editable, false
     *         otherwise.
     */
    public boolean isEditable();

    /**
     * Get the width of the megawidget in columns within its parent.
     * 
     * @return Number of columns it should span.
     */
    public int getWidth();

    /**
     * Determine whether or not the megawidget fills the width of the column it
     * is occupying within its parent. This may be used by parent megawidgets to
     * determine whether their children may be laid out side by side in the same
     * column or not.
     * 
     * @return True if the megawidget fills the width of the column it occupies,
     *         false otherwise.
     */
    public boolean isFullWidthOfColumn();

    /**
     * Get the spacing between this megawidget and the one above it in pixels.
     * 
     * @return Spacing.
     */
    public int getSpacing();
}