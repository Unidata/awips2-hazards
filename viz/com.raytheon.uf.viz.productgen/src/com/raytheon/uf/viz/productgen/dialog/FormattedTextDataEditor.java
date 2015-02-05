/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.productgen.dialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * 
 * The FormattedTextDataEditor class encapsulates information necessary for
 * coloring and correctly editing text in the formatted text tab in the Product
 * Editor.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * 01/15/2015   5109       bphillip     Initial creation
 * 02/05/2015   6322       Robert.Blum  Changed return value if editableRanges is empty.
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class FormattedTextDataEditor extends AbstractDataEditor {

    /** The logger */
    private static final IUFStatusHandler handler = UFStatus
            .getHandler(FormattedTextDataEditor.class);

    /** Label for the issue button */
    private static final String ISSUE_BUTTON_LABEL = "Issue";

    /**
     * The key value used to get/set the original text value on the styled text
     * object
     */
    protected static final String ORIGINAL_TEXT_KEY = "Original Text";

    /**
     * Special characters that must be escaped when used with regular expression
     * processing
     */
    private static final List<Character> SPECIAL_CHARS = Arrays.asList('<',
            '(', '[', '{', '\\', '^', '-', '=', '$', '!', '|', ']', '}', ')',
            '?', '*', '+', '.', '>');

    /** The name of the formatter used to format the data */
    private String format;

    /**
     * The index into the list of formatted texts for the format in the
     * generated product
     */
    private int formatIndex;

    /** The text content of the formatted product tab */
    private FormattedStyledText styledText;

    /** The issue button */
    private Button issueButton;

    /**
     * List of index ranges which contain editable information in the formatted
     * text
     */
    private List<EditableRange> editableRanges = new ArrayList<EditableRange>();

    /**
     * Creates a new FormattedTextDataEditor object
     * 
     * @param composite
     *            The composite container
     * @param formattedText
     *            The formatted text
     * @param format
     *            The formatter used to create the formatted text
     * @param textColor
     *            The color of the editable text
     * @param uneditableTextColor
     *            The color of the uneditable text
     * @param backgroundColor
     *            The background color of the text area containing the formatted
     *            text
     */
    protected FormattedTextDataEditor(ProductEditor productEditor,
            IGeneratedProduct product, CTabFolder parent, int style,
            String format, int formatIndex) {
        super(productEditor, product, parent, style);
        this.format = format;
        this.formatIndex = formatIndex;
        this.editableKeys = new EditableKeys(product);
    }

    @Override
    protected void initializeSubclass() {
        // Create a new StyledText object containing the formatted text
        this.styledText = new FormattedStyledText(editorPane, SWT.H_SCROLL
                | SWT.V_SCROLL);

        // Attach the listeners to listen for editing events on the style text
        addStyledTextListeners();

        String formattedText = (String) product.getEntries().get(format)
                .get(formatIndex);
        this.styledText.setWordWrap(false);
        this.styledText.setAlwaysShowScrollBars(false);
        this.styledText.setForeground(getDisplay().getSystemColor(
                SWT.COLOR_BLUE));
        this.styledText.setText(formattedText, false);
        this.styledText.setData(ORIGINAL_TEXT_KEY, formattedText);
        ProductEditorUtil.setLayoutInfo(this.styledText, 1, false, SWT.FILL,
                SWT.FILL, true, true, 500, 300);

        /*
         * Generate the label for the formatted text tab if the list contains
         * more than one value. This is special case handling for CAP products
         * that do not segment but rather have separate results
         */
        String formattedTextTabLabel = format;
        if (product.getEntry(format).size() > 1) {
            formattedTextTabLabel = ProductEditorUtil.getFormattedTextTabLabel(
                    product, format, formatIndex);
        }
        setText(formattedTextTabLabel);
        markUneditableText();
    }

    @Override
    protected void createEditorButtons(final Composite editorPane) {
        super.createEditorButtons(editorPane);
        issueButton = new Button(editorButtonPane, SWT.PUSH);

        /*
         * Configure Save button
         */
        issueButton.setText(ISSUE_BUTTON_LABEL);
        ProductEditorUtil.setButtonGridData(issueButton);
        issueButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveModifiedValues();
                disableSaveButton();
                disableRevertButton();
                productEditor.issue(product.getProductID(), format);
            }
        });

        // Editor save button is initially enabled
        issueButton.setEnabled(true);
        
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageDialog warningBox = new MessageDialog(editorPane.getShell(), "Save Unsupported",
                        null, "Saving individual formats currently unimplemented",
                        0, new String[]{"OK"}, 0);
                warningBox.open();
            }
        });
    }

    /**
     * Adds listeners to the styled text object to listen for edit events
     */
    private void addStyledTextListeners() {
        /*
         * Add a verify key listener. This listener handles key strokes entered
         * by the user.
         */
        this.styledText.addVerifyKeyListener(new VerifyKeyListener() {

            @Override
            public void verifyKey(VerifyEvent event) {
                verifyKeyPress(event);
            }
        });

        /*
         * Add a verify listener. This listener is added primarily to prevent
         * the user from pasting (Ctrl+V) text into uneditable regions of the
         * formatted text. It also verifies that programmatic text updates to
         * the styled text object are valid.
         */
        this.styledText.addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent event) {
                event.doit = isTextAtIndexEditable(event.start);
            }
        });

        /*
         * Add an extended modify listener. This listener is fired after the
         * text has already been modified by user actions or programmatic text
         * updates to the formatted text.
         */
        this.styledText.addExtendedModifyListener(new ExtendedModifyListener() {

            @Override
            public void modifyText(ExtendedModifyEvent event) {
                modifyEditableRangesAndColorText(event);

            }
        });
    }

    public void refresh() {
        // Clears the editable ranges so they can be regenerated
        clearRanges();

        /*
         * Sets the formatted text into the StyledText object.
         */
        String newText = product.getEntries().get(format).get(formatIndex)
                .toString();
        styledText.setText(newText, false);
        styledText.setData(ORIGINAL_TEXT_KEY, newText);
        disableButtons();
        markUneditableText();
    }

    /**
     * Colors the formatted text based on the editable/non-editable product
     * parts contained in the generated product.
     * <p>
     * The styled text is by default colored blue (denoting uneditable text).
     * This method searches the formatted text for the editable product parts
     * and colors them in the editable color
     * 
     * @param formatIndex
     *            Since the generated product may contain multiple entries which
     *            were formatted using the same formatter, the formatIndex is
     *            used to get the correct item from the entry list for that
     *            format.
     */
    private void markUneditableText() {

        // Get the editable parts of the formatted text
        Map<String, Serializable> editableParts = product.getEditableEntries()
                .get(format);

        // If nothing is editable in this product, early return
        if (editableParts == null) {
            handler.info("No editable parts were specified for format ["
                    + format + "]");
            return;
        }
        int index = 0;
        int length = 0;
        Pattern pattern = null;
        Matcher matcher = null;

        /*
         * Generate the list of editable ranges. This loop does the following:
         * 1. Iterates over the editable values contained in the product 2.
         * Finds them in the formatted text 3. Calculates the editable range
         * based on the index in which it was found and the text length 4. Adds
         * the range to the editableRange list
         */
        for (Serializable value : editableParts.values()) {
            int lastIndex = 0;
            pattern = createEscapedPattern(value.toString());
            matcher = pattern.matcher(styledText.getText());
            while (matcher.find()) {
                index = styledText.getText().indexOf(matcher.group(),
                        lastIndex + 1);
                if (index == -1) {
                    continue;
                }
                length = matcher.group().length();
                if (length > 0) {
                    addRange(index, index + length);
                }
                lastIndex = index;
            }
        }

        /*
         * Colors the formatted text based on the newly constructed list of
         * editable ranges
         */
        colorText();
    }

    @Override
    public boolean hasUnsavedChanges() {
        return !this.styledText.getText().equals(getOriginalText());
    }

    public boolean isDataEditable() {
        return true;
    }

    public boolean requiredFieldsCompleted() {
        return true;
    }

    public void saveModifiedValues() {
        // TODO: Implement saving of modified formatted data
    }

    @Override
    public void revertValues() {
        String originalText = (String) this.styledText
                .getData(ORIGINAL_TEXT_KEY);
        this.styledText.setText(originalText);
        refresh();
    }

    /**
     * Helper method used to filter out special characters for regular
     * expression searching in the markUneditableText method above.
     * 
     * @param input
     *            The text string to sanitize
     * @return The sanitized string
     */
    private Pattern createEscapedPattern(String input) {

        // Container to hold the new escaped string
        StringBuilder modifiedPattern = new StringBuilder();

        /*
         * Iterate over each character in the input string to look for special
         * characters. If one is found, wrap it in quote tags
         */
        for (int i = 0; i < input.length(); i++) {
            Character currentChar = input.charAt(i);

            /*
             * If the current character is a special character, wrap it in the
             * quote tags
             */
            if (SPECIAL_CHARS.contains(currentChar)) {
                modifiedPattern.append(Pattern.quote(currentChar.toString()));
            }
            /*
             * If the current character is not a space character, then append it
             * the new string. Whitespace characters are not appended as they
             * are handled below.
             */
            else if (!Character.isSpaceChar(currentChar)) {
                modifiedPattern.append(input.charAt(i));
            }
            /*
             * Insert a clause for an indeterminate amount of whitespace
             * characters. This is necessary since the formatted text may
             * contain an arbitrary number of spaces between words. Line breaks
             * may also be present in arbitrary locations as well. This must be
             * taken into account or the regex will fail to find the pattern.
             */
            modifiedPattern.append("\\s*");
        }

        /*
         * Return the case-insensitive pattern
         */
        return Pattern.compile(modifiedPattern.toString(),
                Pattern.CASE_INSENSITIVE);
    }

    /**
     * Adds a new range to the list of editable ranges. This method sorts and
     * combines the range list after each range is added so as to maintain an
     * ordered list at all times.
     * 
     * @param startIndex
     *            The start index of the range
     * @param endIndex
     *            The end index of the range
     */
    public void addRange(int startIndex, int endIndex) {
        editableRanges.add(new EditableRange(startIndex, endIndex));
        sortAndCombine();
    }

    /**
     * Modifies the range at the given index by the specified amount. After the
     * range at the given index is modified, all ranges occurring later in the
     * editable range list are also modified accordingly.
     * 
     * @param index
     *            The index of the range to be modified
     * @param amount
     *            The amount to modify the given range
     */
    public void modifyRangeEnd(int index, int amount) {

        // Boolean denoting if an editable range has been found at the given
        // index
        boolean found = false;
        for (EditableRange range : editableRanges) {
            /*
             * If an editable range has been discovered at the given index, then
             * all subsequent ranges must have their start and end indices
             * updated to maintain correct text coloring
             */
            if (found) {
                range.start += amount;
                range.end += amount;
            }
            // A range has been discovered at the given index
            else if (range.contains(index, index)) {
                range.end += amount;
                found = true;
            }
        }
        // Re-sort the editable range list if a range was found
        if (found) {
            sortAndCombine();
        }
    }

    /**
     * Checks if the text inclusively between the given indices is editable
     * 
     * @param startIndex
     *            The start index to check
     * @param endIndex
     *            The end index to check.
     * @return True if all text contained inclusively in the given range indices
     *         is editable.
     */
    public boolean isTextRangeEditable(int startIndex, int endIndex) {
        if (editableRanges.isEmpty()) {
            return false;
        }
        // Iterate through the editable ranges to determine if they are both
        // contained in an editable range
        for (EditableRange range : editableRanges) {
            if (range.contains(startIndex, endIndex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the text inclusively between the given indices (given as a
     * Point object) is editable
     * 
     * @param range
     *            A Point containing the start index as the x coordinate and the
     *            end index as the y coordinate
     * @return True if all text contained inclusively in the given range indices
     *         is editable.
     */
    public boolean isTextRangeEditable(Point range) {
        return isTextRangeEditable(range.x, range.y);
    }

    /**
     * Checks if the text at the given index is editable
     * 
     * @param index
     *            The index to check
     * @return True if the text at the given index is editable
     */
    public boolean isTextAtIndexEditable(int index) {
        return isTextRangeEditable(index, index);
    }

    /**
     * Clears the editable range list
     */
    public void clearRanges() {
        editableRanges.clear();
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();
        enableIssueButton();
    }

    /**
     * Enables the issue button
     */
    protected void enableIssueButton() {
        setButtonEnabled(this.issueButton, true);
    }

    /**
     * Disables the save button
     */
    protected void disableIssueButton() {
        setButtonEnabled(this.issueButton, false);
    }

    /**
     * Sorts the editable ranges by start index and combines any ranges that
     * overlap. This method is used to maintain an ordered list of editable
     * ranges since the StyleText object requires an array of non-overlapping
     * StyleRange objects in ascending order.
     */
    private void sortAndCombine() {
        // Begin with an ordered list of ranges
        Collections.sort(editableRanges);
        EditableRange current = null;
        EditableRange next = null;
        boolean combine = false;
        do {
            /*
             * Boolean signifying that overlapping ranges have been combined on
             * this iteration. This is the terminating condition of the loop.
             * When an entire iteration of the list has been completed without
             * combining any ranges, the list is in ascending order with no
             * overlapping ranges
             */
            combine = false;

            // Iterate through the editable ranges to find overlaps
            for (int i = 0; i < editableRanges.size() - 1; i++) {
                current = editableRanges.get(i);
                next = editableRanges.get(i + 1);

                /*
                 * If the end of the current range overlaps the beginning of the
                 * next range, remove both ranges and replace them with a single
                 * combined range
                 */
                if (current.end >= next.start) {
                    editableRanges.remove(i);
                    editableRanges.remove(i);
                    editableRanges.add(i, new EditableRange(current.start,
                            next.end));
                    combine = true;
                }
            }
        } while (combine);
    }

    /**
     * This method is executed when a VerifyKeyEvent is received by the
     * VerifyKeyListener on the StyledText object defined in the constructor.
     * <p>
     * This method verifies that the text at the current cursor location is
     * editable. If text entry occurred at an uneditable point in the text, the
     * action is deemed invalid an discarded. If a non-printable character is
     * entered (i.e. arrow keys, Page Up/Down, etc.) the action is allowed even
     * if the cursor is located in an uneditable location.
     * <p>
     * Two special cases are handled by this method. The first is the Backspace
     * key. Since the Backspace key modifies the character immediately preceding
     * the cursor, the editable state of that character is checked instead of
     * the character at the current cursor location. Similarly, the Delete key
     * modifies the character immediately in front of the current cursor
     * location, so the editable state of that character is checked.
     * 
     * @param event
     *            The event object containing details of the proposed
     *            modification
     */
    private void verifyKeyPress(VerifyEvent event) {
        boolean isEditable = false;

        // Determine if the key pressed is a printable character
        Character.UnicodeBlock block = Character.UnicodeBlock
                .of(event.character);
        boolean printable = (!Character.isISOControl(event.character))
                && Character.isDefined(event.character) && block != null
                && block != Character.UnicodeBlock.SPECIALS;

        /*
         * This block handles the most common situation where a key is typed
         * with one or zero characters highlighted.
         */
        if (styledText.getSelectionCount() == 0) {
            int modificationIndex = styledText.getSelection().x;
            /*
             * Special handling of Backspace key. Backspace key modifies one
             * character behind
             */
            if (event.keyCode == 8) {
                modificationIndex--;
            }
            /*
             * Special handling of Delete key Delete key modifies one character
             * ahead
             */
            else if (event.keyCode == 127) {
                modificationIndex++;
            }

            // Determine if the text at the current index is editable
            isEditable = isTextAtIndexEditable(modificationIndex);

            /*
             * Permit the change if the text at the current point is editable or
             * it is a non printable character at a non-editable point in the
             * text, with the exception of the Backspace and Delete keys
             */
            event.doit = (isEditable || (!isEditable && !printable
                    && event.keyCode != 8 && event.keyCode != 127 && event.keyCode != 0));
        }

        /*
         * This block handles if text containing more than one character is
         * highlighted when a key is pressed
         */
        else {
            event.doit = isTextRangeEditable(styledText.getSelection());
        }
    }

    /**
     * This method is executed when an ExtendedModifyEvent is received by the
     * ExtendedModifyListener on the StyledText object defined in the
     * constructor.
     * <p>
     * This method modifies the editable ranges to maintain correct text
     * coloring. The following cases are handled by this method: <br>
     * <ul>
     * <li>Character deleted</li>
     * <li>Character replaced</li>
     * <li>Character inserted</li>
     * <li>String inserted</li>
     * <li>Selection replaced with single character</li>
     * <li>Selection replaced with string</li>
     * <li>Selection deleted</li>
     * <ul>
     * <p>
     * <p>
     * This method is only called after the action has been verified by the
     * verify listeners on the StyledText object
     * 
     * @param event
     *            The event object containing details about the modification
     */
    private void modifyEditableRangesAndColorText(ExtendedModifyEvent event) {

        // The length of the replaced text
        int replacedTextLength = event.replacedText.length();

        // The index where the modification took place
        int modificationIndex = event.start;

        // How much to modify the editable range at the given index
        int rangeModification = 0;

        // Character deleted
        if (replacedTextLength == 1 && event.length == 0) {
            rangeModification = -1;
        }
        // Character replaced
        else if (replacedTextLength == 1 && event.length == 1) {
            // No op but retaining for code clarity
        }
        // Character inserted
        else if (replacedTextLength == 0 && event.length == 1) {
            rangeModification = 1;
        }
        // String inserted
        else if (replacedTextLength == 0 && event.length > 1) {
            rangeModification = event.length;
        }
        // Selection replaced with single character
        else if (replacedTextLength > 1 && event.length == 1) {
            rangeModification = -(replacedTextLength - 1);
        }
        // Selection replaced with string
        else if (replacedTextLength > 1 && event.length > 1) {
            rangeModification = event.length - replacedTextLength;
        }
        // Selection deleted
        else if (replacedTextLength > 1 && event.length == 0) {
            rangeModification = -replacedTextLength;
        }

        // Modify the editable range if necessary
        if (rangeModification != 0) {
            modifyRangeEnd(modificationIndex, rangeModification);
            // Color the text based on the new editable ranges
            colorText();
        }

        enableButtons();
    }

    /**
     * Colors the formatted text using the list of editable ranges.
     */
    private void colorText() {

        // If no editable ranges exists, return
        if (editableRanges == null || editableRanges.isEmpty()) {
            return;
        }

        /*
         * Convert the list of editable ranges into StyleRange objects to be
         * assigned to the StyledText object.
         */
        StyleRange[] styleRanges = new StyleRange[editableRanges.size()];
        for (int i = 0; i < editableRanges.size(); i++) {
            styleRanges[i] = new StyleRange(editableRanges.get(i).start,
                    editableRanges.get(i).length(), getDisplay()
                            .getSystemColor(SWT.COLOR_BLACK), getDisplay()
                            .getSystemColor(SWT.COLOR_WHITE));
        }
        styledText.setStyleRanges(styleRanges);
    }

    /**
     * Gets the StyledText object
     * 
     * @return The StyledText object
     */
    public StyledText getStyledText() {
        return styledText;
    }

    /**
     * Gets the original text of the formatted product prior to any
     * modifications
     * 
     * @return The original formatted text
     */
    private String getOriginalText() {
        return (String) this.styledText.getData(ORIGINAL_TEXT_KEY);
    }

    /**
     * Gets the format associated with this editor
     * 
     * @return The format
     */
    public String getFormat() {
        return format;
    }

    protected int getButtonCount() {
        return 3;
    }

    /**
     * Class representing an index range.
     * 
     * @author bphillip
     * 
     */
    class EditableRange implements Comparable<EditableRange> {

        /** The start index of the range */
        private int start;

        /** The end index of the range */
        private int end;

        /**
         * Constructs a new Range object with the provided start and end indices
         * 
         * @param start
         *            The start index
         * @param end
         *            The end index
         */
        public EditableRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        /**
         * Gets the length of the range
         * 
         * @return The length of the range
         */
        public int length() {
            return end - start;
        }

        /**
         * Checks if the given range, represented by a start and end index, is
         * contained inclusively in this range
         * 
         * @param start
         *            The start index of the range to check
         * @param end
         *            The end index of the range to check
         * @return True if the range is inclusively contained in this range
         */
        public boolean contains(int start, int end) {
            return start >= this.start && end <= this.end;
        }

        @Override
        public int compareTo(EditableRange o) {
            if (this.start < o.start) {
                return -1;
            } else if (this.start > o.start) {
                return 1;
            }
            return 0;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Start: ").append(this.start).append(" End: ")
                    .append(this.end).append(" Length: ").append(this.length());

            return builder.toString();
        }
    }

    class FormattedStyledText extends StyledText {

        public FormattedStyledText(Composite parent, int style) {
            super(parent, style);
        }

        public void setText(String text, boolean notifyListeners) {
            if (notifyListeners) {
                super.setText(text);
            } else {
                this.getContent().setText(text);
            }
        }
    }
}
