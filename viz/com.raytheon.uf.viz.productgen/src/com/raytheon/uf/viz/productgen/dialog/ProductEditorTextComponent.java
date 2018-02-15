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

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.undoable.IUndoRedoManager;
import com.raytheon.uf.common.dataplugin.events.hazards.undoable.IUndoRedoable;
import com.raytheon.uf.viz.spellchecker.text.SpellCheckTextViewer;

import gov.noaa.gsd.viz.megawidgets.UiBuilder;
import gov.noaa.gsd.viz.widgets.ToolTipButton;

/**
 * Text Component for use on the Product Editor. It adds the additional
 * capability undo changes.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 23, 2017 29170      Robert.Blum Initial creation
 * Feb 27, 2017 29170      Robert.Blum Removing the coloring of framed text as it
 *                                     does not work well with the spellcheck underlining.
 * Mar 24, 2017 29170      Robert.Blum Fix bug where required labels were not displayed when
 *                                     doing corrections.
 * Apr 10, 2017 32735      Robert.Blum Adjusted how the StyledText are sized.
 * Jun 05, 2017 29996      Robert.Blum Updates for previous text design.
 *
 * </pre>
 *
 * @author Robert.Blum
 */

public class ProductEditorTextComponent extends TextComponent
        implements IUndoRedoable {

    private final String REQUIRED_FIELD_TEXT = " (* required field)";

    private IUndoRedoManager undoManager;

    private String prevValue;

    /**
     * Label indicating that this text component is required.
     */
    private Label requiredLabel;

    /**
     * Flag indicating whether or not the text component is required.
     */
    private boolean required;

    /**
     * Number of lines the TextViewer should be set to.
     */
    private Integer numLines;

    /**
     * Flag indicating whether or not previous text is being used.
     */
    private boolean usePrevText;

    /**
     * The check box for selecting previous text.
     */
    private ToolTipButton previousTextBtn;

    /**
     * The Editor holding this Text Component.
     */
    private ProductDataEditor editor;

    /**
     * The history of modifications made to this Text Component.
     */
    private final LinkedList<String> modificationHistory = new LinkedList<>();

    public ProductEditorTextComponent(Composite parent,
            ProductDataEditor editor, String identifier, String text,
            String displayLabel, IUndoRedoManager undoManager, boolean required,
            int numLines, boolean usePrevText) {
        super(parent, identifier, displayLabel, text);
        this.editor = editor;
        this.undoManager = undoManager;
        this.required = required;
        this.numLines = numLines;
        this.usePrevText = usePrevText;
        prevValue = text;
        initForProductEditor(text);
    }

    /**
     * Initialize method.
     * 
     * @param parent
     * 
     */
    @Override
    protected void init(Composite parent, String text) {
        GridLayout gl = new GridLayout(1, false);
        this.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gd.minimumHeight = 125;
        setLayoutData(gd);
    }

    private void initForProductEditor(String text) {
        createLabelComposite();
        createTextViewer(text);
    }

    @Override
    protected void createLabelComposite() {
        GC gc = new GC(getDisplay());
        FontMetrics fm = gc.getFontMetrics();

        Composite labelComp = new Composite(this, SWT.NONE);
        GridLayout gl = new GridLayout(3, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        labelComp.setLayout(gl);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        labelComp.setLayoutData(gd);

        previousTextBtn = new ToolTipButton(labelComp, SWT.CHECK,
                "Use Previous Text", "No Previous Text Available");
        GridData gd2 = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
        previousTextBtn.getComposite().setLayoutData(gd2);
        previousTextBtn.getButton().setSelection(usePrevText);
        previousTextBtn.getButton().setEnabled(usePrevText);
        previousTextBtn.getButton()
                .addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        boolean selected = previousTextBtn.getButton()
                                .getSelection();
                        String newValue = null;
                        if (selected) {
                            newValue = editor.getSavedText(
                                    ProductEditorTextComponent.this);
                        } else {
                            newValue = editor.getGeneratedText(
                                    ProductEditorTextComponent.this);
                        }
                        usePrevText = selected;
                        textViewer.getTextWidget().setText(newValue);

                        editor.setUsePreviousText(
                                ProductEditorTextComponent.this, selected);
                        editor.updateValue(ProductEditorTextComponent.this,
                                newValue);
                        clearUndoRedo();
                    }
                });

        if (required) {
            label = new Label(labelComp, SWT.WRAP);
            label.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, true, true));
            label.setText(labelText);

            requiredLabel = new Label(labelComp, SWT.NONE);
            requiredLabel.setLayoutData(
                    new GridData(SWT.LEFT, SWT.BOTTOM, false, false));
            requiredLabel.setText(REQUIRED_FIELD_TEXT);
            requiredLabel.setForeground(
                    Display.getCurrent().getSystemColor(SWT.COLOR_RED));
            requiredLabel.setVisible(true);
        } else {
            int width = fm.getAverageCharWidth()
                    * HazardConstants.LEGACY_TEXT_WRAP_LIMIT;
            label = new Label(labelComp, SWT.WRAP);
            GridData gd3 = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
            gd3.widthHint = width;
            label.setLayoutData(gd3);
            label.setText(labelText);
        }
        layout();
        pack();

        /*
         * If required the widthHint on the label needs to be recalculated after
         * the layout/pack have been done. This will correctly size the label
         * and allow it to be wrapped.
         */
        if (required) {
            requiredLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            int width = fm.getAverageCharWidth()
                    * HazardConstants.LEGACY_TEXT_WRAP_LIMIT
                    - requiredLabel.getSize().x;
            ((GridData) label.getLayoutData()).widthHint = width;
        }
        gc.dispose();
    }

    @Override
    protected void createTextViewer(String text) {
        textViewer = new SpellCheckTextViewer(this,
                SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        textViewer.getTextWidget().setAlwaysShowScrollBars(false);
        if (text == null) {
            text = "";
        }
        setText(text);

        StyledText styledText = textViewer.getTextWidget();
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        styledText.setLayoutData(gd);
        GC gc = new GC(styledText);
        gc.setFont(styledText.getFont());
        FontMetrics fm = gc.getFontMetrics();
        int totalFontHeight = fm.getHeight();
        int lines = determineNumLines(fm);

        /*
         * Size the text fields based on the number of lines and average
         * character width.
         */
        ((GridData) styledText.getLayoutData()).heightHint = lines
                * totalFontHeight;
        ((GridData) styledText.getLayoutData()).widthHint = fm
                .getAverageCharWidth() * HazardConstants.LEGACY_TEXT_WRAP_LIMIT;
        gc.dispose();

        UiBuilder.ensureMouseWheelEventsPassedUpToAncestor(styledText);
        styledText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String currentValue = textViewer.getTextWidget().getText();
                if (!currentValue.equals(prevValue)) {
                    // Text has changed since last time focus was lost
                    modificationHistory.add(prevValue);
                    prevValue = currentValue;
                    editor.setPrevValue(ProductEditorTextComponent.this,
                            currentValue);

                    // Add this undo to the manager
                    undoManager.addUndo(ProductEditorTextComponent.this);
                }
                editor.updateButtonState();
            }
        });

        styledText.addExtendedModifyListener(new ExtendedModifyListener() {

            @Override
            public void modifyText(ExtendedModifyEvent e) {
                String currentValue = textViewer.getTextWidget().getText();
                String savedValue = editor
                        .getSavedText(ProductEditorTextComponent.this);
                if (usePrevText) {
                    /*
                     * If the text doesn't equal the saved text uncheck the box.
                     */
                    if (savedValue != null
                            && !currentValue.equals(savedValue)) {
                        usePrevText = false;
                        previousTextBtn.getButton().setSelection(usePrevText);
                    }
                }
                editor.updateValue(ProductEditorTextComponent.this,
                        currentValue);
                editor.productDialog.updateButtons();
                editor.updateTabLabel();
            }
        });
    }

    public void updateUseSavedText() {
        usePrevText = true;
        previousTextBtn.getButton().setSelection(true);
        previousTextBtn.getButton().setEnabled(true);
    }

    @Override
    public boolean undo() {
        if (isUndoable()) {
            String newValue = modificationHistory.removeLast();
            setText(newValue);
            prevValue = newValue;
            return true;
        }
        return false;
    }

    @Override
    public boolean redo() {
        // Not implemented - This functionality is currently not needed.
        return false;
    }

    @Override
    public boolean isUndoable() {
        return !modificationHistory.isEmpty();
    }

    @Override
    public boolean isRedoable() {
        return false;
    }

    @Override
    public void clearUndoRedo() {
        modificationHistory.clear();
    }

    private int determineNumLines(FontMetrics fm) {
        StyledText sText = textViewer.getTextWidget();
        int lines;
        if (numLines == null || numLines < 1) {
            // Calculate the number of lines if not configured in python.
            Point p = sText.computeSize(HazardConstants.LEGACY_TEXT_WRAP_LIMIT
                    * fm.getAverageCharWidth(), SWT.DEFAULT);
            lines = p.y / fm.getHeight();
            if (lines >= 6) {
                // Max of 6 lines,
                lines = 6;
            } else {
                // Add one additional line for user input.
                lines += 1;
            }
        } else {
            lines = numLines;
        }
        return lines;
    }
}
