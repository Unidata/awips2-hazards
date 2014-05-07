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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Checkboxes menu megawidget, allowing the selection of zero or more choices,
 * each represented visually on a menu as a checkable menu item.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 28, 2013            Chris.Golden      Initial creation
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 23, 2013   2168     Chris.Golden      Changed to implement new IMenu interface.
 * Oct 31, 2013   2336     Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * Mar 06, 2014   2155     Chris.Golden      Fixed bug caused by a lack of
 *                                           defensive copying of the state when
 *                                           notifying a state change listener of
 *                                           a change. Also took advantage of new
 *                                           JDK 1.7 features.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see CheckBoxesMenuSpecifier
 */
public class CheckBoxesMenuMegawidget extends MultipleBoundedChoicesMegawidget
        implements IMenu {

    // Private Variables

    /**
     * Menu item holding the menu associated with this megawidget, if any; if
     * the megawidget is located on its parent menu, this will be
     * <code>null</code>.
     */
    private final MenuItem topItem;

    /**
     * Menu items that make up this megawidget.
     */
    private final List<MenuItem> items;

    /**
     * Menu item listener.
     */
    private final SelectionListener listener;

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
    protected CheckBoxesMenuMegawidget(CheckBoxesMenuSpecifier specifier,
            Menu parent, Map<String, Object> paramMap) {
        super(specifier, paramMap);

        /*
         * Create the checked menu item selection listener.
         */
        listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MenuItem item = (MenuItem) e.widget;
                String choice = (String) item.getData();
                if (item.getSelection()) {
                    state.add(choice);
                } else {
                    state.remove(choice);
                }
                notifyListener(getSpecifier().getIdentifier(), new ArrayList<>(
                        state));
                notifyListener();
            }
        };

        /*
         * Put in a separator if called for.
         */
        if (specifier.shouldShowSeparator()) {
            new MenuItem(parent, SWT.SEPARATOR);
        }

        /*
         * If the menu items are to be shown on their own separate submenu,
         * create the top-level menu item with the given label, and then create
         * a menu that springs off of it holding all the choices as menu items.
         * Otherwise, just use the parent menu as the place for the menu items.
         */
        Menu menu;
        if (specifier.shouldBeOnParentMenu() == false) {
            topItem = new MenuItem(parent, SWT.CASCADE);
            topItem.setText(specifier.getLabel());
            topItem.setEnabled(specifier.isEnabled());
            menu = new Menu(parent);
            topItem.setMenu(menu);
        } else {
            topItem = null;
            menu = parent;
            new MenuItem(menu, SWT.SEPARATOR);
        }

        /*
         * Create the menu items.
         */
        items = new ArrayList<>();
        createMenuItemsForChoices(menu, -1);

        /*
         * Synchronize user-facing widgets to the starting state.
         */
        synchronizeComponentWidgetsToState();
    }

    // Public Methods

    /**
     * Set the choices to those specified. If the current state is not a subset
     * of the new choices, the state will be set to <code>null</code>.
     * 
     * @param value
     *            List of new choices.
     * @throws MegawidgetPropertyException
     *             If the choices are invalid.
     */
    public final void setChoices(Object value)
            throws MegawidgetPropertyException {
        doSetChoices(value);
    }

    // Protected Methods

    @Override
    protected final boolean isChoicesListMutable() {
        return true;
    }

    @Override
    protected final void prepareForChoicesChange() {

        /*
         * No action.
         */
    }

    @Override
    protected void cancelPreparationForChoicesChange() {
        /*
         * No action.
         */
    }

    @Override
    protected final void synchronizeComponentWidgetsToChoices() {

        /*
         * Find the parent menu of the items, and if the parent menu is not a
         * submenu build explicitly for this megawidget, the index at which to
         * start adding new menu items.
         */
        Menu menu = (topItem == null ? items.get(0).getParent() : topItem
                .getMenu());
        int index = (topItem == null ? menu.indexOf(items.get(0)) : -1);

        /*
         * Dispose of the old menu items.
         */
        for (MenuItem item : items) {
            item.dispose();
        }
        items.clear();

        /*
         * Create the new menu items.
         */
        createMenuItemsForChoices(menu, index);

        /*
         * Ensure that the new menu items are synced with the old state.
         */
        synchronizeComponentWidgetsToState();
    }

    @Override
    protected final void doSynchronizeComponentWidgetsToState() {
        for (MenuItem item : items) {
            item.setSelection(state.contains(item.getData()));
        }
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (topItem != null) {
            topItem.setEnabled(enable);
        } else {
            for (MenuItem item : items) {
                item.setEnabled(enable);
            }
        }
    }

    // Private Methods

    /**
     * Create menu items for the choices.
     * 
     * @param menu
     *            Menu to which to attach the menu items.
     * @param index
     *            Index at which to start inserting menu items within the menu,
     *            or <code>-1</code> if they should be appended.
     */
    private void createMenuItemsForChoices(Menu menu, int index) {
        CheckBoxesMenuSpecifier specifier = getSpecifier();
        for (Object choice : getStateValidator().getAvailableChoices()) {
            MenuItem item = (index == -1 ? new MenuItem(menu, SWT.CHECK)
                    : new MenuItem(menu, SWT.CHECK, index++));
            item.setText(specifier.getNameOfNode(choice));
            item.setData(specifier.getIdentifierOfNode(choice));
            item.addSelectionListener(listener);
            items.add(item);
        }
    }
}