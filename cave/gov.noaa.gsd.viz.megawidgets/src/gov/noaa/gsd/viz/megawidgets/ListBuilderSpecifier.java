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
 * List builder megawidget specifier, used to create megawidgets that allow the
 * user to build up an orderable list from a set of choices.
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
 * @see ListBuilderMegawidget
 */
public class ListBuilderSpecifier extends ChoicesMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Megawidget selected items label parameter name; a megawidget may include
     * a value associated with this name, in which case it will be used to label
     * the selected items list. (The <code>MEGAWIDGET_LABEL</code> value is used
     * to label the available items list.) Any string is valid as a value.
     */
    public static final String MEGAWIDGET_SELECTED_LABEL = "selectedLabel";

    /**
     * Megawidget number of visible lines parameter name; a megawidget may
     * include a positive integer associated with this name to indicate that it
     * wishes to have this number of rows visible at once in its lists. If not
     * specified, the number of visible lines is assumed to be 6.
     */
    public static final String MEGAWIDGET_VISIBLE_LINES = "lines";

    // Private Variables

    /**
     * Selected items label.
     */
    private final String selectedLabel;

    /**
     * Number of lines that should be visible.
     */
    private final int numVisibleLines;

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
    public ListBuilderSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);

        // Ensure that the selected items label, if present,
        // is acceptable.
        try {
            selectedLabel = (String) parameters.get(MEGAWIDGET_SELECTED_LABEL);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_SELECTED_LABEL,
                    parameters.get(MEGAWIDGET_SELECTED_LABEL), "must be string");
        }

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
    }

    // Public Methods

    /**
     * Get the selected items label.
     * 
     * @return Selected items label.
     */
    public final String getSelectedLabel() {
        return selectedLabel;
    }

    /**
     * Get the number of visible lines.
     * 
     * @return Number of visible lines.
     */
    public final int getNumVisibleLines() {
        return numVisibleLines;
    }
}
