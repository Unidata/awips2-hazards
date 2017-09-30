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

import gov.noaa.gsd.viz.hazards.display.action.ToolAction;

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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ToolResultDialog extends AbstractToolDialog {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param presenter
     *            Presenter.
     * @param parent
     *            Parent shell.
     * @param type
     *            Type of the tool.
     * @param jsonParams
     *            JSON string giving the parameters for this dialog. Within the
     *            set of all fields that are defined by these parameters, all
     *            the fields (megawidget specifiers) must have unique
     *            identifiers.
     */
    public ToolResultDialog(ToolsPresenter presenter, Shell parent,
            ToolType type, String jsonParams) {
        super(presenter, parent, type, jsonParams);
    }

    // Protected Methods

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected void cancelPressed() {
        super.okPressed();
        getPresenter().publish(new ToolAction(
                ToolAction.RecommenderActionEnum.RECOMMENDER_RESULTS_DISPLAY_COMPLETE,
                null, getType(), getState(), null));
    }
}
