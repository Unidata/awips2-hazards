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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Description: Choice button (checkbox or radio button) component for use as
 * visual elements within megawidgets.
 * <p>
 * This class is a hack to get around the fact that checkboxes and radio buttons
 * in SWT cannot have their actual graphical button portions given a read-only
 * (gray) background. It gets around this by having both a button and a label.
 * When the button is editable, the label is hidden. Otherwise, the button is
 * shortened to not show its label and is disabled; meanwhile, the label is
 * shown to its right.
 * </p>
 * <p>
 * Note that the {@link Button} component widget created by an instance of this
 * class has its data set to the latter, i.e. calling {@link Button#getData()}
 * on the <code>Button</code> will yield the instance of this class that created
 * (and that includes) the <code>Button</code>.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 12, 2014    3982    Chris.Golden Initial creation.
 * Jun 23, 2014    4010    Chris.Golden Changed to allow the option of not
 *                                      having its component widgets ask to take
 *                                      up extra space when more vertical space
 *                                      is available.
 * Apr 17, 2017   32734    Kevin.Bisanz Add null/disposed checks on choiceButton.
 * Jun 28, 2017   35648    Robert.Blum  Preventing widget dispose error.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ChoiceButtonComponent {

    // Private Variables

    /**
     * Choice button.
     */
    private final Button choiceButton;

    /**
     * Read-only label.
     */
    private final Label readOnlyLabel;

    /**
     * Grid data for the containing panel.
     */
    private final GridData containerGridData;

    /**
     * Grid data for the button.
     */
    private final GridData buttonGridData;

    /**
     * Grid data for the label.
     */
    private final GridData labelGridData;

    /**
     * Identifier of the choice.
     */
    private final String choice;

    /**
     * Flag indicating whether or not the component is enabled.
     */
    private boolean enabled;

    /**
     * Flag indicating whether or not the component is editable.
     */
    private boolean editable = true;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parent
     *            Parent of this component.
     * @param radioButton
     *            Flag indicating whether or not the button to be created is a
     *            radio button. If false, it is to be a checkbox.
     * @param buttonFlags
     *            Flags from {@link SWT} to be passed to the button when it is
     *            constructed.
     * @param enabled
     *            Flag indicating whether or not the component should start off
     *            as enabled.
     * @param expandVertically
     *            Flag indicating whether or not the component should expand to
     *            fill any available vertical space.
     * @param choice
     *            Choice identifier, if any.
     * @param description
     *            Label description.
     */
    public ChoiceButtonComponent(Composite parent, boolean radioButton,
            int buttonFlags, boolean enabled, boolean expandVertically,
            String choice, String description) {
        this.choice = choice;

        /*
         * Create the panel in which the button and the read-only label are to
         * be placed.
         */
        Composite panel = new Composite(parent,
                (radioButton ? SWT.NO_RADIO_GROUP : SWT.NONE));
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = layout.marginWidth = layout.marginHeight = 0;
        panel.setLayout(layout);
        containerGridData = new GridData(SWT.LEFT, SWT.CENTER, false,
                expandVertically);
        panel.setLayoutData(containerGridData);

        /*
         * Create the choice button itself.
         */
        choiceButton = new Button(panel, buttonFlags);
        choiceButton.setText(description == null ? "" : description);
        choiceButton.setEnabled(enabled);
        choiceButton.setData(this);
        this.enabled = enabled;
        buttonGridData = new GridData(SWT.LEFT, SWT.CENTER, false, true);
        buttonGridData.horizontalSpan = 2;
        choiceButton.setLayoutData(buttonGridData);

        /*
         * If there is a description to be shown next to the choice button,
         * create a label. If not, set the button to be its asked-for width
         * minus a magic number that makes up for the right-side padding SWT
         * seems to add at different font sizes.
         */
        if (choiceButton.getText().length() > 0) {
            readOnlyLabel = new Label(panel, SWT.NONE);
            readOnlyLabel.setText(choiceButton.getText());
            labelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, true);
            labelGridData.exclude = true;
            readOnlyLabel.setLayoutData(labelGridData);
        } else {
            buttonGridData.widthHint = choiceButton.computeSize(SWT.DEFAULT,
                    SWT.DEFAULT).x - 6;
            readOnlyLabel = null;
            labelGridData = null;
        }
    }

    // Public Methods

    /**
     * Get the grid data for the button.
     * 
     * @return Grid data for the button.
     */
    public GridData getGridData() {
        return containerGridData;
    }

    /**
     * Get the choice identifier.
     * 
     * @return Choice identifier.
     */
    public String getChoice() {
        return choice;
    }

    /**
     * Get the button widget used by this component.
     * 
     * @return Button widget.
     */
    public Button getButton() {
        return choiceButton;
    }

    /**
     * Get the width of the button minus any text.
     * 
     * @return Width of the button minus any text.
     */
    public int getButtonNonTextWidth() {

        /*
         * Determine the difference in widths between the button and the label.
         * Since there seems to be a little extra padding with the button in SWT
         * at different font sizes, subtract a magic number from the button's
         * width.
         */
        Point checkBoxSize = choiceButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (readOnlyLabel != null) {
            Point labelSize = readOnlyLabel.computeSize(SWT.DEFAULT,
                    SWT.DEFAULT);
            return checkBoxSize.x - (labelSize.x + 2);
        }
        return checkBoxSize.x - 2;
    }

    /**
     * Add the specified selection listener.
     * 
     * @param listener
     *            Selection listener.
     */
    public void addSelectionListener(SelectionListener listener) {
        choiceButton.addSelectionListener(listener);
    }

    /**
     * Set the enabled state.
     * 
     * @param enable
     *            Flag indicating whether or not the component is enabled.
     */
    public void setEnabled(boolean enable) {
        this.enabled = enable;
        if ((choiceButton != null) && (choiceButton.isDisposed() == false)) {
            choiceButton.setEnabled(enable && editable);
        }
        if (readOnlyLabel != null) {
            readOnlyLabel.setEnabled(enable);
        }
    }

    /**
     * Set the editable state.
     * 
     * @param editable
     *            Flag indicating whether or not the component is editable.
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
        if ((choiceButton != null) && (choiceButton.isDisposed() == false)) {
            choiceButton.setEnabled(enabled && editable);
            if (readOnlyLabel != null && readOnlyLabel.isDisposed() == false) {
                buttonGridData.horizontalSpan = (editable ? 2 : 1);
                buttonGridData.widthHint = (editable
                        ? choiceButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x
                        : getButtonNonTextWidth());
                labelGridData.exclude = editable;
                choiceButton.getParent().layout();
            }
        }
    }

    /**
     * Set the checked state.
     * 
     * @param checked
     *            Flag indicating whether or not the button is checked.
     */
    public void setChecked(boolean checked) {
        if ((choiceButton != null) && (choiceButton.isDisposed() == false)) {
            choiceButton.setSelection(checked);
        }
    }

    /**
     * Receive notification that a parent's minimum height changed.
     * 
     * @param minimumHeight
     *            New minimum height for the parent.
     */
    public void parentMinimumHeightChanged(int minimumHeight) {
        if (minimumHeight > 0) {
            int diff = minimumHeight
                    - choiceButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
            if (diff > 0) {
                buttonGridData.verticalIndent = diff % 2;
            }
        }
    }
}
