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

import com.raytheon.uf.viz.hazards.sessionmanager.tools.ToolResultDialogSpecifier;

/**
 * Description: Dialog for displaying results to the user following the running
 * of a tool.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 15, 2017   22757    Chris.Golden Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Changed to work with new recommender
 *                                      manager.
 * Jan 30, 2018   45994    Chris.Golden Fixed bug that could potentially stop
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
public class ToolResultDialog
        extends AbstractToolDialog<ToolResultDialogSpecifier> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parent
     *            Parent shell.
     * @param dialogParameters
     *            Parameters for this subview. Within the set of all fields that
     *            are defined by these parameters, all the fields (megawidget
     *            specifiers) must have unique identifiers.
     * @param toolDialogListener
     *            Tool dialog listener.
     */
    public ToolResultDialog(Shell parent,
            ToolResultDialogSpecifier dialogSpecifier,
            IToolDialogListener toolDialogListener) {
        super(parent, dialogSpecifier, toolDialogListener);
    }

    // Protected Methods

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected void handleShellCloseEvent() {
        cancelPressed();
        close();
    }

    @Override
    protected Map<String, Serializable> getState() {
        return null;
    }
}
