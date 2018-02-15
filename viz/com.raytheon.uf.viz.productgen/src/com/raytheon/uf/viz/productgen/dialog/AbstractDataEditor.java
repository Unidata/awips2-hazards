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

import java.util.List;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.ProductPart;

/**
 * 
 * Abstract data editor class. Currently, there is one implementation of this
 * class, the ProductDataEditor. The GUI for this editor is created by the
 * Megawidgets library and contains the editable data that can be modified by
 * the user.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * 01/15/2015   5109       bphillip     Initial creation
 * 03/11/2015   6889       bphillip     Changed revertButton to undoButton.  More than one undo is now allowed.
 * 03/23/2015   7165       Robert.Blum  Adding * to editor tab and product tab
 *                                      labels when there are unsaved changes.
 * 04/16/2015   7579       Robert.Blum  Removed the Save Button from the Product Editor.
 *                                      Saving the edits are required to generate the correct
 *                                      product, so saving is now done automatically.
 * 05/04/2015   6979       Robert.Blum  Adding Save button back as well as making the save and
 *                                      undo buttons always enabled.
 * 07/28/2015   9687       Robert.Blum  Moved buttons to ProductDataEditor since they were not
 *                                      common to all subclasses.
 * 07/30/2015   9681       Robert.Blum  Changed to use new abstract product dialog class.
 * Feb 23, 2017 29170      Robert.Blum  Product Editor refactor.
 * Apr 03, 2017 32572      Roger.Ferrel Added {@link #getIncompleteRequiredFields()}.
 * Jun 05, 2017 29996      Robert.Blum  Created abstract parent class.
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public abstract class AbstractDataEditor extends AbstractProductTab {

    /**
     * Creates a new Data Editor
     * 
     * @param productEditor
     *            The parent product editor instance
     * @param product
     *            The product associated with this data editor
     * @param parent
     *            The parent tab folder
     * @param style
     *            Style hints
     */
    protected AbstractDataEditor(AbstractProductDialog productDialog,
            CTabItem productTab, IGeneratedProduct product, CTabFolder parent,
            int style) {
        super(productDialog, productTab, product, parent, style);
    }

    /**
     * Checks if this data editor has unsaved changes
     * 
     * @return True if unsaved changes are present, else false
     */
    public abstract boolean hasUnsavedChanges();

    /**
     * Saves any modified values.
     */
    public abstract void saveModifiedValues();

    /**
     * Checks if the user has completed all required fields
     * 
     * @return True if the user has completed all required fields, else false
     */
    public abstract boolean requiredFieldsCompleted();

    /**
     * Checks to see if there are any required fields that needs to be
     * completed.
     */
    public abstract List<ProductPart> getIncompleteRequiredFields();

    /**
     * Gets the number of undo actions that remain.
     * 
     * @return
     */
    protected abstract int getUndosRemaining();

    /**
     * Allow for updating the tab label with an "*" to visually show that the
     * tab has unsaved changes.
     */
    protected void updateTabLabel() {
        String prevLabel = getText();
        String prevProductLabel = productTab.getText();
        if (hasUnsavedChanges()) {
            // Add the asterisk to the tab label
            if (prevLabel.startsWith("*") == false) {
                setText("*" + prevLabel);
            }
            // Add the asterisk to the product tab label
            if (prevProductLabel.startsWith("*") == false) {
                productTab.setText("*" + prevProductLabel);
            }
        } else {
            prevLabel = prevLabel.replace("*", "");
            setText(prevLabel);
            prevProductLabel = prevProductLabel.replace("*", "");
            productTab.setText(prevProductLabel);
        }
    }
}
