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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

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
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see HierarchicalChoicesTreeSpecifier
 */
public class HierarchicalChoicesTreeMegawidget extends
        HierarchicalChoicesMegawidget {

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

        // Create a panel in which to place the widgets.
        // This is needed in order to group the widgets pro-
        // perly into a single megawidget.
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.marginWidth = 0;
        panel.setLayout(gridLayout);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.horizontalSpan = specifier.getWidth();
        gridData.verticalIndent = specifier.getSpacing();
        panel.setLayoutData(gridData);

        // Add a label if one is required.
        if ((specifier.getLabel() != null)
                && (specifier.getLabel().length() > 0)) {

            // Create a label widget.
            label = new Label(panel, SWT.NONE);
            label.setText(specifier.getLabel());
            label.setEnabled(specifier.isEnabled());

            // Place the label in the grid.
            label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        } else {
            label = null;
        }

        // Create a tree to hold the checkable choices hier-
        // archy, and add said choices.
        tree = new Tree(panel, SWT.BORDER + SWT.CHECK);
        tree.setLinesVisible(false);
        tree.setHeaderVisible(false);
        tree.setEnabled(specifier.isEnabled());
        gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = (specifier.getNumVisibleLines() * tree
                .getItemHeight()) + 7;
        tree.setLayoutData(gridData);
        for (Object choice : specifier.getChoices()) {
            convertStateToTree(tree, choice);
        }

        // Add the Select All and Select None buttons, if appropriate.
        if (specifier.shouldShowAllNoneButtons()) {
            Composite allNoneContainer = new Composite(panel, SWT.FILL);
            FillLayout fillLayout = new FillLayout();
            fillLayout.spacing = 10;
            fillLayout.marginWidth = 10;
            fillLayout.marginHeight = 5;
            allNoneContainer.setLayout(fillLayout);
            allButton = new Button(allNoneContainer, SWT.PUSH);
            allButton.setText("  All  ");
            allButton.setEnabled(specifier.isEnabled()
                    && specifier.isEditable());
            allButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    HierarchicalChoicesTreeSpecifier specifier = getSpecifier();
                    state.clear();
                    state.addAll(specifier.choicesList);
                    setAllItemsCheckedState(
                            HierarchicalChoicesTreeMegawidget.this.tree
                                    .getItems(), true);
                    notifyListeners();
                }
            });
            noneButton = new Button(allNoneContainer, SWT.PUSH);
            noneButton.setText("  None  ");
            noneButton.setEnabled(specifier.isEnabled()
                    && specifier.isEditable());
            noneButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    state.clear();
                    setAllItemsCheckedState(
                            HierarchicalChoicesTreeMegawidget.this.tree
                                    .getItems(), false);
                    notifyListeners();
                }
            });
            allNoneContainer.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
                    true, false));
        } else {
            allButton = noneButton = null;
        }

        // Bind check events to trigger a change in the
        // record of the state for the widget.
        tree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                // If this is not a check event, do
                // nothing.
                if (e.detail != SWT.CHECK) {
                    return;
                }

                // If the widget is editable, record
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

    // Protected Methods

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
        if (label != null) {
            label.setEnabled(enable);
        }
        tree.setEnabled(enable);
        if (allButton != null) {
            allButton.setEnabled(isEditable() && enable);
            noneButton.setEnabled(isEditable() && enable);
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
        tree.setBackground(getBackgroundColor(editable, tree, label));
        if (allButton != null) {
            allButton.setEnabled(isEnabled() && editable);
            noneButton.setEnabled(isEnabled() && editable);
        }
    }

    /**
     * Synchronize the user-facing widgets making up this megawidget to the
     * current state.
     */
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
     * Clear the selection of all items in the specified tree.
     * 
     * @param tree
     *            Tree (if the parameter is of type <code> Tree</code>) or tree
     *            item (if of type <code>TreeItem</code>) to have the selection
     *            state of all its items cleared.
     */
    private void clearSelection(Object tree) {

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
    private void setLeafSelectionToMatchState(Object tree, List<?> state) {

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
     * Turn the specified trees into a single hierarchy of tree items. If there
     * are two trees, combine them, using the second one's choices as long
     * versions of the first one's choices.
     * 
     * @param parent
     *            Parent tree (if the parameter is of type <code>Tree</code>) or
     *            tree item (if the parameter is of type <code>
     *                TreeItem</code).
     * @param tree
     *            State hierarchy to be converted.
     * @return Tree item hierarchy resulting from the conversion.
     */
    private TreeItem convertStateToTree(Object parent, Object tree) {
        if (tree instanceof String) {
            return createTreeItem(parent, (String) tree, null);
        } else {
            Map<?, ?> dict = (Map<?, ?>) tree;
            String name = (String) dict
                    .get(HierarchicalChoicesTreeSpecifier.CHOICE_NAME);
            String identifier = (String) dict
                    .get(HierarchicalChoicesTreeSpecifier.CHOICE_IDENTIFIER);
            TreeItem item = createTreeItem(parent, name, identifier);

            List<?> children = (List<?>) dict
                    .get(HierarchicalChoicesTreeSpecifier.CHOICE_CHILDREN);
            if (children != null) {
                for (Object child : children) {
                    convertStateToTree(item, child);
                }
            }
            return item;
        }
    }

    /**
     * Turn the specified tree into a state hierarchy.
     * 
     * @param tree
     *            Tree to be converted; must be a <code>
     *              Tree</code> or a <code>TreeItem</code>.
     * @return State hierarchy resulting from the conversion.
     */
    private List<Object> convertTreeToState(Object tree) {

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
        List<Object> children = new ArrayList<Object>();
        for (TreeItem item : items) {
            if ((item.getChecked() == false) && (item.getGrayed() == false)) {
                continue;
            }
            if (item.getItemCount() == 0) {
                children.add(item.getData());
            } else {
                Map<String, Object> node = new HashMap<String, Object>();
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
    private TreeItem createTreeItem(Object parent, String name,
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
    private void updateNonLeafStates(Object tree) {

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
}