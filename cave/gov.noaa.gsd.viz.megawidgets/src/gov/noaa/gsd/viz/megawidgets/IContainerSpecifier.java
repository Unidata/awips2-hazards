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

/**
 * Interface describing the methods to be implemented by a megawidget specifier
 * that is a container for other megawidget specifiers. Any subclasses of <code>
 * MegawidgetSpecifier</code> must implement this interface if they are to hold
 * other megawidget specifiers. Also, any such subclasses must only produce
 * <code>Megawidget</code> objects that implement the <code>IContainer</code>
 * interface.
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
 * @see IContainer
 * @see Megawidget
 * @see MegawidgetSpecifier
 */
public interface IContainerSpecifier {

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

    /**
     * Megawidget specifier factory parameter name; each container widget
     * specifier must contain a reference to an
     * <code>IMegawidgetSpecifierFactory</code> object associated with this
     * name. The provided factory will be used to construct any child megawidget
     * specifiers of the container.
     */
    public static final String MEGAWIDGET_SPECIFIER_FACTORY = "widgetSpecifierFactory";

    // Public Methods

    /**
     * Get the list of all megawidget specifiers that are children of this
     * specifier.
     * 
     * @return List of child megawidget specifiers; this list must not be
     *         modified by the caller.
     */
    public List<MegawidgetSpecifier> getChildMegawidgetSpecifiers();

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

    /**
     * Get the megawidget specifier factory.
     * 
     * @return Megawidget specifier factory.
     */
    public IMegawidgetSpecifierFactory getMegawidgetSpecifierFactory();
}