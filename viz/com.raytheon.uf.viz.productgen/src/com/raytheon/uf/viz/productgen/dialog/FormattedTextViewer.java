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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;

/**
 * 
 * The FormattedTextViewer class encapsulates information necessary for viewing
 * the text in the formatted text in the Product Editor.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * 04/20/2015   7579       Robert.Blum  Initial creation
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */
public class FormattedTextViewer extends AbstractDataEditor {

    /** The name of the formatter used to format the data */
    private final String format;

    /** The composite which holds the viewer */
    private Composite viewerPane;

    /** The generated product associated with this data editor */
    private IGeneratedProduct product;

    /**
     * The index into the list of formatted texts for the format in the
     * generated product
     */
    private final int formatIndex;

    /** The text content of the formatted product tab */
    private StyledText styledText;

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
    protected FormattedTextViewer(ProductEditor productEditor,
            CTabItem productTab, IGeneratedProduct product, CTabFolder parent,
            int style, String format, int formatIndex) {
        super(productEditor, productTab, product, parent, style);
        this.product = product;
        this.format = format;
        this.formatIndex = formatIndex;

        // Initialize the composite which will hold the text viewer
        viewerPane = new Composite(parent, SWT.NONE);
    }

    @Override
    protected void initializeSubclass() {
        ProductEditorUtil.setLayoutInfo(viewerPane, 1, false, SWT.FILL,
                SWT.FILL, true, true);
        setControl(viewerPane);

        // Create a new StyledText object containing the formatted text
        this.styledText = new StyledText(viewerPane, SWT.H_SCROLL
                | SWT.V_SCROLL);

        String formattedText = (String) product.getEntries().get(format)
                .get(formatIndex);
        this.styledText.setWordWrap(false);
        this.styledText.setEditable(false);
        this.styledText.setAlwaysShowScrollBars(false);
        this.styledText.setText(formattedText);
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
    }

    @Override
    public void refresh() {
        /*
         * Sets the formatted text into the StyledText object.
         */
        String newText = product.getEntries().get(format).get(formatIndex)
                .toString();
        styledText.setText(newText);
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
     * Gets the format associated with this editor
     * 
     * @return The format
     */
    public String getFormat() {
        return format;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return false;
    }

    @Override
    public boolean isDataEditable() {
        return false;
    }

    @Override
    public boolean requiredFieldsCompleted() {
        return true;
    }

    @Override
    public void saveModifiedValues() {
    }

    @Override
    public void revertValues() {
    }

    @Override
    public void undoModification() {

    }

    @Override
    protected boolean undosRemaining() {
        return false;
    }

    @Override
    protected int getUndosRemaining() {
        return 0;
    }
}