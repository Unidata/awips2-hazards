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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Choices tree megawidget, allowing the selection of zero or more choices in a
 * hierarchy, presented to the user in tree form.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 22, 2013   2168     Chris.Golden      Replaced some GUI creation code with
 *                                           calls to UiBuilder methods to avoid
 *                                           code duplication and encourage uni-
 *                                           form look, and changed to implement
 *                                           new IControl interface.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see HierarchicalChoicesTreeSpecifier
 */
public class HierarchicalChoicesTreeMegawidget extends
        HierarchicalChoicesMegawidget implements IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = Sets
                .newHashSet(HierarchicalChoicesMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Private Classes

    /**
     * Nodes map, a map in which the key is a choice identifier and the value is
     * either <code>null</code>, meaning that there are no child nodes
     * associated with the key, or a node map holding child choice identifier
     * mappings.
     */
    private class NodesMap extends HashMap<String, NodesMap> {

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;
    };

    // Private Variables

    /**
     * Label associated with this megawidget, if any.
     */
    private final Label label;

    /**
     * Tree associated with this megawidget.
     */
    private final Tree tree;

    /**
     * Select all items button, if any.
     */
    private final Button allButton;

    /**
     * Deselect all items button, if any.
     */
    private final Button noneButton;

    /**
     * Last position of the vertical scrollbar. This is used whenever the
     * choices are being changed via <code>setChoices()</code> or one of the
     * mutable property manipulation methods, in order to keep a similar visual
     * state to what came before.
     */
    private int scrollPosition = 0;

    /**
     * List of zero or more identifiers indicating what node in the hierarchy,
     * if any, was last selected. If the list is not empty, the identifiers are
     * specified in order from topmost branch to leaf, providing the full
     * hierarchy of the identifiers. Thus, if the last selected node had the
     * identifier "bar" and was the child of a parent node "foo", the list would
     * contain two elements, "foo" and "bar", respectively. This list is used
     * whenever the choices are being changed via <code>setChoices()</code> or
     * one of the mutable property manipulation methods, in order to keep a
     * similar visual state to what came before.
     */
    private final List<String> selectedNodeIdentifiers = Lists.newArrayList();

    /**
     * Map of choice identifiers that were last found to be expanded to
     * sub-maps, with each of the latter being in turn a mapping of child choice
     * identifiers that were found to be expanded to sub-sub-maps, and so on.
     * The value in each mapping may also be <code>null</code>, which simply
     * means that while the parent choice identifier was found to be expanded,
     * none of its children were. This map is used whenever the choices are
     * being changed via <code>setChoices()</code> or one of the mutable
     * property manipulation methods, in order to keep a similar visual state to
     * what came before.
     */
    private final NodesMap expandedNodes = new NodesMap();

    /**
     * Control component helper.
     */
    private final ControlComponentHelper helper;

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
    protected HierarchicalChoicesTreeMegawidget(
            HierarchicalChoicesTreeSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);

        // Create the composite holding the components, and
        // the label if appropriate.
        Composite panel = UiBuilder.buildComposite(parent, 1, SWT.NONE,
                UiBuilder.CompositeType.MULTI_ROW_VERTICALLY_EXPANDING,
                specifier);
        label = UiBuilder.buildLabel(panel, specifier);

        // Create a tree to hold the checkable choices hier-
        // archy, and add said choices.
        tree = new Tree(panel, SWT.BORDER + SWT.CHECK);
        tree.setLinesVisible(false);
        tree.setHeaderVisible(false);
        tree.setEnabled(specifier.isEnabled());
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = (specifier.getNumVisibleLines() * tree
                .getItemHeight()) + 7;
        tree.setLayoutData(gridData);
        for (Object choice : choices) {
            convertStateToTree(tree, choice, null);
        }

        // Add the Select All and Select None buttons if appropriate.
        List<Button> buttons = UiBuilder.buildAllNoneButtons(panel, specifier,
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        state.clear();
                        state.addAll(choices);
                        setAllItemsCheckedState(
                                HierarchicalChoicesTreeMegawidget.this.tree
                                        .getItems(), true);
                        notifyListeners();
                    }
                }, new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        state.clear();
                        setAllItemsCheckedState(
                                HierarchicalChoicesTreeMegawidget.this.tree
                                        .getItems(), false);
                        notifyListeners();
                    }
                });
        if (buttons.isEmpty() == false) {
            allButton = buttons.get(0);
            noneButton = buttons.get(1);
        } else {
            allButton = noneButton = null;
        }

        // Bind check events to trigger a change in the
        // record of the state for the megawidget.
        tree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                // If this is not a check event, do
                // nothing.
                if (e.detail != SWT.CHECK) {
                    return;
                }

                // If the megawidget is editable, record
                // the state change; otherwise, undo
                // what was just done.
                if (isEditable()) {
                    TreeItem item = (TreeItem) e.item;
                    item.setGrayed(false);
                    updateDescendantStates(item);
                    updateAncestorStates(item);
                    state.clear();
                    state.addAll(convertTreeToState(item.getParent()));
                    notifyListeners();
                } else {
                    e.detail = SWT.NONE;
                    e.doit = false;
                    HierarchicalChoicesTreeMegawidget.this.tree
                            .setRedraw(false);
                    TreeItem item = (TreeItem) e.item;
                    if (updateItemStateBasedUponChildrenStates(item) == false) {
                        item.setChecked(!item.getChecked());
                    }
                    HierarchicalChoicesTreeMegawidget.this.tree.setRedraw(true);
                    return;
                }
            }
        });

        // Render the tree uneditable if necessary.
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
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            setEditable(getPropertyBooleanValueFromObject(value, name, null));
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

        // No action.
    }

    @Override
    public final int getRightDecorationWidth() {
        return 0;
    }

    @Override
    public final void setRightDecorationWidth(int width) {

        // No action.
    }

    // Protected Methods

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        tree.setEnabled(enable);
        if (allButton != null) {
            allButton.setEnabled(isEditable() && enable);
            noneButton.setEnabled(isEditable() && enable);
        }
    }

    @Override
    protected final void prepareForChoicesChange() {

        // Remember the scrollbar position so that it can be approximately
        // restored.
        scrollPosition = tree.getVerticalBar().getSelection();

        // Remember the identifiers in hierarchical order of the selected
        // node, if any.
        TreeItem[] selectedItems = tree.getSelection();
        if (selectedItems.length > 0) {
            for (TreeItem item = selectedItems[0]; item != null; item = item
                    .getParentItem()) {
                selectedNodeIdentifiers.add((String) item.getData());
            }
            Collections.reverse(selectedNodeIdentifiers);
        }

        // Determine what nodes from the soon-to-be-removed hierarchy are
        // expanded, so that any nodes with the same identifiers at the
        // same levels in the new hierarchy may be expanded at creation
        // time.
        recordExpandedNodes(tree.getItems(), expandedNodes);
    }

    @Override
    protected final void synchronizeWidgetsToChoices() {

        // Remove all the previous tree items.
        tree.removeAll();

        // Create the new tree items.
        for (Object choice : choices) {
            convertStateToTree(tree, choice, expandedNodes);
        }

        // Select the appropriate node, if one that has the same place in
        // the old hierarchy is found.
        TreeItem itemToSelect = null;
        for (String identifier : selectedNodeIdentifiers) {
            TreeItem[] items = (itemToSelect == null ? tree.getItems()
                    : itemToSelect.getItems());
            if (items == null) {
                break;
            }
            boolean foundMatch = false;
            for (TreeItem item : items) {
                if (item.getData().equals(identifier)) {
                    itemToSelect = item;
                    foundMatch = true;
                    break;
                }
            }
            if (foundMatch == false) {
                break;
            }
        }
        if (itemToSelect != null) {
            tree.setSelection(itemToSelect);
        }

        // Clear the expanded nodes map and selected node list, as they
        // are no longer needed.
        expandedNodes.clear();
        selectedNodeIdentifiers.clear();

        // Ensure that the new tree items are synced with the old state.
        synchronizeWidgetsToState();

        // Set the scrollbar position to be similar to what it was before.
        tree.getVerticalBar().setSelection(scrollPosition);
    }

    @Override
    protected final void synchronizeWidgetsToState() {

        // Clear the tree items' selection states.
        clearSelection(tree);

        // Set the leaf tree items' selection states to
        // match the state.
        setLeafSelectionToMatchState(tree, state);

        // Update the non-leaf tree items' selections to
        // reflect their children's states.
        updateNonLeafStates(tree);
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
        tree.setBackground(helper.getBackgroundColor(editable, tree, label));
        if (allButton != null) {
            allButton.setEnabled(isEnabled() && editable);
            noneButton.setEnabled(isEnabled() && editable);
        }
    }

    /**
     * Clear the selection of all items in the specified tree.
     * 
     * @param tree
     *            Tree (if the parameter is of type <code> Tree</code>) or tree
     *            item (if of type <code>TreeItem</code>) to have the selection
     *            state of all its items cleared.
     */
    private void clearSelection(Widget tree) {

        // Get the children of the tree or tree item.
        TreeItem[] items = null;
        if (tree instanceof Tree) {
            items = ((Tree) tree).getItems();
        } else {
            items = ((TreeItem) tree).getItems();
        }

        // Iterate through the children, clearing the se-
        // lection state of each in turn.
        for (TreeItem item : items) {
            item.setChecked(false);
            item.setGrayed(false);
            if (item.getItemCount() > 0) {
                clearSelection(item);
            }
        }
    }

    /**
     * Set the selection (checked items) of the specified tree's leaves
     * according to the given state.
     * 
     * @param tree
     *            Tree (if the parameter is of type <code>Tree</code>) or tree
     *            item (if of type <code>TreeItem</code>) to have the selection
     *            state of its items checked. It is assumed that this tree has
     *            had all its items' selection states cleared prior to this
     *            invocation.
     * @param state
     *            State hierarchy containing all items to be checked or
     *            half-checked. It is assumed that this hierarchy has been
     *            checked against the hierarchy used to build <code>tree</code>
     *            and does not contain any nodes that are not found in the
     *            latter. If <code>null</code>, all leaves in <code>tree</code>
     *            are to be selected.
     */
    private void setLeafSelectionToMatchState(Widget tree, List<?> state) {

        // Get the children of the tree or tree item.
        TreeItem[] items = null;
        if (tree instanceof Tree) {
            items = ((Tree) tree).getItems();
        } else {
            items = ((TreeItem) tree).getItems();
        }

        // Iterate through the state hierarchy's children,
        // finding the corresponding tree item for each
        // and telling that item to update its leaf selec-
        // tions to match this state (if it is not a leaf),
        // or to set itself as checked (if it is a leaf).
        HierarchicalChoicesTreeSpecifier specifier = getSpecifier();
        for (Object node : state) {
            String identifier = specifier.getIdentifierOfNode(node);
            int index;
            for (index = 0; index < items.length; index++) {
                if (items[index].getData().equals(identifier)) {
                    break;
                }
            }
            List<?> childState = null;
            if (node instanceof Map) {
                childState = (List<?>) ((Map<?, ?>) node)
                        .get(HierarchicalChoicesTreeSpecifier.CHOICE_CHILDREN);
            }
            if (items[index].getItemCount() > 0) {
                setLeafSelectionToMatchState(items[index], childState);
            } else {
                items[index].setChecked(true);
            }
        }
    }

    /**
     * Set the checked state for all the specified items to the specified value.
     * 
     * @param treeItems
     *            Items to have their checked states set.
     * @param checked
     *            Flag indicating whether or not the items should be checked.
     */
    private void setAllItemsCheckedState(TreeItem[] items, boolean checked) {
        for (TreeItem item : items) {
            item.setChecked(checked);
            item.setGrayed(false);
            if (item.getItemCount() > 0) {
                setAllItemsCheckedState(item.getItems(), checked);
            }
        }
    }

    /**
     * Turn the specified state hierarchy into a tree item hierarchy.
     * 
     * @param parent
     *            Parent tree (if the parameter is of type <code>Tree</code>) or
     *            tree item (if the parameter is of type <code>TreeItem</code).
     * @param tree
     *            State hierarchy to be converted.
     * @param expandedMap
     *            Map with key-value pairs for all nodes that were expanded at
     *            this level of the previous hierarchy, with each key being the
     *            identifier of an old node that was expanded, and each value
     *            being a sub-map for its children, or <code>null</code> if none
     *            of the old node's children were expanded.
     * @return Tree item hierarchy resulting from the conversion.
     */
    private TreeItem convertStateToTree(Widget parent, Object tree,
            NodesMap expandedMap) {

        // If the node is a string, just create a leaf; otherwise, create a node
        // that may be a leaf or a branch, depending upon whether or not it has
        // children.
        if (tree instanceof String) {
            return createTreeItem(parent, (String) tree, null);
        } else {

            // Get the name and/or identifier of the node.
            Map<?, ?> dict = (Map<?, ?>) tree;
            String name = (String) dict
                    .get(HierarchicalChoicesTreeSpecifier.CHOICE_NAME);
            String identifier = (String) dict
                    .get(HierarchicalChoicesTreeSpecifier.CHOICE_IDENTIFIER);

            // Create the tree item for the node.
            TreeItem item = createTreeItem(parent, name, identifier);

            // If the newly-created node should have children, create them.
            List<?> children = (List<?>) dict
                    .get(HierarchicalChoicesTreeSpecifier.CHOICE_CHILDREN);
            if (children != null) {

                // Determine whether or not an old node at this level in the
                // old hierarchy with the same identifier was expanded, and
                // if so, expand the new node after creating the node's
                // children.
                String key = (identifier == null ? name : identifier);
                boolean expanded = (expandedMap == null ? false : expandedMap
                        .containsKey(key));
                NodesMap childExpandedMap = (expanded ? expandedMap.get(key)
                        : null);
                for (Object child : children) {
                    convertStateToTree(item, child, childExpandedMap);
                }
                item.setExpanded(expanded);
            }
            return item;
        }
    }

    /**
     * Turn the specified tree or tree item into a state hierarchy.
     * 
     * @param tree
     *            Tree to be converted; must be a <code>Tree</code> or a
     *            <code>TreeItem</code>.
     * @return State hierarchy resulting from the conversion.
     */
    private List<Object> convertTreeToState(Widget tree) {

        // Get the child items from this tree or tree item.
        TreeItem[] items = null;
        if (tree instanceof Tree) {
            items = ((Tree) tree).getItems();
        } else {
            items = ((TreeItem) tree).getItems();
        }

        // Iterate through the child items, adding each
        // one that is checked or grayed to the state
        // hierarchy, as well as any descendants that are
        // checked or grayed.
        List<Object> children = Lists.newArrayList();
        for (TreeItem item : items) {
            if ((item.getChecked() == false) && (item.getGrayed() == false)) {
                continue;
            }
            if (item.getItemCount() == 0) {
                children.add(item.getData());
            } else {
                Map<String, Object> node = Maps.newHashMap();
                node.put(HierarchicalChoicesTreeSpecifier.CHOICE_NAME,
                        item.getText());
                node.put(HierarchicalChoicesTreeSpecifier.CHOICE_IDENTIFIER,
                        item.getData());
                node.put(HierarchicalChoicesTreeSpecifier.CHOICE_CHILDREN,
                        convertTreeToState(item));
                children.add(node);
            }
        }
        return children;
    }

    /**
     * Create the specified tree item.
     * 
     * @param parent
     *            Parent tree (if the parameter is of type <code>Tree</code>) or
     *            tree item (if the parameter is of type <code>TreeItem</code).
     * @param name
     *            Choice name.
     * @param identifier
     *            Choice identifier, or <code>null</code> if none exists, in
     *            which case the name will be used as the identifier.
     * @return Created tree item.
     */
    private TreeItem createTreeItem(Widget parent, String name,
            String identifier) {
        TreeItem item = (parent instanceof Tree ? new TreeItem((Tree) parent,
                SWT.NONE) : new TreeItem((TreeItem) parent, SWT.NONE));
        item.setText(name);
        item.setData(identifier != null ? identifier : name);
        return item;
    }

    /**
     * Update the checked/grayed states of all non-leaf items in the specified
     * tree to reflect the states of the tree's leaves.
     * 
     * @param tree
     *            Tree to have its non-leaf items update their checked/grayed
     *            states to reflect the states of the leaves; must be a
     *            <code>Tree</code> or a <code>TreeItem</code>.
     */
    private void updateNonLeafStates(Widget tree) {

        // Get the child items from this tree or tree
        // item.
        TreeItem[] items = null;
        if (tree instanceof Tree) {
            items = ((Tree) tree).getItems();
        } else {
            items = ((TreeItem) tree).getItems();
        }

        // If there are no child items, this is a leaf,
        // so do nothing.
        if (items.length == 0) {
            return;
        }

        // Iterate through the children, ensuring each
        // has the right state based upon its descen-
        // dants, and then, if this "tree" is actually
        // a node, update the node's state as well.
        boolean checked = false, unchecked = false;
        for (TreeItem item : items) {
            updateNonLeafStates(item);
            if (tree instanceof Tree) {
                continue;
            }
            if (item.getGrayed()) {
                checked = unchecked = true;
            } else if (item.getChecked()) {
                checked = true;
            } else {
                unchecked = true;
            }
        }
        if ((tree instanceof TreeItem) && checked) {
            ((TreeItem) tree).setChecked(true);
            if (unchecked) {
                ((TreeItem) tree).setGrayed(true);
            }
        }
    }

    /**
     * Update the checked/grayed states of all ancestors of the specified item.
     * 
     * @param item
     *            Item for which the checked/grayed states of ancestors are to
     *            be updated.
     */
    private void updateAncestorStates(TreeItem item) {

        // Iterate upwards through the hierarchy from
        // this item, updating each ancestor in turn.
        for (item = item.getParentItem(); item != null; item = item
                .getParentItem()) {
            updateItemStateBasedUponChildrenStates(item);
        }
    }

    /**
     * Update the specified item's state based upon its children's states. If it
     * has no children, nothing is done.
     * 
     * @param item
     *            Item to be updated.
     * @return True if the item has children and had its state updated to go
     *         with their states, false if the item had no children and
     *         therefore did not have its state updated.
     */
    private boolean updateItemStateBasedUponChildrenStates(TreeItem item) {

        // If the item has no children, do nothing.
        if (item.getItemCount() == 0) {
            return false;
        }

        // Iterate through the item's children to find
        // out whether they are mixed in state. If they
        // are, make the parent item grayed in state;
        // otherwise, make the parent item checked or
        // unchecked depending upon which state the
        // children all have.
        boolean checked = false, unchecked = false;
        for (int j = 0; j < item.getItemCount(); j++) {
            if (item.getItem(j).getGrayed()) {
                checked = unchecked = true;
            } else if (item.getItem(j).getChecked()) {
                checked = true;
            } else {
                unchecked = true;
            }
            if (checked && unchecked) {
                break;
            }
        }
        if (checked && unchecked) {
            item.setChecked(true);
            item.setGrayed(true);
        } else {
            item.setGrayed(false);
            item.setChecked(checked);
        }
        return true;
    }

    /**
     * Update the checked/grayed states of all descendants of the specified
     * item.
     * 
     * @param item
     *            Item for which the checked/grayed states of descendants are to
     *            be updated.
     */
    private void updateDescendantStates(TreeItem item) {
        boolean checked = item.getChecked();
        for (int j = 0; j < item.getItemCount(); j++) {
            TreeItem childItem = item.getItem(j);
            childItem.setChecked(checked);
            childItem.setGrayed(false);
            updateDescendantStates(childItem);
        }
    }

    /**
     * Record any nodes (items) that are currently expanded, returning a nodes
     * map of any that are.
     * 
     * @param items
     *            Items to be checked to see if they are expanded.
     * @param map
     *            Map in which to place any expanded items, or <code>null
     *            </code> if the map should be created only if expanded nodes
     *            are found.
     * @return Provided map of expanded nodes, or if no such map was provided,
     *         then a newly created map containing expanded nodes, or
     *         <code>null</code> if no expanded nodes were found.
     */
    private NodesMap recordExpandedNodes(TreeItem[] items, NodesMap map) {
        for (TreeItem item : items) {
            if (item.getExpanded()) {
                if (map == null) {
                    map = new NodesMap();
                }
                NodesMap childMap = recordExpandedNodes(item.getItems(), null);
                map.put((String) item.getData(), childMap);
            }
        }
        return map;
    }
}