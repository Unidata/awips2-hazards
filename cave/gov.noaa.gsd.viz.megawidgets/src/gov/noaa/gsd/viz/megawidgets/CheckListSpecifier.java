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
 * Checklist megawidget specifier.
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
 * @see CheckListMegawidget
 */
public class CheckListSpecifier extends ChoicesMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Megawidget number of visible lines parameter name; a megawidget may
     * include a positive integer associated with this name to indicate that it
     * wishes to have this number of rows visible at once. If not specified, the
     * number of visible lines is assumed to be 6.
     */
    public static final String MEGAWIDGET_VISIBLE_LINES = "lines";

    /**
     * Megawidget include-select-all/select-none-buttons parameter name; a
     * megawidget may include a boolean value associated with this name to
     * indicate whether or not it wishes to have All and None buttons included
     * to allow the user to easily select or deselect all the items in the check
     * list. If not specified, it is assumed to be true.
     */
    public static final String MEGAWIDGET_SHOW_ALL_NONE_BUTTONS = "showAllNoneButtons";

    // Private Variables

    /**
     * Number of lines that should be visible.
     */
    private final int numVisibleLines;

    /**
     * Flag indicating whether or not the All and None buttons should be
     * included.
     */
    private final boolean showAllNoneButtons;

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
    public CheckListSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);

        // Ensure that the visible lines count, if present,
        // is acceptable, and if not present is assigned a
        // default value.
        numVisibleLines = getSpecifierIntegerValueFromObject(
                parameters.get(MEGAWIDGET_VISIBLE_LINES),
                MEGAWIDGET_VISIBLE_LINES, 6);
        if (numVisibleLines < 1) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_VISIBLE_LINES, numVisibleLines,
                    "must be positive integer");
        }

        // Record the value of the show all/none buttons
        // flag.
        showAllNoneButtons = getSpecifierBooleanValueFromObject(
                parameters.get(MEGAWIDGET_SHOW_ALL_NONE_BUTTONS),
                MEGAWIDGET_SHOW_ALL_NONE_BUTTONS, true);
    }

    // Public Methods

    /**
     * Get the number of visible lines.
     * 
     * @return Number of visible lines.
     */
    public final int getNumVisibleLines() {
        return numVisibleLines;
    }

    /**
     * Determine whether or not the All and None buttons should be shown.
     * 
     * @return Flag indicating whether or not the All and None buttons should be
     *         shown.
     */
    public final boolean shouldShowAllNoneButtons() {
        return showAllNoneButtons;
    }
}
