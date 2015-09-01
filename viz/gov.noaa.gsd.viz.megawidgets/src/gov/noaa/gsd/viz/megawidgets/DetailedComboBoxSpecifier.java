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

import gov.noaa.gsd.viz.megawidgets.validators.SingleChoiceValidatorHelper;

import java.util.List;
import java.util.Map;

/**
 * Detailed combo box megawidget specifier. Each choice may have zero or more
 * detail fields associated with it, each of the latter being itself a
 * megawidget. The detail fields appear in a group-style panel below the combo
 * box.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 23, 2014    4122    Chris.Golden Initial creation.
 * Oct 20, 2014    4818    Chris.Golden Added option of providing a scrollable
 *                                      panel for child megawidgets.
 * Aug 20, 2015    9617    Robert.Blum  Readonly property is now optional for
 *                                      comboboxes.
 * Aug 28, 2015    9617    Chris.Golden Fixed code added under this ticket.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see DetailedComboBoxMegawidget
 */
public class DetailedComboBoxSpecifier extends
        FlatChoicesWithDetailMegawidgetSpecifier<String> implements
        IPotentiallyScrollableContainerSpecifier<IControlSpecifier>,
        IComboBoxSpecifier {

    // Public Static Constants

    /**
     * Expand header to fill horizontal space parameter name; a detailed combo
     * box may include a boolean associated with this name to indicate whether
     * or not the header (label and combo box) should expand to fill any
     * available horizontal space that the grouping fills. If not specified, the
     * header is not expanded horizontally.
     */
    public static final String HEADER_EXPAND_HORIZONTALLY = "headerExpandHorizontally";

    /**
     * New choice detail fields parameter name; a detailed combo box may include
     * a reference to a {@link List} object associated with this name. The
     * provided list must contain zero or more child megawidget specifier
     * parameter maps, each in the form of a {@link Map}, from which a
     * megawidget specifier will be constructed. The resulting list of
     * specifiers together comprise the specification of the detail panel to be
     * shown when new choices are added to the megawidget by the user. If
     * {@link IComboBoxSpecifier#ALLOW_NEW_CHOICE_ENABLED} is false, this
     * parameter is ignored.
     */
    public static final String NEW_CHOICE_DETAIL_FIELDS = "newChoiceDetailFields";

    // Private Variables

    /**
     * Container specifier options manager.
     */
    private final ContainerSpecifierOptionsManager<IControlSpecifier> containerOptionsManager;

    /**
     * Combo box options manager.
     */
    private final ComboBoxSpecifierOptionsManager comboBoxOptionsManager;

    /**
     * List of detail megawidget specifiers to be used to populate the panel of
     * any user-created choice; if {@link #NEW_CHOICE_DETAIL_FIELDS} is not
     * supplied, this will be <code>null</code>.
     */
    private final List<IControlSpecifier> newChoiceDetailFields;

    /**
     * Flag indicating whether the header should expand to fill the available
     * space of the grouping horizontally.
     */
    private final boolean headerExpandHorizontally;

    /**
     * Flag indicating whether or not the client area is to be scrollable.
     */
    private final boolean scrollable;

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
    public DetailedComboBoxSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, new SingleChoiceValidatorHelper(
                MEGAWIDGET_VALUE_CHOICES, CHOICE_NAME, CHOICE_IDENTIFIER), true);
        containerOptionsManager = new ContainerSpecifierOptionsManager<>(this,
                IControlSpecifier.class, parameters);
        comboBoxOptionsManager = new ComboBoxSpecifierOptionsManager(this,
                parameters);

        /*
         * If new choice detail fields are specified, ensure they are correct.
         */
        if (isAllowNewChoiceEnabled()) {
            Object fieldsObj = parameters.get(NEW_CHOICE_DETAIL_FIELDS);
            List<?> fields = null;
            try {
                fields = (List<?>) fieldsObj;
            } catch (Exception e) {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), NEW_CHOICE_DETAIL_FIELDS, fieldsObj,
                        "bad child megawidget specifier list", e);
            }
            try {
                newChoiceDetailFields = getChildManager()
                        .createMegawidgetSpecifiers(fields, fields.size());
            } catch (MegawidgetSpecificationException e) {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), NEW_CHOICE_DETAIL_FIELDS, fields,
                        "bad child megawidget specifier", e);
            }
        } else {
            newChoiceDetailFields = null;
        }

        /*
         * Ensure that the header expand flag, if present, is acceptable.
         */
        headerExpandHorizontally = ConversionUtilities
                .getSpecifierBooleanValueFromObject(getIdentifier(), getType(),
                        parameters.get(HEADER_EXPAND_HORIZONTALLY),
                        HEADER_EXPAND_HORIZONTALLY, false);

        /*
         * Ensure that the scrollable flag, if present, is valid.
         */
        scrollable = ConversionUtilities.getSpecifierBooleanValueFromObject(
                getIdentifier(), getType(), parameters.get(SCROLLABLE),
                SCROLLABLE, false);
    }

    @Override
    public int getLeftMargin() {
        return containerOptionsManager.getLeftMargin();
    }

    @Override
    public int getTopMargin() {
        return containerOptionsManager.getTopMargin();
    }

    @Override
    public int getRightMargin() {
        return containerOptionsManager.getRightMargin();
    }

    @Override
    public int getBottomMargin() {
        return containerOptionsManager.getBottomMargin();
    }

    @Override
    public int getColumnSpacing() {
        return containerOptionsManager.getColumnSpacing();
    }

    @Override
    public boolean isHorizontalExpander() {
        return containerOptionsManager.isHorizontalExpander();
    }

    @Override
    public boolean isVerticalExpander() {
        return containerOptionsManager.isVerticalExpander();
    }

    @Override
    public final boolean isScrollable() {
        return scrollable;
    }

    @Override
    public final boolean isAutocompleteEnabled() {
        return comboBoxOptionsManager.isAutocompleteEnabled();
    }

    @Override
    public boolean isAllowNewChoiceEnabled() {
        return comboBoxOptionsManager.isAllowNewChoiceEnabled();
    }

    /**
     * Get the list of detail megawidget specifiers to be used to populate any
     * panel associated with a newly created choice.
     * 
     * @return List of detail megawidget specifiers, or <code>null</code> if
     *         {@link #isAllowNewChoiceEnabled()} is false.
     */
    public List<IControlSpecifier> getNewChoiceDetailFields() {
        return newChoiceDetailFields;
    }

    /**
     * Determine whether or not the megawidget's header is to expand to take up
     * available horizontal space.
     * 
     * @return Flag indicating whether or not the megawidget's header is to
     *         expand horizontally.
     */
    public boolean isHeaderHorizontalExpander() {
        return headerExpandHorizontally;
    }
}
