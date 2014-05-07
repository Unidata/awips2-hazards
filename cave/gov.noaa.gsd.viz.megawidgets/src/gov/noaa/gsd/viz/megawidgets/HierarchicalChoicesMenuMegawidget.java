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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Hierarchical choices menu megawidget, allowing the selection of zero or more
 * choices in a hierarchy, presented to the user in nested menu form.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 27, 2013            Chris.Golden      Initial creation
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 23, 2013   2168     Chris.Golden      Minor cleanup.
 * Oct 31, 2013   2336     Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * Jan 28, 2014   2161     Chris.Golden      Minor fix to Javadoc and adaptation
 *                                           to new JDK 1.7 features.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see HierarchicalChoicesMenuSpecifier
 */
public class HierarchicalChoicesMenuMegawidget extends
        HierarchicalBoundedChoicesMegawidget implements IMenu {

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
     * Listener for menu items.
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
    protected HierarchicalChoicesMenuMegawidget(
            HierarchicalChoicesMenuSpecifier specifier, Menu parent,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);

        /*
         * Create the checked menu item selection listener.
         */
        listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                state.clear();
                state.addAll(convertMenuItemsToState(items
                        .toArray(new MenuItem[items.size()])));
                notifyListeners();
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
        }

        /*
         * Create menu items and any nested submenus required for the
         * hierarchical state choices.
         */
        items = new ArrayList<>();
        for (Object choice : getStateValidator().getAvailableChoices()) {
            items.add(convertStateToMenuItem(menu, -1, choice, listener));
        }

        /*
         * Synchronize user-facing widgets to the starting state.
         */
        synchronizeComponentWidgetsToState();
    }

    // Protected Methods

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
        for (Object choice : getStateValidator().getAvailableChoices()) {
            items.add(convertStateToMenuItem(menu,
                    (index == -1 ? -1 : index++), choice, listener));
        }

        /*
         * Ensure that the new menu items are synced with the old state.
         */
        synchronizeComponentWidgetsToState();
    }

    @Override
    protected final void doSynchronizeComponentWidgetsToState() {
        synchronizeMenuToState(items.toArray(new MenuItem[items.size()]), state);
    }

    // Private Methods

    /**
     * Synchronize the specified menu to the specified state. This includes
     * synchronizing any nested menus of this menu as well.
     * 
     * @param items
     *            Array of menu items to be synchronized.
     * @param state
     *            State to which the menu item is to be synchronized, in the
     *            form of a list containing strings and/or maps that give the
     *            items that are to be selected, or <code>null</code> if none of
     *            the menu's items are to be selected.
     */
    private void synchronizeMenuToState(MenuItem[] items, List<?> state) {

        /*
         * Iterate through the state list, recording each selected item's
         * identifier paired with its sub-list (if it is a branch node in the
         * state tree) or with nothing (if it is a leaf).
         */
        Map<String, List<?>> subListsForSelectedIdentifiers = new HashMap<>();
        if (state != null) {
            for (Object element : state) {
                String identifier;
                List<?> subList = null;
                if (element instanceof String) {
                    identifier = (String) element;
                } else {
                    identifier = (String) ((Map<?, ?>) element)
                            .get(HierarchicalChoicesMenuSpecifier.CHOICE_IDENTIFIER);
                    if (identifier == null) {
                        identifier = (String) ((Map<?, ?>) element)
                                .get(HierarchicalChoicesMenuSpecifier.CHOICE_NAME);
                    }
                    subList = (List<?>) ((Map<?, ?>) element)
                            .get(HierarchicalChoicesMenuSpecifier.CHOICE_CHILDREN);
                }
                subListsForSelectedIdentifiers.put(identifier, subList);
            }
        }

        /*
         * Iterate through the menu items, setting the selection of each leaf
         * item to be true (checked) only if it was found in the state list
         * above, and recursively calling this method on the submenu of any
         * branch item to synchronize the submenu with that branch's state
         * subtree.
         */
        for (MenuItem item : items) {
            if (item.getStyle() == SWT.CHECK) {
                item.setSelection(subListsForSelectedIdentifiers
                        .containsKey(item.getData()));
            } else {
                synchronizeMenuToState(item.getMenu().getItems(),
                        subListsForSelectedIdentifiers.get(item.getData()));
            }
        }
    }

    /**
     * Turn the specified trees into a single hierarchy of tree items. If there
     * are two trees, combine them, using the second one's choices as long
     * versions of the first one's choices.
     * 
     * @param parent
     *            Parent menu.
     * @param index
     *            Index at which to create the menu item, or <code>-1</code> if
     *            the menu item is to be appended.
     * @param tree
     *            State hierarchy to be converted.
     * @param listener
     *            Selection listener for the checked menu items.
     * @return Menu item hierarchy resulting from the conversion.
     */
    private MenuItem convertStateToMenuItem(Menu parent, int index,
            Object tree, SelectionListener listener) {
        if (tree instanceof String) {
            return createMenuItem(parent, index, (String) tree, null, listener);
        } else {
            Map<?, ?> map = (Map<?, ?>) tree;
            String name = (String) map
                    .get(HierarchicalChoicesMenuSpecifier.CHOICE_NAME);
            String identifier = (String) map
                    .get(HierarchicalChoicesMenuSpecifier.CHOICE_IDENTIFIER);
            List<?> children = (List<?>) map
                    .get(HierarchicalChoicesTreeSpecifier.CHOICE_CHILDREN);
            MenuItem item = createMenuItem(parent, index, name, identifier,
                    (children == null ? listener : null));
            if (children != null) {
                Menu menu = new Menu(parent);
                item.setMenu(menu);
                for (Object child : children) {
                    convertStateToMenuItem(menu, -1, child, listener);
                }
            }
            return item;
        }
    }

    /**
     * Turn the specified menu items into a state hierarchy.
     * 
     * @param items
     *            Menu items to be converted.
     * @return State hierarchy resulting from the conversion.
     */
    private List<Object> convertMenuItemsToState(MenuItem[] items) {

        /*
         * Iterate through the child items, adding each one that is checked or
         * grayed to the state hierarchy, as well as any descendants that are
         * checked or grayed.
         */
        List<Object> children = new ArrayList<>();
        for (MenuItem item : items) {
            if (item.getStyle() == SWT.CHECK) {
                if (item.getSelection()) {
                    children.add(item.getData());
                }
            } else {
                Map<String, Object> node = new HashMap<>();
                node.put(HierarchicalChoicesMenuSpecifier.CHOICE_NAME,
                        item.getText());
                node.put(HierarchicalChoicesMenuSpecifier.CHOICE_IDENTIFIER,
                        item.getData());
                node.put(HierarchicalChoicesMenuSpecifier.CHOICE_CHILDREN,
                        convertMenuItemsToState(item.getMenu().getItems()));
                children.add(node);
            }
        }
        return children;
    }

    /**
     * Create the specified menu item.
     * 
     * @param parent
     *            Parent menu.
     * @param index
     *            Index at which to create the menu item, or <code>-1</code> if
     *            the menu item is to be appended.
     * @param name
     *            Choice name.
     * @param identifier
     *            Choice identifier, or <code>null</code> if none exists, in
     *            which case the name will be used as the identifier.
     * @param listener
     *            Selection listener to be registered; if present, the menu item
     *            must be of the style {@link SWT#CHECK}; otherwise, it must be
     *            of the style {@link SWT#CASCADE}.
     * @return Created menu item.
     */
    private MenuItem createMenuItem(Menu parent, int index, String name,
            String identifier, SelectionListener listener) {
        int style = (listener == null ? SWT.CASCADE : SWT.CHECK);
        MenuItem item = (index == -1 ? new MenuItem(parent, style)
                : new MenuItem(parent, style, index));
        item.setText(name);
        item.setData(identifier != null ? identifier : name);
        if (listener != null) {
            item.addSelectionListener(listener);
        }
        return item;
    }
}