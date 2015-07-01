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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * 
 * Product Generation Issue Confirmation. In order to work with the IMessager
 * logic this dialog must be blocking.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 20, 2015 3795       rferrel     Initial creation
 * Jul 01, 2015 6726       Robert.Blum Made dialog APPLICATION_MODAL.
 * 
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */
public class ProductGenConfirmationDlg extends CaveSWTDialog {

    /**
     * Handler used for messages.
     */
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductGenConfirmationDlg.class);

    private String modeMessage;

    private CAVEMode mode;

    private String IMAGE_OPERATIONAL = "res/images/hsOper.gif";

    private String IMAGE_TEST = "res/images/hsTest.gif";

    private String IMAGE_PRACTICE = "res/images/hsPractice.gif";

    private Image stopSign = null;

    private final String okBtnText;

    private final String cancelBtnText;

    /**
     * Constructor the dialog must be blocking to work with the logic in
     * IMessenger.
     * 
     * @param parentShell
     * @param title
     * @param modeMessage
     * @param okBtnText
     * @param cancelBtnText
     */
    protected ProductGenConfirmationDlg(Shell parentShell, String title,
            String modeMessage, String okBtnText, String cancelBtnText) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, CAVE.NONE);

        setText(title);

        this.modeMessage = modeMessage;
        this.mode = CAVEMode.getMode();
        this.okBtnText = okBtnText;
        this.cancelBtnText = cancelBtnText;
        setReturnValue(Boolean.FALSE);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        Composite mainComposite = new Composite(shell, SWT.NONE);
        mainComposite.setLayout(new GridLayout(1, false));
        createImage(mainComposite);
        createMessageLabel(mainComposite);
        createButtonRow(mainComposite);
    }

    private void createImage(Composite mainComposite) {

        InputStream is = null;
        try {
            ClassLoader cl = ProductGenConfirmationDlg.class.getClassLoader();

            if (mode.equals(CAVEMode.OPERATIONAL)) {
                // add Live image
                is = cl.getResourceAsStream(IMAGE_OPERATIONAL);
            } else if (mode.equals(CAVEMode.TEST)) {
                // add Test image
                is = cl.getResourceAsStream(IMAGE_TEST);
            } else if (mode.equals(CAVEMode.PRACTICE)) {
                // add Practice image
                is = cl.getResourceAsStream(IMAGE_PRACTICE);
            } else {
                // unknown
                is = cl.getResourceAsStream(IMAGE_OPERATIONAL);
            }

            stopSign = new Image(mainComposite.getDisplay(), is);
            Label stopSignLbl = new Label(mainComposite, 0);
            stopSignLbl.setImage(stopSign);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, "Unable to get image.", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private void createMessageLabel(Composite mainComposite) {
        Label modeMsgLbl = new Label(mainComposite, 0);
        modeMsgLbl.setText(this.modeMessage);

        Label sepLbl = new Label(mainComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        sepLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void createButtonRow(Composite mainComposite) {
        Composite buttonRowComp = new Composite(mainComposite, SWT.NONE);
        buttonRowComp.setLayout(new GridLayout(2, true));
        buttonRowComp.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true,
                false));

        // Add the Go Ahead (OK) button.
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.widthHint = 100;
        Button okBtn = new Button(buttonRowComp, SWT.PUSH);
        okBtn.setText(okBtnText);
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                setReturnAndClose(Boolean.TRUE);
            }
        });

        // Add the Cancel button.
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd.widthHint = 100;
        Button cancelBtn = new Button(buttonRowComp, SWT.PUSH);
        cancelBtn.setText(cancelBtnText);
        cancelBtn.setLayoutData(gd);
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                setReturnAndClose(Boolean.FALSE);
            }
        });
    }

    private void setReturnAndClose(Boolean returnValue) {
        setReturnValue(returnValue);
        close();
    }

    @Override
    protected void disposed() {
        if (stopSign != null) {
            stopSign.dispose();
        }
    }
}
