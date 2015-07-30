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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.KeyInfo;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * 
 * The dialog that allows the user to modify editable fields in data produced by
 * the content generator. This dialog also allows the user to view the resulting
 * formats produced by the formatter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 29, 2015 9681       Robert.Blum  Initial creation
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

public abstract class AbstractProductDialog extends CaveSWTDialog {

    /** Horizontal spacing between buttons */
    protected static final int HORIZONTAL_BUTTON_SPACING = 65;

    /** List of generated product lists from the product generators */
    protected final List<GeneratedProductList> generatedProductListStorage;

    /** Top level tab folder holding the products currently displayed */
    protected CTabFolder productFolder;

    /** Data structure used to manage the data editors */
    protected final DataEditorManager editorManager = new DataEditorManager();

    /**
     * Creates a new AbstractProductDialog on the given shell with the provided
     * generated product lists
     * 
     * @param parentShell
     *            The shell used to create the AbstractProductDialog
     * @param generatedProductListStorage
     *            The generated products to be displayed on this
     *            AbstractProductDialog
     */
    public AbstractProductDialog(Shell parentShell, int style, int caveStyle,
            List<GeneratedProductList> generatedProductListStorage) {
        super(parentShell, SWT.RESIZE, CAVE.PERSPECTIVE_INDEPENDENT);
        this.generatedProductListStorage = generatedProductListStorage;
    }

    /**
     * Initializes the GUI component of the AbstractProductDialog. The
     * AbstractProductDialog is initialized as follows:
     * <p>
     * <li>Configure the size and layout of the shell</li>
     * <li>Build the product tabs</li>
     * <li>Create the Buttons</li>
     * <p>
     * 
     * @param shell
     *            The shell on which to initialize the components
     */
    @Override
    protected void initializeComponents(Shell shell) {

        shell.setMinimumSize(600, 800);
        shell.setLayout(new GridLayout(1, false));
        shell.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
        initializeShellForSubClass(shell);

        /*
         * Create and configure the top level composite that will contain the
         * product tabs as well as the Issue All and Dismiss buttons
         */
        Composite topComposite = new Composite(shell, SWT.NONE);
        ProductEditorUtil.setLayoutInfo(topComposite, 1, false, SWT.FILL,
                SWT.FILL, true, true);

        // Create the product tabs
        createProductTabs(topComposite, generatedProductListStorage);

        // Create the buttons
        createButtons(topComposite);

    }

    /**
     * Initializes the shell for the sub class.
     * 
     * @param shell
     *            The shell on which to initialize the components
     */
    protected abstract void initializeShellForSubClass(Shell shell);

    /**
     * Creates the buttons for the product dialog
     * 
     * @param parent
     *            The parent composite to create buttons on
     */
    protected abstract void createButtons(Composite parent);

    /**
     * Creates the product tabs which will contain the data editor tabs
     * 
     * @param parent
     *            The parent composite on which the product folder will get
     *            created
     * @param generatedProductListStorage
     *            The generated products used to generate the product tabs
     */
    protected abstract void createProductTabs(Composite parent,
            List<GeneratedProductList> generatedProductListStorage);

    /**
     * Regenerates the product data for the generated products already
     * associated with this ProductDialog
     * 
     * @param keyInfo
     * 
     */
    protected abstract void regenerate(KeyInfo keyInfo);

    /**
     * Updates the state of the buttons on the product dialog.
     */
    protected abstract void updateButtons();

    /**
     * Gets the GeneratedProductList associated with this AbstractProductDialog
     * 
     * @return The GeneratedProductList associated with this
     *         AbstractProductDialog
     */
    public List<GeneratedProductList> getGeneratedProductListStorage() {
        return this.generatedProductListStorage;
    }
}