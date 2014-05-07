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
 * that implement @{link IControlSpecifier}. This class may be used by such
 * classes to handle the setting and getting of such options.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 21, 2013    2168    Chris.Golden      Initial creation.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ControlSpecifierOptionsManager {

    // Public Enumerated Types

    /**
     * Possible values indicating how to set a boolean variable.
     */
    public enum BooleanSource {
        TRUE, FALSE, USE_PARAMETER_VALUE;
    }

    // Private Variables

    /**
     * Flag indicating whether or not the megawidget should start off as
     * editable.
     */
    private final boolean editable;

    /**
     * Number of columns which the megawidget should take up within its parent.
     */
    private final int width;

    /**
     * Flag indicating whether or not the megawidget occupies the full width of
     * its parent megawidget's column.
     */
    private final boolean fullWidthOfColumn;

    /**
     * Spacing between this megawidget and whatever is above it.
     */
    private final int spacing;

    // Public Constructors

    /**
     * Construct a standard instance to manage the control-related options of
     * the provided specifier.
     * 
     * @param specifier
     *            Specifier for which to manage the control-related options.
     * @param parameters
     *            Map containing the parameters to be used to construct the
     *            megawidget specifier, including mappings for the options
     *            needed for the control aspect of said specifier.
     * @param howToSetFullWidthOption
     *            Indicator of how to set the full-width-of-column variable.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public ControlSpecifierOptionsManager(MegawidgetSpecifier specifier,
            Map<String, Object> parameters,
            BooleanSource howToSetFullWidthOption)
            throws MegawidgetSpecificationException {

        /*
         * Ensure that the editable flag, if present, is acceptable.
         */
        editable = ConversionUtilities.getSpecifierBooleanValueFromObject(
                specifier.getIdentifier(), specifier.getType(),
                parameters.get(IControlSpecifier.MEGAWIDGET_EDITABLE),
                IControlSpecifier.MEGAWIDGET_EDITABLE, true);

        /*
         * Get the number of columns available within the parent.
         */
        int parentColumnCount = ConversionUtilities
                .getSpecifierIntegerValueFromObject(
                        specifier.getIdentifier(),
                        specifier.getType(),
                        parameters
                                .get(IControlSpecifier.MEGAWIDGET_PARENT_COLUMN_COUNT),
                        IControlSpecifier.MEGAWIDGET_PARENT_COLUMN_COUNT, 1);

        /*
         * Ensure that the width, if present, is acceptable, and if not present
         * is assigned a default value.
         */
        width = ConversionUtilities.getSpecifierIntegerValueFromObject(
                specifier.getIdentifier(), specifier.getType(),
                parameters.get(IControlSpecifier.MEGAWIDGET_WIDTH),
                IControlSpecifier.MEGAWIDGET_WIDTH, 1);
        if ((width < 1) || (width > parentColumnCount)) {
            throw new MegawidgetSpecificationException(
                    specifier.getIdentifier(), specifier.getType(),
                    IControlSpecifier.MEGAWIDGET_WIDTH, width,
                    "must be between 1 and " + parentColumnCount
                            + " (inclusive)");
        }

        /*
         * Ensure that the full-width-of-column flag, if present, is acceptable.
         */
        switch (howToSetFullWidthOption) {
        case USE_PARAMETER_VALUE:
            fullWidthOfColumn = ConversionUtilities
                    .getSpecifierBooleanValueFromObject(
                            specifier.getIdentifier(),
                            specifier.getType(),
                            parameters
                                    .get(IControlSpecifier.MEGAWIDGET_FULL_WIDTH_OF_COLUMN),
                            IControlSpecifier.MEGAWIDGET_FULL_WIDTH_OF_COLUMN,
                            true);
            break;
        default:
            fullWidthOfColumn = (howToSetFullWidthOption == BooleanSource.TRUE);
        }

        /*
         * Ensure that the spacing, if present, is acceptable, and if not
         * present is assigned a default value.
         */
        spacing = ConversionUtilities.getSpecifierIntegerValueFromObject(
                specifier.getIdentifier(), specifier.getType(),
                parameters.get(IControlSpecifier.MEGAWIDGET_SPACING),
                IControlSpecifier.MEGAWIDGET_SPACING, 0);
        if (spacing < 0) {
            throw new MegawidgetSpecificationException(
                    specifier.getIdentifier(), specifier.getType(),
                    IControlSpecifier.MEGAWIDGET_SPACING, spacing,
                    "must be non-negative");
        }
    }

    // Public Methods

    /**
     * Get the flag indicating whether or not the megawidget is to be created in
     * an editable state.
     * 
     * @return True if the megawidget is to be created as editable, false
     *         otherwise.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Get the width of the megawidget in columns within its parent.
     * 
     * @return Number of columns it should span.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Determine whether or not the megawidget fills the width of the column it
     * is occupying within its parent. This may be used by parent megawidgets to
     * determine whether their children may be laid out side by side in the same
     * column or not.
     * 
     * @return True if the megawidget fills the width of the column it occupies,
     *         false otherwise.
     */
    public boolean isFullWidthOfColumn() {
        return fullWidthOfColumn;
    }

    /**
     * Get the spacing between this megawidget and the one above it in pixels.
     * 
     * @return Spacing.
     */
    public int getSpacing() {
        return spacing;
    }
}
