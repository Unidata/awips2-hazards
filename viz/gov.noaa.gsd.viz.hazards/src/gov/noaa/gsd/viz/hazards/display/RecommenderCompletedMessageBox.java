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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Message Box that pops up upon completion of a recommender when no hazard
 * events were recommended. This notifies the user that the recommender has
 * successfully completed execution.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 08, 2015    7369    Robert.Blum Initial creation
 * 
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

public class RecommenderCompletedMessageBox extends CaveSWTDialog {

    private final String message;

    public RecommenderCompletedMessageBox(Shell parent, String recommenderName) {
        super(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL, CAVE.NONE);

        // Set the title and text using the recommender name.
        setText(recommenderName);
        message = recommenderName + " completed. No recommendations.";
    }

    @Override
    protected Layout constructShellLayout() {
        return new GridLayout(1, false);
    }

    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        GridLayout gl = new GridLayout(1, false);
        Composite msgComp = new Composite(shell, SWT.NONE);
        msgComp.setLayout(gl);
        msgComp.setLayoutData(gd);

        gd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        Label msg = new Label(msgComp, SWT.NONE);
        msg.setLayoutData(gd);
        msg.setText(message);

        // Create button
        gd = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gl = new GridLayout(3, false);
        Composite buttonComp = new Composite(shell, SWT.NONE);
        buttonComp.setLayout(gl);
        buttonComp.setLayoutData(gd);

        int btnWidth = 85;
        Button dismissBtn = new Button(buttonComp, SWT.PUSH);
        dismissBtn.setLayoutData(new GridData(btnWidth, SWT.DEFAULT));
        dismissBtn.setText("Dismiss");
        dismissBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }
}

