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
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see CheckBoxesMenuSpecifier
 */
public class CheckBoxesMenuMegawidget extends MultipleChoicesMegawidget {

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

        // Create the checked menu item selection listener.
        SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MenuItem item = (MenuItem) e.widget;
                String choice = (String) item.getData();
                if (item.getSelection()) {
                    state.add(choice);
                } else {
                    state.remove(choice);
                }
                notifyListener(getSpecifier().getIdentifier(), state);
                notifyListener();
            }
        };

        // Put in a separator if called for.
        if (specifier.shouldShowSeparator()) {
            new MenuItem(parent, SWT.SEPARATOR);
        }

        // If the menu items are to be shown on their own
        // separate submenu, create the top-level menu item
        // with the given label, and then create a menu that
        // springs off of it holding all the choices as menu
        // items. Otherwise, just use the parent menu as the
        // place for the menu items.
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

        // Create the menu items.
        items = new ArrayList<MenuItem>();
        for (String choice : specifier.getChoiceIdentifiers()) {
            MenuItem item = new MenuItem(menu, SWT.CHECK);
            item.setText(specifier.getLongVersionFromChoice(choice));
            item.setData(choice);
            item.addSelectionListener(listener);
            items.add(item);
        }
    }

    // Protected Methods

    /**
     * Receive notification that the megawidget's state has changed.
     * 
     * @param state
     *            New state.
     */
    @Override
    protected final void megawidgetStateChanged(List<String> state) {
        for (MenuItem item : items) {
            item.setSelection(state.contains(item.getData()));
        }
    }

    /**
     * Change the component widgets to ensure their state matches that of the
     * enabled flag.
     * 
     * @param enable
     *            Flag indicating whether the component widgets are to be
     *            enabled or disabled.
     */
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

    /**
     * Change the component widgets to ensure their state matches that of the
     * editable flag.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     */
    @Override
    protected final void doSetEditable(boolean editable) {

        // Not supported for menu-based megawidgets.
        throw new UnsupportedOperationException(
                "cannot change editability of menu-based megawidget");
    }
}