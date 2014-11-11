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

import gov.noaa.gsd.viz.megawidgets.validators.TextValidator;

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
 * Nov 04, 2013    2336    Chris.Golden      Added implementation of new superclass-
 *                                           specified abstract method, and multi-line
 *                                           option. Also changed to offer option of
 *                                           not notifying listeners of state changes
 *                                           caused by ongoing text alterations, but
 *                                           instead to save them for when the mega-
 *                                           widget loses focus.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 17, 2014   3982     Chris.Golden      Changed "isFullWidthOfColumn"
 *                                           property to "isFullWidthOfDetailPanel".
 * Aug 06, 2014   3777     Chris.Golden      Added "spellcheck" boolean property.
 * Oct 20, 2014   4818     Chris.Golden      Changed to only stretch across the full
 *                                           width of a details panel if multi-line.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TextMegawidget
 */
public class TextSpecifier extends StatefulMegawidgetSpecifier implements
        ISingleLineSpecifier, IMultiLineSpecifier,
        IRapidlyChangingStatefulSpecifier {

    // Public Static Constants

    /**
     * Maximum number of characters parameter name; a megawidget may include a
     * non-negative integer as the value associated with this name. This
     * specifies the maximum number of characters that may be input into the
     * text widget, or if <code>0</code>, indicates that there is no practical
     * limit. If not specified, the default is <code>0</code>.
     */
    public static final String MEGAWIDGET_MAX_CHARS = "maxChars";

    /**
     * Visible characters length parameter name; a megawidget may include a
     * positive integer as the value associated with this name. This specifies
     * the maximum number of characters that should be visible at once, which is
     * to say that the megawidget text field will sized to show this many
     * average-sized characters. If not specified, the default is the same as
     * the value of {@link #MEGAWIDGET_MAX_CHARS} unless the latter is <code>
     * 0</code>, in which case this defaults to <code>20</code>.
     */
    public static final String MEGAWIDGET_VISIBLE_CHARS = "visibleChars";

    /**
     * Spellcheck parameter name; a megawidget may include a boolean associated
     * with this name to indicate whether or not spellcheck should be enabled.
     * If not specified, spellcheck is disabled.
     */
    public static final String SPELLCHECK_ENABLED = "spellcheck";

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
     * Flag indicating whether or not state changes that are part of a group of
     * rapid changes are to result in notifications to the listener.
     */
    private final boolean sendingEveryChange;

    /**
     * Visible character length.
     */
    private final int visibleLength;

    /**
     * Number of lines that should be visible; if greater than <code>1</code>,
     * it is a multi-line text field.
     */
    private final int numVisibleLines;

    /**
     * Flag indicating whether or not spellcheck is to be used.
     */
    private final boolean spellCheck;

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
        super(parameters, new TextValidator(parameters, MEGAWIDGET_MAX_CHARS));

        /*
         * Ensure that the rapid change notification flag, if provided, is
         * appropriate.
         */
        sendingEveryChange = ConversionUtilities
                .getSpecifierBooleanValueFromObject(getIdentifier(), getType(),
                        parameters.get(MEGAWIDGET_SEND_EVERY_STATE_CHANGE),
                        MEGAWIDGET_SEND_EVERY_STATE_CHANGE, true);

        /*
         * Ensure that the visible lines count, if present, is acceptable, and
         * if not present is assigned a default value.
         */
        numVisibleLines = ConversionUtilities
                .getSpecifierIntegerValueFromObject(getIdentifier(), getType(),
                        parameters.get(MEGAWIDGET_VISIBLE_LINES),
                        MEGAWIDGET_VISIBLE_LINES, 1);
        if (numVisibleLines < 1) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_VISIBLE_LINES, numVisibleLines,
                    "must be positive integer");
        }

        /*
         * Create the options manager, now that the number of visible lines is
         * known.
         */
        optionsManager = new ControlSpecifierOptionsManager(
                this,
                parameters,
                (numVisibleLines > 1 ? ControlSpecifierOptionsManager.BooleanSource.TRUE
                        : ControlSpecifierOptionsManager.BooleanSource.FALSE));

        /*
         * Ensure that the visible length, if present, is acceptable.
         */
        TextValidator validator = getStateValidator();
        visibleLength = ConversionUtilities.getSpecifierIntegerValueFromObject(
                getIdentifier(),
                getType(),
                parameters.get(MEGAWIDGET_VISIBLE_CHARS),
                MEGAWIDGET_VISIBLE_CHARS,
                (validator.getMaxCharacters() == 0 ? 20 : validator
                        .getMaxCharacters()));
        if (visibleLength < 1) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_VISIBLE_CHARS,
                    validator.getMaxCharacters(), "must be positive integer");
        }

        /*
         * Get the horizontal expansion flag if available.
         */
        horizontalExpander = ConversionUtilities
                .getSpecifierBooleanValueFromObject(getIdentifier(), getType(),
                        parameters.get(EXPAND_HORIZONTALLY),
                        EXPAND_HORIZONTALLY, false);

        /*
         * Ensure that the spellcheck flag, if provided, is appropriate.
         */
        spellCheck = ConversionUtilities.getSpecifierBooleanValueFromObject(
                getIdentifier(), getType(), parameters.get(SPELLCHECK_ENABLED),
                SPELLCHECK_ENABLED, false);
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
    public final boolean isFullWidthOfDetailPanel() {
        return optionsManager.isFullWidthOfDetailPanel();
    }

    @Override
    public final int getSpacing() {
        return optionsManager.getSpacing();
    }

    @Override
    public final boolean isSendingEveryChange() {
        return sendingEveryChange;
    }

    /**
     * Get the maximum text length.
     * 
     * @return Maximum text length.
     */
    public final int getMaxTextLength() {
        return ((TextValidator) getStateValidator()).getMaxCharacters();
    }

    /**
     * Get the visible text length.
     * 
     * @return Visible text length.
     */
    public final int getVisibleTextLength() {
        return visibleLength;
    }

    /**
     * Determine whether or not spellcheck is enabled.
     * 
     * @return True if spellcheck is enabled, false otherwise.
     */
    public final boolean isSpellCheck() {
        return spellCheck;
    }

    @Override
    public final boolean isHorizontalExpander() {
        return horizontalExpander;
    }

    @Override
    public final int getNumVisibleLines() {
        return numVisibleLines;
    }
}
