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

/**
 * Description: Manager of any options associated with megawidget specifiers
 * that implement @{link IContainerSpecifier}. This class may be used by such
 * classes to handle the setting and getting of such options. The <code>C</code>
 * parameter indicates what type of {@link IControlSpecifier} each child
 * specifier must be.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 23, 2014    4122    Chris.Golden Initial creation (extracted from
 *                                      ContainerMegawidgetSpecifier to allow
 *                                      reuse in DetailedComboBoxSpecifier).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ContainerSpecifierOptionsManager<C extends IControlSpecifier> {

    // Private Static Constants

    /**
     * Array of margins in the order they are specified in the {@link #margins}
     * member variable.
     */
    private static final String[] MARGIN_NAMES = {
            IContainerSpecifier.LEFT_MARGIN, IContainerSpecifier.TOP_MARGIN,
            IContainerSpecifier.RIGHT_MARGIN, IContainerSpecifier.BOTTOM_MARGIN };

    /**
     * Array of expand flags in the order they are specified in the
     * {@link #expander} member variable.
     */
    private static final String[] EXPANDER_NAMES = {
            IContainerSpecifier.EXPAND_HORIZONTALLY,
            IContainerSpecifier.EXPAND_VERTICALLY };

    // Private Variables

    /**
     * Margins in pixels, the first being left, the second being top, the third
     * right, and the fourth bottom.
     */
    private final int[] margins = new int[4];

    /**
     * Column spacing in pixels.
     */
    private final int columnSpacing;

    /**
     * Expander flags, indicating whether the megawidget should expand to fill
     * available space horizontally (for the first) and vertically (for the
     * second).
     */
    private final boolean[] expander = new boolean[2];

    // Public Constructors

    /**
     * Construct a standard instance to manage the container-related options of
     * the provided specifier.
     * 
     * @param specifier
     *            Specifier for which to manage the container-related options.
     * @param superClass
     *            Class that must be the superclass of any child megawidget
     *            specifier.
     * @param parameters
     *            Map containing the parameters to be used to construct the
     *            megawidget specifier, including mappings for the options
     *            needed for the container aspect of said specifier.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public ContainerSpecifierOptionsManager(MegawidgetSpecifier specifier,
            Class<C> superClass, Map<String, Object> parameters)
            throws MegawidgetSpecificationException {

        /*
         * Ensure that the margins, if present, are acceptable.
         */
        for (int j = 0; j < MARGIN_NAMES.length; j++) {
            margins[j] = ConversionUtilities
                    .getSpecifierIntegerValueFromObject(
                            specifier.getIdentifier(), specifier.getType(),
                            parameters.get(MARGIN_NAMES[j]), MARGIN_NAMES[j], 0);
        }

        /*
         * Ensure that the column spacing, if present, is acceptable.
         */
        columnSpacing = ConversionUtilities.getSpecifierIntegerValueFromObject(
                specifier.getIdentifier(), specifier.getType(),
                parameters.get(IContainerSpecifier.COLUMN_SPACING),
                IContainerSpecifier.COLUMN_SPACING, 15);

        /*
         * Ensure that the expand flags, if present, are acceptable.
         */
        for (int j = 0; j < EXPANDER_NAMES.length; j++) {
            expander[j] = ConversionUtilities
                    .getSpecifierBooleanValueFromObject(
                            specifier.getIdentifier(), specifier.getType(),
                            parameters.get(EXPANDER_NAMES[j]),
                            EXPANDER_NAMES[j], false);
        }
    }

    // Public Methods

    /**
     * Get the left margin.
     * 
     * @return left margin.
     */
    public final int getLeftMargin() {
        return margins[0];
    }

    /**
     * Get the top margin.
     * 
     * @return top margin.
     */
    public final int getTopMargin() {
        return margins[1];
    }

    /**
     * Get the right margin.
     * 
     * @return right margin.
     */
    public final int getRightMargin() {
        return margins[2];
    }

    /**
     * Get the bottom margin.
     * 
     * @return bottom margin.
     */
    public final int getBottomMargin() {
        return margins[3];
    }

    /**
     * Get the column spacing.
     * 
     * @return Column spacing.
     */
    public final int getColumnSpacing() {
        return columnSpacing;
    }

    /**
     * Determine whether or not the megawidget is to expand to take up available
     * horizontal space within its parent.
     * 
     * @return Flag indicating whether or not the megawidget is to expand
     *         horizontally.
     */
    public final boolean isHorizontalExpander() {
        return expander[0];
    }

    /**
     * Determine whether or not the megawidget is to expand to take up available
     * vertical space within its parent.
     * 
     * @return Flag indicating whether or not the megawidget is to expand
     *         vertically.
     */
    public final boolean isVerticalExpander() {
        return expander[1];
    }
}
