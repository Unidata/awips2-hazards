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
 * Oct 23, 2013    2168    Chris.Golden      Changed to implement ISingleLineSpecifier
 *                                           and use ControlSpecifierOptionsManager
 *                                           (composition over inheritance).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TextMegawidget
 */
public class TextSpecifier extends StatefulMegawidgetSpecifier implements
        ISingleLineSpecifier {

    // Public Static Constants

    /**
     * Maximum number of characters parameter name; a megawidget must include a
     * positive integer as the value associated with this name. This specifies
     * the maximum number of characters that may be input into the text widget.
     */
    public static final String MEGAWIDGET_MAX_CHARS = "maxChars";

    /**
     * Visible characters length parameter name; a megawidget may include a
     * positive integer as the value associated with this name. This specifies
     * the maximum number of characters that should be visible at once, which is
     * to say that the megawidget text field will sized to show this many
     * average-sized characters. If not specified, the default is the same as
     * the value of <code>MEGAWIDGET_MAX_CHARS</code>.
     */
    public static final String MEGAWIDGET_VISIBLE_CHARS = "visibleChars";

    // Private Variables

    /**
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager optionsManager;

    /**
     * Flag indicating whether or not the megawidget is to expand to fill all
     * available horizontal space within its parent.
     */
    private final boolean horizontalExpander;

    /**
     * Maximum character length.
     */
    private final int maxLength;

    /**
     * Visible character length.
     */
    private final int visibleLength;

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
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                ControlSpecifierOptionsManager.BooleanSource.FALSE);

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

        // Ensure that the visible length, if present, is
        // acceptable.
        visibleLength = getSpecifierIntegerValueFromObject(
                parameters.get(MEGAWIDGET_VISIBLE_CHARS),
                MEGAWIDGET_VISIBLE_CHARS, maxLength);
        if (maxLength < 1) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_VISIBLE_CHARS, maxLength,
                    "must be positive integer");
        }

        // Get the horizontal expansion flag if available.
        horizontalExpander = getSpecifierBooleanValueFromObject(
                parameters.get(EXPAND_HORIZONTALLY), EXPAND_HORIZONTALLY, false);
    }

    // Public Methods

    @Override
    public final boolean isEditable() {
        return optionsManager.isEditable();
    }

    @Override
    public final int getWidth() {
        return optionsManager.getWidth();
    }

    @Override
    public final boolean isFullWidthOfColumn() {
        return optionsManager.isFullWidthOfColumn();
    }

    @Override
    public final int getSpacing() {
        return optionsManager.getSpacing();
    }

    /**
     * Get the maximum text length.
     * 
     * @return Maximum text length.
     */
    public final int getMaxTextLength() {
        return maxLength;
    }

    /**
     * Get the visible text length.
     * 
     * @return Visible text length.
     */
    public final int getVisibleTextLength() {
        return visibleLength;
    }

    @Override
    public final boolean isHorizontalExpander() {
        return horizontalExpander;
    }
}
