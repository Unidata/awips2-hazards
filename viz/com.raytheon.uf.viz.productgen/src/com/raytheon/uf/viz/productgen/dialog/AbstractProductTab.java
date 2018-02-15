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
import org.eclipse.swt.widgets.Composite;

import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;

/**
 * 
 * Abstract Product tab class. Currently, there are two abstract implementations
 * of this class. The first being the AbstractDataEditor. The GUI for this
 * editor is created based on Product Parts containing the editable text that
 * can be modified by the user. The second implementation is the
 * AbstractProductViewerTab. This viewer contains the formatted text from the
 * formatters.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 05, 2017 29996      Robert.Blum  Initial Creation
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */
public abstract class AbstractProductTab extends CTabItem {

    /** The number of buttons on the GUI */
    private static final int BUTTON_COUNT = 0;

    /** Horizontal spacing between Save and Undo buttons */
    protected static final int HORIZONTAL_BUTTON_SPACING = 65;

    /** The composite which holds the editor */
    protected Composite editorPane;

    /** The composite which holds the buttons */
    protected Composite editorButtonPane;

    /** The parent product dialog instance to which this data editor belongs */
    protected AbstractProductDialog productDialog;

    /** The generated product associated with this data editor */
    protected IGeneratedProduct product;

    /** The product CTabItem to which this data editor belongs */
    protected CTabItem productTab;

    /**
     * Creates a new Abstract Product Tab
     * 
     * @param productDialog
     *            The parent product editor/viewer instance
     * @param productTab
     *            The product Tab containing this sub Tab.
     * @param product
     *            The product associated with this data editor
     * @param parent
     *            The parent tab folder
     * @param style
     *            Style hints
     */
    protected AbstractProductTab(AbstractProductDialog productDialog,
            CTabItem productTab, IGeneratedProduct product, CTabFolder parent,
            int style) {
        super(parent, style);
        this.productDialog = productDialog;
        this.productTab = productTab;
        this.product = product;

        // Initialize the composite which will hold the data editor
        editorPane = new Composite(parent, SWT.NONE);
        ProductEditorUtil.setLayoutInfo(editorPane, 1, false, SWT.FILL,
                SWT.FILL, true, true);
        setControl(editorPane);
    }

    /**
     * Refreshes the tab contents
     */
    public abstract void refresh();

    /**
     * Checks if this editor contains editable information
     * 
     * @return True if this editor contains editable information, else false
     */
    public abstract boolean isDataEditable();

    /**
     * Called by initialize. This method allows subclasses of AbstractDataEditor
     * to make GUI contributes prior to adding the buttons on the bottom of the
     * editor panel
     */
    protected abstract void initializeSubclass();

    /**
     * Updates the text on the buttons.
     */
    protected abstract void updateButtonState();

    /**
     * Initializes the GUI components for this data editor. Delegates to the
     * initializeSubclass method so the subclass may add any GUI contributes
     * first. Then adds and disables the buttons to the data editor
     */
    protected void initialize() {

        // Create the data editor specific GUI components
        initializeSubclass();

        // Put the buttons on the bottom of the data editor
        if (this.getButtonCount() > 0) {
            createEditorButtons(editorPane);
        }
    }

    /**
     * Creates common AbstractDataEditor buttons
     * 
     * @param editorPane
     *            The parent composite
     */
    protected void createEditorButtons(Composite editorPane) {
        // Do Nothing - no common buttons between all AbstractDataEditors
    }

    /**
     * Retrieves the number of buttons present on the editor GUI. The default
     * value is 0.
     * 
     * @return The number of buttons present on the button composite
     */
    protected int getButtonCount() {
        return BUTTON_COUNT;
    }

    /**
     * @return the product
     */
    public IGeneratedProduct getProduct() {
        return product;
    }
}
