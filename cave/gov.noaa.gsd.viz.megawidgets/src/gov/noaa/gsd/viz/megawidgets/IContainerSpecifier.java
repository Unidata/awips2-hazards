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
 * Interface describing the methods to be implemented by a megawidget specifier
 * that is a container for other megawidget specifiers, providing configurable
 * padding and column layout for its children. Any subclasses of <code>
 * MegawidgetSpecifier</code> must implement this interface if they are to hold
 * other megawidget specifiers and offer such configurable layout options. Also,
 * any such subclasses must only produce <code>Megawidget</code> objects that
 * implement the <code>IParent</code> interface. The <code>C</code> parameter
 * indicates what type of <code>ISpecifier</code> each child specifier must be.
 * <p>
 * Note that each instance of this interface should use an instance of <code>
 * ChildSpecifiersManager</code> to manage its child specifiers.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Sep 24, 2013    2168    Chris.Golden      Added column spacing parameter,
 *                                           and changed to extend
 *                                           IParentSpecifier and to have
 *                                           the generic C parameter.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IParent
 * @see Megawidget
 * @see MegawidgetSpecifier
 * @see ChildSpecifiersManager
 */
public interface IContainerSpecifier<C extends ISpecifier> extends
        IParentSpecifier<C> {

    // Public Static Constants

    /**
     * Left margin parameter name; a megawidget may include a non-negative
     * integer associated with this name to indicate that it wishes to have this
     * many pixels as a left margin. If not specified, 0 pixels are used.
     */
    public static final String LEFT_MARGIN = "leftMargin";

    /**
     * Top margin parameter name; a megawidget may include a non-negative
     * integer associated with this name to indicate that it wishes to have this
     * many pixels as a top margin. If not specified, 0 pixels are used.
     */
    public static final String TOP_MARGIN = "topMargin";

    /**
     * Right margin parameter name; a megawidget may include a non-negative
     * integer associated with this name to indicate that it wishes to have this
     * many pixels as a right margin. If not specified, 0 pixels are used.
     */
    public static final String RIGHT_MARGIN = "rightMargin";

    /**
     * Bottom margin parameter name; a megawidget may include a non-negative
     * integer associated with this name to indicate that it wishes to have this
     * many pixels as a bottom margin. If not specified, 0 pixels are used.
     */
    public static final String BOTTOM_MARGIN = "bottomMargin";

    /**
     * Column spacing parameter name; a megawidget may include a non-negative
     * integer associated with this name to indicate that it wishes to this many
     * pixels between columns. If not specified, 15 pixels are used by default.
     */
    public static final String COLUMN_SPACING = "columnSpacing";

    /**
     * Expand to fill horizontal space parameter name; a megawidget may include
     * a boolean associated with this name to indicate whether or not the
     * container megawidget should expand to fill any available horizontal space
     * within its parent. If not specified, the megawidget is not expanded
     * horizontally.
     */
    public static final String EXPAND_HORIZONTALLY = "expandHorizontally";

    /**
     * Expand to fill vertical space parameter name; a megawidget may include a
     * boolean associated with this name to indicate whether or not the
     * container\ megawidget should expand to fill any available vertical space
     * within its parent. If not specified, the megawidget is not expanded
     * vertically.
     */
    public static final String EXPAND_VERTICALLY = "expandVertically";

    // Public Methods

    /**
     * Get the left margin.
     * 
     * @return left margin.
     */
    public int getLeftMargin();

    /**
     * Get the top margin.
     * 
     * @return Top margin.
     */
    public int getTopMargin();

    /**
     * Get the right margin.
     * 
     * @return Right margin.
     */
    public int getRightMargin();

    /**
     * Get the bottom margin.
     * 
     * @return bottom margin.
     */
    public int getBottomMargin();

    /**
     * Get the column spacing.
     * 
     * @return Column spacing.
     */
    public int getColumnSpacing();

    /**
     * Determine whether or not the megawidget is to expand to take up available
     * horizontal space within its parent.
     * 
     * @return Flag indicating whether or not the megawidget is to expand
     *         horizontally.
     */
    public boolean isHorizontalExpander();

    /**
     * Determine whether or not the megawidget is to expand to take up available
     * vertical space within its parent.
     * 
     * @return Flag indicating whether or not the megawidget is to expand
     *         vertically.
     */
    public boolean isVerticalExpander();
}