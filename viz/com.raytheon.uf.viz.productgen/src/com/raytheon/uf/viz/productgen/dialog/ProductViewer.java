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
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.hazards.productgen.ITextProduct;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;

/**
 * 
 * The dialog that allows the user to view previously issued products.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 29, 2015 9681       Robert.Blum  Initial creation
 * Jan 26, 2016 11860      Robert.Blum  Product Editor is now modal.
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

public class ProductViewer extends AbstractProductDialog {

    /** The label for the Dismiss button */
    private static final String DISMISS_BUTTON_LABEL = "Dismiss";

    /** Product editor dialog title */
    private static final String DIALOG_TITLE = "Product Viewer";

    /** The Dismiss Button */
    private Button dismissButton;

    /**
     * Creates a new ProductViewer on the given shell with the provided
     * generated product lists
     * 
     * @param parentShell
     *            The shell used to create the ProductViewer
     * @param generatedProductListStorage
     *            The generated products to be displayed on this product viewer
     * @param hazardTypes
     *            Hazard types configuration information.
     */
    public ProductViewer(Shell parentShell,
            List<GeneratedProductList> generatedProductListStorage,
            HazardTypes hazardTypes) {
        super(parentShell, SWT.RESIZE, generatedProductListStorage, hazardTypes);
        setText(DIALOG_TITLE);
    }

    @Override
    protected void initializeShellForSubClass(Shell shell) {
        // Do nothing

    }

    @Override
    protected void createButtons(Composite parent) {
        Composite buttonComp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.horizontalSpacing = HORIZONTAL_BUTTON_SPACING;
        GridData data = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        buttonComp.setLayout(layout);
        buttonComp.setLayoutData(data);
        createDismissButton(buttonComp);
    }

    /**
     * Creates the dismiss button on the given Composite
     * 
     * @param parent
     *            The parent composite to create the button on
     */
    protected void createDismissButton(Composite parent) {
        dismissButton = new Button(parent, SWT.PUSH);

        // Configure the Dismiss button
        dismissButton.setText(DISMISS_BUTTON_LABEL);
        ProductEditorUtil.setButtonGridData(dismissButton);

        dismissButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    @Override
    protected void createProductTabs(Composite parent,
            List<GeneratedProductList> generatedProductListStorage) {

        /*
         * Create the tab folder containing the product tabs
         */
        productFolder = new CTabFolder(parent, SWT.BORDER);
        ProductEditorUtil.setLayoutInfo(productFolder, 1, false, SWT.FILL,
                SWT.FILL, true, true);

        /*
         * Iterate over each product list in the generated product list storage
         * and create a tab for each product. Then create a sub-tab for each
         * format contained in the product
         */
        for (GeneratedProductList products : generatedProductListStorage) {
            for (int folderIndex = 0; folderIndex < products.size(); folderIndex++) {

                IGeneratedProduct product = products.get(folderIndex);

                // Create a tab for the product
                CTabItem productTab = new CTabItem(productFolder, SWT.NONE);
                productTab.setText(product.getProductID());
                Composite productComposite = new Composite(productFolder,
                        SWT.NONE);
                productTab.setControl(productComposite);
                ProductEditorUtil.setLayoutInfo(productComposite, 1, true,
                        SWT.FILL, SWT.FILL, true, true);

                /*
                 * Create a tab folder to hold the data editor tabs
                 */
                CTabFolder editorAndFormatsTabFolder = new CTabFolder(
                        productComposite, SWT.BOTTOM);
                ProductEditorUtil.setLayoutInfo(editorAndFormatsTabFolder, 1,
                        false, SWT.FILL, SWT.FILL, true, true);

                /*
                 * Iterate over the formatted entries in the product and create
                 * a FormattedTextViewer for each one.
                 */
                for (final Entry<String, List<Serializable>> entry : product
                        .getEntries().entrySet()) {
                    // Get the formatted text entries for this format
                    List<Serializable> values = entry.getValue();
                    for (int formattedTextIndex = 0; formattedTextIndex < values
                            .size(); formattedTextIndex++) {

                        /*
                         * If this is a text based product, create a
                         * FormattedTextDataEditor to hold the formatted text
                         */
                        if (product instanceof ITextProduct) {

                            // Add the text Viewer to the editor manager
                            editorManager.addFormattedTextViewer(product,
                                    new FormattedTextViewer(this, productTab,
                                            product, editorAndFormatsTabFolder,
                                            SWT.VERTICAL, entry.getKey(),
                                            formattedTextIndex));
                        } else {
                            throw new IllegalArgumentException(
                                    "Cannot create formatted text tab for format ["
                                            + entry.getKey()
                                            + "]. Unexpected product type");
                        }
                    }
                }

                editorAndFormatsTabFolder.setSelection(0);
            }
        }
        productFolder.setSelection(0);
    }

    @Override
    protected void regenerate(KeyInfo keyInfo) {
        // Do nothing
    }

    @Override
    public void updateButtons() {
        // Do nothing
    }
}