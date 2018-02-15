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
package gov.noaa.gsd.viz.hazards.display;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Basic dialog for displaying/editing text.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 27, 2017 22308      Robert.Blum Initial creation
 *
 * </pre>
 *
 * @author Robert.Blum
 */

public class TextEditorDlg extends CaveSWTDialog {
    /**
     * Control font.
     */
    private Font controlFont;

    /**
     * Read-only flag that hides controls from the user.
     */
    private boolean readOnly = true;

    /**
     * Styled text control used for editing the text.
     */
    private StyledText textST;

    /**
     * The text to be displayed.
     */
    private final String text;

    /**
     * Constructor.
     * 
     * @param parent
     *            Parent shell.
     * @param readOnly
     *            Read only flag.
     * @param text
     *            text to view/edit.
     * @param title
     *            title of the dialog.
     */
    public TextEditorDlg(Shell parent, boolean readOnly, String text,
            String title) {
        super(parent, SWT.DIALOG_TRIM | SWT.RESIZE, CAVE.DO_NOT_BLOCK);
        this.text = text;
        this.readOnly = readOnly;
        setText(title);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayout()
     */
    @Override
    protected Layout constructShellLayout() {
        // Create the main layout for the shell.
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 2;
        mainLayout.marginWidth = 2;
        mainLayout.verticalSpacing = 2;
        return mainLayout;
    }

    @Override
    protected void disposed() {
        controlFont.dispose();
    }

    @Override
    protected void initializeComponents(Shell shell) {
        controlFont = new Font(shell.getDisplay(), "Monospace", 10, SWT.NORMAL);
        createTextControl();
        createButtonRow();

        // Add the text data to the text control.
        textST.setText(this.text);
        textST.setFocus();
    }

    /**
     * Create the text control used for viewing/editing.
     */
    private void createTextControl() {
        Composite textComp = new Composite(shell, SWT.NONE);
        textComp.setLayout(new GridLayout(1, false));
        textComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 600;
        gd.heightHint = 600;
        textST = new StyledText(textComp,
                SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        textST.setWordWrap(true);
        textST.setFont(controlFont);
        textST.setLayoutData(gd);

        if (readOnly) {
            textST.setEditable(false);
        }
    }

    private void createButtonRow() {
        Composite btnRowComposite = new Composite(shell, SWT.NONE);
        btnRowComposite.setLayout(new GridLayout(1, false));
        btnRowComposite.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        Button closeBtn = new Button(btnRowComposite, SWT.PUSH);
        closeBtn.setText("Close");
        GridData gd = new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false);
        gd.widthHint = 150;
        closeBtn.setLayoutData(gd);
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    public void showDialog() {
        shell.setVisible(true);
        shell.setFocus();
    }
}
