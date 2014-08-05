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

import net.sf.swtaddons.autocomplete.AutocompleteContentProposalProvider;
import net.sf.swtaddons.autocomplete.combo.AutocompleteComboSelector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Description: Helper class for handling some of the grunt work of creating and
 * configuring {@link Combo} components.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 04, 2014    4122    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ComboBoxComponentHelper {

    // Private Classes

    /**
     * Autocomplete combo box selector, used to implement autocomplete.
     */
    private class AutocompleteSelector extends AutocompleteComboSelector {

        // Private Classes

        /**
         * Content proposal provider that tries to match strings to any
         * substring within the possible matches, not just the beginning of the
         * latter.
         */
        private class ContentProposalProvider extends
                AutocompleteContentProposalProvider {

            // Public Constructors

            /**
             * Construct a standard instance.
             * 
             * @param proposals
             *            Proposals for this instance.
             */
            public ContentProposalProvider(String[] proposals) {
                super(proposals);
            }

            // Protected Methods

            @Override
            protected List<?> getMatchingProposals(String[] proposals,
                    String contents) {
                List<String> matches = new ArrayList<>();
                String lowerCaseContents = contents.toLowerCase();
                for (String proposal : proposals) {
                    if (proposal.toLowerCase().contains(lowerCaseContents)) {
                        matches.add(proposal);
                    }
                }
                return super.getMatchingProposals(
                        matches.toArray(new String[matches.size()]), "");
            }
        }

        // Public Constructors

        /**
         * Construct a standard instance to be used to handle the enclosing
         * class's combo box.
         */
        public AutocompleteSelector() {
            super(comboBox);
        }

        // Protected Methods

        @Override
        protected AutocompleteContentProposalProvider getContentProposalProvider(
                String[] proposals) {
            return new ContentProposalProvider(proposals);
        }
    }

    // Private Variables

    /**
     * Combo box.
     */
    private final Combo comboBox;

    /**
     * Flag indicating whether or not the combo box is of the autocomplete type.
     */
    private final boolean autocomplete;

    /**
     * Holder of the combo box component.
     */
    private final IComboBoxComponentHolder holder;

    /**
     * Label for the combo box, or <code>null</code> if there is none.
     */
    private final Label label;

    /**
     * Control component helper.
     */
    private final ControlComponentHelper helper;

    /**
     * Last valid selection.
     */
    private String lastSelection;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parent
     *            Parent of the combo box to be created.
     * @param holder
     *            Holder of this combo box component.
     * @param autocomplete
     *            Flag indicating whether or not the combo box is to support
     *            autocomplete.
     * @param helper
     *            Control component helper.
     */
    public ComboBoxComponentHelper(Composite parent,
            IComboBoxComponentHolder holder, boolean autocomplete,
            boolean enable, Label label, ControlComponentHelper helper) {
        comboBox = new Combo(parent, (autocomplete ? SWT.DROP_DOWN
                : SWT.READ_ONLY));
        if (autocomplete) {
            new AutocompleteSelector();
        }
        this.autocomplete = autocomplete;
        this.holder = holder;
        this.label = label;
        this.helper = helper;
        setEnabled(enable);

        /*
         * Bind the combo box selection event to trigger a change in the record
         * of the state for the widget.
         */
        comboBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleSelectionChange();
            }
        });
        if (autocomplete) {
            comboBox.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    if (e.character == SWT.CR) {
                        handleSelectionChange();
                    }
                }
            });
            comboBox.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    handleSelectionChange();
                }
            });
        }
    }

    // Public Methods

    /**
     * Get the combo box. This should only be used to query the combo box's
     * state, or to set its items or provide its layout parameters.
     * 
     * @return Combo box.
     */
    public Combo getComboBox() {
        return comboBox;
    }

    /**
     * Set the combo box to have the specified currently selected item.
     * 
     * @param item
     *            New item to be selected.
     */
    public void setSelection(String item) {
        if (item != null) {
            for (int j = 0; j < comboBox.getItemCount(); j++) {
                if (comboBox.getItem(j).equals(item)) {
                    comboBox.select(j);
                    lastSelection = item;
                    return;
                }
            }
        }
        lastSelection = null;
        comboBox.deselectAll();
    }

    /**
     * Enable or disable the combo box.
     * 
     * @param enable
     *            Flag indicating whether or not the combo box should be
     *            enabled.
     */
    public void setEnabled(boolean enable) {
        comboBox.setEnabled(enable);
    }

    /**
     * Render the combo box either editable or read-only.
     * 
     * @param editable
     *            Flag indicating whether the combo box should be editable.
     */
    public void setEditable(boolean editable) {
        if (autocomplete && (editable == false)) {
            comboBox.setSelection(new Point(0, 0));
        }
        comboBox.getParent().setEnabled(editable);
        comboBox.setBackground(editable ? null : helper.getBackgroundColor(
                editable, comboBox, label));
    }

    // Private Methods

    /**
     * Handle the selection having changed.
     */
    private void handleSelectionChange() {
        String text = comboBox.getText();
        if (text.equals(holder.getSelection()) == false) {
            for (String item : comboBox.getItems()) {
                if (item.equals(text)) {
                    lastSelection = text;
                    holder.setSelection(text);
                    return;
                }
            }
            comboBox.setText(lastSelection);
        }
    }
}
