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
import gov.noaa.gsd.viz.widgets.MenuButton;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.google.common.collect.ImmutableSet;

/**
 * Megawidget providing a button that when pressed displays a dropdown menu,
 * which in turn contains menu items that when selected fire off invocation
 * notifications.
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
 * @see MenuButtonSpecifier
 */
public class MenuButtonMegawidget extends NotifierMegawidget implements
        IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = new HashSet<>(
                NotifierMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        names.add(MenuButtonSpecifier.MEGAWIDGET_MENU_CHOICES);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Private Variables

    /**
     * Button.
     */
    private final MenuButton button;

    /**
     * Menu for the button.
     */
    private final Menu menu;

    /**
     * Control component helper.
     */
    private final ControlComponentHelper helper;

    /**
     * List of menu choices.
     */
    private List<?> menuChoices;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param parent
     *            Parent of the megawidget.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    protected MenuButtonMegawidget(MenuButtonSpecifier specifier,
            Composite parent, Map<String, Object> paramMap) {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);
        menuChoices = specifier.getMenuChoices();

        /*
         * Create a button widget.
         */
        button = new MenuButton(parent, SWT.PUSH);
        button.setText(specifier.getLabel());
        button.setEnabled(specifier.isEnabled());

        /*
         * Place the widget in the grid.
         */
        GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        gridData.horizontalSpan = specifier.getWidth();
        gridData.verticalIndent = specifier.getSpacing();
        button.setLayoutData(gridData);

        /*
         * Create the menu for the button.
         */
        menu = new Menu(button);
        populateMenu();
        button.setDropDownMenu(menu);

        /*
         * Disable the button if not editable.
         */
        if (isEditable() == false) {
            doSetEditable(false);
        }
    }

    // Public Methods

    @Override
    public Set<String> getMutablePropertyNames() {
        return MUTABLE_PROPERTY_NAMES;
    }

    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            return isEditable();
        } else if (name.equals(MenuButtonSpecifier.MEGAWIDGET_MENU_CHOICES)) {
            return getMenuChoices();
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            setEditable(ConversionUtilities.getPropertyBooleanValueFromObject(
                    getSpecifier().getIdentifier(), getSpecifier().getType(),
                    value, name, null));
        } else if (name.equals(MenuButtonSpecifier.MEGAWIDGET_MENU_CHOICES)) {
            setMenuChoices(value);
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public final boolean isEditable() {
        return helper.isEditable();
    }

    @Override
    public final void setEditable(boolean editable) {
        helper.setEditable(editable);
        doSetEditable(editable);
    }

    @Override
    public final int getLeftDecorationWidth() {
        return 0;
    }

    @Override
    public final void setLeftDecorationWidth(int width) {

        /*
         * No action.
         */
    }

    @Override
    public final int getRightDecorationWidth() {
        return 0;
    }

    @Override
    public final void setRightDecorationWidth(int width) {

        /*
         * No action.
         */
    }

    /**
     * Get the menu choices.
     * 
     * @return Menu choices.
     */
    public final List<?> getMenuChoices() {
        return menuChoices;
    }

    /**
     * Set the menu choices.
     * 
     * @param choices
     *            New menu choices.
     * @throws MegawidgetPropertyException
     *             If the menu choices list provided is invalid.
     */
    public final void setMenuChoices(Object choices)
            throws MegawidgetPropertyException {

        /*
         * Set the choices as specified.
         */
        ChoiceListValidator validator = ((MenuButtonSpecifier) getSpecifier())
                .getMenuChoicesValidator();
        try {
            menuChoices = validator.convertToUnmodifiable(validator
                    .convertToAvailableForProperty(choices));
        } catch (MegawidgetPropertyException e) {
            throw e;
        }

        /*
         * Repopulate the menu.
         */
        for (MenuItem item : menu.getItems()) {
            item.dispose();
        }
        populateMenu();
    }

    // Protected Methods

    @Override
    protected final void doSetEnabled(boolean enable) {
        button.setEnabled(enable && isEditable());
    }

    // Private Methods

    /**
     * Change the component widgets to ensure their state matches that of the
     * editable flag.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     */
    private void doSetEditable(boolean editable) {
        button.setEnabled(editable && isEnabled());
    }

    /**
     * Populate the popup menu with the choices.
     */
    private void populateMenu() {
        ChoiceListValidator validator = ((MenuButtonSpecifier) getSpecifier())
                .getMenuChoicesValidator();
        for (Object choice : menuChoices) {
            MenuItem item = new MenuItem(menu, SWT.PUSH);
            item.setText(validator.getNameOfNode(choice));
            item.setData(validator.getIdentifierOfNode(choice));
            item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    notifyListener((String) e.widget.getData());
                }
            });
        }
    }
}