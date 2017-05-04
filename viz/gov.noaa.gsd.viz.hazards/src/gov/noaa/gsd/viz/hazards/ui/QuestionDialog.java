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

package gov.noaa.gsd.viz.hazards.ui;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.viz.core.mode.CAVEMode;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Question dialog, used to ask the user a question, with custom button labels
 * in place of OK and Cancel, an image indicating what CAVE mode (Operational,
 * Test, or Practice) is in effect, and if desired, a scrollable area to display
 * a potentially long list of items.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 20, 2015 3795       rferrel      Initial creation
 * Jul 01, 2015 6726       Robert.Blum  Made dialog APPLICATION_MODAL.
 * Nov 23, 2015 13017      Chris.Golden Added ability to specify a many-line
 *                                      message below the main message, so that
 *                                      the latter may be embedded in a
 *                                      scrollable composite. Also generalized
 *                                      the class to be for any sort of messages,
 *                                      and renamed it and relocated it to suit
 *                                      its more general role.
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */
public class QuestionDialog extends CaveSWTDialog {

    // Private Static Constants

    /**
     * Handler used for messages.
     */
    private final static IUFStatusHandler statusHandler = UFStatus
            .getHandler(QuestionDialog.class);

    /**
     * File holding image indicating that Operational mode is in effect.
     */
    private final static String IMAGE_OPERATIONAL = "res/images/hsOper.gif";

    /**
     * File holding image indicating that Test mode is in effect.
     */
    private final static String IMAGE_TEST = "res/images/hsTest.gif";

    /**
     * File holding image indicating that Practice mode is in effect.
     */
    private final static String IMAGE_PRACTICE = "res/images/hsPractice.gif";

    // Private Variables

    /**
     * Image being displayed.
     */
    private Image image;

    /**
     * Current mode of CAVE.
     */
    private final CAVEMode mode;

    /**
     * Base message to be displayed; should be no more than a few lines.
     */
    private final String baseMessage;

    /**
     * Potentially long message to be displayed under the base message; may be a
     * large number of lines, as it is embedded in a scrollable composite. If
     * <code>null</code>, no such message is shown.
     */
    private final String potentiallyLongMessage;

    /**
     * Text to be used for the OK button.
     */
    private final String okButtonText;

    /**
     * Text to be used for the Cancel button.
     */
    private final String cancelButtonText;

    // Public Constructors

    /**
     * Construct a standard instance with only a message that is no more than a
     * few lines in length.
     * 
     * @param parent
     *            Parent of the dialog to be created.
     * @param title
     *            Title of the dialog.
     * @param message
     *            Message to be displayed.
     * @param okButtonText
     *            Text to be used for the OK button.
     * @param cancelButtonText
     *            Text to be used for the Cancel button.
     */
    public QuestionDialog(Shell parent, String title, String message,
            String okButtonText, String cancelButtonText) {
        this(parent, title, message, null, okButtonText, cancelButtonText);
    }

    /**
     * Construct a standard instance with a base message that is no more than a
     * few lines in length, and a potentially many-line message that is shown in
     * a scrollable composite.
     * 
     * @param parent
     *            Parent of the dialog to be created.
     * @param title
     *            Title of the dialog.
     * @param baseMessage
     *            Base message to be displayed, which should only be a few lines
     *            in length at most.
     * @param potentiallyLongMessage
     *            Potentially many-line message to be displayed under the base
     *            message; may be <code>null</code>.
     * @param okButtonText
     *            Text to be used for the OK button.
     * @param cancelButtonText
     *            Text to be used for the Cancel button.
     */
    public QuestionDialog(Shell parent, String title, String baseMessage,
            String potentiallyLongMessage, String okButtonText,
            String cancelButtonText) {
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, CAVE.NONE);
        setText(title);
        this.baseMessage = baseMessage;
        this.potentiallyLongMessage = potentiallyLongMessage;
        this.mode = CAVEMode.getMode();
        this.okButtonText = okButtonText;
        this.cancelButtonText = cancelButtonText;
        setReturnValue(Boolean.FALSE);
    }

    // Protected Methods

    @Override
    protected void initializeComponents(Shell shell) {
        Composite mainComposite = new Composite(shell, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 15;
        mainComposite.setLayout(gridLayout);
        createImage(mainComposite);
        createMessageLabels(mainComposite);
        createButtonRow(mainComposite);
    }

    @Override
    protected void disposed() {
        if (image != null) {
            image.dispose();
        }
    }

    // Private Methods

    /**
     * Create an image to show in the specified composite.
     * 
     * @param parent
     *            Composite in which to show the image.
     */
    private void createImage(Composite parent) {

        InputStream inputStream = null;
        try {
            ClassLoader classLoader = QuestionDialog.class.getClassLoader();

            if (mode.equals(CAVEMode.OPERATIONAL)) {
                inputStream = classLoader
                        .getResourceAsStream(IMAGE_OPERATIONAL);
            } else if (mode.equals(CAVEMode.TEST)) {
                inputStream = classLoader.getResourceAsStream(IMAGE_TEST);
            } else if (mode.equals(CAVEMode.PRACTICE)) {
                inputStream = classLoader.getResourceAsStream(IMAGE_PRACTICE);
            } else {
                inputStream = classLoader
                        .getResourceAsStream(IMAGE_OPERATIONAL);
            }

            image = new Image(parent.getDisplay(), inputStream);
            Label imageLabel = new Label(parent, 0);
            GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
            imageLabel.setLayoutData(gridData);
            imageLabel.setImage(image);
        } catch (Exception e) {
            statusHandler.handle(Priority.PROBLEM, "Unable to get image.", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {

                    /*
                     * No action.
                     */
                }
            }
        }
    }

    /**
     * Create the message labels for the dialog.
     * 
     * @param parent
     *            Composite in which to place the message labels.
     */
    private void createMessageLabels(Composite parent) {

        /*
         * Add the question to the composite.
         */
        Label messageLabel = new Label(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gridData.verticalIndent = 10;
        messageLabel.setLayoutData(gridData);
        messageLabel.setText(baseMessage);

        /*
         * Add the potentially many-line message to the composite if one was
         * supplied.
         */
        if (potentiallyLongMessage != null) {

            /*
             * Create the text widget that will serve as the label so that it
             * gets a scrollbar for free, and make it read-only and using the
             * background of a label.
             */
            Text potentiallyLongMessageLabel = new Text(parent, SWT.MULTI
                    | SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS);
            potentiallyLongMessageLabel.setBackground(messageLabel
                    .getBackground());
            potentiallyLongMessageLabel.setEditable(false);
            potentiallyLongMessageLabel.setText(potentiallyLongMessage.trim());

            /*
             * Set up the height for the text widget to be 5 lines of text or
             * the number of lines it holds, whichever is less.
             */
            gridData = new GridData(GridData.FILL_HORIZONTAL);
            int lineCount = potentiallyLongMessageLabel.getLineCount() - 1;
            gridData.heightHint = (lineCount > 5 ? 5 : lineCount)
                    * potentiallyLongMessageLabel.getLineHeight();
            gridData.verticalIndent = 10;
            potentiallyLongMessageLabel.setLayoutData(gridData);

            /*
             * Set up a listener that will ensure that the label will only show
             * scrollbars as appropriate.
             */
            Listener scrollBarListener = new Listener() {
                @Override
                public void handleEvent(Event event) {
                    Text text = (Text) event.widget;
                    Rectangle textClientArea = text.getClientArea();
                    Rectangle textDesiredArea = text.computeTrim(
                            textClientArea.x, textClientArea.y,
                            textClientArea.width, textClientArea.height);
                    Point size = text.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                            true);
                    text.getHorizontalBar().setVisible(
                            textDesiredArea.width <= size.x);
                    text.getVerticalBar().setVisible(
                            textDesiredArea.height <= size.y);
                    if (event.type == SWT.Modify) {
                        text.getParent().layout(true);
                        text.showSelection();
                    }
                }
            };
            potentiallyLongMessageLabel.addListener(SWT.Resize,
                    scrollBarListener);
            potentiallyLongMessageLabel.addListener(SWT.Modify,
                    scrollBarListener);
        }
    }

    /**
     * Create the row of buttons for the dialog.
     * 
     * @param parent
     *            Composite in which to place the row of buttons.
     */
    private void createButtonRow(Composite composite) {
        Composite buttonRowComposite = new Composite(composite, SWT.NONE);
        buttonRowComposite.setLayout(new GridLayout(2, true));
        GridData gridData = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        gridData.verticalIndent = 10;
        buttonRowComposite.setLayoutData(gridData);

        /*
         * Add the OK button.
         */
        gridData = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gridData.widthHint = 100;
        Button okButton = new Button(buttonRowComposite, SWT.PUSH);
        okButton.setText(okButtonText);
        okButton.setLayoutData(gridData);
        okButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                setReturnAndClose(true);
            }
        });

        /*
         * Add the Cancel button.
         */
        gridData = new GridData(SWT.CENTER, SWT.DEFAULT, true, false);
        gridData.widthHint = 100;
        Button cancelButton = new Button(buttonRowComposite, SWT.PUSH);
        cancelButton.setText(cancelButtonText);
        cancelButton.setLayoutData(gridData);
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                setReturnAndClose(false);
            }
        });
        cancelButton.setFocus();
    }

    /**
     * Set the return value to that specified and close the dialog.
     * 
     * @param returnValue
     *            Return value to be used.
     */
    private void setReturnAndClose(boolean returnValue) {
        setReturnValue(returnValue);
        close();
    }
}
