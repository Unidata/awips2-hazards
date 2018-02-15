/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.tools;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.DialogButtonsSpecifier;
import com.raytheon.uf.viz.hazards.sessionmanager.tools.ToolParameterDialogSpecifier;

import gov.noaa.gsd.viz.hazards.utilities.Utilities;

/**
 * Description: Dialog for gathering parameters from the user in preparation for
 * the running of a tool.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 15, 2017   22757    Chris.Golden Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Changed to work with new recommender
 *                                      manager.
 * Jan 30, 2018   45994    Chris.Golden Added the option of requesting custom
 *                                      buttons at the bottom of the dialog
 *                                      in place of the usual buttons. Also
 *                                      fixed bug that could potentially stop
 *                                      future recommenders from running if
 *                                      the user closed a dialog via the X
 *                                      button in the title bar.
 * May 22, 2018    3782    Chris.Golden Changed to have configuration options
 *                                      passed in using dedicated objects and
 *                                      having already been vetted, instead of
 *                                      passing them in as raw maps. Also
 *                                      changed to conform somewhat better to
 *                                      the MVP design guidelines.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ToolParameterDialog
        extends AbstractToolDialog<ToolParameterDialogSpecifier> {

    // Private Static Constants

    /**
     * OK button text.
     */
    private static final String OK_BUTTON_LABEL = "Run";

    // Private Variables

    /**
     * Index of the button used to dismiss the dialog, if the dialog has been
     * dismissed and the dismissal is not a cancellation.
     */
    private int dismissingCustomButtonIndex;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parent
     *            Parent shell.
     * @param dialogSpecifier
     *            Specifier of the dialog to be created to gather parameters.
     * @param toolDialogListener
     *            Tool dialog listener.
     */
    public ToolParameterDialog(Shell parent,
            ToolParameterDialogSpecifier dialogSpecifier,
            IToolDialogListener toolDialogListener) {
        super(parent, dialogSpecifier, toolDialogListener);
    }

    // Protected Methods

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        DialogButtonsSpecifier customButtonsSpecifier = getSpecifier()
                .getCustomButtonsSpecifier();
        if (customButtonsSpecifier != null) {
            int count = 0;
            for (String customButtonIdentifier : customButtonsSpecifier
                    .getButtonIdentifiers()) {
                createButton(parent, count,
                        customButtonsSpecifier.getLabelsForButtonIdentifiers()
                                .get(customButtonIdentifier),
                        (count++ == customButtonsSpecifier
                                .getDefaultButtonIndex()));
            }
        } else {
            createButton(parent, IDialogConstants.OK_ID,
                    ToolParameterDialog.OK_BUTTON_LABEL, true);
            createButton(parent, IDialogConstants.CANCEL_ID,
                    IDialogConstants.CANCEL_LABEL, false);
        }
    }

    @Override
    protected void buttonPressed(int buttonId) {

        /*
         * If custom buttons are in use, treat this as a cancellation if the
         * button that was pressed is considered equivalent to the cancel
         * button, or treat it as an OK press if not the cancel button,
         * recording the index of the button that was pressed so that that
         * information can be passed back as part of the state. If instead
         * custom buttons are not being used, treat it as a standard button
         * press.
         */
        DialogButtonsSpecifier customButtonsSpecifier = getSpecifier()
                .getCustomButtonsSpecifier();
        if (customButtonsSpecifier != null) {
            if (buttonId == customButtonsSpecifier.getCancelButtonIndex()) {
                cancelPressed();
            } else {
                dismissingCustomButtonIndex = buttonId;
                okPressed();
            }
        } else {
            super.buttonPressed(buttonId);
        }
    }

    @Override
    protected void handleShellCloseEvent() {

        /*
         * If custom buttons are in use, treat this as a press of whatever
         * button is equivalent to the title bar close button. Otherwise, just
         * treat it as a cancel.
         */
        DialogButtonsSpecifier customButtonsSpecifier = getSpecifier()
                .getCustomButtonsSpecifier();
        if (customButtonsSpecifier != null) {
            buttonPressed(customButtonsSpecifier.getCloseButtonIndex());
        } else {
            cancelPressed();
        }
        close();
    }

    @Override
    protected Map<String, Serializable> getState() {
        Map<String, Serializable> state = Utilities
                .asMap(getSpecifier().getInitialStatesForMegawidgets());
        DialogButtonsSpecifier customButtonsSpecifier = getSpecifier()
                .getCustomButtonsSpecifier();
        if (customButtonsSpecifier != null) {
            state.put(HazardConstants.DIALOG_DISMISS_CHOICE,
                    customButtonsSpecifier.getButtonIdentifiers()
                            .get(dismissingCustomButtonIndex));
        }
        return state;
    }
}
