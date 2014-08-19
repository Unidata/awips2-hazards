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
 * Description: Specifier for a megawidget that acts as a wrapper for
 * non-megawidget widgets (user interface elements).
 * <p>
 * TODO: Consider making this non-SWT-specific. However, this should be part of
 * separating megawidget specifiers and abstract, non-widget-toolkit-specific
 * megawidget classes out from SWT-based megawidget subclasses of the latter,
 * which is not a small job.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 19, 2014    4098    Chris.Golden Initial creation
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class SwtWrapperSpecifier extends MegawidgetSpecifier implements
        IControlSpecifier {

    // Public Static Constants

    /**
     * Expand to fill horizontal space parameter name; a megawidget may include
     * a boolean associated with this name to indicate whether or not the
     * megawidget should expand to fill any available horizontal space within
     * its parent. If not specified, the megawidget is not expanded
     * horizontally.
     */
    public static final String EXPAND_HORIZONTALLY = "expandHorizontally";

    /**
     * Expand to fill vertical space parameter name; a megawidget may include a
     * boolean associated with this name to indicate whether or not the
     * megawidget should expand to fill any available vertical space within its
     * parent. If not specified, the megawidget is not expanded vertically.
     */
    public static final String EXPAND_VERTICALLY = "expandVertically";

    // Private Static Constants

    /**
     * Array of expand flags in the order they are specified in the
     * {@link #expander} member variable.
     */
    private static final String[] EXPANDER_NAMES = { EXPAND_HORIZONTALLY,
            EXPAND_VERTICALLY };

    // Private Variables

    /**
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager optionsManager;

    /**
     * Flag indicating whether or not the megawidget should expand horizontally
     * (first index) and/or vertically (second index) within its parent.
     */
    private final boolean[] expander = new boolean[2];

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map containing the parameters to be used to construct the
     *            megawidget specifier.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public SwtWrapperSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);
        optionsManager = new ControlSpecifierOptionsManager(
                this,
                parameters,
                ControlSpecifierOptionsManager.BooleanSource.USE_PARAMETER_VALUE);

        /*
         * Ensure that the expand flags, if present, are acceptable.
         */
        for (int j = 0; j < EXPANDER_NAMES.length; j++) {
            expander[j] = ConversionUtilities
                    .getSpecifierBooleanValueFromObject(getIdentifier(),
                            getType(), parameters.get(EXPANDER_NAMES[j]),
                            EXPANDER_NAMES[j], false);
        }
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

    /**
     * Determine whether or not the megawidget should expand horizontally within
     * its parent.
     * 
     * @return True if the megawidget should expand horizontally, false
     *         otherwise.
     */
    public final boolean isHorizontalExpander() {
        return expander[0];
    }

    /**
     * Determine whether or not the megawidget should expand vertically within
     * its parent.
     * 
     * @return True if the megawidget should expand vertically, false otherwise.
     */
    public final boolean isVerticalExpander() {
        return expander[1];
    }
}
