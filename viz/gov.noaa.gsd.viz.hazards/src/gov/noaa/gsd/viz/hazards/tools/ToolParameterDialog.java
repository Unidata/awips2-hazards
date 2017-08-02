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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;

import gov.noaa.gsd.viz.hazards.display.action.ToolAction;

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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ToolParameterDialog extends AbstractToolDialog {

    // Private Static Constants

    /**
     * OK button text.
     */
    private static final String OK_BUTTON_LABEL = "Run";

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param presenter
     *            Presenter.
     * @param parent
     *            Parent shell.
     * @param tool
     *            Identifier of the tool to be executed.
     * @param type
     *            Type of the tool.
     * @param context
     *            Execution context in which this tool is to be run.
     * @param jsonParams
     *            JSON string giving the parameters for this dialog. Within the
     *            set of all fields that are defined by these parameters, all
     *            the fields (megawidget specifiers) must have unique
     *            identifiers.
     */
    public ToolParameterDialog(ToolsPresenter presenter, Shell parent,
            String tool, ToolType type, RecommenderExecutionContext context,
            String jsonParams) {
        super(presenter, parent, tool, type, context, jsonParams);
    }

    // Protected Methods

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID,
                ToolParameterDialog.OK_BUTTON_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
        super.okPressed();
        getPresenter().publish(new ToolAction(
                ToolAction.RecommenderActionEnum.RUN_RECOMMENDER_WITH_PARAMETERS,
                getTool(), getType(), getState(), getContext()));
    }
}
