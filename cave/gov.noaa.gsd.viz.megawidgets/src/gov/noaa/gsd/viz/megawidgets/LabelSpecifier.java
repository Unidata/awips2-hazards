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
 * Label megawidget specifier.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Sep 26, 2013   2168     Chris.Golden      Made wrapping an option instead
 *                                           of standard, to improve
 *                                           compatibility with multicolumn
 *                                           layouts, and changed to implement
 *                                           IControlSpecifier and use
 *                                           ControlSpecifierOptionsManager
 *                                           (composition over inheritance).
 * Nov 04, 2013    2336    Chris.Golden      Added bold and italic options.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see LabelMegawidget
 */
public class LabelSpecifier extends MegawidgetSpecifier implements
        IControlSpecifier {

    // Public Static Constants

    /**
     * Wrap flag parameter name; a megawidget may include a boolean value
     * associated with this name to indicate whether or not the label text
     * should stretch to use the full space of its parent, and wrap when it is
     * too long. If this parameter is not specified, the default value is <code>
     * false</code>.
     */
    public static final String LABEL_WRAP = "wrap";

    /**
     * Bold flag parameter name; a megawidget may include a boolean value
     * associated with this name to indicate whether or not the label text will
     * be rendered using a bold font. If this parameter is not specified, the
     * default value is <code>false</code>.
     */
    public static final String LABEL_BOLD = "bold";

    /**
     * Italic flag parameter name; a megawidget may include a boolean value
     * associated with this name to indicate whether or not the label text will
     * be rendered using an italic font. If this parameter is not specified, the
     * default value is <code>false</code>.
     */
    public static final String LABEL_ITALIC = "italic";

    // Private Variables

    /**
     * Flag indicating whether or not wrapping should occur.
     */
    private final boolean wrap;

    /**
     * Flag indicating whether or not a bold font is to be used.
     */
    private final boolean bold;

    /**
     * Flag indicating whether or not an italic font is to be used.
     */
    private final boolean italic;

    /**
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager optionsManager;

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
    public LabelSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                ControlSpecifierOptionsManager.BooleanSource.FALSE);

        /*
         * Ensure that the wrap flag, if present, is acceptable.
         */
        wrap = ConversionUtilities.getSpecifierBooleanValueFromObject(
                getIdentifier(), getType(), parameters.get(LABEL_WRAP),
                LABEL_WRAP, false);

        /*
         * Ensure that the bold and italic flags, if present, are acceptable.
         */
        bold = ConversionUtilities.getSpecifierBooleanValueFromObject(
                getIdentifier(), getType(), parameters.get(LABEL_BOLD),
                LABEL_BOLD, false);
        italic = ConversionUtilities.getSpecifierBooleanValueFromObject(
                getIdentifier(), getType(), parameters.get(LABEL_ITALIC),
                LABEL_ITALIC, false);
    }

    // Public Methods

    /**
     * Determine whether or not the text should use the full width of its
     * parent, and wrap when it is too long to fit within a single line.
     * 
     * @return Flag indicating whether or not the text should wrap.
     */
    public boolean isToWrap() {
        return wrap;
    }

    /**
     * Determine whether or not the text should be rendered in a bold font.
     * 
     * @return Flag indicating whether or not the text should be bold.
     */
    public boolean isBold() {
        return bold;
    }

    /**
     * Determine whether or not the text should be rendered in an italic font.
     * 
     * @return Flag indicating whether or not the text should be italic.
     */
    public boolean isItalic() {
        return italic;
    }

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
}
