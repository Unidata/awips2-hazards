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

import com.raytheon.uf.common.colormap.Color;

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
 * Jun 17, 2014    3982    Chris.Golden      Changed "isFullWidthOfColumn"
 *                                           property to "isFullWidthOfDetailPanel".
 * Oct 10, 2014    4042    Chris.Golden      Added "preferredWidth" parameter.
 * Jun 07, 2016   19464    Chris.Golden      Added "color" parameter.
 * Dec 06, 2016   26855    Chris.Golden      Changed so that the preferred width
 *                                           creation time parameter is not allowed
 *                                           to be specified if the specification
 *                                           does not call for wrapping.
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
     * Preferred width parameter name; a megawidget may include a positive
     * integer associated with this name to indicate what the preferred width
     * (measured in average character width for the font the label is using) is.
     * If provided, the megawidget will request that its parent be that wide if
     * possible; if not, it will request that the parent be wide enough to show
     * all of its text. This is intended to be used only when
     * {@link #LABEL_WRAP} is <code>true</code>; if specified when the latter is
     * <code>false</code>, it will generate an error.
     */
    public static final String LABEL_PREFERRED_WIDTH = "preferredWidth";

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

    /**
     * Color parameter name; a megawidget may include a map holding "red",
     * "green", and "blue" values ranging between <code>0.0</code> and
     * <code>1.0</code> inclusive. The specified color is used to render the
     * text that is part of the label. If not specified, the default value is
     * the default label color.
     */
    public static final String LABEL_COLOR = "color";

    // Private Static Constants

    /**
     * Color object used as a placeholder for the default color.
     */
    private static final Color DEFAULT_COLOR = new Color();

    // Private Variables

    /**
     * Flag indicating whether or not wrapping should occur.
     */
    private final boolean wrap;

    /**
     * Preferred width (in average character width); if 0, it is assumed to be
     * the length of the text.
     */
    private final int preferredWidth;

    /**
     * Flag indicating whether or not a bold font is to be used.
     */
    private final boolean bold;

    /**
     * Flag indicating whether or not an italic font is to be used.
     */
    private final boolean italic;

    /**
     * Color of the label holding values, or <code>null</code> if an empty map
     * if the default color is to be used.
     */
    private final Color color;

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
         * Ensure that the preferred width, if present, is acceptable.
         */
        int preferredWidth = ConversionUtilities
                .getSpecifierIntegerValueFromObject(getIdentifier(), getType(),
                        parameters.get(LABEL_PREFERRED_WIDTH),
                        LABEL_PREFERRED_WIDTH, 0);
        if (preferredWidth < 0) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), LABEL_PREFERRED_WIDTH, preferredWidth,
                    "must be positive integer");
        } else if ((preferredWidth > 0) && (wrap == false)) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), LABEL_PREFERRED_WIDTH, preferredWidth,
                    "cannot be specified when " + LABEL_WRAP + " is false");
        }
        this.preferredWidth = preferredWidth;

        /*
         * Ensure that the bold and italic flags, if present, are acceptable.
         */
        bold = ConversionUtilities.getSpecifierBooleanValueFromObject(
                getIdentifier(), getType(), parameters.get(LABEL_BOLD),
                LABEL_BOLD, false);
        italic = ConversionUtilities.getSpecifierBooleanValueFromObject(
                getIdentifier(), getType(), parameters.get(LABEL_ITALIC),
                LABEL_ITALIC, false);

        /*
         * Ensure that the color, if present, is acceptable.
         */
        Color color = ConversionUtilities.getSpecifierColorFromObject(
                getIdentifier(), getType(), parameters.get(LABEL_COLOR),
                LABEL_COLOR, DEFAULT_COLOR);
        this.color = (color == DEFAULT_COLOR ? null : color);
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
     * Get the preferred width (in units equivalent to average character size).
     * If 0, the preferred width is assumed to be the length of the text.
     * 
     * @return Preferred width.
     */
    public int getPreferredWidth() {
        return preferredWidth;
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

    /**
     * Get the color of the label, or <code>null</code> if the default color is
     * to be used.
     * 
     * @return Color or <code>null</code> if the default color is to be used.
     */
    public Color getColor() {
        return color;
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
    public final boolean isFullWidthOfDetailPanel() {
        return optionsManager.isFullWidthOfDetailPanel();
    }

    @Override
    public final int getSpacing() {
        return optionsManager.getSpacing();
    }
}
