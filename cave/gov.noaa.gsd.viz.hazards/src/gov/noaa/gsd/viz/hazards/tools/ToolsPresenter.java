/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.tools;

import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.IHazardServicesModel;
import gov.noaa.gsd.viz.hazards.display.IHazardServicesModel.Element;

import java.util.EnumSet;

import com.google.common.eventbus.EventBus;

/**
 * Settings presenter, used to mediate between the model and the settings view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle,
 *                                           including the passing in of the event
 *                                           bus so that the latter is no longer a
 *                                           singleton.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ToolsPresenter extends HazardServicesPresenter<IToolsView<?, ?>> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param view
     *            Tools view to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public ToolsPresenter(IHazardServicesModel model, IToolsView<?, ?> view,
            EventBus eventBus) {
        super(model, view, eventBus);
    }

    // Public Methods

    /**
     * Receive notification of a model change.
     * 
     * @param changes
     *            Set of elements within the model that have changed.
     */
    @Override
    public void modelChanged(EnumSet<Element> changed) {
        if (changed.contains(IHazardServicesModel.Element.TOOLS)) {
            getView().setTools(getModel().getToolList());
        }
    }

    /**
     * Show a tool subview that is used to gather parameter values for a tool
     * that is to be executed.
     * 
     * @param toolName
     *            Name of the tool for which parameters are to be gathered.
     * @param jsonParams
     *            JSON string giving the parameters for this subview. Within the
     *            set of all fields that are defined by these parameters, all
     *            the fields (megawidget specifiers) must have unique
     *            identifiers.
     */
    public final void showToolParameterGatherer(String toolName,
            String jsonParams) {
        getView().showToolParameterGatherer(toolName, jsonParams);
    }

    // Protected Methods

    /**
     * Initialize the specified view in a subclass-specific manner.
     * 
     * @param view
     *            View to be initialized.
     */
    @Override
    protected void initialize(IToolsView<?, ?> view) {
        view.initialize(this, getModel().getToolList());
    }
}
