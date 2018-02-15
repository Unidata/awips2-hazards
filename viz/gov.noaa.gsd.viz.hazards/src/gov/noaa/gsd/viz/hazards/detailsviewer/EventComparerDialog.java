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
package gov.noaa.gsd.viz.hazards.detailsviewer;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog to view the comparison of two hazard events.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date            Ticket#    Engineer              Description
 * ------------    ---------- ------------          --------------------------
 * Apr 27, 2017     33189     Robert.Blum           Initial creation
 * </pre>
 * 
 */
public class EventComparerDialog extends CaveSWTDialog {

    private CompareEditorInput input;

    /**
     * @param parentShell
     * @param input
     */
    public EventComparerDialog(Shell parentShell, CompareEditorInput input) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.RESIZE);
        setText("Event Comparison");
        this.input = input;
    }

    @Override
    protected Layout constructShellLayout() {
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 3;
        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, true);
        return gd;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        Composite mainComp = new Composite(shell, SWT.NONE);
        mainComp.setLayout(new GridLayout(1, false));
        mainComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Composite compareComp = (Composite) input.createContents(mainComp);
        compareComp.setLayout(new GridLayout(1, false));
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 800;
        compareComp.setLayoutData(gd);

        Composite buttonComp = new Composite(mainComp, SWT.NONE);
        buttonComp.setLayout(new GridLayout(1, false));
        buttonComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, false));

        GridData gd2 = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gd2.minimumWidth = 85;

        Button closeBtn = new Button(buttonComp, SWT.PUSH);
        closeBtn.setLayoutData(gd2);
        closeBtn.setText("Close");
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }
}
