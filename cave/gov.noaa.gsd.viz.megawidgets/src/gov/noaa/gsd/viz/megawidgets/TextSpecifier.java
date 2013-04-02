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
 * Text megawidget specifier, providing the specification of a text editing
 * megawidget that allows the manipulation of a text string. The string is
 * always associated with a single state identifier, so the megawidget
 * identifiers for these specifiers must not consist of colon-separated
 * substrings.
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
 * @see TextMegawidget
 */
public class TextSpecifier extends StatefulMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Expand to fill horizontal space parameter name; a megawidget may include
     * a boolean associated with this name to indicate whether or not the
     * container megawidget should expand to fill any available horizontal space
     * within its parent. If not specified, the megawidget is not expanded
     * horizontally.
     */
    public static final String EXPAND_HORIZONTALLY = "expandHorizontally";

    /**
     * Maximum number of characters parameter name; a megawidget must include a
     * positive integer as the value associated with this name. This specifies
     * the maximum number of characters that may be input into the text widget.
     */
    public static final String MEGAWIDGET_MAX_CHARS = "length";

    // Private Variables

    /**
     * Flag indicating whether or not the megawidget is to expand to fill all
     * available horizontal space within its parent.
     */
    private final boolean horizontalExpander;

    /**
     * Maximum character length.
     */
    private final int maxLength;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public TextSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);

        // Ensure that the maximum length is present and
        // acceptable.
        maxLength = getSpecifierIntegerValueFromObject(
                parameters.get(MEGAWIDGET_MAX_CHARS), MEGAWIDGET_MAX_CHARS,
                null);
        if (maxLength < 1) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_MAX_CHARS, maxLength,
                    "must be positive integer");
        }

        // Get the horizontal expansion flag if available.
        horizontalExpander = getSpecifierBooleanValueFromObject(
                parameters.get(EXPAND_HORIZONTALLY), EXPAND_HORIZONTALLY, false);
    }

    // Public Methods

    /**
     * Get the maximum text length.
     * 
     * @return Maximum text length.
     */
    public final int getMaxTextLength() {
        return maxLength;
    }

    /**
     * Determine whether or not the megawidget is to expand to take up available
     * horizontal space within its parent.
     * 
     * @return Flag indicating whether or not the megawidget is to expand
     *         horizontally.
     */
    public final boolean isHorizontalExpander() {
        return horizontalExpander;
    }
}
