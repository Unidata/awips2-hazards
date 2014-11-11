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

import gov.noaa.gsd.viz.megawidgets.validators.ChoiceListValidator;

import java.util.List;
import java.util.Map;

/**
 * Menu button megawidget specifier, providing a button that when pressed
 * displays a menu including one or more menu items, which when chosen fire off
 * an invocation notification. The notification includes both the identifier of
 * the megawidget, and the identifier of the menu item chosen, the latter being
 * the subcommand.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 10, 2014    4042    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see MenuButtonMegawidget
 */
public class MenuButtonSpecifier extends MegawidgetSpecifier implements
        IControlSpecifier {

    // Public Static Constants

    /**
     * Megawidget menu choices parameter name; a megawidget must include an
     * array of one or more choices associated with this name. Each such choice
     * may be either a string, meaning that the string value is used as the
     * choice's name, or else a {@link Map} holding an entry for
     * {@link #CHOICE_NAME} and, optionally, an entry for
     * {@link #CHOICE_IDENTIFIER}.
     */
    public static final String MEGAWIDGET_MENU_CHOICES = "choices";

    /**
     * Choice name parameter name; each choice in the array of menu choices
     * associated with {@link #MEGAWIDGET_MENU_CHOICES} that is a map must
     * contain a reference to a string associated with this name. The string
     * serves to label and to uniquely identify the choice; thus, each name must
     * be unique in the set of all choice names.
     */
    public static final String CHOICE_NAME = "displayString";

    /**
     * Choice identifier parameter name; each choice in the array of menu
     * choices associated with {@link #MEGAWIDGET_MENU_CHOICES} that is a map
     * may contain a reference to a string associated with this name. The string
     * serves as the identifier of the menu choice. If not provided, the
     * {@link #CHOICE_NAME} is used as its identifier instead. Each identifier
     * must be unique in the set of all choice identifiers.
     */
    public static final String CHOICE_IDENTIFIER = "identifier";

    // Private Variables

    /**
     * List of menu choices.
     */
    private final List<?> menuChoices;

    /**
     * Menu choices validator.
     */
    private final ChoiceListValidator menuChoicesValidator;

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
    public MenuButtonSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                ControlSpecifierOptionsManager.BooleanSource.FALSE);
        menuChoicesValidator = new ChoiceListValidator(MEGAWIDGET_MENU_CHOICES,
                CHOICE_NAME, CHOICE_IDENTIFIER);
        menuChoicesValidator.initialize(getType(), getIdentifier());
        menuChoices = menuChoicesValidator
                .convertToAvailableForSpecifier(parameters
                        .get(MEGAWIDGET_MENU_CHOICES));
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
     * Get the list of available choices.
     * 
     * @return List of available choices.
     */
    protected final List<?> getMenuChoices() {
        return menuChoices;
    }

    // Protected Methods

    /**
     * Get the menu choices validator.
     * 
     * @return Menu choices validator.
     */
    protected final ChoiceListValidator getMenuChoicesValidator() {
        return menuChoicesValidator;
    }
}
